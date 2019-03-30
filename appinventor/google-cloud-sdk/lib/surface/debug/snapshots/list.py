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

"""List command for gcloud debug snapshots command group."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import datetime

from googlecloudsdk.api_lib.debug import debug
from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.debug import flags
from googlecloudsdk.core import properties
from googlecloudsdk.core.util import times


class List(base.ListCommand):
  """List debug snapshots."""

  detailed_help = {
      'DESCRIPTION': """\
          This command displays a list of the active debug snapshots for a
          Cloud Debugger debug target.
      """
  }

  @staticmethod
  def Args(parser):
    base.URI_FLAG.RemoveFromParser(parser)
    base.PAGE_SIZE_FLAG.RemoveFromParser(parser)
    flags.AddIdOptions(parser, 'snapshot', 'snapshots', 'displayed')
    parser.add_argument(
        '--all-users', action='store_true', default=False,
        help="""\
            If set, display snapshots from all users, rather than only the
            current user.
        """)
    parser.add_argument(
        '--include-inactive', default=300,
        type=arg_parsers.BoundedInt(lower_bound=0, unlimited=True),
        help="""\
            Include snapshots which have completed in the last INCLUDE_INACTIVE
            seconds. If the value is "unlimited", all inactive snapshots will
            be included.
        """)
    parser.display_info.AddFormat(flags.SNAPSHOT_LIST_FORMAT)

  def Run(self, args):
    """Run the list command."""
    project_id = properties.VALUES.core.project.Get(required=True)
    debugger = debug.Debugger(project_id)
    debuggee = debugger.FindDebuggee(args.target)
    snapshots = debuggee.ListBreakpoints(
        args.location, resource_ids=args.ids, include_all_users=args.all_users,
        include_inactive=(args.include_inactive != 0),
        restrict_to_type=debugger.SNAPSHOT_TYPE)
    # Filter any results more than include_inactive seconds old.
    # include_inactive may be None, which means we do not want to filter the
    # results.
    if args.include_inactive:
      cutoff_time = (times.Now(times.UTC) -
                     datetime.timedelta(seconds=args.include_inactive))
      snapshots = [s for s in snapshots if _ShouldInclude(s, cutoff_time)]
    return snapshots


def _ShouldInclude(snapshot, cutoff_time):
  """Determines if a snapshot should be included in the output.

  Args:
    snapshot: a Breakpoint message desciribing a snapshot.
    cutoff_time: The oldest finalTime to include for completed snapshots.
  Returns:
    True if the snapshot should be included based on the criteria in args.
  """
  if not snapshot.isFinalState or not snapshot.finalTime:
    return True
  final_time = times.ParseDateTime(snapshot.finalTime, tzinfo=times.UTC)
  return final_time >= cutoff_time
