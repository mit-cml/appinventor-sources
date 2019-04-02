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
"""Common utility functions to consturct compute allocation message."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals


# TODO(b/119257245): clean up this file and move it to reservations/.


def MakeSpecificSKUAllocationMessage(
    messages, vm_count, accelerators, local_ssds, machine_type,
    min_cpu_platform):
  """Constructs a single specific sku allocation message object."""
  prop_msgs = (
      messages.AllocationSpecificSKUAllocationAllocatedInstanceProperties)
  return messages.AllocationSpecificSKUAllocation(
      count=vm_count,
      instanceProperties=prop_msgs(
          guestAccelerators=accelerators,
          localSsds=local_ssds,
          machineType=machine_type,
          minCpuPlatform=min_cpu_platform))


def MakeAllocationMessage(
    messages, allocation_name, specific_allocation,
    require_specific_allocation, allocation_zone):
  """Constructs a single allocation message object."""
  return messages.Allocation(
      name=allocation_name,
      specificAllocation=specific_allocation,
      specificAllocationRequired=require_specific_allocation,
      zone=allocation_zone)


def MakeAllocationMessageFromArgs(messages, args, allocation_ref):
  accelerators = MakeGuestAccelerators(
      messages, getattr(args, 'accelerator', None))
  local_ssds = MakeLocalSsds(
      messages, getattr(args, 'local_ssd', None))
  specific_allocation = MakeSpecificSKUAllocationMessage(
      messages, args.vm_count, accelerators, local_ssds, args.machine_type,
      args.min_cpu_platform)
  return MakeAllocationMessage(
      messages, allocation_ref.Name(), specific_allocation,
      args.require_specific_allocation, allocation_ref.zone)


def MakeGuestAccelerators(messages, accelerator_configs):
  """Constructs the repeated accelerator message objects."""
  if accelerator_configs is None:
    return []

  accelerators = []

  for a in accelerator_configs:
    m = messages.AcceleratorConfig(
        acceleratorCount=a['count'], acceleratorType=a['type'])
    accelerators.append(m)

  return accelerators


def MakeLocalSsds(messages, ssd_configs):
  """Constructs the repeated local_ssd message objects."""
  if ssd_configs is None:
    return []

  local_ssds = []
  disk_msg = (
      messages.
      AllocationSpecificSKUAllocationAllocatedInstancePropertiesAllocatedDisk)
  interface_msg = disk_msg.InterfaceValueValuesEnum

  for s in ssd_configs:
    if s['interface'].upper() == 'NVME':
      interface = interface_msg.NVME
    else:
      interface = interface_msg.SCSI
    m = disk_msg(
        diskSizeGb=s['size'],
        interface=interface)
    local_ssds.append(m)

  return local_ssds
