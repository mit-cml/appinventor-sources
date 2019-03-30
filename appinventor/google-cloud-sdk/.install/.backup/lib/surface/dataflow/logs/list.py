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
"""Implementation of gcloud dataflow logs list command.
"""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.dataflow import apis
from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.dataflow import dataflow_util
from googlecloudsdk.command_lib.dataflow import job_utils
from googlecloudsdk.core.util import times


class List(base.ListCommand):
  """Retrieve the job logs for a specific job.

  Retrieves the job logs from a specified job using the Dataflow Messages API
  with at least the specified importance level. Can also be used to display
  logs between a given time period using the --before and --after flags. These
  logs are produced by the service and are distinct from worker logs. Worker
  logs can be found in Cloud Logging.

  ## EXAMPLES

  Retrieve only error logs:

    $ {command} --importance=error

  Retrieve all logs after some date:

    $ {command} --after="2016-08-12 00:00:00"

  Retrieve logs from this year:

    $ {command} --after=2018-01-01

  Retrieve logs more than a week old:

    $ {command} --before=-P1W
  """

  @staticmethod
  def Args(parser):
    job_utils.ArgsForJobRef(parser)

    base.SORT_BY_FLAG.RemoveFromParser(parser)
    base.URI_FLAG.RemoveFromParser(parser)
    base.ASYNC_FLAG.RemoveFromParser(parser)
    base.PAGE_SIZE_FLAG.RemoveFromParser(parser)

    parser.add_argument(
        '--after',
        type=arg_parsers.Datetime.Parse,
        help=('Only display messages logged after the given time. '
              'See $ gcloud topic datetimes for information on time formats. '
              'For example, `2018-01-01` is the first day of the year, and '
              '`-P2W` is 2 weeks ago.'))
    parser.add_argument(
        '--before',
        type=arg_parsers.Datetime.Parse,
        help=('Only display messages logged before the given time. '
              'See $ gcloud topic datetimes for information on time formats.'))
    parser.add_argument(
        '--importance',
        choices=['debug', 'detailed', 'warning', 'error'],
        default='warning',
        help='Minimum importance a message must have to be displayed.')

    parser.display_info.AddFormat("""
          table[no-heading,pad=1](
            messageImportance.enum(dataflow.JobMessage),
            time.date(tz=LOCAL):label=TIME,
            id,
            messageText:label=TEXT
          )
    """)

    symbols = {'dataflow.JobMessage::enum': {
        'JOB_MESSAGE_DETAILED': 'd',
        'JOB_MESSAGE_DEBUG': 'D',
        'JOB_MESSAGE_WARNING': 'W',
        'JOB_MESSAGE_ERROR': 'E',
    }}

    parser.display_info.AddTransforms(symbols)

  def Run(self, args):
    """This is what gets called when the user runs this command.

    Args:
      args: all the arguments that were provided to this command invocation.

    Returns:
      None on success, or a string containing the error message.
    """
    job_ref = job_utils.ExtractJobRef(args)

    importance_enum = (
        apis.Messages.LIST_REQUEST.MinimumImportanceValueValuesEnum)
    importance_map = {
        'debug': importance_enum.JOB_MESSAGE_DEBUG,
        'detailed': importance_enum.JOB_MESSAGE_DETAILED,
        'error': importance_enum.JOB_MESSAGE_ERROR,
        'warning': importance_enum.JOB_MESSAGE_WARNING,
    }

    request = apis.Messages.LIST_REQUEST(
        projectId=job_ref.projectId,
        jobId=job_ref.jobId,
        location=job_ref.location,
        minimumImportance=(args.importance and importance_map[args.importance]),

        # Note: It if both are present, startTime > endTime, because we will
        # return messages with actual time [endTime, startTime).
        startTime=args.after and times.FormatDateTime(args.after),
        endTime=args.before and times.FormatDateTime(args.before))

    return dataflow_util.YieldFromList(
        job_id=job_ref.jobId,
        project_id=job_ref.projectId,
        region_id=job_ref.location,
        service=apis.Messages.GetService(),
        request=request,
        batch_size=args.limit,
        batch_size_attribute='pageSize',
        field='jobMessages')
