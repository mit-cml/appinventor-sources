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
"""Util methods for Stackdriver Monitoring Surface."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from apitools.base.py import encoding
from googlecloudsdk.calliope import exceptions as calliope_exc
from googlecloudsdk.command_lib.util.args import labels_util
from googlecloudsdk.core import exceptions
from googlecloudsdk.core import properties
from googlecloudsdk.core import resources
from googlecloudsdk.core import yaml
import six


CHANNELS_FIELD_REMAPPINGS = {'channelLabels': 'labels'}


class YamlOrJsonLoadError(exceptions.Error):
  """Exception for when a JSON or YAML string could not loaded as a message."""


class NoUpdateSpecifiedError(exceptions.Error):
  """Exception for when user passes no arguments that specifies an update."""


class ConditionNotFoundError(exceptions.Error):
  """Indiciates the Condition the user specified does not exist."""


class ConflictingFieldsError(exceptions.Error):
  """Inidicates that the JSON or YAML string have conflicting fields."""


def ValidateUpdateArgsSpecified(args, update_arg_dests, resource):
  if not any([args.IsSpecified(dest) for dest in update_arg_dests]):
    raise NoUpdateSpecifiedError(
        'Did not specify any flags for updating the {}.'.format(resource))


def _RemapFields(yaml_obj, field_remappings):
  for field_name, remapped_name in six.iteritems(field_remappings):
    if field_name in yaml_obj:
      if remapped_name in yaml_obj:
        raise ConflictingFieldsError('Cannot specify both {} and {}.'.format(
            field_name, remapped_name))
      yaml_obj[remapped_name] = yaml_obj.pop(field_name)
  return yaml_obj


def MessageFromString(msg_string, message_type, display_type,
                      field_remappings=None):
  try:
    msg_as_yaml = yaml.load(msg_string)
    if field_remappings:
      msg_as_yaml = _RemapFields(msg_as_yaml, field_remappings)
    msg = encoding.PyValueToMessage(message_type, msg_as_yaml)
    return msg
  except Exception as exc:  # pylint: disable=broad-except
    raise YamlOrJsonLoadError(
        'Could not parse YAML or JSON string for [{0}]: {1}'.format(
            display_type, exc))


def _FlagToDest(flag_name):
  """Converts a --flag-arg to its dest name."""
  return flag_name[len('--'):].replace('-', '_')


def _FormatDuration(duration):
  return '{}s'.format(duration)


def GetBasePolicyMessageFromArgs(args, policy_class):
  """Returns the base policy from args."""
  if args.IsSpecified('policy') or args.IsSpecified('policy_from_file'):
    # Policy and policy_from_file are in a mutex group.
    policy_string = args.policy or args.policy_from_file
    policy = MessageFromString(policy_string, policy_class, 'AlertPolicy')
  else:
    policy = policy_class()
  return policy


def CheckConditionArgs(args):
  if args.IsSpecified('condition_filter'):
    if not args.IsSpecified('if_value'):
      raise calliope_exc.RequiredArgumentException(
          '--if',
          'If --condition-filter is set then --if must be set as well.')
    return True
  else:
    should_not_be_set = [
        '--aggregation',
        '--duration',
        '--trigger-count',
        '--trigger-percent',
        '--condition-display-name',
        '--if'
    ]
    for flag in should_not_be_set:
      if flag == '--if':
        dest = 'if_value'
      else:
        dest = _FlagToDest(flag)
      if args.IsSpecified(dest):
        raise calliope_exc.InvalidArgumentException(
            flag,
            'Should only be specified if --condition-filter is also specified.')
    return False


def BuildCondition(messages, condition=None, display_name=None,
                   aggregations=None, trigger_count=None,
                   trigger_percent=None, duration=None, condition_filter=None,
                   if_value=None):
  """Populates the fields of a Condition message from args.

  Args:
    messages: module, module containing message classes for the stackdriver api
    condition: Condition or None, a base condition to populate the fields of.
    display_name: str, the display name for the condition.
    aggregations: list[Aggregation], list of Aggregation messages for the
      condition.
    trigger_count: int, corresponds to the count field of the condition
      trigger.
    trigger_percent: float, corresponds to the percent field of the
      condition trigger.
    duration: int, The amount of time that a time series must fail to report
      new data to be considered failing.
    condition_filter: str, A filter that identifies which time series should be
      compared with the threshold.
    if_value: tuple[str, float] or None, a tuple containing a string value
      corresponding to the comparison value enum and a float with the
      condition threshold value. None indicates that this should be an
      Absence condition.

  Returns:
    Condition, a condition with it's fields populated from the args
  """
  if not condition:
    condition = messages.Condition()

  if display_name is not None:
    condition.displayName = display_name

  trigger = None
  if trigger_count or trigger_percent:
    trigger = messages.Trigger(
        count=trigger_count, percent=trigger_percent)

  kwargs = {
      'aggregations': aggregations if aggregations else None,
      'trigger': trigger,
      'duration': duration,
      'filter': condition_filter,
  }

  if if_value is not None:
    comparator, threshold_value = if_value  # pylint: disable=unpacking-non-sequence
    if not comparator:
      condition.conditionAbsent = messages.MetricAbsence(**kwargs)
    else:
      comparison_enum = messages.MetricThreshold.ComparisonValueValuesEnum
      condition.conditionThreshold = messages.MetricThreshold(
          comparison=getattr(comparison_enum, comparator),
          thresholdValue=threshold_value,
          **kwargs)

  return condition


def ParseNotificationChannel(channel_name, project=None):
  project = project or properties.VALUES.core.project.Get(required=True)
  return resources.REGISTRY.Parse(
      channel_name, params={'projectsId': project},
      collection='monitoring.projects.notificationChannels')


def ModifyAlertPolicy(base_policy, messages, display_name=None,
                      documentation_content=None, documentation_format=None,
                      enabled=None, channels=None, field_masks=None):
  """Override and/or add fields from other flags to an Alery Policy."""
  if field_masks is None:
    field_masks = []

  if display_name is not None:
    field_masks.append('display_name')
    base_policy.displayName = display_name

  if ((documentation_content is not None or documentation_format is not None)
      and not base_policy.documentation):
    base_policy.documentation = messages.Documentation()
  if documentation_content is not None:
    field_masks.append('documentation.content')
    base_policy.documentation.content = documentation_content
  if documentation_format is not None:
    field_masks.append('documentation.mime_type')
    base_policy.documentation.mimeType = documentation_format

  if enabled is not None:
    field_masks.append('enabled')
    base_policy.enabled = enabled

  # None indicates no update and empty list indicates we want to explicitly set
  # an empty list.
  if channels is not None:
    field_masks.append('notification_channels')
    base_policy.notificationChannels = channels


def ValidateAtleastOneSpecified(args, flags):
  if not any([args.IsSpecified(_FlagToDest(flag))
              for flag in flags]):
    raise calliope_exc.MinimumArgumentException(flags)


def CreateAlertPolicyFromArgs(args, messages):
  """Builds an AleryPolicy message from args."""
  policy_base_flags = ['--display-name', '--policy', '--policy-from-file']
  ValidateAtleastOneSpecified(args, policy_base_flags)

  # Get a base policy object from the flags
  policy = GetBasePolicyMessageFromArgs(args, messages.AlertPolicy)

  enabled = args.enabled if args.IsSpecified('enabled') else None
  channel_refs = args.CONCEPTS.notification_channels.Parse() or []
  channels = [channel.RelativeName() for channel in channel_refs] or None
  documentation_content = args.documentation or args.documentation_from_file
  documentation_format = (
      args.documentation_format if documentation_content else None)
  ModifyAlertPolicy(
      policy,
      messages,
      display_name=args.display_name,
      documentation_content=documentation_content,
      documentation_format=documentation_format,
      enabled=enabled,
      channels=channels)

  if CheckConditionArgs(args):
    aggregations = None
    if args.aggregation:
      aggregations = [MessageFromString(
          args.aggregation, messages.Aggregation, 'Aggregation')]

    condition = BuildCondition(
        messages,
        display_name=args.condition_display_name,
        aggregations=aggregations,
        trigger_count=args.trigger_count,
        trigger_percent=args.trigger_percent,
        duration=_FormatDuration(args.duration),
        condition_filter=args.condition_filter,
        if_value=args.if_value)
    policy.conditions.append(condition)

  return policy


def GetConditionFromArgs(args, messages):
  """Builds a Condition message from args."""
  condition_base_flags = ['--condition-filter', '--condition',
                          '--condition-from-file']
  ValidateAtleastOneSpecified(args, condition_base_flags)

  condition = None
  condition_string = args.condition or args.condition_from_file
  if condition_string:
    condition = MessageFromString(
        condition_string, messages.Condition, 'Condition')

  aggregations = None
  if args.aggregation:
    aggregations = [MessageFromString(
        args.aggregation, messages.Aggregation, 'Aggregation')]

  return BuildCondition(
      messages,
      condition=condition,
      display_name=args.condition_display_name,
      aggregations=aggregations,
      trigger_count=args.trigger_count,
      trigger_percent=args.trigger_percent,
      duration=_FormatDuration(args.duration),
      condition_filter=args.condition_filter,
      if_value=args.if_value)


def GetConditionFromPolicy(condition_name, policy):
  for condition in policy.conditions:
    if condition.name == condition_name:
      return condition

  raise ConditionNotFoundError(
      'No condition with name [{}] found in policy.'.format(condition_name))


def RemoveConditionFromPolicy(condition_name, policy):
  for i, condition in enumerate(policy.conditions):
    if condition.name == condition_name:
      policy.conditions.pop(i)
      return policy

  raise ConditionNotFoundError(
      'No condition with name [{}] found in policy.'.format(condition_name))


def ModifyNotificationChannel(base_channel, channel_type=None, enabled=None,
                              display_name=None, description=None,
                              field_masks=None):
  """Modifies base_channel's properties using the passed arguments."""
  if field_masks is None:
    field_masks = []

  if channel_type is not None:
    field_masks.append('type')
    base_channel.type = channel_type
  if display_name is not None:
    field_masks.append('display_name')
    base_channel.displayName = display_name
  if description is not None:
    field_masks.append('description')
    base_channel.description = description
  if enabled is not None:
    field_masks.append('enabled')
    base_channel.enabled = enabled
  return base_channel


