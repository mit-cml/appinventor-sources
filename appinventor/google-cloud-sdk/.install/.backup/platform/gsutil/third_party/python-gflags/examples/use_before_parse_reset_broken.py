#!/usr/bin/env python
"""FLAGS.Reset() is evli."""

import unittest
import gflags

FLAGS = gflags.FLAGS


class CertCheckTest(googletest.TestCase):

  def setUp(self):
    FLAGS.Reset()  # Resets all flags, including stuff like test_srcdir.

  def testFoo(self):
    FLAGS.foo = 'bar'
    self.assertEqual(2, 2)
