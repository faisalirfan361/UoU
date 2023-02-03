import os, json, datetime
import pytest
from unittest.mock import patch, Mock
from uone_data_scraping.configs import templates as tmp
from uone_caching.item_types.object_item import ObjectItem


obj_1 = ObjectItem(key='scraping_template', client='uone', source='nic', data={'template': '{"fake":"template"}'})
obj_1.reset_id('fake-template')
obj_2 = ObjectItem(key='scraping_template', client='uone', source='nic', data={'template': '{"fake2":"some-entirely-different-template"}'})
obj_2.reset_id('other-fake-template')
@patch('uone_data_scraping.configs.templates.QueryEngine.get', return_value=[obj_1, obj_2])
def test_config_templates_returns_all_nic_with_source(cqe):
	res = tmp.config_templates('nic')
	expected = {
		'fake-template': {'fake': 'template'},
		'other-fake-template': {'fake2': 'some-entirely-different-template'}
	}

	assert res == expected


obj_1 = ObjectItem(key='scraping_template', client='uone', source='nic', data={'template': '{"fake":"template"}'})
obj_1.reset_id('fake-template')
obj_2 = ObjectItem(key='scraping_template', client='uone', source='nic', data={'template': '{"fake2":"some-entirely-different-template"}'})
obj_2.reset_id('other-fake-template')
@patch('uone_data_scraping.configs.templates.QueryEngine.get', return_value=[obj_1, obj_2])
def test_config_templates_can_regex_pattern_match(cqe):
	res = tmp.config_templates('nic', client='uone', pattern='.*', cache_access_key='template')
	expected = {
		'fake-template': {'fake': 'template'},
		'other-fake-template': {'fake2': 'some-entirely-different-template'}
	}

	assert res == expected


obj_1 = ObjectItem(key='scraping_template', client='uone', source='nic', data={'template': '{"fake":"template"}'})
obj_1.reset_id('fake-template')
obj_2 = ObjectItem(key='scraping_template', client='uone', source='nic', data={'template': '{"fake2":"some-entirely-different-template"}'})
obj_2.reset_id('other-fake-template')
@patch('uone_data_scraping.configs.templates.QueryEngine.get', return_value=[obj_1, obj_2])
def test_config_templates_can_use_start_instead_of_dot_start(cqe):
	res = tmp.config_templates('nic', client='uone', pattern='*', cache_access_key='template')
	expected = {
		'fake-template': {'fake': 'template'},
		'other-fake-template': {'fake2': 'some-entirely-different-template'}
	}

	assert res == expected


obj_1 = ObjectItem(key='scraping_template', client='uone', source='nic', data={'template': '{"fake":"template"}'})
obj_1.reset_id('fake-template')
obj_2 = ObjectItem(key='scraping_template', client='uone', source='nic', data={'template': '{"fake2":"some-entirely-different-template"}'})
obj_2.reset_id('other-fake-template')
@patch('uone_data_scraping.configs.templates.QueryEngine.get', return_value=[obj_1, obj_2])
def test_config_templates_returns_empty_dict_with_no_match(cqe):
	res = tmp.config_templates('nic', client='uone', pattern='if-this-matches-anything-we-are-screwed', cache_access_key='template')
	expected = {}

	assert res == expected


obj_1 = ObjectItem(key='scraping_template', client='uone', source='nic', data={'template': '{"fake":"template"}'})
obj_1.reset_id('fake-template')
obj_2 = ObjectItem(key='scraping_template', client='uone', source='nic', data={'template': '{"fake2":"some-entirely-different-template"}'})
obj_2.reset_id('other-fake-template')
@patch('uone_data_scraping.configs.templates.QueryEngine.get', return_value=[obj_1, obj_2])
def test_config_templates_properly_filters(cqe):
	res = tmp.config_templates('nic', client='uone', pattern='other', cache_access_key='template')
	expected = {
		'other-fake-template': {'fake2': 'some-entirely-different-template'}
	}

	assert res == expected


@patch('uone_data_scraping.configs.templates.QueryEngine')
def test_config_templates_calls_QueryEngine_properly(cqe):
	tmp.config_templates('nic')
	cqe.assert_called_with(key='template', source='nic', client='uone')


# def test_config_templates_returns_correct_one_with_pattern():
# 	res = tmp.config_templates('nic', 'agent-states')
# 	assert res['agent-states'] == "make sure it's a valid request and that it only returns the things with that pattern"

