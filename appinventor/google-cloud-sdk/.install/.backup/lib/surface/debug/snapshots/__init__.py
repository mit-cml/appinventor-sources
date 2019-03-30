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

"""The targets command group for the gcloud debug command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.debug import flags


class Snapshots(base.Group):
  """Commands for interacting with Cloud Debugger snapshots.

  Commands to interact with debug snapshots. Snapshots allow you to capture
  stack traces and local variables from running services without interfering
  with the normal function of the service.
  """

  detailed_help = {
      # TODO(b/36056503) Add some examples
      # 'EXAMPLES': ''
  }

  @staticmethod
  def Args(parser):
    flags.AddTargetOption(parser)
