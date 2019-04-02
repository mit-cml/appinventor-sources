# -*- coding: utf-8 -*- #
# Copyright 2018 Google Inc. All Rights Reserved.
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
"""Flags and helpers for the compute allocations commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.calliope import base

# TODO(b/116061353): use common flags instead.


def GetDescriptionFlag():
  return base.Argument(
      '--description', help='A text description of the allocation to create.')


def GetRequireSpecificAllocation():
  help_text = """\
Indicates whether the allocation can be consumed by VMs with "any allocation"
defined.
If enabled, then only VMs that target the allocation by name using
--allocation-affinity can consume this allocation. It also indicates that if
true, the existing instances are also accounted in the allocation.
"""
  return base.Argument(
      '--require-specific-allocation', action='store_true', help=help_text)


def GetVmCountFlag(required=True):
  return base.Argument(
      '--vm-count',
      required=required,
      type=int,
      help='The number of resources that are allocated to this allocation.')


def GetMinCpuPlatform():
  """Gets the --min-cpu-platform flag."""
  return base.Argument(
      '--min-cpu-platform',
      help='The minimum cpu platform of the allocation.')


def GetMachineType(required=True):
  """Gets the --machine-type flag."""
  help_text = """\
The type of machine (name only) which has fixed number of vCPUs and fixed amount
of memory. This also includes specifying custom machine type following
custom-NUMBER_OF_CPUS-AMOUNT_OF_MEMORY pattern.
"""
  return base.Argument('--machine-type', required=required, help=help_text)


def GetLocalSsdFlag():
  """Gets the -local-ssd flag."""
  help_text = """\
Manage the size and the interface of local SSD to use.

*interface*::: The kind of disk interface exposed to the VM for this SSD. Valid
values are ``scsi'' and ``nvme''. SCSI is the default and is supported by more
guest operating systems. NVME may provide higher performance.

*size*::: The size of the local SSD in base-2 GB.
"""
  return base.Argument(
      '--local-ssd',
      type=arg_parsers.ArgDict(spec={
          'interface': (lambda x: x.upper()),
          'size': int,
      }),
      action='append',
      help=help_text)


def GetAcceleratorFlag():
  """Gets the --accelerator flag."""
  help_text = """\
Manage the configuration of the type and number of accelerator cards attached.

*count*::: The number of pieces of the accelerator to attach to the allocation.

*type*::: The specific type (e.g. nvidia-tesla-k80 for nVidia Tesla K80) of
accelerator to attach to the allocation. Use
'gcloud compute accelerator-types list' to learn about all available accelerator
types.
"""
  return base.Argument(
      '--accelerator',
      type=arg_parsers.ArgDict(spec={
          'count': int,
          'type': str,
      }),
      action='append',
      help=help_text)


def GetDestinationFlag():
  """Get the --destination flag."""
  help_text = """\
The name of destination allocation where the machines are added. If existing,
its machine spec must match the modified machine spec. If non existing, new
allocation with this name and modified machine spec is created automatically.
"""
  return base.Argument('--destination', required=True, help=help_text)


def AddCreateFlags(parser):
  """Adds all flags needed for the create command."""
  GetDescriptionFlag().AddToParser(parser)

  group = base.ArgumentGroup(
      'Manage the specific SKU allocation properties to create', required=True)

  group.AddArgument(GetRequireSpecificAllocation())
  group.AddArgument(GetVmCountFlag())
  group.AddArgument(GetMinCpuPlatform())
  group.AddArgument(GetMachineType())
  group.AddArgument(GetLocalSsdFlag())
  group.AddArgument(GetAcceleratorFlag())

  group.AddToParser(parser)


def AddUpdateFlags(parser):
  """Adds all flags needed for the update command."""
  GetVmCountFlag().AddToParser(parser)
  GetDestinationFlag().AddToParser(parser)

  group = base.ArgumentGroup(
      'Manage the specific SKU allocation properties to update')

  group.AddArgument(GetMinCpuPlatform())
  group.AddArgument(GetMachineType())
  group.AddArgument(GetLocalSsdFlag())
  group.AddArgument(GetAcceleratorFlag())

  group.AddToParser(parser)
