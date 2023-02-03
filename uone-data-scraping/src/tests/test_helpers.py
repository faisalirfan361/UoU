import os, json
import datetime
import dateutil.parser
import datetime
import dateutil.parser
from datetime import date
from datetime import datetime as dtime
import pytest
from unittest.mock import patch, Mock
from uone_data_scraping.utils import helpers
# from uone_data_scraping.utils import helpers

################################################################################
# Make sure to write meaningful specs for _parse_event_for_date...
################################################################################
def test__parse_event_for_date_can_decipher_timedelta_with_hyphen():
	res = helpers._parse_event_for_date('4-days')
	assert res == datetime.timedelta(days=4)


def test__parse_event_for_date_can_return_timedelta():
	res = helpers._parse_event_for_date('4 days')
	assert type(res) == type(datetime.timedelta(days=4))


def test__parse_event_for_date_can_return_datetime():
	dp = '12-21-2021T12:34:56Z'
	res = helpers._parse_event_for_date(dp)
	assert type(res) == type(dateutil.parser.parse(dp))


def test__parse_event_for_date_can_parse_date_string():
	dp = '12-21-2021T12:34:56Z'
	res = helpers._parse_event_for_date(dp)
	assert res == dateutil.parser.parse(dp)


def test__parse_event_for_date_can_return_today():
	res = helpers._parse_event_for_date('today')
	assert type(res) == type(dtime.combine(date.today(), dtime.min.time()))


def test_time_windows_for_returns_one_bucketed_request_conf_with_small_window():
	start = dateutil.parser.parse('2021-01-01')
	end = dateutil.parser.parse('2021-01-02')
	aconf = helpers.time_windows_for(start, end, datetime.timedelta(days=5))

	assert len(list(aconf)) == 1


def test_time_windows_for_returns_many_bucketed_request_conf_with_small_window():
	start = dateutil.parser.parse('2021-01-01')
	end = dateutil.parser.parse('2021-01-02')
	aconf = helpers.time_windows_for(start, end, datetime.timedelta(minutes=5))

	assert len(aconf) > 1


def test_time_windows_returns_non_overlapping_time_windows():
	start = dateutil.parser.parse('2021-01-01')
	end = dateutil.parser.parse('2021-01-02')
	aconf = helpers.time_windows_for(start, end, datetime.timedelta(minutes=5))

	for i in list(range(len(aconf))):
		try:
			now_sd = aconf[i][0]
			future_sd = aconf[i+1][0]
			now_e = aconf[i][1]
			future_e = aconf[i+1][1]

			assert not ((now_sd >= future_sd) and (now_e >= future_e))

		except IndexError:
			print('yay, you made it through the configs time bucket overlap test')
			pass


def test__build_query_params_returns_empty_string_if_no_params_given():
	result = helpers._build_query_params({})
	assert result == ''


def test__build_query_params_returns_empty_string_if_None_given():
	result = helpers._build_query_params(None)
	assert result == ''


def test__build_query_params_filters_null_items():
	result = helpers._build_query_params({'thisone': 'should-show', 'butnotthisone': None})
	assert result == '?thisone=should-show'


def test_month_of_returns_formatted_month_string():
	dateobj = datetime.datetime(year=1983, month=3, day=20, hour=7)
	result = helpers.month_of(dateobj)
	assert result == f"0{dateobj.month}"


def test_day_of_returns_formatted_day_string():
	dateobj = datetime.datetime(year=2011, month=5, day=2, hour=7)
	result = helpers.day_of(dateobj)
	assert result == f"00{dateobj.day}"


def test_hour_of_returns_formatted_hour_string():
	dateobj = datetime.datetime(year=1981, month=12, day=11, hour=7)
	result = helpers.hour_of(dateobj)
	assert result == f"0{dateobj.hour}"


def test__format_json_records_returns_reformatted_json():
	pre = [{'a':123,'b':234,'c':345} for i in enumerate(list(range(0,10)))]
	res = helpers.format_json_records(pre, 'coolness')
	print(f"Res: {res}")

	res_obj = json.loads(res)
	for k,v in res_obj.items():
		assert isinstance(v, list)
		assert len(v) == len(pre)
