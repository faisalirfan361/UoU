from aws_cdk import core as cdk
from aws_cdk.aws_codecommit import Repository
from typing import Dict, List, Any
from aws_cdk.aws_iam import AccountPrincipal
from aws_cdk.aws_ssm import StringParameter
from .pipeline import create_pipeline, add_promotion, add_testing, create_unit_of_change, add_publish


class UOneCachingProject(cdk.Stack):
    def __init__(self, scope: cdk.Construct, _id: str, environments: Dict[str,Dict[str, Any]],
                 layer_basename: str, ou_ids: List[str], pypi_repo_arn: str, version_param_name: str,
                 env: cdk.Environment, sl_token_name: str, **kwargs) -> None:
        super().__init__(scope, _id, env=env, **kwargs)

        code_repo = Repository.from_repository_name(self, 'UOne-Caching-Code-Repo-Ref', repository_name='uone-caching')
        code_repo.grant_pull_push(AccountPrincipal(environments['shared']['env'].account))
        code_repo.grant_read(AccountPrincipal(environments['shared']['env'].account))

        # Create operations off the "sbx-micro" branch-environment pair
        sbx_micro_pipeline = create_pipeline(self, branch='sbx-micro', code_repo=code_repo, env_name='sbx-micro')
        sbx_micro = create_unit_of_change(self,
            layer_name=f"{layer_basename}-sbx-micro",
            version_param_name=version_param_name,
            env_name='sbx-micro',
            pipeline=sbx_micro_pipeline,
            ou_ids=ou_ids,
            environments=environments
        )
        sbx_micro = add_testing(unit=sbx_micro, sl_token_name=sl_token_name)
        sbx_micro = add_publish(
            unit=sbx_micro,
            pypi_repo_arn=pypi_repo_arn,
            environments=environments,
            tagged_release=False
        )
        sbx_micro = add_promotion(
            unit=sbx_micro,
            pypi_repo_arn=pypi_repo_arn,
            frm='sbx-micro',
            to='development',
            require_approval=False,
            tagged_release=False, # possibly change this to true after launch
            bump_build=self._should_bump_version('sbx-micro', environments)
        )

        sbx_1_pipeline = create_pipeline(self, branch='sbx-1', code_repo=code_repo, env_name='sbx-1')
        sbx_1 = create_unit_of_change(self,
            layer_name=f"{layer_basename}-sbx-1",
            version_param_name=version_param_name,
            env_name='sbx-1',
            pipeline=sbx_1_pipeline,
            ou_ids=ou_ids,
            environments=environments
        )
        sbx_1 = add_testing(unit=sbx_1, sl_token_name=sl_token_name)
        sbx_1 = add_publish(
            unit=sbx_1,
            pypi_repo_arn=pypi_repo_arn,
            environments=environments,
            tagged_release=False
        )

        # Create operations off the "development" branch-environment pair
        dev_pipeline = create_pipeline(self, branch='development', code_repo=code_repo, env_name='dev')
        dev = create_unit_of_change(self,
            layer_name=f"{layer_basename}-dev",
            version_param_name=version_param_name,
            env_name='dev',
            pipeline=dev_pipeline,
            ou_ids=ou_ids,
            environments=environments
        )
        dev = add_testing(unit=dev, sl_token_name=sl_token_name)
        dev = add_publish(
            unit=dev,
            pypi_repo_arn=pypi_repo_arn,
            environments=environments,
            tagged_release=False
        )
        # dev = add_promotion(unit=dev,
        #     pypi_repo_arn=pypi_repo_arn,
        #     frm='development',
        #     to='master',
        #     require_approval=True,
        #     tagged_release=True,
        #     bump_build=self._should_bump_version('dev', environments)
        # )

    def _should_bump_version(self, env_name, environments):
        # rank is [0, infinity], where 0 is the top ranking environment
        weakest_rank_level = sorted(map(lambda e: e['rank'], environments.values()), reverse=True)[0]
        this_rank_level = environments.get(env_name, {}).get('rank', 999999999)

        return bool(this_rank_level > weakest_rank_level)
