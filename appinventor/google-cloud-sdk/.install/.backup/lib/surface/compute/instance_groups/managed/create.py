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
"""Command for creating managed instance group."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import sys
from apitools.base.py import encoding

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.api_lib.compute import managed_instance_groups_utils
from googlecloudsdk.api_lib.compute import utils
from googlecloudsdk.api_lib.compute import zone_utils
from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.compute import flags
from googlecloudsdk.command_lib.compute import scope as compute_scope
from googlecloudsdk.command_lib.compute.instance_groups import flags as instance_groups_flags
from googlecloudsdk.command_lib.compute.instance_groups.managed import flags as managed_flags
from googlecloudsdk.command_lib.compute.managed_instance_groups import auto_healing_utils
from googlecloudsdk.core import properties

# API allows up to 58 characters but asked us to send only 54 (unless user
# explicitly asks us for more).
_MAX_LEN_FOR_DEDUCED_BASE_INSTANCE_NAME = 54


def _AddInstanceGroupManagerArgs(parser):
  """Adds args."""
  parser.add_argument(
      '--template',
      required=True,
      help=('Specifies the instance template to use when creating new '
            'instances.'))
  parser.add_argument(
      '--base-instance-name',
      help=('The base name to use for the Compute Engine instances that will '
            'be created with the managed instance group. If not provided '
            'base instance name will be the prefix of instance group name.'))
  parser.add_argument(
      '--size',
      required=True,
      type=arg_parsers.BoundedInt(0, sys.maxsize, unlimited=True),
      help='The initial number of instances you want in this group.')
  parser.add_argument(
      '--description',
      help='An optional description for this group.')
  parser.add_argument(
      '--target-pool',
      type=arg_parsers.ArgList(),
      metavar='TARGET_POOL',
      help=('Specifies any target pools you want the instances of this '
            'managed instance group to be part of.'))


def _IsZonalGroup(ref):
  """Checks if reference to instance group is zonal."""
  return ref.Collection() == 'compute.instanceGroupManagers'


@base.ReleaseTracks(base.ReleaseTrack.GA, base.ReleaseTrack.BETA)
class CreateGA(base.CreateCommand):
  """Create Google Compute Engine managed instance groups."""

  @staticmethod
  def Args(parser):
    parser.display_info.AddFormat(managed_flags.DEFAULT_LIST_FORMAT)
    _AddInstanceGroupManagerArgs(parser)
    auto_healing_utils.AddAutohealingArgs(parser)
    igm_arg = instance_groups_flags.GetInstanceGroupManagerArg(zones_flag=True)
    igm_arg.AddArgument(parser, operation_type='create')
    instance_groups_flags.AddZonesFlag(parser)

  def CreateGroupReference(self, args, client, resources):
    if args.zones:
      zone_ref = resources.Parse(
          args.zones[0],
          collection='compute.zones',
          params={'project': properties.VALUES.core.project.GetOrFail})
      region = utils.ZoneNameToRegionName(zone_ref.Name())
      return resources.Parse(
          args.name,
          params={
              'region': region,
              'project': properties.VALUES.core.project.GetOrFail
          },
          collection='compute.regionInstanceGroupManagers')
    group_ref = (
        instance_groups_flags.GetInstanceGroupManagerArg().
        ResolveAsResource)(args, resources,
                           default_scope=compute_scope.ScopeEnum.ZONE,
                           scope_lister=flags.GetDefaultScopeLister(client))
    if _IsZonalGroup(group_ref):
      zonal_resource_fetcher = zone_utils.ZoneResourceFetcher(client)
      zonal_resource_fetcher.WarnForZonalCreation([group_ref])
    return group_ref

  def _CreateDistributionPolicy(self, zones, resources, messages):
    if zones:
      policy_zones = []
      for zone in zones:
        zone_ref = resources.Parse(
            zone,
            collection='compute.zones',
            params={'project': properties.VALUES.core.project.GetOrFail})
        policy_zones.append(
            messages.DistributionPolicyZoneConfiguration(
                zone=zone_ref.SelfLink()))
      return messages.DistributionPolicy(zones=policy_zones)

  def GetRegionForGroup(self, group_ref):
    if _IsZonalGroup(group_ref):
      return utils.ZoneNameToRegionName(group_ref.zone)
    else:
      return group_ref.region

  def GetServiceForGroup(self, group_ref, compute):
    if _IsZonalGroup(group_ref):
      return compute.instanceGroupManagers
    else:
      return compute.regionInstanceGroupManagers

  def CreateResourceRequest(self, group_ref, instance_group_manager, client,
                            resources):
    if _IsZonalGroup(group_ref):
      instance_group_manager.zone = group_ref.zone
      return client.messages.ComputeInstanceGroupManagersInsertRequest(
          instanceGroupManager=instance_group_manager,
          project=group_ref.project,
          zone=group_ref.zone)
    else:
      region_link = resources.Parse(
          group_ref.region,
          params={'project': properties.VALUES.core.project.GetOrFail},
          collection='compute.regions')
      instance_group_manager.region = region_link.SelfLink()
      return client.messages.ComputeRegionInstanceGroupManagersInsertRequest(
          instanceGroupManager=instance_group_manager,
          project=group_ref.project,
          region=group_ref.region)

  def _GetInstanceGroupManagerTargetPools(
      self, target_pools, group_ref, holder):
    pool_refs = []
    if target_pools:
      region = self.GetRegionForGroup(group_ref)
      for pool in target_pools:
        pool_refs.append(holder.resources.Parse(
            pool,
            params={
                'project': properties.VALUES.core.project.GetOrFail,
                'region': region
            },
            collection='compute.targetPools'))
    return [pool_ref.SelfLink() for pool_ref in pool_refs]

  def _GetInstanceGroupManagerBaseInstanceName(
      self, base_name_arg, group_ref):
    if base_name_arg:
      return base_name_arg
    return group_ref.Name()[0:_MAX_LEN_FOR_DEDUCED_BASE_INSTANCE_NAME]

  def _CreateInstanceGroupManager(
      self, args, group_ref, template_ref, client, holder):
    """Create parts of Instance Group Manager shared for the track."""
    instance_groups_flags.ValidateManagedInstanceGroupScopeArgs(
        args, holder.resources)
    health_check = managed_instance_groups_utils.GetHealthCheckUri(
        holder.resources, args)
    auto_healing_policies = (
        managed_instance_groups_utils.CreateAutohealingPolicies(
            client.messages, health_check, args.initial_delay))
    managed_instance_groups_utils.ValidateAutohealingPolicies(
        auto_healing_policies)
    return client.messages.InstanceGroupManager(
        name=group_ref.Name(),
        description=args.description,
        instanceTemplate=template_ref.SelfLink(),
        baseInstanceName=self._GetInstanceGroupManagerBaseInstanceName(
            args.base_instance_name, group_ref),
        targetPools=self._GetInstanceGroupManagerTargetPools(
            args.target_pool, group_ref, holder),
        targetSize=int(args.size),
        autoHealingPolicies=auto_healing_policies,
        distributionPolicy=self._CreateDistributionPolicy(
            args.zones, holder.resources, client.messages),
    )

  def Run(self, args):
    """Creates and issues an instanceGroupManagers.Insert request.

    Args:
      args: the argparse arguments that this command was invoked with.

    Returns:
      List containing one dictionary: resource augmented with 'autoscaled'
      property
    """
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client

    group_ref = self.CreateGroupReference(args, client, holder.resources)

    template_ref = holder.resources.Parse(
        args.template,
        params={'project': properties.VALUES.core.project.GetOrFail},
        collection='compute.instanceTemplates')

    instance_group_manager = self._CreateInstanceGroupManager(
        args, group_ref, template_ref, client, holder)
    request = self.CreateResourceRequest(group_ref, instance_group_manager,
                                         client, holder.resources)
    service = self.GetServiceForGroup(group_ref, client.apitools_client)
    migs = client.MakeRequests([(service, 'Insert', request)])

    migs_as_dicts = [encoding.MessageToDict(m) for m in migs]
    _, augmented_migs = (
        managed_instance_groups_utils.AddAutoscaledPropertyToMigs(
            migs_as_dicts, client, holder.resources))
    return augmented_migs


@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class CreateAlpha(CreateGA):
  """Create Google Compute Engine managed instance groups."""

  @classmethod
  def Args(cls, parser):
    CreateGA.Args(parser)
    instance_groups_flags.AddMigCreateStatefulFlags(parser)
    instance_groups_flags.AddMigInstanceRedistributionTypeFlag(parser)

  @staticmethod
  def _MakePreservedStateWithDisks(client, device_names):
    """Create StatefulPolicyPreservedState from a list of device names."""
    additional_properties = []
    # Disk device with AutoDelete NEVER
    disk_device = client.messages.StatefulPolicyPreservedStateDiskDevice(
        autoDelete=
        client.messages.StatefulPolicyPreservedStateDiskDevice \
          .AutoDeleteValueValuesEnum.NEVER)
    # Add all disk_devices to map
    for device_name in device_names:
      disk_value = client.messages.StatefulPolicyPreservedState.DisksValue \
        .AdditionalProperty(key=device_name, value=disk_device)
      additional_properties.append(disk_value)
    return client.messages.StatefulPolicyPreservedState(
        disks=client.messages.StatefulPolicyPreservedState.DisksValue(
            additionalProperties=additional_properties))

  @staticmethod
  def _GetStatefulPolicy(args, client):
    if args.stateful_disks:
      disks = [
          client.messages.StatefulPolicyPreservedDisk(deviceName=device)
          for device in args.stateful_disks
      ]
      preserved_resources = client.messages.StatefulPolicyPreservedResources(
          disks=disks)
      preserved_state =\
          CreateAlpha._MakePreservedStateWithDisks(client, args.stateful_disks)
      return client.messages.StatefulPolicy(
          preservedResources=preserved_resources,
          preservedState=preserved_state)
    if args.stateful_names:
      return client.messages.StatefulPolicy()
    return None

  def _CreateInstanceGroupManager(
      self, args, group_ref, template_ref, client, holder):
    """Create parts of Instance Group Manager shared for the track."""
    instance_groups_flags.ValidateManagedInstanceGroupScopeArgs(
        args, holder.resources)
    instance_groups_flags.ValidateManagedInstanceGroupStatefulProperties(args)
    health_check = managed_instance_groups_utils.GetHealthCheckUri(
        holder.resources, args)
    auto_healing_policies = (
        managed_instance_groups_utils.CreateAutohealingPolicies(
            client.messages, health_check, args.initial_delay))
    managed_instance_groups_utils.ValidateAutohealingPolicies(
        auto_healing_policies)
    instance_groups_flags.ValidateMigInstanceRedistributionTypeFlag(
        args.GetValue('instance_redistribution_type'), group_ref)
    update_policy = (managed_instance_groups_utils.
                     ApplyInstanceRedistributionTypeToUpdatePolicy)(
                         client, args.GetValue('instance_redistribution_type'),
                         None)

    return client.messages.InstanceGroupManager(
        name=group_ref.Name(),
        description=args.description,
        instanceTemplate=template_ref.SelfLink(),
        baseInstanceName=self._GetInstanceGroupManagerBaseInstanceName(
            args.base_instance_name, group_ref),
        targetPools=self._GetInstanceGroupManagerTargetPools(
            args.target_pool, group_ref, holder),
        targetSize=int(args.size),
        autoHealingPolicies=auto_healing_policies,
        distributionPolicy=self._CreateDistributionPolicy(
            args.zones, holder.resources, client.messages),
        statefulPolicy=self._GetStatefulPolicy(args, client),
        updatePolicy=update_policy,
    )


DETAILED_HELP = {
    'brief': 'Create a Compute Engine managed instance group',
    'DESCRIPTION': """\
        *{command}* creates a Google Compute Engine managed instance group.

For example, running:

        $ {command} example-managed-instance-group --zone us-central1-a --template example-instance-template --size 1

will create one managed instance group called 'example-managed-instance-group'
in the ``us-central1-a'' zone.
""",
}
CreateGA.detailed_help = DETAILED_HELP
