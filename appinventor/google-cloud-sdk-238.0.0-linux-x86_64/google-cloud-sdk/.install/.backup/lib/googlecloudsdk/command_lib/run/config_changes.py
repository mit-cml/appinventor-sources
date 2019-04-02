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
"""Class for representing various changes to a Configuration."""

from __future__ import absolute_import
from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

import abc
import six


class ConfigChanger(six.with_metaclass(abc.ABCMeta, object)):
  """An abstract class representing configuration changes."""

  @abc.abstractmethod
  def AdjustConfiguration(self, config, metadata):
    """Adjust the given Service configuration.

    Args:
      config: Configuration, The service's Configuration object to adjust.
      metadata: ObjectMeta, the config's metadata message object.
    """
    pass


class EnvVarChanges(ConfigChanger):
  """Represents the user-intent to modify environment variables."""

  def __init__(self, env_vars_to_update=None,
               env_vars_to_remove=None, clear_others=False):
    """Initialize a new EnvVarChanges object.

    Args:
      env_vars_to_update: {str, str}, Update env var names and values.
      env_vars_to_remove: [str], List of env vars to remove.
      clear_others: bool, If true, clear all non-updated env vars.
    """
    self._clear_others = clear_others
    self._to_update = env_vars_to_update
    self._to_remove = env_vars_to_remove

  def AdjustConfiguration(self, config, metadata):
    """Mutates the given config's env vars to match the desired changes."""
    del metadata  # Unused, but requred by ConfigChanger's signature.

    if self._clear_others:
      config.env_vars.clear()
    elif self._to_remove:
      for env_var in self._to_remove:
        if env_var in config.env_vars: del config.env_vars[env_var]

    if self._to_update: config.env_vars.update(self._to_update)


class ResourceChanges(ConfigChanger):
  """Represents the user-intent to update resource limits."""

  def __init__(self, memory):
    self._memory = memory

  def AdjustConfiguration(self, config, metadata):
    """Mutates the given config's resource limits to match what's desired."""
    del metadata  # Unused, but requred by ConfigChanger's signature.
    config.resource_limits['memory'] = self._memory


class ConcurrencyChanges(ConfigChanger):
  """Represents the user-intent to update concurrency preference."""

  def __init__(self, concurrency):
    self._concurrency = None if concurrency == 'default' else concurrency

  def AdjustConfiguration(self, config, metadata):
    """Mutates the given config's resource limits to match what's desired."""
    del metadata  # Unused, but requred by ConfigChanger's signature.
    if self._concurrency is None:
      config.deprecated_string_concurrency = None
      config.concurrency = None
    elif isinstance(self._concurrency, int):
      config.concurrency = self._concurrency
      config.deprecated_string_concurrency = None
    else:
      config.deprecated_string_concurrency = self._concurrency
      config.concurrency = None


class TimeoutChanges(ConfigChanger):
  """Represents the user-intent to update request duration."""

  def __init__(self, timeout):
    self._timeout = timeout

  def AdjustConfiguration(self, config, metadata):
    """Mutates the given config's timeout to match what's desired."""
    del metadata  # Unused, but required by ConfigChanger's signature.
    config.timeout = self._timeout
