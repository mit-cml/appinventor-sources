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

"""Cloud SDK markdown document linter renderer."""
from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import io

from googlecloudsdk.core.document_renderers import text_renderer


class LinterRenderer(text_renderer.TextRenderer):
  """Renders markdown to a list of lines where there is a linter error."""

  _HEADINGS_TO_LINT = ['NAME', 'EXAMPLES', 'DESCRIPTION']
  _NAME_WORD_LIMIT = 20
  _PERSONAL_PRONOUNS = [' me ', ' we ', ' I ', ' us ', ' he ', ' she ', ' him ',
                        ' her ', ' them ', ' they ']

  def __init__(self, *args, **kwargs):
    super(LinterRenderer, self).__init__(*args, **kwargs)
    self._file_out = self._out  # the output file inherited from TextRenderer
    self._null_out = io.StringIO()
    self._buffer = io.StringIO()
    self._out = self._buffer
    self._analyze = {'NAME': self._analyze_name,
                     'EXAMPLES': self._analyze_examples,
                     'DESCRIPTION': self._analyze_description}
    self._heading = ''
    self._prev_heading = ''
    self.example = False
    self.command_name = ''
    self.name_section = ''
    self.command_name_length = 0
    self.command_text = ''
    self.equals_violation_flags = []
    self.nonexistent_violation_flags = []

  def _CaptureOutput(self, heading):
    # check if buffer is full from previous heading
    if self._buffer.getvalue() and self._prev_heading:
      self._Analyze(self._prev_heading, self._buffer.getvalue())
      # refresh the StringIO()
      self._buffer = io.StringIO()
    self._out = self._buffer
    # save heading so can get it in next section
    self._prev_heading = self._heading

  def _DiscardOutput(self, heading):
    self._out = self._null_out

  def _Analyze(self, heading, section):
    self._analyze[heading](section)

  def check_for_personal_pronouns(self, section):
    warnings = []
    for pronoun in self._PERSONAL_PRONOUNS:
      if pronoun in section:
        warnings = ['# ' + self._heading + '_PRONOUN_CHECK FAILED'
                    '\nPlease remove personal pronouns in the '
                    + self._heading + ' section.']
        break
    return warnings

  def Finish(self):
    if self._buffer.getvalue() and self._prev_heading:
      self._Analyze(self._prev_heading, self._buffer.getvalue())
    self._buffer.close()
    self._null_out.close()
    if (self.command_metadata and not self.command_metadata.is_group and
        not self.example):
      self._file_out.write('Refer to the detailed style guide: '
                           'go/cloud-sdk-help-guide#examples\nThis is the '
                           'analysis for EXAMPLES:\n# EXAMPLE_PRESENT_CHECK '
                           'FAILED\nYou have not included an '
                           'example in the Examples section.\n\n')

  def Heading(self, level, heading):
    self._heading = heading
    if heading in self._HEADINGS_TO_LINT:
      self._CaptureOutput(heading)
    else:
      self._DiscardOutput(heading)

  def Example(self, line):
    # ensure this example is in the EXAMPLES section and it is not a group level
    # command
    if (self.command_metadata and not self.command_metadata.is_group and
        self._heading == 'EXAMPLES'):
      # if previous line ended in a backslash, it is not the last line of the
      # command so append new line of command to command_text
      if self.command_text and self.command_text.endswith('\\'):
        self.command_text += line.strip()
      # This is the first line of the command and ignore the `$ ` in it.
      else:
        self.command_text = line.replace('$ ', '')
      # if the current line doesn't end with a `\`, it is the end of the command
      # so self.command_text is the whole command
      if not line.endswith('\\'):
        # check that the example starts with the command of the help text
        if self.command_text.startswith(self.command_name):
          self.example = True
          self._file_out.write('# EXAMPLE_PRESENT_CHECK SUCCESS\n')
          rest_of_command = self.command_text[self.command_name_length:].split()
          flag_names = []
          for word in rest_of_command:
            word = word.replace('\\--', '--')
            if word.startswith('--'):
              flag_names.append(word)
          self._analyze_example_flags_equals(flag_names)
          flags = [flag.partition('=')[0] for flag in flag_names]
          if self.command_metadata and self.command_metadata.flags:
            self._check_valid_flags(flags)

  def _check_valid_flags(self, flags):
    for flag in flags:
      if flag not in self.command_metadata.flags:
        self.nonexistent_violation_flags.append(flag)

  def _analyze_example_flags_equals(self, flags):
    for flag in flags:
      if '=' not in flag and flag not in self.command_metadata.bool_flags:
        self.equals_violation_flags.append(flag)

  def _analyze_name(self, section):
    successful_linters = []
    warnings = self.check_for_personal_pronouns(section)
    if not warnings:
      successful_linters.append('# NAME_PRONOUN_CHECK SUCCESS')
    self.command_name = section.strip().split(' -')[0]
    if len(section.strip().split(' - ')) == 1:
      self.name_section = ''
      warnings.append('# NAME_DESCRIPTION_CHECK FAILED')
      warnings.append('Please add an explanation for the command.')
    else:
      self.name_section = section.strip().split(' -')[1]
      successful_linters.append('# NAME_DESCRIPTION_CHECK SUCCESS')
    self.command_name_length = len(self.command_name)
    # check that name section is not too long
    if len(self.name_section.split()) > self._NAME_WORD_LIMIT:
      warnings.append('# NAME_LENGTH_CHECK FAILED')
      warnings.append('Please shorten the name section description to less '
                      'than ' + str(self._NAME_WORD_LIMIT) + ' words.')
    else:
      successful_linters.append('# NAME_LENGTH_CHECK SUCCESS')
    if successful_linters:
      self._file_out.write('\n'.join(successful_linters))
      self._file_out.write('\n')
    if warnings:
      # TODO(b/119550825): remove the go/ link from open source code
      self._file_out.write('Refer to the detailed style guide: '
                           'go/cloud-sdk-help-guide#name\nThis is the '
                           'analysis for NAME:\n')
      self._file_out.write('\n'.join(warnings))
      self._file_out.write('\n\n')
    else:
      self._file_out.write('There are no errors for the NAME section.\n\n')

  def _analyze_examples(self, section):
    successful_linters = []
    if not self.command_metadata.is_group:
      warnings = self.check_for_personal_pronouns(section)
      if not warnings:
        successful_linters.append('# EXAMPLES_PRONOUN_CHECK SUCCESS')
      if self.equals_violation_flags:
        warnings.append('# EXAMPLE_FLAG_EQUALS_CHECK FAILED')
        warnings.append('There should be an `=` between the flag name and the '
                        'value.')
        warnings.append('The following flags are not formatted properly:')
        for flag in self.equals_violation_flags:
          warnings.append(flag)
      else:
        successful_linters.append('# EXAMPLE_FLAG_EQUALS_CHECK SUCCESS')
      if self.nonexistent_violation_flags:
        warnings.append('# EXAMPLE_NONEXISTENT_FLAG_CHECK FAILED')
        warnings.append('The following flags are not valid for the command:')
        for flag in self.nonexistent_violation_flags:
          warnings.append(flag)
      else:
        successful_linters.append('# EXAMPLE_NONEXISTENT_FLAG_CHECK SUCCESS')
      if successful_linters:
        self._file_out.write('\n'.join(successful_linters))
        self._file_out.write('\n')
      if warnings:
        # TODO(b/119550825): remove the go/ link from open source code
        self._file_out.write('Refer to the detailed style guide: '
                             'go/cloud-sdk-help-guide#examples\n'
                             'This is the analysis for EXAMPLES:\n')
        self._file_out.write('\n'.join(warnings))
      else:
        self._file_out.write('There are no errors for the EXAMPLES '
                             'section.\n\n')

  def _analyze_description(self, section):
    successful_linters = []
    warnings = self.check_for_personal_pronouns(section)
    if not warnings:
      successful_linters.append('# DESCRIPTION_PRONOUN_CHECK SUCCESS')
    if successful_linters:
      self._file_out.write('\n'.join(successful_linters))
      self._file_out.write('\n')
    if warnings:
      # TODO(b/119550825): remove the go/ link from open source code
      self._file_out.write('Refer to the detailed style guide: '
                           'go/cloud-sdk-help-guide#description\n'
                           'This is the analysis for DESCRIPTION:\n')
      self._file_out.write('\n'.join(warnings))
    else:
      self._file_out.write('There are no errors for the DESCRIPTION '
                           'section.\n\n')

