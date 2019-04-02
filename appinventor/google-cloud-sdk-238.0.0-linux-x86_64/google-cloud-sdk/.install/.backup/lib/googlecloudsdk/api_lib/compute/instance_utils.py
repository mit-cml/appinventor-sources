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
"""Convenience functions for dealing with instances and instance templates."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import collections
import re

from googlecloudsdk.api_lib.compute import alias_ip_range_utils
from googlecloudsdk.api_lib.compute import constants
from googlecloudsdk.api_lib.compute import containers_utils
from googlecloudsdk.api_lib.compute import csek_utils
from googlecloudsdk.api_lib.compute import image_utils
from googlecloudsdk.api_lib.compute import kms_utils
from googlecloudsdk.api_lib.compute import metadata_utils
from googlecloudsdk.api_lib.compute import utils
from googlecloudsdk.api_lib.compute import zone_utils
from googlecloudsdk.calliope import exceptions as calliope_exceptions
from googlecloudsdk.command_lib.compute import flags as compute_flags
from googlecloudsdk.command_lib.compute import scope as compute_scopes
from googlecloudsdk.command_lib.compute.instances import flags
from googlecloudsdk.command_lib.compute.sole_tenancy import util as sole_tenancy_util
from googlecloudsdk.command_lib.util.ssh import ssh
from googlecloudsdk.core import log
import ipaddress
import six


EMAIL_REGEX = re.compile(r'(^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+$)')

_DEFAULT_DEVICE_NAME_CONTAINER_WARNING = (
    'Default device-name for disk name [{0}] will be [{0}] because it is being '
    'mounted to a container with [`--container-mount-disk`]')


def GetCpuRamFromCustomName(name):
  """Gets the CPU and memory specs from the custom machine type name.

  Args:
    name: the custom machine type name for the 'instance create' call

  Returns:
    A two-tuple with the number of cpu and amount of memory for the custom
    machine type

    custom_cpu, the number of cpu desired for the custom machine type instance
    custom_memory_mib, the amount of ram desired in MiB for the custom machine
      type instance
    None for both variables otherwise
  """
  check_custom = re.search('custom-([0-9]+)-([0-9]+)', name)
  if check_custom:
    custom_cpu = check_custom.group(1)
    custom_memory_mib = check_custom.group(2)
    return custom_cpu, custom_memory_mib
  return None, None


def GetNameForCustom(custom_cpu, custom_memory_mib, ext=False):
  """Creates a custom machine type name from the desired CPU and memory specs.

  Args:
    custom_cpu: the number of cpu desired for the custom machine type
    custom_memory_mib: the amount of ram desired in MiB for the custom machine
      type instance
    ext: extended custom machine type should be used if true

  Returns:
    The custom machine type name for the 'instance create' call
  """
  machine_type = 'custom-{0}-{1}'.format(custom_cpu, custom_memory_mib)
  if ext:
    machine_type += '-ext'
  return machine_type


def InterpretMachineType(machine_type, custom_cpu, custom_memory, ext=True):
  """Interprets the machine type for the instance.

  Args:
    machine_type: name of existing machine type, eg. n1-standard
    custom_cpu: number of CPU cores for custom machine type,
    custom_memory: amount of RAM memory in bytes for custom machine type,
    ext: extended custom machine type should be used if true,

  Returns:
    A string representing the URL naming a machine-type.

  Raises:
    calliope_exceptions.RequiredArgumentException when only one of the two
      custom machine type flags are used.
    calliope_exceptions.InvalidArgumentException when both the machine type and
      custom machine type flags are used to generate a new instance.
  """
  # Setting the machine type
  machine_type_name = constants.DEFAULT_MACHINE_TYPE
  if machine_type:
    machine_type_name = machine_type

  # Setting the specs for the custom machine.
  if custom_cpu or custom_memory or ext:
    if not custom_cpu:
      raise calliope_exceptions.RequiredArgumentException(
          '--custom-cpu', 'Both [--custom-cpu] and [--custom-memory] must be '
          'set to create a custom machine type instance.')
    if not custom_memory:
      raise calliope_exceptions.RequiredArgumentException(
          '--custom-memory', 'Both [--custom-cpu] and [--custom-memory] must '
          'be set to create a custom machine type instance.')
    if machine_type:
      raise calliope_exceptions.InvalidArgumentException(
          '--machine-type', 'Cannot set both [--machine-type] and '
          '[--custom-cpu]/[--custom-memory] for the same instance.')
    custom_type_string = GetNameForCustom(
        custom_cpu,
        # converting from B to MiB.
        custom_memory // (2**20),
        ext)

    # Updating the machine type that is set for the URIs
    machine_type_name = custom_type_string
  return machine_type_name


def CheckCustomCpuRamRatio(compute_client, project, zone, machine_type_name):
  """Checks that the CPU and memory ratio is a supported custom instance type.

  Args:
    compute_client: GCE API client,
    project: a project,
    zone: the zone of the instance(s) being created,
    machine_type_name: The machine type of the instance being created.

  Returns:
    Nothing. Function acts as a bound checker, and will raise an exception from
      within the function if needed.

  Raises:
    utils.RaiseToolException if a custom machine type ratio is out of bounds.
  """
  messages = compute_client.messages
  compute = compute_client.apitools_client
  if 'custom' in machine_type_name:
    mt_get_pb = messages.ComputeMachineTypesGetRequest(
        machineType=machine_type_name,
        project=project,
        zone=zone)
    mt_get_reqs = [(compute.machineTypes, 'Get', mt_get_pb)]
    errors = []

    # Makes a 'machine-types describe' request to check the bounds
    _ = list(compute_client.MakeRequests(
        requests=mt_get_reqs,
        errors_to_collect=errors))

    if errors:
      utils.RaiseToolException(
          errors,
          error_message='Could not fetch machine type:')


def CreateServiceAccountMessages(messages, scopes, service_account):
  """Returns a list of ServiceAccount messages corresponding to scopes."""
  if scopes is None:
    scopes = constants.DEFAULT_SCOPES
  if service_account is None:
    service_account = 'default'

  accounts_to_scopes = collections.defaultdict(list)
  for scope in scopes:
    parts = scope.split('=')
    if len(parts) == 1:
      account = service_account
      scope_uri = scope
    elif len(parts) == 2:
      # TODO(b/33688878) Remove exception for this deprecated format
      raise calliope_exceptions.InvalidArgumentException(
          '--scopes',
          'Flag format --scopes [ACCOUNT=]SCOPE,[[ACCOUNT=]SCOPE, ...] is '
          'removed. Use --scopes [SCOPE,...] --service-account ACCOUNT '
          'instead.')
    else:
      raise calliope_exceptions.ToolException(
          '[{0}] is an illegal value for [--scopes]. Values must be of the '
          'form [SCOPE].'.format(scope))

    if service_account != 'default' and not ssh.Remote.FromArg(service_account):
      raise calliope_exceptions.InvalidArgumentException(
          '--service-account',
          'Invalid format: expected default or user@domain.com, received ' +
          service_account)

    # Expands the scope if the user provided an alias like
    # "compute-rw".
    scope_uri = constants.SCOPES.get(scope_uri, [scope_uri])
    accounts_to_scopes[account].extend(scope_uri)

  res = []
  for account, scopes in sorted(six.iteritems(accounts_to_scopes)):
    res.append(messages.ServiceAccount(email=account,
                                       scopes=sorted(scopes)))
  return res


def CreateOnHostMaintenanceMessage(messages, maintenance_policy):
  """Create on-host-maintenance message for VM."""
  if maintenance_policy:
    on_host_maintenance = messages.Scheduling.OnHostMaintenanceValueValuesEnum(
        maintenance_policy)
  else:
    on_host_maintenance = None
  return on_host_maintenance


def CreateSchedulingMessage(
    messages, maintenance_policy, preemptible, restart_on_failure,
    node_affinities=None):
  """Create scheduling message for VM."""
  # Note: We always specify automaticRestart=False for preemptible VMs. This
  # makes sense, since no-restart-on-failure is defined as "store-true", and
  # thus can't be given an explicit value. Hence it either has its default
  # value (in which case we override it for convenience's sake to the only
  # setting that makes sense for preemptible VMs), or the user actually
  # specified no-restart-on-failure, the only usable setting.
  on_host_maintenance = CreateOnHostMaintenanceMessage(messages,
                                                       maintenance_policy)
  if preemptible:
    scheduling = messages.Scheduling(automaticRestart=False,
                                     onHostMaintenance=on_host_maintenance,
                                     preemptible=True)
  else:
    scheduling = messages.Scheduling(automaticRestart=restart_on_failure,
                                     onHostMaintenance=on_host_maintenance)
  if node_affinities:
    scheduling.nodeAffinities = node_affinities

  return scheduling


def CreateShieldedVmConfigMessage(
    messages, enable_secure_boot, enable_vtpm, enable_integrity_monitoring):
  """Create shieldedVMConfig message for VM."""

  shielded_vm_config = messages.ShieldedVmConfig(
      enableSecureBoot=enable_secure_boot,
      enableVtpm=enable_vtpm,
      enableIntegrityMonitoring=enable_integrity_monitoring)

  return shielded_vm_config


def CreateShieldedVmIntegrityPolicyMessage(messages,
                                           update_auto_learn_policy=True):
  """Creates shieldedVmIntegrityPolicy message for VM."""

  shielded_vm_integrity_policy = messages.ShieldedVmIntegrityPolicy(
      updateAutoLearnPolicy=update_auto_learn_policy)

  return shielded_vm_integrity_policy


def CreateMachineTypeUris(
    resources, compute_client,
    machine_type, custom_cpu, custom_memory, ext, instance_refs):
  """Create machine type URIs for given args and instance references."""
  # The element at index i is the machine type URI for instance
  # i. We build this list here because we want to delay work that
  # requires API calls as much as possible. This leads to a better
  # user experience because the tool can fail fast upon a spelling
  # mistake instead of delaying the user by making API calls whose
  # purpose has already been rendered moot by the spelling mistake.
  machine_type_uris = []

  # Setting the machine type
  machine_type_name = InterpretMachineType(
      machine_type, custom_cpu, custom_memory, ext)

  for instance_ref in instance_refs:
    # Check to see if the custom machine type ratio is supported
    CheckCustomCpuRamRatio(compute_client,
                           instance_ref.project,
                           instance_ref.zone,
                           machine_type_name)
    machine_type_uris.append(
        resources.Parse(
            machine_type_name,
            collection='compute.machineTypes',
            params={
                'project': instance_ref.project,
                'zone': instance_ref.zone
            }).SelfLink())

  return machine_type_uris


def CreateNetworkInterfaceMessage(resources,
                                  compute_client,
                                  network,
                                  subnet,
                                  private_network_ip,
                                  no_address,
                                  address,
                                  instance_refs,
                                  alias_ip_ranges_string=None,
                                  network_tier=None,
                                  no_public_dns=None,
                                  public_dns=None,
                                  no_public_ptr=None,
                                  public_ptr=None,
                                  no_public_ptr_domain=None,
                                  public_ptr_domain=None):
  """Returns a new NetworkInterface message."""
  # TODO(b/30460572): instance reference should have zone name, not zone URI.
  region = utils.ZoneNameToRegionName(instance_refs[0].zone.split('/')[-1])
  messages = compute_client.messages
  network_interface = messages.NetworkInterface()
  # By default interface is attached to default network. If network or subnet
  # are specified they're used instead.
  if subnet is not None:
    subnet_ref = resources.Parse(
        subnet,
        collection='compute.subnetworks',
        params={
            'project': instance_refs[0].project,
            'region': region
        })
    network_interface.subnetwork = subnet_ref.SelfLink()
  if network is not None:
    network_ref = resources.Parse(
        network,
        params={
            'project': instance_refs[0].project,
        },
        collection='compute.networks')
    network_interface.network = network_ref.SelfLink()
  elif subnet is None:
    network_ref = resources.Parse(
        constants.DEFAULT_NETWORK,
        params={'project': instance_refs[0].project},
        collection='compute.networks')
    network_interface.network = network_ref.SelfLink()

  if private_network_ip is not None:
    # Try interpreting the address as IPv4 or IPv6.
    try:
      # ipaddress only allows unicode input
      ipaddress.ip_address(six.text_type(private_network_ip))
      network_interface.networkIP = private_network_ip
    except ValueError:
      # ipaddress could not resolve as an IPv4 or IPv6 address.
      network_interface.networkIP = flags.GetAddressRef(
          resources, private_network_ip, region).SelfLink()

  if alias_ip_ranges_string:
    network_interface.aliasIpRanges = (
        alias_ip_range_utils.CreateAliasIpRangeMessagesFromString(
            messages, True, alias_ip_ranges_string))

  if not no_address:
    access_config = messages.AccessConfig(
        name=constants.DEFAULT_ACCESS_CONFIG_NAME,
        type=messages.AccessConfig.TypeValueValuesEnum.ONE_TO_ONE_NAT)
    if network_tier is not None:
      access_config.networkTier = (messages.AccessConfig.
                                   NetworkTierValueValuesEnum(network_tier))

    # If the user provided an external IP, populate the access
    # config with it.
    # TODO(b/25278937): plays poorly when creating multiple instances
    if len(instance_refs) == 1:
      address_resource = flags.ExpandAddressFlag(
          resources, compute_client, address, region)
      if address_resource:
        access_config.natIP = address_resource

    if no_public_dns is True:
      access_config.setPublicDns = False
    elif public_dns is True:
      access_config.setPublicDns = True

    if no_public_ptr is True:
      access_config.setPublicPtr = False
    elif public_ptr is True:
      access_config.setPublicPtr = True

    if no_public_ptr_domain is not True and public_ptr_domain is not None:
      access_config.publicPtrDomainName = public_ptr_domain

    network_interface.accessConfigs = [access_config]

  return network_interface


def CreateNetworkInterfaceMessages(resources, compute_client,
                                   network_interface_arg, instance_refs):
  """Create network interface messages.

  Args:
    resources: generates resource references.
    compute_client: creates resources.
    network_interface_arg: CLI argument specyfying network interfaces.
    instance_refs: reference to instances that will own the generated
                   interfaces.
  Returns:
    list, items are NetworkInterfaceMessages.
  """
  result = []
  if network_interface_arg:
    for interface in network_interface_arg:
      address = interface.get('address', None)
      no_address = 'no-address' in interface
      network_tier = interface.get('network-tier', None)

      result.append(CreateNetworkInterfaceMessage(
          resources, compute_client, interface.get('network', None),
          interface.get('subnet', None),
          interface.get('private-network-ip', None), no_address,
          address, instance_refs, interface.get('aliases', None), network_tier))
  return result


def ParseDiskResource(resources, name, project, zone, type_):
  """Parses the regional disk resources."""
  if type_ == compute_scopes.ScopeEnum.REGION:
    return resources.Parse(
        name,
        collection='compute.regionDisks',
        params={
            'project': project,
            'region': utils.ZoneNameToRegionName(zone)
        })
  else:
    return resources.Parse(
        name,
        collection='compute.disks',
        params={
            'project': project,
            'zone': zone
        })


def GetDiskDeviceName(disk, name, container_mount_disk):
  """Helper method to get device-name for a disk message."""
  if (container_mount_disk and
      filter(bool,
             [d.get('name', name) == name for d in container_mount_disk])):
    # device-name must be the same as name if it is being mounted to a
    # container.
    if not disk.get('device-name'):
      log.warning(_DEFAULT_DEVICE_NAME_CONTAINER_WARNING.format(name))
      return name
    # This is defensive only; should be validated before this method is called.
    elif disk.get('device-name') != name:
      raise calliope_exceptions.InvalidArgumentException(
          '--container-mount-disk',
          'Attempting to mount disk named [{}] with device-name [{}]. If '
          'being mounted to container, disk name must match device-name.'
          .format(name, disk.get('device-name')))
  return disk.get('device-name')


def CreatePersistentAttachedDiskMessages(
    resources, compute_client, csek_keys, disks, instance_ref,
    container_mount_disk=None):
  """Returns a list of AttachedDisk messages and the boot disk's reference."""
  disks_messages = []
  boot_disk_ref = None

  messages = compute_client.messages
  compute = compute_client.apitools_client
  for disk in disks:
    name = disk['name']

    # Resolves the mode.
    mode_value = disk.get('mode', 'rw')
    if mode_value == 'rw':
      mode = messages.AttachedDisk.ModeValueValuesEnum.READ_WRITE
    else:
      mode = messages.AttachedDisk.ModeValueValuesEnum.READ_ONLY

    boot = disk.get('boot') == 'yes'
    auto_delete = disk.get('auto-delete') == 'yes'

    if 'scope' in disk and disk['scope'] == 'regional':
      scope = compute_scopes.ScopeEnum.REGION
    else:
      scope = compute_scopes.ScopeEnum.ZONE
    disk_ref = ParseDiskResource(resources, name, instance_ref.project,
                                 instance_ref.zone, scope)

    if boot:
      boot_disk_ref = disk_ref

    # TODO(b/36051031) drop test after CSEK goes GA
    if csek_keys:
      disk_key_or_none = csek_utils.MaybeLookupKeyMessage(
          csek_keys, disk_ref, compute)
      kwargs = {'diskEncryptionKey': disk_key_or_none}
    else:
      kwargs = {}

    device_name = GetDiskDeviceName(disk, name, container_mount_disk)

    attached_disk = messages.AttachedDisk(
        autoDelete=auto_delete,
        boot=boot,
        deviceName=device_name,
        mode=mode,
        source=disk_ref.SelfLink(),
        type=messages.AttachedDisk.TypeValueValuesEnum.PERSISTENT,
        **kwargs)

    # The boot disk must end up at index 0.
    if boot:
      disks_messages = [attached_disk] + disks_messages
    else:
      disks_messages.append(attached_disk)

  return disks_messages, boot_disk_ref


