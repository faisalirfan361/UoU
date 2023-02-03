import re
import simplejson as json

from .. import utils


class CacheItem(object):
	"""
	Object formatter base class to get events into the format they need to
	be in for a particular cache.
	The cache names can be logically found so this
	class helps to centralize logic for creating and referencing cache items
	in this cache.
	"""
	REQUIRED_KEYS = ['client', 'source']
	ALL_TYPES = ['NULL', 'BOOL', 'L', 'M', 'BS', 'NS', 'SS', 'B', 'S', 'N']


	def __init__(self, *args, **kwargs):
		if len(args) == 0 and len(kwargs) == 0:
			raise KeyError(f"Must pass at least client and source, only given: '{args}' and '{kwargs}'")

		key, data = None, {}

		if len(args) == 1:
			if isinstance(args[0], str):
				key = args[0]
				if 'data' in kwargs and isinstance(kwargs['data'], dict):
					data = kwargs.pop('data')
			elif isinstance(args[0], dict):
				if 'key' in kwargs:
					key = kwargs.pop('key')
				data = args[0]
			else:
				raise KeyError(f"I have no idea how to deal with this...You must pass at least client and source, only given: '{args}' and '{kwargs}'")
		elif len(args) == 2:
			if isinstance(args[0], str) and isinstance(args[1], dict):
				key = args[0]
				data = args[1]
			elif isinstance(args[1], str) and isinstance(args[0], dict):
				key = args[1]
				data = args[0]
			else:
				raise KeyError(f"I have no idea how to deal with this...You must pass at least client and source, only given: '{args}' and '{kwargs}'")
		else:
			raise KeyError(f"I have no idea how to deal with this...You must pass at least client and source, only given: '{args}' and '{kwargs}'")

		self.key = key
		self.client = kwargs.pop('client', False) or data.get('client')
		self.source = kwargs.pop('source', False) or data.get('source')
		# Allow for overrides from `kwargs` but
		# default to `data` for internal data storage
		self.data = {**data, **kwargs}

		if not self.client or not self.source:
			raise KeyError(f"CacheItem input params did not contain all required keys: {self.REQUIRED_KEYS} -> client: {self.client}, source: {self.source}")


	def reset_id(self, _id):
		self.sort_key = str(_id)
		self.address = CacheItem._address(self, self.partition_name, self.sort_name, self.partition, self.sort_key)
		return self.sort_key


	def _address(self, partition_name, sort_name, partition, sort_key):
		"""
		Produce something like this for a primary (complex) key
			{
				'object-type': {'S': 'gnx-nic-agentId'}
				'object-id': {'S': 'abc-123'},
			}
		"""
		return {
			partition_name: {'S': str(partition)},
			sort_name: {'S': str(sort_key)}
		}


	def _get_client(self, data):
		"""Get the customer/client name from the event
		"""
		return data['client']


	def _get_source(self, data):
		"""Get the data source name from the event
		"""
		return data['source']


	def serialize(self):
		return json.dumps({
			'type': type(self).__name__,
			'key': self.key,
			'_id': self.sort_key,
			'client': self.client,
			'source': self.source,
			'data': self.data,
			'address': CacheItem._address(self,
										  self.partition_name,
										  self.sort_name,
										  self.partition,
										  self.sort_key),
			'compiled': CacheItem.to_record(self,
											self.partition_name,
											self.sort_name,
											self.partition,
											self.sort_key,
											self.data)
		}, default=utils.serialize_helper)


	# def matches(keys):
	# 	"""Find matching keys from the CacheItem standard base keys

	# 	Params:
	# 	-------
	# 	- keys : list
	# 		keys you'd like to match against CacheItem

	# 	Returns:
	# 	--------
	# 	all matching keys from the given keys that match a CacheItem base key : list
	# 	"""
	# 	return [k for k in keys if k in CacheItem.BASE_KEYS]


	def to_update_expression_values(self, data, valus_list):
		update_expression_values = []
		expression_attribute_values = {}
		seperator = ','
		for k, v in self.data.items():
			print(f"CacheItem._attr_type(v) {CacheItem._attr_type(v)} , {CacheItem._numlike(v)}")
			if CacheItem._numlike(v):
				v = str(v)
			if v not in valus_list:
				print(f"{k}, {v} ===> {valus_list}")
				update_expression_values.append(f"{k} = :val_{k}")
				expression_attribute_values[f":val_{k}"] = {
					CacheItem._attr_type(v): v
				}
				
		return {
			'UpdateExpression': seperator.join(update_expression_values),
			'ExpressionAttributeValues': expression_attribute_values
		}


	def to_record(self, partition_name, sort_name, partition, sort_key, data):
		"""Get this object ready to be inserted into a cache.

		Params:
		-------
		- partition_name : str
			the reference type for this item that helps define the partition key
		- sort_name : str
			reference type for the sort key in the cache index
		- partition : str
			formatted partition key for this particular type of object.
		- sort_key : Object
			key used as the sort key in a complex index key
		- data : dict-like

		Returns:
		--------
			dict : each key in data receives a new value with the data type of the value
					listed as a key of the new value for the data key.  View the `key`
					function for a more detailed example of its output and the format
					for a record in general.
			data == {'some': 'key', ...}
			response == {'some': {'S': 'key'}, <key()>}
		"""

		internal = self.data_with_types(**self.data)
		return {**internal, **CacheItem._address(self, partition_name, sort_name, partition, sort_key)}


	def data_with_types(self, **data):
		return CacheItem._data_with_types(**data)


	def _data_with_types(**data):
		internal = {}
		for k,v in data.items():
			if k == "is_update":
				continue

			if CacheItem._attr_type(v) in ['L', 'M']:
				if isinstance(v, str):
					v = json.loads(json.dumps(json.loads(v), default=utils.serialize_helper))
				else:
					v = json.loads(json.dumps(v, default=utils.serialize_helper))

			if bool(CacheItem._attr_type(v)):
				if CacheItem._attr_type(v) == 'NULL':
					# val = True
					val = CacheItem.valmap(v)
				elif CacheItem._attr_type(v) == 'BOOL':
					if isinstance(v, str):
						val = True if v.lower() == 'true' else False
					elif isinstance(v, int):
						val = bool(v)
					elif isinstance(v, bool):
						val = v
					else:
						try:
							val = bool(v)
						except Exception:
							raise KeyError(f"{v} has type of 'BOOL' but can't be cast as bool")
				elif CacheItem._attr_type(v) in ['SS', 'NS']:
					val = [str(vv) for vv in v]
				elif CacheItem._attr_type(v) in ['S', 'N']:
					val = str(v)
				elif CacheItem._attr_type(v) == 'B':
					val = v.encode()
				elif CacheItem._attr_type(v) == 'M':
					val = {key:{CacheItem._attr_type(value):CacheItem.valmap(value)} for key,value in v.items()}
				elif CacheItem._attr_type(v) == 'L':
					val = [CacheItem.valmap(vv) for vv in v]
				else:
					print(f"Not sure what to cast this from, into string: {v}")

				internal[k] = {CacheItem._attr_type(v): val}
		return internal


	def valmap(val):
		if CacheItem._attr_type(val) == 'M':
			return {'M':CacheItem._data_with_types(**val)}
		if CacheItem._attr_type(val) == 'NULL':
			val = True
		elif CacheItem._attr_type(val) == 'L':
			return {CacheItem._attr_type(v):v for v in val}
		elif CacheItem._attr_type(val) in ['S', 'N']:
			val = str(val)
		elif CacheItem._attr_type(val) in ['SS', 'NS']:
			val = [str(vv) for vv in val]
		elif CacheItem._attr_type(val) == 'BOOL':
			if isinstance(val, str):
				val = True if val.lower() == 'true' else False
			elif isinstance(val, int):
				val = bool(val)
			elif isinstance(val, bool):
				val = val
			else:
				raise KeyError(f"{v} has type of 'BOOL' but can't be cast as bool")
		return val


	def from_typed(val):
		def _to_list(ls):
			if isinstance(ls, list):
				return ls
			elif isinstance(ls, str):
				if '[' in ls:
					try:
						ls = json.loads(ls)
					except json.decoder.JSONDecodeError:
						ls = ls.replace('[', '').replace(']', '').replace('"', '').replace("'", '')
				return ls.split(',')
			else:
				raise KeyError(f"This is not a list of numbers: {ls}")

		def _bl(bl):
			if not CacheItem._attr_type(bl) == 'BOOL':
				return False
			elif isinstance(bl, str):
				return True if bl.lower() == 'true' else False
			else:
				return bool(bl)

		def _l(l):
			try:
				l = json.loads(l)
			except json.decoder.JSONDecodeError:
				l = l
			except TypeError:
				l = l
			return l


		_n = lambda n: float(n) if '.' in str(n) else int(n)
		_s = lambda s: str(s)
		_b = lambda v: _n(v).to_bytes(2,'big')

		key = list(val.keys())[0]
		value = list(val.values())[0]
		_info = str(value).replace("\n",'').replace("\t",'')

		_report = _info[:100] + '...' + _info[-100:] if len(_info) > 100 else ''
		print(f"Assuring value cast for '{key}' -> '{_report}'")

		return {
			'N': _n,
			'S': _s,
			'B': _b,
			'SS': lambda ss: [_s(v) for v in _to_list(ss)],
			'NS': lambda ns: [_n(v) for v in _to_list(ns)],
			'BS': lambda bs: [_b(v) for v in _to_list(bs)],
			'M': lambda m: {k:CacheItem.from_typed({CacheItem._attr_type(v):v}) for k,v in m.items()},
			'L': _l,
			'BOOL': _bl,
			'NULL': lambda a: None
		}[key](value)


	def attr_type(self, val):
		return CacheItem._attr_type(val)


	def boollike(self, val):
		return CacheItem._boollike(val)


	def numlike(self, val):
		return CacheItem._numlike(val)


	def strlike(self, val):
		return CacheItem._strlike(val)

	def byteslike(self, val):
		return CacheItem._byteslike(val)


	################################################################################
	# private crap
	################################################################################
	def _byteslike(val):
		try:
			val.decode()
			return True
		except (UnicodeDecodeError, AttributeError):
			return False


	def _attr_type(val):
		"""Map the value given in the parameter to a Dynamo data type

		Params:
		-------
		- self : class
		- val : any
			value will be matched to a type if it can be

		Returns:
		--------
		Dyanmo data-type name acronymn : str
		"""

		t = None
		if CacheItem._boollike(val): # "BOOL": true
			t = 'BOOL'
		elif CacheItem._numlike(val):
			t = 'N'
		elif CacheItem._strlike(val):
			t = 'S'
		elif CacheItem._byteslike(val): # figure out if this is bytes
			t = 'B'
		elif isinstance(val, list) and len(list(filter(CacheItem._strlike, val))) == len(val): # like array of string
			t = 'SS'
		elif isinstance(val, list) and len(list(filter(CacheItem._numlike, val))) == len(val): # "NS": ["42.2", "-19", "7.5", "3.14"]
			t = 'NS'
		elif isinstance(val, list) and len(list(filter(CacheItem._byteslike, val))) == len(val): # "BS": ["U3Vubnk=", "UmFpbnk=", "U25vd3k="]
			t = 'BS'
		elif isinstance(val, dict) and len(list(filter(CacheItem._reclike, val.values()))) == len(val): # "M": {"Name": {"S": "Joe"}, "Age": {"N": "35"}}
			t = 'M'
		elif isinstance(val, dict) and not CacheItem._reclike(val):
			t = 'M'
		elif isinstance(val, list): # "L": [ {"S": "Cookies"} , {"S": "Coffee"}, {"N", "3.14159"}]
			if len(list(filter(CacheItem._reclike, val))) == len(val):
				t = 'L'
			elif len(list(filter(lambda x: bool(CacheItem._attr_type(x)), val))) == len(val):
				t = 'L'
		elif val is None: # "NULL": true
			t = 'NULL'

		return t


	def _reclike(val):
			return bool(isinstance(val, dict) and len(list(filter(lambda k: k in CacheItem.ALL_TYPES, val))) == len(val))


	def _boollike(val):
		"""Check if something behaves like a bool (boolean)

		Params:
		-------
		- self : class
		- val : any
			item you'd like to check for booliness

		Returns:
		--------
		whether or not the object behaves like a bool : bool
		"""
		if isinstance(val, bool):
			return True
		elif isinstance(val, str):
			return bool(val.upper() in ['FALSE', 'TRUE'])
		else:
			return False


	def _numlike(val):
		"""Check if something behaves like a number (string)

		Params:
		-------
		- self : class
		- val : any
			item you'd like to check for numberiness

		Returns:
		--------
		whether or not the object behaves like a num : bool
		"""
		numbers = ['float', 'int', 'double']
		if type(val).__name__ in numbers:
			return True

		try:
			int(val)
			return True
		except ValueError as ve:
			err = 'invalid literal for int() with base 10: '
			if err in str(ve):
				err_val = str(ve).replace(err, '').replace("'", '')
				if re.search(r"\d+", err_val):
					return bool(re.search(r"\d+e-\d+", err_val))
				else:
					return False # wait what?  did i typo this? for sure missing a bunch of cases...
			else:
				return False
		except TypeError:
			return False


	def _strlike(val):
		"""Check if something behaves like a str (string)

		Params:
		-------
		- self : class
		- val : any
			item you'd like to check for stringiness

		Returns:
		--------
		whether or not the object behaves like a str : bool
		"""
		strings = ['str']
		return bool(type(val).__name__ in strings and not CacheItem._numlike(val))
