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

"""Command for stopping an instance."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.api_lib.compute.operations import poller
from googlecloudsdk.api_lib.util import waiter
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.compute.instances import flags
from googlecloudsdk.core import exceptions as core_exceptions
from googlecloudsdk.core import log


@base.ReleaseTracks(base.ReleaseTrack.GA, base.ReleaseTrack.BETA)
class Stop(base.SilentCommand):
  """Stop a virtual machine instance.

  *{command}* is used to stop a Google Compute Engine virtual machine.
  Stopping a VM performs a clean shutdown, much like invoking the shutdown
  functionality of a workstation or laptop. Stopping a VM with a local SSD
  is not supported and will result in an API error.
  """

  @staticmethod
  def Args(parser):
    flags.INSTANCES_ARG.AddArgument(parser)
    base.ASYNC_FLAG.AddToParser(parser)

  def _CreateStopRequest(self, client, instance_ref):
    return client.messages.ComputeInstancesStopRequest(
        instance=instance_ref.Name(),
        project=instance_ref.project,
        zone=instance_ref.zone)

  def _CreateRequests(self, client, instance_refs, unused_args):
    return [(client.apitools_client.instances, 'Stop',
             self._CreateStopRequest(client, instance_ref))
            for instance_ref in instance_refs]

  def Run(self, args):
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client

    instance_refs = flags.INSTANCES_ARG.ResolveAsResource(
        args, holder.resources,
        scope_lister=flags.GetInstanceZoneScopeLister(client))

    requests = self._CreateRequests(client, instance_refs, args)

    errors_to_collect = []
    responses = client.BatchRequests(requests, errors_to_collect)
    if errors_to_collect:
      raise core_exceptions.MultiError(errors_to_collect)

    operation_refs = [holder.resources.Parse(r.selfLink) for r in responses]

    if args.async:
      for operation_ref in operation_refs:
        log.status.Print('Stop instance in progress for [{}].'.format(
            operation_ref.SelfLink()))
      log.status.Print(
          'Use [gcloud compute operations describe URI] command to check the '
          'status of the operation(s).')
      return responses

    operation_poller = poller.BatchPoller(
        client, client.apitools_client.instances, instance_refs)
    waiter.WaitFor(
        operation_poller,
        poller.OperationBatch(operation_refs),
        'Stopping instance(s) {0}'.format(
            ', '.join(i.Name() for i in instance_refs)),
        max_wait_ms=None)

    for instance_ref in instance_refs:
      log.status.Print('Updated [{0}].'.format(instance_ref))


@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class StopAlpha(Stop):
  """Stop a virtual machine instance.

  *{command}* is used stop a Google Compute Engine virtual machine.
  Stopping a VM performs a clean shutdown, much like invoking the shutdown
  functionality of a workstation or laptop. Stopping a VM with a local SSD
  is not supported and will result in an API error.
  """

  @staticmethod
  def Args(parser):
    flags.INSTANCES_ARG.AddArgument(parser)
    parser.add_argument(
        '--discard-local-ssd',
        action='store_true',
        help=('If provided, local SSD data is discarded.'))

    base.ASYNC_FLAG.AddToParser(parser)

  def _CreateStopRequest(self, client, instance_ref, discard_local_ssd):
    """Adds the discardLocalSsd var into the message."""
    return client.messages.ComputeInstancesStopRequest(
        discardLocalSsd=discard_local_ssd,
        instance=instance_ref.Name(),
        project=instance_ref.project,
        zone=instance_ref.zone)

  def _CreateRequests(self, client, instance_refs, args):
    return [(client.apitools_client.instances, 'Stop',
             self._CreateStopRequest(client, instance_ref,
                                     args.discard_local_ssd))
            for instance_ref in instance_refs]
