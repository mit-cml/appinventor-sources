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

"""Deletes a Google Cloud Function."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.functions import exceptions
from googlecloudsdk.api_lib.functions import operations
from googlecloudsdk.api_lib.functions import util
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.functions import flags
from googlecloudsdk.core import log
from googlecloudsdk.core.console import console_io


class Delete(base.DeleteCommand):
  """Delete a Google Cloud Function."""

  @staticmethod
  def Args(parser):
    """Register flags for this command."""
    flags.AddFunctionResourceArg(parser, 'to delete')
    parser.display_info.AddCacheUpdater(None)

  @util.CatchHTTPErrorRaiseHTTPException
  def Run(self, args):
    """This is what gets called when the user runs this command.

    Args:
      args: an argparse namespace. All the arguments that were provided to this
        command invocation.

    Returns:
      None

    Raises:
      FunctionsError: If the user doesn't confirm on prompt.
    """
    client = util.GetApiClientInstance()
    messages = client.MESSAGES_MODULE
    function_ref = args.CONCEPTS.name.Parse()
    function__url = function_ref.RelativeName()
    prompt_message = 'Resource [{0}] will be deleted.'.format(function__url)
    if not console_io.PromptContinue(message=prompt_message):
      raise exceptions.FunctionsError('Deletion aborted by user.')
    op = client.projects_locations_functions.Delete(
        messages.CloudfunctionsProjectsLocationsFunctionsDeleteRequest(
            name=function__url))
    operations.Wait(op, messages, client)
    log.DeletedResource(function__url)
