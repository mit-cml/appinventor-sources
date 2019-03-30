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
"""Flags and helpers for the compute VM instances commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import copy
import functools

from googlecloudsdk.api_lib.compute import constants
from googlecloudsdk.api_lib.compute import containers_utils
from googlecloudsdk.api_lib.compute import image_utils
from googlecloudsdk.api_lib.compute import kms_utils
from googlecloudsdk.api_lib.compute import utils
from googlecloudsdk.api_lib.compute.zones import service as zones_service
from googlecloudsdk.api_lib.util import apis
from googlecloudsdk.calliope import actions
from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.calliope import exceptions
from googlecloudsdk.command_lib.compute import completers as compute_completers
from googlecloudsdk.command_lib.compute import flags as compute_flags
from googlecloudsdk.command_lib.compute import scope as compute_scope
from googlecloudsdk.command_lib.compute.kms import resource_args as kms_resource_args
from googlecloudsdk.core import log
from googlecloudsdk.core import properties
from googlecloudsdk.core import resources as core_resources
import ipaddress
import six

ZONE_PROPERTY_EXPLANATION = """\
If not specified, you may be prompted to select a zone. `gcloud` will attempt
to identify the zone by searching for resources in your project. If the zone
cannot be determined, you will then be prompted with all Google Cloud
Platform zones.

To avoid prompting when this flag is omitted, you can set the
``compute/zone'' property:

  $ gcloud config set compute/zone ZONE

A list of zones can be fetched by running:

  $ gcloud compute zones list

To unset the property, run:

  $ gcloud config unset compute/zone

