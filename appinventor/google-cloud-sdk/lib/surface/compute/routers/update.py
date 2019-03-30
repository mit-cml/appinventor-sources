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
"""Command for updating Google Compute Engine routers."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.api_lib.compute import routers_utils
from googlecloudsdk.api_lib.compute.operations import poller
from googlecloudsdk.api_lib.util import waiter
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.compute.routers import flags
from googlecloudsdk.command_lib.compute.routers import router_utils
from googlecloudsdk.core import log
from googlecloudsdk.core import resources


@base.ReleaseTracks(base.ReleaseTrack.GA, base.ReleaseTrack.BETA)
class Update(base.UpdateCommand):
  """Update a Google Compute Engine router."""

  ROUTER_ARG = None

  @classmethod
  def _Args(cls, parser, support_keepalive_interval=False):
    cls.ROUTER_ARG = flags.RouterArgument()
    cls.ROUTER_ARG.AddArgument(parser, operation_type='update')
    base.ASYNC_FLAG.AddToParser(parser)
    if support_keepalive_interval:
      flags.AddKeepaliveIntervalArg(parser)
    flags.AddUpdateCustomAdvertisementArgs(parser, 'router')

  @classmethod
  def Args(cls, parser):
    cls._Args(parser)

  def _Run(self, args, support_keepalive_interval=False):
    # Manually ensure replace/incremental flags are mutually exclusive.
    router_utils.CheckIncompatibleFlagsOrRaise(args)

    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    messages = holder.client.messages
    service = holder.client.apitools_client.routers

    router_ref = self.ROUTER_ARG.ResolveAsResource(args, holder.resources)

    request_type = messages.ComputeRoutersGetRequest
    replacement = service.Get(request_type(**router_ref.AsDict()))
    existing_mode = replacement.bgp.advertiseMode

    if support_keepalive_interval and args.keepalive_interval is not None:
      setattr(replacement.bgp, 'keepaliveInterval', args.keepalive_interval)

    if router_utils.HasReplaceAdvertisementFlags(args):
      mode, groups, ranges = router_utils.ParseAdvertisements(
          messages=messages, resource_class=messages.RouterBgp, args=args)

      router_utils.PromptIfSwitchToDefaultMode(
          messages=messages,
          resource_class=messages.RouterBgp,
          existing_mode=existing_mode,
          new_mode=mode)

      attrs = {
          'advertiseMode': mode,
          'advertisedGroups': groups,
          'advertisedIpRanges': ranges,
      }

      for attr, value in attrs.items():
        if value is not None:
          setattr(replacement.bgp, attr, value)

    if router_utils.HasIncrementalAdvertisementFlags(args):
      # This operation should only be run on custom mode routers.
      router_utils.ValidateCustomMode(
          messages=messages,
          resource_class=messages.RouterBgp,
          resource=replacement.bgp)

      # These arguments are guaranteed to be mutually exclusive in args.
      if args.add_advertisement_groups:
        groups_to_add = routers_utils.ParseGroups(
            resource_class=messages.RouterBgp,
            groups=args.add_advertisement_groups)
        replacement.bgp.advertisedGroups.extend(groups_to_add)

      if args.remove_advertisement_groups:
        groups_to_remove = routers_utils.ParseGroups(
            resource_class=messages.RouterBgp,
            groups=args.remove_advertisement_groups)
        router_utils.RemoveGroupsFromAdvertisements(
            messages=messages,
            resource_class=messages.RouterBgp,
            resource=replacement.bgp,
            groups=groups_to_remove)

      if args.add_advertisement_ranges:
        ip_ranges_to_add = routers_utils.ParseIpRanges(
            messages=messages, ip_ranges=args.add_advertisement_ranges)
        replacement.bgp.advertisedIpRanges.extend(ip_ranges_to_add)

      if args.remove_advertisement_ranges:
        router_utils.RemoveIpRangesFromAdvertisements(
            messages=messages,
            resource_class=messages.RouterBgp,
            resource=replacement.bgp,
            ip_ranges=args.remove_advertisement_ranges)

    # Cleared list fields need to be explicitly identified for Patch API.
    cleared_fields = []
    if not replacement.bgp.advertisedGroups:
      cleared_fields.append('bgp.advertisedGroups')
    if not replacement.bgp.advertisedIpRanges:
      cleared_fields.append('bgp.advertisedIpRanges')

    with holder.client.apitools_client.IncludeFields(cleared_fields):
      request_type = messages.ComputeRoutersPatchRequest
      result = service.Patch(
          request_type(
              project=router_ref.project,
              region=router_ref.region,
              router=router_ref.Name(),
              routerResource=replacement))

    operation_ref = resources.REGISTRY.Parse(
        result.name,
        collection='compute.regionOperations',
        params={
            'project': router_ref.project,
            'region': router_ref.region,
        })

    if args.async:
      log.UpdatedResource(
          operation_ref,
          kind='router [{0}]'.format(router_ref.Name()),
          is_async=True,
          details='Run the [gcloud compute operations describe] command '
          'to check the status of this operation.')
      return result

    target_router_ref = holder.resources.Parse(
        router_ref.Name(),
        collection='compute.routers',
        params={
            'project': router_ref.project,
            'region': router_ref.region,
        })

    operation_poller = poller.Poller(service, target_router_ref)
    return waiter.WaitFor(operation_poller, operation_ref,
                          'Updating router [{0}]'.format(router_ref.Name()))

  def Run(self, args):
    """See base.UpdateCommand."""
    return self._Run(args)


@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class UpdateAlpha(Update):
  """Update a Google Compute Engine router."""

  ROUTER_ARG = None

  @classmethod
  def Args(cls, parser):
    cls._Args(parser, support_keepalive_interval=True)

  def Run(self, args):
    return self._Run(args, support_keepalive_interval=True)


Update.detailed_help = {
    'DESCRIPTION':
        """
        *{command}* is used to update a Google Compute Engine router.
        """,
}
