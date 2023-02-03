import os
import sys
import json
import logging
import datetime
import copy
from typing import Dict, Any
import uone_data_scraping.configs.templates as templates
from uone_data_scraping.processing import dotmap
from uone_data_scraping.utils import helpers


_DSNAME = 'nic'
_START_TIME_ORIENTED_COLS = ['startDate', 'updatedSince', 'startTime']
_END_TIME_ORIENTED_COLS = ['endDate', 'endTime']
_TIME_ORIENTED_COLS = _START_TIME_ORIENTED_COLS + _END_TIME_ORIENTED_COLS

def targets(pattern: str=None) -> Dict[str,Dict[str,Any]]:
    """Find target configuration templates based on string pattern matching
    Params:
    -------
    - pattern : str
        can be the name of a target config or '*'.  If '*' is used, no filter is applied and you'll
        get all results.  If a `pattern` is a `str`, it can be used to match against the name(s) of
        config templates.  These patterns, if passed, are usually the name of a config item in
        `configs.templates` and correspond to an API endpoint you want to scrape

    Returns:
    --------
    Dict[str, Dict[str,...]] -> keys are target names and values are templates
    """
    print(f"Searching for template(s) for source='{_DSNAME}', pattern='{pattern}'...")
    return templates.config_templates(source=_DSNAME, pattern=pattern)


def _configs_for_job_id(endpoint, event):
    # _configs_for_* always returns list
    return _configs_for_pass_through(endpoint, event)


# def _configs_for_pass_through(target, event):
#     config = templates.config_base(_DSNAME, target) if isinstance(target, str) else target
#     return dotmap.build(config)
def _configs_for_pass_through(config, event):
    # _configs_for_* always returns list
    # if isinstance(config, str):
    #     config = dotmap.build(templates.config_base(_DSNAME, config))

    return config if isinstance(config, list) else [config]


def _configs_for_zipped(endpoint, event):
    # _configs_for_* always returns list
    config = templates.config_base(_DSNAME, endpoint)
    return [{'params':{**config['params'], **event}}]


def _configs_for_agent(endpoint, event):
    # _configs_for_* always returns list
    config = templates.config_base(_DSNAME, endpoint)
    return [{'params':{**config['params'], **event}}]


def _configs_for_id(config, event):
    # _configs_for_* always returns list
    # config = templates.config_base(_DSNAME, endpoint)
    # e_conf = dotmap.build(event)

    if event.updatedSince:
        u_s = event.updatedSince
        use_this_time = helpers._parse_event_for_date(u_s)
        print(f"Found an updatedSince param: {u_s} and converted to: {use_this_time}")
        config['params']['updatedSince'] = use_this_time.isoformat()
    else:
        print('No time windowing was supplied so we are removing those params and using epoch')
        use_this_time = datetime.datetime.fromtimestamp(0)

        if len(list(config['params'].keys())) > 1: # if there's more than just 'updatedSince'
            config['params'].pop('updatedSince')
        else:
            config.pop('params')

    formatted_save_path = helpers.format_save_path(config['config']['save_path'],
                                                   day=helpers.day_of(use_this_time),
                                                   hour=helpers.hour_of(use_this_time),
                                                   client=event.client,
                                                   window_end=datetime.datetime.now().isoformat(),
                                                   source=event.source)

    print(f"The save_path has been formatted into: {formatted_save_path}")
    config['config']['save_path'] = formatted_save_path
    print(f"Created conf: {config}")
    return [config]


