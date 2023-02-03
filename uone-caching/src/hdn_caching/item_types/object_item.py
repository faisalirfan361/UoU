from .cache_item import CacheItem
import simplejson as json
from .. import utils


class ObjectItem(CacheItem):
	"""
	Cache Item for dealing with objects, specifically things with IDs and
	a type of some kind.  This can be `User`s, `Department`s, `Report`s, etc
	"""

	# REQUIRED_KEYS = ['object_type', 'object_id']

	def __init__(self, key:str, *args, **kwargs):
		"""
		Must provide an event which is a dictionary of key-value pairs which
		should be added to the cache.  This event contains the elements required
		to find the cache and/or item required.

		The required parameters will form the key for this item in the cache
		by following the format of:

			<client>-<source>-<type>
			for example:
			greenix-nic-agent
			or
			customerABC-dialpad-user

		These keys help to determine storage location on disk for fast access in the cache
		and they also provide a programatic way to reference lists of IDs by _what_ they are.
		Automatic key formatting along with automatic detection of dynamo data types are
		the features that this class provides.

		Validations are the other bit of important functionality for this class.
		Keys are formatted and `KeyError` exceptions are thrown for missing required info.


		Params:
		-------
		- client : str
			client for which this record was pulled
		- partition_prefix : str
		"""
		super().__init__(key, *args, **kwargs)

		################################################################################
		# !!! TODO !!!
		################################################################################
		# this should do a Mapping request to find what this source/endpoint calls their
		# partition key in their data.  this is another cache lookup, where the partition
		# is the company and endpoint and the sort is the name we're trying to map to;
		# e.g. dialpad-user == target
		# so
		# company123-dialpad-target == user
		################################################################################
		# !!! TODO !!!
		################################################################################
		self.partition_name = 'object-type'
		self.sort_name = 'object-id'

		if not self.key:
			raise KeyError(f"ObjectItem init args/kwargs must yield a key but none could be found: '{args}', '{kwargs}'")

		self.partition_key = self.key # .get_mapping(key=self.key, client=self.client, source=self.source)
		self.sort_key = self._get_id()

		if self.sort_key is None:
			print(f"Sort key (ID) could not be found for this item: event['{self.partition_key}']")

		self.partition = self._get_partition(self.data)
		self.address = super()._address(self.partition_name, self.sort_name, self.partition, self.sort_key)


	def _get_id(self):
		"""Find the value of the key that is being used as a partition-key in the
		index key for this record.  This is usually something like `agentId` or some
		other noun's ID name

		Params:
		-------
		- self : class
		- data : dict

		Returns:
		--------
		object from the data at the `partition_key` of this particular object
		"""
		try:
			# if the index name for this object is present in the data, it's considerd
			# explicitly telling us the ID, stop searching.
			found = self.data.get(self.sort_name) or self.data.get(self.partition_key)
			print(f"ObjectItem found possible ID(s): {found}")

			if str(found).isalnum():
				return found
			else:
				try:
					json.loads(found)
					print(f"It looks like what we found is NOT an ID, it's JSON, so use the found[{self.partition_key}] or {self.partition_key}...")
					return self.partition_key # '*' <- maybe return that instead?
				except json.decoder.JSONDecodeError:
					# this is a regular string probably, so let's assume it's the ID
					return found
		except TypeError as te:
			if 'string indices must be integers' in str(te):
				return None
		except KeyError as ke:
			return None


	def _get_partition(self, data):
		"""Format a partition key for a complex key in Dynamo indexing by using
		data on this object in the proper format

		Params:
		-------
		- self : class

		Returns:
		--------
		<client>-<source>-<partition_key> format : str
		"""
		return f"{self.client}-{self.source}-{self.partition_key}"


	def _get_partition_prefix(self, data):
		return data['object_type']


	def to_update_expression_values(self, keys_values):
		return super().to_update_expression_values(self.data, keys_values)


	def to_record(self):
		"""Create a record that is syntactically correct for Dynamo `put`s

		Params:
		- self : class

		Returns:
		--------
		object that contains any data for the record along with a properly formatted
		complex index key : dict|JSON
		"""
		return super().to_record(self.partition_name,
								 self.sort_name,
								 self.partition,
								 self.sort_key,
								 self.data)


	def to_json(self):
		return json.dumps(self.to_record(), default=utils.serialize_helper)
