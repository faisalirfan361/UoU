import os, json
import pytest
from unittest.mock import patch, Mock
from uone_caching.actions import InsertEngine
from uone_caching.item_types.object_item import ObjectItem
from . import contracts as contr

# items = contr.cache_response('query',
# 							'user',
# 							'*',
# 							contr.fake_user_data({'id':agent_id + '3'}),
# 							contr.fake_user_data({'id':agent_id + '4', 'firstname': 'mike'}),
# 							contr.fake_user_data({'id':agent_id + '5', 'firstname': 'not-a-commy'}))

agent_id = 'abc-123'
insert_funcname = 'uOne-ingest-associations-insert'
insert_funcname = None
client = 'fakeCustomer'
source = 'fakeSource'


@patch('uone_caching.actions.InsertEngine._invoke_lambda')
def test_specific_id_insert_with_star_cashes_all_columns_as_id(mocked_res):
	template = {'some': 123, 'keys': 'in-a', 'cool-scraping-template': [123,321]}
	template_name = 'some-fake-template'
	expected = '{"type": "ObjectItem", "key": "template", "_id": "some-fake-template", "client": "fakeCustomer", "source": "fakeDataSource", "data": {"some": 123, "keys": "in-a", "cool-scraping-template": [123, 321]}, "address": {"object-type": {"S": "fakeCustomer-fakeDataSource-template"}, "object-id": {"S": "some-fake-template"}}, "compiled": {"some": {"N": "123"}, "keys": {"S": "in-a"}, "cool-scraping-template": {"NS": ["123", "321"]}, "object-type": {"S": "fakeCustomer-fakeDataSource-template"}, "object-id": {"S": "some-fake-template"}}}'
	inserter = InsertEngine(key='template',
							client='fakeCustomer',
							source='fakeDataSource',
							data=template)

	inserter.cache(template_name, '*')
	mocked_res.assert_called_with(expected)


@patch('uone_caching.actions.InsertEngine._invoke_lambda')
def test_no_id_insert_with_start_cashes_all_columns_as_type(mocked_res):
	template = {'some': 123, 'keys': 'in-a', 'cool-scraping-template': [123,321]}
	template_name = 'some-fake-template'
	expected = '{"type": "ObjectItem", "key": "template", "_id": "template", "client": "fakeCustomer", "source": "fakeDataSource", "data": {"some": 123, "keys": "in-a", "cool-scraping-template": [123, 321]}, "address": {"object-type": {"S": "fakeCustomer-fakeDataSource-template"}, "object-id": {"S": "template"}}, "compiled": {"some": {"N": "123"}, "keys": {"S": "in-a"}, "cool-scraping-template": {"NS": ["123", "321"]}, "object-type": {"S": "fakeCustomer-fakeDataSource-template"}, "object-id": {"S": "template"}}}'
	inserter = InsertEngine(key='template',
							client='fakeCustomer',
							source='fakeDataSource',
							data=template)

	inserter.cache('*')
	mocked_res.assert_called_with(expected)


mykey = 'agentId'
myrecord = {mykey:12345, 'column1': 23.9}
myobj = ObjectItem(key=mykey, client='CompanyQ', source='nic', data=myrecord)
@patch('uone_caching.actions.InsertEngine._invoke_lambda', return_value=myobj.serialize())
def test_cache_maintains__id_from_obj_item(lbmda):
	client = 'CompanyQ'
	source = 'nic'
	event = {'source': source, 'client': client, 'endpoints': 'fleepflorp'}

	# ci = item_builder(key, record, client='CompanyQ', source='nic')
	inserter = InsertEngine(key='agentId', client=client, source=source, data=myrecord)
	res = inserter.cache('*')
	lbmda.assert_called_with(myobj.serialize())


mykey = 'agentId'
myrecord = {mykey:12345, 'column1': 23.9, 'object-id': 54321}
myobj = ObjectItem(key=mykey, client='CompanyQ', source='nic', data=myrecord)
@patch('uone_caching.actions.InsertEngine._invoke_lambda', return_value=myobj.serialize())
def test_cache_finds__id_from_obj_item_if_sort_name_exists(lbmda):
	client = 'CompanyQ'
	source = 'nic'
	event = {'source': source, 'client': client, 'endpoints': 'fleepflorp'}

	# ci = item_builder(key, record, client='CompanyQ', source='nic')
	inserter = InsertEngine(key='agentId', client=client, source=source, data=myrecord)
	res = inserter.cache('*')
	lbmda.assert_called_with(myobj.serialize())



