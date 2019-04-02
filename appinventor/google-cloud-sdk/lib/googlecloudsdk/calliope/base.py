# -*- coding: utf-8 -*- #
# Copyright 2013 Google Inc. All Rights Reserved.
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
"""Base classes for calliope commands and groups.

"""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import abc
import collections
from functools import wraps
import itertools
import re
import sys

from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.calliope import display
from googlecloudsdk.core import exceptions
from googlecloudsdk.core import log
from googlecloudsdk.core import properties
from googlecloudsdk.core.resource import resource_printer

import six

# Category constants
AI_AND_MACHINE_LEARNING_CATEGORY = 'AI and Machine Learning'
API_PLATFORM_AND_ECOSYSTEMS_CATEGORY = 'API Platform and Ecosystems'
COMPUTE_CATEGORY = 'Compute'
DATA_ANALYTICS_CATEGORY = 'Data Analytics'
DATABASES_CATEGORY = 'Databases'
IDENTITY_AND_SECURITY_CATEGORY = 'Identity and Security'
INTERNET_OF_THINGS_CATEGORY = 'Internet of Things'
MANAGEMENT_TOOLS_CATEGORY = 'Management Tools'
MOBILE_CATEGORY = 'Mobile'
NETWORKING_CATEGORY = 'Networking'
SDK_TOOLS_CATEGORY = 'SDK Tools'
DISKS_CATEGORY = 'Disks'
INFO_CATEGORY = 'Info'
INSTANCES_CATEGORY = 'Instances'
LOAD_BALANCING_CATEGORY = 'Load Balancing'
TOOLS_CATEGORY = 'Tools'
STORAGE_CATEGORY = 'Storage'


# Common markdown.
MARKDOWN_BOLD = '*'
MARKDOWN_ITALIC = '_'
MARKDOWN_CODE = '`'


class DeprecationException(exceptions.Error):
  """An exception for when a command or group has been deprecated."""


class ReleaseTrack(object):
  """An enum representing the release track of a command or command group.

  The release track controls where a command appears.  The default of GA means
  it will show up under gcloud.  If you enable a command or group for the alpha,
  beta, or preview tracks, those commands will be duplicated under those groups
  as well.
  """

  class _TRACK(object):
    """An enum representing the release track of a command or command group."""

    # pylint: disable=redefined-builtin
    def __init__(self, id, prefix, help_tag, help_note):
      self.id = id
      self.prefix = prefix
      self.help_tag = help_tag
      self.help_note = help_note

    def __str__(self):
      return self.id

    def __eq__(self, other):
      return self.id == other.id

    def __hash__(self):
      return hash(self.id)

  GA = _TRACK('GA', None, None, None)
  BETA = _TRACK(
      'BETA', 'beta',
      '{0}(BETA){0} '.format(MARKDOWN_BOLD),
      'This command is currently in BETA and may change without notice.')
  ALPHA = _TRACK(
      'ALPHA', 'alpha',
      '{0}(ALPHA){0} '.format(MARKDOWN_BOLD),
      'This command is currently in ALPHA and may change without notice. '
      'If this command fails with API permission errors despite specifying '
      'the right project, you will have to apply for early access and have your'
      ' projects registered on the API whitelist to use it. To do so, contact '
      'Support at https://cloud.google.com/support/.')
  _ALL = [GA, BETA, ALPHA]

  @staticmethod
  def AllValues():
    """Gets all possible enum values.

    Returns:
      list, All the enum values.
    """
    return list(ReleaseTrack._ALL)

  @staticmethod
  def FromPrefix(prefix):
    """Gets a ReleaseTrack from the given release track prefix.

    Args:
      prefix: str, The prefix string that might be a release track name.

    Returns:
      ReleaseTrack, The corresponding object or None if the prefix was not a
      valid release track.
    """
    for track in ReleaseTrack._ALL:
      if track.prefix == prefix:
        return track
    return None

  @staticmethod
  def FromId(id):  # pylint: disable=redefined-builtin
    """Gets a ReleaseTrack from the given release track prefix.

    Args:
      id: str, The id string that must be a release track name.

    Raises:
      ValueError: For unknown release track ids.

    Returns:
      ReleaseTrack, The corresponding object.
    """
    for track in ReleaseTrack._ALL:
      if track.id == id:
        return track
    raise ValueError('Unknown release track id [{}].'.format(id))


