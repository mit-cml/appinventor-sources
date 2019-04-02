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
"""Command util functions for gcloud container commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.container import api_adapter
from googlecloudsdk.calliope import exceptions as calliope_exceptions
from googlecloudsdk.core import exceptions
from googlecloudsdk.core import properties
from googlecloudsdk.core.util import text


class Error(exceptions.Error):
  """Class for errors raised by container commands."""


class NodePoolError(Error):
  """Error when a node pool name doesn't match a node pool in the cluster."""


def _NodePoolFromCluster(cluster, node_pool_name):
  """Helper function to get node pool from a cluster, given its name."""
  for node_pool in cluster.nodePools:
    if node_pool.name == node_pool_name:
      # Node pools always have unique names.
      return node_pool
  raise NodePoolError('No node pool found matching the name [{}].'.format(
      node_pool_name))


def _MasterUpgradeMessage(name, server_conf, cluster, new_version):
  """Returns the prompt message during a master upgrade.

  Args:
    name: str, the name of the cluster being upgraded.
    server_conf: the server config object.
    cluster: the cluster object.
    new_version: str, the name of the new version, if given.

  Raises:
    NodePoolError: if the node pool name can't be found in the cluster.

  Returns:
    str, a message about which nodes in the cluster will be upgraded and
        to which version.
  """
  if cluster:
    version_message = 'version [{}]'.format(cluster.currentMasterVersion)
  else:
    version_message = 'its current version'

  if not new_version and server_conf:
    new_version = server_conf.defaultClusterVersion

  if new_version:
    new_version_message = 'version [{}]'.format(new_version)
  else:
    new_version_message = 'the default cluster version'

  return ('Master of cluster [{}] will be upgraded from {} to {}.'
          .format(name, version_message, new_version_message))


def _NodeUpgradeMessage(name, cluster, node_pool_name, new_version,
                        concurrent_node_count):
  """Returns the prompt message during a node upgrade.

  Args:
    name: str, the name of the cluster being upgraded.
    cluster: the cluster object.
    node_pool_name: str, the name of the node pool if the upgrade is for a
        specific node pool.
    new_version: str, the name of the new version, if given.
    concurrent_node_count: int, the number of nodes to upgrade concurrently.

  Raises:
    NodePoolError: if the node pool name can't be found in the cluster.

  Returns:
    str, a message about which nodes in the cluster will be upgraded and
        to which version.
  """
  node_message = 'All nodes'
  current_version = None
  if node_pool_name:
    node_message = '{} in node pool [{}]'.format(node_message, node_pool_name)
    if cluster:
      current_version = _NodePoolFromCluster(cluster, node_pool_name).version
  elif cluster:
    node_message = '{} ({} {})'.format(
        node_message,
        cluster.currentNodeCount,
        text.Pluralize(cluster.currentNodeCount, 'node'))
    current_version = cluster.currentNodeVersion

  if current_version:
    version_message = 'version [{}]'.format(current_version)
  else:
    version_message = 'its current version'

  if not new_version and cluster:
    new_version = cluster.currentMasterVersion

  if new_version:
    new_version_message = 'version [{}]'.format(new_version)
  else:
    new_version_message = 'the master version'

  concurrent_message = ''
  if concurrent_node_count:
    concurrent_message = ' {} {} will be upgraded at a time.'.format(
        concurrent_node_count,
        text.Pluralize(concurrent_node_count, 'node'))

  return ('{} of cluster [{}] will be upgraded from {} to {}.{}'
          .format(node_message, name, version_message,
                  new_version_message, concurrent_message))


def ClusterUpgradeMessage(name, server_conf=None, cluster=None, master=False,
                          node_pool_name=None, new_version=None,
                          concurrent_node_count=None):
  """Get a message to print during gcloud container clusters upgrade.

  Args:
    name: str, the name of the cluster being upgraded.
    server_conf: the server config object.
    cluster: the cluster object.
    master: bool, if the upgrade applies to the master version.
    node_pool_name: str, the name of the node pool if the upgrade is for a
        specific node pool.
    new_version: str, the name of the new version, if given.
    concurrent_node_count: int, the number of nodes to upgrade concurrently.

  Raises:
    NodePoolError: if the node pool name can't be found in the cluster.

  Returns:
    str, a message about which nodes in the cluster will be upgraded and
        to which version.
  """
  if master:
    upgrade_message = _MasterUpgradeMessage(name, server_conf, cluster,
                                            new_version)
  else:
    upgrade_message = _NodeUpgradeMessage(name, cluster, node_pool_name,
                                          new_version, concurrent_node_count)

  return ('{} This operation is long-running and will block other operations '
          'on the cluster (including delete) until it has run to completion.'
          .format(upgrade_message))


def GetZone(args, ignore_property=False, required=True):
  """Get a zone from argument or property.

  Args:
    args: an argparse namespace. All the arguments that were provided to this
        command invocation.
    ignore_property: bool, if true, will get location only from argument.
    required: bool, if true, lack of zone will cause raise an exception.

  Raises:
    MinimumArgumentException: if location if required and not provided.

  Returns:
    str, a zone selected by user.
  """
  zone = getattr(args, 'zone', None)

  if ignore_property:
    zone_property = None
  else:
    zone_property = properties.VALUES.compute.zone.Get()

  if required and not zone and not zone_property:
    raise calliope_exceptions.MinimumArgumentException(
        ['--zone'], 'Please specify zone')

  return zone or zone_property


def GetZoneOrRegion(args, ignore_property=False, required=True):
  """Get a location (zone or region) from argument or property.

  Args:
    args: an argparse namespace. All the arguments that were provided to this
        command invocation.
    ignore_property: bool, if true, will get location only from argument.
    required: bool, if true, lack of zone will cause raise an exception.

  Raises:
    MinimumArgumentException: if location if required and not provided.
    ConflictingArgumentsException: if both --zone and --region arguments
        provided.

  Returns:
    str, a location selected by user.
  """
  zone = getattr(args, 'zone', None)
  region = getattr(args, 'region', None)

  if ignore_property:
    zone_property = None
  else:
    zone_property = properties.VALUES.compute.zone.Get()

  if zone and region:
    raise calliope_exceptions.ConflictingArgumentsException(
        '--zone', '--region')

  location = region or zone or zone_property
  if required and not location:
    raise calliope_exceptions.MinimumArgumentException(
        ['--zone', '--region'], 'Please specify location')

  return location


def ParseUpdateOptionsBase(args, locations):
  """Helper function to build ClusterUpdateOptions object from args.

  Args:
    args: an argparse namespace. All the arguments that were provided to this
        command invocation.
    locations: list of strings. Zones in which cluster has nodes.

  Returns:
    ClusterUpdateOptions, object with data used to update cluster.
  """
  return api_adapter.UpdateClusterOptions(
      monitoring_service=args.monitoring_service,
      logging_service=args.logging_service,
      disable_addons=args.disable_addons,
      enable_autoscaling=args.enable_autoscaling,
      min_nodes=args.min_nodes,
      max_nodes=args.max_nodes,
      node_pool=args.node_pool,
      locations=locations,
      enable_master_authorized_networks=args.enable_master_authorized_networks,
      master_authorized_networks=args.master_authorized_networks)
