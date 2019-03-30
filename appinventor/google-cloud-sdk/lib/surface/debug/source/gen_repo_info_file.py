# -*- coding: utf-8 -*- #
# Copyright 2014 Google Inc. All Rights Reserved.
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

"""The gen_repo_info_file command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import json
import os

from googlecloudsdk.calliope import base
from googlecloudsdk.core import exceptions as core_exceptions
from googlecloudsdk.core import log
from googlecloudsdk.core.util import files
from googlecloudsdk.third_party.appengine.tools import context_util


class GenRepoInfoFile(base.Command):
  """Generates repository information files for the Stackdriver Debugger.

  The generated file contains opaque information representing which source
  revision the application was built at, and which repository this revision
  will be pushed to.
  """

  detailed_help = {
      'DESCRIPTION': """\
          This command generates a file named {name} containing information on
          the source revision and remote repository associated with the given
          source directory.
          """.format(name=context_util.CONTEXT_FILENAME),
      'EXAMPLES': """\
          To generate repository information files for your app,
          from your source directory run:

            $ {command}
          """,
  }

  @staticmethod
  def Args(parser):
    parser.add_argument(
        '--source-directory',
        default='.',
        help='The directory containing the source code for the build.')
    parser.add_argument(
        '--output-directory',
        default='.',
        help='The directory in which to create the source context file. ')

  def Run(self, args):
    try:
      contexts = context_util.CalculateExtendedSourceContexts(
          args.source_directory)
    except context_util.GenerateSourceContextError as e:
      # This is a usage error. Wrap it with core_exceptions.Error to report
      # it properly (i.e., as an error instead of a crash).
      raise core_exceptions.Error(e)

    # Create the source-context.json file.
    output_file = context_util.CONTEXT_FILENAME

    output_directory = args.output_directory
    output_file = os.path.join(output_directory, output_file)

    if context_util.HasPendingChanges(args.source_directory):
      log.warning(
          'There are uncommitted changes in directory [{0}].\n'
          'The generated source context files will not reflect the current '
          'state of your source code.\n'
          'For best results, commit all changes and re-run this command.\n'
          .format(args.source_directory))
    best_context = context_util.BestSourceContext(contexts)
    files.MakeDir(output_directory)
    files.WriteFileContents(
        output_file, json.dumps(best_context, indent=2, sort_keys=True))