Alternatively, the zone can be stored in the environment variable
``CLOUDSDK_COMPUTE_ZONE''.
"""

MIGRATION_OPTIONS = {
    'MIGRATE': (
        'The instances should be migrated to a new host. This will temporarily '
        'impact the performance of instances during a migration event.'),
    'TERMINATE': 'The instances should be terminated.',
}

LOCAL_SSD_INTERFACES = ['NVME', 'SCSI']

DISK_METAVAR = (
    'name=NAME [mode={ro,rw}] [boot={yes,no}] [device-name=DEVICE_NAME] '
    '[auto-delete={yes,no}]')

DISK_METAVAR_ZONAL_OR_REGIONAL = (
    'name=NAME [mode={ro,rw}] [boot={yes,no}] [device-name=DEVICE_NAME] '
    '[auto-delete={yes,no}] [scope={zonal,regional}]')

DEFAULT_LIST_FORMAT = """\
    table(
      name,
      zone.basename(),
      machineType.machine_type().basename(),
      scheduling.preemptible.yesno(yes=true, no=''),
      networkInterfaces[].networkIP.notnull().list():label=INTERNAL_IP,
      networkInterfaces[].accessConfigs[0].natIP.notnull().list()\
      :label=EXTERNAL_IP,
      status
    )"""

INSTANCE_ARG = compute_flags.ResourceArgument(
    resource_name='instance',
    name='instance_name',
    completer=compute_completers.InstancesCompleter,
    zonal_collection='compute.instances',
    zone_explanation=ZONE_PROPERTY_EXPLANATION)

INSTANCES_ARG = compute_flags.ResourceArgument(
    resource_name='instance',
    name='instance_names',
    completer=compute_completers.InstancesCompleter,
    zonal_collection='compute.instances',
    zone_explanation=ZONE_PROPERTY_EXPLANATION,
    plural=True)

INSTANCES_ARG_FOR_CREATE = compute_flags.ResourceArgument(
    resource_name='instance',
    name='instance_names',
    completer=compute_completers.InstancesCompleter,
    zonal_collection='compute.instances',
    zone_explanation=compute_flags.ZONE_PROPERTY_EXPLANATION,
    plural=True)

SSH_INSTANCE_RESOLVER = compute_flags.ResourceResolver.FromMap(
    'instance', {compute_scope.ScopeEnum.ZONE: 'compute.instances'})


def GetInstanceZoneScopeLister(compute_client):
  return functools.partial(InstanceZoneScopeLister, compute_client)


def InstanceZoneScopeLister(compute_client, _, underspecified_names):
  """Scope lister for zones of underspecified instances."""
  messages = compute_client.messages
  instance_name = underspecified_names[0]
  project = properties.VALUES.core.project.Get(required=True)
  # TODO(b/33813901): look in cache if possible
  request = (compute_client.apitools_client.instances,
             'AggregatedList',
             messages.ComputeInstancesAggregatedListRequest(
                 filter='name eq ^{0}$'.format(instance_name),
                 project=project,
                 maxResults=constants.MAX_RESULTS_PER_PAGE))
  errors = []
  matching_instances = compute_client.MakeRequests([request],
                                                   errors_to_collect=errors)
  zones = []
  if errors:
    # Fall back to displaying all possible zones if can't resolve
    log.debug('Errors fetching filtered aggregate list:\n{}'.format(errors))
    log.status.Print(
        'Error fetching possible zones for instance: [{0}].'.format(
            ', '.join(underspecified_names)))
    zones = zones_service.List(compute_client, project)
  elif not matching_instances:
    # Fall back to displaying all possible zones if can't resolve
    log.debug('Errors fetching filtered aggregate list:\n{}'.format(errors))
    log.status.Print(
        'Unable to find an instance with name [{0}].'.format(instance_name))
    zones = zones_service.List(compute_client, project)
  else:
    for i in matching_instances:
      zone = core_resources.REGISTRY.Parse(
          i.zone, collection='compute.zones', params={'project': project})
      zones.append(messages.Zone(name=zone.Name()))
  return {compute_scope.ScopeEnum.ZONE: zones}


def InstanceArgumentForRoute(required=True):
  return compute_flags.ResourceArgument(
      resource_name='instance',
      name='--next-hop-instance',
      completer=compute_completers.InstancesCompleter,
      required=required,
      zonal_collection='compute.instances',
      zone_explanation=ZONE_PROPERTY_EXPLANATION)


def InstanceArgumentForTargetInstance(required=True):
  return compute_flags.ResourceArgument(
      resource_name='instance',
      name='--instance',
      completer=compute_completers.InstancesCompleter,
      required=required,
      zonal_collection='compute.instances',
      short_help=('The name of the virtual machine instance that will handle '
                  'the traffic.'),
      zone_explanation=(
          'If not specified, it will be set to the same as zone.'))


def InstanceArgumentForTargetPool(action, required=True):
  return compute_flags.ResourceArgument(
      resource_name='instance',
      name='--instances',
      completer=compute_completers.InstancesCompleter,
      required=required,
      zonal_collection='compute.instances',
      short_help=(
          'Specifies a list of instances to {0} the target pool.'.format(action)
      ),
      plural=True,
      zone_explanation=compute_flags.ZONE_PROPERTY_EXPLANATION)


def MakeSourceInstanceTemplateArg():
  return compute_flags.ResourceArgument(
      name='--source-instance-template',
      resource_name='instance template',
      completer=compute_completers.InstanceTemplatesCompleter,
      required=False,
      global_collection='compute.instanceTemplates',
      short_help=('The name of the instance template that the instance will '
                  'be created from.\n\nUsers can also override machine '
                  'type and labels. Values of other flags will be ignored and '
                  '`--source-instance-template` will be used instead.'))


def AddMachineImageArg():
  return compute_flags.ResourceArgument(
      name='--source-machine-image',
      resource_name='machine image',
      completer=compute_completers.MachineImagesCompleter,
      required=False,
      global_collection='compute.machineImages',
      short_help=('The name of the machine image that the instance will '
                  'be created from.'))


def AddImageArgs(parser, enable_snapshots=False):
  """Adds arguments related to images for instances and instance-templates."""

  def AddImageHelp():
    """Returns the detailed help for the `--image` flag."""
    return """
          Specifies the boot image for the instances. For each
          instance, a new boot disk will be created from the given
          image. Each boot disk will have the same name as the
          instance. To view a list of public images and projects, run
          `$ gcloud compute images list`. It is best practice to use `--image`
          when a specific version of an image is needed.

          When using this option, ``--boot-disk-device-name'' and
          ``--boot-disk-size'' can be used to override the boot disk's
          device name and size, respectively.
          """

  image_parent_group = parser.add_group()
  image_group = image_parent_group.add_mutually_exclusive_group()
  image_group.add_argument(
      '--image',
      help=AddImageHelp,
      metavar='IMAGE')
  image_utils.AddImageProjectFlag(image_parent_group)

  image_group.add_argument(
      '--image-family',
      help="""\
      The family of the image that the boot disk will be initialized
      with. When a family is specified instead of an image, the latest
      non-deprecated image associated with that family is used. It is best
      practice to use `--image-family` when the latest version of an image is
      needed.

      By default, ``{default_image_family}'' is assumed for this flag.
      """.format(default_image_family=constants.DEFAULT_IMAGE_FAMILY))
  if enable_snapshots:
    image_group.add_argument(
        '--source-snapshot',
        help="""\
        The name of the source disk snapshot that the instance boot disk
        will be created from. You can provide this as a full URL
        to the snapshot or just the snapshot name. For example, the following
        are valid values:
          * https://www.googleapis.com/compute/v1/projects/myproject/global/snapshots/snapshot
          * snapshot
        """)


def AddCanIpForwardArgs(parser):
  parser.add_argument(
      '--can-ip-forward',
      action='store_true',
      help=('If provided, allows the instances to send and receive packets '
            'with non-matching destination or source IP addresses.'))


def AddLocalSsdArgs(parser):
  """Adds local SSD argument for instances and instance-templates."""

  parser.add_argument(
      '--local-ssd',
      type=arg_parsers.ArgDict(spec={
          'device-name': str,
          'interface': (lambda x: x.upper()),
      }),
      action='append',
      help="""\
      Attaches a local SSD to the instances.

      *device-name*::: Optional. A name that indicates the disk name
      the guest operating system will see.  If omitted, a device name
      of the form ``local-ssd-N'' will be used.

      *interface*::: Optional. The kind of disk interface exposed to the VM
      for this SSD.  Valid values are ``SCSI'' and ``NVME''.  SCSI is
      the default and is supported by more guest operating systems.  NVME
      may provide higher performance.
      """)


def AddLocalNvdimmArgs(parser):
  """Adds local NVDIMM argument for instances and instance-templates."""

  parser.add_argument(
      '--local-nvdimm',
      type=arg_parsers.ArgDict(spec={
          'size': arg_parsers.BinarySize(),
      }),
      action='append',
      help="""\
      Attaches a local NVDIMM to the instances.

      *size*::: Optional. Size of the NVDIMM disk. The value must be a whole
      number followed by a size unit of ``KB'' for kilobyte, ``MB'' for
      megabyte, ``GB'' for gigabyte, or ``TB'' for terabyte. For example,
      ``3TB'' will produce a 3 terabyte disk. Allowed values are: 3TB and 6TB
      and the default is 3 TB.
      """)


def AddLocalSsdArgsWithSize(parser):
  """Adds local SSD argument for instances and instance-templates."""

  parser.add_argument(
      '--local-ssd',
      type=arg_parsers.ArgDict(spec={
          'device-name': str,
          'interface': (lambda x: x.upper()),
          'size': arg_parsers.BinarySize(lower_bound='375GB'),
      }),
      action='append',
      help="""\
      Attaches a local SSD to the instances.

      This flag is currently in BETA and may change without notice.

      *device-name*::: Optional. A name that indicates the disk name
      the guest operating system will see.  If omitted, a device name
      of the form ``local-ssd-N'' will be used.

      *interface*::: Optional. The kind of disk interface exposed to the VM
      for this SSD.  Valid values are ``SCSI'' and ``NVME''.  SCSI is
      the default and is supported by more guest operating systems.  NVME
      may provide higher performance.

      *size*::: Optional. Size of the SSD disk. The value must be a whole number
      followed by a size unit of ``KB'' for kilobyte, ``MB'' for megabyte,
      ``GB'' for gigabyte, or ``TB'' for terabyte. For example, ``750GB'' will
      produce a 750 gigabyte disk. The size must be a multiple of 375 GB and
      the default is 375 GB. For Alpha API only.
      """)


def _GetDiskDeviceNameHelp(container_mount_enabled=False):
  """Helper to get documentation for "device-name" param of disk spec."""
  if container_mount_enabled:
    return (
        'An optional name that indicates the disk name the guest operating '
        'system will see. Must be the same as `name` if used with '
        '`--container-mount-disk`. If omitted, a device name of the form '
        '`persistent-disk-N` will be used. If omitted and used with '
        '`--container-mount-disk` (where the `name` of the container mount '
        'disk is the same as in this flag), a device name equal to disk `name` '
        'will be used.')
  else:
    return (
        'An optional name that indicates the disk name the guest operating '
        'system will see. If omitted, a device name of the form '
        '`persistent-disk-N` will be used.')


def AddDiskArgs(parser, enable_regional_disks=False, enable_kms=False,
                container_mount_enabled=False):
  """Adds arguments related to disks for instances and instance-templates."""

  disk_device_name_help = _GetDiskDeviceNameHelp(
      container_mount_enabled=container_mount_enabled)

  parser.add_argument(
      '--boot-disk-device-name',
      help="""\
      The name the guest operating system will see for the boot disk.  This
      option can only be specified if a new boot disk is being created (as
      opposed to mounting an existing persistent disk).
      """)
  parser.add_argument(
      '--boot-disk-size',
      type=arg_parsers.BinarySize(lower_bound='10GB'),
      help="""\
      The size of the boot disk. This option can only be specified if a new
      boot disk is being created (as opposed to mounting an existing
      persistent disk). The value must be a whole number followed by a size
      unit of ``KB'' for kilobyte, ``MB'' for megabyte, ``GB'' for gigabyte,
      or ``TB'' for terabyte. For example, ``10GB'' will produce a 10 gigabyte
      disk. The minimum size a boot disk can have is 10 GB. Disk size must be a
      multiple of 1 GB. Limit your boot disk size to 2TB to account for MBR
      partition table limitations.
      """)

  parser.add_argument(
      '--boot-disk-type',
      help="""\
      The type of the boot disk. This option can only be specified if a new boot
      disk is being created (as opposed to mounting an existing persistent
      disk). To get a list of available disk types, run
      `$ gcloud compute disk-types list`.
      """)

  parser.add_argument(
      '--boot-disk-auto-delete',
      action='store_true',
      default=True,
      help='Automatically delete boot disks when their instances are deleted.')

  if enable_kms:
    kms_resource_args.AddKmsKeyResourceArg(
        parser, 'disk', boot_disk_prefix=True)

  disk_arg_spec = {
      'name': str,
      'mode': str,
      'boot': str,
      'device-name': str,
      'auto-delete': str,
  }

  if enable_regional_disks:
    disk_arg_spec['scope'] = str

  disk_help = """
      Attaches persistent disks to the instances. The disks
      specified must already exist.

      *name*::: The disk to attach to the instances. When creating
      more than one instance and using this property, the only valid
      mode for attaching the disk is read-only (see *mode* below).

      *mode*::: Specifies the mode of the disk. Supported options
      are ``ro'' for read-only and ``rw'' for read-write. If
      omitted, ``rw'' is used as a default. It is an error for mode
      to be ``rw'' when creating more than one instance because
      read-write disks can only be attached to a single instance.

      *boot*::: If ``yes'', indicates that this is a boot disk. The
      virtual machines will use the first partition of the disk for
      their root file systems. The default value for this is ``no''.

      *device-name*::: {}

      *auto-delete*::: If ``yes'',  this persistent disk will be
      automatically deleted when the instance is deleted. However,
      if the disk is later detached from the instance, this option
      won't apply. The default value for this is ``no''.
      """.format(disk_device_name_help)
  if enable_regional_disks:
    disk_help += """
      *scope*::: Can be `zonal` or `regional`. If ``zonal'', the disk is
      interpreted as a zonal disk in the same zone as the instance (default).
      If ``regional'', the disk is interpreted as a regional disk in the same
      region as the instance. The default value for this is ``zonal''.
      """

  parser.add_argument(
      '--disk',
      type=arg_parsers.ArgDict(spec=disk_arg_spec),
      action='append',
      help=disk_help)


def AddCreateDiskArgs(parser, enable_kms=False, enable_snapshots=False,
                      container_mount_enabled=False):
  """Adds create-disk argument for instances and instance-templates."""

  disk_device_name_help = _GetDiskDeviceNameHelp(
      container_mount_enabled=container_mount_enabled)
  disk_name_extra_help = '' if not container_mount_enabled else (
      ' Must specify this option if attaching the disk '
      'to a container with `--container-mount-disk`.')
  disk_mode_extra_help = '' if not container_mount_enabled else (
      ' It is an error to create a disk in `ro` mode '
      'if attaching it to a container with `--container-mount-disk`.')

  disk_help = """\
      Creates and attaches persistent disks to the instances.

      *name*::: Specifies the name of the disk. This option cannot be
      specified if more than one instance is being created.{}

      *description*::: Optional textual description for the disk being created.

      *mode*::: Specifies the mode of the disk. Supported options
      are ``ro'' for read-only and ``rw'' for read-write. If
      omitted, ``rw'' is used as a default.{}

      *image*::: Specifies the name of the image that the disk will be
      initialized with. A new disk will be created based on the given
      image. To view a list of public images and projects, run
      `$ gcloud compute images list`. It is best practice to use image when
      a specific version of an image is needed. If both image and image-family
      flags are omitted a blank disk will be created.

      *image-family*::: The family of the image that the disk will be
      initialized with. When a family is specified instead of an image,
      the latest non-deprecated image associated with that family is
      used. It is best practice to use image-family when the latest version
      of an image is needed.

      *image-project*::: The project that the image or image family
      belongs to. It is best practice to define image-project.
          * If specifying one of our public images, image-project must be
            provided.
          * If there are several of the same image-family value in multiple
            projects, image-project must be specified to clarify the image to
            be used.
          * If not specified and either image or image-family is provided, the
            current default project is used.

      *size*::: The size of the disk. The value must be a whole number
      followed by a size unit of ``KB'' for kilobyte, ``MB'' for
      megabyte, ``GB'' for gigabyte, or ``TB'' for terabyte. For
      example, ``10GB'' will produce a 10 gigabyte disk. Disk size must
      be a multiple of 1 GB. If not specified, the default image size
      will be used for the new disk.

      *type*::: The type of the disk. To get a list of available disk
      types, run $ gcloud compute disk-types list. The default disk type
      is ``pd-standard''.

      *device-name*::: {}

      *auto-delete*::: If ``yes'',  this persistent disk will be
      automatically deleted when the instance is deleted. However,
      if the disk is later detached from the instance, this option
      won't apply. The default value for this is ``no''.
      """.format(disk_name_extra_help, disk_mode_extra_help,
                 disk_device_name_help)
  if enable_kms:
    disk_help += """
      *kms-key*::: Fully qualified Cloud KMS cryptokey name that will
      protect the {resource}.
      This can either be the fully qualified path or the name.
      The fully qualified Cloud KMS cryptokey name format is:
      ``projects/<kms-project>/locations/<kms-location>/keyRings/<kms-keyring>/
      cryptoKeys/<key-name>''.
      If the value is not fully qualified then kms-location, kms-keyring, and
      optionally kms-project are required.
      See {kms_help} for more details.

      *kms-project*::: Project that contains the Cloud KMS cryptokey that will
      protect the {resource}.
      If the project is not specified then the project where the {resource} is
      being created will be used.
      If this flag is set then key-location, kms-keyring, and kms-key
      are required.
      See {kms_help} for more details.

      *kms-location*::: Location of the Cloud KMS cryptokey to be used for
      protecting the {resource}.
      All Cloud KMS cryptokeys are reside in a 'location'.
      To get a list of possible locations run 'gcloud kms locations list'.
      If this flag is set then kms-keyring and kms-key are required.
      See {kms_help} for more details.

      *kms-keyring*::: The keyring which contains the Cloud KMS cryptokey that
      will protect the {resource}.
      If this flag is set then kms-location and kms-key are required.
      See {kms_help} for more details.
      """.format(resource='disk', kms_help=kms_utils.KMS_HELP_URL)
  spec = {
      'name': str,
      'description': str,
      'mode': str,
      'image': str,
      'image-family': str,
      'image-project': str,
      'size': arg_parsers.BinarySize(lower_bound='1GB'),
      'type': str,
      'device-name': str,
      'auto-delete': str,
  }
  if enable_kms:
    spec['kms-key'] = str
    spec['kms-project'] = str
    spec['kms-location'] = str
    spec['kms-keyring'] = str

  if enable_snapshots:
    disk_help += """
      *source-snapshot*::: The source disk snapshot that will be used to
      create the disk. You can provide this as a full URL
      to the snapshot or just the snapshot name. For example, the following
      are valid values:
        * https://www.googleapis.com/compute/v1/projects/myproject/global/snapshots/snapshot
        * snapshot
      """
    spec['source-snapshot'] = str

  parser.add_argument(
      '--create-disk',
      type=arg_parsers.ArgDict(spec=spec),
      action='append',
      metavar='PROPERTY=VALUE',
      help=disk_help)


def AddCustomMachineTypeArgs(parser):
  """Adds arguments related to custom machine types for instances."""
  custom_group = parser.add_group(
      help='Custom machine type extensions.')
  custom_group.add_argument(
      '--custom-cpu',
      type=int,
      required=True,
      help="""\
      A whole number value indicating how many cores are desired in the custom
      machine type.
      """)
  custom_group.add_argument(
      '--custom-memory',
      type=arg_parsers.BinarySize(),
      required=True,
      help="""\
      A whole number value indicating how much memory is desired in the custom
      machine type. A size unit should be provided (eg. 3072MB or 9GB) - if no
      units are specified, GB is assumed.
      """)
  custom_group.add_argument(
      '--custom-extensions',
      action='store_true',
      help='Use the extended custom machine type.')


def _GetAddress(compute_client, address_ref):
  """Returns the address resource corresponding to the given reference.

  Args:
    compute_client: GCE API client,
    address_ref: resource reference to reserved IP address

  Returns:
    GCE reserved IP address resource
  """
  errors = []
  messages = compute_client.messages
  compute = compute_client.apitools_client
  res = compute_client.MakeRequests(
      requests=[(compute.addresses,
                 'Get',
                 messages.ComputeAddressesGetRequest(
                     address=address_ref.Name(),
                     project=address_ref.project,
                     region=address_ref.region))],
      errors_to_collect=errors)
  if errors:
    utils.RaiseToolException(
        errors,
        error_message='Could not fetch address resource:')
  return res[0]


def ExpandAddressFlag(resources, compute_client, address, region):
  """Resolves the --address flag value.

  If the value of --address is a name, the regional address is queried.

  Args:
    resources: resources object,
    compute_client: GCE API client,
    address: The command-line flags. The flag accessed is --address,
    region: The region.

  Returns:
    If an --address is given, the resolved IP address; otherwise None.
  """
  if not address:
    return None

  # Try interpreting the address as IPv4 or IPv6.
  try:
    # ipaddress only allows unicode input
    ipaddress.ip_address(six.text_type(address))
    return address
  except ValueError:
    # ipaddress could not resolve as an IPv4 or IPv6 address.
    pass

  # Lookup the address.
  address_ref = GetAddressRef(resources, address, region)
  res = _GetAddress(compute_client, address_ref)
  return res.address


def GetAddressRef(resources, address, region):
  """Generates an address reference from the specified address and region."""
  return resources.Parse(
      address,
      collection='compute.addresses',
      params={
          'project': properties.VALUES.core.project.GetOrFail,
          'region': region
      })


def ValidateDiskFlags(args, enable_kms=False, enable_snapshots=False):
  """Validates the values of all disk-related flags."""
  ValidateDiskCommonFlags(args)
  ValidateDiskAccessModeFlags(args)
  ValidateDiskBootFlags(args, enable_kms=enable_kms)
  ValidateCreateDiskFlags(args, enable_snapshots=enable_snapshots)


def ValidateDiskCommonFlags(args):
  """Validates the values of common disk-related flags."""

  for disk in args.disk or []:
    disk_name = disk.get('name')
    if not disk_name:
      raise exceptions.ToolException(
          '[name] is missing in [--disk]. [--disk] value must be of the form '
          '[{0}].'.format(DISK_METAVAR))

    mode_value = disk.get('mode')
    if mode_value and mode_value not in ('rw', 'ro'):
      raise exceptions.ToolException(
          'Value for [mode] in [--disk] must be [rw] or [ro], not [{0}].'
          .format(mode_value))

    auto_delete_value = disk.get('auto-delete')
    if auto_delete_value and auto_delete_value not in ['yes', 'no']:
      raise exceptions.ToolException(
          'Value for [auto-delete] in [--disk] must be [yes] or [no], not '
          '[{0}].'.format(auto_delete_value))


def ValidateDiskAccessModeFlags(args):
  """Checks disks R/O and R/W access mode."""
  for disk in args.disk or []:
    disk_name = disk.get('name')
    mode_value = disk.get('mode')
    # Ensures that the user is not trying to attach a read-write
    # disk to more than one instance.
    if len(args.instance_names) > 1 and mode_value == 'rw':
      raise exceptions.ToolException(
          'Cannot attach disk [{0}] in read-write mode to more than one '
          'instance.'.format(disk_name))


def ValidateDiskBootFlags(args, enable_kms=False):
  """Validates the values of boot disk-related flags."""
  boot_disk_specified = False
  for disk in args.disk or []:
    # If this is a boot disk and we have already seen a boot disk,
    # we need to fail because only one boot disk can be attached.
    boot_value = disk.get('boot')
    if boot_value and boot_value not in ('yes', 'no'):
      raise exceptions.ToolException(
          'Value for [boot] in [--disk] must be [yes] or [no], not [{0}].'
          .format(boot_value))

    if boot_value == 'yes':
      if boot_disk_specified:
        raise exceptions.ToolException(
            'Each instance can have exactly one boot disk. At least two '
            'boot disks were specified through [--disk].')
      else:
        boot_disk_specified = True

  if args.image and boot_disk_specified:
    raise exceptions.ToolException(
        'Each instance can have exactly one boot disk. One boot disk '
        'was specified through [--disk] and another through [--image].')

  if boot_disk_specified:
    if args.boot_disk_device_name:
      raise exceptions.ToolException(
          '[--boot-disk-device-name] can only be used when creating a new '
          'boot disk.')

    if args.boot_disk_type:
      raise exceptions.ToolException(
          '[--boot-disk-type] can only be used when creating a new boot '
          'disk.')

    if args.boot_disk_size:
      raise exceptions.ToolException(
          '[--boot-disk-size] can only be used when creating a new boot '
          'disk.')

    if not args.boot_disk_auto_delete:
      raise exceptions.ToolException(
          '[--no-boot-disk-auto-delete] can only be used when creating a '
          'new boot disk.')

    if enable_kms:
      if args.boot_disk_kms_key:
        raise exceptions.ToolException(
            '[--boot-disk-kms-key] can only be used when creating a new boot '
            'disk.')

      if args.boot_disk_kms_keyring:
        raise exceptions.ToolException(
            '[--boot-disk-kms-keyring] can only be used when creating a new '
            'boot disk.')

      if args.boot_disk_kms_location:
        raise exceptions.ToolException(
            '[--boot-disk-kms-location] can only be used when creating a new '
            'boot disk.')

      if args.boot_disk_kms_project:
        raise exceptions.ToolException(
            '[--boot-disk-kms-project] can only be used when creating a new '
            'boot disk.')


def ValidateCreateDiskFlags(args, enable_snapshots=False):
  """Validates the values of create-disk related flags."""
  require_csek_key_create = getattr(args, 'require_csek_key_create', None)
  csek_key_file = getattr(args, 'csek_key_file', None)
  resource_names = getattr(args, 'names', [])
  for disk in getattr(args, 'create_disk', []) or []:
    disk_name = disk.get('name')
    if len(resource_names) > 1 and disk_name:
      raise exceptions.ToolException(
          'Cannot create a disk with [name]={} for more than one instance.'
          .format(disk_name))
    if not disk_name and require_csek_key_create and csek_key_file:
      raise exceptions.ToolException(
          'Cannot create a disk with customer supplied key when disk name '
          'is not specified.')

    mode_value = disk.get('mode')
    if mode_value and mode_value not in ('rw', 'ro'):
      raise exceptions.ToolException(
          'Value for [mode] in [--disk] must be [rw] or [ro], not [{0}].'
          .format(mode_value))

    image_value = disk.get('image')
    image_family_value = disk.get('image-family')
    source_snapshot = disk.get('source-snapshot')

    disk_source = set()
    if image_value:
      disk_source.add(image_value)
    if image_family_value:
      disk_source.add(image_family_value)
    if source_snapshot:
      disk_source.add(source_snapshot)

    source_error_message = (
        'Cannot specify [image] and [image-family] for a '
        '[--create-disk]. The fields are mutually exclusive.')
    if enable_snapshots:
      source_error_message = (
          'Must specify exactly one of [image], [image-family] or '
          '[source-snapshot] for a [--create-disk]. '
          'These fields are mutually exclusive.')
    if len(disk_source) > 1:
      raise exceptions.ToolException(source_error_message)


def ValidateImageFlags(args):
  """Validates the image flags."""
  if args.image_project and not (args.image or args.image_family):
    raise exceptions.ToolException(
        'Must specify either [--image] or [--image-family] when specifying '
        '[--image-project] flag.')


def AddAddressArgs(parser,
                   instances=True,
                   multiple_network_interface_cards=True):
  """Adds address arguments for instances and instance-templates."""
  addresses = parser.add_mutually_exclusive_group()
  addresses.add_argument(
      '--no-address',
      action='store_true',
      help=('If provided, the instances will not be assigned external IP '
            'addresses.'))
  if instances:
    address_help = """\
        Assigns the given external address to the instance that is created.
        The address may be an IP address or the name or URI of an address
        resource. This option can only be used when creating a single instance.
        """
  else:
    address_help = """\
        Assigns the given external IP address to the instance that is created.
        This option can only be used when creating a single instance.
        """
  addresses.add_argument(
      '--address',
      help=address_help)
  multiple_network_interface_cards_spec = {
      'address': str,
      'network': str,
      'no-address': None,
      'subnet': str,
  }
  if instances:
    multiple_network_interface_cards_spec['private-network-ip'] = str

  def ValidateNetworkTier(network_tier_input):
    network_tier = network_tier_input.upper()
    if network_tier in constants.NETWORK_TIER_CHOICES_FOR_INSTANCE:
      return network_tier
    else:
      raise exceptions.InvalidArgumentException(
          '--network-interface', 'Invalid value for network-tier')

  multiple_network_interface_cards_spec['network-tier'] = ValidateNetworkTier

  if multiple_network_interface_cards:
    multiple_network_interface_cards_spec['aliases'] = str
    network_interface_help = """\
        Adds a network interface to the instance. Mutually exclusive with any
        of these flags: *--address*, *--network*, *--network-tier*, *--subnet*,
        *--private-network-ip*. This flag can be repeated to specify multiple
        network interfaces.

        The following keys are allowed:
        *address*::: Assigns the given external address to the instance that is
        created. Specifying an empty string will assign an ephemeral IP.
        Mutually exclusive with no-address. If neither key is present the
        instance will get an ephemeral IP.

        *network*::: Specifies the network that the interface will be part of.
        If subnet is also specified it must be subnetwork of this network. If
        neither is specified, this defaults to the "default" network.

        *no-address*::: If specified the interface will have no external IP.
        Mutually exclusive with address. If neither key is present the
        instance will get an ephemeral IP.
        """
    network_interface_help += """
        *network-tier*::: Specifies the network tier of the interface.
        ``NETWORK_TIER'' must be one of: `PREMIUM`, `STANDARD`. The default
        value is `PREMIUM`.
        """
    if instances:
      network_interface_help += """
        *private-network-ip*::: Assigns the given RFC1918 IP address to the
        interface.
        """
    network_interface_help += """
        *subnet*::: Specifies the subnet that the interface will be part of.
        If network key is also specified this must be a subnetwork of the
        specified network.
        """
    network_interface_help += """
        *aliases*::: Specifies the IP alias ranges to allocate for this
        interface.  If there are multiple IP alias ranges, they are separated
        by semicolons.

        For example:

            --aliases="10.128.1.0/24;range1:/32"

        """
    if instances:
      network_interface_help += """
          Each IP alias range consists of a range name and an IP range
          separated by a colon, or just the IP range.
          The range name is the name of the range within the network
          interface's subnet from which to allocate an IP alias range. If
          unspecified, it defaults to the primary IP range of the subnet.
          The IP range can be a CIDR range (e.g. `192.168.100.0/24`), a single
          IP address (e.g. `192.168.100.1`), or a netmask in CIDR format (e.g.
          `/24`). If the IP range is specified by CIDR range or single IP
          address, it must belong to the CIDR range specified by the range
          name on the subnet. If the IP range is specified by netmask, the
          IP allocator will pick an available range with the specified netmask
          and allocate it to this network interface."""
    else:
      network_interface_help += """
          Each IP alias range consists of a range name and an CIDR netmask
          (e.g. `/24`) separated by a colon, or just the netmask.
          The range name is the name of the range within the network
          interface's subnet from which to allocate an IP alias range. If
          unspecified, it defaults to the primary IP range of the subnet.
          The IP allocator will pick an available range with the specified
          netmask and allocate it to this network interface."""
    parser.add_argument(
        '--network-interface',
        type=arg_parsers.ArgDict(
            spec=multiple_network_interface_cards_spec,
            allow_key_only=True,
        ),
        action='append',  # pylint:disable=protected-access
        metavar='PROPERTY=VALUE',
        help=network_interface_help
    )


def AddMachineTypeArgs(parser, required=False, unspecified_help=None):
  if unspecified_help is None:
    unspecified_help = ' If unspecified, the default type is n1-standard-1.'
  parser.add_argument(
      '--machine-type',
      completer=compute_completers.MachineTypesCompleter,
      required=required,
      help="""\
      Specifies the machine type used for the instances. To get a
      list of available machine types, run 'gcloud compute
      machine-types list'.{}""".format(unspecified_help)
  )


