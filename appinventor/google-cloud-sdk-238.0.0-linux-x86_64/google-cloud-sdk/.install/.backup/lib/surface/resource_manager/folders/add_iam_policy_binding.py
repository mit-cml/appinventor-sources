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
"""Command to add IAM policy binding for a folder."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.resource_manager import folders
from googlecloudsdk.api_lib.util import http_retry
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.iam import iam_util
from googlecloudsdk.command_lib.resource_manager import completers
from googlecloudsdk.command_lib.resource_manager import flags

import six.moves.http_client


@base.ReleaseTracks(base.ReleaseTrack.ALPHA, base.ReleaseTrack.BETA)
class AddIamPolicyBinding(base.Command):
  """Add IAM policy binding for a folder.

  Adds a policy binding to the IAM policy of a folder,
  given a folder ID and the binding.
  """

  detailed_help = iam_util.GetDetailedHelpForAddIamPolicyBinding(
      'folder', '3589215982')

  @staticmethod
  def Args(parser):
    flags.FolderIdArg('to which you want to add a binding').AddToParser(parser)
    iam_util.AddArgsForAddIamPolicyBinding(
        parser,
        role_completer=completers.FoldersIamRolesCompleter)

  # Allow for retries due to ETag-based optimistic concurrency control
  @http_retry.RetryOnHttpStatus(six.moves.http_client.CONFLICT)
  def Run(self, args):
    messages = folders.FoldersMessages()
    policy = folders.GetIamPolicy(args.id)
    iam_util.AddBindingToIamPolicy(
        messages.Binding, policy, args.member, args.role)
    return folders.SetIamPolicy(args.id, policy)
