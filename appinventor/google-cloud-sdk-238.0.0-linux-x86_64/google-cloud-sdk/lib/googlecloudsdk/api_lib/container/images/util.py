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
"""Utilities for the container images commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from contextlib import contextmanager

# pytype: disable=import-error
from containerregistry.client import docker_creds
from containerregistry.client import docker_name
# We use distinct versions of the library for v2 and v2.2 because
# the schema of the JSON data returned is fairly different, and
# images addressed by digest must be accessed via the API version
# corresponding to how they are stored.
from containerregistry.client.v2 import docker_http as v2_docker_http
from containerregistry.client.v2 import docker_image as v2_image
from containerregistry.client.v2_2 import docker_http as v2_2_docker_http
from containerregistry.client.v2_2 import docker_image as v2_2_image
from containerregistry.client.v2_2 import docker_image_list
# pytype: enable=import-error
from googlecloudsdk.api_lib.container.images import container_analysis_data_util
from googlecloudsdk.api_lib.containeranalysis import util as containeranalysis_util
from googlecloudsdk.api_lib.util import apis
from googlecloudsdk.core import exceptions
from googlecloudsdk.core import http
from googlecloudsdk.core import resources
from googlecloudsdk.core.credentials import store as c_store
from googlecloudsdk.core.docker import constants
from googlecloudsdk.core.docker import docker
from googlecloudsdk.core.util import times
import six
from six.moves import map
import six.moves.http_client


class UtilError(exceptions.Error):
  """Base class for util errors."""


class InvalidImageNameError(UtilError):
  """Raised when the user supplies an invalid image name."""


class UserRecoverableV2Error(UtilError):
  """Raised when a user-recoverable V2 API error is encountered."""


class TokenRefreshError(UtilError):
  """Raised when there's an error refreshing tokens."""


def IsFullySpecified(image_name):
  return ':' in image_name or '@' in image_name


def ValidateRepositoryPath(repository_path):
  """Validates the repository path.

  Args:
    repository_path: str, The repository path supplied by a user.

  Returns:
    The parsed docker_name.Repository object.

  Raises:
    InvalidImageNameError: If the image name is invalid.
    docker.UnsupportedRegistryError: If the path is valid, but belongs to a
      registry we don't support.
  """
  if IsFullySpecified(repository_path):
    raise InvalidImageNameError(
        'Image names must not be fully-qualified. Remove the tag or digest '
        'and try again.')
  if repository_path.endswith('/'):
    raise InvalidImageNameError('Image name cannot end with \'/\'. '
                                'Remove the trailing \'/\' and try again.')

  try:
    if repository_path in constants.MIRROR_REGISTRIES:
      repository = docker_name.Registry(repository_path)
    else:
      repository = docker_name.Repository(repository_path)
    if repository.registry not in constants.ALL_SUPPORTED_REGISTRIES:
      raise docker.UnsupportedRegistryError(repository_path)
    return repository
  except docker_name.BadNameException as e:
    # Reraise with the proper base class so the message gets shown.
    raise InvalidImageNameError(six.text_type(e))


class CredentialProvider(docker_creds.Basic):
  """CredentialProvider is a class to refresh oauth2 creds during requests."""

  _USERNAME = '_token'

  def __init__(self):
    super(CredentialProvider, self).__init__(self._USERNAME, 'does not matter')

  @property
  def password(self):
    cred = c_store.LoadIfEnabled()
    return cred.access_token if cred else None


def _TimeCreatedToDateTime(time_created_ms):
  # Convert to float.
  timestamp = float(time_created_ms)
  # Round the timestamp to whole seconds.
  timestamp = round(timestamp / 1000)
  try:
    return times.GetDateTimeFromTimeStamp(timestamp)
  except (ArithmeticError, times.DateTimeValueError):
    # Values like -62135596800000 have been observed, causing underflows.
    return None


def RecoverProjectId(repository):
  """Recovers the project-id from a GCR repository."""
  if repository.registry in constants.MIRROR_REGISTRIES:
    return constants.MIRROR_PROJECT
  if repository.registry in constants.LAUNCHER_REGISTRIES:
    return constants.LAUNCHER_PROJECT
  parts = repository.repository.split('/')
  if '.' not in parts[0]:
    return parts[0]
  elif len(parts) > 1:
    return parts[0] + ':' + parts[1]
  else:
    raise ValueError('Domain-scoped app missing project name: %s', parts[0])