def AddMinCpuPlatformArgs(parser, track, required=False):
  parser.add_argument(
      '--min-cpu-platform',
      metavar='PLATFORM',
      required=required,
      help="""\
      When specified, the VM will be scheduled on host with specified CPU
      architecture or a newer one. To list available CPU platforms in given
      zone, run:

          $ gcloud {}compute zones describe ZONE --format="value(availableCpuPlatforms)"

      Default setting is "AUTOMATIC".

      CPU platform selection is available only in selected zones.

      You can find more information on-line:
      [](https://cloud.google.com/compute/docs/instances/specify-min-cpu-platform)
      """.format(track.prefix + ' ' if track.prefix else ''))


def AddPreemptibleVmArgs(parser):
  parser.add_argument(
      '--preemptible',
      action='store_true',
      default=False,
      help="""\
      If provided, instances will be preemptible and time-limited.
      Instances may be preempted to free up resources for standard VM instances,
      and will only be able to run for a limited amount of time. Preemptible
      instances can not be restarted and will not migrate.
      """)


def AddNetworkArgs(parser):
  """Set arguments for choosing the network/subnetwork."""
  parser.add_argument(
      '--network',
      help="""\
      Specifies the network that the instances will be part of. If --subnet is
      also specified subnet must be a subnetwork of network specified by
      --network. If neither is specified, this defaults to the "default"
      network.
      """)

  parser.add_argument(
      '--subnet',
      help="""\
      Specifies the subnet that the instances will be part of. If --network is
      also specified subnet must be a subnetwork of network specified by
      --network.
      """)


