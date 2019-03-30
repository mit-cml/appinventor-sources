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
"""Describe a CryptoKeyVersion."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.cloudkms import base as cloudkms_base
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.kms import flags


class Describe(base.DescribeCommand):
  """Get metadata for a given CryptoKeyVersion.

  Returns metadata for the given CryptoKeyVersion.

  ## EXAMPLES

  The following command returns metadata for version 2 within CryptoKey `frodo`
  within the KeyRing `fellowship` in the location `us-east1`:

    $ {command} 2 --cryptokey frodo --keyring fellowship --location us-east1
  """

  @staticmethod
  def Args(parser):
    flags.AddKeyVersionResourceArgument(parser, 'to describe')

  def Run(self, args):
    client = cloudkms_base.GetClientInstance()
    messages = cloudkms_base.GetMessagesModule()

    version_ref = flags.ParseCryptoKeyVersionName(args)
    return client.projects_locations_keyRings_cryptoKeys_cryptoKeyVersions.Get(
        messages.
        CloudkmsProjectsLocationsKeyRingsCryptoKeysCryptoKeyVersionsGetRequest(
            name=version_ref.RelativeName()))
