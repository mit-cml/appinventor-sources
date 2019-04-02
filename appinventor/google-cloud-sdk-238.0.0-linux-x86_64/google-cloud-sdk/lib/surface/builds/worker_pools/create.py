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
"""Create worker pool command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.cloudbuild import cloudbuild_util
from googlecloudsdk.api_lib.cloudbuild import workerpool_config
from googlecloudsdk.api_lib.compute import utils as compute_utils
from googlecloudsdk.calliope import base
from googlecloudsdk.calliope import exceptions as c_exceptions
from googlecloudsdk.command_lib.cloudbuild import workerpool_flags
from googlecloudsdk.core import log
from googlecloudsdk.core import properties
from googlecloudsdk.core import resources


class Create(base.CreateCommand):
  """Create a worker pool for use by Google Cloud Build.

  Create a worker pool for use by Google Cloud Build.
  """

  _region_choice_to_enum = cloudbuild_util.GenerateRegionChoiceToEnum()

  @staticmethod
  def Args(parser):
    """Register flags for this command.

    Args:
      parser: An argparse.ArgumentParser-like object. It is mocked out in order
        to capture some information, but behaves like an ArgumentParser.
    """
    parser = workerpool_flags.AddWorkerpoolCreateArgs(parser)
    parser.display_info.AddFormat("""
          table(
            name,
            createTime.date('%Y-%m-%dT%H:%M:%S%Oz', undefined='-'),
            status
          )
        """)

  def Run(self, args):
    """This is what gets called when the user runs this command.

    Args:
      args: an argparse namespace. All the arguments that were provided to this
        command invocation.

    Returns:
      Some value that we want to have printed later.
    """

    client = cloudbuild_util.GetClientInstanceAlpha()
    messages = cloudbuild_util.GetMessagesModuleAlpha()

    parent = properties.VALUES.core.project.Get(required=True)

    # Get the workerpool proto from either the flags or the specified file.
    wp = messages.WorkerPool()
    if args.config_from_file is not None:
      wp = workerpool_config.LoadWorkerpoolConfigFromPath(
          args.config_from_file, messages)
    else:
      wp.name = args.WORKER_POOL
      if args.worker_count is not None:
        try:
          wp.workerCount = int(args.worker_count)
        except ValueError as e:
          raise c_exceptions.InvalidArgumentException('--worker-count', e)
      if args.regions is not None:
        for region_str in args.regions:
          region = Create._region_choice_to_enum[region_str]
          wp.regions.append(region)
      worker_config = messages.WorkerConfig()
      if args.worker_machine_type is not None:
        worker_config.machineType = args.worker_machine_type
      if args.worker_disk_size is not None:
        worker_config.diskSizeGb = compute_utils.BytesToGb(
            args.worker_disk_size)
      if any([
          args.worker_network_project is not None,
          args.worker_network_name is not None,
          args.worker_network_subnet is not None
      ]):
        if not all([
            args.worker_network_project is not None,
            args.worker_network_name is not None,
            args.worker_network_subnet is not None
        ]):
          raise c_exceptions.RequiredArgumentException(
              '--worker_network_*',
              'The flags --worker_network_project, --worker_network_name, and '
              '--worker_network_subnet must all be set if any of them are set.')
        # At this point all network flags are set, but possibly empty string.
        # The API handles default values.
        network = messages.Network()
        network.projectId = args.worker_network_project
        network.network = args.worker_network_name
        network.subnetwork = args.worker_network_subnet
        worker_config.network = network
      if args.worker_tag is not None:
        worker_config.tag = args.worker_tag
      wp.workerConfig = worker_config

    # Get the parent project ref
    parent_resource = resources.REGISTRY.Create(
        collection='cloudbuild.projects', projectId=parent)

    # Send the Create request
    created_wp = client.projects_workerPools.Create(
        messages.CloudbuildProjectsWorkerPoolsCreateRequest(
            workerPool=wp, parent=parent_resource.RelativeName()))

    # Get the workerpool ref
    wp_resource = resources.REGISTRY.Parse(
        None,
        collection='cloudbuild.projects.workerPools',
        api_version='v1alpha1',
        params={
            'projectsId': parent,
            'workerPoolsId': wp.name,
        })
    log.CreatedResource(wp_resource)

    return created_wp
