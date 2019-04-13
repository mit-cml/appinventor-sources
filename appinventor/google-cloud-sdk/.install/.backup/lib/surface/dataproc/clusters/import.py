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
"""Import cluster command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.dataproc import dataproc as dp
from googlecloudsdk.api_lib.dataproc import util as dp_util
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.dataproc import clusters
from googlecloudsdk.command_lib.dataproc import flags
from googlecloudsdk.command_lib.export import util as export_util
from googlecloudsdk.core.console import console_io


@base.ReleaseTracks(base.ReleaseTrack.ALPHA, base.ReleaseTrack.BETA)
class Import(base.UpdateCommand):
  """Import a cluster.

  This will create a new cluster with the given configuration. If a cluster with
  this name already exists, an error will be thrown.
  """

  @classmethod
  def GetApiVersion(cls):
    """Returns the API version based on the release track."""
    if cls.ReleaseTrack() == base.ReleaseTrack.BETA:
      return 'v1beta2'
    return 'v1'

  @classmethod
  def GetSchemaPath(cls, for_help=False):
    """Returns the resource schema path."""
    return export_util.GetSchemaPath(
        'dataproc', cls.GetApiVersion(), 'Cluster', for_help=for_help)

  @classmethod
  def Args(cls, parser):
    parser.add_argument('name', help='The name of the cluster to import.')
    export_util.AddImportFlags(parser, cls.GetSchemaPath(for_help=True))
    base.ASYNC_FLAG.AddToParser(parser)
    # 30m is backend timeout + 5m for safety buffer.
    flags.AddTimeoutFlag(parser, default='35m')

  def Run(self, args):
    dataproc = dp.Dataproc(self.ReleaseTrack())
    msgs = dataproc.messages

    data = console_io.ReadFromFileOrStdin(args.source or '-', binary=False)
    cluster = export_util.Import(message_type=msgs.Cluster,
                                 stream=data,
                                 schema_path=self.GetSchemaPath())

    cluster_ref = dp_util.ParseCluster(args.name, dataproc)
    cluster.clusterName = cluster_ref.clusterName
    cluster.projectId = cluster_ref.projectId

    # Import only supports create, not update (for now).
    return clusters.CreateCluster(dataproc, cluster, args.async, args.timeout)
