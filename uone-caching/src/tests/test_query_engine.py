import json
import os
from unittest.mock import patch, Mock
from boto3.dynamodb.conditions import Key
from uone_caching.actions import QueryEngine
from uone_caching.item_types.object_item import ObjectItem
from . import contracts as contr


agent_id = 'abc-123'
query_funcname = 'uOne-ingest-associations-query'
insert_funcname = None
client = 'fakeCustomer'
source = 'fakeSource'


# first_thingz = contr.reference_with_columns('input', 'user', agent_id)
# @patch('uone_caching.actions.QueryEngine._invoke_lambda',
# 	   return_value=[ObjectItem(key='user', client=first_thingz['client'], source=first_thingz['source'], _id=agent_id).serialize()])
# @patch('uone_caching.item_types.cache_item.serialize', return_value=5)
# def test_get_raised_KeyError_if_payload_cannot_cast_to_byte(mock_lambda):
# 	q = QueryEngine(key='user', client=client, source=source)

# 	with pytest.raises(KeyError) as e_info:
# 		q.get(agent_id)
# 		assert e_info.value.args[0] == 'Query must be converted to a bytes object but this object failed to cast:'


first_thingz = contr.reference_with_columns('input', 'user', agent_id)
@patch('uone_caching.actions.QueryEngine._lambda_invoker')
def test_get_invokes_with_bytes(mock_lambda):
	expected = json.dumps(first_thingz).encode('utf-8')
	q = QueryEngine(key='user', client=client, source=source)
	try:
		res = q.get(agent_id)
	except Exception as ex:
		if 'Invoking lambda failed with a 500-level error: uOne-ingest-associations-query ->' in str(ex) or \
			"'<=' not supported between instances of 'int' and 'MagicMock'" in str(ex):
			kwargs = mock_lambda.mock_calls[1].kwargs
			assert kwargs['FunctionName'] == query_funcname
			assert kwargs['InvocationType'] == 'RequestResponse'
			assert kwargs['Payload'] == expected
		else:
			raise ex


first_thingz = contr.reference_with_columns('input', 'user', agent_id)
@patch('uone_caching.actions.QueryEngine._invoke_lambda',
	   return_value=[ObjectItem(key='user', client=first_thingz['client'], source=first_thingz['source'], _id=agent_id).serialize()])
def test_get_with_id_uses_id_during_invoke_lambda(mock_lambda):
	expected = json.dumps(first_thingz)
	q = QueryEngine(key='user', client=client, source=source)

	res = q.get(agent_id)
	mock_lambda.assert_called_with(expected, query_funcname)


# return_value=json.dumps(contr.reference_with_columns('user', agent_id, firstname='fredward'))
thingz = contr.reference_with_columns('input', 'user', agent_id, 'firstname')
@patch('uone_caching.actions.QueryEngine._invoke_lambda',
	   return_value=[ObjectItem(key='user', client=thingz['client'], source=thingz['source'], data=thingz['data']).serialize()])
def test_get_with_id_and_col_returns_col_for_that_item(mock_lambda):
	expected = json.dumps(thingz)
	q = QueryEngine(key='user', client=client, source=source)

	q.get(agent_id, 'firstname')
	mock_lambda.assert_called_with(expected, query_funcname)


# return_value=json.dumps(contr.reference_with_columns('user', agent_id, firstname='fredward'))
thingz = contr.reference_with_columns('input', 'user', agent_id, 'firstname')
@patch('uone_caching.actions.QueryEngine._invoke_lambda',
	   return_value=ObjectItem(key='user', client=thingz['client'], source=thingz['source'], data=thingz['data']).serialize())
def test_get_always_returns_list(mock_lambda):
	expected = json.dumps(thingz)
	q = QueryEngine(key='user', client=client, source=source)

	res = q.get(agent_id, 'firstname')
	assert isinstance(res, list)
	# mock_lambda.assert_called_with(expected, query_funcname)



