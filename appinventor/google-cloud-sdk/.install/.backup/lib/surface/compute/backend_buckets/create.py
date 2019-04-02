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
"""Command for creating backend buckets."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import backend_buckets_utils
from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.compute import signed_url_flags
from googlecloudsdk.command_lib.compute.backend_buckets import flags as backend_buckets_flags


class Create(base.CreateCommand):
  """Create a backend bucket.

  *{command}* is used to create backend buckets. Backend buckets
  define Google Cloud Storage buckets that can serve content. URL
  maps define which requests are sent to which backend buckets.
  """

  BACKEND_BUCKET_ARG = None

  @classmethod
  def Args(cls, parser):
    """Set up arguments for this command."""
    parser.display_info.AddFormat(backend_buckets_flags.DEFAULT_LIST_FORMAT)
    backend_buckets_utils.AddUpdatableArgs(cls, parser, 'create')
    backend_buckets_flags.REQUIRED_GCS_BUCKET_ARG.AddArgument(parser)
    parser.display_info.AddCacheUpdater(
        backend_buckets_flags.BackendBucketsCompleter)
    signed_url_flags.AddSignedUrlCacheMaxAge(parser, required=False)

  def CreateBackendBucket(self, args):
    """Creates and returns the backend bucket."""
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client

    backend_buckets_ref = self.BACKEND_BUCKET_ARG.ResolveAsResource(
        args, holder.resources)

    enable_cdn = args.enable_cdn or False

    cdn_policy = None
    if args.IsSpecified('signed_url_cache_max_age'):
      cdn_policy = client.messages.BackendBucketCdnPolicy(
          signedUrlCacheMaxAgeSec=args.signed_url_cache_max_age)

    return client.messages.BackendBucket(
        description=args.description,
        name=backend_buckets_ref.Name(),
        bucketName=args.gcs_bucket_name,
        enableCdn=enable_cdn,
        cdnPolicy=cdn_policy)

  def Run(self, args):
    """Issues the request necessary for creating a backend bucket."""
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client

    backend_buckets_ref = self.BACKEND_BUCKET_ARG.ResolveAsResource(
        args, holder.resources)

    backend_bucket = self.CreateBackendBucket(args)

    request = client.messages.ComputeBackendBucketsInsertRequest(
        backendBucket=backend_bucket, project=backend_buckets_ref.project)
    return client.MakeRequests([(client.apitools_client.backendBuckets,
                                 'Insert', request)])
