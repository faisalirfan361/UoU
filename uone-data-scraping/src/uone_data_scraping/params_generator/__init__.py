from . import nic
from . import genesys
from . import dialpad
from uone_data_scraping.utils import helpers
from uone_data_scraping.processing import dotmap


def get_generator(source):
    """Retrieve a `params_generator` module by data source name

    Params:
    -------
    - source : str

    Returns:
    --------
    a sub-module of the `params_generator` module : module
    """
    try:
        return {
            'Genesys': genesys.api,
            'genesys': genesys.api,
            'gen': genesys.api,
            'NiceInContact': nic.api,
            'niceincontact': nic.api,
            'nice-in-contact': nic.api,
            'nic': nic.api,
            'nice': nic.api,
            'dialpad': dialpad.api
        }[source]
    except KeyError:
        raise ModuleNotFoundError(f"Requested params generator not found for source='{source}'")


def targets(source, pattern=''):
    """Use `params_generator`s to find target endpoint(s), queue(s), DB(s), etc, for this `source`

    Params:
    -------
    - source : str
        data source you want to find targets for
    - pattern : str
        pattern to match target names against.  This filter filters out non-matches

    Returns:
    --------
    dictionary of target names as keys and config base for that target as a value in the dict
    """
    generator = get_generator(source)
    assert generator._DSNAME == source # why not?  'course...why?
    return generator.targets(pattern=pattern)


def configs_for(source, target, event):
    """Generate configuration objects for use in API scraping.
    This is a wrapper function that actually calls other functions on integration-specific
    modules to get the job done.

    Params:
    -------
    - source : str
        data source; e.g. 'nic', 'dialpad', 'genesys', etc
    - target : str
        name of the endpoint, DB, etc to be queried during the scrape
    - event : dict
        event for the scraping job which will be used to configure the config object

    Returns:
    --------
    all config items : list[dict]

    Notes:
    ------
    Calls one of:
    - `params_generator.dialpad.api.configs_for_<bucketing strategy>()`
    - `params_generator.genesys.api.configs_for_<bucketing strategy>()`
    - `params_generator.nic.api.configs_for_<bucketing strategy>()`
    """
    print(f"Finding generator for {source} against target: {target} and event: {event}")
    generator = get_generator(source)
    if isinstance(target, str):
        target = dotmap.build(targets(source, target)[target])
    bucketing_strategy = target.pop('bucketed_by') or 'pass_through' # helpers.find_bucketing_strategy(target) or 'pass_through'
    bucketing_strategy = bucketing_strategy.replace('-', '_').replace(' ', '_')
    print(f"Found bucketing strategy of {bucketing_strategy}")

    funcname = f"_configs_for_{bucketing_strategy}"
    print(f"Searching for function that matches generator and ~{source}.{funcname} for {bucketing_strategy}")
    func = getattr(generator, funcname)
    print(f"Running configs generator...")
    results = func(target, event)

    return results

