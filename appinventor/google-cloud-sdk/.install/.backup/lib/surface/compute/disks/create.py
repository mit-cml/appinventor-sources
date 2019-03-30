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
"""Command for creating disks."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import argparse
import textwrap

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.api_lib.compute import constants
from googlecloudsdk.api_lib.compute import csek_utils
from googlecloudsdk.api_lib.compute import image_utils
from googlecloudsdk.api_lib.compute import kms_utils
from googlecloudsdk.api_lib.compute import utils
from googlecloudsdk.api_lib.compute import zone_utils
from googlecloudsdk.api_lib.compute.regions import utils as region_utils
from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.calliope import base
from googlecloudsdk.calliope import exceptions
from googlecloudsdk.command_lib.compute import completers
from googlecloudsdk.command_lib.compute import flags
from googlecloudsdk.command_lib.compute.disks import create
from googlecloudsdk.command_lib.compute.disks import flags as disks_flags
from googlecloudsdk.command_lib.compute.kms import resource_args as kms_resource_args
from googlecloudsdk.command_lib.compute.resource_policies import flags as resource_flags
from googlecloudsdk.command_lib.compute.resource_policies import util as resource_util
from googlecloudsdk.command_lib.util.args import labels_util
from googlecloudsdk.core import log
import six

DETAILED_HELP = {
    'brief': 'Create Google Compute Engine persistent disks',
    'DESCRIPTION': """\
        *{command}* creates one or more Google Compute Engine
        persistent disks. When creating virtual machine instances,
        disks can be attached to the instances through the
        `gcloud compute instances create` command. Disks can also be
        attached to instances that are already running using
        `gcloud compute instances attach-disk`.

        Disks are zonal resources, so they reside in a particular zone
        for their entire lifetime. The contents of a disk can be moved
        to a different zone by snapshotting the disk (using
        `gcloud compute disks snapshot`) and creating a new disk using
        `--source-snapshot` in the desired zone. The contents of a
        disk can also be moved across project or zone by creating an
        image (using `gcloud compute images create`) and creating a
        new disk using `--image` in the desired project and/or
        zone.

        When creating disks, be sure to include the `--zone` option:

          $ {command} my-disk-1 my-disk-2 --zone us-east1-a
        """,
}


def _SourceArgs(parser, source_snapshot_arg):
  """Add mutually exclusive source args."""
  source_parent_group = parser.add_group()
  source_group = source_parent_group.add_mutually_exclusive_group()

  def AddImageHelp():
    """Returns detailed help for `--image` argument."""
    template = """\
        An image to apply to the disks being created. When using
        this option, the size of the disks must be at least as large as
        the image size. Use ``--size'' to adjust the size of the disks.

        This flag is mutually exclusive with ``--source-snapshot'' and
        ``--image-family''.
        """
    return template

  source_group.add_argument(
      '--image',
      help=AddImageHelp)

  image_utils.AddImageProjectFlag(source_parent_group)

  source_group.add_argument(
      '--image-family',
      help=('The family of the image that the boot disk will be initialized '
            'with. When a family is used instead of an image, the latest '
            'non-deprecated image associated with that family is used.')
  )
  source_snapshot_arg.AddArgument(source_group)


def _CommonArgs(parser,
                source_snapshot_arg,
                include_physical_block_size_support=False):
  """Add arguments used for parsing in all command tracks."""
  Create.disks_arg.AddArgument(parser, operation_type='create')
  parser.add_argument(
      '--description',
      help='An optional, textual description for the disks being created.')

  parser.add_argument(
      '--size',
      type=arg_parsers.BinarySize(
          lower_bound='1GB',
          suggested_binary_size_scales=['GB', 'GiB', 'TB', 'TiB', 'PiB', 'PB']),
      help="""\
        Size of the disks. The value must be a whole
        number followed by a size unit of ``GB'' for gigabyte, or ``TB''
        for terabyte. If no size unit is specified, GB is
        assumed. For example, ``10GB'' will produce 10 gigabyte
        disks. Disk size must be a multiple of 1 GB. Limit your boot disk size
        to 2TB to account for MBR partition table limitations. If disk size is
        not specified, the default size of {}GB for standard disks and {}GB for
        pd-ssd disks will be used.
        """.format(constants.DEFAULT_STANDARD_DISK_SIZE_GB,
                   constants.DEFAULT_SSD_DISK_SIZE_GB))

  parser.add_argument(
      '--type',
      completer=completers.DiskTypesCompleter,
      help="""\
      Specifies the type of disk to create. To get a
      list of available disk types, run `gcloud compute disk-types list`.
      The default disk type is pd-standard.
      """)

  parser.display_info.AddFormat(
      'table(name, zone.basename(), sizeGb, type.basename(), status)')

  parser.add_argument(
      '--licenses',
      type=arg_parsers.ArgList(),
      metavar='LICENSE',
      help=('A list of URIs to license resources. The provided licenses will '
            'be added onto the created disks to indicate the licensing and '
            'billing policies.'))

  _SourceArgs(parser, source_snapshot_arg)

  csek_utils.AddCsekKeyArgs(parser)
  labels_util.AddCreateLabelsFlags(parser)

  if include_physical_block_size_support:
    parser.add_argument(
        '--physical-block-size',
        choices=['4096', '16384'],
        default='4096',
        help="""\