def CreatePersistentCreateDiskMessages(compute_client,
                                       resources, csek_keys, create_disks,
                                       instance_ref, enable_kms=False,
                                       enable_snapshots=False,
                                       container_mount_disk=None):
  """Returns a list of AttachedDisk messages for newly creating disks.

  Args:
    compute_client: creates resources,
    resources: parser of resources,
    csek_keys: customer suplied encryption keys,
    create_disks: disk objects - contains following properties
             * name - the name of disk,
             * description - an optional description for the disk,
             * mode - 'rw' (R/W), 'ro' (R/O) access mode,
             * disk-size - the size of the disk,
             * disk-type - the type of the disk (HDD or SSD),
             * image - the name of the image to initialize from,
             * image-family - the image family name,
             * image-project - the project name that has the image,
             * auto-delete - whether disks is deleted when VM is deleted,
             * device-name - device name on VM,
             * source-snapshot - the snapshot to initialize from.
    instance_ref: reference to the instance that will own the new disks.
    enable_kms: True if KMS keys are supported for the disk.
    enable_snapshots: True if snapshot initialization is supported for the disk.
    container_mount_disk: list of disks to be mounted to container, if any.

  Returns:
    list of API messages for attached disks
  """
  disks_messages = []

  messages = compute_client.messages
  compute = compute_client.apitools_client
  for disk in create_disks or []:
    name = disk.get('name')

    # Resolves the mode.
    mode_value = disk.get('mode', 'rw')
    if mode_value == 'rw':
      mode = messages.AttachedDisk.ModeValueValuesEnum.READ_WRITE
    else:
      mode = messages.AttachedDisk.ModeValueValuesEnum.READ_ONLY

    auto_delete_value = disk.get('auto-delete', 'yes')
    auto_delete = auto_delete_value == 'yes'

    disk_size_gb = utils.BytesToGb(disk.get('size'))
    disk_type = disk.get('type')
    if disk_type:
      disk_type_ref = resources.Parse(disk_type,
                                      collection='compute.diskTypes',
                                      params={
                                          'project': instance_ref.project,
                                          'zone': instance_ref.zone
                                      })

      disk_type_uri = disk_type_ref.SelfLink()
    else:
      disk_type_uri = None

    img = disk.get('image')
    img_family = disk.get('image-family')
    img_project = disk.get('image-project')

    image_uri = None
    if img or img_family:
      image_expander = image_utils.ImageExpander(compute_client, resources)
      image_uri, _ = image_expander.ExpandImageFlag(
          user_project=instance_ref.project,
          image=img,
          image_family=img_family,
          image_project=img_project,
          return_image_resource=False)

    image_key = None
    disk_key = None
    if csek_keys:
      image_key = csek_utils.MaybeLookupKeyMessagesByUri(csek_keys,
                                                         resources,
                                                         [image_uri],
                                                         compute)
      if name:
        disk_ref = resources.Parse(name,
                                   collection='compute.disks',
                                   params={'zone': instance_ref.zone})
        disk_key = csek_utils.MaybeLookupKeyMessage(csek_keys, disk_ref,
                                                    compute)

    if enable_kms:
      disk_key = kms_utils.MaybeGetKmsKeyFromDict(disk, messages, disk_key)

    initialize_params = messages.AttachedDiskInitializeParams(
        diskName=name,
        description=disk.get('description'),
        sourceImage=image_uri,
        diskSizeGb=disk_size_gb,
        diskType=disk_type_uri,
        sourceImageEncryptionKey=image_key)

    if enable_snapshots:
      snapshot_name = disk.get('source-snapshot')
      attached_snapshot_uri = ResolveSnapshotURI(
          snapshot=snapshot_name, user_project=instance_ref.project,
          resource_parser=resources)
      if attached_snapshot_uri:
        initialize_params.sourceImage = None
        initialize_params.sourceSnapshot = attached_snapshot_uri

    device_name = GetDiskDeviceName(disk, name, container_mount_disk)
    create_disk = messages.AttachedDisk(
        autoDelete=auto_delete,
        boot=False,
        deviceName=device_name,
        initializeParams=initialize_params,
        mode=mode,
        type=messages.AttachedDisk.TypeValueValuesEnum.PERSISTENT,
        diskEncryptionKey=disk_key)

    disks_messages.append(create_disk)

  return disks_messages


