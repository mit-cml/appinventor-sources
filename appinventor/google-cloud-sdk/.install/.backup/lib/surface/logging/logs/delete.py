# -*- coding: utf-8 -*- #
# Copyright 2014 Google Inc. All Rights Reserved.
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

"""'logging logs delete' command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.logging import util
from googlecloudsdk.calliope import base
from googlecloudsdk.core import log
from googlecloudsdk.core.console import console_io


class Delete(base.DeleteCommand):
  """Deletes all entries from a log."""

  @staticmethod
  def Args(parser):
    """Register flags for this command."""
    parser.add_argument('log_name', help='Log name.')

  def Run(self, args):
    """This is what gets called when the user runs this command.

    Args:
      args: an argparse namespace. All the arguments that were provided to this
        command invocation.
    """
    console_io.PromptContinue(
        'Really delete all log entries from [%s]?' % args.log_name,
        cancel_on_no=True)

    util.GetClient().projects_logs.Delete(
        util.GetMessages().LoggingProjectsLogsDeleteRequest(
            logName=util.CreateLogResourceName(
                util.GetCurrentProjectParent(), args.log_name)))
    log.DeletedResource(args.log_name)


Delete.detailed_help = {
    'DESCRIPTION': """\
        {index}
        With no entries, the log will not appear in the list of your
        project's logs. However, you can write new entries to the log.
    """,
}
