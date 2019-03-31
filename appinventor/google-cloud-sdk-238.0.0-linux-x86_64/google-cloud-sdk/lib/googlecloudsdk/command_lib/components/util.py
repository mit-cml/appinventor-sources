# -*- coding: utf-8 -*- #
# Copyright 2013 Google Inc. All Rights Reserved.
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

"""Utilities for components commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope import exceptions
from googlecloudsdk.core.updater import update_manager
from googlecloudsdk.core.util import files
from googlecloudsdk.core.util import platforms


def GetUpdateManager(group_args):
  """Construct the UpdateManager to use based on the common args for the group.

  Args:
    group_args: An argparse namespace.

  Returns:
    update_manager.UpdateManager, The UpdateManager to use for the commands.
  """
  try:
    os_override = platforms.OperatingSystem.FromId(
        group_args.operating_system_override)
  except platforms.InvalidEnumValue as e:
    raise exceptions.InvalidArgumentException('operating-system-override', e)
  try:
    arch_override = platforms.Architecture.FromId(
        group_args.architecture_override)
  except platforms.InvalidEnumValue as e:
    raise exceptions.InvalidArgumentException('architecture-override', e)

  platform = platforms.Platform.Current(os_override, arch_override)

  root = (files.ExpandHomeDir(group_args.sdk_root_override)
          if group_args.sdk_root_override else None)
  url = (files.ExpandHomeDir(group_args.snapshot_url_override)
         if group_args.snapshot_url_override else None)

  return update_manager.UpdateManager(
      sdk_root=root, url=url, platform_filter=platform)