class Action(six.with_metaclass(abc.ABCMeta, object)):
  """A class that allows you to save an Action configuration for reuse."""

  def __init__(self, *args, **kwargs):
    """Creates the Action.

    Args:
      *args: The positional args to parser.add_argument.
      **kwargs: The keyword args to parser.add_argument.
    """
    self.args = args
    self.kwargs = kwargs

  @property
  def name(self):
    return self.args[0]

  @abc.abstractmethod
  def AddToParser(self, parser):
    """Adds this Action to the given parser.

    Args:
      parser: The argparse parser.

    Returns:
      The result of adding the Action to the parser.
    """
    pass

  def RemoveFromParser(self, parser):
    """Removes this Action from the given parser.

    Args:
      parser: The argparse parser.
    """
    pass

  def SetDefault(self, parser, default):
    """Sets the default value for this Action in the given parser.

    Args:
      parser: The argparse parser.
      default: The default value.
    """
    pass


class ArgumentGroup(Action):
  """A class that allows you to save an argument group configuration for reuse.
  """

  def __init__(self, *args, **kwargs):
    super(ArgumentGroup, self).__init__(*args, **kwargs)
    self.arguments = []

  def AddArgument(self, arg):
    self.arguments.append(arg)

  def AddToParser(self, parser):
    """Adds this argument group to the given parser.

    Args:
      parser: The argparse parser.

    Returns:
      The result of parser.add_argument().
    """
    group = self._CreateGroup(parser)
    for arg in self.arguments:
      arg.AddToParser(group)
    return group

  def _CreateGroup(self, parser):
    return parser.add_group(*self.args, **self.kwargs)


class Argument(Action):
  """A class that allows you to save an argument configuration for reuse."""

  def __GetFlag(self, parser):
    """Returns the flag object in parser."""
    for flag in itertools.chain(parser.flag_args, parser.ancestor_flag_args):
      if self.name in flag.option_strings:
        return flag
    return None

  def AddToParser(self, parser):
    """Adds this argument to the given parser.

    Args:
      parser: The argparse parser.

    Returns:
      The result of parser.add_argument().
    """
    return parser.add_argument(*self.args, **self.kwargs)

  def RemoveFromParser(self, parser):
    """Removes this flag from the given parser.

    Args:
      parser: The argparse parser.
    """
    flag = self.__GetFlag(parser)
    if flag:
      # Remove the flag and its inverse, if it exists, from its container.
      name = flag.option_strings[0]
      conflicts = [(name, flag)]
      no_name = '--no-' + name[2:]
      for no_flag in itertools.chain(parser.flag_args,
                                     parser.ancestor_flag_args):
        if no_name in no_flag.option_strings:
          conflicts.append((no_name, no_flag))
      # pylint: disable=protected-access, argparse, why can't we be friends
      flag.container._handle_conflict_resolve(flag, conflicts)
      # Remove the conflict flags from the calliope argument interceptor.
      for _, flag in conflicts:
        parser.defaults.pop(flag.dest, None)
        if flag.dest in parser.dests:
          parser.dests.remove(flag.dest)
        if flag in parser.flag_args:
          parser.flag_args.remove(flag)
        if flag in parser.arguments:
          parser.arguments.remove(flag)

  def SetDefault(self, parser, default):
    """Sets the default value for this flag in the given parser.

    Args:
      parser: The argparse parser.
      default: The default flag value.
    """
    flag = self.__GetFlag(parser)
    if flag:
      kwargs = {flag.dest: default}
      parser.set_defaults(**kwargs)

      # Update the flag's help text.
      original_help = flag.help
      match = re.search(r'(.*The default is ).*?(\.([ \t\n].*))',
                        original_help, re.DOTALL)
      if match:
        new_help = '{}*{}*{}'.format(match.group(1), default, match.group(2))
      else:
        new_help = original_help + ' The default is *{}*.'.format(default)
      flag.help = new_help

# Common flag definitions for consistency.

# Common flag categories.

COMMONLY_USED_FLAGS = 'COMMONLY USED'

FLAGS_FILE_FLAG = Argument(
    '--flags-file',
    metavar='YAML_FILE',
    default=None,
    category=COMMONLY_USED_FLAGS,
    help="""\
        A YAML or JSON file that specifies a *--flag*:*value* dictionary.
        Useful for specifying complex flag values with special characters
        that work with any command interpreter. Additionally, each
        *--flags-file* arg is replaced by its constituent flags. See
        $ gcloud topic flags-file for more information.""")

