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

"""Command to create private keys for service accounts."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import textwrap

from googlecloudsdk.api_lib.iam import util
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.iam import iam_util
from googlecloudsdk.core import log


class Create(base.Command):
  """Create a private key for a service account."""

  detailed_help = {
      'NOTES': textwrap.dedent("""\
          The option --key-file-type=p12 is available here only for legacy
          reasons; all new use cases are encouraged to use the default 'json'
          format.
          """),
      'EXAMPLES': textwrap.dedent("""\
          To create a new private key for a service account, and save a copy
          of it locally, run:

            $ {command} --iam-account my-iam-account@somedomain.com key.json
          """),
  }

  @staticmethod
  def Args(parser):
    parser.add_argument('--key-file-type',
                        choices=['json', 'p12'],
                        default='json',
                        help='The type of key to create.')

    parser.add_argument('--iam-account',
                        required=True,
                        type=iam_util.GetIamAccountFormatValidator(),
                        help='The service account for which to create a key.')

    parser.add_argument('output',
                        metavar='OUTPUT-FILE',
                        type=iam_util.GetIamOutputFileValidator(),
                        help='The path where the resulting private key should '
                        'be written. File system write permission will be '
                        'checked on the specified path prior to the key '
                        'creation.')

  def Run(self, args):
    client, messages = util.GetClientAndMessages()
    result = client.projects_serviceAccounts_keys.Create(
        messages.IamProjectsServiceAccountsKeysCreateRequest(
            name=iam_util.EmailToAccountResourceName(args.iam_account),
            createServiceAccountKeyRequest=
            messages.CreateServiceAccountKeyRequest(
                privateKeyType=iam_util.KeyTypeToCreateKeyType(
                    iam_util.KeyTypeFromString(args.key_file_type)))))

    # Only the creating user has access. Set file permission to "-rw-------".
    log.WriteToFileOrStdout(
        args.output, content=result.privateKeyData, binary=True, private=True)
    log.status.Print(
        'created key [{0}] of type [{1}] as [{2}] for [{3}]'.format(
            iam_util.GetKeyIdFromResourceName(result.name),
            iam_util.KeyTypeToString(result.privateKeyType),
            args.output,
            args.iam_account))
