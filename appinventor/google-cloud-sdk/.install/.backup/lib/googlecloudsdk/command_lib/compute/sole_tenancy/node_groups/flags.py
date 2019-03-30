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
"""Flags for the `compute sole-tenancy node-groups` commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.command_lib.compute import flags as compute_flags


def MakeNodeGroupArg():
  return compute_flags.ResourceArgument(
      resource_name='node group',
      zonal_collection='compute.nodeGroups',
      zone_explanation=compute_flags.ZONE_PROPERTY_EXPLANATION)


def AddNoteTemplateFlagToParser(parser, required=True):
  parser.add_argument(
      '--node-template',
      required=required,
      help='The name of the node template resource to be set for this node '
           'group.')


def AddCreateArgsToParser(parser):
  """Add flags for creating a node group to the argument parser."""
  parser.add_argument(
      '--description',
      help='An optional description of this resource.')
  AddNoteTemplateFlagToParser(parser)
  parser.add_argument(
      '--target-size',
      required=True,
      type=int,
      help='The target initial number of nodes in the node group.')


def AddUpdateArgsToParser(parser):
  """Add flags for updating a node group to the argument parser."""
  update_node_count_group = parser.add_group(mutex=True)
  update_node_count_group.add_argument(
      '--add-nodes',
      type=int,
      help='The number of nodes to add to the node group.')
  update_node_count_group.add_argument(
      '--delete-nodes',
      metavar='NODE',
      type=arg_parsers.ArgList(),
      help='The names of the nodes to remove from the group.')
  AddNoteTemplateFlagToParser(parser, required=False)
