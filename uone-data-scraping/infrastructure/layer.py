import os
from os import path
from typing import Dict
from constructs import Construct
from aws_cdk import Stack, Environment, SecretValue, Stage, CfnOutput, BundlingOptions, DockerImage
from aws_cdk.aws_lambda import Runtime, Code, LayerVersion, LayerVersionPermission
from aws_cdk.aws_ssm import StringParameter


class UOneDataScrapingLayerStack(Stack):
    def __init__(self, scope: Construct, _id: str, layer_name: str, ou_ids: str,
                 version_param_name: str, env: Environment, **kwargs) -> None:
        super().__init__(scope, _id, env=env, **kwargs)

        # Create Lambda Layer
        new_version = LayerVersion(self, 'UOne-Data-Scraping-Layer',
            layer_version_name=layer_name,
            description='uone_data_scraping as a layer',
            code=Code.from_docker_build(
                path=os.path.abspath("./"),
                file='Dockerfile',
                build_args={'target':'layer'}
            ),
            compatible_runtimes=[Runtime.PYTHON_3_6, Runtime.PYTHON_3_7, Runtime.PYTHON_3_8],
        )

        for org_name,org_id in ou_ids.items():
            new_version.add_permission(f"allow-{org_name}-org",
                account_id='*',
                organization_id=org_id
            )

        param = StringParameter(self, 'UOne-Data-Scraping-Layer-Arn',
            parameter_name=version_param_name,
            string_value=new_version.layer_version_arn
        )


class UOneDataScrapingLayerStage(Stage):
    def __init__(self, scope: Construct, _id: str, layer_name: str, ou_ids: str,
                 version_param_name: str, env: Environment, **kwargs) -> None:
        super().__init__(scope, _id, env=env, **kwargs)

        layer = UOneDataScrapingLayerStack(self, 'UOne-Data-Scraping-Layer-Stack',
            layer_name=layer_name,
            version_param_name=version_param_name,
            ou_ids=ou_ids,
            env=env
        )
