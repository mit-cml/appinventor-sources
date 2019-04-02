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

"""Create command for gcloud debug snapshots command group."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.debug import debug
from googlecloudsdk.calliope import base
from googlecloudsdk.core import log
from googlecloudsdk.core import properties


class Create(base.CreateCommand):
  """Create debug snapshots."""

  detailed_help = {
      'DESCRIPTION': """\
          This command creates a debug snapshot on a Cloud Debugger debug
          target. Snapshots allow you to capture stack traces and local
          variables from your running service without interfering with normal
          operations.

          When any instance of the target executes the snapshot location, the
          optional condition expression is evaluated. If the result is true
          (or if there is no condition), the instance captures the current
          thread state and reports it back to Cloud Debugger. Once any instance
          captures a snapshot, the snapshot is marked as completed, and it
          will not be captured again.

          You can most easily view snapshot results in the developer console. It
          is also possible to inspect snapshot results with the "snapshots
          describe" command.
      """
  }

  @staticmethod
  def Args(parser):
    parser.add_argument(
        'location',
        help="""\
            The location where the snapshot should be taken. Locations are of
            the form FILE:LINE, where FILE can be simply the file name, or the
            file name preceded by enough path components to differntiate it from
            other files with the same name. If the file name is not unique in
            the debug target, the behavior is unspecified.
        """)
    parser.add_argument(
        '--condition',
        help="""\
            A condition to restrict when the snapshot is taken. When the
            snapshot location is executed, the condition will be evaluated, and
            the snapshot will be generated only if the condition is true.
        """)
    parser.add_argument(
        '--expression', action='append',
        help="""\
            An expression to evaluate when the snapshot is taken. You may
            specify --expression multiple times.
        """)
    parser.add_argument(
        '--wait', default=10,
        help="""\
            The number of seconds to wait to ensure that no error is returned
            from a debugger agent when creating the snapshot. When a snapshot
            is created, there will be a delay before the agents see and apply
            the snapshot. Until at least one agent has attempted to
            enable the snapshot, it cannot be determined if the snapshot is
            valid.
        """)
    parser.display_info.AddFormat("""
          list(
            format("id: {0}", id),
            format("location: {0}", location),
            format("status: {0}", full_status()),
            format("consoleViewUrl: {0}", consoleViewUrl)
          )
    """)

  def Run(self, args):
    """Run the create command."""
    project_id = properties.VALUES.core.project.Get(required=True)
    user_email = properties.VALUES.core.account.Get(required=True)
    debugger = debug.Debugger(project_id)
    debuggee = debugger.FindDebuggee(args.target)
    snapshot = debuggee.CreateSnapshot(
        location=args.location, expressions=args.expression,
        condition=args.condition, user_email=user_email)
    final_snapshot = debuggee.WaitForBreakpointSet(snapshot.id, args.wait,
                                                   args.location)
    if args.location != final_snapshot.location:
      log.status.write(
          'The debugger adjusted the snapshot location to {0}'.format(
              final_snapshot.location))
    return final_snapshot or snapshot
