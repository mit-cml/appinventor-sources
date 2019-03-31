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

"""Debug apis layer."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import re
import threading

from apitools.base.py import exceptions as apitools_exceptions

from googlecloudsdk.api_lib.debug import errors
from googlecloudsdk.api_lib.util import apis
from googlecloudsdk.core import config
from googlecloudsdk.core import log
from googlecloudsdk.core import resources
from googlecloudsdk.core.util import retry

import six
from six.moves import urllib

# Names for default module and version. In App Engine, the default module and
# version don't report explicit names to the debugger, so use these strings
# instead when displaying the target name. Note that this code assumes there
# will not be a non-default version or module explicitly named 'default', since
# that would result in a naming conflict between the actual default and the
# one named 'default'.
DEFAULT_MODULE = 'default'
DEFAULT_VERSION = 'default'


def SplitLogExpressions(format_string):
  """Extracts {expression} substrings into a separate array.

  Each substring of the form {expression} will be extracted into an array, and
  each {expression} substring will be replaced with $N, where N is the index
  of the extraced expression in the array. Any '$' sequence outside an
  expression will be escaped with '$$'.

  For example, given the input:
    'a={a}, b={b}'
   The return value would be:
    ('a=$0, b=$1', ['a', 'b'])

  Args:
    format_string: The string to process.
  Returns:
    string, [string] - The new format string and the array of expressions.
  Raises:
    InvalidLogFormatException: if the string has unbalanced braces.
  """
  expressions = []
  log_format = ''
  current_expression = ''
  brace_count = 0
  need_separator = False
  for c in format_string:
    if need_separator and c.isdigit():
      log_format += ' '
    need_separator = False
    if c == '{':
      if brace_count:
        # Nested braces
        current_expression += c
      else:
        # New expression
        current_expression = ''
      brace_count += 1
    elif not brace_count:
      if c == '}':
        # Unbalanced left brace.
        raise errors.InvalidLogFormatException(
            'There are too many "}" characters in the log format string')
      elif c == '$':
        # Escape '$'
        log_format += '$$'
      else:
        # Not in or starting an expression.
        log_format += c
    else:
      # Currently reading an expression.
      if c != '}':
        current_expression += c
        continue
      brace_count -= 1
      if brace_count == 0:
        # Finish processing the expression
        if current_expression in expressions:
          i = expressions.index(current_expression)
        else:
          i = len(expressions)
          expressions.append(current_expression)
        log_format += '${0}'.format(i)
        # If the next character is a digit, we need an extra space to prevent
        # the agent from combining the positional argument with the subsequent
        # digits.
        need_separator = True
      else:
        # Closing a nested brace
        current_expression += c

  if brace_count:
    # Unbalanced left brace.
    raise errors.InvalidLogFormatException(
        'There are too many "{" characters in the log format string')
  return log_format, expressions


def MergeLogExpressions(log_format, expressions):
  """Replaces each $N substring with the corresponding {expression}.

  This function is intended for reconstructing an input expression string that
  has been split using SplitLogExpressions. It is not intended for substituting
  the expression results at log time.

  Args:
    log_format: A string containing 0 or more $N substrings, where N is any
      valid index into the expressions array. Each such substring will be
      replaced by '{expression}', where "expression" is expressions[N].
    expressions: The expressions to substitute into the format string.
  Returns:
    The combined string.
  """
  def GetExpression(m):
    try:
      return '{{{0}}}'.format(expressions[int(m.group(0)[1:])])
    except IndexError:
      return m.group(0)

  parts = log_format.split('$$')
  return '$'.join(re.sub(r'\$\d+', GetExpression, part) for part in parts)


def DebugViewUrl(breakpoint):
  """Returns a URL to view a breakpoint in the browser.

  Given a breakpoint, this transform will return a URL which will open the
  snapshot's location in a debug view pointing at the snapshot.

  Args:
    breakpoint: A breakpoint object with added information on project and
    debug target.
  Returns:
    The URL for the breakpoint.
  """
  debug_view_url = 'https://console.cloud.google.com/debug/fromgcloud?'
  data = [
      ('project', breakpoint.project),
      ('dbgee', breakpoint.target_id),
      ('bp', breakpoint.id)
  ]
  return debug_view_url + urllib.parse.urlencode(data)


def LogQueryV2String(breakpoint, separator=' '):
  """Returns an advanced log query string for use with gcloud logging read.

  Args:
    breakpoint: A breakpoint object with added information on project, service,
      and debug target.
    separator: A string to append between conditions
  Returns:
    A log query suitable for use with gcloud logging read.
  Raises:
    InvalidLogFormatException if the breakpoint has an invalid log expression.
  """
  query = (
      'resource.type=gae_app{sep}'
      'logName:request_log{sep}'
      'resource.labels.module_id="{service}"{sep}'
      'resource.labels.version_id="{version}"{sep}'
      'severity={logLevel}').format(
          service=breakpoint.service, version=breakpoint.version,
          logLevel=breakpoint.logLevel or 'INFO', sep=separator)
  if breakpoint.logMessageFormat:
    # Search for all of the non-expression components of the message.
    # The re.sub converts the format to a series of quoted strings.
    query += '{sep}"{text}"'.format(
        text=re.sub(r'\$([0-9]+)', r'" "',
                    SplitLogExpressions(breakpoint.logMessageFormat)[0]),
        sep=separator)
  return query


def LogViewUrl(breakpoint):
  """Returns a URL to view the output for a logpoint.

  Given a breakpoint in an appengine service, this transform will return a URL
  which will open the log viewer to the request log for the service.

  Args:
    breakpoint: A breakpoint object with added information on project, service,
      debug target, and logQuery.
  Returns:
    The URL for the appropriate logs.
  """
  debug_view_url = 'https://console.cloud.google.com/logs?'
  data = [
      ('project', breakpoint.project),
      ('advancedFilter', LogQueryV2String(breakpoint, separator='\n') + '\n')
  ]
  return debug_view_url + urllib.parse.urlencode(data)


class DebugObject(object):
  """Base class for debug api wrappers."""

  # Lock for remote calls in routines which might be multithreaded. Client
  # connections are not thread-safe. Currently, only WaitForBreakpoint can
  # be called from multiple threads.
  _client_lock = threading.Lock()

  # Breakpoint type name constants
  SNAPSHOT_TYPE = 'SNAPSHOT'
  LOGPOINT_TYPE = 'LOGPOINT'

  def BreakpointAction(self, type_name):
    if type_name == self.SNAPSHOT_TYPE:
      return self._debug_messages.Breakpoint.ActionValueValuesEnum.CAPTURE
    if type_name == self.LOGPOINT_TYPE:
      return self._debug_messages.Breakpoint.ActionValueValuesEnum.LOG
    raise errors.InvalidBreakpointTypeError(type_name)

  CLIENT_VERSION = 'google.com/gcloud/{0}'.format(config.CLOUD_SDK_VERSION)

  def __init__(self, debug_client=None, debug_messages=None,
               resource_client=None, resource_messages=None):
    """Sets up class with instantiated api client."""
    self._debug_client = (
        debug_client or apis.GetClientInstance('clouddebugger', 'v2'))
    self._debug_messages = (
        debug_messages or apis.GetMessagesModule('clouddebugger', 'v2'))
    self._resource_client = (
        resource_client or
        apis.GetClientInstance('cloudresourcemanager', 'v1beta1'))
    self._resource_messages = (
        resource_messages or
        apis.GetMessagesModule('cloudresourcemanager', 'v1beta1'))
    self._resource_parser = resources.REGISTRY.Clone()
    self._resource_parser.RegisterApiByName('clouddebugger', 'v2')


class Debugger(DebugObject):
  """Abstracts Cloud Debugger service for a project."""

  def __init__(self, project, debug_client=None, debug_messages=None,
               resource_client=None, resource_messages=None):
    super(Debugger, self).__init__(
        debug_client=debug_client, debug_messages=debug_messages,
        resource_client=resource_client, resource_messages=resource_messages)
    self._project = project

  def ListDebuggees(self, include_inactive=False, include_stale=False):
    """Lists all debug targets registered with the debug service.

    Args:
      include_inactive: If true, also include debuggees that are not currently
        running.
      include_stale: If false, filter out any debuggees that refer to
        stale minor versions. A debugge represents a stale minor version if it
        meets the following criteria:
            1. It has a minorversion label.
            2. All other debuggees with the same name (i.e., all debuggees with
               the same module and version, in the case of app engine) have a
               minorversion label.
            3. The minorversion value for the debuggee is less than the
               minorversion value for at least one other debuggee with the same
               name.
    Returns:
      [Debuggee] A list of debuggees.
    """
    request = self._debug_messages.ClouddebuggerDebuggerDebuggeesListRequest(
        project=self._project, includeInactive=include_inactive,
        clientVersion=self.CLIENT_VERSION)
    try:
      response = self._debug_client.debugger_debuggees.List(request)
    except apitools_exceptions.HttpError as error:
      raise errors.UnknownHttpError(error)

    result = [Debuggee(debuggee) for debuggee in response.debuggees]

    if not include_stale:
      return _FilterStaleMinorVersions(result)

    return result

  def DefaultDebuggee(self):
    """Find the default debuggee.

    Returns:
      The default debug target, which is either the only target available
      or the latest minor version of the application, if all targets have the
      same module and version.
    Raises:
      errors.NoDebuggeeError if no debuggee was found.
      errors.MultipleDebuggeesError if there is not a unique default.
    """
    debuggees = self.ListDebuggees()
    if len(debuggees) == 1:
      # Just one possible target
      return debuggees[0]

    if not debuggees:
      raise errors.NoDebuggeeError()

    # More than one module or version. Can't determine the default target.
    raise errors.MultipleDebuggeesError(None, debuggees)

  def FindDebuggee(self, pattern=None):
    """Find the unique debuggee matching the given pattern.

    Args:
      pattern: A string containing a debuggee ID or a regular expression that
        matches a single debuggee's name or description. If it matches any
        debuggee name, the description will not be inspected.
    Returns:
      The matching Debuggee.
    Raises:
      errors.MultipleDebuggeesError if the pattern matches multiple debuggees.
      errors.NoDebuggeeError if the pattern matches no debuggees.
    """
    if not pattern:
      debuggee = self.DefaultDebuggee()
      log.status.write(
          'Debug target not specified. Using default target: {0}\n'.format(
              debuggee.name))
      return debuggee

    try:
      # Look for active debuggees first, since there are usually very
      # few of them compared to inactive debuggees.
      all_debuggees = self.ListDebuggees()
      return self._FilterDebuggeeList(all_debuggees, pattern)
    except errors.NoDebuggeeError:
      # Try looking at inactive debuggees
      pass
    all_debuggees = self.ListDebuggees(include_inactive=True,
                                       include_stale=True)
    return self._FilterDebuggeeList(all_debuggees, pattern)

  def _FilterDebuggeeList(self, all_debuggees, pattern):
    """Finds the debuggee which matches the given pattern.

    Args:
      all_debuggees: A list of debuggees to search.
      pattern: A string containing a debuggee ID or a regular expression that
        matches a single debuggee's name or description. If it matches any
        debuggee name, the description will not be inspected.
    Returns:
      The matching Debuggee.
    Raises:
      errors.MultipleDebuggeesError if the pattern matches multiple debuggees.
      errors.NoDebuggeeError if the pattern matches no debuggees.
    """
    if not all_debuggees:
      raise errors.NoDebuggeeError()

    latest_debuggees = _FilterStaleMinorVersions(all_debuggees)

    # Find all debuggees specified by ID, plus all debuggees which are the
    # latest minor version when specified by name.
    debuggees = ([d for d in all_debuggees if d.target_id == pattern] +
                 [d for d in latest_debuggees if pattern == d.name])
    if not debuggees:
      # Try matching as an RE on name or description. Name and description
      # share common substrings, so filter out duplicates.
      match_re = re.compile(pattern)
      debuggees = (
          [d for d in latest_debuggees if match_re.search(d.name)] +
          [d for d in latest_debuggees
           if d.description and match_re.search(d.description)])

    if not debuggees:
      raise errors.NoDebuggeeError(pattern, debuggees=all_debuggees)

    debuggee_ids = set(d.target_id for d in debuggees)
    if len(debuggee_ids) > 1:
      raise errors.MultipleDebuggeesError(pattern, debuggees)

    # Just one possible target
    return debuggees[0]

  def RegisterDebuggee(self, description, uniquifier, agent_version=None):
    """Register a debuggee with the Cloud Debugger.

    This method is primarily intended to simplify testing, since it registering
    a debuggee is only a small part of the functionality of a debug agent, and
    the rest of the API is not supported here.
    Args:
      description: A concise description of the debuggee.
      uniquifier: A string uniquely identifying the debug target. Note that the
        uniquifier distinguishes between different deployments of a service,
        not between different replicas of a single deployment. I.e., all
        replicas of a single deployment should report the same uniquifier.
      agent_version: A string describing the program registering the debuggee.
        Defaults to "google.com/gcloud/NNN" where NNN is the gcloud version.
    Returns:
      The registered Debuggee.
    """
    if not agent_version:
      agent_version = self.CLIENT_VERSION
    request = self._debug_messages.RegisterDebuggeeRequest(
        debuggee=self._debug_messages.Debuggee(
            project=self._project, description=description,
            uniquifier=uniquifier, agentVersion=agent_version))
    try:
      response = self._debug_client.controller_debuggees.Register(request)
    except apitools_exceptions.HttpError as error:
      raise errors.UnknownHttpError(error)
    return Debuggee(response.debuggee)


class Debuggee(DebugObject):
  """Represents a single debuggee."""

  def __init__(self, message, debug_client=None, debug_messages=None,
               resource_client=None, resource_messages=None):
    super(Debuggee, self).__init__(
        debug_client=debug_client, debug_messages=debug_messages,
        resource_client=resource_client, resource_messages=resource_messages)
    self.project = message.project
    self.agent_version = message.agentVersion
    self.description = message.description
    self.ext_source_contexts = message.extSourceContexts
    self.target_id = message.id
    self.is_disabled = message.isDisabled
    self.is_inactive = message.isInactive
    self.source_contexts = message.sourceContexts
    self.status = message.status
    self.target_uniquifier = message.uniquifier
    self.labels = {}
    if message.labels:
      for l in message.labels.additionalProperties:
        self.labels[l.key] = l.value

  def __eq__(self, other):
    return (isinstance(other, self.__class__) and
            self.target_id == other.target_id)

  def __ne__(self, other):
    return not self.__eq__(other)

  def __repr__(self):
    return '<id={0}, name={1}{2}>'.format(
        self.target_id, self.name, ', description={0}'.format(self.description)
        if self.description else '')

  @property
  def service(self):
    return self.labels.get('module', None)

  @property
  def version(self):
    return self.labels.get('version', None)

  @property
  def minorversion(self):
    return self.labels.get('minorversion', None)

  @property
  def name(self):
    service = self.service
    version = self.version
    if service or version:
      return (service or DEFAULT_MODULE) + '-' + (version or DEFAULT_VERSION)
    return self.description

  def _BreakpointDescription(self, restrict_to_type):
    if not restrict_to_type:
      return 'breakpoint'
    elif restrict_to_type == self.SNAPSHOT_TYPE:
      return 'snapshot'
    else:
      return 'logpoint'

  def GetBreakpoint(self, breakpoint_id):
    """Gets the details for a breakpoint.

    Args:
      breakpoint_id: A breakpoint ID.
    Returns:
      The full Breakpoint message for the ID.
    """
    request = (self._debug_messages.
               ClouddebuggerDebuggerDebuggeesBreakpointsGetRequest(
                   breakpointId=breakpoint_id, debuggeeId=self.target_id,
                   clientVersion=self.CLIENT_VERSION))
    try:
      response = self._debug_client.debugger_debuggees_breakpoints.Get(request)
    except apitools_exceptions.HttpError as error:
      raise errors.UnknownHttpError(error)
    return self.AddTargetInfo(response.breakpoint)

  def DeleteBreakpoint(self, breakpoint_id):
    """Deletes a breakpoint.

    Args:
      breakpoint_id: A breakpoint ID.
    """
    request = (self._debug_messages.
               ClouddebuggerDebuggerDebuggeesBreakpointsDeleteRequest(
                   breakpointId=breakpoint_id, debuggeeId=self.target_id,
                   clientVersion=self.CLIENT_VERSION))
    try:
      self._debug_client.debugger_debuggees_breakpoints.Delete(request)
    except apitools_exceptions.HttpError as error:
      raise errors.UnknownHttpError(error)

  def ListBreakpoints(self, location_regexp=None, resource_ids=None,
                      include_all_users=False, include_inactive=False,
                      restrict_to_type=None, full_details=False):
    """Returns all breakpoints matching the given IDs or patterns.

    Lists all breakpoints for this debuggee, and returns every breakpoint
    where the location field contains the given pattern or the ID is exactly
    equal to the pattern (there can be at most one breakpoint matching by ID).

    Args:
      location_regexp: A list of regular expressions to compare against the
        location ('path:line') of the breakpoints. If both location_regexp and
        resource_ids are empty or None, all breakpoints will be returned.
      resource_ids: Zero or more resource IDs in the form expected by the
        resource parser. These breakpoints will be retrieved regardless
        of the include_all_users or include_inactive flags
      include_all_users: If true, search breakpoints created by all users.
      include_inactive: If true, search breakpoints that are in the final state.
        This option controls whether regular expressions can match inactive
        breakpoints. If an object is specified by ID, it will be returned
        whether or not this flag is set.
      restrict_to_type: An optional breakpoint type (LOGPOINT_TYPE or
        SNAPSHOT_TYPE)
      full_details: If true, issue a GetBreakpoint request for every result to
        get full details including the call stack and variable table.
    Returns:
      A list of all matching breakpoints.
    Raises:
      InvalidLocationException if a regular expression is not valid.
    """
    resource_ids = resource_ids or []
    location_regexp = location_regexp or []
    ids = set(
        [self._resource_parser.Parse(
            r, params={'debuggeeId': self.target_id},
            collection='clouddebugger.debugger.debuggees.breakpoints').Name()
         for r in resource_ids])
    patterns = []
    for r in location_regexp:
      try:
        patterns.append(re.compile(r'^(.*/)?(' + r + ')$'))
      except re.error as e:
        raise errors.InvalidLocationException(
            'The location pattern "{0}" is not a valid Python regular '
            'expression: {1}'.format(r, e))

    request = (self._debug_messages.
               ClouddebuggerDebuggerDebuggeesBreakpointsListRequest(
                   debuggeeId=self.target_id,
                   includeAllUsers=include_all_users,
                   includeInactive=include_inactive or bool(ids),
                   clientVersion=self.CLIENT_VERSION))
    try:
      response = self._debug_client.debugger_debuggees_breakpoints.List(request)
    except apitools_exceptions.HttpError as error:
      raise errors.UnknownHttpError(error)
    if not patterns and not ids:
      return self._FilteredDictListWithInfo(response.breakpoints,
                                            restrict_to_type)

    if include_inactive:
      # Match everything (including inactive breakpoints) against all ids and
      # patterns.
      result = [bp for bp in response.breakpoints
                if _BreakpointMatchesIdOrRegexp(bp, ids, patterns)]
    else:
      # Return everything that is listed by ID, plus every breakpoint that
      # is not inactive (i.e. isFinalState is false) which matches any pattern.
      # Breakpoints that are inactive should not be matched against the
      # patterns.
      result = [bp for bp in response.breakpoints
                if _BreakpointMatchesIdOrRegexp(
                    bp, ids, [] if bp.isFinalState else patterns)]
    # Check if any ids were missing, and fetch them individually. This can
    # happen if an ID for another user's breakpoint was specified, but the
    # all_users flag was false. This code will also raise an error for any
    # missing IDs.
    missing_ids = ids - set([bp.id for bp in result])
    if missing_ids:
      raise errors.BreakpointNotFoundError(
          missing_ids, self._BreakpointDescription(restrict_to_type))

    # Verify that all patterns matched at least one breakpoint.
    for p in patterns:
      if not [bp for bp in result
              if _BreakpointMatchesIdOrRegexp(bp, [], [p])]:
        raise errors.NoMatchError(self._BreakpointDescription(restrict_to_type),
                                  p.pattern)
    result = self._FilteredDictListWithInfo(result, restrict_to_type)
    if full_details:
      def IsCompletedSnapshot(bp):
        return ((not bp.action or
                 bp.action == self.BreakpointAction(self.SNAPSHOT_TYPE)) and
                bp.isFinalState and not (bp.status and bp.status.isError))
      result = [
          self.GetBreakpoint(bp.id) if IsCompletedSnapshot(bp) else bp
          for bp in result
      ]
    return result

  def CreateSnapshot(self, location, condition=None, expressions=None,
                     user_email=None, labels=None):
    """Creates a "snapshot" breakpoint.

    Args:
      location: The breakpoint source location, which will be interpreted by
        the debug agents on the machines running the Debuggee. Usually of the
        form file:line-number
      condition: An optional conditional expression in the target's programming
        language. The snapshot will be taken when the expression is true.
      expressions: A list of expressions to evaluate when the snapshot is
        taken.
      user_email: The email of the user who created the snapshot.
      labels: A dictionary containing key-value pairs which will be stored
        with the snapshot definition and reported when the snapshot is queried.
    Returns:
      The created Breakpoint message.
    """
    labels_value = None
    if labels:
      labels_value = self._debug_messages.Breakpoint.LabelsValue(
          additionalProperties=[
              self._debug_messages.Breakpoint.LabelsValue.AdditionalProperty(
                  key=key, value=value)
              for key, value in six.iteritems(labels)])
    location = self._LocationFromString(location)
    if not expressions:
      expressions = []
    request = (
        self._debug_messages.
        ClouddebuggerDebuggerDebuggeesBreakpointsSetRequest(
            debuggeeId=self.target_id,
            breakpoint=self._debug_messages.Breakpoint(
                location=location, condition=condition, expressions=expressions,
                labels=labels_value, userEmail=user_email,
                action=(self._debug_messages.Breakpoint.
                        ActionValueValuesEnum.CAPTURE)),
            clientVersion=self.CLIENT_VERSION))
    try:
      response = self._debug_client.debugger_debuggees_breakpoints.Set(request)
    except apitools_exceptions.HttpError as error:
      raise errors.UnknownHttpError(error)
    return self.AddTargetInfo(response.breakpoint)

  def CreateLogpoint(self, location, log_format_string, log_level=None,
                     condition=None, user_email=None, labels=None):
    """Creates a logpoint in the debuggee.

    Args:
      location: The breakpoint source location, which will be interpreted by
        the debug agents on the machines running the Debuggee. Usually of the
        form file:line-number
      log_format_string: The message to log, optionally containin {expression}-
        style formatting.
      log_level: String (case-insensitive), one of 'info', 'warning', or
        'error', indicating the log level that should be used for logging.
      condition: An optional conditional expression in the target's programming
        language. The snapshot will be taken when the expression is true.
      user_email: The email of the user who created the snapshot.
      labels: A dictionary containing key-value pairs which will be stored
        with the snapshot definition and reported when the snapshot is queried.
    Returns:
      The created Breakpoint message.
    Raises:
      InvalidLocationException: if location is empty or malformed.
      InvalidLogFormatException: if log_format is empty or malformed.
    """
    if not location:
      raise errors.InvalidLocationException(
          'The location must not be empty.')
    if not log_format_string:
      raise errors.InvalidLogFormatException(
          'The log format string must not be empty.')
    labels_value = None
    if labels:
      labels_value = self._debug_messages.Breakpoint.LabelsValue(
          additionalProperties=[
              self._debug_messages.Breakpoint.LabelsValue.AdditionalProperty(
                  key=key, value=value)
              for key, value in six.iteritems(labels)])
    location = self._LocationFromString(location)
    if log_level:
      log_level = (
          self._debug_messages.Breakpoint.LogLevelValueValuesEnum(
              log_level.upper()))
    log_message_format, expressions = SplitLogExpressions(log_format_string)
    request = (
        self._debug_messages.
        ClouddebuggerDebuggerDebuggeesBreakpointsSetRequest(
            debuggeeId=self.target_id,
            breakpoint=self._debug_messages.Breakpoint(
                location=location, condition=condition, logLevel=log_level,
                logMessageFormat=log_message_format, expressions=expressions,
                labels=labels_value, userEmail=user_email,
                action=(self._debug_messages.Breakpoint.
                        ActionValueValuesEnum.LOG)),
            clientVersion=self.CLIENT_VERSION))
    try:
      response = self._debug_client.debugger_debuggees_breakpoints.Set(request)
    except apitools_exceptions.HttpError as error:
      raise errors.UnknownHttpError(error)
    return self.AddTargetInfo(response.breakpoint)

  def _CallGet(self, request):
    with self._client_lock:
      return self._debug_client.debugger_debuggees_breakpoints.Get(request)

  def WaitForBreakpointSet(self, breakpoint_id, original_location, timeout=None,
                           retry_ms=500):
    """Waits for a breakpoint to be set by at least one agent.

      Breakpoint set can be detected in two ways: it can be completed, or the
      location may change if the breakpoint could not be set at the specified
      location. A breakpoint may also be set without any change being reported
      to the server, in which case this function will wait until the timeout
      is reached.
    Args:
      breakpoint_id: A breakpoint ID.
      original_location: string, the user-specified breakpoint location. If a
        response has a different location, the function will return immediately.
      timeout: The number of seconds to wait for completion.
      retry_ms: Milliseconds to wait betweeen retries.
    Returns:
      The Breakpoint message, or None if the breakpoint did not get set before
      the timeout.
    """
    def MovedOrFinal(r):
      return (
          r.breakpoint.isFinalState or
          (original_location and
           original_location != _FormatLocation(r.breakpoint.location)))
    try:
      return self.WaitForBreakpoint(
          breakpoint_id=breakpoint_id, timeout=timeout, retry_ms=retry_ms,
          completion_test=MovedOrFinal)
    except apitools_exceptions.HttpError as error:
      raise errors.UnknownHttpError(error)

  def WaitForBreakpoint(self, breakpoint_id, timeout=None, retry_ms=500,
                        completion_test=None):
    """Waits for a breakpoint to be completed.

    Args:
      breakpoint_id: A breakpoint ID.
      timeout: The number of seconds to wait for completion.
      retry_ms: Milliseconds to wait betweeen retries.
      completion_test: A function that accepts a Breakpoint message and
        returns True if the breakpoint wait is not finished. If not specified,
        defaults to a function which just checks the isFinalState flag.
    Returns:
      The Breakpoint message, or None if the breakpoint did not complete before
      the timeout,
    """
    if not completion_test:
      completion_test = lambda r: r.breakpoint.isFinalState
    retry_if = lambda r, _: not completion_test(r)
    retryer = retry.Retryer(
        max_wait_ms=1000*timeout if timeout is not None else None,
        wait_ceiling_ms=1000)
    request = (self._debug_messages.
               ClouddebuggerDebuggerDebuggeesBreakpointsGetRequest(
                   breakpointId=breakpoint_id, debuggeeId=self.target_id,
                   clientVersion=self.CLIENT_VERSION))
    try:
      result = retryer.RetryOnResult(self._CallGet, [request],
                                     should_retry_if=retry_if,
                                     sleep_ms=retry_ms)
    except retry.RetryException:
      # Timeout before the beakpoint was finalized.
      return None
    except apitools_exceptions.HttpError as error:
      raise errors.UnknownHttpError(error)
    if not completion_test(result):
      # Termination condition was not met
      return None
    return self.AddTargetInfo(result.breakpoint)

  def WaitForMultipleBreakpoints(self, ids, wait_all=False, timeout=None):
    """Waits for one or more breakpoints to complete.

    Args:
      ids: A list of breakpoint IDs.
      wait_all: If True, wait for all breakpoints to complete. Otherwise, wait
        for any breakpoint to complete.
      timeout: The number of seconds to wait for completion.
    Returns:
      The completed Breakpoint messages, in the order requested. If wait_all was
      specified and the timeout was reached, the result will still comprise the
      completed Breakpoints.
    """
    waiter = _BreakpointWaiter(wait_all, timeout)
    for i in ids:
      waiter.AddTarget(self, i)
    results = waiter.Wait()
    return [results[i] for i in ids if i in results]

  def AddTargetInfo(self, message):
    """Converts a message into an object with added debuggee information.

    Args:
      message: A message returned from a debug API call.
    Returns:
      An object including the fields of the original object plus the following
      fields: project, target_uniquifier, and target_id.
    """
    result = _MessageDict(message, hidden_fields={
        'project': self.project,
        'target_uniquifier': self.target_uniquifier,
        'target_id': self.target_id,
        'service': self.service,
        'version': self.version})
    # Restore some default values if they were stripped
    if (message.action ==
        self._debug_messages.Breakpoint.ActionValueValuesEnum.LOG and
        not message.logLevel):
      result['logLevel'] = (
          self._debug_messages.Breakpoint.LogLevelValueValuesEnum.INFO)

    if message.isFinalState is None:
      result['isFinalState'] = False

    # Reformat a few fields for readability
    if message.location:
      result['location'] = _FormatLocation(message.location)
    if message.logMessageFormat:
      result['logMessageFormat'] = MergeLogExpressions(message.logMessageFormat,
                                                       message.expressions)
      result.HideExistingField('expressions')

    if not message.status or not message.status.isError:
      if message.action == self.BreakpointAction(self.LOGPOINT_TYPE):
        # We can only generate view URLs for GAE, since there's not a standard
        # way to view them in GCE. Use the presence of minorversion as an
        # indicator that it's GAE.
        if self.minorversion:
          result['logQuery'] = LogQueryV2String(result)
          result['logViewUrl'] = LogViewUrl(result)
      else:
        result['consoleViewUrl'] = DebugViewUrl(result)

    return result

  def _LocationFromString(self, location):
    """Converts a file:line location string into a SourceLocation.

    Args:
      location: A string of the form file:line.
    Returns:
      The corresponding SourceLocation message.
    Raises:
      InvalidLocationException: if the line is not of the form path:line
    """
    components = location.split(':')
    if len(components) != 2:
      raise errors.InvalidLocationException(
          'Location must be of the form "path:line"')
    try:
      return self._debug_messages.SourceLocation(path=components[0],
                                                 line=int(components[1]))
    except ValueError:
      raise errors.InvalidLocationException(
          'Location must be of the form "path:line", where "line" must be an '
          'integer.')

  def _FilteredDictListWithInfo(self, result, restrict_to_type):
    """Filters a result list to contain only breakpoints of the given type.

    Args:
      result: A list of breakpoint messages, to be filtered.
      restrict_to_type: An optional breakpoint type. If None, no filtering
        will be done.
    Returns:
      The filtered result, converted to equivalent dicts with debug info fields
      added.
    """
    return [self.AddTargetInfo(r) for r in result
            if not restrict_to_type
            or r.action == self.BreakpointAction(restrict_to_type)
            or (not r.action and restrict_to_type == self.SNAPSHOT_TYPE)]


class _BreakpointWaiter(object):
  """Waits for multiple breakpoints.

  Attributes:
    _result_lock: Lock for modifications to all fields
    _done: Flag to indicate that the wait condition is satisfied and wait
        should stop even if some threads are not finished.
    _threads: The list of active threads
    _results: The set of completed breakpoints.
    _failures: All exceptions which caused any thread to stop waiting.
    _wait_all: If true, wait for all breakpoints to complete, else wait for
        any breakpoint to complete. Controls whether to set _done after any
        breakpoint completes.
    _timeout: Mazimum time (in ms) to wait for breakpoints to complete.
  """

  def __init__(self, wait_all, timeout):
    self._result_lock = threading.Lock()
    self._done = False
    self._threads = []
    self._results = {}
    self._failures = []
    self._wait_all = wait_all
    self._timeout = timeout

  def _IsComplete(self, response):
    if response.breakpoint.isFinalState:
      return True
    with self._result_lock:
      return self._done

  def _WaitForOne(self, debuggee, breakpoint_id):
    try:
      breakpoint = debuggee.WaitForBreakpoint(
          breakpoint_id, timeout=self._timeout,
          completion_test=self._IsComplete)
      if not breakpoint:
        # Breakpoint never completed (i.e. timeout)
        with self._result_lock:
          if not self._wait_all:
            self._done = True
        return
      if breakpoint.isFinalState:
        with self._result_lock:
          self._results[breakpoint_id] = breakpoint
          if not self._wait_all:
            self._done = True
    except errors.DebugError as e:
      with self._result_lock:
        self._failures.append(e)
        self._done = True

  def AddTarget(self, debuggee, breakpoint_id):
    self._threads.append(
        threading.Thread(target=self._WaitForOne,
                         args=(debuggee, breakpoint_id)))

  def Wait(self):
    for t in self._threads:
      t.start()
    for t in self._threads:
      t.join()
    if self._failures:
      # Just raise the first exception we handled
      raise self._failures[0]
    return self._results


def _FormatLocation(location):
  if not location:
    return None
  return '{0}:{1}'.format(location.path, location.line)


def _BreakpointMatchesIdOrRegexp(breakpoint, ids, patterns):
  """Check if a breakpoint matches any of the given IDs or regexps.

  Args:
    breakpoint: Any _debug_messages.Breakpoint message object.
    ids: A set of strings to search for exact matches on breakpoint ID.
    patterns: A list of regular expressions to match against the file:line
      location of the breakpoint.
  Returns:
    True if the breakpoint matches any ID or pattern.
  """
  if breakpoint.id in ids:
    return True
  if not breakpoint.location:
    return False
  location = _FormatLocation(breakpoint.location)
  for p in patterns:
    if p.match(location):
      return True
  return False


def _FilterStaleMinorVersions(debuggees):
  """Filter out any debugees referring to a stale minor version.

  Args:
    debuggees: A list of Debuggee objects.
  Returns:
    A filtered list containing only the debuggees denoting the most recent
    minor version with the given name. If any debuggee with a given name does
    not have a 'minorversion' label, the resulting list will contain all
    debuggees with that name.
  """
  # First group by name
  byname = {}
  for debuggee in debuggees:
    if debuggee.name in byname:
      byname[debuggee.name].append(debuggee)
    else:
      byname[debuggee.name] = [debuggee]
  # Now look at each list for a given name, choosing only the latest
  # version.
  result = []
  for name_list in byname.values():
    latest = _FindLatestMinorVersion(name_list)
    if latest:
      result.append(latest)
    else:
      result.extend(name_list)
  return result


def _FindLatestMinorVersion(debuggees):
  """Given a list of debuggees, find the one with the highest minor version.

  Args:
    debuggees: A list of Debuggee objects.
  Returns:
    If all debuggees have the same name, return the one with the highest
    integer value in its 'minorversion' label. If any member of the list does
    not have a minor version, or if elements of the list have different
    names, returns None.
  """
  if not debuggees:
    return None
  best = None
  best_version = None
  name = None
  for d in debuggees:
    if not name:
      name = d.name
    elif name != d.name:
      return None
    minor_version = d.labels.get('minorversion', 0)
    if not minor_version:
      return None
    try:
      minor_version = int(minor_version)
      if not best_version or minor_version > best_version:
        best_version = minor_version
        best = d
    except ValueError:
      # Got a bogus minor version. We can't determine which is best.
      return None
  return best


class _MessageDict(dict):
  """An extensible wrapper around message data.

  Fields can be added as dictionary items and retrieved as attributes.
  """

  def __init__(self, message, hidden_fields=None):
    super(_MessageDict, self).__init__()
    self._orig_type = type(message).__name__
    if hidden_fields:
      self._hidden_fields = hidden_fields
    else:
      self._hidden_fields = {}
    for field in message.all_fields():
      value = getattr(message, field.name)
      if not value:
        self._hidden_fields[field.name] = value
      else:
        self[field.name] = value

  def __getattr__(self, attr):
    if attr in self:
      return self[attr]
    if attr in self._hidden_fields:
      return self._hidden_fields[attr]
    raise AttributeError('Type "{0}" does not have attribute "{1}"'.format(
        self._orig_type, attr))

  def HideExistingField(self, field_name):
    if field_name in self._hidden_fields:
      return
    self._hidden_fields[field_name] = self.pop(field_name, None)
