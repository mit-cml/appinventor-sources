# -*- coding: utf-8 -*- #
# Copyright 2014 Google Inc. All Rights Reserved.
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
"""Read and write properties for the CloudSDK."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import functools
import os
import re
import sys
import enum

from googlecloudsdk.core import config
from googlecloudsdk.core import exceptions
from googlecloudsdk.core.configurations import named_configs
from googlecloudsdk.core.configurations import properties_file as prop_files_lib
from googlecloudsdk.core.docker import constants as const_lib
from googlecloudsdk.core.util import encoding
from googlecloudsdk.core.util import http_proxy_types
from googlecloudsdk.core.util import times

import six

# Try to parse the command line flags at import time to see if someone provided
# the --configuration flag.  If they did, this could affect the value of the
# properties defined in that configuration.  Since some libraries (like logging)
# use properties at startup, we want to use the correct configuration for that.
named_configs.FLAG_OVERRIDE_STACK.PushFromArgs(sys.argv)

_SET_PROJECT_HELP = """\
To set your project, run:

  $ gcloud config set project PROJECT_ID

or to unset it, run:

  $ gcloud config unset project"""

_VALID_PROJECT_REGEX = re.compile(
    r'^'
    # An optional domain-like component, ending with a colon, e.g.,
    # google.com:
    r'(?:(?:[-a-z0-9]{1,63}\.)*(?:[a-z](?:[-a-z0-9]{0,61}[a-z0-9])?):)?'
    # Followed by a required identifier-like component, for example:
    #   waffle-house    match
    #   -foozle        no match
    #   Foozle         no match
    # We specifically disallow project number, even though some GCP backends
    # could accept them.
    # We also allow a leading digit as some legacy project ids can have
    # a leading digit.
    r'(?:(?:[a-z0-9](?:[-a-z0-9]{0,61}[a-z0-9])?))'
    r'$')

_VALID_ENDPOINT_OVERRIDE_REGEX = re.compile(
    r'^'
    # require http or https for scheme
    r'(?:https?)://'
    # netlocation portion of address. can be any of
    # - domain name
    # - 'localhost'
    # - ipv4 addr
    # - ipv6 addr
    r'(?:'  # begin netlocation
    # - domain name, e.g. 'test-foo.sandbox.googleapis.com'
    #   1 or more domain labels ending in '.', e.g. 'sandbox.', 'googleapis.'
    r'(?:[A-Z0-9](?:[A-Z0-9-]{0,61}[A-Z0-9])?\.)+'
    #   ending top-level domain, e.g. 'com'
    r'(?:[A-Z]{2,6}|[A-Z0-9-]{2,})|'
    # - localhost
    r'localhost|'
    # - ipv4
    r'\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}|'
    # - ipv6
    r'\[?[A-F0-9]*:[A-F0-9:]+\]?'
    r')'  # end netlocation
    # optional port
    r'(?::\d+)?'
    # require trailing slash, fragment optional
    r'(?:/|[/?]\S+/)'
    r'$',
    re.IGNORECASE)

_PUBSUB_NOTICE_URL = (
    'https://cloud.google.com/functions/docs/writing/background#event_parameter'
)


def Stringize(value):
  if isinstance(value, six.string_types):
    return value
  return str(value)


def _LooksLikeAProjectName(project):
  """Heuristics testing if a string looks like a project name, but an id."""

  if re.match(r'[-0-9A-Z]', project[0]):
    return True

  return any(c in project for c in ' !"\'')


def _BooleanValidator(property_name, value):
  """Validates boolean properties.

  Args:
    property_name: str, the name of the property
    value: str | bool, the value to validate

  Raises:
    InvalidValueError: if value is not boolean
  """
  accepted_strings = [
      'true', '1', 'on', 'yes', 'y', 'false', '0', 'off', 'no', 'n', '', 'none'
  ]
  if Stringize(value).lower() not in accepted_strings:
    raise InvalidValueError(
        'The [{0}] value [{1}] is not valid. Possible values: [{2}]. '
        '(See http://yaml.org/type/bool.html)'.format(
            property_name, value,
            ', '.join([x if x else "''" for x in accepted_strings])))


def _BuildTimeoutValidator(timeout):
  """Validates build timeouts."""
  if timeout is None:
    return
  seconds = times.ParseDuration(timeout, default_suffix='s').total_seconds
  if seconds <= 0:
    raise InvalidValueError('Timeout must be a positive time duration.')


class Error(exceptions.Error):
  """Exceptions for the properties module."""


class PropertiesParseError(Error):
  """An exception to be raised when a properties file is invalid."""


class NoSuchPropertyError(Error):
  """An exception to be raised when the desired property does not exist."""


class MissingInstallationConfig(Error):
  """An exception to be raised when the sdk root does not exist."""

  def __init__(self):
    super(MissingInstallationConfig, self).__init__(
        'Installation properties could not be set because the installation '
        'root of the Cloud SDK could not be found.')


class InvalidScopeValueError(Error):
  """Exception for when a string could not be parsed to a valid scope value."""

  def __init__(self, given):
    """Constructs a new exception.

    Args:
      given: str, The given string that could not be parsed.
    """
    super(InvalidScopeValueError, self).__init__(
        'Could not parse [{0}] into a valid configuration scope.  '
        'Valid values are [{1}]'.format(given,
                                        ', '.join(Scope.AllScopeNames())))


class InvalidValueError(Error):
  """An exception to be raised when the set value of a property is invalid."""


class InvalidProjectError(InvalidValueError):
  """An exception for bad project names, with a little user help."""

  def __init__(self, given):
    super(InvalidProjectError, self).__init__(given + '\n' + _SET_PROJECT_HELP)


class RequiredPropertyError(Error):
  """Generic exception for when a required property was not set."""
  FLAG_STRING = ('It can be set on a per-command basis by re-running your '
                 'command with the [{flag}] flag.\n\n')

  def __init__(self, prop, flag=None, extra_msg=None):
    if prop.section != VALUES.default_section.name:
      section = prop.section + '/'
    else:
      section = ''

    if flag:
      flag_msg = RequiredPropertyError.FLAG_STRING.format(flag=flag)
    else:
      flag_msg = ''

    msg = ("""\
The required property [{property_name}] is not currently set.
{flag_msg}You may set it for your current workspace by running:

  $ gcloud config set {section}{property_name} VALUE

