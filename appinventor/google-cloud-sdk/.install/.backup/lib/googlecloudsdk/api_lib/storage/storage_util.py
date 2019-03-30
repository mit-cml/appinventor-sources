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

"""Utilities for interacting with Google Cloud Storage."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import argparse
import os
import re
import string

from googlecloudsdk.api_lib.util import apis as core_apis
from googlecloudsdk.core import config
from googlecloudsdk.core import exceptions
from googlecloudsdk.core import execution_utils
from googlecloudsdk.core import log
from googlecloudsdk.core import resources
from googlecloudsdk.core.util import files as file_utils
from googlecloudsdk.core.util import platforms


GSUTIL_BUCKET_PREFIX = 'gs://'


class Error(exceptions.Error):
  """Base class for exceptions in this module."""


class GsutilError(Error):
  """Exception raised when gsutil cannot be found."""


class InvalidNameError(ValueError):
  """Error indicating that a given name is invalid."""

  def __init__(self, name, reason, type_name, url):
    super(InvalidNameError, self).__init__(
        ('Invalid {type} name [{name}]: {reason}\n\n'
         'See {url} for details.').format(name=name, reason=reason,
                                          type=type_name, url=url))


class InvalidBucketNameError(InvalidNameError):
  """Error indicating that a given bucket name is invalid."""
  TYPE = 'bucket'
  URL = 'https://cloud.google.com/storage/docs/naming#requirements'

  def __init__(self, name, reason):
    super(InvalidBucketNameError, self).__init__(
        name, reason, self.TYPE, self.URL)


class InvalidObjectNameError(InvalidNameError):
  """Error indicating that a given object name is invalid."""
  TYPE = 'object'
  URL = 'https://cloud.google.com/storage/docs/naming#objectnames'

  def __init__(self, name, reason):
    super(InvalidObjectNameError, self).__init__(
        name, reason, self.TYPE, self.URL)


VALID_BUCKET_CHARS_MESSAGE = """\
Bucket names must contain only lowercase letters, numbers, dashes (-), \
underscores (_), and dots (.)."""
VALID_BUCKET_START_END_MESSAGE = """\
Bucket names must start and end with a number or letter."""
VALID_BUCKET_LENGTH_MESSAGE = """\
Bucket names must contain 3 to 63 characters. \
Names containing dots can contain up to 222 characters, but each \
dot-separated component can be no longer than 63 characters."""
VALID_BUCKET_DOTTED_DECIMAL_MESSAGE = """\
Bucket names cannot be represented as an IP address in dotted-decimal \
notation (for example, 192.168.5.4)."""


VALID_OBJECT_LENGTH_MESSAGE = """\
Object names can contain any sequence of valid Unicode characters, \
of length 1-1024 bytes when UTF-8 encoded."""
VALID_OBJECT_CHARS_MESSAGE = """\
Object names must not contain Carriage Return or Line Feed characters."""


def _ValidateBucketName(name):
  """Validate the given bucket name according to the naming requirements.

  See https://cloud.google.com/storage/docs/naming#requirements

  Args:
    name: the name of the bucket, not including 'gs://'

  Raises:
    InvalidBucketNameError: if the given bucket name is invalid
  """
  components = name.split('.')
  if not (3 <= len(name) <= 222) or any(len(c) > 63 for c in components):
    raise InvalidBucketNameError(name, VALID_BUCKET_LENGTH_MESSAGE)

  if set(name) - set(string.ascii_lowercase + string.digits + '-_.'):
    raise InvalidBucketNameError(name, VALID_BUCKET_CHARS_MESSAGE)

  if set(name[0] + name[-1]) - set(string.ascii_lowercase + string.digits):
    raise InvalidBucketNameError(name, VALID_BUCKET_START_END_MESSAGE)

  if len(components) == 4 and ''.join(components).isdigit():
    raise InvalidBucketNameError(name, VALID_BUCKET_DOTTED_DECIMAL_MESSAGE)

  # Not validating the following guidelines, since Google can create such
  # buckets and they may be read from:
  # - Bucket names cannot begin with the "goog" prefix.
  # - Bucket names cannot contain "google" or close misspellings of "google".

  # Not validating the following guideline, because it seems to be a guideline
  # and not a requirement:
  # - Also, for DNS compliance and future compatibility, you should not use
  #   underscores (_) or have a period adjacent to another period or dash. For
  #   example, ".." or "-." or ".-" are not valid in DNS names.


def _ValidateBucketUrl(url):
  # These are things that cause unhelpful error messages during parsing, so we
  # check for them here.
  if url.startswith(GSUTIL_BUCKET_PREFIX):
    name = url[len(GSUTIL_BUCKET_PREFIX):]
  else:
    name = url
  _ValidateBucketName(name.rstrip('/'))


class BucketReference(object):
  """A wrapper class to make working with GCS bucket names easier."""

  def __init__(self, bucket):
    """Creates a BucketReference.

    Args:
      bucket: str, The bucket name
    """
    self.bucket = bucket

  @classmethod
  def FromMessage(cls, bucket):
    """Create a bucket reference from a bucket message from the API."""
    return cls(bucket.name)

  @classmethod
  def FromUrl(cls, url):
    """Parse a bucket URL ('gs://' optional) into a BucketReference."""
    return cls(resources.REGISTRY.Parse(url, collection='storage.buckets')
               .bucket)

  @classmethod
  def FromArgument(cls, value, require_prefix=True):
    """Validates that the argument is a reference to a Cloud Storage bucket."""
    if require_prefix and not value.startswith(GSUTIL_BUCKET_PREFIX):
      raise argparse.ArgumentTypeError(
          'Must be a valid Google Cloud Storage bucket of the form '
          '[gs://somebucket]')

    try:
      _ValidateBucketUrl(value)
    except InvalidBucketNameError as err:
      raise argparse.ArgumentTypeError(str(err))

    return cls.FromUrl(value)

  def ToUrl(self):
    return 'gs://{}'.format(self.bucket)

  def GetPublicUrl(self):
    return 'https://storage.googleapis.com/{0}'.format(self.bucket)

  def __eq__(self, other):
    return self.bucket == other.bucket

  def __ne__(self, other):
    return not self.__eq__(other)

  def __hash__(self):
    return hash(self.bucket)


class ObjectReference(object):
  """Wrapper class to make working with Cloud Storage bucket/objects easier."""

  GSUTIL_OBJECT_REGEX = r'^gs://(?P<bucket>[^/]+)/(?P<object>.+)'
  GSUTIL_BUCKET_REGEX = r'^gs://(?P<bucket>[^/]+)/?'

  def __init__(self, bucket, name):
    self.bucket = bucket
    self.name = name

  @property
  def object(self):
    """Emulates the object field on the object core/resource ref."""
    return self.name

  @property
  def bucket_ref(self):
    """Gets a bucket reference for the bucket this object is in."""
    return BucketReference(self.bucket)

  @classmethod
  def FromMessage(cls, obj):
    """Create an object reference from an object message from the API."""
    return cls(obj.bucket, obj.name)

  @classmethod
  def FromName(cls, bucket, name):
    """Create an object reference after ensuring the name is valid."""
    _ValidateBucketName(bucket)
    # TODO(b/118379726): Fully implement the object naming requirement checks.
    # See https://cloud.google.com/storage/docs/naming#objectnames
    if not 0 <= len(name.encode('utf8')) <= 1024:
      raise InvalidObjectNameError(name, VALID_OBJECT_LENGTH_MESSAGE)
    if '\r' in name or '\n' in name:
      raise InvalidObjectNameError(name, VALID_OBJECT_CHARS_MESSAGE)
    return cls(bucket, name)

  @classmethod
  def FromBucketRef(cls, bucket_ref, name):
    """Create an object reference from a bucket reference and a name."""
    return cls.FromName(bucket_ref.bucket, name)

  @classmethod
  def FromUrl(cls, url, allow_empty_object=False):
    """Parse an object URL ('gs://' required) into an ObjectReference."""
    match = re.match(cls.GSUTIL_OBJECT_REGEX, url, re.DOTALL)
    if match:
      return cls.FromName(match.group('bucket'), match.group('object'))
    match = re.match(cls.GSUTIL_BUCKET_REGEX, url, re.DOTALL)
    if match:
      if allow_empty_object:
        return cls(match.group('bucket'), '')
      else:
        raise InvalidObjectNameError('', 'Empty object name is not allowed')
    raise ValueError('Must be of form gs://bucket/object')

  @classmethod
  def FromArgument(cls, url, allow_empty_object=False):
    try:
      return cls.FromUrl(url, allow_empty_object=allow_empty_object)
    except (InvalidObjectNameError, ValueError) as err:
      raise argparse.ArgumentTypeError(str(err))

  @classmethod
  def IsStorageUrl(cls, path):
    try:
      cls.FromUrl(path)
    except ValueError:
      return False
    return True

  def ToUrl(self):
    return 'gs://{}/{}'.format(self.bucket, self.name)

  def GetPublicUrl(self):
    return 'https://storage.googleapis.com/{}/{}'.format(self.bucket, self.name)

  def __eq__(self, other):
    return self.ToUrl() == other.ToUrl()

  def __ne__(self, other):
    return not self.__eq__(other)

  def __hash__(self):
    return hash(self.ToUrl())


def GetMessages():
  """Import and return the appropriate storage messages module."""
  return core_apis.GetMessagesModule('storage', 'v1')


def GetClient():
  """Import and return the appropriate storage client."""
  return core_apis.GetClientInstance('storage', 'v1')


def _GetGsutilPath():
  """Determines the path to the gsutil binary."""
  sdk_bin_path = config.Paths().sdk_bin_path
  if not sdk_bin_path:
    # Check if gsutil is located on the PATH.
    gsutil_path = file_utils.FindExecutableOnPath('gsutil')
    if gsutil_path:
      log.debug('Using gsutil found at [{path}]'.format(path=gsutil_path))
      return gsutil_path
    else:
      raise GsutilError('A path to the storage client `gsutil` could not be '
                        'found. Please check your SDK installation.')
  return os.path.join(sdk_bin_path, 'gsutil')


def RunGsutilCommand(command_name,
                     command_args=None,
                     run_concurrent=False,
                     out_func=log.file_only_logger.debug,
                     err_func=log.file_only_logger.debug):
  """Runs the specified gsutil command and returns the command's exit code.

  WARNING: This is not compatible with python 3 and should no longer be used.

  Args:
    command_name: The gsutil command to run.
    command_args: List of arguments to pass to the command.
    run_concurrent: Whether concurrent uploads should be enabled while running
      the command.
    out_func: str->None, a function to call with the stdout of the gsutil
        command.
    err_func: str->None, a function to call with the stderr of the gsutil
        command.

  Returns:
    The exit code of the call to the gsutil command.
  """
  command_path = _GetGsutilPath()

  args = ['-m', command_name] if run_concurrent else [command_name]
  if command_args is not None:
    args += command_args

  if platforms.OperatingSystem.Current() == platforms.OperatingSystem.WINDOWS:
    gsutil_args = execution_utils.ArgsForCMDTool(command_path + '.cmd', *args)
  else:
    gsutil_args = execution_utils.ArgsForExecutableTool(command_path, *args)
  log.debug('Running command: [{args}]]'.format(args=' '.join(gsutil_args)))
  return execution_utils.Exec(gsutil_args, no_exit=True,
                              out_func=out_func,
                              err_func=err_func)
