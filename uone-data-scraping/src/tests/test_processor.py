import os, json
import pytest
from unittest.mock import patch, Mock
from uone_data_scraping.processing import processor, modifiers, dotmap
from uone_caching.item_types.object_item import ObjectItem


key = 'stuff'
template = {key:'icarenot', 'client': 'boogerz-R-us'}
# @patch('uone_data_scraping.configs.processor.modifiers.pull_it', return_value=key)
# @patch('uone_data_scraping.configs.processor.modifiers.get_it') #, return_value=template[key])
@patch('uone_data_scraping.processing.modifiers.print_it')
def test_processor_calls_functions_in_params_too(pi): #gi, pi):
	comm = {'print_it': [
				{'get_it': [
					'$$config',
					{'pull_it': ['nothing here to see.., just a pretend argument. the mock has the return value for pull_it']}
			]}]}

	conf = dotmap.build(template)
	res = processor.process_it(comm, conf)
	assert template == dict(conf)
	pi.assert_called()



@patch('uone_data_scraping.processing.modifiers.get_it')
def test_processor_uses_modifiers_module_when_called_for(gi):
	arg = '__doc__'
	comm = {'pass_through': [
				'$$config',
				{'get_it': [
					'$$modifiers',
					'__doc__'
			]}]}

	conf = dotmap.build(template)
	res = processor.process_it(comm, conf)
	gi.assert_called_with(modifiers, arg)


@patch('uone_data_scraping.processing.modifiers.get_it')
def test_final_command_can_be_run_with_event_instead_of_context(gi):
	comm = {'get_it': ['$$event', 'yo']}
	event = {'yo': 'hey'}
	conf = dotmap.build(template)

	res = processor.process_it(comm, conf, event)
	gi.assert_called_with(event, 'yo')


# def test_nested_commands_can_be_run_with_event_instead_of_context():
# 	comm = {'print_it': [{'set_it': ['$$event', 'mediaTypeIds', {'de_cache_it': ['mediaTypeIds', '$$event']}]}]}
# 	event = {'yo': 'hey', 'client': 'boogerz-R-us'}
# 	conf = dotmap.build(template)

# 	_should_find = modifiers.de_cache_it(conf, 'mediaTypeIds', event)
# 	res = processor.process_it(conf, comm, **event)

# 	assert 'mediaTypeIds' in event.keys()
# 	assert event['mediaTypeIds'] == _should_find
# 	# assert res ==


obj_1 = ObjectItem(key='department', client='uone', source='nic', data={'departmentId': 7, 'mediaTypeIds': 2})
obj_1.reset_id(7)
obj_2 = ObjectItem(key='department', client='uone', source='nic', data={'departmentId': 5, 'mediaTypeIds': 8})
obj_2.reset_id(5)
@patch('uone_data_scraping.configs.templates.QueryEngine.get', return_value=[obj_1, obj_2])
def test_de_cache_it_when_event_is_passed_as_kwargs(cqe):
	comm = {'de_cache_it': ['boogerz-R-us', 'nic', 'mediaTypeIds']}

	event = {'client': 'boogerz-R-us'}
	conf = dotmap.build(template)

	res = processor.process_it(comm, conf, event)
	assert res == modifiers.de_cache_it(source='nic', grouping_term='mediaTypeIds', **event)


def test_de_cache_it_throws_exception_if_no_index_params_passed():
	comm = {'de_cache_it': ['mediaTypeIds', '$$event']}

	event = {'client': 'boogerz-R-us'}
	conf = dotmap.build(template)
	with pytest.raises(KeyError) as e:
		processor.process_it(comm, conf, event)

	assert e.errisinstance(KeyError)


def test_final_command_can_get_actual_result():
	comm = {'get_it': ['$$event', 'yo']}
	event = {'yo': 'hey'}
	conf = dotmap.build(template)

	res = processor.process_it(comm, conf, event)
	assert res == 'hey'


def test_modifier_funcs_can_be_used_as_param():
	comm = {'print_it': ['$$modifiers.build_dotmap_from_it']}
	event = {'yo': 'hey'}
	conf = dotmap.build(template)

	res = processor.process_it(comm, conf, event)
	assert str(modifiers.build_dotmap_from_it) == res[0]


@patch('uone_data_scraping.processing.processor.modifiers')
def test_processor_calls_correct_command(mods):
	comm = {'fake_it': ['nothing here to see.., just a pretend argument']}
	conf = dotmap.build({'stuff':'icarenot'})

	res = processor.process_it(comm, conf)
	mods.fake_it.assert_called()


@patch('uone_data_scraping.processing.processor.modifiers', spec=modifiers)
def test_processor_fails_if_modifier_command_not_exist(mods):
	comm = {'fake_it': ['arg1', 'arg2']}
	try:
		conf = dotmap.build({'stuff':'icarenot'})
		res = processor.process_it(comm, conf)
		assert False, f"Processor should have failed because {comm} is not a real command on the modifiers module..."
	except AttributeError as ex:

		if "object has no attribute 'fake_it'" in str(ex):
			assert True, 'Exception thrown for missing method on modifiers from processor.process_it'
		else:
			assert False, f"Exception thrown but it was for the wrong reason -> {ex}"