@patch('uone_caching.actions.QueryEngine._dynamo_client')
def test_read_from_datastore_can_call_dynamo_for_list_with_proper_obj_item(mock_dynamo):
	event = json.dumps(contr.reference_with_columns('input', 'user', '*'))

	item = QueryEngine.deserialize(event)
	q = QueryEngine(key=item.key, client=item.client, source=item.source)
	try:
		q.read_from_datastore(item)
	except Exception:
		mock_dynamo.query.assert_called_with(
			KeyConditionExpression=Key(item.partition_name).eq(item.partition_key),
			Select='SPECIFIC_ATTRIBUTES',
			ProjectionExpression='#RENAME_TOKEN_object-type, #RENAME_TOKEN_object-id, #RENAME_TOKEN_type, #RENAME_TOKEN_key, #RENAME_TOKEN_client, #RENAME_TOKEN_source, #RENAME_TOKEN_address, #RENAME_TOKEN_compiled',
			ExpressionAttributeNames={
				'#RENAME_TOKEN_object-type': 'object-type',
				'#RENAME_TOKEN_object-id': 'object-id',
				'#RENAME_TOKEN_type': 'type',
				'#RENAME_TOKEN_key': 'key',
				'#RENAME_TOKEN_client': 'client',
				'#RENAME_TOKEN_source': 'source',
				'#RENAME_TOKEN_address': 'address',
				'#RENAME_TOKEN_compiled': 'compiled'
			}
		)


# m = Mock()
# m.query = Mock(return_value={'Item': {'user': agent_id, 'object-id': agent_id, 'email': 'xmascomesfast@duh.com'}})
@patch('uone_caching.actions.QueryEngine._dynamo_client') #, return_value=m)
def test_read_from_datastore_can_call_dynamo_for_single_item_with_proper_obj_item(mock_dynamo):
	event = json.dumps(contr.reference_with_columns('input', 'user', agent_id))

	item = QueryEngine.deserialize(event)
	q = QueryEngine(key=item.key, client=item.client, source=item.source)

	try:
		q.read_from_datastore(item)
		raise Exception('just making sure we call the assertions, regardless of success or fail of function')
	except Exception as ex:
		assert mock_dynamo.mock_calls[1].kwargs['KeyConditionExpression'] == Key(item.partition_name).eq(item.partition) & Key(item.sort_name).eq(agent_id)
		assert mock_dynamo.mock_calls[1].kwargs['Select'] == 'SPECIFIC_ATTRIBUTES'
		assert mock_dynamo.mock_calls[1].kwargs['ProjectionExpression'] == '#RENAMETOKENobjectid, #RENAMETOKENobjecttype'
		assert mock_dynamo.mock_calls[1].kwargs['ExpressionAttributeNames'] == {'#RENAMETOKENobjectid': 'object-id', '#RENAMETOKENobjecttype': 'object-type'}

m = Mock()
m.query = Mock(return_value={'Item': {}})
@patch('uone_caching.actions.QueryEngine._dynamo_client', return_value=m)
def test_read_from_datastore_can_handle_empty_response(mock_dynamo):
	event = json.dumps(contr.reference_with_columns('input', 'user', agent_id))

	item = QueryEngine.deserialize(event)
	q = QueryEngine(key=item.key, client=item.client, source=item.source)

	try:
		q.read_from_datastore(item)
		raise Exception('An exception should have already been raised by now so the test is probably broken but we will proceed anyways because the test assertion is really the only thing that matters here')
	except Exception:
		m.query.assert_called_with(
			KeyConditionExpression=Key(item.partition_name).eq(item.partition) \
				& Key(item.sort_name).eq(agent_id),
			Select='SPECIFIC_ATTRIBUTES',
			ProjectionExpression='#RENAMETOKENobjectid, #RENAMETOKENobjecttype',
			ExpressionAttributeNames={'#RENAMETOKENobjectid': 'object-id', '#RENAMETOKENobjecttype': 'object-type'}
		)



