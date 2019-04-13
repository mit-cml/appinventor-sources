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
"""Flags and helpers for the compute interconnects commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from collections import OrderedDict

from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.compute import completers as compute_completers
from googlecloudsdk.command_lib.compute import flags as compute_flags


_BANDWIDTH_CHOICES = OrderedDict([
    ('50m', '50 Mbit/s'),
    ('100m', '100 Mbit/s'),
    ('200m', '200 Mbit/s'),
    ('300m', '300 Mbit/s'),
    ('400m', '400 Mbit/s'),
    ('500m', '500 Mbit/s'),
    ('1g', '1 Gbit/s'),
    ('2g', '2 Gbit/s'),
    ('5g', '5 Gbit/s'),
    ('10g', '10 Gbit/s'),
    ('bps-50m', '50 Mbit/s'),
    ('bps-100m', '100 Mbit/s'),
    ('bps-200m', '200 Mbit/s'),
    ('bps-300m', '300 Mbit/s'),
    ('bps-400m', '400 Mbit/s'),
    ('bps-500m', '500 Mbit/s'),
    ('bps-1g', '1 Gbit/s'),
    ('bps-2g', '2 Gbit/s'),
    ('bps-5g', '5 Gbit/s'),
    ('bps-10g', '10 Gbit/s'),
])

_EDGE_AVAILABILITY_DOMAIN_CHOICES = {
    'availability-domain-1': 'Edge Availability Domain 1',
    'availability-domain-2': 'Edge Availability Domain 2',
    'any': 'Any Availability Domain',
}


class InterconnectAttachmentsCompleter(compute_completers.ListCommandCompleter):

  def __init__(self, **kwargs):
    super(InterconnectAttachmentsCompleter, self).__init__(
        collection='compute.interconnectAttachments',
        list_command='alpha compute interconnects attachments list --uri',
        **kwargs)


def InterconnectAttachmentArgument(required=True, plural=False):
  return compute_flags.ResourceArgument(
      resource_name='interconnect attachment',
      completer=InterconnectAttachmentsCompleter,
      plural=plural,
      required=required,
      regional_collection='compute.interconnectAttachments',
      region_explanation=compute_flags.REGION_PROPERTY_EXPLANATION)


def InterconnectAttachmentArgumentForRouter(required=False,
                                            plural=False,
                                            operation_type='added'):
  resource_name = 'interconnectAttachment{0}'.format('s' if plural else '')
  return compute_flags.ResourceArgument(
      resource_name=resource_name,
      name='--interconnect-attachment',
      completer=InterconnectAttachmentsCompleter,
      plural=plural,
      required=required,
      regional_collection='compute.interconnectAttachments',
      short_help='The interconnect attachment of the interface being {0}.'
      .format(operation_type),
      region_explanation='If not specified it will be set to the region of '
      'the router.')


def AddAdminEnabled(parser, default_behavior=True, update=False):
  """Adds adminEnabled flag to the argparse.ArgumentParser."""
  group = parser.add_group(mutex=True,
                           required=False,
                           help='')
  if update:
    # Update command
    help_text = """\
      Administrative status of the interconnect attachment.
      When this is enabled, the attachment is operational and will carry
      traffic. Use --no-enable-admin to disable it.
      """
  elif default_behavior:
    # Create command for dedicated attachments, backend default behavior is to
    # enable if not specified.
    help_text = """\
      Administrative status of the interconnect attachment. If not provided
      on creation, defaults to enabled.
      When this is enabled, the attachment is operational and will carry
      traffic. Use --no-enable-admin to disable it.
      """
  else:
    # Create command for partner attachments, backend default behavior is to
    # disabled if not specified.
    help_text = """\
      Administrative status of the interconnect attachment. If not provided
      on creation, defaults to disabled.
      When this is enabled, the attachment is operational and will carry
      traffic. Use --no-enable-admin to disable it.
      """

  group.add_argument(
      '--admin-enabled',
      hidden=True,
      default=None,
      action='store_true',
      help='(DEPRECATED) Use --enable-admin instead.')

  group.add_argument(
      '--enable-admin', action='store_true', default=None, help=help_text)


def AddBandwidth(parser, required):
  """Adds bandwidth flag to the argparse.ArgumentParser."""
  help_text = """\
      Provisioned capacity of the attachment.
      """
  base.ChoiceArgument(
      '--bandwidth',
      # TODO(b/80311900): use arg_parsers.BinarySize()
      # and deprecate the proto enum names
      choices=_BANDWIDTH_CHOICES,
      required=required,
      help_str=help_text).AddToParser(parser)


def AddVlan(parser):
  """Adds vlan flag to the argparse.ArgumentParser."""
  parser.add_argument(
      '--vlan',
      type=int,
      help="""\
      Desired VLAN for this attachment, in the range 2-4094. If not supplied,
      Google will automatically select a VLAN.
      """)


def AddPartnerAsn(parser):
  """Adds partner asn flag to the argparse.ArgumentParser."""
  parser.add_argument(
      '--partner-asn',
      type=int,
      help="""\
      BGP ASN of the partner. This should only be supplied by layer 3
      providers that have configured BGP on behalf of the customer.
      """)


def AddPartnerMetadata(parser, required=True):
  """Adds partner metadata flags to the argparse.ArgumentParser."""
  group = parser.add_group(mutex=False,
                           required=required,
                           help='Partner metadata.')
  group.add_argument(
      '--partner-name',
      required=required,
      help="""\
      Plain text name of the Partner providing this attachment. This value
      may be validated to match approved Partner values.
      """)
  group.add_argument(
      '--partner-interconnect-name',
      required=required,
      help="""\
      Plain text name of the Interconnect this attachment is connected to,
      as displayed in the Partner's portal. For instance "Chicago 1".
      """)
  group.add_argument(
      '--partner-portal-url',
      required=required,
      help="""\
      URL of the Partner's portal for this Attachment. The Partner may wish
      to customize this to be a deep-link to the specific resource on the
      Partner portal. This value may be validated to match approved Partner
      values.
      """)


def AddPairingKey(parser):
  """Adds pairing key flag to the argparse.ArgumentParser."""
  parser.add_argument(
      '--pairing-key',
      required=True,
      help="""\
      Value of the pairing-key from the target partner attachment provided by
      the customer.
      """)


def AddEdgeAvailabilityDomain(parser):
  """Adds edge-availability-domain flag to the argparse.ArgumentParser."""
  parser.add_argument(
      '--edge-availability-domain',
      choices=_EDGE_AVAILABILITY_DOMAIN_CHOICES,
      required=True,
      metavar='AVAILABILITY_DOMAIN',
      help="""\
      Desired edge availability domain for this attachment:
      `availability-domain-1`, `availability-domain-2`, `any`.

      In each metro where the Partner can connect to Google, there are two sets
      of redundant hardware. These sets are described as edge availability
      domain 1 and 2. Within a metro, Google will only schedule maintenance in
      one availability domain at a time. This guarantee does not apply to
      availability domains outside the metro; Google may perform maintenance in
      (say) New York availability domain 1 at the same time as Chicago
      availability domain 1.
      """)


def AddDescription(parser):
  """Adds description flag to the argparse.ArgumentParser."""
  parser.add_argument(
      '--description',
      help='Human-readable plain-text description of attachment.')


def AddCandidateSubnets(parser):
  """Adds candidate subnets flag to the argparse.ArgumetnParser."""
  parser.add_argument(
      '--candidate-subnets',
      type=arg_parsers.ArgList(max_length=16),
      metavar='SUBNET',
      help="""\
      Up to 16 candidate prefixes that can be used to restrict the allocation of
      `cloudRouterIpAddress` and `customerRouterIpAddress` for this
      attachment. All prefixes must be within link-local address space. Google
      will attempt to select an unused /29 from the supplied candidate
      subnet(s), or all of link-local space if no subnets supplied. Google will
      not re-use a /29 already in-use by your project, even if it's contained in
      one of the candidate subnets. The request will fail if all /29s within the
      candidate subnets are in use at Google's edge.""",
      default=[])
