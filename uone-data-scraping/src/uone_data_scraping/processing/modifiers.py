from functools import wraps
import os
import json
import re
import boto3
from copy import deepcopy as __copier
from . import dotmap
from uone_data_scraping.utils import helpers
from uone_caching.actions import InsertEngine
from uone_caching.actions import QueryEngine


__doc__ = """Functions that can be called from the HDN Data Scraping projects"""


def build_dotmap_from_it(obj, *args, event={}, **kwargs):
	"""Build a DotMap object from the given data

	Params:
	-------
	- obj : dict
	- args : list-like(any)
	- event : dict
	- kwargs : dict-like(key-value pairs)

	Returns:
	--------
	DotMap
	"""
	return dotmap.build(obj)

def _get_queue_client():
	"""Get an SQS client for interacting with Queue(s)

	Returns:
	--------
	boto3 sqs client
	"""
	sqs = boto3.client('sqs')
	return sqs


def cache_it(key, client, source, data):
	"""Use the caching interface to insert a record in the proper cache.  The key is
	used to help figure out which table this belongs to, the client AKA "customer" param
	will help us insert into the correct partition in Dynamo, etc.  This is all figured
	out for you so you don't have to program it into your scraping code; Because everything
	using the cache relies on the same contract, so we can rely on the data we need to be
	where it can be found using these params.

	Params:
	-------
	- key : str
		The key you plan on using as a partition key.  This key is often something like
		`agentId`.  This key makes up the suffix to the partition key for the index
		e.g.

			<client>-<key> : <key's identifier ID>

	- client : str
		The name of the customer for which this job is running
	- source : str
		The name of the data source for which this job is running
	- data : dict
		The data that will be cached.  not all data will always be cached. kwargs and
		the type of CacheItem sub-class

	Returns:
	--------
	partition_key+sort_key : str
	"""
	inserter = InsertEngine(key=key, client=client, source=source, data=data)
	res = inserter.cache('*')
	return res


def cache_and_seek_on_itter(client, source, data, key, *targets, **kwargs):
	"""Iterate through a collection, seek out certain data points you care about and then cache them.
	This is useful for adding things to a cache, as you are scraping.  An example is you can use this
	function when you list users to add all userIDs to the cache because other jobs will probably need
	to have userIDs in order to run, so you just granted them all the IDs they need by caching them
	when you had access.

	Params:
	-------
	- client : str
		The client/customer this data belongs to
	- source : str
		The data source you got this data from
	- data : collection
		This is usually the response from your data scrape request, during the `on-scrape` hooks.
		This may come in as a list, dict or other collection but the collection should always be iterable
	- key : str
		The data key where the specific data lives that you want to cache.  Usually you don't want to cache
		the entire row of the CSV you're processing or the entire JSON object for the JSON response you just
		got from the data source.  Instead, you'll want to cache a particular bit of information, like an
		agentId.  The key points at that particular bit of data you are interested in.  You'll know the key
		because you have seen the data source's response during testing.  Use the key they provide us in the
		response for the data you want to cache.
	- targets : n number of str optional
		Additional data that you want to cache.  This is the same functionality as the `key` parameter except
		in this case, you're interested in caching extra data from each record in the collection as opposed
		to the actual ID or email or something like that.  You can use this for cases when you see two useful
		IDs in the same respose; e.g. GET request for User data has a teamID in each record of the response.
		You can supply the 'teamID' as a `targets=['teamID']` param and ALSO cache the team ID for this user.
	- kwargs : key-value pair(s) optional
	Returns:
	--------
	response from sqs data dump
	"""
	data = data if isinstance(data, list) else [data]
	print(f"Running modifiers.cache_and_seek_on_itter({client}, {source}, data[{len(data)},...], {key}, {targets}, kwargs[{len(kwargs)},...])")
	for d in data:
		d = json.loads(d) if isinstance(d, str) else d
		vals = None

		if targets[0]=='*' or targets[0]=='.*':
			vals = d
		elif len(targets)==0:
			vals = d[key]
		else:
			vals = {k:v for k,v in d.items() if k in targets}

		cache_it(key, client, source, vals, **kwargs)


