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

"""Command to remove a principal from a service's access policy."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.endpoints import common_flags
from googlecloudsdk.command_lib.iam import iam_util


_ERROR = ('The `service-management remove-iam-policy-binding` command has '
          'been replaced by `endpoints services remove-iam-policy-binding`.')


@base.Deprecate(is_removed=True, error=_ERROR)
class RemoveIamPolicyBinding(base.Command):
  """Removes an IAM policy binding from a service's access policy."""

  @staticmethod
  def Args(parser):
    """Args is called by calliope to gather arguments for this command.

    Args:
      parser: An argparse parser that you can use to add arguments that go
          on the command line after this command. Positional arguments are
          allowed.
    """
    service_flag = common_flags.producer_service_flag(
        suffix='from which the member is to be removed')
    service_flag.AddToParser(parser)

    iam_util.AddArgsForRemoveIamPolicyBinding(parser)

  def Run(self, args):
    """Stub for 'service-management remove-iam-policy-binding'.

    Args:
      args: argparse.Namespace, The arguments that this command was invoked
          with.
    """
    pass