FLATTEN_FLAG = Argument(
    '--flatten',
    metavar='KEY',
    default=None,
    type=arg_parsers.ArgList(),
    category=COMMONLY_USED_FLAGS,
    help="""\
        Flatten _name_[] output resource slices in _KEY_ into separate records
        for each item in each slice. Multiple keys and slices may be specified.
        This also flattens keys for *--format* and *--filter*. For example,
        *--flatten=abc.def* flattens *abc.def[].ghi* references to
        *abc.def.ghi*. A resource record containing *abc.def[]* with N elements
        will expand to N records in the flattened output. This flag interacts
        with other flags that are applied in this order: *--flatten*,
        *--sort-by*, *--filter*, *--limit*.""")

FORMAT_FLAG = Argument(
    '--format',
    default=None,
    category=COMMONLY_USED_FLAGS,
    help="""\
        Set the format for printing command output resources. The default is a
        command-specific human-friendly output format. The supported formats
        are: `{0}`. For more details run $ gcloud topic formats.""".format(
            '`, `'.join(resource_printer.SupportedFormats())))

LIST_COMMAND_FLAGS = 'LIST COMMAND'

ASYNC_FLAG = Argument(
    '--async',
    action='store_true',
    help="""\
    Display information about the operation in progress, without waiting for
    the operation to complete.""")

FILTER_FLAG = Argument(
    '--filter',
    metavar='EXPRESSION',
    require_coverage_in_tests=False,
    category=LIST_COMMAND_FLAGS,
    help="""\
    Apply a Boolean filter _EXPRESSION_ to each resource item to be listed.
    If the expression evaluates `True`, then that item is listed. For more
    details and examples of filter expressions, run $ gcloud topic filters. This
    flag interacts with other flags that are applied in this order: *--flatten*,
    *--sort-by*, *--filter*, *--limit*.""")

LIMIT_FLAG = Argument(
    '--limit',
    type=arg_parsers.BoundedInt(1, sys.maxsize, unlimited=True),
    require_coverage_in_tests=False,
    category=LIST_COMMAND_FLAGS,
    help="""\
    Maximum number of resources to list. The default is *unlimited*.
    This flag interacts with other flags that are applied in this order:
    *--flatten*, *--sort-by*, *--filter*, *--limit*.
    """)

PAGE_SIZE_FLAG = Argument(
    '--page-size',
    type=arg_parsers.BoundedInt(1, sys.maxsize, unlimited=True),
    require_coverage_in_tests=False,
    category=LIST_COMMAND_FLAGS,
    help="""\
    Some services group resource list output into pages. This flag specifies
    the maximum number of resources per page. The default is determined by the
    service if it supports paging, otherwise it is *unlimited* (no paging).
    Paging may be applied before or after *--filter* and *--limit* depending
    on the service.
    """)

SORT_BY_FLAG = Argument(
    '--sort-by',
    metavar='FIELD',
    type=arg_parsers.ArgList(),
    require_coverage_in_tests=False,
    category=LIST_COMMAND_FLAGS,
    help="""\
    Comma-separated list of resource field key names to sort by. The
    default order is ascending. Prefix a field with ``~'' for descending
    order on that field. This flag interacts with other flags that are applied
    in this order: *--flatten*, *--sort-by*, *--filter*, *--limit*.
    """)

URI_FLAG = Argument(
    '--uri',
    action='store_true',
    require_coverage_in_tests=False,
    category=LIST_COMMAND_FLAGS,
    help='Print a list of resource URIs instead of the default output.')


