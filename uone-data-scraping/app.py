#!/usr/bin/env python
# ^^ set your python up correctly or change the line up above to point
# at your Python 3 command.  if you don't know what "correctly" means,
# check the confluence docs for developer setup for Pyenv.
import os
from constructs import Construct
from aws_cdk import App, Environment, Tags # core constructs
from infrastructure.project import UOneDataScrapingProject


app = App()

pypi_repo_arn = app.node.try_get_context('pypi-repo-arn')
sl_token_name = app.node.try_get_context('sl_token_name')
slack_workspace_id = app.node.try_get_context('slack-workspace-id')
slack_channel_name = app.node.try_get_context('slack-channel-name')
slack_channel_id = app.node.try_get_context('slack-channel-id')

# layer_version = sorted([int(v['Version']) for v in boto3.client('lambda').list_layer_versions(LayerName=layer_name)['LayerVersions']])[-1]

if pypi_repo_arn is None:
    print('Internal PyPi repo ARN being defaulted to "arn:aws:codeartifact:us-west-2:972576019456:domain/uone-engineering"' )
    pypi_repo_arn = 'arn:aws:codeartifact:us-west-2:972576019456:domain/uone-engineering'

if sl_token_name is None:
    print('ShiftLeft auth token will be retrieved from the default secret at "shiftleft-credentials-CICD"')
    sl_token_name = 'shiftleft-credentials-CICD'

if slack_workspace_id is None:
    slack_workspace_id = 'T01EVR71X4G'

if slack_channel_id is None:
    slack_channel_id =  'G01P0N2KE6S'

if slack_channel_name is None:
    slack_channel_name = 'development' #"G01P0N2KE6S",

environments = {
    # 'demo': cdk.Environment(account='697930627381', region='us-west-2'),
    'dev': Environment(account='379516241584', region='us-west-2'),
    # 'prod': cdk.Environment(account='817604536927', region='us-west-2')
    # 'qa': cdk.Environment(account='', region='us-west-2')
    'shared': Environment(account='972576019456', region='us-west-2'),
    'sbx': Environment(account='214563902886', region='us-west-2'),
    'sbx-micro': Environment(account='677179051929', region='us-west-2'),
    'sbx-1': Environment(account='143936714092', region='us-west-2')
}

ou_ids = {
    # 'sdlc-ou': app.node.try_get_context('sdlc') or 'ou-igag-7zegmuso',
    # 'sandboxes': app.node.try_get_context('sandboxes') or 'ou-igag-ezuna6im',
    'sdlc': app.node.try_get_context('sdlc') or 'o-7m462u5leo'
}

shared_acct = app.node.try_get_context('shared') or '972576019456'


scraping_pkg = UOneDataScrapingProject(app, "UOne-Data-Scraping-Project",
    layer_basename='uone-data-scraping-layer',
    version_param_name='UOne-Data-Scraping-Layer-ARN-Latest',
    ou_ids=ou_ids,
    pypi_repo_arn=pypi_repo_arn,
    sl_token_name=sl_token_name,
    slack_channel_id=slack_channel_id,
    slack_channel_name=slack_channel_name,
    slack_workspace_id=slack_workspace_id,
    env=environments['shared'],
    environments=environments
)

Tags.of(scraping_pkg).add('cost-center', 'Engineering')

app.synth()