# items = contr.cache_response('query',
# 							'user',
# 							'*',
# 							contr.fake_user_data(**{'id':agent_id + '3'}),
# 							contr.fake_user_data(**{'id':agent_id + '4', 'firstname': 'mike'}),
# 							contr.fake_user_data(**{'id':agent_id + '5', 'firstname': 'not-a-commy'}))
items = {
			'Items': [
				contr.fake_user_data(**{'object-id':agent_id + '3'}),
				contr.fake_user_data(**{'object-id':agent_id + '4', 'firstname': 'mike'}),
				contr.fake_user_data(**{'object-id':agent_id + '5', 'firstname': 'not-a-commy'})
		]
}
mock = Mock()
qry = Mock(return_value=items)
mock.query = qry
@patch('uone_caching.actions.QueryEngine._dynamo_client', return_value=mock)
def test_read_from_datastore_can_return_properly_formatted_list_of_ObjectItems(mock_dynamo):
	event = json.dumps(contr.reference_with_columns('input', 'user', '*'))

	item = QueryEngine.deserialize(event)
	q = QueryEngine(key=item.key, client=item.client, source=item.source)

	results = q.read_from_datastore(item)
	suffixes = ['3', '4', '5']

	for result in results:
		assert result.sort_key == agent_id + suffixes.pop(0)


# item = contr.cache_response('query',
# 							'user',
# 							agent_id,
# 							contr.fake_user_data({'id':agent_id}))
item = {
		'Items': [
			contr.fake_user_data(**{'id':agent_id, 'object-id':agent_id})
		]
}
mock = Mock()
gi = Mock(return_value=item)
mock.query = gi
@patch('uone_caching.actions.QueryEngine._dynamo_client', return_value=mock)
def test_read_from_datastore_can_return_single_item_with_proper_obj_item(mock_dynamo):
	event = json.dumps(contr.reference_with_columns('input', 'user', agent_id))

	item = QueryEngine.deserialize(event)
	q = QueryEngine(key=item.key, client=item.client, source=item.source)

	result = q.read_from_datastore(item)
	assert result.sort_key == agent_id

# 2
# items = contr.cache_response('query',
# 							'user',
# 							None,
# 							contr.fake_user_data({'id':agent_id}),
# 							contr.fake_user_data({'id':agent_id + '4', 'firstname': 'mike'}),
# 							contr.fake_user_data({'id':agent_id + '5', 'firstname': 'not-a-commy'}))
items = {
	'Items': [
		contr.fake_user_data({'id':agent_id}),
		contr.fake_user_data({'id':agent_id + '4', 'firstname': 'mike'}),
		contr.fake_user_data({'id':agent_id + '5', 'firstname': 'not-a-commy'})
	]
}
mock_dynamo_client = Mock()
mock_dynamo_client.query = Mock(return_value=items)
@patch('uone_caching.actions.QueryEngine._dynamo_client', return_value=mock_dynamo_client)
def test_read_from_datastore_can_return_properly_formatted_ObjectItems(mock_dynamo):
	event = json.dumps(contr.reference_with_columns('input', 'user', '*'))
	item = QueryEngine.deserialize(event)
	q = QueryEngine(key=item.key, client=item.client, source=item.source)

	result = q.read_from_datastore(item)

	mock_dynamo_client.query.assert_called_with(
		KeyConditionExpression=Key(item.partition_name).eq(item.partition),
		Select='SPECIFIC_ATTRIBUTES',
		ProjectionExpression='#RENAMETOKENobjectid, #RENAMETOKENobjecttype',
		ExpressionAttributeNames={'#RENAMETOKENobjectid': 'object-id', '#RENAMETOKENobjecttype': 'object-type'}
	)
	for item in result:
		assert isinstance(item, ObjectItem)


# 2
def test_get_table_name_uses_existing_env_var(monkeypatch):
	q = QueryEngine(key='my_key', client='my_client', source='my_source')
	object_item = ObjectItem(agent_id, client='my_client', source='my_source')

	environ_table_name = "ReferencesX"
	monkeypatch.setenv('REFERENCE_TABLE', environ_table_name)
	fetched_table_name = q._get_table_name(object_item)
	assert fetched_table_name == environ_table_name

	environ_table_name = "ReferencesY"
	monkeypatch.setenv('REFERENCE_TABLE', environ_table_name)
	fetched_table_name = q._get_table_name(object_item)
	assert fetched_table_name == environ_table_name


#3
# def test___init___handles_json_data():
# 	data = {'firstname': 'hey', 'email': 'whereru@now.jp'}
# 	q = QueryEngine(key,client=client, source=source, data=json.dumps(data))
# 	assert set(q.data.keys()) == set(data.keys())



#3
# def test___init___handles_dict_data():
# 	data = {'firstname': 'hey', 'email': 'whereru@now.jp'}
# 	q = QueryEngine(key,client=client, source=source, data=data)
# 	assert set(q.data.keys()) == set(data.keys())


