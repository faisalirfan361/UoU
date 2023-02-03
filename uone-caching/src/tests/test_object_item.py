import pytest
from unittest.mock import patch, Mock
from uone_caching.item_types.object_item import ObjectItem


def make_event(key='agentId', val=123456, **kwargs):
	return {
		**{
			'client': 'abc-123',
			key: val,
			'source': 'some-api'
		},
		**kwargs
	}


def test_ObjectItem_fails_for_missing_partition_key():
	event = make_event(None, 12345)
	e = {k:v for k,v in event.items() if k != 'user_id'}
	with pytest.raises(KeyError) as e_info:
		cc = ObjectItem(e)


def test_ObjectItem_finds_id_from_sort_name():
	data = {'admin_office_ids': ['5743137921302528'], 'company_id': '4617238014459904', 'country': 'us', 'date_active': '2019-06-17T18:08:53.439000', 'date_added': '2019-06-17T13:48:31.392000', 'date_first_login': '2019-06-17T21:12:56.665000', 'do_not_disturb': False, 'emails': ['adrianne.meaders@homie.com'], 'first_name': 'Adrianne', 'forwarding_numbers': ['+18016476444'], 'id': '4506563412099072', 'image_url': 'https://dialpad.com/avatar/user/agxzfnViZXItdm9pY2VyGAsSC1VzZXJQcm9maWxlGICA2Pqg1oAIDA.png?version=15e6cee6d8daa619f6774858545533f4', 'is_admin': False, 'is_available': True, 'is_on_duty': False, 'is_online': False, 'is_super_admin': False, 'language': 'en', 'last_name': 'Meaders', 'license': 'talk', 'muted': False, 'office_id': '5743137921302528', 'phone_numbers': ['+13859556878'], 'state': 'active', 'timezone': 'US/Pacific', 'user': '4506563412099072'}
	key = 'user'
	oi = ObjectItem(key, client='abc-123', source='some-api', data=data)
	assert oi.sort_key == '4506563412099072'


def test_ObjectItem_can_automatically_find_sort_key_from_data_when_key_exists_in_data_only():
	event = make_event('agentId', 12345, **{'object-id': 65432})
	event['key'] = 'agentId'
	oi = ObjectItem(**event)
	assert oi.sort_key == 65432


def test_ObjectItem_fails_for_missing_sort_key():
	event = make_event('agentId', None)
	e = {k:v if k != 'agentId' else None for k,v in event.items()}
	with pytest.raises(KeyError) as e_info:
		cc = ObjectItem(e)


def test_ObjectItem_fails_for_missing_client_key():
	event = make_event('agentId', 12345)
	e = {k:v for k,v in event.items() if k != 'client'}
	with pytest.raises(KeyError) as e_info:
		cc = ObjectItem(e)


def test_ObjectItem_can_init_with_valid_keys():
	ev = 'randomstuff'
	event = make_event(ev, 'zzz-999')
	cc = ObjectItem(ev, event)
	assert cc.partition_key == ev


def test_partition_has_proper_format():
	ev = 'target'
	event = make_event(ev, 'zzz-999')
	cc = ObjectItem(ev, event)
	assert cc.partition == f"{event['client']}-{event['source']}-{ev}" #-{cc.partition_key}"


def test_key_casts_values_to_str():
	event = {'client': 'c123', 'source': 's123', 'departmentId': 'd483'}
	oi = ObjectItem('departmentId', event)
	for k,v in oi.to_record().items():
		assert isinstance(v['S'], str)


def test_ObjectItem_creates_S_types_for_primary_key():
	event = make_event('agentId', 12345)
	cc = ObjectItem('agentId', event)

	assert list(cc.to_record()['object-type'].keys())[0] == 'S'
	assert list(cc.to_record()['object-id'].keys())[0] == 'S'
