import os, json
import pytest
from unittest.mock import patch, Mock
# from uone_data_scraping.processing import modifiers, dotmap
from uone_data_scraping.expressions import expressions
from uone_data_scraping.utils import dotmap
from uone_caching.actions.query import QueryEngine
from uone_caching.actions.insert import InsertEngine
from uone_caching.item_types.object_item import ObjectItem

comm = {'get_it': ['$$data', 'something']}
state = {'job': comm, 'event': None,'data': {'thing': 'stuff', 'something': 'else'}}

def test_copy_and_set_on_iter_gives_item_on_iteration():
	context = dotmap.build({'some': 'stuff', 'query_parameters': {'mediaTypeId': None}})
	fake_ids = [1,2,3,4]
	key = 'query_parameters.mediaTypeId'
	results = expressions.copy_and_set_on_itter(state, context, fake_ids, expressions.build_dotmap_from_it, key)
	assert len(results) == len(fake_ids)
	for res in results:
		ind = fake_ids.index(res[key])
		assert res[key] == results[ind][key]


def test_copy_and_set_on_iter_can_make_simple_collections_of_one():
	old_cursor = None
	context = dotmap.build({'some': 'stuff', 'query_parameters': {'cursor': old_cursor}})
	fake_id = 12345
	key = 'query_parameters.cursor'
	results = expressions.copy_and_set_on_itter(state, context, fake_id, expressions.build_dotmap_from_it, key)
	assert len(results) == 1
	assert context.query_parameters['cursor'] != results[0].query_parameters['cursor']
	assert context.query_parameters['cursor'] == old_cursor
	assert results[0].query_parameters['cursor'] == fake_id


def test_format_it_can_accept_one_args_on_named_placeholders():
	raw = 'my/path/{pathname}/filething.sned'
	args = ['baseball']

	res = expressions.format_it(state, raw, *args)
	assert res == raw.format(pathname=args[0])


def test_format_it_can_accept_one_args_on_empty_placeholders():
	raw = 'my/path/{}/filething.sned'
	args = ['baseball']
	res = expressions.format_it(state, raw, *args)
	assert res == raw.format(args[0])


def test_format_it_can_accept_many_args():
	raw = '{}/path/{pathname}/filething.{}{}{five}{six}'
	args = ['baseball', 'snedular', '123', 'fleep', 'florp', 'santa']

	res = expressions.format_it(state, raw, *args)
	# if you liked the commented out version of the behavior, feel free
	# to make a PR or a Jira ticket or whatever :)
	# assert res == 'baseball/path/snedular/filething.123fleepflorpsanta'
	assert res == 'fleep/path/baseball/filething.florpsantasnedular123'


def test_format_it_can_replace_placeholders_in_order_if_they_are_empty_placeholders():
	raw = '{}/path/{}/filething.{}{}{}{}'
	args = ['baseball', 'snedular', '123', 'fleep', 'florp', 'santa']

	res = expressions.format_it(state, raw, *args)
	assert res == 'baseball/path/snedular/filething.123fleepflorpsanta'


def test_format_it_can_replace_placeholders_in_order_if_they_are_named_placeholders():
	raw = '{first}/path/{second}/filething.{third}{fourth}{five}{six}'
	args = ['baseball', 'snedular', '123', 'fleep', 'florp', 'santa']

	res = expressions.format_it(state, raw, *args)
	assert res == 'baseball/path/snedular/filething.123fleepflorpsanta'


def test_format_it_can_replace_kwargs_given_named_placeholders_in_raw():
	raw = '{first}/path/{second}/filething.{third}{fourth}{five}{six}'
	kwargs = {
		'first': 'baseball',
		'second': 'snedular',
		'third': '123',
		'fourth': 'fleep',
		'five': 'florp',
		'six': 'santa',
	}

	res = expressions.format_it(state, raw, **kwargs)
	assert res == 'baseball/path/snedular/filething.123fleepflorpsanta'


def test_format_it_will_skip_replace_if_format_kwargs_does_not_match_any_placeholder():
	raw = '{first}/path/{second}/filething.{third}{fourth}{five}{six}'
	kwargs = {
		'first': 'baseball',
		'second': 'snedular',
		'third': '123',
		'qua': 'dafuc',
		'anotherone': 'snarglebargle',
		'last': 'huh?',
		'fourth': 'fleep',
		'five': 'florp',
		'six': 'santa',
		'hah-jk-this-is-last': 'clause'
	}

	res = expressions.format_it(state, raw, **kwargs)
	assert res == 'baseball/path/snedular/filething.123fleepflorpsanta'


