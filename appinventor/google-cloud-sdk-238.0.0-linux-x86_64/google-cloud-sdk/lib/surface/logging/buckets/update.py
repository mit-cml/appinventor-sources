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

"""'logging buckets update' command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.logging import util
from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.calliope import base
from googlecloudsdk.calliope import exceptions as calliope_exceptions


class Update(base.UpdateCommand):
  """Updates a bucket.

  Changes one or more proprties associated with a bucket.
  """

  @staticmethod
  def Args(parser):
    """Register flags for this command."""
    parser.add_argument(
        'BUCKET_ID', help='The id of the bucket to update.')
    parser.add_argument(
        '--retention-period', type=arg_parsers.Duration(),
        help='A new retention period for the bucket.')
    parser.add_argument(
        '--display-name',
        help='A new display name for the bucket.')
    parser.add_argument(
        '--description',
        help='A new description for the bucket.')
    util.AddBucketLocationArg(parser, True, 'Location of the bucket.')

  def Run(self, args):
    """This is what gets called when the user runs this command.

    Args:
      args: an argparse namespace. All the arguments that were provided to this
        command invocation.

    Returns:
      The updated bucket.
    """
    bucket_data = {}
    update_mask = []
    if args.retention_period:
      bucket_data['retentionPeriod'] = '%ds' % args.retention_period
      update_mask.append('retention_period')
    if args.display_name:
      bucket_data['displayName'] = args.display_name
      update_mask.append('display_name')
    if args.description:
      bucket_data['description'] = args.description
      update_mask.append('description')

    if not update_mask:
      raise calliope_exceptions.MinimumArgumentException(
          ['--retention-period', '--display-name', '--description'],
          'Please specify at least one property to update')

    return util.GetClient().projects_locations_buckets.Patch(
        util.GetMessages().LoggingProjectsLocationsBucketsPatchRequest(
            name=util.CreateResourceName(
                util.CreateResourceName(
                    util.GetProjectResource().RelativeName(), 'locations',
                    args.location),
                'buckets', args.BUCKET_ID),
            logBucket=util.GetMessages().LogBucket(**bucket_data),
            updateMask=','.join(update_mask)))
