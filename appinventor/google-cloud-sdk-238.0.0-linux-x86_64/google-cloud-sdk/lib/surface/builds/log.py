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
"""Stream-logs command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.cloudbuild import cloudbuild_util
from googlecloudsdk.api_lib.cloudbuild import logs as cb_logs
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.builds import flags
from googlecloudsdk.core import properties
from googlecloudsdk.core import resources


class Log(base.Command):
  """Stream the logs for a build."""

  @staticmethod
  def Args(parser):
    flags.AddBuildArg(parser, intro='The build whose logs shall be printed.')
    parser.add_argument(
        '--stream',
        help=('If a build is ongoing, stream the logs to stdout until '
              'the build completes.'),
        action='store_true')

  def Run(self, args):
    """This is what gets called when the user runs this command.

    Args:
      args: an argparse namespace. All the arguments that were provided to this
        command invocation.

    Returns:
      Some value that we want to have printed later.
    """

    client = cloudbuild_util.GetClientInstance()
    messages = cloudbuild_util.GetMessagesModule()

    build_ref = resources.REGISTRY.Parse(
        args.build,
        params={
            'projectId': properties.VALUES.core.project.GetOrFail,
        },
        collection='cloudbuild.projects.builds')

    logger = cb_logs.CloudBuildClient(client, messages)
    if args.stream:
      logger.Stream(build_ref)
      return

    # Just print out what's available now.
    logger.PrintLog(build_ref)