class _Common(six.with_metaclass(abc.ABCMeta, object)):
  """Base class for Command and Group."""
  category = None
  _cli_generator = None
  _is_hidden = False
  _is_unicode_supported = False
  _release_track = None
  _valid_release_tracks = None
  _notices = None

  def __init__(self, is_group=False):
    self.exit_code = 0
    self.is_group = is_group

  @staticmethod
  def Args(parser):
    """Set up arguments for this command.

    Args:
      parser: An argparse.ArgumentParser.
    """
    pass

  @staticmethod
  def _Flags(parser):
    """Adds subclass flags.

    Args:
      parser: An argparse.ArgumentParser object.
    """
    pass

  @classmethod
  def IsHidden(cls):
    return cls._is_hidden

  @classmethod
  def IsUnicodeSupported(cls):
    if six.PY2:
      return cls._is_unicode_supported
    # We always support unicode on Python 3.
    return True

  @classmethod
  def ReleaseTrack(cls):
    return cls._release_track

  @classmethod
  def ValidReleaseTracks(cls):
    return cls._valid_release_tracks

  @classmethod
  def GetTrackedAttribute(cls, obj, attribute):
    """Gets the attribute value from obj for tracks.

    The values are checked in ReleaseTrack._ALL order.

    Args:
      obj: The object to extract attribute from.
      attribute: The attribute name in object.

    Returns:
      The attribute value from obj for tracks.
    """
    for track in ReleaseTrack._ALL:  # pylint: disable=protected-access
      if track not in cls._valid_release_tracks:  # pylint: disable=unsupported-membership-test
        continue
      names = []
      names.append(attribute + '_' + track.id)
      if track.prefix:
        names.append(attribute + '_' + track.prefix)
      for name in names:
        if hasattr(obj, name):
          return getattr(obj, name)
    return getattr(obj, attribute, None)

  @classmethod
  def Notices(cls):
    return cls._notices

  @classmethod
  def AddNotice(cls, tag, msg, preserve_existing=False):
    if not cls._notices:
      cls._notices = {}
    if tag in cls._notices and preserve_existing:
      return
    cls._notices[tag] = msg

  @classmethod
  def GetCLIGenerator(cls):
    """Get a generator function that can be used to execute a gcloud command.

    Returns:
      A bound generator function to execute a gcloud command.
    """
    if cls._cli_generator:
      return cls._cli_generator.Generate
    return None


class Group(_Common):
  """Group is a base class for groups to implement."""

  IS_COMMAND_GROUP = True

  _allow_py3 = True

  def __init__(self):
    super(Group, self).__init__(is_group=True)

  def Filter(self, context, args):
    """Modify the context that will be given to this group's commands when run.

    Args:
      context: {str:object}, A set of key-value pairs that can be used for
          common initialization among commands.
      args: argparse.Namespace: The same namespace given to the corresponding
          .Run() invocation.
    """
    pass


class Command(six.with_metaclass(abc.ABCMeta, _Common)):
  """Command is a base class for commands to implement.

  Attributes:
    _cli_do_not_use_directly: calliope.cli.CLI, The CLI object representing this
      command line tool. This should *only* be accessed via commands that
      absolutely *need* introspection of the entire CLI.
    context: {str:object}, A set of key-value pairs that can be used for
        common initialization among commands.
    _uri_cache_enabled: bool, The URI cache enabled state.
  """

  IS_COMMAND = True

  def __init__(self, cli, context):
    super(Command, self).__init__(is_group=False)
    self._cli_do_not_use_directly = cli
    self.context = context
    self._uri_cache_enabled = False

  @property
  def _cli_power_users_only(self):
    return self._cli_do_not_use_directly

  def ExecuteCommandDoNotUse(self, args):
    """Execute a command using the given CLI.

    Do not introduce new invocations of this method unless your command
    *requires* it; any such new invocations must be approved by a team lead.

    Args:
      args: list of str, the args to Execute() via the CLI.

    Returns:
      pass-through of the return value from Execute()
    """
    return self._cli_power_users_only.Execute(args, call_arg_complete=False)

  @staticmethod
  def _Flags(parser):
    """Sets the default output format.

    Args:
      parser: The argparse parser.
    """
    parser.display_info.AddFormat('default')

  @abc.abstractmethod
  def Run(self, args):
    """Runs the command.

    Args:
      args: argparse.Namespace, An object that contains the values for the
          arguments specified in the .Args() method.

    Returns:
      A resource object dispatched by display.Displayer().
    """
    pass

  def Epilog(self, resources_were_displayed):
    """Called after resources are displayed if the default format was used.

    Args:
      resources_were_displayed: True if resources were displayed.
    """
    _ = resources_were_displayed

  def GetReferencedKeyNames(self, args):
    """Returns the key names referenced by the filter and format expressions."""
    return display.Displayer(self, args, None).GetReferencedKeyNames()

  def GetUriFunc(self):
    """Returns a function that transforms a command resource item to a URI.

    Returns:
      func(resource) that transforms resource into a URI.
    """
    return None


class TopicCommand(six.with_metaclass(abc.ABCMeta, Command)):
  """A command that displays its own help on execution."""

  def Run(self, args):
    self.ExecuteCommandDoNotUse(args.command_path[1:] +
                                ['--document=style=topic'])
    return None


