# -*- coding: utf-8 -*- #
# Copyright 2019 Google Inc. All Rights Reserved.
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
"""Base class for the Presto job."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from apitools.base.py import encoding

from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.command_lib.dataproc.jobs import base as job_base


class PrestoBase(job_base.JobBase):
  """Submit a Presto job to a cluster."""

  @staticmethod
  def Args(parser):
    """Parses command line arguments specific to submitting Presto jobs."""
    driver = parser.add_mutually_exclusive_group(required=True)
    driver.add_argument(
        '--execute',
        '-e',
        metavar='QUERY',
        dest='queries',
        action='append',
        default=[],
        help='A Presto query to execute as the job.')
    driver.add_argument(
        '--file',
        '-f',
        help='HCFS URI of file containing Presto script to execute as the job')

    parser.add_argument(
        '--properties',
        type=arg_parsers.ArgDict(),
        metavar='PARAM=VALUE',
        help='A list of key value pairs to set Presto session properties')
    parser.add_argument(
        '--driver-log-levels',
        type=arg_parsers.ArgDict(),
        metavar='PACKAGE=LEVEL',
        help=('A list of package to log4j log level pairs to configure driver '
              'logging. For example: root=FATAL,com.example=INFO'))
    parser.add_argument(
        '--continue-on-failure',
        action='store_true',
        help='Whether to continue if a single query fails.')
    parser.add_argument(
        '--query-output-format',
        help=('The format in which query output will be displayed. See the '
              'Presto documentation for supported output formats.'))
    parser.add_argument(
        '--client-tags',
        type=arg_parsers.ArgList(),
        metavar='CLIENT_TAG',
        help='List of Presto client tags to attach to this query.')

  @staticmethod
  def GetFilesByType(args):
    return {'file': args.file}

  @staticmethod
  def ConfigureJob(messages, job, files_by_type, logging_config, args):
    """Populates the prestoJob member of the given job."""

    presto_job = messages.PrestoJob(
        continueOnFailure=args.continue_on_failure,
        queryFileUri=files_by_type['file'],
        loggingConfig=logging_config)

    if args.queries:
      presto_job.queryList = messages.QueryList(queries=args.queries)
    if args.properties:
      presto_job.properties = encoding.DictToAdditionalPropertyMessage(
          args.properties, messages.PrestoJob.PropertiesValue)
    if args.query_output_format:
      presto_job.outputFormat = args.query_output_format
    if args.client_tags:
      presto_job.clientTags = args.client_tags

    job.prestoJob = presto_job
