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


"""CloudBuild resource transforms and symbols dict.

A resource transform function converts a JSON-serializable resource to a string
value. This module contains built-in transform functions that may be used in
resource projection and filter expressions.

NOTICE: Each TransformFoo() method is the implementation of a foo() transform
function. Even though the implementation here is in Python the usage in resource
projection and filter expressions is language agnostic. This affects the
Pythonicness of the Transform*() methods:
  (1) The docstrings are used to generate external user documentation.
  (2) The method prototypes are included in the documentation. In particular the
      prototype formal parameter names are stylized for the documentation.
  (3) The 'r', 'kwargs', and 'projection' args are not included in the external
      documentation. Docstring descriptions, other than the Args: line for the
      arg itself, should not mention these args. Assume the reader knows the
      specific item the transform is being applied to. When in doubt refer to
      the output of $ gcloud topic projections.
  (4) The types of some args, like r, are not fixed until runtime. Other args
      may have either a base type value or string representation of that type.
      It is up to the transform implementation to silently do the string=>type
      conversions. That's why you may see e.g. int(arg) in some of the methods.
  (5) Unless it is documented to do so, a transform function must not raise any
      exceptions related to the resource r. The `undefined' arg is used to
      handle all unusual conditions, including ones that would raise exceptions.
      Exceptions for arguments explicitly under the caller's control are OK.
"""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from apitools.base.py import encoding as apitools_encoding
from googlecloudsdk.api_lib.util import apis as core_apis
from googlecloudsdk.core import resources


def TransformBuildImages(r, undefined=''):
  """Returns the formatted build results images.

  Args:
    r: JSON-serializable object.
    undefined: Returns this value if the resource cannot be formatted.
  Returns:
    The formatted build results images.
  """
  messages = core_apis.GetMessagesModule('cloudbuild', 'v1')
  b = apitools_encoding.DictToMessage(r, messages.Build)
  if b.results is None:
    return undefined
  images = b.results.images
  if not images:
    return undefined
  names = []
  for i in images:
    if i.name is None:
      names.append(undefined)
    else:
      names.append(i.name)
  if len(names) > 1:
    return names[0] + ' (+{0} more)'.format(len(names)-1)
  return names[0]


def TransformBuildSource(r, undefined=''):
  """Returns the formatted build source.

  Args:
    r: JSON-serializable object.
    undefined: Returns this value if the resource cannot be formatted.
  Returns:
    The formatted build source.
  """
  messages = core_apis.GetMessagesModule('cloudbuild', 'v1')
  b = apitools_encoding.DictToMessage(r, messages.Build)
  if b.source is None:
    return undefined
  storage_source = b.source.storageSource
  repo_source = b.source.repoSource
  if storage_source is not None:
    bucket = storage_source.bucket
    obj = storage_source.object
    if bucket is None or obj is None:
      return undefined
    return 'gs://{0}/{1}'.format(bucket, obj)
  if repo_source is not None:
    repo_name = repo_source.repoName or 'default'
    branch_name = repo_source.branchName
    if branch_name is not None:
      return '{0}@{1}'.format(repo_name, branch_name)
    tag_name = repo_source.tagName
    if tag_name is not None:
      return '{0}@{1}'.format(repo_name, tag_name)
    commit_sha = repo_source.commitSha
    if commit_sha is not None:
      return '{0}@{1}'.format(repo_name, commit_sha)
  return undefined


def _GetUri(resource, undefined=None):
  build_ref = resources.REGISTRY.Parse(
      None,
      params={
          'projectId': resource.projectId,
          'id': resource.id,
      },
      collection='cloudbuild.projects.builds')
  return build_ref.SelfLink() or undefined


_TRANSFORMS = {
    'build_images': TransformBuildImages,
    'build_source': TransformBuildSource,
    'uri': _GetUri,
}


def GetTransforms():
  """Returns the cloudbuild specific resource transform symbol table."""
  return _TRANSFORMS
