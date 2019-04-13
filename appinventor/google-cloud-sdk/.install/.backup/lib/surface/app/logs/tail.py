# -*- coding: utf-8 -*- #
# Copyright 2017 Google Inc. All Rights Reserved.
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
"""app logs tail command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.app import logs_util
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.app import flags
from googlecloudsdk.command_lib.logs import stream
from googlecloudsdk.core import log
from googlecloudsdk.core import properties


@base.ReleaseTracks(base.ReleaseTrack.BETA, base.ReleaseTrack.GA)
class Tail(base.Command):
  """Streams logs for App Engine apps."""

  detailed_help = {
      'EXAMPLES': """\
          To stream logs from a serving app, run:

            $ {command}

          To show only logs with a specific service, version, and level, run:

            $ {command} --service=s1 --version=v1 --level=warning

          To show only the logs from the request log for Standard apps, run:

            $ {command} --logs=request_log

          To show only the logs from the request log for Flex apps, run:

            $ {command} --logs=nginx.request
      """
  }

  @staticmethod
  def Args(parser):
    """Register flags for this command."""
    flags.SERVICE.AddToParser(parser)
    flags.VERSION.AddToParser(parser)
    flags.LEVEL.AddToParser(parser)
    flags.LOGS.AddToParser(parser)

  def Run(self, args):
    printer = logs_util.LogPrinter()
    printer.RegisterFormatter(logs_util.FormatRequestLogEntry)
    printer.RegisterFormatter(logs_util.FormatNginxLogEntry)
    printer.RegisterFormatter(logs_util.FormatAppEntry)
    project = properties.VALUES.core.project.Get(required=True)
    filters = logs_util.GetFilters(project, args.logs, args.service,
                                   args.version, args.level)

    log.status.Print('Waiting for new log entries...')
    log_fetcher = stream.LogFetcher(filters=filters,
                                    polling_interval=1,
                                    num_prev_entries=100)
    for log_entry in log_fetcher.YieldLogs():
      log.out.Print(printer.Format(log_entry))
