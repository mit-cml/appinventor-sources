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
"""Create a key."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.cloudkms import base as cloudkms_base
from googlecloudsdk.calliope import base
from googlecloudsdk.calliope import exceptions
from googlecloudsdk.command_lib.kms import flags
from googlecloudsdk.command_lib.kms import maps
from googlecloudsdk.command_lib.util.args import labels_util


@base.ReleaseTracks(base.ReleaseTrack.BETA, base.ReleaseTrack.GA)
class Create(base.CreateCommand):
  r"""Create a new key.

  Creates a new key within the given keyring.

  The optional flags `rotation-period` and `next-rotation-time` define a
  rotation schedule for the key. A schedule can also be defined
  by the `create-rotation-schedule` command.

  The flag `next-rotation-time` must be in ISO 8601 or RFC3339 format,
  and `rotation-period` must be in the form INTEGER[UNIT], where units
  can be one of seconds (s), minutes (m), hours (h) or days (d).

  The optional flag 'labels' defines a user specified key/value pair for the
  given key.

  ## EXAMPLES

  The following command creates a key named `frodo` within the
  keyring `fellowship` and location `us-east1`:

    $ {command} frodo \
        --location us-east1 \
        --keyring fellowship \
        --purpose encryption

  The following command creates a key named `strider` within the
  keyring `rangers` and location `global` with a specified rotation
  schedule:

    $ {command} strider \
        --location global --keyring rangers \
        --purpose encryption \
        --rotation-period 30d \
        --next-rotation-time 2017-10-12T12:34:56.1234Z

  The following command creates a key named `foo` within the
  keyring `fellowship` and location `us-east1` with two specified labels:

    $ {command} foo \
        --location us-east1 \
        --keyring fellowship \
        --purpose encryption \
        --labels env=prod,team=kms
  """

  @staticmethod
  def Args(parser):
    flags.AddKeyResourceArgument(parser, 'to create')
    flags.AddRotationPeriodFlag(parser)
    flags.AddNextRotationTimeFlag(parser)
    labels_util.AddCreateLabelsFlags(parser)
    parser.add_argument(
        '--purpose',
        choices=sorted(maps.PURPOSE_MAP.keys()),
        required=True,
        help='The "purpose" of the key.')
    parser.display_info.AddCacheUpdater(flags.KeyRingCompleter)

  def _CreateRequest(self, args):
    messages = cloudkms_base.GetMessagesModule()

    crypto_key_ref = flags.ParseCryptoKeyName(args)
    parent_ref = flags.ParseParentFromResource(crypto_key_ref)

    req = messages.CloudkmsProjectsLocationsKeyRingsCryptoKeysCreateRequest(
        parent=parent_ref.RelativeName(),
        cryptoKeyId=crypto_key_ref.Name(),
        cryptoKey=messages.CryptoKey(
            # TODO(b/35914817): Find a better way to get the enum value by name.
            purpose=maps.PURPOSE_MAP[args.purpose],
            labels=labels_util.ParseCreateArgs(args,
                                               messages.CryptoKey.LabelsValue)))

    flags.SetNextRotationTime(args, req.cryptoKey)
    flags.SetRotationPeriod(args, req.cryptoKey)
    return req

  def Run(self, args):
    client = cloudkms_base.GetClientInstance()
    return client.projects_locations_keyRings_cryptoKeys.Create(
        self._CreateRequest(args))


@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class CreateALPHA(Create):
  r"""Create a new key.

  Creates a new key within the given keyring.

  The flag 'purpose' is always required when creating a key.
  The flag 'default-algorithm' is required when creating an asymmetric key.
  Algorithm and purpose should be compatible.

  The optional flags `rotation-period` and `next-rotation-time` define a
  rotation schedule for the key. A schedule can also be defined
  by the `create-rotation-schedule` command.

  The flag `next-rotation-time` must be in ISO 8601 or RFC3339 format,
  and `rotation-period` must be in the form INTEGER[UNIT], where units
  can be one of seconds (s), minutes (m), hours (h) or days (d).

  The optional flag `protection-level` specifies the protection level of the
  created key. The default is software; use "HSM" to create a hardware-backed
  key.

  ## EXAMPLES

  The following command creates a key named `frodo` within the
  keyring `fellowship` and location `us-east1`:

    $ {command} frodo \
        --location us-east1 \
        --keyring fellowship \
        --purpose encryption

  The following command creates a key named `strider` within the
  keyring `rangers` and location `global` with a specified rotation
  schedule:

    $ {command} strider \
        --location global --keyring rangers \
        --purpose encryption \
        --rotation-period 30d \
        --next-rotation-time 2017-10-12T12:34:56.1234Z

  The following command creates an asymmetric key named `samwise` with default
  algorithm 'ec-sign-p256-sha256' within the keyring `fellowship` and location
  `us-east1`:

    $ {command} samwise \
        --location us-east1 \
        --keyring fellowship \
        --purpose asymmetric-signing \
        --default-algorithm ec-sign-p256-sha256
  """

  @staticmethod
  def Args(parser):
    super(CreateALPHA, CreateALPHA).Args(parser)
    flags.AddProtectionLevelFlag(parser)
    flags.AddDefaultAlgorithmFlag(parser)

  def _CreateRequest(self, args):
    messages = cloudkms_base.GetMessagesModule()
    purpose = maps.PURPOSE_MAP[args.purpose]
    valid_algorithms = maps.VALID_ALGORITHMS_MAP[purpose]

    # Check default algorithm has been specified for asymmetric keys. For
    # backward compatibility, the algorithm is google-symmetric-encryption by
    # default if the purpose is encryption.
    if not args.default_algorithm:
      if args.purpose != 'encryption':
        raise exceptions.ToolException(
            '--default-algorithm needs to be specified when creating a key with'
            ' --purpose={}. The valid algorithms are: {}'.format(
                args.purpose, ', '.join(valid_algorithms)))
      args.default_algorithm = 'google-symmetric-encryption'

    # Check default algorithm and purpose are compatible.
    if args.default_algorithm not in valid_algorithms:
      raise exceptions.ToolException(
          'Default algorithm and purpose are incompatible. Here are the valid '
          'algorithms for --purpose={}: {}'.format(args.purpose,
                                                   ', '.join(valid_algorithms)))

    crypto_key_ref = flags.ParseCryptoKeyName(args)
    parent_ref = flags.ParseParentFromResource(crypto_key_ref)

    req = messages.CloudkmsProjectsLocationsKeyRingsCryptoKeysCreateRequest(
        parent=parent_ref.RelativeName(),
        cryptoKeyId=crypto_key_ref.Name(),
        cryptoKey=messages.CryptoKey(
            purpose=purpose,
            versionTemplate=messages.CryptoKeyVersionTemplate(
                # TODO(b/35914817): Find a better way to get the enum value by
                # name.
                protectionLevel=maps.PROTECTION_LEVEL_MAPPER.GetEnumForChoice(
                    args.protection_level),
                algorithm=maps.ALGORITHM_MAPPER.GetEnumForChoice(
                    args.default_algorithm)),
            labels=labels_util.ParseCreateArgs(args,
                                               messages.CryptoKey.LabelsValue)))

    flags.SetNextRotationTime(args, req.cryptoKey)
    flags.SetRotationPeriod(args, req.cryptoKey)

    return req
