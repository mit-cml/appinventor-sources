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
"""Command for suspending an instance."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.api_lib.compute.operations import poller
from googlecloudsdk.api_lib.util import waiter
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.compute.instances import flags
from googlecloudsdk.core import exceptions
from googlecloudsdk.core import log


@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class Suspend(base.SilentCommand):
  """Suspend a virtual machine instance.

  *{command}* is used to suspend a Google Compute Engine virtual machine.
  Suspending a VM is the equivalent of sleep or standby mode:
  the guest receives an ACPI S3 suspend signal, after which all VM state
  is saved to temporary storage.  An instance can only be suspended while
  it is in the RUNNING state.  A suspended instance will be put in
  SUSPENDED state.

  Note: A suspended instance can be resumed by running the
  `gcloud alpha compute instances resume` command.

  Alpha restrictions: Suspending a Preemptible VM is not supported and
  will result in an API error. Suspending a VM that is using CSEK or GPUs
  is not supported and will result in an API error.
  """

  @staticmethod
  def Args(parser):
    flags.INSTANCES_ARG.AddArgument(parser)
    parser.add_argument(
        '--discard-local-ssd',
        action='store_true',
        help=('If provided, local SSD data is discarded.'))
    # TODO(b/36057354): consider adding detailed help.
    base.ASYNC_FLAG.AddToParser(parser)

  def _CreateSuspendRequest(self, client, instance_ref, discard_local_ssd):
    return client.messages.ComputeInstancesSuspendRequest(
        discardLocalSsd=discard_local_ssd,
        instance=instance_ref.Name(),
        project=instance_ref.project,
        zone=instance_ref.zone)

  def Run(self, args):
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client

    instance_refs = flags.INSTANCES_ARG.ResolveAsResource(
        args,
        holder.resources,
        scope_lister=flags.GetInstanceZoneScopeLister(client))

    requests = []
    for instance_ref in instance_refs:
      requests.append((client.apitools_client.instances,
                       'Suspend', self._CreateSuspendRequest(
                           client, instance_ref, args.discard_local_ssd)))

    errors_to_collect = []
    responses = client.BatchRequests(requests, errors_to_collect)
    if errors_to_collect:
      raise exceptions.MultiError(errors_to_collect)

    operation_refs = [holder.resources.Parse(r.selfLink) for r in responses]

    if args.async:
      for operation_ref in operation_refs:
        log.status.Print('Suspend instance in progress for [{}].'.format(
            operation_ref.SelfLink()))
      log.status.Print(
          'Use [gcloud compute operations describe URI] command to check the '
          'status of the operation(s).'
      )
      return responses

    operation_poller = poller.BatchPoller(
        client, client.apitools_client.instances, instance_refs)

    result = waiter.WaitFor(
        operation_poller,
        poller.OperationBatch(operation_refs),
        'Suspending instance(s) {0}'.format(', '.join(
            i.Name() for i in instance_refs)),
        max_wait_ms=None)

    for instance_ref in instance_refs:
      log.status.Print('Updated [{0}].'.format(instance_ref))

    return result
