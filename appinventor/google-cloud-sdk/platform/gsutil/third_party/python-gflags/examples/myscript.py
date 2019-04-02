#!/usr/bin/env python
"""Adopts all flags from libfoo and one flag from libbar."""

import gflags
from gflags.examples import libbar  # pylint: disable=unused-import
from gflags.examples import libfoo

gflags.DEFINE_integer('num_iterations', 0, 'Number of iterations.')

# Declare that all flags that are key for libfoo are
# key for this module too.
gflags.ADOPT_module_key_flags(libfoo)

# Declare that the flag --bar_gfs_path (defined in libbar) is key
# for this module.
gflags.DECLARE_key_flag('bar_gfs_path')
