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
"""Command for creating instance templates."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.api_lib.compute import image_utils
from googlecloudsdk.api_lib.compute import instance_template_utils
from googlecloudsdk.api_lib.compute import instance_utils
from googlecloudsdk.api_lib.compute import metadata_utils
from googlecloudsdk.api_lib.compute import utils
from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.calliope import base
from googlecloudsdk.calliope import exceptions
from googlecloudsdk.command_lib.compute import completers
from googlecloudsdk.command_lib.compute import flags
from googlecloudsdk.command_lib.compute.instance_templates import flags as instance_templates_flags
from googlecloudsdk.command_lib.compute.instances import flags as instances_flags
from googlecloudsdk.command_lib.compute.sole_tenancy import flags as sole_tenancy_flags
from googlecloudsdk.command_lib.compute.sole_tenancy import util as sole_tenancy_util
from googlecloudsdk.command_lib.util.args import labels_util

_INSTANTIATE_FROM_VALUES = [
    'attach-read-only',
    'blank',
    'custom-image',
    'do-not-include',
    'source-image',
    'source-image-family',
]


def _CommonArgs(parser,
                release_track,
                support_source_instance,
                support_local_ssd_size=False,
                support_shielded_vms=False,
                support_kms=False,
               ):
  """Adding arguments applicable for creating instance templates."""
  parser.display_info.AddFormat(instance_templates_flags.DEFAULT_LIST_FORMAT)
  metadata_utils.AddMetadataArgs(parser)
  instances_flags.AddDiskArgs(parser, enable_kms=support_kms)
  instances_flags.AddCreateDiskArgs(parser, enable_kms=support_kms)
  if support_local_ssd_size:
    instances_flags.AddLocalSsdArgsWithSize(parser)
  else:
    instances_flags.AddLocalSsdArgs(parser)
  instances_flags.AddCanIpForwardArgs(parser)
  instances_flags.AddAddressArgs(parser, instances=False)
  instances_flags.AddAcceleratorArgs(parser)
  instances_flags.AddMachineTypeArgs(parser)
  deprecate_maintenance_policy = release_track in [base.ReleaseTrack.ALPHA]
  instances_flags.AddMaintenancePolicyArgs(parser, deprecate_maintenance_policy)
  instances_flags.AddNoRestartOnFailureArgs(parser)
  instances_flags.AddPreemptibleVmArgs(parser)
  instances_flags.AddServiceAccountAndScopeArgs(parser, False)
  instances_flags.AddTagsArgs(parser)
  instances_flags.AddCustomMachineTypeArgs(parser)
  instances_flags.AddImageArgs(parser)
  instances_flags.AddNetworkArgs(parser)
  if support_shielded_vms:
    instances_flags.AddShieldedVMConfigArgs(parser)
  labels_util.AddCreateLabelsFlags(parser)
  instances_flags.AddNetworkTierArgs(parser, instance=True)

  sole_tenancy_flags.AddNodeAffinityFlagToParser(parser)

  flags.AddRegionFlag(
      parser,
      resource_type='subnetwork',
      operation_type='attach')

  parser.add_argument(
      '--description',
      help='Specifies a textual description for the instance template.')

  Create.InstanceTemplateArg = (
      instance_templates_flags.MakeInstanceTemplateArg())
  Create.InstanceTemplateArg.AddArgument(parser, operation_type='create')
  if support_source_instance:
    instance_templates_flags.MakeSourceInstanceArg().AddArgument(parser)
    parser.add_argument(
        '--configure-disk',
        type=arg_parsers.ArgDict(
            spec={
                'auto-delete': arg_parsers.ArgBoolean(),
                'device-name': str,
                'instantiate-from': str,
                'custom-image': str,
            },
        ),
        metavar='PROPERTY=VALUE',
        action='append',
        help="""\
        This option has effect only when used with `--source-instance`. It
        allows you to override how the source-instance's disks are defined in
        the template.

        *auto-delete*::: If `true`, this persistent disk will be automatically
        deleted when the instance is deleted. However, if the disk is later
        detached from the instance, this option won't apply. If not provided,
        the setting is copied from the source instance. Allowed values of the
        flag are: `false`, `no`, `true`, and `yes`.

        *device-name*::: Name of the device.

        *instantiate-from*::: Specifies whether to include the disk and which
        image to use. Valid values are: {}

        *custom-image*::: The custom image to use if custom-image is specified
        for instantiate-from.
        """.format(', '.join(_INSTANTIATE_FROM_VALUES)),
    )

  parser.display_info.AddCacheUpdater(completers.InstanceTemplatesCompleter)


def _ValidateInstancesFlags(args, support_kms=False):
  """Validate flags for instance template that affects instance creation.

  Args:
      args: argparse.Namespace, An object that contains the values for the
          arguments specified in the .Args() method.
      support_kms: If KMS is supported.
  """
  instances_flags.ValidateDiskCommonFlags(args)
  instances_flags.ValidateDiskBootFlags(args, enable_kms=support_kms)
  instances_flags.ValidateCreateDiskFlags(args)
  instances_flags.ValidateLocalSsdFlags(args)
  instances_flags.ValidateNicFlags(args)
  instances_flags.ValidateServiceAccountAndScopeArgs(args)
  instances_flags.ValidateAcceleratorArgs(args)


def _AddSourceInstanceToTemplate(
    compute_api, args, instance_template, support_source_instance):
  """Set the source instance for the template."""

  if not support_source_instance or not args.source_instance:
    return
  source_instance_arg = instance_templates_flags.MakeSourceInstanceArg()
  source_instance_ref = source_instance_arg.ResolveAsResource(
      args, compute_api.resources)
  instance_template.sourceInstance = source_instance_ref.SelfLink()
  if args.configure_disk:
    messages = compute_api.client.messages
    instance_template.sourceInstanceParams = messages.SourceInstanceParams()
    for disk in args.configure_disk:
      instantiate_from = disk.get('instantiate-from')
      custom_image = disk.get('custom-image')
      if custom_image and instantiate_from != 'custom-image':
        raise exceptions.InvalidArgumentException(
            '--configure-disk',
            'Value for `instaniate-from` must be \'custom-image\' if the key '
            '`custom-image` is specified.')
      disk_config = messages.DiskInstantiationConfig()
      disk_config.autoDelete = disk.get('auto-delete')
      disk_config.deviceName = disk.get('device-name')
      disk_config.instantiateFrom = (
          messages.DiskInstantiationConfig.InstantiateFromValueValuesEnum(
              instantiate_from.upper().replace('-', '_')))
      disk_config.customImage = custom_image
      instance_template.sourceInstanceParams.diskConfigs.append(disk_config)
  # `properties` and `sourceInstance` are a one of.
  instance_template.properties = None


def BuildShieldedVMConfigMessage(messages, args):
  """Common routine for creating instance template.

  Build a shielded VM config message.

  Args:
      messages: The client messages.
      args: the arguments passed to the test.

  Returns:
      A shielded VM config message.
  """
  # Set the default values for ShieldedVmConfig parameters

  shielded_vm_config_message = None
  enable_secure_boot = None
  enable_vtpm = None
  enable_integrity_monitoring = None
  if not (hasattr(args, 'shielded_vm_secure_boot') or
          hasattr(args, 'shielded_vm_vtpm') or
          hasattr(args, 'shielded_vm_integrity_monitoring')):
    return shielded_vm_config_message

  if (not args.IsSpecified('shielded_vm_secure_boot') and
      not args.IsSpecified('shielded_vm_vtpm') and
      not args.IsSpecified('shielded_vm_integrity_monitoring')):
    return shielded_vm_config_message

  if args.shielded_vm_secure_boot is not None:
    enable_secure_boot = args.shielded_vm_secure_boot
  if args.shielded_vm_vtpm is not None:
    enable_vtpm = args.shielded_vm_vtpm
  if args.shielded_vm_integrity_monitoring is not None:
    enable_integrity_monitoring = args.shielded_vm_integrity_monitoring
  # compute message for shielded VM configuration.
  shielded_vm_config_message = instance_utils.CreateShieldedVmConfigMessage(
      messages,
      enable_secure_boot,
      enable_vtpm,
      enable_integrity_monitoring)

  return shielded_vm_config_message


def _RunCreate(compute_api,
               args,
               support_source_instance,
               support_shielded_vms=False,
               support_kms=False):
  """Common routine for creating instance template.

  This is shared between various release tracks.

  Args:
      compute_api: The compute api.
      args: argparse.Namespace, An object that contains the values for the
          arguments specified in the .Args() method.
      support_source_instance: indicates whether source instance is supported.
      support_shielded_vms: Indicate whether a shielded vm config is supported
      or not.
      support_kms: Indicate whether KMS is integrated or not.

  Returns:
      A resource object dispatched by display.Displayer().
  """
  _ValidateInstancesFlags(args, support_kms=support_kms)
  instances_flags.ValidateNetworkTierArgs(args)

  client = compute_api.client

  boot_disk_size_gb = utils.BytesToGb(args.boot_disk_size)
  utils.WarnIfDiskSizeIsTooSmall(boot_disk_size_gb, args.boot_disk_type)

  instance_template_ref = (
      Create.InstanceTemplateArg.ResolveAsResource(
          args, compute_api.resources))

  metadata = metadata_utils.ConstructMetadataMessage(
      client.messages,
      metadata=args.metadata,
      metadata_from_file=args.metadata_from_file)

  if hasattr(args, 'network_interface') and args.network_interface:
    network_interfaces = (
        instance_template_utils.CreateNetworkInterfaceMessages)(
            resources=compute_api.resources,
            scope_lister=flags.GetDefaultScopeLister(client),
            messages=client.messages,
            network_interface_arg=args.network_interface,
            region=args.region)
  else:
    network_tier = getattr(args, 'network_tier', None)
    network_interfaces = [
        instance_template_utils.CreateNetworkInterfaceMessage(
            resources=compute_api.resources,
            scope_lister=flags.GetDefaultScopeLister(client),
            messages=client.messages,
            network=args.network,
            region=args.region,
            subnet=args.subnet,
            address=(instance_template_utils.EPHEMERAL_ADDRESS
                     if not args.no_address and not args.address
                     else args.address),
            network_tier=network_tier)
    ]

  # Compute the shieldedVmConfig message.
  if support_shielded_vms:
    shieldedvm_config_message = BuildShieldedVMConfigMessage(
        messages=client.messages,
        args=args)

  node_affinities = sole_tenancy_util.GetSchedulingNodeAffinityListFromArgs(
      args, client.messages)

  scheduling = instance_utils.CreateSchedulingMessage(
      messages=client.messages,
      maintenance_policy=args.maintenance_policy,
      preemptible=args.preemptible,
      restart_on_failure=args.restart_on_failure,
      node_affinities=node_affinities)

  if args.no_service_account:
    service_account = None
  else:
    service_account = args.service_account
  service_accounts = instance_utils.CreateServiceAccountMessages(
      messages=client.messages,
      scopes=[] if args.no_scopes else args.scopes,
      service_account=service_account)

  create_boot_disk = not instance_utils.UseExistingBootDisk(args.disk or [])
  if create_boot_disk:
    image_expander = image_utils.ImageExpander(client, compute_api.resources)
    try:
      image_uri, _ = image_expander.ExpandImageFlag(
          user_project=instance_template_ref.project,
          image=args.image,
          image_family=args.image_family,
          image_project=args.image_project,
          return_image_resource=True)
    except utils.ImageNotFoundError as e:
      if args.IsSpecified('image_project'):
        raise e
      image_uri, _ = image_expander.ExpandImageFlag(
          user_project=instance_template_ref.project,
          image=args.image,
          image_family=args.image_family,
          image_project=args.image_project,
          return_image_resource=False)
      raise utils.ImageNotFoundError(
          'The resource [{}] was not found. Is the image located in another '
          'project? Use the --image-project flag to specify the '
          'project where the image is located.'.format(image_uri))
  else:
    image_uri = None

  if args.tags:
    tags = client.messages.Tags(items=args.tags)
  else:
    tags = None

  persistent_disks = (
      instance_template_utils.CreatePersistentAttachedDiskMessages(
          client.messages, args.disk or []))

  persistent_create_disks = (
      instance_template_utils.CreatePersistentCreateDiskMessages(
          client, compute_api.resources, instance_template_ref.project,
          getattr(args, 'create_disk', []), support_kms=support_kms))

  if create_boot_disk:
    boot_disk_list = [
        instance_template_utils.CreateDefaultBootAttachedDiskMessage(
            messages=client.messages,
            disk_type=args.boot_disk_type,
            disk_device_name=args.boot_disk_device_name,
            disk_auto_delete=args.boot_disk_auto_delete,
            disk_size_gb=boot_disk_size_gb,
            image_uri=image_uri,
            kms_args=args,
            support_kms=support_kms)]
  else:
    boot_disk_list = []

  local_nvdimms = instance_utils.CreateLocalNvdimmMessages(
      args,
      compute_api.resources,
      client.messages,
  )

  local_ssds = instance_utils.CreateLocalSsdMessages(
      args,
      compute_api.resources,
      client.messages,
  )

  disks = (
      boot_disk_list + persistent_disks + persistent_create_disks +
      local_nvdimms + local_ssds
  )

  machine_type = instance_utils.InterpretMachineType(
      machine_type=args.machine_type,
      custom_cpu=args.custom_cpu,
      custom_memory=args.custom_memory,
      ext=getattr(args, 'custom_extensions', None))

  guest_accelerators = (
      instance_template_utils.CreateAcceleratorConfigMessages(
          client.messages, getattr(args, 'accelerator', None)))

  instance_template = client.messages.InstanceTemplate(
      properties=client.messages.InstanceProperties(
          machineType=machine_type,
          disks=disks,
          canIpForward=args.can_ip_forward,
          metadata=metadata,
          minCpuPlatform=args.min_cpu_platform,
          networkInterfaces=network_interfaces,
          serviceAccounts=service_accounts,
          scheduling=scheduling,
          tags=tags,
          guestAccelerators=guest_accelerators,
      ),
      description=args.description,
      name=instance_template_ref.Name(),
  )

  if support_shielded_vms:
    instance_template.properties.shieldedVmConfig = shieldedvm_config_message

  request = client.messages.ComputeInstanceTemplatesInsertRequest(
      instanceTemplate=instance_template,
      project=instance_template_ref.project)

  request.instanceTemplate.properties.labels = labels_util.ParseCreateArgs(
      args, client.messages.InstanceProperties.LabelsValue)

  _AddSourceInstanceToTemplate(
      compute_api, args, instance_template, support_source_instance)

  return client.MakeRequests([(client.apitools_client.instanceTemplates,
                               'Insert', request)])


@base.ReleaseTracks(base.ReleaseTrack.GA)
class Create(base.CreateCommand):
  """Create a Compute Engine virtual machine instance template.

  *{command}* facilitates the creation of Google Compute Engine
  virtual machine instance templates. For example, running:

      $ {command} INSTANCE-TEMPLATE

  will create one instance templates called 'INSTANCE-TEMPLATE'.

  Instance templates are global resources, and can be used to create
  instances in any zone.
  """
  _support_source_instance = True
  _support_kms = True

  @classmethod
  def Args(cls, parser):
    _CommonArgs(
        parser,
        release_track=base.ReleaseTrack.GA,
        support_source_instance=cls._support_source_instance,
        support_kms=cls._support_kms
    )
    instances_flags.AddMinCpuPlatformArgs(parser, base.ReleaseTrack.GA)

  def Run(self, args):
    """Creates and runs an InstanceTemplates.Insert request.

    Args:
      args: argparse.Namespace, An object that contains the values for the
          arguments specified in the .Args() method.

    Returns:
      A resource object dispatched by display.Displayer().
    """
    return _RunCreate(
        base_classes.ComputeApiHolder(base.ReleaseTrack.GA),
        args,
        support_source_instance=self._support_source_instance,
        support_kms=self._support_kms
    )


@base.ReleaseTracks(base.ReleaseTrack.BETA)
class CreateBeta(Create):
  """Create a Compute Engine virtual machine instance template.

  *{command}* facilitates the creation of Google Compute Engine
  virtual machine instance templates. For example, running:

      $ {command} INSTANCE-TEMPLATE

  will create one instance templates called 'INSTANCE-TEMPLATE'.

  Instance templates are global resources, and can be used to create
  instances in any zone.
  """
  _support_source_instance = True
  _support_shielded_vms = True
  _support_kms = True

  @classmethod
  def Args(cls, parser):
    _CommonArgs(
        parser,
        release_track=base.ReleaseTrack.BETA,
        support_local_ssd_size=False,
        support_source_instance=cls._support_source_instance,
        support_shielded_vms=cls._support_shielded_vms,
        support_kms=cls._support_kms
    )
    instances_flags.AddMinCpuPlatformArgs(parser, base.ReleaseTrack.BETA)

  def Run(self, args):
    """Creates and runs an InstanceTemplates.Insert request.

    Args:
      args: argparse.Namespace, An object that contains the values for the
          arguments specified in the .Args() method.

    Returns:
      A resource object dispatched by display.Displayer().
    """
    return _RunCreate(
        base_classes.ComputeApiHolder(base.ReleaseTrack.BETA),
        args=args,
        support_source_instance=self._support_source_instance,
        support_shielded_vms=self._support_shielded_vms,
        support_kms=self._support_kms
    )


@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class CreateAlpha(Create):
  """Create a Compute Engine virtual machine instance template.

  *{command}* facilitates the creation of Google Compute Engine
  virtual machine instance templates. For example, running:

      $ {command} INSTANCE-TEMPLATE

  will create one instance templates called 'INSTANCE-TEMPLATE'.

  Instance templates are global resources, and can be used to create
  instances in any zone.
  """
  _support_source_instance = True
  _support_shielded_vms = True
  _support_kms = True

  @classmethod
  def Args(cls, parser):
    _CommonArgs(
        parser,
        release_track=base.ReleaseTrack.ALPHA,
        support_local_ssd_size=True,
        support_source_instance=cls._support_source_instance,
        support_shielded_vms=cls._support_shielded_vms,
        support_kms=cls._support_kms
    )
    instances_flags.AddLocalNvdimmArgs(parser)
    instances_flags.AddMinCpuPlatformArgs(parser, base.ReleaseTrack.ALPHA)

  def Run(self, args):
    """Creates and runs an InstanceTemplates.Insert request.

    Args:
      args: argparse.Namespace, An object that contains the values for the
          arguments specified in the .Args() method.

    Returns:
      A resource object dispatched by display.Displayer().
    """
    return _RunCreate(
        base_classes.ComputeApiHolder(base.ReleaseTrack.ALPHA),
        args=args,
        support_source_instance=self._support_source_instance,
        support_shielded_vms=self._support_shielded_vms,
        support_kms=self._support_kms
    )
