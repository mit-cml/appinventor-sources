# -*- coding: utf-8 -*- #
# Copyright 2017 Google Inc. All Rights Reserved.
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

"""The gcloud resources list command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.util import resource_search
from googlecloudsdk.calliope import base


class List(base.ListCommand):
  r"""List Google Cloud resources accessible from your account.

  *{command}* lists all indexed Google Cloud resources that you have access to.
  Filter expressions apply to the specific resource type selected. Currently,
  only a limited subset of Cloud resource types are supported.

  ## EXAMPLES

  List all compute instances URIs with names starting with `test` or `prod`:

      $ gcloud alpha resources list --uri \
          --filter="@type:compute.instances name:(test prod)"

  Print the number of resources with any part containing the substring `foobar`:

      $ gcloud alpha resources list --filter="foobar" --uri | wc -l

  The previous command uses `--uri` to count because each output line is the URI
  for one resource. Otherwise the resource descriptions could be multiple lines
  per resource.
  """

  @staticmethod
  def Args(parser):
    base.FILTER_FLAG.RemoveFromParser(parser)
    parser.add_argument(
        '--filter',
        help=('A filter expression that is rewritten into a '
              'CloudResourceSearch query expression. It is applied to the '
              'resource specific data in the search results.'
              '\n\n'
              'By default all indexed resources are listed. Use '
              '`@type`:_COLLECTION_ to select resources for _COLLECTION_. It '
              'is an error to specify a _COLLECTION_ not indexed by the API. '
              'The supported collections are:\n * {collections}\n'
              'Collections named `resources.`_RESOURCE-TYPE_ may be used for '
              'debugging, where _RESOURCE-TYPE_ is defined by the '
              'CloudResourceSearch API.'
              '\n\n'
              'See `$ gcloud topic filters` for filter expression details.'
              .format(collections='\n * '.join(sorted(
                  resource_search.RESOURCE_TYPES.keys())))),
    )
    base.PAGE_SIZE_FLAG.SetDefault(parser, resource_search.PAGE_SIZE)

  def Run(self, args):
    query = args.filter
    args.filter = None
    return resource_search.List(limit=args.limit,
                                page_size=args.page_size,
                                query=query,
                                sort_by=args.sort_by,
                                uri=args.uri)
