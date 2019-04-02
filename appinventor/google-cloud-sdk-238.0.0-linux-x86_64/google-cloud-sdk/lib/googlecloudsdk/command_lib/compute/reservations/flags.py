# -*- coding: utf-8 -*- #
# Copyright 2019 Google Inc. All Rights Reserved.
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
"""Flags and helpers for the compute reservations commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.calliope import base


def GetDescriptionFlag():
  return base.Argument(
      '--description',
      help='An optional description of the reservation to create.')


def GetRequireSpecificAllocation():
  help_text = """\
Indicates whether the reservation can be consumed by VMs with "any reservation"
defined. If enabled, then only VMs that target this reservation by name using
``--reservation-affinity=specific`` can consume from this reservation.
"""
  return base.Argument(
      '--require-specific-reservation', action='store_true', help=help_text)


def GetVmCountFlag(required=True):
  return base.Argument(
      '--vm-count',
      required=required,
      type=int,
      help="""\
The number of VM instances that are allocated to this reservation.
The value of this field must be an int in the range [1, 1000].
""")


def GetMinCpuPlatform():
  """Gets the --min-cpu-platform flag."""
  return base.Argument(
      '--min-cpu-platform',
      help="""\
An optional minimum CPU platform of the reservation to create.
""")


def GetMachineType(required=True):
  """Gets the --machine-type flag."""
  help_text = """\
The type of machine (name only) which has a fixed number of vCPUs and a fixed amount
of memory. This also includes specifying custom machine type following
``custom-number_of_CPUs-amount_of_memory`` pattern, e.g. ``custom-32-29440``.
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
*count*::: The number of accelerators to attach to each instance in the reservation.
*type*::: The specific type (e.g. ``nvidia-tesla-k80`` for nVidia Tesla K80) of
accelerator to attach to instances in the reservation. Use
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


def AddCreateFlags(parser):
  """Adds all flags needed for the create command."""
  GetDescriptionFlag().AddToParser(parser)

  group = base.ArgumentGroup(
      'Manage the specific SKU reservation properties to create', required=True)

  group.AddArgument(GetRequireSpecificAllocation())
  group.AddArgument(GetVmCountFlag())
  group.AddArgument(GetMinCpuPlatform())
  group.AddArgument(GetMachineType())
  group.AddArgument(GetLocalSsdFlag())
  group.AddArgument(GetAcceleratorFlag())

  group.AddToParser(parser)
