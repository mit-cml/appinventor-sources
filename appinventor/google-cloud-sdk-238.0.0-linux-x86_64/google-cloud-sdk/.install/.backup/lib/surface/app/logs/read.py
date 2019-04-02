# -*- coding: utf-8 -*- #
# Copyright 2015 Google Inc. All Rights Reserved.
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
"""app logs read command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.app import logs_util
from googlecloudsdk.api_lib.logging import common
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.app import flags
from googlecloudsdk.core import log
from googlecloudsdk.core import properties


class Read(base.Command):
  """Reads log entries for the current App Engine app."""

  @staticmethod
  def Args(parser):
    """Register flags for this command."""
    flags.SERVICE.AddToParser(parser)
    flags.VERSION.AddToParser(parser)
    flags.LEVEL.AddToParser(parser)
    flags.LOGS.AddToParser(parser)
    parser.add_argument('--limit', required=False, type=int,
                        default=200, help='Number of log entries to show.')

  def Run(self, args):
    """This is what gets called when the user runs this command.

    Args:
      args: an argparse namespace. All the arguments that were provided to this
        command invocation.

    Returns:
      The list of log entries.
    """
    printer = logs_util.LogPrinter()
    printer.RegisterFormatter(logs_util.FormatRequestLogEntry)
    printer.RegisterFormatter(logs_util.FormatNginxLogEntry)
    printer.RegisterFormatter(logs_util.FormatAppEntry)
    project = properties.VALUES.core.project.Get(required=True)
    filters = logs_util.GetFilters(project, args.logs, args.service,
                                   args.version, args.level)

    lines = []
    # pylint: disable=g-builtin-op, For the .keys() method
    for entry in common.FetchLogs(log_filter=' AND '.join(filters),
                                  order_by='DESC',
                                  limit=args.limit):
      lines.append(printer.Format(entry))
    for line in reversed(lines):
      log.out.Print(line)

Read.detailed_help = {
    'DESCRIPTION': """\
        Display the latest log entries from stdout, stderr and crash log for the
        current Google App Engine app in a human readable format.
    """,
    'EXAMPLES': """\
        To display the latest entries for the current app, run:

          $ {command}

        To show only the entries with severity at `warning` or higher, run:

          $ {command} --level=warning

        To show only the entries with a specific version, run:

          $ {command} --version=v1

        To show only the 10 latest log entries for the default service, run:

          $ {command} --limit 10 --service=default

        To show only the logs from the request log for standard apps, run:

          $ {command} --logs=request_log

        To show only the logs from the request log for Flex apps, run:

          $ {command} --logs=nginx.request
    """,
}
