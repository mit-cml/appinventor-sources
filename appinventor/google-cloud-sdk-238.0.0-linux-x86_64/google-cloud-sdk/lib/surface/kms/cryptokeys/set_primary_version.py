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
"""Set the primary version of a CryptoKey."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.cloudkms import base as cloudkms_base
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.kms import flags


class SetPrimaryVersion(base.Command):
  """Set the primary version of a CryptoKey.

  Sets the specified CryptoKeyVersion as the primary version of the
  given CryptoKey.
  The CryptoKeyVersion is specified by its version number assigned on creation.

  ## EXAMPLES

  The following command sets version 9 as the primary version of the
  CryptoKey `samwise` within KeyRing `fellowship` and location `global`:

    $ {command} samwise --version 9 --keyring fellowship --location global
  """

  @staticmethod
  def Args(parser):
    flags.AddKeyResourceArgument(parser, 'to modify')
    parser.add_argument(
        '--version',
        required=True,
        completer=flags.KeyVersionCompleter,
        help='The version to make primary.')

  def Run(self, args):
    # pylint: disable=line-too-long
    client = cloudkms_base.GetClientInstance()
    messages = cloudkms_base.GetMessagesModule()

    version_ref = flags.ParseCryptoKeyVersionName(args)

    req = messages.CloudkmsProjectsLocationsKeyRingsCryptoKeysUpdatePrimaryVersionRequest(
        name=version_ref.RelativeName(),
        updateCryptoKeyPrimaryVersionRequest=(
            messages.UpdateCryptoKeyPrimaryVersionRequest(
                cryptoKeyVersionId=version_ref.cryptoKeyVersionsId)))

    return client.projects_locations_keyRings_cryptoKeys.UpdatePrimaryVersion(
        req)