def cache_update_and_seek_on_itter(client, source, data, key, *targets, **kwargs):
	"""Iterate through a collection, seek out certain data points you care about and then cache them.
	This is useful for adding things to a cache, as you are scraping.  An example is you can use this
	function when you list users to add all userIDs to the cache because other jobs will probably need
	to have userIDs in order to run, so you just granted them all the IDs they need by caching them
	when you had access.

	Params:
	-------
	- client : str
		The client/customer this data belongs to
	- source : str
		The data source you got this data from
	- data : collection
		This is usually the response from your data scrape request, during the `on-scrape` hooks.
		This may come in as a list, dict or other collection but the collection should always be iterable
	- key : str
		The data key where the specific data lives that you want to cache.  Usually you don't want to cache
		the entire row of the CSV you're processing or the entire JSON object for the JSON response you just
		got from the data source.  Instead, you'll want to cache a particular bit of information, like an
		agentId.  The key points at that particular bit of data you are interested in.  You'll know the key
		because you have seen the data source's response during testing.  Use the key they provide us in the
		response for the data you want to cache.
	- targets : n number of str optional
		Additional data that you want to cache.  This is the same functionality as the `key` parameter except
		in this case, you're interested in caching extra data from each record in the collection as opposed
		to the actual ID or email or something like that.  You can use this for cases when you see two useful
		IDs in the same respose; e.g. GET request for User data has a teamID in each record of the response.
		You can supply the 'teamID' as a `targets=['teamID']` param and ALSO cache the team ID for this user.
	- kwargs : key-value pair(s) optional
	Returns:
	--------
	response from sqs data dump
	"""
	obj = data
	data = data if isinstance(data, list) else [data]
	print(f"Running modifiers.cache_update_and_seek_on_itter({client}, {source}, data[{len(data)},...], {key}, {targets}, kwargs[{len(kwargs)},...])")
	for d in data:
		d = json.loads(d) if isinstance(d, str) else d
		vals = None

		if targets[0]=='*' or targets[0]=='.*':
			vals = d
		elif len(targets)==0:
			vals = d[key]
		else:
			vals = {k:v for k,v in d.items() if k in targets}
		vals['is_update'] = 1
		cache_it(key, client, source, vals, **kwargs)
	return obj


def copy_and_set_on_itter(obj, collection, copier, setter_location, *args, event={}, **kwargs):
	"""Loop through a collection and set a given val on a given key in the copied object, which
	creates `len(collection)` number of copies of `obj`, where the copy has been updated at
	`setter_location` with an item from the `collection`.

	Params:
	-------
	- obj : dict-like
		an object that can be copied by `copier` and set using `setter_location`
	- collection : iterable
		collection to loop through and consume items from in a setter on
		the copied object, during iteration
	- copier : function
		function object which can accept the `obj` as a param and returns a copy of the `obj`
	- setter_location : str
		like the key for a dictionary lookup, it will be used on `obj[setter_location] = ...`
		during iteration
	- args : n-number of args
	- event : dict-like
		triggering event e.g. event in a lambda
	- kwargs : key-value pairs
	"""
	results = []
	try:
		# enforce a collection.  `str` is the only type i could think of that let's you iterate
		# like you're using a `list` but it's actually iterating over a `list` of `char` that
		# comprises your `str`.  So we check if it's a tricky `str`
		if isinstance(collection, str):
			collection = [collection]
		collection.__iter__()
	except AttributeError:
		collection = [collection]

	print(f"Running modifiers.copy_and_set_on_iter({type(obj).__name__}, collection[{len(collection)}], {copier}, {setter_location}")
	for item in collection:
		clone = copier(obj)
		print(f"Setting clone[{setter_location}] = {item}")
		clone[setter_location] = item
		results.append(clone)
	return results


