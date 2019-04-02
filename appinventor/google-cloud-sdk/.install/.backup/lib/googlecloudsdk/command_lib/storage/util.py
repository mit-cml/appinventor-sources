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

"""Utilities for storage commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals


from googlecloudsdk.core import properties


def SetBucket(resource_ref, namespace, request):
  """Helper used in a declarative hook to set the bucket in a storage request.

  This is needed because the buckets resource is not rooted under a project,
  but a project is required when creating a bucket or listing buckets.

  Args:
    resource_ref: The parsed bucket resource
    namespace: unused
    request: The request the declarative framework has generated.

  Returns:
    The request to issue.
  """
  del namespace
  request.project = properties.VALUES.core.project.Get(required=True)
  request.bucket.name = resource_ref.bucket
  return request
