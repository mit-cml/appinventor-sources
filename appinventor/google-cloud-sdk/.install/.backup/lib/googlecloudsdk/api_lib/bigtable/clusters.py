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
"""Bigtable clusters API helper."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.bigtable import util


def Delete(cluster_ref):
  """Delete a cluster.

  Args:
    cluster_ref: A resource reference to the cluster to delete.
  """
  client = util.GetAdminClient()
  msgs = util.GetAdminMessages()
  msg = msgs.BigtableadminProjectsInstancesClustersDeleteRequest(
      name=cluster_ref.RelativeName())
  client.projects_instances_clusters.Delete(msg)


def Create(cluster_ref, zone, serve_nodes=3):
  """Create a cluster.

  Args:
    cluster_ref: A resource reference to the cluster to create.
    zone: string, The zone this cluster should be inside.
    serve_nodes: int, The number of nodes in this cluster.

  Returns:
    Long running operation.
  """
  client = util.GetAdminClient()
  msgs = util.GetAdminMessages()

  storage_type = (
      msgs.Cluster.DefaultStorageTypeValueValuesEnum.STORAGE_TYPE_UNSPECIFIED)

  cluster_msg = msgs.Cluster(
      serveNodes=serve_nodes,
      location=util.LocationUrl(zone),
      defaultStorageType=storage_type)

  msg = msgs.BigtableadminProjectsInstancesClustersCreateRequest(
      cluster=cluster_msg,
      clusterId=cluster_ref.Name(),
      parent=cluster_ref.Parent().RelativeName())
  return client.projects_instances_clusters.Create(msg)


def Update(cluster_ref, serve_nodes):
  """Update a cluster.

  Args:
    cluster_ref: A resource reference to the cluster to update.
    serve_nodes: int, The number of nodes in this cluster.

  Returns:
    Long running operation.
  """
  client = util.GetAdminClient()
  msgs = util.GetAdminMessages()
  msg = msgs.Cluster(name=cluster_ref.RelativeName(), serveNodes=serve_nodes)
  return client.projects_instances_clusters.Update(msg)
