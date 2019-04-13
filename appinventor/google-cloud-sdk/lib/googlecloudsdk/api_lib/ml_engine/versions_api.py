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
"""Utilities for dealing with ML versions API."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from apitools.base.py import encoding
from apitools.base.py import list_pager
from googlecloudsdk.api_lib.util import apis
from googlecloudsdk.core import exceptions
from googlecloudsdk.core import yaml
from googlecloudsdk.core.util import text


class InvalidVersionConfigFile(exceptions.Error):
  """Error indicating an invalid Version configuration file."""


class NoFieldsSpecifiedError(exceptions.Error):
  """Error indicating an invalid Version configuration file."""


def GetMessagesModule(version='v1'):
  return apis.GetMessagesModule('ml', version)


def GetClientInstance(version='v1', no_http=False):
  return apis.GetClientInstance('ml', version, no_http=no_http)


class VersionsClient(object):
  """Client for the versions service of Cloud ML Engine."""

  _ALLOWED_YAML_FIELDS = set(['autoScaling', 'description', 'deploymentUri',
                              'runtimeVersion', 'manualScaling', 'labels',
                              'machineType', 'framework', 'pythonVersion',
                              'modelClass', 'packageUris', 'serviceAccount'])

  def __init__(self, client=None, messages=None):
    self.client = client or GetClientInstance()
    self.messages = messages or self.client.MESSAGES_MODULE

  @property
  def version_class(self):
    return self.messages.GoogleCloudMlV1Version

  def _MakeCreateRequest(self, parent, version):
    return self.messages.MlProjectsModelsVersionsCreateRequest(
        parent=parent,
        googleCloudMlV1Version=version)

  def _MakeSetDefaultRequest(self, name):
    request = self.messages.GoogleCloudMlV1SetDefaultVersionRequest()
    return self.messages.MlProjectsModelsVersionsSetDefaultRequest(
        name=name,
        googleCloudMlV1SetDefaultVersionRequest=request)

  def Create(self, model_ref, version):
    """Creates a new version in an existing model."""
    return self.client.projects_models_versions.Create(
        self._MakeCreateRequest(
            parent=model_ref.RelativeName(),
            version=version))

  def Patch(self, version_ref, labels_update, description=None,
            model_class_update=None, package_uris=None):
    """Update a version."""
    version = self.messages.GoogleCloudMlV1Version()
    update_mask = []
    if labels_update.needs_update:
      version.labels = labels_update.labels
      update_mask.append('labels')

    if description:
      version.description = description
      update_mask.append('description')

    if model_class_update is not None and model_class_update.needs_update:
      update_mask.append('modelClass')
      version.modelClass = model_class_update.value

    if package_uris is not None:
      update_mask.append('packageUris')
      version.packageUris = package_uris

    if not update_mask:
      raise NoFieldsSpecifiedError('No updates requested.')

    return self.client.projects_models_versions.Patch(
        self.messages.MlProjectsModelsVersionsPatchRequest(
            name=version_ref.RelativeName(),
            googleCloudMlV1Version=version,
            updateMask=','.join(update_mask)))

  def Delete(self, version_ref):
    """Deletes a version from a model."""
    return self.client.projects_models_versions.Delete(
        self.messages.MlProjectsModelsVersionsDeleteRequest(
            name=version_ref.RelativeName()))

  def Get(self, version_ref):
    """Gets details about an existing model version."""
    return self.client.projects_models_versions.Get(
        self.messages.MlProjectsModelsVersionsGetRequest(
            name=version_ref.RelativeName()))

  def List(self, model_ref):
    """Lists the versions for a model."""
    list_request = self.messages.MlProjectsModelsVersionsListRequest(
        parent=model_ref.RelativeName())
    return list_pager.YieldFromList(
        self.client.projects_models_versions, list_request,
        field='versions', batch_size_attribute='pageSize')

  def SetDefault(self, version_ref):
    """Sets a model's default version."""
    return self.client.projects_models_versions.SetDefault(
        self._MakeSetDefaultRequest(name=version_ref.RelativeName()))

  def BuildVersion(self, name,
                   path=None,
                   deployment_uri=None,
                   runtime_version=None,
                   labels=None,
                   machine_type=None,
                   description=None,
                   framework=None,
                   python_version=None,
                   model_class=None,
                   package_uris=None,
                   accelerator_config=None,
                   service_account=None):
    """Create a Version object.

    The object is based on an optional YAML configuration file and the
    parameters to this method; any provided method parameters override any
    provided in-file configuration.

    The file may only have the fields given in
    VersionsClientBase._ALLOWED_YAML_FIELDS specified; the only parameters
    allowed are those that can be specified on the command line.

    Args:
      name: str, the name of the version object to create.
      path: str, the path to the YAML file.
      deployment_uri: str, the deploymentUri to set for the Version
      runtime_version: str, the runtimeVersion to set for the Version
      labels: Version.LabelsValue, the labels to set for the version
      machine_type: str, the machine type to serve the model version on.
      description: str, the version description.
      framework: FrameworkValueValuesEnum, the ML framework used to train this
        version of the model.
      python_version: str, The version of Python used to train the model.
      model_class: str, the FQN of a Python class implementing the Model
        interface for custom prediction.
      package_uris: list of str, Cloud Storage URIs containing user-supplied
        Python code to use.
      accelerator_config: an accelerator config message object.
      service_account: Specifies the service account for resource access
        control.


    Returns:
      A Version object (for the corresponding API version).

    Raises:
      InvalidVersionConfigFile: If the file contains unexpected fields.
    """
    version = self.version_class()

    if path:
      try:
        data = yaml.load_path(path)
      except (yaml.Error) as err:
        raise InvalidVersionConfigFile(
            'Could not read Version configuration file [{path}]:\n\n'
            '{err}'.format(path=path, err=str(err.inner_error)))
      if data:
        version = encoding.DictToMessage(data, self.version_class)

    specified_fields = set([f.name for f in version.all_fields() if
                            getattr(version, f.name)])
    invalid_fields = (specified_fields - self._ALLOWED_YAML_FIELDS |
                      set(version.all_unrecognized_fields()))
    if invalid_fields:
      raise InvalidVersionConfigFile(
          'Invalid {noun} [{fields}] in configuration file [{path}]. '
          'Allowed fields: [{allowed}].'.format(
              noun=text.Pluralize(len(invalid_fields), 'field'),
              fields=', '.join(sorted(invalid_fields)),
              path=path,
              allowed=', '.join(sorted(self._ALLOWED_YAML_FIELDS))))

    additional_fields = {
        'name': name,
        'deploymentUri': deployment_uri,
        'runtimeVersion': runtime_version,
        'labels': labels,
        'machineType': machine_type,
        'description': description,
        'framework': framework,
        'pythonVersion': python_version,
        'modelClass': model_class,
        'packageUris': package_uris,
        'acceleratorConfig': accelerator_config,
        'serviceAccount': service_account
    }
    for field_name, value in additional_fields.items():
      if value is not None:
        setattr(version, field_name, value)

    return version
