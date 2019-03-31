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
"""Flags and helpers for the compute routers commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import utils
from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.command_lib.compute import completers as compute_completers
from googlecloudsdk.command_lib.compute import flags as compute_flags

DEFAULT_LIST_FORMAT = """\
    table(
      name,
      region.basename(),
      network.basename()
    )"""

_MODE_CHOICES = {
    'DEFAULT': 'Default (Google-managed) BGP advertisements.',
    'CUSTOM': 'Custom (user-configured) BGP advertisements.',
}

_GROUP_CHOICES = {
    'ALL_SUBNETS': 'Automatically advertise all available subnets.',
}

_BFD_MODE_CHOICES = {
    'ACTIVE':
        'The Cloud Router will initiate the BFD session for this BGP peer.',
    'PASSIVE':
        'The Cloud Router will wait for the peer router to initiate the BFD '
        'session for this BGP peer.',
    'DISABLED':
        'BFD is disabled for this BGP peer.',
}

_BFD_PACKET_MODE_CHOICES = {
    'CONTROL_AND_ECHO':
        'BFD echo mode is enabled for this BGP peer. If the peer router also '
        'has BFD echo mode enabled, BFD echo packets will be sent to the other'
        ' router. If the peer router does not have BFD echo mode enabled, only'
        ' control packets will be sent.',
    'CONTROL_ONLY':
        'BFD echo mode is disabled for this BGP peer. If this router and the '
        'peer router have a multi-hop connection, BFD_PACKET_MODE should be '
        'set to CONTROL_ONLY as BFD echo mode is only supported on single hop '
        'connections.',
}


class RoutersCompleter(compute_completers.ListCommandCompleter):

  def __init__(self, **kwargs):
    super(RoutersCompleter, self).__init__(
        collection='compute.routers',
        list_command='compute routers list --uri',
        **kwargs)


def RouterArgument(required=True, plural=False):
  return compute_flags.ResourceArgument(
      resource_name='router',
      completer=RoutersCompleter,
      plural=plural,
      required=required,
      regional_collection='compute.routers',
      region_explanation=compute_flags.REGION_PROPERTY_EXPLANATION)


def RouterArgumentForVpnTunnel(required=True):
  return compute_flags.ResourceArgument(
      resource_name='router',
      name='--router',
      completer=RoutersCompleter,
      plural=False,
      required=required,
      regional_collection='compute.routers',
      short_help='The Router to use for dynamic routing.',
      region_explanation=compute_flags.REGION_PROPERTY_EXPLANATION)


def RouterArgumentForOtherResources(required=True, suppress_region=True):
  region_explanation = ('Should be the same as --region, if not specified, '
                        'it will be inherited from --region.')
  return compute_flags.ResourceArgument(
      resource_name='router',
      name='--router',
      completer=RoutersCompleter,
      plural=False,
      required=required,
      regional_collection='compute.routers',
      short_help='The Google Cloud Router to use for dynamic routing.',
      region_explanation=region_explanation,
      region_hidden=suppress_region)


def RouterArgumentForNat():
  return compute_flags.ResourceArgument(
      resource_name='router',
      name='--router',
      completer=RoutersCompleter,
      plural=False,
      required=True,
      regional_collection='compute.routers',
      short_help='The Router to use for NAT.',
      region_hidden=True)


def AddCreateRouterArgs(parser):
  """Adds common arguments for creating routers."""

  parser.add_argument(
      '--description', help='An optional description of this router.')

  parser.add_argument(
      '--asn',
      required=False,
      type=int,
      help='The optional BGP autonomous system number (ASN) for this router. '
      'Must be a 16-bit or 32-bit private ASN as defined in '
      'https://tools.ietf.org/html/rfc6996, for example `--asn=64512`.')


def AddKeepaliveIntervalArg(parser):
  """Adds keepalive interval argument for routers."""
  parser.add_argument(
      '--keepalive-interval',
      type=arg_parsers.Duration(
          default_unit='s', lower_bound='1s', upper_bound='120s'),
      hidden=True,
      help='The interval between BGP keepalive messages that are sent to the '
      'peer. If set, this value must be between 1 and 120 seconds. The default '
      'is 20 seconds. See $ gcloud topic datetimes for information on duration '
      'formats.\n\n'
      'BGP systems exchange keepalive messages to determine whether a link or '
      'host has failed or is no longer available. Hold time is the length of '
      'time in seconds that the BGP session is considered operational without '
      'any activity. After the hold time expires, the session is dropped.\n\n'
      'Hold time is three times the interval at which keepalive messages are '
      'sent, and the hold time is the maximum number of seconds allowed to '
      'elapse between successive keepalive messages that BGP receives from a '
      'peer. BGP will use the smaller of either the local hold time value or '
      'the peer\'s  hold time value as the hold time for the BGP connection '
      'between the two peers.')


def AddInterfaceArgs(parser, for_update=False):
  """Adds common arguments for routers add-interface or update-interface."""

  operation = 'added'
  if for_update:
    operation = 'updated'

  parser.add_argument(
      '--interface-name',
      required=True,
      help='The name of the interface being {0}.'.format(operation))

  parser.add_argument(
      '--ip-address',
      type=utils.IPV4Argument,
      help='The link local address of the router for this interface.')

  parser.add_argument(
      '--mask-length',
      type=arg_parsers.BoundedInt(lower_bound=0, upper_bound=31),
      help='The subnet mask for the link-local IP range of the interface. '
      'The interface IP address and BGP peer IP address must be selected from '
      'the subnet defined by this link-local range.')


def AddBgpPeerArgs(parser,
                   for_add_bgp_peer=False,
                   support_bfd=False,
                   support_enable=False,
                   is_update=False):
  """Adds common arguments for managing BGP peers."""

  operation = 'updated'
  if for_add_bgp_peer:
    operation = 'added'

  parser.add_argument(
      '--peer-name',
      required=True,
      help='The name of the new BGP peer being {0}.'.format(operation))

  parser.add_argument(
      '--interface',
      required=for_add_bgp_peer,
      help='The name of the interface for this BGP peer.')

  parser.add_argument(
      '--peer-asn',
      required=for_add_bgp_peer,
      type=int,
      help='The BGP autonomous system number (ASN) for this BGP peer. '
      'Must be a 16-bit or 32-bit private ASN as defined in '
      'https://tools.ietf.org/html/rfc6996, for example `--asn=64512`.')

  # For add_bgp_peer, we only require the interface and infer the IP instead.
  if not for_add_bgp_peer:
    parser.add_argument(
        '--ip-address',
        type=utils.IPV4Argument,
        help='The link-local address of the Cloud Router interface for this '
        'BGP peer. Must be a link-local IPv4 address belonging to the range '
        '169.254.0.0/16 and must belong to same subnet as the interface '
        'address of the peer router.')

  parser.add_argument(
      '--peer-ip-address',
      type=utils.IPV4Argument,
      help='The link-local address of the peer router. Must be a link-local '
      'IPv4 address belonging to the range 169.254.0.0/16.')

  parser.add_argument(
      '--advertised-route-priority',
      type=arg_parsers.BoundedInt(lower_bound=0, upper_bound=65535),
      help='The priority of routes advertised to this BGP peer. In the case '
      'where there is more than one matching route of maximum length, '
      'the routes with lowest priority value win. 0 <= priority <= '
      '65535. If not specified, will use Google-managed priorities.')

  if support_bfd:
    bfd_group_help = (
        'Arguments to {0} BFD (Bidirectional Forwarding Detection) '
        'settings:'.format('update' if is_update else 'configure'))
    bfd_group = parser.add_group(help=bfd_group_help,
                                 hidden=True)
    bfd_group.add_argument(
        '--bfd-mode',
        choices=_BFD_MODE_CHOICES,
        type=lambda mode: mode.upper(),
        metavar='BFD_MODE',
        hidden=True,
        help='The BFD session initiation mode for this BGP peer. Must be one '
        'of:\n\n'
        'ACTIVE - The Cloud Router will initiate the BFD session for this BGP '
        'peer.\n\n'
        'PASSIVE - The Cloud Router will wait for the peer router to initiate '
        'the BFD session for this BGP peer.\n\n'
        'DISABLED - BFD is disabled for this BGP peer.')

    bfd_group.add_argument(
        '--bfd-min-transmit-interval',
        type=arg_parsers.Duration(
            default_unit='ms',
            lower_bound='33ms',
            upper_bound='30000ms',
            parsed_unit='ms'),
        hidden=True,
        help='The minimum transmit interval between BFD packets. If BFD echo '
        'mode is enabled on both this router and the peer router this sets the '
        'minimum transmit interval of BFD echo packets. Otherwise, this sets '
        'the minimum transmit interval of BFD control packets. The default is '
        '300 milliseconds. See $ gcloud topic datetimes for information on '
        'duration formats.')
    bfd_group.add_argument(
        '--bfd-min-receive-interval',
        type=arg_parsers.Duration(
            default_unit='ms',
            lower_bound='33ms',
            upper_bound='30000ms',
            parsed_unit='ms'),
        hidden=True,
        help='The minimum receive interval between BFD packets. If BFD echo '
        'mode is enabled on both this router and the peer router this sets the '
        'minimum receive interval of BFD echo packets. Otherwise, this sets '
        'the minimum receive interval of BFD control packets. The default is '
        '300 milliseconds. See $ gcloud topic datetimes for information on '
        'duration formats.')
    bfd_group.add_argument(
        '--bfd-multiplier',
        type=int,
        hidden=True,
        help='The number of consecutive BFD control packets that must be '
        'missed before BFD declares that a peer is unavailable.')
    bfd_group.add_argument(
        '--bfd-packet-mode',
        choices=_BFD_PACKET_MODE_CHOICES,
        type=lambda mode: mode.upper(),
        metavar='BFD_PACKET_MODE',
        hidden=True,
        help='The BGP packet mode for this BGP peer. Must be one of:\n\n'
        'CONTROL_AND_ECHO - BFD echo mode is enabled for this BGP peer. If the '
        'peer router also has BFD echo mode enabled, BFD echo packets will be '
        'sent to the other router. If the peer router does not have BFD echo '
        'mode enabled, only control packets will be sent.\n\n'
        'CONTROL_ONLY - BFD echo mode is disabled for this BGP peer. If this '
        'router and the peer router have a multi-hop connection, '
        'BFD_PACKET_MODE should be set to CONTROL_ONLY as BFD echo mode is '
        'only supported on single hop connections.')
    bfd_group.add_argument(
        '--bfd-slow-timer-interval',
        type=arg_parsers.Duration(
            default_unit='ms',
            lower_bound='1000ms',
            upper_bound='30000ms',
            parsed_unit='ms'),
        hidden=True,
        help='The transmit and receive interval between BFD control packets in '
        'milliseconds if echo mode is enabled on both this router and the peer '
        'router. The default is 5000 milliseconds. See $ gcloud topic '
        'datetimes for information on duration formats.')
    enabled_display_help = (
        'If enabled, the peer connection can be established with routing '
        'information. If disabled, any active session with the peer is '
        'terminated and all associated routing information is removed.')
  if support_enable:
    if not is_update:
      enabled_display_help += ' Enabled by default.'
    parser.add_argument(
        '--enabled',
        hidden=True,
        action=arg_parsers.StoreTrueFalseAction,
        help=enabled_display_help)


def AddUpdateCustomAdvertisementArgs(parser, resource_str):
  """Adds common arguments for setting/updating custom advertisements."""

  AddReplaceCustomAdvertisementArgs(parser, resource_str)
  AddIncrementalCustomAdvertisementArgs(parser, resource_str)


def AddReplaceCustomAdvertisementArgs(parser, resource_str):
  """Adds common arguments for replacing custom advertisements."""

  parser.add_argument(
      '--advertisement-mode',
      choices=_MODE_CHOICES,
      type=lambda mode: mode.upper(),
      metavar='MODE',
      help="""The new advertisement mode for this {0}.""".format(resource_str))

  parser.add_argument(
      '--set-advertisement-groups',
      type=arg_parsers.ArgList(
          choices=_GROUP_CHOICES, element_type=lambda group: group.upper()),
      metavar='GROUP',
      help="""The list of pre-defined groups of IP ranges to dynamically
              advertise on this {0}. This list can only be specified in
              custom advertisement mode.""".format(resource_str))

  parser.add_argument(
      '--set-advertisement-ranges',
      type=arg_parsers.ArgDict(allow_key_only=True),
      metavar='CIDR_RANGE=DESC',
      help="""The list of individual IP ranges, in CIDR format, to dynamically
              advertise on this {0}. Each IP range can (optionally) be given a
              text description DESC. For example, to advertise a specific range,
              use `--set-advertisement-ranges=192.168.10.0/24`.  To store a
              description with the range, use
              `--set-advertisement-ranges=192.168.10.0/24=my-networks`. This
              list can only be specified in custom advertisement mode."""
      .format(resource_str))


def AddIncrementalCustomAdvertisementArgs(parser, resource_str):
  """Adds common arguments for incrementally updating custom advertisements."""

  incremental_args = parser.add_mutually_exclusive_group(required=False)

  incremental_args.add_argument(
      '--add-advertisement-groups',
      type=arg_parsers.ArgList(
          choices=_GROUP_CHOICES, element_type=lambda group: group.upper()),
      metavar='GROUP',
      help="""A list of pre-defined groups of IP ranges to dynamically advertise
              on this {0}. This list is appended to any existing advertisements.
              This field can only be specified in custom advertisement mode."""
      .format(resource_str))

  incremental_args.add_argument(
      '--remove-advertisement-groups',
      type=arg_parsers.ArgList(
          choices=_GROUP_CHOICES, element_type=lambda group: group.upper()),
      metavar='GROUP',
      help="""A list of pre-defined groups of IP ranges to remove from dynamic
              advertisement on this {0}. Each group in the list must exist in
              the current set of custom advertisements. This field can only be
              specified in custom advertisement mode.""".format(resource_str))

  incremental_args.add_argument(
      '--add-advertisement-ranges',
      type=arg_parsers.ArgDict(allow_key_only=True),
      metavar='CIDR_RANGE=DESC',
      help="""A list of individual IP ranges, in CIDR format, to dynamically
              advertise on this {0}. This list is appended to any existing
              advertisements. Each IP range can (optionally) be given a text
              description DESC. For example, to advertise a specific range, use
              `--advertisement-ranges=192.168.10.0/24`.  To store a description
              with the range, use
              `--advertisement-ranges=192.168.10.0/24=my-networks`. This list
              can only be specified in custom advertisement mode.""".format(
                  resource_str))

  incremental_args.add_argument(
      '--remove-advertisement-ranges',
      type=arg_parsers.ArgList(),
      metavar='CIDR_RANGE',
      help="""A list of individual IP ranges, in CIDR format, to remove from
              dynamic advertisement on this {0}. Each IP range in the list must
              exist in the current set of custom advertisements. This field can
              only be specified in custom advertisement mode.""".format(
                  resource_str))
