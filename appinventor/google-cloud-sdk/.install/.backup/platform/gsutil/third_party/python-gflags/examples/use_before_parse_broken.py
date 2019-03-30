#!/usr/bin/env python
"""Broken due to access to flags before parsing."""

from google.apputils import app
import gflags

# Defines string with default value of 'default value'
gflags.DEFINE_string('test_flag', 'default value', 'Test flag')

FLAGS = gflags.FLAGS

# Assigns value of FLAGS.test_flag (string) to MY_CONST during module execution.
# Since flags were not parsed yet FLAGS.test_flag will always return default
# value of the flag.
MY_CONST = FLAGS.test_flag


def main(_):
  print MY_CONST  # Will ALWAYS output 'default value'

if __name__ == '__main__':
  app.run()  # Does a lot of useful stuff, including parsing flags.
