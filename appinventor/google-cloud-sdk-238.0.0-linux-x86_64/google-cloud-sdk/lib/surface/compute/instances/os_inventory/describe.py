# -*- coding: utf-8 -*- #
# Copyright 2019 Google Inc. All Rights Reserved.
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
"""Command for describing instance's OS inventory data."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import base64
import textwrap
import zlib

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.api_lib.compute import utils
from googlecloudsdk.calliope import base
from googlecloudsdk.calliope import exceptions as calliope_exceptions
from googlecloudsdk.command_lib.compute.instances import flags
from googlecloudsdk.command_lib.compute.instances.os_inventory import exceptions


@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class Describe(base.DescribeCommand):
  """Describe a Google Compute Engine virtual instance's OS inventory data.

  *{command}* displays all OS inventory data associated with a Google Compute
  Engine virtual machine instance.
  """

  @staticmethod
  def Args(parser):
    flags.INSTANCE_ARG.AddArgument(parser, operation_type='describe')
    parser.display_info.AddFormat(
        textwrap.dedent("""table[no-heading](
      key.list(separator='
      '),
      value.list(separator='
      '))"""))

  def _GetInstanceRef(self, holder, args):
    return flags.INSTANCE_ARG.ResolveAsResource(
        args,
        holder.resources,
        scope_lister=flags.GetInstanceZoneScopeLister(holder.client))

  def _GetGuestInventoryGuestAttributes(self, holder, instance_ref):
    try:
      client = holder.client
      messages = client.messages
      request = messages.ComputeInstancesGetGuestAttributesRequest(
          instance=instance_ref.Name(),
          project=instance_ref.project,
          queryPath='guestInventory/',
          zone=instance_ref.zone)
      response = holder.client.MakeRequests(
          [(holder.client.apitools_client.instances, 'GetGuestAttributes',
            request)])[0]

      for item in response.queryValue.items:
        if item.key == 'InstalledPackages' or item.key == 'PackageUpdates':
          item.value = zlib.decompress(
              base64.b64decode(item.value), zlib.MAX_WBITS | 32)

      return response.queryValue.items
    except calliope_exceptions.ToolException as e:
      if ('The resource \'guestInventory/\' of type \'Guest Attribute\' was not'
          ' found.') in str(e):
        problems = [{
            '',
            'OS inventory data was not found. Make sure the OS Config agent '
            'is running on this instance.'
        }]
        utils.RaiseException(
            problems,
            exceptions.OsInventoryNotFoundException,
            error_message='Could not fetch resource:')
      raise e

  def Run(self, args):
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    instance_ref = self._GetInstanceRef(holder, args)
    return self._GetGuestInventoryGuestAttributes(holder, instance_ref)
