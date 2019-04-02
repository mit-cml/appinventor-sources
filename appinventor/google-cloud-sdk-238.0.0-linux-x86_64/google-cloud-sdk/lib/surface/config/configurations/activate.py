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

"""Command to activate named configuration."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.config import completers
from googlecloudsdk.core import log
from googlecloudsdk.core.configurations import named_configs


class Activate(base.SilentCommand):
  """Activates an existing named configuration."""

  detailed_help = {
      'DESCRIPTION': """\
          {description}

          See `gcloud topic configurations` for an overview of named
          configurations.
          """,
      'EXAMPLES': """\
          To activate an existing named configuration, run:

            $ {command} my_config

          To list all properties in the activated configuration, run:

            $ gcloud config list --all
          """,
  }

  @staticmethod
  def Args(parser):
    """Adds args for this command."""
    parser.add_argument(
        'configuration_name',
        completer=completers.NamedConfigCompleter,
        help='Name of the configuration to activate')

  def Run(self, args):
    named_configs.ConfigurationStore.ActivateConfig(args.configuration_name)
    log.status.write('Activated [{0}].\n'.format(args.configuration_name))
    return args.configuration_name

