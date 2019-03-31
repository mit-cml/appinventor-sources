# -*- coding: utf-8 -*- #
# Copyright 2017 Google Inc. All Rights Reserved.
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
"""Reducer functions to generate instance props from prior state and flags."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import argparse
from googlecloudsdk.api_lib.sql import api_util as common_api_util
from googlecloudsdk.api_lib.sql import constants
from googlecloudsdk.api_lib.sql import instances as api_util
from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.calliope import exceptions
from googlecloudsdk.core import properties
from googlecloudsdk.core.util import files


def BackupConfiguration(sql_messages,
                        instance=None,
                        backup=None,
                        no_backup=None,
                        backup_start_time=None,
                        enable_bin_log=None):
  """Generates the backup configuration for the instance.

  Args:
    sql_messages: module, The messages module that should be used.
    instance: sql_messages.DatabaseInstance, the original instance, if the
        previous state is needed.
    backup: boolean, True if backup should be enabled.
    no_backup: boolean, True if backup should be disabled.
    backup_start_time: string, start time of backup specified in 24-hour format.
    enable_bin_log: boolean, True if binary logging should be enabled.

  Returns:
    sql_messages.BackupConfiguration object, or None

  Raises:
    ToolException: Bad combination of arguments.
  """
  backup_enabled = no_backup is False or backup
  should_generate_config = any(
      [backup_start_time, enable_bin_log is not None, not backup_enabled])

  # TODO(b/63139210): Remove v1beta3-related logic -- isinstance(..., list).
  if not should_generate_config:
    return None
  elif not instance or not instance.settings.backupConfiguration:
    backup_config = sql_messages.BackupConfiguration(
        startTime='00:00', enabled=backup_enabled)
  elif isinstance(instance.settings.backupConfiguration, list):
    # Only one backup configuration was ever allowed.
    # Field switched from list to single object in v1beta4.
    backup_config = instance.settings.backupConfiguration[0]
  else:
    backup_config = instance.settings.backupConfiguration

  if backup_start_time:
    backup_config.startTime = backup_start_time
    backup_config.enabled = True
  if no_backup:
    if backup_start_time or enable_bin_log is not None:
      raise exceptions.ToolException(
          ('Argument --no-backup not allowed with'
           ' --backup-start-time or --enable-bin-log'))
    backup_config.enabled = False

  if enable_bin_log is not None:
    backup_config.binaryLogEnabled = enable_bin_log

  return backup_config


def DatabaseFlags(sql_messages,
                  settings=None,
                  database_flags=None,
                  clear_database_flags=False):
  """Generates the database flags for the instance.

  Args:
    sql_messages: module, The messages module that should be used.
    settings: sql_messages.Settings, the original settings, if the previous
        state is needed.
    database_flags: dict of flags.
    clear_database_flags: boolean, True if flags should be cleared.

  Returns:
    list of sql_messages.DatabaseFlags objects
  """
  updated_flags = []
  if database_flags:
    for (name, value) in sorted(database_flags.items()):
      updated_flags.append(sql_messages.DatabaseFlags(name=name, value=value))
  elif clear_database_flags:
    updated_flags = []
  elif settings:
    updated_flags = settings.databaseFlags

  return updated_flags


def MaintenanceWindow(sql_messages,
                      instance,
                      maintenance_release_channel=None,
                      maintenance_window_day=None,
                      maintenance_window_hour=None):
  """Generates the maintenance window for the instance.

  Args:
    sql_messages: module, The messages module that should be used.
    instance: sql_messages.DatabaseInstance, The original instance, if
        it might be needed to generate the maintenance window.
    maintenance_release_channel: string, which channel's updates to apply.
    maintenance_window_day: string, maintenance window day of week.
    maintenance_window_hour: int, maintenance window hour of day.

  Returns:
    sql_messages.MaintenanceWindow or None

  Raises:
    argparse.ArgumentError: no maintenance window specified.
  """
  channel = maintenance_release_channel
  day = maintenance_window_day
  hour = maintenance_window_hour
  if not any([channel, day, hour]):
    return None

  maintenance_window = sql_messages.MaintenanceWindow()

  # If there's no existing maintenance window,
  # both or neither of day and hour must be set.
  if (not instance or not instance.settings or
      not instance.settings.maintenanceWindow):
    if ((day is None and hour is not None) or
        (hour is None and day is not None)):
      raise argparse.ArgumentError(
          None, 'There is currently no maintenance window on the instance. '
          'To add one, specify values for both day, and hour.')

  if channel:
    # Map UI name to API name.
    names = {'production': 'stable', 'preview': 'canary'}
    maintenance_window.updateTrack = names[channel]
  if day:
    # Map day name to number.
    day_num = arg_parsers.DayOfWeek.DAYS.index(day)
    if day_num == 0:
      day_num = 7
    maintenance_window.day = day_num
  if hour is not None:  # must execute on hour = 0
    maintenance_window.hour = hour
  return maintenance_window


def _CustomMachineTypeString(cpu, memory_mib):
  """Creates a custom machine type from the CPU and memory specs.

  Args:
    cpu: the number of cpu desired for the custom machine type
    memory_mib: the amount of ram desired in MiB for the custom machine
        type instance

  Returns:
    The custom machine type name for the 'instance create' call
  """
  machine_type = 'db-custom-{0}-{1}'.format(cpu, memory_mib)
  return machine_type


def MachineType(instance=None, tier=None, memory=None, cpu=None):
  """Generates the machine type for the instance.  Adapted from compute.

  Args:
    instance: sql_messages.DatabaseInstance, The original instance, if
        it might be needed to generate the machine type.
    tier: string, the v1 or v2 tier.
    memory: string, the amount of memory.
    cpu: int, the number of CPUs.

  Returns:
    A string representing the URL naming a machine-type.

  Raises:
    exceptions.RequiredArgumentException when only one of the two custom
        machine type flags are used, or when none of the flags are used.
    exceptions.InvalidArgumentException when both the tier and
        custom machine type flags are used to generate a new instance.
  """

  # Setting the machine type.
  machine_type = None
  if tier:
    machine_type = tier

  # Setting the specs for the custom machine.
  if cpu or memory:
    if not cpu:
      raise exceptions.RequiredArgumentException(
          '--cpu', 'Both [--cpu] and [--memory] must be '
          'set to create a custom machine type instance.')
    if not memory:
      raise exceptions.RequiredArgumentException(
          '--memory', 'Both [--cpu] and [--memory] must '
          'be set to create a custom machine type instance.')
    if tier:
      raise exceptions.InvalidArgumentException(
          '--tier', 'Cannot set both [--tier] and '
          '[--cpu]/[--memory] for the same instance.')
    custom_type_string = _CustomMachineTypeString(
        cpu,
        # Converting from B to MiB.
        memory // (2**20))

    # Updating the machine type that is set for the URIs.
    machine_type = custom_type_string

  # Reverting to default if creating instance and no flags are set.
  if not machine_type and not instance:
    machine_type = constants.DEFAULT_MACHINE_TYPE

  return machine_type


def OnPremisesConfiguration(sql_messages, source_ip_address, source_port):
  """Generates the external master configuration for the instance.

  Args:
    sql_messages: module, The messages module that should be used.
    source_ip_address: string, the IP address of the external data source.
    source_port: number, the port number of the external data source.

  Returns:
    sql_messages.OnPremisesConfiguration object.
  """
  return sql_messages.OnPremisesConfiguration(
      hostPort='{0}:{1}'.format(source_ip_address, source_port))


def PrivateNetworkUrl(network):
  """Generates the self-link of the instance's private network.

  Args:
    network: The ID of the network.

  Returns:
    string, the URL of the network.
  """
  client = common_api_util.SqlClient(common_api_util.API_VERSION_DEFAULT)
  network_ref = client.resource_parser.Parse(
      network,
      params={
          'project': properties.VALUES.core.project.GetOrFail,
      },
      collection='compute.networks')
  return network_ref.SelfLink()


def ReplicaConfiguration(sql_messages,
                         master_username,
                         master_password,
                         master_dump_file_path,
                         master_ca_certificate_path=None,
                         client_certificate_path=None,
                         client_key_path=None):
  """Generates the config for an external master replica.

  Args:
    sql_messages: module, The messages module that should be used.
    master_username: The username for connecting to the external instance.
    master_password: The password for connecting to the external instance.
    master_dump_file_path: ObjectReference, a wrapper for the URI of the Cloud
        Storage path containing the dumpfile to seed the replica with.
    master_ca_certificate_path: The path to the CA certificate PEM file.
    client_certificate_path: The path to the client certificate PEM file.
    client_key_path: The path to the client private key PEM file.

  Returns:
    sql_messages.MySqlReplicaConfiguration object.
  """
  mysql_replica_configuration = sql_messages.MySqlReplicaConfiguration(
      username=master_username,
      password=master_password,
      dumpFilePath=master_dump_file_path.ToUrl())
  if master_ca_certificate_path:
    mysql_replica_configuration.caCertificate = files.ReadFileContents(
        master_ca_certificate_path)
  if client_certificate_path:
    mysql_replica_configuration.clientCertificate = files.ReadFileContents(
        client_certificate_path)
  if client_key_path:
    mysql_replica_configuration.clientKey = files.ReadFileContents(
        client_key_path)
  return sql_messages.ReplicaConfiguration(
      mysqlReplicaConfiguration=mysql_replica_configuration)


def Region(specified_region, gce_zone):
  """Generates the region string for the instance.

  Args:
    specified_region: string, the GCE region to create the instance in.
    gce_zone: string, the GCE zone to create the instance in.

  Returns:
    string, the region to create the instance in.
  """
  if gce_zone:
    derived_region = api_util.GetRegionFromZone(gce_zone)
    return derived_region
  return specified_region