@patch('uone_caching.actions.InsertEngine._invoke_lambda')
def test_specific_id_insert_on_one_col_cashes_only_that_col_as_id(mocked_res):
	template = {'some': 'in-a', 'keys': 123, 'cool-scraping-template': [123,321]}
	template_name = 'some-fake-template'
	# expected = '{"type": "ObjectItem", "key": "template", "_id": "some-fake-template", "client": "fakeCustomer", "source": "fakeDataSource", "data": {"cool-scraping-template": [123, 321]}, "address": {"object-type": {"S": "fakeCustomer-fakeDataSource-template"}, "object-id": {"S": "some-fake-template"}}, "compiled": {"cool-scraping-template": {"NS": "[123, 321]"}, "object-type": {"S": "fakeCustomer-fakeDataSource-template"}, "object-id": {"S": "some-fake-template"}}}'
	expected = json.dumps(contr.reference_with_columns('input', 'template', template_name, **{'some': 'in-a'}))
	inserter = InsertEngine(key='template',
							client=client,
							source=source,
							data=template)

	inserter.cache(template_name, 'some')
	mocked_res.assert_called_with(expected)


@patch('uone_caching.actions.InsertEngine._invoke_lambda')
def test_specific_id_insert_on_no_col_cashes_only_id(mocked_res):
	template = {'some': 123, 'keys': 'in-a', 'cool-scraping-template': [123,321]}
	template_name = 'some-fake-template'

	expected = json.dumps(contr.reference_with_columns('input', 'template', template_name))
	inserter = InsertEngine(key='template',
							client=client,
							source=source,
							data=template)

	inserter.cache(template_name)
	mocked_res.assert_called_with(expected)


@patch('uone_caching.actions.InsertEngine._invoke_lambda')
def test_data_maintains_json_format(mocked_res):
	template = json.dumps({'some': 123, 'keys': 'in-a', 'cool-scraping-template': [123,321]})
	template_name = 'some-fake-template'

	expected = json.dumps(contr.reference_with_columns('input', 'template', template_name, payload='{"some": 123, "keys": "in-a", "cool-scraping-template": [123, 321]}'))
	inserter = InsertEngine(key='template',
							client=client,
							source=source,
							data=template)

	inserter.cache(template_name, '*')
	mocked_res.assert_called_with(expected)


@patch('uone_caching.actions.InsertEngine._dynamo_client')
def test_write_to_datastore_can_call_dynamo_for_single_item_with_proper_obj_item(mock_dynamo):
	event = json.dumps(contr.reference_with_columns('response', 'user', agent_id))
	expected = {
		'firstname': {'S': 'fredward'},
		'id': {'S': 'abc-123'},
		'object-id': {'S': 'abc-123'},
		'object-type': {'S': 'fakeCustomer-fakeSource-user'}
	}

	item = InsertEngine.deserialize(event)
	inserter = InsertEngine(key=item.key, client=item.client, source=item.source, data=item.data)

	try:
		inserter.write_to_datastore(item)
	except Exception:
		# this approach is fragile.  find a better way to test this
		put_item_call = mock_dynamo.mock_calls[1]
		pic_kwargs = put_item_call._get_call_arguments()[-1]
		assert pic_kwargs['TableName'] == 'References'
		# assert pic_kwargs['Item'] == item.to_record()

		assert pic_kwargs['Item'] == expected
		# mock_dynamo.put_item.assert_called_with(TableName='References', Item=item.to_record())


# @patch('uone_caching.actions.InsertEngine._dynamo_client')
# def test_write_to_datastore_can_call_dynamo_for_list_of_ObjectItems(mock_dynamo):
# 	event = json.dumps(contr.reference_with_columns('response', 'template'))
# 	import pdb
# 	pdb.set_trace()
# 	item = InsertEngine.deserialize(event)
# 	q = InsertEngine(key=item.key, client=item.client, source=item.source, data=item.data)
# 	results = q.write_to_datastore(item)
# 	mock_dynamo.assert_called_with(TableName='References', Item=item.to_record())

	# suffixes = ['3', '4', '5']
	# for result in results:
	# 	assert result.sort_key == agent_id + suffixes.pop()
m = Mock()
this_one_event = json.dumps(contr.reference_with_columns('response', 'user', agent_id))
# m.put_item = lambda **x: {'Attributes': json.loads(this_one_event)['compiled']}
m.put_item = lambda **x: contr._dynamo_put_item_response(**this_one_event['compiled'])
@patch('uone_caching.actions.InsertEngine._dynamo_client', return_value=m)
def test_write_to_datastore_can_return_proper_obj_item(mock_dynamo):
	item = InsertEngine.deserialize(this_one_event)
	inserter = InsertEngine(key=item.key, client=item.client, source=item.source, data=item.data)

	record = inserter.write_to_datastore(item)
	irec = item.to_record()
	for k,v in record.to_record().items():
		assert irec[k] == v