def de_cache_it(client=None, source=None, grouping_term=None, identifier=None, *columns, **data):
	"""Query a cache, using the caching interface included in this module and
	the args passed to this function

	Params:
	-------
	- client : str default=None
		The client/customer for which we are running this function.  This usually comes from
		the event or other job processing configs passed to this processor
	- source : str default=None
		The data source we're interested in information for; e.g. nic, genesys, dialpad
	- key_suffix : str default=None
		the last part of the partition key in a complex key in the index of the cache,
		which is formatted like this:
			<client>-<source>-<key suffix> : <sort key, usually the actual ID for this obj>
		Usually this is the type of object/user/resource you want to get information for; e.g. 'agentId's
	- sort_key : str default=None
		This is usually the actual ID for the resource/record in the cache.  If you're looking up agents,
		this is probably their actual ID, in the 3rd party data source we're pulling data from, for example.
	- event : dict default={}
		Usually the lambda event that caused this function to be run.  This is
		usually because of a data scraping job that requires looking up resource
		IDs to fill out the parameters in an API request.  For those use cases,
		these events are the events used to trigger a scrape job and you can consult
		those docs for exact event structure and data.
	- kwargs : key-value pairs optional
		any key-value pairs used to configure the caching interface and/or its query

	Returns:
	--------
	Objects created from the data in the cache record retrieved from the query
		: `CacheItem`|`ObjectItem`|`ScheduleItem`
	"""

	if grouping_term is None and identifier is None:
		raise KeyError('If "grouping_term" is None then "identifier" must be provided but both were None...')

	print(f"Running modifiers.de_cache_it with: {client}, {grouping_term} + {columns} and {data}")
	col_ref = {**{k:None for k in columns}, **{k:None for k in data}}
	print(f"Grouping columns and data into a combined dict, such that they can be used to inform a 'projection' in the caching system -> {col_ref}")
	q = QueryEngine(key=grouping_term, client=client, source=source, data=col_ref)
	result = None


	if identifier:
		if len(col_ref) >= 1:
			print(f"Using columns to find ref -> ID: {identifier}, columns: {list(col_ref.keys())}")
			result = q.get(identifier, *list(col_ref.keys()))
		else:
			print(f"Using ID to find ref: id: {identifier}")
			result = q.get(identifier)
	else:
		if len(columns) > 1:
			result = q.get('*', *columns)
		else:
			result = q.get('*')
	# result = q.get(identifier) if identifier else q.get()

	_prepare_response = lambda r,*cols: r.data[cols[0]] if len(cols) == 1 else {c:r.data.get(c) for c in cols}
	response = [_prepare_response(record, *columns) for record in result]

	print('Successful modifiers.de_cache_it call')
	return response


def dequeu_it(queue_url, event={}, **kwargs):
	"""Pull a message from a Queue

	Params:
	-------
	- queue_url : str
		The URL for the SQS::Queue that you want to pull messages from
	- event : dict default={}
		Usually the lambda event that caused this function to be run.  This is
		usually because of a data scraping job that requires looking up resource
		IDs to fill out the parameters in an API request.  For those use cases,
		these events are the events used to trigger a scrape job and you can consult
		those docs for exact event structure and data.
	- kwargs : key-value pairs optional
		any key-value pairs you need to configure the client and/or it's pull

	Returns:
	--------
	Queue message, including payload and header-type information from SQS : `dict`
	"""
	print(f"Running modifiers.dequeu_it({context}, {queue_url}, {args}, {event})")
	print(f"Using QueueUrl={queue_url}")
	msg = sqs.receive_message(QueueUrl=args[0], MaxNumberOfMessages=1)

	if msg is None or ('Messages' not in msg.keys()):
		print('No message found during modifiers.dequeu_it run, returning None')
		return None

	body = msg['Messages'][0]['Body']
	print('Found message body')
	body = json.loads(body) if isinstance(body, str) else body
	print('Loaded message into JSON body...')
	return body


