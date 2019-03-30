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
"""Helpers and common arguments for Composer commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import argparse
import re

from googlecloudsdk.calliope import actions
from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.calliope import base
from googlecloudsdk.calliope import exceptions
from googlecloudsdk.command_lib.composer import parsers
from googlecloudsdk.core import properties

ENVIRONMENT_NAME_ARG = base.Argument(
    'name', metavar='NAME', help='The name of an environment.')

MULTI_ENVIRONMENT_NAME_ARG = base.Argument(
    'name', metavar='NAME', nargs='+', help='The name of an environment.')

MULTI_OPERATION_NAME_ARG = base.Argument(
    'name', metavar='NAME', nargs='+', help='The name or UUID of an operation.')

OPERATION_NAME_ARG = base.Argument(
    'name', metavar='NAME', help='The name or UUID of an operation.')

LOCATION_FLAG = base.Argument(
    '--location',
    required=False,
    help='The Cloud Composer location (e.g., us-central1).',
    action=actions.StoreProperty(properties.VALUES.composer.location))

_ENV_VAR_NAME_ERROR = (
    'Only upper and lowercase letters, digits, and underscores are allowed. '
    'Environment variable names may not start with a digit.')

CLEAR_AIRFLOW_CONFIGS_FLAG = base.Argument(
    '--clear-airflow-configs',
    action='store_true',
    help="""\
    Remove all Airflow config overrides from the environment.
    """)

UPDATE_AIRFLOW_CONFIGS_FLAG = base.Argument(
    '--update-airflow-configs',
    metavar='KEY=VALUE',
    type=arg_parsers.ArgDict(key_type=str, value_type=str),
    action=arg_parsers.UpdateAction,
    help="""\
    A list of Airflow config override KEY=VALUE pairs to set. If a config
    override already exists, its value is updated; otherwise a new config
    override is created.

    KEYs should specify the configuration section and property name,
    separated by a hyphen, for example `core-print_stats_interval`. The
    section may not contain a closing square brace or period. The property
    name must be non-empty and may not contain an equals sign, semicolon,
    or period. By convention, property names are spelled with
    `snake_case.` VALUEs may contain any character.
    """)

REMOVE_AIRFLOW_CONFIGS_FLAG = base.Argument(
    '--remove-airflow-configs',
    metavar='KEY',
    type=arg_parsers.ArgList(),
    action=arg_parsers.UpdateAction,
    help="""\
    A list of Airflow config override keys to remove.
    """)

UPDATE_ENV_VARIABLES_FLAG = base.Argument(
    '--update-env-variables',
    metavar='NAME=VALUE',
    type=arg_parsers.ArgDict(key_type=str, value_type=str),
    action=arg_parsers.UpdateAction,
    help="""\
    A list of environment variable NAME=VALUE pairs to set and provide to the
    Aiflow scheduler, worker, and webserver processes. If an environment
    variable already exists, its value is updated; otherwise a new environment
    variable is created.

    NAMEs are the environment variable names and may contain upper and
    lowercase letters, digits, and underscores; they must not begin with a
    digit.

    User-specified environment variables should not be used to set Airflow
    configuration properties. Instead use the `--update-airflow-configs` flag.
    """)

REMOVE_ENV_VARIABLES_FLAG = base.Argument(
    '--remove-env-variables',
    metavar='NAME',
    type=arg_parsers.ArgList(),
    action=arg_parsers.UpdateAction,
    help="""\
    A list of environment variables to remove.

    Environment variables that have system-provided defaults cannot be unset
    with the `--remove-env-variables` or `--clear-env-variables` flags; only
    the user-supplied overrides will be removed.
    """)

CLEAR_ENV_VARIABLES_FLAG = base.Argument(
    '--clear-env-variables',
    action='store_true',
    help="""\
    Remove all environment variables from the environment.

    Environment variables that have system-provided defaults cannot be unset
    with the `--remove-env-variables` or `--clear-env-variables` flags; only
    the user-supplied overrides will be removed.
    """)

UPDATE_PYPI_FROM_FILE_FLAG = base.Argument(
    '--update-pypi-packages-from-file',
    help="""\
    The path to a file containing a list of PyPI packages to install in
    the environment. Each line in the file should contain a package
    specification in the format of the update-pypi-package argument
    defined above. The path can be a local file path or a Google Cloud Storage
    file path (Cloud Storage file path starts with 'gs://').
    """)

CLEAR_PYPI_PACKAGES_FLAG = base.Argument(
    '--clear-pypi-packages',
    action='store_true',
    help="""\
    Remove all PyPI packages from the environment.

    PyPI packages that are required by the environment's core software
    cannot be uninstalled with the `--remove-pypi-packages` or
    `--clear-pypi-packages` flags.
    """)

UPDATE_PYPI_PACKAGE_FLAG = base.Argument(
    '--update-pypi-package',
    metavar='PACKAGE[EXTRAS_LIST]VERSION_SPECIFIER',
    action='append',
    default=[],
    help="""\
    A PyPI package add to the environment. If a package already exists,
    its value is updated; otherwise a new package is installed.

    The value takes the form of: `PACKAGE[EXTRAS_LIST]VERSION_SPECIFIER`,
    as one would specify in a pip requirements file.

    PACKAGE is specified as a package name such as `numpy.` EXTRAS_LIST is
    a comma-delimited list of PEP 508 distribution extras that may be
    empty, in which case the enclosing square brackets may be omitted.
    VERSION_SPECIFIER is an optional PEP 440 version specifier. If both
    EXTRAS_LIST and VERSION_SPECIFIER are omitted, the `=` and
    everything to the right may be left empty.

    This is a repeated argument that can be specified multiple times to
    update multiple packages. If PACKAGE appears more than once, the last
    value will be used.
    """)

REMOVE_PYPI_PACKAGES_FLAG = base.Argument(
    '--remove-pypi-packages',
    metavar='PACKAGE',
    type=arg_parsers.ArgList(),
    action=arg_parsers.UpdateAction,
    help="""\
    A list of PyPI package names to remove.

    PyPI packages that are required by the environment's core software
    cannot be uninstalled with the `--remove-pypi-packages` or
    `--clear-pypi-packages` flags.
    """)


def AddImportSourceFlag(parser, folder):
  """Adds a --source flag for a storage import command to a parser.

  Args:
    parser: argparse.ArgumentParser, the parser to which to add the flag
    folder: str, the top-level folder in the bucket into which the import
        command will write. Should not contain any slashes. For example, 'dags'.
  """
  base.Argument(
      '--source',
      required=True,
      help="""\
      Path to a local directory/file or Cloud Storage bucket/object to be
      imported into the {}/ subdirectory in the environment's Cloud Storage
      bucket. Cloud Storage paths must begin with 'gs://'.
      """.format(folder)).AddToParser(parser)


def AddImportDestinationFlag(parser, folder):
  """Adds a --destination flag for a storage import command to a parser.

  Args:
    parser: argparse.ArgumentParser, the parser to which to add the flag
    folder: str, the top-level folder in the bucket into which the import
        command will write. Should not contain any slashes. For example, 'dags'.
  """
  base.Argument(
      '--destination',
      metavar='DESTINATION',
      required=False,
      help="""\
      An optional subdirectory under the {}/ directory in the environment's Cloud
      Storage bucket into which to import files. May contain forward slashes to
      delimit multiple levels of subdirectory nesting, but should not contain
      leading or trailing slashes. If the DESTINATION does not exist, it will be
      created.
      """.format(folder)).AddToParser(parser)


def AddExportSourceFlag(parser, folder):
  """Adds a --source flag for a storage export command to a parser.

  Args:
    parser: argparse.ArgumentParser, the parser to which to add the flag
    folder: str, the top-level folder in the bucket from which the export
        command will read. Should not contain any slashes. For example, 'dags'.
  """
  base.Argument(
      '--source',
      help="""\
      An optional relative path to a file or directory to be exported from the
      {}/ subdirectory in the environment's Cloud Storage bucket.
      """.format(folder)).AddToParser(parser)


def AddExportDestinationFlag(parser):
  """Adds a --destination flag for a storage export command to a parser.

  Args:
    parser: argparse.ArgumentParser, the parser to which to add the flag
  """
  base.Argument(
      '--destination',
      metavar='DESTINATION',
      required=True,
      help="""\
      The path to an existing local directory or a Cloud Storage
      bucket/directory into which to export files.
      """).AddToParser(parser)


def AddDeleteTargetPositional(parser, folder):
  base.Argument(
      'target',
      nargs='?',
      help="""\
      A relative path to a file or subdirectory to delete within the
      {folder} Cloud Storage subdirectory. If not specified, the entire contents
      of the {folder} subdirectory will be deleted.
      """.format(folder=folder)).AddToParser(parser)


def _IsValidEnvVarName(name):
  """Validates that a user-provided arg is a valid environment variable name.

  Intended to be used as an argparse validator.

  Args:
    name: str, the environment variable name to validate

  Returns:
    bool, True if and only if the name is valid
  """
  return re.match('^[a-zA-Z_][a-zA-Z0-9_]*$', name) is not None


ENV_VAR_NAME_FORMAT_VALIDATOR = arg_parsers.CustomFunctionValidator(
    _IsValidEnvVarName, _ENV_VAR_NAME_ERROR)
CREATE_ENV_VARS_FLAG = base.Argument(
    '--env-variables',
    metavar='NAME=VALUE',
    type=arg_parsers.ArgDict(
        key_type=ENV_VAR_NAME_FORMAT_VALIDATOR, value_type=str),
    action=arg_parsers.UpdateAction,
    help='A comma-delimited list of environment variable `NAME=VALUE` '
    'pairs to provide to the Airflow scheduler, worker, and webserver '
    'processes. NAME may contain upper and lowercase letters, digits, '
    'and underscores, but they may not begin with a digit. '
    'To include commas as part of a `VALUE`, see `{top_command} topics'
    ' escaping` for information about overriding the delimiter.')


def IsValidUserPort(val):
  """Validates that a user-provided arg is a valid user port.

  Intended to be used as an argparse validator.

  Args:
    val: str, a string specifying a TCP port number to be validated

  Returns:
    int, the provided port number

  Raises:
    ArgumentTypeError: if the provided port is not an integer outside the
        system port range
  """
  port = int(val)
  if 1024 <= port and port <= 65535:
    return port
  raise argparse.ArgumentTypeError('PORT must be in range [1024, 65535].')


def ValidateDiskSize(parameter_name, disk_size):
  """Validates that a disk size is a multiple of some number of GB.

  Args:
    parameter_name: parameter_name, the name of the parameter, formatted as
        it would be in help text (e.g., '--disk-size' or 'DISK_SIZE')
    disk_size: int, the disk size in bytes

  Raises:
    exceptions.InvalidArgumentException: the disk size was invalid
  """
  gb_mask = (1 << 30) - 1
  if disk_size & gb_mask:
    raise exceptions.InvalidArgumentException(
        parameter_name,
        'Must be an integer quantity of GB.')


def _AddPartialDictUpdateFlagsToGroup(update_type_group, clear_flag,
                                      remove_flag, update_flag):
  """Adds flags related to a partial update of arg represented by a dictionary.

  Args:
    update_type_group: argument group, the group to which flags should be added.
    clear_flag: flag, the flag to clear dictionary.
    remove_flag: flag, the flag to remove values from dictionary.
    update_flag: flag, the flag to add or update values in dictionary.
  """
  group = update_type_group.add_argument_group()
  remove_group = group.add_mutually_exclusive_group()
  clear_flag.AddToParser(remove_group)
  remove_flag.AddToParser(remove_group)
  update_flag.AddToParser(group)


def AddNodeCountUpdateFlagToGroup(update_type_group):
  """Adds flag related to setting node count.

  Args:
    update_type_group: argument group, the group to which flag should be added.
  """
  update_type_group.add_argument(
      '--node-count',
      metavar='NODE_COUNT',
      type=arg_parsers.BoundedInt(lower_bound=3),
      help='The new number of nodes running the Environment. Must be >= 3.')


def AddPypiUpdateFlagsToGroup(update_type_group):
  """Adds flag related to setting Pypi updates.

  Args:
    update_type_group: argument group, the group to which flag should be added.
  """
  group = update_type_group.add_mutually_exclusive_group()
  UPDATE_PYPI_FROM_FILE_FLAG.AddToParser(group)
  _AddPartialDictUpdateFlagsToGroup(
      group, CLEAR_PYPI_PACKAGES_FLAG, REMOVE_PYPI_PACKAGES_FLAG,
      UPDATE_PYPI_PACKAGE_FLAG)


def AddEnvVariableUpdateFlagsToGroup(update_type_group):
  """Adds flags related to updating environent variables.

  Args:
    update_type_group: argument group, the group to which flags should be added.
  """
  _AddPartialDictUpdateFlagsToGroup(
      update_type_group, CLEAR_ENV_VARIABLES_FLAG,
      REMOVE_ENV_VARIABLES_FLAG, UPDATE_ENV_VARIABLES_FLAG)


def AddAirflowConfigUpdateFlagsToGroup(update_type_group):
  """Adds flags related to updating Airflow configurations.

  Args:
    update_type_group: argument group, the group to which flags should be added.
  """
  _AddPartialDictUpdateFlagsToGroup(
      update_type_group, CLEAR_AIRFLOW_CONFIGS_FLAG,
      REMOVE_AIRFLOW_CONFIGS_FLAG, UPDATE_AIRFLOW_CONFIGS_FLAG)


def FallthroughToLocationProperty(location_refs, flag_name, failure_msg):
  """Provides a list containing composer/location if `location_refs` is empty.

  This intended to be used as a fallthrough for a plural Location resource arg.
  The built-in fallthrough for plural resource args doesn't play well with
  properties, as it will iterate over each character in the string and parse
  it as the resource type. This function will parse the entire property and
  return a singleton list if `location_refs` is empty.

  Args:
    location_refs: [core.resources.Resource], a possibly empty list of location
        resource references
    flag_name: str, if `location_refs` is empty, and the composer/location
        property is also missing, an error message will be reported that will
        advise the user to set this flag name
    failure_msg: str, an error message to accompany the advisory described in
        the docs for `flag_name`.

  Returns:
    [core.resources.Resource]: a non-empty list of location resourc references.
    If `location_refs` was non-empty, it will be the same list, otherwise it
    will be a singleton list containing the value of the [composer/location]
    property.

  Raises:
    exceptions.RequiredArgumentException: both the user-provided locations
        and property fallback were empty
  """
  if location_refs:
    return location_refs

  fallthrough_location = parsers.GetLocation(required=False)
  if fallthrough_location:
    return [parsers.ParseLocation(fallthrough_location)]
  else:
    raise exceptions.RequiredArgumentException(flag_name, failure_msg)
