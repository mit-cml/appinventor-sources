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




"""Deprecated endpoints module from google.appengine.ext."""






import itertools
import logging
import os
import re
import sys

logging.warning('Importing endpoints from google.appengine.ext is deprecated '
                'and will be removed.  Add the endpoints library to '
                'app.yaml, then endpoints can be imported simply with '
                '"import endpoints".')


if 'APPENGINE_RUNTIME' not in os.environ:


  if not hasattr(sys, 'version_info'):
    raise RuntimeError('Endpoints library isn\'t available in older Python '
                       'runtime environments. Use the python27 runtime.')
  version_tuple = tuple(sys.version_info[:2])
  if version_tuple < (2, 7):
    raise RuntimeError('Endpoints library isn\'t available in python %d.%d. '
                       'Use version 2.7 or greater.' % version_tuple)
elif os.environ['APPENGINE_RUNTIME'] == 'python':
  raise RuntimeError('Endpoints library isn\'t available in python 2.5 '
                     'runtime. Use the python27 runtime instead.')





for path in sys.path:
  lib_path, final_dir = os.path.split(path)
  if re.match('webapp2-.+', final_dir):
    endpoints_path = os.path.join(lib_path, 'endpoints-1.0')
    if endpoints_path not in sys.path:
      sys.path.append(endpoints_path)
    break














from endpoints import *



__version__ = '1.0'
