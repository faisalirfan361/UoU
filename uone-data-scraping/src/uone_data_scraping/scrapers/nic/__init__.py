# from . import nic as nscrape


# def _get_scraper(source):
#     return {'nice-in-contact': nreq, 'nic': nreq}[source]


def make_api_call(source, endpoint, params={}):
    """Make an API call using a scraper.
    This function is a wrapper function, basically.  It ends up calling
    a scraper's `make_api_call` function which will use the request config
    it got.  It can receive its request config from things like an SQS::Queue,
    database or even using a config template from this package.  It will find
    that request config because each scraper is able to figure out where to get
    it, based on the params that were passed in to this function.

    Params:
    -------
    - source : str
        The data source to make an API call against
    - endpoint : str
        The endpoint or target name to make an API call against
    - params : dict default={}
        Params dictionary that can be sent along to the scraper's `_make_api_call()`
        function

    Returns:
    --------
    results from a scraper._make_api_call() result.  This is usually the result of
    something like `requests.get(url, headers=headers)`
    """
    print(f"Making API call: source: {source}, endpoint: {endpoint}, params={params}")
    scraper = _get_scraper(source)
    resutls = scraper._make_api_call(endpoint=endpoint, params=params)
    return results
