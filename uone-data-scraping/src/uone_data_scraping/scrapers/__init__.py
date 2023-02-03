# from . import nic
from .nic import scraper as nic_scraper
from .genesys import scraper as gen_scraper
from .dialpad import scraper as dialpad_scraper
from . import genesys
from ..utils import helpers


def get_scraper(source):
    """Get a scraper for a given data source

    Params:
    -------
    - source : str
        The data source we'll be scraping against.
        Right now, you'll only see 'nic', 'nice-in-contact', 'gen', 'genesys', 'dialpad'

    Returns:
    --------
    scraper for a given data source : scrapers.*
    """
    print(f"Loading scraper for {source}")
    try:
        return {
            'dialpad': dialpad_scraper,
            'dp': dialpad_scraper,
            'incontact': nic_scraper,
            'niceincontact': nic_scraper,
            'nic': nic_scraper,
            'genesys': gen_scraper,
            'gen': gen_scraper
        }[source.lower().replace('-','').replace('_','').replace(' ','')]
    except KeyError:
        raise ModuleNotFoundError(f"Requested API scraper not found for source='{source}'")


# def endpoints_for(source):
#     print(f"Loading targets for {source}")
#     scraper = get_scraper(source)
#     return scraper.targets()
