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
"""Command for deleting machine images."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.api_lib.compute import utils
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.compute import flags as compute_flags
from googlecloudsdk.command_lib.compute.machine_images import flags


class Delete(base.DeleteCommand):
  """Delete Google Compute Engine machine images.

  *{command}* deletes one or more Google Compute Engine
  machine images. Machine images can be deleted only if they are not
  being used to restore any virtual machine instances.
  """

  @staticmethod
  def Args(parser):
    Delete.MACHINE_IMAGE_ARG = flags.MakeMachineImageArg(plural=True)
    Delete.MACHINE_IMAGE_ARG.AddArgument(parser, operation_type='delete')

  def Run(self, args):
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client

    machine_image_refs = Delete.MACHINE_IMAGE_ARG.ResolveAsResource(
        args,
        holder.resources,
        scope_lister=compute_flags.GetDefaultScopeLister(client))

    utils.PromptForDeletion(machine_image_refs)

    requests = [(client.apitools_client.machineImages, 'Delete',
                 client.messages.ComputeMachineImagesDeleteRequest(
                     **machine_image_ref.AsDict()))
                for machine_image_ref in machine_image_refs]

    return client.MakeRequests(requests)
