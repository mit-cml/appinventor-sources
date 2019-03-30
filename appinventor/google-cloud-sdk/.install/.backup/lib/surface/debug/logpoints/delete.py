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

"""Delete command for gcloud debug logpoints command group."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import io

from googlecloudsdk.api_lib.debug import debug
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.debug import flags
from googlecloudsdk.core import log
from googlecloudsdk.core import properties
from googlecloudsdk.core.console import console_io
from googlecloudsdk.core.resource import resource_printer


class Delete(base.DeleteCommand):
  """Delete debug logpoints.

  This command deletes logpoints from a Cloud Debugger debug target. It will
  ask for confirmation before deleting any logpoints. To suppress confirmation,
  use the global --quiet option.
  """

  @staticmethod
  def Args(parser):
    flags.AddIdOptions(parser, 'logpoint', 'logpoints', 'deleted')
    parser.add_argument(
        '--all-users', action='store_true', default=False,
        help="""\
            If set, any location regexp will match logpoints from all users,
            rather than only logpoints created by the current user. This flag is
            not required when specifying the exact ID of a logpoint created by
            another user.
        """)
    parser.add_argument(
        '--include-inactive', action='store_true', default=False,
        help="""\
            If set, any location regexp will also match inactive logpoints,
            rather than only logpoints which have not expired. This flag is
            not required when specifying the exact ID of an inactive logpoint.
        """)

  def Run(self, args):
    """Run the delete command."""
    project_id = properties.VALUES.core.project.Get(required=True)
    debugger = debug.Debugger(project_id)
    debuggee = debugger.FindDebuggee(args.target)
    logpoints = debuggee.ListBreakpoints(
        args.location, resource_ids=args.ids,
        include_all_users=args.all_users,
        include_inactive=args.include_inactive,
        restrict_to_type=debugger.LOGPOINT_TYPE)
    if logpoints:
      logpoint_list = io.StringIO()
      resource_printer.Print(
          logpoints,
          'table(location, condition, logLevel, logMessageFormat, id)',
          logpoint_list)
      console_io.PromptContinue(
          message=(
              'This command will delete the following logpoints:'
              '\n\n{0}\n'.format(logpoint_list.getvalue())),
          cancel_on_no=True)
    for s in logpoints:
      debuggee.DeleteBreakpoint(s.id)
    # Guaranteed we have at least one logpoint, since ListMatchingBreakpoints
    # would raise an exception otherwise.
    if len(logpoints) == 1:
      log.status.write('Deleted 1 logpoint.\n')
    else:
      log.status.write('Deleted {0} logpoints.\n'.format(len(logpoints)))
    return logpoints
