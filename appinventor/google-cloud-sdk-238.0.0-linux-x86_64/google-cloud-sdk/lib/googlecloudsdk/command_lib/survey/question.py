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
"""This module contains all survey question types."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import abc

from googlecloudsdk.command_lib.survey import util as survey_util
from googlecloudsdk.core import exceptions
from googlecloudsdk.core import log

import six


class Error(exceptions.Error):
  """Base error class for this module."""
  pass


class AnswerRejectedError(Error):
  """Raises when answer is rejected."""
  pass


class RetrieveAnswerOfUnansweredQuestion(Error):
  """Raises when retrieving answer from an unanswered question."""
  pass


class QuestionCreationError(Error):
  """Raises when question cannot be created with the provided data."""
  pass


class Question(six.with_metaclass(abc.ABCMeta, object)):
  """Base class for survey questions.

  Attributes:
    _question: str, the question to ask.
    _instruction: str, instruction on how to answer the question.
    _instruction_on_rejection: str, instruction after the answer is rejected.
    _answer: str, the answer to the question.
  """

  def __init__(self,
               question,
               instruction,
               instruction_on_rejection=None,
               answer=None):
    self._question = question
    self._instruction = instruction
    self._instruction_on_rejection = instruction_on_rejection
    self._answer = answer

  @abc.abstractmethod
  def FromDictionary(self, content):
    pass

  @property
  def question(self):
    return self._question

  @property
  def instruction(self):
    return self._instruction

  @property
  def instruction_on_rejection(self):
    return self._instruction_on_rejection

  @abc.abstractmethod
  def PrintQuestion(self):
    pass

  def PrintInstruction(self):
    if self._instruction:
      log.err.write(self._instruction)

  def PrintInstructionOnRejection(self):
    if self._instruction_on_rejection:
      log.err.write(self._instruction_on_rejection)

  @abc.abstractmethod
  def AcceptAnswer(self, answer):
    """Returns True if answer is accepted, otherwise returns False."""
    pass

  def IsAnswered(self):
    return self._answer is not None

  def AnswerQuestion(self, answer):
    if self.AcceptAnswer(answer):
      self._answer = answer
    else:
      raise AnswerRejectedError('Answer is invalid.')

  @property
  def answer(self):
    if self.IsAnswered():
      return self._answer
    else:
      raise RetrieveAnswerOfUnansweredQuestion('No answer for this question.')

  def __eq__(self, other):
    if isinstance(other, self.__class__):
      # pylint: disable=protected-access
      return (self._question == other._question and
              self._instruction == other._instruction and
              self._instruction_on_rejection == other._instruction_on_rejection)
      # pylint: enable=protected-access
    return False

  def __ne__(self, other):
    return not self == other  # pylint: disable=g-comparison-negation

  def __hash__(self):
    return hash((self._question, self._instruction,
                 self._instruction_on_rejection))


class MultiChoiceQuestion(Question):
  """Multi-choice question.

  Attributes:
    _choices: [str], list of choices.
  """

  def __init__(self,
               question,
               instruction,
               instruction_on_rejection,
               choices,
               answer=None):
    super(MultiChoiceQuestion, self).__init__(
        question, instruction, instruction_on_rejection, answer=answer)
    self._choices = choices

  @classmethod
  def FromDictionary(cls, content):
    try:
      return cls(
          question=content['question'],
          instruction=content['instruction'],
          instruction_on_rejection=content['instruction_on_rejection'],
          choices=content['choices'])
    except KeyError:
      raise QuestionCreationError('Question cannot be created because some '
                                  'required field is missing.')

  def PrintQuestion(self):
    """Prints question and lists all choices."""
    choices_repr = [
        '[{}] {}'.format(index, msg)
        for index, msg in enumerate(self._choices, 1)
    ]
    choices_repr = [survey_util.Indent(content, 2) for content in choices_repr]
    choices_repr = '\n'.join(choices_repr)
    question_repr = survey_util.Indent(self._question, 1)
    log.Print('\n'.join([question_repr, choices_repr]))

  def AcceptAnswer(self, answer):
    """Returns True if answer is accepted, otherwise returns False."""
    try:
      answer_int = int(answer)
    except ValueError:
      return False
    else:
      return 1 <= answer_int <= len(self._choices)

  def __eq__(self, other):
    if isinstance(other, self.__class__):
      # pylint: disable=protected-access
      return (self._question == other._question and
              self._instruction == other._instruction and
              self._instruction_on_rejection == other._instruction_on_rejection
              and self._choices == other._choices)
      # pylint: enable=protected-access
    return False

  def __hash__(self):
    return hash((self._question, self._instruction,
                 self._instruction_on_rejection, tuple(self._choices)))


class FreeTextQuestion(Question):
  """Free text question."""

  def PrintQuestion(self):
    question = survey_util.Indent(self._question, 1)
    log.Print(question)

  def AcceptAnswer(self, answer):
    """Returns True if answer is accepted, otherwise returns False.

    Accepts any answer for free text question.

    Args:
      answer: str, the answer to check.

    Returns:
       True
    """
    return True

  @classmethod
  def FromDictionary(cls, content):
    try:
      return cls(
          question=content['question'],
          instruction=content['instruction'])
    except KeyError:
      raise QuestionCreationError('Question cannot be created because some'
                                  ' required field is missing.')