def CreateAcceleratorConfigMessages(msgs, accelerator_type_ref,
                                    accelerator_count):
  """Returns a list of accelerator config messages.

  Args:
    msgs: tracked GCE API messages.
    accelerator_type_ref: reference to the accelerator type.
    accelerator_count: number of accelerators to attach to the VM.

  Returns:
    a list of accelerator config message that specifies the type and number of
    accelerators to attach to an instance.
  """

  accelerator_config = msgs.AcceleratorConfig(
      acceleratorType=accelerator_type_ref.SelfLink(),
      acceleratorCount=accelerator_count)
  return [accelerator_config]


def CreateDefaultBootAttachedDiskMessage(
    compute_client, resources, disk_type, disk_device_name, disk_auto_delete,
    disk_size_gb, require_csek_key_create, image_uri, instance_ref,
    csek_keys=None, kms_args=None, enable_kms=False, snapshot_uri=None):
  """Returns an AttachedDisk message for creating a new boot disk."""
  messages = compute_client.messages
  compute = compute_client.apitools_client

  if disk_type:
    disk_type_ref = resources.Parse(
        disk_type,
        collection='compute.diskTypes',
        params={
            'project': instance_ref.project,
            'zone': instance_ref.zone
        })
    disk_type_uri = disk_type_ref.SelfLink()
  else:
    disk_type_uri = None

  if csek_keys:
    # If we're going to encrypt the boot disk make sure that we select
    # a name predictably, instead of letting the API deal with name
    # conflicts automatically.
    #
    # Note that when csek keys are being used we *always* want force this
    # even if we don't have any encryption key for default disk name.
    #
    # Consider the case where the user's key file has a key for disk `foo-1`
    # and no other disk.  Assume she runs
    #   gcloud compute instances create foo --csek-key-file f \
    #       --no-require-csek-key-create
    # and gcloud doesn't force the disk name to be `foo`.  The API might
    # select name `foo-1` for the new disk, but has no way of knowing
    # that the user has a key file mapping for that disk name.  That
    # behavior violates the principle of least surprise.
    #
    # Instead it's better for gcloud to force a specific disk name in the
    # instance create, and fail if that name isn't available.

    effective_boot_disk_name = (
        disk_device_name or instance_ref.Name())

    disk_ref = resources.Parse(effective_boot_disk_name,
                               collection='compute.disks',
                               params={
                                   'project': instance_ref.project,
                                   'zone': instance_ref.zone
                               })
    disk_key_or_none = csek_utils.MaybeToMessage(
        csek_keys.LookupKey(disk_ref, require_csek_key_create),
        compute)
    [image_key_or_none] = csek_utils.MaybeLookupKeyMessagesByUri(
        csek_keys, resources, [image_uri], compute)
    kwargs_init_parms = {'sourceImageEncryptionKey': image_key_or_none}
    kwargs_disk = {'diskEncryptionKey': disk_key_or_none}
  else:
    kwargs_disk = {}
    kwargs_init_parms = {}
    effective_boot_disk_name = disk_device_name

  if enable_kms:
    kms_key = kms_utils.MaybeGetKmsKey(
        kms_args, messages, kwargs_disk.get('diskEncryptionKey', None),
        boot_disk_prefix=True)
    if kms_key:
      kwargs_disk = {'diskEncryptionKey': kms_key}

  initialize_params = messages.AttachedDiskInitializeParams(
      sourceImage=image_uri,
      diskSizeGb=disk_size_gb,
      diskType=disk_type_uri,
      **kwargs_init_parms)

  if snapshot_uri:
    initialize_params.sourceImage = None
    initialize_params.sourceSnapshot = snapshot_uri

  return messages.AttachedDisk(
      autoDelete=disk_auto_delete,
      boot=True,
      deviceName=effective_boot_disk_name,
      initializeParams=initialize_params,
      mode=messages.AttachedDisk.ModeValueValuesEnum.READ_WRITE,
      type=messages.AttachedDisk.TypeValueValuesEnum.PERSISTENT,
      **kwargs_disk)


