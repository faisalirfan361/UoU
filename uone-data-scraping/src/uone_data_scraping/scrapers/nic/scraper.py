import os
import csv
import io
import requests
import json
import boto3
from botocore.exceptions import ClientError
from ...utils import helpers


def _secs_from_epoch(date_obj):
    """Convert a datetime timestamp or similar that can be converted into seconds from 01/01/1970
    Params:
    -------
    - date_obj: str, datetime.datetime, datetime.timestamp
        date to be converted into seconds from epoch time
    Returns:
    --------
        int: this date converted into seconds from epoch
    """
    given = helpers._parse_event_for_date(date_obj)
    return given.total_seconds()
    # try:
    #     if 'total_seconds' in dir(given):
    #         return given.total_seconds()
    #         elif


def _make_api_call(client, call_config, refresher_func_name=None, attempts=0): # endpoint, params, attempts=0):
    client = client.upper()
    
    print(f"Retrieving API access token for NiceInContact for client: {client}")
    access_token = helpers._get_secret(f"etl/integrations/{client}_ACCESS_TOKEN")
    base_url = helpers._get_secret(f"etl/integrations/{client}_BASE_URL")
    headers = helpers._build_headers(kind='bearer', api_key=access_token)
    # find HTTP method that this endpoint requires from the event/params that drive
    # this function; default to the HTTP GET method for NiceInContact (per API docs)
    # or default to 'GET' HTTP method
    http_method = call_config.method.lower()
    if not helpers._is_http_method(http_method):
        raise KeyError(f"'{http_method}' is not a valid HTTP method; Must send in one of '{helpers.REGISTERED_HTTP_METHODS}'")

    query_params = helpers._build_query_params(call_config.query_params)
    endpoint = call_config.endpoint
    print(f"Using Method: {http_method}, URL: {base_url}, endpoint: {endpoint} + query_params of: {query_params}, params: {call_config.params}")
    final_url = base_url + endpoint + query_params

    # get the function object off the requests module.  This is the same as
    # saying something like `requests.get` or `requests.post`
    request_function = getattr(requests, http_method)
    # use requests function to make HTTP request for this endpoint
    print(f"Making '{http_method}' request to {final_url} using: '{headers}' with: {call_config.params}")
    results = request_function(final_url,
                               headers=headers,
                               params=call_config.params)
    print(f"Recieved results from request with status of {results.status_code}")

    attempts += 1
    if results.status_code == 401 and attempts <= 2:
        print(f"Failed initial request because of: {results.text} {call_config}")
        rfunc = refresher_func_name if refresher_func_name is not None else os.getenv('REFRESHER_FUNC_NAME')
        print(f"Using {rfunc} to refresh the API credentials...")
        helpers._refresh_api_key(refresher_func_name=rfunc, source='nic', client=client.lower())
        return _make_api_call(client=client, call_config=call_config, refresher_func_name=rfunc, attempts=attempts)
    # we've done our best, return the results regardless of status_code etc.
    return results
