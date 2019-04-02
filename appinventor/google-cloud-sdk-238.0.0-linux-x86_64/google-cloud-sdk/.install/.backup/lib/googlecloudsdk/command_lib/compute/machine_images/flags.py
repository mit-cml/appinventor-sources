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
"""Flags and helpers for the compute machine-images commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.command_lib.compute import completers as compute_completers
from googlecloudsdk.command_lib.compute import flags as compute_flags

DEFAULT_LIST_FORMAT = """\
    table(
      name,
      status
    )"""


def MakeSourceInstanceArg():
  return compute_flags.ResourceArgument(
      resource_name='instance',
      name='--source-instance',
      completer=compute_completers.InstancesCompleter,
      required=True,
      zonal_collection='compute.instances',
      short_help='The source instance to create a machine image from.',
      zone_explanation=compute_flags.ZONE_PROPERTY_EXPLANATION)


def MakeMachineImageArg(plural=False):
  return compute_flags.ResourceArgument(
      name='IMAGE',
      resource_name='machineImage',
      completer=compute_completers.MachineImagesCompleter,
      plural=plural,
      global_collection='compute.machineImages')
