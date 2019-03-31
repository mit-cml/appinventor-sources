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

"""service-management operations describe command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.endpoints import common_flags


_ERROR = ('The `service-management operations describe` command has been '
          'replaced by `endpoints operations describe` and '
          '`services operations describe`.')


@base.Deprecate(is_removed=True, error=_ERROR)
class Describe(base.DescribeCommand):
  """Describes an operation resource for a given operation name."""

  @staticmethod
  def Args(parser):
    """Args is called by calliope to gather arguments for this command.

    Args:
      parser: An argparse parser that you can use to add arguments that go
          on the command line after this command. Positional arguments are
          allowed.
    """
    common_flags.operation_flag(suffix='to describe').AddToParser(parser)

    parser.display_info.AddFormat(
        ':(metadata.startTime.date(format="%Y-%m-%d %H:%M:%S %Z", tz=LOCAL)) '
        '[transforms] default')

    parser.add_argument(
        '--full',
        action='store_true',
        default=False,
        help=('Print the entire operation resource, which could be large. '
              'By default, a summary will be printed instead.'))

  def Run(self, args):
    """Stubs 'service-management operations describe'.

    Args:
      args: argparse.Namespace, The arguments that this command was invoked
          with.
    """
    pass
