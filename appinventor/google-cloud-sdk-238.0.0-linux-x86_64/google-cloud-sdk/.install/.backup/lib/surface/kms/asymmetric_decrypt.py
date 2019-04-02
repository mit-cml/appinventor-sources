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
"""Decrypt an input file using an asymmetric-encryption key version."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.cloudkms import base as cloudkms_base
from googlecloudsdk.calliope import base
from googlecloudsdk.calliope import exceptions
from googlecloudsdk.command_lib.kms import flags
from googlecloudsdk.core import log
from googlecloudsdk.core.console import console_io
from googlecloudsdk.core.util import files


@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class AsymmetricDecrypt(base.Command):
  r"""Decrypt an input file using an asymmetric-encryption key version.

  Decrypts the given ciphertext file using the provided asymmetric-encryption
  key version and saves the decrypted data to the plaintext file.

  ## EXAMPLES
  The following command will read the file '/tmp/my/secret.file.enc', decrypt it
  using the asymmetric CryptoKey `dont-panic` Version 3 and write the plaintext
  to '/tmp/my/secret.file.dec'.

    $ {command} \
    --location us-central1 \
    --keyring hitchhiker \
    --key dont-panic \
    --version 3 \
    --ciphertext-file /tmp/my/secret.file.enc \
    --plaintext-file /tmp/my/secret.file.dec

  """

  @staticmethod
  def Args(parser):
    flags.AddKeyResourceFlags(parser, 'to use for asymmetric-decryption.')
    flags.AddCryptoKeyVersionFlag(parser, 'to use for asymmetric-decryption')
    flags.AddCiphertextFileFlag(parser, 'to decrypt')
    flags.AddPlaintextFileFlag(parser, 'to output')

  def Run(self, args):
    try:
      ciphertext = console_io.ReadFromFileOrStdin(
          args.ciphertext_file, binary=True)
    except files.Error as e:
      raise exceptions.BadFileException(
          'Failed to read ciphertext file [{0}]: {1}'.format(
              args.ciphertext_file, e))

    client = cloudkms_base.GetClientInstance()
    messages = cloudkms_base.GetMessagesModule()
    crypto_key_ref = flags.ParseCryptoKeyVersionName(args)

    req = messages.CloudkmsProjectsLocationsKeyRingsCryptoKeysCryptoKeyVersionsAsymmetricDecryptRequest(  # pylint: disable=line-too-long
        name=crypto_key_ref.RelativeName())
    req.asymmetricDecryptRequest = messages.AsymmetricDecryptRequest(
        ciphertext=ciphertext)

    resp = (
        client.projects_locations_keyRings_cryptoKeys_cryptoKeyVersions.
        AsymmetricDecrypt(req))

    try:
      log.WriteToFileOrStdout(
          args.plaintext_file,
          resp.plaintext or '',
          overwrite=True,
          binary=True,
          private=True)
    except files.Error as e:
      raise exceptions.BadFileException(e)
