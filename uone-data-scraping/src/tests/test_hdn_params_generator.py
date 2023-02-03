import os, json
import pytest
from unittest.mock import patch, Mock
from uone_data_scraping import params_generator as pgen
from uone_data_scraping.configs import templates
# import uone_data_scraping.params_generator as pgen


def large_exp_stub():
	return {'client': 'sner', 'source': 'nice-in-contact', 'start_date': '2021-01-01', 'end_date': '2021-01-30'}


def small_exp_stub():
	return {'client': 'sner', 'source': 'nice-in-contact', 'start_date': '2021-01-01', 'end_date': '2021-01-01'}


@patch.dict(os.environ, {"STAGING_BUCKET": "s3://some-staging-bucket-name"})
def test_get_generator_raises_ModuleNotFoundError_if_wrong_key():
	try:
		pgen.get_generator('borked!')
		assert False, 'params_generator.get_generator() does not raise error when generator does not exist'
	except ModuleNotFoundError:
		assert True, 'params_generator.get_generator() raises proper error when generator key does not exist'
	except Exception:
		assert False, 'params_generator.get_generator() does not raise error when generator does not exist'


@patch.dict(os.environ, {"STAGING_BUCKET": "s3://some-staging-bucket-name"})
def test_get_generator_returns_nic_for_nic_key():
	gen = pgen.get_generator('nic')

	assert gen.__spec__.name == 'uone_data_scraping.params_generator.nic.api'


# # @patch.dict(os.environ, {"STAGING_BUCKET": "s3://some-staging-bucket-name"})
# @patch('uone_data_scraping.params_generator.nic.api.templates.QueryEngine.get')
# def test_targets_for_returns_nic_targets_for_nic_source(ok):
# 	# assert pgen.targets('nic') == templates.config_templates('nic')
# 	import pdb
# 	pdb.set_trace()
# 	ok.assert_called_once()


@patch.dict(os.environ, {"STAGING_BUCKET": "s3://some-staging-bucket-name"})
@patch('uone_data_scraping.params_generator.get_generator', spec=pgen.get_generator, return_value=pgen.get_generator('nic'))
def test_config_for_gets_proper_generator(gg):
	try:
		pgen.configs_for('nic', 'agent-states', {})
	except Exception as ex:
		if type(ex).__name__ in ['ProfileNotFound', 'NoRegionError', 'ClientError']: # botocore exceptions
			gg.assert_called_with('nic')
		else:
			raise ex


@patch.dict(os.environ, {"STAGING_BUCKET": "s3://some-staging-bucket-name"})
@patch('uone_data_scraping.configs.templates.QueryEngine.get')
@patch('uone_data_scraping.params_generator.nic.api._configs_for_time')
@patch('uone_data_scraping.params_generator.nic.api._configs_for_id')
@patch('uone_data_scraping.params_generator.nic.api._configs_for_agent')
def test_config_for_getter_finds_endpoint_style(agent, eye_d, time, boogs):
	for ep in templates.target_names('nic'):
		conf = pgen.configs_for('nic', ep, {})

		if conf.bucketed_by is None:
			pass
		elif conf.bucketed_by == 'time':
			time.assert_called()
		elif conf.bucketed_by == 'id':
			eye_d.assert_called()
		elif conf.bucketed_by == 'agent':
			agent.assert_called()
		# else:
		# 	assert False, f"found no bucketing in test for {ep}"

# def test_configs_for_returns_list():
# 	# this might be a pain...
