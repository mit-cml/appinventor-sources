# -*- coding: utf-8 -*- #
# Copyright 2016 Google Inc. All Rights Reserved.
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
"""Command for updating access configs for virtual machine instances."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from apitools.base.py import encoding

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.calliope import base
from googlecloudsdk.calliope import exceptions
from googlecloudsdk.command_lib.compute.instances import flags
from googlecloudsdk.core import log

DETAILED_HELP = {
    'DESCRIPTION': """\
        *{command}* is used to update access configurations for network
        interfaces of Google Compute Engine virtual machines.
        """,
}


def _Args(parser, support_public_dns, support_network_tier):
  """Register parser args common to all tracks."""

  flags.INSTANCE_ARG.AddArgument(parser)
  flags.AddNetworkInterfaceArgs(parser)
  flags.AddPublicPtrArgs(parser, instance=False)
  if support_public_dns:
    flags.AddPublicDnsArgs(parser, instance=False)
  if support_network_tier:
    flags.AddNetworkTierArgs(parser, instance=False, for_update=True)


@base.ReleaseTracks(base.ReleaseTrack.GA)
class UpdateAccessConfigInstances(base.UpdateCommand):
  """Update a Google Compute Engine virtual machine access configuration."""

  _support_public_dns = False
  _support_network_tier = False

  @classmethod
  def Args(cls, parser):
    _Args(
        parser,
        support_public_dns=cls._support_public_dns,
        support_network_tier=cls._support_network_tier)

  def CreateReference(self, client, resources, args):
    return flags.INSTANCE_ARG.ResolveAsResource(
        args,
        resources,
        scope_lister=flags.GetInstanceZoneScopeLister(client))

  def GetGetRequest(self, client, instance_ref):
    return (client.apitools_client.instances,
            'Get',
            client.messages.ComputeInstancesGetRequest(**instance_ref.AsDict()))

  def GetSetRequest(self, client, args, instance_ref, replacement):
    for network_interface in replacement.networkInterfaces:
      if network_interface.name == args.network_interface:
        access_config_replacement = network_interface.accessConfigs[0]

    return (client.apitools_client.instances,
            'UpdateAccessConfig',
            client.messages.ComputeInstancesUpdateAccessConfigRequest(
                instance=instance_ref.instance,
                networkInterface=args.network_interface,
                accessConfig=access_config_replacement,
                project=instance_ref.project,
                zone=instance_ref.zone))

  def Modify(self, client, args, original):
    set_public_dns = None
    if self._support_public_dns:
      if args.public_dns is True:
        set_public_dns = True
      elif args.no_public_dns is True:
        set_public_dns = False

    set_ptr = None
    if args.public_ptr is True:
      set_ptr = True
    elif args.no_public_ptr is True:
      set_ptr = False

    modified = encoding.CopyProtoMessage(original)
    for interface in modified.networkInterfaces:
      if interface.name == args.network_interface:
        if self._support_public_dns:
          if set_public_dns is not None:
            interface.accessConfigs[0].setPublicDns = set_public_dns
            # publicDnsName is output only.
          interface.accessConfigs[0].publicDnsName = None

        if set_ptr is not None:
          interface.accessConfigs[0].setPublicPtr = set_ptr
          interface.accessConfigs[0].publicPtrDomainName = ''
          if args.public_ptr_domain is not None:
            interface.accessConfigs[
                0].publicPtrDomainName = args.public_ptr_domain

        if self._support_network_tier:
          if args.network_tier is not None:
            interface.accessConfigs[0].networkTier = (
                client.messages.AccessConfig.NetworkTierValueValuesEnum(
                    args.network_tier))

        return modified

    raise exceptions.InvalidArgumentException(
        '--network-interface',
        'The specified network interface \'{0}\' does not exist.'.format(
            args.network_interface))

  def Run(self, args):
    flags.ValidatePublicPtrFlags(args)
    if self._support_public_dns:
      flags.ValidatePublicDnsFlags(args)
    if self._support_network_tier:
      flags.ValidateNetworkTierArgs(args)
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client

    instance_ref = self.CreateReference(client, holder.resources, args)
    get_request = self.GetGetRequest(client, instance_ref)

    objects = client.MakeRequests([get_request])

    new_object = self.Modify(client, args, objects[0])

    # If existing object is equal to the proposed object or if
    # Modify() returns None, then there is no work to be done, so we
    # print the resource and return.
    if not new_object or objects[0] == new_object:
      log.status.Print(
          'No change requested; skipping update for [{0}].'.format(
              objects[0].name))
      return objects

    return client.MakeRequests(
        requests=[self.GetSetRequest(client, args, instance_ref, new_object)])


@base.ReleaseTracks(base.ReleaseTrack.BETA)
class UpdateAccessConfigInstancesBeta(UpdateAccessConfigInstances):
  """Update a Google Compute Engine virtual machine access configuration."""

  _support_public_dns = False
  _support_network_tier = False


@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class UpdateAccessConfigInstancesAlpha(UpdateAccessConfigInstances):
  """Update a Google Compute Engine virtual machine access configuration."""

  _support_public_dns = True
  _support_network_tier = True

UpdateAccessConfigInstances.detailed_help = DETAILED_HELP
