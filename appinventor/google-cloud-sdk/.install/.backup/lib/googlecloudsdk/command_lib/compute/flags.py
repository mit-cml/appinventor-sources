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

"""Flags and helpers for the compute related commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import functools
import enum  # pylint: disable=unused-import, for pytype

from googlecloudsdk.api_lib.compute import filter_rewrite
from googlecloudsdk.api_lib.compute.regions import service as regions_service
from googlecloudsdk.api_lib.compute.zones import service as zones_service
from googlecloudsdk.calliope import actions
from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.command_lib.compute import completers
from googlecloudsdk.command_lib.compute import scope as compute_scope
from googlecloudsdk.command_lib.compute import scope_prompter
from googlecloudsdk.core import exceptions
from googlecloudsdk.core import log
from googlecloudsdk.core import properties
from googlecloudsdk.core import resources
from googlecloudsdk.core.console import console_io
from googlecloudsdk.core.resource import resource_projection_spec
from googlecloudsdk.core.util import text
import six

ZONE_PROPERTY_EXPLANATION = """\
If not specified and the ``compute/zone'' property isn't set, you
may be prompted to select a zone.

To avoid prompting when this flag is omitted, you can set the
``compute/zone'' property:

  $ gcloud config set compute/zone ZONE

A list of zones can be fetched by running:

  $ gcloud compute zones list

To unset the property, run:

  $ gcloud config unset compute/zone

Alternatively, the zone can be stored in the environment variable
``CLOUDSDK_COMPUTE_ZONE''.
"""

ZONE_PROPERTY_EXPLANATION_NO_DEFAULT = """\
If not specified, you may be prompted to select a zone.

A list of zones can be fetched by running:

  $ gcloud compute zones list
"""

REGION_PROPERTY_EXPLANATION = """\
If not specified, you may be prompted to select a region.

To avoid prompting when this flag is omitted, you can set the
``compute/region'' property:

  $ gcloud config set compute/region REGION

A list of regions can be fetched by running:

  $ gcloud compute regions list

To unset the property, run:

  $ gcloud config unset compute/region

Alternatively, the region can be stored in the environment
variable ``CLOUDSDK_COMPUTE_REGION''.
"""

REGION_PROPERTY_EXPLANATION_NO_DEFAULT = """\
If not specified, you may be prompted to select a region.

A list of regions can be fetched by running:

  $ gcloud compute regions list
