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
"""Utilities for the instance-groups managed update-instances commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import re

from googlecloudsdk.calliope import exceptions

STANDBY_NAME = 'standby'

TARGET_SIZE_NAME = 'target-size'

TEMPLATE_NAME = 'template'


def _ParseFixed(fixed_or_percent_str):
  """Retrieves int value from string."""
  if re.match(r'^\d+$', fixed_or_percent_str):
    return int(fixed_or_percent_str)
  return None


def _ParsePercent(fixed_or_percent_str):
  """Retrieves percent value from string."""
  if re.match(r'^\d+%$', fixed_or_percent_str):
    percent = int(fixed_or_percent_str[:-1])
    return percent
  return None


def ParseFixedOrPercent(flag_name, flag_param_name,
                        fixed_or_percent_str, messages):
  """Retrieves value: number or percent.

  Args:
    flag_name: name of the flag associated with the parsed string.
    flag_param_name: name of the inner parameter of the flag.
    fixed_or_percent_str: string containing fixed or percent value.
    messages: module containing message classes.

  Returns:
    FixedOrPercent message object.
  """
  if fixed_or_percent_str is None:
    return None
  fixed = _ParseFixed(fixed_or_percent_str)
  if fixed is not None:
    return messages.FixedOrPercent(fixed=fixed)

  percent = _ParsePercent(fixed_or_percent_str)
  if percent is not None:
    if percent > 100:
      raise exceptions.InvalidArgumentException(
          flag_name, 'percentage cannot be higher than 100%.')
    return messages.FixedOrPercent(percent=percent)
  raise exceptions.InvalidArgumentException(
      flag_name,
      flag_param_name + ' has to be non-negative integer number or percent.')


def ParseUpdatePolicyType(flag_name, policy_type_str, messages):
  """Retrieves value of update policy type: opportunistic or proactive.

  Args:
    flag_name: name of the flag associated with the parsed string.
    policy_type_str: string containing update policy type.
    messages: module containing message classes.

  Returns:
    InstanceGroupManagerUpdatePolicy.TypeValueValuesEnum message enum value.
  """
  if policy_type_str == 'opportunistic':
    return (messages.InstanceGroupManagerUpdatePolicy
            .TypeValueValuesEnum.OPPORTUNISTIC)
  elif policy_type_str == 'proactive':
    return (messages.InstanceGroupManagerUpdatePolicy
            .TypeValueValuesEnum.PROACTIVE)
  raise exceptions.InvalidArgumentException(flag_name, 'unknown update policy.')


def ValidateUpdateInstancesArgs(args):
  """Validates update arguments provided by the user.

  Args:
    args: arguments provided by the user.
  """
  if args.action == 'restart':
    if args.version_original:
      raise exceptions.InvalidArgumentException(
          '--version-original', 'can\'t be specified for --action restart.')
    if args.version_new:
      raise exceptions.InvalidArgumentException(
          '--version-new', 'can\'t be specified for --action restart.')

  elif args.action == 'replace':
    if not args.version_new:
      raise exceptions.RequiredArgumentException(
          '--version-new',
          'must be specified for --action replace (or default).')

    if not args.version_original and (TARGET_SIZE_NAME in args.version_new):
      if args.version_new[TARGET_SIZE_NAME] == '100%':
        del args.version_new[TARGET_SIZE_NAME]
      else:
        raise exceptions.InvalidArgumentException(
            '--version-new',
            'target-size can\'t be specified if there is only one version.')

    if (args.version_original and args.version_new and
        (TARGET_SIZE_NAME in args.version_original)
        == (TARGET_SIZE_NAME in args.version_new)):
      raise exceptions.ToolException(
          'Exactly one version must have the target-size specified.')


def ParseVersion(project, flag_name, version_map, resources, messages):
  """Retrieves version from input map.

  Args:
    project: name of the project
    flag_name: name of the flag associated with the parsed string.
    version_map: map containing version data provided by the user.
    resources: provides reference for instance template resource.
    messages: module containing message classes.

  Returns:
    InstanceGroupManagerVersion message object.
  """
  if TEMPLATE_NAME not in version_map:
    raise exceptions.InvalidArgumentException(flag_name,
                                              'template has to be specified.')

  template_ref = resources.Parse(
      version_map[TEMPLATE_NAME],
      params={'project': project},
      collection='compute.instanceTemplates')

  if TARGET_SIZE_NAME in version_map:
    target_size = ParseFixedOrPercent(flag_name, TARGET_SIZE_NAME,
                                      version_map[TARGET_SIZE_NAME], messages)
  else:
    target_size = None

  name = version_map.get('name')

  return messages.InstanceGroupManagerVersion(
      instanceTemplate=template_ref.SelfLink(),
      targetSize=target_size,
      name=name)


def ValidateCanaryVersionFlag(flag_name, version_map):
  """Retrieves canary version from input map.

  Args:
    flag_name: name of the flag associated with the parsed string.
    version_map: map containing version data provided by the user.
  """
  if version_map and TARGET_SIZE_NAME not in version_map:
    raise exceptions.RequiredArgumentException(
        '{} {}={}'.format(flag_name, TARGET_SIZE_NAME,
                          TARGET_SIZE_NAME.upper()),
        'target size must be specified for canary version')
