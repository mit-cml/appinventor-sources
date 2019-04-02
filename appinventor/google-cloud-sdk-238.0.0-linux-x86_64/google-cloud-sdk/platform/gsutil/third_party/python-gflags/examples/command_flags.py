#!/usr/bin/env python
"""Example for appcomands flag usage."""

from __future__ import print_function

from google.apputils import appcommands
import gflags

FLAGS = gflags.FLAGS


class HelloWorld(appcommands.Cmd):

  def __init__(self, name, flag_values, **kargs):
    super(HelloWorld, self).__init__(name, flag_values, kargs)

    # Define the new flag inside the __init__ function of the class.
    gflags.DEFINE_bool('world', False, 'Display world as well',
                      flag_values=flag_values)

  def Run(self, argv):
    # Output different things depending on flag value.
    if FLAGS.world:
      print('Hello world')
    else:
      print('Hello')


def main(unused_argv):
  appcommands.AddCmd('hello', HelloWorld, help_full='Runs hello world')


if __name__ == '__main__':
  appcommands.Run()
