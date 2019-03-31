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
"""Decrypt a ciphertext file using a key."""

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


class Decrypt(base.Command):
  r"""Decrypt a ciphertext file using a Cloud KMS key.

  {command} decrypts the given ciphertext file using the given Cloud KMS key and
  writes the result to the named plaintext file. Note that to permit users to
  decrypt using a key, they must be have at least one of the following IAM roles
  for that key: `roles/cloudkms.cryptoKeyDecrypter`,
  `roles/cloudkms.cryptoKeyEncrypterDecrypter`.

  Additional authenticated data (AAD) is used as an additional check by Cloud
  KMS to authenticate a decryption request. If an additional authenticated data
  file is provided, its contents must match the additional authenticated data
  provided during encryption and must not be larger than 64KiB. If you don't
  provide a value for `--additional-authenticated-data-file`, an empty string is
  used. For a thorough explanation of AAD, refer to this
  guide: https://cloud.google.com/kms/docs/additional-authenticated-data

  If `--ciphertext-file` or `--additional-authenticated-data-file` is set to
  '-', that file is read from stdin. Note that both files cannot be read from
  stdin. Similarly, if `--plaintext-file` is set to '-', the decrypted plaintext
  is written to stdout.

  ## EXAMPLES

  To decrypt the file 'path/to/ciphertext' using the key `frodo` with key
  ring `fellowship` and location `global` and write the plaintext
  to 'path/to/plaintext.dec', run:

    $ {command} \
        --key=frodo \
        --keyring=fellowship \
        --location=global \
        --ciphertext-file=path/to/input/ciphertext \
        --plaintext-file=path/to/output/plaintext.dec

  To decrypt the file 'path/to/ciphertext' using the key `frodo` and the
  additional authenticated data that was used to encrypt the ciphertext, and
  write the decrypted plaintext to stdout, run:

    $ {command} \
        --key=frodo \
        --keyring=fellowship \
        --location=global \
        --additional-authenticated-data-file=path/to/aad \
        --ciphertext-file=path/to/input/ciphertext \
        --plaintext-file='-'
  """

  @staticmethod
  def Args(parser):
    flags.AddKeyResourceFlags(parser, 'Cloud KMS key to use for decryption.')
    flags.AddCiphertextFileFlag(parser, 'to decrypt')
    flags.AddPlaintextFileFlag(parser, 'to output')
    flags.AddAadFileFlag(parser)

  def _ReadFileOrStdin(self, path, max_bytes):
    data = console_io.ReadFromFileOrStdin(path, binary=True)
    if len(data) > max_bytes:
      raise exceptions.BadFileException(
          'The file [{0}] is larger than the maximum size of {1} bytes.'.format(
              path, max_bytes))
    return data

  def Run(self, args):
    if (args.ciphertext_file == '-' and
        args.additional_authenticated_data_file == '-'):
      raise exceptions.InvalidArgumentException(
          '--ciphertext-file',
          '--ciphertext-file and --additional-authenticated-data-file cannot '
          'both read from stdin.')

    try:
      # The Encrypt API has a limit of 64K; the output ciphertext files will be
      # slightly larger. Check proactively (but generously) to avoid attempting
      # to buffer and send obviously oversized files to KMS.
      ciphertext = self._ReadFileOrStdin(
          args.ciphertext_file, max_bytes=2 * 65536)
    except files.Error as e:
      raise exceptions.BadFileException(
          'Failed to read ciphertext file [{0}]: {1}'.format(
              args.ciphertext_file, e))

    aad = None
    if args.additional_authenticated_data_file:
      try:
        # The Encrypt API limits the AAD to 64KiB.
        aad = self._ReadFileOrStdin(
            args.additional_authenticated_data_file, max_bytes=65536)
      except files.Error as e:
        raise exceptions.BadFileException(
            'Failed to read additional authenticated data file [{0}]: {1}'.
            format(args.additional_authenticated_data_file, e))

    crypto_key_ref = flags.ParseCryptoKeyName(args)

    client = cloudkms_base.GetClientInstance()
    messages = cloudkms_base.GetMessagesModule()

    req = messages.CloudkmsProjectsLocationsKeyRingsCryptoKeysDecryptRequest(
        name=crypto_key_ref.RelativeName())
    req.decryptRequest = messages.DecryptRequest(
        ciphertext=ciphertext, additionalAuthenticatedData=aad)

    resp = client.projects_locations_keyRings_cryptoKeys.Decrypt(req)

    try:
      log.WriteToFileOrStdout(
          args.plaintext_file, resp.plaintext, binary=True, overwrite=True)
    except files.Error as e:
      raise exceptions.BadFileException(e)