def UseExistingBootDisk(disks):
  """Returns True if the user has specified an existing boot disk."""
  return any(disk.get('boot') == 'yes' for disk in disks)


# TODO(b/116515070) Replace `aep-nvdimm` with `local-nvdimm`
NVDIMM_DISK_TYPE = 'aep-nvdimm'


def CreateLocalNvdimmMessages(args, resources, messages, zone=None,
                              project=None):
  """Create messages representing local NVDIMMs."""
  local_nvdimms = []
  for local_nvdimm_disk in getattr(args, 'local_nvdimm', []) or []:
    local_nvdimm = _CreateLocalNvdimmMessage(
        resources,
        messages,
        local_nvdimm_disk.get('size'),
        zone,
        project
    )
    local_nvdimms.append(local_nvdimm)
  return local_nvdimms


def _CreateLocalNvdimmMessage(resources, messages, size_bytes=None, zone=None,
                              project=None):
  """Create a message representing a local NVDIMM."""

  if zone:
    disk_type_ref = resources.Parse(
        NVDIMM_DISK_TYPE,
        collection='compute.diskTypes',
        params={
            'project': project,
            'zone': zone
        })
    disk_type = disk_type_ref.SelfLink()
  else:
    disk_type = NVDIMM_DISK_TYPE

  local_nvdimm = messages.AttachedDisk(
      type=messages.AttachedDisk.TypeValueValuesEnum.SCRATCH,
      autoDelete=True,
      interface=messages.AttachedDisk.InterfaceValueValuesEnum.NVDIMM,
      mode=messages.AttachedDisk.ModeValueValuesEnum.READ_WRITE,
      initializeParams=messages.AttachedDiskInitializeParams(
          diskType=disk_type),
      )

  if size_bytes is not None:
    local_nvdimm.diskSizeGb = utils.BytesToGb(size_bytes)

  return local_nvdimm


