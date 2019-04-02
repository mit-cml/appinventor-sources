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

"""Flags and helpers for the compute disks commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.command_lib.compute import completers as compute_completers
from googlecloudsdk.command_lib.compute import flags as compute_flags

_DETAILED_SOURCE_SNAPSHOT_HELP = """\
      Source snapshot used to create the disks. It is safe to
      delete a snapshot after a disk has been created from the
      snapshot. In such cases, the disks will no longer reference
      the deleted snapshot. To get a list of snapshots in your
      current project, run `gcloud compute snapshots list`. A
      snapshot from an existing disk can be created using the
      `gcloud compute disks snapshot` command. This flag is mutually
      exclusive with *--image*.

      When using this option, the size of the disks must be at least
      as large as the snapshot size. Use *--size* to adjust the
      size of the disks.
"""


DEFAULT_LIST_FORMAT = """\
    table(
      name,
      zone.basename(),
      sizeGb,
      type.basename(),
      status
    )"""


MULTISCOPE_LIST_FORMAT = """
    table(
      name,
      location(),
      location_scope(),
      sizeGb,
      type.basename(),
      status
      )"""


class SnapshotsCompleter(compute_completers.ListCommandCompleter):

  def __init__(self, **kwargs):
    super(SnapshotsCompleter, self).__init__(
        collection='compute.snapshots',
        list_command='compute snapshots list --uri',
        **kwargs)


def MakeDiskArg(plural):
  return compute_flags.ResourceArgument(
      resource_name='disk',
      completer=compute_completers.DisksCompleter,
      plural=plural,
      name='DISK_NAME',
      zonal_collection='compute.disks',
      zone_explanation=compute_flags.ZONE_PROPERTY_EXPLANATION)


def MakeDiskArgZonalOrRegional(plural):
  return compute_flags.ResourceArgument(
      resource_name='disk',
      completer=compute_completers.DisksCompleter,
      plural=plural,
      name='DISK_NAME',
      zonal_collection='compute.disks',
      regional_collection='compute.regionDisks',
      zone_explanation=compute_flags.ZONE_PROPERTY_EXPLANATION,
      region_explanation=compute_flags.REGION_PROPERTY_EXPLANATION)

SOURCE_SNAPSHOT_ARG = compute_flags.ResourceArgument(
    resource_name='snapshot',
    completer=SnapshotsCompleter,
    name='--source-snapshot',
    plural=False,
    required=False,
    global_collection='compute.snapshots',
    short_help='Source snapshot used to create the disks.',
    detailed_help=_DETAILED_SOURCE_SNAPSHOT_HELP,)
