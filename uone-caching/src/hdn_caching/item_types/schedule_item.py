from .cache_item import CacheItem


class ScheduleItem(CacheItem):
	"""
	CacheItem structured around schedules, "last run" for example.
	"""
	SCHEDULING_KEYS = [
		'start_date', 'startDate', 'end_date', 'endDate', 'updated_at',
		'updatedAt', 'updatedSince', 'updated_since'
	]

	def __init__(self, key=None, event={}, **kwargs):
		super().__init__(event, key=key, **kwargs)
		data = {**event, **kwargs}

		partition_matches = ScheduleItem.matches([key] if key else list(data.keys()))

		if len(partition_matches) != 1:
			raise KeyError(f"Too many matches for partition key: {partition_matches}")

		self.partition_key = partition_matches[0]
		self.partition_name = 'schedule-type'

		self.data = {k:v for k,v in data.items() if k not in self.SCHEDULING_KEYS}
		self.partition = self._get_partition(data)


	def _get_partition(self, data):
		return f"{self.client}-{self.source}-{self.partition_key}"


	def matches(keys):
		return [k for k in keys if k in ScheduleItem.SCHEDULING_KEYS]
