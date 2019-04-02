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
"""Command to export assets to Google Cloud Storage."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.asset import client_util
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.asset import flags
from googlecloudsdk.command_lib.asset import utils as asset_utils
from googlecloudsdk.core import log


# pylint: disable=line-too-long
class Export(base.Command):
  """Export the cloud assets to Google Cloud Storage.

  Export the cloud assets to Google Cloud Storage. Use gcloud asset operations
  describe to get the latest status of the operation. Note that to use this
  command, you must be authenticated with a service account.
  See https://cloud.google.com/resource-manager/docs/cloud-asset-inventory/gcloud-asset
  for more details.
  """
  # pylint: enable=line-too-long

  @staticmethod
  def Args(parser):
    flags.AddParentArgs(parser)
    flags.AddSnapshotTimeArgs(parser)
    flags.AddAssetTypesArgs(parser)
    flags.AddContentTypeArgs(parser, required=False)
    flags.AddOutputPathArgs(parser)

  def Run(self, args):
    parent = asset_utils.GetParentName(args.organization, args.project,
                                       args.folder)
    if parent.startswith('projects'):
      client = client_util.AssetProjectExportClient(parent)
    elif parent.startswith('folders'):
      client = client_util.AssetFolderExportClient(parent)
    else:
      client = client_util.AssetOrganizationExportClient(parent)
    operation = client.Export(args)

    prefix = self.ReleaseTrack().prefix
    if prefix:
      operation_describe_command = 'gcloud {} asset operations describe'.format(
          prefix)
    else:
      operation_describe_command = 'gcloud asset operations describe'
    log.ExportResource(parent, is_async=True, kind='root asset')
    log.status.Print(
        'Use [{} {}] to check the status of the operation.'.format(
            operation_describe_command, operation.name))
