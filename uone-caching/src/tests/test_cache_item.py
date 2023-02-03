import json
import unittest

import pytest
from decimal import Decimal
from uone_caching.item_types.cache_item import CacheItem
from uone_caching.item_types.object_item import ObjectItem


def make_event(key='agentId', val=123456, **kwargs):
	return {**{key: val,'client': 'abc-123','source': 'some-api'},**kwargs}


def test__get_client_throws_KeyError_on_missing_client():
	event = make_event(None, 12345)
	e = {k:v for k,v in event.items() if k != 'client'}
	with pytest.raises(KeyError) as e_info:
		cc = CacheItem(e)


def test__get_client_gets_client():
	event = {'client': 'c123', 'source': 's123'}
	ci = CacheItem(event)
	res = ci._get_client(event)
	assert res == event['client']


def test__get_source():
	event = {'client': 'c123', 'source': 's123'}
	ci = CacheItem(event)
	res = CacheItem._get_source(CacheItem, event)
	assert res == event['source']


def test_key_casts_values_to_str():
	event = {'client': 'c123', 'source': 's123'}
	schmevent = event.copy()
	schmevent['departmentId'] = 'sned932'

	ci = CacheItem(event)
	oi = ObjectItem('departmentId', schmevent)

	for k,v in ci.to_record(oi.partition_name, oi.sort_name, oi.partition, oi.sort_key, ci.data).items():
		assert isinstance(v['S'], str)


def test_key_sets_types_to_S():
	event = {'client': 'c123', 'source': 's123'}
	schmevent = event.copy()
	schmevent['departmentId'] = 'sned932'

	ci = CacheItem(event)
	oi = ObjectItem('departmentId', schmevent)

	for k,v in ci._address(oi.partition_name, oi.sort_name, oi.partition, oi.sort_key).items():
		assert list(v.keys())[0] == 'S'


def test__numlike_returns_True_for_int():
	assert CacheItem._numlike(9) is True


def test__numlike_returns_True_for_float():
	assert CacheItem._numlike(9.23) is True


def test__numlike_returns_False_for_string():
	assert CacheItem._numlike('a') is False


def test__strlike_returns_True_for_string():
	assert CacheItem._strlike('yoyo') is True


def test__strlike_returns_False_for_int():
	assert CacheItem._strlike(5) is False


def test__strlike_returns_False_for_float():
	assert CacheItem._strlike(3.20) is False


def test__byteslike_returns_True_for_byets():
	assert(CacheItem._byteslike('yo'.encode())) == True


def test__byteslike_returns_False_for_other():
	assert(CacheItem._byteslike('yo')) == False
	assert(CacheItem._byteslike(5)) == False
	assert(CacheItem._byteslike(['ping'.encode()])) == False

# def test__attr_type_returns_str_attr_type_for_string():
# 	# I wrote this test case in a list of test cases and
# 	# forgot what it was supposed to test when i came back
# 	# to actually finish the test suite...
# 	assert False


# def test_matches_finds_match_for_unmatched_cache_item_type():
# 	# I wrote this test case in a list of test cases and
# 	# forgot what it was supposed to test when i came back
# 	# to actually finish the test suite...
# 	assert False


def test_to_record_formats_self_into_cache_record_format():
	event = {'client': 'c123', 'source': 's123'}
	ci = CacheItem(event)

	schmevent = event.copy()
	schmevent['departmentId'] = 'sned932'

	ci = CacheItem(event)

	for k,v in ci.to_record('part_ref', 'sort_ref', 'partition', 'sort_key', event).items():
		assert 'S' in v.keys()
		assert v['S'] in [str(v) for v in event.values()] + ['partition', 'sort_key']
		assert isinstance(v['S'], str)

		if k == 'item':
			assert v['S'] == str(event['client'])
		else:
			assert k in list(event.keys()) + ['part_ref', 'sort_ref']


def test_to_record_is_generalized_for_subclasses_1():
	event = {'client': 'c123', 'source': 's123', 'stuffAndThings': 12345, 'wowow': 'woa', 'agentId': 19009009090}
	class SubClasseroo(CacheItem):
		def __init__(self, event, **kwargs):
			self.partition_name = 'eroo-type'
			super().__init__(event, **kwargs)

	sc = SubClasseroo(event)

	for k,v in sc.to_record('part_ref', 'sort_ref', 'partition', 'sort_key', event).items():
		assert k in list(event.keys()) + ['part_ref', 'sort_ref']
		if k in event:
			assert CacheItem._attr_type(event[k]) == list(v.keys())[0]
			assert v[CacheItem._attr_type(event[k])] == str(event[k])
		else:
			assert 'S' in v.keys()
			assert v['S'] in [str(v) for v in event.values()] + ['partition', 'sort_key']


