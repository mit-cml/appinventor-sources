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
"""Shared flags for Cloud IoT Edge commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import re

from googlecloudsdk.api_lib.edge import util
from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.core import yaml


class TopicTypeError(arg_parsers.ArgumentTypeError):

  def __init__(self):
    super(TopicTypeError, self).__init__(
        'Invalid topic format. A topic can be specified in [NAME:]TOPIC'
        ' format, which means that an abstract name can optionally be given to'
        ' a topic. The NAME part should only contain alphanumeric characters,'
        ' underscores, and hyphens. TOPIC should be a valid non-internal MQTT'
        ' topic name, and also should not contain colons, commas, and MQTT'
        ' wildcards.')


class VolumeBindingTypeError(arg_parsers.ArgumentTypeError):

  def __init__(self, msg=None):
    super(VolumeBindingTypeError, self).__init__(
        msg or 'Volume binding argument should be in format'
        ' [SOURCE:]DESTINATION[:(ro|rw)], SOURCE and DESTINATION should be'
        ' absolute paths, starting with "/", and the paths should not contain'
        ' colons.')


class DeviceBindingTypeError(arg_parsers.ArgumentTypeError):

  def __init__(self, msg=None):
    super(DeviceBindingTypeError, self).__init__(
        msg or 'Device binding argument should be in format'
        ' SOURCE[:DESTINATION][:CGROUP_PERMS], SOURCE and DESTINATION should be'
        ' absolute paths, starting with "/", and the paths should not contain'
        ' colons. CGROUP_PERMS should be a combination of the following flags'
        ' in order: "r/w/m".')


def TopicType(value):
  """Converts [NAME:]TOPIC format string to TopicInfo object.

  Args:
    value: a topic string, provided in [NAME:]TOPIC format.

  Returns:
    TopicInfo message (name=NAME, topic=TOPIC)

  Raises:
    TopicTypeError: when topic format is invalid
  """
  topic_name_re = re.compile(r'^[a-zA-Z0-9-_]*$')
  # topic should not start with '$' and should not contain any of '+#,:'
  topic_re = re.compile(r'^[^$+#,:][^+#,:]*$')

  topic_parts = value.split(':')
  if len(topic_parts) > 2:
    raise TopicTypeError()

  if len(topic_parts) == 2:
    name, topic = topic_parts

  if len(topic_parts) == 1:
    name = ''
    topic = topic_parts[0]

  if not topic_name_re.match(name) or not topic_re.match(topic):
    raise TopicTypeError()

  messages = util.GetMessagesModule()
  return messages.TopicInfo(id=name, topic=topic)


def VolumeBindingType(value):
  """Verifies volume binding flag format, and returns VolumeBinding messages.

  Args:
    value: a volume binding string parsed by ArgList from CLI flag.

  Returns:
   VolumeBinding message

  Raises:
    VolumeBindingTypeError: when the format is invalid.
  """
  binding_parts = value.split(':')

  if len(binding_parts) > 3:
    raise VolumeBindingTypeError()

  if len(binding_parts) == 3:
    source, destination, read_only = binding_parts

  if len(binding_parts) == 2:
    if binding_parts[1] in ['ro', 'rw']:
      source = destination = binding_parts[0]
      read_only = binding_parts[1]
    else:
      source, destination = binding_parts
      read_only = 'rw'

  if len(binding_parts) == 1:
    source = destination = binding_parts[0]
    read_only = 'rw'

  if not destination.startswith('/'):
    raise VolumeBindingTypeError(
        'DESTINATION {0} is not a valid absolute path.'.format(destination))

  if source and not source.startswith('/'):
    raise VolumeBindingTypeError(
        'SOURCE {0} is not a valid absolute path.'.format(source))

  if read_only not in ['ro', 'rw']:
    raise VolumeBindingTypeError(
        'The last value should be "ro" for read-only volume, and'
        ' "rw" for writable volume.')

  messages = util.GetMessagesModule()
  return messages.VolumeBinding(
      source=source or destination,
      destination=destination,
      # read_only is one of 'ro' or 'rw'
      readOnly=(read_only == 'ro'))


def DeviceBindingType(value):
  """Verifies device binding flag format, and returns device binding list.

  Args:
    value: a device binding string parsed by ArgList from CLI flag.

  Returns:
    DeviceBinding message

  Raises:
    DeviceBindingTypeError: when the format is invalid.
  """
  cgroup_perms_re = re.compile(r'^r?w?m?$')

  binding_parts = value.split(':')

  if len(binding_parts) > 3:
    raise DeviceBindingTypeError()

  if len(binding_parts) == 3:
    source, destination, cgroup_permissions = binding_parts

  if len(binding_parts) == 2:
    if cgroup_perms_re.match(binding_parts[1]):
      source = destination = binding_parts[0]
      cgroup_permissions = binding_parts[1]
    else:
      source, destination = binding_parts
      cgroup_permissions = 'rwm'

  if len(binding_parts) == 1:
    source = destination = binding_parts[0]
    cgroup_permissions = 'rwm'

  if not source.startswith('/'):
    raise DeviceBindingTypeError(
        'SOURCE {0} is not a valid absolute path.'.format(source))
  if destination and not destination.startswith('/'):
    raise DeviceBindingTypeError(
        'DESTINATION {0} is not a valid absolute path.'.format(destination))

  if not cgroup_perms_re.match(cgroup_permissions):
    raise DeviceBindingTypeError(
        'CGROUP_PERMS should be a combination of the following flags'
        ' in order: "r/w/m."')

  messages = util.GetMessagesModule()
  return messages.DeviceBinding(
      source=source,
      destination=destination or source,
      cgroupPermissions=cgroup_permissions)


def EnvVarKeyType(key):
  """Validates environment variable keys.

  Args:
    key: The environment variable key.

  Returns:
    The environment variable key.

  Raises:
    ArgumentTypeError: If the key is not a valid environment variable key.
  """

  env_var_key_validator = arg_parsers.RegexpValidator(
      r'^[a-zA-Z_][a-zA-Z0-9_]*$',
      'Environment variable keys should only consist of alphanumeric '
      'characters and underscores. The first character cannot be a digit.')
  env_var_key_validator(key)
  if key.startswith('X_GOOGLE_'):
    raise arg_parsers.ArgumentTypeError(
        'Environment variable keys that start with `X_GOOGLE_` are reserved '
        'for use by deployment tools and cannot be specified manually.')
  return key


def EnvVarsFile(file_path):
  """Interprets a YAML file as a dict.

  Args:
    file_path: path of the file to read.

  Returns:
    list of {'key': key, 'value': value} dict.
  """
  map_file_dict = yaml.load_path(file_path)
  env_vars = []
  for key, value in map_file_dict.items():
    key = EnvVarKeyType(key)
    env_vars.append({'key': key, 'value': value})
  return env_vars


def BinarySize(value):
  """BinarySize wrapper for memory flag."""
  return arg_parsers.BinarySize(
      suggested_binary_size_scales=['MB', 'MiB', 'GB', 'GiB'],
      default_unit='MB', lower_bound='1MB')(value)