def enqueue_it(queue_url, message, event={}, **kwargs):
	print(f"Running modifiers.enqueue_it with queue: {queue_url}, message: {message} and {event}")
	sqs.send_message(QueueUrl=queue_url, message=message)
	return args


def for_each_it(collection, mapping, *args, event={}, **kwargs):
	"""Apply a given `mapping` to a collection, using args, event and kwargs

	Params:
	-------
	- collection : iterable
	- mapping : function
	- args : n number of Object
	- kwargs : n number of key-value args

	Returns:
	--------
	list of Objects from the collection that had a mapping applied to them : list
	"""
	return [mapping(context, item, *args, **event) for item in collection]


def format_it(raw, *format_args, **format_kwargs):
	"""String format a given string, using a dict of formatting variables

	Params:
	-------
	- raw : str
		The raw string you wish to format.  Placeholders are required to format
		and the placeholders are replaced with the `format_args` and `format_kwargs`
	- format_args : n number of str
		Used to replace placeholders in the string.
		Used in order from first to last, replace the placeholders in raw
		in order from first to last.
	- format_kwargs : n number of key-value pairs
		The key will be used to replace the placeholder in `raw` with the same
		name and the value will be used in the formatting of the string.
			'my_{fancy} sentence{}'.format(**{'fancy': 'your string', 'boogers': 123})
			=> 'my_your string sentence'

	Returns:
	--------
	str

	Notes:
	------
	- Priority is good to understand if you're going to start sending in complex use cases.
	1. named placeholders are matched with given format_kwargs
	2. placeholders that weren't matched with format_kwargs keys are matched with format_args
		in the order they were given with the order of remaining named placeholders
	3. the rest of the empty placeholders will be filled with the rest of the unused `format_args`
	`format_kwargs` will be used first, then `format_args` will be used to fill
	every other placeholder in `raw` that's left.
	"""
	placeholders = re.findall(r"{(\w+)}", raw)
	remainder_placeholders = [p for p in placeholders if p not in format_kwargs]
	num_remaining_args = len(remainder_placeholders)
	# Gather all kwargs for formatting based on the raw string that was given, then the given kwargs, then
	# the available args
	formatters = {**format_kwargs, **dict(zip(remainder_placeholders, format_args[:num_remaining_args]))}
	# Use all remaining args for empty {} placeholder values in the raw param
	empty_placeholder_vals = format_args[num_remaining_args:]

	return raw.format(*empty_placeholder_vals, **formatters)


def _get_cache(table_name):
	"""Get a cache table

	Params:
	-------
	- table_name : str

	Returns:
	--------
	Dynamo Db Client from boto3
	"""
	print(f"Retrieving '{table_name}' cache...")
	client = boto3.client('dynamodb')
	return client.table(table_name)


def get_it(obj, receiver_pointer):
	"""Retrieve a value from a given dict object

	Params:
	-------
	- obj : dict
	- receiver_pointer : str

	Returns:
	--------
	value from the `obj` dict at the given `receiver_pointer` : Object
	"""
	if not isinstance(obj, dict):
		# it might work as is if obj is a DotMap
		val = getattr(obj, receiver_pointer)
	else:
		# this needs to split on '.' and get the nested objects too.
		val = obj[receiver_pointer]
	return val


def date_today_ts(type):
	print(f"helpers._parse_event_for_date('today') running ")
	today = helpers._parse_event_for_date('now')
	resp = {
		"ts": today.isoformat(),
		"day": helpers.day_of(today),
		"hour": helpers.hour_of(today),
		"year": helpers.year_of(today)
	}
	return resp[type]

