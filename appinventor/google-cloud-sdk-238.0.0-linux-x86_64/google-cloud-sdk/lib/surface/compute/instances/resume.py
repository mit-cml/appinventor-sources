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
"""Command for resuming a suspended instance."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.api_lib.compute import csek_utils
from googlecloudsdk.api_lib.compute.operations import poller
from googlecloudsdk.api_lib.util import waiter
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.compute.instances import flags
from googlecloudsdk.core import exceptions
from googlecloudsdk.core import log
from googlecloudsdk.core import resources
from six.moves import zip


@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class Resume(base.SilentCommand):
  """Resume a virtual machine instance.

  *{command}* is used to resume a previously suspended Google Compute Engine
  virtual machine.
  """

  @staticmethod
  def Args(parser):
    flags.INSTANCES_ARG.AddArgument(parser)
    csek_utils.AddCsekKeyArgs(parser, flags_about_creation=False)
    base.ASYNC_FLAG.AddToParser(parser)

  def GetInstances(self, client, refs):
    """Fetches instance objects corresponding to the given references."""
    instance_get_requests = []
    for ref in refs:
      request_protobuf = client.messages.ComputeInstancesGetRequest(
          instance=ref.Name(), zone=ref.zone, project=ref.project)
      instance_get_requests.append((client.apitools_client.instances, 'Get',
                                    request_protobuf))

    instances = client.MakeRequests(instance_get_requests)
    return instances

  def Run(self, args):
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client

    csek_key_file = args.csek_key_file
    request_list = []
    instance_refs = flags.INSTANCES_ARG.ResolveAsResource(
        args,
        holder.resources,
        scope_lister=flags.GetInstanceZoneScopeLister(client))

    # If csek_key_file is supplied, we must first get a reference to the
    # instances specified in the file to ensure that they exist.
    # Only then can we verify that the key specified in the file matches what
    # was used to create the instance.
    if csek_key_file:
      instances = self.GetInstances(client, instance_refs)
    else:
      instances = [None for _ in instance_refs]

    for instance_ref, instance in zip(instance_refs, instances):
      disks = []

      if instance:
        allow_rsa_encrypted = self.ReleaseTrack() in [
            base.ReleaseTrack.ALPHA, base.ReleaseTrack.BETA
        ]
        csek_keys = csek_utils.CsekKeyStore.FromArgs(args, allow_rsa_encrypted)
        for disk in instance.disks:
          disk_resource = resources.REGISTRY.Parse(disk.source)

          disk_key_or_none = csek_utils.MaybeLookupKeyMessage(
              csek_keys, disk_resource, client.apitools_client)

          if disk_key_or_none:
            disks.append(
                client.messages.CustomerEncryptionKeyProtectedDisk(
                    diskEncryptionKey=disk_key_or_none, source=disk.source))
      if disks:
        encryption_req = client.messages.InstancesResumeRequest(disks=disks)

        request = (client.apitools_client.instances, 'Resume',
                   client.messages.ComputeInstancesResumeRequest(
                       instance=instance_ref.Name(),
                       instancesResumeRequest=encryption_req,
                       project=instance_ref.project,
                       zone=instance_ref.zone))

      else:
        request = (client.apitools_client.instances, 'Resume',
                   client.messages.ComputeInstancesResumeRequest(
                       instance=instance_ref.Name(),
                       project=instance_ref.project,
                       zone=instance_ref.zone))

      request_list.append(request)

    errors_to_collect = []
    responses = client.BatchRequests(request_list, errors_to_collect)
    if errors_to_collect:
      raise exceptions.MultiError(errors_to_collect)

    operation_refs = [holder.resources.Parse(r.selfLink) for r in responses]

    if args.async:
      for operation_ref in operation_refs:
        log.status.Print('Resume instance in progress for [{}].'.format(
            operation_ref.SelfLink()))
      log.status.Print(
          'Use [gcloud compute operations describe URI] command to check the '
          'status of the operation(s).')
      return responses

    operation_poller = poller.BatchPoller(
        client, client.apitools_client.instances, instance_refs)

    result = waiter.WaitFor(
        operation_poller,
        poller.OperationBatch(operation_refs),
        'Resuming instance(s) {0}'.format(', '.join(
            i.Name() for i in instance_refs)),
        max_wait_ms=None)

    for instance_ref in instance_refs:
      log.status.Print('Updated [{0}].'.format(instance_ref))

    return result
