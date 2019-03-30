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

"""'logging sinks update' command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.logging import util
from googlecloudsdk.calliope import base
from googlecloudsdk.calliope import exceptions as calliope_exceptions
from googlecloudsdk.core import log
from googlecloudsdk.core.console import console_io


class Update(base.UpdateCommand):
  """Updates a sink.

  Changes the *[destination]* or *--log-filter* associated with a sink.
  The new destination must already exist and Stackdriver Logging must have
  permission to write to it.
  Log entries are exported to the new destination immediately.

  ## EXAMPLES

  To only update a sink filter, run:

    $ {command} my-sink --log-filter='severity>=ERROR'

  Detailed information about filters can be found at:
  [](https://cloud.google.com/logging/docs/view/advanced_filters)
  """

  @staticmethod
  def Args(parser):
    """Register flags for this command."""
    parser.add_argument(
        'sink_name', help='The name of the sink to update.')
    parser.add_argument(
        'destination', nargs='?',
        help=('A new destination for the sink. '
              'If omitted, the sink\'s existing destination is unchanged.'))
    parser.add_argument(
        '--log-filter', required=False,
        help=('A new filter expression for the sink. '
              'If omitted, the sink\'s existing filter (if any) is unchanged.'))
    util.AddNonProjectArgs(parser, 'Update a sink')

  def GetSink(self, parent, sink_ref):
    """Returns a sink specified by the arguments."""
    return util.GetClient().projects_sinks.Get(
        util.GetMessages().LoggingProjectsSinksGetRequest(
            sinkName=util.CreateResourceName(
                parent, 'sinks', sink_ref.sinksId)))

  def UpdateSink(self, parent, sink_data):
    """Updates a sink specified by the arguments."""
    messages = util.GetMessages()
    return util.GetClient().projects_sinks.Update(
        messages.LoggingProjectsSinksUpdateRequest(
            sinkName=util.CreateResourceName(
                parent, 'sinks', sink_data['name']),
            logSink=messages.LogSink(**sink_data),
            uniqueWriterIdentity=True))

  def Run(self, args):
    """This is what gets called when the user runs this command.

    Args:
      args: an argparse namespace. All the arguments that were provided to this
        command invocation.

    Returns:
      The updated sink with its new destination.
    """
    # One of the flags is required to update the sink.
    # log_filter can be an empty string, so check explicitly for None.
    if not args.destination and args.log_filter is None:
      raise calliope_exceptions.MinimumArgumentException(
          ['[destination]', '--log-filter'],
          'Please specify at least one property to update')

    sink_ref = util.GetSinkReference(args.sink_name, args)

    # Calling Update on a non-existing sink creates it.
    # We need to make sure it exists, otherwise we would create it.
    sink = self.GetSink(util.GetParentFromArgs(args), sink_ref)

    # Only update fields that were passed to the command.
    if args.destination:
      destination = args.destination
    else:
      destination = sink.destination

    if args.log_filter is not None:
      log_filter = args.log_filter
    else:
      log_filter = sink.filter

    sink_data = {
        'name': sink_ref.sinksId,
        'destination': destination,
        'filter': log_filter,
        'includeChildren': sink.includeChildren,
    }

    # Check for legacy configuration, and let users decide if they still want
    # to update the sink with new settings.
    if 'cloud-logs@' in sink.writerIdentity:
      console_io.PromptContinue(
          'This update will create a new writerIdentity (service account) for '
          'the sink. In order for the sink to continue working, grant that '
          'service account correct permission on the destination. The service '
          'account will be displayed after a successful update operation.',
          cancel_on_no=True, default=False)

    result = self.UpdateSink(util.GetParentFromArgs(args), sink_data)

    log.UpdatedResource(sink_ref)
    self._epilog_result_destination = result.destination
    self._epilog_writer_identity = result.writerIdentity
    return result

  def Epilog(self, unused_resources_were_displayed):
    util.PrintPermissionInstructions(self._epilog_result_destination,
                                     self._epilog_writer_identity)