def GetNotificationChannelFromArgs(args, messages):
  """Builds a NotificationChannel message from args."""
  channels_base_flags = ['--display-name', '--channel-content',
                         '--channel-content-from-file']
  ValidateAtleastOneSpecified(args, channels_base_flags)

  channel_string = args.channel_content or args.channel_content_from_file
  if channel_string:
    channel = MessageFromString(channel_string, messages.NotificationChannel,
                                'NotificationChannel',
                                field_remappings=CHANNELS_FIELD_REMAPPINGS)
    # Without this, labels will be in a random order every time.
    if channel.labels:
      channel.labels.additionalProperties = sorted(
          channel.labels.additionalProperties, key=lambda prop: prop.key)
  else:
    channel = messages.NotificationChannel()

  enabled = args.enabled if args.IsSpecified('enabled') else None
  return ModifyNotificationChannel(channel,
                                   channel_type=args.type,
                                   display_name=args.display_name,
                                   description=args.description,
                                   enabled=enabled)


def ParseCreateLabels(labels, labels_cls):
  return encoding.DictToAdditionalPropertyMessage(
      labels, labels_cls, sort_items=True)


def ProcessUpdateLabels(args, labels_name, labels_cls, orig_labels):
  """Returns the result of applying the diff constructed from args.

  This API doesn't conform to the standard patch semantics, and instead does
  a replace operation on update. Therefore, if there are no updates to do,
  then the original labels must be returned as writing None into the labels
  field would replace it.

  Args:
    args: argparse.Namespace, the parsed arguments with update_labels,
      remove_labels, and clear_labels
    labels_name: str, the name for the labels flag.
    labels_cls: type, the LabelsValue class for the new labels.
    orig_labels: message, the original LabelsValue value to be updated.

  Returns:
    LabelsValue: The updated labels of type labels_cls.

  Raises:
    ValueError: if the update does not change the labels.
  """
  labels_diff = labels_util.Diff(
      additions=getattr(args, 'update_' + labels_name),
      subtractions=getattr(args, 'remove_' + labels_name),
      clear=getattr(args, 'clear_' + labels_name))
  if not labels_diff.MayHaveUpdates():
    return None

  return labels_diff.Apply(labels_cls, orig_labels).GetOrNone()
