# -*- coding: utf-8 -*- #
# Copyright 2014 Google Inc. All Rights Reserved.
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
"""Command for reserving IP addresses."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.api_lib.compute import constants
from googlecloudsdk.api_lib.compute import name_generator
from googlecloudsdk.calliope import base
from googlecloudsdk.calliope import exceptions
from googlecloudsdk.command_lib.compute import flags as compute_flags
from googlecloudsdk.command_lib.compute.addresses import flags
from six.moves import zip  # pylint: disable=redefined-builtin


def _Args(cls, parser):
  """Argument parsing."""

  cls.ADDRESSES_ARG = flags.AddressArgument(required=False)
  cls.ADDRESSES_ARG.AddArgument(parser, operation_type='create')
  flags.AddDescription(parser)
  parser.display_info.AddCacheUpdater(flags.AddressesCompleter)


class Create(base.CreateCommand):
  """Reserve IP addresses.

  *{command}* is used to reserve one or more IP addresses. Once an IP address
  is reserved, it will be associated with the project until it is released
  using 'gcloud compute addresses delete'. Ephemeral IP addresses that are in
  use by resources in the project can be reserved using the '--addresses' flag.

  ## EXAMPLES
  To reserve three IP addresses in the 'us-central1' region, run:

    $ {command} ADDRESS-1 ADDRESS-2 ADDRESS-3 --region us-central1

  To reserve ephemeral IP addresses 162.222.181.198 and 23.251.146.189 which
  are being used by virtual machine instances in the 'us-central1' region, run:

    $ {command} --addresses 162.222.181.198,23.251.146.189 --region us-central1

  In the above invocation, the two addresses will be assigned random names.

  To reserve an IP address from the subnet 'default' in the 'us-central1'
  region, run:

    $ {command} SUBNET-ADDRESS-1 --region us-central1 --subnet default

  To reserve an IP range 10.110.0.0/16 from the network 'default' for
  VPC_PEERING, run:

    $ {command} IP-RANGE-1 --global --addresses 10.110.0.0 --prefix-length 16
    --purpose VPC_PEERING --network default

  To reserve any IP range with prefix length 16 from the network 'default' for
  VPC_PEERING, run:

    $ {command} IP-RANGE-1 --global --prefix-length 16 --purpose VPC_PEERING
    --network default

  """

  ADDRESSES_ARG = None
  SUBNETWORK_ARG = None
  NETWORK_ARG = None

  @classmethod
  def Args(cls, parser):
    _Args(cls, parser)
    flags.AddAddressesAndIPVersions(parser, required=False)
    flags.AddNetworkTier(parser)
    flags.AddPrefixLength(parser)
    flags.AddPurpose(parser)

    cls.SUBNETWORK_ARG = flags.SubnetworkArgument()
    cls.SUBNETWORK_ARG.AddArgument(parser)

    cls.NETWORK_ARG = flags.NetworkArgument()
    cls.NETWORK_ARG.AddArgument(parser)

  def ConstructNetworkTier(self, messages, args):
    if args.network_tier:
      network_tier = args.network_tier.upper()
      if network_tier in constants.NETWORK_TIER_CHOICES_FOR_INSTANCE:
        return messages.Address.NetworkTierValueValuesEnum(args.network_tier)
      else:
        raise exceptions.InvalidArgumentException(
            '--network-tier',
            'Invalid network tier [{tier}]'.format(tier=network_tier))
    else:
      return None

  def Run(self, args):
    """Issues requests necessary to create Addresses."""
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client

    names, addresses = self._GetNamesAndAddresses(args)
    if not args.name:
      args.name = names

    address_refs = self.ADDRESSES_ARG.ResolveAsResource(
        args, holder.resources,
        scope_lister=compute_flags.GetDefaultScopeLister(client))

    requests = []
    for address, address_ref in zip(addresses, address_refs):
      address_msg = self.GetAddress(client.messages, args, address, address_ref,
                                    holder.resources)

      if address_ref.Collection() == 'compute.globalAddresses':
        requests.append((client.apitools_client.globalAddresses, 'Insert',
                         client.messages.ComputeGlobalAddressesInsertRequest(
                             address=address_msg, project=address_ref.project)))
      elif address_ref.Collection() == 'compute.addresses':
        requests.append((client.apitools_client.addresses, 'Insert',
                         client.messages.ComputeAddressesInsertRequest(
                             address=address_msg,
                             region=address_ref.region,
                             project=address_ref.project)))

    return client.MakeRequests(requests)

  def _GetNamesAndAddresses(self, args):
    """Returns names and addresses provided in args."""
    if not args.addresses and not args.name:
      raise exceptions.ToolException(
          'At least one name or address must be provided.')

    if args.name:
      names = args.name
    else:
      # If we dont have any names then we must some addresses.
      names = [name_generator.GenerateRandomName() for _ in args.addresses]

    if args.addresses:
      addresses = args.addresses
    else:
      # If we dont have any addresses then we must some names.
      addresses = [None] * len(args.name)

    if len(addresses) != len(names):
      raise exceptions.ToolException(
          'If providing both, you must specify the same number of names as '
          'addresses.')

    return names, addresses

  def GetAddress(self, messages, args, address, address_ref, resource_parser):
    network_tier = self.ConstructNetworkTier(messages, args)

    if args.ip_version or (
        address is None and
        address_ref.Collection() == 'compute.globalAddresses'):
      ip_version = messages.Address.IpVersionValueValuesEnum(
          args.ip_version or 'IPV4')
    else:
      # IP version is only specified in global requests if an address is not
      # specified to determine whether an ipv4 or ipv6 address should be
      # allocated.
      ip_version = None

    if args.subnet and args.network:
      raise exceptions.ConflictingArgumentsException('--network', '--subnet')

    purpose = None
    if args.purpose and not args.network and not args.subnet:
      raise exceptions.MinimumArgumentException(['--network', '--subnet'],
                                                ' if --purpose is specified')

    # TODO(b/36862747): get rid of args.subnet check
    if args.subnet:
      if address_ref.Collection() == 'compute.globalAddresses':
        raise exceptions.ToolException(
            '[--subnet] may not be specified for global addresses.')
      if not args.subnet_region:
        args.subnet_region = address_ref.region
      subnetwork_url = flags.SubnetworkArgument().ResolveAsResource(
          args, resource_parser).SelfLink()
      purpose = messages.Address.PurposeValueValuesEnum(args.purpose or
                                                        'GCE_ENDPOINT')
      if purpose != messages.Address.PurposeValueValuesEnum.GCE_ENDPOINT:
        raise exceptions.InvalidArgumentException(
            '--purpose',
            'must be GCE_ENDPOINT for regional internal addresses.')
    else:
      subnetwork_url = None

    network_url = None
    if args.network:
      if address_ref.Collection() == 'compute.addresses':
        raise exceptions.InvalidArgumentException(
            '--network', 'network may not be specified for regional addresses.')
      network_url = flags.NetworkArgument().ResolveAsResource(
          args, resource_parser).SelfLink()
      purpose = messages.Address.PurposeValueValuesEnum(args.purpose or
                                                        'VPC_PEERING')
      if purpose != messages.Address.PurposeValueValuesEnum.VPC_PEERING:
        raise exceptions.InvalidArgumentException(
            '--purpose', 'must be VPC_PEERING for global internal addresses.')
      if not args.prefix_length:
        raise exceptions.RequiredArgumentException(
            '--prefix-length',
            'prefix length is needed for reserving IP ranges.')

    if args.prefix_length:
      if purpose != messages.Address.PurposeValueValuesEnum.VPC_PEERING:
        raise exceptions.InvalidArgumentException(
            '--prefix-length', 'can only be used with [--purpose VPC_PEERING].')

    return messages.Address(
        address=address,
        prefixLength=args.prefix_length,
        description=args.description,
        networkTier=network_tier,
        ipVersion=ip_version,
        name=address_ref.Name(),
        addressType=(messages.Address.AddressTypeValueValuesEnum.INTERNAL
                     if subnetwork_url or network_url else None),
        purpose=purpose,
        subnetwork=subnetwork_url,
        network=network_url)
