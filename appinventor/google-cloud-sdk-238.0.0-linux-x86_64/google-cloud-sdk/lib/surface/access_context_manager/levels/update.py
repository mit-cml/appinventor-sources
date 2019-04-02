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
"""`gcloud access-context-manager levels update` command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.accesscontextmanager import levels as levels_api
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.accesscontextmanager import levels


@base.ReleaseTracks(base.ReleaseTrack.GA)
class UpdateLevelsGA(base.UpdateCommand):
  """Update an existing access level."""

  _API_VERSION = 'v1'

  @staticmethod
  def Args(parser):
    UpdateLevelsGA.ArgsVersioned(parser, version='v1')

  @staticmethod
  def ArgsVersioned(parser, version='v1'):
    levels.AddResourceArg(parser, 'to update')
    levels.AddLevelArgs(parser, version=version)
    levels.AddLevelSpecArgs(parser, version=version)

  def Run(self, args):
    client = levels_api.Client(version=self._API_VERSION)

    level_ref = args.CONCEPTS.level.Parse()

    mapper = levels.GetCombineFunctionEnumMapper(version=self._API_VERSION)
    combine_function = mapper.GetEnumForChoice(args.combine_function)
    return client.Patch(
        level_ref,
        description=args.description,
        title=args.title,
        combine_function=combine_function,
        basic_level_conditions=args.basic_level_spec)


@base.ReleaseTracks(base.ReleaseTrack.BETA, base.ReleaseTrack.ALPHA)
class UpdateLevelsBeta(UpdateLevelsGA):
  _API_VERSION = 'v1beta'

  @staticmethod
  def Args(parser):
    UpdateLevelsGA.ArgsVersioned(parser, version='v1beta')
