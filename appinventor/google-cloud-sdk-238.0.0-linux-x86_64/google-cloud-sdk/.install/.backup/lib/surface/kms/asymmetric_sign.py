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
"""Sign a user input file using an asymmetric-signing key."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.cloudkms import base as cloudkms_base
from googlecloudsdk.calliope import base
from googlecloudsdk.calliope import exceptions
from googlecloudsdk.command_lib.kms import flags
from googlecloudsdk.command_lib.kms import get_digest
from googlecloudsdk.core import log
from googlecloudsdk.core.util import files


@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class AsymmetricSign(base.Command):
  r"""Sign a user input file using an asymmetric-signing key version.

  Creates a digital signature of the input file using the provided
  asymmetric-signing key version and saves the base64 encoded signature.

  The required flag `signature-file` indicates the path to store signature.

  ## EXAMPLES
  The following command will read the file '/tmp/my/file.to.sign', digest it
  with the digest algorithm 'sha256' and sign it using the asymmetric CryptoKey
  `dont-panic` Version 3, and save the signature in base64 format to
  '/tmp/my/signature'.

    $ {command} \
    --location us-central1 \
    --keyring hitchhiker \
    --key dont-panic \
    --version 1 \
    --digest-algorithm sha256 \
    --input-file /tmp/my/file.to.sign \
    --signature-file /tmp/my/signature

  """

  @staticmethod
  def Args(parser):
    flags.AddKeyResourceFlags(parser, 'to use for signing.')
    flags.AddCryptoKeyVersionFlag(parser, 'to use for signing')
    flags.AddDigestAlgorithmFlag(parser, 'The algorithm to digest the input.')
    flags.AddInputFileFlag(parser, 'to sign')
    flags.AddSignatureFileFlag(parser, 'to output')

  def Run(self, args):
    client = cloudkms_base.GetClientInstance()
    messages = cloudkms_base.GetMessagesModule()

    try:
      digest = get_digest.GetDigest(args.digest_algorithm, args.input_file)
    except EnvironmentError as e:
      raise exceptions.BadFileException(
          'Failed to read input file [{0}]: {1}'.format(args.input_file, e))

    req = messages.CloudkmsProjectsLocationsKeyRingsCryptoKeysCryptoKeyVersionsAsymmetricSignRequest(  # pylint: disable=line-too-long
        name=flags.ParseCryptoKeyVersionName(args).RelativeName())
    req.asymmetricSignRequest = messages.AsymmetricSignRequest(digest=digest)

    resp = (
        client.projects_locations_keyRings_cryptoKeys_cryptoKeyVersions.
        AsymmetricSign(req))

    try:
      log.WriteToFileOrStdout(
          args.signature_file,
          resp.signature,
          overwrite=True,
          binary=True,
          private=True)
    except files.Error as e:
      raise exceptions.BadFileException(e)
