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

"""Command for updating managed instance config."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.api_lib.compute import managed_instance_groups_utils
from googlecloudsdk.api_lib.compute.operations import poller
from googlecloudsdk.api_lib.util import waiter
from googlecloudsdk.calliope import base
from googlecloudsdk.calliope import exceptions
from googlecloudsdk.command_lib.compute import flags as compute_flags
from googlecloudsdk.command_lib.compute.instance_groups import flags as instance_groups_flags
from googlecloudsdk.command_lib.compute.instance_groups.managed.instance_configs import instance_configs_getter
from googlecloudsdk.command_lib.compute.instance_groups.managed.instance_configs import instance_configs_messages
from googlecloudsdk.command_lib.compute.instance_groups.managed.instance_configs import instance_disk_getter
import six


@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class Update(base.UpdateCommand):
  r"""Update per instance config of a managed instance group.

  *{command}* updates per instance config of instance controlled by a Google
  Compute Engine managed instance group. Command gives option to change the list
  of preserved resources by the instance during restart or recreation.

  For example:

    $ {command} example-group --instance=example-instance \
        --update-stateful-disk=device-name=my-disk-3,\
        source=projects/my-project/zones/us-central1-a/disks/my-disk-3 \
        --remove-stateful-disks=my-disk-1,my-disk-2

  will update stateful disk `my-disk-3` to the new one pointed by `source` (or
  add if `my-disk-3` did not exist in the instance config); it will also remove
  `my-disk-1` and `my-disk-2` from the instance config overrides - they will not
  be preserved anymore during next restart or recreation of the instance.
  """

  @staticmethod
  def _CombinePerInstanceConfigMessage(
      holder, configs_getter, igm_ref, instance_ref, update_stateful_disks,
      remove_stateful_disks, update_stateful_metadata,
      remove_stateful_metadata):
    disk_getter = instance_disk_getter.InstanceDiskGetter(
        instance_ref=instance_ref, holder=holder)
    messages = holder.client.messages
    per_instance_config = configs_getter.get_instance_config(
        igm_ref=igm_ref, instance_ref=instance_ref)

    remove_stateful_disks_set = set(remove_stateful_disks or [])
    update_stateful_disks_dict = Update._UpdateStatefulDisksToDict(
        update_stateful_disks)
    new_stateful_disks = []
    existing_disks = []
    if per_instance_config.preservedState.disks:
      existing_disks =\
          per_instance_config.preservedState.disks.additionalProperties
    for current_stateful_disk in existing_disks:
      disk_name = current_stateful_disk.key
      # Disk to be removed
      if disk_name in remove_stateful_disks_set:
        continue
      # Disk to be updated
      if disk_name in update_stateful_disks_dict:
        update_disk_data = update_stateful_disks_dict[disk_name]
        source = update_disk_data.get('source')
        mode = update_disk_data.get('mode')
        if not (source or mode):
          raise exceptions.InvalidArgumentException(
              parameter_name='--update-stateful-disk',
              message=('[source] or [mode] is required when updating'
                       ' [device-name] already existing in instance config'))
        preserved_disk = current_stateful_disk.value
        if source:
          preserved_disk.source = source
        if mode:
          preserved_disk.mode = instance_configs_messages.GetMode(
              messages=messages, mode=mode, preserved_state_mode=True)
        del update_stateful_disks_dict[disk_name]
      new_stateful_disks.append(current_stateful_disk)
    for update_stateful_disk in update_stateful_disks_dict.values():
      new_stateful_disks.append(
          instance_configs_messages.MakePreservedStateDiskEntry(
              messages=messages,
              stateful_disk_data=update_stateful_disk,
              disk_getter=disk_getter))

    existing_metadata = []
    if per_instance_config.preservedState.metadata:
      existing_metadata = per_instance_config.preservedState\
          .metadata.additionalProperties
    new_stateful_metadata = {
        metadata.key: metadata.value
        for metadata in existing_metadata
    }
    for stateful_metadata_key_to_remove in remove_stateful_metadata or []:
      if stateful_metadata_key_to_remove in new_stateful_metadata:
        del new_stateful_metadata[stateful_metadata_key_to_remove]
      else:
        raise exceptions.InvalidArgumentException(
            parameter_name='--remove-stateful-metadata',
            message=('stateful metadata key to remove `{0}` does not exist in'
                     ' the given instance config'.format(
                         stateful_metadata_key_to_remove)))
    new_stateful_metadata.update(update_stateful_metadata)

    # Create preserved state
    preserved_state = messages.PreservedState()
    preserved_state.disks = messages.PreservedState.DisksValue(
        additionalProperties=new_stateful_disks)
    preserved_state.metadata = messages.PreservedState.MetadataValue(
        additionalProperties=[
            instance_configs_messages.MakePreservedStateMetadataEntry(
                messages, key=key, value=value)
            for key, value in sorted(six.iteritems(new_stateful_metadata))]
    )
    per_instance_config.preservedState = preserved_state

    # Create overrides (only if required)
    if per_instance_config.override:
      per_instance_config.override = \
          instance_configs_messages.MakeOverridesFromPreservedState(
              messages, preserved_state)
      per_instance_config.override.reset('origin')
    return per_instance_config

  @staticmethod
  def _UpdateStatefulDisksToDict(update_stateful_disks):
    update_stateful_disks_dict = {}
    for update_stateful_disk in update_stateful_disks or []:
      update_stateful_disks_dict[update_stateful_disk.get(
          'device-name')] = update_stateful_disk
    return update_stateful_disks_dict

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

  @staticmethod
  def Args(parser):
    instance_groups_flags.GetInstanceGroupManagerArg(
        region_flag=True).AddArgument(
            parser, operation_type='update per instance config for')
    instance_groups_flags.AddMigStatefulFlagsForInstanceConfigs(
        parser, for_update=True)
    instance_groups_flags.AddMigStatefulForceInstanceUpdateFlag(parser)

  def Run(self, args):
    instance_groups_flags.ValidateMigStatefulFlagsForInstanceConfigs(
        args, for_update=True)

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
        igm_ref=igm_ref, instance_ref=instance_ref, should_exist=True)

    per_instance_config_message = self._CombinePerInstanceConfigMessage(
        holder, configs_getter, igm_ref, instance_ref,
        args.update_stateful_disk, args.remove_stateful_disks,
        args.update_stateful_metadata, args.remove_stateful_metadata)

    operation_ref = instance_configs_messages.CallPerInstanceConfigUpdate(
        holder=holder,
        igm_ref=igm_ref,
        per_instance_config_message=per_instance_config_message)

    if igm_ref.Collection() == 'compute.instanceGroupManagers':
      service = holder.client.apitools_client.instanceGroupManagers
    elif igm_ref.Collection() == 'compute.regionInstanceGroupManagers':
      service = holder.client.apitools_client.regionInstanceGroupManagers
    else:
      raise ValueError('Unknown reference type {0}'.format(
          igm_ref.Collection()))

    operation_poller = poller.Poller(service)
    update_result = waiter.WaitFor(operation_poller, operation_ref,
                                   'Updating instance config.')

    if args.force_instance_update:
      apply_operation_ref = (
          instance_configs_messages.CallApplyUpdatesToInstances)(
              holder=holder, igm_ref=igm_ref, instances=[str(instance_ref)])
      return waiter.WaitFor(operation_poller, apply_operation_ref,
                            'Applying updates to instances.')

    return update_result
