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
"""Command for modifying the properties of a subnetwork."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.api_lib.compute import subnets_utils
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.compute.networks.subnets import flags


@base.ReleaseTracks(base.ReleaseTrack.BETA, base.ReleaseTrack.GA)
class Update(base.UpdateCommand):
  """Updates properties of an existing Google Compute Engine subnetwork."""

  SUBNETWORK_ARG = None

  @classmethod
  def Args(cls, parser):
    """The command arguments handler.

    Args:
      parser: An argparse.ArgumentParser instance.
    """
    cls.SUBNETWORK_ARG = flags.SubnetworkArgument()
    cls.SUBNETWORK_ARG.AddArgument(parser, operation_type='update')

    flags.AddUpdateArgs(parser)

  def Run(self, args):
    """Issues requests necessary to update Subnetworks."""
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client
    subnet_ref = self.SUBNETWORK_ARG.ResolveAsResource(args, holder.resources)

    return subnets_utils.MakeSubnetworkUpdateRequest(
        client,
        subnet_ref,
        enable_private_ip_google_access=args.enable_private_ip_google_access,
        add_secondary_ranges=args.add_secondary_ranges,
        remove_secondary_ranges=args.remove_secondary_ranges,
        enable_flow_logs=args.enable_flow_logs)


@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class UpdateAlpha(base.UpdateCommand):
  """Updates properties of an existing Google Compute Engine subnetwork."""

  @classmethod
  def Args(cls, parser):
    """The command argument handler."""
    cls.SUBNETWORK_ARG = flags.SubnetworkArgument()
    cls.SUBNETWORK_ARG.AddArgument(parser, operation_type='update')

    flags.AddUpdateArgs(parser, include_alpha=True)

  def Run(self, args):
    """Issues requests necessary to update Subnetworks."""
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client
    subnet_ref = self.SUBNETWORK_ARG.ResolveAsResource(args, holder.resources)

    return subnets_utils.MakeSubnetworkUpdateRequest(
        client,
        subnet_ref,
        enable_private_ip_google_access=args.enable_private_ip_google_access,
        add_secondary_ranges=args.add_secondary_ranges,
        remove_secondary_ranges=args.remove_secondary_ranges,
        enable_flow_logs=args.enable_flow_logs,
        aggregation_interval=args.aggregation_interval,
        flow_sampling=args.flow_sampling,
        metadata=args.metadata,
        set_role_active=getattr(args, 'role', None) == 'ACTIVE',
        drain_timeout_seconds=args.drain_timeout,
        enable_private_ipv6_access=args.enable_private_ipv6_access,
        private_ipv6_google_access_type=args.private_ipv6_google_access_type)