def _UnqualifiedResourceUrl(repo):
  return 'https://{repo}@'.format(repo=str(repo))


def _ResourceUrl(repo, digest):
  return 'https://{repo}@{digest}'.format(repo=str(repo), digest=digest)


def _FullyqualifiedDigest(digest):
  return 'https://{digest}'.format(digest=digest)


def _MakeSummaryRequest(project_id, url_filter):
  """Helper function to make Summary request."""
  client = apis.GetClientInstance('containeranalysis', 'v1alpha1')
  messages = apis.GetMessagesModule('containeranalysis', 'v1alpha1')
  project_ref = resources.REGISTRY.Parse(
      project_id, collection='cloudresourcemanager.projects')

  req = (
      messages.
      ContaineranalysisProjectsOccurrencesGetVulnerabilitySummaryRequest(
          parent=project_ref.RelativeName(), filter=url_filter))
  return client.projects_occurrences.GetVulnerabilitySummary(req)


def FetchOccurrencesForResource(digest, occurrence_filter=None):
  """Fetches the occurrences attached to this image."""
  project_id = RecoverProjectId(digest)
  resource_filter = 'resource_url="{resource_url}"'.format(
      resource_url=_FullyqualifiedDigest(digest))
  return containeranalysis_util.MakeOccurrenceRequest(
      project_id, resource_filter, occurrence_filter)


def FetchDeploymentsForImage(image, occurrence_filter=None):
  """Fetches the deployment occurrences attached to this image."""
  project_id = RecoverProjectId(image)
  depl_filter = 'kind="DEPLOYABLE"'  # and details.deployment.resource_uri=image
  occ_filter = '({arg_filter} AND {depl_filter})'.format(
      arg_filter=occurrence_filter,
      depl_filter=depl_filter,
  )
  occurrences = list(
      containeranalysis_util.MakeOccurrenceRequest(project_id, occ_filter))
  deployments = []
  image_string = str(image)
  for occ in occurrences:
    if not occ.deployment:
      continue
    if image_string in occ.deployment.resourceUri:
      deployments.append(occ)
  return deployments


def TransformContainerAnalysisData(image_name,
                                   occurrence_filter=None,
                                   deployments=False):
  """Transforms the occurrence data from Container Analysis API."""
  analysis_obj = container_analysis_data_util.ContainerAndAnalysisData(
      image_name)
  occs = FetchOccurrencesForResource(image_name, occurrence_filter)
  for occ in occs:
    analysis_obj.add_record(occ)
  if deployments:
    depl_occs = FetchDeploymentsForImage(image_name, occurrence_filter)
    for depl_occ in depl_occs:
      analysis_obj.add_record(depl_occ)
  analysis_obj.resolveSummaries()
  return analysis_obj


def FetchSummary(repository, resource_url):
  """Fetches the summary of vulnerability occurrences for some resource.

  Args:
    repository: A parsed docker_name.Repository object.
    resource_url: The URL identifying the resource.

  Returns:
    A GetVulnzOccurrencesSummaryResponse.
  """
  project_id = RecoverProjectId(repository)
  url_filter = 'resource_url = "{resource_url}"'.format(
      resource_url=resource_url)
  return _MakeSummaryRequest(project_id, url_filter)


def FetchOccurrences(repository, occurrence_filter=None, resource_urls=None):
  """Fetches the occurrences attached to the list of manifests."""
  project_id = RecoverProjectId(repository)

  # Retrieve all resource urls prefixed with the image path
  resource_filter = 'has_prefix(resource_url, "{repo}")'.format(
      repo=_UnqualifiedResourceUrl(repository))

  occurrences = containeranalysis_util.MakeOccurrenceRequest(
      project_id, resource_filter, occurrence_filter, resource_urls)
  occurrences_by_resources = {}
  for occ in occurrences:
    if occ.resourceUrl not in occurrences_by_resources:
      occurrences_by_resources[occ.resourceUrl] = []
    occurrences_by_resources[occ.resourceUrl].append(occ)
  return occurrences_by_resources


