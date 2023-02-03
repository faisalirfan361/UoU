import os, json
from uone_data_scraping.processing import dotmap


# i forgot how to do fixtures...
def big_nic_template():
    # Last time I checked, I should expect these keys to be directly accessible on a new object:
    # - `config`
    # - `config.method`
    # - `method`
    # - `config.endpoint`
    # - `endpoint`
    # - `preprocess`
    # - `preprocess.results.save_path.start_time`
    # - `results.save_path.start_time`
    # - `results`
    # - `results.save_path`
    # - `save_path`
    # - `params`
    # - `params.startDate`
    # - `startDate`
    # - `params.endDate`
    # - `endDate`
    # - `postprocess`
    # - `postprocess.config.save_path`
    # - `config.save_path`
	# return templates._nic_configs()['schedule-adherence']
	return {
	    "bucketed_by": None,
	    "before-generate": [
	        {
	            "set_it": [
	                "$$event",
	                "agentsIds",
	                {
	                    "de_cache_it": [
	                        {
	                            "get_it": [
	                                "$$event",
	                                "client"
	                            ]
	                        },
	                        {
	                            "get_it": [
	                                "$$event",
	                                "source"
	                            ]
	                        },
	                        "agentId"
	                    ]
	                }
	            ]
	        }
	    ],
	    "on-generate": [
	        {
	            "copy_and_set_on_itter": [
	                "$$config",
	                {
	                    "get_it": [
	                        "$$event",
	                        "agentsIds"
	                    ]
	                },
	                "__copier",
	                "agentId"
	            ]
	        },
	        {
	            "set_it": [
	                "$$config",
	                "config.save_path",
	                {
	                    "format_it": [
	                        {
	                            "get_it": [
	                                "$$config",
	                                "save_path"
	                            ]
	                        },
	                        {
	                            "get_it": [
	                                "$$event",
	                                "source"
	                            ]
	                        },
	                        {
	                            "get_it": [
	                                "$$event",
	                                "client"
	                            ]
	                        },
	                        {
	                            "get_it": [
	                                "$$config",
	                                "agentId"
	                            ]
	                        }
	                    ]
	                }
	            ]
	        },
	        {
	            "set_it": [
	                "$$config",
	                "query_params",
	                {
	                    "get_it": [
	                        "$$config",
	                        "agentId"
	                    ]
	                }
	            ]
	        }
	    ],
	    "config": {
	        "bucket": "{STAGING_BUCKET}",
	        "save_path": "{source}/agents-client-data/customer={client}/{agentId}.txt"
	    },
	    "request_config": {
	        "method": "GET",
	        "endpoint": "services/v20.0/agents/client-data"
	    },
	    "params": {},
	    "query_params": {
	        "agentId": "{agentId}"
	    },
	    "data_key": "data"
	}


def test_pop_allows_for_a_default():
	dm = dotmap.build({'hey': 'yo'})
	res = dm.pop('wowow', [])
	assert isinstance(res, list)


def test_dotmap_is_json_serializable_ish():
	dm = dotmap.build({'hey': 'yo'})
	res = json.dumps(dict(dm))
	assert isinstance(res, str)
	assert json.loads(res)['hey'] == 'yo'


def test_pop_removes_item_from_dotmap():
	dm = dotmap.build({'hey': 'yo'})
	res = dm.pop('hey', [])
	assert 'hey' not in dm.keys()


def test_pop_returns_val_for_removed_key():
	val = 'yo'
	dm = dotmap.build({'hey': val})
	res = dm.pop('hey', [])
	assert res == val


def test_None_is_returned_on_missing_attr():
	val = 'yo'
	dm = dotmap.build({'hey': val})
	res = dm.wowow
	assert res == None


def test_exception_is_raised_on_missing_ref():
	val = 'yo'
	dm = dotmap.build({'hey': val})
	try:
		res = dm['wowow']
		assert False, 'Should have raised exception...'
	except Exception:
		assert True, 'Properly raised exception on missing item'


def test_dotmap_can_create_empty_obj():
	dm = dotmap.build({})
	assert len(dm) == 0


def test_dotmap_can_ref_single_element_obj():
	dm = dotmap.build({'hey': 'yo'})
	assert list(dm.keys()) == ['hey']


def test_dotmap_can_ref_complex_collection_obj():
	template = big_nic_template()
	dm = dotmap.build(template)

	for k in template.keys():
		assert k in dm.keys()


def test_nested_ref_has_single_key_ref():
	template = big_nic_template()
	dm = dotmap.build(template)
	assert dm.save_path == template['config']['save_path']


