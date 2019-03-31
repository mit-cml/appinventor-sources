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
"""bigtable clusters update command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.bigtable import clusters
from googlecloudsdk.api_lib.bigtable import util
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.bigtable import arguments
from googlecloudsdk.core import log


@base.ReleaseTracks(base.ReleaseTrack.BETA, base.ReleaseTrack.ALPHA)
class UpdateCluster(base.UpdateCommand):
  """Update a Bigtable cluster's number of nodes."""

  @staticmethod
  def Args(parser):
    """Register flags for this command."""
    arguments.AddClusterResourceArg(parser, 'to update')
    (arguments.ArgAdder(parser).AddClusterNodes().AddAsync())

  def Run(self, args):
    """This is what gets called when the user runs this command.

    Args:
      args: an argparse namespace. All the arguments that were provided to this
        command invocation.

    Returns:
      None
    """
    cluster_ref = args.CONCEPTS.cluster.Parse()
    operation = clusters.Update(cluster_ref, args.num_nodes)
    if not args.async:
      operation_ref = util.GetOperationRef(operation)
      return util.AwaitCluster(
          operation_ref,
          'Updating bigtable cluster {0}'.format(cluster_ref.Name()))

    log.UpdatedResource(cluster_ref.Name(), kind='cluster', is_async=args.async)
    return None
