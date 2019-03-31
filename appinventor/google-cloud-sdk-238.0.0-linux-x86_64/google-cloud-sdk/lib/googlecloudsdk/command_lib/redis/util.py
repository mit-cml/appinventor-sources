# -*- coding: utf-8 -*- #
# Copyright 2018 Google Inc. All Rights Reserved.
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
"""Flag utilities for `gcloud redis`."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from apitools.base.py import encoding
from googlecloudsdk.api_lib import redis
from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.util.args import labels_util
import six

VALID_REDIS_CONFIG_KEYS = ('maxmemory-policy', 'notify-keyspace-events')


def GetClientForResource(resource_ref):
  api_version = resource_ref.GetCollectionInfo().api_version
  client = redis.Client(api_version)
  return client


def GetMessagesForResource(resource_ref):
  api_version = resource_ref.GetCollectionInfo().api_version
  messages = redis.Messages(api_version)
  return messages


def InstanceRedisConfigArgDictSpec():
  return {k: six.text_type for k in VALID_REDIS_CONFIG_KEYS}


def InstanceRedisConfigArgType(value):
  return arg_parsers.ArgDict(spec=InstanceRedisConfigArgDictSpec())(value)


def InstanceLabelsArgType(value):
  return arg_parsers.ArgDict(
      key_type=labels_util.KEY_FORMAT_VALIDATOR,
      value_type=labels_util.VALUE_FORMAT_VALIDATOR)(value)


def AdditionalInstanceUpdateArguments():
  return InstanceUpdateLabelsFlags() + [InstanceUpdateRedisConfigFlag(),
                                        InstanceRemoveRedisConfigFlag()]


def InstanceUpdateLabelsFlags():
  remove_group = base.ArgumentGroup(mutex=True)
  remove_group.AddArgument(labels_util.GetClearLabelsFlag())
  remove_group.AddArgument(labels_util.GetRemoveLabelsFlag(''))
  return [labels_util.GetUpdateLabelsFlag(''), remove_group]


def InstanceUpdateRedisConfigFlag():
  return base.Argument(
      '--update-redis-config',
      metavar='KEY=VALUE',
      type=InstanceRedisConfigArgType,
      action=arg_parsers.UpdateAction,
      help="""\
      A list of Redis config KEY=VALUE pairs to update according to
      http://redis.io/topics/config. If a config parameter is already set,
      its value is modified; otherwise a new Redis config parameter is added.
      Currently, the only supported parameters are: {}.
      """.format(', '.join(VALID_REDIS_CONFIG_KEYS)))


def InstanceRemoveRedisConfigFlag():
  return base.Argument(
      '--remove-redis-config',
      metavar='KEY',
      type=arg_parsers.ArgList(),
      action=arg_parsers.UpdateAction,
      help="""\
      A list of Redis config parameters to remove. Removing a non-existent
      config parameter is silently ignored.""")


def PackageInstanceRedisConfig(config, messages):
  return encoding.DictToAdditionalPropertyMessage(
      config, messages.Instance.RedisConfigsValue, sort_items=True)
