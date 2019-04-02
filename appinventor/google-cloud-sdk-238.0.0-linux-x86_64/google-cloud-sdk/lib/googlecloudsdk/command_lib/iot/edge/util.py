# -*- coding: utf-8 -*- #
# Copyright 2019 Google Inc. All Rights Reserved.
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
"""Utility for IoT Edge flags."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.util import apis
from googlecloudsdk.command_lib.iot.edge import flags


def MemoryBytesToMb(value):
  """Converts bytes value to truncated MB value."""
  if value == 0:
    return 0
  memory_mb = value // 1024 // 1024
  if memory_mb == 0:
    memory_mb = 1
  return memory_mb


def ContainerAddDefaultTopicHook(ref, args, req):
  """Adds default input/output topic to topic list.

  Default Topic is always the first topic of input/out topics, and takes form of
  /{component}/{component_name}/(input|output).

  Args:
    ref: A resource ref to the parsed Edge Container resource
    args: The parsed args namespace from CLI
    req: Created request for the API call.

  Returns:
    req, modified with default topics as first element in each topics.
  """
  del args
  messages = apis.GetMessagesModule('edge', 'v1alpha1')
  prefix = '/container/{}/'.format(ref.containersId)
  req.container.inputTopics.insert(0,
                                   messages.TopicInfo(topic=prefix + 'input'))
  req.container.outputTopics.insert(0,
                                    messages.TopicInfo(topic=prefix + 'output'))
  return req


def _RemoveEnvVars(env_vars, removed_keys):
  """Removes keys in removed_keys from env_vars dict."""
  for key in removed_keys:
    if key in env_vars:
      del env_vars[key]


def _UpdateEnvVars(env_vars, updated_env_vars):
  """Updates env_vars dict with updated_env_vars dict."""
  for key, value in updated_env_vars.items():
    key = flags.EnvVarKeyType(key)
    env_vars[key] = value


def _ListToDict(env_vars_list):
  """Converts [{'key': key, 'value': value}, ...] list to dict."""
  return {item.key: item.value for item in env_vars_list}


def _DictToList(env_vars_dict):
  """Converts dict to [{'key': key, 'value': value}, ...] list."""
  return [{'key': key, 'value': value} for key, value in env_vars_dict.items()]


def ContainerUpdateEnvVarsHook(ref, args, req):
  """Applies remove-env-vars and update-env-vars flags.

  Args:
    ref: A resource ref to the parsed Edge Container resource
    args: The parsed args namespace from CLI
    req: Created request for the API call.

  Returns:
    Modified request for the API call.
  """
  del ref
  if not any(
      map(args.IsSpecified, [
          'remove_env_vars',
          'update_env_vars',
          'clear_env_vars',
      ])):
    return req

  if args.IsSpecified('clear_env_vars'):
    req.container.environmentVariables.additionalProperties = []
    return req

  env_vars = _ListToDict(
      req.container.environmentVariables.additionalProperties)

  if args.IsSpecified('remove_env_vars'):
    _RemoveEnvVars(env_vars, args.remove_env_vars)
  if args.IsSpecified('update_env_vars'):
    _UpdateEnvVars(env_vars, args.update_env_vars)

  req.container.environmentVariables.additionalProperties = _DictToList(
      env_vars)
  return req


def ContainerUpdateMaskHook(ref, args, req):
  """Constructs updateMask for container update request.

  Args:
    ref: A resource ref to the parsed Edge Container resource
    args: The parsed args namespace from CLI
    req: Created request for the API call.

  Returns:
    Modified request for the API call.
  """
  del ref
  arg_name_to_field = {
      '--docker-image': 'dockerImageUri',
      '--autostart': 'autostart',
      '--no-autostart': 'autostart',
      '--description': 'description',
      '--memory': 'availableMemoryMb',
      '--input-topic': 'inputTopics',
      '--output-topic': 'outputTopics',
      '--volume-binding': 'volumeBindings',
      '--device-binding': 'deviceBindings'
  }

  update_mask = []
  for arg_name in args.GetSpecifiedArgNames():
    if arg_name in arg_name_to_field:
      update_mask.append(arg_name_to_field[arg_name])
    elif 'env-var' in arg_name and 'environmentVariables' not in update_mask:
      update_mask.append('environmentVariables')

  req.updateMask = ','.join(update_mask)
  return req


def ContainerNameAnnotateHook(ref, args, req):
  """Modifies name of req from containersId to full resource path.

  Example:
    (for req.container.name)
    foo -> projects/p/locations/r/registries/r/devices/d/containers/foo

  Args:
    ref: A resource ref to the parsed Edge Container resource
    args: The parsed args namespace from CLI
    req: Created request for the API call.

  Returns:
    Modified request for the API call.
  """
  del ref, args
  req.container.name = req.parent + '/containers/' + req.container.name
  return req
