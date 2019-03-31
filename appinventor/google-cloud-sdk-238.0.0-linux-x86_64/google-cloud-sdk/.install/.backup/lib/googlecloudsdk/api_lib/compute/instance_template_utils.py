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
"""Convenience functions for dealing with instance templates."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import alias_ip_range_utils
from googlecloudsdk.api_lib.compute import constants
from googlecloudsdk.api_lib.compute import image_utils
from googlecloudsdk.api_lib.compute import instance_utils
from googlecloudsdk.api_lib.compute import kms_utils
from googlecloudsdk.api_lib.compute import utils
from googlecloudsdk.command_lib.compute import scope as compute_scope
from googlecloudsdk.command_lib.compute.networks.subnets import flags as subnet_flags
from googlecloudsdk.core import properties

EPHEMERAL_ADDRESS = object()


def CreateNetworkInterfaceMessage(
    resources, scope_lister, messages, network, region, subnet, address,
    alias_ip_ranges_string=None, network_tier=None):
  """Creates and returns a new NetworkInterface message.

  Args:
    resources: generates resource references,
    scope_lister: function, provides scopes for prompting subnet region,
    messages: GCE API messages,
    network: network,
    region: region for subnetwork,
    subnet: regional subnetwork,
    address: specify static address for instance template
               * None - no address,
               * EPHEMERAL_ADDRESS - ephemeral address,
               * string - address name to be fetched from GCE API.
    alias_ip_ranges_string: command line string specifying a list of alias
        IP ranges.
    network_tier: specify network tier for instance template
               * None - no network tier
               * PREMIUM - network tier being PREMIUM
               * SELECT - network tier being SELECT
               * STANDARD - network tier being STANDARD
  Returns:
    network_interface: a NetworkInterface message object
  """
  # By default interface is attached to default network. If network or subnet
  # are specified they're used instead.
  network_interface = messages.NetworkInterface()
  if subnet is not None:
    subnet_ref = subnet_flags.SubnetworkResolver().ResolveResources(
        [subnet], compute_scope.ScopeEnum.REGION, region, resources,
        scope_lister=scope_lister)[0]
    network_interface.subnetwork = subnet_ref.SelfLink()
  if network is not None:
    network_ref = resources.Parse(
        network,
        params={'project': properties.VALUES.core.project.GetOrFail},
        collection='compute.networks')
    network_interface.network = network_ref.SelfLink()
  elif subnet is None:
    network_ref = resources.Parse(
        constants.DEFAULT_NETWORK,
        params={'project': properties.VALUES.core.project.GetOrFail},
        collection='compute.networks')
    network_interface.network = network_ref.SelfLink()

  if address:
    access_config = messages.AccessConfig(
        name=constants.DEFAULT_ACCESS_CONFIG_NAME,
        type=messages.AccessConfig.TypeValueValuesEnum.ONE_TO_ONE_NAT)

    # If the user provided an external IP, populate the access
    # config with it.
    if address != EPHEMERAL_ADDRESS:
      access_config.natIP = address

    if network_tier is not None:
      access_config.networkTier = (messages.AccessConfig.
                                   NetworkTierValueValuesEnum(network_tier))

    network_interface.accessConfigs = [access_config]

  if alias_ip_ranges_string:
    network_interface.aliasIpRanges = (
        alias_ip_range_utils.CreateAliasIpRangeMessagesFromString(
            messages, False, alias_ip_ranges_string))

  return network_interface


def CreateNetworkInterfaceMessages(resources, scope_lister, messages,
                                   network_interface_arg, region):
  """Create network interface messages.

  Args:
    resources: generates resource references,
    scope_lister: function, provides scopes for prompting subnet region,
    messages: creates resources.
    network_interface_arg: CLI argument specifying network interfaces.
    region: region of the subnetwork.
  Returns:
    list, items are NetworkInterfaceMessages.
  """
  result = []
  if network_interface_arg:
    for interface in network_interface_arg:
      address = interface.get('address', None)
      # pylint: disable=g-explicit-bool-comparison
      if address == '':
        address = EPHEMERAL_ADDRESS

      network_tier = interface.get('network-tier', None)

      result.append(CreateNetworkInterfaceMessage(
          resources, scope_lister, messages, interface.get('network', None),
          region,
          interface.get('subnet', None),
          address,
          interface.get('aliases', None),
          network_tier))
  return result


def CreatePersistentAttachedDiskMessages(
    messages, disks, container_mount_disk=None):
  """Returns a list of AttachedDisk messages and the boot disk's reference.

  Args:
    messages: GCE API messages,
    disks: disk objects - contains following properties
             * name - the name of disk,
             * mode - 'rw' (R/W), 'ro' (R/O) access mode,
             * boot - whether it is a boot disk ('yes' if True),
             * autodelete - whether disks is deleted when VM is deleted ('yes'
               if True),
             * device-name - device name on VM.
    container_mount_disk: list of disks to be mounted to container, if any.

  Returns:
    list of API messages for attached disks
  """

  disks_messages = []
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
    device_name = instance_utils.GetDiskDeviceName(disk, name,
                                                   container_mount_disk)

    attached_disk = messages.AttachedDisk(
        autoDelete=auto_delete,
        boot=boot,
        deviceName=device_name,
        mode=mode,
        source=name,
        type=messages.AttachedDisk.TypeValueValuesEnum.PERSISTENT)

    # The boot disk must end up at index 0.
    if boot:
      disks_messages = [attached_disk] + disks_messages
    else:
      disks_messages.append(attached_disk)

  return disks_messages


def CreatePersistentCreateDiskMessages(
    client, resources, user_project, create_disks, support_kms=False,
    container_mount_disk=None):
  """Returns a list of AttachedDisk messages.

  Args:
    client: Compute client adapter
    resources: Compute resources registry
    user_project: name of user project
    create_disks: disk objects - contains following properties
             * name - the name of disk,
             * description - an optional description for the disk,
             * mode - 'rw' (R/W), 'ro' (R/O) access mode,
             * size - the size of the disk,
             * type - the type of the disk (HDD or SSD),
             * image - the name of the image to initialize from,
             * image-family - the image family name,
             * image-project - the project name that has the image,
             * auto-delete - whether disks is deleted when VM is deleted ('yes'
               if True),
             * device-name - device name on VM.
    support_kms: if KMS is supported
    container_mount_disk: list of disks to be mounted to container, if any.

  Returns:
    list of API messages for attached disks
  """

  disks_messages = []
  for disk in create_disks or []:
    name = disk.get('name')
    # Resolves the mode.
    mode_value = disk.get('mode', 'rw')
    if mode_value == 'rw':
      mode = client.messages.AttachedDisk.ModeValueValuesEnum.READ_WRITE
    else:
      mode = client.messages.AttachedDisk.ModeValueValuesEnum.READ_ONLY

    auto_delete = disk.get('auto-delete') == 'yes'
    disk_size_gb = utils.BytesToGb(disk.get('size'))
    img = disk.get('image')
    img_family = disk.get('image-family')
    img_project = disk.get('image-project')

    image_uri = None
    if img or img_family:
      image_expander = image_utils.ImageExpander(client, resources)
      image_uri, _ = image_expander.ExpandImageFlag(
          user_project=user_project,
          image=img,
          image_family=img_family,
          image_project=img_project,
          return_image_resource=False)

    disk_key = None
    if support_kms:
      disk_key = kms_utils.MaybeGetKmsKeyFromDict(
          disk, client.messages, disk_key)

    device_name = instance_utils.GetDiskDeviceName(disk, name,
                                                   container_mount_disk)

    create_disk = client.messages.AttachedDisk(
        autoDelete=auto_delete,
        boot=False,
        deviceName=device_name,
        initializeParams=client.messages.AttachedDiskInitializeParams(
            diskName=name,
            description=disk.get('description'),
            sourceImage=image_uri,
            diskSizeGb=disk_size_gb,
            diskType=disk.get('type')),
        mode=mode,
        type=client.messages.AttachedDisk.TypeValueValuesEnum.PERSISTENT,
        diskEncryptionKey=disk_key)

    disks_messages.append(create_disk)

  return disks_messages


def CreateDefaultBootAttachedDiskMessage(
    messages, disk_type, disk_device_name, disk_auto_delete, disk_size_gb,
    image_uri, kms_args=None, support_kms=False):
  """Returns an AttachedDisk message for creating a new boot disk."""
  disk_key = None

  if support_kms:
    disk_key = kms_utils.MaybeGetKmsKey(
        kms_args, messages, disk_key, boot_disk_prefix=True)

  return messages.AttachedDisk(
      autoDelete=disk_auto_delete,
      boot=True,
      deviceName=disk_device_name,
      initializeParams=messages.AttachedDiskInitializeParams(
          sourceImage=image_uri,
          diskSizeGb=disk_size_gb,
          diskType=disk_type),
      mode=messages.AttachedDisk.ModeValueValuesEnum.READ_WRITE,
      type=messages.AttachedDisk.TypeValueValuesEnum.PERSISTENT,
      diskEncryptionKey=disk_key)


def CreateAcceleratorConfigMessages(messages, accelerator):
  """Returns a list of accelerator config messages for Instance Templates.

  Args:
    messages: tracked GCE API messages.
    accelerator: accelerator object with the following properties:
        * type: the accelerator's type.
        * count: the number of accelerators to attach. Optional, defaults to 1.

  Returns:
    a list of accelerator config messages that specify the type and number of
    accelerators to attach to an instance.
  """
  if accelerator is None:
    return []

  accelerator_type = accelerator['type']
  accelerator_count = int(accelerator.get('count', 1))
  accelerator_config = messages.AcceleratorConfig(
      acceleratorType=accelerator_type, acceleratorCount=accelerator_count)
  return [accelerator_config]
