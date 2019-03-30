#!/usr/bin/env python
"""Example for basic usage of the flags library."""

from google.apputils import app
import gflags

FLAGS = gflags.FLAGS

# Flag names are globally defined!  So in general, we need to be
# careful to pick names that are unlikely to be used by other libraries.
# If there is a conflict, we'll get an error at import time.
gflags.DEFINE_string('name', 'Mr. President', 'your name')
gflags.DEFINE_integer('age', None, 'your age in years', lower_bound=0)
gflags.DEFINE_boolean('debug', False, 'produces debugging output')
gflags.DEFINE_enum('job', 'running', ['running', 'stopped'], 'job status')


def main(argv):
  if FLAGS.debug:
    print 'non-flag arguments:', argv
  print 'Happy Birthday', FLAGS.name
  if FLAGS.age is not None:
    print 'You are %d years old, and your job is %s' % (FLAGS.age, FLAGS.job)

if __name__ == '__main__':
  app.run()
