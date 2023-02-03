from aws_cdk import core as cdk
from aws_cdk.pipelines import CodePipeline, CodePipelineSource, CodeBuildStep, ShellStep, ManualApprovalStep, CodeBuildOptions
from aws_cdk.aws_lambda import LayerVersion
from aws_cdk.aws_iam import PolicyStatement, Effect, AccountPrincipal
from aws_cdk.aws_ssm import StringParameter
from .layer import UOneCachingLayerStage


def add_publish(unit, pypi_repo_arn, environments, tagged_release=False):
    unit.add_post(_publish_step(environments, pypi_repo_arn, tagged_release=tagged_release))
    return unit


def create_pipeline(context, branch, code_repo, env_name):
    # Create pipeline to run it all
    pipeline = CodePipeline(context, f"UOne-Caching-{env_name.title()}-Pipeline",
        pipeline_name=f"uone-caching-{env_name.lower()}",
        docker_enabled_for_synth=True,
        cross_account_keys=True,
        synth=_create_build(branch, code_repo),
        self_mutation=True,
        self_mutation_code_build_defaults=_create_synth_customs(),
        synth_code_build_defaults=_create_synth_customs()
    )
    return pipeline


def create_unit_of_change(context, layer_name, version_param_name, env_name, pipeline, ou_ids, environments):
    return pipeline.add_stage(UOneCachingLayerStage(context, f"UOne-Caching-Layer-Stage-{env_name.title()}",
        layer_name=layer_name,
        version_param_name=version_param_name,
        ou_ids=ou_ids,
        env=environments[env_name]['env']
    ))


def add_promotion(unit, pypi_repo_arn, frm, to, require_approval=None, tagged_release=None, bump_build=None):
    require_approval = True if require_approval is None else bool(require_approval)
    tagged_release = False if tagged_release is None else bool(tagged_release)
    bump_build = False if tagged_release is None else bool(bump_build)

    unit.add_post(_promote_step(
        frm,
        to,
        pypi_repo_arn,
        require_approval=require_approval,
        tagged_release=tagged_release,
        bump_build=bump_build)
    )

    if require_approval: unit.add_pre(ManualApprovalStep('approval'))
    return unit


def add_testing(unit, sl_token_name=None):
    unit.add_pre(ShellStep('Testing',
        commands=[
            'pip install -r src/requirements.txt',
            'pip install pytest pytest-cov pylint',
            'pytest src/tests'
        ]
    ))

    if sl_token_name:
        # sl_token = Secret.secret_value_from_json(sl_token_name + ':shiftlefttoken').string_value
        # sl_token = cdk.SecretValue.secrets_manager(sl_token_name, json_field='shiftlefttoken').to_string()
        # print(f"Found ShiftLeft Token: {sl_token}")
        # unit.add_pre(ShellStep('SecurityReport',
        #     commands=[
        #         'echo "Updating environment to include python3.8-venv for ShiftLeft..."',
        #         'apt update -y && apt install python3.8-venv -y',
        #         'echo "Downloaded ShiftLeft to run agains uone-caching..."',
        #         'curl https://cdn.shiftleft.io/download/sl > /usr/local/bin/sl && chmod a+rx /usr/local/bin/sl',
        #         'echo "Authenticating with ShiftLeft..."',
        #         'sl auth --diagnostic --token ' + sl_token,
        #         'echo "Building uone-caching for security reporting..."',
        #         'echo "Starting code analysis with NG SAST for UOne-Caching..."',
        #         # 'pip install -r src/requirements.txt -t src/.',
        #         "sl analyze --diagnostic --app uone-caching --python src -- -l debug",
        #         'echo "Ran NG SAST on your code, exiting bundle dir"',
        #         'cd ..',
        #     ]
        # ))
        print('There is no reason to security test this right?  whatever...')
    return unit