m = Mock()
this_one_event = json.dumps(contr.reference_with_columns('response', 'user', agent_id))
# m.put_item = lambda **x: {'Attributes': json.loads(this_one_event)['compiled']}
m.put_item = lambda **x: contr._dynamo_put_item_response(show=False, success=False)
@patch('uone_caching.actions.InsertEngine._dynamo_client', return_value=m)
def test_write_to_datastore_can_fail_on_bad_response_from_cache(mock_dynamo):
	item = InsertEngine.deserialize(this_one_event)
	inserter = InsertEngine(key=item.key, client=item.client, source=item.source, data=item.data)

	with pytest.raises(KeyError) as e_info:
		record = inserter.write_to_datastore(item)
		assert e_info.value.args[0] == 'Something failed during insert at cache but no exception could be found'


m = Mock()
this_one_event = contr.reference_with_columns('response', 'user', agent_id, boogers='false')
some_other_event = json.dumps(contr.reference_with_columns('response', 'user', agent_id, boogers='true'))
m.put_item = lambda **x: contr._dynamo_put_item_response(show=True, success=True, **this_one_event['compiled'])
@patch('uone_caching.actions.InsertEngine._dynamo_client', return_value=m)
def test_write_to_datastore_can_return_updated_cach_object_on_show(mock_dynamo):
	item = InsertEngine.deserialize(some_other_event)
	inserter = InsertEngine(key=item.key, client=item.client, source=item.source, data=item.data)

	record = inserter.write_to_datastore(item)
	# the response from dynamo was stubbed to be 'false' but we've overwritten with our updates and
	# should be seeing our request data instead of the stubbed response from dynamo's value of 'false'.
	assert record.data['boogers'] == 'true'

@patch('uone_caching.actions.InsertEngine._dynamo_client', return_value=m)
def test_deserialize_properly_casts_data(mock_dynamo):
	event = {
		'type': 'ObjectItem',
		'key': 'user',
		'_id': '4506563412099072',
		'client': 'homie',
		'source': 'dialpad',
		'data': {
			'admin_office_ids': ['5743137921302528'],
			'company_id': '4617238014459904',
			'emails': ['adrianne.meaders@homie.com'],
			'image_url': 'https://dialpad.com/avatar/user/agxzfnViZXItdm9pY2VyGAsSC1VzZXJQcm9maWxlGICA2Pqg1oAIDA.png?version=15e6cee6d8daa619f6774858545533f4',
			'is_admin': False,
			'office_id': '5743137921302528',
			'timezone': 'US/Pacific',
			'user': '4506563412099072'
		},
		'address': {
			'object-type': {'S': 'homie-dialpad-user'},
			'object-id': {'S': '4506563412099072'}
		},
		'compiled': {
			'admin_office_ids': {'NS': "['5743137921302528']"},
			'company_id': {'N': '4617238014459904'},
			'emails': {'SS': "['adrianne.meaders@homie.com']"},
			'image_url': {'S': 'https://dialpad.com/avatar/user/agxzfnViZXItdm9pY2VyGAsSC1VzZXJQcm9maWxlGICA2Pqg1oAIDA.png?version=15e6cee6d8daa619f6774858545533f4'},
			'is_admin': {'BOOL': False},
			'office_id': {'N': '5743137921302528'},
			'timezone': {'S': 'US/Pacific'},
			'user': {'N': '4506563412099072'},
			'object-type': {'S': 'homie-dialpad-user'},
			'object-id': {'S': '4506563412099072'}
		}
	}
	expected = {
		'admin_office_ids': {'NS': ['5743137921302528']},
		'company_id': {'N': '4617238014459904'},
		'emails': {'SS': ['adrianne.meaders@homie.com']},
		'image_url': {'S': 'https://dialpad.com/avatar/user/agxzfnViZXItdm9pY2VyGAsSC1VzZXJQcm9maWxlGICA2Pqg1oAIDA.png?version=15e6cee6d8daa619f6774858545533f4'},
		'is_admin': {'BOOL': False},
		'office_id': {'N': '5743137921302528'},
		'timezone': {'S': 'US/Pacific'},
		'user': {'N': '4506563412099072'},
		'object-type': {'S': 'homie-dialpad-user'},
		'object-id': {'S': '4506563412099072'}
	}

	item = InsertEngine.deserialize(event)
	print(f"Created item: ({item.sort_key} -> {item.to_record()}")
	print('Creating insert engine and writing record to datastore...')
	inserter = InsertEngine(key=item.key, client=item.client, source=item.source, data=item.data)
	record = inserter.write_to_datastore(item)
	assert item.to_record() == expected