def AddPrivateNetworkIpArgs(parser):
  """Set arguments for choosing the network IP address."""
  parser.add_argument(
      '--private-network-ip',
      help="""\
      Specifies the RFC1918 IP to assign to the instance. The IP should be in
      the subnet or legacy network IP range.
      """)


def AddServiceAccountAndScopeArgs(parser, instance_exists,
                                  extra_scopes_help=''):
  """Add args for configuring service account and scopes.

  This should replace AddScopeArgs (b/30802231).

  Args:
    parser: ArgumentParser, parser to which flags will be added.
    instance_exists: bool, If instance already exists and we are modifying it.
    extra_scopes_help: str, Extra help text for the scopes flag.
  """
  service_account_group = parser.add_mutually_exclusive_group()
  service_account_group.add_argument(
      '--no-service-account', action='store_true',
      help='Remove service account from the instance' if instance_exists
      else 'Create instance without service account')

  sa_exists = 'keep the service account it currently has'
  sa_not_exists = 'get project\'s default service account'
  service_account_help = """\
  A service account is an identity attached to the instance. Its access tokens
  can be accessed through the instance metadata server and are used to
  authenticate applications on the instance. The account can be set using an
  email address corresponding to the required service account. You can
  explicitly specify the Compute Engine default service account using the
  'default' alias.

  If not provided, the instance will {0}.
  """.format(sa_exists if instance_exists else sa_not_exists)
  service_account_group.add_argument(
      '--service-account',
      help=service_account_help)

  scopes_group = parser.add_mutually_exclusive_group()
  scopes_group.add_argument(
      '--no-scopes', action='store_true',
      help='Remove all scopes from the instance' if instance_exists
      else 'Create instance without scopes')
  scopes_exists = 'keep the scopes it currently has'
  scopes_not_exists = 'be assigned the default scopes, described below'
  scopes_help = """\
If not provided, the instance will {exists}. {extra}

{scopes_help}
""".format(exists=scopes_exists if instance_exists else scopes_not_exists,
           extra=extra_scopes_help,
           scopes_help=constants.ScopesHelp())
  scopes_group.add_argument(
      '--scopes',
      type=arg_parsers.ArgList(),
      metavar='SCOPE',
      help=scopes_help)


