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
"""Command to run an Airflow CLI sub-command in an environment."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import argparse

from googlecloudsdk.api_lib.composer import environments_util as environments_api_util
from googlecloudsdk.api_lib.composer import util as api_util
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.composer import resource_args
from googlecloudsdk.command_lib.composer import util as command_util
from googlecloudsdk.core import log

WORKER_POD_SUBSTR = 'worker'
WORKER_CONTAINER = 'airflow-worker'


class Run(base.Command):
  """Run an Airflow sub-command remotely in a Cloud Composer environment.

  Executes an Airflow CLI sub-command remotely in an environment. If the
  sub-command takes flags, separate the environment name from the sub-command
  and its flags with ``--''. This command waits for the sub-command to
  complete; its exit code will match the sub-command's exit code.

  ## EXAMPLES

    The following command:

    {command} myenv trigger_dag -- some_dag --run_id=foo

  is equivalent to running the following command from a shell inside the
  *my-environment* environment:

    airflow trigger_dag some_dag --run_id=foo
  """

  @staticmethod
  def Args(parser):
    resource_args.AddEnvironmentResourceArg(
        parser, 'in which to run an Airflow command')

    parser.add_argument(
        'subcommand',
        metavar='SUBCOMMAND',
        choices=command_util.SUBCOMMAND_WHITELIST,
        help='The Airflow CLI subcommand to run. (see '
        'https://airflow.incubator.apache.org/cli.html)')
    parser.add_argument(
        'cmd_args',
        metavar='CMD_ARGS',
        nargs=argparse.REMAINDER,
        help='Command line arguments to the subcommand.',
        example='{command} myenv trigger_dag -- some_dag --run_id=foo')

  def BypassConfirmationPrompt(self, args):
    """Bypasses confirmations with "yes" responses.

    Prevents certain Airflow CLI subcommands from presenting a confirmation
    prompting (which can hang the gcloud CLI). When necessary, bypass
    confirmations with a "yes" response.

    Args:
      args: argparse.Namespace, An object that contains the values for the
        arguments specified in the .Args() method.
    """
    prompting_subcommands = ['resetdb', 'delete_dag']
    if args.subcommand in prompting_subcommands and set(
        args.cmd_args).isdisjoint({'-y', '--yes'}):
      args.cmd_args.append('--yes')

  def Run(self, args):
    running_state = (
        api_util.GetMessagesModule(release_track=self.ReleaseTrack())
        .Environment.StateValueValuesEnum.RUNNING)

    env_ref = args.CONCEPTS.environment.Parse()
    env_obj = environments_api_util.Get(
        env_ref, release_track=self.ReleaseTrack())

    if env_obj.state != running_state:
      raise command_util.Error(
          'Cannot execute subcommand for environment in state {}. '
          'Must be RUNNING.'.format(env_obj.state))

    cluster_id = env_obj.config.gkeCluster
    cluster_location_id = command_util.ExtractGkeClusterLocationId(env_obj)

    with command_util.TemporaryKubeconfig(cluster_location_id, cluster_id):
      pod = command_util.GetGkePod(pod_substr=WORKER_POD_SUBSTR)

      self.BypassConfirmationPrompt(args)
      kubectl_args = [
          'exec', pod, '-tic', WORKER_CONTAINER, 'airflow', args.subcommand
      ]
      if args.cmd_args:
        # Add '--' to the argument list so kubectl won't eat the command args.
        kubectl_args.extend(['--'] + args.cmd_args)
      command_util.RunKubectlCommand(kubectl_args, out_func=log.status.Print)
