# Copyright (c) The PyAMF Project.
# See LICENSE.txt for details.

"""
C{weakref} support.

@since: 0.6.2
"""

import weakref

import pyamf
from pyamf.adapters import util


class Foo(object):
    pass


weakref_type = type(weakref.ref(Foo()))


def get_referent(reference, **kwargs):
    return reference()


pyamf.add_type(weakref_type, get_referent)


if hasattr(weakref, 'WeakValueDictionary'):
    pyamf.add_type(weakref.WeakValueDictionary, util.to_dict)


if hasattr(weakref, 'WeakSet'):
    pyamf.add_type(weakref.WeakSet, util.to_list)
