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
"""'functions deploy' utilities for function source code."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import os
import random
import re
import string

from apitools.base.py import http_wrapper
from apitools.base.py import transfer


from googlecloudsdk.api_lib.functions import exceptions
from googlecloudsdk.api_lib.functions import util as api_util
from googlecloudsdk.api_lib.storage import storage_api
from googlecloudsdk.api_lib.storage import storage_util
from googlecloudsdk.calliope import exceptions as calliope_exceptions
from googlecloudsdk.command_lib.util import gcloudignore
from googlecloudsdk.core import http as http_utils
from googlecloudsdk.core import log
from googlecloudsdk.core import properties
from googlecloudsdk.core.util import archive
from googlecloudsdk.core.util import files as file_utils
from six.moves import range


def _GcloudIgnoreCreationPredicate(directory):
  return gcloudignore.AnyFileOrDirExists(
      directory, gcloudignore.GIT_FILES + ['node_modules'])


def _GetChooser(path):
  default_ignore_file = gcloudignore.DEFAULT_IGNORE_FILE + '\nnode_modules\n'

  return gcloudignore.GetFileChooserForDir(
      path, default_ignore_file=default_ignore_file,
      gcloud_ignore_creation_predicate=_GcloudIgnoreCreationPredicate)


def _ValidateUnpackedSourceSize(path):
  """Validate size of unpacked source files."""
  chooser = _GetChooser(path)
  predicate = chooser.IsIncluded
  try:
    size_b = file_utils.GetTreeSizeBytes(path, predicate=predicate)
  except OSError as e:
    raise exceptions.FunctionsError(
        'Error building source archive from path [{path}]. '
        'Could not validate source files: [{error}]. '
        'Please ensure that path [{path}] contains function code or '
        'specify another directory with --source'.format(path=path, error=e))
  size_limit_mb = 512
  size_limit_b = size_limit_mb * 2 ** 20
  if size_b > size_limit_b:
    raise exceptions.OversizedDeployment(
        str(size_b) + 'B', str(size_limit_b) + 'B')


def _CreateSourcesZipFile(zip_dir, source_path):
  """Prepare zip file with source of the function to upload.

  Args:
    zip_dir: str, directory in which zip file will be located. Name of the file
             will be `fun.zip`.
    source_path: str, directory containing the sources to be zipped.
  Returns:
    Path to the zip file (str).
  Raises:
    FunctionsError
  """
  api_util.ValidateDirectoryExistsOrRaiseFunctionError(source_path)
  _ValidateUnpackedSourceSize(source_path)
  zip_file_name = os.path.join(zip_dir, 'fun.zip')
  try:
    chooser = _GetChooser(source_path)
    predicate = chooser.IsIncluded
    archive.MakeZipFromDir(zip_file_name, source_path, predicate=predicate)
  except ValueError as e:
    raise exceptions.FunctionsError(
        'Error creating a ZIP archive with the source code '
        'for directory {0}: {1}'.format(source_path, str(e)))
  return zip_file_name


def _GenerateRemoteZipFileName(function_name):
  suffix = ''.join(random.choice(string.ascii_lowercase) for _ in range(12))
  return '{0}-{1}-{2}.zip'.format(
      properties.VALUES.functions.region.Get(), function_name, suffix)


def _UploadFileToGcs(source, function_ref, stage_bucket):
  """Upload local source files to GCS staging bucket."""
  zip_file = _GenerateRemoteZipFileName(function_ref.RelativeName())
  bucket_ref = storage_util.BucketReference.FromArgument(
      stage_bucket)
  dest_object = storage_util.ObjectReference.FromBucketRef(bucket_ref, zip_file)

  # TODO(b/109938541): Remove gsutil implementation after the new implementation
  # seems stable.
  use_gsutil = properties.VALUES.storage.use_gsutil.GetBool()
  if use_gsutil:
    upload_success = _UploadFileToGcsGsutil(source, dest_object)
  else:
    upload_success = _UploadFileToGcsStorageApi(source, dest_object)

  if not upload_success:
    raise exceptions.FunctionsError(
        'Failed to upload the function source code to the bucket {0}'
        .format(stage_bucket))
  return dest_object.ToUrl()


def _UploadFileToGcsGsutil(source, dest_object):
  """Upload local source files to GCS staging bucket. Returns upload success."""
  ret_code = storage_util.RunGsutilCommand(
      'cp', [source, dest_object.ToUrl()])
  return ret_code == 0


def _UploadFileToGcsStorageApi(source, dest_object):
  """Upload local source files to GCS staging bucket. Returns upload success."""
  client = storage_api.StorageClient()
  try:
    client.CopyFileToGCS(source, dest_object)
    return True
  except calliope_exceptions.BadFileException:
    return False


def _AddDefaultBranch(source_archive_url):
  cloud_repo_pattern = (r'^https://source\.developers\.google\.com'
                        r'/projects/[^/]+'
                        r'/repos/[^/]+$')
  if re.match(cloud_repo_pattern, source_archive_url):
    return source_archive_url + '/moveable-aliases/master'
  return source_archive_url


def _GetUploadUrl(messages, service, function_ref):
  request = (messages.
             CloudfunctionsProjectsLocationsFunctionsGenerateUploadUrlRequest)(
                 parent='projects/{}/locations/{}'.format(
                     function_ref.projectsId, function_ref.locationsId))
  response = service.GenerateUploadUrl(request)
  return response.uploadUrl


def _CheckUploadStatus(status_code):
  """Validates that HTTP status for upload is 2xx."""
  return status_code // 100 == 2


def _UploadFileToGeneratedUrl(source, messages, service, function_ref):
  """Upload function source to URL generated by API."""
  url = _GetUploadUrl(messages, service, function_ref)
  upload = transfer.Upload.FromFile(source,
                                    mime_type='application/zip')
  upload_request = http_wrapper.Request(
      url, http_method='PUT', headers={
          'content-type': 'application/zip',
          # Magic header, request will fail without it.
          # Not documented at the moment this comment was being written.
          'x-goog-content-length-range': '0,104857600',
          'Content-Length': '{0:d}'.format(upload.total_size)})
  upload_request.body = upload.stream.read()
  response = http_wrapper.MakeRequest(
      http_utils.Http(), upload_request, retry_func=upload.retry_func,
      retries=upload.num_retries)
  if not _CheckUploadStatus(response.status_code):
    raise exceptions.FunctionsError(
        'Failed to upload the function source code to signed url: {url}. '
        'Status: [{code}:{detail}]'.format(url=url,
                                           code=response.status_code,
                                           detail=response.content))
  return url


def UploadFile(source, stage_bucket, messages, service, function_ref):
  if stage_bucket:
    return _UploadFileToGcs(source, function_ref, stage_bucket)
  return _UploadFileToGeneratedUrl(source, messages, service, function_ref)


def SetFunctionSourceProps(function, function_ref, source_arg, stage_bucket):
  """Add sources to function.

  Args:
    function: The function to add a source to.
    function_ref: The reference to the function.
    source_arg: Location of source code to deploy.
    stage_bucket: The name of the Google Cloud Storage bucket where source code
        will be stored.
  Returns:
    A list of fields on the function that have been changed.
  """
  function.sourceArchiveUrl = None
  function.sourceRepository = None
  function.sourceUploadUrl = None

  messages = api_util.GetApiMessagesModule()

  if source_arg is None:
    source_arg = '.'
  source_arg = source_arg or '.'
  if source_arg.startswith('gs://'):
    if not source_arg.endswith('.zip'):
      # Users may have .zip archives with unusual names, and we don't want to
      # prevent those from being deployed; the deployment should go through so
      # just warn here.
      log.warning(
          '[{}] does not end with extension `.zip`. '
          'The `--source` argument must designate the zipped source archive '
          'when providing a Google Cloud Storage URI.'.format(source_arg))
    function.sourceArchiveUrl = source_arg
    return ['sourceArchiveUrl']
  elif source_arg.startswith('https://'):
    function.sourceRepository = messages.SourceRepository(
        url=_AddDefaultBranch(source_arg)
    )
    return ['sourceRepository']
  with file_utils.TemporaryDirectory() as tmp_dir:
    zip_file = _CreateSourcesZipFile(tmp_dir, source_arg)
    service = api_util.GetApiClientInstance().projects_locations_functions

    upload_url = UploadFile(
        zip_file, stage_bucket, messages, service, function_ref)
    if upload_url.startswith('gs://'):
      function.sourceArchiveUrl = upload_url
      return ['sourceArchiveUrl']
    else:
      function.sourceUploadUrl = upload_url
      return ['sourceUploadUrl']
