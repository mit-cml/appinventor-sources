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
"""Spanner instanceConfigs API helper."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from apitools.base.py import list_pager
from googlecloudsdk.api_lib.util import apis
from googlecloudsdk.core import properties
from googlecloudsdk.core import resources


def Get(config):
  """Get the specified instance config."""
  client = apis.GetClientInstance('spanner', 'v1')
  msgs = apis.GetMessagesModule('spanner', 'v1')
  ref = resources.REGISTRY.Parse(
      config,
      params={'projectsId': properties.VALUES.core.project.GetOrFail},
      collection='spanner.projects.instanceConfigs')
  req = msgs.SpannerProjectsInstanceConfigsGetRequest(
      name=ref.RelativeName())
  return client.projects_instanceConfigs.Get(req)


def List():
  """List instance configs in the project."""
  client = apis.GetClientInstance('spanner', 'v1')
  msgs = apis.GetMessagesModule('spanner', 'v1')
  req = msgs.SpannerProjectsInstanceConfigsListRequest(
      parent='projects/'+properties.VALUES.core.project.GetOrFail())
  return list_pager.YieldFromList(
      client.projects_instanceConfigs,
      req,
      field='instanceConfigs',
      batch_size_attribute='pageSize')