Physical block size of the persistent disk in bytes.
Valid values are 4096(default) and 16384.
""")


def _AddReplicaZonesArg(parser):
  parser.add_argument(
      '--replica-zones',
      type=arg_parsers.ArgList(min_length=2, max_length=2),
      metavar='ZONE',
      help=('A comma-separated list of exactly 2 zones that a regional disk '
            'will be replicated to. Required when creating regional disk. '
            'The zones must be in the same region as specified in the '
            '`--region` flag. See available zones with '
            '`gcloud compute zones list`.'))


def _ParseGuestOsFeaturesToMessages(args, client_messages):
  """Parse GuestOS features."""
  guest_os_feature_messages = []
  if args.guest_os_features:
    for feature in args.guest_os_features:
      gf_type = client_messages.GuestOsFeature.TypeValueValuesEnum(feature)
      guest_os_feature = client_messages.GuestOsFeature()
      guest_os_feature.type = gf_type
      guest_os_feature_messages.append(guest_os_feature)

  return guest_os_feature_messages


@base.ReleaseTracks(base.ReleaseTrack.GA)
class Create(base.Command):
  """Create Google Compute Engine persistent disks."""

  @staticmethod
  def Args(parser):
    Create.disks_arg = disks_flags.MakeDiskArg(plural=True)
    _CommonArgs(parser, disks_flags.SOURCE_SNAPSHOT_ARG)
    image_utils.AddGuestOsFeaturesArg(parser, base.ReleaseTrack.GA)
    kms_resource_args.AddKmsKeyResourceArg(
        parser, 'disk', region_fallthrough=True)

  def ParseLicenses(self, args):
    """Parse license.

    Subclasses may override it to customize parsing.

    Args:
      args: The argument namespace

    Returns:
      List of licenses.
    """
    if args.licenses:
      return args.licenses
    return []

  def ValidateAndParseDiskRefs(self, args, compute_holder):
    """Validate flags and parse disks references.

    Subclasses may override it to customize parsing.

    Args:
      args: The argument namespace
      compute_holder: base_classes.ComputeApiHolder instance

    Returns:
      List of compute.regionDisks resources.
    """
    return Create.disks_arg.ResolveAsResource(
        args,
        compute_holder.resources,
        scope_lister=flags.GetDefaultScopeLister(compute_holder.client))

  def GetFromImage(self, args):
    return args.image or args.image_family

  def GetDiskSizeGb(self, args, from_image):
    size_gb = utils.BytesToGb(args.size)

    if not size_gb and not args.source_snapshot and not from_image:
      if args.type and 'pd-ssd' in args.type:
        size_gb = constants.DEFAULT_SSD_DISK_SIZE_GB
      else:
        size_gb = constants.DEFAULT_STANDARD_DISK_SIZE_GB
    utils.WarnIfDiskSizeIsTooSmall(size_gb, args.type)
    return size_gb

  def GetProjectToSourceImageDict(
      self, args, disk_refs, compute_holder, from_image):
    project_to_source_image = {}

    image_expander = image_utils.ImageExpander(
        compute_holder.client, compute_holder.resources)

    for disk_ref in disk_refs:
      if from_image:
        if disk_ref.project not in project_to_source_image:
          source_image_uri, _ = image_expander.ExpandImageFlag(
              user_project=disk_ref.project,
              image=args.image,
              image_family=args.image_family,
              image_project=args.image_project,
              return_image_resource=False)
          project_to_source_image[disk_ref.project] = argparse.Namespace()
          project_to_source_image[disk_ref.project].uri = source_image_uri
      else:
        project_to_source_image[disk_ref.project] = argparse.Namespace()
        project_to_source_image[disk_ref.project].uri = None
    return project_to_source_image

  def WarnAboutScopeDeprecationsAndMaintainance(self, disk_refs, client):
    # Check if the zone is deprecated or has maintenance coming.
    zone_resource_fetcher = zone_utils.ZoneResourceFetcher(client)
    zone_resource_fetcher.WarnForZonalCreation(
        (ref for ref in disk_refs if ref.Collection() == 'compute.disks'))
    # Check if the region is deprecated or has maintenance coming.
    region_resource_fetcher = region_utils.RegionResourceFetcher(client)
    region_resource_fetcher.WarnForRegionalCreation(
        (ref for ref in disk_refs if ref.Collection() == 'compute.regionDisks'))

  def GetSnapshotUri(self, args, compute_holder):
    snapshot_ref = disks_flags.SOURCE_SNAPSHOT_ARG.ResolveAsResource(
        args, compute_holder.resources)
    if snapshot_ref:
      return snapshot_ref.SelfLink()
    return None

  def GetLabels(self, args, client):
    labels = None
    args_labels = getattr(args, 'labels', None)
    if args_labels:
      labels = client.messages.Disk.LabelsValue(additionalProperties=[
          client.messages.Disk.LabelsValue.AdditionalProperty(
              key=key, value=value)
          for key, value in sorted(six.iteritems(args.labels))])
    return labels

  def GetDiskTypeUri(self, args, disk_ref, compute_holder):
    if args.type:
      if disk_ref.Collection() == 'compute.disks':
        type_ref = compute_holder.resources.Parse(
            args.type, collection='compute.diskTypes',
            params={
                'project': disk_ref.project,
                'zone': disk_ref.zone
            })
      elif disk_ref.Collection() == 'compute.regionDisks':
        type_ref = compute_holder.resources.Parse(
            args.type, collection='compute.regionDiskTypes',
            params={
                'project': disk_ref.project,
                'region': disk_ref.region
            })
      return type_ref.SelfLink()
    return None

  def GetReplicaZones(self, args, compute_holder, disk_ref):
    result = []
    for zone in args.replica_zones:
      zone_ref = compute_holder.resources.Parse(
          zone,
          collection='compute.zones',
          params={
              'project': disk_ref.project
          })
      result.append(zone_ref.SelfLink())
    return result

  def Run(self, args):
    return self._Run(args, supports_kms_keys=True)

  def _Run(self, args, supports_kms_keys=False, supports_physical_block=False):
    compute_holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = compute_holder.client

    self.show_unformated_message = not (args.IsSpecified('image') or
                                        args.IsSpecified('image_family') or
                                        args.IsSpecified('source_snapshot'))

    disk_refs = self.ValidateAndParseDiskRefs(args, compute_holder)
    from_image = self.GetFromImage(args)
    size_gb = self.GetDiskSizeGb(args, from_image)
    self.WarnAboutScopeDeprecationsAndMaintainance(disk_refs, client)
    project_to_source_image = self.GetProjectToSourceImageDict(
        args, disk_refs, compute_holder, from_image)
    snapshot_uri = self.GetSnapshotUri(args, compute_holder)

    # Those features are only exposed in alpha/beta, it would be nice to have
    # code supporting them only in alpha and beta versions of the command.
    labels = self.GetLabels(args, client)

    allow_rsa_encrypted = self.ReleaseTrack() in [base.ReleaseTrack.ALPHA,
                                                  base.ReleaseTrack.BETA]
    csek_keys = csek_utils.CsekKeyStore.FromArgs(args, allow_rsa_encrypted)

    for project in project_to_source_image:
      source_image_uri = project_to_source_image[project].uri
      project_to_source_image[project].keys = (
          csek_utils.MaybeLookupKeyMessagesByUri(
              csek_keys, compute_holder.resources,
              [source_image_uri, snapshot_uri], client.apitools_client))

    resource_policies = getattr(args, 'resource_policies', None)
    # end of alpha/beta features.

    guest_os_feature_messages = _ParseGuestOsFeaturesToMessages(
        args, client.messages)

    requests = []
    for disk_ref in disk_refs:
      type_uri = self.GetDiskTypeUri(args, disk_ref, compute_holder)

      # Those features are only exposed in alpha/beta, it would be nice to have
      # code supporting them only in alpha and beta versions of the command.
      # TODO(b/65161039): Stop checking release path in the middle of GA code.
      kwargs = {}
      if csek_keys:
        disk_key_or_none = csek_keys.LookupKey(
            disk_ref, args.require_csek_key_create)
        disk_key_message_or_none = csek_utils.MaybeToMessage(
            disk_key_or_none, client.apitools_client)
        kwargs['diskEncryptionKey'] = disk_key_message_or_none
        kwargs['sourceImageEncryptionKey'] = (
            project_to_source_image[disk_ref.project].keys[0])
        kwargs['sourceSnapshotEncryptionKey'] = (
            project_to_source_image[disk_ref.project].keys[1])
      if labels:
        kwargs['labels'] = labels

      if supports_kms_keys:
        kwargs['diskEncryptionKey'] = kms_utils.MaybeGetKmsKey(
            args, client.messages, kwargs.get('diskEncryptionKey', None))

      if resource_policies:
        if disk_ref.Collection() == 'compute.regionDisks':
          disk_region = disk_ref.region
        else:
          disk_region = utils.ZoneNameToRegionName(disk_ref.zone)
        parsed_resource_policies = []
        for policy in resource_policies:
          resource_policy_ref = resource_util.ParseResourcePolicy(
              compute_holder.resources,
              policy,
              project=disk_ref.project,
              region=disk_region)
          parsed_resource_policies.append(resource_policy_ref.SelfLink())
        kwargs['resourcePolicies'] = parsed_resource_policies

      # end of alpha/beta features.

      if supports_physical_block and args.IsSpecified('physical_block_size'):
        physical_block_size_bytes = int(args.physical_block_size)
      else:
        physical_block_size_bytes = None

      disk = client.messages.Disk(
          name=disk_ref.Name(),
          description=args.description,
          sizeGb=size_gb,
          sourceSnapshot=snapshot_uri,
          type=type_uri,
          physicalBlockSizeBytes=physical_block_size_bytes,
          **kwargs)

      if guest_os_feature_messages:
        disk.guestOsFeatures = guest_os_feature_messages

      disk.licenses = self.ParseLicenses(args)

      if disk_ref.Collection() == 'compute.disks':
        request = client.messages.ComputeDisksInsertRequest(
            disk=disk,
            project=disk_ref.project,
            sourceImage=project_to_source_image[disk_ref.project].uri,
            zone=disk_ref.zone)

        request = (client.apitools_client.disks, 'Insert', request)
      elif disk_ref.Collection() == 'compute.regionDisks':
        disk.replicaZones = self.GetReplicaZones(args, compute_holder, disk_ref)
        request = client.messages.ComputeRegionDisksInsertRequest(
            disk=disk,
            project=disk_ref.project,
            sourceImage=project_to_source_image[disk_ref.project].uri,
            region=disk_ref.region)

        request = (client.apitools_client.regionDisks, 'Insert', request)

      requests.append(request)

    return client.MakeRequests(requests)

  def Epilog(self, resources_were_displayed=True):
    message = """\

        New disks are unformatted. You must format and mount a disk before it
        can be used. You can find instructions on how to do this at:

        https://cloud.google.com/compute/docs/disks/add-persistent-disk#formatting
        """
    if self.show_unformated_message:
      log.status.Print(textwrap.dedent(message))


@base.ReleaseTracks(base.ReleaseTrack.BETA)
class CreateBeta(Create):
  """Create Google Compute Engine persistent disks."""

  @staticmethod
  def Args(parser):
    Create.disks_arg = disks_flags.MakeDiskArgZonalOrRegional(plural=True)

    _CommonArgs(
        parser,
        disks_flags.SOURCE_SNAPSHOT_ARG,
        include_physical_block_size_support=True)
    image_utils.AddGuestOsFeaturesArg(parser, base.ReleaseTrack.BETA)
    _AddReplicaZonesArg(parser)
    resource_flags.AddResourcePoliciesArgs(parser, 'added to', 'disk')
    kms_resource_args.AddKmsKeyResourceArg(
        parser, 'disk', region_fallthrough=True)

  def ValidateAndParseDiskRefs(self, args, compute_holder):
    return _ValidateAndParseDiskRefsRegionalReplica(args, compute_holder)

  def Run(self, args):
    return self._Run(args, supports_kms_keys=True, supports_physical_block=True)


@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class CreateAlpha(Create):
  """Create Google Compute Engine persistent disks."""

  @staticmethod
  def Args(parser):
    Create.disks_arg = disks_flags.MakeDiskArgZonalOrRegional(plural=True)

    _CommonArgs(
        parser,
        disks_flags.SOURCE_SNAPSHOT_ARG,
        include_physical_block_size_support=True)
    image_utils.AddGuestOsFeaturesArg(parser, base.ReleaseTrack.ALPHA)
    _AddReplicaZonesArg(parser)
    resource_flags.AddResourcePoliciesArgs(parser, 'added to', 'disk')
    kms_resource_args.AddKmsKeyResourceArg(
        parser, 'disk', region_fallthrough=True)

  def ValidateAndParseDiskRefs(self, args, compute_holder):
    return _ValidateAndParseDiskRefsRegionalReplica(args, compute_holder)

  def Run(self, args):
    return self._Run(args, supports_kms_keys=True, supports_physical_block=True)


def _ValidateAndParseDiskRefsRegionalReplica(args, compute_holder):
  """Validate flags and parse disks references.

  Subclasses may override it to customize parsing.

  Args:
    args: The argument namespace
    compute_holder: base_classes.ComputeApiHolder instance

  Returns:
    List of compute.regionDisks resources.
  """
  if not args.IsSpecified('replica_zones') and args.IsSpecified('region'):
    raise exceptions.RequiredArgumentException(
        '--replica-zones',
        '--replica-zones is required for regional disk creation')
  if args.replica_zones is not None:
    return create.ParseRegionDisksResources(
        compute_holder.resources, args.DISK_NAME, args.replica_zones,
        args.project, args.region)

  disk_refs = Create.disks_arg.ResolveAsResource(
      args,
      compute_holder.resources,
      scope_lister=flags.GetDefaultScopeLister(compute_holder.client))

  # --replica-zones is required for regional disks always - also when region
  # is selected in prompt.
  for disk_ref in disk_refs:
    if disk_ref.Collection() == 'compute.regionDisks':
      raise exceptions.RequiredArgumentException(
          '--replica-zones',
          '--replica-zones is required for regional disk creation [{}]'.
          format(disk_ref.SelfLink()))

  return disk_refs


Create.detailed_help = DETAILED_HELP
