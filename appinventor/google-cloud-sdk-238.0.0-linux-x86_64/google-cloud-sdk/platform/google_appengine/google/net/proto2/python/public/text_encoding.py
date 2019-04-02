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
"""Encoding related utilities."""
import re

from google.appengine._internal import six

_cescape_chr_to_symbol_map = {}
_cescape_chr_to_symbol_map[9] = r'\t'
_cescape_chr_to_symbol_map[10] = r'\n'
_cescape_chr_to_symbol_map[13] = r'\r'
_cescape_chr_to_symbol_map[34] = r'\"'
_cescape_chr_to_symbol_map[39] = r"\'"
_cescape_chr_to_symbol_map[92] = r'\\'


_cescape_unicode_to_str = [chr(i) for i in range(0, 256)]
for byte, string in _cescape_chr_to_symbol_map.items():
  _cescape_unicode_to_str[byte] = string


_cescape_byte_to_str = ([r'\%03o' % i for i in range(0, 32)] +
                        [chr(i) for i in range(32, 127)] +
                        [r'\%03o' % i for i in range(127, 256)])
for byte, string in _cescape_chr_to_symbol_map.items():
  _cescape_byte_to_str[byte] = string
del byte, string


def CEscape(text, as_utf8):

  """Escape a bytes string for use in an text protocol buffer.

  Args:
    text: A byte string to be escaped.
    as_utf8: Specifies if result may contain non-ASCII characters.
        In Python 3 this allows unescaped non-ASCII Unicode characters.
        In Python 2 the return value will be valid UTF-8 rather than only ASCII.
  Returns:
    Escaped string (str).
  """





  if six.PY3:
    text_is_unicode = isinstance(text, str)
    if as_utf8 and text_is_unicode:

      return text.translate(_cescape_chr_to_symbol_map)
    ord_ = ord if text_is_unicode else lambda x: x
  else:
    ord_ = ord
  if as_utf8:
    return ''.join(_cescape_unicode_to_str[ord_(c)] for c in text)
  return ''.join(_cescape_byte_to_str[ord_(c)] for c in text)


_CUNESCAPE_HEX = re.compile(r'(\\+)x([0-9a-fA-F])(?![0-9a-fA-F])')


def CUnescape(text):

  """Unescape a text string with C-style escape sequences to UTF-8 bytes.

  Args:
    text: The data to parse in a str.
  Returns:
    A byte string.
  """

  def ReplaceHex(m):


    if len(m.group(1)) & 1:
      return m.group(1) + 'x0' + m.group(2)
    return m.group(0)



  result = _CUNESCAPE_HEX.sub(ReplaceHex, text)

  if six.PY2:
    return result.decode('string_escape')
  return (result.encode('utf-8')
          .decode('unicode_escape')

          .encode('raw_unicode_escape'))
