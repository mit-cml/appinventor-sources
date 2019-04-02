# -*- coding: utf-8 -*- #
# Copyright 2015 Google Inc. All Rights Reserved.
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

"""Utilities for loading and parsing kubeconfig."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import os

from googlecloudsdk.core import config
from googlecloudsdk.core import exceptions as core_exceptions
from googlecloudsdk.core import log
from googlecloudsdk.core import properties
from googlecloudsdk.core import yaml
from googlecloudsdk.core.util import encoding
from googlecloudsdk.core.util import files as file_utils
from googlecloudsdk.core.util import platforms


class Error(core_exceptions.Error):
  """Class for errors raised by kubeconfig utilities."""


class MissingEnvVarError(Error):
  """An exception raised when required environment variables are missing."""


class Kubeconfig(object):
  """Interface for interacting with a kubeconfig file."""

  def __init__(self, raw_data, filename):
    self._filename = filename
    self._data = raw_data
    self.clusters = {}
    self.users = {}
    self.contexts = {}
    for cluster in self._data['clusters']:
      self.clusters[cluster['name']] = cluster
    for user in self._data['users']:
      self.users[user['name']] = user
    for context in self._data['contexts']:
      self.contexts[context['name']] = context

  @property
  def current_context(self):
    return self._data['current-context']

  def Clear(self, key):
    self.contexts.pop(key, None)
    self.clusters.pop(key, None)
    self.users.pop(key, None)
    if self._data.get('current-context') == key:
      self._data['current-context'] = ''

  def SaveToFile(self):
    """Save kubeconfig to file.

    Raises:
      Error: don't have the permission to open kubeconfig file.
    """
    self._data['clusters'] = list(self.clusters.values())
    self._data['users'] = list(self.users.values())
    self._data['contexts'] = list(self.contexts.values())
    with file_utils.FileWriter(self._filename, private=True) as fp:
      yaml.dump(self._data, fp)

  def SetCurrentContext(self, context):
    self._data['current-context'] = context

  @classmethod
  def _Validate(cls, data):
    try:
      if not data:
        raise Error('empty file')
      for key in ('clusters', 'users', 'contexts'):
        if not isinstance(data[key], list):
          raise Error(
              'invalid type for {0}: {1}'.format(data[key], type(data[key])))
    except KeyError as error:
      raise Error('expected key {0} not found'.format(error))

  @classmethod
  def LoadFromFile(cls, filename):
    try:
      data = yaml.load_path(filename)
    except yaml.Error as error:
      raise Error('unable to load kubeconfig for {0}: {1}'.format(
          filename, error.inner_error))
    cls._Validate(data)
    return cls(data, filename)

  @classmethod
  def LoadOrCreate(cls, filename):
    try:
      return cls.LoadFromFile(filename)
    except (Error, IOError) as error:
      log.debug('unable to load default kubeconfig: {0}; recreating {1}'.format(
          error, filename))
      file_utils.MakeDir(os.path.dirname(filename))
      kubeconfig = cls(EmptyKubeconfig(), filename)
      kubeconfig.SaveToFile()
      return kubeconfig

  @classmethod
  def Default(cls):
    return cls.LoadOrCreate(Kubeconfig.DefaultPath())

  @staticmethod
  def DefaultPath():
    """Return default path for kubeconfig file."""

    kubeconfig = encoding.GetEncodedValue(os.environ, 'KUBECONFIG')
    if kubeconfig:
      kubeconfig = kubeconfig.split(os.pathsep)[0]
      return os.path.abspath(kubeconfig)

    # This follows the same resolution process as kubectl for the config file.
    home_dir = encoding.GetEncodedValue(os.environ, 'HOME')
    if not home_dir and platforms.OperatingSystem.IsWindows():
      home_drive = encoding.GetEncodedValue(os.environ, 'HOMEDRIVE')
      home_path = encoding.GetEncodedValue(os.environ, 'HOMEPATH')
      if home_drive and home_path:
        home_dir = os.path.join(home_drive, home_path)
      if not home_dir:
        home_dir = encoding.GetEncodedValue(os.environ, 'USERPROFILE')

    if not home_dir:
      raise MissingEnvVarError(
          'environment variable {vars} or KUBECONFIG must be set to store '
          'credentials for kubectl'.format(
              vars='HOMEDRIVE/HOMEPATH, USERPROFILE, HOME,'
              if platforms.OperatingSystem.IsWindows() else 'HOME'))
    return os.path.join(home_dir, '.kube', 'config')


def Cluster(name, server, ca_path=None, ca_data=None):
  """Generate and return a cluster kubeconfig object."""
  cluster = {
      'server': server,
  }
  if ca_path and ca_data:
    raise Error('cannot specify both ca_path and ca_data')
  if ca_path:
    cluster['certificate-authority'] = ca_path
  elif ca_data:
    cluster['certificate-authority-data'] = ca_data
  else:
    cluster['insecure-skip-tls-verify'] = True
  return {
      'name': name,
      'cluster': cluster
  }


def User(name,
         auth_provider=None,
         cert_path=None,
         cert_data=None,
         key_path=None,
         key_data=None):
  """Generate and return a user kubeconfig object.

  Args:
    name: str, nickname for this user entry.
    auth_provider: str, authentication provider.
    cert_path: str, path to client certificate file.
    cert_data: str, base64 encoded client certificate data.
    key_path: str, path to client key file.
    key_data: str, base64 encoded client key data.
  Returns:
    dict, valid kubeconfig user entry.

  Raises:
    Error: if no auth info is provided (auth_provider or cert AND key)
  """
  # TODO(b/70856999) Figure out what the correct behavior for client certs is.
  if not (auth_provider or (cert_path and key_path) or
          (cert_data and key_data)):
    raise Error('either auth_provider or cert & key must be provided')
  user = {}
  if auth_provider:
    user['auth-provider'] = _AuthProvider(name=auth_provider)

  if cert_path and cert_data:
    raise Error('cannot specify both cert_path and cert_data')
  if cert_path:
    user['client-certificate'] = cert_path
  elif cert_data:
    user['client-certificate-data'] = cert_data

  if key_path and key_data:
    raise Error('cannot specify both key_path and key_data')
  if key_path:
    user['client-key'] = key_path
  elif key_data:
    user['client-key-data'] = key_data

  return {
      'name': name,
      'user': user
  }


SDK_BIN_PATH_NOT_FOUND = '''\
Path to sdk installation not found. Please switch to application default
credentials using one of

$ gcloud config set container/use_application_default_credentials true
$ export CLOUDSDK_CONTAINER_USE_APPLICATION_DEFAULT_CREDENTIALS=true'''


def _AuthProvider(name='gcp'):
  """Generate and return an auth provider config.

  Constructs an auth provider config entry readable by kubectl. This tells
  kubectl to call out to a specific gcloud command and parse the output to
  retrieve access tokens to authenticate to the kubernetes master.
  Kubernetes gcp auth provider plugin at
  https://github.com/kubernetes/kubernetes/tree/master/staging/src/k8s.io/client-go/plugin/pkg/client/auth/gcp

  Args:
    name: auth provider name
  Returns:
    dict, valid auth provider config entry.
  Raises:
    Error: Path to sdk installation not found. Please switch to application
    default credentials using one of

    $ gcloud config set container/use_application_default_credentials true
    $ export CLOUDSDK_CONTAINER_USE_APPLICATION_DEFAULT_CREDENTIALS=true.
  """
  provider = {'name': name}
  if (name == 'gcp' and not
      properties.VALUES.container.use_app_default_credentials.GetBool()):
    bin_name = 'gcloud'
    if platforms.OperatingSystem.IsWindows():
      bin_name = 'gcloud.cmd'
    sdk_bin_path = config.Paths().sdk_bin_path
    if sdk_bin_path is None:
      log.error(SDK_BIN_PATH_NOT_FOUND)
      raise Error(SDK_BIN_PATH_NOT_FOUND)
    cfg = {
        # Command for gcloud credential helper
        'cmd-path': os.path.join(sdk_bin_path, bin_name),
        # Args for gcloud credential helper
        'cmd-args': 'config config-helper --format=json',
        # JSONpath to the field that is the raw access token
        'token-key': '{.credential.access_token}',
        # JSONpath to the field that is the expiration timestamp
        'expiry-key': '{.credential.token_expiry}',
        # Note: we're omitting 'time-fmt' field, which if provided, is a
        # format string of the golang reference time. It can be safely omitted
        # because config-helper's default time format is RFC3339, which is the
        # same default kubectl assumes.
    }
    provider['config'] = cfg
  return provider


def Context(name, cluster, user):
  """Generate and return a context kubeconfig object."""
  return {
      'name': name,
      'context': {
          'cluster': cluster,
          'user': user,
      },
  }


def EmptyKubeconfig():
  return {
      'apiVersion': 'v1',
      'contexts': [],
      'clusters': [],
      'current-context': '',
      'kind': 'Config',
      'preferences': {},
      'users': [],
  }
