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

"""Exceptions that can be thrown by calliope tools.

The exceptions in this file, and those that extend them, can be thrown by
the Run() function in calliope tools without worrying about stack traces
littering the screen in CLI mode. In interpreter mode, they are not caught
from within calliope.
"""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import errno
from functools import wraps
import os
import sys

from googlecloudsdk.api_lib.util import exceptions as api_exceptions  # pytype: disable=import-error
from googlecloudsdk.core import exceptions as core_exceptions
from googlecloudsdk.core import log
from googlecloudsdk.core import properties
from googlecloudsdk.core.console import console_attr
from googlecloudsdk.core.console import console_attr_os

import six


def NewErrorFromCurrentException(error, *args):
  """Creates a new error based on the current exception being handled.

  If no exception is being handled, a new error with the given args
  is created.  If there is a current exception, the original exception is
  first logged (to file only).  A new error is then created with the
  same args as the current one.

  Args:
    error: The new error to create.
    *args: The standard args taken by the constructor of Exception for the new
      exception that is created.  If None, the args from the exception
      currently being handled will be used.

  Returns:
    The generated error exception.
  """
  (_, current_exception, _) = sys.exc_info()

  # Log original exception details and traceback to the log file if we are
  # currently handling an exception.
  if current_exception:
    file_logger = log.file_only_logger
    file_logger.error('Handling the source of a tool exception, '
                      'original details follow.')
    file_logger.exception(current_exception)  # pytype: disable=wrong-arg-types

  if args:
    return error(*args)
  elif current_exception:
    return error(*current_exception.args)  # pytype: disable=attribute-error
  return error('An unknown error has occurred')


# TODO(b/32328530): Remove ToolException when the last ref is gone
class ToolException(core_exceptions.Error):
  """ToolException is for Run methods to throw for non-code-bug errors.

  Attributes:
    command_name: The dotted group and command name for the command that threw
        this exception. This value is set by calliope.
  """

  @staticmethod
  def FromCurrent(*args):
    return NewErrorFromCurrentException(ToolException, *args)


class ExitCodeNoError(core_exceptions.Error):
  """A special exception for exit codes without error messages.

  If this exception is raised, it's identical in behavior to returning from
  the command code, except the overall exit code will be different.
  """


class FailedSubCommand(core_exceptions.Error):
  """Exception capturing a subcommand which did sys.exit(code)."""

  def __init__(self, cmd, code):
    super(FailedSubCommand, self).__init__(
        'Failed command: [{0}] with exit code [{1}]'.format(
            ' '.join(cmd), code),
        exit_code=code)


def RaiseErrorInsteadOf(error, *error_types):
  """A decorator that re-raises as an error.

  If any of the error_types are raised in the decorated function, this decorator
  will re-raise as an error.

  Args:
    error: Exception, The new exception to raise.
    *error_types: [Exception], A list of exception types that this decorator
        will watch for.

  Returns:
    The decorated function.
  """
  def Wrap(func):
    """Wrapper function for the decorator."""
    @wraps(func)
    def TryFunc(*args, **kwargs):
      try:
        return func(*args, **kwargs)
      except error_types:
        core_exceptions.reraise(NewErrorFromCurrentException(error))
    return TryFunc
  return Wrap


# TODO(b/32328530): Remove RaiseToolExceptionInsteadOf when the last ref is gone
def RaiseToolExceptionInsteadOf(*error_types):
  """A decorator that re-raises as ToolException."""
  return RaiseErrorInsteadOf(ToolException, *error_types)