"""


class ScopesFetchingException(exceptions.Error):
  pass


class BadArgumentException(ValueError):
  """Unhandled error for validating function arguments."""
  pass


def AddZoneFlag(parser, resource_type, operation_type, flag_prefix=None,
                explanation=ZONE_PROPERTY_EXPLANATION,
                hidden=False, plural=False, custom_plural=None):
  """Adds a --zone flag to the given parser.

  Args:
    parser: argparse parser.
    resource_type: str, human readable name for the resource type this flag is
                   qualifying, for example "instance group".
    operation_type: str, human readable name for the operation, for example
                    "update" or "delete".
    flag_prefix: str, flag will be named --{flag_prefix}-zone.
    explanation: str, detailed explanation of the flag.
    hidden: bool, If True, --zone argument help will be hidden.
    plural: bool, resource_type will be pluralized or not depending on value.
    custom_plural: str, If plural is True then this string will be used as
                        resource types, otherwise resource_types will be
                        pluralized by appending 's'.
  """
  short_help = 'Zone of the {0} to {1}.'.format(
      text.Pluralize(
          int(plural) + 1, resource_type or '', custom_plural), operation_type)
  flag_name = 'zone'
  if flag_prefix is not None:
    flag_name = flag_prefix + '-' + flag_name
  parser.add_argument(
      '--' + flag_name,
      hidden=hidden,
      completer=completers.ZonesCompleter,
      action=actions.StoreProperty(properties.VALUES.compute.zone),
      help='{0} {1}'.format(short_help, explanation))


def AddRegionFlag(parser, resource_type, operation_type,
                  flag_prefix=None,
                  explanation=REGION_PROPERTY_EXPLANATION,
                  hidden=False, plural=False, custom_plural=None):
  """Adds a --region flag to the given parser.

  Args:
    parser: argparse parser.
    resource_type: str, human readable name for the resource type this flag is
                   qualifying, for example "instance group".
    operation_type: str, human readable name for the operation, for example
                    "update" or "delete".
    flag_prefix: str, flag will be named --{flag_prefix}-region.
    explanation: str, detailed explanation of the flag.
    hidden: bool, If True, --region argument help will be hidden.
    plural: bool, resource_type will be pluralized or not depending on value.
    custom_plural: str, If plural is True then this string will be used as
                        resource types, otherwise resource_types will be
                        pluralized by appending 's'.
  """
  short_help = 'Region of the {0} to {1}.'.format(
      text.Pluralize(
          int(plural) + 1, resource_type or '', custom_plural), operation_type)
  flag_name = 'region'
  if flag_prefix is not None:
    flag_name = flag_prefix + '-' + flag_name
  parser.add_argument(
      '--' + flag_name,
      completer=completers.RegionsCompleter,
      action=actions.StoreProperty(properties.VALUES.compute.region),
      hidden=hidden,
      help='{0} {1}'.format(short_help, explanation))


class UnderSpecifiedResourceError(exceptions.Error):
  """Raised when argument is required additional scope to be resolved."""

  def __init__(self, underspecified_names, flag_names):
    phrases = ('one of ', 'flags') if len(flag_names) > 1 else ('', 'flag')
    super(UnderSpecifiedResourceError, self).__init__(
        'Underspecified resource [{3}]. Specify {0}the [{1}] {2}.'
        .format(phrases[0],
                ', '.join(sorted(flag_names)),
                phrases[1],
                ', '.join(underspecified_names)))


class ResourceStub(object):
  """Interface used by scope listing to report scope names."""

  def __init__(self, name, deprecated=None):
    self.name = name
    self.deprecated = deprecated


def GetDefaultScopeLister(compute_client, project=None):
  """Constructs default zone/region lister."""
  scope_func = {
      compute_scope.ScopeEnum.ZONE:
          functools.partial(zones_service.List, compute_client),
      compute_scope.ScopeEnum.REGION:
          functools.partial(regions_service.List, compute_client),
      compute_scope.ScopeEnum.GLOBAL: lambda _: [ResourceStub(name='')]
  }
  def Lister(scopes, _):
    prj = project or properties.VALUES.core.project.Get(required=True)
    results = {}
    for scope in scopes:
      results[scope] = scope_func[scope](prj)
    return results
  return Lister


class ResourceArgScope(object):
  """Facilitates mapping of scope, flag and collection."""

  def __init__(self, scope, flag_prefix, collection):
    self.scope_enum = scope
    if flag_prefix:
      flag_prefix = flag_prefix.replace('-', '_')
      if scope is compute_scope.ScopeEnum.GLOBAL:
        self.flag_name = scope.flag_name + '_' + flag_prefix
      else:
        self.flag_name = flag_prefix + '_' + scope.flag_name
    else:
      self.flag_name = scope.flag_name
    self.flag = '--' + self.flag_name.replace('_', '-')
    self.collection = collection


class ResourceArgScopes(object):
  """Represents chosen set of scopes."""

  def __init__(self, flag_prefix):
    self.flag_prefix = flag_prefix
    self.scopes = {}

  def AddScope(self, scope, collection):
    # type: (enum.Enum, str) -> None
    self.scopes[scope] = ResourceArgScope(scope, self.flag_prefix, collection)

  def SpecifiedByArgs(self, args):
    """Given argparse args return selected scope and its value."""
    for resource_scope in six.itervalues(self.scopes):
      scope_value = getattr(args, resource_scope.flag_name, None)
      if scope_value is not None:
        return resource_scope, scope_value
    return None, None

  def GetImplicitScope(self, default_scope=None):
    """See if there is no ambiguity even if scope is not known from args."""
    if len(self.scopes) == 1:
      return next(six.itervalues(self.scopes))
    return default_scope

  def __iter__(self):
    return iter(six.itervalues(self.scopes))

  def __contains__(self, scope):
    return scope in self.scopes

  def __getitem__(self, scope):
    return self.scopes[scope]

  def __len__(self):
    return len(self.scopes)


class ResourceResolver(object):
  """Object responsible for resolving resources.

  There are two ways to build an instance of this object:
  1. Preferred when you don't have instance of ResourceArgScopes already built,
     using .FromMap static function. For example:

     resolver = ResourceResolver.FromMap(
         'instance',
         {compute_scope.ScopeEnum.ZONE: 'compute.instances'})

     where:
     - 'instance' is human readable name of the resource,
     - dictionary maps allowed scope (in this case only zone) to resource types
       in those scopes.
     - optional prefix of scope flags was skipped.

  2. Using constructor. Recommended only if you have instance of
     ResourceArgScopes available.

  Once you've built the resover you can use it to build resource references (and
  prompt for scope if it was not specified):

  resolver.ResolveResources(
        instance_name, compute_scope.ScopeEnum.ZONE,
        instance_zone, self.resources,
        scope_lister=flags.GetDefaultScopeLister(
            self.compute_client, self.project))

  will return a list of instances (of length 0 or 1 in this case, because we
  pass a name of single instance or None). It will prompt if and only if
  instance_name was not None but instance_zone was None.

  scope_lister is necessary for prompting.
  """

  def __init__(self, scopes, resource_name):
    """Initilize ResourceResolver instance.

    Prefer building with FromMap unless you have ResourceArgScopes object
    already built.

    Args:
      scopes: ResourceArgScopes, allowed scopes and resource types in those
              scopes.
      resource_name: str, human readable name for resources eg
                     "instance group".
    """
    self.scopes = scopes
    self.resource_name = resource_name

  @staticmethod
  def FromMap(resource_name, scopes_map, scope_flag_prefix=None):
    """Initilize ResourceResolver instance.

    Args:
      resource_name: str, human readable name for resources eg
                     "instance group".
      scopes_map: dict, with keys should be instances of ScopeEnum, values
              should be instances of ResourceArgScope.
      scope_flag_prefix: str, prefix of flags specyfying scope.
    Returns:
      New instance of ResourceResolver.
    """
    scopes = ResourceArgScopes(flag_prefix=scope_flag_prefix)
    for scope, resource in six.iteritems(scopes_map):
      scopes.AddScope(scope, resource)
    return ResourceResolver(scopes, resource_name)

  def _ValidateNames(self, names):
    if not isinstance(names, list):
      raise BadArgumentException(
          "Expected names to be a list but it is '{0}'".format(names))

  def _ValidateDefaultScope(self, default_scope):
    if default_scope is not None and default_scope not in self.scopes:
      raise BadArgumentException(
          'Unexpected value for default_scope {0}, expected None or {1}'
          .format(default_scope,
                  ' or '.join([s.scope_enum.name for s in self.scopes])))

  def _GetResourceScopeParam(
      self, resource_scope, scope_value, project, api_resource_registry):
    if scope_value is not None:
      if resource_scope.scope_enum == compute_scope.ScopeEnum.GLOBAL:
        return None
      else:
        collection = compute_scope.ScopeEnum.CollectionForScope(
            resource_scope.scope_enum)
        return api_resource_registry.Parse(
            scope_value,
            params={'project': project},
            collection=collection).Name()
    else:
      if resource_scope and (
          resource_scope.scope_enum != compute_scope.ScopeEnum.GLOBAL):
        return resource_scope.scope_enum.property_func

  def _GetRefsAndUnderspecifiedNames(
      self, names, params, collection, scope_defined, api_resource_registry):
    """Returns pair of lists: resolved references and unresolved names.

    Args:
      names: list of names to attempt resolving
      params: params given when attempting to resolve references
      collection: collection for the names
      scope_defined: bool, whether scope is known
      api_resource_registry: Registry object
    """
    refs = []
    underspecified_names = []
    for name in names:
      try:
        # Make each element an array so that we can do in place updates.
        ref = [api_resource_registry.Parse(name, params=params,
                                           collection=collection,
                                           enforce_collection=False)]
      except (resources.UnknownCollectionException,
              resources.RequiredFieldOmittedException,
              properties.RequiredPropertyError):
        if scope_defined:
          raise
        ref = [name]
        underspecified_names.append(ref)
      refs.append(ref)
    return refs, underspecified_names

  def _ResolveUnderspecifiedNames(
      self, underspecified_names, default_scope, scope_lister, project,
      api_resource_registry):
    """Attempt to resolve scope for unresolved names.

    If unresolved_names was generated with _GetRefsAndUnderspecifiedNames
    changing them will change corresponding elements of refs list.

    Args:
      underspecified_names: list of one-items lists containing str
      default_scope: default scope for the resources
      scope_lister: callback used to list potential scopes for the resources
      project: str, id of the project
      api_resource_registry: resources Registry

    Raises:
      UnderSpecifiedResourceError: when resource scope can't be resolved.
    """
    if not underspecified_names:
      return

    names = [n[0] for n in underspecified_names]

    if not console_io.CanPrompt():
      raise UnderSpecifiedResourceError(names, [s.flag for s in self.scopes])

    resource_scope_enum, scope_value = scope_prompter.PromptForScope(
        self.resource_name, names, [s.scope_enum for s in self.scopes],
        default_scope.scope_enum if default_scope is not None else None,
        scope_lister)
    if resource_scope_enum is None:
      raise UnderSpecifiedResourceError(names, [s.flag for s in self.scopes])

    resource_scope = self.scopes[resource_scope_enum]
    params = {'project': project,}

    if resource_scope.scope_enum != compute_scope.ScopeEnum.GLOBAL:
      params[resource_scope.scope_enum.param_name] = scope_value

    for name in underspecified_names:
      name[0] = api_resource_registry.Parse(
          name[0],
          params=params,
          collection=resource_scope.collection,
          enforce_collection=True)

  def ResolveResources(self,
                       names,
                       resource_scope,
                       scope_value,
                       api_resource_registry,
                       default_scope=None,
                       scope_lister=None):
    """Resolve this resource against the arguments.

    Args:
      names: list of str, list of resource names
      resource_scope: ScopeEnum, kind of scope of resources; if this is not None
                   scope_value should be name of scope of type specified by this
                   argument. If this is None scope_value should be None, in that
                   case if prompting is possible user will be prompted to
                   select scope (if prompting is forbidden it will raise an
                   exception).
      scope_value: ScopeEnum, scope of resources; if this is not None
                   resource_scope should be type of scope specified by this
                   argument. If this is None resource_scope should be None, in
                   that case if prompting is possible user will be prompted to
                   select scope (if prompting is forbidden it will raise an
                   exception).
      api_resource_registry: instance of core.resources.Registry.
      default_scope: ScopeEnum, ZONE, REGION, GLOBAL, or None when resolving
          name and scope was not specified use this as default. If there is
          exactly one possible scope it will be used, there is no need to
          specify default_scope.
      scope_lister: func(scope, underspecified_names), a callback which returns
        list of items (with 'name' attribute) for given scope.
    Returns:
      Resource reference or list of references if plural.
    Raises:
      BadArgumentException: when names is not a list or default_scope is not one
          of the configured scopes.
      UnderSpecifiedResourceError: if it was not possible to resolve given names
          as resources references.
    """
    self._ValidateNames(names)
    self._ValidateDefaultScope(default_scope)
    if resource_scope is not None:
      resource_scope = self.scopes[resource_scope]
    if default_scope is not None:
      default_scope = self.scopes[default_scope]
    project = properties.VALUES.core.project.GetOrFail
    params = {
        'project': project,
    }
    if scope_value is None:
      resource_scope = self.scopes.GetImplicitScope(default_scope)

    resource_scope_param = self._GetResourceScopeParam(
        resource_scope, scope_value, project, api_resource_registry)
    if resource_scope_param is not None:
      params[resource_scope.scope_enum.param_name] = resource_scope_param

    collection = resource_scope and resource_scope.collection

    # See if we can resolve names with so far deduced scope and its value.
    refs, underspecified_names = self._GetRefsAndUnderspecifiedNames(
        names, params, collection, scope_value is not None,
        api_resource_registry)

    # If we still have some resources which need to be resolve see if we can
    # prompt the user and try to resolve these again.
    self._ResolveUnderspecifiedNames(
        underspecified_names, default_scope, scope_lister, project,
        api_resource_registry)

    # Now unpack each element.
    refs = [ref[0] for ref in refs]  # type: list[resources.Resource]

    # Make sure correct collection was given for each resource, for example
    # URLs have implicit collections.
    expected_collections = [scope.collection for scope in self.scopes]
    for ref in refs:
      if ref.Collection() not in expected_collections:
        raise resources.WrongResourceCollectionException(
            expected=','.join(expected_collections),
            got=ref.Collection(),
            path=ref.SelfLink())
    return refs


class ResourceArgument(object):
  """Encapsulates concept of compute resource as command line argument.

  Basic Usage:
    class MyCommand(base.Command):
      _BACKEND_SERVICE_ARG = flags.ResourceArgument(
          resource_name='backend service',
          completer=compute_completers.BackendServiceCompleter,
          regional_collection='compute.regionBackendServices',
          global_collection='compute.backendServices')
      _INSTANCE_GROUP_ARG = flags.ResourceArgument(
          resource_name='instance group',
          completer=compute_completers.InstanceGroupsCompleter,
          zonal_collection='compute.instanceGroups',)

      @staticmethod
      def Args(parser):
        MyCommand._BACKEND_SERVICE_ARG.AddArgument(parser)
        MyCommand._INSTANCE_GROUP_ARG.AddArgument(parser)

      def Run(args):
        api_resource_registry = resources.REGISTRY.CloneAndSwitch(
            api_tools_client)
        backend_service_ref = _BACKEND_SERVICE_ARG.ResolveAsResource(
            args, api_resource_registry, default_scope=flags.ScopeEnum.GLOBAL)
        instance_group_ref = _INSTANCE_GROUP_ARG.ResolveAsResource(
            args, api_resource_registry, default_scope=flags.ScopeEnum.ZONE)
        ...

    In the above example the following five arguments/flags will be defined:
      NAME - positional for backend service
      --region REGION to qualify backend service
      --global  to qualify backend service
      --instance-group INSTANCE_GROUP name for the instance group
      --instance-group-zone INSTANCE_GROUP_ZONE further qualifies instance group

    More generally this construct can simultaneously support global, regional
    and zonal qualifiers (or any combination of) for each resource.
  """

  def __init__(self, name=None, resource_name=None, completer=None,
               plural=False, required=True, zonal_collection=None,
               regional_collection=None, global_collection=None,
               region_explanation=None, region_hidden=False,
               zone_explanation=None, zone_hidden=False,
               short_help=None, detailed_help=None, custom_plural=None):

    """Constructor.

    Args:
      name: str, argument name.
      resource_name: str, human readable name for resources eg "instance group".
      completer: completion_cache.Completer, The completer class type.
      plural: bool, whether to accept multiple values.
      required: bool, whether this argument is required.
      zonal_collection: str, include zone flag and use this collection
                             to resolve it.
      regional_collection: str, include region flag and use this collection
                                to resolve it.
      global_collection: str, if also zonal and/or regional adds global flag
                              and uses this collection to resolve as
                              global resource.
      region_explanation: str, long help that will be given for region flag,
                               empty by default.
      region_hidden: bool, Hide region in help if True.
      zone_explanation: str, long help that will be given for zone flag, empty
                             by default.
      zone_hidden: bool, Hide region in help if True.
      short_help: str, help for the flag being added, if not provided help text
                       will be 'The name[s] of the ${resource_name}[s].'.
      detailed_help: str, detailed help for the flag being added, if not
                          provided there will be no detailed help for the flag.
      custom_plural: str, If plural is True then this string will be used as
                          plural resource name.

    Raises:
      exceptions.Error: if there some inconsistency in arguments.
    """
    self.name_arg = name or 'name'
    self._short_help = short_help
    self._detailed_help = detailed_help

    if self.name_arg.startswith('--'):
      self.is_flag = True
      self.name = self.name_arg[2:].replace('-', '_')
      self.scopes = ResourceArgScopes(flag_prefix=self.name_arg[2:])

    else:  # positional
      self.scopes = ResourceArgScopes(flag_prefix=None)
      self.name = self.name_arg  # arg name is same as its spec.
    self.resource_name = resource_name
    self.completer = completer
    self.plural = plural
    self.custom_plural = custom_plural
    self.required = required
    if not (zonal_collection or regional_collection or global_collection):
      raise exceptions.Error('Must specify at least one resource type zonal, '
                             'regional or global')
    if zonal_collection:
      self.scopes.AddScope(compute_scope.ScopeEnum.ZONE,
                           collection=zonal_collection)
    if regional_collection:
      self.scopes.AddScope(compute_scope.ScopeEnum.REGION,
                           collection=regional_collection)
    if global_collection:
      self.scopes.AddScope(compute_scope.ScopeEnum.GLOBAL,
                           collection=global_collection)
    self._region_explanation = region_explanation or ''
    self._region_hidden = region_hidden
    self._zone_explanation = zone_explanation or ''
    self._zone_hidden = zone_hidden
    self._resource_resolver = ResourceResolver(self.scopes, resource_name)

  # TODO(b/31933786) remove cust_metavar once surface supports metavars for
  # plural flags.
  # TODO(b/32116723) remove mutex_group when argparse handles nesting groups
  def AddArgument(self,
                  parser,
                  mutex_group=None,
                  operation_type='operate on',
                  cust_metavar=None):
    """Add this set of arguments to argparse parser."""

    params = dict(
        metavar=cust_metavar if cust_metavar else self.name.upper(),
        completer=self.completer,
    )

    if self._detailed_help:
      params['help'] = self._detailed_help
    elif self._short_help:
      params['help'] = self._short_help
    else:
      params['help'] = 'Name{} of the {} to {}.'.format(
          's' if self.plural else '',
          text.Pluralize(
              int(self.plural) + 1, self.resource_name or '',
              self.custom_plural),
          operation_type)

    if self.name_arg.startswith('--'):
      params['required'] = self.required
      if self.plural:
        params['type'] = arg_parsers.ArgList(min_length=1)
    else:
      if self.required:
        if self.plural:
          params['nargs'] = '+'
      else:
        params['nargs'] = '*' if self.plural else '?'

    (mutex_group or parser).add_argument(self.name_arg, **params)

    if len(self.scopes) > 1:
      scope = parser.add_mutually_exclusive_group()
    else:
      scope = parser

    if compute_scope.ScopeEnum.ZONE in self.scopes:
      AddZoneFlag(
          scope,
          flag_prefix=self.scopes.flag_prefix,
          resource_type=self.resource_name,
          operation_type=operation_type,
          explanation=self._zone_explanation,
          hidden=self._zone_hidden,
          plural=self.plural,
          custom_plural=self.custom_plural)

    if compute_scope.ScopeEnum.REGION in self.scopes:
      AddRegionFlag(
          scope,
          flag_prefix=self.scopes.flag_prefix,
          resource_type=self.resource_name,
          operation_type=operation_type,
          explanation=self._region_explanation,
          hidden=self._region_hidden,
          plural=self.plural,
          custom_plural=self.custom_plural)

    if not self.plural:
      resource_mention = '{} is'.format(self.resource_name)
    elif self.plural and not self.custom_plural:
      resource_mention = '{}s are'.format(self.resource_name)
    else:
      resource_mention = '{} are'.format(self.custom_plural)
    if compute_scope.ScopeEnum.GLOBAL in self.scopes and len(self.scopes) > 1:
      scope.add_argument(
          self.scopes[compute_scope.ScopeEnum.GLOBAL].flag,
          action='store_true',
          default=None,
          help='If set, the {0} global.'
          .format(resource_mention))

  def ResolveAsResource(self, args,
                        api_resource_registry,
                        default_scope=None,
                        scope_lister=None):
    """Resolve this resource against the arguments.

    Args:
      args: Namespace, argparse.Namespace.
      api_resource_registry: instance of core.resources.Registry.
      default_scope: ScopeEnum, ZONE, REGION, GLOBAL, or None when resolving
          name and scope was not specified use this as default. If there is
          exactly one possible scope it will be used, there is no need to
          specify default_scope.
      scope_lister: func(scope, underspecified_names), a callback which returns
        list of items (with 'name' attribute) for given scope.
    Returns:
      Resource reference or list of references if plural.
    """
    names = self._GetResourceNames(args)
    resource_scope, scope_value = self.scopes.SpecifiedByArgs(args)
    if resource_scope is not None:
      resource_scope = resource_scope.scope_enum
      # Complain if scope was specified without actual resource(s).
      if not self.required and not names:
        if self.scopes.flag_prefix:
          flag = '--{0}-{1}'.format(
              self.scopes.flag_prefix, resource_scope.flag_name)
        else:
          flag = '--' + resource_scope
        raise exceptions.Error(
            'Can\'t specify {0} without specifying resource via {1}'.format(
                flag, self.name))
    refs = self._resource_resolver.ResolveResources(
        names, resource_scope, scope_value, api_resource_registry,
        default_scope, scope_lister)
    if self.plural:
      return refs
    if refs:
      return refs[0]
    return None

  def _GetResourceNames(self, args):
    """Return list of resource names specified by args."""
    if self.plural:
      return getattr(args, self.name)

    name_value = getattr(args, self.name)
    if name_value is not None:
      return [name_value]
    return []


def AddRegexArg(parser):
  parser.add_argument(
      '--regexp', '-r',
      help="""\
      A regular expression to filter the names of the results on. Any names
      that do not match the entire regular expression will be filtered out.
      """)


def AddPolicyFileFlag(parser):
  parser.add_argument('policy_file', help="""\
      JSON or YAML file containing the IAM policy.""")


def AddStorageLocationFlag(parser, resource):
  parser.add_argument(
      '--storage-location',
      metavar='LOCATION',
      help="""\
      Google Cloud Storage location, either regional or multi-regional, where
      {} content is to be stored. If absent, a nearby regional or
      multi-regional location is chosen automatically.
      """.format(resource))


def RewriteFilter(args, message=None, frontend_fields=None):
  """Rewrites args.filter into client and server filter expression strings.

  Usage:

    args.filter, request_filter = flags.RewriteFilter(args)

  Args:
    args: The parsed args namespace containing the filter expression args.filter
      and display_info.
    message: The response resource message proto for the request.
    frontend_fields: A set of dotted key names supported client side only.

  Returns:
    A (client_filter, server_filter) tuple of filter expression strings.
    None means the filter does not need to applied on the respective
    client/server side.
  """
  if not args.filter:
    return None, None
  display_info = args.GetDisplayInfo()
  defaults = resource_projection_spec.ProjectionSpec(
      symbols=display_info.transforms,
      aliases=display_info.aliases)
  client_filter, server_filter = filter_rewrite.Rewriter(
      message=message, frontend_fields=frontend_fields).Rewrite(
          args.filter, defaults=defaults)
  log.info('client_filter=%r server_filter=%r', client_filter, server_filter)
  return client_filter, server_filter
