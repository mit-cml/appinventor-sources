# -*- coding: utf-8 -*- #
# Copyright 2014 Google Inc. All Rights Reserved.
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
"""Utility functions that don't belong in the other utility modules."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import argparse
import io
import re

from googlecloudsdk.api_lib.compute import constants
from googlecloudsdk.api_lib.compute import exceptions
from googlecloudsdk.calliope import exceptions as calliope_exceptions
from googlecloudsdk.core import log
from googlecloudsdk.core.console import console_io
from googlecloudsdk.core.resource import resource_printer


COMPUTE_ALPHA_API_VERSION = 'alpha'
COMPUTE_BETA_API_VERSION = 'beta'
COMPUTE_GA_API_VERSION = 'v1'


class InstanceNotReadyError(exceptions.Error):
  """The user is attempting to perform an operation on a not-ready instance."""


class InvalidUserError(exceptions.Error):
  """The user provided an invalid username."""


class MissingDependencyError(exceptions.Error):
  """An external dependency is missing."""


class TimeoutError(exceptions.Error):
  """The user command timed out."""


class WrongInstanceTypeError(exceptions.Error):
  """The instance type is not appropriate for this command."""


class ImageNotFoundError(exceptions.Error):
  """The image resource could not be found."""


def ZoneNameToRegionName(zone_name):
  """Converts zone name to region name: 'us-central1-a' -> 'us-central1'."""
  return zone_name.rsplit('-', 1)[0]


def CollectionToResourceType(collection):
  """Converts a collection to a resource type: 'compute.disks' -> 'disks'."""
  return collection.split('.', 1)[1]


def _GetApiNameFromCollection(collection):
  """Converts a collection to an api: 'compute.disks' -> 'compute'."""
  return collection.split('.', 1)[0]


def GetApiCollection(resource_type):
  """Coverts a resource type to a collection."""
  return 'compute.' + resource_type


def NormalizeGoogleStorageUri(uri):
  """Converts gs:// to http:// if uri begins with gs:// else returns uri."""
  if uri and uri.startswith('gs://'):
    return 'http://storage.googleapis.com/' + uri[len('gs://'):]
  else:
    return uri


def CamelCaseToOutputFriendly(string):
  """Converts camel case text into output friendly text.

  Args:
    string: The string to convert.

  Returns:
    The string converted from CamelCase to output friendly text.

  Examples:
    'camelCase' -> 'camel case'
    'CamelCase' -> 'camel case'
    'camelTLA' -> 'camel tla'
  """
  return re.sub('([A-Z]+)', r' \1', string).strip().lower()


def ConstructList(title, items):
  """Returns a string displaying the items and a title."""
  buf = io.StringIO()
  fmt = 'list[title="{title}",always-display-title]'.format(title=title)
  resource_printer.Print(sorted(set(items)), fmt, out=buf)
  return buf.getvalue()


def RaiseToolException(problems, error_message=None):
  """Raises a ToolException with the given list of problems."""
  RaiseException(problems, calliope_exceptions.ToolException, error_message)


def RaiseException(problems, exception, error_message=None):
  """Raises the provided exception with the given list of problems."""
  errors = []
  for _, message in problems:
    errors.append(message)

  raise exception(
      ConstructList(
          error_message or 'Some requests did not succeed:',
          errors))


# TODO(b/32637269) delete and clean up uses of scope_name.
def PromptForDeletion(refs, scope_name=None, prompt_title=None):
  """Prompts the user to confirm deletion of resources."""
  if not refs:
    return
  resource_type = CollectionToResourceType(refs[0].Collection())
  resource_name = CamelCaseToOutputFriendly(resource_type)
  prompt_list = []
  for ref in refs:
    if scope_name:
      ref_scope_name = scope_name
    elif hasattr(ref, 'region'):
      ref_scope_name = 'region'
    else:
      ref_scope_name = None
    if ref_scope_name:
      item = '[{0}] in [{1}]'.format(ref.Name(), getattr(ref, ref_scope_name))
    else:
      item = '[{0}]'.format(ref.Name())
    prompt_list.append(item)

  PromptForDeletionHelper(resource_name, prompt_list, prompt_title=prompt_title)


def PromptForDeletionHelper(resource_name, prompt_list, prompt_title=None):
  prompt_title = (prompt_title or
                  'The following {0} will be deleted:'.format(resource_name))
  prompt_message = ConstructList(prompt_title, prompt_list)
  if not console_io.PromptContinue(message=prompt_message):
    raise calliope_exceptions.ToolException('Deletion aborted by user.')


def BytesToGb(size):
  """Converts a disk size in bytes to GB."""
  if not size:
    return None

  if size % constants.BYTES_IN_ONE_GB != 0:
    raise calliope_exceptions.ToolException(
        'Disk size must be a multiple of 1 GB. Did you mean [{0}GB]?'
        .format(size // constants.BYTES_IN_ONE_GB + 1))

  return size // constants.BYTES_IN_ONE_GB


def BytesToMb(size):
  """Converts a disk size in bytes to MB."""
  if not size:
    return None

  if size % constants.BYTES_IN_ONE_MB != 0:
    raise calliope_exceptions.ToolException(
        'Disk size must be a multiple of 1 MB. Did you mean [{0}MB]?'
        .format(size // constants.BYTES_IN_ONE_MB + 1))

  return size // constants.BYTES_IN_ONE_MB


def WarnIfDiskSizeIsTooSmall(size_gb, disk_type):
  """Writes a warning message if the given disk size is too small."""
  if not size_gb:
    return

  if disk_type and 'pd-ssd' in disk_type:
    warning_threshold_gb = constants.SSD_DISK_PERFORMANCE_WARNING_GB
  else:
    warning_threshold_gb = constants.STANDARD_DISK_PERFORMANCE_WARNING_GB

  if size_gb < warning_threshold_gb:
    log.warning(
        'You have selected a disk size of under [%sGB]. This may result in '
        'poor I/O performance. For more information, see: '
        'https://developers.google.com/compute/docs/disks#performance.',
        warning_threshold_gb)


def IsValidIPV4(ip):
  """Accepts an ipv4 address in string form and returns True if valid."""
  match = re.match(r'^(\d{1,3})\.(\d{1,3})\.(\d{1,3})\.(\d{1,3})$', ip)
  if not match:
    return False

  octets = [int(x) for x in match.groups()]

  # first octet must not be 0
  if octets[0] == 0:
    return False

  for n in octets:
    if n < 0 or n > 255:
      return False

  return True


def IPV4Argument(value):
  """Argparse argument type that checks for a valid ipv4 address."""
  if not IsValidIPV4(value):
    raise argparse.ArgumentTypeError("invalid ipv4 value: '{0}'".format(value))

  return value


def MakeGetUriFunc():
  return lambda x: x['selfLink']


def GetListPager(client, request, get_value_fn):
  """Returns the paged results for request from client.

  Args:
    client: The client object.
    request: The request.
    get_value_fn: Called to extract a value from an additionlProperties list
      item.

  Returns:
    The list of request results.
  """

  def _GetNextListPage():
    response = client.AggregatedList(request)
    items = []
    for item in response.items.additionalProperties:
      items += get_value_fn(item)
    return items, response.nextPageToken

  results, next_page_token = _GetNextListPage()
  while next_page_token:
    request.pageToken = next_page_token
    page, next_page_token = _GetNextListPage()
    results += page
  return results
