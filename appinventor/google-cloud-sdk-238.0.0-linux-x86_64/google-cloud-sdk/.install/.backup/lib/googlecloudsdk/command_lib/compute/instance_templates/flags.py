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
"""Flags and helpers for the compute instance groups commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.command_lib.compute import completers
from googlecloudsdk.command_lib.compute import flags

DEFAULT_LIST_FORMAT = """\
    table(
      name,
      properties.machineType.machine_type(),
      properties.scheduling.preemptible.yesno(yes=true, no=''),
      creationTimestamp
    )"""


def MakeInstanceTemplateArg(plural=False):
  return flags.ResourceArgument(
      resource_name='instance template',
      completer=completers.InstanceTemplatesCompleter,
      plural=plural,
      global_collection='compute.instanceTemplates')


def MakeSourceInstanceArg():
  return flags.ResourceArgument(
      name='--source-instance',
      resource_name='instance',
      completer=completers.InstancesCompleter,
      required=False,
      zonal_collection='compute.instances',
      short_help=('The name of the source instance that the instance template '
                  'will be created from.\n\nYou can override machine type and '
                  'labels. Values of other flags will be ignored and values '
                  'from the source instance will be used instead.')
  )
