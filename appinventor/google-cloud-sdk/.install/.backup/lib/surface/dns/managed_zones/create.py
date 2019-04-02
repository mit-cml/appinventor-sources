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
"""gcloud dns managed-zone create command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.dns import util
from googlecloudsdk.api_lib.util import apis
from googlecloudsdk.calliope import base
from googlecloudsdk.calliope import exceptions
from googlecloudsdk.command_lib.dns import flags
from googlecloudsdk.command_lib.dns import util as command_util
from googlecloudsdk.command_lib.util.args import labels_util
from googlecloudsdk.core import log
from googlecloudsdk.core import properties
from googlecloudsdk.core import resources


def _AddArgsCommon(parser, messages):
  flags.GetDnsZoneArg(
      'The name of the managed-zone to be created.').AddToParser(parser)
  flags.GetManagedZonesDnsNameArg().AddToParser(parser)
  flags.GetManagedZonesDescriptionArg(required=True).AddToParser(parser)
  flags.AddCommonManagedZonesDnssecArgs(parser, messages)
  labels_util.AddCreateLabelsFlags(parser)


def _MakeDnssecConfig(args, messages):
  """Parse user-specified args into a DnssecConfig message."""
  dnssec_config = None
  if args.dnssec_state is not None:
    dnssec_config = command_util.ParseDnssecConfigArgs(args, messages)
  else:
    bad_args = ['denial_of_existence', 'ksk_algorithm', 'zsk_algorithm',
                'ksk_key_length', 'zsk_key_length']
    for bad_arg in bad_args:
      if getattr(args, bad_arg, None) is not None:
        raise exceptions.InvalidArgumentException(
            bad_arg,
            'DNSSEC must be enabled in order to use other DNSSEC arguments. '
            'Please set --dnssec-state to "on" or "transfer".')
  return dnssec_config


@base.ReleaseTracks(base.ReleaseTrack.GA)
class Create(base.CreateCommand):
  """Create a Cloud DNS managed-zone.

  This command creates a Cloud DNS managed-zone.

  ## EXAMPLES

  To create a managed-zone, run:

    $ {command} my_zone --dns-name my.zone.com. --description "My zone!"
  """

  @staticmethod
  def Args(parser):
    messages = apis.GetMessagesModule('dns', 'v1')
    _AddArgsCommon(parser, messages)
    parser.display_info.AddCacheUpdater(flags.ManagedZoneCompleter)

  def Run(self, args):
    dns = apis.GetClientInstance('dns', 'v1')
    messages = apis.GetMessagesModule('dns', 'v1')

    zone_ref = resources.REGISTRY.Parse(
        args.dns_zone,
        params={
            'project': properties.VALUES.core.project.GetOrFail,
        },
        collection='dns.managedZones')

    dnssec_config = _MakeDnssecConfig(args, messages)

    labels = labels_util.ParseCreateArgs(args, messages.ManagedZone.LabelsValue)

    zone = messages.ManagedZone(name=zone_ref.managedZone,
                                dnsName=util.AppendTrailingDot(args.dns_name),
                                description=args.description,
                                dnssecConfig=dnssec_config,
                                labels=labels)

    result = dns.managedZones.Create(
        messages.DnsManagedZonesCreateRequest(managedZone=zone,
                                              project=zone_ref.project))
    log.CreatedResource(zone_ref)
    return [result]


@base.ReleaseTracks(base.ReleaseTrack.BETA)
class CreateBeta(base.CreateCommand):
  r"""Create a Cloud DNS managed-zone.

  This command creates a Cloud DNS managed-zone.

  ## EXAMPLES

  To create a managed-zone, run:

    $ {command} my_zone --dns-name my.zone.com. --description "My zone!"

  To create a managed-zone with DNSSEC, run:

    $ {command} my_zone_2 --description "Signed Zone" \
        --dns-name myzone.example \
        --dnssec-state=on
  """

  @staticmethod
  def Args(parser):
    messages = apis.GetMessagesModule('dns', 'v1beta2')
    _AddArgsCommon(parser, messages)
    flags.GetManagedZoneNetworksArg().AddToParser(parser)
    flags.GetManagedZoneVisibilityArg().AddToParser(parser)
    parser.display_info.AddCacheUpdater(flags.ManagedZoneCompleter)
    flags.GetForwardingTargetsArg().AddToParser(parser)

  def Run(self, args):
    # We explicitly want to allow --networks='' as a valid option and we need
    # to differentiate between that option and not passing --networks at all.
    if args.visibility == 'public':
      if args.IsSpecified('networks'):
        raise exceptions.InvalidArgumentException(
            '--networks',
            'If --visibility is set to public (default), setting networks is '
            'not allowed.')
      if args.IsSpecified('forwarding_targets'):
        raise exceptions.InvalidArgumentException(
            '--forwarding-targets',
            'If --visibility is set to public, setting --forwarding-targets is '
            'not allowed.')
    if args.visibility == 'private' and args.networks is None:
      raise exceptions.RequiredArgumentException(
          '--networks', ("""\
           If --visibility is set to private, a list of networks must be
           provided.'
         NOTE: You can provide an empty value ("") for private zones that
          have NO network binding.
          """))

    dns = apis.GetClientInstance('dns', 'v1beta2')
    messages = apis.GetMessagesModule('dns', 'v1beta2')
    registry = util.GetRegistry('v1beta2')

    zone_ref = registry.Parse(
        args.dns_zone,
        params={'project': properties.VALUES.core.project.GetOrFail},
        collection='dns.managedZones')

    visibility = messages.ManagedZone.VisibilityValueValuesEnum(args.visibility)
    visibility_config = None
    if visibility == messages.ManagedZone.VisibilityValueValuesEnum.private:
      # Handle explicitly empty networks case (--networks='')
      networks = args.networks if args.networks != [''] else []
      network_urls = [
          registry.Parse(
              n,
              collection='compute.networks',
              params={'project': zone_ref.project}).SelfLink()
          for n in networks]
      network_configs = [
          messages.ManagedZonePrivateVisibilityConfigNetwork(
              networkUrl=nurl)
          for nurl in network_urls]
      visibility_config = messages.ManagedZonePrivateVisibilityConfig(
          networks=network_configs)

    if args.forwarding_targets:
      forward_config = command_util.ParseManagedZoneForwardingConfig(
          args.forwarding_targets, messages)
    else:
      forward_config = None

    dnssec_config = _MakeDnssecConfig(args, messages)
    labels = labels_util.ParseCreateArgs(args, messages.ManagedZone.LabelsValue)
    zone = messages.ManagedZone(
        name=zone_ref.managedZone,
        dnsName=util.AppendTrailingDot(args.dns_name),
        description=args.description,
        dnssecConfig=dnssec_config,
        labels=labels,
        visibility=visibility,
        forwardingConfig=forward_config,
        privateVisibilityConfig=visibility_config)

    result = dns.managedZones.Create(
        messages.DnsManagedZonesCreateRequest(managedZone=zone,
                                              project=zone_ref.project))
    log.CreatedResource(zone_ref)
    return [result]