def AddNetworkInterfaceArgs(parser):
  """Adds network interface flag to the argparse."""

  parser.add_argument(
      '--network-interface',
      default=constants.DEFAULT_NETWORK_INTERFACE,
      action=arg_parsers.StoreOnceAction,
      help="""\
      Specifies the name of the network interface which contains the access
      configuration. If this is not provided, then "nic0" is used
      as the default.
      """)


def AddNetworkTierArgs(parser, instance=True, for_update=False):
  """Adds network tier flag to the argparse."""

  if for_update:
    parser.add_argument(
        '--network-tier',
        type=lambda x: x.upper(),
        help=
        'Update the network tier of the access configuration. It does not allow'
        ' to change from `PREMIUM` to `STANDARD` and visa versa.')
    return

  if instance:
    network_tier_help = """\
        Specifies the network tier that will be used to configure the instance.
        ``NETWORK_TIER'' must be one of: `PREMIUM`, `STANDARD`. The default
        value is `PREMIUM`.
        """
  else:
    network_tier_help = """\
        Specifies the network tier of the access configuration. ``NETWORK_TIER''
        must be one of: `PREMIUM`, `STANDARD`. The default value is `PREMIUM`.
        """
  parser.add_argument(
      '--network-tier',
      type=lambda x: x.upper(),
      help=network_tier_help)


def AddDisplayDeviceArg(parser, is_update=False):
  """Adds public DNS arguments for instance or access configuration."""
  display_help = 'Enable a display device for instances using a Windows image.'
  if not is_update:
    display_help += ' Disabled by default.'
  parser.add_argument(
      '--enable-display-device',
      action=arg_parsers.StoreTrueFalseAction if is_update else 'store_true',
      help=display_help)


def AddPublicDnsArgs(parser, instance=True):
  """Adds public DNS arguments for instance or access configuration."""

  public_dns_args = parser.add_mutually_exclusive_group()
  if instance:
    no_public_dns_help = """\
        If provided, the instance will not be assigned a public DNS name.
        """
  else:
    no_public_dns_help = """\
        If provided, the external IP in the access configuration will not be
        assigned a public DNS name.
        """
  public_dns_args.add_argument(
      '--no-public-dns',
      action='store_true',
      help=no_public_dns_help)

  if instance:
    public_dns_help = """\
        Assigns a public DNS name to the instance.
        """
  else:
    public_dns_help = """\
        Assigns a public DNS name to the external IP in the access
        configuration. This option can only be specified for the default
        network-interface, "nic0".
        """
  public_dns_args.add_argument(
      '--public-dns',
      action='store_true',
      help=public_dns_help)


def AddPublicPtrArgs(parser, instance=True):
  """Adds public PTR arguments for instance or access configuration."""

  public_ptr_args = parser.add_mutually_exclusive_group()
  if instance:
    no_public_ptr_help = """\
        If provided, no DNS PTR record is created for the external IP of the
        instance. Mutually exclusive with public-ptr-domain.
        """
  else:
    no_public_ptr_help = """\
        If provided, no DNS PTR record is created for the external IP in the
        access configuration. Mutually exclusive with public-ptr-domain.
        """
  public_ptr_args.add_argument(
      '--no-public-ptr',
      action='store_true',
      help=no_public_ptr_help)

  if instance:
    public_ptr_help = """\
        Creates a DNS PTR record for the external IP of the instance.
        """
  else:
    public_ptr_help = """\
        Creates a DNS PTR record for the external IP in the access
        configuration. This option can only be specified for the default
        network-interface, "nic0"."""
  public_ptr_args.add_argument(
      '--public-ptr',
      action='store_true',
      help=public_ptr_help)

  public_ptr_domain_args = parser.add_mutually_exclusive_group()
  if instance:
    no_public_ptr_domain_help = """\
        If both this flag and --public-ptr are specified, creates a DNS PTR
        record for the external IP of the instance with the PTR domain name
        being the DNS name of the instance.
        """
  else:
    no_public_ptr_domain_help = """\
        If both this flag and --public-ptr are specified, creates a DNS PTR
        record for the external IP in the access configuration with the PTR
        domain name being the DNS name of the instance.
        """
  public_ptr_domain_args.add_argument(
      '--no-public-ptr-domain',
      action='store_true',
      help=no_public_ptr_domain_help)

  if instance:
    public_ptr_domain_help = """\
        Assigns a custom PTR domain for the external IP of the instance.
        Mutually exclusive with no-public-ptr.
        """
  else:
    public_ptr_domain_help = """\
        Assigns a custom PTR domain for the external IP in the access
        configuration. Mutually exclusive with no-public-ptr. This option can
        only be specified for the default network-interface, "nic0".
        """
  public_ptr_domain_args.add_argument(
      '--public-ptr-domain',
      help=public_ptr_domain_help)


def ValidatePublicDnsFlags(args):
  """Validates the values of public DNS related flags."""

  network_interface = getattr(args, 'network_interface', None)
  public_dns = getattr(args, 'public_dns', None)
  if public_dns is True:
    if (network_interface is not None and
        network_interface != constants.DEFAULT_NETWORK_INTERFACE):
      raise exceptions.ToolException(
          'Public DNS can only be enabled for default network interface '
          '\'{0}\' rather than \'{1}\'.'.format(
              constants.DEFAULT_NETWORK_INTERFACE, network_interface))


def ValidatePublicPtrFlags(args):
  """Validates the values of public PTR related flags."""

  network_interface = getattr(args, 'network_interface', None)
  public_ptr = getattr(args, 'public_ptr', None)
  if public_ptr is True:
    if (network_interface is not None and
        network_interface != constants.DEFAULT_NETWORK_INTERFACE):
      raise exceptions.ToolException(
          'Public PTR can only be enabled for default network interface '
          '\'{0}\' rather than \'{1}\'.'.format(
              constants.DEFAULT_NETWORK_INTERFACE, network_interface))

  if args.public_ptr_domain is not None and args.no_public_ptr is True:
    raise exceptions.ConflictingArgumentsException('--public-ptr-domain',
                                                   '--no-public-ptr')


def ValidateServiceAccountAndScopeArgs(args):
  if args.no_service_account and not args.no_scopes:
    raise exceptions.RequiredArgumentException(
        '--no-scopes', 'required with argument '
        '--no-service-account')
  # Reject empty scopes
  for scope in (args.scopes or []):
    if not scope:
      raise exceptions.InvalidArgumentException(
          '--scopes', 'Scope cannot be an empty string.')


def AddTagsArgs(parser):
  parser.add_argument(
      '--tags',
      type=arg_parsers.ArgList(min_length=1),
      metavar='TAG',
      help="""\
      Specifies a list of tags to apply to the instance. These tags allow
      network firewall rules and routes to be applied to specified VM instances.
      See gcloud_compute_firewall-rules_create(1) for more details.

      To read more about configuring network tags, read this guide:
      https://cloud.google.com/vpc/docs/add-remove-network-tags

      To list instances with their respective status and tags, run:

        $ gcloud compute instances list --format='table(name,status,tags.list())'

      To list instances tagged with a specific tag, `tag1`, run:

        $ gcloud compute instances list --filter='tags:tag1'
      """)


def AddNoRestartOnFailureArgs(parser):
  parser.add_argument(
      '--restart-on-failure',
      action='store_true',
      default=True,
      help="""\
      The instances will be restarted if they are terminated by Compute Engine.
      This does not affect terminations performed by the user.
      """)


def AddMaintenancePolicyArgs(parser, deprecate=False):
  """Adds maintenance behavior related args."""
  help_text = ('Specifies the behavior of the instances when their host '
               'machines undergo maintenance. The default is MIGRATE.')
  flag_type = lambda x: x.upper()
  action = None
  if deprecate:
    # Use nested group to group the deprecated arg with the new one.
    parser = parser.add_mutually_exclusive_group('Maintenance Behavior.')
    parser.add_argument(
        '--on-host-maintenance',
        dest='maintenance_policy',
        choices=MIGRATION_OPTIONS,
        type=flag_type,
        help=help_text)
    action = actions.DeprecationAction(
        '--maintenance-policy',
        warn='The {flag_name} flag is now deprecated. Please use '
             '`--on-host-maintenance` instead')
  parser.add_argument(
      '--maintenance-policy',
      action=action,
      choices=MIGRATION_OPTIONS,
      type=flag_type,
      help=help_text)


def AddAcceleratorArgs(parser):
  """Adds Accelerator-related args."""
  # Attaches accelerators (e.g. GPUs) to the instances. e.g. --accelerator
  # type=nvidia-tesla-k80,count=4
  parser.add_argument(
      '--accelerator',
      type=arg_parsers.ArgDict(spec={
          'type': str,
          'count': int,
      }),
      help="""\
      Attaches accelerators (e.g. GPUs) to the instances.

      *type*::: The specific type (e.g. nvidia-tesla-k80 for nVidia Tesla K80)
      of accelerator to attach to the instances. Use 'gcloud compute
      accelerator-types list' to learn about all available accelerator types.

      *count*::: The number of pieces of the accelerator to attach to the
      instances. The default value is 1.
      """)


def ValidateAcceleratorArgs(args):
  """Valiadates flags specifying accelerators (e.g. GPUs).

  Args:
    args: parsed comandline arguments.
  Raises:
    InvalidArgumentException: when type is not specified in the accelerator
    config dictionary.
  """
  accelerator_args = getattr(args, 'accelerator', None)
  if accelerator_args:
    accelerator_type_name = accelerator_args.get('type', '')
    if not accelerator_type_name:
      raise exceptions.InvalidArgumentException(
          '--accelerator', 'accelerator type must be specified. '
          'e.g. --accelerator type=nvidia-tesla-k80,count=2')


