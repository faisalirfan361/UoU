import os
import requests
import pytest
from unittest.mock import patch, Mock
from uone_data_scraping.scrapers.nic import scraper as nscraper
from uone_data_scraping.configs import templates
from uone_data_scraping.processing import dotmap
# from uone_data_scraping.scrapers.nic import scraper as nscraper
# from uone_data_scraping.configs import templates, dotmap


def large_exp_stub():
	# return {'source': 'nice-in-contact', 'startDate': '2021-01-01', 'endDate': '2021-01-30'}
	conf = templates._nic_configs()['schedule-adherence']
	conf['params']['startDate'] = '2021-01-01'
	conf['params']['endDate'] = '2021-01-30'

	return conf


def small_exp_stub():
	# return {'source': 'nice-in-contact', 'startDate': '2021-01-01', 'endDate': '2021-01-01'}
	conf = templates._nic_configs()['schedule-adherence']
	conf['params']['startDate'] = '2021-01-01'
	conf['params']['endDate'] = '2021-01-01'

	return conf


@patch('uone_data_scraping.utils.helpers._get_secret', return_value='faking-stuff')
@patch('uone_data_scraping.scrapers.nic.scraper.requests', spec=requests)
def test__make_api_call_requests_auth_secret(req, gs):
	company = 'CompanyXYZ'
	params=dotmap.build({'endpoint':'https://some.com/schedule-adherence', 'source':'nic', 'client': company, 'method': 'get'})
	res = nscraper._make_api_call(company, params)
	gs.assert_called_with(f"etl/integrations/{company.upper()}_BASE_URL")