class SilentCommand(six.with_metaclass(abc.ABCMeta, Command)):
  """A command that produces no output."""

  @staticmethod
  def _Flags(parser):
    parser.display_info.AddFormat('none')


class DescribeCommand(six.with_metaclass(abc.ABCMeta, Command)):
  """A command that prints one resource in the 'default' format."""


class CacheCommand(six.with_metaclass(abc.ABCMeta, Command)):
  """A command that affects the resource URI cache."""

  def __init__(self, *args, **kwargs):
    super(CacheCommand, self).__init__(*args, **kwargs)
    self._uri_cache_enabled = True


class ListCommand(six.with_metaclass(abc.ABCMeta, CacheCommand)):
  """A command that pretty-prints all resources."""

  @staticmethod
  def _Flags(parser):
    """Adds the default flags for all ListCommand commands.

    Args:
      parser: The argparse parser.
    """

    FILTER_FLAG.AddToParser(parser)
    LIMIT_FLAG.AddToParser(parser)
    PAGE_SIZE_FLAG.AddToParser(parser)
    SORT_BY_FLAG.AddToParser(parser)
    URI_FLAG.AddToParser(parser)
    parser.display_info.AddFormat('default')

  def Epilog(self, resources_were_displayed):
    """Called after resources are displayed if the default format was used.

    Args:
      resources_were_displayed: True if resources were displayed.
    """
    if not resources_were_displayed:
      log.status.Print('Listed 0 items.')


class CreateCommand(CacheCommand, SilentCommand):
  """A command that creates resources."""


class DeleteCommand(CacheCommand, SilentCommand):
  """A command that deletes resources."""


class RestoreCommand(CacheCommand, SilentCommand):
  """A command that restores resources."""


class UpdateCommand(SilentCommand):
  """A command that updates resources."""

  pass


def Hidden(cmd_class):
  """Decorator for hiding calliope commands and groups.

  Decorate a subclass of base.Command or base.Group with this function, and the
  decorated command or group will not show up in help text.

  Args:
    cmd_class: base._Common, A calliope command or group.

  Returns:
    A modified version of the provided class.
  """
  # pylint: disable=protected-access
  cmd_class._is_hidden = True
  return cmd_class


def DisallowPython3(group_class):
  """Decorator to indicate a surface doesn't support being run undder Python 3.

  Calliope will error out if sub-elements of this surface are attempted to be
  loaded. Should only be applied to groups.

  Args:
    group_class: base.Group, A calliope group.

  Returns:
    A modified version of the provided class.
  """
  # pylint: disable=protected-access
  group_class._allow_py3 = False
  return group_class


def UnicodeIsSupported(cmd_class):
  """Decorator for calliope commands and groups that support unicode.

  Decorate a subclass of base.Command or base.Group with this function, and the
  decorated command or group will not raise the argparse unicode command line
  argument exception.

  Args:
    cmd_class: base._Common, A calliope command or group.

  Returns:
    A modified version of the provided class.
  """
  # pylint: disable=protected-access
  cmd_class._is_unicode_supported = True
  return cmd_class


def ReleaseTracks(*tracks):
  """Mark this class as the command implementation for the given release tracks.

  Args:
    *tracks: [ReleaseTrack], A list of release tracks that this is valid for.

  Returns:
    The decorated function.
  """
  def ApplyReleaseTracks(cmd_class):
    """Wrapper function for the decorator."""
    # pylint: disable=protected-access
    cmd_class._valid_release_tracks = set(tracks)
    return cmd_class
  return ApplyReleaseTracks


def Deprecate(is_removed=True,
              warning='This command is deprecated.',
              error='This command has been removed.'):
  """Decorator that marks a Calliope command as deprecated.

  Decorate a subclass of base.Command with this function and the
  decorated command will be modified as follows:

  - If is_removed is false, a warning will be logged when *command* is run,
  otherwise an *exception* will be thrown containing error message

  -Command help output will be modified to include warning/error message
  depending on value of is_removed

  - Command help text will automatically hidden from the reference documentation
  (e.g. @base.Hidden) if is_removed is True


  Args:
      is_removed: boolean, True if the command should raise an error
      when executed. If false, a warning is printed
      warning: string, warning message
      error: string, error message

  Returns:
    A modified version of the provided class.
  """

  def DeprecateCommand(cmd_class):
    """Wrapper Function that creates actual decorated class.

    Args:
      cmd_class: base.Command or base.Group subclass to be decorated

    Returns:
      The decorated class.
    """
    if is_removed:
      msg = error
      deprecation_tag = '{0}(REMOVED){0} '.format(MARKDOWN_BOLD)
    else:
      msg = warning
      deprecation_tag = '{0}(DEPRECATED){0} '.format(MARKDOWN_BOLD)

    cmd_class.AddNotice(deprecation_tag, msg)

    def RunDecorator(run_func):
      @wraps(run_func)
      def WrappedRun(*args, **kw):
        if is_removed:
          raise DeprecationException(error)
        log.warning(warning)
        return run_func(*args, **kw)
      return WrappedRun

    if issubclass(cmd_class, Group):
      cmd_class.Filter = RunDecorator(cmd_class.Filter)
    else:
      cmd_class.Run = RunDecorator(cmd_class.Run)

    if is_removed:
      return Hidden(cmd_class)

    return cmd_class

  return DeprecateCommand


