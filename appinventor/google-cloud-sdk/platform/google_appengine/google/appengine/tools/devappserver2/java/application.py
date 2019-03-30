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
"""An abstraction around the source and classfiles for a Java application."""




import os
import os.path
import google


# The location of devappserver2 source code changes infrequently enough we are
# confident depending on relative paths.
#
# __file__ is located at:
# <SDK_ROOT>/google/appengine/tools/devappserver2/java/application.py
_SDKROOT = os.path.abspath(__file__)
for _ in range(6):
  _SDKROOT = os.path.dirname(_SDKROOT)


class JavaApplication(object):
  """An abstraction around the compiled class files for a Java application."""

  def __init__(self, module_configuration):
    """Initializer for Module.

    Args:
      module_configuration: An application_configuration.ModuleConfiguration
          instance storing the configuration data for a module.
    """
    self._module_configuration = module_configuration

  def get_environment(self):
    """Return the environment that should be used to run the Java executable."""

    # Allow the SDK root to be overwritten if we need to use a Java sdk in a
    # different directory
    sdkroot = os.getenv('SDKROOT', _SDKROOT)

    environ = {'APPLICATION_ID': self._module_configuration.application,
               'GAE_SERVICE': 'default',
               'GAE_ENV': 'localdev',
               'GAE_RUNTIME': self._module_configuration.runtime,
               'PWD': self._module_configuration.application_root,
               'SDKROOT': sdkroot,
               'TZ': 'UTC'}

    if self._module_configuration.application:
      environ['GAE_APPLICATION'] = self._module_configuration.application
    if self._module_configuration.major_version:
      environ['GAE_VERSION'] = self._module_configuration.major_version

    # Most of the env variables are needed for a JVM on Windows.
    for var in ('PATH', 'SYSTEMROOT', 'USER', 'TMP', 'TEMP'):
      if var in os.environ:
        environ[var] = os.environ[var]
    return environ
