# -*- coding: utf-8 -*-
"""
    tests.conftest
    ~~~~~~~~~~~~~~

    :copyright: (c) 2014 by the Werkzeug Team, see AUTHORS for more details.
    :license: BSD, see LICENSE for more details.
"""

import pytest


@pytest.fixture
def xprocess():
    pytest.skip('pytest-xprocess not installed.')

@pytest.fixture
def dev_server():
    pytest.skip('dev_server disabled.')