def test_format_it_will_prefer_kwargs_then_set_args_in_order_of_remaining_placeholders():
	raw = '{first}/path/{second}/filething.{third}{fourth}{five}{six}'
	args = ['baseball', 'fleep']
	kwargs = {
		'second': 'snedular',
		'third': '123',
		'qua': 'dafuc',
		'anotherone': 'snarglebargle',
		'last': 'huh?',
		'five': 'florp',
		'six': 'santa',
		'hah-jk-this-is-last': 'clause'
	}

	res = expressions.format_it(state, raw, *args, **kwargs)
	assert res == 'baseball/path/snedular/filething.123fleepflorpsanta'


def test_copy_and_set_on_iter_sets_nested_paths_on_itter():
	context = dotmap.build({'some': 'stuff', 'query_parameters': {'mediaTypeId': None}})
	fake_ids = [1,2,3,4]
	key = 'query_parameters.mediaTypeId'
	path = key.split('.')

	results = expressions.copy_and_set_on_itter(state, context, fake_ids, expressions.build_dotmap_from_it, key)
	for res in results:
		ind = fake_ids.index(res[path[0]][path[1]])
		assert res[path[0]][path[1]] == results[ind][key]


def test_pass_through_sends_context_as_is():
	context = dotmap.build({'some':'crap', 'and':['thingz']})
	res_state, res_cont, res_res = expressions.pass_through(state, context, 'hey')
	assert res_state == state
	assert res_cont == context
	assert res_res == 'hey'


# def test_print_it_is_pass_through():
# 	context = dotmap.build({})
# 	res = expressions.print_it(state, context, 'hey')
# 	assert res == ["(DotMap({}), 'hey')"]


def test_print_it_returns_same_as_regular_print():
	context = dotmap.build({})
	res = expressions.print_it(state, context, 'hey')
	assert res == None


def test_set_it_sets_at_root():
	context = dotmap.build({'hey': 'wowowow'})
	res = expressions.set_it(state, context, 'hey', 'yo')
	assert context['hey'] == 'yo'


def test_set_it_sets_all_the_way_down_on_dot_path():
	context = dotmap.build({'hey': 'wowowow', 'things': {'ping': 'pong'}})
	res = expressions.set_it(state, context, 'things.ping', 'yo')
	assert context['things.ping'] == 'yo'
	assert context['things']['ping'] == 'yo'
	assert context['ping'] == 'yo'


def test_set_it_sets_nested_reference():
	context = dotmap.build({'hey': 'wowowow', 'bleep': {'blorp': 'fleep'}})
	res = expressions.set_it(state, context, 'bleep.blorp', 'yo')
	assert context['bleep']['blorp'] == 'yo'


# @patch('uone_data_scraping.processing.expressions._get_queue_client')
# @patch.dict(os.environ, {"CACHE_DUMP_QUEUE": "https://stuffstuffstuff.com"})
mykey = 'agentId'
myrecord = {mykey:12345, 'column1': 23.9}
myobj = ObjectItem(key=mykey, client='CompanyQ', source='nic', data=myrecord)
@patch('uone_data_scraping.expressions.expressions.InsertEngine._invoke_lambda', return_value=myobj.serialize())
# @patch('uone_data_scraping.configs.templates.QueryEngine.get', return_value=[obj_1, obj_2])
def test_cache_it_sends_obj_key_for_obj_type(lbmda):
	context = dotmap.build({'hey': 'wowowow', 'bleep': {'blorp': 'fleep'}})
	event = {'source': 'nic', 'client': 'CompanyQ', 'endpoints': 'fleepflorp'}

	res = expressions.cache_it(state, mykey, 'CompanyQ', 'nic', myrecord)
	lbmda.assert_called_with(myobj.serialize())


@patch('uone_data_scraping.expressions.expressions.QueryEngine')
def test_de_cache_it_invokes_query_engine_with_no_extra_cols_for_empty_cols_and_data(qe_mock):
	context = dotmap.build({'hey': 'wowowow', 'bleep': {'blorp': 'fleep'}})
	event = {'source': 'nic', 'client': 'CompanyQ', 'endpoints': 'fleepflorp'}

	res = expressions.de_cache_it(state, event['client'], event['source'], 'user', None, 'fake', 'columnname', **{'cool': 'beans'})
	qe_mock.assert_called_with(key='user',
							   client=event['client'],
							   source=event['source'],
							   data={'fake': None, 'columnname': None, 'cool': None}
	)


def test_skip_if_present_returns_list_for_list_input():
	coll = ['a', 'b', 'c', 'd', {'aa':'eh-eh'}]
	results = expressions.skip_if_present(state, coll, 'ccccc')
	assert isinstance(results, list)


