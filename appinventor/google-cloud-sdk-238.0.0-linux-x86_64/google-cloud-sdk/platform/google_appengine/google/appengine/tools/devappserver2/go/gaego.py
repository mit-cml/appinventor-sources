#!/usr/bin/env python
#
# Copyright 2007 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
"""An abstraction around the source and executable for a stock Go app."""

import atexit
import logging
import os
import os.path
import tempfile

import google
from google.appengine.tools.devappserver2.go import errors as go_errors
from google.appengine.tools.devappserver2.go.managedvm import _file_is_executable
from google.appengine.tools.devappserver2.go.managedvm import _rmtree
from google.appengine.tools.devappserver2.go.managedvm import _run_tool


class GaeGoApplication(object):
  """An abstraction around the source and executable for a stock Go app."""

  def __init__(self, module_configuration, work_dir, enable_debugging=False):
    """Initializer for Module.

    Args:
      module_configuration: An application_configuration.ModuleConfiguration
          instance storing the configuration data for a module.
      work_dir: Directory to store intermediate files.
      enable_debugging: Enable build flags for debugging.
    """
    self._module_configuration = module_configuration
    self._go_executable = None
    self._work_dir = None
    self._main_executable_path = os.path.normpath(
        getattr(self._module_configuration, 'main', ''))
    dotslash = '.' + os.path.sep
    if (self._main_executable_path != '.' and
        not self._main_executable_path.startswith(dotslash) and
        os.path.exists(
            os.path.join(self._module_configuration.application_root,
                         self._main_executable_path))):
      self._main_executable_path = dotslash + self._main_executable_path
    if work_dir:
      # Multiple modules might be running within the same server.
      # These must not share a single workdir, as otherwise the build fails
      # unpredictably.
      self._work_dir = os.path.join(
          work_dir, self._module_configuration.module_name)
    self._enable_debugging = enable_debugging

  @property
  def go_executable(self):
    """The path to the Go executable. None if it has not been built."""
    return self._go_executable

  def get_environment(self):
    """Return the environment that will be used to run the Go executable."""
    environ = os.environ.copy()
    environ['RUN_WITH_DEVAPPSERVER'] = '1'
    return environ

  def _build(self):
    """Builds the app locally.

    Note that the go compiler must be called from within the app directory.
    Otherwise, it returns an error like:
    can't load package: package /a/b: import "/a/b": cannot import absolute path

    Raises:
      BuildError: if build fails.
    """
    logging.debug('Building Go application')

    app_root = self._module_configuration.application_root
    exe_name = os.path.join(self._work_dir, '_ah_exe')
    args = ['build', '-o', exe_name, self._main_executable_path]
    if self._enable_debugging:
      args.extend(['-N', '-l'])
    try:
      cwd = os.getcwd()
      os.chdir(app_root)
      logging.debug('Working from dir %s', os.getcwd())
      stdout, stderr = _run_tool('go', args)
    finally:
      os.chdir(cwd)
    if not _file_is_executable(exe_name):
      # TODO: Fix this doc string
      raise go_errors.BuildError(
          'Your Go app must use "package main" and must provide a func main(). '
          'See https://cloud.google.com/appengine/docs/standard/go/'
          'building-app/creating-your-application#creating_your_maingo_file '
          'for more information.')
    logging.debug('Build succeeded:\n%s\n%s', stdout, stderr)
    self._go_executable = exe_name

  def maybe_build(self, maybe_modified_since_last_build):
    """Builds an executable for the application if necessary.

    Args:
      maybe_modified_since_last_build: True if any files in the application root
          or the GOPATH have changed since the last call to maybe_build, False
          otherwise. This argument is used to decide whether a build is Required
          or not.

    Returns:
      True if compilation was successfully performed (will raise
        an exception if compilation was attempted but failed).
      False if compilation was not attempted.

    Raises:
      BuildError: if building the executable fails for any reason.
    """
    if not self._work_dir:
      self._work_dir = tempfile.mkdtemp('appengine-go-bin')
      atexit.register(_rmtree, self._work_dir)

    if self._go_executable and not maybe_modified_since_last_build:
      return False

    if self._go_executable:
      logging.debug('Rebuilding Go application due to source modification')
    else:
      logging.debug('Building Go application')
    self._build()
    return True