def skip_if_present(collection, location):
	"""Filter a collection based on whether or not an item at a location is "truthy".
	If the collection is a dictionary, we'll try to look for any key that matches the
	`location` param and we'll also look at a value if the key is present.  If the key
	exists or the value at the key is truthy, the entire item will be filtered out.
	If the collection is a dictionary of dictionaries, we look in the dictionaries too.
	If the collection is a list, we'll look for exact matches to the location.  It's not an
	index, it's an item matcher.
	If the list is a list of dictionaries, we search each dictionary for the key and truthiness.

	Params:
	- collection
		collection that will be filtered and returned
	- location
		key in a dictionary or item from a list that we'll use to match and filter
	"""
	if isinstance(collection, dict):
		if isinstance(list(collection.values())[0], dict):
			return {k:v for k,v in collection.items() if not (k in location or location in v and v[location])}
		else:
			return {k:v for k,v in collection.items() if not (k == location or v == location)}
	else: # assumes collection is list-like
		if isinstance(collection[0], dict):
			return [c for c in collection if not (location in c and c[location])]
		else:
			return [c for c in collection if c != location]


def skip_if_not_present(collection, location):
	"""Filter a collection based on whether or not an item at a location is "truthy".
	If the collection is a dictionary, we'll try to look for any key that matches the
	`location` param and we'll also look at a value if the key is present.  If the key
	doesn't exist or the value at the key is falsey, the entire item will be filtered out.
	If the collection is a dictionary of dictionaries, we look in the dictionaries too.
	If the collection is a list, we'll look for exact matches to the location.  It's not an
	index, it's an item matcher.
	If the list is a list of dictionaries, we search each dictionary for the key and falsiness.

	Params:
	- collection
		collection that will be filtered and returned
	- location
		key in a dictionary or item from a list that we'll use to match and filter
	"""
	if isinstance(collection, dict):
		if isinstance(list(collection.values())[0], dict):
			return {k:v for k,v in collection.items() if (k not in location or (location in v and not v[location]))}
		else:
			return {k:v for k,v in collection.items() if (k == location or v == location)}
	else: # assumes collection is list-like
		if isinstance(collection[0], dict):
			return [c for c in collection if (location in c and c[location])]
		else:
			return [c for c in collection if c == location]


# def _pass_through(function, args=False, context=True, event=True, results=True):
# 	def _outter(fn):
# 		@wraps(fn)
# 		def _inner(context, *args, **event):
# 			"""Wrapper that always responds with a tuple that includes
# 			the originally supplied context and event (even if a mutation occured on either)
# 			as the first two elements of a tuple and the wrapped function's result as the third."""
# 			result = function(context, *args, **event)
# 			return (context, event, result)
# 		return _inner
# 	return _outter


def pass_through(*args, **kwargs):
	"""Do nothing with the arguments and allow processing to continue with no changes.
	This is useful for when no processing steps exist but we still want to complete
	all steps of the processing hooks that are available.  If no processing steps are
	found in the templates or picked up through overrides, a pass-through operation
	should be defaulted in your processing.

	Params:
	-------
	- args : n number of `Ojbect`
	- kwargs : key-value pair(s)

	Returns:
	--------
	first arg received : `Object`
	"""
	print(f"Running pass_through({args}, {kwargs}) -> {args[0]}")
	return args[0]


def print_it(*args, event={}, **kwargs):
	"""Print all received items, often useful for debugging or progress in logs

	Params:
	-------
	- args : n number of `Object`
	- event : dict default={}
	- kwargs : key-value pair(s)

	Returns:
	--------
	list of all collected items : `list`
	"""
	args = args[0] if len(args) == 1 else args
	items = [str(item) for item in [args, event] if item]
	print(f"{', '.join(items)}")
	return items


def pop_it(obj, key, **kwargs):
	"""Pop an key-value pair off a dict-like object

	Params:
	-------
	- obj : dict-like
		Any object that respondes to the `pop` function
	- key : str
		The key for which you want to remove the key-value pair of

	Returns:
	--------
	the value that was popped : `Object`
	"""
	return obj.pop(key)


def pull_it(*args, event={}, **kwargs):
	"""Retrieve messages from <db, queue, etc?>
	"""
	print(f"!! modifiers.pull_it() has not been implemented yet, can't do anything with {args} or {event}!!")
	# return args
	return 'stuff'

