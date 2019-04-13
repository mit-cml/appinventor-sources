# -*- coding: utf-8 -*- #
# Copyright 2015 Google Inc. All Rights Reserved.
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
"""This module constructs surveys."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import os
import enum

from googlecloudsdk.command_lib.survey import question
from googlecloudsdk.core import exceptions
from googlecloudsdk.core import log
from googlecloudsdk.core import yaml
from googlecloudsdk.core.util import encoding
from googlecloudsdk.core.util import files as files_util


class Error(exceptions.Error):
  """Base error class for this module."""
  pass


class QuestionTypeNotDefinedError(Error):
  """Raises when question type is not defined in the question module."""
  pass


class SurveyContentNotDefinedError(Error):
  """Raises when survey is not defined in the contents folder."""
  pass


def _GetSurveyContentDirectory():
  """Get the directory containing all surveys in yaml format.

  Returns:
    Path to the surveys directory, i.e.
      $CLOUDSDKROOT/lib/googlecloudsdk/command_lib/survey/contents
  """
  return os.path.join(os.path.dirname(encoding.Decode(__file__)), 'contents')


class Survey(object):
  """The survey class.

  Survey content are defined in yaml files in
  googlecloudsdk/command_lib/survey/contents. Each yaml file represents one
  survey.

  Attributes:
    name: str, name of the survey. It should match a name of one yaml file in
      googlecloudsdk/command_lib/survey/contents (w/o the file extension).
    _survey_content: parsed yaml data, raw content of the survey.
    questions: [Question], list of questions in this survey.
    welcome: str, welcome message when entering the survey.
  """

  @enum.unique
  class ControlOperation(enum.Enum):
    EXIT_SURVEY = 'x'
    SKIP_QUESTION = 's'

  INSTRUCTION_MESSAGE = (
      'To skip this question, type {}; to exit the survey, '
      'type {}.').format(ControlOperation.SKIP_QUESTION.value,
                         ControlOperation.EXIT_SURVEY.value)

  def __init__(self, name):
    self.name = name
    self._survey_content = self._LoadSurveyContent()
    self._questions = list(self._LoadQuestions())

  def _LoadSurveyContent(self):
    """Loads the survey yaml file and return the parsed data."""
    survey_file = os.path.join(_GetSurveyContentDirectory(),
                               self.name + '.yaml')
    if not os.path.isfile(survey_file):
      raise SurveyContentNotDefinedError(
          'Cannot find survey {}.yaml in contents folder.'.format(
              self.name))
    with files_util.FileReader(survey_file) as fp:
      return yaml.load(fp)

  def _LoadQuestions(self):
    """Generator of questions in this survey."""
    for q in self._survey_content['questions']:
      question_type = q['question_type']
      if not hasattr(question, question_type):
        raise QuestionTypeNotDefinedError('The question type is not defined.')
      yield getattr(question, question_type).FromDictionary(q['properties'])

  @property
  def questions(self):
    return self._questions

  @property
  def welcome(self):
    return self._survey_content['welcome']

  def __len__(self):
    return len(self.questions)

  def __iter__(self):
    for q in self.questions:
      yield q

  def PrintWelcomeMsg(self):
    log.err.Print(self.welcome)

  @classmethod
  def PrintInstruction(cls):
    log.err.Print(cls.INSTRUCTION_MESSAGE)

