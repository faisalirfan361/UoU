#!/usr/bin/env python
import os
from aws_cdk import core as cdk
from infrastructure.project import UOneCachingProject


app = cdk.App()

pypi_repo_arn = app.node.try_get_context('pypi-repo-arn')
sl_token_name = app.node.try_get_context('sl_token_name')

if pypi_repo_arn is None:
    print('Internal PyPi repo ARN being defaulted to "arn:aws:codeartifact:us-west-2:972576019456:domain/uone-engineering"' )
    pypi_repo_arn = 'arn:aws:codeartifact:us-west-2:972576019456:domain/uone-engineering'

if sl_token_name is None:
    print('ShiftLeft auth token will be retrieved from the default secret at "shiftleft-credentials-CICD"')
    sl_token_name = 'shiftleft-credentials-CICD'

environments = {
    # 'demo': cdk.Environment(account='697930627381', region='us-west-2'),
    'dev': {
        'env': cdk.Environment(account='379516241584', region='us-west-2'),
        'rank': 5
    },
    # 'prod': cdk.Environment(account='817604536927', region='us-west-2')
    # 'qa': cdk.Environment(account='', region='us-west-2')
    'shared': {
        'env': cdk.Environment(account='972576019456', region='us-west-2'),
        'rank': 0
    },
    'sbx': {
        'env': cdk.Environment(account='214563902886', region='us-west-2'),
        'rank': 9
    },
    'sbx-micro': {
        'env': cdk.Environment(account='677179051929', region='us-west-2'),
        'rank': 9
    },
    'sbx-1': {
        'env': cdk.Environment(account='143936714092', region='us-west-2'),
        'rank': 9
    }
}

ou_ids = {
    # 'sdlc-ou': app.node.try_get_context('sdlc') or 'ou-igag-7zegmuso',
    # 'sandboxes': app.node.try_get_context('sandboxes') or 'ou-igag-ezuna6im',
    'sdlc': app.node.try_get_context('sdlc') or 'o-7m462u5leo'
}

caching_project = UOneCachingProject(app, "UOne-Caching-CICD",
    layer_basename='uone-caching-layer',
    version_param_name='UOne-Caching-Layer-ARN-Latest',
    ou_ids=ou_ids,
    pypi_repo_arn=pypi_repo_arn,
    sl_token_name=sl_token_name,
    env=environments['shared']['env'],
    environments=environments
)

cdk.Tags.of(caching_project).add('cost-center', 'Engineering')
cdk.Tags.of(caching_project).add('project', 'uone-caching')
cdk.Tags.of(caching_project).add('functionality', 'DataIngest')
app.synth()
