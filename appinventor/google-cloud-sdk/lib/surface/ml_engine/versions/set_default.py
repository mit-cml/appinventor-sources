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
"""ml-engine versions set-default command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.ml_engine import versions_api
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.ml_engine import flags
from googlecloudsdk.command_lib.ml_engine import versions_util


def _AddSetDefaultArgs(parser):
  flags.GetModelName(positional=False, required=True).AddToParser(parser)
  flags.VERSION_NAME.AddToParser(parser)


class SetDefault(base.DescribeCommand):
  """Sets an existing Cloud ML Engine version as the default for its model."""

  @staticmethod
  def Args(parser):
    _AddSetDefaultArgs(parser)

  def Run(self, args):
    return versions_util.SetDefault(versions_api.VersionsClient(),
                                    args.version,
                                    model=args.model)


_DETAILED_HELP = {
    'DESCRIPTION': """\
Sets an existing Cloud ML Engine version as the default for its model.

*{command}* sets an existing Cloud ML Engine version as the default for its
model.  Only one version may be the default for a given version.
"""
}


SetDefault.detailed_help = _DETAILED_HELP
