# Copyright (c) The PyAMF Project.
# See LICENSE.txt for details.

"""
Useful helpers for adapters.

@since: 0.4
"""


def to_list(obj, encoder):
    """
    Converts an arbitrary object C{obj} to a C{list}.
    """
    return list(obj)


def to_dict(obj, encoder):
    """
    Converts an arbitrary object C{obj} to a C{dict}.
    """
    return dict(obj)


def to_set(obj, encoder):
    """
    Converts an arbitrary object C{obj} to a C{set}.
    """
    return set(obj)


def to_tuple(obj, encoder):
    """
    Converts an arbitrary object C{obj} to a C{tuple}.
    """
    return tuple(obj)


def to_string(obj, encoder):
    """
    Converts an arbitrary object C{obj} to a string.

    Change in 0.7: This now returns a unicode object for Python 2

    @since: 0.5
    """
    return unicode(obj)


def to_bytes(obj, encoder):
    """
    Converts an arbitrary object C{obj} to a byte string.

    @since: 0.7
    """
    return str(obj)
