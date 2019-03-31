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
"""Helper methods for constructing messages for the container CLI."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals


def AutoUpdateUpgradeRepairMessage(value, flag_name):
  """Messaging for when auto-upgrades or node auto-repairs.

  Args:
    value: bool, value that the flag takes.
    flag_name: str, the name of the flag. Must be either autoupgrade or
        autorepair

  Returns:
    the formatted message string.
  """
  action = 'enable' if value else 'disable'
  plural = flag_name + 's'
  link = 'node-management' if flag_name == 'autoupgrade' else 'node-auto-repair'
  return ('This will {0} the {1} feature for nodes. Please see '
          'https://cloud.google.com/kubernetes-engine/docs/'
          '{2} for more '
          'information on node {3}.').format(action, flag_name, link, plural)
