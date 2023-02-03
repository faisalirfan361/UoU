index_mappings = {
    'reference': {
        'partition-name': 'object-type',
        'partition-type': 'S',
        'sort-name': 'object-id',
        'sort-type': 'S'
    },
    'association': {
        'partition-name': 'assoc-type',
        'partition-type': 'S',
        'sort-name': 'assoc-link',
        'sort-type': 'S'
    },
    'mapping': {
        'partition-name': 'mapping-pointer',
        'partition-type': 'S',
        'sort-name': 'mapping-target',
        'sort-type': 'S'
    },
    'schedule': {
        'partition-name': 'action-type',
        'partition-type': 'S',
        'sort-name': 'key-time',
        'sort-type': 'N'
    }
}


def fake_template_data(nested=False, **cols):
	if nested:
		res = {
			"template": {
				"some": 123,
				"keys": "in-a",
				"cool-scraping-template": [123, 321],
				**cols
			}
		}
	else:
		res = {
			"some": 123,
			"keys": "in-a",
			"cool-scraping-template": [123, 321],
			**cols
		}

	return res


def fake_user_data(nested=False, **cols):
	if nested:
		res = {
			"user": {
				"firstname": cols.pop('firstname', 'fredward'),
				"id": cols.pop('id', 'abc-123'),
				**cols
			}
		}
	else:
		res = {
			"firstname": cols.pop('firstname', 'fredward'),
			"id": cols.pop('id', 'abc-123'),
			**cols
		}

	return res


def _base_insert_serialized():
	return {
		"type": None,
		"key": None,
		"_id": None,
		"client": None,
		"source": None,
		"data": None,
		"address": None,
		"compiled": None
	}


def _address(a_type, key, sort, client, source):
	return {
		index_mappings[a_type]['partition-name']: {index_mappings[a_type]['partition-type']: f"{client}-{source}-{key}"},
		index_mappings[a_type]['sort-name']: {index_mappings[a_type]['sort-type']: sort}
	}


def reference_with_columns(eng_type, key, _id, *acols, **kcols):
	cols = {**{c:None for c in acols}, **kcols}
	client = 'fakeCustomer'
	source = 'fakeSource'
	base = _base_insert_serialized()
	address = _address('reference', key, _id, client, source)

	base['type'] = 'ObjectItem' # !!! TODO !!! use other types
	base['key'] = key
	base['client'] = client
	base['source'] = source
	base['_id'] = _id
	base['address'] = address
	if eng_type == 'input':
		data = cols
	elif eng_type == 'response':
		data = fake_template_data(**cols) if cols.pop('template', False) else fake_user_data(**cols)
	else:
		raise KeyError(f"Pass 'query' or 'insert' for 'eng_type' param, given: '{eng_type}'")
	base['data'] = data
	base['compiled'] = {**compiled(**base['data']), **address}
	return base


def compiled(**kwargs):
	def _t(v):
		if not v:
			return 'NULL'
		else:
			try:
				v/1
				return 'N'
			except Exception:
				if isinstance(v, list):
					if len([n for n in v if not isinstance(n, str)]) == 0:
						return 'SS'
					elif len([n for n in v if not isinstance(n, int)]) == 0:
						return 'NS'
					else:
						return 'L'
				else:
					return 'S'


	return {k:{_t(v): True if _t(v) == 'NULL' else v} for k,v in kwargs.items()}


def cache_response(eng_type, key, _id, *data):
	client = 'fakeCustomer'
	source = 'fakeSource'

	items = []
	if len(data) == 1:
		item = compiled(**data[0])

		if 'user' in item:
			new_id = item['user']['S']['id']
			items = {new_id: item.pop('user')}
		else:
			new_id = item['id']['S']
			new_item = {new_id: item}
			items = new_item
	else:
		for d in data:
			item = compiled(**d)
			if 'user' in item:
				new_id = item['user']['S']['id']
				item[new_id] = item.pop('user')
				items.append(item)
			else:
				new_id = item['id']['S']
				new_item = {new_id: item}
				items.append(new_item)

	response = {
		'Items' if _id == '*' or _id is None else 'Item': items,
		'Count': len(items)
	}
	return response


def _dynamo_put_item_response(show=False, success=True, **kwargs):
	if show:
		return {
			'ResponseMetadata': {'HTTPStatusCode': 200 if success else 500},
		    'Attributes': kwargs,
		    'ConsumedCapacity': {},
		    'ItemCollectionMetrics': {}
		}
	else:
		return {
			'ResponseMetadata': {'HTTPStatusCode': 200 if success else 500}
		}
