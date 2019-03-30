#!/usr/bin/env python
"""Defines some other flags."""

import gflags

gflags.DEFINE_string('bar_gfs_path', '/gfs/path',
                    'Path to the GFS files for libbar.')
gflags.DEFINE_string('email_for_bar_errors', 'bar-team@google.com',
                    'Email address for bug reports about module libbar.')
gflags.DEFINE_boolean('bar_risky_hack', False,
                     'Turn on an experimental and buggy optimization.')
