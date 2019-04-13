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
"""Utilities for cloud resources."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import re

from googlecloudsdk.core import exceptions


class CollectionInfo(object):
  """Holds information about a resource collection.

  Attributes:
      api_name: str, name of the api of resources parsed by this parser.
      api_version: str, version id for this api.
      path: str, Atomic URI template for this resource.
      flat_paths: {name->path}, Named detailed URI templates for this resource.
          If there is an entry ''->path it replaces path and corresponding param
          attributes for resources parsing. path and params are not used
          in this case.
          Also note that key in this dictionary is referred as subcollection,
          as it extends 'name' attribute.
      params: list(str), description of parameters in the path.
      name: str, collection name for this resource without leading api_name.
      base_url: str, URL for service providing these resources.
      docs_url: str, URL to the API reference docs for this API.
  """

  def __init__(self, api_name, api_version, base_url, docs_url, name,
               path, flat_paths, params, enable_uri_parsing=True):
    self.api_name = api_name
    self.api_version = api_version
    self.base_url = base_url
    self.docs_url = docs_url
    self.name = name
    self.path = path
    self.flat_paths = flat_paths
    self.params = params
    self.enable_uri_parsing = enable_uri_parsing

  @property
  def full_name(self):
    return self.api_name + '.'  + self.name

  def GetSubcollection(self, collection_name):
    name = self.full_name
    # collection_name could be equal to name in which case subcollection is
    # empty string or have additional suffix .subcollection.
    if collection_name.startswith(name):
      return collection_name[len(name) + 1:]
    raise KeyError('{0} does not exist in {1}'.format(collection_name, name))

  def GetPathRegEx(self, subcollection):
    """Returns regex for matching path template."""
    path = self.GetPath(subcollection)
    parts = []
    prev_end = 0
    for match in re.finditer('({[^}]+}/)|({[^}]+})$', path):
      parts.append(path[prev_end:match.start()])
      parts.append('([^/]+)')
      if match.group(1):
        parts.append('/')
      prev_end = match.end()
    if prev_end == len(path):
      parts[-1] = '(.*)$'
    return ''.join(parts)

  def GetParams(self, subcollection):
    """Returns ordered list of parameters for given subcollection.

    Args:
      subcollection: str, key name for flat_paths. If self.flat_paths is empty
          use '' (or any other falsy value) for subcollection to get default
          path parameters.
    Returns:
      Paramaters present in specified subcollection path.
    Raises:
      KeyError if given subcollection does not exists.
    """
    # If default collection requested and we are not using custom paths.
    if not subcollection and not self.flat_paths:
      return self.params
    return GetParamsFromPath(self.flat_paths[subcollection])

  def GetPath(self, subcollection):
    """Returns uri template path for given subcollection."""
    # If default collection requested and we are not using custom paths.
    if not subcollection and not self.flat_paths:
      return self.path
    return self.flat_paths[subcollection]

  def __eq__(self, other):
    return (self.api_name == other.api_name and
            self.api_version == other.api_version and
            self.name == other.name)

  def __ne__(self, other):
    return not self == other

  @classmethod
  def _CmpHelper(cls, x, y):
    """Just a helper equivalent to the cmp() function in Python 2."""
    return (x > y) - (x < y)

  def __lt__(self, other):
    return self._CmpHelper((self.api_name, self.api_version, self.name),
                           (other.api_name, other.api_version, other.name)) < 0

  def __gt__(self, other):
    return self._CmpHelper((self.api_name, self.api_version, self.name),
                           (other.api_name, other.api_version, other.name)) > 0

  def __le__(self, other):
    return not self.__gt__(other)

  def __ge__(self, other):
    return not self.__lt__(other)

  def __str__(self):
    return self.full_name


class InvalidEndpointException(exceptions.Error):
  """Exception for when an API endpoint is malformed."""

  def __init__(self, url):
    super(InvalidEndpointException, self).__init__(
        "URL does not start with 'http://' or 'https://' [{0}]".format(url))


def SplitDefaultEndpointUrl(url):
  """Returns api_name, api_version, resource_path tuple for a default api url.

  Supports five formats:
  http(s)://www.googleapis.com/api/version/resource-path
  http(s)://www-googleapis-staging.sandbox.google.com/api/version/resource-path
  http(s)://api.googleapis.com/version/resource-path
  http(s)://someotherdoman/api/version/resource-path
  http(s)://api.googleapis.com/apis/kube-api.name/version/resource-path


  If there is an api endpoint override defined that maches the url,
  that api name will be returned.

  Args:
    url: str, The resource url.

  Returns:
    (str, str, str): The API name, version, resource_path.
    For a malformed URL, the return value for API name is undefined, version is
    None, and resource path is ''.
  """
  tokens = _StripUrl(url).split('/')
  domain = tokens[0]
  resource_path = ''
  if ('googleapis' not in domain
      or domain.startswith('www.') or domain.startswith('www-')):
    if len(tokens) > 1:
      api_name = tokens[1]
    else:
      api_name = None
    if len(tokens) > 2:
      version = tokens[2]
    else:
      version = None
    resource_path = '/'.join(tokens[3:])
  else:
    api_name = tokens[0].split('.')[0]
    if len(tokens) > 1:
      if tokens[1] == 'apis':
        if len(tokens) > 3:
          # kubernetes api name is tokens[2]
          version = tokens[3]
          resource_path = '/'.join(tokens[4:])
        else:
          version = None
      else:
        version = tokens[1]
        resource_path = '/'.join(tokens[2:])
    else:
      version = None
  return api_name, version, resource_path


def GetParamsFromPath(path):
  """Extract parameters from uri template path.

    See https://tools.ietf.org/html/rfc6570. This function makes simplifying
    assumption that all parameter names are surrounded by /{ and }/.

  Args:
    path: str, uri template path.
  Returns:
    list(str), list of parameters in the template path.
  """
  path = path.split(':')[0]
  parts = path.split('/')
  params = []
  for part in parts:
    if part.startswith('{') and part.endswith('}'):
      part = part[1:-1]
      if part.startswith('+'):
        params.append(part[1:])
      else:
        params.append(part)
  return params


def _StripUrl(url):
  """Strip a http: or https: prefix, then strip leading and trailing slashes."""
  if url.startswith('https://') or url.startswith('http://'):
    return url[url.index(':') + 1:].strip('/')
  raise InvalidEndpointException(url)
