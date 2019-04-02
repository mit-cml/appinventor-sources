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
"""Common utility functions for Image Version validation."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.composer import environments_util as environments_api_util
from googlecloudsdk.api_lib.composer import image_versions_util as image_version_api_util
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.composer import util as command_util
from googlecloudsdk.core import log

# Names of possible aliases that can be used within image version strings.
LATEST = 'latest'

# Set of possible image version aliases
ALIASES = {LATEST}


class InvalidImageVersionError(command_util.Error):
  """Class for errors raised when an invalid image version is encountered."""


class _ImageVersionItem(object):
  """Class used to dissect and analyze image version components and strings."""

  def __init__(self, image_ver=None, composer_ver=None, airflow_ver=None):
    if image_ver is not None:
      iv_parts = image_ver.split('-', 4)
      self.composer_ver = iv_parts[1]
      self.airflow_ver = iv_parts[3]

    if composer_ver is not None:
      self.composer_ver = composer_ver

    if airflow_ver is not None:
      self.airflow_ver = airflow_ver

    self.contains_aliases = False
    if not ALIASES.isdisjoint({self.composer_ver, self.airflow_ver}):
      self.contains_aliases = True

  def GetImageVersionString(self):
    return 'composer-{}-airflow-{}'.format(self.composer_ver, self.airflow_ver)


def ListImageVersionUpgrades(env_ref, release_track=base.ReleaseTrack.GA):
  """List of available image version upgrades for provided env_ref."""
  env_details = environments_api_util.Get(env_ref, release_track)
  cur_image_version_id = env_details.config.softwareConfig.imageVersion
  cur_python_version = env_details.config.softwareConfig.pythonVersion

  log.status.Print(
      'Fetching list of available upgrades for image version \'{}\' ...'.format(
          cur_image_version_id))

  image_version_service = image_version_api_util.ImageVersionService(
      release_track)

  available_upgrades = []
  for version in image_version_service.List(env_ref.Parent()):
    try:
      if (_ValidateCandidateVersionStrings(cur_image_version_id,
                                           version.imageVersionId) and
          cur_python_version in version.supportedPythonVersions):
        available_upgrades.append(version)
    except InvalidImageVersionError:
      pass

  return available_upgrades


def _ValidateCandidateVersionStrings(current_image_version_id,
                                     candidate_image_version_id):
  """Determines if candidate version is a valid upgrade from current version."""
  if current_image_version_id == candidate_image_version_id:
    raise InvalidImageVersionError('Cannot upgrade to the same image version '
                                   'ID.')

  parsed_curr = _ImageVersionItem(image_ver=current_image_version_id)
  parsed_cand = _ImageVersionItem(image_ver=candidate_image_version_id)

  # Relies on API front-end to interpret aliases found within a candidate ID.
  # Candidate IDs containing aliases most likely will occur during in-place env
  # upgrade scenarios.
  if parsed_cand.contains_aliases:
    return True

  if not _IsValidComposerUpgrade(parsed_curr.composer_ver,
                                 parsed_cand.composer_ver):
    raise InvalidImageVersionError('Not a valid Composer upgrade version.')

  if not _IsValidAirflowUpgrade(parsed_curr.airflow_ver,
                                parsed_cand.airflow_ver):
    raise InvalidImageVersionError('Not a valid Airflow upgrade version.')

  return True


def _IsValidAirflowUpgrade(cur_version, candidate_version):
  """Validates that only PATCH-level version increments are attempted.

  (For Airflow upgrades)

  Checks that major and minor-levels remain the same and patch-level is same
  or higher

  Args:
    cur_version: current 'a.b.c' Airflow version
    candidate_version: candidate 'a.b.d' Airflow version

  Returns:
    boolean value whether Airflow candidate is valid
  """
  curr_parts = list(map(int, cur_version.split('.', 3)))
  cand_parts = list(map(int, candidate_version.split('.', 3)))

  if (curr_parts[0] == cand_parts[0] and curr_parts[1] == cand_parts[1] and
      curr_parts[2] <= cand_parts[2]):
    return True

  return False


def _IsValidComposerUpgrade(cur_version, candidate_version):
  """Validates that only MINOR and PATCH-level version increments are attempted.

  (For Composer upgrades)

  Checks that major-level remains the same, minor-level ramains same or higher,
  and patch-level is same or higher (if it's the only change)

  Args:
    cur_version: current 'a.b.c' Composer version
    candidate_version: candidate 'a.b.d' Composer version

  Returns:
    boolean value whether Composer candidate is valid
  """
  curr_parts = list(map(int, cur_version.split('.', 3)))
  cand_parts = list(map(int, candidate_version.split('.', 3)))

  if (curr_parts[0] == cand_parts[0] and
      (curr_parts[1] < cand_parts[1] or
       (curr_parts[1] <= cand_parts[1] and curr_parts[2] <= cand_parts[2]))):
    return True

  return False