def AddKonletArgs(parser):
  """Adds Konlet-related args."""
  parser.add_argument(
      '--container-image',
      help="""\
      Full container image name, which should be pulled onto VM instance,
      eg. `docker.io/tomcat`.
      """)

  parser.add_argument(
      '--container-command',
      help="""\
      Specifies what executable to run when the container starts (overrides
      default entrypoint), eg. `nc`.

      Default: None (default container entrypoint is used)
      """)

  parser.add_argument(
      '--container-arg',
      action='append',
      help="""\
      Argument to append to container entrypoint or to override container CMD.
      Each argument must have a separate flag. Arguments are appended in the
      order of flags. Example:

      Assuming the default entry point of your container (or an entry point
      overridden with --container-command flag) is a Bourne shell-compatible
      executable, in order to execute 'ls -l' command in the container,
      you could use:

      `--container-arg="-c" --container-arg="ls -l"`

      Caveat: due to the nature of the argument parsing, it's impossible to
      provide the flag value that starts with a dash (`-`) without the `=` sign
      (that is, `--container-arg "-c"` will not work correctly).

      Default: None. (no arguments appended)
      """)

  parser.add_argument(
      '--container-privileged',
      action='store_true',
      help="""\
      Specify whether to run container in privileged mode.

      Default: `--no-container-privileged`.
      """)

  _AddContainerMountHostPathFlag(parser)
  _AddContainerMountTmpfsFlag(parser)

  parser.add_argument(
      '--container-env',
      type=arg_parsers.ArgDict(),
      action='append',
      metavar='KEY=VALUE, ...',
      help="""\
      Declare environment variables KEY with value VALUE passed to container.
      Only the last value of KEY is taken when KEY is repeated more than once.

      Values, declared with --container-env flag override those with the same
      KEY from file, provided in --container-env-file.
      """)

  parser.add_argument(
      '--container-env-file',
      help="""\
      Declare environment variables in a file. Values, declared with
      --container-env flag override those with the same KEY from file.

      File with environment variables in format used by docker (almost).
      This means:
      - Lines are in format KEY=VALUE.
      - Values must contain equality signs.
      - Variables without values are not supported (this is different from
        docker format).
      - If `#` is first non-whitespace character in a line the line is ignored
        as a comment.
      - Lines with nothing but whitespace are ignored.
      """)

  parser.add_argument(
      '--container-stdin',
      action='store_true',
      help="""\
      Keep container STDIN open even if not attached.

      Default: `--no-container-stdin`.
      """)

  parser.add_argument(
      '--container-tty',
      action='store_true',
      help="""\
      Allocate a pseudo-TTY for the container.

      Default: `--no-container-tty`.
      """)

  parser.add_argument(
      '--container-restart-policy',
      choices=['never', 'on-failure', 'always'],
      default='always',
      metavar='POLICY',
      type=lambda val: val.lower(),
      help="""\
      Specify whether to restart a container on exit.
      """)


def ValidateKonletArgs(args):
  """Validates Konlet-related args."""
  if not args.IsSpecified('container_image'):
    raise exceptions.RequiredArgumentException(
        '--container-image', 'You must provide container image')


def ValidateLocalSsdFlags(args):
  """Validate local ssd flags."""
  for local_ssd in args.local_ssd or []:
    interface = local_ssd.get('interface')
    if interface and interface not in LOCAL_SSD_INTERFACES:
      raise exceptions.InvalidArgumentException(
          '--local-ssd:interface', 'Unexpected local SSD interface: [{given}]. '
          'Legal values are [{ok}].'
          .format(given=interface,
                  ok=', '.join(LOCAL_SSD_INTERFACES)))
    size = local_ssd.get('size')
    if size is not None and size % (375 * constants.BYTES_IN_ONE_GB) != 0:
      raise exceptions.InvalidArgumentException(
          '--local-ssd:size', 'Unexpected local SSD size: [{given}]. '
          'Legal values are positive multiples of 375GB.'
          .format(given=size))


def ValidateNicFlags(args):
  """Valiadates flags specifying network interface cards.

  Args:
    args: parsed comandline arguments.
  Raises:
    InvalidArgumentException: when it finds --network-interface that has both
                              address, and no-address keys.
    ConflictingArgumentsException: when it finds --network-interface and at
                                   least one of --address, --network,
                                   --private_network_ip, or --subnet.
  """
  network_interface = getattr(args, 'network_interface', None)
  if network_interface is None:
    return
  for ni in network_interface:
    if 'address' in ni and 'no-address' in ni:
      raise exceptions.InvalidArgumentException(
          '--network-interface',
          'specifies both address and no-address for one interface')

  conflicting_args = [
      'address', 'network', 'private_network_ip', 'subnet']
  conflicting_args_present = [
      arg for arg in conflicting_args if getattr(args, arg, None)]
  if not conflicting_args_present:
    return
  conflicting_args = ['--{0}'.format(arg.replace('_', '-'))
                      for arg in conflicting_args_present]
  raise exceptions.ConflictingArgumentsException(
      '--network-interface',
      'all of the following: ' + ', '.join(conflicting_args))


def AddDiskScopeFlag(parser):
  """Adds --disk-scope flag."""
  parser.add_argument(
      '--disk-scope',
      choices={'zonal':
               'The disk specified in --disk is interpreted as a '
               'zonal disk in the same zone as the instance. '
               'Ignored if a full URI is provided to the `--disk` flag.',
               'regional':
               'The disk specified in --disk is interpreted as a '
               'regional disk in the same region as the instance. '
               'Ignored if a full URI is provided to the `--disk` flag.'},
      help='The scope of the disk.',
      default='zonal')


def WarnForSourceInstanceTemplateLimitations(args):
  """Warn if --source-instance-template is mixed with unsupported flags.

  Args:
    args: Argument namespace
  """
  allowed_flags = [
      '--project', '--zone', '--region', '--source-instance-template',
      'INSTANCE_NAMES:1', '--machine-type', '--custom-cpu', '--custom-memory',
      '--labels'
  ]

  if args.IsSpecified('source_instance_template'):
    specified_args = args.GetSpecifiedArgNames()
    # TODO(b/62933344) - Improve flag collision detection
    for flag in allowed_flags:
      if flag in specified_args:
        specified_args.remove(flag)
    if specified_args:
      log.status.write('When a source instance template is used, additional '
                       'parameters other than --machine-type and --labels will '
                       'be ignored but provided by the source instance '
                       'template\n')


def ValidateNetworkTierArgs(args):
  if (args.network_tier and
      args.network_tier not in constants.NETWORK_TIER_CHOICES_FOR_INSTANCE):
    raise exceptions.InvalidArgumentException(
        '--network-tier',
        'Invalid network tier [{tier}]'.format(tier=args.network_tier))


def AddDeletionProtectionFlag(parser, use_default_value=True):
  """Adds --deletion-protection Boolean flag.

  Args:
    parser: ArgumentParser, parser to which flags will be added.
    use_default_value: Bool, if True, deletion protection flag will be given
        the default value False, else None. Update uses None as an indicator
        that no update needs to be done for deletion protection.
  """
  help_text = ('Enables deletion protection for the instance.')
  action = ('store_true' if use_default_value else
            arg_parsers.StoreTrueFalseAction)
  parser.add_argument(
      '--deletion-protection',
      help=help_text,
      action=action)


def AddShieldedVMConfigArgs(parser, use_default_value=True, for_update=False):
  """Adds flags for shielded VM configuration.

  Args:
    parser: ArgumentParser, parser to which flags will be added.
    use_default_value: Bool, if True, flag will be given the default value
        False, else None. Update uses None as an indicator that no update needs
        to be done for deletion protection.
    for_update: Bool, if True, flags are intended for an update operation.
  """
  if use_default_value:
    default_action = 'store_true'
    action_kwargs = {'default': None}
  else:
    default_action = arg_parsers.StoreTrueFalseAction
    action_kwargs = {}

  # --shielded-vm-secure-boot
  secure_boot_help = """\
      The instance will boot with secure boot enabled.
      """
  if for_update:
    secure_boot_help += """\
      Changes to this setting (via the update command) will only take effect
      after stopping and starting the instance.
      """
  # TODO(b/120429236): remove shielded-vm flags after migration to
  # shielded flags is complete.
  # Use nested group to group the deprecated arg with the new one.
  secure_boot_parser = parser.add_mutually_exclusive_group(
      'Shielded Instance Secure Boot.')
  secure_boot_parser.add_argument(
      '--shielded-vm-secure-boot',
      help=secure_boot_help,
      action=actions.DeprecationAction(
          '--shielded-vm-secure-boot',
          warn='The {flag_name} flag is now deprecated. Please use '
          '`--shielded-secure-boot` instead.',
          action=default_action),
      **action_kwargs)
  secure_boot_parser.add_argument(
      '--shielded-secure-boot',
      help=secure_boot_help,
      dest='shielded_vm_secure_boot',
      action=default_action,
      **action_kwargs)

  # --shielded-vm-vtpm
  vtpm_help = """\
      The instance will boot with the TPM (Trusted Platform Module) enabled.
      A TPM is a hardware module that can be used for different security
      operations such as remote attestation, encryption and sealing of keys.
      """
  if for_update:
    vtpm_help += """\
      Changes to this setting (via the update command) will only take effect
      after stopping and starting the instance.
      """
  # TODO(b/120429236): remove shielded-vm flags after migration to
  # shielded flags is complete.
  vtpm_parser = parser.add_mutually_exclusive_group('Shielded Instance vTPM.')
  vtpm_parser.add_argument(
      '--shielded-vm-vtpm',
      help=vtpm_help,
      action=actions.DeprecationAction(
          '--shielded-vm-vtpm',
          warn='The {flag_name} flag is now deprecated. Please use '
          '`--shielded-vtpm` instead.',
          action=default_action),
      **action_kwargs)
  vtpm_parser.add_argument(
      '--shielded-vtpm',
      dest='shielded_vm_vtpm',
      help=vtpm_help,
      action=default_action,
      **action_kwargs)

  # --shielded-vm-integrity-monitoring
  integrity_monitoring_help = """\
      Enables monitoring and attestation of the boot integrity of the
      instance. The attestation is performed against the integrity policy
      baseline. This baseline is initially derived from the implicitly
      trusted boot image when the instance is created. This baseline can be
      updated by using `--shielded-vm-learn-integrity-policy`.
      """
  if for_update:
    integrity_monitoring_help += """\
      Changes to this setting (via the update command) will only take effect
      after stopping and starting the instance.
      """
  # TODO(b/120429236): remove shielded-vm flags after migration to
  # shielded flags is complete.
  integrity_monitoring_parser = parser.add_mutually_exclusive_group(
      'Shielded Instance Integrity Monitoring.')
  integrity_monitoring_parser.add_argument(
      '--shielded-vm-integrity-monitoring',
      help=integrity_monitoring_help,
      action=actions.DeprecationAction(
          '--shielded-vm-integrity-monitoring',
          warn='The {flag_name} flag is now deprecated. Please use '
          '`--shielded-integrity-monitoring` instead.',
          action=default_action),
      **action_kwargs)
  integrity_monitoring_parser.add_argument(
      '--shielded-integrity-monitoring',
      help=integrity_monitoring_help,
      dest='shielded_vm_integrity_monitoring',
      action=default_action,
      **action_kwargs)