def CreateLocalSsdMessages(args, resources, messages, zone=None,
                           project=None):
  """Create messages representing local ssds."""
  local_ssds = []
  for local_ssd_disk in getattr(args, 'local_ssd', []) or []:
    local_ssd = _CreateLocalSsdMessage(
        resources,
        messages,
        local_ssd_disk.get('device-name'),
        local_ssd_disk.get('interface'),
        local_ssd_disk.get('size'),
        zone,
        project
    )
    local_ssds.append(local_ssd)
  return local_ssds


def _CreateLocalSsdMessage(resources, messages, device_name, interface,
                           size_bytes=None, zone=None, project=None):
  """Create a message representing a local ssd."""

  if zone:
    disk_type_ref = resources.Parse(
        'local-ssd',
        collection='compute.diskTypes',
        params={
            'project': project,
            'zone': zone
        }
    )
    disk_type = disk_type_ref.SelfLink()
  else:
    disk_type = 'local-ssd'

  maybe_interface_enum = (
      messages.AttachedDisk.InterfaceValueValuesEnum(interface)
      if interface else None)

  local_ssd = messages.AttachedDisk(
      type=messages.AttachedDisk.TypeValueValuesEnum.SCRATCH,
      autoDelete=True,
      deviceName=device_name,
      interface=maybe_interface_enum,
      mode=messages.AttachedDisk.ModeValueValuesEnum.READ_WRITE,
      initializeParams=messages.AttachedDiskInitializeParams(
          diskType=disk_type),
  )

  if size_bytes is not None:
    local_ssd.diskSizeGb = utils.BytesToGb(size_bytes)

  return local_ssd


