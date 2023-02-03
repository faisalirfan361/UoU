import os
import simplejson as json
from boto3 import resource as b3_resource
from boto3.dynamodb.conditions import Key
from .base_engine import BaseEngine
from ..item_types.object_item import ObjectItem
from ..item_types.cache_item import CacheItem
from ..item_types.schedule_item import ScheduleItem


class QueryEngine(BaseEngine):
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
		# try to parse different shapes of data
		if data is None:
			self.data = {}
		elif isinstance(data, dict):
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

		super().__init__(key=key, client=client, source=source, data=data)
		self.funcname = os.getenv('QUERY_LAMBDA', 'uOne-ingest-associations-query')


	def get(self, _id=None, *cols) -> list:
		"""Execute a client call to a lambda which can interact with our datastore
		Params:
		------
		- _id : str default '*'
			used as a sort key search
		- cols : n number of str
			columns to be returned, used in a projection statement

		Returns:
		--------
		lambda response as cache item object(s)
		"""
		_id = _id if _id is not None else '*'
		print(f"Using '{self.funcname}' lambda to process query for: '{_id}'")

		# probably want to pick a cache class type here but for now a reference should work
		# ref = getattr(uonecq, obj_type)(key=self.key, client=self.client, source=self.source)
		ref = ObjectItem(key=self.key, client=self.client, source=self.source, data={col:None for col in cols})
		ref.reset_id(_id)

		payload = ref.serialize()
		print(f"Invoking '{self.funcname}' lambda with: {payload}")
		results = self._invoke_lambda(payload, self.funcname)
		results_count = len(results) if isinstance(results, list) else 1
		print(f"Found {results_count} results")
		items = [BaseEngine.deserialize(res) for res in results] if isinstance(results, list) else [BaseEngine.deserialize(results)]
		return items


	def read_from_datastore(self, item_reference):
		table_name = self._get_table_name(item_reference)
		table = self._dynamo_client(table_name)

		actual = []

		attr_names = [item_reference.sort_name, *item_reference.data.keys()]
		attr_names_map = {f"#RENAMETOKEN{a.replace('-', '').replace('_', '')}": a for a in attr_names}
		projection = ', '.join(attr_names_map.keys())

		if item_reference.sort_key == '*' or item_reference.sort_key is None:
			print(f"Running query(KeyConditionExpression=Key({item_reference.partition_name}).eq({item_reference.partition})), Select='SPECIFIC_ATTRIBUTES', ProjectionExpression={projection}, , ExpressionAttributeNames={attr_names_map}")
			request = {
				'KeyConditionExpression': Key(item_reference.partition_name).eq(item_reference.partition),
		        'Select': 'SPECIFIC_ATTRIBUTES',
		        'ProjectionExpression': projection,
		        'ExpressionAttributeNames': attr_names_map
			}
		else:
			print(f"Running query(KeyConditionExpression=Key({item_reference.partition_name}).eq({item_reference.partition}) & Key({item_reference.sort_name}).eq({item_reference.sort_key}), Select='SPECIFIC_ATTRIBUTES', ProjectionExpression={projection}, ExpressionAttributeNames={attr_names_map})")
			request = {
				'KeyConditionExpression': Key(item_reference.partition_name).eq(item_reference.partition) \
		        						& Key(item_reference.sort_name).eq(item_reference.sort_key),
		        'Select': 'SPECIFIC_ATTRIBUTES',
		        'ProjectionExpression': projection,
		        'ExpressionAttributeNames': attr_names_map
			}

		response = table.query(**request)
		raw_results = response.get('Items') or response.get('Item') or []
		raw_results = [raw_results] if isinstance(raw_results, dict) else raw_results
		print(f"Found '{len(raw_results)}' result(s), converting to CacheItems or a subclass...")

		for result in raw_results:
			print(f"Creating item from: {result}")
			item = type(item_reference)(key=self.key, client=self.client, source=self.source, data=result)
			print(f"Item: {item.address}")
			actual.append(item)

		return actual[0] if len(actual) == 1 else actual
