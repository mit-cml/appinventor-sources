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

"""Common utilities for the gcloud export/import commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import os
import re

from apitools.base.py import encoding as api_encoding
from googlecloudsdk.api_lib.dataproc import exceptions
from googlecloudsdk.core import log
from googlecloudsdk.core import yaml
from googlecloudsdk.core import yaml_validator
from googlecloudsdk.core.resource import resource_property
from googlecloudsdk.core.util import encoding


def _GetSchemaResourceName(schema_path):
  """Returns the schema resource name given the schema path."""
  basename = os.path.basename(schema_path).split('.')[0]
  return resource_property.ConvertToSnakeCase(basename).replace('_', ' ')


def AddExportFlags(parser, schema_path):
  """Add common export flags to the arg parser.

  Args:
    parser: The argparse parser object.
    schema_path: The resource instance schema file path.
  """
  parser.add_argument(
      '--destination',
      help="""Path to a YAML file where the {} configuration will be exported.
      The exported data will not contain any output-only fields. Alternatively,
      you may omit this flag to write to standard output. A schema describing
      the export/import format can be found in:
      {}.
      """.format(_GetSchemaResourceName(schema_path), schema_path),
      # Allow writing to stdout.
      required=False)


def AddImportFlags(parser, schema_path):
  """Add common import flags to the arg parser.

  Args:
    parser: The argparse parser object.
    schema_path: The resource instance schema file path.
  """
  parser.add_argument(
      '--source',
      help="""Path to a YAML file containing {} configuration export data. The
      YAML file must not contain any output-only fields. Alternatively, you may
      omit this flag to read from standard input. A schema describing the
      export/import format can be found in:
      {}.
      """.format(_GetSchemaResourceName(schema_path), schema_path),
      # Allow reading from stdin.
      required=False)


def GetSchemaPath(api_name, api_version='v1', message_name=None,
                  for_help=False):
  """Returns the schema installation path.

  $CLOUDSDKROOT/lib/googlecloudsdk/schemas/
    {api}/{api_version}/{message_name}.yaml

  Args:
    api_name: The api name.
    api_version: The API version string.
    message_name: The UpperCamelCase message name.
    for_help: Replaces the actual Cloud SDK installation root dir with
      $CLOUDSDKROOT.
  """
  path = os.path.join(
      os.path.dirname(os.path.dirname(os.path.dirname(
          encoding.Decode(__file__)))),
      'schemas',
      api_name,
      api_version,
      '{}.yaml'.format(message_name),
  )
  if for_help:
    rel_path_index = path.rfind(os.path.sep + 'googlecloudsdk' + os.path.sep)
    if rel_path_index < 0:
      return path
    path = os.path.join('$CLOUDSDKROOT', 'lib', path[rel_path_index + 1:])
  return path


def ValidateYAML(parsed_yaml, schema_path):
  """Validates YAML against JSON schema.

  Args:
    parsed_yaml: YAML to validate
    schema_path: JSON schema file path.

  Raises:
    IOError: if schema not found in installed resources.
    files.Error: if schema file not found.
    ValidationError: if the template doesn't obey the schema.
    SchemaError: if the schema is invalid.
  """
  yaml_validator.Validator(schema_path).Validate(parsed_yaml)


def _ParseProperties(error_message):
  """Parses disallowed properties from an error message.

  Args:
    error_message: The error message to parse.

  Returns:
    A list of property names.

  A sample error message might look like this:

  Additional properties are not allowed ('id', 'createTime', 'updateTime',
  'name' were unexpected)

  """
  return list(
      property.strip('\'') for property in re.findall("'[^']*'", error_message))


def _ClearFields(fields, path_deque, py_dict):
  """Clear the given fields in a dict at a given path.

  Args:
    fields: A list of fields to clear
    path_deque: A deque containing path segments
    py_dict: A nested dict from which to clear the fields
  """
  tmp_dict = py_dict
  for elem in path_deque:
    tmp_dict = tmp_dict[elem]
  for field in fields:
    if field in tmp_dict:
      del tmp_dict[field]


def _IsDisallowedPropertiesError(error):
  """Checks if an error is due to properties that were not in the schema.

  Args:
    error: A ValidationError

  Returns:
    Whether the error was due to disallowed properties
  """
  prop_validator = 'additionalProperties'
  prop_message = 'Additional properties are not allowed'
  return error.validator == prop_validator and prop_message in error.message


def _FilterYAML(parsed_yaml, schema_path):
  """Filter out fields from the yaml that are not in the schema.

  Args:
    parsed_yaml: yaml to filter
    schema_path: Path to schema.
  """
  has_warnings = False
  for error in yaml_validator.Validator(schema_path).Iterate(parsed_yaml):
    # There are other types of errors (for example, missing a required field),
    # but these are the only ones we expect to see on export and the only ones
    # we want to act on. There is no way to distinguish disallowed fields from
    # unrecognized fields. If we attempt to export an unrecognized value for a
    # recognized field (this will happen whenever we add a new enum value), or
    # if we attempt to export a resource that is missing a required field, we
    # will log the errors as warnings and the exported data will not be able to
    # be imported via the import command until the import command is updated.
    if _IsDisallowedPropertiesError(error):
      fields_to_remove = _ParseProperties(error.message)
      _ClearFields(fields_to_remove, error.path, parsed_yaml)
    else:
      log.warning(error.message)
      has_warnings = True
    if has_warnings:
      log.warning('The import command may need to be updated to handle '
                  'the export data.')


def Import(message_type, stream, schema_path=None):
  """Reads YAML from a stream as a message.

  Args:
    message_type: Type of message to load YAML into.
    stream: Input stream or buffer containing the YAML.
    schema_path: JSON schema file path. None for no YAML validation.

  Raises:
    ParseError: if yaml could not be parsed as the given message type.

  Returns:
    message_type object.
  """
  parsed_yaml = yaml.load(stream)
  if schema_path:
    # If a schema is provided, validate against it.
    yaml_validator.Validator(schema_path).Validate(parsed_yaml)
  try:
    message = api_encoding.PyValueToMessage(message_type, parsed_yaml)
  except Exception as e:
    raise exceptions.ParseError('Cannot parse YAML: [{0}]'.format(e))
  return message


def Export(message, stream=None, schema_path=None):
  """Writes a message as YAML to a stream.

  Args:
    message: Message to write.
    stream: Output stream, None for writing to a string and returning it.
    schema_path: JSON schema file path. If None then all message fields are
      written, otherwise only fields in the schema are written.

  Returns:
    Returns the return value of yaml.dump(). If stream is None then the return
    value is the YAML data as a string.
  """
  message_dict = api_encoding.MessageToPyValue(message)
  if schema_path:
    _FilterYAML(message_dict, schema_path)
  return yaml.dump(message_dict, stream=stream)
