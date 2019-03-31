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

"""services enable command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.services import services_util
from googlecloudsdk.api_lib.services import serviceusage
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.services import common_flags
from googlecloudsdk.core import log
from googlecloudsdk.core import properties

_OP_BASE_CMD = 'gcloud beta services operations '
_OP_WAIT_CMD = _OP_BASE_CMD + 'wait {0}'

_DETAILED_HELP = {
    'DESCRIPTION':
        """\
        This command enables a service for consumption for a project.

        To see a list of available services for a project, run:

          $ {parent_command} list --available

     More information on listing services can be found at:
     https://cloud.google.com/service-usage/docs/list-services and on
     disabling a service at:
     https://cloud.google.com/service-usage/docs/enable-disable
        """,
    'EXAMPLES':
        """\
        To enable a service called `my-consumed-service` on the current
        project, run:

          $ {command} my-consumed-service

        To run the same command asynchronously (non-blocking), run:

          $ {command} my-consumed-service --async

        To enable services called `service1`, `service2`, and `service3` on the
        current project, run:

          $ {command} service1 service2 service3
        """,
}


class Enable(base.SilentCommand):
  """Enables a service for consumption for a project."""

  @staticmethod
  def Args(parser):
    """Args is called by calliope to gather arguments for this command.

    Args:
      parser: An argparse parser that you can use to add arguments that go
          on the command line after this command. Positional arguments are
          allowed.
    """
    common_flags.available_service_flag(suffix='to enable').AddToParser(parser)
    base.ASYNC_FLAG.AddToParser(parser)

  def Run(self, args):
    """Run 'services enable'.

    Args:
      args: argparse.Namespace, The arguments that this command was invoked
          with.

    Returns:
      Nothing.
    """
    project = properties.VALUES.core.project.Get(required=True)
    if len(args.service) == 1:
      op = serviceusage.EnableApiCall(project, args.service[0])
    else:
      op = serviceusage.BatchEnableApiCall(project, args.service)
    if op.done:
      return
    if args.async:
      cmd = _OP_WAIT_CMD.format(op.name)
      log.status.Print('Asynchronous operation is in progress... '
                       'Use the following command to wait for its '
                       'completion:\n {0}'.format(cmd))
      return
    op = serviceusage.WaitOperation(op.name)
    services_util.PrintOperation(op)


Enable.detailed_help = _DETAILED_HELP
