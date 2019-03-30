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
"""Command for diffing image packages."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.compute import base_classes
from googlecloudsdk.api_lib.containeranalysis import util as containeranalysis_util
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.compute.images.packages import exceptions
from googlecloudsdk.command_lib.compute.images.packages import filter_utils
from googlecloudsdk.command_lib.compute.images.packages import flags as package_flags


class Diff(base.ListCommand):
  """ Displays the version differences of packages between two images."""

  @staticmethod
  def Args(parser):
    parser.display_info.AddFormat("""\
       table(
          name:label=PACKAGE,
          version_base:label=VERSION_BASE,
          revision_base:label=REVISION_BASE,
          version_diff:label=VERSION_DIFF,
          revision_diff:label=REVISION_DIFF
        )""")
    # Add resource flags.
    package_flags.AddResourceArgs(parser)
    # Add filter flags.
    package_flags.AddShowAddedPackagesFlag(parser)
    package_flags.AddShowRemovedPackagesFlag(parser)
    package_flags.AddShowUpdatedPackagesFlag(parser)

    Diff._parser = parser

  def _GetVersions(self, image_packages, image_name):
    package_versions = {}
    for occurrence in image_packages:
      package_name = occurrence.installation.name
      versions = []
      for location in occurrence.installation.location:
        versions.append((location.version.name, location.version.revision))
      package_versions[package_name] = versions

    if not package_versions:
      raise exceptions.ImagePackagesInfoUnavailableException(image_name)

    return package_versions

  def _GetDiff(self, args, package_versions_base, package_versions_diff):
    all_package_names = set(package_versions_base.keys()).union(
        set(package_versions_diff.keys()))

    show_all_diff_packages = True
    if (args.show_added_packages or args.show_removed_packages or
        args.show_updated_packages):
      show_all_diff_packages = False

    diff = []
    empty = ('-', '-')
    for package_name in all_package_names:
      versions_base = package_versions_base.get(package_name, [])
      versions_diff = package_versions_diff.get(package_name, [])
      if set(versions_base) != set(versions_diff):
        len_base = len(versions_base)
        len_diff = len(versions_diff)
        if (show_all_diff_packages or
            (args.show_added_packages and len_base == 0 and len_diff != 0) or
            (args.show_removed_packages and len_base != 0 and len_diff == 0) or
            (args.show_updated_packages and len_base != 0 and len_diff != 0)):
          for idx in range(max(len_base, len_diff)):
            version_base, revision_base = versions_base[idx] if (
                idx < len_base) else empty
            version_diff, revision_diff = versions_diff[idx] if (
                idx < len_diff) else empty
            package_diff = {
                'name': package_name,
                'version_base': version_base,
                'revision_base': revision_base,
                'version_diff': version_diff,
                'revision_diff': revision_diff
            }
            diff.append(package_diff)

    return sorted(diff, key=lambda package_diff: package_diff['name'])

  def Run(self, args):
    """Yields the differences of packages between two images."""
    # If not specified, both base project and diff project are the user project.

    base_image_ref = args.CONCEPTS.base_image.Parse()
    diff_image_ref = args.CONCEPTS.diff_image.Parse()

    # Use GA to construct the compute API holder since the containeranalysis
    # API always call compute v1 API to refer the compute resources.
    holder = base_classes.ComputeApiHolder(base.ReleaseTrack.GA)
    resource_filter_base = filter_utils.GetFilter(base_image_ref, holder)
    resource_filter_diff = filter_utils.GetFilter(diff_image_ref, holder)

    image_packages_base = containeranalysis_util.MakeOccurrenceRequest(
        project_id=base_image_ref.project, resource_filter=resource_filter_base,
        occurrence_filter=None, resource_urls=None)

    image_packages_diff = containeranalysis_util.MakeOccurrenceRequest(
        project_id=diff_image_ref.project, resource_filter=resource_filter_diff,
        occurrence_filter=None, resource_urls=None)

    package_versions_base = self._GetVersions(image_packages_base,
                                              args.base_image)
    package_versions_diff = self._GetVersions(image_packages_diff,
                                              args.diff_image)

    return self._GetDiff(args, package_versions_base, package_versions_diff)