def test_skip_if_present_returns_dict_for_dict_input():
	coll = {'aa':'eh-eh', 'yo': 123, 'stuff': 'is-stuff'}
	results = expressions.skip_if_present(state, coll, 'aa')
	assert isinstance(results, dict)


def test_skip_if_present_keeps_entire_collection_if_no_match_in_list():
	coll = ['a', 'b', 'c', 'd', {'aa':'eh-eh'}]
	results = expressions.skip_if_present(state, coll, 'ccccc')
	assert results == coll


def test_skip_if_present_keeps_entire_collection_if_no_match_in_dict():
	coll = {'hey': 'you', 'guys': 5}
	results = expressions.skip_if_present(state, coll, 'z')
	assert results == coll


def test_skip_if_present_filters_entire_collection_if_all_match_in_list():
	coll = ['a', 'a', 'a', 'a']
	results = expressions.skip_if_present(state, coll, 'a')
	assert len(results) == 0


def test_skip_if_present_filters_entire_collection_if_all_vals_match_in_dict():
	coll = {'hey': 'a', 'guys': 'a'}
	results = expressions.skip_if_present(state, coll, 'a')
	assert len(results) == 0


def test_skip_if_present_filters_entire_collection_if_all_keys_match_in_dict():
	coll = {'hey': 'a'}
	results = expressions.skip_if_present(state, coll, 'hey')
	assert len(results) == 0


def test_skip_if_present_keeps_only_unmatched_items_in_list():
	coll = ['hey', 'a', 'guys', 'a']
	results = expressions.skip_if_present(state, coll, 'qq')
	assert coll == results


def test_pass_if_present_keeps_only_unmatched_items_in_dict():
	coll = {'hey': 'a', 'guys': 'a'}
	results = expressions.skip_if_present(state, coll, 'b')
	assert coll == results


def test_pass_if_present_filters_only_matched_items_in_in_list():
	coll = ['a', 'b', 'c', 'd', {'aa':'eh-eh'}]
	results = expressions.skip_if_present(state, coll, 'b')
	results = expressions.skip_if_present(state, results, {'aa':'eh-eh'})
	coll.pop(1)
	coll.pop(-1)
	assert results == coll


def test_pass_if_present_filters_only_matched_items_in_dict():
	coll = {'hey': 'a', 'guys': 'a', 'look': 'over', 'here': [2,1,3]}
	results = expressions.skip_if_present(state, coll, 'a')
	results = expressions.skip_if_present(state, results, 'look')
	assert {'here': [2,1,3]} == results

########
def test_skip_if_not_present_returns_list_for_list_input():
	coll = ['a', 'b', 'c', 'd', {'aa':'eh-eh'}]
	results = expressions.skip_if_not_present(state, coll, 'ccccc')
	assert isinstance(results, list)


def test_skip_if_not_present_returns_dict_for_dict_input():
	coll = {'aa':'eh-eh', 'yo': 123, 'stuff': 'is-stuff'}
	results = expressions.skip_if_not_present(state, coll, 'aa')
	assert isinstance(results, dict)


def test_skip_if_not_present_keeps_entire_collection_if_all_match_in_list():
	coll = ['a', 'a', 'a', 'a']
	results = expressions.skip_if_not_present(state, coll, 'a')
	assert results == coll


def test_skip_if_not_present_keeps_entire_collection_if_no_match_in_dict():
	coll = {'hey': 'you'}
	results = expressions.skip_if_not_present(state, coll, 'hey')
	assert results == coll


def test_skip_if_not_present_keeps_entire_collection_if_all_match_in_list():
	coll = ['a', 'a', 'a', 'a']
	results = expressions.skip_if_not_present(state, coll, 'a')
	assert results == coll


def test_skip_if_not_present_keeps_entire_collection_if_all_vals_match_in_dict():
	coll = {'hey': 'a', 'guys': 'a'}
	results = expressions.skip_if_not_present(state, coll, 'a')
	assert results == coll


def test_skip_if_not_present_keeps_entire_collection_if_all_keys_match_in_dict():
	coll = {'hey': 'a'}
	results = expressions.skip_if_not_present(state, coll, 'hey')
	assert results == coll


def test_skip_if_not_present_keeps_only_matched_items_in_list():
	coll = ['hey', 'a', 'guys', 'a']
	results = expressions.skip_if_not_present(state, coll, 'hey')
	assert ['hey'] == results


def test_skip_if_not_present_keeps_only_matched_items_in_dict():
	coll = {'hey': 'you', 'guys': 'run'}
	results = expressions.skip_if_not_present(state, coll, 'guys')
	assert {'guys': 'run'} == results
