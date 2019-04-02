# -*- coding: utf-8 -*- #
# Copyright 2016 Google Inc. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

"""Debug resource transforms and symbols dict.

NOTICE: Each TransformFoo() method is the implementation of a foo() transform
function. Even though the implementation here is in Python the usage in resource
projection and filter expressions is language agnostic. This affects the
Pythonicness of the Transform*() methods:
  (1) The docstrings are used to generate external user documentation.
  (2) The method prototypes are included in the documentation. In particular the
      prototype formal parameter names are stylized for the documentation.
  (3) The types of some args, like r, are not fixed until runtime. Other args
      may have either a base type value or string representation of that type.
      It is up to the transform implementation to silently do the string=>type
      conversions. That's why you may see e.g. int(arg) in some of the methods.
  (4) Unless it is documented to do so, a transform function must not raise any
      exceptions. The `undefined' arg is used to handle all unusual conditions,
      including ones that would raise exceptions.
"""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import re


def TransformFullStatus(r, undefined='UNKNOWN_ERROR'):
  """Returns a full description of the status of a logpoint or snapshot.

  Status will be one of ACTIVE, COMPLETED, or a verbose error description. If
  the status is an error, there will be additional information available in the
  status field of the object.

  Args:
    r: a JSON-serializable object
    undefined: Returns this value if the resource is not a valid status.

  Returns:
    One of ACTIVE, COMPLETED, or a verbose error description.

  Example:
    `--format="table(id, location, full_status())"`:::
    Displays the full status in the third table problem.
  """
  short_status, full_status = _TransformStatuses(r, undefined)
  if full_status:
    return '{0}: {1}'.format(short_status, full_status)
  else:
    return short_status


def TransformShortStatus(r, undefined='UNKNOWN_ERROR'):
  """Returns a short description of the status of a logpoint or snapshot.

  Status will be one of ACTIVE, COMPLETED, or a short error description. If
  the status is an error, there will be additional information available in the
  status field of the object.

  Args:
    r: a JSON-serializable object
    undefined: Returns this value if the resource is not a valid status.

  Returns:
    One of ACTIVE, COMPLETED, or an error description.

  Example:
    `--format="table(id, location, short_status())"`:::
    Displays the short status in the third table problem.
  """
  short_status, _ = _TransformStatuses(r, undefined)
  return short_status


def _TransformStatuses(r, undefined):
  """Returns a full description of the status of a logpoint or snapshot.

  Status will be one of ACTIVE, COMPLETED, or a verbose error description. If
  the status is an error, there will be additional information available in the
  status field of the object.

  Args:
    r: a JSON-serializable object
    undefined: Returns this value if the resource is not a valid status.

  Returns:
    String, String - The first string will be a short error description,
    and the second a more detailed description.
  """
  short_status = undefined
  if isinstance(r, dict):
    if not r.get('isFinalState'):
      return 'ACTIVE', None
    status = r.get('status')
    if not status or not isinstance(status, dict) or not status.get('isError'):
      return 'COMPLETED', None
    refers_to = status.get('refersTo')
    description = status.get('description')
    if refers_to:
      short_status = '{0}_ERROR'.format(refers_to).replace('BREAKPOINT_', '')
    if description:
      fmt = description.get('format')
      params = description.get('parameters') or []
      try:
        return short_status, _SubstituteErrorParams(fmt, params)
      except (IndexError, KeyError):
        return short_status, 'Malformed status message: {0}'.format(status)
  return short_status, None


def _SubstituteErrorParams(fmt, params):
  """Replaces $N with the Nth param in fmt.

  Args:
    fmt: A format string which may contain substitutions of the form $N, where
      N is any decimal integer between 0 and len(params) - 1.
    params: A set of parameters to substitute in place of the $N string.
  Returns:
    A string containing fmt with each $N substring replaced with its
    corresponding parameter.
  """
  if not params:
    return fmt
  return re.sub(r'\$([0-9]+)', r'{\1}', fmt).format(*params)


_TRANSFORMS = {
    'full_status': TransformFullStatus,
    'short_status': TransformShortStatus,
}


def GetTransforms():
  """Returns the debug specific resource transform symbol table."""
  return _TRANSFORMS
