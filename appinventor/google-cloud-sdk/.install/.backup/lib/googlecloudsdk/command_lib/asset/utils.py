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
"""The utils for asset surface."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope import exceptions as gcloud_exceptions


def VerifyParent(organization, project, folder, attribute='root cloud asset'):
  """Verify the parent name."""
  if organization is None and project is None and folder is None:
    raise gcloud_exceptions.RequiredArgumentException(
        '--organization or --project or --folder',
        'Should specify the project, or organization name, or the folder for '
        '{0}.'.format(attribute))
  if organization and project:
    raise gcloud_exceptions.ConflictingArgumentsException(
        'organization', 'project')
  if organization and folder:
    raise gcloud_exceptions.ConflictingArgumentsException(
        'organization', 'folder')
  if project and folder:
    raise gcloud_exceptions.ConflictingArgumentsException('project', 'folder')


def GetParentName(organization, project, folder, attribute='root cloud asset'):
  """Gets the parent name from organization Id, project Id, or folder Id."""
  VerifyParent(organization, project, folder, attribute)
  if organization:
    return 'organizations/{0}'.format(organization)
  if folder:
    return 'folders/{0}'.format(folder)
  return 'projects/{0}'.format(project)
