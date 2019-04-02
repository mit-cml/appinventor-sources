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
"""`gcloud access-context-manager zones update` command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.accesscontextmanager import zones as zones_api
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.accesscontextmanager import perimeters
from googlecloudsdk.command_lib.util.args import repeated


@base.ReleaseTracks(base.ReleaseTrack.ALPHA, base.ReleaseTrack.BETA)
class Update(base.UpdateCommand):
  """Update an existing access zone."""

  @staticmethod
  def Args(parser):
    perimeters.AddResourceArg(parser, 'to update')
    perimeters.AddPerimeterUpdateArgs(parser)

  def Run(self, args):
    client = zones_api.Client()
    perimeter_ref = args.CONCEPTS.perimeter.Parse()
    result = repeated.CachedResult.FromFunc(client.Get, perimeter_ref)

    return client.Patch(
        perimeter_ref,
        description=args.description,
        title=args.title,
        perimeter_type=perimeters.GetTypeEnumMapper().GetEnumForChoice(
            args.type),
        resources=perimeters.ParseResources(args, result),
        restricted_services=perimeters.ParseRestrictedServices(args, result),
        unrestricted_services=perimeters.ParseUnrestrictedServices(
            args, result),
        levels=perimeters.ParseLevels(args, result,
                                      perimeter_ref.accessPoliciesId))
