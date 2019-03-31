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
"""Command for update to instance network interfaces."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import alias_ip_range_utils
from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.api_lib.compute.operations import poller
from googlecloudsdk.api_lib.util import waiter
from googlecloudsdk.calliope import base
from googlecloudsdk.calliope import exceptions
from googlecloudsdk.command_lib.compute import flags
from googlecloudsdk.command_lib.compute.instances import flags as instances_flags


class Update(base.UpdateCommand):
  r"""Update a Google Compute Engine virtual machine network interface.

  *{command}* updates network interfaces of a Google Compute Engine
  virtual machine. For example:

    $ {command} example-instance --zone us-central1-a --aliases r1:172.16.0.1/32

  sets 172.16.0.1/32 from range r1 of the default interface's subnetwork
  as the interface's alias IP.
  """

  @staticmethod
  def Args(parser):
    instances_flags.INSTANCE_ARG.AddArgument(parser)
    parser.add_argument(
        '--network-interface',
        default='nic0',
        help='The name of the network interface to update.')
    parser.add_argument(
        '--aliases',
        type=str,
        help="""
        The IP alias ranges to allocate for this interface. If there are
        multiple IP alias ranges, they are separated by semicolons.

        For example:

            --aliases="10.128.1.0/24;r1:/32"
        """)

  def Run(self, args):
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client.apitools_client
    messages = holder.client.messages

    instance_ref = instances_flags.INSTANCE_ARG.ResolveAsResource(
        args, holder.resources,
        scope_lister=flags.GetDefaultScopeLister(holder.client))

    instance = client.instances.Get(
        messages.ComputeInstancesGetRequest(**instance_ref.AsDict()))
    for i in instance.networkInterfaces:
      if i.name == args.network_interface:
        fingerprint = i.fingerprint
        break
    else:
      raise exceptions.UnknownArgumentException(
          'network-interface',
          'Instance does not have a network interface [{}], '
          'present interfaces are [{}].'.format(
              args.network_interface, ', '.join(
                  [i.name for i in instance.networkInterfaces])))

    patch_network_interface = messages.NetworkInterface(
        aliasIpRanges=(
            alias_ip_range_utils.CreateAliasIpRangeMessagesFromString(
                messages, True, args.aliases)),
        fingerprint=fingerprint)

    request = messages.ComputeInstancesUpdateNetworkInterfaceRequest(
        project=instance_ref.project,
        instance=instance_ref.instance,
        zone=instance_ref.zone,
        networkInterface=args.network_interface,
        networkInterfaceResource=patch_network_interface)

    cleared_fields = []
    if not patch_network_interface.aliasIpRanges:
      cleared_fields.append('aliasIpRanges')
    with client.IncludeFields(cleared_fields):
      operation = client.instances.UpdateNetworkInterface(request)
    operation_ref = holder.resources.Parse(
        operation.selfLink, collection='compute.zoneOperations')

    operation_poller = poller.Poller(client.instances)
    return waiter.WaitFor(
        operation_poller, operation_ref,
        'Updating network interface [{0}] of instance [{1}]'.format(
            args.network_interface, instance_ref.Name()))
