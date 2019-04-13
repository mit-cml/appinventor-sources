# -*- coding: utf-8 -*- #
# Copyright 2015 Google Inc. All Rights Reserved.
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

"""Supplementary help for uninstalling Cloud SDK."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope import base


class Uninstall(base.TopicCommand):
  """Supplementary help for uninstalling Cloud SDK.


  # Uninstalling Google Cloud SDK

  Note: For installations completed using an OS package (such as `apt-get`,
  `yum`, etc.), uninstall Cloud SDK via the OS package manager.

  To completely remove Cloud SDK, follow these instructions:

      * Locate your installation directory by running:

        $ gcloud info --format='value(installation.sdk_root)'

      * Locate your user config directory (typically `~/.config/gcloud`
        on MacOS and Linux) by running:

        $ gcloud info --format='value(config.paths.global_config_dir)'

      * Delete both these directories.

      * Additionally, remove lines sourcing `completion.bash.inc` and
        `paths.bash.inc` in your `.bashrc` or equivalent shell init file,
        if you added them during installation.
  """
