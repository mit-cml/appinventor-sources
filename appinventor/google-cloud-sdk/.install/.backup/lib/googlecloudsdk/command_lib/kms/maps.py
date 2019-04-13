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
"""Maps that match gcloud enum values to api enum ones."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.cloudkms import base as cloudkms_base
from googlecloudsdk.command_lib.util.apis import arg_utils

MESSAGES = cloudkms_base.GetMessagesModule()

DIGESTS = {'sha256', 'sha384', 'sha512'}

ALGORITHM_ENUM = MESSAGES.CryptoKeyVersionTemplate.AlgorithmValueValuesEnum
ALGORITHM_MAPPER = arg_utils.ChoiceEnumMapper('algorithm_enum', ALGORITHM_ENUM)

PURPOSE_ENUM = MESSAGES.CryptoKey.PurposeValueValuesEnum
PURPOSE_MAP = {
    'encryption': PURPOSE_ENUM.ENCRYPT_DECRYPT,
    'asymmetric-signing': PURPOSE_ENUM.ASYMMETRIC_SIGN,
    'asymmetric-encryption': PURPOSE_ENUM.ASYMMETRIC_DECRYPT,
}

PROTECTION_LEVEL_ENUM = (
    MESSAGES.CryptoKeyVersionTemplate.ProtectionLevelValueValuesEnum)
PROTECTION_LEVEL_MAPPER = arg_utils.ChoiceEnumMapper('protection_level_enum',
                                                     PROTECTION_LEVEL_ENUM)

# Add new algorithms according to their purposes here.
VALID_ALGORITHMS_MAP = {
    PURPOSE_ENUM.ENCRYPT_DECRYPT: ['google-symmetric-encryption'],
    PURPOSE_ENUM.ASYMMETRIC_SIGN: [
        'ec-sign-p256-sha256',
        'ec-sign-p384-sha384',
        'rsa-sign-pss-2048-sha256',
        'rsa-sign-pss-3072-sha256',
        'rsa-sign-pss-4096-sha256',
        'rsa-sign-pss-4096-sha512',
        'rsa-sign-pkcs1-2048-sha256',
        'rsa-sign-pkcs1-3072-sha256',
        'rsa-sign-pkcs1-4096-sha256',
        'rsa-sign-pkcs1-4096-sha512',
    ],
    PURPOSE_ENUM.ASYMMETRIC_DECRYPT: [
        'rsa-decrypt-oaep-2048-sha256',
        'rsa-decrypt-oaep-3072-sha256',
        'rsa-decrypt-oaep-4096-sha256',
        'rsa-decrypt-oaep-4096-sha512',
    ],
}

# Derive available algorithms from VALID_ALGORITHMS_MAP.
ALL_ALGORITHMS = {
    algorithm for algorithms in VALID_ALGORITHMS_MAP.values()
    for algorithm in algorithms
}
