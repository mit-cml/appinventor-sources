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



"""Convenience wrapper for starting an appengine tool."""



import os
import sys
import time

sys_path = sys.path
try:
  sys.path = [os.path.dirname(__file__)] + sys.path

  import wrapper_util

finally:
  sys.path = sys_path

wrapper_util.reject_old_python_versions((2, 5))
if sys.version_info < (2, 6):
  sys.stderr.write(
      'WARNING: In an upcoming release the SDK will no longer support Python'
      ' 2.5. Users should upgrade to Python 2.6 or higher.\n')
  time.sleep(1)




def get_dir_path(sibling):
  """Get a path to the directory of this script.

  By default, the canonical path (symlinks resolved) will be returned. In some
  environments the canonical directory is not sufficient because different
  parts of the SDK are referenced by symlinks, including this very module's
  file. In this case, the non-canonical path to this file's directory will be
  returned (i.e., the directory where the symlink lives, not the directory
  where it points).

  Args:
    sibling: Relative path to a sibling of this module file. Choose a sibling
    that is potentially symlinked into the parent directory.

  Returns:
    A directory name.

  Raises:
    ValueError: If no proper path could be determined.
  """
  return wrapper_util.get_dir_path(__file__, sibling)













DIR_PATH = get_dir_path(os.path.join('lib', 'ipaddr'))
_PATHS = wrapper_util.Paths(DIR_PATH)

SCRIPT_DIR = _PATHS.default_script_dir
GOOGLE_SQL_DIR = _PATHS.google_sql_dir

EXTRA_PATHS = _PATHS.v1_extra_paths

API_SERVER_EXTRA_PATHS = _PATHS.api_server_extra_paths

ENDPOINTSCFG_EXTRA_PATHS = _PATHS.endpointscfg_extra_paths


OAUTH_CLIENT_EXTRA_PATHS = _PATHS.oauth_client_extra_paths


GOOGLE_SQL_EXTRA_PATHS = _PATHS.google_sql_extra_paths




def fix_sys_path(extra_extra_paths=()):
  """Fix the sys.path to include our extra paths."""
  sys.path = EXTRA_PATHS + list(extra_extra_paths) + sys.path


def run_file(file_path, globals_):
  """Execute the given script with the passed-in globals.

  Args:
    file_path: the path to the wrapper for the given script. This will usually
      be a copy of this file.
    globals_: the global bindings to be used while executing the wrapped script.
  """
  script_name = os.path.basename(file_path)

  sys.path = (_PATHS.script_paths(script_name) +
              _PATHS.scrub_path(script_name, sys.path))







  if 'google' in sys.modules:
    del sys.modules['google']

  execfile(_PATHS.script_file(script_name), globals_)


if __name__ == '__main__':
  run_file(__file__, globals())