def IsAnySpecified(args, *dests):
  return any([args.IsSpecified(dest) for dest in dests])


def GetSourceInstanceTemplate(args, resources, source_instance_template_arg):
  if not args.IsSpecified('source_instance_template'):
    return None
  ref = source_instance_template_arg.ResolveAsResource(args, resources)
  return ref.SelfLink()


def GetSkipDefaults(source_instance_template):
  # gcloud creates default values for some fields in Instance resource
  # when no value was specified on command line.
  # When --source-instance-template was specified, defaults are taken from
  # Instance Template and gcloud flags are used to override them - by default
  # fields should not be initialized.
  return source_instance_template is not None


def GetScheduling(args, client, skip_defaults, support_node_affinity=False):
  """Generate a Scheduling Message or None based on specified args."""
  node_affinities = None
  if support_node_affinity:
    node_affinities = sole_tenancy_util.GetSchedulingNodeAffinityListFromArgs(
        args, client.messages)
  if (skip_defaults and
      not IsAnySpecified(
          args, 'maintenance_policy', 'preemptible', 'restart_on_failure') and
      not node_affinities):
    return None
  return CreateSchedulingMessage(
      messages=client.messages,
      maintenance_policy=args.maintenance_policy,
      preemptible=args.preemptible,
      restart_on_failure=args.restart_on_failure,
      node_affinities=node_affinities)


