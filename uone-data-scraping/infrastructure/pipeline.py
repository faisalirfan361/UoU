from constructs import Construct
from aws_cdk import Stack, Environment, SecretValue
from aws_cdk.pipelines import CodePipeline, CodePipelineSource, CodeBuildStep, ShellStep, ManualApprovalStep, CodeBuildOptions
from aws_cdk.aws_lambda import LayerVersion
from aws_cdk.aws_iam import PolicyStatement, Effect, AccountPrincipal
from .layer import UOneDataScrapingLayerStage


def add_publish(unit, pypi_repo_arn, environments):
    unit.add_post(_publish_step(environments, pypi_repo_arn, tagged_release=False))
    return unit


def create_pipeline(context, branch, code_repo, env_name=None):
    # Create pipeline to run it all
    return CodePipeline(context, f"UOne-Data-Scraping-{env_name.title()}-Pipeline",
        pipeline_name=f"uone-data-scraping-{env_name.lower()}",
        docker_enabled_for_synth=True,
        cross_account_keys=True,
        synth=_create_build(branch, code_repo),
        self_mutation=True,
        self_mutation_code_build_defaults=_create_synth_customs(),
        synth_code_build_defaults=_create_synth_customs()
    )


def create_unit_of_change(context, layer_name, version_param_name, env_name, pipeline, ou_ids, environments):
    # Add lambda layer
    return pipeline.add_stage(UOneDataScrapingLayerStage(context, f"UOne-Data-Scraping-Layer-{env_name.title()}",
        layer_name=layer_name,
        version_param_name=version_param_name,
        ou_ids=ou_ids,
        env=environments[env_name]
    ))


def add_promotion(unit, pypi_repo_arn, frm, to, tagged_release=False, add_approval=True):
    if add_approval:
        unit.add_pre(ManualApprovalStep('Promotion-Approval'))
    unit.add_post(_promote_step(frm, to, pypi_repo_arn, tagged_release))
    return unit


def add_testing(unit, pypi_repo_arn, sl_token_name=None):
    unit.add_pre(CodeBuildStep('Testing',
        commands=[
            'pip install pytest pytest-cov pylint',
            'aws codeartifact login --tool pip --repository internal-pypi --domain uone-engineering --domain-owner 972576019456',
            'pip install -r local/local.requirements.txt',
            'pytest src/tests'
        ],
        role_policy_statements=_codeartifact_policies(pypi_repo_arn)
    ))

    if sl_token_name is not None:
        # sl_token = Secret.secret_value_from_json(sl_token_name + ':shiftlefttoken').string_value
        sl_token = SecretValue.secrets_manager(sl_token_name, json_field='shiftlefttoken').to_string()
        print(f"Found ShiftLeft Token: {sl_token}")
        unit.add_pre(ShellStep('SecurityReport',
            commands=[
                'echo "Updating environment to include python3.8-venv for ShiftLeft..."',
                'apt update -y && apt install python3.8-venv -y',
                'echo "Downloaded ShiftLeft to run agains uone-data-scraping..."',
                'curl https://cdn.shiftleft.io/download/sl > /usr/local/bin/sl && chmod a+rx /usr/local/bin/sl',
                'echo "Authenticating with ShiftLeft..."',
                'sl auth --diagnostic --token ' + sl_token,
                'echo "Building uone-data-scraping for security reporting..."',
                'echo "Starting code analysis with NG SAST for UOne-Data-Scraping..."',
                # 'pip install -r src/requirements.txt -t src/.',
                "sl analyze --diagnostic --app uone-data-scraping --python src -- -l debug",
                'echo "Ran NG SAST on your code, exiting bundle dir"',
                'cd ..',
            ]
        ))

    return unit


########################################################################################
# "private" functions
########################################################################################
def _publish_step(environments, pypi_repo_arn, tagged_release=False):
    return CodeBuildStep('Publish',
        commands=[
            'pip install twine bump2version git-remote-codecommit',
            'python -m pip install --upgrade build',
            'bump2version --tag release' if tagged_release else 'bump2version build',
            'cd src',
            f"aws codeartifact login --tool pip --repository internal-pypi --domain uone-engineering --domain-owner {environments['shared'].account}",
            'python -m build',
            f"aws codeartifact login --tool twine --repository internal-pypi --domain uone-engineering --domain-owner {environments['shared'].account}", # '972576019456',
            "twine upload --repository codeartifact dist/* --verbose"
        ],
        role_policy_statements=_git_synching_policies(pypi_repo_arn)
    )


def _promote_step(frm, to, pypi_repo_arn, tagged_release):
    return CodeBuildStep('Promote',
        commands=[
            'pip install bump2version git-remote-codecommit',
            'mkdir -p tmp/uone-data-scraping',
            'cd tmp/uone-data-scraping',
            'git init',
            'git config --global user.email "it@heydaynow.com"',
            'git config --global user.name "UOne Eng CICD"',
            'git remote add origin codecommit::us-west-2://uone-data-scraping',
            'git fetch',
            f"git checkout {frm}",
            'git pull',
            f"git checkout {to}",
            'git pull',
            f"git merge {frm}",
            'bump2version --tag release' if tagged_release else 'bump2version build',
            f"git push origin {to}"
        ],
        role_policy_statements=_git_synching_policies(pypi_repo_arn)
    )


def _create_build(branch, code_repo):
    return CodeBuildStep(f"Synth-{branch[:3].title()}",
        input=CodePipelineSource.code_commit(
            repository=code_repo,
            branch=branch,
        ),
        commands=[
            'pip install -r requirements.txt',
            'npm install -g aws-cdk@2.14.0',
            'cdk context --clear',
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
                resources=["*"],
                conditions={
                    "StringEquals": {
                        "iam:_resource_tag/aws-cdk:bootstrap-role": "lookup"
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


def _codeartifact_policies(pypi_repo_arn):
    return [
        PolicyStatement(
            effect=Effect.ALLOW,
            actions=['codeartifact:GetAuthorizationToken'],
            resources=[pypi_repo_arn]
        ),
        PolicyStatement(
            effect=Effect.ALLOW,
            actions=[
                "codeartifact:PublishPackageVersion",
                "codecommit:*"
            ],
            resources=[pypi_repo_arn]
        ),
        PolicyStatement(
            effect=Effect.ALLOW,
            actions=["sts:GetServiceBearerToken"],
            resources=["*"]
        )
    ]


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
