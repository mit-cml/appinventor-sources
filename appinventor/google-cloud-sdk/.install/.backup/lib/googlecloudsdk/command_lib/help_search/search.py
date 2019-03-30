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

"""gcloud search-help command resources."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope import cli_tree
from googlecloudsdk.command_lib.help_search import lookup
from googlecloudsdk.command_lib.help_search import rater
from googlecloudsdk.command_lib.help_search import search_util

from six.moves import zip


def RunSearch(terms, cli):
  """Runs search-help by opening and reading help table, finding commands.

  Args:
    terms: [str], list of strings that must be found in the command.
    cli: the Calliope CLI object

  Returns:
    a list of json objects representing gcloud commands.
  """
  parent = cli_tree.Load(cli=cli, one_time_use_ok=True)
  searcher = Searcher(parent, terms)
  return searcher.Search()


class Searcher(object):
  """Class to run help search."""

  def __init__(self, parent, terms):
    self.parent = parent
    self.terms = terms
    self._rater = rater.CumulativeRater()

  def Search(self):
    """Run a search and return a list of processed matching commands.

    The search walks the command tree and returns a list of matching commands.
    The commands are modified so that child commands in command groups are
    replaced with just a list of their names, and include summaries and
    "relevance" ratings as well.

    Commands match if at least one of the searcher's terms is found in the
    command.

    Returns:
      [dict], a list of the matching commands in json form.
    """

    def _WalkTree(current_parent, found_commands):
      """Recursively walks command tree, checking for matches.

      If a command matches it is postprocessed and added to found_commands.

      Args:
        current_parent: dict, a json representation of a CLI command.
        found_commands: [dict], a list of matching commands.

      Returns:
        [dict], a list of commands that have matched so far.
      """
      result = self._PossiblyGetResult(current_parent)
      if result:
        found_commands.append(result)
      for child_command in current_parent.get(lookup.COMMANDS, {}).values():
        found_commands = _WalkTree(child_command, found_commands)
      return found_commands

    found_commands = _WalkTree(self.parent, [])
    self._rater.RateAll()
    return found_commands

  def _PossiblyGetResult(self, command):
    """Helper function to determine whether a command contains all terms.

    Returns a copy of the command or command group with modifications to the
    'commands' field and an added 'summary' field if the command matches
    the searcher's search terms.

    Args:
      command: dict, a json representation of a command.

    Returns:
      a modified copy of the command if the command is a result, otherwise None.
    """
    locations = [search_util.LocateTerm(command, term) for term in self.terms]
    if any(locations):
      results = search_util.CommandSearchResults(
          dict(zip(self.terms, locations)))
      new_command = search_util.ProcessResult(command, results)
      self._rater.AddFoundCommand(new_command, results)
      return new_command



