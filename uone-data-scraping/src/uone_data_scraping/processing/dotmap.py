import json
from copy import deepcopy as __copier


def build(data={}, **kwargs):
    """Create a DotMap object from dict-like inputs
    Params:
    -------
    - data : dict optional
    - kwargs : key-value pairs optional

    Returns:
    --------
    DotMap
    """
    name='DotMap'
    spec = {
        '__doc__': _custom_doc,
        '__init__': _custom_init,
        '__getattr__': _custom_getattr,
        '__getitem__': _custom_getitem,
        '__len__': _custom_len,
        '__repr__': _custom_repr,
        '__setitem__': _custom_setitem,
        '__str__': _custom_str,
        '_internals': [],
        '_originals': [],
        '_add_to_internals': _add_to_internals,
        '_flatten': _flatten,
        'include': _custom_include,
        'items': _custom_items,
        'keys': _custom_keys,
        'pop': _custom_pop,
        'to_json': _custom_to_json
    }
    kls = type(name, (), spec)
    if type(data).__name__ == kls.__name__:
        print(f"Recreating '{type(kls).__name__}' instance from original {type(data).__name__} keys and the current values they contain")
        data = {key: data[key] for key in data._originals}
        print(f"New input will be a merge of the new data: {data} and kwargs: {kwargs}")
        kls(__copier({**data, **kwargs}))
    elif isinstance(data, dict):
        data = __copier({**data, **kwargs})

    return kls(data)


def _custom_doc(kls):
    return __doc__()


def __doc__(self=None):
    return """
        dot.notation access to dictionary attributes
        Facilitate this excerpt taken from the api_scraper_func with pre and post processing

        Given, for `nic.schedule-adherence`:

            template = {
                'config': {
                    'method': 'GET',
                    'endpoint': 'services/v20.0/wfm-data/agents/schedule-adherence/'
                },
                'before-generate': [
                    {
                        'set_it': [
                            'mediaTypeIds',
                            {'de_cache_it': [{'get_it': ['$$config', 'client']}, 'mediaTypeIds']}
                        ]
                    }
                ],
                'results': {
                    'save_path': os.path.join(
                        os.getenv('STAGING_BUCKET', '{STAGING_BUCKET}'),
                        'endpoint=schedule-adherence',
                        'source=nice-in-contact',
                        'client={client}',
                        'start_time={startDate}',
                        'end_time={endDate}'
                    )
                },
                'params': {'startDate':None, 'endDate':None}
            }


        I should expect these keys to be directly accessible on a DotMap object:
        - `config`
        - `config.method`
        - `method`
        - `config.endpoint`
        - `endpoint`
        - `preprocess`
        - `preprocess.results.save_path.start_time`
        - `results.save_path.start_time`
        - `results`
        - `results.save_path`
        - `save_path`
        - `params`
        - `params.startDate`
        - `startDate`
        - `params.endDate`
        - `endDate`
        - `postprocess`
        - `postprocess.config.save_path`
        - `config.save_path`
        - `before-generate`

        i.e.

            dm = DotMap(template)
            dm['method'] == 'GET'
            dm.save_path == template['config']['save_path']

        etc

        A pretend lambda (the scraper) might use it something like this
        You should be able to run this from the scraper lambda:

            conf = DotMap(event)
            preprocessing = conf.preprocess
            for recipient, hookset in preprocessing:
                func = getattr(scraper, hookset[0])
                inputs = conf[hookset[1]]
                # ! mutation on conf !
                func(conf[recipient], inputs)

            resp = make_api_call(source=conf.source, endpoint=conf.endpoint, params=conf.params)

            print(f"responsense status: {resp}")

            if resp.status_code == 200:
                results = []
                postprocessing = conf.pop('after-scrape')
                for hook in postprocessing:
                    # ! mutation on conf !
                    results.append(processor.process_it(conf, hook, resp.json()))

                path = conf.save_path
                data = format_data(results)
                boto3.client('s3').put_object(Bucket=BUCKET, Key=path, Body=data)

    """


