# -*- coding: utf-8 -*- #
# Copyright 2018 Google Inc. All Rights Reserved.
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
"""Add Attestor public key command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.container.binauthz import apis
from googlecloudsdk.api_lib.container.binauthz import attestors
from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.container.binauthz import flags


class Add(base.Command):
  """Add a public key to an Attestor."""

  @classmethod
  def Args(cls, parser):
    flags.AddConcepts(
        parser,
        flags.GetAttestorPresentationSpec(
            required=True,
            positional=False,
            group_help=(
                'The attestor to which the public key should be added.'),
        ),
    )
    parser.add_argument(
        '--public-key-file',
        type=arg_parsers.BufferedFileInput(),
        help='The path to the file containing the '
        'ASCII-armored PGP public key to add.',
        required=True)
    parser.add_argument(
        '--comment', help='The comment describing the public key.')

  def Run(self, args):
    api_version = apis.GetApiVersion(self.ReleaseTrack())
    attestors_client = attestors.Client(api_version)

    attestor_ref = args.CONCEPTS.attestor.Parse()

    # TODO(b/71700164): Validate the contents of the public key file.

    return attestors_client.AddKey(attestor_ref, args.public_key_file,
                                   args.comment)
