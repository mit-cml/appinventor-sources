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

"""A library that is used to support Functions commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import argparse
import functools
import json
import os
import re

from apitools.base.py import exceptions as apitools_exceptions

from googlecloudsdk.api_lib.functions import exceptions
from googlecloudsdk.api_lib.functions import operations
from googlecloudsdk.api_lib.storage import storage_util
from googlecloudsdk.api_lib.util import apis
from googlecloudsdk.api_lib.util import exceptions as exceptions_util
from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.calliope import base as calliope_base
from googlecloudsdk.calliope import exceptions as base_exceptions
from googlecloudsdk.core import exceptions as core_exceptions
from googlecloudsdk.core import properties
from googlecloudsdk.core import resources
from googlecloudsdk.core.util import encoding

import six.moves.http_client

_DEPLOY_WAIT_NOTICE = 'Deploying function (may take a while - up to 2 minutes)'

_ENTRY_POINT_NAME_RE = re.compile(
    r'^(?=.{1,128}$)[_a-zA-Z0-9]+(?:\.[_a-zA-Z0-9]+)*$')
_ENTRY_POINT_NAME_ERROR = (
    'Entry point name must contain only Latin letters (lower- or '
    'upper-case), digits, dot (.) and underscore (_), and must be at most 128 '
    'characters long. It can neither begin nor end with a dot (.), '
    'nor contain two consecutive dots (..).')

_FUNCTION_NAME_RE = re.compile(r'^[A-Za-z](?:[-_A-Za-z0-9]{0,61}[A-Za-z0-9])?$')
_FUNCTION_NAME_ERROR = (
    'Function name must contain only lower case Latin letters, digits and a '
    'hyphen (-). It must start with letter, must not end with a hyphen, '
    'and must be at most 63 characters long.')

_TOPIC_NAME_RE = re.compile(r'^[a-zA-Z][\-\._~%\+a-zA-Z0-9]{2,254}$')
_TOPIC_NAME_ERROR = (
    'Topic must contain only Latin letters (lower- or upper-case), digits and '
    'the characters - + . _ ~ %. It must start with a letter and be from 3 to '
    '255 characters long.')

_BUCKET_RESOURCE_URI_RE = re.compile(r'^projects/_/buckets/.{3,222}$')

_API_NAME = 'cloudfunctions'
_API_VERSION = 'v1'


def _GetApiVersion(track=calliope_base.ReleaseTrack.GA):  # pylint: disable=unused-argument
  return _API_VERSION


def GetApiClientInstance(track=calliope_base.ReleaseTrack.GA):
  return apis.GetClientInstance(_API_NAME, _GetApiVersion(track))


def GetApiMessagesModule(track=calliope_base.ReleaseTrack.GA):
  return apis.GetMessagesModule(_API_NAME, _GetApiVersion(track))


def GetFunctionRef(name):
  return resources.REGISTRY.Parse(
      name, params={
          'projectsId': properties.VALUES.core.project.Get(required=True),
          'locationsId': properties.VALUES.functions.region.Get()},
      collection='cloudfunctions.projects.locations.functions')


_ID_CHAR = '[a-zA-Z0-9_]'
_P_CHAR = "[][~@#$%&.,?:;+*='()-]"
# capture: '{' ID_CHAR+ ('=' '*''*'?)? '}'
# Named wildcards may be written in curly brackets (e.g. {variable}). The
# value that matched this parameter will be included  in the event
# parameters.
_CAPTURE = r'(\{' + _ID_CHAR + r'(=\*\*?)?})'
# segment: (ID_CHAR | P_CHAR)+
_SEGMENT = '((' + _ID_CHAR + '|' + _P_CHAR + ')+)'
# part: '/' segment | capture
_PART = '(/(' + _SEGMENT + '|' + _CAPTURE + '))'
# path: part+ (but first / is optional)
_PATH = '(/?(' + _SEGMENT + '|' + _CAPTURE + ')' + _PART + '*)'

_PATH_RE_ERROR = ('Path must be a slash-separated list of segments and '
                  'captures. For example, [users/{userId}/profilePic].')


def GetHttpErrorMessage(error):
  """Returns a human readable string representation from the http response.

  Args:
    error: HttpException representing the error response.

  Returns:
    A human readable string representation of the error.
  """
  status = error.response.status
  code = error.response.reason
  message = ''
  try:
    data = json.loads(error.content)
    if 'error' in data:
      error_info = data['error']
      if 'message' in error_info:
        message = error_info['message']
      violations = _GetViolationsFromError(error)
      if violations:
        message += '\nProblems:\n' + violations
      if status == 403:
        permission_issues = _GetPermissionErrorDetails(error_info)
        if permission_issues:
          message += '\nPermission Details:\n' + permission_issues
  except (ValueError, TypeError):
    message = error.content
  return 'ResponseError: status=[{0}], code=[{1}], message=[{2}]'.format(
      status, code, encoding.Decode(message))


def _ValidateArgumentByRegexOrRaise(argument, regex, error_message):
  if isinstance(regex, str):
    match = re.match(regex, argument)
  else:
    match = regex.match(argument)
  if not match:
    raise arg_parsers.ArgumentTypeError(
        "Invalid value '{0}': {1}".format(argument, error_message))
  return argument


def ValidateFunctionNameOrRaise(name):
  """Checks if a function name provided by user is valid.

  Args:
    name: Function name provided by user.
  Returns:
    Function name.
  Raises:
    ArgumentTypeError: If the name provided by user is not valid.
  """
  return _ValidateArgumentByRegexOrRaise(name, _FUNCTION_NAME_RE,
                                         _FUNCTION_NAME_ERROR)


def ValidateEntryPointNameOrRaise(entry_point):
  """Checks if a entry point name provided by user is valid.

  Args:
    entry_point: Entry point name provided by user.
  Returns:
    Entry point name.
  Raises:
    ArgumentTypeError: If the entry point name provided by user is not valid.
  """
  return _ValidateArgumentByRegexOrRaise(entry_point, _ENTRY_POINT_NAME_RE,
                                         _ENTRY_POINT_NAME_ERROR)


def ValidateAndStandarizeBucketUriOrRaise(bucket):
  """Checks if a bucket uri provided by user is valid.

  If the Bucket uri is valid, converts it to a standard form.

  Args:
    bucket: Bucket uri provided by user.
  Returns:
    Sanitized bucket uri.
  Raises:
    ArgumentTypeError: If the name provided by user is not valid.
  """
  if _BUCKET_RESOURCE_URI_RE.match(bucket):
    bucket_ref = storage_util.BucketReference.FromUrl(bucket)
  else:
    try:
      bucket_ref = storage_util.BucketReference.FromArgument(
          bucket, require_prefix=False)
    except argparse.ArgumentTypeError as e:
      raise arg_parsers.ArgumentTypeError(
          "Invalid value '{}': {}".format(bucket, e))

  # strip any extrenuous '/' and append single '/'
  bucket = bucket_ref.ToUrl().rstrip('/') + '/'
  return bucket


def ValidatePubsubTopicNameOrRaise(topic):
  """Checks if a Pub/Sub topic name provided by user is valid.

  Args:
    topic: Pub/Sub topic name provided by user.
  Returns:
    Topic name.
  Raises:
    ArgumentTypeError: If the name provided by user is not valid.
  """
  topic = _ValidateArgumentByRegexOrRaise(topic, _TOPIC_NAME_RE,
                                          _TOPIC_NAME_ERROR)
  return topic


def ValidateDirectoryExistsOrRaiseFunctionError(directory):
  """Checks if a source directory exists.

  Args:
    directory: A string: a local path to directory provided by user.
  Returns:
    The argument provided, if found valid.
  Raises:
    ArgumentTypeError: If the user provided a directory which is not valid.
  """
  if not os.path.exists(directory):
    raise exceptions.FunctionsError(
        'argument --source: Provided directory does not exist. If '
        'you intended to provide a path to Google Cloud Repository, you must '
        'specify the --source-url argument')
  if not os.path.isdir(directory):
    raise exceptions.FunctionsError(
        'argument --source: Provided path does not point to a directory. If '
        'you intended to provide a path to Google Cloud Repository, you must '
        'specify the --source-url argument')
  return directory


def ValidatePathOrRaise(path):
  """Check if path provided by user is valid.

  Args:
    path: A string: resource path
  Returns:
    The argument provided, if found valid.
  Raises:
    ArgumentTypeError: If the user provided a path which is not valid
  """
  path = _ValidateArgumentByRegexOrRaise(path, _PATH, _PATH_RE_ERROR)
  return path


def _GetViolationsFromError(error):
  """Looks for violations descriptions in error message.

  Args:
    error: HttpError containing error information.
  Returns:
    String of newline-separated violations descriptions.
  """
  error_payload = exceptions_util.HttpErrorPayload(error)
  field_errors = error_payload.field_violations
  if not field_errors:
    return ''
  return '\n'.join(field_errors.values()) + '\n'


def _GetPermissionErrorDetails(error_info):
  """Looks for permission denied details in error message.

  Args:
    error_info: json containing error information.
  Returns:
    string containing details on permission issue and suggestions to correct.
  """
  try:
    if 'details' in error_info:
      details = error_info['details'][0]
      if 'detail' in details:
        return details['detail']

  except (ValueError, TypeError):
    pass
  return None


def CatchHTTPErrorRaiseHTTPException(func):
  """Decorator that catches HttpError and raises corresponding exception."""

  @functools.wraps(func)
  def CatchHTTPErrorRaiseHTTPExceptionFn(*args, **kwargs):
    try:
      return func(*args, **kwargs)
    except apitools_exceptions.HttpError as error:
      core_exceptions.reraise(
          base_exceptions.HttpException(GetHttpErrorMessage(error)))

  return CatchHTTPErrorRaiseHTTPExceptionFn


def FormatTimestamp(timestamp):
  """Formats a timestamp which will be presented to a user.

  Args:
    timestamp: Raw timestamp string in RFC3339 UTC "Zulu" format.
  Returns:
    Formatted timestamp string.
  """
  return re.sub(r'(\.\d{3})\d*Z$', r'\1', timestamp.replace('T', ' '))


@CatchHTTPErrorRaiseHTTPException
def GetFunction(function_name):
  """Returns the Get method on function response, None if it doesn't exist."""
  client = GetApiClientInstance()
  messages = client.MESSAGES_MODULE
  try:
    # We got response for a get request so a function exists.
    return client.projects_locations_functions.Get(
        messages.CloudfunctionsProjectsLocationsFunctionsGetRequest(
            name=function_name))
  except apitools_exceptions.HttpError as error:
    if error.status_code == six.moves.http_client.NOT_FOUND:
      # The function has not been found.
      return None
    raise


@CatchHTTPErrorRaiseHTTPException
def PatchFunction(function, fields_to_patch):
  """Call the api to patch a function based on updated fields.

  Args:
    function: the function to patch
    fields_to_patch: the fields to patch on the function
  Returns:
    The patched function.
  """
  client = GetApiClientInstance()
  messages = client.MESSAGES_MODULE
  fields_to_patch_str = ','.join(sorted(fields_to_patch))
  op = client.projects_locations_functions.Patch(
      messages.CloudfunctionsProjectsLocationsFunctionsPatchRequest(
          cloudFunction=function,
          name=function.name,
          updateMask=fields_to_patch_str,
      )
  )
  operations.Wait(op, messages, client, _DEPLOY_WAIT_NOTICE)
  return GetFunction(function.name)


@CatchHTTPErrorRaiseHTTPException
def CreateFunction(function, location):
  client = GetApiClientInstance()
  messages = client.MESSAGES_MODULE
  op = client.projects_locations_functions.Create(
      messages.CloudfunctionsProjectsLocationsFunctionsCreateRequest(
          location=location, cloudFunction=function))
  operations.Wait(op, messages, client, _DEPLOY_WAIT_NOTICE)
  return GetFunction(function.name)
