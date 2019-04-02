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

"""Wait command for gcloud debug snapshots command group."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.debug import debug
from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.debug import flags
from googlecloudsdk.core import log
from googlecloudsdk.core import properties


class Wait(base.ListCommand):
  """Wait for debug snapshots to complete."""

  detailed_help = {
      'DESCRIPTION': """\
          This command waits for one or more snapshots on a Cloud Debugger debug
          target to become completed. A snapshot is considered completed either
          if there was an error setting the snapshot or if the snapshot was hit
          on an instance of the debug target.
      """
  }

  @staticmethod
  def Args(parser):
    base.URI_FLAG.RemoveFromParser(parser)
    base.PAGE_SIZE_FLAG.RemoveFromParser(parser)
    parser.add_argument(
        'ids', metavar='ID', nargs='*',
        help="""\
            Zero or more snapshot resource identifiers. The command will
            wait for any of the specified snapshots to complete.
        """)
    parser.add_argument(
        '--location', metavar='LOCATION-REGEXP', action='append',
        help="""\
            A regular expression to match against snapshot
            locations. The command will wait for any snapshots matching these
            criteria to complete. You may specify --location multiple times.

            EXAMPLE:

              {command} \\
                --location foo.py:[1-3] --location bar.py:3
        """)
    parser.add_argument(
        '--all', action='store_true', default=False,
        help="""\
            If set, wait for all of the specified snapshots to complete, instead of
            waiting for any one of them to complete.
        """)
    parser.add_argument(
        '--all-users', action='store_true', default=False,
        help="""\
            If set, wait for matching snapshots from all users, rather than
            only the current user.
        """)
    parser.add_argument(
        '--timeout',
        type=arg_parsers.BoundedInt(lower_bound=0, unlimited=True),
        default='unlimited',
        help="""\
            Maximum number of seconds to wait for a snapshot to complete. By
            default, wait indefinitely.
        """)
    parser.display_info.AddFormat(flags.SNAPSHOT_LIST_FORMAT)

  def Run(self, args):
    """Run the wait command."""
    project_id = properties.VALUES.core.project.Get(required=True)
    debugger = debug.Debugger(project_id)
    debuggee = debugger.FindDebuggee(args.target)
    snapshots = [
        s for s in debuggee.ListBreakpoints(
            args.location, resource_ids=args.ids,
            include_all_users=args.all_users)]

    ids = [s.id for s in snapshots]
    if not ids:
      self._is_partial = False
      return []

    if len(ids) == 1:
      log.status.Print('Waiting for 1 snapshot.')
    else:
      log.status.Print('Waiting for {0} snapshots.'.format(len(ids)))

    snapshots = debuggee.WaitForMultipleBreakpoints(ids, wait_all=args.all,
                                                    timeout=args.timeout)
    self._is_partial = args.all and len(snapshots) != len(ids)
    return snapshots

  def Epilog(self, resources_were_displayed):
    if not resources_were_displayed:
      log.status.Print('No snapshots completed before timeout')
    elif self._is_partial:
      log.status.Print('Partial results - Not all snapshots completed')
