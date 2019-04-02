# -*- coding: utf-8 -*-
# Copyright 2018 Google Inc. All Rights Reserved.
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
"""Shared utility structures and methods for manipulating text."""

from __future__ import absolute_import
from __future__ import print_function

import re
import urlparse

from gslib.exception import CommandException
from gslib.lazy_wrapper import LazyWrapper
from gslib.utils.constants import UTF8
from gslib.utils.constants import WINDOWS_1252
from gslib.utils.system_util import IS_CP1252

STORAGE_CLASS_SHORTHAND_TO_FULL_NAME = {
    # Values should remain uppercase, as required by non-gs providers.
    'CL': 'COLDLINE',
    'DRA': 'DURABLE_REDUCED_AVAILABILITY',
    'NL': 'NEARLINE',
    'S': 'STANDARD',
    'STD': 'STANDARD'}

VERSION_MATCHER = LazyWrapper(
    lambda: re.compile(r'^(?P<maj>\d+)(\.(?P<min>\d+)(?P<suffix>.*))?'))


def AddQueryParamToUrl(url_str, param_name, param_value):
  """Adds a query parameter to a URL string.

  Appends a query parameter to the query string portion of a url. If a parameter
  with the given name was already present, it is not removed; the new name/value
  pair will be appended to the end of the query string. It is assumed that all
  arguments will be of type `str` (either ASCII or UTF-8 encoded) or `unicode`.

  Note that this method performs no URL-encoding. It is the caller's
  responsibility to ensure proper URL encoding of the entire URL; i.e. if the
  URL is already URL-encoded, you should pass in URL-encoded values for
  param_name and param_value. If the URL is not URL-encoded, you should not pass
  in URL-encoded parameters; instead, you could perform URL-encoding using the
  URL string returned from this function.

  Args:
    url_str: (str or unicode) String representing the URL.
    param_name: (str or unicode) String key of the query parameter.
    param_value: (str or unicode) String value of the query parameter.

  Returns:
    (str or unicode) A string representing the modified url, of type `unicode`
    if the url_str argument was a `unicode`, otherwise a `str` encoded in UTF-8.
  """
  url_was_unicode = isinstance(url_str, unicode)
  if isinstance(url_str, unicode):
    url_str = url_str.encode('utf-8')
  if isinstance(param_name, unicode):
    param_name = param_name.encode('utf-8')
  if isinstance(param_value, unicode):
    param_value = param_value.encode('utf-8')
  scheme, netloc, path, query_str, fragment = urlparse.urlsplit(url_str)

  query_params = urlparse.parse_qsl(query_str, keep_blank_values=True)
  query_params.append((param_name, param_value))
  new_query_str = '&'.join(['%s=%s' % (k, v) for (k, v) in query_params])

  new_url = urlparse.urlunsplit((scheme, netloc, path, new_query_str, fragment))
  if url_was_unicode:
    new_url = new_url.decode('utf-8')
  return new_url


def CompareVersions(first, second):
  """Compares the first and second gsutil version strings.

  For example, 3.33 > 3.7, and 4.1 is a greater major version than 3.33.
  Does not handle multiple periods (e.g. 3.3.4) or complicated suffixes
  (e.g., 3.3RC4 vs. 3.3RC5). A version string with a suffix is treated as
  less than its non-suffix counterpart (e.g. 3.32 > 3.32pre).

  Args:
    first: First gsutil version string.
    second: Second gsutil version string.

  Returns:
    (g, m):
       g is True if first known to be greater than second, else False.
       m is True if first known to be greater by at least 1 major version,
         else False.
  """
  m1 = VERSION_MATCHER().match(str(first))
  m2 = VERSION_MATCHER().match(str(second))

  # If passed strings we don't know how to handle, be conservative.
  if not m1 or not m2:
    return (False, False)

  major_ver1 = int(m1.group('maj'))
  minor_ver1 = int(m1.group('min')) if m1.group('min') else 0
  suffix_ver1 = m1.group('suffix')
  major_ver2 = int(m2.group('maj'))
  minor_ver2 = int(m2.group('min')) if m2.group('min') else 0
  suffix_ver2 = m2.group('suffix')

  if major_ver1 > major_ver2:
    return (True, True)
  elif major_ver1 == major_ver2:
    if minor_ver1 > minor_ver2:
      return (True, False)
    elif minor_ver1 == minor_ver2:
      return (bool(suffix_ver2) and not suffix_ver1, False)
  return (False, False)