def _configs_for_time(config, event): # endpoint, event):
    """Generate API scrape configuration items for a given time window.  Because we have to abide
    by several different levels of SLAs, not only from ourselves but from customers and their APIs,
    we must be able to break apart a large time window into appropriate sized blocks, adhering to
    said SLAs.

    This function makes a copy of a configuration base for each time window.  Time windows are the
    maximum size allowed.  We'll create as many of these smaller windows necessary to gather all
    the data we originally requested while still following Size and request speed limitations that
    we must follow.  An example might be if we are trying to generate data for the last 30 days but
    our API endpoint only allows us to query for 1 day's worth of data at a time.  This function
    would use the base `config` template to create 30 copies that have adjusted params to fit each
    of their individual specific parameters, given their newly resized, smaller time windows.

    Params:
    -------
    - config : dict
        The base configuration template, which came from `configs.nic_configs` at the target given
        in the original job start event.  We'll use to copy and/or fill out, given the data we found
        in the `event`.  We use the base config item to know _what_ to fill and and _how_ it should
        be filled out.  An example is the `max_window` setting in base config items.  This tells us
        how large of a maximum size for each request we create for the current job we're creating
        configs for.  We'll find out how many `max_window` sized blocks fit into the total report
        window and then create that many copies, each properly configured with their special time
        block etc.
    - event : dict
        Usually an event from Lambda which came from upstream scheduling systems to run _this_ report.
        The event will tell us the actual special configuration items like the start and end date for
        this entire request.

    Returns:
    --------
    configured API request config objects : list(dict)

    Notes:
    ------
    - This function was called because the base config item that was passed in as `config`
    is `bucketed_by='time'`.
    - If startDate is not given, endDate-1Day is used.
    - If endDate is not given, today is used.
    """
    configs = []

    if isinstance(config, str):
        config = dotmap.build(templates.config_base(_DSNAME, config))

    if isinstance(event, str):
        e_conf = dotmap.build(json.loads(event))
    else:
        e_conf = dotmap.build(event)

    print(f"Generated config for event and template config as: event -> {e_conf} and config -> {config}")

    client = e_conf.client
    end_date = helpers._parse_event_for_date(e_conf.end_date or 'today')
    start_date = helpers._parse_event_for_date(e_conf.start_date or e_conf.updated_since or (end_date - datetime.timedelta(days=1)).isoformat())
    print(f"Found start and end date for times based config in NIC API -> start: '{start_date}' to end: '{end_date}'")

    given_window = end_date - start_date
    max_window = config.pop('max_window', e_conf.max_window or given_window)
    max_window = helpers._parse_event_for_date(max_window) if isinstance(max_window, str) else max_window
    print(f"Found given and max time window sizes for configs in time based windows -> given: '{given_window}' and max: '{max_window}'")

    if max_window is None or max_window < given_window:
        dates_list = helpers.time_windows_for(start_date, end_date, max_window)
        print(f"Number of config blocks to generate -> {len(dates_list)}")

        start_keys = start_keys_for(config)
        end_keys = end_keys_for(config) or end_keys_for(e_conf)
        mapping = ((0,start_keys),(1,end_keys)) if end_keys is not None else ((0,start_keys))
        time_func = _set_times(mapping)
        print(f"Using these addresses to map settings into parameters for our datetimes: {mapping}")

        for dates_tup in dates_list:
            template = dotmap.build(copy.deepcopy(dict(config)))
            settings, conf = time_func(template, dates_tup)

            time_related = {
                'day': helpers.day_of(helpers._parse_event_for_date(settings[0])),
                'hour': helpers.hour_of(helpers._parse_event_for_date(settings[0])),
                'window_start': helpers._parse_event_for_date(e_conf.start_date or conf.start_date or settings[0]).isoformat(),
                'window_end': helpers._parse_event_for_date(settings[-1] if settings[-1] != settings[0] else e_conf.end_date or conf.end_date).isoformat()
            }
            template_vals = {**time_related, **e_conf, **dict(conf)}
            configs.append(_set_save_path(conf, **template_vals))

    else:
        start_keys = start_keys_for(config)
        end_keys = end_keys_for(config) or end_keys_for(e_conf)
        mapping = [(0,start_keys),(1,end_keys)] if end_keys is not None else [(0,start_keys)]
        time_func = _set_times(mapping)
        print(f"Using these addresses to map settings into parameters for our datetimes: {mapping}")
        # config['params.startDate'] = start_date.isoformat()
        # config['params.endDate'] = end_date.isoformat()
        settings, config = time_func(config, (start_date.isoformat(), end_date.isoformat()))
        config['config.save_path'] = helpers.format_save_path(config['config']['save_path'],
                                                              client=client,
                                                              day=helpers.day_of(start_date),
                                                              hour=helpers.hour_of(start_date),
                                                              start_time=start_date.isoformat(),
                                                              end_time=end_date.isoformat(),
                                                              window_start=start_date.isoformat(),
                                                              window_end=end_date.isoformat(),
                                                              source=event.source)
        configs.append(config)

    return configs


