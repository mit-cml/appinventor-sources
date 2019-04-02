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

"""'logging read' command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import datetime
from googlecloudsdk.api_lib.logging import common
from googlecloudsdk.api_lib.logging import util
from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.calliope import base


class Read(base.Command):
  """Read log entries."""

  @staticmethod
  def Args(parser):
    """Register flags for this command."""
    base.LIMIT_FLAG.AddToParser(parser)
    parser.add_argument(
        'log_filter', help=('Filter expression that specifies the '
                            'log entries to return. A detailed guide on '
                            'basic and advanced filters can be found at: '
                            'https://cloud.google.com/logging/docs/view/'
                            'overview'),
        nargs='?')
    order_arg = base.ChoiceArgument(
        '--order',
        choices=('desc', 'asc'),
        required=False,
        default='desc',
        help_str='Ordering of returned log entries based on timestamp field.'
    )
    order_arg.AddToParser(parser)
    parser.add_argument(
        '--freshness', required=False, type=arg_parsers.Duration(),
        help=('Return entries that are not older than this value. '
              'Works only with DESC ordering and filters without a timestamp. '
              'See $ gcloud topic datetimes for information on '
              'duration formats.'),
        default='1d')
    util.AddNonProjectArgs(parser, 'Read log entries')

  def Run(self, args):
    """This is what gets called when the user runs this command.

    Args:
      args: an argparse namespace. All the arguments that were provided to this
        command invocation.

    Returns:
      The list of log entries.
    """
    # Take into account freshness only if all requirements are met.
    if (args.freshness and args.order == 'desc' and
        (not args.log_filter or 'timestamp' not in args.log_filter)):
      # Argparser returns freshness in seconds.
      freshness = datetime.timedelta(seconds=args.freshness)
      # Cloud Logging uses timestamps in UTC timezone.
      last_timestamp = datetime.datetime.utcnow() - freshness
      # Construct timestamp filter.
      log_filter = ('timestamp>="%s"' % util.FormatTimestamp(last_timestamp))
      # Append any user supplied filters.
      if args.log_filter:
        log_filter += ' AND (%s)' % args.log_filter
    else:
      log_filter = args.log_filter

    return common.FetchLogs(log_filter,
                            order_by=args.order,
                            limit=args.limit,
                            parent=util.GetParentFromArgs(args))


Read.detailed_help = {
    'DESCRIPTION': """\
        {command} reads log entries.  Log entries matching *log-filter* are
        returned in order of decreasing timestamps, most-recent entries first.
        If the log entries come from multiple logs, then entries from different
        logs might be intermingled in the results.
    """,
    'EXAMPLES': """\
        To read log entries from Google Compute Engine instances, run:

          $ {command} "resource.type=gce_instance"

        To read log entries with severity ERROR or higher, run:

          $ {command} "severity>=ERROR"

        To read log entries written in a specific time window, run:

          $ {command} 'timestamp<="2015-05-31T23:59:59Z" AND timestamp>="2015-05-31T00:00:00Z"'

        To read up to 10 log entries in your project's syslog log from Compute
        Engine instances containing payloads that include the word `SyncAddress`
        and format the output in `JSON` format, run:

          $ {command} "resource.type=gce_instance AND logName=projects/[PROJECT_ID]/logs/syslog AND textPayload:SyncAddress" --limit=10 --format=json

        To read a log entry from a folder, run:

          $ {command} "resource.type=global" --folder=[FOLDER_ID] --limit=1

        Detailed information about filters can be found at:
        [](https://cloud.google.com/logging/docs/view/advanced_filters)
    """,
}
