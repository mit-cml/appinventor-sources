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
"""Local configuration for service and region.

The content in this file is strictly as:
service: foo
region: bar
"""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.core import exceptions
from googlecloudsdk.core import yaml


DEFAULT_LOCAL_CONFIG_NAME = '.gcloud-serverless-service'


_SERVICE = 'service'

_REGION = 'region'

_VALID_FIELDS_SET = set([_SERVICE, _REGION])


class ConfigError(exceptions.Error):
  """Raised when a local config file is not valid."""
  pass


class LocalConfig(object):
  """Wrapping class for local config yaml file.
  """

  def __init__(self, service=None, region=None):
    self._service = service
    self._region = region

  @property
  def service(self):
    return self._service

  @property
  def region(self):
    return self._region

  @classmethod
  def ParseFrom(cls, filename):
    """Parse local config from filename."""
    config = yaml.load_path(filename)

    # TODO(b/78124357): Implement validation in more general way.
    for field in config.keys():
      if field not in _VALID_FIELDS_SET:
        raise ConfigError('Invalid field {} in {}'.format(field, filename))

    return LocalConfig(config.get(_SERVICE, None), config.get(_REGION, None))