def test_to_record_sets_val_into_typed_dict():
	assert CacheItem._strlike(3.20) is False


###########################################################
# probably dropping to_record_v2
###########################################################
# def test_to_record_is_generalized_for_subclasses_2():
# 	event = {'client': 'c123', 'source': 's123', 'stuffAndThings': 12345, 'wowow': 'woa', 'agentId': 19009009090}
# 	expected = {'stuffAndThings': 12345, 'wowow': 'woa', 'agentId': 19009009090, 'part_ref': 'partition', 'sort_ref': 'sort_key'}
# 	class SubClasseroo(CacheItem):
# 		def __init__(self, event, **kwargs):
# 			self.partition_name = 'eroo-type'
# 			super().__init__(event, **kwargs)

# 	import pdb
# 	sc = SubClasseroo(event)
# 	for k,v in sc.to_record_v2('part_ref', 'sort_ref', 'partition', 'sort_key', event).items():
# 		assert k in list(event.keys()) + ['part_ref', 'sort_ref']

# 		if k == 'stuffAndThings':
# 			pdb.set_trace()

# 		if k not in ['part_ref', 'sort_ref']:
# 			assert event[k] == v
# 		else:
# 			assert v == 'partition' if k == 'part_ref' else 'sort_key'
##############################################


def test___init__takes_kwargs_as_override():
	event = {'client': 'c123', 'source': 's123'}
	kwargs = {'client': 'override'}
	ci = CacheItem(event, **kwargs)

	assert ci.client == kwargs['client']


def test___init__raises_KeyError_for_missing_required_source_base_keys():
	event = make_event(None, 12345)
	e = {k:v for k,v in event.items() if k != 'source'}
	with pytest.raises(KeyError) as e_info:
		cc = CacheItem(e)


def test___init__raises_KeyError_for_missing_required_client_base_keys():
	event = make_event(None, 12345)
	e = {k:v for k,v in event.items() if k != 'client'}
	with pytest.raises(KeyError) as e_info:
		cc = CacheItem(e)


def test_to_record_raises_AttributeError_if_missing_partition():
	event = make_event(None, 12345)
	class SubClasseroo(CacheItem):
		def __init__(self, event, **kwargs):
			self.sort_key = 'sk'
			self.sort_ref = 'sf'
			self.partition_key = 'pk'
			super().__init__(event, **kwargs)

	cc = SubClasseroo(event)
	# subclasses should impliment their own function which calls the parent class function with proper params
	with pytest.raises(TypeError) as e_info:
		cc.to_record()

# def test___init__leaves_key_out_of_internal_data_ref_if_rec_is_not_key_only():
# 	event = {'client': 'c123', 'source': 's123', 'thing': 123}
# 	# import pdb
# 	# pdb.set_trace()
# 	ci = CacheItem(event)

# 	assert ci.address()[list(ci.key().address())[0]]['S'] == str(event['thing'])


# def test___init__finds_sort_key():
# 	event = {'client': 'c123', 'source': 's123'}
# 	ci = CacheItem(event)
# 	assert ci.partition_key == 'source'


# def test___init__finds_sort_ref():
# 	event = {'client': 'c123', 'source': 's123'}
# 	ci = CacheItem(event)
# 	assert ci.sort_ref == 'item'


# def test___init__finds_partition_key():
# 	event = {'client': 'c123', 'source': 's123'}
# 	ci = CacheItem(event)
# 	assert ci.partition_key == 'source'


# def test___init__finds_partition_name():
# 	event = {'client': 'c123', 'source': 's123'}
# 	ci = CacheItem(event)
# 	assert ci.partition_name == 'item'


# def test___init__finds_partition():
# 	client = 'client-1'
# 	source = 'source-1'
# 	expected = f"{source}-item"
# 	event = {'client': client, 'source': source}

# 	ci = CacheItem(event)
# 	assert ci.partition == expected


def test__attr_type_finds_S_for_str():
	assert CacheItem._attr_type('h4mc!') == 'S'


def test__attr_type_finds_M_for_map():
	assert CacheItem._attr_type({'hey': 'dude', 'you': 'should', 'work': [1,2,3]}) is 'M'


def test__attr_type_finds_M_for_dynamo_record():
	assert CacheItem._attr_type({'hey': {'N': 5}}) is 'M'


def test__attr_type_finds_N_for_num():
	assert CacheItem._attr_type(53.4234212341) is 'N'