#3
# def test___init___handles_kwargs_data():
# 	data = {'firstname': 'hey', 'email': 'whereru@now.jp'}
# 	q = QueryEngine(key,client=client, source=source, **data)
# 	assert set(q.data.keys()) == set(data.keys())


#3
# def test___init___handles_no_data():
# 	q = QueryEngine(key,client=client, source=source)
# 	assert set(q.data.keys()) == set([])


#3
# def test___init___handles_string_list_data():
# 	q = QueryEngine(key,client=client, source=source, data='col1,col2,col3')
# 	assert set(q.data.keys()) == set(['col1','col2','col3'])
# 	assert set(q.data.values()) == set([None,None,None])


#5
# def test_query_engine_finds_funcname_for_proper_uOne_ingest_associations_integration():
# 	q = QueryEngine(...)
# 	assert q.funcname == the correct thing


# 7
# def test_deserialize_fails_on_missing_client():


# 7
# def test_deserialize_fails_on_missing_source():


# 7
# def test_deserialize_fails_on_missing_key():


# 7
# def test_deserialize_fails_on_missing_type():


# 3
# def test_deserialize_defaults_to_empty_dict_on_missing_data():


# 1
# # This looks for the key called `_id`, which is different from `data['id']`.
def test_deserialize_sets_sort_id_with__id_if_present():
	event = json.dumps(contr.reference_with_columns('input', 'user', agent_id))
	item = QueryEngine.deserialize(event)
	assert item.sort_key == agent_id


# 1
# # This should make sure we look through data for a matching key to the partition_key.
# # Often times, data will come in about an object and the ID is at the key for which
# # we are describing the cache object.  An example is for users, while we're not using
# # the Association Class/Table.  A user in NIC is represented by an `agentId`.  The partition
# # key also happens to be `agentId`, so we can look in the `data` for `data['agentId']` and
# # that should become the `sort_key`.
# # !!! This test will fail in the current implimentation.  Needs work in `BaseEngine.deserialize`
# def test_deserialize_sets_sort_key_with_id_if_present():


# 1
def test_deserialize_creates_object_with_proper_partition_key():
	event = json.dumps(contr.reference_with_columns('input', 'user', agent_id))

	item = QueryEngine.deserialize(event)
	assert item.key == 'user'

# 1
# # Make sure only the columns we want are returned.  This should come from
# # the cache object's data's column names.
# # Decide if sort_key should be included.
# nbb - since projection is managed by dynamo client, and we are mocking dynamo client
#		so this test can only test mocked response, and not the actual projection
# def test_read_from_datastore_returns_proper_projection():


# 2
# def test_deserialize_create_ScheduleItem_cache_class_based_on_input_type_param():
# 	item = q.deserialize({'type': 'ScheduleItem',...})
# 	assert type(item).__name__ == 'ScheduleItem'


# 2
# def test_deserialize_create_CacheItem_cache_class_based_on_input_type_param():
# 	item = q.deserialize({'type': 'CacheItem',...})
# 	assert type(item).__name__ == 'ScheduleItem'


# 3
# def test__invoke_lambda_returns_SUCCESS_message_for_success():


# 2
# def test__invoke_lambda_does_not_return_SUCCESS_message_for_fail():


# 2
# def test__invoke_lambda_wraps_response_in_payload():



#9 def test__get_tablename_returns_reference_for_ObjectItem_with_no_cols_and_no_id():
# 	assert False


#9 def test__get_tablename_returns_reference_for_ObjectItem_with_id():
# 	assert False


#9 def test__get_tablename_returns_reference_for_ObjectItem_with_id_and_cols_on_the_same_record():
# 	assert False


#9 def test__get_tablename_returns_reference_for_MappingItem_with_id():
# 	assert False


#9 def test__get_tablename_returns_reference_for_MappingItem_with_id_and_col():
# 	assert False

#9 def test__get_tablename_returns_reference_for_AssociationItem_with_only_key():
# 	assert False


#9 def test__get_tablename_returns_reference_for_AssociationItem_with_id_and_unrelated_cols():
# 	assert False


#9 def test__get_tablename_returns_reference_for_AssociationItem_with_id_and_unrelated_item_id():
# 	assert False

# ...
