# -*- coding: utf-8 -*- #
# Copyright 2018 Google Inc. All Rights Reserved.
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
"""bigtable app_profiles update command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from apitools.base.py.exceptions import HttpError
from googlecloudsdk.api_lib.bigtable import app_profiles
from googlecloudsdk.api_lib.bigtable import util
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.bigtable import arguments
from googlecloudsdk.core import log


class UpdateAppProfile(base.CreateCommand):
  """Update a Bigtable app_profile."""

  @staticmethod
  def Args(parser):
    arguments.AddAppProfileResourceArg(parser, 'to update')
    (arguments.ArgAdder(parser).AddDescription('app-profile', required=False)
     .AddAppProfileRouting(required=False).AddForce('update').AddAsync())

  def Run(self, args):
    """This is what gets called when the user runs this command.

    Args:
      args: an argparse namespace. All the arguments that were provided to this
        command invocation.

    Raises:
      exceptions.ConflictingArgumentsException: If the user provides
        --transactional-writes and --route-any.

    Returns:
      Created resource.
    """
    app_profile_ref = args.CONCEPTS.app_profile.Parse()
    try:
      result = app_profiles.Update(
          app_profile_ref,
          cluster=args.route_to,
          description=args.description,
          multi_cluster=args.route_any,
          transactional_writes=args.transactional_writes,
          force=args.force)
    except HttpError as e:
      util.FormatErrorMessages(e)
    else:
      operation_ref = util.GetOperationRef(result)

      if args.async:
        log.UpdatedResource(
            operation_ref,
            kind='bigtable app-profile {0}'.format(app_profile_ref.Name()),
            is_async=True)
        return result

      return util.AwaitAppProfile(
          operation_ref,
          'Updating bigtable app-profile {0}'.format(app_profile_ref.Name()))
