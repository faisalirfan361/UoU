import os
import simplejson as json
import boto3
from .base_engine import BaseEngine
from ..item_types.object_item import ObjectItem
from ..item_types.schedule_item import ScheduleItem


class InsertEngine(BaseEngine):
	def __init__(self, key: str, client: str, source: str, data={}):
		# The idea here is to capture all non-columnar data in a `payload` column
		# in the cache, so we always know where this item will be and we save on
		# adding lots of columns in the dynamo References table
		data = data if isinstance(data, dict) else {'payload':data}
		# if we don't like using 'payload' to capture things like JSON (templates)
		# then we can use the following line instead, which will cache things at
		# a column with the same name as the cache item's `key` AKA `partition_key`
		# data = data if isinstance(data, dict) else {key:data}
		super().__init__(key=key, client=client, source=source, data=data)
		self.funcname = os.getenv('INSERT_LAMBDA', 'uOne-ingest-associations-insert')


	def cache(self, *keys):
		"""Insert or update a record in the cache, based on the data, the `key` and
		any special ID we get here, as a first argument.

		Params:
		-------
		- keys: n number of str
			- if no args supplied, cache the entire `data` from `InsertEngine()` at the given
				`sort_key` as `key`
			- if only 1 arg:
				A. if 1st arg == '*', use all columns in the data that was supplied in InsertEngine()
				B. if 1st arg == `key`, use '*' as ID and add only the `key` column from the data
				C. if 1st arg != `key` but != '*', use as custom ID and add any remaining to cache as columns from the data
			- if 2 args or more:
				- if 1st arg was A, '*' is used for ID and all columns are cached
				- if 1st arg was B or C, all following args will be used to pick columns from `data` to cache

		Returns:
		--------
		response from uOne-ingest-associations INSERT request
			{
				'status': 'SUCCESS'|'FAILED',
				'payload': ...
			}


		Notes:
		------
		The code is more accurate than the documentation.  The docs list use cases
		of arguments but the implimentation changes might change the subtle difference
		in that list of args and what they mean, so read the docs and accept that they're
		probably 80% or better accurate but it's possible they're off.

		If the tests still pass, the code is accurate, so that's your source of truth...
		"""
		_id = None
		keys = list(keys)
		print(f"Caching item via uOne-ingest-associations-insert lambda, preparing record metadata with keys: {keys}...")
		if len(keys) == 1:
			if keys[0] == '*':
				_id = self.key
				keys = list(self.data.keys())
			else:
				_id = keys.pop(0)
		elif len(keys) >= 2:
			_id = keys.pop(0)
			if keys[0] == '*':
				keys = list(self.data.keys())
		else:
			# this will be a catch-all.
			# for all uncaught cases or
			# for all cases where '*' applies globally,
			# 	for this partition.
			_id = '*'

		# this is hard coded to always use the ObjectItem class but it should mirror
		# the logical steps that we will create in `_get_table_name` function.
		item = ObjectItem(key=self.key,
						  client=self.client,
						  source=self.source,
						  data={k:v for k,v in self.data.items() if k in keys})

		if item.sort_key is None or self.key != _id:
			print(f"Setting sort_key to: {_id}")
			item.reset_id(_id)
		else:
			print(f"Because sort_key is not None, it was left as is: {item.sort_key}")

		print(f"New ObjectItem: {item.serialize()}")
		return self._invoke_lambda(item.serialize())


	def write_to_datastore(self, cache_class_obj, return_values=None, is_update=None):
		"""Send a cache item to the cache

		Params:
		-------
		- cache_class_obj : `ObjectItem`|`ScheduleItem`|`CacheItem`
		"""
		return_values = False if return_values is None else return_values
		is_update = False if is_update is None else is_update

		client = self._dynamo_client()
		table_name = self._get_table_name(cache_class_obj)

		print(f"Sending to {table_name}: {cache_class_obj.data}")
		if not is_update:
			result = client.put_item(
				TableName=table_name,
				Item=cache_class_obj.to_record(),
				ReturnValues='ALL_OLD' if return_values else 'NONE' # add this if we do a diff on new/old
			)
		else:
			key_values = {}
			values_only = []
			for key, value in cache_class_obj.address.items():
				key_values[key] = value['S']
				values_only.append(value['S'])

			expression_meta = cache_class_obj.to_update_expression_values(values_only)
			print(f"key_values {expression_meta}")
			result = client.update_item(
				TableName=table_name,
				Key=cache_class_obj.address,
				UpdateExpression='SET ' + expression_meta["UpdateExpression"],
				ExpressionAttributeValues=expression_meta["ExpressionAttributeValues"],
				ReturnValues='UPDATED_NEW' if return_values else 'NONE' # add this if we do a diff on new/old
			)

		if 'ResponseMetadata' in result \
			and 'HTTPStatusCode' in result['ResponseMetadata'] \
			and result['ResponseMetadata']['HTTPStatusCode'] == 200:
			return cache_class_obj
		elif 'Attributes' in result:
			cache_class_obj.data = {**result['Attributes'], **cache_class_obj.data}
			return cache_class_obj
		else:
			raise KeyError(f"Something failed during insert at cache but no exception could be found: {result}")

		return cache_class_obj


	################################################################################
	# Private
	################################################################################
	def _get_table_name(self, cache_class_obj):
		"""Use logical steps to determine which table to hit by using the data in
		cache_class_obj and the query being run.


		Notes:
		------
		the implementation as of 11/01 is basically hard coded.  when we are able
		logical steps should be taken here to determine which table to use, based
		on the type of query we're running in the cache_class_obj and this engine
		instance.
		"""
		if type(cache_class_obj).__name__ in ['ObjectItem']:
			return os.getenv('REFERENCE_TABLE', 'References')
		raise KeyError(f"Counld not find a table associated with '{type(cache_class_obj).__name__}' object...")


	def _dynamo_client(self):
		return boto3.client('dynamodb')


	def _lambda_invoker(self):
		"""Get client to invoke lambda

		Returns:
		--------
		boto3 Lambda Client
		"""
		return boto3.client('lambda')


	def _invoke_lambda(self, payload: dict) -> dict:
		"""Directly invoke a lambda in uOne-ingest-associations
		"""
		client = self._lambda_invoker()
		# payload should be b'bytes'|file
		print(f"Invoking uOne-ingest-associations-insert lambda with: {payload}")
		res = client.invoke(FunctionName=self.funcname, InvocationType='RequestResponse', Payload=payload)

		if 200 <= res['StatusCode'] < 300:
			print(f"Received successfull response from lambda, responding with SUCCESS")
			return res['Payload'].read().decode('utf-8')
		else:
			raise Exception(f"Invoking lambda failed with a 500-level error: {funcname}")
