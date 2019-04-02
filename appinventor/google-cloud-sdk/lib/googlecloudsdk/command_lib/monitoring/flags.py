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
"""Shared resource flags for Cloud Monitoring commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.calliope import exceptions
from googlecloudsdk.command_lib.util.args import labels_util
from googlecloudsdk.command_lib.util.args import repeated


COMPARISON_TO_ENUM = {
    '>': 'COMPARISON_GT',
    '<': 'COMPARISON_LT',
    '>=': 'COMPARISON_GE',
    '<=': 'COMPARISON_LE',
    '==': 'COMPARISON_EQ',
    '=': 'COMPARISON_EQ',
    '!=': 'COMPARISON_NE',
}


def AddMessageFlags(parser, resource, flag=None):
  """Adds flags for specifying a message as a string/file to the parser."""
  message_group = parser.add_group(mutex=True)
  message_group.add_argument(
      '--{}'.format(flag or resource),
      help='The {} as a string. In either JSON or YAML format.'.format(
          resource))
  message_group.add_argument(
      '--{}-from-file'.format(flag or resource),
      type=arg_parsers.BufferedFileInput(),
      help='The path to a JSON or YAML file containing the {}.'.format(
          resource))


def AddDisplayNameFlag(parser, resource):
  parser.add_argument(
      '--display-name', help='The display name for the {}.'.format(resource))


def AddPolicySettingsFlags(parser, update=False):
  """Adds policy settings flags to the parser."""
  policy_settings_group = parser.add_group(help="""\
      Policy Settings.
      If any of these are specified, they will overwrite fields in the
      `--policy` or `--policy-from-file` flags if specified.""")
  AddDisplayNameFlag(policy_settings_group, resource='Alert Policy')

  enabled_kwargs = {
      'action': arg_parsers.StoreTrueFalseAction if update else 'store_true'
  }
  if not update:
    # Can't specify default if using StoreTrueFalseAction.
    enabled_kwargs['default'] = True
  policy_settings_group.add_argument(
      '--enabled', help='If the policy is enabled.', **enabled_kwargs)

  documentation_group = policy_settings_group.add_group(help='Documentation')
  documentation_group.add_argument(
      '--documentation-format',
      default='text/markdown' if not update else None,
      help='The MIME type that should be used with `--documentation` or '
           '`--documentation-from-file`. Currently, only "text/markdown" is '
           'supported.')
  documentation_string_group = documentation_group.add_group(mutex=True)
  documentation_string_group.add_argument(
      '--documentation',
      help='The documentation to be included with the policy.')
  documentation_string_group.add_argument(
      '--documentation-from-file',
      type=arg_parsers.BufferedFileInput(),
      help='The path to a file containing the documentation to be included '
           'with the policy.')
  if update:
    repeated.AddPrimitiveArgs(
        policy_settings_group,
        'Alert Policy',
        'notification-channels',
        'Notification Channels')
    AddUpdateLabelsFlags(
        'user-labels', policy_settings_group, group_text='User Labels')
  else:
    AddCreateLabelsFlag(policy_settings_group, 'user-labels', 'policy')


def AddFieldsFlagsWithMutuallyExclusiveSettings(parser,
                                                fields_help,
                                                add_settings_func,
                                                fields_choices=None,
                                                **kwargs):
  update_group = parser.add_group(mutex=True)
  update_group.add_argument(
      '--fields',
      metavar='field',
      type=arg_parsers.ArgList(choices=fields_choices),
      help=fields_help)
  add_settings_func(update_group, **kwargs)


def ValidateAlertPolicyUpdateArgs(args):
  if args.fields and not (args.policy or args.policy_from_file):
    raise exceptions.OneOfArgumentsRequiredException(
        ['--policy', '--policy-from-file'],
        'If --fields is specified.')


def ComparisonValidator(if_value):
  if if_value.lower() == 'absent':
    return (None, None)

  if_value = if_value.split()
  if len(if_value) != 2:
    raise exceptions.BadArgumentException('--if', 'Invalid value for flag.')
  try:
    comparator = COMPARISON_TO_ENUM[if_value[0]]
    threshold_value = float(if_value[1])
    return comparator, threshold_value
  except KeyError:
    raise exceptions.BadArgumentException('--if',
                                          'Comparator must be < or >.')
  except ValueError:
    raise exceptions.BadArgumentException('--if',
                                          'Threshold not a value float.')


def AddConditionSettingsFlags(parser):
  """Adds policy condition flags to the parser."""
  condition_group = parser.add_group(help="""\
        Condition Settings.
        This will add a condition to the created policy. If any conditions are
        already specified, this condition will be appended.""")
  condition_group.add_argument(
      '--condition-display-name',
      help='The display name for the condition.')
  condition_group.add_argument(
      '--condition-filter',
      help='Specifies the "filter" in a metric absence or metric threshold '
           'condition.')
  condition_group.add_argument(
      '--aggregation',
      help='Specifies an Aggregation message as a JSON/YAML value to be '
           'applied to the condition. For more information about the format: '
           'https://cloud.google.com/monitoring/api/ref_v3/rest/v3/'
           'projects.alertPolicies')
  condition_group.add_argument(
      '--duration',
      type=arg_parsers.Duration(),
      help='The duration (e.g. "60s", "2min", etc.) that the condition '
           'must hold in order to trigger as true.')
  AddUpdateableConditionFlags(condition_group)


def AddUpdateableConditionFlags(parser):
  """Adds flags for condition settings that are updateable to the parser."""
  parser.add_argument(
      '--if',
      dest='if_value',  # To avoid specifying args.if.
      type=ComparisonValidator,
      help='One of "absent", "< THRESHOLD", "> THRESHOLD" where "THRESHOLD" is '
           'an integer or float.')
  trigger_group = parser.add_group(mutex=True)
  trigger_group.add_argument(
      '--trigger-count',
      type=int,
      help='The absolute number of time series that must fail the predicate '
           'for the condition to be triggered.')
  trigger_group.add_argument(
      '--trigger-percent',
      type=float,
      help='The percentage of time series that must fail the predicate for '
           'the condition to be triggered.')


def ValidateNotificationChannelUpdateArgs(args):
  if (args.fields and
      not (args.channel_content or args.channel_content_from_file)):
    raise exceptions.OneOfArgumentsRequiredException(
        ['--channel-content', '--channel-content-from-file'],
        'If --fields is specified.')


def AddNotificationChannelSettingFlags(parser, update=False):
  """Adds flags for channel settings to the parser."""
  channel_group = parser.add_group(help='Notification channel settings')
  AddDisplayNameFlag(channel_group, 'channel')
  channel_group.add_argument(
      '--description',
      help='An optional description for the channel.')
  channel_group.add_argument(
      '--type',
      help='The type of the notification channel. This field matches the '
           'value of the NotificationChannelDescriptor type field.')

  enabled_kwargs = {
      'action': arg_parsers.StoreTrueFalseAction if update else 'store_true'
  }
  if not update:
    # Can't specify default if using StoreTrueFalseAction.
    enabled_kwargs['default'] = True
  channel_group.add_argument(
      '--enabled',
      help='Whether notifications are forwarded to the described channel.',
      **enabled_kwargs)
  if update:
    AddUpdateLabelsFlags(
        'user-labels', channel_group, group_text='User Labels')
    AddUpdateLabelsFlags(
        'channel-labels', channel_group, validate_values=False,
        group_text='Configuration Fields: Key-Value pairs that define the '
                   'channel and its behavior.')
  else:
    AddCreateLabelsFlag(channel_group, 'user-labels', 'channel')
    AddCreateLabelsFlag(
        channel_group, 'channel-labels', 'channel', validate_values=False,
        extra_message='These are configuration fields that define the channel '
                      'and its behavior.')


def AddCreateLabelsFlag(parser, labels_name, resource_name, extra_message='',
                        validate_values=True):
  extra_message += ('If the {0} was given as a JSON/YAML object from a string '
                    'or file, this flag will replace the labels value '
                    'in the given {0}.'.format(resource_name))
  labels_util.GetCreateLabelsFlag(
      extra_message=extra_message,
      labels_name=labels_name,
      validate_values=validate_values).AddToParser(parser)


def AddUpdateLabelsFlags(labels_name, parser, group_text='',
                         validate_values=True):
  labels_group = parser.add_group(group_text)
  labels_util.GetUpdateLabelsFlag(
      '', labels_name=labels_name,
      validate_values=validate_values).AddToParser(labels_group)
  remove_group = labels_group.add_group(mutex=True)
  labels_util.GetRemoveLabelsFlag(
      '', labels_name=labels_name).AddToParser(remove_group)
  labels_util.GetClearLabelsFlag(
      labels_name=labels_name).AddToParser(remove_group)
