# -*- coding: utf-8 -*- #
# Copyright 2017 Google Inc. All Rights Reserved.
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
"""Utilities for interacting with message classes and instances."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import six


def UpdateMessage(message, diff):
  """Updates given message from diff object recursively.

  The function recurses down through the properties of the diff object,
  checking, for each key in the diff, if the equivalent property exists on the
  message at the same depth. If the property exists, it is set to value from the
  diff. If it does not exist, that diff key is silently ignored. All diff keys
  are assumed to be strings.

  Args:
    message: An apitools.base.protorpclite.messages.Message instance.
    diff: A dict of changes to apply to the message
      e.g. {'settings': {'availabilityType': 'REGIONAL'}}.

  Returns:
    The modified message instance.
  """
  if diff:
    return _UpdateMessageHelper(message, diff)
  return message


def _UpdateMessageHelper(message, diff):
  for key, val in six.iteritems(diff):
    if hasattr(message, key):
      if isinstance(val, dict):
        _UpdateMessageHelper(getattr(message, key), diff[key])
      else:
        setattr(message, key, val)
  return message
