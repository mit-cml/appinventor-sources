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
"""Command for moving instances."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.api_lib.compute.operations import poller
from googlecloudsdk.api_lib.util import waiter
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.compute import completers
from googlecloudsdk.command_lib.compute.instances import flags
from googlecloudsdk.core import log
from googlecloudsdk.core import resources


class Move(base.SilentCommand):
  """Move an instance and its attached persistent disks between zones."""

  @staticmethod
  def Args(parser):
    flags.INSTANCE_ARG.AddArgument(parser)

    parser.add_argument(
        '--destination-zone',
        completer=completers.ZonesCompleter,
        help='The zone to move the instance to.',
        required=True)

    base.ASYNC_FLAG.AddToParser(parser)

  def Run(self, args):
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    target_instance = flags.INSTANCE_ARG.ResolveAsResource(
        args, holder.resources,
        scope_lister=flags.GetInstanceZoneScopeLister(holder.client))
    destination_zone = holder.resources.Parse(
        args.destination_zone,
        params={'project': target_instance.project},
        collection='compute.zones')

    client = holder.client.apitools_client
    messages = holder.client.messages

    request = messages.ComputeProjectsMoveInstanceRequest(
        instanceMoveRequest=messages.InstanceMoveRequest(
            destinationZone=destination_zone.SelfLink(),
            targetInstance=target_instance.SelfLink(),
        ),
        project=target_instance.project,
    )

    result = client.projects.MoveInstance(request)
    operation_ref = resources.REGISTRY.Parse(
        result.name,
        params={'project': target_instance.project},
        collection='compute.globalOperations')

    if args.async:
      log.UpdatedResource(
          operation_ref,
          kind='gce instance {0}'.format(target_instance.Name()),
          is_async=True,
          details='Use [gcloud compute operations describe] command '
                  'to check the status of this operation.'
      )
      return result

    destination_instance_ref = holder.resources.Parse(
        target_instance.Name(), collection='compute.instances',
        params={
            'project': target_instance.project,
            'zone': destination_zone.Name()
        })

    operation_poller = poller.Poller(client.instances, destination_instance_ref)
    return waiter.WaitFor(
        operation_poller, operation_ref,
        'Moving gce instance {0}'.format(target_instance.Name()))


Move.detailed_help = {
    'brief': ('Move an instance and its attached persistent disks between '
              'zones.'),
    'DESCRIPTION': """\
        *{command}* facilitates moving a Google Compute Engine virtual machine
        from one zone to another. Moving a virtual machine may incur downtime
        if the guest OS must be shutdown in order to quiesce disk volumes
        prior to snapshotting.

        For example, running:

           $ gcloud compute instances move example-instance-1 --zone us-central1-b --destination-zone us-central1-f

        will move the instance called example-instance-1 with its all attached
        persistent disks, currently running in us-central1-b, to us-central1-f.
    """}
