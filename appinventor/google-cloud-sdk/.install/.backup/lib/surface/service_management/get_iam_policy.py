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

"""Command to describe the access policy for a service."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.endpoints import common_flags


_ERROR = ('The `service-management get-iam-policy` command has been '
          'replaced by `endpoints services get-iam-policy`.')


@base.Deprecate(is_removed=True, error=_ERROR)
class GetIamPolicy(base.ListCommand):
  """Describes the IAM policy for a service.

  Gets the IAM policy for a produced service, given the service name.

  ## EXAMPLES

  To print the IAM policy for a service named `my-service`, run:

    $ {command} my-service
  """

  @staticmethod
  def Args(parser):
    """Args is called by calliope to gather arguments for this command.

    Args:
      parser: An argparse parser that you can use to add arguments that go
          on the command line after this command. Positional arguments are
          allowed.
    """
    service_flag = common_flags.producer_service_flag(
        suffix='whose IAM policy is to be described')
    service_flag.AddToParser(parser)
    base.URI_FLAG.RemoveFromParser(parser)

  def Run(self, args):
    """Stubs 'service-management get-iam-policy'.

    Args:
      args: argparse.Namespace, The arguments that this command was invoked
          with.
    """
    pass
