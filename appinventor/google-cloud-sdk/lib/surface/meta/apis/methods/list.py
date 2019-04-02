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

"""A command that lists the resource collections for a given API."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.meta.apis import flags
from googlecloudsdk.command_lib.util.apis import registry


class List(base.ListCommand):
  """List the methods of a resource collection for an API."""

  @staticmethod
  def Args(parser):
    base.PAGE_SIZE_FLAG.RemoveFromParser(parser)
    base.URI_FLAG.RemoveFromParser(parser)

    flags.API_VERSION_FLAG.AddToParser(parser)
    flags.COLLECTION_FLAG.AddToParser(parser)
    parser.display_info.AddFormat("""
      table(
        name:sort=1,
        detailed_path:optional,
        http_method,
        request_type,
        response_type
      )
    """)

  def Run(self, args):
    return registry.GetMethods(args.collection, api_version=args.api_version)
