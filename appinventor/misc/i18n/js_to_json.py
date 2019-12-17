#!/usr/bin/python

# Gives the translation status of the specified apps and languages.
#
# Copyright 2013 Google Inc.
# https://developers.google.com/blockly/
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

"""Extracts messages from .js files into .json files for translation.

Specifically, lines with the following formats are extracted:

    /// Here is a description of the following message.
    Blockly.SOME_KEY = 'Some value';

Adjacent "///" lines are concatenated.

There are two output files, each of which is proper JSON.  For each key, the
file en.json would get an entry of the form:

    "Blockly.SOME_KEY", "Some value",

The file qqq.json would get:

    "Blockly.SOME_KEY", "Here is a description of the following message.",

Commas would of course be omitted for the final entry of each value.

@author Ellen Spertus (ellen.spertus@gmail.com)
"""

import argparse
import codecs
import json
import os
import re
from common import write_files

linere = re.compile(r"(Blockly\.Msg\.[A-Z0-9_]+)\s*=\s*?[\"\'\[](.*)[\"\'\]];")
continuation = re.compile(r'\s*\+?\s*(?:\"|\')?(.*)?(?:\"|\')\s*\+?')

def main():
  # Set up argument parser.
  parser = argparse.ArgumentParser(description='Create translation files.')
  parser.add_argument(
      '--author',
      default='Ellen Spertus <ellen.spertus@gmail.com>',
      help='name and email address of contact for translators')
  parser.add_argument('--lang', default='en',
                      help='ISO 639-1 source language code')
  parser.add_argument('--output_dir', default='json',
                      help='relative directory for output files')
  parser.add_argument('--input_file', default='messages.js',
                      help='input file')
  parser.add_argument('--quiet', action='store_true', default=False,
                      help='only display warnings, not routine info')
  args = parser.parse_args()
  if (not args.output_dir.endswith(os.path.sep)):
    args.output_dir += os.path.sep

  # Read and parse input file.
  results = []
  synonyms = {}
  infile = codecs.open(args.input_file, 'r', 'utf-8')
  comment = None
  full_line = ''
  is_block_comment = False
  is_line_continuation = False
  for line in infile:
    line = line.strip()
    if line == '':
      continue
    if is_block_comment:
      full_line += ' ' + line
      if line.endswith(r'*/'):
        comment = full_line
        is_block_comment = False
        full_line = ''
      continue
    if line.startswith(r'/*') and line.endswith(r'*/'):
      comment = line[2:][:-2].lstrip()
      continue
    if line.startswith(r'/*') and line.find('*/') > -1:  # Handle block comment that is only part of a line
      line_partial_comment = line.split('*/', 1)
      comment = line_partial_comment[0]
      line = line_partial_comment[1].strip()
    if line.startswith(r'//'):
      comment = line[2:].lstrip()
      continue
    if line.startswith(r'/*'):
      full_line = line
      is_block_comment = True
      continue
    if line.endswith('{'):
      full_line = ''
      continue
    if line.startswith('+'):
      line = line[3:]
      full_line = full_line[:-1]
    if line.endswith('+'):
      line = continuation.match(line).group(1).strip()
      is_line_continuation = True
    elif is_line_continuation:
      line = line[1:]
      is_line_continuation = False
    if line.find('_HELPURL') > -1:  # HELPURL strings are not currently translated
      continue
    full_line += line
    if full_line.endswith(';'):
      is_line_continuation = False
      match = linere.match(full_line)
      if match is not None:
        result = {}
        result['meaning'] = match.group(1).replace("\\\"", "'").replace("\\'", "'").replace("\(", "(").replace("\)", ")")
        result['source'] = match.group(2).replace("\\\"", "'").replace("\\'", "'").replace("\(", "(").replace("\)", ")")
        if not comment:
          comment = 'No description for ' + result['meaning']
        result['description'] = comment.replace("\\\"", "'").replace("\\'", "'").replace("\(", "(").replace("\)", ")")
        comment = None
        results.append(result)
      full_line = ''
  infile.close()

  # Create <lang_file>.json, keys.json, and qqq.json.
  write_files(args.author, args.lang, args.output_dir, results, False)

  # Create synonyms.json.
  synonym_file_name = os.path.join(os.curdir, args.output_dir, 'synonyms.json')
  with open(synonym_file_name, 'w') as outfile:
    json.dump(synonyms, outfile)
  if not args.quiet:
    print("Wrote {0} synonym pairs to {1}.".format(
        len(synonyms), synonym_file_name))


if __name__ == '__main__':
  main()
