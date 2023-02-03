import os
import sys
import json
import logging
import datetime
import copy
from typing import Dict, Any
from uone_data_scraping.configs import templates
from uone_data_scraping.processing import dotmap
from uone_data_scraping.utils import helpers


_DSNAME = 'dialpad'

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


def _configs_for_pass_through(config, event):
    # if isinstance(config, str):
    #     config = dotmap.build(templates.config_base(_DSNAME, config))

    return config if isinstance(config, list) else [config]
