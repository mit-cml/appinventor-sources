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
"""The python hooks for IAM surface."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.iam import util
from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.calliope import exceptions as gcloud_exceptions
from googlecloudsdk.command_lib.iam import iam_util


def UpdateRequestWithConditionFromFile(ref, args, request):
  """Python hook to add condition from --condition-from-file to request.

  Args:
    ref: A resource ref to the parsed resource.
    args: Parsed args namespace.
    request: The apitools request message to be modified.

  Returns:
    The modified apitools request message.
  """
  del ref
  if args.IsSpecified('condition_from_file'):
    _, messages = util.GetClientAndMessages()
    condition_message = messages.Expr(
        description=args.condition_from_file.get('description'),
        title=args.condition_from_file.get('title'),
        expression=args.condition_from_file.get('expression'))
    request.condition = condition_message
  return request


def _ConditionFileFormatException(filename):
  return gcloud_exceptions.InvalidArgumentException(
      'condition-from-file',
      '{filename} must be a path to a YAML or JSON file containing the '
      'condition. `expression` and `title` are required keys. `description` is '
      'optional.'.format(filename=filename))


def ParseConditionFromFile(condition_from_file):
  """Read condition from YAML or JSON file."""

  condition = arg_parsers.BufferedFileInput()(condition_from_file)
  condition_dict = iam_util.ParseYamlOrJsonCondition(
      condition, _ConditionFileFormatException(condition_from_file))
  return condition_dict
