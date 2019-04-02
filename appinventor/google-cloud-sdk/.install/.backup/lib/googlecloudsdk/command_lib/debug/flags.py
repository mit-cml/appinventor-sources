# -*- coding: utf-8 -*- #
# Copyright 2016 Google Inc. All Rights Reserved.
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

"""Debug command flags."""

from __future__ import unicode_literals
SNAPSHOT_LIST_FORMAT = """
          table(
            short_status():label=STATUS,
            userEmail.if(all_users),
            location,
            condition,
            finalTime.if(include_inactive != 0):label=COMPLETED_TIME,
            id,
            consoleViewUrl:label=VIEW
          )
"""


def AddIdOptions(parser, entity, plural_entity, action_description):
  parser.add_argument(
      'ids', metavar='ID', nargs='*',
      help="""\
          Zero or more {entity} resource identifiers. The specified
          {plural_entity} will be {action_description}.
      """.format(entity=entity, plural_entity=plural_entity,
                 action_description=action_description))
  parser.add_argument(
      '--location', metavar='LOCATION-REGEXP', action='append',
      help="""\
          A regular expression to match against {entity}
          locations. All {plural_entity} matching this value will be
          {action_description}.  You may specify --location multiple times.

          EXAMPLE:

            {{command}} \\
                --location foo.py:[1-3] --location bar.py:4
      """.format(entity=entity, plural_entity=plural_entity,
                 action_description=action_description))


def AddTargetOption(parser):
  parser.add_argument(
      '--target', metavar='(ID|DESCRIPTION_REGEXP)',
      help="""\
          The debug target. It may be a target ID or name obtained from
          'debug targets list', or it may be a regular expression uniquely
          specifying a debuggee based on its description or name. For App
          Engine projects, if not specified, the default target is
          the most recent deployment of the default module and version.
      """)
