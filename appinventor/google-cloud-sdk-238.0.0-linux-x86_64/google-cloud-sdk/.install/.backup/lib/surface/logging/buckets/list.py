# -*- coding: utf-8 -*- #
# Copyright 2019 Google Inc. All Rights Reserved.
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

"""'logging buckets list' command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.logging import util
from googlecloudsdk.calliope import base


class List(base.ListCommand):
  """Lists the defined buckets."""

  @staticmethod
  def Args(parser):
    """Register flags for this command."""
    util.AddNonProjectArgs(parser, 'List buckets')
    util.AddBucketLocationArg(
        parser, False,
        'Location from which to list buckets. By default, buckets in all '
        'locations will be listed')
    parser.display_info.AddFormat(
        'table(name, display_name, retentionPeriod, locked, create_time,'
        'update_time, description)')
    parser.display_info.AddCacheUpdater(None)

  def Run(self, args):
    """This is what gets called when the user runs this command.

    Args:
      args: an argparse namespace. All the arguments that were provided to this
      command invocation.

    Yields:
      The list of buckets.
    """
    result = util.GetClient().projects_locations_buckets.List(
        util.GetMessages().LoggingProjectsLocationsBucketsListRequest(
            parent=util.GetBucketLocationFromArgs(args)))
    for bucket in result.buckets:
      yield bucket