def test_update_with_nested_key_sets_root():
	template = big_nic_template()

	dm = dotmap.build(template)
	dm['config.save_path'] = 'snargle-bargle'
	assert dm.save_path == 'snargle-bargle'


def test_update_with_dot_sets_nested_collections():
	template = big_nic_template()
	dm = dotmap.build(template)
	update = 'fleep-florp'
	dm['config.save_path'] = update

	assert dm['config.save_path'] == update
	assert dm.save_path == update
	assert dm['config']['save_path'] == update


def test_nested_ref_has_dotted_key_ref():
	template = big_nic_template()
	dm = dotmap.build(template)
	assert dm['config.save_path'] == template['config']['save_path']


def test_ref_to_list_maintains_the_val():
	template = big_nic_template()
	template['preprocess'] = ['some_funcname', ('param', 'one', 'two')]
	dm = dotmap.build(template)
	# any list as a value is a viable test
	# a dict is not viable because the intended behavior is to explode nested dictionaries
	assert isinstance(dm.preprocess, list)


def test_can_add_with_include():
	template = big_nic_template()
	template['preprocess'] = ['some_funcname', ('param', 'one', 'two')]
	dm = dotmap.build(template)

	dm.include({'some':'data','goes':'here'})
	assert dm.goes == 'here'
	assert dm.some == 'data'


def test_base_level_keys_remain():
	template = big_nic_template()

	dm = dotmap.build(template)
	assert dm.preprocess is None

	template['preprocess'] = ['some_funcname', ('param', 'one', 'two')]
	dm = dotmap.build(template)
	assert dm.preprocess is not None

	assert dm.config == template['config']
	assert dm.params == template['params']


def test_dotmap_can_jsonize():
	template = big_nic_template()
	conf = dotmap.build(template)

	try:
		res = conf.to_json()
		assert isinstance(res, str)
		nconf = json.loads(res)
		for k in conf.keys():
			assert k in list(nconf.keys())
	except Exception:
		assert False, f"Config was not JSON serializable...{conf}"


def test_updates_to_root_update_root():
	template = {'ping': 'pong', 'fleep': 'florp', 'some': {'nested1': {'nested2': 'gotit', 'bad':'badness'}, 'arr1': [1,2,3]}}

	dm = dotmap.build(template)
	assert dm.thingz is None
	update = 'somestuff'
	dm['thingz'] = update
	assert dm.thingz == update
	assert dm['thingz'] == update


def test_all_keys_exist():
	# template = {'ping': 'pong', 'fleep': 'florp', 'some': {'nested1': {'nested2': 'changeme', 'bad':'badness'}, 'arr1': [1,2,3]}}
	template = {'some': {'nested1': {'nested2': 'changeme', 'bad':'badness'}, 'arr1': [1,2,3]}}
	expected = ['some', 'some.nested1', 'some.nested1.nested2', 'some.nested1.bad', 'arr1', 'nested1',
				'nested2', 'bad', 'nested1.nested2', 'nested1.bad']

	dm = dotmap.build(template)
	for k in expected:
		assert k in dm.keys()
	for k in dm.keys():
		assert k in expected

	assert len(set(dm.keys()) - set(expected)) == 0
	assert len(set(expected) - set(dm.keys())) == 0


def test___init__yields_returns_already_instantiated_param_if_param_was_dotmap():
	template = {'ping': 'pong', 'fleep': 'florp', 'some': {'nested1': {'nested2': 'gotit', 'bad':'badness'}, 'arr1': [1,2,3]}}

	dm = dotmap.build(template)
	dm2 = dotmap.build(dm)
	assert dict(dm2) == dict(dm)


def test__init__yields_snapshot_of_previous_instance():
	template = {'ping': 'pong', 'fleep': 'florp', 'some': {'nested1': {'nested2': 'gotit', 'bad':'badness'}, 'arr1': [1,2,3]}}

	dm = dotmap.build(template)
	dm2 = dotmap.build(dm)

	for k,v in dm.items():
		assert dm2[k] == v


def test__init__yields_copy_but_not_pointer():
	template = {'ping': 'pong', 'fleep': 'florp', 'some': {'nested1': {'nested2': 'gotit', 'bad':'badness'}, 'arr1': [1,2,3]}}

	dm = dotmap.build(template)
	dm2 = dotmap.build(dm)
	path = ['some', 'nested1']
	dm[path[0]][path[1]] = {'uh': 'oh'}
	assert dm2[path[0]][path[1]] != dm[path[0]][path[1]]
