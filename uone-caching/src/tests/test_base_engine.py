import os, json
import pytest
from unittest.mock import patch, Mock
from uone_caching.actions.base_engine import BaseEngine
from . import contracts as contr
from botocore.response import StreamingBody


agent_id = 'abc-123'
# insert_funcname = 'uOne-ingest-associations-insert'
# insert_funcname = None
client = 'fakeCustomer'
source = 'fakeSource'

pload = Mock()
pload.read = Mock(return_value=json.dumps({'payload': 'wow', 'status': 'SUCCESS'}).encode('utf-8'))
mocked = Mock()
mocked.invoke = Mock(return_value={'StatusCode': 200, 'Payload': pload})
@patch('uone_caching.actions.BaseEngine._lambda_invoker', return_value=mocked)
def test__invoke_lambda_decodes_response(mock_client):
	b = BaseEngine(key='user', client=client, source=source)
	b.funcname = 'fake' # subclasses set this normally but we're testing the base class right now...

	res = b._invoke_lambda('fake', )
	assert res == 'wow'
	# mock_client.assert_called_with(expected, query_funcname)

# def test__invoke_lambda_returns_True_on_asynch():
# def test__invoke_lambda_returns_response_payload_on_synch():

def test_deserialize_can_form_an_ObjectItem():
	event = json.dumps(contr.reference_with_columns('input', 'user', agent_id))
	obj = BaseEngine.deserialize(event)
	assert type(obj).__name__ == 'ObjectItem'


def test_deserialize_can_handle_JSON():
	event = json.dumps(contr.reference_with_columns('input', 'user', agent_id))
	obj = BaseEngine.deserialize(event)
	assert type(obj).__name__ == 'ObjectItem'


def test_deserialize_can_handle_dict():
	event = contr.reference_with_columns('input', 'user', agent_id)
	obj = BaseEngine.deserialize(event)
	assert type(obj).__name__ == 'ObjectItem'


def test_deserialize_properly_formats_data():
	edata = contr.reference_with_columns('input', 'user', 123, **{'user': 123, 'some_id': 654, 'things': ['9']})
	dd = contr.reference_with_columns('input', 'user', '123', **{'user': 123, 'some_id': '654', 'things': [9]})


	obj = BaseEngine.deserialize(json.dumps(edata))
	oo = BaseEngine.deserialize(json.dumps(dd))

	assert isinstance(obj.sort_key, str)
	assert isinstance(oo.sort_key, str)
	assert obj.data['some_id'] == 654
	assert oo.data['some_id'] == '654'
	assert obj.data['things'] == ['9']
	assert oo.data['things'] == [9]
