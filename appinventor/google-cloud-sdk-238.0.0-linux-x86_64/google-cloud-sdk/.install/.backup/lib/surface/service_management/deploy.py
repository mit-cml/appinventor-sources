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

"""service-management deploy command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope import base


_ERROR = ('The `service-management deploy` command has been replaced by '
          '`endpoints services deploy`.')


def _CommonArgs(parser):
  parser.add_argument(
      'service_config_file',
      nargs='+',
      help=('The service configuration file (or files) containing the API '
            'specification to upload. Proto Descriptors, Open API (Swagger) '
            'specifications, and Google Service Configuration files in JSON '
            'and YAML formats are acceptable.'))
  base.ASYNC_FLAG.AddToParser(parser)


@base.ReleaseTracks(base.ReleaseTrack.ALPHA, base.ReleaseTrack.BETA,
                    base.ReleaseTrack.GA)
@base.Deprecate(is_removed=True, error=_ERROR)
class Deploy(base.Command):
  """Deploys a service configuration for the given service name."""

  @staticmethod
  def Args(parser):
    """Args is called by calliope to gather arguments for this command.

    Args:
      parser: An argparse parser that you can use to add arguments that go
          on the command line after this command. Positional arguments are
          allowed.
    """
    _CommonArgs(parser)
    parser.add_argument(
        '--validate-only',
        action='store_true',
        help='If included, the command will only validate the service '
             'configuration(s). No configuration(s) will be persisted.')

    parser.add_argument(
        '--force',
        '-f',
        action='store_true',
        default=False,
        help='Force the deployment even if any hazardous changes to the '
             'service configuration are detected.')

  def Run(self, args):
    """Stub for 'service-management deploy'.

    Args:
      args: argparse.Namespace, The arguments that this command was invoked
          with.
    """
    pass
