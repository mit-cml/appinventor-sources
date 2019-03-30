#!/usr/bin/env python
#
# Copyright 2007 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#




"""Bulkloader Transform Helper functions.

A collection of helper functions for bulkloading data, typically referenced
from a bulkloader.yaml file.
"""











import base64

import datetime
import os
import re
import sys
import tempfile

from google.appengine.api import datastore
from google.appengine.api import datastore_types
from google.appengine.ext.bulkload import bulkloader_errors


CURRENT_PROPERTY = None

KEY_TYPE_NAME = 'name'
KEY_TYPE_ID = 'ID'

# Decorators


def none_if_empty(fn):
  """A wrapper that returns None if its input is empty else fn(x).

  Useful on import.  Can be used in config files
  (e.g. "transform.none_if_empty(int)") or as a decorator.

  Args:
    fn: Single-argument transform function.

  Returns:
    The wrapped function.
  """

  def wrapper(value):


    if value == '' or value is None or value == []:
      return None
    return fn(value)

  return wrapper


def empty_if_none(fn):
  """A wrapper that returns '' if its input is None. Useful on export.

  Can be used in config files (e.g. "transform.empty_if_none(unicode)") or
  as a decorator.

  Args:
    fn: Single-argument transform function.

  Returns:
    The wrapped function.
  """

  def wrapper(value):

    if value is None:
      return ''
    return fn(value)

  return wrapper


# Key helpers.


def create_foreign_key(kind, key_is_id=False):
  """A method that makes single-level Key objects.

  These are typically used in ReferenceProperty in Python, where the reference
  value is a key with kind (or model) name.

  This helper method does not support keys with parents. Use create_deep_key
  instead to create keys with parents.

  Args:
    kind: The kind name of the reference as a string.
    key_is_id: If True, converts the key into an integer to be used as an ID.
        If False, leaves the key in the input format (typically a string).

  Returns:
    A single-argument function that parses a value into a Key of kind
    entity_kind.
  """

  def generate_foreign_key_lambda(value):
    if key_is_id:
      value = int(value)
    return datastore.Key.from_path(kind, value)

  return generate_foreign_key_lambda



def create_deep_key(*path_info):
  """A method that makes multi-level Key objects.

  Generates a multi-level key from multiple fields in the input dictionary.

  This is typically used for keys for entities that have variable parent keys,
  e.g. ones with owned relationships. It can used for both __key__ and
  references.

  Use create_foreign_key as a simpler way to create single-level keys.

  Args:
    *path_info: A list of tuples, describing (kind, property, is_id=False).
    kind: The kind name.
    property: The external property in the current import dictionary, or
        transform.CURRENT_PROPERTY for the value passed to the transform.
    is_id: If True, converts value to int and treats it as a numeric ID.
        If False, the value is a string name. Default is False.

        Example:
        create_deep_key(('rootkind', 'rootcolumn'),
                        ('childkind', 'childcolumn', True),
                        ('leafkind', transform.CURRENT_PROPERTY))

  Returns:
    A transform function that parses the info from the current neutral
    dictionary into a Key with parents as described by path_info.
  """

  validated_path_info = []
  for level_info in path_info:
    if len(level_info) == 3:
      key_is_id = level_info[2]
    elif len(level_info) == 2:
      key_is_id = False
    else:
      raise bulkloader_errors.InvalidConfiguration(
          'Each list in create_deep_key must specify exactly 2 or 3 '
          'parameters: (kind, property, is_id=False). You specified: %s' %
          repr(path_info))
    kind_name = level_info[0]
    property_name = level_info[1]
    validated_path_info.append((kind_name, property_name, key_is_id))


  def create_deep_key_lambda(value, bulkload_state):
    path = []
    for kind_name, property_name, key_is_id in validated_path_info:
      if property_name is CURRENT_PROPERTY:
        name_or_id = value
      else:
        name_or_id = bulkload_state.current_dictionary[property_name]

      if key_is_id:
        name_or_id = int(name_or_id)

      path += [kind_name, name_or_id]

    return datastore.Key.from_path(*path)

  return create_deep_key_lambda




