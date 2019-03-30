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
"""Utils for getting filters of container analysis API occurrences request."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import image_utils
from googlecloudsdk.core import properties


def GetFilter(image_ref, holder):
  """Get the filter of occurrences request for container analysis API."""
  filters = [
      # Display only packages
      'kind = "PACKAGE_MANAGER"',
      # Display only compute metadata
      'has_prefix(resource_url,"https://www.googleapis.com/compute/")',
  ]
  client = holder.client
  resource_parser = holder.resources

  if image_ref:
    image_expander = image_utils.ImageExpander(client, resource_parser)
    self_link, image = image_expander.ExpandImageFlag(
        user_project=properties.VALUES.core.project.Get(),
        image=image_ref.image,
        image_project=image_ref.project,
        return_image_resource=True
        )

    image_url = self_link+'/id/'+str(image.id)
    filters.append('has_prefix(resource_url,"{}")'.format(image_url))

  return ' AND '.join(filters)
