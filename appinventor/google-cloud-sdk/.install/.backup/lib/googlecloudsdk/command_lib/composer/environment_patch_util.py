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
"""Common utility functions for Composer environment patch commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.composer import environments_util as environments_api_util
from googlecloudsdk.api_lib.composer import operations_util as operations_api_util
from googlecloudsdk.api_lib.composer import util as api_util
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.composer import util as command_util
from googlecloudsdk.core import log
import six


def Patch(env_resource,
          field_mask,
          patch,
          is_async,
          release_track=base.ReleaseTrack.GA):
  """Patches an Environment, optionally waiting for the operation to complete.

  This function is intended to perform the common work of an Environment
  patching command's Run method. That is, calling the patch API method and
  waiting for the result or immediately returning the Operation.

  Args:
    env_resource: googlecloudsdk.core.resources.Resource, Resource representing
        the Environment to be patched
    field_mask: str, a field mask string containing comma-separated paths to be
        patched
    patch: Environment, a patch Environment containing updated values to apply
    is_async: bool, whether or not to perform the patch asynchronously
    release_track: base.ReleaseTrack, the release track of command. Will dictate
        which Composer client library will be used.

  Returns:
    an Operation corresponding to the Patch call if `is_async` is True;
    otherwise None is returned after the operation is complete

  Raises:
    command_util.Error: if `is_async` is False and the operation encounters
    an error
  """
  operation = environments_api_util.Patch(
      env_resource, patch, field_mask, release_track=release_track)
  details = 'with operation [{0}]'.format(operation.name)
  if is_async:
    log.UpdatedResource(
        env_resource.RelativeName(),
        kind='environment',
        is_async=True,
        details=details)
    return operation

  try:
    operations_api_util.WaitForOperation(
        operation,
        'Waiting for [{}] to be updated with [{}]'.format(
            env_resource.RelativeName(), operation.name),
        release_track=release_track)
  except command_util.Error as e:
    raise command_util.Error('Error updating [{}]: {}'.format(
        env_resource.RelativeName(), six.text_type(e)))


def ConstructPatch(env_ref=None,
                   node_count=None,
                   update_pypi_packages_from_file=None,
                   clear_pypi_packages=None,
                   remove_pypi_packages=None,
                   update_pypi_packages=None,
                   clear_labels=None,
                   remove_labels=None,
                   update_labels=None,
                   clear_airflow_configs=None,
                   remove_airflow_configs=None,
                   update_airflow_configs=None,
                   clear_env_variables=None,
                   remove_env_variables=None,
                   update_env_variables=None,
                   release_track=base.ReleaseTrack.GA):
  """Constructs an environment patch.

  Args:
    env_ref: resource argument, Environment resource argument for environment
      being updated.
    node_count: int, the desired node count
    update_pypi_packages_from_file: str, path to local requirements file
      containing desired pypi dependencies.
    clear_pypi_packages: bool, whether to uninstall all PyPI packages.
    remove_pypi_packages: iterable(string), Iterable of PyPI packages to
      uninstall.
    update_pypi_packages: {string: string}, dict mapping PyPI package name to
      extras and version specifier.
    clear_labels: bool, whether to clear the labels dictionary.
    remove_labels: iterable(string), Iterable of label names to remove.
    update_labels: {string: string}, dict of label names and values to set.
    clear_airflow_configs: bool, whether to clear the Airflow configs
      dictionary.
    remove_airflow_configs: iterable(string), Iterable of Airflow config
      property names to remove.
    update_airflow_configs: {string: string}, dict of Airflow config property
      names and values to set.
    clear_env_variables: bool, whether to clear the environment variables
      dictionary.
    remove_env_variables: iterable(string), Iterable of environment variables
      to remove.
    update_env_variables: {string: string}, dict of environment variable
      names and values to set.
    release_track: base.ReleaseTrack, the release track of command. Will dictate
        which Composer client library will be used.

  Returns:
    (str, Environment), the field mask and environment to use for update.

  Raises:
    command_util.Error: if no update type is specified
  """
  if node_count:
    return _ConstructNodeCountPatch(node_count, release_track=release_track)
  if update_pypi_packages_from_file:
    return _ConstructPyPiPackagesPatch(
        True, [],
        command_util.ParseRequirementsFile(update_pypi_packages_from_file),
        release_track=release_track)
  if clear_pypi_packages or remove_pypi_packages or update_pypi_packages:
    return _ConstructPyPiPackagesPatch(
        clear_pypi_packages,
        remove_pypi_packages,
        update_pypi_packages,
        release_track=release_track)
  if clear_labels or remove_labels or update_labels:
    return _ConstructLabelsPatch(
        clear_labels, remove_labels, update_labels, release_track=release_track)
  if (clear_airflow_configs or remove_airflow_configs or
      update_airflow_configs):
    return _ConstructAirflowConfigsPatch(
        clear_airflow_configs,
        remove_airflow_configs,
        update_airflow_configs,
        release_track=release_track)
  if clear_env_variables or remove_env_variables or update_env_variables:
    return _ConstructEnvVariablesPatch(
        env_ref,
        clear_env_variables,
        remove_env_variables,
        update_env_variables,
        release_track=release_track)
  raise command_util.Error(
      'Cannot update Environment with no update type specified.')


def _ConstructNodeCountPatch(node_count, release_track=base.ReleaseTrack.GA):
  """Constructs an environment patch for node count.

  Args:
    node_count: int, the desired node count
    release_track: base.ReleaseTrack, the release track of command. Will dictate
        which Composer client library will be used.

  Returns:
    (str, Environment), the field mask and environment to use for update.
  """
  messages = api_util.GetMessagesModule(release_track=release_track)
  config = messages.EnvironmentConfig(nodeCount=node_count)
  return 'config.node_count', messages.Environment(config=config)


def _ConstructPyPiPackagesPatch(clear_pypi_packages,
                                remove_pypi_packages,
                                update_pypi_packages,
                                release_track=base.ReleaseTrack.GA):
  """Constructs an environment patch for partially updating PyPI packages.

  Args:
    clear_pypi_packages: bool, whether to clear the PyPI packages dictionary.
    remove_pypi_packages: iterable(string), Iterable of PyPI package names to
      remove.
    update_pypi_packages: {string: string}, dict mapping PyPI package name
      to optional extras and version specifier.
    release_track: base.ReleaseTrack, the release track of command. Will dictate
        which Composer client library will be used.

  Returns:
    (str, Environment), the field mask and environment to use for update.
  """
  messages = api_util.GetMessagesModule(release_track=release_track)
  env_cls = messages.Environment
  pypi_packages_cls = (messages.SoftwareConfig.PypiPackagesValue)
  entry_cls = pypi_packages_cls.AdditionalProperty

  def _BuildEnv(entries):
    software_config = messages.SoftwareConfig(
        pypiPackages=pypi_packages_cls(additionalProperties=entries))
    config = messages.EnvironmentConfig(softwareConfig=software_config)
    return env_cls(config=config)

  return command_util.BuildPartialUpdate(
      clear_pypi_packages, remove_pypi_packages, update_pypi_packages,
      'config.software_config.pypi_packages', entry_cls, _BuildEnv)


def _ConstructLabelsPatch(clear_labels,
                          remove_labels,
                          update_labels,
                          release_track=base.ReleaseTrack.GA):
  """Constructs an environment patch for updating labels.

  Args:
    clear_labels: bool, whether to clear the labels dictionary.
    remove_labels: iterable(string), Iterable of label names to remove.
    update_labels: {string: string}, dict of label names and values to set.
    release_track: base.ReleaseTrack, the release track of command. Will dictate
        which Composer client library will be used.

  Returns:
    (str, Environment), the field mask and environment to use for update.
  """
  messages = api_util.GetMessagesModule(release_track=release_track)
  env_cls = messages.Environment
  entry_cls = env_cls.LabelsValue.AdditionalProperty

  def _BuildEnv(entries):
    return env_cls(labels=env_cls.LabelsValue(additionalProperties=entries))

  return command_util.BuildPartialUpdate(clear_labels, remove_labels,
                                         update_labels, 'labels', entry_cls,
                                         _BuildEnv)


def _ConstructAirflowConfigsPatch(clear_airflow_configs,
                                  remove_airflow_configs,
                                  update_airflow_configs,
                                  release_track=base.ReleaseTrack.GA):
  """Constructs an environment patch for updating Airflow configs.

  Args:
    clear_airflow_configs: bool, whether to clear the Airflow configs
      dictionary.
    remove_airflow_configs: iterable(string), Iterable of Airflow config
      property names to remove.
    update_airflow_configs: {string: string}, dict of Airflow config property
      names and values to set.
    release_track: base.ReleaseTrack, the release track of command. Will dictate
        which Composer client library will be used.

  Returns:
    (str, Environment), the field mask and environment to use for update.
  """
  messages = api_util.GetMessagesModule(release_track=release_track)
  env_cls = messages.Environment
  airflow_config_overrides_cls = (
      messages.SoftwareConfig.AirflowConfigOverridesValue)
  entry_cls = airflow_config_overrides_cls.AdditionalProperty

  def _BuildEnv(entries):
    software_config = messages.SoftwareConfig(
        airflowConfigOverrides=airflow_config_overrides_cls(
            additionalProperties=entries))
    config = messages.EnvironmentConfig(softwareConfig=software_config)
    return env_cls(config=config)

  return command_util.BuildPartialUpdate(
      clear_airflow_configs, remove_airflow_configs, update_airflow_configs,
      'config.software_config.airflow_config_overrides', entry_cls, _BuildEnv)


def _ConstructEnvVariablesPatch(env_ref,
                                clear_env_variables,
                                remove_env_variables,
                                update_env_variables,
                                release_track=base.ReleaseTrack.GA):
  """Constructs an environment patch for updating environment variables.

  Note that environment variable updates do not support partial update masks
  unlike other map updates due to comments in (b/78298321). For this reason, we
  need to retrieve the Environment, apply an update on EnvVariable dictionary,
  and patch the entire dictionary. The potential race condition here
  (environment variables being updated between when we retrieve them and when we
  send patch request)is not a concern since environment variable updates take
  5 mins to complete, and environments cannot be updated while already in the
  updating state.

  Args:
    env_ref: resource argument, Environment resource argument for environment
      being updated.
    clear_env_variables: bool, whether to clear the environment variables
      dictionary.
    remove_env_variables: iterable(string), Iterable of environment variable
      names to remove.
    update_env_variables: {string: string}, dict of environment variable names
      and values to set.
    release_track: base.ReleaseTrack, the release track of command. Will dictate
        which Composer client library will be used.

  Returns:
    (str, Environment), the field mask and environment to use for update.
  """
  env_obj = environments_api_util.Get(env_ref, release_track=release_track)
  initial_env_var_value = env_obj.config.softwareConfig.envVariables
  initial_env_var_list = (
      initial_env_var_value.additionalProperties
      if initial_env_var_value else [])

  messages = api_util.GetMessagesModule(release_track=release_track)
  env_cls = messages.Environment
  env_variables_cls = messages.SoftwareConfig.EnvVariablesValue
  entry_cls = env_variables_cls.AdditionalProperty

  def _BuildEnv(entries):
    software_config = messages.SoftwareConfig(
        envVariables=env_variables_cls(additionalProperties=entries))
    config = messages.EnvironmentConfig(softwareConfig=software_config)
    return env_cls(config=config)

  return ('config.software_config.env_variables',
          command_util.BuildFullMapUpdate(
              clear_env_variables, remove_env_variables, update_env_variables,
              initial_env_var_list, entry_cls, _BuildEnv))
