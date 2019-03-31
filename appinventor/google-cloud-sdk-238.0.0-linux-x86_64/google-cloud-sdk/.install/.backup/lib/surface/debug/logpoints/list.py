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

"""List command for gcloud debug logpoints command group."""

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
  """List debug logpoints."""

  detailed_help = {
      'DESCRIPTION': """\
          This command displays a list of the active debug logpoints for a
          Cloud Debugger debug target.
      """
  }

  @staticmethod
  def Args(parser):
    base.URI_FLAG.RemoveFromParser(parser)
    base.PAGE_SIZE_FLAG.RemoveFromParser(parser)
    flags.AddIdOptions(parser, 'logpoint', 'logpoints', 'listed')
    parser.add_argument(
        '--all-users', action='store_true', default=True,
        help="""\
            If false, display only logpoints created by the current user.
        """)
    parser.add_argument(
        '--include-inactive', default=300,
        type=arg_parsers.BoundedInt(lower_bound=0, unlimited=True),
        help="""\
            Include logpoints which failed or expired in the last
            INCLUDE_INACTIVE seconds. If the value is "unlimited", all failed
            or expired logpoints will be included.
        """)
    parser.display_info.AddFormat("""
          table(
            userEmail.if(all_users),
            location,
            condition,
            logLevel,
            logMessageFormat,
            id,
            full_status():label=STATUS)
            :(isFinalState:sort=1, createTime:sort=2)
    """)

  def Run(self, args):
    """Run the list command."""
    project_id = properties.VALUES.core.project.Get(required=True)
    debugger = debug.Debugger(project_id)
    debuggee = debugger.FindDebuggee(args.target)
    logpoints = debuggee.ListBreakpoints(
        args.location, resource_ids=args.ids, include_all_users=args.all_users,
        include_inactive=(args.include_inactive != 0),
        restrict_to_type=debugger.LOGPOINT_TYPE)

    # Filter any results more than include_inactive seconds old.
    # include_inactive may be None, which means we do not want to filter the
    # results.
    if args.include_inactive:
      cutoff_time = (times.Now(times.UTC) -
                     datetime.timedelta(seconds=args.include_inactive))
      logpoints = [lp for lp in logpoints if _ShouldInclude(lp, cutoff_time)]

    return logpoints


def _ShouldInclude(logpoint, cutoff_time):
  """Determines if a logpoint should be included in the output.

  Args:
    logpoint: a Breakpoint object describing a logpoint.
    cutoff_time: The oldest finalTime to include for completed logpoints.
  Returns:
    True if the logpoint should be included based on the criteria in args.
  """
  if not logpoint.isFinalState or not logpoint.finalTime:
    return True
  final_time = times.ParseDateTime(logpoint.finalTime, tzinfo=times.UTC)
  return final_time >= cutoff_time
