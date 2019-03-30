#!/usr/bin/env python
"""Example of the code that accesses flags at top of the module."""

import os
import unittest
import gflags

FLAGS = gflags.FLAGS

OUTPUT_DIR = os.path.join(FLAGS.my_dir, 'my_subdir')


class MyTest(googletest.TestCase):

  def setUp(self):
    self.filename = os.path.join(OUTPUT_DIR, 'filename')
