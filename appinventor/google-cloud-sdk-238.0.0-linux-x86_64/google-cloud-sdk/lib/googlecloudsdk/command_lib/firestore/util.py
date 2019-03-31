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
"""Utilities for Cloud Firestore commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from apitools.base.py import encoding
from googlecloudsdk.api_lib.util import apis
from googlecloudsdk.calliope import exceptions
from googlecloudsdk.core import log
from googlecloudsdk.core.util import text


# All commands that manage indexes use this API (since v1/v1beta1 don't have
# index management functionality).
FIRESTORE_INDEX_API_VERSION = 'v1beta2'


def GetMessagesModule():
  return apis.GetMessagesModule('firestore', FIRESTORE_INDEX_API_VERSION)


def GetDatabaseFallthrough():
  """Python hook to get the value for the default database.

  Firestore currently only supports one database called '(default)'.

  Returns:
    The name of the default database.
  """
  return '(default)'


def GetCollectionGroupFallthrough():
  """Python hook to get the value for the '-' collection group.

  See details at:

  https://cloud.google.com/apis/design/design_patterns#list_sub-collections

  This allows us to describe or delete an index by specifying just its ID,
  without needing to know which collection group it belongs to.

  Returns:
    The value of the wildcard collection group.
  """
  return '-'


def GetDefaultFieldCollectionGroupFallthrough():
  return '__default__'


def GetDefaultFieldPathFallthrough():
  return '*'


def ValidateFieldConfig(unused_ref, args, request):
  """Python hook to validate the field configuration of the given request.

  Note that this hook is only called after the request has been formed based on
  the spec. Thus, the validation of the user's choices for order and
  array-config, as well as the check for the required field-path attribute, have
  already been performed. As such the only remaining things to verify are that
  the user has specified at least 2 fields, and that exactly one of order or
  array-config was specified for each field.

  Args:
    unused_ref: The resource ref (unused).
    args: The parsed arg namespace.
    request: The request formed based on the spec.
  Returns:
    The original request assuming the field configuration is valid.
  Raises:
    InvalidArgumentException: If the field configuration is invalid.
  """
  if len(args.field_config) < 2:
    raise exceptions.InvalidArgumentException(
        '--field-config',
        'Composite indexes must be configured with at least 2 fields. For '
        'single-field index management, use the commands under `gcloud '
        'firestore indexes fields`.')

  invalid_field_configs = []
  for field_config in args.field_config:
    # Because of the way declarative ArgDict parsing works, the type of
    # field_config here is already an apitools message, as opposed to an
    # ArgDict.
    order = field_config.order
    array_config = field_config.arrayConfig
    if (order and array_config) or (not order and not array_config):
      invalid_field_configs.append(field_config)

  if invalid_field_configs:
    raise exceptions.InvalidArgumentException(
        '--field-config',
        "Exactly one of 'order' or 'array-config' must be specified for the "
        "{field_word} with the following {path_word}: [{paths}].".format(
            field_word=text.Pluralize(len(invalid_field_configs), 'field'),
            path_word=text.Pluralize(len(invalid_field_configs), 'path'),
            paths=', '.join(field_config.fieldPath
                            for field_config in invalid_field_configs)))

  return request


def ExtractOperationMetadata(response, unused_args):
  """Python hook to extract the operation metadata message.

  This is needed because apitools gives us a MetadataValue message for the
  operation metadata field, instead of the actual message that we want.

  Args:
    response: The response field in the operation returned by the API.
    unused_args: The parsed arg namespace (unused).
  Returns:
    The metadata field converted to a
    GoogleFirestoreAdminV1beta2IndexOperationMetadata message.
  """
  messages = GetMessagesModule()
  return encoding.DictToMessage(
      encoding.MessageToDict(response.metadata),
      messages.GoogleFirestoreAdminV1beta2IndexOperationMetadata)


def LogCreatedIndex(response, args):
  """Python hook to log the creation of the index to stderr.

  The declarative framework doesn't log creation of resources if
  is_parent_resource=True in the spec, so we have to do it manually here.

  Args:
    response: GoogleFirestoreAdminV1beta2IndexOperationMetadata message
    args: The parsed arg namespace.
  Returns:
    The same response that was passed in.
  """
  if not args.async:
    log.CreatedResource(response.index.split('/')[-1], kind='index')
  return response


def ValidateFieldArg(ref, unused_args, request):
  """Python hook to validate that the field reference is correctly specified.

  The user should be able to describe database-wide settings as well as
  collection-group wide settings; however it doesn't make sense to describe a
  particular field path's settings unless the collection group was also
  specified. The API will catch this but it's better to do it here for a clearer
  error message.

  Args:
    ref: The field resource reference.
    unused_args: The parsed arg namespace (unused).
    request: The field describe request.
  Returns:
    The original request assuming the field configuration is valid.
  Raises:
    InvalidArgumentException: If the field resource is invalid.
  """
  if (ref.fieldsId != GetDefaultFieldPathFallthrough() and
      ref.collectionGroupsId == GetDefaultFieldCollectionGroupFallthrough()):
    raise exceptions.InvalidArgumentException(
        'FIELD',
        'Collection group must be provided if the field path was specified.')
  return request


def CreateIndexMessage(messages, index):
  """Creates a message for the given index.

  Args:
    messages: The Cloud Firestore messages module.
    index: The index ArgDict.
  Returns:
    GoogleFirestoreAdminV1beta2Index
  """
  # Currently all indexes are COLLECTION-scoped
  query_scope = (messages.GoogleFirestoreAdminV1beta2Index.
                 QueryScopeValueValuesEnum.COLLECTION)
  # Since this is a single-field index there will only be 1 field
  index_fields = [messages.GoogleFirestoreAdminV1beta2IndexField(
      arrayConfig=index.get('array-config'),
      order=index.get('order'))]

  return messages.GoogleFirestoreAdminV1beta2Index(
      queryScope=query_scope,
      fields=index_fields)


def ValidateFieldIndexArgs(args):
  """Validates the repeated --index arg.

  Args:
    args: The parsed arg namespace.
  Raises:
    InvalidArgumentException: If the provided indexes are incorrectly specified.
  """
  if not args.IsSpecified('index'):
    return

  for index in args.index:
    order = index.get('order')
    array_config = index.get('array-config')
    if (order and array_config) or (not order and not array_config):
      raise exceptions.InvalidArgumentException(
          '--index',
          "Exactly one of 'order' or 'array-config' must be specified "
          "for each --index flag provided.")


def CreateFieldUpdateRequest(ref, args):
  """Python hook to create the field update request.

  The mapping of index config message to API behavior is as follows:
    None          - Clears the exemption
    indexes=[]    - Disables all indexes
    indexes=[...] - Sets the index config to the indexes provided

  Args:
    ref: The field resource reference.
    args: The parsed arg namespace.
  Returns:
    FirestoreProjectsDatabasesCollectionGroupsFieldsPatchRequest
  """
  ValidateFieldIndexArgs(args)

  messages = GetMessagesModule()
  index_config = None
  if args.disable_indexes:
    index_config = messages.GoogleFirestoreAdminV1beta2IndexConfig(indexes=[])
  elif args.IsSpecified('index'):
    # args.index is a repeated argument
    index_config = messages.GoogleFirestoreAdminV1beta2IndexConfig(
        indexes=[CreateIndexMessage(messages, index) for index in args.index])

  field = messages.GoogleFirestoreAdminV1beta2Field(
      name=ref.RelativeName(),
      indexConfig=index_config)

  request = (
      messages.FirestoreProjectsDatabasesCollectionGroupsFieldsPatchRequest(
          name=ref.RelativeName(),
          updateMask='indexConfig',
          googleFirestoreAdminV1beta2Field=field)
  )

  return request
