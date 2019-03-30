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

"""Flags for the compute resource-policies commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.compute import flags as compute_flags
from googlecloudsdk.command_lib.util.apis import arg_utils
from googlecloudsdk.command_lib.util.args import labels_util


def MakeResourcePolicyArg():
  return compute_flags.ResourceArgument(
      resource_name='resource policy',
      regional_collection='compute.resourcePolicies',
      region_explanation=compute_flags.REGION_PROPERTY_EXPLANATION)


def AddCycleFrequencyArgs(parser, flag_suffix, start_time_help,
                          cadence_help, supports_hourly=False,
                          has_restricted_start_times=False,
                          supports_weekly=False):
  """Add Cycle Frequency args for Resource Policies."""
  freq_group = parser.add_argument_group(
      'Cycle Frequency Group.', required=True, mutex=True)
  if has_restricted_start_times:
    start_time_help += """\
        Valid choices are 00:00, 04:00, 08:00,12:00,
        16:00 and 20:00 UTC. For example, `--start-time="03:00-05"`
        (which gets converted to 08:00 UTC)."""
  freq_flags_group = freq_group.add_group(
      'From flags:' if supports_weekly else '')
  freq_flags_group.add_argument(
      '--start-time', required=True,
      type=arg_parsers.Datetime.Parse,
      help=start_time_help)
  cadence_group = freq_flags_group.add_group(mutex=True, required=True)
  cadence_group.add_argument(
      '--daily-{}'.format(flag_suffix),
      dest='daily_cycle',
      action='store_true',
      help='{} starts daily at START_TIME.'.format(cadence_help))

  if supports_hourly:
    cadence_group.add_argument(
        '--hourly-{}'.format(flag_suffix),
        metavar='HOURS',
        dest='hourly_cycle',
        type=arg_parsers.BoundedInt(lower_bound=1),
        help='{} occurs every n hours starting at START_TIME.'.format(
            cadence_help))

  if supports_weekly:
    base.ChoiceArgument(
        '--weekly-{}'.format(flag_suffix),
        dest='weekly_cycle',
        choices=['monday', 'tuesday', 'wednesday', 'thursday', 'friday',
                 'saturday', 'sunday'],
        help_str='{} occurs weekly on WEEKLY_{} at START_TIME.'.format(
            cadence_help, flag_suffix.upper())).AddToParser(cadence_group)
    freq_file_group = freq_group.add_group('From file:')
    freq_file_group.add_argument(
        '--weekly-{}-from-file'.format(flag_suffix),
        dest='weekly_cycle_from_file',
        type=arg_parsers.BufferedFileInput(),
        help="""\
        A JSON/YAML file which specifies a weekly schedule. It should be a
        list of objects with the following fields:

        day: Day of the week with the same choices as `--weekly-{}`.
        startTime: Start time of the snapshot schedule with the same format
            as --start-time.
        """.format(flag_suffix))


def AddCommonArgs(parser):
  parser.add_argument(
      '--description',
      help='An optional, textual description for the backend.')


def GetOnSourceDiskDeleteFlagMapper(messages):
  return arg_utils.ChoiceEnumMapper(
      '--on-source-disk-delete',
      messages.ResourcePolicySnapshotSchedulePolicyRetentionPolicy
      .OnSourceDiskDeleteValueValuesEnum,
      custom_mappings={
          'KEEP_AUTO_SNAPSHOTS': (
              'keep-auto-snapshots',
              'Keep automatically-created snapshots when the source disk is '
              'deleted. This is the default behavior.'),
          'APPLY_RETENTION_POLICY': (
              'apply-retention-policy',
              'Continue to apply the retention window to automatically-created '
              'snapshots when the source disk is deleted.')
      },
      default=None,
      help_str='Retention behavior of automatic snapshots in the event of '
      'source disk deletion.')


def AddSnapshotScheduleArgs(parser, messages):
  """Adds flags specific to snapshot schedule resource policies."""
  parser.add_argument(
      '--max-retention-days',
      required=True,
      type=arg_parsers.BoundedInt(lower_bound=1),
      help='Maximum number of days snapshot can be retained.')
  GetOnSourceDiskDeleteFlagMapper(messages).choice_arg.AddToParser(parser)
  snapshot_properties_group = parser.add_group('Snapshot properties')
  labels_util.GetCreateLabelsFlag(
      extra_message='These will be added to the disk snapshots on creation.',
      labels_name='snapshot-labels').AddToParser(snapshot_properties_group)
  snapshot_properties_group.add_argument(
      '--guest-flush',
      action='store_true',
      help='Create an application consistent snapshot by informing the OS to '
           'prepare for the snapshot process.')
  compute_flags.AddStorageLocationFlag(snapshot_properties_group, 'snapshot')


def AddResourcePoliciesArgs(parser, action, resource, required=False):
  parser.add_argument(
      '--resource-policies',
      metavar='RESOURCE_POLICY',
      type=arg_parsers.ArgList(),
      required=required,
      help=('A list of resource policy names to be {action} the {resource}. '
            'The policies must exist in the same region as the {resource}.'
            .format(action=action, resource=resource)))