def _TruncateToLineWidth(string, align, width, fill=''):
  """Truncate string to line width, right aligning at align.

  Examples (assuming a screen width of 10):

  >>> _TruncateToLineWidth('foo', 0)
  'foo'
  >>> # Align to the beginning. Should truncate the end.
  ... _TruncateToLineWidth('0123456789abcdef', 0)
  '0123456789'
  >>> _TruncateToLineWidth('0123456789abcdef', 0, fill='...')
  '0123456...'
  >>> # Align to the end. Should truncate the beginning.
  ... _TruncateToLineWidth('0123456789abcdef', 16)
  '6789abcdef'
  >>> _TruncateToLineWidth('0123456789abcdef', 16, fill='...')
  '...9abcdef'
  >>> # Align to the middle (note: the index is toward the end of the string,
  ... # because this function right-aligns to the given index).
  ... # Should truncate the begnining and end.
  ... _TruncateToLineWidth('0123456789abcdef', 12)
  '23456789ab'
  >>> _TruncateToLineWidth('0123456789abcdef', 12, fill='...')
  '...5678...'

  Args:
    string: string to truncate
    align: index to right-align to
    width: maximum length for the resulting string
    fill: if given, indicate truncation with this string. Must be shorter than
      terminal width / 2.

  Returns:
    str, the truncated string

  Raises:
    ValueError, if provided fill is too long for the terminal.
  """
  if len(fill) >= width // 2:
    # Either the caller provided a fill that's way too long, or the user has a
    # terminal that's way too narrow. In either case, we aren't going to be able
    # to make this look nice, but we don't want to throw an error because that
    # will mask the original error.
    log.warning('Screen not wide enough to display correct error message.')
    return string

  if len(string) <= width:
    return string

  if align > width:
    string = fill + string[align-width+len(fill):]

  if len(string) <= width:
    return string
  string = string[:width-len(fill)] + fill
  return string


_MARKER = '^ invalid character'


def _NonAsciiIndex(s):
  """Returns the index of the first non-ascii char in s, -1 if all ascii."""
  if isinstance(s, six.text_type):
    for i, c in enumerate(s):
      try:
        c.encode('ascii')
      except (AttributeError, UnicodeError):
        return i
  else:
    for i, b in enumerate(s):
      try:
        b.decode('ascii')
      except (AttributeError, UnicodeError):
        return i
  return -1


# pylint: disable=g-doc-bad-indent
def _FormatNonAsciiMarkerString(args):
  r"""Format a string that will mark the first non-ASCII character it contains.


  Example:

  >>> args = ['command.py', '--foo=\xce\x94']
  >>> _FormatNonAsciiMarkerString(args) == (
  ...     'command.py --foo=\u0394\n'
  ...     '                 ^ invalid character'
  ... )
  True

  Args:
    args: The arg list for the command executed

  Returns:
    unicode, a properly formatted string with two lines, the second of which
      indicates the non-ASCII character in the first.

  Raises:
    ValueError: if the given string is all ASCII characters
  """
  # pos is the position of the first non-ASCII character in ' '.join(args)
  pos = 0
  for arg in args:
    first_non_ascii_index = _NonAsciiIndex(arg)
    if first_non_ascii_index >= 0:
      pos += first_non_ascii_index
      break
    # this arg was all ASCII; add 1 for the ' ' between args
    pos += len(arg) + 1
  else:
    raise ValueError(
        'The command line is composed entirely of ASCII characters.')

  # Make a string that, when printed in parallel, will point to the non-ASCII
  # character
  marker_string = ' ' * pos + _MARKER

  # Make sure that this will still print out nicely on an odd-sized screen
  align = len(marker_string)
  args_string = ' '.join(
      [console_attr.SafeText(arg) for arg in args])
  width, _ = console_attr_os.GetTermSize()
  fill = '...'
  if width < len(_MARKER) + len(fill):
    # It's hopeless to try to wrap this and make it look nice. Preserve it in
    # full for logs and so on.
    return '\n'.join((args_string, marker_string))
  # If len(args_string) < width < len(marker_string) (ex:)
  #
  #   args_string   = 'command BAD'
  #   marker_string = '        ^ invalid character'
  #   width     = len('----------------')
  #
  # then the truncation can give a result like the following:
  #
  #   args_string   = 'command BAD'
  #   marker_string = '   ^ invalid character'
  #
  # (This occurs when args_string is short enough to not be truncated, but
  # marker_string is long enough to be truncated.)
  #
  # ljust args_string to make it as long as marker_string before passing to
  # _TruncateToLineWidth, which will yield compatible truncations. rstrip at the
  # end to get rid of the new trailing spaces.
  formatted_args_string = _TruncateToLineWidth(args_string.ljust(align), align,
                                               width, fill=fill).rstrip()
  formatted_marker_string = _TruncateToLineWidth(marker_string, align, width)
  return '\n'.join((formatted_args_string, formatted_marker_string))


class InvalidCharacterInArgException(ToolException):
  """InvalidCharacterInArgException is for non-ASCII CLI arguments."""

  def __init__(self, args, invalid_arg):
    self.invalid_arg = invalid_arg
    cmd = os.path.basename(args[0])
    if cmd.endswith('.py'):
      cmd = cmd[:-3]
    args = [cmd] + args[1:]

    super(InvalidCharacterInArgException, self).__init__(
        'Failed to read command line argument [{0}] because it does '
        'not appear to be valid 7-bit ASCII.\n\n'
        '{1}'.format(
            console_attr.SafeText(self.invalid_arg),
            _FormatNonAsciiMarkerString(args)))