def end_keys_for(conf, include_root=False):
    """Use string pattern matching to find all keys that _look_ like they should be a
    end date/window/bucket

    Params:
    -------
    - conf
    - include_root : bool default=False

    Returns:
    --------
    the key(s) found that string match things that sound like end keys :
    `str` if only 1 found and `list(str)` if many found

    Notes:
    ------
    Sometimes there won't be an end, only a start;
    e.g. 'updatedSince'
    """
    found = []
    keys = list(conf.keys())
    for col in _END_TIME_ORIENTED_COLS:
        found += [k for k in keys if col in k]

    nonroot = [k for k in found if '.' in k]
    results = found if include_root else nonroot

    return results if len(results) > 0 else None


def start_keys_for(conf, include_root=False):
    """Use string pattern matching to find all keys that _look_ like they should be a
    start date/window/bucket

    Params:
    -------
    - conf
    - include_root : bool default=False

    Returns:
    --------
    the key(s) found that string match things that sound like start keys :
    `str` if only 1 found and `list(str)` if many found

    Notes:
    ------
    Use start time because sometimes there won't be an end, only a start;
    e.g. 'updatedSince'
    """
    found = []
    keys = list(conf.keys())
    for col in _START_TIME_ORIENTED_COLS:
        found += [k for k in keys if col in k]

    nonroot = [k for k in found if '.' in k]
    results = found if include_root else nonroot

    return results if len(results) > 0 else None


def _set_save_path(template, **settings):
    """Use `str.format()` to apply key-value pairs to a given string template

    Params:
    -------
    - template : str
        the string should have `str.format()` syntax, where each variable to be
        filled out in the template should be encapsulated in `{}`; e.g. `some_{my_var}_string_template`
    - settings : key-value pair(s)
        keys should match template variables and values should be able to be run in `str.format()`

    Returns:
    --------
    formatted string : `str`
    """
    # settings = dotmap.build(settings)
    for key in [k for k in template.keys() if 'save_path' in k]:
        template[key] = helpers.format_save_path(template.save_path, **settings)

    return template


def _set_times(paths):
    """Generate a pre-configured function which can use the pre-configuration path mappings.

    Params:
    -------
    - paths : list(tuple(int,list(str)))
        the first entry in a path tuple is the index to map TO.
        the second entry in a path tuple is the list of data-keys to map FROM.

            [(0,['startDate', 'start_date',...]), (1,['endDate', 'end_date',...])]

        The items in the first tuple are destined to be set at the start window because
        the TO item in the tuple is a 0, which means "first".
        The items in the second tuple are going to be used for the second setting for which
        the pre-configured `_st_func` will be setting because it has an index of 1 as its
        TO item in the tuple.

        The items in the second spot in the tuples are a list of keys that _might_ match
        one of the keys in the `setting` parameter of the `_st_func` that we're configuring
        by invoking this `_set_times` wrapper function.

    Returns:
    --------
    pre-configured `_st_func` that can be used to apply a given setting from the `paths` param
    of this function : `function`
    """
    hints = copy.deepcopy(paths)
    def _st_func(obj, setting):
        """Apply a given setting to a given object using a pre-defined set of addresses
        that were passed to this function through its wrapper function,
        called `params_generator.nic.api._set_times()`

        Params:
        -------
        - obj : dict-like
            The object for which the setting applies.  This object will be mutated by adding
            the path(s) to the object at locations given by the `paths` and `setting`
        - setting : dict
            Contains the key we found from the wrapper function's pre-configured settings.
            The value at that key is the actual setting we want to apply to the `obj`
        Returns:
        --------
        (setting, mutated-object) : `tuple`
        """
        print(f"_st_func({obj}, {setting}")
        begin_index = hints[0][0]
        end_index = hints[-1][0]
        begin_paths = hints[0][1]
        end_paths = hints[-1][1]

        bdate = setting[begin_index]
        edate = setting[end_index]

        beginning = bdate if isinstance(bdate, str) else bdate.isoformat()
        ending = edate if isinstance(edate, str) else edate.isoformat()
        window_tup = (beginning, ending)
        print(f"Using the time widowing of: {window_tup}")

        for begin_path in begin_paths:
            obj[begin_path] = bdate
        for end_path in end_paths:
            obj[end_path] = edate

        return (setting, obj)

    return _st_func


def copy_over(collection, template, settings, mapper):
    configs = []

    for item in collection:
        config = dotmap.build(copy.deepcopy(dict(template)))
        tup = mapper(config, item)
        # yield cfg
        configs.append(tup)
    return configs
