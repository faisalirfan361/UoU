from typing import Dict, List
from constructs import Construct
from aws_cdk import Stack, Environment
from aws_cdk.aws_codecommit import Repository
from aws_cdk.aws_iam import AccountPrincipal
import aws_cdk.aws_chatbot as chatbot
from .pipeline import create_pipeline, add_promotion, add_testing, create_unit_of_change, add_publish


class UOneDataScrapingProject(Stack):
    def __init__(self, scope: Construct, _id: str, environments: Dict[str,Environment],
                 layer_basename: str, ou_ids: List[str], pypi_repo_arn: str, version_param_name: str,
                 slack_workspace_id: str, slack_channel_id: str, slack_channel_name: str,
                 env: Environment, sl_token_name: str, **kwargs) -> None:
        super().__init__(scope, _id, env=env, **kwargs)

        # Create a git code repo to interact with
        code_repo = Repository.from_repository_name(self, 'UOne-Data-Scraping-Code-Repo-Ref', repository_name='uone-data-scraping')
        code_repo.grant_pull_push(AccountPrincipal(environments['shared'].account))
        code_repo.grant_read(AccountPrincipal(environments['shared'].account))

        # Create slack notification
        target = chatbot.SlackChannelConfiguration(self, "UOne-Data-Scraping-Slack-Channel",
            slack_workspace_id=slack_workspace_id,
            slack_channel_configuration_name=slack_channel_name,
            slack_channel_id=slack_channel_id
        )
        rule = code_repo.notify_on_pull_request_created("UOne-Data-Scraping-NotifyOnPullRequestCreated", target)

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
        sbx_micro = add_testing(unit=sbx_micro, pypi_repo_arn=pypi_repo_arn)
        sbx_micro = add_publish(
            unit=sbx_micro,
            pypi_repo_arn=pypi_repo_arn,
            environments=environments
        )
        # make add_approval=True after major sandboxing
        # sbx_micro = add_promotion(sbx_micro, pypi_repo_arn, frm='sbx-micro', to='development', tagged_release=False, add_approval=True)

        # Sandbox 1
        sbx_1_pipeline = create_pipeline(self, branch='sbx-1', code_repo=code_repo, env_name='sbx-1')
        sbx_1 = create_unit_of_change(self,
            layer_name=f"{layer_basename}-sbx-1",
            version_param_name=version_param_name,
            env_name='sbx-1',
            pipeline=sbx_1_pipeline,
            ou_ids=ou_ids,
            environments=environments
        )
        sbx_1 = add_testing(unit=sbx_1, pypi_repo_arn=pypi_repo_arn)
        sbx_1 = add_publish(
            unit=sbx_1,
            pypi_repo_arn=pypi_repo_arn,
            environments=environments
        )
        # make add_approval=True after major sandboxing
        # sbx_1 = add_promotion(sbx_1, pypi_repo_arn, frm='sbx-1', to='sbx-micro', tagged_release=False, add_approval=True)

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
        dev = add_testing(unit=dev, pypi_repo_arn=pypi_repo_arn, sl_token_name=sl_token_name)
        dev = add_publish(
            unit=dev,
            pypi_repo_arn=pypi_repo_arn,
            environments=environments
        )
        #dev = add_promotion(dev, pypi_repo_arn, frm='development', to='master', tagged_release=True)

        # Create operations off the "qa" branch-environment pair
        # qa_pipeline = create_pipeline('stage', code_repo)
        # qa = create_unit_of_change('master', 'qa', qa_pipeline, org_name, org_id, environments)
        # qa = add_promotion(qa_stage, to='prod')

        # prod_pipeline = create_pipeline(self, 'master', code_repo, env_name='prod')
        # prod = create_unit_of_change(self, 'master', 'prod', prod_pipeline, org_name, org_id, environments)
        # prod = add_publish('master', prod, pypi_repo_arn, environments, require_approval=True)
