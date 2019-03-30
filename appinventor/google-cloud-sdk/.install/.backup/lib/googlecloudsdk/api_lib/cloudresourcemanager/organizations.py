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
"""API library for cloudresourcemanager organizations."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from apitools.base.py import list_pager

from googlecloudsdk.api_lib.cloudresourcemanager import projects_util


class Client(object):

  def __init__(self, client=None, messages=None):
    self.client = client or projects_util.GetClient()
    self.messages = messages or self.client.MESSAGES_MODULE

  def List(self, filter_=None, limit=None, page_size=None):
    req = self.messages.SearchOrganizationsRequest(filter=filter_)
    return list_pager.YieldFromList(
        self.client.organizations, req,
        method='Search',
        limit=limit,
        batch_size_attribute='pageSize',
        batch_size=page_size,
        field='organizations')
