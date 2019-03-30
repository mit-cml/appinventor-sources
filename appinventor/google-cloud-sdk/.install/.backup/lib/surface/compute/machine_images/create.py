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
"""Command for creating machine images."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.compute import flags
from googlecloudsdk.command_lib.compute import scope
from googlecloudsdk.command_lib.compute.machine_images import flags as machine_image_flags


class Create(base.CreateCommand):
  """Create Google Compute Engine machine images."""

  @staticmethod
  def Args(parser):
    parser.display_info.AddFormat(machine_image_flags.DEFAULT_LIST_FORMAT)
    Create.MACHINE_IMAGE_ARG = machine_image_flags.MakeMachineImageArg()
    Create.MACHINE_IMAGE_ARG.AddArgument(parser, operation_type='create')
    parser.add_argument(
        '--description',
        help='Specifies a textual description of the machine image.')

    Create.SOURCE_INSTANCE = machine_image_flags.MakeSourceInstanceArg()
    Create.SOURCE_INSTANCE.AddArgument(parser)

  def _CreateRequest(self, messages, machine_image_ref, description,
                     source_instance):
    return messages.ComputeMachineImagesInsertRequest(
        machineImage=messages.MachineImage(
            name=machine_image_ref.Name(),
            description=description,
            sourceInstance=source_instance),
        project=machine_image_ref.project)

  def Run(self, args):
    """Returns a list of requests necessary for adding machine images."""
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client

    machine_image_ref = Create.MACHINE_IMAGE_ARG.ResolveAsResource(
        args,
        holder.resources,
        default_scope=scope.ScopeEnum.GLOBAL,
        scope_lister=flags.GetDefaultScopeLister(client))

    source_instance = Create.SOURCE_INSTANCE.ResolveAsResource(
        args, holder.resources).SelfLink()
    return client.MakeRequests([(client.apitools_client.machineImages, 'Insert',
                                 self._CreateRequest(
                                     client.messages,
                                     machine_image_ref,
                                     description=args.description,
                                     source_instance=source_instance))])
