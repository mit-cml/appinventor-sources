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
"""Shared utilities for access the CloudAsset API client."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import abc
from apitools.base.py import encoding
from apitools.base.py import exceptions as api_exceptions

from googlecloudsdk.api_lib.util import apis
from googlecloudsdk.api_lib.util import exceptions
from googlecloudsdk.command_lib.asset import utils as asset_utils
from googlecloudsdk.core import exceptions as core_exceptions
from googlecloudsdk.core.credentials import http
from googlecloudsdk.core.util import encoding as core_encoding
from googlecloudsdk.core.util import times

import six

API_NAME = 'cloudasset'
DEFAULT_API_VERSION = 'v1beta1'
BASE_URL = 'https://cloudasset.googleapis.com'
_HEADERS = {'Content-Type': 'application/json', 'X-HTTP-Method-Override': 'GET'}
_HTTP_ERROR_FORMAT = ('HTTP request failed with status code {}. '
                      'Response content: {}')


class MessageDecodeError(core_exceptions.Error):
  """Error raised when a failure to decode a message occurs."""


def GetMessages(version=DEFAULT_API_VERSION):
  """Import and return the cloudasset messages module.

  Args:
    version: the API version

  Returns:
    cloudasset message module.
  """
  return apis.GetMessagesModule(API_NAME, version)


def GetClient(version=DEFAULT_API_VERSION):
  """Import and return the cloudasset client module.

  Args:
    version: the API version

  Returns:
    cloudasset API client module.
  """
  return apis.GetClientInstance(API_NAME, version)


def ContentTypeTranslation(content_type):
  if content_type == 'resource':
    return 'RESOURCE'
  if content_type == 'iam-policy':
    return 'IAM_POLICY'
  return 'CONTENT_TYPE_UNSPECIFIED'


def MakeGetAssetsHistoryHttpRequests(args, api_version=DEFAULT_API_VERSION):
  """Manually make the get assets history request."""
  http_client = http.Http()
  query_params = [
      ('assetNames', asset_name) for asset_name in args.asset_names or []
  ]
  query_params.extend([('contentType',
                        ContentTypeTranslation(args.content_type)),
                       ('readTimeWindow.startTime',
                        times.FormatDateTime(args.start_time))])
  if args.IsSpecified('end_time'):
    query_params.extend([('readTimeWindow.endTime',
                          times.FormatDateTime(args.end_time))])
  parent = asset_utils.GetParentName(args.organization, args.project, None)
  url_base = '{0}/{1}/{2}:{3}'.format(BASE_URL, api_version, parent,
                                      'batchGetAssetsHistory')
  url_query = six.moves.urllib.parse.urlencode(query_params)
  url = '?'.join([url_base, url_query])
  response, raw_content = http_client.request(uri=url, headers=_HEADERS)

  content = core_encoding.Decode(raw_content)

  if response['status'] != '200':
    http_error = api_exceptions.HttpError(response, content, url)
    raise exceptions.HttpException(http_error)

  response_message_class = GetMessages(
      api_version).BatchGetAssetsHistoryResponse
  try:
    history_response = encoding.JsonToMessage(response_message_class, content)
  except ValueError as e:
    err_msg = ('Failed receiving proper response from server, cannot'
               'parse received assets. Error details: ' + str(e))
    raise MessageDecodeError(err_msg)

  for asset in history_response.assets:
    yield asset


@six.add_metaclass(abc.ABCMeta)
class AssetExportClient(object):
  """Base class for exporting assets."""

  def __init__(self, parent, api_version):
    self.parent = parent
    self.message_module = GetMessages(api_version)

  def Export(self, args):
    """Export assets with the asset export method."""
    content_type = ContentTypeTranslation(args.content_type)
    content_type = getattr(
        self.message_module.ExportAssetsRequest.ContentTypeValueValuesEnum,
        content_type)
    output_config = self.message_module.OutputConfig(
        gcsDestination=self.message_module.GcsDestination(uri=args.output_path))
    snapshot_time = None
    if args.snapshot_time:
      snapshot_time = times.FormatDateTime(args.snapshot_time)
    export_assets_request = self.message_module.ExportAssetsRequest(
        assetTypes=args.asset_types,
        contentType=content_type,
        outputConfig=output_config,
        readTime=snapshot_time)
    request_message = self.export_message(
        parent=self.parent, exportAssetsRequest=export_assets_request)
    operation = self.service.ExportAssets(request_message)
    return operation


class AssetProjectExportClient(AssetExportClient):

  def __init__(self, parent, api_version=DEFAULT_API_VERSION):
    super(AssetProjectExportClient, self).__init__(parent, api_version)
    self.service = GetClient(api_version).projects
    self.export_message = self.message_module.CloudassetProjectsExportAssetsRequest  # pylint: disable=line-too-long


class AssetOrganizationExportClient(AssetExportClient):

  def __init__(self, parent, api_version=DEFAULT_API_VERSION):
    super(AssetOrganizationExportClient, self).__init__(parent, api_version)
    self.service = GetClient(api_version).organizations
    self.export_message = self.message_module.CloudassetOrganizationsExportAssetsRequest  # pylint: disable=line-too-long


class AssetFolderExportClient(AssetExportClient):

  def __init__(self, parent, api_version=DEFAULT_API_VERSION):
    super(AssetFolderExportClient, self).__init__(parent, api_version)
    self.service = GetClient(api_version).folders
    self.export_message = self.message_module.CloudassetFoldersExportAssetsRequest  # pylint: disable=line-too-long
