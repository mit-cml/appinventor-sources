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
"""Command for listing images."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.api_lib.containeranalysis import util as containeranalysis_util
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.compute.images.packages import exceptions
from googlecloudsdk.command_lib.compute.images.packages import filter_utils
from googlecloudsdk.command_lib.compute.images.packages import flags as package_flags
from googlecloudsdk.core import properties


class List(base.ListCommand):
  """List the packages in an image.
  """

  @staticmethod
  def Args(parser):
    parser.display_info.AddFormat("""\
        table(
          name:label=PACKAGE,
          version:label=VERSION,
          revision:label=REVISION
        )""")
    # Add resource flag for image.
    package_flags.AddImageResourceArg(parser)

  def _GetPackageVersions(self, image_packages, image_name):
    package_versions = []
    for occurrence in image_packages:
      package_name = occurrence.installation.name
      for location in occurrence.installation.location:
        package_version = {'name': package_name,
                           'version': location.version.name,
                           'revision': location.version.revision}
        package_versions.append(package_version)

    if not package_versions:
      raise exceptions.ImagePackagesInfoUnavailableException(image_name)

    return sorted(package_versions,
                  key=lambda package_version: package_version['name'])

  def Run(self, args):
    """Yields filtered packages."""
    project = properties.VALUES.core.project.Get()
    image_ref = args.CONCEPTS.image.Parse()

    # Use GA to construct the compute API holder since the containeranalysis
    # API always call compute v1 API to refer the compute resources.
    holder = base_classes.ComputeApiHolder(base.ReleaseTrack.GA)
    resource_filter = filter_utils.GetFilter(image_ref, holder)

    image_packages = containeranalysis_util.MakeOccurrenceRequest(
        project_id=project, resource_filter=resource_filter,
        occurrence_filter=None, resource_urls=None)

    return self._GetPackageVersions(image_packages, args.image)
