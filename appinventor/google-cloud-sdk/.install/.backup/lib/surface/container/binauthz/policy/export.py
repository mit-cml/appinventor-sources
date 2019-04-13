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
"""Export Binary Authorization policy command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.container.binauthz import apis
from googlecloudsdk.api_lib.container.binauthz import policies
from googlecloudsdk.api_lib.container.binauthz import util
from googlecloudsdk.calliope import base


class Export(base.Command):
  """Export the Binary Authorization policy for the current project.

  This function's default output is a valid policy YAML file. If dumped to a
  file and edited, the new policy can be provided to the $ {parent_command}
  import command to cause these edits to be reflected in the project policy.

  ## EXAMPLE

  One way of updating the current project's policy is to run:

      $ {parent_command} export > my_policy.yaml
      $ edit my_policy.yaml
      $ {parent_command} import --policy-file=my_policy.yaml
  """

  def Run(self, args):
    api_version = apis.GetApiVersion(self.ReleaseTrack())
    return policies.Client(api_version).Get(util.GetPolicyRef())
