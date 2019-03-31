# -*- coding: utf-8 -*- #
# Copyright 2018 Google Inc. All Rights Reserved.
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
"""Command for bigtable clusters create."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.bigtable import clusters
from googlecloudsdk.api_lib.bigtable import util
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.bigtable import arguments
from googlecloudsdk.core import log


@base.ReleaseTracks(base.ReleaseTrack.BETA, base.ReleaseTrack.ALPHA)
class CreateCluster(base.CreateCommand):
  """Create a bigtable cluster."""

  @staticmethod
  def Args(parser):
    """Register flags for this command."""
    arguments.AddClusterResourceArg(parser, 'to describe')
    arguments.ArgAdder(parser).AddClusterZone().AddClusterNodes(
        required=False, default=3).AddAsync()

  def Run(self, args):
    """This is what gets called when the user runs this command.

    Args:
      args: an argparse namespace. All the arguments that were provided to this
        command invocation.

    Returns:
      Some value that we want to have printed later.
    """
    cluster_ref = args.CONCEPTS.cluster.Parse()
    operation = clusters.Create(
        cluster_ref, args.zone, serve_nodes=args.num_nodes)
    operation_ref = util.GetOperationRef(operation)
    if args.async:
      log.CreatedResource(
          operation_ref,
          kind='bigtable cluster {0}'.format(cluster_ref.Name()),
          is_async=True)
      return
    return util.AwaitCluster(
        operation_ref,
        'Creating bigtable cluster {0}'.format(cluster_ref.Name()))
