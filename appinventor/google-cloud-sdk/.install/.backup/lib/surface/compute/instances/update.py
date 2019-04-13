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
"""Command for labels update to instances."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.api_lib.compute import instance_utils
from googlecloudsdk.api_lib.compute.operations import poller
from googlecloudsdk.api_lib.util import waiter
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.compute.instances import flags
from googlecloudsdk.command_lib.util.args import labels_util

DETAILED_HELP = {
    'DESCRIPTION': """\
        *{command}* updates labels and requested CPU Platform for a Google
        Compute
        Engine virtual machine.  For example:

          $ {command} example-instance --zone us-central1-a --update-labels=k0=value1,k1=value2 --remove-labels=k3

        will add/update labels ``k0'' and ``k1'' and remove labels with key
        ``k3''.

        Labels can be used to identify the instance and to filter them as in

          $ {parent_command} list --filter='labels.k1:value2'

        To list existing labels

          $ {parent_command} describe example-instance --format='default(labels)'
  """
}


@base.ReleaseTracks(base.ReleaseTrack.GA)
class Update(base.UpdateCommand):
  """Update a Google Compute Engine virtual machine."""

  @staticmethod
  def Args(parser):

    flags.INSTANCE_ARG.AddArgument(parser, operation_type='update')
    labels_util.AddUpdateLabelsFlags(parser)
    flags.AddMinCpuPlatformArgs(parser, Update.ReleaseTrack())
    flags.AddDeletionProtectionFlag(parser, use_default_value=False)

  def Run(self, args):
    return self._Run(args)

  def _Run(self, args, supports_shielded_vm=False,
           supports_display_device=False):
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client.apitools_client
    messages = holder.client.messages

    instance_ref = flags.INSTANCE_ARG.ResolveAsResource(
        args, holder.resources,
        scope_lister=flags.GetInstanceZoneScopeLister(holder.client))

    result = None

    labels_operation_ref = None
    min_cpu_platform_operation_ref = None
    deletion_protection_operation_ref = None
    shielded_vm_config_ref = None
    display_device_ref = None

    labels_diff = labels_util.Diff.FromUpdateArgs(args)
    if labels_diff.MayHaveUpdates():
      instance = client.instances.Get(
          messages.ComputeInstancesGetRequest(**instance_ref.AsDict()))
      result = instance
      labels_operation_ref = self._GetLabelsOperationRef(
          labels_diff, instance, instance_ref, holder)
    if hasattr(args, 'min_cpu_platform') and args.min_cpu_platform is not None:
      min_cpu_platform_operation_ref = self._GetMinCpuPlatformOperationRef(
          args.min_cpu_platform, instance_ref, holder)
    if args.deletion_protection is not None:
      deletion_protection_operation_ref = (
          self._GetDeletionProtectionOperationRef(
              args.deletion_protection, instance_ref, holder))

    operation_poller = poller.Poller(client.instances)
    result = self._WaitForResult(
        operation_poller, labels_operation_ref,
        'Updating labels of instance [{0}]', instance_ref.Name()) or result
    result = self._WaitForResult(
        operation_poller, min_cpu_platform_operation_ref,
        'Changing minimum CPU platform of instance [{0}]',
        instance_ref.Name()) or result
    result = self._WaitForResult(
        operation_poller, deletion_protection_operation_ref,
        'Setting deletion protection of instance [{0}] to [{1}]',
        instance_ref.Name(), args.deletion_protection) or result

    if supports_shielded_vm:
      if (args.IsSpecified('shielded_vm_secure_boot') or
          args.IsSpecified('shielded_vm_vtpm') or
          args.IsSpecified('shielded_vm_integrity_monitoring')):
        shielded_vm_config_ref = self._GetShieldedVMConfigRef(
            instance_ref, args, holder)
        result = self._WaitForResult(
            operation_poller, shielded_vm_config_ref,
            'Setting shieldedVMConfig  of instance [{0}]',
            instance_ref.Name()) or result

      if args.IsSpecified('shielded_vm_learn_integrity_policy'):
        shielded_vm_integrity_policy_ref = (
            self._GetShieldedVMIntegrityPolicyRef(instance_ref, holder))
        result = self._WaitForResult(
            operation_poller, shielded_vm_integrity_policy_ref,
            'Setting shieldedVMIntegrityPolicy of instance [{0}]',
            instance_ref.Name()) or result

    if supports_display_device and args.IsSpecified('enable_display_device'):
      display_device_ref = self._GetDisplayDeviceOperationRef(
          args.enable_display_device,
          instance_ref,
          holder)
      result = self._WaitForResult(
          operation_poller, display_device_ref,
          'Updating display device of instance [{0}]',
          instance_ref.Name()) or result

    return result

  def _GetShieldedVMConfigRef(self, instance_ref, args, holder):
    client = holder.client.apitools_client
    messages = holder.client.messages

    if (args.shielded_vm_secure_boot is None and
        args.shielded_vm_vtpm is None and
        args.shielded_vm_integrity_monitoring is None):
      return None
    shieldedvm_config_message = instance_utils.CreateShieldedVmConfigMessage(
        messages,
        args.shielded_vm_secure_boot,
        args.shielded_vm_vtpm,
        args.shielded_vm_integrity_monitoring)

    request = messages.ComputeInstancesUpdateShieldedVmConfigRequest(
        instance=instance_ref.Name(),
        project=instance_ref.project,
        shieldedVmConfig=shieldedvm_config_message,
        zone=instance_ref.zone)

    operation = client.instances.UpdateShieldedVmConfig(request)
    return holder.resources.Parse(
        operation.selfLink, collection='compute.zoneOperations')

  def _GetShieldedVMIntegrityPolicyRef(self, instance_ref, holder):
    client = holder.client.apitools_client
    messages = holder.client.messages

    shieldedvm_integrity_policy_message = (
        instance_utils.CreateShieldedVmIntegrityPolicyMessage(messages))

    request = messages.ComputeInstancesSetShieldedVmIntegrityPolicyRequest(
        instance=instance_ref.Name(),
        project=instance_ref.project,
        shieldedVmIntegrityPolicy=shieldedvm_integrity_policy_message,
        zone=instance_ref.zone)

    operation = client.instances.SetShieldedVmIntegrityPolicy(request)
    return holder.resources.Parse(
        operation.selfLink, collection='compute.zoneOperations')

  def _GetLabelsOperationRef(self, labels_diff, instance, instance_ref, holder):
    client = holder.client.apitools_client
    messages = holder.client.messages

    labels_update = labels_diff.Apply(
        messages.InstancesSetLabelsRequest.LabelsValue,
        instance.labels)

    if labels_update.needs_update:
      request = messages.ComputeInstancesSetLabelsRequest(
          project=instance_ref.project,
          instance=instance_ref.instance,
          zone=instance_ref.zone,
          instancesSetLabelsRequest=
          messages.InstancesSetLabelsRequest(
              labelFingerprint=instance.labelFingerprint,
              labels=labels_update.labels))

      operation = client.instances.SetLabels(request)
      return holder.resources.Parse(
          operation.selfLink, collection='compute.zoneOperations')

  def _GetMinCpuPlatformOperationRef(self, min_cpu_platform, instance_ref,
                                     holder):
    client = holder.client.apitools_client
    messages = holder.client.messages
    embedded_request = messages.InstancesSetMinCpuPlatformRequest(
        minCpuPlatform=min_cpu_platform or None)
    request = messages.ComputeInstancesSetMinCpuPlatformRequest(
        instance=instance_ref.instance,
        project=instance_ref.project,
        instancesSetMinCpuPlatformRequest=embedded_request,
        zone=instance_ref.zone)

    operation = client.instances.SetMinCpuPlatform(request)
    return holder.resources.Parse(
        operation.selfLink, collection='compute.zoneOperations')

  def _GetDeletionProtectionOperationRef(self, deletion_protection,
                                         instance_ref, holder):
    client = holder.client.apitools_client
    messages = holder.client.messages
    request = messages.ComputeInstancesSetDeletionProtectionRequest(
        deletionProtection=deletion_protection,
        project=instance_ref.project,
        resource=instance_ref.instance,
        zone=instance_ref.zone)

    operation = client.instances.SetDeletionProtection(request)
    return holder.resources.Parse(
        operation.selfLink, collection='compute.zoneOperations')

  def _GetDisplayDeviceOperationRef(self, display_device, instance_ref, holder):
    client = holder.client.apitools_client
    messages = holder.client.messages
    request = messages.ComputeInstancesUpdateDisplayDeviceRequest(
        displayDevice=messages.DisplayDevice(
            enableDisplay=display_device),
        project=instance_ref.project,
        instance=instance_ref.instance,
        zone=instance_ref.zone)

    operation = client.instances.UpdateDisplayDevice(request)
    return holder.resources.Parse(
        operation.selfLink, collection='compute.zoneOperations')

  def _WaitForResult(self, operation_poller, operation_ref, message, *args):
    if operation_ref:
      return waiter.WaitFor(
          operation_poller, operation_ref, message.format(*args))
    return None


@base.ReleaseTracks(base.ReleaseTrack.BETA)
class UpdateBeta(Update):
  """Update a Google Compute Engine virtual machine."""

  @staticmethod
  def Args(parser):
    flags.INSTANCE_ARG.AddArgument(parser, operation_type='update')
    labels_util.AddUpdateLabelsFlags(parser)
    flags.AddMinCpuPlatformArgs(parser, UpdateBeta.ReleaseTrack())
    flags.AddDeletionProtectionFlag(parser, use_default_value=False)
    flags.AddShieldedVMConfigArgs(
        parser, use_default_value=False, for_update=True)
    flags.AddShieldedVMIntegrityPolicyArgs(parser)
    flags.AddDisplayDeviceArg(parser, is_update=True)

  def Run(self, args):
    return self._Run(args,
                     supports_shielded_vm=True,
                     supports_display_device=True)


@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class UpdateAlpha(UpdateBeta):
  """Update a Google Compute Engine virtual machine."""

  @staticmethod
  def Args(parser):
    flags.INSTANCE_ARG.AddArgument(parser, operation_type='update')
    labels_util.AddUpdateLabelsFlags(parser)
    flags.AddMinCpuPlatformArgs(parser, UpdateAlpha.ReleaseTrack())
    flags.AddDeletionProtectionFlag(parser, use_default_value=False)
    flags.AddShieldedVMConfigArgs(
        parser, use_default_value=False, for_update=True)
    flags.AddShieldedVMIntegrityPolicyArgs(parser)
    flags.AddDisplayDeviceArg(parser, is_update=True)


Update.detailed_help = DETAILED_HELP
