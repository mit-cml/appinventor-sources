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
"""One place for all runtime instance factories."""



from google.appengine.tools.devappserver2.custom import instance_factory as custom_factory
from google.appengine.tools.devappserver2.go import instance_factory as go_factory
from google.appengine.tools.devappserver2.php import instance_factory as php_factory
from google.appengine.tools.devappserver2.python import instance_factory as python_factory

# pylint: disable=g-import-not-at-top
try:
  from google.appengine.tools.devappserver2.java import instance_factory as java_factory
except ImportError:
  java_factory = None
# pylint: enable=g-import-not-at-top

# TODO - b/34669624, automatically get Version of python runtime in prod.
PYTHON27_PROD_VERSION = (2, 7, 12)


MODERN_RUNTIMES = set(['python37', 'go111'])


FACTORIES = {
    'go': go_factory.GoRuntimeInstanceFactory,
    'go111': go_factory.GoRuntimeInstanceFactory,
    'php55': php_factory.PHPRuntimeInstanceFactory,
    'php72': php_factory.PHPRuntimeInstanceFactory,
    'python': python_factory.PythonRuntimeInstanceFactory,
    'python37': python_factory.PythonRuntimeInstanceFactory,
    'python27': python_factory.PythonRuntimeInstanceFactory,
    'python-compat': python_factory.PythonRuntimeInstanceFactory,
    'custom': custom_factory.CustomRuntimeInstanceFactory,
}
if java_factory:
  FACTORIES.update({
      'java': java_factory.JavaRuntimeInstanceFactory,
      'java7': java_factory.JavaRuntimeInstanceFactory,
      'java8': java_factory.JavaRuntimeInstanceFactory,
  })


def valid_runtimes():
  return FACTORIES.keys()
