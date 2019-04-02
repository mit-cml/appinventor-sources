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

"""A command that searches the gcloud group and command tree."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.help_search import search
from googlecloudsdk.command_lib.help_search import search_util


_DEPRECATION_WARNING = (
    '`search-help` is deprecated. Please use `gcloud help` instead.')


@base.Deprecate(is_removed=False, warning=_DEPRECATION_WARNING)
@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class HelpSearch(base.ListCommand):
  """Search the help text of gcloud commands.

  Search the help text of gcloud commands for a term of interest. Prints the
  command name and a summary of the help text for any general release command
  whose help text contains the searched term.

  By default, results are sorted from most to least relevant, using a localized
  rating that is based on several heuristics and that may change in future
  runs of this command.
  """

  @staticmethod
  def Args(parser):
    parser.display_info.AddTransforms(search_util.GetTransforms())
    parser.display_info.AddFormat("""
        table[all-box](
            commandpath():label='COMMAND',
            summary():wrap)
        """)
    parser.add_argument('term',
                        help=('Term to search for.'))
    base.URI_FLAG.RemoveFromParser(parser)
    base.LIMIT_FLAG.SetDefault(parser, 5)
    base.SORT_BY_FLAG.SetDefault(parser, '~relevance')

  def Run(self, args):
    return search.RunSearch([args.term], self._cli_power_users_only)
