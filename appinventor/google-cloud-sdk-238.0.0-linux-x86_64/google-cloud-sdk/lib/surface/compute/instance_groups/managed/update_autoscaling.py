# -*- coding: utf-8 -*- #
# Copyright 2015 Google Inc. All Rights Reserved.
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
"""Command to PATCH-style update autoscaling for a managed instance group."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.api_lib.compute import managed_instance_groups_utils as mig_utils
from googlecloudsdk.api_lib.compute.instance_groups.managed import autoscalers as autoscalers_api
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.compute.instance_groups import flags as instance_groups_flags
from googlecloudsdk.core import exceptions


class NoMatchingAutoscalerFoundError(exceptions.Error):
  pass


@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class UpdateAutoscaling(base.Command):
  """Update autoscaling parameters of a managed instance group."""

  @staticmethod
  def Args(parser):
    instance_groups_flags.MULTISCOPE_INSTANCE_GROUP_MANAGER_ARG.AddArgument(
        parser)
    mig_utils.GetModeFlag().AddToParser(parser)

  def Run(self, args):
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client

    igm_ref = instance_groups_flags.CreateGroupReference(
        client, holder.resources, args)

    # Assert that Instance Group Manager exists.
    mig_utils.GetInstanceGroupManagerOrThrow(igm_ref, client)

    old_autoscaler = mig_utils.AutoscalerForMigByRef(client, holder.resources,
                                                     igm_ref)
    if mig_utils.IsAutoscalerNew(old_autoscaler):
      raise NoMatchingAutoscalerFoundError(
          'Instance group manager [{}] has no existing autoscaler; '
          'cannot update.'.format(igm_ref.Name()))

    autoscalers_client = autoscalers_api.GetClient(client, igm_ref)
    new_autoscaler = autoscalers_client.message_type(
        name=old_autoscaler.name,  # PATCH needs this
        autoscalingPolicy=client.messages.AutoscalingPolicy())
    if args.IsSpecified('mode'):
      mode = mig_utils.ParseModeString(args.mode, client.messages)
      new_autoscaler.autoscalingPolicy.mode = mode
    return autoscalers_client.Patch(igm_ref, new_autoscaler)


UpdateAutoscaling.detailed_help = {
    'brief': 'Update autoscaling parameters of a managed instance group',
    'DESCRIPTION': """\
*{command}* updates autoscaling parameters of specified managed instance
group.

Autoscalers can use one or more policies listed below. Information on using
multiple policies can be found here: [](https://cloud.google.com/compute/docs/autoscaler/multiple-policies)

In contrast to *{parent_command} set-autoscaling*, this command *only* updates
specified fields. For instance:

    $ {command} --mode only-up

would change the *mode* field of the autoscaler policy, but leave the rest of
the settings intact.
        """,
}
