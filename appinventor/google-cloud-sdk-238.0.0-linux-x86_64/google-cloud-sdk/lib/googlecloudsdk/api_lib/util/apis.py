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

"""Library for obtaining API clients and messages."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import re
from apitools.base.py import exceptions as apitools_exceptions
from apitools.base.py import http_wrapper
from googlecloudsdk.api_lib.services import enable_api
from googlecloudsdk.api_lib.util import apis_internal
from googlecloudsdk.api_lib.util import apis_util
from googlecloudsdk.api_lib.util import exceptions as api_exceptions
from googlecloudsdk.core import properties
from googlecloudsdk.core.console import console_io
from googlecloudsdk.third_party.apis import apis_map


def _CamelCase(snake_case):
  parts = snake_case.split('_')
  return ''.join(s.capitalize() for s in parts)


def ConstructApiDef(api_name,
                    api_version,
                    is_default,
                    base_pkg='googlecloudsdk.third_party.apis'):
  """Creates and returns the APIDef specified by the given arguments.

  Args:
    api_name: str, The API name (or the command surface name, if different).
    api_version: str, The version of the API.
    is_default: bool, Whether this API version is the default.
    base_pkg: str, Base package from which generated API files are accessed.

  Returns:
    APIDef, The APIDef created using the given args.
  """
  # pylint:disable=protected-access
  api_name, _ = apis_internal._GetApiNameAndAlias(api_name)
  client_cls_name = _CamelCase(api_name) + _CamelCase(api_version)
  class_path = '{base}.{api_name}.{api_version}'.format(
      base=base_pkg, api_name=api_name, api_version=api_version,)

  common_fmt = '{api_name}_{api_version}_'
  client_cls_path_fmt = common_fmt + 'client.{api_client_class}'
  client_cls_path = client_cls_path_fmt.format(api_name=api_name,
                                               api_version=api_version,
                                               api_client_class=client_cls_name)

  messages_mod_path_fmt = common_fmt + 'messages'
  messages_mod_path = messages_mod_path_fmt.format(api_name=api_name,
                                                   api_version=api_version)
  return apis_map.APIDef(class_path, client_cls_path,
                         messages_mod_path, is_default)


def AddToApisMap(api_name, api_version, default=None,
                 base_pkg='googlecloudsdk.third_party.apis'):
  """Adds the APIDef specified by the given arguments to the APIs map.

  This method should only be used for runtime patcing of the APIs map. Additions
  to the map should ensure that there is only one and only one default version
  for each API.

  Args:
    api_name: str, The API name (or the command surface name, if different).
    api_version: str, The version of the API.
    default: bool, Whether this API version is the default. If set to None
      will be set to True if this is first version of api, otherwise false.
    base_pkg: str, Base package from which generated API files are accessed.
  """
  # pylint:disable=protected-access
  api_name, _ = apis_internal._GetApiNameAndAlias(api_name)
  api_def = ConstructApiDef(api_name, api_version, default, base_pkg)
  api_versions = apis_map.MAP.get(api_name, {})
  if default is None:
    api_def.default_version = not api_versions
  api_versions[api_version] = api_def
  apis_map.MAP[api_name] = api_versions


def SetDefaultVersion(api_name, api_version):
  """Resets default version for given api."""
  # pylint:disable=protected-access
  api_def = apis_internal._GetApiDef(api_name, api_version)
  # pylint:disable=protected-access
  default_version = apis_internal._GetDefaultVersion(api_name)
  # pylint:disable=protected-access
  default_api_def = apis_internal._GetApiDef(api_name, default_version)
  default_api_def.default_version = False
  api_def.default_version = True


def GetVersions(api_name):
  """Return available versions for given api.

  Args:
    api_name: str, The API name (or the command surface name, if different).

  Raises:
    UnknownAPIError: If api_name does not exist in the APIs map.

  Returns:
    list, of version names.
  """
  # pylint:disable=protected-access
  return apis_internal._GetVersions(api_name)


def ResolveVersion(api_name, default_override=None):
  """Resolves the version for an API based on the APIs map and API overrides.

  Args:
    api_name: str, The API name (or the command surface name, if different).
    default_override: str, The override for the default version.

  Raises:
    apis_internal.UnknownAPIError: If api_name does not exist in the APIs map.

  Returns:
    str, The resolved version.
  """
  # pylint:disable=protected-access
  api_name, api_name_alias = apis_internal._GetApiNameAndAlias(api_name)
  if api_name not in apis_map.MAP:
    raise apis_util.UnknownAPIError(api_name)

  version_overrides = properties.VALUES.api_client_overrides.AllValues()
  version_override = version_overrides.get(api_name_alias, None)
  return (version_override or default_override or
          # pylint:disable=protected-access
          apis_internal._GetDefaultVersion(api_name))


API_ENABLEMENT_REGEX = re.compile(
    '(?:Access Not Configured. )?.*has not been used in project \\S+ before or '
    'it is disabled. Enable it by visiting https://console.developers.google'
    '.com/apis/api/([^/]+)/overview\\?project=(\\S+) then retry. If you '
    'enabled this API recently, wait a few minutes for the action to propagate '
    'to our systems and retry.')


API_ENABLEMENT_ERROR_EXPECTED_STATUS_CODE = 403  # retry status code


def _GetApiEnablementInfo(exc):
  """This is a handler for apitools errors allowing more specific errors.

  While HttpException is great for generally parsing apitools exceptions,
  in the case of an API enablement error we want to know what the service
  is that was rejected. This will attempt to parse the error for said
  service token.

  Args:
    exc: api_exceptions.HttpException

  Returns:
    (str, str), (enablement project, service token), or (None, None) if the
      exception isn't an API enablement error
  """
  match = API_ENABLEMENT_REGEX.match(exc.payload.status_message)
  if (exc.payload.status_code == API_ENABLEMENT_ERROR_EXPECTED_STATUS_CODE
      and match is not None):
    return (match.group(2), match.group(1))
  return (None, None)


_PROJECTS_NOT_TO_ENABLE = {'google.com:cloudsdktool'}


def ShouldAttemptProjectEnable(project):
  return project not in _PROJECTS_NOT_TO_ENABLE


def GetApiEnablementInfo(exception):
  """Returns the API Enablement info or None if prompting is not necessary.

  Args:
    exception (apitools_exceptions.HttpError): Exception if an error occurred.

  Returns:
    tuple[str]: The project, service token, exception tuple to be used for
      prompting to enable the API.

  Raises:
    api_exceptions.HttpException: If gcloud should not prompt to enable the API.
  """
  parsed_error = api_exceptions.HttpException(exception)
  (project, service_token) = _GetApiEnablementInfo(parsed_error)
  if (project is not None and ShouldAttemptProjectEnable(project)
      and service_token is not None):
    return (project, service_token, parsed_error)


def PromptToEnableApi(project, service_token, exception,
                      is_batch_request=False):
  """Prompts to enable the API and throws if the answer is no.

  Args:
    project (str): The project that the API is not enabled on.
    service_token (str): The service token of the API to prompt for.
    exception (api_Exceptions.HttpException): Exception to throw if the prompt
      is denied.
    is_batch_request: If the request is a batch request. This determines how to
      get apitools to retry the request.

  Raises:
    api_exceptions.HttpException: API not enabled error if the user chooses to
      not enable the API.
  """
  if console_io.PromptContinue(
      default=False,
      prompt_string=('API [{}] not enabled on project [{}]. '
                     'Would you like to enable and retry (this will take a '
                     'few minutes)?')
      .format(service_token, project)):
    enable_api.EnableServiceIfDisabled(project, service_token)
    # In the case of a batch request, as long as the error's retryable code
    # (in this case 403) was set, after this runs it should retry. This
    # error code should be consistent with apis.GetApiEnablementInfo
    if not is_batch_request:
      raise apitools_exceptions.RequestError('Retry')
  else:
    raise exception


def _CheckResponse(response):
  """Checks API error and if it's an enablement error, prompt to enable & retry.

  Args:
    response: response that had an error.

  Raises:
    apitools_exceptions.RequestError: error which should signal apitools to
      retry.
    api_exceptions.HttpException: the parsed error.
  """
  # This will throw if there was a specific type of error. If not, then we can
  # parse and deal with our own class of errors.
  http_wrapper.CheckResponse(response)
  if not properties.VALUES.core.should_prompt_to_enable_api.GetBool():
    return
  # Once we get here, we check if it was an API enablement error and if so,
  # prompt the user to enable the API. If yes, we make that call and then
  # raise a RequestError, which will prompt the caller to retry. If not, we
  # raise the actual HTTP error.
  response_as_error = apitools_exceptions.HttpError.FromResponse(response)
  enablement_info = GetApiEnablementInfo(response_as_error)
  if enablement_info:
    PromptToEnableApi(*enablement_info)


def GetClientClass(api_name, api_version):
  """Returns the client class for the API specified in the args.

  Args:
    api_name: str, The API name (or the command surface name, if different).
    api_version: str, The version of the API.

  Returns:
    base_api.BaseApiClient, Client class for the specified API.
  """
  # pylint:disable=protected-access
  return apis_internal._GetClientClass(api_name, api_version)


def GetClientInstance(api_name, api_version, no_http=False,
                      enable_resource_quota=True):
  """Returns an instance of the API client specified in the args.

  Args:
    api_name: str, The API name (or the command surface name, if different).
    api_version: str, The version of the API.
    no_http: bool, True to not create an http object for this client.
    enable_resource_quota: bool, By default, we are going to tell APIs to use
      the quota of the project being operated on. For some APIs we want to use
      gcloud's quota, so you can explicitly disable that behavior by passing
      False here.

  Returns:
    base_api.BaseApiClient, An instance of the specified API client.
  """
  # pylint:disable=protected-access
  return apis_internal._GetClientInstance(
      api_name, api_version, no_http, _CheckResponse, enable_resource_quota)


def GetEffectiveApiEndpoint(api_name, api_version, client_class=None):
  """Returns effective endpoint for given api."""
  # pylint:disable=protected-access
  return apis_internal._GetEffectiveApiEndpoint(api_name,
                                                api_version,
                                                client_class)


def GetMessagesModule(api_name, api_version):
  """Returns the messages module for the API specified in the args.

  Args:
    api_name: str, The API name (or the command surface name, if different).
    api_version: str, The version of the API.

  Returns:
    Module containing the definitions of messages for the specified API.
  """
  # pylint:disable=protected-access
  api_def = apis_internal._GetApiDef(api_name, api_version)
  # fromlist below must not be empty, see:
  # http://stackoverflow.com/questions/2724260/why-does-pythons-import-require-fromlist.
  return __import__(api_def.messages_full_modulepath, fromlist=['something'])
