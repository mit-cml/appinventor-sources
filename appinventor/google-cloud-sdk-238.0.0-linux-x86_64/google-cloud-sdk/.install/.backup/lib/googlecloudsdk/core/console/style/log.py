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
"""Contains a list of colors and attributes available in ANSI."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.core import log as core_log
from googlecloudsdk.core.console.style import text


def _PrintResourceChange(operation,
                         resource,
                         kind,
                         is_async,
                         details,
                         failed,
                         operation_past_tense=None):
  """Prints a status message for operation on resource.

  The non-failure status messages are disabled when user output is disabled.

  Args:
    operation: str, The completed operation name.
    resource: str, The resource name.
    kind: str, The resource kind (instance, cluster, project, etc.).
    is_async: bool, True if the operation is in progress.
    details: str, Extra details appended to the message. Keep it succinct.
    failed: str, Failure message. For commands that operate on multiple
      resources and report all successes and failures before exiting. Failure
      messages use core_log.error. This will display the message on the standard
      error even when user output is disabled.
    operation_past_tense: str, The past tense version of the operation verb.
      If None assumes operation + 'd'
  """

  msg = []
  if failed:
    msg.append('Failed to ')
    msg.append(operation)
  elif is_async:
    msg.append(operation.capitalize())
    msg.append(' in progress for')
  else:
    verb = operation_past_tense or '{0}d'.format(operation)
    msg.append('{0}'.format(verb.capitalize()))

  if kind:
    msg.append(' {}'.format(kind))
  if resource:
    msg.append(' ')
    msg.append(text.TextTypes.RESOURCE_NAME(str(resource)))
  if details:
    msg.append(' ')
    msg.append(details)

  if failed:
    msg[-1] = '{0}: '.format(msg[-1])
    msg.append(failed)
  msg.append('.')
  msg = text.TypedText(msg)
  writer = core_log.error if failed else core_log.status.Print
  writer(msg)


def CreatedResource(resource, kind=None, is_async=False, details=None,
                    failed=None):
  """Prints a status message indicating that a resource was created.

  Args:
    resource: str, The resource name.
    kind: str, The resource kind (instance, cluster, project, etc.).
    is_async: bool, True if the operation is in progress.
    details: str, Extra details appended to the message. Keep it succinct.
    failed: str, Failure message.
  """
  _PrintResourceChange('create', resource, kind, is_async, details, failed)


def DeletedResource(resource, kind=None, is_async=False, details=None,
                    failed=None):
  """Prints a status message indicating that a resource was deleted.

  Args:
    resource: str, The resource name.
    kind: str, The resource kind (instance, cluster, project, etc.).
    is_async: bool, True if the operation is in progress.
    details: str, Extra details appended to the message. Keep it succinct.
    failed: str, Failure message.
  """
  _PrintResourceChange('delete', resource, kind, is_async, details, failed)


def RestoredResource(resource, kind=None, is_async=False, details=None,
                     failed=None):
  """Prints a status message indicating that a resource was restored.

  Args:
    resource: str, The resource name.
    kind: str, The resource kind (instance, cluster, project, etc.).
    is_async: bool, True if the operation is in progress.
    details: str, Extra details appended to the message. Keep it succinct.
    failed: str, Failure message.
  """
  _PrintResourceChange('restore', resource, kind, is_async, details, failed)


def UpdatedResource(resource, kind=None, is_async=False, details=None,
                    failed=None):
  """Prints a status message indicating that a resource was updated.

  Args:
    resource: str, The resource name.
    kind: str, The resource kind (instance, cluster, project, etc.).
    is_async: bool, True if the operation is in progress.
    details: str, Extra details appended to the message. Keep it succinct.
    failed: str, Failure message.
  """
  _PrintResourceChange('update', resource, kind, is_async, details, failed)


def ResetResource(resource, kind=None, is_async=False, details=None,
                  failed=None):
  """Prints a status message indicating that a resource was reset.

  Args:
    resource: str, The resource name.
    kind: str, The resource kind (instance, cluster, project, etc.).
    is_async: bool, True if the operation is in progress.
    details: str, Extra details appended to the message. Keep it succinct.
    failed: str, Failure message.
  """
  _PrintResourceChange('reset', resource, kind, is_async, details, failed,
                       operation_past_tense='reset')


# pylint: disable=invalid-name,protected-access
# Redirects to log copy of common logging functions. This way core.log and
# this module don't need to both be imported.
getLogger = core_log.getLogger
log = core_log.log
debug = core_log.debug
info = core_log.info
warning = core_log.warning
error = core_log.error
critical = core_log.critical
fatal = core_log.fatal
exception = core_log.exception
out = core_log._log_manager.stdout_writer
err = core_log._log_manager.stderr_writer
status = core_log.err
file_only_logger = core_log._log_manager.file_only_logger
