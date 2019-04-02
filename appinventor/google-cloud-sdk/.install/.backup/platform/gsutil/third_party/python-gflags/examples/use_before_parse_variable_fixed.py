#!/usr/bin/env python
"""Proposed fix."""

import os
import unittest
import gflags

FLAGS = gflags.FLAGS


def _get_output_dir():
  return os.path.join(FLAGS.my_dir, 'my_subdir')


class MyTest(googletest.TestCase):

  def setUp(self):
    self.filename = os.path.join(_get_output_dir(), 'filename')