class BadArgumentException(ToolException):
  """For arguments that are wrong for reason hard to summarize."""

  def __init__(self, argument_name, message):
    super(BadArgumentException, self).__init__(
        'Invalid value for [{0}]: {1}'.format(argument_name, message))
    self.argument_name = argument_name


# TODO(b/35938745): Eventually use api_exceptions.HttpException exclusively.
class HttpException(api_exceptions.HttpException):
  """HttpException is raised whenever the Http response status code != 200.

  See api_lib.util.exceptions.HttpException for full documentation.
  """


class InvalidArgumentException(ToolException):
  """InvalidArgumentException is for malformed arguments."""

  def __init__(self, parameter_name, message):
    super(InvalidArgumentException, self).__init__(
        'Invalid value for [{0}]: {1}'.format(parameter_name, message))
    self.parameter_name = parameter_name


class ConflictingArgumentsException(ToolException):
  """ConflictingArgumentsException arguments that are mutually exclusive."""

  def __init__(self, *parameter_names):
    super(ConflictingArgumentsException, self).__init__(
        'arguments not allowed simultaneously: ' + ', '.join(parameter_names))
    self.parameter_names = parameter_names


class UnknownArgumentException(ToolException):
  """UnknownArgumentException is for arguments with unexpected values."""

  def __init__(self, parameter_name, message):
    super(UnknownArgumentException, self).__init__(
        'Unknown value for [{0}]: {1}'.format(parameter_name, message))
    self.parameter_name = parameter_name


class RequiredArgumentException(ToolException):
  """An exception for when a usually optional argument is required in this case.
  """

  def __init__(self, parameter_name, message):
    super(RequiredArgumentException, self).__init__(
        'Missing required argument [{0}]: {1}'.format(parameter_name, message))
    self.parameter_name = parameter_name


class OneOfArgumentsRequiredException(ToolException):
  """An exception for when one of usually optional arguments is required.
  """

  def __init__(self, parameters, message):
    super(OneOfArgumentsRequiredException, self).__init__(
        'One of arguments [{0}] is required: {1}'.format(
            ', '.join(parameters), message))
    self.parameters = parameters


class MinimumArgumentException(ToolException):
  """An exception for when one of several arguments is required."""

  def __init__(self, parameter_names, message=None):
    if message:
      message = ': {}'.format(message)
    else:
      message = ''
    super(MinimumArgumentException, self).__init__(
        'One of [{0}] must be supplied{1}.'.format(
            ', '.join(['{0}'.format(p) for p in parameter_names]),
            message)
        )


class BadFileException(ToolException):
  """BadFileException is for problems reading or writing a file."""


# pylint: disable=g-import-not-at-top, Delay the import of this because
# importing store is relatively expensive.
def _GetTokenRefreshError(exc):
  from googlecloudsdk.core.credentials import store
  return store.TokenRefreshError(exc)


# In general, lower level libraries should be catching exceptions and re-raising
# exceptions that extend core.Error so nice error messages come out. There are
# some error classes that want to be handled as recoverable errors, but cannot
# import the core_exceptions module (and therefore the Error class) for various
# reasons (e.g. circular dependencies). To work around this, we keep a list of
# known "friendly" error types, which we handle in the same way as core.Error.
# Additionally, we provide an alternate exception class to convert the errors
# to which may add additional information.  We use strings here so that we don't
# have to import all these libraries all the time, just to be able to handle the
# errors when they come up.  Only add errors here if there is no other way to
# handle them.
_KNOWN_ERRORS = {
    'apitools.base.py.exceptions.HttpError': HttpException,
    'googlecloudsdk.calliope.parser_errors.ArgumentError': lambda x: None,
    'googlecloudsdk.core.util.files.Error': lambda x: None,
    'httplib.ResponseNotReady': core_exceptions.NetworkIssueError,
    # Same error but different location on PY3.
    'http.client.ResponseNotReady': core_exceptions.NetworkIssueError,
    'oauth2client.client.AccessTokenRefreshError': _GetTokenRefreshError,
    'ssl.SSLError': core_exceptions.NetworkIssueError,
    'socket.error': core_exceptions.NetworkIssueError,
}


