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
"""Utility methods related to Unicode."""


def _Unicode32(s):
  """Tells whether a string contains 32-bit Unicode characters.

  Args:
    s: a string, possibly of unicode type.
  Returns:
    True if there are 32-bit characters, False otherwise.
  """
  if isinstance(s, unicode):
    return any(ord(ch) >= 0x10000 for ch in s)
  else:
    return False


def _SplitUnicode(s):
  """Generator function to limit characters to UTF-16.

  Converts all characters in the Supplementary Planes
  (> 64K) to surrogate pairs. Leaves lower codepoints
  unchanged.

  See https://wikipedia.org/wiki/UTF-16#U.2B10000_to_U.2B10FFFF

  Args:
    s: a unicode string, possibly containing 32-bit characters

  Yields:
    Characters of the translated string.
  """
  for ch in s:
    if ord(ch) < 0x10000:
      yield ch
    else:
      twentybit = ord(ch) - 0x10000
      yield unichr(0xD800 + (twentybit >> 10))
      yield unichr(0xDC00 + (twentybit & 0x3FF))


def LimitUnicode(s):
  """Replaces 32-bit Unicode characters with surrogate pairs.

  Returns a version of the string argument with all Unicode characters
  above 0xFFFF (those from the Supplementary Plane) replaced with the
  appropriate surrogate pairs. If there are no such characters,
  returns the same string instance.

  See https://wikipedia.org/wiki/UTF-16#U.2B10000_to_U.2B10FFFF

  Args:
    s: a string, possibly of unicode type, to be converted
    if necessary.
  Returns:
    Unicode string with surrogate pairs, or the argument
    unmodified.
  """
  if _Unicode32(s):
    return u''.join(_SplitUnicode(s))
  else:
    return s