def _custom_init(kls, data={}, **kwargs):
    """__init__() function to be included in the custom class built in `build_dotmap()`
    Params:
    -------
    - data : dict optional
    - kwargs : key-value pairs optional

    Returns:
    --------
    None
    """
    copied = __copier({**data, **kwargs})
    kls._originals = [k for k in copied]
    for k,v in _flatten(copied, refs={}).items():
        _custom_setitem(kls, k, v, on_init=True)


def _custom_getattr(kls, key):
    """Facilitate easy accessors by overriding __getattr__()`

    Params:
    -------
    - kls : class (self)
    - key : str
        key used to search `kls` for matching attributes

    Returns:
    --------
    the attribute on the `kls` of the name given in the `key` param or None if attribute was not found
         : obj|None
    """
    if key in _custom_keys(kls):
        return _custom_getitem(kls, key)
    else:
        return None


def _custom_getitem(kls, key):
    """Facilitate easy accessors by overriding __getitem__()`

    Params:
    -------
    - kls : class (self)
    - key : str
        key used to search `kls` for matching attributes

    Returns:
    --------
    the attribute on the `kls` of the name given in the `key` param or None if attribute was not found
         : obj|None

    Notes:
    ------
    `_custom_getitem(kls,newkey)` is called again (recursion) if a '.' is found in the keyname.
    A '.' in the keyname signifies a nested lookup, which can only be found by searching the
    internal data's nested structure.
    """
    try:
        return kls[key] if isinstance(kls, dict) else object.__getattribute__(kls, key)
    except AttributeError as ae:
        if '.' in key:
            path = key.split('.')
            newkey = '.'.join(path[1:])
            return _custom_getitem(kls, newkey)
        else:
            return None


def _add_to_internals(kls, key):
    """Add a key to the list of keys used to do lookups

    Params:
    -------
    - kls : class (self)
    - key : str
        key to add to the internal list of keys for lookups.

    Returns:
    --------
    None

    Notes:
    ------
    This function shouldn't really be called by users; It's only
    used internally to facilitate things like nested lookups
    """
    if key not in kls._internals:
        kls._internals.append(key)

    path = key.split('.')
    itter = list(range(len(path)-1))
    for i in itter:
        nm = '.'.join(path[:i+1])

        if nm not in kls._internals:
            kls._internals.append(nm)

    for i in itter:
        nm = '.'.join(path[i+1:])

        if nm not in kls._internals:
            kls._internals.append(nm)


def _set_nested(kls, path, val):
    """Set values on the internal data storage for nested paths, which are denoted with dot-notation.
    Each '.' signifies a level of nesting to be used when setting the value on the internal data dict.

    Params:
    -------
    - kls : class (self)
    - path : str
        the location/address used to set the value on the internal data.  The path can be a regular
        key, which is usually just a string.  The path can also be a string with dots or periods in it.
        The dots are used to signify that the path is nested.

            'some.nested.path'

        This path would set a dictionary like this `internal_data['some']['nested']['path'] = val`
    - val : any
        value to set at the given `path`

    Returns:
    --------
    `val` : any

    Notes:
    ------
    !! This only works on nested paths up to 10 levels of nesting !!
    """

    copied = __copier(val)
    if len(path) == 1:
        kls[path[0]] = copied
    elif len(path) == 2:
        kls[path[0]][path[1]] = copied
    elif len(path) == 3:
        kls[path[0]][path[1]][path[2]] = copied
    elif len(path) == 4:
        kls[path[0]][path[1]][path[2]][path[3]] = copied
    elif len(path) == 5:
        kls[path[0]][path[1]][path[2]][path[3]][path[4]] = copied
    elif len(path) == 1:
        kls[path[0]][path[1]][path[2]][path[3]][path[4]][path[5]] = copied
    elif len(path) == 2:
        kls[path[0]][path[1]][path[2]][path[3]][path[4]][path[5]][path[6]] = copied
    elif len(path) == 3:
        kls[path[0]][path[1]][path[2]][path[3]][path[4]][path[5]][path[6]][path[7]] = copied
    elif len(path) == 4:
        kls[path[0]][path[1]][path[2]][path[3]][path[4]][path[5]][path[6]][path[7]][path[8]] = copied
    elif len(path) == 5:
        kls[path[0]][path[1]][path[2]][path[3]][path[4]][path[5]][path[6]][path[7]][path[8]][path[9]] = copied


