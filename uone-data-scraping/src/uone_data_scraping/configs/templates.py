import os
import json
import re
# from functools import lru_cache
import datetime
import copy
import logging
from typing import List
from uone_caching.actions.query import QueryEngine


DATA_SOURCES = ['gen', 'nic', 'dialpad', 'leaflogic', 'alpineiq']

# @lru_cache(maxsize=50, typed=False)
def config_templates(source, client='uone', pattern=None, cache_access_key='template'):
    """Get parameter configuration templates based on source; i.e. Genesys, NiceInContact etc.

    Params:
    -------
    - source: str - name or identifier of the data warehouse or location we're going to scrape
    - filter: lambda or function that accepts a source parameter and returns desired config templates
    """
    print(f"Getting scraping templates for {client} {source} using '{pattern}'")
    q = QueryEngine(key='template', source=source, client=client)
    _templates = q.get('*', 'template')
    # default the filter to a pass-through function that allows everything through
    # ffunc = lambda key: pattern in key if pattern is not None else lambda key: True
    try:
        pattern = '.*' if (pattern == '*' or pattern is None) else pattern
        print(f"This is the stupid pattern we're searching for now: '{pattern}'")
        pattern = pattern.replace('-', '.').replace('_', '.')
    except TypeError as te:
        print(f"Having stupid problems with the pattern again: {te}")
        pattern = '.*'

    regex_pattern = re.compile(pattern) if pattern is not None else re.compile('.*')

    print(f"Searching and loading records from a first-pass match with {len(_templates)} results to filter with: {regex_pattern}")
    # return {record.sort_key:json.loads(record.data[cache_access_key]) for record in _templates if bool(regex_pattern.search(record.sort_key))}
    template_objs = {}
    for record in _templates:
        print(f"Second pass filtering on {record.sort_key} against {pattern} -> {regex_pattern} for {record.data}")
        if bool(regex_pattern.search(record.sort_key)):
            print(f"Found {record.sort_key} -> '{cache_access_key}' in {record.data}")
            template_objs[record.sort_key] = json.loads(record.data[cache_access_key])

    return template_objs



def target_names(source: str, client: str='uone') -> List[str]:
    """Find target names for this data source

    Params:
    -------
    - source : str
        The name/id of a data source
    - clietn : str default uone
    Returns:
    --------
    list of names of targets : `List[str]`
    """
    q = QueryEngine(key='scraping_template', source=source, client=client)
    return q.get('*', 'object-id')


def config_base(source, endpoint):
    """Get API request configuration base templates for a given `source` and `endpoint` name

    Params:
    -------
    - source : str
        Name or ID of the data source to search for template bases from
    - endpoint : str
        Name of the endpoint, used in string matching to find the targets

    Returns:
    --------
    base API request configuration tempatlate, for the given params : dict
    """
    try:
        return config_templates(source, pattern=endpoint)[endpoint]
    except KeyError as ke:
        logging.warning(f"No template for {endpoint}, using empty template")
        return {}
