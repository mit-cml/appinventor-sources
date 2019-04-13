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
"""Utilities for calling the Composer Environments API."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from collections import OrderedDict
from googlecloudsdk.api_lib.composer import util as api_util
from googlecloudsdk.calliope import base


# TODO(b/111385813): Refactor utils into a class
def GetService(release_track=base.ReleaseTrack.GA):
  return api_util.GetClientInstance(
      release_track).projects_locations_environments


def Create(environment_ref,
           node_count=None,
           labels=None,
           location=None,
           machine_type=None,
           network=None,
           subnetwork=None,
           env_variables=None,
           airflow_config_overrides=None,
           service_account=None,
           oauth_scopes=None,
           tags=None,
           disk_size_gb=None,
           python_version=None,
           image_version=None,
           airflow_executor_type=None,
           release_track=base.ReleaseTrack.GA):
  """Calls the Composer Environments.Create method.

  Args:
    environment_ref: Resource, the Composer environment resource to
        create.
    node_count: int or None, the number of VMs to create for the environment
    labels: dict(str->str), a dict of user-provided resource labels to apply
        to the environment and its downstream resources
    location: str or None, the Compute Engine zone in which to create the
        environment specified as relative resource name.
    machine_type: str or None, the Compute Engine machine type of the VMs to
        create specified as relative resource name.
    network: str or None, the Compute Engine network to which to connect the
        environment specified as relative resource name.
    subnetwork: str or None, the Compute Engine subnetwork to which to
        connect the environment specified as relative resource name.
    env_variables: dict(str->str), a dict of user-provided environment
        variables to provide to the Airflow scheduler, worker, and webserver
        processes.
    airflow_config_overrides: dict(str->str), a dict of user-provided Airflow
        configuration overrides.
    service_account: str or None, the user-provided service account
    oauth_scopes: [str], the user-provided OAuth scopes
    tags: [str], the user-provided networking tags
    disk_size_gb: int, the disk size of node VMs, in GB
    python_version: str or None, major python version to use within created
        environment.
    image_version: str or None, the desire image for created environment in the
        format of 'composer-(version)-airflow-(version)'
    airflow_executor_type: str or None, the airflow executor type to run task
        instances.
    release_track: base.ReleaseTrack, the release track of command. Will dictate
        which Composer client library will be used.

  Returns:
    Operation: the operation corresponding to the creation of the environment
  """
  messages = api_util.GetMessagesModule(release_track=release_track)
  config = messages.EnvironmentConfig()
  is_config_empty = True
  if node_count:
    is_config_empty = False
    config.nodeCount = node_count
  if (location or machine_type or network or subnetwork or service_account or
      oauth_scopes or tags or disk_size_gb):
    is_config_empty = False
    config.nodeConfig = messages.NodeConfig(
        location=location,
        machineType=machine_type,
        network=network,
        subnetwork=subnetwork,
        serviceAccount=service_account,
        diskSizeGb=disk_size_gb)
    if oauth_scopes:
      config.nodeConfig.oauthScopes = list(
          OrderedDict((s.strip(), None) for s in oauth_scopes).keys())
    if tags:
      config.nodeConfig.tags = list(
          OrderedDict((t.strip(), None) for t in tags).keys())
  if (image_version or env_variables or airflow_config_overrides or
      python_version or airflow_executor_type):
    is_config_empty = False
    config.softwareConfig = messages.SoftwareConfig()
    if image_version:
      config.softwareConfig.imageVersion = image_version
    if env_variables:
      config.softwareConfig.envVariables = api_util.DictToMessage(
          env_variables, messages.SoftwareConfig.EnvVariablesValue)
    if airflow_config_overrides:
      config.softwareConfig.airflowConfigOverrides = api_util.DictToMessage(
          airflow_config_overrides,
          messages.SoftwareConfig.AirflowConfigOverridesValue)
    if python_version:
      config.softwareConfig.pythonVersion = python_version
    if airflow_executor_type:
      config.softwareConfig.airflowExecutorType = ConvertToTypeEnum(
          messages.SoftwareConfig.AirflowExecutorTypeValueValuesEnum,
          airflow_executor_type)
  environment = messages.Environment(name=environment_ref.RelativeName())
  if not is_config_empty:
    environment.config = config
  if labels:
    environment.labels = api_util.DictToMessage(
        labels, messages.Environment.LabelsValue)

  return GetService(release_track=release_track).Create(
      api_util.GetMessagesModule(release_track=release_track)
      .ComposerProjectsLocationsEnvironmentsCreateRequest(
          environment=environment,
          parent=environment_ref.Parent().RelativeName()))


def ConvertToTypeEnum(type_enum, airflow_executor_type):
  """Converts airflow executor type string to enum.

  Args:
    type_enum: AirflowExecutorTypeValueValuesEnum, executor type enum value.
    airflow_executor_type: string, executor type string value.

  Returns:
    AirflowExecutorTypeValueValuesEnum: the executor type enum value.
  """
  return type_enum(airflow_executor_type)


def Delete(environment_ref, release_track=base.ReleaseTrack.GA):
  """Calls the Composer Environments.Delete method.

  Args:
    environment_ref: Resource, the Composer environment resource to
        delete.
    release_track: base.ReleaseTrack, the release track of command. Will dictate
        which Composer client library will be used.

  Returns:
    Operation: the operation corresponding to the deletion of the environment
  """
  return GetService(release_track=release_track).Delete(
      api_util.GetMessagesModule(release_track=release_track)
      .ComposerProjectsLocationsEnvironmentsDeleteRequest(
          name=environment_ref.RelativeName()))


def Get(environment_ref, release_track=base.ReleaseTrack.GA):
  """Calls the Composer Environments.Get method.

  Args:
    environment_ref: Resource, the Composer environment resource to
        retrieve.
    release_track: base.ReleaseTrack, the release track of command. Will dictate
        which Composer client library will be used.

  Returns:
    Environment: the requested environment
  """
  return GetService(release_track=release_track).Get(
      api_util.GetMessagesModule(release_track=release_track)
      .ComposerProjectsLocationsEnvironmentsGetRequest(
          name=environment_ref.RelativeName()))


def List(location_refs,
         page_size,
         limit=None,
         release_track=base.ReleaseTrack.GA):
  """Lists Composer Environments across all locations.

  Uses a hardcoded list of locations, as there is no way to dynamically
  discover the list of supported locations. Support for new locations
  will be aligned with Cloud SDK releases.

  Args:
    location_refs: [core.resources.Resource], a list of resource reference to
        locations in which to list environments.
    page_size: An integer specifying the maximum number of resources to be
      returned in a single list call.
    limit: An integer specifying the maximum number of environments to list.
        None if all available environments should be returned.
    release_track: base.ReleaseTrack, the release track of command. Will dictate
        which Composer client library will be used.

  Returns:
    list: a generator over Environments in the locations in `location_refs`
  """
  return api_util.AggregateListResults(
      api_util.GetMessagesModule(release_track=release_track)
      .ComposerProjectsLocationsEnvironmentsListRequest,
      GetService(release_track=release_track),
      location_refs,
      'environments',
      page_size,
      limit=limit)


def Patch(environment_ref,
          environment_patch,
          update_mask,
          release_track=base.ReleaseTrack.GA):
  """Calls the Composer Environments.Update method.

  Args:
    environment_ref: Resource, the Composer environment resource to update.
    environment_patch: The Environment message specifying the patch associated
      with the update_mask.
    update_mask: A field mask defining the patch.
    release_track: base.ReleaseTrack, the release track of command. Will dictate
        which Composer client library will be used.
  Returns:
    Operation: the operation corresponding to the environment update
  """
  return GetService(release_track=release_track).Patch(
      api_util.GetMessagesModule(release_track=release_track)
      .ComposerProjectsLocationsEnvironmentsPatchRequest(
          name=environment_ref.RelativeName(),
          environment=environment_patch,
          updateMask=update_mask))