def test__attr_type_finds_N_for_scientific_notation():
	assert CacheItem._attr_type('9e-58') is 'N'


def test__attr_type_does_not_return_N_for_bool():
	assert CacheItem._attr_type(False) is not 'N'


def test__attr_type_finds_SS_for_str_arr():
	assert CacheItem._attr_type(['a','e','i','o','u','z','yz']) is 'SS'


def test__attr_type_finds_NN_for_num_arr():
	assert CacheItem._attr_type([4,3.4,0,10]) is 'NS'


def test__attr_type_finds_NULL_for_None():
	assert CacheItem._attr_type(None) is 'NULL'


def test_attr_types_returns_None_for_unmatched_type():
	class A():
		def __init__(self):
			pass
	assert CacheItem._attr_type(A()) is None


def test_to_record_does_not_cast_bool_to_str():
	event = {'client': 'c123', 'source': 's123', 'boogers': False, 'hmm': True}
	ci = CacheItem(event)
	rec = ci.to_record('item-type', 'item', 'abc_123', 'sort', event)
	assert rec['boogers']['BOOL'] == False
	assert rec['hmm']['BOOL'] == True


def test_attr_types_returns_BOOL_for_boollike_str():
	for b in ['True', 'true', 'TRUE', 'tRuE', 'FALSE', 'False', 'false']:
		assert CacheItem._attr_type(b) is 'BOOL'


def test_from_typed_returns_BOOL_for_BOOL():
	assert CacheItem.from_typed({'BOOL': 'false'}) == False


def test_from_typed_returns_str_for_S():
	assert CacheItem.from_typed({'S': 'wowowow'}) == 'wowowow'


def test_from_typed_returns_ints_for_N():
	assert CacheItem.from_typed({'N': '582938'}) == 582938


def test_from_typed_returns_floats_for_N():
	result = CacheItem.from_typed({'N': '54321.12345'})
	assert not isinstance(result, int)
	assert 54321 < result < 54322


def test_from_typed_returns_dict_for_M():
	assert CacheItem.from_typed({'M': {'wow': 123, 'stuff': ['works']}}) == {'wow': 123, 'stuff': ['works']}


def test_from_typed_returns_list_for_L():
	expected = [{"do_not_disturb": False, "group_id": "6615731558547456", "group_type": "department"}]
	result = CacheItem.from_typed({'L': '[{"do_not_disturb": false, "group_id": "6615731558547456", "group_type": "department"}]'})
	assert result == expected


def test_from_typed_returns_list_of_randos_for_L():
	assert CacheItem.from_typed({'L': ['5', 'hey', [1,2,3,4,5], 'stuff']}) == ['5', 'hey', [1,2,3,4,5], 'stuff']


def test_from_typed_returns_list_of_str_for_SS():
	assert CacheItem.from_typed({'SS': ['5', 7, 'cool', 'hey']}) == ['5', '7', 'cool', 'hey']


def test_from_typed_returns_list_of_number_for_NS():
	assert CacheItem.from_typed({'NS': ['5', '7.2', '9', '10']}) == [5,7.2,9,10]


# def test_from_typed_returns_str_for_S():
# 	assert CacheItem.from_typed({'S': 'wowowow'}) == 'wowowow'

###################################
def test_to_record_sets_NULL_val_to_True():
	event = {'client': 'c123', 'source': 's123', 'boogers': None}
	ci = CacheItem(event)

	assert ci.to_record('item-type', 'item', 'abc_123', 'sort', event)['boogers']['NULL'] == True


def test_to_record_casts_BOOL_val_to_bool():
	event = {'client': 'c123', 'source': 's123', 'boogers': False, 'bleh': True, 'stuff': 'true', 'andthings': 'False'}
	ci = CacheItem(event)

	assert ci.to_record('item-type', 'item', 'abc_123', 'sort', event)['boogers']['BOOL'] == False
	assert ci.to_record('item-type', 'item', 'abc_123', 'sort', event)['andthings']['BOOL'] == False
	assert ci.to_record('item-type', 'item', 'abc_123', 'sort', event)['bleh']['BOOL'] == True
	assert ci.to_record('item-type', 'item', 'abc_123', 'sort', event)['stuff']['BOOL'] == True