def _ChoiceValueType(value):
  """Returns a function that ensures choice flag values match Cloud SDK Style.

  Args:
    value: string, string representing flag choice value parsed from command
           line.

  Returns:
       A string value entirely in lower case, with words separated by
       hyphens.
  """
  return value.replace('_', '-').lower()


def ChoiceArgument(name_or_flag, choices, help_str=None, required=False,
                   action=None, metavar=None, dest=None, default=None,
                   hidden=False):
  """Returns Argument with a Cloud SDK style compliant set of choices.

  Args:
    name_or_flag: string, Either a name or a list of option strings,
       e.g. foo or -f, --foo.
    choices: container,  A container (e.g. set, dict, list, tuple) of the
       allowable values for the argument. Should consist of strings entirely in
       lower case, with words separated by hyphens.
    help_str: string,  A brief description of what the argument does.
    required: boolean, Whether or not the command-line option may be omitted.
    action: string or argparse.Action, The basic type of argeparse.action
       to be taken when this argument is encountered at the command line.
    metavar: string,  A name for the argument in usage messages.
    dest: string,  The name of the attribute to be added to the object returned
       by parse_args().
    default: string,  The value produced if the argument is absent from the
       command line.
    hidden: boolean, Whether or not the command-line option is hidden.

  Returns:
     Argument object with choices, that can accept both lowercase and uppercase
     user input with hyphens or undersores.

  Raises:
     TypeError: If choices are not an iterable container of string options.
     ValueError: If provided choices are not Cloud SDK Style compliant.
  """

  if not choices:
    raise ValueError('Choices must not be empty.')

  if (not isinstance(choices, collections.Iterable)
      or isinstance(choices, six.string_types)):
    raise TypeError(
        'Choices must be an iterable container of options: [{}].'.format(
            ', '.join(choices)))

  # Valid choices should be alphanumeric sequences followed by an optional
  # period '.', separated by a single hyphen '-'.
  choice_re = re.compile(r'^([a-z0-9]\.?-?)+[a-z0-9]$')
  invalid_choices = [x for x in choices if not choice_re.match(x)]
  if invalid_choices:
    raise ValueError(
        ('Invalid choices [{}]. Choices must be entirely in lowercase with '
         'words separated by hyphens(-)').format(', '.join(invalid_choices)))

  return Argument(name_or_flag, choices=choices, required=required,
                  type=_ChoiceValueType, help=help_str, action=action,
                  metavar=metavar, dest=dest, default=default, hidden=hidden)


def DisableUserProjectQuota():
  """Disable the quota header if the user hasn't manually specified it."""
  if not properties.VALUES.billing.quota_project.IsExplicitlySet():
    properties.VALUES.billing.quota_project.Set(
        properties.VALUES.billing.LEGACY)


def EnableUserProjectQuota():
  """Enable the quota header for current project."""
  properties.VALUES.billing.quota_project.Set(
      properties.VALUES.billing.CURRENT_PROJECT)


def LogCommand(prog, args):
  """Log (to debug) the command/arguments being run in a standard format.

  `gcloud feedback` depends on this format.

  Example format is:

      Running [gcloud.example.command] with arguments: [--bar: "baz"]

  Args:
    prog: string, the dotted name of the command being run (ex.
        "gcloud.foos.list")
    args: argparse.namespace, the parsed arguments from the command line
  """
  specified_args = sorted(six.iteritems(args.GetSpecifiedArgs()))
  arg_string = ', '.join(['{}: "{}"'.format(k, v) for k, v in specified_args])
  log.debug('Running [{}] with arguments: [{}]'.format(prog, arg_string))
