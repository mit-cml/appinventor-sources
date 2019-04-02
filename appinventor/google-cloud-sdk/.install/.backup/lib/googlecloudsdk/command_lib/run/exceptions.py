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
"""This module holds exceptions raised by Cloud Run commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.util import exceptions as exceptions_util
from googlecloudsdk.core import exceptions


class BucketAccessError(exceptions.Error):
  """Indicates a failed attempt to access a GCS bucket."""
  pass


class ConfigurationError(exceptions.Error):
  """Indicates an error in configuration."""
  pass


class ServiceNotFoundError(exceptions.Error):
  """Indicates that a provided service name was not found."""
  pass


class RevisionNotFoundError(exceptions.Error):
  """Indicates that a provided revision name was not found."""
  pass


class DockerVersionError(exceptions.Error):
  """Indicates an error in determining the docker version."""
  pass


class AmbiguousContainerError(exceptions.Error):
  """More than one container fits our criteria, we do not know which to run."""
  pass


class ContainerIdError(exceptions.Error):
  """Container Id cannot be found by docker."""
  pass


class NoActiveRevisionsError(exceptions.Error):
  """Active revisions were expected but not found."""
  pass


class SourceNotSupportedError(exceptions.Error):
  """Your Cloud Run install does not support source deployment."""
  pass


class NoConfigurationChangeError(exceptions.Error):
  """No configuration changes were requested."""
  pass


class UnknownDeployableError(exceptions.Error):
  """Could not identify the deployable app, function, or container."""
  pass


class AppNotReadyError(exceptions.InternalError):
  """The application must be uploaded before it can be deployed."""
  pass


class DeploymentFailedError(exceptions.Error):
  """An error was encountered during deployment."""
  pass


class DomainMappingCreationError(exceptions.Error):
  """An error was encountered during the creation of a domain mapping."""


class BadImageError(exceptions_util.HttpException):
  """Invalid image provided in the revision template."""

  # The relevant field that'll have a violation.
  IMAGE_ERROR_FIELD = 'spec.revisionTemplate.spec.container.image'

  def __init__(self, http_exception):
    super(BadImageError, self).__init__(
        http_exception,
        '{{field_violations.{}}}'.format(self.IMAGE_ERROR_FIELD))


class NoTLSError(exceptions.Error):
  """TLS 1.2 support is required to connect to GKE.

  Your Python installation does not support TLS 1.2. For Python2, please upgrade
  to version 2.7.9 or greater; for Python3, please upgrade to version 3.4 or
  greater.
  """
