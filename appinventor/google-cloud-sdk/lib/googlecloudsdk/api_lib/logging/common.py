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

"""A library that contains common logging commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from apitools.base.py import list_pager

from googlecloudsdk.api_lib.logging import util
from googlecloudsdk.calliope import exceptions
from googlecloudsdk.core import properties


def FetchLogs(log_filter=None,
              order_by='DESC',
              limit=None,
              parent=None):
  """Fetches log entries.

  This method uses Cloud Logging V2 api.
  https://cloud.google.com/logging/docs/api/introduction_v2

  Entries are sorted on the timestamp field, and afterwards filter is applied.
  If limit is passed, returns only up to that many matching entries.

  If neither log_filter nor log_ids are passed, no filtering is done.

  Args:
    log_filter: filter expression used in the request.
    order_by: the sort order, either DESC or ASC.
    limit: how many entries to return.
    parent: the name of the log's parent resource, e.g. "projects/foo" or
      "organizations/123" or "folders/123". Defaults to the current project.

  Returns:
    A generator that returns matching log entries.
    Callers are responsible for handling any http exceptions.
  """
  if parent:
    if not ('projects/' in parent or 'organizations/' in parent
            or 'folders/' in parent or 'billingAccounts/' in parent):
      raise exceptions.InvalidArgumentException(
          'parent', 'Unknown parent type in parent %s' % parent)
  else:
    parent = 'projects/%s' % properties.VALUES.core.project.Get(required=True)
  # The backend has an upper limit of 1000 for page_size.
  # However, there is no need to retrieve more entries if limit is specified.
  page_size = min(limit or 1000, 1000)
  if order_by.upper() == 'DESC':
    order_by = 'timestamp desc'
  else:
    order_by = 'timestamp asc'

  client = util.GetClient()
  request = client.MESSAGES_MODULE.ListLogEntriesRequest(resourceNames=[parent],
                                                         filter=log_filter,
                                                         orderBy=order_by)
  return list_pager.YieldFromList(
      client.entries, request, field='entries', limit=limit,
      batch_size=page_size, batch_size_attribute='pageSize')
