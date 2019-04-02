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
"""Code that's shared between multiple url-maps subcommands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals


def ResolveUrlMapDefaultService(args, backend_service_arg, url_map_ref,
                                resources):
  """Parses the backend service that is pointed to by a URL map from args.

  This function handles parsing a regional/global backend service that is
  pointed to by a regional/global URL map.

  Args:
    args: The arguments provided to the url-maps command
    backend_service_arg: The ResourceArgument specification for the
                         backend service argument.
    url_map_ref: The resource reference to the URL MAP. This is returned by
                 parsing the URL map arguments provided.
    resources: ComputeApiHolder resources.

  Returns:
    Backend service reference parsed from args.
  """

  if IsRegionalUrlMapRef(url_map_ref):
    if not getattr(args, 'default_service_region', None):
      args.default_service_region = url_map_ref.region
  else:
    if not getattr(args, 'global_default_service', None):
      args.global_default_service = args.default_service

  return backend_service_arg.ResolveAsResource(args, resources)


def IsRegionalUrlMapRef(url_map_ref):
  """Returns True if the URL Map reference is regional."""

  return url_map_ref.Collection() == 'compute.regionUrlMaps'


def IsGlobalUrlMapRef(url_map_ref):
  """Returns True if the URL Map reference is global."""

  return url_map_ref.Collection() == 'compute.urlMaps'