def test_serialize():
	# given
	event = {'client': 'c123', 'source': 's123', 'boogers': 21}
	ci = _build_test_cache_item(event)
	# when
	res = json.loads(ci.serialize())
	# then
	assert res['type'] == 'CacheItem'
	assert res['key'] == None
	assert res['_id'] == 'abc-123'
	assert res['client'] == 'c123'
	assert res['source'] == 's123'
	assert res['data']['client'] == 'c123'
	assert res['data']['source'] == 's123'
	assert res['data']['boogers'] == 21
	assert res['address'] == {'a-fakeobjtype': {'S': 'some-fake-partition'}, 'glebdebsnerglebergle': {'S': 'abc-123'}}
	moreboogs = res['compiled'].pop('boogers')
	assert moreboogs['N'] == '21'
	assert res['compiled'] == {'client': {'S': 'c123'}, 'source': {'S': 's123'}, 'a-fakeobjtype': {'S': 'some-fake-partition'}, 'glebdebsnerglebergle': {'S': 'abc-123'}}


def test_serialize_can_handle_Decimal():
	# given
	event = {'client': 'c123', 'source': 's123', 'boogers': Decimal(1.21)}
	ci = _build_test_cache_item(event)
	# when
	res = json.loads(ci.serialize())
	# then
	assert event['boogers'] <= float(res['data']['boogers']) <= float(event['boogers']) + 0.1 # maybe it's more accurate but this _should_ work
	moreboogs = res['compiled'].pop('boogers')
	assert 1.20 < float(moreboogs['N']) < 1.22


def test_serialize_can_handle_set():
	test_case = unittest.TestCase()
	# given
	event = {'client': 'c123', 'source': 's123', 'boogers': {"1", "2"}}
	ci = _build_test_cache_item(event)
	# when
	res = json.loads(ci.serialize())
	# then
	test_case.assertCountEqual(list(event['boogers']), res['data']['boogers'])


# *** HELPER METHODS ***

def _build_test_cache_item(event):
	ci = CacheItem(event)
	ci.sort_name = 'glebdebsnerglebergle'
	ci.sort_key = 'abc-123'
	ci.partition_name = 'a-fakeobjtype'
	ci.partition = 'some-fake-partition'
	return ci


def test_data_with_types_can_find_S_type_for_string():
	event = {'client': 'c123', 'source': 's123', 'boogers': {"1", "2"}}
	result = CacheItem(event).data_with_types(**{'thing': 'ok'})
	assert ['S'] == list(result['thing'].keys())


def test_data_with_types_can_find_N_type_for_int():
	event = {'client': 'c123', 'source': 's123', 'boogers': {"1", "2"}}
	result = CacheItem(event).data_with_types(**{'thing': 321})
	assert ['N'] == list(result['thing'].keys())
	assert result['thing']['N'] == '321'


def test_data_with_types_can_find_N_type_for_float():
	event = {'client': 'c123', 'source': 's123', 'boogers': {"1", "2"}}
	result = CacheItem(event).data_with_types(**{'thing': 321.123})
	assert ['N'] == list(result['thing'].keys())


def test_data_with_types_can_find_L_type_for_list_of_maps():
	event = {'client': 'c123', 'source': 's123', 'boogers': {"1", "2"}}
	mapped = {'M': {'client': {'S': 'c123'}, 'source': {'S': 's123'}, 'boogers': {'NS': ['2','1']}}}
	result = CacheItem._data_with_types(**{'thing': [event]})
	assert ['L'] == list(result['thing'].keys())

	for k,v in result['thing']['L'][0]['M'].items():
		assert k in mapped['M']
		if isinstance(list(v.values())[0], str):
			assert mapped['M'][k] == v
		else:
			assert set(list(mapped['M'][k].values())[0]) == set(list(v.values())[0])

# def test_data_in_nested_objects_maintains_order():
# 	# this is different from test_data_with_types_can_find_L_type_for_list_of_maps
# 	# because that test allows the NS items in 'boogers' to be out of order
# 	# the order reversal is due to the recursion used to find types in nested objects
# 	event = {'client': 'c123', 'source': 's123', 'boogers': {"1", "2"}}
# 	mapped = {'M': {'client': {'S': 'c123'}, 'source': {'S': 's123'}, 'boogers': {'NS': ['1','2']}}}
# 	result = CacheItem._data_with_types(**{'thing': [event]})
# 	assert ['L'] == list(result['thing'].keys())
# 	assert result['thing']['L'][0] == mapped



def test_data_with_types_can_find_M_type_for_dict():
	data = {'thing': {'that': 'is', 'a': 'map'}}
	expected = {'thing': {'M': {'that': {'S': 'is'}, 'a': {'S': 'map'}}}}
	result = CacheItem._data_with_types(**data)
	assert ['M'] == list(result['thing'].keys())
	assert result == expected

