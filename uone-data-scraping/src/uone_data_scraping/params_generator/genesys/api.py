import os
import json
import logging
import datetime
import dateutil.parser
from dateutil.rrule import rrule, YEARLY, MONTHLY, WEEKLY, DAILY, HOURLY, MINUTELY, SECONDLY
import copy
from uone_data_scraping.configs import templates


_DSNAME = 'genesys'


def targets():
    return templates.target_names(_DSNAME)


def _endpoint_is_something_you_pick_based(endpoint):
    # return {
    #     some name of your endpoint: True,
    #     some other endpoint name: True,
    #     ...
    # }.get(endpoint, False)
    return False # !!! TODO !!! Fix this to use the stuff form above


# do you actually need this or are you copy/pasting from nic?
def _configs_for_agent(config, event):
    new_conf = {'params':None}
    new_conf['params'] = {**config['params'], **event}
    return new_conf


def _configs_for_pass_through(*args, **kwargs):
    print(f"Genesys called with configs generator it doesn't support: {args} and {kwargs}")
    return {'args': args, 'kwargs': kwargs}


def _configs_for_id(*args, **kwargs):
    print(f"Genesys called with configs generator it doesn't support: {args} and {kwargs}")
    return {'args': args, 'kwargs': kwargs}

