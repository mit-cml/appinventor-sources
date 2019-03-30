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
"""Command that updates scalar properties of an environment."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.composer import environment_patch_util as patch_util
from googlecloudsdk.command_lib.composer import flags
from googlecloudsdk.command_lib.composer import resource_args
from googlecloudsdk.command_lib.composer import util as command_util
from googlecloudsdk.command_lib.util.args import labels_util


class Update(base.Command):
  """Update properties of a Cloud Composer environment."""

  @staticmethod
  def Args(parser):
    resource_args.AddEnvironmentResourceArg(parser, 'to update')
    base.ASYNC_FLAG.AddToParser(parser)
    update_type_group = parser.add_mutually_exclusive_group(
        required=True, help='The update type.')
    flags.AddNodeCountUpdateFlagToGroup(update_type_group)
    flags.AddPypiUpdateFlagsToGroup(update_type_group)
    flags.AddEnvVariableUpdateFlagsToGroup(update_type_group)
    labels_update_group = update_type_group.add_argument_group()
    labels_util.AddUpdateLabelsFlags(labels_update_group)
    flags.AddAirflowConfigUpdateFlagsToGroup(update_type_group)

  def Run(self, args):
    env_ref = args.CONCEPTS.environment.Parse()
    field_mask, patch = patch_util.ConstructPatch(
        env_ref=env_ref,
        node_count=args.node_count,
        update_pypi_packages_from_file=args.update_pypi_packages_from_file,
        clear_pypi_packages=args.clear_pypi_packages,
        remove_pypi_packages=args.remove_pypi_packages,
        update_pypi_packages=dict(
            command_util.SplitRequirementSpecifier(r)
            for r in args.update_pypi_package),
        clear_labels=args.clear_labels,
        remove_labels=args.remove_labels,
        update_labels=args.update_labels,
        clear_airflow_configs=args.clear_airflow_configs,
        remove_airflow_configs=args.remove_airflow_configs,
        update_airflow_configs=args.update_airflow_configs,
        clear_env_variables=args.clear_env_variables,
        remove_env_variables=args.remove_env_variables,
        update_env_variables=args.update_env_variables,
        release_track=self.ReleaseTrack())
    return patch_util.Patch(
        env_ref,
        field_mask,
        patch,
        args.async,
        release_track=self.ReleaseTrack())