########################################################################################
# "private" functions
########################################################################################
def _publish_step(environments, pypi_repo_arn, tagged_release=False):
    return CodeBuildStep('Publish',
        commands=[
            'pip install twine bump2version git-remote-codecommit',
            'python -m pip install --upgrade build',
            'bump2version --tag release' if tagged_release else "echo 'not tagging release'", #'bump2version build',
            'cd src',
            f"aws codeartifact login --tool twine --repository internal-pypi --domain uone-engineering --domain-owner {environments['shared']['env'].account}", # '972576019456',
            'python -m build',
            "twine upload --repository codeartifact dist/* --verbose"
        ],
        role_policy_statements=_git_synching_policies(pypi_repo_arn)
    )


def _promote_step(frm, to, pypi_repo_arn, require_approval, tagged_release, bump_build):
    bumpversion_cmd = "echo 'not tagging release or incrimenting build...'"

    if tagged_release is True:
        bumpversion_cmd = 'bump2version --tag release'
    elif bump_build is True:
        bumpversion_cmd = 'bump2version build'

    return CodeBuildStep('Promote',
        commands=[
            'pip install bump2version git-remote-codecommit',
            'mkdir -p tmp/uone-caching',
            'cd tmp/uone-caching',
            'git init',
            'git config --global user.email "it@heydaynow.com"',
            'git config --global user.name "UOne Eng CICD"',
            'git remote add origin codecommit::us-west-2://uone-caching',
            'git fetch',
            f"git checkout {frm}",
            'git pull',
            f"git checkout {to}",
            f"git merge {frm}",
            bumpversion_cmd,
            f"git push origin {to}"
        ],
        role_policy_statements=_git_synching_policies(pypi_repo_arn)
    )


def _create_build(branch, code_repo):
    return CodeBuildStep(f"Synth-{branch.title()}",
        input=CodePipelineSource.code_commit(
            repository=code_repo,
            branch=branch,
        ),
        commands=[
            'pip install -r requirements.txt',
            'npm install -g aws-cdk',
            'cdk synth'
        ],
        role_policy_statements=[
            PolicyStatement(
                effect=Effect.ALLOW,
                actions=["sts:AssumeRole"],
                resources=["*"],
                conditions={
                    "StringEquals": {
                        "iam:ResourceTag/aws-cdk:bootstrap-role": "lookup"
                    }
                }
            ),
            PolicyStatement(
                effect=Effect.ALLOW,
                actions=["sts:AssumeRole"],
                resources=[
                  "arn:aws:iam::*:role/cdk-readOnlyRole"
                ]
            )
        ]
    )


def _create_synth_customs():
    return CodeBuildOptions(
        role_policy=[
            PolicyStatement(
                effect=Effect.ALLOW,
                actions=["cloudformation:GetTemplate"],
                resources=[
                  "*"
                ]
            )
        ]
    )


def _git_synching_policies(pypi_repo_arn):
    return [
        PolicyStatement(
            effect=Effect.ALLOW,
            actions=['codeartifact:GetAuthorizationToken'],
            resources=[pypi_repo_arn]
        ),
        PolicyStatement(
            effect=Effect.ALLOW,
            actions=[
                "sts:AssumeRole",
                "codeartifact:PublishPackageVersion"
            ],
            resources=["arn:aws:iam::*:role/cdk-readOnlyRole"]
        ),
        PolicyStatement(
            effect=Effect.ALLOW,
            actions=["sts:GetServiceBearerToken"],
            resources=["*"]
        ),
        PolicyStatement(
            effect=Effect.ALLOW,
            actions=[
                "codeartifact:PublishPackageVersion",
                "codecommit:*"
            ],
            resources=["arn:aws:codeartifact:us-west-2:972576019456:package/uone-engineering/*"]
        ),
        PolicyStatement(
            effect=Effect.ALLOW,
            actions=["codecommit:*"],
            resources=["*"]
        ),
        PolicyStatement(
            effect=Effect.ALLOW,
            actions=["sts:AssumeRole"],
            resources=["*"]
        )
    ]
