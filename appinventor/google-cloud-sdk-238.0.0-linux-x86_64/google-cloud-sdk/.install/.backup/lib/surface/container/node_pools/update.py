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
"""Update node pool command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from apitools.base.py import exceptions as apitools_exceptions
from googlecloudsdk.api_lib.container import api_adapter
from googlecloudsdk.api_lib.container import util
from googlecloudsdk.calliope import base
from googlecloudsdk.calliope import exceptions
from googlecloudsdk.command_lib.container import flags
from googlecloudsdk.command_lib.container import messages
from googlecloudsdk.core import log

DETAILED_HELP = {
    'DESCRIPTION':
        """\
        *{command}* updates a node pool in a Google Kubernetes Engine cluster.
        """,
    'EXAMPLES':
        """\
        To turn on node auto repair in "node-pool-1" in the cluster
        "sample-cluster", run:

          $ {command} node-pool-1 --cluster=example-cluster --enable-autoupgrade
        """,
}


def _Args(parser):
  """Register flags for this command.

  Args:
    parser: An argparse.ArgumentParser-like object. It is mocked out in order to
      capture some information, but behaves like an ArgumentParser.
  """
  flags.AddNodePoolNameArg(parser, 'The name of the node pool.')
  flags.AddNodePoolClusterFlag(parser, 'The name of the cluster.')
  # Timeout in seconds for operation
  parser.add_argument(
      '--timeout',
      type=int,
      default=1800,
      hidden=True,
      help='THIS ARGUMENT NEEDS HELP TEXT.')


@base.ReleaseTracks(base.ReleaseTrack.GA)
class Update(base.UpdateCommand):
  """Updates a node pool in a running cluster."""

  @staticmethod
  def Args(parser):
    _Args(parser)
    node_management_group = parser.add_argument_group(
        'Node management', required=True)
    flags.AddEnableAutoRepairFlag(node_management_group, for_node_pool=True)
    flags.AddEnableAutoUpgradeFlag(node_management_group, for_node_pool=True)

  def ParseUpdateNodePoolOptions(self, args):
    return api_adapter.UpdateNodePoolOptions(
        enable_autorepair=args.enable_autorepair,
        enable_autoupgrade=args.enable_autoupgrade)

  def Run(self, args):
    """This is what gets called when the user runs this command.

    Args:
      args: an argparse namespace. All the arguments that were provided to this
        command invocation.

    Returns:
      Cluster message for the successfully updated node pool.

    Raises:
      util.Error, if creation failed.
    """
    adapter = self.context['api_adapter']
    location_get = self.context['location_get']
    location = location_get(args)
    pool_ref = adapter.ParseNodePool(args.name, location)
    options = self.ParseUpdateNodePoolOptions(args)

    if options.enable_autorepair is not None:
      log.status.Print(
          messages.AutoUpdateUpgradeRepairMessage(options.enable_autorepair,
                                                  'autorepair'))

    if options.enable_autoupgrade is not None:
      log.status.Print(
          messages.AutoUpdateUpgradeRepairMessage(options.enable_autoupgrade,
                                                  'autoupgrade'))
    try:
      operation_ref = adapter.UpdateNodePool(pool_ref, options)

      adapter.WaitForOperation(
          operation_ref,
          'Updating node pool {0}'.format(pool_ref.nodePoolId),
          timeout_s=args.timeout)
      pool = adapter.GetNodePool(pool_ref)
    except apitools_exceptions.HttpError as error:
      raise exceptions.HttpException(error, util.HTTP_ERROR_FORMAT)

    log.UpdatedResource(pool_ref)
    return pool


@base.ReleaseTracks(base.ReleaseTrack.BETA)
class UpdateBeta(Update):
  """Updates a node pool in a running cluster."""

  @staticmethod
  def Args(parser):
    _Args(parser)
    group = parser.add_mutually_exclusive_group(required=True)
    node_management_group = group.add_argument_group('Node management')
    flags.AddEnableAutoRepairFlag(node_management_group, for_node_pool=True)
    flags.AddEnableAutoUpgradeFlag(node_management_group, for_node_pool=True)
    autoscaling_group = flags.AddClusterAutoscalingFlags(group, hidden=False)
    flags.AddNodePoolAutoprovisioningFlag(autoscaling_group, hidden=True)

  def ParseUpdateNodePoolOptions(self, args):
    ops = api_adapter.UpdateNodePoolOptions(
        enable_autorepair=args.enable_autorepair,
        enable_autoupgrade=args.enable_autoupgrade,
        enable_autoscaling=args.enable_autoscaling,
        max_nodes=args.max_nodes,
        min_nodes=args.min_nodes,
        enable_autoprovisioning=args.enable_autoprovisioning)
    return ops


@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class UpdateAlpha(Update):
  """Updates a node pool in a running cluster."""

  @staticmethod
  def Args(parser):
    _Args(parser)
    group = parser.add_mutually_exclusive_group(required=True)
    node_management_group = group.add_argument_group('Node management')
    flags.AddEnableAutoRepairFlag(node_management_group, for_node_pool=True)
    flags.AddEnableAutoUpgradeFlag(node_management_group, for_node_pool=True)
    autoscaling_group = flags.AddClusterAutoscalingFlags(group, hidden=False)
    flags.AddNodePoolAutoprovisioningFlag(autoscaling_group, hidden=True)

  def ParseUpdateNodePoolOptions(self, args):
    ops = api_adapter.UpdateNodePoolOptions(
        enable_autorepair=args.enable_autorepair,
        enable_autoupgrade=args.enable_autoupgrade,
        enable_autoscaling=args.enable_autoscaling,
        max_nodes=args.max_nodes,
        min_nodes=args.min_nodes,
        enable_autoprovisioning=args.enable_autoprovisioning)
    return ops


Update.detailed_help = DETAILED_HELP
