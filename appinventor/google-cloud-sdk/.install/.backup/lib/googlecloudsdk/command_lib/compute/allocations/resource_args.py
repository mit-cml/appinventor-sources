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

from googlecloudsdk.command_lib.compute import completers as compute_completers

from googlecloudsdk.command_lib.compute import flags as compute_flags


# TODO(b/119257245): clean up this file and move it to reservations/.
class RegionCommitmentsCompleter(compute_completers.ListCommandCompleter):

  def __init__(self, **kwargs):
    super(RegionCommitmentsCompleter, self).__init__(
        collection='compute.regionCommitments',
        list_command='alpha compute commitments list --uri',
        **kwargs)


def GetCommitmentResourceArg(required=True):
  return compute_flags.ResourceArgument(
      name='--commitment',
      resource_name='regional commitment',
      completer=RegionCommitmentsCompleter,
      plural=False,
      required=required,
      regional_collection='compute.regionCommitments',
      region_explanation=compute_flags.REGION_PROPERTY_EXPLANATION)


class ZoneAllocationsCompleter(compute_completers.ListCommandCompleter):

  def __init__(self, **kwargs):
    super(ZoneAllocationsCompleter, self).__init__(
        collection='compute.allocations',
        list_command='alpha compute allocations list --uri',
        **kwargs)


def GetAllocationResourceArg(positional=True):
  if positional:
    name = 'ALLOCATION'
  else:
    name = '--allocation'

  return compute_flags.ResourceArgument(
      name=name,
      resource_name='zonal allocation',
      completer=ZoneAllocationsCompleter,
      plural=False,
      required=True,
      zonal_collection='compute.allocations',
      zone_explanation=compute_flags.ZONE_PROPERTY_EXPLANATION)


def GetReservationResourceArg(positional=True):
  if positional:
    name = 'reservation'
  else:
    name = '--reservation'

  return compute_flags.ResourceArgument(
      name=name,
      resource_name='reservation',
      completer=ZoneAllocationsCompleter,
      plural=False,
      required=True,
      zonal_collection='compute.allocations',
      zone_explanation=compute_flags.ZONE_PROPERTY_EXPLANATION)
