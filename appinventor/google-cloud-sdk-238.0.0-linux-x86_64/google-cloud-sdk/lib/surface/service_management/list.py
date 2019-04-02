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

"""service-management list command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.services import services_util
from googlecloudsdk.calliope import base
from googlecloudsdk.core import resources


def _GetUriFromResource(resource):
  """Returns the URI for resource."""
  return resources.REGISTRY.ParseRelativeName(
      'services/' + resource.serviceName,
      collection=services_util.SERVICES_COLLECTION).SelfLink()


_ERROR = ('The `service-management list` command has been replaced by '
          '`endpoints services list` and `services list`.')


@base.Deprecate(is_removed=True, error=_ERROR)
class List(base.ListCommand):
  """List services for a project.

  This command lists the services that are produced, enabled, or
  available to be enabled by a project. You can choose the mode in
  which the command will list services by using exactly one of the
  `--produced`, `--enabled`, or `--available` flags.
  `--enabled` is the default.

  ## EXAMPLES

  To list the services the current project produces, run:

    $ {command} --produced

  To list the services the current project has enabled for consumption, run:

    $ {command} --enabled

  To list the services the current project can enable for consumption, run:

    $ {command} --available
  """

  _DEFAULT_PAGE_SIZE = 2000

  @staticmethod
  def Args(parser):
    """Args is called by calliope to gather arguments for this command.

    Args:
      parser: An argparse parser that you can use to add arguments that go
          on the command line after this command. Positional arguments are
          allowed.
    """
    mode_group = parser.add_mutually_exclusive_group(required=False)

    mode_group.add_argument('--enabled',
                            action='store_true',
                            help=('(DEFAULT) Return the services which the '
                                  'project has enabled. Or use one of '
                                  '--produced or --available.'))

    mode_group.add_argument('--produced',
                            action='store_true',
                            help=('Return the services that the project '
                                  'has produced. Or use one of --enabled or '
                                  '--available.'))

    mode_group.add_argument('--available',
                            action='store_true',
                            help=('Return the services available to the '
                                  'project to enable. This list will '
                                  'include any services that the project '
                                  'has already enabled. Or use one of '
                                  '--enabled or --produced.'))

    parser.display_info.AddFormat("""
          table(
            serviceName:label=NAME,
            serviceConfig.title
          )
        """)
    parser.display_info.AddUriFunc(_GetUriFromResource)

  def Run(self, args):
    """Stubs 'service-management list'.

    Args:
      args: argparse.Namespace, The arguments that this command was invoked
          with.
    """
    pass