def AddShieldedVMIntegrityPolicyArgs(parser):
  """Adds flags for shielded VM integrity policy settings."""
  help_text = """\
  Causes the instance to re-learn the integrity policy baseline using
  the current instance configuration. Use this flag after any planned
  boot-specific changes in the instance configuration, like kernel
  updates or kernel driver installation.
  """
  default_action = 'store_true'
  parser = parser.add_mutually_exclusive_group(
      'Shielded Instance Learn Integrity Policy.')
  # TODO(b/120429236): remove shielded-vm flag after migration to
  # shielded flags is complete.
  parser.add_argument(
      '--shielded-vm-learn-integrity-policy',
      action=actions.DeprecationAction(
          '--shielded-vm-learn-integrity-policy',
          warn='The {flag_name} flag is now deprecated. Please use '
          '`--shielded-learn-integrity-policy` instead.',
          action=default_action),
      default=None,
      help=help_text)
  parser.add_argument(
      '--shielded-learn-integrity-policy',
      dest='shielded_vm_learn_integrity_policy',
      action=default_action,
      default=None,
      help=help_text)


def AddHostnameArg(parser):
  """Adds flag for overriding hostname for instance."""
  parser.add_argument(
      '--hostname',
      help="""\
      Specify the hostname of the instance to be created. The specified
      homename must be RFC1035 compliant. If hostname is not specified, the
      default hostname is [INSTANCE_NAME].c.[PROJECT_ID].internal when using
      the global DNS, and [INSTANCE_NAME].[ZONE].c.[PROJECT_ID].internal
      when using zonal DNS.
      """)


def AddAllocationAffinityGroup(parser):
  """Adds the argument group to handle allocation affinity configurations."""
  group = parser.add_group(help="""
Manage the configuration of desired allocation which this instance could take
capacity from.
""")
  group.add_argument(
      '--allocation-affinity',
      choices=['any', 'none', 'specific'],
      default='any',
      help="""
Specifies the configuration of desired allocation which this instance could
take capacity from. Choices are 'any', 'none' and 'specific', default is 'any'.
""")
  group.add_argument(
      '--allocation-label',
      type=arg_parsers.ArgDict(spec={
          'key': str,
          'value': str,
      }),
      help="""
The key and values of the label of the allocation resource. Required if the
value of `--allocation-affinity` is `specific`.

*key*::: The label key of allocation resource.

*value*::: The label value of allocation resource.
""")


def ValidateAllocationAffinityGroup(args):
  """Validates flags specifying allocation affinity."""
  affinity = getattr(args, 'allocation_affinity', None)
  if affinity == 'specific':
    if not args.IsSpecified('allocation_label'):
      raise exceptions.InvalidArgumentException(
          '--allocation-label',
          'The label of the specific allocation must be specified.')


def _GetContainerMountDescriptionAndNameDescription(for_update=False):
  """Get description text for --container-mount-disk flag."""
  if for_update:
    description = ("""\
Mounts a disk to the container by using mount-path or updates how the volume is
mounted if the same mount path has already been declared. The disk must already
be attached to the instance with a device-name that matches the disk name.
Multiple flags are allowed.
""")
    name_description = ("""\
Name of the disk. Can be omitted if exactly one additional disk is attached to
the instance. The name of the single additional disk will be used by default.
""")
    return description, name_description
  else:
    description = ("""\
Mounts a disk to the specified mount path in the container. Multiple '
flags are allowed. Must be used with `--disk` or `--create-disk`.
""")
    name_description = ("""\
Name of the disk. If exactly one additional disk is attached
to the instance using `--disk` or `--create-disk`, specifying disk
name here is optional. The name of the single additional disk will be
used by default.
""")
    return description, name_description


def ParseMountVolumeMode(argument_name, mode):
  """Parser for mode option in ArgDict specs."""
  if not mode or mode == 'rw':
    return containers_utils.MountVolumeMode.READ_WRITE
  elif mode == 'ro':
    return containers_utils.MountVolumeMode.READ_ONLY
  else:
    raise exceptions.InvalidArgumentException(
        argument_name, 'Mode can only be [ro] or [rw].')


def AddContainerMountDiskFlag(parser, for_update=False):
  """Add --container-mount-disk flag."""
  description, name_description = (
      _GetContainerMountDescriptionAndNameDescription(for_update=for_update))
  help_text = ("""\
{}

*name*::: {}

*mount-path*::: Path on container to mount to. Mount paths with spaces
      and commas (and other special characters) are not supported by this
      command.

*partition*::: Optional. The partition of the disk to mount. Multiple
partitions of a disk may be mounted.{}

*mode*::: Volume mount mode: `rw` (read/write) or `ro` (read-only).
Defaults to `rw`. Fails if the disk mode is `ro` and volume mount mode
is `rw`.
""".format(description, name_description,
           '' if for_update else ' May not be used with --create-disk.'))

  spec = {
      'name': str,
      'mount-path': str,
      'partition': int,
      'mode': functools.partial(ParseMountVolumeMode,
                                '--container-mount-disk')}
  parser.add_argument(
      '--container-mount-disk',
      type=arg_parsers.ArgDict(spec=spec, required_keys=['mount-path']),
      help=help_text,
      action='append')


def _GetMatchingDiskFromMessages(holder, mount_disk_name, disk, client=None):
  """Helper to match a mount disk's name to a disk message."""
  if client is None:
    client = apis.GetClientClass('compute', 'alpha')
  if mount_disk_name is None and len(disk) == 1:
    return {
        'name': holder.resources.Parse(disk[0].source).Name(),
        'device_name': disk[0].deviceName,
        'ro': (disk[0].mode ==
               client.MESSAGES_MODULE.AttachedDisk.ModeValueValuesEnum
               .READ_WRITE)}, False
  for disk_spec in disk:
    disk_name = holder.resources.Parse(disk_spec.source).Name()
    if disk_name == mount_disk_name:
      return {
          'name': disk_name,
          'device_name': disk_spec.deviceName,
          'ro': (disk_spec.mode ==
                 client.MESSAGES_MODULE.AttachedDisk.ModeValueValuesEnum
                 .READ_WRITE)}, False
  return None, None


def _GetMatchingDiskFromFlags(mount_disk_name, disk, create_disk):
  """Helper to match a mount disk's name to a disk spec from a flag."""

  def _GetMatchingDiskFromSpec(spec):
    return {'name': spec.get('name'),
            'device_name': spec.get('device-name'),
            'ro': spec.get('mode') == 'ro'}

  if mount_disk_name is None and len(disk + create_disk) == 1:
    disk_spec = (disk + create_disk)[0]
    return _GetMatchingDiskFromSpec(disk_spec), bool(create_disk)
  for disk_spec in disk:
    if disk_spec.get('name') == mount_disk_name:
      return _GetMatchingDiskFromSpec(disk_spec), False
  for disk_spec in create_disk:
    if disk_spec.get('name') == mount_disk_name:
      return _GetMatchingDiskFromSpec(disk_spec), True
  return None, None


def _CheckMode(name, mode_value, mount_disk, matching_disk, create):
  """Make sure the correct mode is specified for container mount disk."""
  partition = mount_disk.get('partition')
  if (mode_value == containers_utils.MountVolumeMode.READ_WRITE and
      matching_disk.get('ro')):
    raise exceptions.InvalidArgumentException(
        '--container-mount-disk',
        'Value for [mode] in [--container-mount-disk] cannot be [rw] if the '
        'disk is attached in [ro] mode: disk name [{}], partition [{}]'
        .format(name, partition))
  if matching_disk.get('ro') and create:
    raise exceptions.InvalidArgumentException(
        '--container-mount-disk',
        'Cannot mount disk named [{}] to container: disk is created in [ro] '
        'mode and thus cannot be formatted.'.format(
            name))