def GetServiceAccounts(args, client, skip_defaults):
  if args.no_service_account:
    service_account = None
  else:
    service_account = args.service_account
  if (skip_defaults and not IsAnySpecified(
      args, 'scopes', 'no_scopes', 'service_account', 'no_service_account')):
    return []
  return CreateServiceAccountMessages(
      messages=client.messages,
      scopes=[] if args.no_scopes else args.scopes,
      service_account=service_account)


def GetValidatedMetadata(args, client):
  user_metadata = metadata_utils.ConstructMetadataMessage(
      client.messages,
      metadata=args.metadata,
      metadata_from_file=args.metadata_from_file)
  containers_utils.ValidateUserMetadata(user_metadata)
  return user_metadata


def GetMetadata(args, client, skip_defaults):
  if (skip_defaults and
      not IsAnySpecified(args, 'metadata', 'metadata_from_file')):
    return None
  else:
    return metadata_utils.ConstructMetadataMessage(
        client.messages,
        metadata=args.metadata,
        metadata_from_file=args.metadata_from_file)


def GetBootDiskSizeGb(args):
  boot_disk_size_gb = utils.BytesToGb(args.boot_disk_size)
  utils.WarnIfDiskSizeIsTooSmall(boot_disk_size_gb, args.boot_disk_type)
  return boot_disk_size_gb


def GetInstanceRefs(args, client, holder):
  instance_refs = flags.INSTANCES_ARG.ResolveAsResource(
      args,
      holder.resources,
      scope_lister=compute_flags.GetDefaultScopeLister(client))
  # Check if the zone is deprecated or has maintenance coming.
  zone_resource_fetcher = zone_utils.ZoneResourceFetcher(client)
  zone_resource_fetcher.WarnForZonalCreation(instance_refs)
  return instance_refs


def GetNetworkInterfaces(
    args, client, holder, instance_refs, skip_defaults):
  """Get network interfaces."""
  if (skip_defaults and not args.IsSpecified('network') and not IsAnySpecified(
      args,
      'address',
      'network_tier',
      'no_address',
      'no_public_ptr',
      'no_public_ptr_domain',
      'private_network_ip',
      'public_ptr',
      'public_ptr_domain',
      'subnet',
  )):
    return []
  return [
      CreateNetworkInterfaceMessage(
          resources=holder.resources,
          compute_client=client,
          network=args.network,
          subnet=args.subnet,
          private_network_ip=args.private_network_ip,
          no_address=args.no_address,
          address=args.address,
          instance_refs=instance_refs,
          no_public_ptr=args.no_public_ptr,
          public_ptr=args.public_ptr,
          no_public_ptr_domain=args.no_public_ptr_domain,
          public_ptr_domain=args.public_ptr_domain,
          network_tier=getattr(args, 'network_tier', None),
      )
  ]


def GetNetworkInterfacesAlpha(
    args, client, holder, instance_refs, skip_defaults):
  if (skip_defaults and
      not IsAnySpecified(
          args, 'network', 'subnet', 'private_network_ip', 'no_address',
          'address', 'network_tier', 'no_public_dns', 'public_dns',
          'no_public_ptr', 'public_ptr', 'no_public_ptr_domain',
          'public_ptr_domain')):
    return []
  return [
      CreateNetworkInterfaceMessage(
          resources=holder.resources,
          compute_client=client,
          network=args.network,
          subnet=args.subnet,
          private_network_ip=args.private_network_ip,
          no_address=args.no_address,
          address=args.address,
          instance_refs=instance_refs,
          network_tier=getattr(args, 'network_tier', None),
          no_public_dns=getattr(args, 'no_public_dns', None),
          public_dns=getattr(args, 'public_dns', None),
          no_public_ptr=getattr(args, 'no_public_ptr', None),
          public_ptr=getattr(args, 'public_ptr', None),
          no_public_ptr_domain=getattr(args, 'no_public_ptr_domain', None),
          public_ptr_domain=getattr(args, 'public_ptr_domain', None)
      )
  ]


