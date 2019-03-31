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
"""Utilities for reading instances for prediction."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import io
import json


from googlecloudsdk.core import exceptions as core_exceptions
from googlecloudsdk.core import properties
from googlecloudsdk.core import resources
from googlecloudsdk.core.console import console_io
from googlecloudsdk.core.util import encoding

import six


class InvalidInstancesFileError(core_exceptions.Error):
  """Indicates that the input file was invalid in some way."""
  pass


def ReadInstances(input_file, data_format, limit=None):
  """Reads the instances from input file.

  Args:
    input_file: An open file-like object for the input file.
    data_format: str, data format of the input file, 'json' or 'text'.
    limit: int, the maximum number of instances allowed in the file

  Returns:
    A list of instances.

  Raises:
    InvalidInstancesFileError: If the input file is invalid (invalid format or
        contains too many/zero instances).
  """
  instances = []

  for line_num, line in enumerate(input_file):
    if isinstance(line, six.binary_type):
      line = encoding.Decode(line, encoding='utf-8-sig')  # Handle UTF8-BOM
    line_content = line.rstrip('\r\n')
    if not line_content:
      raise InvalidInstancesFileError('Empty line is not allowed in the '
                                      'instances file.')
    if limit and line_num >= limit:
      raise InvalidInstancesFileError(
          'Online prediction can process no more than ' + str(limit) +
          ' instances per file. Please use batch prediction instead.')
    if data_format == 'json':
      try:
        instances.append(json.loads(line_content))
      except ValueError:
        raise InvalidInstancesFileError(
            'Input instances are not in JSON format. '
            'See "gcloud ml-engine predict --help" for details.')
    elif data_format == 'text':
      instances.append(line_content)

  if not instances:
    raise InvalidInstancesFileError(
        'No valid instance was found in input file.')

  return instances


def ReadInstancesFromArgs(json_instances, text_instances, limit=None):
  """Reads the instances from the given file path ('-' for stdin).

  Exactly one of json_instances, text_instances must be given.

  Args:
    json_instances: str or None, a path to a file ('-' for stdin) containing
        instances in JSON format.
    text_instances: str or None, a path to a file ('-' for stdin) containing
        instances in text format.
    limit: int, the maximum number of instances allowed in the file

  Returns:
    A list of instances.

  Raises:
    InvalidInstancesFileError: If the input file is invalid (invalid format or
        contains too many/zero instances), or an improper combination of input
        files was given.
  """
  if (json_instances and text_instances or
      not (json_instances or text_instances)):
    raise InvalidInstancesFileError(
        'Exactly one of --json-instances and --text-instances must be '
        'specified.')

  if json_instances:
    data_format = 'json'
    input_file = json_instances
  elif text_instances:
    data_format = 'text'
    input_file = text_instances

  data = console_io.ReadFromFileOrStdin(input_file, binary=True)
  with io.BytesIO(data) as f:
    return ReadInstances(f, data_format, limit=limit)


def ParseModelOrVersionRef(model_id, version_id):
  if version_id:
    return resources.REGISTRY.Parse(
        version_id,
        collection='ml.projects.models.versions',
        params={
            'projectsId': properties.VALUES.core.project.GetOrFail,
            'modelsId': model_id
        })
  else:
    return resources.REGISTRY.Parse(
        model_id,
        params={'projectsId': properties.VALUES.core.project.GetOrFail},
        collection='ml.projects.models')


def GetDefaultFormat(predictions):
  if not isinstance(predictions, list):
    # This usually indicates some kind of error case, so surface the full API
    # response
    return 'json'
  elif not predictions:
    return None
  # predictions is guaranteed by API contract to be a list of similarly shaped
  # objects, but we don't know ahead of time what those objects look like.
  elif isinstance(predictions[0], dict):
    keys = ', '.join(sorted(predictions[0].keys()))
    return """
          table(
              predictions:format="table(
                  {}
              )"
          )""".format(keys)

  else:
    return 'table[no-heading](predictions)'
