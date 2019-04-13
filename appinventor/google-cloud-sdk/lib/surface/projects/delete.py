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

"""Command to delete a project."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.cloudresourcemanager import projects_api
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.projects import flags
from googlecloudsdk.command_lib.projects import util as command_lib_util
from googlecloudsdk.command_lib.resource_manager import completers
from googlecloudsdk.core import log
from googlecloudsdk.core.console import console_io


class Delete(base.DeleteCommand):
  """Delete a project.

  Deletes the project with the given project ID.

  This command can fail for the following reasons:
  * The project specified does not exist.
  * The active account does not have Owner permissions for the given project.

  ## EXAMPLES

  The following command deletes the project with the ID `example-foo-bar-1`:

    $ {command} example-foo-bar-1

  ## SEE ALSO

  {see_also}
  """

  detailed_help = {'see_also': flags.CREATE_DELETE_IN_CONSOLE_SEE_ALSO}

  @staticmethod
  def Args(parser):
    flags.GetProjectFlag('delete').AddToParser(parser)
    parser.display_info.AddCacheUpdater(completers.ProjectCompleter)

  def Run(self, args):
    project_ref = command_lib_util.ParseProject(args.id)
    if not console_io.PromptContinue('Your project will be deleted.'):
      return None
    result = projects_api.Delete(project_ref)
    log.DeletedResource(project_ref)
    # Print this here rather than in Epilog because Epilog doesn't have access
    # to the deleted resource.
    # We can't be more specific than "limited period" because the API says
    # "at an unspecified time".
    log.status.Print(
        '\nYou can undo this operation for a limited period by running:\n'
        '  $ gcloud projects undelete {0}'.format(args.id))
    return result
