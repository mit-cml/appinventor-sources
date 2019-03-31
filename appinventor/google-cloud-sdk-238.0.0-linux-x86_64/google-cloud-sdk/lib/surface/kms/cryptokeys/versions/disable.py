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
"""Make a CryptoKeyVersion deactivated."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.cloudkms import base as cloudkms_base
from googlecloudsdk.api_lib.cloudkms import cryptokeyversions
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.kms import flags


class Disable(base.Command):
  """Disable a given CryptoKeyVersion.

  Disables the specified CryptoKeyVersion within the given CryptoKey.

  Only a CryptoKeyVersion which is Enabled can be Disabled.

  ## EXAMPLES

  The following command disables version 3 of CryptoKey `frodo` within
  KeyRing `fellowship` and Location `us-east1`:

    $ {command} 3 --location us-east1 --keyring fellowship --cryptokey frodo
  """

  @staticmethod
  def Args(parser):
    flags.AddKeyVersionResourceArgument(parser, 'to disable')

  def Run(self, args):
    messages = cloudkms_base.GetMessagesModule()

    version_ref = flags.ParseCryptoKeyVersionName(args)

    return cryptokeyversions.SetState(
        version_ref, messages.CryptoKeyVersion.StateValueValuesEnum.DISABLED)