def TransformManifests(manifests,
                       repository,
                       show_occurrences=False,
                       occurrence_filter=None,
                       resource_urls=None):
  """Transforms the manifests returned from the server."""
  if not manifests:
    return []

  # Map from resource url to the occurrence.
  occurrences = {}
  if show_occurrences:
    occurrences = FetchOccurrences(
        repository,
        occurrence_filter=occurrence_filter,
        resource_urls=resource_urls)

  # Attach each occurrence to the resource to which it applies.
  results = []
  for k, v in six.iteritems(manifests):
    result = {
        'digest': k,
        'tags': v.get('tag', []),
        'timestamp': _TimeCreatedToDateTime(v.get('timeCreatedMs'))
    }

    # Partition the (non-PACKAGE_VULNERABILITY) occurrences into different
    # columns by kind.
    for occ in occurrences.get(_ResourceUrl(repository, k), []):
      if occ.kind not in result:
        result[occ.kind] = []
      result[occ.kind].append(occ)  # pytype: disable=attribute-error

    if show_occurrences and resource_urls:
      result['vuln_counts'] = {}
      # If this manifest is in the list of resource urls for which to show
      # summaries, query the API for the summary.
      resource_url = _ResourceUrl(repository, k)
      if resource_url not in resource_urls:
        continue

      summary = FetchSummary(repository, resource_url)
      for severity_count in summary.counts:
        if severity_count.severity:
          result['vuln_counts'][str(severity_count.severity)] = (
              severity_count.count)  # pytype: disable=attribute-error

    results.append(result)
  return results


def GetTagNamesForDigest(digest, http_obj):
  """Gets all of the tags for a given digest.

  Args:
    digest: docker_name.Digest, The digest supplied by a user.
    http_obj: http.Http(), The http transport.

  Returns:
    A list of all of the tags associated with the input digest.
  """
  repository_path = digest.registry + '/' + digest.repository
  repository = ValidateRepositoryPath(repository_path)
  with v2_2_image.FromRegistry(
      basic_creds=CredentialProvider(), name=repository,
      transport=http_obj) as image:
    if digest.digest not in image.manifests():
      return []
    manifest_value = image.manifests().get(digest.digest, {})
    return manifest_value.get('tag', [])  # digest tags


def GetDockerTagsForDigest(digest, http_obj):
  """Gets all of the tags for a given digest.

  Args:
    digest: docker_name.Digest, The digest supplied by a user.
    http_obj: http.Http(), The http transport.

  Returns:
    A list of all of the tags associated with the input digest.
  """
  repository_path = digest.registry + '/' + digest.repository
  repository = ValidateRepositoryPath(repository_path)
  tags = []
  tag_names = GetTagNamesForDigest(digest, http_obj)
  for tag_name in tag_names:  # iterate over digest tags
    try:
      tag = docker_name.Tag(str(repository) + ':' + tag_name)
    except docker_name.BadNameException as e:
      raise InvalidImageNameError(six.text_type(e))
    tags.append(tag)
  return tags


def ValidateImagePathAndReturn(digest_or_tag):
  # Repository should contain project/image_path.
  if '/' not in digest_or_tag.repository:
    raise InvalidImageNameError('Image name should start with '
                                '*.gcr.io/project_id/image_path. ')
  return digest_or_tag


def GetDockerImageFromTagOrDigest(image_name):
  """Gets an image object given either a tag or a digest.

  Args:
    image_name: Either a fully qualified tag or a fully qualified digest.
      Defaults to latest if no tag specified.

  Returns:
    Either a docker_name.Tag or a docker_name.Digest object.

  Raises:
    InvalidImageNameError: Given digest could not be resolved to a full digest.
  """
  if not IsFullySpecified(image_name):
    image_name += ':latest'

  try:
    return ValidateImagePathAndReturn(docker_name.Tag(image_name))
  except docker_name.BadNameException:
    pass

  parts = image_name.split('@', 1)
  if len(parts) == 2:
    if not parts[1].startswith('sha256:'):
      raise InvalidImageNameError(
          '[{0}] digest must be of the form "sha256:<digest>".'.format(
              image_name))

    # If the full digest wasn't specified, check if what was passed
    # in is a valid digest prefix.
    # 7 for 'sha256:' and 64 for the full digest
    if len(parts[1]) < 7 + 64:
      resolved = GetDockerDigestFromPrefix(image_name)
      if resolved == image_name:
        raise InvalidImageNameError(
            '[{0}] could not be resolved to a full digest.'.format(image_name))
      image_name = resolved
  try:
    return ValidateImagePathAndReturn(docker_name.Digest(image_name))
  except docker_name.BadNameException:
    raise InvalidImageNameError(
        '[{0}] digest must be of the form "sha256:<digest>".'.format(
            image_name))