def _custom_setitem(kls, key, val, on_init=False):
    """Override `__setitem__()` in a custom DotMap class

    Params:
    -------
    - kls : class (self)
    - key : str
        key to be used to set the value on the internal data
    - val : Object
        value to set on the custom DotMap's internal data storage
    - on_init : bool defaulted to `False`
        setting used to initialize internal data structures and references.
        This isn't required to update because `_flatten()` is used to pull out
        the required setting structure when the DotMap is not being initialized.
        During initialization this is required.

    Returns:
    --------
    `val` : Object
    """
    setattr(kls, key, val)
    _add_to_internals(kls, key)

    if on_init:
        if '.' in key:
            _set_nested(kls, key.split('.'), val)
            setattr(kls, key.split('.')[-1], val)
            _add_to_internals(kls, key.split('.')[-1])
    else:
        for k,v in _flatten({key:val}, refs={}).items():
            setattr(kls, k, v)
            if '.' in k:
                _set_nested(kls, key.split('.'), val)
                setattr(kls, k.split('.')[-1], v)
                _add_to_internals(kls, key)
            _add_to_internals(kls, key)
    return val


def _flatten(data, dotted_key=None, refs={}):
    """Flatten a nested dictionary

    Params:
    -------
    - data : dict
    - dotted_key : bool default=None
        used to _also_ set the actual dot-path during an invocation that caused
        recursion
    - refs : dict default={}
        used to maintain references during recursion so the nesting can be flattened

    Returns:
    --------
    flat dict : dict

    Notes:
    ------
    This is used mainly to create flat references (as opposed to nested lookups)
    for dictionary data
    """
    if dotted_key:
        refs[dotted_key] = data

    for k,v in data.items():
        refs[k] = v

        if dotted_key:
            k = f"{dotted_key}.{k}"
            refs[k] = v

        if not isinstance(v, dict):
            refs[k] = v
        else:
            _flatten(v, dotted_key=k, refs=refs)
            dotted_key=None

    return refs


def _custom_len(kls):
    return len(kls.keys())


def _custom_keys(kls):
    return kls._internals


def _custom_include(kls, data, **kwargs):
    for k,v in _flatten({**data, **kwargs}, refs={}).items():
        kls[k] = v


def _custom_items(kls):
    for k in _custom_keys(kls):
        yield (k,getattr(kls, k))


def _custom_str(kls):
    return str(dict(kls))


def _custom_repr(kls):
    return f"{type(kls).__name__}({_custom_str(kls)})"


def _custom_pop(kls, key, default=None, **kwargs):
    try:
        val = getattr(kls, key)
        delattr(kls, kls._internals.pop(kls._internals.index(key)))
        return val
    except AttributeError:
        return default
    except ValueError:
        return default


def _custom_to_json(kls):
    def _str_recurse(obj):
        new_obj = {}
        for k,v in obj.items():
            if isinstance(v, dict):
                val = _str_recurse(v)
            else:
                if 'isoformat' in dir(v):
                    val = v.isoformat()
                elif 'total_seconds' in dir(v):
                    val = v.total_seconds()
                else:
                    val = v
            new_obj[k] = val
        return new_obj
    return json.dumps(_str_recurse(dict(kls)))
