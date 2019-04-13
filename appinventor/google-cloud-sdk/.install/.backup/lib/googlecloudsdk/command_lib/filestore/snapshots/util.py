# -*- coding: utf-8 -*- #
# Copyright 2019 Google Inc. All Rights Reserved.
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
"""Common utility functions for Cloud Filestore snapshot commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.core import properties

INSTANCE_NAME_TEMPLATE = 'projects/{}/locations/{}/instances/{}'


def AddInstanceNameToRequest(ref, args, req):
  """Python hook for yaml commands to process the source instance name."""
  del ref
  project = properties.VALUES.core.project.Get(required=True)
  req.snapshot.sourceInstance = INSTANCE_NAME_TEMPLATE.format(
      project, args.instance_zone, args.instance)
  return req