or it can be set temporarily by the environment variable [{env_var}]""".format(
    property_name=prop.name,
    flag_msg=flag_msg,
    section=section,
    env_var=prop.EnvironmentName()))
    if extra_msg:
      msg += '\n\n' + extra_msg
    super(RequiredPropertyError, self).__init__(msg)
    self.property = prop


class _Sections(object):
  """Represents the available sections in the properties file.

  Attributes:
    access_context_manager: Section, The section containing access context
      manager properties for the Cloud SDK.
    accessibility: Section, The section containing accessibility properties for
      the Cloud SDK.
    api_client_overrides: Section, The section containing API client override
      properties for the Cloud SDK.
    api_endpoint_overrides: Section, The section containing API endpoint
      override properties for the Cloud SDK.
    app: Section, The section containing app properties for the Cloud SDK.
    auth: Section, The section containing auth properties for the Cloud SDK.
    billing: Section, The section containing billing properties for the Cloud
      SDK.
    builds: Section, The section containing builds properties for the Cloud SDK.
    component_manager: Section, The section containing properties for the
      component_manager.
    composer: Section, The section containing composer properties for the Cloud
      SDK.
    compute: Section, The section containing compute properties for the Cloud
      SDK.
    container: Section, The section containing container properties for the
      Cloud SDK.
    core: Section, The section containing core properties for the Cloud SDK.
    dataproc: Section, The section containing dataproc properties for the Cloud
      SDK.
    default_section: Section, The main section of the properties file (core).
    deployment_manager: Section, The section containing deployment_manager
      properties for the Cloud SDK.
    devshell: Section, The section containing devshell properties for the Cloud
      SDK.
    diagnostics: Section, The section containing diagnostics properties for the
      Cloud SDK.
    emulator: Section, The section containing emulator properties for the Cloud
      SDK.
    experimental: Section, The section containing experimental properties for
      the Cloud SDK.
    filestore: Section, The section containing filestore properties for the
      Cloud SDK.
    functions: Section, The section containing functions properties for the
      Cloud SDK.
    gcloudignore: Section, The section containing gcloudignore properties for
      the Cloud SDK.
    healthcare: Section, The section containing healthcare properties for the
      Cloud SDK.
    interactive: Section, The section containing interactive properties for the
      Cloud SDK.
    metrics: Section, The section containing metrics properties for the Cloud
      SDK.
    ml_engine: Section, The section containing ml_engine properties for the
      Cloud SDK.
    proxy: Section, The section containing proxy properties for the Cloud SDK.
    pubsub: Section, The section containing pubsub properties for the Cloud SDK.
    redis: Section, The section containing redis properties for the Cloud SDK.
    run: Section, The section containing run properties for the Cloud SDK.
    spanner: Section, The section containing spanner properties for the Cloud
      SDK.
    storage: Section, The section containing storage properties for the Cloud
      SDK.
    test: Section, The section containing test properties for the Cloud SDK.
  """

  class _ValueFlag(object):

    def __init__(self, value, flag):
      self.value = value
      self.flag = flag

  def __init__(self):
    self.access_context_manager = _SectionAccessContextManager()
    self.accessibility = _SectionAccessibility()
    self.api_client_overrides = _SectionApiClientOverrides()
    self.api_endpoint_overrides = _SectionApiEndpointOverrides()
    self.app = _SectionApp()
    self.auth = _SectionAuth()
    self.billing = _SectionBilling()
    self.builds = _SectionBuilds()
    self.component_manager = _SectionComponentManager()
    self.composer = _SectionComposer()
    self.compute = _SectionCompute()
    self.container = _SectionContainer()
    self.core = _SectionCore()
    self.dataproc = _SectionDataproc()
    self.deployment_manager = _SectionDeploymentManager()
    self.devshell = _SectionDevshell()
    self.diagnostics = _SectionDiagnostics()
    self.emulator = _SectionEmulator()
    self.experimental = _SectionExperimental()
    self.filestore = _SectionFilestore()
    self.functions = _SectionFunctions()
    self.gcloudignore = _SectionGcloudignore()
    self.healthcare = _SectionHealthcare()
    self.interactive = _SectionInteractive()
    self.metrics = _SectionMetrics()
    self.ml_engine = _SectionMlEngine()
    self.proxy = _SectionProxy()
    self.pubsub = _SectionPubsub()
    self.redis = _SectionRedis()
    self.run = _SectionRun()
    self.spanner = _SectionSpanner()
    self.storage = _SectionStorage()
    self.test = _SectionTest()

    sections = [
        self.access_context_manager,
        self.accessibility,
        self.api_client_overrides,
        self.api_endpoint_overrides,
        self.app,
        self.auth,
        self.billing,
        self.builds,
        self.component_manager,
        self.composer,
        self.compute,
        self.container,
        self.core,
        self.dataproc,
        self.deployment_manager,
        self.devshell,
        self.diagnostics,
        self.emulator,
        self.experimental,
        self.filestore,
        self.functions,
        self.gcloudignore,
        self.healthcare,
        self.interactive,
        self.metrics,
        self.ml_engine,
        self.proxy,
        self.redis,
        self.run,
        self.spanner,
        self.test,
    ]
    self.__sections = {section.name: section for section in sections}
    self.__invocation_value_stack = [{}]

  @property
  def default_section(self):
    return self.core

  def __iter__(self):
    return iter(self.__sections.values())

  def PushInvocationValues(self):
    self.__invocation_value_stack.append({})

  def PopInvocationValues(self):
    self.__invocation_value_stack.pop()

  def SetInvocationValue(self, prop, value, flag):
    """Set the value of this property for this command, using a flag.

    Args:
      prop: _Property, The property with an explicit value.
      value: str, The value that should be returned while this command is
        running.
      flag: str, The flag that a user can use to set the property, reported if
        it was required at some point but not set by the command line.
    """
    value_flags = self.GetLatestInvocationValues()
    if value:
      prop.Validate(value)
    value_flags[prop] = _Sections._ValueFlag(value, flag)

  def GetLatestInvocationValues(self):
    return self.__invocation_value_stack[-1]

  def GetInvocationStack(self):
    return self.__invocation_value_stack

  def Section(self, section):
    """Gets a section given its name.

    Args:
      section: str, The section for the desired property.

    Returns:
      Section, The section corresponding to the given name.

    Raises:
      NoSuchPropertyError: If the section is not known.
    """
    try:
      return self.__sections[section]
    except KeyError:
      raise NoSuchPropertyError(
          'Section "{section}" does not exist.'.format(section=section))

  def AllSections(self, include_hidden=False):
    """Gets a list of all registered section names.

    Args:
      include_hidden: bool, True to include hidden properties in the result.

    Returns:
      [str], The section names.
    """
    return [
        name for name, value in six.iteritems(self.__sections)
        if not value.is_hidden or include_hidden
    ]

  def AllValues(self,
                list_unset=False,
                include_hidden=False,
                properties_file=None,
                only_file_contents=False):
    """Gets the entire collection of property values for all sections.

    Args:
      list_unset: bool, If True, include unset properties in the result.
      include_hidden: bool, True to include hidden properties in the result. If
        a property has a value set but is hidden, it will be included regardless
        of this setting.
      properties_file: PropertyFile, the file to read settings from.  If None
        the active property file will be used.
      only_file_contents: bool, True if values should be taken only from the
        properties file, false if flags, env vars, etc. should be consulted too.
        Mostly useful for listing file contents.

    Returns:
      {str:{str:str}}, A dict of sections to dicts of properties to values.
    """
    result = {}
    for section in self:
      section_result = section.AllValues(
          list_unset=list_unset,
          include_hidden=include_hidden,
          properties_file=properties_file,
          only_file_contents=only_file_contents)
      if section_result:
        result[section.name] = section_result
    return result

  def GetHelpString(self):
    """Gets a string with the help contents for all properties and descriptions.

    Returns:
      str, The string for the man page section.
    """
    messages = []
    sections = [self.default_section]
    default_section_name = self.default_section.name
    sections.extend(
        sorted([
            s for name, s in six.iteritems(self.__sections)
            if name != default_section_name and not s.is_hidden
        ]))
    for section in sections:
      props = sorted([p for p in section if not p.is_hidden])
      if not props:
        continue
      messages.append('_{section}_::'.format(section=section.name))
      for prop in props:
        messages.append('*{prop}*:::\n\n{text}'.format(
            prop=prop.name, text=prop.help_text))
    return '\n\n\n'.join(messages)


class _Section(object):
  """Represents a section of the properties file that has related properties.

  Attributes:
    name: str, The name of the section.
    is_hidden: bool, True if the section is hidden, False otherwise.
  """

  def __init__(self, name, hidden=False):
    self.__name = name
    self.__is_hidden = hidden
    self.__properties = {}

  @property
  def name(self):
    return self.__name

  @property
  def is_hidden(self):
    return self.__is_hidden

  def __iter__(self):
    return iter(self.__properties.values())

  def __hash__(self):
    return hash(self.name)

  def __eq__(self, other):
    return self.name == other.name

  def __ne__(self, other):
    return self.name != other.name

  def __gt__(self, other):
    return self.name > other.name

  def __ge__(self, other):
    return self.name >= other.name

  def __lt__(self, other):
    return self.name < other.name

  def __le__(self, other):
    return self.name <= other.name

  def _Add(self,  # pylint: disable=missing-docstring
           name,
           help_text=None,
           internal=False,
           hidden=False,
           callbacks=None,
           default=None,
           validator=None,
           choices=None,
           completer=None):
    prop = _Property(
        section=self.__name,
        name=name,
        help_text=help_text,
        internal=internal,
        hidden=(self.is_hidden or hidden),
        callbacks=callbacks,
        default=default,
        validator=validator,
        choices=choices,
        completer=completer)
    self.__properties[name] = prop
    return prop

  def _AddBool(self,
               name,
               help_text=None,
               internal=False,
               hidden=False,
               callbacks=None,
               default=None):
    return self._Add(
        name=name,
        help_text=help_text,
        internal=internal,
        hidden=hidden,
        callbacks=callbacks,
        default=default,
        validator=functools.partial(_BooleanValidator, name),
        choices=('true', 'false'))

  def Property(self, property_name):
    """Gets a property from this section, given its name.

    Args:
      property_name: str, The name of the desired property.

    Returns:
      Property, The property corresponding to the given name.

    Raises:
      NoSuchPropertyError: If the property is not known for this section.
    """
    try:
      return self.__properties[property_name]
    except KeyError:
      raise NoSuchPropertyError('Section [{s}] has no property [{p}].'.format(
          s=self.__name, p=property_name))

  def HasProperty(self, property_name):
    """True iff section has given property.

    Args:
      property_name: str, The name of the property to check for membership.

    Returns:
      a boolean. True iff this section contains property_name.
    """
    return property_name in self.__properties

  def AllProperties(self, include_hidden=False):
    """Gets a list of all registered property names in this section.

    Args:
      include_hidden: bool, True to include hidden properties in the result.

    Returns:
      [str], The property names.
    """
    return [
        name for name, prop in six.iteritems(self.__properties)
        if include_hidden or not prop.is_hidden
    ]

  def AllValues(self,
                list_unset=False,
                include_hidden=False,
                properties_file=None,
                only_file_contents=False):
    """Gets all the properties and their values for this section.

    Args:
      list_unset: bool, If True, include unset properties in the result.
      include_hidden: bool, True to include hidden properties in the result. If
        a property has a value set but is hidden, it will be included regardless
        of this setting.
      properties_file: properties_file.PropertiesFile, the file to read settings
        from.  If None the active property file will be used.
      only_file_contents: bool, True if values should be taken only from the
        properties file, false if flags, env vars, etc. should be consulted too.
        Mostly useful for listing file contents.

    Returns:
      {str:str}, The dict of {property:value} for this section.
    """
    properties_file = (
        properties_file or named_configs.ActivePropertiesFile.Load())

    result = {}
    for prop in self:
      if prop.is_internal:
        # Never show internal properties, ever.
        continue
      if (prop.is_hidden and not include_hidden and
          _GetPropertyWithoutCallback(prop, properties_file) is None):
        continue

      if only_file_contents:
        value = properties_file.Get(prop.section, prop.name)
      else:
        value = _GetPropertyWithoutDefault(prop, properties_file)

      if value is None:
        if not list_unset:
          # Never include if not set and not including unset values.
          continue
        if prop.is_hidden and not include_hidden:
          # If including unset values, exclude if hidden and not including
          # hidden properties.
          continue

      # Always include if value is set (even if hidden)
      result[prop.name] = value
    return result


class _SectionRun(_Section):
  """Contains the properties for the 'run' section."""

  def __init__(self):
    super(_SectionRun, self).__init__('run', hidden=True)
    self.region = self._Add(
        'region',
        default='us-central1',
        help_text='The default region to use when working with Google '
        'Run resources. When a `--region` flag is required '
        'but not provided, the command will fall back to this value, if set. '
        'To see valid choices, run `gcloud run regions list`.')

    self.namespace = self._Add(
        'namespace',
        help_text='Kubernetes namespace to create Run resources in.',
        hidden=True)

    self.cluster = self._Add(
        'cluster',
        help_text='GKE cluster to use. '
        'Knative must be installed in this cluster.')

    self.cluster_location = self._Add(
        'cluster_location',
        help_text='Zone or region of the GKE cluster to use.')


class _SectionSpanner(_Section):
  """Contains the properties for the 'spanner' section."""

  def __init__(self):
    super(_SectionSpanner, self).__init__('spanner')
    self.instance = self._Add(
        'instance',
        help_text='Default instance to use when working with Cloud Spanner '
        'resources. When an instance is required but not provided by a flag, '
        'the command will fall back to this value, if set.',
        completer='googlecloudsdk.command_lib.spanner.flags:InstanceCompleter')


class _SectionCompute(_Section):
  """Contains the properties for the 'compute' section."""

  def __init__(self):
    super(_SectionCompute, self).__init__('compute')
    self.zone = self._Add(
        'zone',
        help_text='Default zone to use when working with zonal Compute '
        'Engine resources. When a `--zone` flag is required but not provided, '
        'the command will fall back to this value, if set. To see valid '
        'choices, run `gcloud compute zones list`.',
        completer=('googlecloudsdk.command_lib.compute.completers:'
                   'ZonesCompleter'))
    self.region = self._Add(
        'region',
        help_text='Default region to use when working with regional Compute'
        ' Engine resources. When a `--region` flag is required but not '
        'provided, the command will fall back to this value, if set. To see '
        'valid choices, run `gcloud compute regions list`.',
        completer=('googlecloudsdk.command_lib.compute.completers:'
                   'RegionsCompleter'))
    self.gce_metadata_read_timeout_sec = self._Add(
        'gce_metadata_read_timeout_sec', default=1, hidden=True)
    self.use_new_list_usable_subnets_api = self._AddBool(
        'use_new_list_usable_subnets_api',
        default=False,
        help_text=(
            'If True, use the new API for listing usable subnets which only '
            'returns subnets in the current project.'))


class _SectionFunctions(_Section):
  """Contains the properties for the 'functions' section."""

  def __init__(self):
    super(_SectionFunctions, self).__init__('functions')
    self.region = self._Add(
        'region',
        default='us-central1',
        help_text='Default region to use when working with Cloud '
        'Functions resources. When a `--region` flag is required but not '
        'provided, the command will fall back to this value, if set. To see '
        'valid choices, run `gcloud beta functions regions list`.',
        completer=('googlecloudsdk.command_lib.functions.flags:'
                   'LocationsCompleter'))


class _SectionGcloudignore(_Section):
  """Contains the properties for the 'gcloudignore' section."""

  def __init__(self):
    super(_SectionGcloudignore, self).__init__('gcloudignore')
    self.enabled = self._AddBool(
        'enabled',
        default=True,
        help_text=(
            'If True, do not upload `.gcloudignore` files (see `$ gcloud topic '
            'gcloudignore`). If False, turn off the gcloudignore mechanism '
            'entirely and upload all files.'))


class _SectionHealthcare(_Section):
  """Contains the properties for the 'healthcare' section."""

  def __init__(self):
    super(_SectionHealthcare, self).__init__('healthcare')
    self.location = self._Add(
        'location',
        default='us-central1',
        help_text='Default location to use when working with Cloud Healthcare  '
        'resources. When a `--location` flag is required but not provided, the  '
        'command will fall back to this value.')
    self.dataset = self._Add(
        'dataset',
        help_text='Default dataset to use when working with Cloud Healthcare '
        'resources. When a `--dataset` flag is required but not provided, the '
        'command will fall back to this value, if set.')


class _SectionAccessibility(_Section):
  """Contains the properties for the 'accessibility' section."""

  def __init__(self):
    super(_SectionAccessibility, self).__init__('accessibility')
    self.screen_reader = self._AddBool(
        'screen_reader',
        default=False,
        help_text='Make gcloud more screen reader friendly.')


class _SectionApp(_Section):
  """Contains the properties for the 'app' section."""

  def __init__(self):
    super(_SectionApp, self).__init__('app')
    self.promote_by_default = self._AddBool(
        'promote_by_default',
        help_text='If True, when deploying a new version of a service, that '
        'version will be promoted to receive all traffic for the service. '
        'This property can be overridden via the `--promote-by-default` or '
        '`--no-promote-by-default` flags.',
        default=True)
    self.stop_previous_version = self._AddBool(
        'stop_previous_version',
        help_text='If True, when deploying a new version of a service, the '
        'previously deployed version is stopped. If False, older versions must '
        'be stopped manually.',
        default=True)
    self.trigger_build_server_side = self._AddBool(
        'trigger_build_server_side', hidden=True, default=None)
    self.cloud_build_timeout = self._Add(
        'cloud_build_timeout',
        validator=_BuildTimeoutValidator,
        help_text='Timeout, in seconds, to wait for Docker builds to '
        'complete during deployments. All Docker builds now use the '
        'Cloud Build API.')
    self.container_builder_image = self._Add(
        'container_builder_image',
        default='gcr.io/cloud-builders/docker',
        hidden=True)
    self.use_appengine_api = self._AddBool(
        'use_appengine_api', default=True, hidden=True)
    # This property is currently ignored except on OS X Sierra or beta
    # deployments.
    # There's a theoretical benefit to exceeding the number of cores available,
    # since the task is bound by network/API latency among other factors, and
    # mini-benchmarks validated this (I got speedup from 4 threads to 8 on a
    # 4-core machine).
    self.num_file_upload_threads = self._Add(
        'num_file_upload_threads', default=None, hidden=True)

    def GetRuntimeRoot():
      sdk_root = config.Paths().sdk_root
      if sdk_root is None:
        return None
      else:
        return os.path.join(config.Paths().sdk_root, 'platform', 'ext-runtime')

    self.runtime_root = self._Add(
        'runtime_root', callbacks=[GetRuntimeRoot], hidden=True)

    # Whether or not to use the (currently under-development) Flex Runtime
    # Builders, as opposed to Externalized Runtimes.
    #   True  => ALWAYS
    #   False => NEVER
    #   Unset => default behavior, which varies between beta/GA commands
    self.use_runtime_builders = self._Add(
        'use_runtime_builders',
        default=None,
        help_text=('If set, opt in/out to a new code path for building '
                   'applications using pre-fabricated runtimes that can be '
                   'updated independently of client tooling. If not set, '
                   'the default path for each runtime is used.'))
    # The Cloud Storage path prefix for the Flex Runtime Builder configuration
    # files. The configuration files will live at
    # "<PREFIX>/<runtime>-<version>.yaml", with an additional
    # "<PREFIX>/runtime.version" indicating the latest version.
    self.runtime_builders_root = self._Add(
        'runtime_builders_root', default='gs://runtime-builders/', hidden=True)


class _SectionBuilds(_Section):
  """Contains the properties for the 'builds' section."""

  def __init__(self):
    super(_SectionBuilds, self).__init__('builds')

    self.timeout = self._Add(
        'timeout',
        validator=_BuildTimeoutValidator,
        help_text='Timeout, in seconds, to wait for builds to complete.')
    self.check_tag = self._AddBool(
        'check_tag',
        default=True,
        hidden=True,
        help_text='If True, validate that the --tag value to builds '
        'submit is in the gcr.io or *.gcr.io namespace.')
    # TODO(b/118509363): Remove this after its default is True.
    self.use_kaniko = self._AddBool(
        'use_kaniko',
        default=False,
        help_text='If True, kaniko will be used to build images described by '
        'a Dockerfile, instead of `docker build`.')
    self.kaniko_cache_ttl = self._Add(
        'kaniko_cache_ttl',
        default=6,
        help_text='TTL, in hours, of cached layers when using Kaniko. If zero, '
        'layer caching is disabled.')
    self.kaniko_image = self._Add(
        'kaniko_image',
        default='gcr.io/kaniko-project/executor:latest',
        hidden=True,
        help_text='Kaniko builder image to use when use_kaniko=True. Defaults '
        'to gcr.io/kaniko-project/executor:latest')


class _SectionContainer(_Section):
  """Contains the properties for the 'container' section."""

  def __init__(self):
    super(_SectionContainer, self).__init__('container')
    self.cluster = self._Add(
        'cluster',
        help_text='Name of the cluster to use by default when '
        'working with Kubernetes Engine.')
    self.use_client_certificate = self._AddBool(
        'use_client_certificate',
        default=False,
        help_text='If True, use the cluster\'s client certificate to '
        'authenticate to the cluster API server.')
    self.use_app_default_credentials = self._AddBool(
        'use_application_default_credentials',
        default=False,
        help_text='If True, use application default credentials to authenticate'
        ' to the cluster API server.')
    self.new_scopes_behavior = self._AddBool(
        'new_scopes_behavior',
        default=False,
        help_text='If True, use new scopes behavior and do not add `compute-rw`'
        ', `storage-ro`, `service-control`, or `service-management` scopes. The'
        ' former two (`compute-rw` and `storage-ro`) only apply to clusters '
        'at Kubernetes v1.9 and below; starting v1.10, `compute-rw` and '
        '`storage-ro` are not added by default. Any of these scopes '
        'may be added explicitly using `--scopes`. Using new scopes behavior '
        'will be the default in a future release. Additionally, if '
        'this property is set to True, using `--[no-]enable-cloud-endpoints` '
        'is not allowed. This property is ignored in alpha and beta, since '
        'these tracks always use the new behavior. See `--scopes` help for '
        'more info.')

    self.build_timeout = self._Add(
        'build_timeout',
        validator=_BuildTimeoutValidator,
        help_text='Timeout, in seconds, to wait for container builds to '
        'complete.')
    self.build_check_tag = self._AddBool(
        'build_check_tag',
        default=True,
        hidden=True,
        help_text='If True, validate that the --tag value to container builds '
        'submit is in the gcr.io or *.gcr.io namespace.')


class _SectionCore(_Section):
  """Contains the properties for the 'core' section."""

  class InteractiveUXStyles(enum.Enum):
    NORMAL = 0
    OFF = 1
    TESTING = 2

  def __init__(self):
    super(_SectionCore, self).__init__('core')
    self.account = self._Add(
        'account',
        help_text='Account `gcloud` should use for authentication. '
        'Run `gcloud auth list` to see your currently available accounts.')
    self.disable_collection_path_deprecation_warning = self._AddBool(
        'disable_collection_path_deprecation_warning',
        hidden=True,
        help_text='If False, any usage of collection paths will result in '
        'deprecation warning. Set it to False to disable it.')
    self.default_regional_backend_service = self._AddBool(
        'default_regional_backend_service',
        help_text='If True, backend services in `gcloud compute '
        'backend-services` will be regional by default. Setting the `--global` '
        'flag is required for global backend services.')
    self.disable_color = self._AddBool(
        'disable_color',
        help_text='If True, color will not be used when printing messages in '
        'the terminal.')
    self.disable_command_lazy_loading = self._AddBool(
        'disable_command_lazy_loading', hidden=True)
    self.disable_prompts = self._AddBool(
        'disable_prompts',
        help_text='If True, the default answer will be assumed for all user '
        'prompts. However, for any prompts that require user input, an error '
        'will be raised. This is equivalent to either using the global '
        '`--quiet` flag or setting the environment variable '
        '`CLOUDSDK_CORE_DISABLE_PROMPTS` to 1. Setting this property is '
        'useful when scripting with `gcloud`.')
    self.disable_usage_reporting = self._AddBool(
        'disable_usage_reporting',
        help_text='If True, anonymous statistics on SDK usage will not be '
        'collected.  This value is set by default based on your choices during '
        'installation, but can be changed at any time.  For more information, '
        'see: https://cloud.google.com/sdk/usage-statistics')
    self.enable_gri = self._AddBool(
        'enable_gri',
        default=False,
        hidden=True,
        help_text='If True, the parser for gcloud Resource Identifiers will be'
        'enabled when interpreting resource arguments.')
    self.resource_completion_style = self._Add(
        'resource_completion_style',
        choices=('flags', 'gri'),
        default='flags',
        hidden=True,
        help_text='The resource completion style controls how resource strings '
        'are represented in command argument completions.  All styles, '
        'including uri, are handled on input.')
    self.lint = self._Add(
        'lint',
        # Current runtime lint patterns. Delete from this comment when the
        # pattern usage has been deleted.
        #
        #   AddCacheUpdaters: Throws an exception for each command that needs
        #     a parser.display_info.AddCacheUpdater() call.
        #
        # When running tests set default=PATTERN[,PATTERN...] here to weed out
        # all occurrences of the patterns. Patterns are checked using substring
        # matching on the lint property string value:
        #
        #   if 'AddCacheUpdaters' in properties.VALUES.core.lint.Get():
        #     # AddCacheUpdaters lint check enabled.
        default='none',
        hidden=True,
        help_text='Enable the runtime linter for specific patterns. '
        'Each occurrence of a runtime pattern raises an exception. '
        'The pattern names are source specific. Consult the source for '
        'details.')
    self.api_host = self._Add(
        'api_host', hidden=True, default='https://www.googleapis.com')
    self.verbosity = self._Add(
        'verbosity',
        help_text='Default logging verbosity for `gcloud` commands.  This is '
        'the equivalent of using the global `--verbosity` flag. Supported '
        'verbosity levels: `debug`, `info`, `warning`, `error`, and `none`.')
    self.user_output_enabled = self._AddBool(
        'user_output_enabled',
        help_text='True, by default. If False, messages to the user and command'
        ' output on both standard output and standard error will be'
        ' suppressed.',
        default=True)
    self.interactive_ux_style = self._Add(
        'interactive_ux_style',
        help_text='How to display interactive UX elements like progress bars '
        'and trackers.',
        hidden=True,
        default=_SectionCore.InteractiveUXStyles.NORMAL,
        choices=[x.name for x in list(_SectionCore.InteractiveUXStyles)])
    self.log_http = self._AddBool(
        'log_http',
        help_text='If True, log HTTP requests and responses to the logs.  '
        'To see logs in the terminal, adjust `verbosity` settings. '
        'Otherwise, logs are available in their respective log files.',
        default=False)
    self.log_http_redact_token = self._AddBool(
        'log_http_redact_token',
        help_text='If true, this prevents log_http from printing access tokens.'
        ' This property does not have effect unless log_http is true.',
        default=True,
        hidden=True)
    self.http_timeout = self._Add('http_timeout', hidden=True)
    self.check_gce_metadata = self._AddBool(
        'check_gce_metadata', hidden=True, default=True)
    self.print_completion_tracebacks = self._AddBool(
        'print_completion_tracebacks',
        hidden=True,
        help_text='If True, print actual completion exceptions with traceback '
        'instead of the nice UX scrubbed exceptions.')
    self.print_unhandled_tracebacks = self._AddBool(
        'print_unhandled_tracebacks', hidden=True)
    self.print_handled_tracebacks = self._AddBool(
        'print_handled_tracebacks', hidden=True)
    self.trace_token = self._Add(
        'trace_token',
        help_text='Token used to route traces of service requests for '
        'investigation of issues. This token will be provided by Google '
        'support.')
    self.trace_email = self._Add('trace_email', hidden=True)
    self.trace_log = self._Add('trace_log', default=False, hidden=True)
    self.request_reason = self._Add('request_reason', hidden=True)
    self.pass_credentials_to_gsutil = self._AddBool(
        'pass_credentials_to_gsutil',
        default=True,
        help_text='If True, pass the configured Cloud SDK authentication '
        'to gsutil.')
    self.api_key = self._Add(
        'api_key',
        hidden=True,
        help_text='If provided, this API key is attached to all outgoing '
        'API calls.')
    self.should_prompt_to_enable_api = self._AddBool(
        'should_prompt_to_enable_api',
        default=True,
        hidden=True,
        help_text='If true, will prompt to enable an API if a command fails due'
        ' to the API not being enabled.')
    self.allow_py3 = self._AddBool(
        'allow_py3',
        default=True,
        hidden=True,
        help_text='If true, allow a Python 3 interpreter to run gcloud.')
    self.color_theme = self._Add(
        'color_theme',
        help_text='Color palette for output.',
        hidden=True,
        default='off',
        choices=['off', 'normal', 'testing'])

    def ShowStructuredLogsValidator(show_structured_logs):
      if show_structured_logs is None:
        return
      if show_structured_logs not in ['always', 'log', 'terminal', 'never']:
        raise InvalidValueError(('show_structured_logs must be one of: '
                                 '[always, log, terminal, never]'))

    self.show_structured_logs = self._Add(
        'show_structured_logs',
        choices=['always', 'log', 'terminal', 'never'],
        default='never',
        hidden=False,
        validator=ShowStructuredLogsValidator,
        help_text="""\
        Control when JSON-structured log messages for the current verbosity
        level (and above) will be written to standard error. If this property
        is disabled, logs are formatted as `text` by default.

        Valid values are:
            *   `never` - Log messages as text
            *   `always` - Always log messages as JSON
            *   `log` - Only log messages as JSON if stderr is a file
            *    `terminal` - Only log messages as JSON if stderr is a terminal

        If unset, default is `never`.""")

    def MaxLogDaysValidator(max_log_days):
      if max_log_days is None:
        return
      try:
        if int(max_log_days) < 0:
          raise InvalidValueError('Max number of days must be at least 0')
      except ValueError:
        raise InvalidValueError('Max number of days must be an integer')

    self.max_log_days = self._Add(
        'max_log_days',
        validator=MaxLogDaysValidator,
        help_text='Maximum number of days to retain log files before deleting.'
        ' If set to 0, turns off log garbage collection and does not delete log'
        ' files. If unset, the default is 30 days.',
        default='30')

    def ExistingAbsoluteFilepathValidator(file_path):
      """Checks to see if the file path exists and is an absolute path."""
      if file_path is None:
        return
      if not os.path.isfile(file_path):
        raise InvalidValueError('The provided path must exist.')
      if not os.path.isabs(file_path):
        raise InvalidValueError('The provided path must be absolute.')

    self.custom_ca_certs_file = self._Add(
        'custom_ca_certs_file',
        validator=ExistingAbsoluteFilepathValidator,
        help_text='Absolute path to a custom CA cert file.')

    def ProjectValidator(project):
      """Checks to see if the project string is valid."""
      if project is None:
        return

      if not isinstance(project, six.string_types):
        raise InvalidValueError('project must be a string')
      if project == '':  # pylint: disable=g-explicit-bool-comparison
        raise InvalidProjectError('The project property is set to the '
                                  'empty string, which is invalid.')
      if project.isdigit():
        raise InvalidProjectError(
            'The project property must be set to a valid project ID, not the '
            'project number [{value}]'.format(value=project))

      if _VALID_PROJECT_REGEX.match(project):
        return

      if _LooksLikeAProjectName(project):
        raise InvalidProjectError(
            'The project property must be set to a valid project ID, not the '
            'project name [{value}]'.format(value=project))
      # Non heuristics for a better error message.
      raise InvalidProjectError(
          'The project property must be set to a valid project ID, '
          '[{value}] is not a valid project ID.'.format(value=project))

    self.project = self._Add(
        'project',
        help_text='Project ID of the Cloud Platform project to operate on '
        'by default.  This can be overridden by using the global `--project` '
        'flag.',
        validator=ProjectValidator,
        completer=('googlecloudsdk.command_lib.resource_manager.completers:'
                   'ProjectCompleter'))
    self.credentialed_hosted_repo_domains = self._Add(
        'credentialed_hosted_repo_domains', hidden=True)


class _SectionAuth(_Section):
  """Contains the properties for the 'auth' section."""

  def __init__(self):
    super(_SectionAuth, self).__init__('auth')
    self.auth_host = self._Add(
        'auth_host',
        hidden=True,
        default='https://accounts.google.com/o/oauth2/auth')
    self.disable_credentials = self._AddBool(
        'disable_credentials',
        default=False,
        help_text='If True, `gcloud` will not attempt to load any credentials '
        'or authenticate any requests. This is useful when behind a proxy '
        'that adds authentication to requests.')
    self.token_host = self._Add(
        'token_host',
        hidden=True,
        default='https://www.googleapis.com/oauth2/v4/token')
    self.disable_ssl_validation = self._AddBool(
        'disable_ssl_validation', hidden=True)
    self.client_id = self._Add(
        'client_id', hidden=True, default=config.CLOUDSDK_CLIENT_ID)
    self.client_secret = self._Add(
        'client_secret',
        hidden=True,
        default=config.CLOUDSDK_CLIENT_NOTSOSECRET)
    self.authority_selector = self._Add('authority_selector', hidden=True)
    self.authorization_token_file = self._Add(
        'authorization_token_file', hidden=True)
    self.credential_file_override = self._Add(
        'credential_file_override', hidden=True)
    self.impersonate_service_account = self._Add(
        'impersonate_service_account', hidden=True)


class _SectionBilling(_Section):
  """Contains the properties for the 'auth' section."""

  LEGACY = 'LEGACY'
  CURRENT_PROJECT = 'CURRENT_PROJECT'

  def __init__(self):
    super(_SectionBilling, self).__init__('billing')

    self.quota_project = self._Add(
        'quota_project',
        default=_SectionBilling.CURRENT_PROJECT,
        help_text="""\
        Project that will be charged quota for the
        operations performed in `gcloud`. When unset, the default is
        [CURRENT_PROJECT]; this will charge quota against the currently set
        project for operations performed on it. Additionally, some existing
        APIs will continue to use a shared project for quota by default, when
        this property is unset.

        If you need to operate on one project, but
        need quota against a different project, you can use this property to
        specify the alternate project.""")


class _SectionMetrics(_Section):
  """Contains the properties for the 'metrics' section."""

  def __init__(self):
    super(_SectionMetrics, self).__init__('metrics', hidden=True)
    self.environment = self._Add('environment', hidden=True)
    self.environment_version = self._Add('environment_version', hidden=True)
    self.command_name = self._Add('command_name', internal=True)


class _SectionComponentManager(_Section):
  """Contains the properties for the 'component_manager' section."""

  def __init__(self):
    super(_SectionComponentManager, self).__init__('component_manager')
    self.additional_repositories = self._Add(
        'additional_repositories',
        help_text='Comma separated list of additional repositories to check '
        'for components.  This property is automatically managed by the '
        '`gcloud components repositories` commands.')
    self.disable_update_check = self._AddBool(
        'disable_update_check',
        help_text='If True, Cloud SDK will not automatically check for '
        'updates.')
    self.fixed_sdk_version = self._Add('fixed_sdk_version', hidden=True)
    self.snapshot_url = self._Add('snapshot_url', hidden=True)


class _SectionExperimental(_Section):
  """Contains the properties for gcloud experiments."""

  def __init__(self):
    super(_SectionExperimental, self).__init__('experimental', hidden=True)
    self.fast_component_update = self._AddBool(
        'fast_component_update', default=False)


class _SectionFilestore(_Section):
  """Contains the properties for the 'filestore' section."""

  def __init__(self):
    super(_SectionFilestore, self).__init__('filestore')
    self.location = self._Add(
        'location',
        help_text='Default location to use when working with Cloud Filestore '
        'locations. When a `--location` flag is required but not '
        'provided, the command will fall back to this value, if set.')


class _SectionTest(_Section):
  """Contains the properties for the 'test' section."""

  def __init__(self):
    super(_SectionTest, self).__init__('test')
    self.results_base_url = self._Add('results_base_url', hidden=True)
    self.matrix_status_interval = self._Add(
        'matrix_status_interval', hidden=True)


class _SectionMlEngine(_Section):
  """Contains the properties for the 'ml_engine' section."""

  def __init__(self):
    super(_SectionMlEngine, self).__init__('ml_engine')
    self.polling_interval = self._Add(
        'polling_interval',
        default=60,
        help_text=('Interval (in seconds) at which to poll logs from your '
                   'Cloud ML Engine jobs. Note that making it much faster than '
                   'the default (60) will quickly use all of your quota.'))
    self.local_python = self._Add(
        'local_python',
        default=None,
        help_text=('Full path to the Python interpreter to use for '
                   'Cloud ML Engine local predict/train jobs. If not '
                   'specified, the default path is the one to the Python '
                   'interpreter found on system `PATH`.'))


class _SectionPubsub(_Section):
  """Contains the properties for the 'pubsub' section."""

  def __init__(self):
    super(_SectionPubsub, self).__init__('pubsub')
    self.legacy_output = self._AddBool(
        'legacy_output',
        default=False,
        help_text=('Use the legacy output for beta pubsub commands. The legacy '
                   'output from beta is being deprecated. This property will '
                   'eventually be removed.'))


class _SectionComposer(_Section):
  """Contains the properties for the 'composer' section."""

  def __init__(self):
    super(_SectionComposer, self).__init__('composer')
    self.location = self._Add(
        'location',
        help_text=(
            'Composer location to use. Each Composer location '
            'constitutes an independent resource namespace constrained to '
            'deploying environments into Compute Engine regions inside this '
            'location. This parameter corresponds to the '
            '/locations/<location> segment of the Composer resource URIs being '
            'referenced.'))


class _SectionDataproc(_Section):
  """Contains the properties for the 'ml_engine' section."""

  def __init__(self):
    super(_SectionDataproc, self).__init__('dataproc')
    self.region = self._Add(
        'region',
        default='global',
        help_text=(
            'Cloud Dataproc region to use. Each Cloud Dataproc '
            'region constitutes an independent resource namespace constrained '
            'to deploying instances into Compute Engine zones inside '
            'the region. The default value of `global` is a special '
            'multi-region namespace which is capable of deploying instances '
            'into all Compute Engine zones globally, and is disjoint '
            'from other Cloud Dataproc regions.'))


class _SectionDeploymentManager(_Section):
  """Contains the properties for the 'deployment_manager' section."""

  def __init__(self):
    super(_SectionDeploymentManager, self).__init__('deployment_manager')
    self.glob_imports = self._AddBool(
        'glob_imports',
        default=False,
        help_text=(
            'Enable import path globbing. Uses glob patterns to match multiple '
            'imports in a config file.'))


class _SectionInteractive(_Section):
  """Contains the properties for the 'interactive' section."""

  def __init__(self):
    super(_SectionInteractive, self).__init__('interactive')
    self.bottom_bindings_line = self._AddBool(
        'bottom_bindings_line',
        default=True,
        help_text='If True, display the bottom key bindings line.')
    self.bottom_status_line = self._AddBool(
        'bottom_status_line',
        default=False,
        help_text='If True, display the bottom status line.')
    self.completion_menu_lines = self._Add(
        'completion_menu_lines',
        default=4,
        help_text='Number of lines in the completion menu.')
    self.context = self._Add(
        'context', default='', help_text='Command context string.')
    self.debug = self._AddBool(
        'debug',
        default=False,
        hidden=True,
        help_text='If True, enable the debugging display.')
    self.fixed_prompt_position = self._Add(
        'fixed_prompt_position',
        default=False,
        help_text='If True, display the prompt at the same position.')
    self.help_lines = self._Add(
        'help_lines',
        default=10,
        help_text='Maximum number of help snippet lines.')
    self.hidden = self._AddBool(
        'hidden',
        default=False,
        help_text='If True, expose hidden commands/flags.')
    self.justify_bottom_lines = self._AddBool(
        'justify_bottom_lines',
        default=False,
        help_text='If True, left- and right-justify bottom toolbar lines.')
    self.manpage_generator = self._Add(
        'manpage_generator',
        default=True,
        help_text=('If True, use the manpage CLI tree generator for '
                   'unsupported commands.'))
    self.multi_column_completion_menu = self._AddBool(
        'multi_column_completion_menu',
        default=False,
        help_text='If True, display the completions as a multi-column menu.')
    self.obfuscate = self._AddBool(
        'obfuscate',
        default=False,
        hidden=True,
        help_text='If True, obfuscate status PII.')
    self.prompt = self._Add(
        'prompt', default='$ ', help_text='Command prompt string.')
    self.show_help = self._AddBool(
        'show_help',
        default=True,
        help_text='If True, show help as command args are being entered.')
    self.suggest = self._AddBool(
        'suggest',
        default=False,
        help_text='If True, add command line suggestions based on history.')


class _SectionProxy(_Section):
  """Contains the properties for the 'proxy' section."""

  def __init__(self):
    super(_SectionProxy, self).__init__('proxy')
    self.address = self._Add(
        'address', help_text='Hostname or IP address of proxy server.')
    self.port = self._Add(
        'port', help_text='Port to use when connected to the proxy server.')
    self.rdns = self._Add(
        'rdns',
        default=True,
        help_text='If True, DNS queries will not be performed '
        'locally, and instead, handed to the proxy to resolve. This is default'
        ' behavior.')
    self.username = self._Add(
        'username',
        help_text='Username to use when connecting, if the proxy '
        'requires authentication.')
    self.password = self._Add(
        'password',
        help_text='Password to use when connecting, if the proxy '
        'requires authentication.')

    valid_proxy_types = sorted(http_proxy_types.PROXY_TYPE_MAP.keys())

    def ProxyTypeValidator(proxy_type):
      if proxy_type is not None and proxy_type not in valid_proxy_types:
        raise InvalidValueError(
            'The proxy type property value [{0}] is not valid. '
            'Possible values: [{1}].'.format(proxy_type,
                                             ', '.join(valid_proxy_types)))

    self.proxy_type = self._Add(
        'type',
        help_text='Type of proxy being used.  Supported proxy types are:'
        ' [{0}].'.format(', '.join(valid_proxy_types)),
        validator=ProxyTypeValidator,
        choices=valid_proxy_types)

    self.use_urllib3_via_shim = self._AddBool(
        'use_urllib3_via_shim',
        default=False,
        hidden=True,
        help_text='If True, use `urllib3` to make requests via `httplib2shim`.')


class _SectionDevshell(_Section):
  """Contains the properties for the 'devshell' section."""

  def __init__(self):
    super(_SectionDevshell, self).__init__('devshell')
    self.image = self._Add(
        'image', hidden=True, default=const_lib.DEFAULT_DEVSHELL_IMAGE)
    self.metadata_image = self._Add(
        'metadata_image', hidden=True, default=const_lib.METADATA_IMAGE)


class _SectionDiagnostics(_Section):
  """Contains the properties for the 'diagnostics' section."""

  def __init__(self):
    super(_SectionDiagnostics, self).__init__('diagnostics', hidden=True)
    self.hidden_property_whitelist = self._Add(
        'hidden_property_whitelist',
        internal=True,
        help_text=('Comma separated list of hidden properties that should be '
                   'allowed by the hidden properties diagnostic.'))


class _SectionApiEndpointOverrides(_Section):
  """Contains the properties for the 'api-endpoint-overrides' section.

  This overrides what endpoint to use when talking to the given API.
  """

  def __init__(self):
    super(_SectionApiEndpointOverrides, self).__init__(
        'api_endpoint_overrides', hidden=True)
    self.remotebuildexecution = self._Add('remotebuildexecution')
    self.accesscontextmanager = self._Add('accesscontextmanager')
    self.appengine = self._Add('appengine')
    self.bigtableadmin = self._Add('bigtableadmin')
    self.bigtableclusteradmin = self._Add('bigtableclusteradmin')
    self.binaryauthorization = self._Add('binaryauthorization')
    self.bio = self._Add('bio')
    self.categorymanager = self._Add('categorymanager')
    self.cloudbilling = self._Add('cloudbilling')
    self.cloudbuild = self._Add('cloudbuild')
    self.clouddebugger = self._Add('clouddebugger')
    self.clouderrorreporting = self._Add('clouderrorreporting')
    self.cloudfunctions = self._Add('cloudfunctions')
    self.cloudiot = self._Add('cloudiot')
    self.cloudkms = self._Add('cloudkms')
    self.cloudresourcemanager = self._Add('cloudresourcemanager')
    self.cloudresourcesearch = self._Add('cloudresourcesearch')
    self.cloudscheduler = self._Add('cloudscheduler')
    self.cloudtasks = self._Add('cloudtasks')
    self.composer = self._Add('composer')
    self.compute = self._Add('compute')
    self.container = self._Add('container')
    self.containeranalysis = self._Add('containeranalysis')
    self.dataflow = self._Add('dataflow')
    self.datapol = self._Add('datapol')
    self.dataproc = self._Add('dataproc')
    self.datastore = self._Add('datastore')
    self.deploymentmanager = self._Add('deploymentmanager')
    self.discovery = self._Add('discovery')
    self.dns = self._Add('dns')
    self.file = self._Add('file')
    self.firestore = self._Add('firestore')
    self.genomics = self._Add('genomics')
    self.gkehub = self._Add('gkehub')
    self.healthcare = self._Add('healthcare')
    self.iam = self._Add('iam')
    self.kubernetespolicy = self._Add('kubernetespolicy')
    self.language = self._Add('language')
    self.logging = self._Add('logging')
    self.manager = self._Add('manager')
    self.ml = self._Add('ml')
    self.monitoring = self._Add('monitoring')
    self.oslogin = self._Add('oslogin')
    self.pubsub = self._Add('pubsub')
    self.replicapoolupdater = self._Add('replicapoolupdater')
    self.runtimeconfig = self._Add('runtimeconfig')
    self.redis = self._Add('redis')
    self.run = self._Add('run')
    self.servicemanagement = self._Add('servicemanagement')
    self.serviceregistry = self._Add('serviceregistry')
    self.serviceusage = self._Add('serviceusage')
    self.serviceuser = self._Add('serviceuser')
    self.source = self._Add('source')
    self.sourcerepo = self._Add('sourcerepo')
    self.spanner = self._Add('spanner')
    self.speech = self._Add('speech')
    self.sql = self._Add('sql')
    self.storage = self._Add('storage')
    self.testing = self._Add('testing')
    self.toolresults = self._Add('toolresults')
    self.tpu = self._Add('tpu')
    self.vision = self._Add('vision')
    self.vpcaccess = self._Add('vpcaccess')

  def EndpointValidator(self, value):
    """Checks to see if the endpoint override string is valid."""
    if value is None:
      return
    if not _VALID_ENDPOINT_OVERRIDE_REGEX.match(value):
      raise InvalidValueError(
          'The endpoint_overrides property must be an absolute URI beginning '
          'with http:// or https:// and ending with a trailing \'/\'. '
          '[{value}] is not a valid endpoint override.'.format(value=value))

  def _Add(self, name):
    return super(_SectionApiEndpointOverrides, self)._Add(
        name, validator=self.EndpointValidator)


class _SectionApiClientOverrides(_Section):
  """Contains the properties for the 'api-client-overrides' section.

  This overrides the API client version to use when talking to this API.
  """

  def __init__(self):
    super(_SectionApiClientOverrides, self).__init__(
        'api_client_overrides', hidden=True)
    self.appengine = self._Add('appengine')
    self.compute = self._Add('compute')
    self.container = self._Add('container')
    self.sql = self._Add('sql')


class _SectionEmulator(_Section):
  """Contains the properties for the 'emulator' section.

  This is used to configure emulator properties for pubsub and datastore, such
  as host_port and data_dir.
  """

  def __init__(self):
    super(_SectionEmulator, self).__init__('emulator', hidden=True)
    self.datastore_data_dir = self._Add('datastore_data_dir')
    self.pubsub_data_dir = self._Add('pubsub_data_dir')
    self.datastore_host_port = self._Add(
        'datastore_host_port', default='localhost:8081')
    self.pubsub_host_port = self._Add(
        'pubsub_host_port', default='localhost:8085')
    self.bigtable_host_port = self._Add(
        'bigtable_host_port', default='localhost:8086')


class _SectionAccessContextManager(_Section):
  """Contains the properties for the 'access_context_manager' section."""

  def __init__(self):
    super(_SectionAccessContextManager, self).__init__(
        'access_context_manager', hidden=True)
    self.policy = self._Add(
        'policy',
        help_text=('ID of the policy resource to operate on. Can be found '
                   'by running the `access-context-manager policies list` '
                   'command.'))


class _SectionRedis(_Section):
  """Contains the properties for the 'redis' section."""

  def __init__(self):
    super(_SectionRedis, self).__init__('redis')
    self.region = self._Add(
        'region',
        help_text='Default region to use when working with Cloud '
        'Memorystore for Redis resources. When a `region` is required but not '
        'provided by a flag, the command will fall back to this value, if set.')


class _SectionStorage(_Section):
  """Contains the properties for the 'storage' section."""

  def __init__(self):
    super(_SectionStorage, self).__init__('storage')
    self.chunk_size = self._Add(
        'chunk_size',
        default=104857600,  # gsutil's default chunksize (1024 * 1024 * 100)
        help_text='Chunk size used for uploading and downloading from '
        'Cloud Storage.')
    # TODO(b/109938541): Remove this after implementation seems stable.
    self.use_gsutil = self._AddBool(
        'use_gsutil',
        default=False,
        hidden=True,
        help_text='If True, use the deprecated upload implementation which '
        'uses gsutil.')


class _Property(object):
  """An individual property that can be gotten from the properties file.

  Attributes:
    section: str, The name of the section the property appears in in the file.
    name: str, The name of the property.
    help_text: str, The man page help for what this property does.
    is_hidden: bool, True to hide this property from display for users that
      don't know about them.
    is_internal: bool, True to hide this property from display even if it is
      set. Internal properties are implementation details not meant to be set by
      users.
    callbacks: [func], A list of functions to be called, in order, if no value
      is found elsewhere.  The result of a callback will be shown in when
      listing properties (if the property is not hidden).
    completer: [func], a completer function
    default: str, A final value to use if no value is found after the callbacks.
      The default value is never shown when listing properties regardless of
      whether the property is hidden or not.
    validator: func(str), A function that is called on the value when .Set()'d
      or .Get()'d. For valid values, the function should do nothing. For invalid
      values, it should raise InvalidValueError with an explanation of why it
      was invalid.
    choices: [str], The allowable values for this property.  This is included in
      the help text and used in tab completion.
  """

  def __init__(self,
               section,
               name,
               help_text=None,
               hidden=False,
               internal=False,
               callbacks=None,
               default=None,
               validator=None,
               choices=None,
               completer=None):
    self.__section = section
    self.__name = name
    self.__help_text = help_text
    self.__hidden = hidden
    self.__internal = internal
    self.__callbacks = callbacks or []
    self.__default = default
    self.__validator = validator
    self.__choices = choices
    self.__completer = completer

  @property
  def section(self):
    return self.__section

  @property
  def name(self):
    return self.__name

  @property
  def help_text(self):
    return self.__help_text

  @property
  def is_hidden(self):
    return self.__hidden

  @property
  def is_internal(self):
    return self.__internal

  @property
  def default(self):
    return self.__default

  @property
  def callbacks(self):
    return self.__callbacks

  @property
  def choices(self):
    return self.__choices

  @property
  def completer(self):
    return self.__completer

  def __hash__(self):
    return hash(self.section) + hash(self.name)

  def __eq__(self, other):
    return self.section == other.section and self.name == other.name

  def __ne__(self, other):
    return not self == other

  def __gt__(self, other):
    return self.name > other.name

  def __ge__(self, other):
    return self.name >= other.name

  def __lt__(self, other):
    return self.name < other.name

  def __le__(self, other):
    return self.name <= other.name

  def GetOrFail(self):
    """Shortcut for Get(required=True).

    Convinient as a callback function.

    Returns:
      str, The value for this property.
    Raises:
      RequiredPropertyError if property is not set.
    """

    return self.Get(required=True)

  def Get(self, required=False, validate=True):
    """Gets the value for this property.

    Looks first in the environment, then in the workspace config, then in the
    global config, and finally at callbacks.

    Args:
      required: bool, True to raise an exception if the property is not set.
      validate: bool, Whether or not to run the fetched value through the
        validation function.

    Returns:
      str, The value for this property.
    """
    value = _GetProperty(self, named_configs.ActivePropertiesFile.Load(),
                         required)
    if validate:
      self.Validate(value)
    return value

  def IsExplicitlySet(self):
    """Determines if this property has been explicitly set by the user.

    Properties with defaults or callbacks don't count as explicitly set.

    Returns:
      True, if the value was explicitly set, False otherwise.
    """
    value = _GetPropertyWithoutCallback(
        self, named_configs.ActivePropertiesFile.Load())
    return value is not None

  def Validate(self, value):
    """Test to see if the value is valid for this property.

    Args:
      value: str, The value of the property to be validated.

    Raises:
      InvalidValueError: If the value was invalid according to the property's
          validator.
    """
    if self.__validator:
      self.__validator(value)

  def GetBool(self, required=False, validate=True):
    """Gets the boolean value for this property.

    Looks first in the environment, then in the workspace config, then in the
    global config, and finally at callbacks.

    Does not validate by default because boolean properties were not previously
    validated, and startup functions rely on boolean properties that may have
    invalid values from previous installations

    Args:
      required: bool, True to raise an exception if the property is not set.
      validate: bool, Whether or not to run the fetched value through the
        validation function.

    Returns:
      bool, The boolean value for this property, or None if it is not set.

    Raises:
      InvalidValueError: if value is not boolean
    """
    value = _GetBoolProperty(
        self,
        named_configs.ActivePropertiesFile.Load(),
        required,
        validate=validate)
    return value

  def GetInt(self, required=False, validate=True):
    """Gets the integer value for this property.

    Looks first in the environment, then in the workspace config, then in the
    global config, and finally at callbacks.

    Args:
      required: bool, True to raise an exception if the property is not set.
      validate: bool, Whether or not to run the fetched value through the
        validation function.

    Returns:
      int, The integer value for this property.
    """
    value = _GetIntProperty(self, named_configs.ActivePropertiesFile.Load(),
                            required)
    if validate:
      self.Validate(value)
    return value

  def Set(self, value):
    """Sets the value for this property as an environment variable.

    Args:
      value: str/bool, The proposed value for this property.  If None, it is
        removed from the environment.
    """
    self.Validate(value)
    if value is not None:
      value = Stringize(value)
    encoding.SetEncodedValue(os.environ, self.EnvironmentName(), value)

  def AddCallback(self, callback):
    """Adds another callback for this property."""
    self.__callbacks.append(callback)

  def RemoveCallback(self, callback):
    """Removess given callback for this property."""
    self.__callbacks.remove(callback)

  def EnvironmentName(self):
    """Get the name of the environment variable for this property.

    Returns:
      str, The name of the correct environment variable.
    """
    return 'CLOUDSDK_{section}_{name}'.format(
        section=self.__section.upper(),
        name=self.__name.upper(),
    )

  def __str__(self):
    return '{section}/{name}'.format(section=self.__section, name=self.__name)


VALUES = _Sections()


def FromString(property_string):
  """Gets the property object corresponding the given string.

  Args:
    property_string: str, The string to parse.  It can be in the format
      section/property, or just property if the section is the default one.

  Returns:
    properties.Property, The property or None if it failed to parse to a valid
      property.
  """
  section, prop = ParsePropertyString(property_string)
  if not prop:
    return None
  return VALUES.Section(section).Property(prop)


def ParsePropertyString(property_string):
  """Parses a string into a section and property name.

  Args:
    property_string: str, The property string in the format section/property.

  Returns:
    (str, str), The section and property.  Both will be none if the input
    string is empty.  Property can be None if the string ends with a slash.
  """
  if not property_string:
    return None, None

  if '/' in property_string:
    section, prop = tuple(property_string.split('/', 1))
  else:
    section = None
    prop = property_string

  section = section or VALUES.default_section.name
  prop = prop or None
  return section, prop


class _ScopeInfo(object):

  # pylint: disable=redefined-builtin
  def __init__(self, id, description):
    self.id = id
    self.description = description


class Scope(object):
  """An enum class for the different types of property files that can be used."""

  INSTALLATION = _ScopeInfo(
      id='installation',
      description='The installation based configuration file applies to all '
      'users on the system that use this version of the Cloud SDK.  If the SDK '
      'was installed by an administrator, you will need administrator rights '
      'to make changes to this file.')
  USER = _ScopeInfo(
      id='user',
      description='The user based configuration file applies only to the '
      'current user of the system.  It will override any values from the '
      'installation configuration.')

  _ALL = [USER, INSTALLATION]
  _ALL_SCOPE_NAMES = [s.id for s in _ALL]

  @staticmethod
  def AllValues():
    """Gets all possible enum values.

    Returns:
      [Scope], All the enum values.
    """
    return list(Scope._ALL)

  @staticmethod
  def AllScopeNames():
    return list(Scope._ALL_SCOPE_NAMES)

  @staticmethod
  def FromId(scope_id):
    """Gets the enum corresponding to the given scope id.

    Args:
      scope_id: str, The scope id to parse.

    Raises:
      InvalidScopeValueError: If the given value cannot be parsed.

    Returns:
      OperatingSystemTuple, One of the OperatingSystem constants or None if the
      input is None.
    """
    if not scope_id:
      return None
    for scope in Scope._ALL:
      if scope.id == scope_id:
        return scope
    raise InvalidScopeValueError(scope_id)

  @staticmethod
  def GetHelpString():
    return '\n\n'.join(
        ['*{0}*::: {1}'.format(s.id, s.description) for s in Scope.AllValues()])


def PersistProperty(prop, value, scope=None):
  """Sets the given property in the properties file.

  This function should not generally be used as part of normal program
  execution.  The property files are user editable config files that they should
  control.  This is mostly for initial setup of properties that get set during
  SDK installation.

  Args:
    prop: properties.Property, The property to set.
    value: str, The value to set for the property. If None, the property is
      removed.
    scope: Scope, The config location to set the property in.  If given, only
      this location will be updated and it is an error if that location does not
      exist.  If not given, it will attempt to update the property in the
      first of the following places that exists: - the active named config -
        user level config It will never fall back to installation properties;
        you must use that scope explicitly to set that value.

  Raises:
    MissingInstallationConfig: If you are trying to set the installation config,
      but there is not SDK root.
  """
  prop.Validate(value)
  if scope == Scope.INSTALLATION:
    config.EnsureSDKWriteAccess()
    config_file = config.Paths().installation_properties_path
    if not config_file:
      raise MissingInstallationConfig()
    prop_files_lib.PersistProperty(config_file, prop.section, prop.name, value)
    named_configs.ActivePropertiesFile.Invalidate(mark_changed=True)
  else:
    active_config = named_configs.ConfigurationStore.ActiveConfig()
    active_config.PersistProperty(prop.section, prop.name, value)
  # Print message if value being set is overridden by environment var
  # to prevent user confusion
  if value is not None:
    env_name = prop.EnvironmentName()
    override = os.getenv(env_name)
    if override:
      warning_message = ('WARNING: Property [{0}] is overridden '
                         'by environment setting [{1}={2}]\n')
      # Writing to sys.stderr because of circular dependency
      # in googlecloudsdk.core.log on properties
      sys.stderr.write(warning_message.format(prop.name, env_name, override))


def _GetProperty(prop, properties_file, required):
  """Gets the given property.

  If the property has a designated command line argument and args is provided,
  check args for the value first. If the corresponding environment variable is
  set, use that second. If still nothing, use the callbacks.

  Args:
    prop: properties.Property, The property to get.
    properties_file: properties_file.PropertiesFile, An already loaded
      properties files to use.
    required: bool, True to raise an exception if the property is not set.

  Raises:
    RequiredPropertyError: If the property was required but unset.

  Returns:
    str, The value of the property, or None if it is not set.
  """

  flag_to_use = None

  invocation_stack = VALUES.GetInvocationStack()
  if len(invocation_stack) > 1:
    # First item is the blank stack entry, second is from the user command args.
    first_invocation = invocation_stack[1]
    if prop in first_invocation:
      flag_to_use = first_invocation.get(prop).flag

  value = _GetPropertyWithoutDefault(prop, properties_file)
  if value is not None:
    return Stringize(value)

  # Still nothing, check the final default.
  if prop.default is not None:
    return Stringize(prop.default)

  # Not set, throw if required.
  if required:
    raise RequiredPropertyError(prop, flag=flag_to_use)

  return None


def _GetPropertyWithoutDefault(prop, properties_file):
  """Gets the given property without using a default.

  If the property has a designated command line argument and args is provided,
  check args for the value first. If the corresponding environment variable is
  set, use that second. Next, return whatever is in the property file.  Finally,
  use the callbacks to find values.  Do not check the default value.

  Args:
    prop: properties.Property, The property to get.
    properties_file: properties_file.PropertiesFile, An already loaded
      properties files to use.

  Returns:
    str, The value of the property, or None if it is not set.
  """
  # Try to get a value from args, env, or property file.
  value = _GetPropertyWithoutCallback(prop, properties_file)
  if value is not None:
    return Stringize(value)

  # No value, try getting a value from the callbacks.
  for callback in prop.callbacks:
    value = callback()
    if value is not None:
      return Stringize(value)

  return None


def _GetPropertyWithoutCallback(prop, properties_file):
  """Gets the given property without using a callback or default.

  If the property has a designated command line argument and args is provided,
  check args for the value first. If the corresponding environment variable is
  set, use that second. Finally, return whatever is in the property file.  Do
  not check for values in callbacks or defaults.

  Args:
    prop: properties.Property, The property to get.
    properties_file: PropertiesFile, An already loaded properties files to use.

  Returns:
    str, The value of the property, or None if it is not set.
  """
  # Look for a value in the flags that were used on this command.
  invocation_stack = VALUES.GetInvocationStack()
  for value_flags in reversed(invocation_stack):
    if prop not in value_flags:
      continue
    value_flag = value_flags.get(prop, None)
    if not value_flag:
      continue
    if value_flag.value is not None:
      return Stringize(value_flag.value)

  # Check the environment variable overrides.
  value = encoding.GetEncodedValue(os.environ, prop.EnvironmentName())
  if value is not None:
    return Stringize(value)

  # Check the property file itself.
  value = properties_file.Get(prop.section, prop.name)
  if value is not None:
    return Stringize(value)

  return None


def _GetBoolProperty(prop, properties_file, required, validate=False):
  """Gets the given property in bool form.

  Args:
    prop: properties.Property, The property to get.
    properties_file: properties_file.PropertiesFile, An already loaded
      properties files to use.
    required: bool, True to raise an exception if the property is not set.
    validate: bool, True to validate the value

  Returns:
    bool, The value of the property, or None if it is not set.
  """
  value = _GetProperty(prop, properties_file, required)
  if validate:
    _BooleanValidator(prop.name, value)
  if value is None or Stringize(value).lower() == 'none':
    return None
  return value.lower() in ['1', 'true', 'on', 'yes', 'y']


def _GetIntProperty(prop, properties_file, required):
  """Gets the given property in integer form.

  Args:
    prop: properties.Property, The property to get.
    properties_file: properties_file.PropertiesFile, An already loaded
      properties files to use.
    required: bool, True to raise an exception if the property is not set.

  Returns:
    int, The integer value of the property, or None if it is not set.
  """
  value = _GetProperty(prop, properties_file, required)
  if value is None:
    return None
  try:
    return int(value)
  except ValueError:
    raise InvalidValueError(
        'The property [{prop}] must have an integer value: [{value}]'.format(
            prop=prop, value=value))


def GetMetricsEnvironment():
  """Get the metrics environment.

  Returns the property metrics/environment if set, if not, it tries to deduce if
  we're on some known platforms like devshell or GCE.

  Returns:
    None, if no environment is set or found
    str, a string denoting the environment if one is set or found
  """

  environment = VALUES.metrics.environment.Get()
  if environment:
    return environment

  # No explicit environment defined, try to deduce it.
  # pylint: disable=g-import-not-at-top
  from googlecloudsdk.core.credentials import devshell as c_devshell
  if c_devshell.IsDevshellEnvironment():
    return 'devshell'

  from googlecloudsdk.core.credentials import gce_cache
  if gce_cache.GetOnGCE(check_age=False):
    return 'GCE'

  return None
