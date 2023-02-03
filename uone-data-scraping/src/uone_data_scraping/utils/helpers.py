import os
import sys
import csv
import io
import copy
import re
import datetime
import dateutil.parser
from datetime import date
from datetime import datetime as dtime
from dateutil.rrule import rrule, YEARLY, MONTHLY, WEEKLY, DAILY, HOURLY, MINUTELY, SECONDLY
import requests
import json
import boto3
from botocore.exceptions import ClientError
import math


REGISTERED_HTTP_METHODS = ['GET', 'PUT', 'POST', 'DELETE', 'OPTIONS']


def _build_headers(kind, **params):
    try:
        req_headers = {
            'bearer': {
                'Authorization': f"Bearer {params.get('api_key')}",
                'content-Type': 'application/x-www-form-urlencoded',
                'Accept': 'application/json, text/javascript, */*'
            }
        }[kind]
    except KeyError as ke:
        raise ke

    return req_headers


def _build_query_params(params):
    """Return params joined with query param formatting
    For example,
        given {'startDate': '2020-01-01'}
        response = '?startDate=2020-01-01'

        given {'startDate': '2020-01-01', 'endDate': 'underwear'}
        response =  '?startDate=2020-01-01&endDate=underwear'
    Params:
    -------
        key-value pairs

    Returns:
        str key-value pairs
        - entire list is prefixed with '?'
        - joined with & between each pair
        - '=' between key-value pairs
    """
    params = params if params else {}
    prefix = '?' if len(params) > 0 else ''
    return  prefix + '&'.join([f"{k}={v}" for k,v in params.items() if v is not None and k is not None])


def day_of(timestamp):
    return f"00{timestamp.day}" if timestamp.day < 10 else timestamp.day


def format_json_records(records, path):
    reshaped = {key:[] for key in records[0].keys()}
    print('Reshaping records into JSON...')
    for rec in records:
        for key in reshaped:
            reshaped[key].append(rec[key])
    print('Reshaped records, dumping and encoding to JSON string...')
    results = json.dumps(reshaped).encode()
    return results


def format_json_lines_records(records, path):
    print(f"Formatting '{len(records)}' records for a file we should save at '{path}'")
    b_body = io.BytesIO()
    b_body.write("\n".join([json.dumps(rec) for rec in records]).encode())
    b_body.seek(0)
    b_body.name = path
    return b_body


def format_csv_records(records, path):
    # raw_recs = data[data_key]
    # if len(raw_recs) == 0:
    #     print(f"No records were found for {endpoint} using {params}")
    #     return None

    # records = [list(rec.values()) for rec in raw_recs]
    # records.insert(0, list(raw_recs[0].keys()))

    # body = _format_records(records, path)

    # print(f"Sending csv of len: {len(records)} to s3://{BUCKET}/{path}")
    records = [list(rec.values()) for rec in raw_recs]
    records.insert(0, list(raw_recs[0].keys()))
    print(f"Writing records to in-memory CSV file object and reformatting to Bytes")
    str_body = io.StringIO()
    csv.writer(str_body).writerows(records)
    str_body.seek(0)
    b_body = io.BytesIO()
    b_body.write(str_body.getvalue().encode())
    b_body.seek(0)
    b_body.name = path

    return b_body


# def _get_secret(path, secret_name):
#     ssm = boto3.client('ssm')
#     print(f"Getting secret at '{path}' + '{secret_name}'")
#     res = ssm.get_parameters_by_path(Path=path)

#     if 'Parameters' in res:
#         num = len(res['Parameters'])
#         count =  0
#         for r in res['Parameters']:
#             if secret_name in r['Name']:
#                 return r['Value']
#             else:
#                 count += 1
#                 if num == count:
#                     raise KeyError(f"Could not find secret for: '{secret_name}'")
#     else:
#         raise KeyError(f"Could not find anything for: '{secret_name}'")
def _get_secret(secret_name):
    # secret_name = "MySecretName"
    ssm = boto3.session.Session().client(service_name='secretsmanager',
                                         region_name=os.getenv('AWS_REGION'))
    secret = None
    try:
        print(f"Getting secret for: {secret_name}")
        res = ssm.get_secret_value(SecretId=secret_name)
    except ClientError as ce:
        if ce.response['Error']['Code'] == 'ResourceNotFoundException':
            print("The requested secret " + secret_name + " was not found")
        elif ce.response['Error']['Code'] == 'InvalidRequestException':
            print("The request was invalid due to:", ce)
        elif ce.response['Error']['Code'] == 'InvalidParameterException':
            print("The request had invalid params:", ce)
        else:
            print(f"secret: {secret_name}, client error: {ce}")
    else:
        # Secrets Manager decrypts the secret value using the associated KMS CMK
        # Depending on whether the secret was a string or binary, only one of
        # these fields will be populated
        return res['SecretString'] if 'SecretString' in res else res['SecretBinary']


def format_save_path(raw, **kwargs):
    print(f"Formatting a save path from: string -> {raw}, kwargs -> {kwargs}")
    new_path = raw.format(**kwargs)
    print(f"Save path formatted to: {new_path}")
    return new_path


def hour_of(timestamp):
    return f"0{timestamp.hour}" if timestamp.hour < 10 else timestamp.hour


