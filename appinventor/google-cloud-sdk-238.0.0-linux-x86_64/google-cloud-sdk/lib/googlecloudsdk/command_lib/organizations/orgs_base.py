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

"""Base class for Organization commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.util import apis
from googlecloudsdk.calliope import base
from googlecloudsdk.core import resources


class OrganizationCommand(base.Command):
  """Common methods for an organization command."""

  ORGS_API = 'cloudresourcemanager'
  ORGS_COLLECTION = ORGS_API + '.organizations'
  ORGS_API_VERSION = 'v1'

  @classmethod
  def OrganizationsClient(cls):
    client = apis.GetClientInstance(cls.ORGS_API, cls.ORGS_API_VERSION)
    return client.organizations

  @classmethod
  def OrganizationsMessages(cls):
    return apis.GetMessagesModule(cls.ORGS_API, cls.ORGS_API_VERSION)

  @classmethod
  def GetOrganizationRef(cls, organization_id):
    registry = resources.REGISTRY.Clone()
    registry.RegisterApiByName(cls.ORGS_API, cls.ORGS_API_VERSION)
    prefix = 'organizations/'
    if organization_id.startswith(prefix):
      organization_id = organization_id[len(prefix):]
    return registry.Parse(
        None,
        params={
            'organizationsId': organization_id,
        },
        collection=cls.ORGS_COLLECTION)


def OrganizationsUriFunc(resource):
  ref = OrganizationCommand.GetOrganizationRef(resource.name)
  return ref.SelfLink()
