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
"""Commands for updating backend buckets."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from apitools.base.py import encoding

from googlecloudsdk.api_lib.compute import backend_buckets_utils
from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.calliope import base
from googlecloudsdk.calliope import exceptions
from googlecloudsdk.command_lib.compute import signed_url_flags
from googlecloudsdk.command_lib.compute.backend_buckets import flags as backend_buckets_flags
from googlecloudsdk.core import log


class Update(base.UpdateCommand):
  """Update a backend bucket.

  *{command}* is used to update backend buckets.
  """

  BACKEND_BUCKET_ARG = None

  @classmethod
  def Args(cls, parser):
    """Set up arguments for this command."""
    backend_buckets_utils.AddUpdatableArgs(cls, parser, 'update')
    backend_buckets_flags.GCS_BUCKET_ARG.AddArgument(parser)
    signed_url_flags.AddSignedUrlCacheMaxAge(
        parser, required=False, unspecified_help='')

  def AnyArgsSpecified(self, args):
    """Returns true if any args for updating backend bucket were specified."""
    return (args.IsSpecified('description') or
            args.IsSpecified('gcs_bucket_name') or
            args.IsSpecified('enable_cdn'))

  def GetGetRequest(self, client, backend_bucket_ref):
    """Returns a request to retrieve the backend bucket."""
    return (
        client.apitools_client.backendBuckets,
        'Get',
        client.messages.ComputeBackendBucketsGetRequest(
            project=backend_bucket_ref.project,
            backendBucket=backend_bucket_ref.Name()))

  def GetSetRequest(self, client, backend_bucket_ref, replacement):
    """Returns a request to update the backend bucket."""
    return (
        client.apitools_client.backendBuckets,
        'Patch',
        client.messages.ComputeBackendBucketsPatchRequest(
            project=backend_bucket_ref.project,
            backendBucket=backend_bucket_ref.Name(),
            backendBucketResource=replacement))

  def Modify(self, args, existing):
    """Modifies and returns the updated backend bucket."""
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client
    replacement = encoding.CopyProtoMessage(existing)
    cleared_fields = []

    if args.IsSpecified('description'):
      replacement.description = args.description

    if args.gcs_bucket_name:
      replacement.bucketName = args.gcs_bucket_name

    if args.enable_cdn is not None:
      replacement.enableCdn = args.enable_cdn

    if args.IsSpecified('signed_url_cache_max_age'):
      replacement.cdnPolicy = client.messages.BackendBucketCdnPolicy(
          signedUrlCacheMaxAgeSec=args.signed_url_cache_max_age)

    if not replacement.description:
      cleared_fields.append('description')
    return replacement, cleared_fields

  def MakeRequests(self, args):
    """Makes the requests for updating the backend bucket."""
    holder = base_classes.ComputeApiHolder(self.ReleaseTrack())
    client = holder.client

    backend_bucket_ref = self.BACKEND_BUCKET_ARG.ResolveAsResource(
        args, holder.resources)
    get_request = self.GetGetRequest(client, backend_bucket_ref)

    objects = client.MakeRequests([get_request])

    new_object, cleared_fields = self.Modify(args, objects[0])

    # If existing object is equal to the proposed object or if
    # Modify() returns None, then there is no work to be done, so we
    # print the resource and return.
    if objects[0] == new_object:
      log.status.Print(
          'No change requested; skipping update for [{0}].'.format(
              objects[0].name))
      return objects

    with client.apitools_client.IncludeFields(cleared_fields):
      return client.MakeRequests(
          [self.GetSetRequest(client, backend_bucket_ref, new_object)])

  def Run(self, args):
    """Issues the request necessary for updating a backend bucket."""
    if not self.AnyArgsSpecified(args) and not args.IsSpecified(
        'signed_url_cache_max_age'):
      raise exceptions.ToolException('At least one property must be modified.')
    return self.MakeRequests(args)