def trigger_step_function(obj, client, source, sessionid, path, agentId):
	print(f"!! modifiers.trigger_step_function() has not been implemented yet, can't do anything with {source} or {client} or {sessionid} or {path} !!")
	
	sfn_client = boto3.client('stepfunctions')

	state_machine_arn = os.getenv('SCRAPER_MACHINE')

	response = sfn_client.start_execution(
		stateMachineArn=state_machine_arn,
		input=json.dumps({ 
			'source': source,
			'client': client,
			'sessionId': sessionid,
			'endpoint': path,
			'agentId': str(agentId)
		})
	)

	return obj

def set_it(obj, key, val, event={}, **kwargs):
	"""Set a value on an object at a key.

	Params:
	-------
	obj : dict-like
		the object to be mutated
	key : str
		the dict key at which to make the insert/update
	val : any
		the value with which to set the dict at the given key
	event : dict optional
		usually a lambda event or similar
	kwargs : key-value pairs
		not used, open for later implimentations...

	Returns:
	--------
	the object given as the `obj` param, mutated with the `val` at `key` path : dict

	Notes:
	------
	This implimentation only works to 10 levels of nesting.
	"""
	if type(obj).__name__ == 'DotMap':
		print(f"Setting '{val}' onto '{key}' as DotMap...")
		obj[key] = val
		print(f"Set object: {obj}")
		return obj
	else:
		rec_path = key.split('.')
		print(f"Setting '{val}' onto '{rec_path}'' of obj...")
		if len(rec_path) == 1:
			obj[rec_path[0]] = val
		elif len(rec_path) == 2:
			obj[rec_path[0]][rec_path[1]] = val
		elif len(rec_path) == 3:
			obj[rec_path[0]][rec_path[1]][rec_path[2]] = val
		elif len(rec_path) == 4:
			obj[rec_path[0]][rec_path[1]][rec_path[2]][rec_path[3]] = val
		elif len(rec_path) == 5:
			obj[rec_path[0]][rec_path[1]][rec_path[2]][rec_path[3]][rec_path[4]] = val
		elif len(rec_path) == 6:
			obj[rec_path[0]][rec_path[1]][rec_path[2]][rec_path[3]][rec_path[4]][rec_path[5]] = val
		elif len(rec_path) == 7:
			obj[rec_path[0]][rec_path[1]][rec_path[2]][rec_path[3]][rec_path[4]][rec_path[5]][rec_path[6]] = val
		elif len(rec_path) == 8:
			obj[rec_path[0]][rec_path[1]][rec_path[2]][rec_path[3]][rec_path[4]][rec_path[5]][rec_path[6]][rec_path[7]] = val
		elif len(rec_path) == 9:
			obj[rec_path[0]][rec_path[1]][rec_path[2]][rec_path[3]][rec_path[4]][rec_path[5]][rec_path[6]][rec_path[7]][rec_path[8]] = val
		elif len(rec_path) == 10:
			obj[rec_path[0]][rec_path[1]][rec_path[2]][rec_path[3]][rec_path[4]][rec_path[5]][rec_path[6]][rec_path[7]][rec_path[8]][rec_path[9]] = val
		print(f"Set object: {obj}")
		return obj


def string_replace_it(string, pattern, replacement, event={}, **kwargs):
	"""Replace a pattern in a string with another string

	Params:
	-------
	- string : str
		The raw string which contains the pattern you wish to replace, using the remaining params
	- pattern : str
		Regex to search the string against.  All matches to this pattern
		will be replaced with the `replacement` arg
	- replacement : str
		String to replace the `pattern` with, in the `string` param
	- event : dict default={}
		not really used...just left open for integration ease.  probably safe to remove
	- kwargs : n number of key-value arts optional
		not really used...just left open for integration ease.  probably safe to remove

	Returns:
	--------
	str
	"""
	return str(string).replace(str(pattern), str(replacement))
