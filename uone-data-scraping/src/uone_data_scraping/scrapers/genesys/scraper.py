import os
import csv
import io
import requests
import json
import boto3
from botocore.exceptions import ClientError
from ...utils import helpers



def _make_api_call(endpoint, params, attempts=0):

    req_headers = {"Content-Type": "application/json", "X-Auth-Token": API_TOKEN}

    params = {"per_page": 50, "page": 1}
    results = requests.get(
        "https://api.genesiscloud.com/compute/v1/ssh-keys",
        headers=req_headers,
        params=params,
    )

    # if the first request was rejected as "Unauthorized", try to refresh the
    # API token and retry the request
    attempts += 1
    if results.status_code == 401 and attempts <= 2:
        # helpers._refresh_api_key()
        return _make_api_call(endpoint, params, attempts)
    # we've done our best, return the results regardless of status_code etc.
    return results