def GetDigestFromName(image_name):
  """Gets a digest object given a repository, tag or digest.

  Args:
    image_name: A docker image reference, possibly underqualified.

  Returns:
    a docker_name.Digest object.

  Raises:
    InvalidImageNameError: If no digest can be resolved.
  """
  tag_or_digest = GetDockerImageFromTagOrDigest(image_name)
  # If we got a digest, then just return it.
  if isinstance(tag_or_digest, docker_name.Digest):
    return tag_or_digest

  # If we got a tag, resolve it to a digest.
  def ResolveV2Tag(tag):
    with v2_image.FromRegistry(
        basic_creds=CredentialProvider(), name=tag,
        transport=http.Http()) as v2_img:
      if v2_img.exists():
        return v2_img.digest()
      return None

  def ResolveV22Tag(tag):
    with v2_2_image.FromRegistry(
        basic_creds=CredentialProvider(),
        name=tag,
        transport=http.Http(),
        accepted_mimes=v2_2_docker_http.SUPPORTED_MANIFEST_MIMES) as v2_2_img:
      if v2_2_img.exists():
        return v2_2_img.digest()
      return None

  def ResolveManifestListTag(tag):
    with docker_image_list.FromRegistry(
        basic_creds=CredentialProvider(), name=tag,
        transport=http.Http()) as manifest_list:
      if manifest_list.exists():
        return manifest_list.digest()
      return None

  # Resolve as manifest list, then v2.2, then v2.1 because for compatibility:
  # - manifest lists can be rewritten to v2.2 "default" images.
  # - v2.2 manifests can be rewritten to v2.1 manifests.
  sha256 = (
      ResolveManifestListTag(tag_or_digest) or ResolveV22Tag(tag_or_digest) or
      ResolveV2Tag(tag_or_digest))
  if not sha256:
    raise InvalidImageNameError(
        '[{0}] is not a valid name. Expected tag in the form "base:tag" or '
        '"tag" or digest in the form "sha256:<digest>"'.format(image_name))

  return docker_name.Digest('{registry}/{repository}@{sha256}'.format(
      registry=tag_or_digest.registry,
      repository=tag_or_digest.repository,
      sha256=sha256))


def GetDockerDigestFromPrefix(digest):
  """Gets a full digest string given a potential prefix.

  Args:
    digest: The digest prefix

  Returns:
    The full digest, or the same prefix if no full digest is found.

  Raises:
    InvalidImageNameError: if the prefix supplied isn't unique.
  """
  repository_path, prefix = digest.split('@', 1)
  repository = ValidateRepositoryPath(repository_path)
  with v2_2_image.FromRegistry(
      basic_creds=CredentialProvider(), name=repository,
      transport=http.Http()) as image:
    matches = [d for d in image.manifests() if d.startswith(prefix)]

    if len(matches) == 1:
      return repository_path + '@' + matches.pop()
    elif len(matches) > 1:
      raise InvalidImageNameError(
          '{0} is not a unique digest prefix. Options are {1}.]'.format(
              prefix, ', '.join(map(str, matches))))
    return digest


@contextmanager
def WrapExpectedDockerlessErrors(optional_image_name=None):
  try:
    yield
  except (v2_docker_http.V2DiagnosticException,
          v2_2_docker_http.V2DiagnosticException) as err:
    if err.status in [
        six.moves.http_client.UNAUTHORIZED, six.moves.http_client.FORBIDDEN
    ]:
      raise UserRecoverableV2Error('Access denied: {}'.format(
          optional_image_name or str(err)))
    elif err.status == six.moves.http_client.NOT_FOUND:
      raise UserRecoverableV2Error('Not found: {}'.format(
          optional_image_name or str(err)))
    raise
  except (v2_docker_http.TokenRefreshException,
          v2_2_docker_http.TokenRefreshException) as err:
    raise TokenRefreshError(str(err))