def ConvertRecursiveToFlatWildcard(url_strs):
  """A generator that adds '**' to each url string in url_strs."""
  for url_str in url_strs:
    yield '%s**' % url_str


def DecodeLongAsString(long_to_convert):
  """Decodes an encoded python long into an ASCII string.

  This is used for modeling S3 version_id's as apitools generation.

  Args:
    long_to_convert: long to convert to ASCII string. If this is already a
                     string, it is simply returned.

  Returns:
    String decoded from the input long.
  """
  if isinstance(long_to_convert, basestring):
    # Already converted.
    return long_to_convert
  return hex(long_to_convert)[2:-1].decode('hex')


def EncodeStringAsLong(string_to_convert):
  """Encodes an ASCII string as a python long.

  This is used for modeling S3 version_id's as apitools generation.  Because
  python longs can be arbitrarily large, this works.

  Args:
    string_to_convert: ASCII string to convert to a long.

  Returns:
    Long that represents the input string.
  """
  return long(string_to_convert.encode('hex'), 16)


def FixWindowsEncodingIfNeeded(input_str):
  """Attempts to detect Windows CP1252 encoding and convert to UTF8.

  Windows doesn't provide a way to set UTF-8 for string encodings; you can set
  the system locale (see
  http://windows.microsoft.com/en-us/windows/change-system-locale#1TC=windows-7)
  but that takes you to a "Change system locale" dropdown that just lists
  languages (e.g., "English (United States)". Instead, we're forced to check if
  a encoding as UTF8 raises an exception and if so, try converting from CP1252
  to Unicode.

  Args:
    input_str: (str) The input string.
  Returns:
    (str) The converted string (or the original, if conversion wasn't needed).
  """
  if IS_CP1252:
    return input_str.decode(WINDOWS_1252).encode(UTF8)
  else:
    return input_str


def GetPrintableExceptionString(exc):
  """Returns a short Unicode string describing the exception."""
  return unicode(exc).encode(UTF8) or unicode(exc.__class__)


def InsistAscii(string, message):
  if not all(ord(c) < 128 for c in string):
    raise CommandException(message)


def InsistAsciiHeader(header):
  InsistAscii(header, 'Invalid non-ASCII header (%s).' % header)


def InsistAsciiHeaderValue(header, value):
  InsistAscii(
      value,
      'Invalid non-ASCII value (%s) was provided for header %s.\nOnly ASCII '
      'characters are allowed in headers other than x-goog-meta- and '
      'x-amz-meta- headers' % (value, header))


def InsistOnOrOff(value, message):
  if value != 'on' and value != 'off':
    raise CommandException(message)


def NormalizeStorageClass(sc):
  """Returns a normalized form of the given storage class name.

  Converts the given string to uppercase and expands valid abbreviations to
  full storage class names (e.g 'std' would return 'STANDARD'). Note that this
  method does not check if the given storage class is valid.

  Args:
    sc: (str) String representing the storage class's full name or abbreviation.

  Returns:
    (str) A string representing the full name of the given storage class.
  """
  # Use uppercase; storage class argument for the S3 API must be uppercase,
  # and it's case-insensitive for GS APIs.
  sc = sc.upper()
  if sc in STORAGE_CLASS_SHORTHAND_TO_FULL_NAME:
    sc = STORAGE_CLASS_SHORTHAND_TO_FULL_NAME[sc]
  return sc


def PrintableStr(input_val):
  """Return an UTF8-encoded string type, or None if `input_val` is None.

  Args:
    input_val: (unicode, str, or None) A string-like object or None. This method
        simply calls encode() on `input_val` if it is not None; if `input_val`
        is not of type "unicode", this will implicitly call decode() with the
        default encoding for strings (for Python 2, this is ASCII), then call
        encode().

  Returns:
    (str) A UTF-8 encoded string, or None.
  """
  return input_val.encode(UTF8) if input_val is not None else None


def RemoveCRLFFromString(input_str):
  r"""Returns the input string with all \n and \r removed."""
  return re.sub(r'[\r\n]', '', input_str)
