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
"""Command for creating interconnects."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.api_lib.compute.interconnects import client
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.compute.interconnects import flags
from googlecloudsdk.command_lib.compute.interconnects.locations import flags as location_flags
from googlecloudsdk.core import log

DETAILED_HELP = {
    'DESCRIPTION':
        """\
        *{command}* is used to create interconnects. An interconnect represents
        a single specific connection between Google and the customer.

        For an example, refer to the *EXAMPLES* section below.
        """,
    # pylint: disable=line-too-long
    'EXAMPLES':
        """\
        To create an interconnect of type DEDICATED, run:

          $ {command} example-interconnect --customer-name "Example Customer Name" --interconnect-type DEDICATED --link-type LINK_TYPE_ETHERNET_10G_LR --location example-zone1-1 --requested-link-count 1 --noc-contact-email noc@example.com --description "Example interconnect"
        """,
    # pylint: enable=line-too-long
}

_LOCATION_FLAG_MSG = (
    'The location for the interconnect. The locations can be listed by using '
    'the `{parent_command} locations list` command to find '
    'the appropriate location to use when creating an interconnect.')

_DOCUMENTATION_LINK = 'https://cloud.google.com/interconnect/docs/how-to/dedicated/retrieving-loas'


@base.ReleaseTracks(base.ReleaseTrack.GA)
class Create(base.CreateCommand):
  """Create a Google Compute Engine interconnect.

  *{command}* is used to create interconnects. An interconnect represents a
  single specific connection between Google and the customer.
  """

  INTERCONNECT_ARG = None
  LOCATION_ARG = None

  @classmethod
  def Args(cls, parser):
    cls.LOCATION_ARG = (
        location_flags.InterconnectLocationArgumentForOtherResource(
            _LOCATION_FLAG_MSG))
    cls.LOCATION_ARG.AddArgument(parser)
    cls.INTERCONNECT_ARG = flags.InterconnectArgument()
    cls.INTERCONNECT_ARG.AddArgument(parser, operation_type='create')
    flags.AddCreateGaArgs(parser)
    parser.display_info.AddCacheUpdater(flags.InterconnectsCompleter)

  def Collection(self):
    return 'compute.interconnects'

  def Run(self, args):
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    ref = self.INTERCONNECT_ARG.ResolveAsResource(args, holder.resources)
    interconnect = client.Interconnect(ref, compute_client=holder.client)
    location_ref = self.LOCATION_ARG.ResolveAsResource(args, holder.resources)

    messages = holder.client.messages
    interconnect_type = flags.GetInterconnectType(messages,
                                                  args.interconnect_type)
    link_type = flags.GetLinkType(messages, args.link_type)

    return interconnect.Create(
        description=args.description,
        interconnect_type=interconnect_type,
        requested_link_count=args.requested_link_count,
        link_type=link_type,
        admin_enabled=args.admin_enabled,
        noc_contact_email=args.noc_contact_email,
        location=location_ref.SelfLink(),
        customer_name=args.customer_name)

  def Epilog(self, resources_were_displayed):
    message = ('Please check the provided contact email for further '
               'instructions on how to activate your Interconnect. See also '
               '{} for more detailed help.'.format(_DOCUMENTATION_LINK))
    log.status.Print(message)


@base.ReleaseTracks(base.ReleaseTrack.BETA)
class CreateBeta(Create):
  """Create a Google Compute Engine interconnect.

  *{command}* is used to create interconnects. An interconnect represents a
  single specific connection between Google and the customer.
  """

  INTERCONNECT_ARG = None
  LOCATION_ARG = None

  supports_100g = False

  @classmethod
  def Args(cls, parser):
    cls.LOCATION_ARG = (
        location_flags.InterconnectLocationArgumentForOtherResource(
            _LOCATION_FLAG_MSG))
    cls.LOCATION_ARG.AddArgument(parser)
    cls.INTERCONNECT_ARG = flags.InterconnectArgument()
    cls.INTERCONNECT_ARG.AddArgument(parser, operation_type='create')
    flags.AddCreateBetaArgs(parser, cls.supports_100g)


@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class CreateAlpha(CreateBeta):
  """Create a Google Compute Engine interconnect.

  *{command}* is used to create interconnects. An interconnect represents a
  single specific connection between Google and the customer.
  """

  supports_100g = True


Create.detailed_help = DETAILED_HELP
