#!/usr/bin/env python
"""RegisterValidator example."""

import gflags

FLAGS = gflags.FLAGS

gflags.DEFINE_integer('my_version', 0, 'Version number.')
gflags.DEFINE_string('filename', None, 'Input file name', short_name='f')

gflags.RegisterValidator('my_version',
                        lambda value: value % 2 == 0,
                        message='--my_version must be divisible by 2')
gflags.MarkFlagAsRequired('filename')