def _key_id_or_name_n(key, index):
  """Internal helper function for key ID and name transforms.

  Args:
    key: A datastore key.
    index: The depth in the key to return, where 0 is the root key and -1 is the
        leaf key.

  Returns:
    The ID or name of the nth deep sub key in key.
  """
  if not key:
    return None
  path = key.to_path()
  if not path:
    return None
  path_index = (index * 2) + 1
  return path[path_index]


def key_id_or_name_as_string_n(index):
  """Retrieves the nth (0-based) key ID or name from a key that has parents.

  If a key is present, returns its ID or name as a string.

  Note that this loses the distinction between integer IDs and strings
  that happen to look like integers. Use key_type to distinguish them.

  This is a useful complement to create_deep_key.

  Args:
    index: The depth of the ID or name to extract, where 0 is the root key and
        -1 is the leaf key.

  Returns:
    A function that will extract the name or ID of the key at depth index, as a
    unicode string. The function returns '' if key is empty (unsaved), otherwise
    raises IndexError if the key is not as deep as described.
  """

  def transform_function(key):
    id_or_name = _key_id_or_name_n(key, index)
    if not id_or_name:
      return u''
    return unicode(id_or_name)

  return transform_function


# # Commonly used helper that returns the value of the leaf key.
key_id_or_name_as_string = key_id_or_name_as_string_n(-1)


def key_type_n(index):
  """Retrieves the nth (0-based) key type from a key that has parents.

  This is most useful when paired with key_id_or_name_as_string_n.
  This is a useful complement to create_deep_key.

  Args:
    index: The depth of the ID or name to extract, where 0 is the root key and
        -1 is the leaf key.

  Returns:
    A function that will return the type ('ID' or 'name') of the key at depth
    index. The function returns '' if key is empty (unsaved), otherwise raises
    IndexError if the key is not as deep as described.
  """

  def transform_function(key):
    id_or_name = _key_id_or_name_n(key, index)
    if id_or_name is None:
      return ''
    if isinstance(id_or_name, basestring):
      return KEY_TYPE_NAME
    return KEY_TYPE_ID

  return transform_function


# # Commonly used helper that returns the type of the leaf key.
key_type = key_type_n(-1)


def key_kind_n(index):
  """Retrieves the nth (0-based) key kind from a key that has parents.

  This is a useful complement to create_deep_key.

  Args:
    index: The depth of the ID or name to extract, where 0 is the root key and
      -1 is the leaf key.

  Returns:
    A function that will return the kind of the key at depth index or raise
    IndexError if the key is not as deep as described.
  """

  @empty_if_none
  def transform_function(key):
    path = key.to_path()
    path_index = (index * 2)
    return unicode(path[path_index])

  return transform_function


# Commonly used helper that returns the kind of the leaf key.
key_kind = key_kind_n(-1)

# Blob and ByteString helpers.


@none_if_empty
def blobproperty_from_base64(value):
  """Returns a datastore blob property containing the base64-decoded value."""
  decoded_value = base64.b64decode(value)
  return datastore_types.Blob(decoded_value)


@none_if_empty
def bytestring_from_base64(value):
  """Returns a datastore bytestring property from a base64-encoded value."""
  decoded_value = base64.b64decode(value)
  return datastore_types.ByteString(decoded_value)


