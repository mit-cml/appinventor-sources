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
"""Shared utilities for access the Cloud KMS API."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.util import apis
from googlecloudsdk.core import resources


def GetClientInstance():
  return apis.GetClientInstance('cloudkms', 'v1')


def GetMessagesModule():
  return apis.GetMessagesModule('cloudkms', 'v1')


def MakeGetUriFunc(collection):
  """Returns a function which turns a resource into a uri.

  Example:
    class List(base.ListCommand):
      def GetUriFunc(self):
        return MakeGetUriFunc(self)

  Args:
    collection: A command instance.

  Returns:
    A function which can be returned in GetUriFunc.
  """

  def _GetUri(resource):
    registry = resources.REGISTRY.Clone()
    registry.RegisterApiByName('cloudkms', 'v1')
    parsed = registry.Parse(resource.name, collection=collection)
    return parsed.SelfLink()

  return _GetUri
