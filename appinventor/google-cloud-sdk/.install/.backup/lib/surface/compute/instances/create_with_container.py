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
"""Command for creating VM instances running Docker images."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.api_lib.compute import containers_utils
from googlecloudsdk.api_lib.compute import image_utils
from googlecloudsdk.api_lib.compute import instance_utils
from googlecloudsdk.api_lib.compute import metadata_utils
from googlecloudsdk.calliope import base
from googlecloudsdk.calliope import exceptions
from googlecloudsdk.command_lib.compute import completers
from googlecloudsdk.command_lib.compute.instances import flags as instances_flags
from googlecloudsdk.command_lib.util.args import labels_util
from googlecloudsdk.core import log
from six.moves import zip


def _Args(parser, deprecate_maintenance_policy=False,
          container_mount_enabled=False):
  """Add flags shared by all release tracks."""
  parser.display_info.AddFormat(instances_flags.DEFAULT_LIST_FORMAT)
  metadata_utils.AddMetadataArgs(parser)
  instances_flags.AddDiskArgs(
      parser, True, container_mount_enabled=container_mount_enabled)
  instances_flags.AddCreateDiskArgs(
      parser, container_mount_enabled=container_mount_enabled)
  instances_flags.AddCanIpForwardArgs(parser)
  instances_flags.AddAddressArgs(parser, instances=True)
  instances_flags.AddMachineTypeArgs(parser)
  instances_flags.AddMaintenancePolicyArgs(
      parser, deprecate=deprecate_maintenance_policy)
  instances_flags.AddNoRestartOnFailureArgs(parser)
  instances_flags.AddPreemptibleVmArgs(parser)
  instances_flags.AddServiceAccountAndScopeArgs(parser, False)
  instances_flags.AddTagsArgs(parser)
  instances_flags.AddCustomMachineTypeArgs(parser)
  instances_flags.AddNetworkArgs(parser)
  instances_flags.AddPrivateNetworkIpArgs(parser)
  instances_flags.AddKonletArgs(parser)
  instances_flags.AddPublicDnsArgs(parser, instance=True)
  instances_flags.AddPublicPtrArgs(parser, instance=True)
  instances_flags.AddImageArgs(parser)
  labels_util.AddCreateLabelsFlags(parser)

  parser.add_argument(
      '--description',
      help='Specifies a textual description of the instances.')

  instances_flags.INSTANCES_ARG.AddArgument(parser, operation_type='create')

  CreateWithContainer.SOURCE_INSTANCE_TEMPLATE = (
      instances_flags.MakeSourceInstanceTemplateArg())
  CreateWithContainer.SOURCE_INSTANCE_TEMPLATE.AddArgument(parser)
  parser.display_info.AddCacheUpdater(completers.InstancesCompleter)


@base.ReleaseTracks(base.ReleaseTrack.GA)
class CreateWithContainer(base.CreateCommand):
  """Command for creating VM instances running container images."""

  @staticmethod
  def Args(parser):
    """Register parser args."""
    _Args(parser)
    instances_flags.AddNetworkTierArgs(parser, instance=True)
    instances_flags.AddMinCpuPlatformArgs(parser, base.ReleaseTrack.GA)

  def _ValidateArgs(self, args):
    instances_flags.ValidateNicFlags(args)
    instances_flags.ValidateNetworkTierArgs(args)
    instances_flags.ValidateKonletArgs(args)
    instances_flags.ValidateDiskCommonFlags(args)
    instances_flags.ValidateServiceAccountAndScopeArgs(args)
    if instance_utils.UseExistingBootDisk(args.disk or []):
      raise exceptions.InvalidArgumentException(
          '--disk',
          'Boot disk specified for containerized VM.')

  def GetImageUri(self, args, client, holder, instance_refs):
    if (args.IsSpecified('image') or args.IsSpecified('image_family') or
        args.IsSpecified('image_project')):
      image_expander = image_utils.ImageExpander(client, holder.resources)
      image_uri, _ = image_expander.ExpandImageFlag(
          user_project=instance_refs[0].project,
          image=args.image,
          image_family=args.image_family,
          image_project=args.image_project)
      if holder.resources.Parse(image_uri).project != 'cos-cloud':
        log.warning('This container deployment mechanism requires a '
                    'Container-Optimized OS image in order to work. Select an '
                    'image from a cos-cloud project (cost-stable, cos-beta, '
                    'cos-dev image families).')
    else:
      image_uri = containers_utils.ExpandKonletCosImageFlag(client)
    return image_uri

  def _GetNetworkInterfaces(
      self, args, client, holder, instance_refs, skip_defaults):
    return instance_utils.GetNetworkInterfaces(args, client, holder,
                                               instance_refs, skip_defaults)

  def GetNetworkInterfaces(
      self, args, resources, client, holder, instance_refs, skip_defaults):
    if args.network_interface:
      return instance_utils.CreateNetworkInterfaceMessages(
          resources=resources,
          compute_client=client,
          network_interface_arg=args.network_interface,
          instance_refs=instance_refs)
    return self._GetNetworkInterfaces(
        args, client, holder, instance_refs, skip_defaults)

  def Run(self, args):
    self._ValidateArgs(args)

    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client
    source_instance_template = instance_utils.GetSourceInstanceTemplate(
        args, holder.resources, self.SOURCE_INSTANCE_TEMPLATE)
    skip_defaults = instance_utils.GetSkipDefaults(source_instance_template)
    scheduling = instance_utils.GetScheduling(args, client, skip_defaults)
    service_accounts = instance_utils.GetServiceAccounts(
        args, client, skip_defaults)
    user_metadata = instance_utils.GetValidatedMetadata(args, client)
    boot_disk_size_gb = instance_utils.GetBootDiskSizeGb(args)
    instance_refs = instance_utils.GetInstanceRefs(args, client, holder)
    network_interfaces = self.GetNetworkInterfaces(
        args, holder.resources, client, holder, instance_refs, skip_defaults)
    machine_type_uris = instance_utils.GetMachineTypeUris(
        args, client, holder, instance_refs, skip_defaults)
    image_uri = self.GetImageUri(args, client, holder, instance_refs)
    labels = containers_utils.GetLabelsMessageWithCosVersion(
        args.labels, image_uri, holder.resources, client.messages.Instance)
    can_ip_forward = instance_utils.GetCanIpForward(args, skip_defaults)
    tags = containers_utils.CreateTagsMessage(client.messages, args.tags)

    requests = []
    for instance_ref, machine_type_uri in zip(instance_refs, machine_type_uris):
      metadata = containers_utils.CreateKonletMetadataMessage(
          client.messages, args, instance_ref.Name(), user_metadata)
      disks = instance_utils.CreateDiskMessages(
          holder, args, boot_disk_size_gb, image_uri, instance_ref,
          skip_defaults)
      request = client.messages.ComputeInstancesInsertRequest(
          instance=client.messages.Instance(
              canIpForward=can_ip_forward,
              disks=disks,
              description=args.description,
              labels=labels,
              machineType=machine_type_uri,
              metadata=metadata,
              minCpuPlatform=args.min_cpu_platform,
              name=instance_ref.Name(),
              networkInterfaces=network_interfaces,
              serviceAccounts=service_accounts,
              scheduling=scheduling,
              tags=tags),
          sourceInstanceTemplate=source_instance_template,
          project=instance_ref.project,
          zone=instance_ref.zone)

      requests.append((client.apitools_client.instances,
                       'Insert', request))

    return client.MakeRequests(requests)


@base.ReleaseTracks(base.ReleaseTrack.BETA)
class CreateWithContainerBeta(CreateWithContainer):
  """Command for creating VM instances running container images."""

  @staticmethod
  def Args(parser):
    """Register parser args."""
    _Args(parser, container_mount_enabled=True)
    instances_flags.AddNetworkTierArgs(parser, instance=True)
    instances_flags.AddContainerMountDiskFlag(parser)
    instances_flags.AddLocalSsdArgsWithSize(parser)
    instances_flags.AddMinCpuPlatformArgs(parser, base.ReleaseTrack.BETA)

  def _ValidateArgs(self, args):
    instances_flags.ValidateLocalSsdFlags(args)
    super(CreateWithContainerBeta, self)._ValidateArgs(args)

  def GetImageUri(self, args, client, holder, instance_refs):
    if (args.IsSpecified('image') or args.IsSpecified('image_family') or
        args.IsSpecified('image_project')):
      image_expander = image_utils.ImageExpander(client, holder.resources)
      image_uri, _ = image_expander.ExpandImageFlag(
          user_project=instance_refs[0].project,
          image=args.image,
          image_family=args.image_family,
          image_project=args.image_project)
      if holder.resources.Parse(image_uri).project != 'cos-cloud':
        log.warning('This container deployment mechanism requires a '
                    'Container-Optimized OS image in order to work. Select an '
                    'image from a cos-cloud project (cost-stable, cos-beta, '
                    'cos-dev image families).')
    else:
      image_uri = containers_utils.ExpandKonletCosImageFlag(client)
    return image_uri

  def Run(self, args):
    self._ValidateArgs(args)

    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    container_mount_disk = instances_flags.GetValidatedContainerMountDisk(
        holder,
        args.container_mount_disk,
        args.disk,
        args.create_disk)
    client = holder.client
    source_instance_template = instance_utils.GetSourceInstanceTemplate(
        args, holder.resources, self.SOURCE_INSTANCE_TEMPLATE)
    skip_defaults = instance_utils.GetSkipDefaults(source_instance_template)
    scheduling = instance_utils.GetScheduling(args, client, skip_defaults)
    service_accounts = instance_utils.GetServiceAccounts(
        args, client, skip_defaults)
    user_metadata = instance_utils.GetValidatedMetadata(args, client)
    boot_disk_size_gb = instance_utils.GetBootDiskSizeGb(args)
    instance_refs = instance_utils.GetInstanceRefs(args, client, holder)
    network_interfaces = self.GetNetworkInterfaces(
        args, holder.resources, client, holder, instance_refs, skip_defaults)
    machine_type_uris = instance_utils.GetMachineTypeUris(
        args, client, holder, instance_refs, skip_defaults)
    image_uri = self.GetImageUri(args, client, holder, instance_refs)
    labels = containers_utils.GetLabelsMessageWithCosVersion(
        args.labels, image_uri, holder.resources, client.messages.Instance)
    can_ip_forward = instance_utils.GetCanIpForward(args, skip_defaults)
    tags = containers_utils.CreateTagsMessage(client.messages, args.tags)

    requests = []
    for instance_ref, machine_type_uri in zip(instance_refs, machine_type_uris):
      metadata = containers_utils.CreateKonletMetadataMessage(
          client.messages, args, instance_ref.Name(), user_metadata,
          container_mount_disk_enabled=True,
          container_mount_disk=container_mount_disk)
      disks = instance_utils.CreateDiskMessages(
          holder, args, boot_disk_size_gb, image_uri, instance_ref,
          skip_defaults, match_container_mount_disks=True)
      request = client.messages.ComputeInstancesInsertRequest(
          instance=client.messages.Instance(
              canIpForward=can_ip_forward,
              disks=disks,
              description=args.description,
              labels=labels,
              machineType=machine_type_uri,
              metadata=metadata,
              minCpuPlatform=args.min_cpu_platform,
              name=instance_ref.Name(),
              networkInterfaces=network_interfaces,
              serviceAccounts=service_accounts,
              scheduling=scheduling,
              tags=tags),
          sourceInstanceTemplate=source_instance_template,
          project=instance_ref.project,
          zone=instance_ref.zone)

      requests.append((client.apitools_client.instances,
                       'Insert', request))

    return client.MakeRequests(requests)


@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class CreateWithContainerAlpha(CreateWithContainerBeta):
  """Alpha version of compute instances create-with-container command."""

  @staticmethod
  def Args(parser):
    _Args(parser, deprecate_maintenance_policy=True,
          container_mount_enabled=True)

    instances_flags.AddNetworkTierArgs(parser, instance=True)
    instances_flags.AddContainerMountDiskFlag(parser)
    instances_flags.AddLocalSsdArgsWithSize(parser)
    instances_flags.AddLocalNvdimmArgs(parser)
    instances_flags.AddMinCpuPlatformArgs(parser, base.ReleaseTrack.ALPHA)

  def _GetNetworkInterfaces(
      self, args, client, holder, instance_refs, skip_defaults):
    return instance_utils.GetNetworkInterfacesAlpha(
        args, client, holder, instance_refs, skip_defaults)

  def Run(self, args):
    self._ValidateArgs(args)
    instances_flags.ValidatePublicDnsFlags(args)
    instances_flags.ValidatePublicPtrFlags(args)

    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    container_mount_disk = instances_flags.GetValidatedContainerMountDisk(
        holder,
        args.container_mount_disk,
        args.disk,
        args.create_disk)

    client = holder.client
    source_instance_template = instance_utils.GetSourceInstanceTemplate(
        args, holder.resources, self.SOURCE_INSTANCE_TEMPLATE)
    skip_defaults = instance_utils.GetSkipDefaults(source_instance_template)
    scheduling = instance_utils.GetScheduling(args, client, skip_defaults)
    service_accounts = instance_utils.GetServiceAccounts(
        args, client, skip_defaults)
    user_metadata = instance_utils.GetValidatedMetadata(args, client)
    boot_disk_size_gb = instance_utils.GetBootDiskSizeGb(args)
    instance_refs = instance_utils.GetInstanceRefs(args, client, holder)
    network_interfaces = self.GetNetworkInterfaces(
        args, holder.resources, client, holder, instance_refs, skip_defaults)
    machine_type_uris = instance_utils.GetMachineTypeUris(
        args, client, holder, instance_refs, skip_defaults)
    image_uri = self.GetImageUri(args, client, holder, instance_refs)
    labels = containers_utils.GetLabelsMessageWithCosVersion(
        args.labels, image_uri, holder.resources, client.messages.Instance)
    can_ip_forward = instance_utils.GetCanIpForward(args, skip_defaults)
    tags = containers_utils.CreateTagsMessage(client.messages, args.tags)

    requests = []
    for instance_ref, machine_type_uri in zip(instance_refs, machine_type_uris):
      metadata = containers_utils.CreateKonletMetadataMessage(
          client.messages, args, instance_ref.Name(), user_metadata,
          container_mount_disk_enabled=True,
          container_mount_disk=container_mount_disk)
      disks = instance_utils.CreateDiskMessages(
          holder, args, boot_disk_size_gb, image_uri, instance_ref,
          skip_defaults, match_container_mount_disks=True)
      request = client.messages.ComputeInstancesInsertRequest(
          instance=client.messages.Instance(
              canIpForward=can_ip_forward,
              disks=disks,
              description=args.description,
              labels=labels,
              machineType=machine_type_uri,
              metadata=metadata,
              minCpuPlatform=args.min_cpu_platform,
              name=instance_ref.Name(),
              networkInterfaces=network_interfaces,
              serviceAccounts=service_accounts,
              scheduling=scheduling,
              tags=tags),
          sourceInstanceTemplate=source_instance_template,
          project=instance_ref.project,
          zone=instance_ref.zone)

      requests.append((client.apitools_client.instances,
                       'Insert', request))
    return client.MakeRequests(requests)


CreateWithContainer.detailed_help = {
    'brief':
        """\
    Creates Google Compute engine virtual machine instances running
    container images.
    """,
    'DESCRIPTION':
        """\
        *{command}* creates Google Compute Engine virtual
        machines that runs a Docker image. For example:

          $ {command} instance-1 --zone us-central1-a \
            --container-image=gcr.io/google-containers/busybox

        creates an instance called instance-1, in the us-central1-a zone,
        running the 'busybox' image.

        For more examples, refer to the *EXAMPLES* section below.
        """,
    'EXAMPLES':
        """\
        To run the gcr.io/google-containers/busybox image on an instance named
        'instance-1' that executes 'echo "Hello world"' as a run command, run:

          $ {command} instance-1 \
            --container-image=gcr.io/google-containers/busybox \
            --container-command='echo "Hello world"'

        To run the gcr.io/google-containers/busybox image in privileged mode,
        run:

          $ {command} instance-1 \
            --container-image=gcr.io/google-containers/busybox
            --container-privileged
        """
}
