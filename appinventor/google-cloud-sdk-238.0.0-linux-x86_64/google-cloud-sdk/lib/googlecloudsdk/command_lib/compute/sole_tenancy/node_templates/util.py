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
"""Utility methods for the compute node templates commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from apitools.base.py import encoding
from googlecloudsdk.command_lib.compute.sole_tenancy.node_templates import flags


def _ParseNodeAffinityLabels(affinity_labels, messages):
  affinity_labels_class = messages.NodeTemplate.NodeAffinityLabelsValue
  return encoding.DictToAdditionalPropertyMessage(
      affinity_labels, affinity_labels_class, sort_items=True)


def CreateNodeTemplate(node_template_ref, args, messages,
                       enable_server_binding):
  """Creates a Node Template message from args."""
  node_affinity_labels = None
  if args.node_affinity_labels:
    node_affinity_labels = _ParseNodeAffinityLabels(
        args.node_affinity_labels, messages)

  node_type_flexbility = None
  if args.IsSpecified('node_requirements'):
    node_type_flexbility = messages.NodeTemplateNodeTypeFlexibility(
        cpus=str(args.node_requirements.get('vCPU', 'any')),
        # local SSD is unique because the user may omit the local SSD constraint
        # entirely to include the possibility of node types with no local SSD.
        # "any" corresponds to "greater than zero".
        localSsd=args.node_requirements.get('localSSD', None),
        memory=args.node_requirements.get('memory', 'any'))

  node_template = messages.NodeTemplate(
      name=node_template_ref.Name(),
      description=args.description,
      nodeAffinityLabels=node_affinity_labels,
      nodeType=args.node_type,
      nodeTypeFlexibility=node_type_flexbility)

  if enable_server_binding:
    server_binding_flag = flags.GetServerBindingMapperFlag(messages)
    server_binding = messages.ServerBinding(
        type=server_binding_flag.GetEnumForChoice(args.server_binding))
    node_template.serverBinding = server_binding

  return node_template
