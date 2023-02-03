import os
import simplejson as json
import boto3
# from boto3 import resource as b3_resource
# from boto3.dynamodb.conditions import Key
from uone_caching.item_types.object_item import ObjectItem
from uone_caching.item_types.cache_item import CacheItem
from uone_caching.item_types.schedule_item import ScheduleItem


class BaseEngine():
	def __init__(self, key, client: str, source: str, data={}) -> None:
		"""Constructor

		Params:
		-------
		- key : str|dict
			Can be partition key or full complex-key
		- client : str
			Customer to which this data belongs
		- source : str
			Data source identifier for where this data came from
		- data : Object|Optional
			Data that can be used to derive certain parts of certain
			queries, particularly Associations
		"""

		# Set up for lazy evaluation, because we don't know what we're
		# doing yet
		self.key = key
		self.client = client
		self.source = source
		self.funcname = None # subclasses set this but now it's explicitly defined for everyone
		# try to parse different shapes of data
		if isinstance(data, dict):
			self.data = data
		elif isinstance(data, str):
			try:
				self.data = json.loads(data)
			except json.decoder.JSONDecodeError:
				print('data argument is not dict or JSON...')
				if ',' in data:
					print('Splitting data on "," and creating empty dict')
					self.data = {col:None for col in data.split(',')}
				else:
					raise KeyError('Cannot use data as it was given, must supply dict, JSON or comma-separated list of column names to be used in the projection')
		else:
			raise KeyError('Cannot use data as it was given, must supply dict, JSON or comma-separated list of column names to be used in the projection')


	def deserialize(json_record):
		"""Receive a serialized input and turn it into an instantiated object that can
		be used to process a later request.

		Params:
		-------
		- json_record : JSON str

			'{
				"type": "ObjectItem",
				"key": "template",
				"_id": "some-fake-template",
				"client": "fakeCustomer",
				"source": "fakeDataSource",
				"args": {'columns': ['email']},
				"address": {"object-type": {"S": "fakeCustomer-fakeDataSource-template"}, "object-id": {"S": "some-fake-template"}},
				"compiled": {"template": {"S": "{\\"some\\": 123, \\"keys\\": \\"in-a\\", \\"cool-scraping-template\\": [123, 321]}"}, "object-type": {"S": "fakeCustomer-fakeDataSource-template"}, "object-id": {"S": "some-fake-template"}}
			}'

		Returns:
		--------
		ObjectItem|CacheItem|ScheduleItem|...Item

		Notes:
		------
		Can accept `dict` instead of `JSON`
		"""
		print(f"Deserializing raw JSON: {json_record}")
		raw_record = json.loads(json_record) if isinstance(json_record, str) else json_record
		client = raw_record['client']
		source = raw_record['source']
		# partition key, also possibly used in finding _id if none supplied
		key = raw_record['key']
		# used to determine class of caching object, during instantiation
		obj_type = raw_record['type'] # 'ObjectItem'|'ScheduleItem'|'AssociationItem'|...
		# keys used in projections, values not used
		data = {**raw_record.pop('data', {}), **raw_record}

		obj_ref = {
			'cacheitem': CacheItem,
			'objectitem': ObjectItem,
			'scheduleitem': ScheduleItem,
		}[obj_type.lower().replace('_', '').replace('-', '')]

		print(f"Creating object from desearialized -> {obj_type}(key={key}, client={client}, source={source}, data={data}")
		if '_id' in data: # or key in data:
			_id = data.pop('_id')

			_typed_notes = data.pop('compiled', False)
			if _typed_notes:
				_typed_data = {k:obj_ref.from_typed(v) for k,v in _typed_notes.items()}
				obj = obj_ref(key=key, client=client, source=source, data=_typed_data)
			else:
				obj = obj_ref(key=key, client=client, source=source, data=data)

			print(f"Resetting ID for new record to '{_id}'")
			obj.reset_id(_id)
		else:
			obj = obj_ref(key=key, client=client, source=source, data=data)

		return obj


	################################################################################
	# Private
	################################################################################
	def _lambda_invoker(self):
		"""Get client to invoke lambda

		Returns:
		--------
		boto3 Lambda Client
		"""
		return boto3.client('lambda')


	def _invoke_lambda(self, payload: dict, funcname=None) -> dict:
		client = self._lambda_invoker()
		# payload should be b'bytes'|file
		payload = payload if type(payload).__name__ == 'bytes' else payload.encode('utf-8')
		response = client.invoke(FunctionName=self.funcname, InvocationType='RequestResponse', Payload=payload)

		res = None
		if 200 <= response['StatusCode'] < 300:
			if response['StatusCode'] == 202: return True
			res = response['Payload'].read().decode('utf-8')
			try:
				res = json.loads(res)
			except TypeError as te:
				if 'the JSON object must be str, bytes or bytearray, not dict' not in str(te):
					raise te

			if res['status'] == 'SUCCESS':
				return res['payload']
			else:
				raise Exception(f"Lambda failed with something we caught: {res.get('reason', 'unk')}")
		else:
			raise Exception(f"Invoking lambda failed with a 500-level error: {funcname} -> {response['FunctionError']}")


	def _get_table_name(self, cache_class_obj):
		if type(cache_class_obj).__name__ in ['ObjectItem']:
			return os.getenv('REFERENCE_TABLE', 'References')
		raise KeyError(f"Counld not find a table associated with '{type(cache_class_obj).__name__}' object...")


	def _dynamo_client(self, table=None):
		return boto3.client('dynamodb') if table is None else boto3.resource('dynamodb').Table(table)
