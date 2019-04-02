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

"""Lists the installed gcloud interactive CLI trees."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import os
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.flag_coverage import generate
from googlecloudsdk.core import log
from googlecloudsdk.core.util import files


class Update(base.Command):
  """Generates the flag coverage tree.

  This command generates and writes the flag coverage tree to a file or standard
  out depending on whether or not a file path is specified. This tree is created
  specifically for the purpose of determining the flag coverage of the surfaces
  in gcloud.
  """

  @staticmethod
  def Args(parser):
    parser.add_argument(
        '--file-path',
        required=True,
        help='The file path to write the output tree into')
    parser.add_argument(
        'restrict',
        metavar='COMMAND/GROUP',
        nargs='*',
        help=('Restrict the listing to these dotted command paths. '
              'For example: gcloud.alpha gcloud.beta.test'))

  def Run(self, args):
    with files.FileWriter(os.path.expanduser(args.file_path)) as stream:
      generate.OutputCoverageTree(self._cli_power_users_only,
                                  out=stream,
                                  restrict=args.restrict)
    log.status.Print('flag coverage CLI tree is up to date')
