import os
from setuptools import setup, find_packages
from setuptools.config import read_configuration


def set_version():
    name="uone_caching",
    here = os.path.abspath(os.path.dirname(__file__))
    config = read_configuration(os.path.join(here, 'setup.cfg'))
    version = config['metadata']['version']
    try:
        branch_name = os.environ['BRANCH_NAME']
        if branch_name != "dev" and "dev" in version:
            version += "-" + branch_name
    except KeyError:
        pass
    return version

setup(version=set_version(),
      py_modules=['uone_caching'],
      install_requires=['simplejson', 'boto3'])
