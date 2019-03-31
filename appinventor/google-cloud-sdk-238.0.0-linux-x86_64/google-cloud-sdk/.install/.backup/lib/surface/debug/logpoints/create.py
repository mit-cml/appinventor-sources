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

"""Create command for gcloud debug logpoints command group."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.debug import debug
from googlecloudsdk.calliope import base
from googlecloudsdk.core import log
from googlecloudsdk.core import properties


class Create(base.CreateCommand):
  """Create debug logpoints."""

  detailed_help = {
      'DESCRIPTION': """\
          This command adds a debug logpoint to a debug target. Logpoints add
          logging to your running service without changing your code or
          restarting your application. When you create a logpoint, the message
          you specify will be added to your logs whenever any instance of your
          service executes the specified line of code.

          The default lifetime of a logpoint is 24 hours from creation, and the
          output will go to the standard log for the programming language of the
          target (``java.logging'' for Java, ``logging'' for Python, etc.)
      """
  }

  @staticmethod
  def Args(parser):
    parser.add_argument(
        'location',
        help="""\
            The logpoint location. Locations are of
            the form FILE:LINE, where FILE can be simply the file name, or the
            file name preceded by enough path components to differentiate it
            from other files with the same name. It is an error to provide a
            file name that is not unique in the debug target.
        """)
    parser.add_argument(
        'log_format_string',
        help="""\
            A format string which will be logged every time the logpoint
            location is executed. If the string contains curly braces ('{' and
            '}'), any text within the curly braces will be interpreted as a
            run-time expression in the debug target's language, which will be
            evaluated when the logpoint is hit.

            The value of the expression will then replace the {} expression in
            the resulting log output. For example, if you specify the format
            string "a={a}, b={b}", and the logpoint is hit when local variable
            a is 1 and b is 2, the resulting log output would be "a=1, b=2".
        """)
    parser.add_argument(
        '--condition',
        help="""\
            A condition to restrict when the log output is generated. When the
            logpoint is hit, the condition will be evaluated, and the log output
            will be generated only if the condition is true.
        """)
    parser.add_argument(
        '--log-level', choices=['info', 'warning', 'error'],
        default='info',
        help='The logging level to use when producing the log message.')
    parser.add_argument(
        '--wait', default=10,
        help="""\
            The number of seconds to wait to ensure that no error is returned
            from a debugger agent when creating the logpoint. When a logpoint
            is created, there will be a delay before the agents see and apply
            the logpoint. Until at least one agent has attempted to
            enable the logpoint, it cannot be determined if the logpoint is
            valid.
        """)
    parser.display_info.AddFormat("""
          list(
            format("id: {0}", id),
            format("location: {0}", location),
            format("logLevel: {0}", logLevel),
            format("logMessageFormat: {0}", logMessageFormat),
            format("condition: {0}", condition),
            format("logViewUrl: {0}", logViewUrl),
            format("status: {0}", full_status())
          )
    """)

  def Run(self, args):
    """Run the create command."""
    project_id = properties.VALUES.core.project.Get(required=True)
    user_email = properties.VALUES.core.account.Get(required=True)
    debugger = debug.Debugger(project_id)
    debuggee = debugger.FindDebuggee(args.target)
    logpoint = debuggee.CreateLogpoint(
        location=args.location, log_level=args.log_level,
        log_format_string=args.log_format_string, condition=args.condition,
        user_email=user_email)
    # Wait a short time to see if the logpoint generates an error. Ideally,
    # we'd want to wait until we get a response that the logpoint was set
    # by at least one instance, but the API does not currently support that.
    final_logpoint = debuggee.WaitForBreakpointSet(logpoint.id, args.wait,
                                                   args.location)
    if args.location != final_logpoint.location:
      log.status.write(
          'The debugger adjusted the logpoint location to {0}'.format(
              final_logpoint.location))
    return final_logpoint or logpoint