def GetValidatedContainerMountDisk(holder, container_mount_disk, disk,
                                   create_disk, for_update=False, client=None):
  """Validate --container-mount-disk value."""
  disk = disk or []
  create_disk = create_disk or []
  if not container_mount_disk:
    return
  if not (disk or create_disk or for_update):
    raise exceptions.InvalidArgumentException(
        '--container-mount-disk',
        'Must be used with `--disk` or `--create-disk`')

  message = '' if for_update else ' using `--disk` or `--create-disk`.'
  validated_disks = []
  for mount_disk in container_mount_disk:
    if for_update:
      matching_disk, create = _GetMatchingDiskFromMessages(
          holder, mount_disk.get('name'), disk, client=client)
    else:
      matching_disk, create = _GetMatchingDiskFromFlags(
          mount_disk.get('name'), disk, create_disk)
    if not mount_disk.get('name'):
      if len(disk + create_disk) != 1:
        raise exceptions.InvalidArgumentException(
            '--container-mount-disk',
            'Must specify the name of the disk to be mounted unless exactly '
            'one disk is attached to the instance{}.'.format(message))
      name = matching_disk.get('name')
      if not name:
        raise exceptions.InvalidArgumentException(
            '--container-mount-disk',
            'When attaching or creating a disk that is also being mounted to '
            'a container, must specify the disk name.')
    else:
      name = mount_disk.get('name')
      if not matching_disk:
        raise exceptions.InvalidArgumentException(
            '--container-mount-disk',
            'Attempting to mount a disk that is not attached to the instance: '
            'must attach a disk named [{}]{}'.format(name, message))
    if (matching_disk and
        matching_disk.get('device_name') and
        matching_disk.get('device_name') != matching_disk.get('name')):
      raise exceptions.InvalidArgumentException(
          '--container-mount-disk',
          'Container mount disk cannot be used with a device whose device-name '
          'is different from its name. The disk with name [{}] has the '
          'device-name [{}].'.format(matching_disk.get('name'),
                                     matching_disk.get('device_name')))

    mode_value = mount_disk.get('mode')
    if matching_disk:
      _CheckMode(name, mode_value, mount_disk, matching_disk, create)
    if matching_disk and create and mount_disk.get('partition'):
      raise exceptions.InvalidArgumentException(
          '--container-mount-disk',
          'Container mount disk cannot specify a partition when the disk '
          'is created with --create-disk: disk name [{}], partition [{}]'
          .format(name, mount_disk.get('partition')))
    mount_disk = copy.deepcopy(mount_disk)
    mount_disk['name'] = mount_disk.get('name') or name
    validated_disks.append(mount_disk)
  return validated_disks


def NonEmptyString(parameter_name):
  def Factory(string):
    if not string:
      raise exceptions.InvalidArgumentException(
          parameter_name, 'Empty string is not allowed.')
    return string
  return Factory


def _AddContainerEnvGroup(parser):
  """Add flags to update the container environment."""

  env_group = parser.add_argument_group()

  env_group.add_argument(
      '--container-env',
      type=arg_parsers.ArgDict(),
      action='append',
      metavar='KEY=VALUE, ...',
      help="""\
      Update environment variables `KEY` with value `VALUE` passed to
      container.
      - Sets `KEY` to the specified value.
      - Adds `KEY` = `VALUE`, if `KEY` is not yet declared.
      - Only the last value of `KEY` is taken when `KEY` is repeated more
      than once.

      Values, declared with `--container-env` flag override those with the
      same `KEY` from file, provided in `--container-env-file`.
      """)

  env_group.add_argument(
      '--container-env-file',
      help="""\
      Update environment variables from a file.
      Same update rules as for `--container-env` apply.
      Values, declared with `--container-env` flag override those with the
      same `KEY` from file.

      File with environment variables declarations in format used by docker
      (almost). This means:
      - Lines are in format KEY=VALUE
      - Values must contain equality signs.
      - Variables without values are not supported (this is different from
      docker format).
      - If # is first non-whitespace character in a line the line is ignored
      as a comment.
      """)

  env_group.add_argument(
      '--remove-container-env',
      type=arg_parsers.ArgList(),
      action='append',
      metavar='KEY',
      help="""\
      Removes environment variables `KEY` from container declaration Does
      nothing, if a variable is not present.
      """)


def _AddContainerArgGroup(parser):
  """Add flags to update the container arg."""

  arg_group = parser.add_mutually_exclusive_group()

  arg_group.add_argument(
      '--container-arg',
      action='append',
      help="""\
      Completely replaces the list of arguments with the new list.
      Each argument must have a separate --container-arg flag.
      Arguments are appended the new list in the order of flags.

      Cannot be used in the same command with `--clear-container-arg`.
      """)

  arg_group.add_argument(
      '--clear-container-args',
      action='store_true',
      default=None,
      help="""\
      Removes the list of arguments from container declaration.

      Cannot be used in the same command with `--container-arg`.
      """
  )


def _AddContainerCommandGroup(parser):
  """Add flags to update the command in the container declaration."""
  command_group = parser.add_mutually_exclusive_group()

  command_group.add_argument(
      '--container-command',
      type=NonEmptyString('--container-command'),
      help="""\
      Sets command in the declaration to the specified value.
      Empty string is not allowed.

      Cannot be used in the same command with `--clear-container-command`.
      """)

  command_group.add_argument(
      '--clear-container-command',
      action='store_true',
      default=None,
      help="""\
      Removes command from container declaration.

      Cannot be used in the same command with `--container-command`.
      """)


def _AddContainerMountHostPathFlag(parser, for_update=False):
  """Helper to add --container-mount-host-path flag."""
  if for_update:
    additional = """\

      - Adds a volume, if `mount-path` is not yet declared.
      - Replaces a volume, if `mount-path` is declared.
      All parameters (`host-path`, `mount-path`, `mode`) are completely
      replaced."""
  else:
    additional = ''
  parser.add_argument(
      '--container-mount-host-path',
      metavar='host-path=HOSTPATH,mount-path=MOUNTPATH[,mode=MODE]',
      type=arg_parsers.ArgDict(spec={'host-path': str,
                                     'mount-path': str,
                                     'mode': functools.partial(
                                         ParseMountVolumeMode,
                                         '--container-mount-host-path')}),
      action='append',
      help="""\
      Mounts a volume by using host-path.{}

      *host-path*::: Path on host to mount from.

      *mount-path*::: Path on container to mount to. Mount paths with spaces
      and commas (and other special characters) are not supported by this
      command.

      *mode*::: Volume mount mode: rw (read/write) or ro (read-only).

      Default: rw.
      """.format(additional))


def _AddContainerMountTmpfsFlag(parser):
  """Helper to add --container-mount-tmpfs flag."""
  parser.add_argument(
      '--container-mount-tmpfs',
      metavar='mount-path=MOUNTPATH',
      type=arg_parsers.ArgDict(spec={'mount-path': str}),
      action='append',
      help="""\
      Mounts empty tmpfs into container at MOUNTPATH.

      *mount-path*::: Path on container to mount to. Mount paths with spaces
      and commas (and other special characters) are not supported by this
      command.
      """)


def _AddContainerMountGroup(parser, container_mount_disk_enabled=False):
  """Add flags to update what is mounted to the container."""

  mount_group = parser.add_argument_group()

  _AddContainerMountHostPathFlag(mount_group, for_update=True)
  _AddContainerMountTmpfsFlag(mount_group)

  if container_mount_disk_enabled:
    AddContainerMountDiskFlag(parser, for_update=True)

  mount_types = ['`host-path`', '`tmpfs`']
  if container_mount_disk_enabled:
    mount_types.append('`disk`')
  mount_group.add_argument(
      '--remove-container-mounts',
      type=arg_parsers.ArgList(),
      metavar='MOUNTPATH[,MOUNTPATH,...]',
      help="""\
      Removes volume mounts ({}) with
      `mountPath: MOUNTPATH` from container declaration.

      Does nothing, if a volume mount is not declared.
      """.format(', '.join(mount_types))
  )


def _AddContainerArgs(parser):
  """Add basic args for update-container."""

  parser.add_argument(
      '--container-image',
      type=NonEmptyString('--container-image'),
      help="""\
      Sets container image in the declaration to the specified value.

      Empty string is not allowed.
      """)

  parser.add_argument(
      '--container-privileged',
      action='store_true',
      default=None,
      help="""\
      Sets permission to run container to the specified value.
      """)

  parser.add_argument(
      '--container-stdin',
      action='store_true',
      default=None,
      help="""\
      Sets configuration whether to keep container `STDIN` always open to the
      specified value.
      """)

  parser.add_argument(
      '--container-tty',
      action='store_true',
      default=None,
      help="""\
      Sets configuration whether to allocate a pseudo-TTY for the container
      to the specified value.
      """)

  parser.add_argument(
      '--container-restart-policy',
      choices=['never', 'on-failure', 'always'],
      metavar='POLICY',
      type=lambda val: val.lower(),
      help="""\
      Sets container restart policy to the specified value.
      """)


def AddUpdateContainerArgs(parser, container_mount_disk_enabled=False):
  INSTANCE_ARG.AddArgument(parser, operation_type='update')
  _AddContainerCommandGroup(parser)
  _AddContainerEnvGroup(parser)
  _AddContainerArgGroup(parser)
  _AddContainerMountGroup(
      parser,
      container_mount_disk_enabled=container_mount_disk_enabled)
  _AddContainerArgs(parser)
