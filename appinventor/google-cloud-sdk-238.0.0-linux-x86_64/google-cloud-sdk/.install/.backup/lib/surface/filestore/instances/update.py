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
"""Updates a Cloud Filestore instance."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.filestore import filestore_client
from googlecloudsdk.calliope import base
from googlecloudsdk.calliope import exceptions
from googlecloudsdk.command_lib.filestore.instances import flags as instances_flags
from googlecloudsdk.command_lib.util.args import labels_util
from googlecloudsdk.core import log

import six


@base.ReleaseTracks(base.ReleaseTrack.GA)
class Update(base.CreateCommand):
  """Update a Cloud Filestore instance."""

  _API_VERSION = filestore_client.V1_API_VERSION

  @staticmethod
  def Args(parser):
    instances_flags.AddInstanceUpdateArgs(parser)

  def Run(self, args):
    """Update a Cloud Filestore instance in the current project."""
    instance_ref = args.CONCEPTS.instance.Parse()
    client = filestore_client.FilestoreClient(self._API_VERSION)
    labels_diff = labels_util.Diff.FromUpdateArgs(args)
    orig_instance = client.GetInstance(instance_ref)
    if labels_diff.MayHaveUpdates():
      labels = labels_diff.Apply(client.messages.Instance.LabelsValue,
                                 orig_instance.labels).GetOrNone()
    else:
      labels = None

    try:
      instance = client.ParseUpdatedInstanceConfig(
          orig_instance,
          description=args.description, labels=labels,
          file_share=args.file_share)
    except filestore_client.Error as e:
      raise exceptions.InvalidArgumentException('--file-share',
                                                six.text_type(e))

    updated_fields = []
    if args.IsSpecified('description'):
      updated_fields.append('description')
    if (args.IsSpecified('update_labels')
        or args.IsSpecified('remove_labels')
        or args.IsSpecified('clear_labels')):
      updated_fields.append('labels')
    if args.IsSpecified('file_share'):
      updated_fields.append('fileShares')
    update_mask = ','.join(updated_fields)

    result = client.UpdateInstance(
        instance_ref, instance, update_mask, args.async)
    if args.async:
      if self._API_VERSION == 'V1':
        log.status.Print(
            'To check the status of the operation, run `gcloud filestore '
            'operations describe {}`'.format(result.name))
      else:
        log.status.Print(
            'To check the status of the operation, run `gcloud beta filestore '
            'operations describe {}`'.format(result.name))
    return result


@base.ReleaseTracks(base.ReleaseTrack.BETA)
class UpdateBeta(Update):
  """Update a Cloud Filestore instance."""

  _API_VERSION = filestore_client.BETA_API_VERSION


Update.detailed_help = {
    'DESCRIPTION':
        'Update a Cloud Filestore instance.',
    'EXAMPLES':
        """\
The following command updates the Cloud Filestore instance NAME to change the
description to "A new description."

  $ {command} NAME --description "A new description."

The following command updates a Cloud Filestore instance named NAME to add the
label "key1=value1" and remove any metadata with the label "key2".

  $ {command} NAME --update-labels key1=value1 --remove-labels key2
"""
}
