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
"""Common utility functions for sql instances."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import os
import subprocess
import time

from apitools.base.py import list_pager
from googlecloudsdk.api_lib.sql import api_util
from googlecloudsdk.api_lib.sql import constants
from googlecloudsdk.api_lib.sql import exceptions as sql_exceptions
from googlecloudsdk.calliope import exceptions
from googlecloudsdk.core import config
from googlecloudsdk.core import execution_utils
from googlecloudsdk.core import log
from googlecloudsdk.core import properties
from googlecloudsdk.core.console import console_io
from googlecloudsdk.core.util import encoding
from googlecloudsdk.core.util import files as file_utils


_BASE_CLOUD_SQL_PROXY_ERROR = 'Failed to start the Cloud SQL Proxy'

_POSTGRES_DATABASE_VERSION_PREFIX = 'POSTGRES'


def GetRegionFromZone(gce_zone):
  """Parses and returns the region string from the gce_zone string."""
  zone_components = gce_zone.split('-')
  # The region is the first two components of the zone.
  return '-'.join(zone_components[:2])


def _GetCloudSqlProxyPath():
  """Determines the path to the cloud_sql_proxy binary."""
  sdk_bin_path = config.Paths().sdk_bin_path
  if not sdk_bin_path:
    # Check if cloud_sql_proxy is located on the PATH.
    proxy_path = file_utils.FindExecutableOnPath('cloud_sql_proxy')
    if proxy_path:
      log.debug(
          'Using cloud_sql_proxy found at [{path}]'.format(path=proxy_path))
      return proxy_path
    else:
      raise exceptions.ToolException(
          'A Cloud SQL Proxy SDK root could not be found. Please check your '
          'installation.')
  return os.path.join(sdk_bin_path, 'cloud_sql_proxy')


def _RaiseProxyError(error_msg=None):
  message = '{}.'.format(_BASE_CLOUD_SQL_PROXY_ERROR)
  if error_msg:
    message = '{}: {}'.format(_BASE_CLOUD_SQL_PROXY_ERROR, error_msg)
  raise sql_exceptions.CloudSqlProxyError(message)


def _ReadLineFromStderr(proxy_process):
  """Reads and returns the next line from the proxy stderr stream."""
  return encoding.Decode(proxy_process.stderr.readline())


def _WaitForProxyToStart(proxy_process, port, seconds_to_timeout):
  """Wait for the proxy to be ready for connections, then return proxy_process.

  Args:
    proxy_process: Process, the process corresponding to the Cloud SQL Proxy.
    port: int, the port that the proxy was started on.
    seconds_to_timeout: Seconds to wait before timing out.

  Returns:
    The Process object corresponding to the Cloud SQL Proxy.
  """

  total_wait_seconds = 0
  seconds_to_sleep = 0.2
  while proxy_process.poll() is None:
    line = _ReadLineFromStderr(proxy_process)
    while line:
      log.status.write(line)
      if constants.PROXY_ADDRESS_IN_USE_ERROR in line:
        _RaiseProxyError(
            'Port already in use. Exit the process running on port {} or try '
            'connecting again on a different port.'.format(port))
      elif constants.PROXY_READY_FOR_CONNECTIONS_MSG in line:
        # The proxy is ready to go, so stop polling!
        return proxy_process
      line = _ReadLineFromStderr(proxy_process)

    # If we've been waiting past the timeout, throw an error.
    if total_wait_seconds >= seconds_to_timeout:
      _RaiseProxyError('Timed out.')

    # Keep polling on the proxy output until relevant lines are found.
    total_wait_seconds += seconds_to_sleep
    time.sleep(seconds_to_sleep)

  # If we've reached this point, the proxy process exited unexpectedly.
  _RaiseProxyError()


def StartCloudSqlProxy(instance, port, seconds_to_timeout=10):
  """Starts the Cloud SQL Proxy for instance on the given port.

  Args:
    instance: The instance to start the proxy for.
    port: The port to bind the proxy to.
    seconds_to_timeout: Seconds to wait before timing out.

  Returns:
    The Process object corresponding to the Cloud SQL Proxy.

  Raises:
    CloudSqlProxyError: An error starting the Cloud SQL Proxy.
    ToolException: An error finding a Cloud SQL Proxy installation.
  """
  command_path = _GetCloudSqlProxyPath()

  # Specify the instance and port to connect with.
  args = ['-instances', '{}=tcp:{}'.format(instance.connectionName, port)]
  # Specify the credentials.
  account = properties.VALUES.core.account.Get(required=True)
  args += ['-credential_file', config.Paths().LegacyCredentialsAdcPath(account)]
  proxy_args = execution_utils.ArgsForExecutableTool(command_path, *args)
  log.status.write(
      'Starting Cloud SQL Proxy: [{args}]]\n'.format(args=' '.join(proxy_args)))

  proxy_process = subprocess.Popen(
      proxy_args,
      stdout=subprocess.PIPE,
      stdin=subprocess.PIPE,
      stderr=subprocess.PIPE)
  return _WaitForProxyToStart(proxy_process, port, seconds_to_timeout)


def IsInstanceV1(instance):
  """Returns a boolean indicating if the database instance is first gen."""
  return (instance.backendType == 'FIRST_GEN' or
          (instance.settings and instance.settings.tier.startswith('D')))


def IsInstanceV2(instance):
  """Returns a boolean indicating if the database instance is second gen."""
  return instance.backendType == 'SECOND_GEN'


# TODO(b/73648377): Factor out static methods into module-level functions.
class _BaseInstances(object):
  """Common utility functions for sql instances."""

  @staticmethod
  def GetDatabaseInstances(limit=None, batch_size=None):
    """Gets SQL instances in a given project.

    Modifies current state of an individual instance to 'STOPPED' if
    activationPolicy is 'NEVER'.

    Args:
      limit: int, The maximum number of records to yield. None if all available
          records should be yielded.
      batch_size: int, The number of items to retrieve per request.

    Returns:
      List of yielded sql_messages.DatabaseInstance instances.
    """

    client = api_util.SqlClient(api_util.API_VERSION_DEFAULT)
    sql_client = client.sql_client
    sql_messages = client.sql_messages
    project_id = properties.VALUES.core.project.Get(required=True)

    params = {}
    if limit is not None:
      params['limit'] = limit
    if batch_size is not None:
      params['batch_size'] = batch_size

    yielded = list_pager.YieldFromList(
        sql_client.instances,
        sql_messages.SqlInstancesListRequest(project=project_id), **params)

    def YieldInstancesWithAModifiedState():
      for result in yielded:
        # TODO(b/63139112): Investigate impact of instances without settings.
        if result.settings and result.settings.activationPolicy == 'NEVER':
          result.state = 'STOPPED'
        yield result

    return YieldInstancesWithAModifiedState()

  @staticmethod
  def PrintAndConfirmAuthorizedNetworksOverwrite():
    console_io.PromptContinue(
        message='When adding a new IP address to authorized networks, '
        'make sure to also include any IP addresses that have already been '
        'authorized. Otherwise, they will be overwritten and de-authorized.',
        default=True,
        cancel_on_no=True)

  @staticmethod
  def IsPostgresDatabaseVersion(database_version):
    """Returns a boolean indicating if the database version is Postgres."""
    return _POSTGRES_DATABASE_VERSION_PREFIX in database_version


class InstancesV1Beta3(_BaseInstances):
  """Common utility functions for sql instances V1Beta3."""

  @staticmethod
  def SetProjectAndInstanceFromRef(instance_resource, instance_ref):
    instance_resource.project = instance_ref.project
    instance_resource.instance = instance_ref.instance

  @staticmethod
  def AddBackupConfigToSettings(settings, backup_config):
    settings.backupConfiguration = [backup_config]


class InstancesV1Beta4(_BaseInstances):
  """Common utility functions for sql instances V1Beta4."""

  @staticmethod
  def SetProjectAndInstanceFromRef(instance_resource, instance_ref):
    instance_resource.project = instance_ref.project
    instance_resource.name = instance_ref.instance

  @staticmethod
  def AddBackupConfigToSettings(settings, backup_config):
    settings.backupConfiguration = backup_config
