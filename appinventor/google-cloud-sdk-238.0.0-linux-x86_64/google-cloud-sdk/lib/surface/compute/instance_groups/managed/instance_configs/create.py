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

"""Command for creating per instance config."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.api_lib.compute import managed_instance_groups_utils
from googlecloudsdk.api_lib.compute.operations import poller
from googlecloudsdk.api_lib.util import waiter
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.compute import flags as compute_flags
from googlecloudsdk.command_lib.compute.instance_groups import flags as instance_groups_flags
from googlecloudsdk.command_lib.compute.instance_groups.managed.instance_configs import instance_configs_getter
from googlecloudsdk.command_lib.compute.instance_groups.managed.instance_configs import instance_configs_messages
from googlecloudsdk.command_lib.compute.instance_groups.managed.instance_configs import instance_disk_getter
import six


# TODO(b/70321546): rewrite help
@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class Create(base.CreateCommand):
  """Create per instance config for managed instance group.

  *{command}* creates per instance config for instance controlled by a Google
  Compute Engine managed instance group. An instance with a per instance config
  will preserve given name and any listed disks during instance recreation and
  deletion. Preserved names will be (re)used and resources (re)attached in
  managed instance group during creation of the new instances in effect of
  recreation, restart and potentially other operations changing existence of the
  instance.

  You can use this command on an instance that does not exist. In this case
  config will be added to the pool of per instance configs to utilise for
  creating new instances. Order of utilisation of these configs from the pool is
  non deterministic.

  If created for existing instance, changes will be applied during next instance
  update or recreation - unless it is forced by `--force-instance-update`
  option.

  When you create config for non existing instance in regional managed instance
  group, use the full URI to the instance - pointing to target zone. Just
  instance name will not be resolved.
  """

  @staticmethod
  def Args(parser):
    instance_groups_flags.GetInstanceGroupManagerArg(
        region_flag=True).AddArgument(
            parser, operation_type='create per instance config for')
    instance_groups_flags.AddMigStatefulFlagsForInstanceConfigs(parser)
    instance_groups_flags.AddMigStatefulForceInstanceUpdateFlag(parser)

  @staticmethod
  def _GetPerInstanceConfigMessage(holder, instance_ref, stateful_disks,
                                   stateful_metadata):
    disk_getter = instance_disk_getter.InstanceDiskGetter(
        instance_ref=instance_ref, holder=holder)
    messages = holder.client.messages
    disk_overrides = [
        instance_configs_messages.GetDiskOverride(
            messages=messages,
            stateful_disk=stateful_disk,
            disk_getter=disk_getter) for stateful_disk in stateful_disks or []
    ]
    metadata_overrides = [
        messages.ManagedInstanceOverride.MetadataValueListEntry(
            key=metadata_key, value=metadata_value)
        for metadata_key, metadata_value in sorted(
            six.iteritems(stateful_metadata))
    ]
    return messages.PerInstanceConfig(
        instance=str(instance_ref),
        name=str(instance_ref).rsplit('/', 1)[-1],
        override=messages.ManagedInstanceOverride(
            disks=disk_overrides, metadata=metadata_overrides),
        preservedState=\
            instance_configs_messages.MakePreservedStateFromOverrides(
                holder.client.messages, disk_overrides, metadata_overrides))

  @staticmethod
  def _CreateInstanceReference(holder, igm_ref, instance_name):
    """Creates reference to instance in instance group (zonal or regional)."""
    if instance_name.startswith('https://') or instance_name.startswith(
        'http://'):
      return holder.resources.ParseURL(instance_name)
    instance_references = (
        managed_instance_groups_utils.CreateInstanceReferences)(
            holder=holder, igm_ref=igm_ref, instance_names=[instance_name])
    if not instance_references:
      raise managed_instance_groups_utils.ResourceCannotBeResolvedException(
          'Instance name {0} cannot be resolved'.format(instance_name))
    return instance_references[0]

  def Run(self, args):
    instance_groups_flags.ValidateMigStatefulFlagsForInstanceConfigs(args)

    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client
    resources = holder.resources

    igm_ref = (instance_groups_flags.MULTISCOPE_INSTANCE_GROUP_MANAGER_ARG.
               ResolveAsResource)(
                   args,
                   resources,
                   scope_lister=compute_flags.GetDefaultScopeLister(client),
               )

    instance_ref = self._CreateInstanceReference(
        holder=holder, igm_ref=igm_ref, instance_name=args.instance)

    configs_getter = (
        instance_configs_getter.InstanceConfigsGetterWithSimpleCache)(
            client)
    configs_getter.check_if_instance_config_exists(
        igm_ref=igm_ref, instance_ref=instance_ref, should_exist=False)

    per_instance_config_message = self._GetPerInstanceConfigMessage(
        holder, instance_ref, args.stateful_disk, args.stateful_metadata)

    operation_ref = instance_configs_messages.CallPerInstanceConfigUpdate(
        holder=holder,
        igm_ref=igm_ref,
        per_instance_config_message=per_instance_config_message)

    if igm_ref.Collection() == 'compute.instanceGroupManagers':
      service = client.apitools_client.instanceGroupManagers
    elif igm_ref.Collection() == 'compute.regionInstanceGroupManagers':
      service = client.apitools_client.regionInstanceGroupManagers
    else:
      raise ValueError('Unknown reference type {0}'.format(
          igm_ref.Collection()))

    operation_poller = poller.Poller(service)
    create_result = waiter.WaitFor(operation_poller, operation_ref,
                                   'Creating instance config.')

    if args.force_instance_update:
      apply_operation_ref = (
          instance_configs_messages.CallApplyUpdatesToInstances)(
              holder=holder, igm_ref=igm_ref, instances=[str(instance_ref)])
      return waiter.WaitFor(operation_poller, apply_operation_ref,
                            'Applying updates to instances.')

    return create_result
