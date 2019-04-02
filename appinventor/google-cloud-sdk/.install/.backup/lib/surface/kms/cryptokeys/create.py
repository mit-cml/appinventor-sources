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
"""Create a CryptoKey."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.cloudkms import base as cloudkms_base
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.kms import flags

PURPOSE_MAP = dict(encryption='ENCRYPT_DECRYPT')


class Create(base.CreateCommand):
  r"""Create a new CryptoKey.

  Creates a new CryptoKey within the given KeyRing.

  The optional flags `--rotation-period` and `--next-rotation-time` define a
  rotation schedule for the CryptoKey. A schedule can also be defined
  by the `create-rotation-schedule` command.

  The flag `--next-rotation-time` must be in ISO or RFC3339 format,
  and `--rotation-period` must be in the form INTEGER[UNIT], where units
  can be one of seconds (s), minutes (m), hours (h) or days (d).

  ## EXAMPLES

  The following command creates a CryptoKey named `frodo` within the
  KeyRing `fellowship` and location `us-east1`:

    $ {command} frodo \
        --location us-east1 \
        --keyring fellowship \
        --purpose encryption

  The following command creates a CryptoKey named `strider` within the
  KeyRing `rangers` and location `global` with a specified rotation
  schedule:

    $ {command} strider \
        --location global --keyring rangers \
        --purpose encryption \
        --rotation-period 30d \
        --next-rotation-time 2017-10-12T12:34:56.1234Z
  """

  @staticmethod
  def Args(parser):
    flags.AddKeyResourceArgument(parser, 'to create')
    flags.AddRotationPeriodFlag(parser)
    flags.AddNextRotationTimeFlag(parser)
    parser.add_argument(
        '--purpose',
        choices=list(PURPOSE_MAP.keys()),
        required=True,
        help='The "purpose" of the CryptoKey.')

  def Run(self, args):
    client = cloudkms_base.GetClientInstance()
    messages = cloudkms_base.GetMessagesModule()

    crypto_key_ref = flags.ParseCryptoKeyName(args)
    parent_ref = flags.ParseKeyRingName(args)

    req = messages.CloudkmsProjectsLocationsKeyRingsCryptoKeysCreateRequest(
        parent=parent_ref.RelativeName(),
        cryptoKeyId=crypto_key_ref.Name(),
        cryptoKey=messages.CryptoKey(
            # TODO(b/35914817): Find a better way to get the enum value by name.
            purpose=getattr(messages.CryptoKey.PurposeValueValuesEnum,
                            PURPOSE_MAP[args.purpose]),),)

    flags.SetNextRotationTime(args, req.cryptoKey)
    flags.SetRotationPeriod(args, req.cryptoKey)

    return client.projects_locations_keyRings_cryptoKeys.Create(req)
