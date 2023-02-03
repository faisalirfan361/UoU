import os
from setuptools import setup, find_packages
from setuptools.config import read_configuration


def set_version():
    name = "uone_data_scraping"
    here = os.path.abspath(os.path.dirname(__file__))
    config = read_configuration(os.path.join(here, 'setup.cfg'))
    version = config['metadata']['version']
    return version

setup(version=set_version(),
      py_modules=['uone_data_scraping'],
      packages=find_packages(exclude=['tests*']),
      install_requires=['python-dateutil', 'requests'])