def _GetExceptionName(cls):
  """Returns the exception name used as index into _KNOWN_ERRORS from type."""
  return cls.__module__ + '.' + cls.__name__


_SOCKET_ERRNO_NAMES = {
    'EADDRINUSE', 'EADDRNOTAVAIL', 'EAFNOSUPPORT', 'EBADMSG', 'ECOMM',
    'ECONNABORTED', 'ECONNREFUSED', 'ECONNRESET', 'EDESTADDRREQ', 'EHOSTDOWN',
    'EHOSTUNREACH', 'EISCONN', 'EMSGSIZE', 'EMULTIHOP', 'ENETDOWN', 'ENETRESET',
    'ENETUNREACH', 'ENOBUFS', 'ENOPROTOOPT', 'ENOTCONN', 'ENOTSOCK', 'ENOTUNIQ',
    'EOPNOTSUPP', 'EPFNOSUPPORT', 'EPROTO', 'EPROTONOSUPPORT', 'EPROTOTYPE',
    'EREMCHG', 'EREMOTEIO', 'ESHUTDOWN', 'ESOCKTNOSUPPORT', 'ETIMEDOUT',
    'ETOOMANYREFS',
}


def _IsSocketError(exc):
  """Returns True if exc is a socket error exception."""

  # I've a feeling we're not in python 2 anymore. PEP 3151 eliminated module
  # specific exceptions in favor of builtin exceptions like OSError. Good
  # for some things, bad for others. For instance, this brittle errno check
  # for "network" errors. We use names because errnos are system dependent.
  return errno.errorcode[exc.errno] in _SOCKET_ERRNO_NAMES


def ConvertKnownError(exc):
  """Convert the given exception into an alternate type if it is known.

  Searches backwards through Exception type hierarchy until it finds a match.

  Args:
    exc: Exception, the exception to convert.

  Returns:
    (exception, bool), exception is None if this is not a known type, otherwise
    a new exception that should be logged. The boolean is True if the error
    should be printed, or False to just exit without printing.
  """
  if isinstance(exc, ExitCodeNoError):
    return exc, False
  elif isinstance(exc, core_exceptions.Error):
    return exc, True

  known_err = None

  classes = [type(exc)]
  processed = set([])  # To avoid circular dependencies
  while classes:
    cls = classes.pop(0)
    processed.add(cls)
    name = _GetExceptionName(cls)
    if name == 'builtins.OSError' and _IsSocketError(exc):
      known_err = core_exceptions.NetworkIssueError
    else:
      known_err = _KNOWN_ERRORS.get(name)
    if known_err:
      break

    bases = [bc for bc in cls.__bases__
             if bc not in processed and issubclass(bc, Exception)]
    classes.extend([base for base in bases if base is not Exception])

  if not known_err:
    # This is not a known error type
    return None, True

  # If there is no known exception just return the original exception.
  new_exc = known_err(exc)
  return (new_exc, True) if new_exc else (exc, True)


def HandleError(exc, command_path, known_error_handler=None):
  """Handles an error that occurs during command execution.

  It calls ConvertKnownError to convert exceptions to known types before
  processing. If it is a known type, it is printed nicely as as error. If not,
  it is raised as a crash.

  Args:
    exc: Exception, The original exception that occurred.
    command_path: str, The name of the command that failed (for error
      reporting).
    known_error_handler: f(): A function to report the current exception as a
      known error.
  """
  known_exc, print_error = ConvertKnownError(exc)
  if known_exc:
    _LogKnownError(known_exc, command_path, print_error)
    # Uncaught errors will be handled in gcloud_main.
    if known_error_handler:
      known_error_handler()
    if properties.VALUES.core.print_handled_tracebacks.GetBool():
      core_exceptions.reraise(exc)
    _Exit(known_exc)
  else:
    # Make sure any uncaught exceptions still make it into the log file.
    log.debug(console_attr.SafeText(exc), exc_info=sys.exc_info())
    core_exceptions.reraise(exc)


def _LogKnownError(known_exc, command_path, print_error):
  msg = '({0}) {1}'.format(
      console_attr.SafeText(command_path),
      console_attr.SafeText(known_exc))
  log.debug(msg, exc_info=sys.exc_info())
  if print_error:
    log.error(msg)


def _Exit(exc):
  """This method exists so we can mock this out during testing to not exit."""
  # exit_code won't be defined in the KNOWN_ERRORs classes
  sys.exit(getattr(exc, 'exit_code', 1))
