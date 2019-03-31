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
"""The gcloud firestore operations list command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.firestore import admin_api
from googlecloudsdk.api_lib.firestore import operations
from googlecloudsdk.api_lib.firestore import rewrite_backend
from googlecloudsdk.calliope import base
from googlecloudsdk.core import properties


class List(base.ListCommand):
  """List pending Cloud Firestore admin operations and their status.

  Filters are case-sensitive and have the following syntax:

    field = value [AND [field = value]] ...

  Only the logical `AND` operator is
  supported; space-separated items are treated as having an implicit `AND`
  operator.
  """

  @staticmethod
  def Args(parser):
    """Register flags for this command."""
    base.PAGE_SIZE_FLAG.SetDefault(parser, operations.DEFAULT_PAGE_SIZE)
    base.LIMIT_FLAG.SetDefault(parser, operations.DEFAULT_PAGE_SIZE)

  def Run(self, args):
    frontend_filter, backend_filter = self._ConvertFilter(args.filter)
    # Override existing filter with frontend filter.
    args.filter = frontend_filter
    return operations.ListOperations(
        project=properties.VALUES.core.project.Get(required=True),
        database=admin_api.DEFAULT_DATABASE,
        limit=args.limit,
        operation_filter=backend_filter)

  def _ConvertFilter(self, expression):
    """Translates user-provided filter spec into one our backend understands.

    Args:
      expression: a filter spec to translate
    Returns:
      A tuple of string filter specs. The first is the frontend spec for post
      filtering, the second is a spec that the Firestore Admin API understands.
    """
    operation_rewrite_backend = rewrite_backend.OperationsRewriteBackend()
    return operation_rewrite_backend.Rewrite(expression)