def GetMachineTypeUris(
    args, client, holder, instance_refs, skip_defaults):
  if (skip_defaults and
      not IsAnySpecified(args, 'machine_type', 'custom_cpu', 'custom_memory')):
    return [None for _ in instance_refs]
  return CreateMachineTypeUris(
      resources=holder.resources,
      compute_client=client,
      machine_type=args.machine_type,
      custom_cpu=args.custom_cpu,
      custom_memory=args.custom_memory,
      ext=getattr(args, 'custom_extensions', None),
      instance_refs=instance_refs)


def GetCanIpForward(args, skip_defaults):
  if skip_defaults and not args.IsSpecified('can_ip_forward'):
    return None
  return args.can_ip_forward


def CreateDiskMessages(
    holder, args, boot_disk_size_gb, image_uri, instance_ref, skip_defaults,
    match_container_mount_disks=False):
  """Creates API messages with disks attached to VM instance."""
  flags_to_check = ['create_disk', 'local_ssd',
                    'boot_disk_type', 'boot_disk_device_name',
                    'boot_disk_auto_delete']
  if hasattr(args, 'local_nvdimm'):
    flags_to_check.append('local_nvdimm')
  if (skip_defaults and not args.IsSpecified('disk') and not
      IsAnySpecified(args, *flags_to_check)):
    return []
  else:
    if match_container_mount_disks:
      container_mount_disk = args.container_mount_disk
    else:
      container_mount_disk = []
    persistent_disks, _ = (
        CreatePersistentAttachedDiskMessages(
            holder.resources, holder.client, None, args.disk or [],
            instance_ref, container_mount_disk=container_mount_disk))
    persistent_create_disks = (
        CreatePersistentCreateDiskMessages(
            holder.client, holder.resources, None,
            getattr(args, 'create_disk', []), instance_ref,
            container_mount_disk=container_mount_disk))
    local_nvdimms = CreateLocalNvdimmMessages(
        args,
        holder.resources,
        holder.client.messages,
        instance_ref.zone,
        instance_ref.project)
    local_ssds = CreateLocalSsdMessages(
        args,
        holder.resources,
        holder.client.messages,
        instance_ref.zone,
        instance_ref.project)
    boot_disk = CreateDefaultBootAttachedDiskMessage(
        holder.client, holder.resources,
        disk_type=args.boot_disk_type,
        disk_device_name=args.boot_disk_device_name,
        disk_auto_delete=args.boot_disk_auto_delete,
        disk_size_gb=boot_disk_size_gb,
        require_csek_key_create=None,
        image_uri=image_uri,
        instance_ref=instance_ref,
        csek_keys=None)
    return (
        [boot_disk] + persistent_disks + persistent_create_disks + local_nvdimms
        + local_ssds)


def GetTags(args, client):
  if args.tags:
    return client.messages.Tags(items=args.tags)
  return None


def GetLabels(args, client):
  if args.labels:
    return client.messages.Instance.LabelsValue(additionalProperties=[
        client.messages.Instance.LabelsValue.AdditionalProperty(
            key=key, value=value)
        for key, value in sorted(six.iteritems(args.labels))
    ])
  return None


def GetAccelerators(args, client, resource_parser, instance_ref):
  """Returns list of messages with accelerators for the instance."""
  if args.accelerator:
    accelerator_type_name = args.accelerator['type']
    accelerator_type_ref = resource_parser.Parse(
        accelerator_type_name,
        collection='compute.acceleratorTypes',
        params={'project': instance_ref.project,
                'zone': instance_ref.zone})
    # Accelerator count is default to 1.
    accelerator_count = int(args.accelerator.get('count', 1))
    return CreateAcceleratorConfigMessages(
        client.messages, accelerator_type_ref, accelerator_count)
  return []


def ResolveSnapshotURI(user_project, snapshot, resource_parser):
  if user_project and snapshot and resource_parser:
    snapshot_ref = resource_parser.Parse(snapshot,
                                         collection='compute.snapshots',
                                         params={'project': user_project})
    return snapshot_ref.SelfLink()
  return None


def GetAllocationAffinity(args, client):
  """Returns the message of allocation affinity for the instance."""
  if args.IsSpecified('allocation_affinity'):
    type_msgs = (client.messages.
                 AllocationAffinity.ConsumeAllocationTypeValueValuesEnum)

    if args.allocation_affinity == 'none':
      allocation_type = type_msgs.NO_ALLOCATION
      allocation_key = None
      allocation_values = []
    elif args.allocation_affinity == 'specific':
      allocation_type = type_msgs.SPECIFIC_ALLOCATION
      # Currently, the key is fixed and the value is the name of the allocation.
      # The value being a repeated field is reserved for future use when user
      # can specify more than one allocation names from which the Vm can take
      # capacity from.
      allocation_key = args.allocation_label.get('key', None)
      allocation_values = [args.allocation_label.get('value', None)]
    else:
      allocation_type = type_msgs.ANY_ALLOCATION
      allocation_key = None
      allocation_values = []

    return client.messages.AllocationAffinity(
        consumeAllocationType=allocation_type,
        key=allocation_key,
        values=allocation_values)

  return None
