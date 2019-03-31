# -*- coding: utf-8 -*- #
# Copyright 2015 Google Inc. All Rights Reserved.
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

"""Cloud Pub/Sub topics create command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from apitools.base.py import exceptions as api_ex

from googlecloudsdk.api_lib.pubsub import topics
from googlecloudsdk.api_lib.util import exceptions
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.kms import resource_args as kms_resource_args
from googlecloudsdk.command_lib.pubsub import resource_args
from googlecloudsdk.command_lib.pubsub import util
from googlecloudsdk.command_lib.util.args import labels_util
from googlecloudsdk.core import exceptions as core_exceptions
from googlecloudsdk.core import log
from googlecloudsdk.core import properties


def _Run(args, enable_labels=False, legacy_output=False, enable_kms=False):
  """Creates one or more topics."""
  client = topics.TopicsClient()

  labels = None
  if enable_labels:
    labels = labels_util.ParseCreateArgs(args,
                                         client.messages.Topic.LabelsValue)
  kms_key = None
  if enable_kms:
    kms_ref = args.CONCEPTS.kms_key.Parse()
    if kms_ref:
      kms_key = kms_ref.RelativeName()
    else:
      # Did user supply any topic-encryption-key flags?
      for keyword in [
          'topic-encryption-key', 'topic-encryption-key-project',
          'topic-encryption-key-location', 'topic-encryption-key-keyring'
      ]:
        if args.IsSpecified(keyword.replace('-', '_')):
          raise core_exceptions.Error(
              '--topic-encryption-key was not fully specified.')

  failed = []
  for topic_ref in args.CONCEPTS.topic.Parse():

    try:
      result = client.Create(topic_ref, labels=labels, kms_key=kms_key)
    except api_ex.HttpError as error:
      exc = exceptions.HttpException(error)
      log.CreatedResource(topic_ref.RelativeName(), kind='topic',
                          failed=exc.payload.status_message)
      failed.append(topic_ref.topicsId)
      continue

    if legacy_output:
      result = util.TopicDisplayDict(result)
    log.CreatedResource(topic_ref.RelativeName(), kind='topic')
    yield result

  if failed:
    raise util.RequestsFailedError(failed, 'create')


@base.ReleaseTracks(base.ReleaseTrack.GA)
class Create(base.CreateCommand):
  """Creates one or more Cloud Pub/Sub topics."""

  detailed_help = {
      'EXAMPLES': """\
          To create a Cloud Pub/Sub topic, run:

              $ {command} mytopic"""
  }

  @staticmethod
  def Args(parser):
    resource_args.AddTopicResourceArg(parser, 'to create.', plural=True)

  def Run(self, args):
    return _Run(args)


@base.ReleaseTracks(base.ReleaseTrack.BETA)
class CreateBeta(Create):
  """Creates one or more Cloud Pub/Sub topics."""

  @classmethod
  def Args(cls, parser):
    resource_args.AddTopicResourceArg(parser, 'to create.', plural=True)
    labels_util.AddCreateLabelsFlags(parser)

  def Run(self, args):
    legacy_output = properties.VALUES.pubsub.legacy_output.GetBool()
    return _Run(args, enable_labels=True, legacy_output=legacy_output)


@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class CreateAlpha(Create):
  """Creates one or more Cloud Pub/Sub topics."""

  @classmethod
  def Args(cls, parser):

    # Create KMS key presentation spec.
    kms_flag_overrides = {
        'kms-key': '--topic-encryption-key',
        'kms-keyring': '--topic-encryption-key-keyring',
        'kms-location': '--topic-encryption-key-location',
        'kms-project': '--topic-encryption-key-project'
    }

    permission_info = """
    The specified CloudKMS key should have purpose set to "ENCRYPT_DECRYPT".
    The service account,
    "service-${CONSUMER_PROJECT_NUMBER}@gcp-sa-pubsub.iam.gserviceaccount.com"

    requires the IAM cryptoKeyEncrypterDecrypter role for the given CloudKMS key.
    CONSUMER_PROJECT_NUMBER is the project number of the project that is the parent of the
    topic being created"""

    kms_key_presentation_spec = kms_resource_args.GetKmsKeyPresentationSpec(
        'topic', flag_overrides=kms_flag_overrides,
        permission_info=permission_info)

    # Create Topic presentation spec.
    topic_presentation_spec = resource_args.CreateTopicResourceArg(
        'to create.', positional=True, plural=True)
    resource_args.AddResourceArgs(
        parser, [kms_key_presentation_spec, topic_presentation_spec])
    labels_util.AddCreateLabelsFlags(parser)

  def Run(self, args):
    legacy_output = properties.VALUES.pubsub.legacy_output.GetBool()
    return _Run(args, enable_labels=True, enable_kms=True,
                legacy_output=legacy_output)