def blob_to_file(filename_hint_propertyname=None, directory_hint=''):
  """Writes the blob contents to a file and replaces them with the filename.

  Args:
    filename_hint_propertyname: If present, the filename will begin with
      the contents of this value in the entity being exported.
    directory_hint: If present, the files will be stored in this directory.

  Returns:
    A function that writes the input blob to a file.
  """

  directory = []


  def transform_function(value, bulkload_state):
    if not directory:
      parent_dir = os.path.dirname(bulkload_state.filename)
      directory.append(os.path.join(parent_dir, directory_hint))
      if directory[0] and not os.path.exists(directory[0]):
        os.makedirs(directory[0])

    filename_hint = 'blob_'
    suffix = ''
    filename = ''
    if filename_hint_propertyname:
      filename_hint = bulkload_state.current_entity[filename_hint_propertyname]
      filename = os.path.join(directory[0], filename_hint)
      if os.path.exists(filename):
        filename = ''
        (filename_hint, suffix) = os.path.splitext(filename_hint)
    if not filename:
      filename = tempfile.mktemp(suffix, filename_hint, directory[0])
    f = open(filename, 'wb')
    f.write(value)
    f.close()
    return filename

  return transform_function


# Formatted string helpers: Extract, convert to boolean, date, or list.




def import_date_time(format, _strptime=None):
  """A wrapper around strptime that returns None if the input is empty.

  Args:
    format: A format string for strptime.

  Returns:
    A single-argument function that parses a string into a datetime using
    format.
  """



  if not _strptime:
    _strptime = datetime.datetime.strptime

  def import_date_time_lambda(value):
    if not value:
      return None
    return _strptime(value, format)

  return import_date_time_lambda





def export_date_time(format):
  """A wrapper around strftime that returns '' if the input is None.

  Args:
    format: A format string for strftime.

  Returns:
    A single-argument function that converts a datetime into a string using
    format.
  """

  def export_date_time_lambda(value):
    if not value:
      return ''
    return datetime.datetime.strftime(value, format)

  return export_date_time_lambda



def regexp_extract(pattern, method=re.match, group=1):
  """Returns the string that matches the specified group in the regex pattern.

  Args:
    pattern: A regular expression to match on with at least one group.
    method: The method to use for matching; normally re.match (the default) or
        re.search.
    group: The group to use for extracting a value; the first group by default.

  Returns:
    A single-argument function that returns the string that matches the
    specified group in the pattern, or None if no match was found or the input
    was empty.
  """

  def regexp_extract_lambda(value):
    if not value:
      return None
    matches = method(pattern, value)
    if not matches:
      return None
    return matches.group(group)

  return regexp_extract_lambda


def regexp_to_list(pattern):
  """Returns a list of objects that match a regex.

  Useful on import. Uses the provided regex to split a string value into a list
  of strings.  Wrapped by none_if_input_or_result_empty, so returns None if
  there are no matches for the regex, or if the input is empty.

  Args:
    pattern: A regular expression pattern to match against the input string.

  Returns:
    A function that returns None if the input was None or no matches were found,
    otherwise a list of strings matching the input expression.
  """

  @none_if_empty
  def regexp_to_list_lambda(value):
    result = re.findall(pattern, value)
    if not result:
      return None
    return result

  return regexp_to_list_lambda


def regexp_bool(regexp, flags=0):
  """Returns a boolean indicating whether the expression matches with re.match.

  Note that re.match anchors at the start but not end of the string.

  Args:
    regexp: String, regular expression.
    flags: Optional flags to pass to re.match.

  Returns:
    A function that returns True if the expression matches.
  """

  def transform_function(value):
    return bool(re.match(regexp, value, flags))

  return transform_function



