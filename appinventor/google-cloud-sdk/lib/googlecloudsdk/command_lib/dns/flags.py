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

"""Common flags for some of the DNS commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.calliope import base
from googlecloudsdk.calliope.concepts import concepts
from googlecloudsdk.command_lib.util import completers
from googlecloudsdk.command_lib.util.apis import arg_utils
from googlecloudsdk.command_lib.util.concepts import concept_parsers


class BetaKeyCompleter(completers.ListCommandCompleter):

  def __init__(self, **kwargs):
    super(BetaKeyCompleter, self).__init__(
        collection='dns.dnsKeys',
        api_version='v1beta2',
        list_command=('beta dns dns-keys list --format=value(keyTag)'),
        parse_output=True,
        flags=['zone'],
        **kwargs)


class KeyCompleter(completers.ListCommandCompleter):

  def __init__(self, **kwargs):
    super(KeyCompleter, self).__init__(
        collection='dns.dnsKeys',
        api_version='v1',
        list_command=('dns dns-keys list --format=value(keyTag)'),
        parse_output=True,
        flags=['zone'],
        **kwargs)


class ManagedZoneCompleter(completers.ListCommandCompleter):

  def __init__(self, **kwargs):
    super(ManagedZoneCompleter, self).__init__(
        collection='dns.managedZones',
        list_command='dns managed-zones list --uri',
        **kwargs)


def GetKeyArg(help_text='The DNS key identifier.', is_beta=False):
  return base.Argument(
      'key_id',
      metavar='KEY-ID',
      completer=BetaKeyCompleter if is_beta else KeyCompleter,
      help=help_text)


def GetDnsZoneArg(help_text):
  return base.Argument(
      'dns_zone', metavar='ZONE_NAME',
      completer=ManagedZoneCompleter,
      help=help_text)


def ZoneAttributeConfig():
  return concepts.ResourceParameterAttributeConfig(
      name='zone',
      help_text='The Cloud DNS zone for the {resource}.')


def GetZoneResourceSpec():
  return concepts.ResourceSpec(
      'dns.managedZones',
      resource_name='zone',
      managedZone=ZoneAttributeConfig(),
      project=concepts.DEFAULT_PROJECT_ATTRIBUTE_CONFIG,
      disable_auto_completers=False)


def GetZoneResourceArg(help_text, positional=True, plural=False):
  arg_name = 'zones' if plural else 'zone'
  return concept_parsers.ConceptParser.ForResource(
      arg_name if positional else '--{}'.format(arg_name),
      GetZoneResourceSpec(),
      help_text,
      plural=plural,
      required=True)


def GetZoneArg(help_text=(
    'Name of the managed-zone whose record-sets you want to manage.'),
               hide_short_zone_flag=False):
  if hide_short_zone_flag:
    zone_group = base.ArgumentGroup(required=True)
    zone_group.AddArgument(
        base.Argument(
            '--zone',
            completer=ManagedZoneCompleter,
            help=help_text))
    zone_group.AddArgument(
        base.Argument(
            '-z',
            dest='zone',
            completer=ManagedZoneCompleter,
            help=help_text,
            hidden=True))
    return zone_group
  else:
    return base.Argument(
        '--zone',
        '-z',
        completer=ManagedZoneCompleter,
        help=help_text,
        required=True)


def GetManagedZonesDnsNameArg():
  return base.Argument(
      '--dns-name',
      required=True,
      help='The DNS name suffix that will be managed with the created zone.')


def GetManagedZonesDescriptionArg(required=False):
  return base.Argument(
      '--description',
      required=required,
      help='Short description for the managed-zone.')


def GetDnsSecStateFlagMapper(messages):
  return arg_utils.ChoiceEnumMapper(
      '--dnssec-state', messages.ManagedZoneDnsSecConfig.StateValueValuesEnum,
      custom_mappings={
          'off': ('off', 'Disable DNSSEC for the managed zone.'),
          'on': ('on', 'Enable DNSSEC for the managed zone.'),
          'transfer': ('transfer', ('Enable DNSSEC and allow '
                                    'transferring a signed zone in '
                                    'or out.'))
      },
      help_str='The DNSSEC state for this managed zone.')


def GetDoeFlagMapper(messages):
  return arg_utils.ChoiceEnumMapper(
      '--denial-of-existence',
      messages.ManagedZoneDnsSecConfig.NonExistenceValueValuesEnum,
      help_str='Requires DNSSEC enabled.')


def GetKeyAlgorithmFlag(key_type, messages):
  return arg_utils.ChoiceEnumMapper(
      '--{}-algorithm'.format(key_type),
      messages.DnsKeySpec.AlgorithmValueValuesEnum,
      help_str='String mnemonic specifying the DNSSEC algorithm of the '
               'key-signing key. Requires DNSSEC enabled')


def AddCommonManagedZonesDnssecArgs(parser, messages):
  """Add Common DNSSEC flags for the managed-zones group."""
  GetDnsSecStateFlagMapper(messages).choice_arg.AddToParser(parser)
  GetDoeFlagMapper(messages).choice_arg.AddToParser(parser)
  GetKeyAlgorithmFlag('ksk', messages).choice_arg.AddToParser(parser)
  GetKeyAlgorithmFlag('zsk', messages).choice_arg.AddToParser(parser)
  parser.add_argument(
      '--ksk-key-length',
      type=int,
      help='Length of the key-signing key in bits. Requires DNSSEC enabled.')
  parser.add_argument(
      '--zsk-key-length',
      type=int,
      help='Length of the zone-signing key in bits. Requires DNSSEC enabled.')


def GetManagedZoneVisibilityArg():
  return base.Argument(
      '--visibility',
      choices=['public', 'private'],
      default='public',
      help='Visibility of the zone. Public zones are visible to the public '
      'internet. Private zones are only visible in your internal '
      'networks denoted by the `--networks` flag.')


def GetManagedZoneNetworksArg():
  return base.Argument(
      '--networks',
      metavar='NETWORK',
      type=arg_parsers.ArgList(),
      help='List of networks that the zone should be visible in if the zone '
      'visibility is [private].')


def GetForwardingTargetsArg():
  return base.Argument(
      '--forwarding-targets',
      type=arg_parsers.ArgList(),
      required=False,
      metavar='IP_ADDRESSES',
      help=('List of IPv4 addresses of target name servers that the zone '
            'will forward queries to. Ignored for `public` visibility.'))


# Policy Flags
def GetPolicyDescriptionArg():
  return base.Argument(
      '--description', required=False, help='A description of the policy.')


def GetPolicyNetworksArg():
  return base.Argument(
      '--networks',
      type=arg_parsers.ArgList(),
      required=False,
      metavar='NETWORKS',
      help=('The comma separated list of network names to associate with '
            'the policy.'))


def GetPolicyInbboundForwardingArg():
  return base.Argument(
      '--enable-inbound-forwarding',
      required=False,
      default=False,
      action='store_true',
      help=('Specifies whether to allow networks bound to this policy to '
            'receive DNS queries sent by VMs or applications over VPN '
            'connections. Defaults to False.'))


def GetPolicyAltNameServersnArg():
  return base.Argument(
      '--alternative-name-servers',
      type=arg_parsers.ArgList(),
      required=False,
      metavar='NAME_SERVERS',
      help=('List of alternative name servers to forward to. Must be a '
            'comma separated list of IPv4 addresses.'))

CHANGES_FORMAT = 'table(id, startTime, status)'