def _is_http_method(http_method):
    return bool(http_method.upper() in REGISTERED_HTTP_METHODS)


def month_of(timestamp):
    return f"0{timestamp.month}" if timestamp.month < 10 else timestamp.month


def queue_url_from_name(client, source, account, suffix=None, region=None):
    queue_name = f"{client}_{source}{'_' + suffix if suffix else ''}"
    region = region or os.getenv('AWS_REGION')
    if region is None:
        raise KeyError('Region must be supplied to create a queue URL')
    queue_url = f"https://sqs.{region}.amazonaws.com/{account}/{queue_name}"
    print(f"Composed Queue URL: {queue_url}")

    return queue_url


def _refresh_api_key(refresher_func_name, client, source):
    boto3.client('lambda').invoke(FunctionName=refresher_func_name, # os.getenv('REFRESH_TOKEN_FUNCTION'),
                                  InvocationType='RequestResponse',
                                  Payload=json.dumps({'type': 'api', 'source':source, 'client': client}))#.encode('utf-8'))


def _parse_event_for_date(date_pattern):
    """Parse a string for a date/time looking pattern
    """

    if isinstance(date_pattern, str):
        # search for '## word', where the '##' means numbers of units of time and the 'word' represents
        # the name of the unit; e.g. hours, minutes, weeks, etc.
        matcher = re.compile(r"(\d+).?((?:milisecond|second|minute|hour|day|week|month|year|decade))")

        if 'now' in date_pattern.lower():
            parsed = dtime.now()
        elif 'today' in date_pattern.lower():
            raw = date.today()
            parsed = dtime.combine(raw, dtime.min.time())
        elif bool(matcher.search(date_pattern)):
            td_info = matcher.search(date_pattern).groups()
            unit = f"{td_info[-1]}s"
            num = int(td_info[0])
            return datetime.timedelta(**{unit:num})
        else:
            parsed = dateutil.parser.parse(date_pattern)
        return parsed
    else:
        return date_pattern


def time_windows_for(start_date, end_date, max_window):
    given_window = (end_date - start_date).total_seconds()
    print(f"Found given time window of: {start_date} to {end_date}")
    minute = 60
    hour = minute * 60
    day = hour * 24
    week = day * 7
    month = week * 4
    year = 365 * day

    if isinstance(max_window, int):
        max_window = max_window
    elif 'total_seconds' in dir(max_window):
        max_window = max_window.total_seconds()
    elif max_window is None:
        max_window = sys.maxsize / 2
    print(f"Found max_window of: {max_window}")

    if max_window == 0:
        num_chunks = 1
    else:
        num_chunks = int(given_window / max_window)
        num_chunks = 1 if num_chunks == 0 else num_chunks
    print(f"Will break apart given_window into {num_chunks} smaller chunks")

    if max_window >= year:
        frequency = YEARLY
        block_size = year
    elif year > max_window >= month:
        frequency = MONTHLY
        block_size = month
    elif month > max_window >= week:
        frequency = WEEKLY
        block_size = week
    elif week > max_window >= day:
        frequency = DAILY
        block_size = day
    elif day > max_window >= hour:
        frequency = HOURLY
        block_size = hour
    elif hour > max_window >= minute:
        frequency = MINUTELY
        block_size = minute
    else:
        frequency = SECONDLY
        block_size = second
    print(f"Because of the given_window: {given_window}, max_window: {max_window} and num_chunks: {num_chunks}, the numeric block size is now: {block_size}")

    count = math.ceil(given_window / block_size)
    count = 1 if int(count) == 0 else count
    print(f"Got {count} blocks by converting block_size with 'given_window / block_size'")

    time_blocks = list(rrule(freq=frequency, count=count, dtstart=start_date))
    window_size = int(count/num_chunks)
    print(f"Got a time window size of {window_size}")
    window_size = 1 if int(window_size) == 0 else window_size
    chunk_size = datetime.timedelta(seconds=block_size * window_size)
    print(f"Creating {count} start and end times of {chunk_size}")
    # return [(time_blocks[i], time_blocks[i] + chunk_size) for i in range(0, len(time_blocks), int(window_size))]
    return [(time_blocks[i], time_blocks[i] + chunk_size) for i in range(0, count, int(window_size))]

    # window_size = int(count/num_chunks)
    # print(f"Got a time window size of {window_size}")
    # window_size = 1 if int(window_size) == 0 else window_size
    # chunk_size = datetime.timedelta(seconds=block_size * window_size)
    # print(f"Creating {count} start and end times of {chunk_size}")
    # def _fucda(ids):
    #     def __fucda__(args):
    #         return args[0] in ids

    # def _dafuc(cs):
    #     def __dafuc__(args):
    #         return (args[1], args[1] + cs)
    #     return __dafuc__

    # indexes = list(range(0, count, int(window_size)))
    # time_blocks = enumerate(rrule(freq=frequency, count=count, dtstart=start_date))
    # configs_with_time = filter(_fucda(indexes), time_blocks)

    # return map(_dafuc(chunk_size), configs_with_time)


def lamb(time_block, chunk_size):
    return (time_block[0], time_block[0] + chunk_size)
    range(0, count, int(window_size))


def year_of(timestamp):
    return timestamp.year