def fix_param_typo(oops, fixed):
  """A decorator that corrects a misspelled parameter name.

  A parameter in the split_string() and join_list() functions was originally
  misspelled 'delimeter' instead of 'delimiter'. We couldn't correct the error
  by simply renaming it, because that would break any client code that named
  the parameter when invoking either function:

  # This is fine: split strings on semi-colons.
  split_string(';')

  # This would break unless the client code also changed delimeter to delimiter.
  split_string(delimeter=';')

  But spelling counts, even in code, so here we are.

  Args:
    oops: The misspelled parameter name.
    fixed: The correctly spelled parameter name, which matches the name in the
      definition of the decorated function.

  Returns:
    A function that calls the decorated function correctly when it is invoked
    with a misspelled parameter.
  """

  def _wrapped(fn):
    """A wrapper that will correct a misspelled parameter name."""

    def _process_args(*args, **kwargs):
      """Calls the decorated function with the correct parameter."""
      if len(args) + len(kwargs) != 1:
        raise ValueError('Please supply exactly 1 argument.')
      if fixed in kwargs:
        return fn(kwargs[fixed])
      if oops in kwargs:
        print >> sys.stderr, (
            'The parameter "%s" is deprecated. Please use "%s" instead.') % (
                oops, fixed)
        return fn(kwargs[oops])
      return fn(args[0])

    return _process_args

  return _wrapped


@fix_param_typo('delimeter', 'delimiter')
def split_string(delimiter):
  """Splits a string into a list using the delimiter.

  This is just a wrapper for string.split.

  Args:
    delimiter: The delimiter to split the string on.

  Returns:
    A function that splits the string into a list along the delimiter.
  """

  def split_string_lambda(value):
    return value.split(delimiter)

  return split_string_lambda


@fix_param_typo('delimeter', 'delimiter')
def join_list(delimiter):
  """Joins a list into a string using the delimiter.

  This is just a wrapper for string.join.

  Args:
    delimiter: The delimiter to use when joining the string.

  Returns:
    A function that joins the list into a string with the delimiter.
  """

  def join_string_lambda(value):
    return delimiter.join(value)

  return join_string_lambda



def list_from_multiproperty(*external_names):
  """Creates a list from multiple properties.

  Args:
    *external_names: A list of properties to use.

  Returns:
    A function that returns a list of the properties in external_names.
  """

  def list_from_multiproperty_lambda(unused_value, bulkload_state):
    result = []
    for external_name in external_names:
      value = bulkload_state.current_dictionary.get(external_name)
      if value:
        result.append(value)
    return result

  return list_from_multiproperty_lambda


def property_from_list(index):
  """Returns the item at position 'index' from a list.

  Args:
    index: The (0-based) item in the list to return.

  Returns:
    A function that returns the specified item from a list, or '' if the list
    contains too few items.
  """

  @empty_if_none
  def property_from_list_lambda(values):
    if len(values) > index:
      return values[index]
    return ''

  return property_from_list_lambda


# SimpleXML list Helpers


def list_from_child_node(xpath, suppress_blank=False):
  """Returns a list property from child nodes of the current xml node.

  This applies only the simplexml helper, as it assumes __node__, the current
  ElementTree node corresponding to the import record.

  Sample usage for structure:
   <Visit>
    <VisitActivities>
     <Activity>A1</Activity>
     <Activity>A2</Activity>
    </VisitActivities>
   </Visit>

  property: activities
  external_name: VisitActivities # Ignored on import, used on export.
  import_transform: list_from_xml_node('VisitActivities/Activity')
  export_transform: child_node_from_list('Activity')

  Args:
    xpath: XPath to run on the current node.
    suppress_blank: if True, nodes with no text will be skipped.

  Returns:
    A function that works as described in the args.
  """

  def list_from_child_node_lambda(unused_value, bulkload_state):
    result = []
    for node in bulkload_state.current_dictionary['__node__'].findall(xpath):
      if node.text:
        result.append(node.text)
      elif not suppress_blank:
        result.append('')
    return result

  return list_from_child_node_lambda


def child_node_from_list(child_node_name):
  """Returns a value suitable for generating an XML child node on export.

  The return value is a list of tuples that the simplexml connector will
  use to build a child node.

  See also list_from_child_node

  Args:
    child_node_name: The name to use for each child node.

  Returns:
    A function that works as described in the args.
  """

  def child_node_from_list_lambda(values):
    return [(child_node_name, value) for value in values]

  return child_node_from_list_lambda
