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
"""Node group api client."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import utils as compute_util
from googlecloudsdk.api_lib.compute.operations import poller
from googlecloudsdk.api_lib.util import waiter
from googlecloudsdk.command_lib.compute.sole_tenancy.node_groups import util
from six.moves import map


class NodeGroupsClient(object):
  """Client for node groups service in the GCE API."""

  def __init__(self, client, messages, resources):
    self.client = client
    self.messages = messages
    self.resources = resources
    self._service = self.client.nodeGroups

  def SetNodeTemplate(self, node_group_ref, node_template):
    """Sets the node template field on the node group."""
    node_template_ref = util.ParseNodeTemplate(
        self.resources,
        node_template,
        project=node_group_ref.project,
        region=compute_util.ZoneNameToRegionName(node_group_ref.zone))
    set_request = self.messages.NodeGroupsSetNodeTemplateRequest(
        nodeTemplate=node_template_ref.RelativeName())
    request = self.messages.ComputeNodeGroupsSetNodeTemplateRequest(
        nodeGroupsSetNodeTemplateRequest=set_request,
        nodeGroup=node_group_ref.Name(),
        project=node_group_ref.project,
        zone=node_group_ref.zone)
    return self._service.SetNodeTemplate(request)

  def AddNodes(self, node_group_ref, additional_node_count):
    request = self.messages.ComputeNodeGroupsAddNodesRequest(
        nodeGroupsAddNodesRequest=self.messages.NodeGroupsAddNodesRequest(
            additionalNodeCount=additional_node_count),
        nodeGroup=node_group_ref.Name(),
        project=node_group_ref.project,
        zone=node_group_ref.zone)
    return self._service.AddNodes(request)

  def DeleteNodes(self, node_group_ref, nodes):
    request = self.messages.ComputeNodeGroupsDeleteNodesRequest(
        nodeGroupsDeleteNodesRequest=self.messages.NodeGroupsDeleteNodesRequest(
            nodes=nodes),
        nodeGroup=node_group_ref.Name(),
        project=node_group_ref.project,
        zone=node_group_ref.zone)
    return self._service.DeleteNodes(request)

  def _GetOperationsRef(self, operation):
    return self.resources.Parse(operation.selfLink,
                                collection='compute.zoneOperations')

  def _WaitForResult(self, operation_poller, operation_ref, message):
    if operation_ref:
      return waiter.WaitFor(operation_poller, operation_ref, message)
    return None

  def Update(self,
             node_group_ref,
             node_template=None,
             additional_node_count=None,
             delete_nodes=None):
    """Updates a Compute Node Group."""
    set_node_template_ref = None
    add_nodes_ref = None
    delete_nodes_ref = None

    if node_template:
      operation = self.SetNodeTemplate(node_group_ref, node_template)
      set_node_template_ref = self._GetOperationsRef(operation)

    if additional_node_count:
      operation = self.AddNodes(node_group_ref, additional_node_count)
      add_nodes_ref = self._GetOperationsRef(operation)

    if delete_nodes:
      operation = self.DeleteNodes(node_group_ref, delete_nodes)
      delete_nodes_ref = self._GetOperationsRef(operation)

    node_group_name = node_group_ref.Name()
    operation_poller = poller.Poller(self._service)
    result = None
    result = self._WaitForResult(
        operation_poller, set_node_template_ref,
        'Setting node template on [{0}] to [{1}].'.format(
            node_group_name, node_template)) or result
    result = self._WaitForResult(
        operation_poller, add_nodes_ref,
        'Adding [{0}] nodes to [{1}].'.format(
            additional_node_count, node_group_name)) or result
    deleted_nodes_str = ','.join(map(str, delete_nodes or []))
    result = self._WaitForResult(
        operation_poller, delete_nodes_ref,
        'Deleting nodes [{0}] in [{1}].'.format(
            deleted_nodes_str, node_group_name)) or result

    return result
