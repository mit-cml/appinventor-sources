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
"""Serves the survey and logs the response to clearcut tables.

NOTE: We are waiting for approval from legal/privacy working group. Before it is
approved, we cannot send the survey responses to backend.
"""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.survey import survey
from googlecloudsdk.core import log
from googlecloudsdk.core.console import console_io

# googlecloudsdk/command_lib/survey/contents folder contains survey definitions
_SURVEY_ID = 'TwoQuestionGeneralSurvey'


def _GetAnswerToQuestion(question):
  """Prompts user for the answer to the question."""
  prompt_msg = question.instruction
  while True:
    answer = console_io.PromptResponse(prompt_msg)
    if answer == survey.Survey.ControlOperation.SKIP_QUESTION.value:  # s
      return survey.Survey.ControlOperation.SKIP_QUESTION
    elif answer == survey.Survey.ControlOperation.EXIT_SURVEY.value:  # x
      return survey.Survey.ControlOperation.EXIT_SURVEY
    elif question.AcceptAnswer(answer):
      return answer
    else:
      prompt_msg = question.instruction_on_rejection


def LogResponse(survey_instance):
  """Sends response to concord's clearcut tables."""
  del survey_instance  # TODO(b/121044443): Remove this line when fixed.
  answer = console_io.PromptContinue(
      prompt_string='Do you want to submit your response')
  if answer:
    # TODO(b/121044443): Send response to backend.
    log.err.Print('Your response is submitted.')
  else:
    log.err.Print('Your response is not recorded.')


@base.Hidden
@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class Survey(base.Command):
  """Invokes a customer survey for Cloud SDK."""

  @staticmethod
  def Args(parser):
    pass

  def Run(self, args):
    survey_instance = survey.Survey(_SURVEY_ID)
    survey_instance.PrintWelcomeMsg()
    # Index questions from 1 instead of 0 (default) to be user-friendly.
    for index, question in enumerate(survey_instance, 1):
      progress_msg = '\nQuestion {} of {}:\n'.format(index,
                                                     len(survey_instance))
      log.err.Print(progress_msg)
      question.PrintQuestion()
      log.err.write('\n')
      survey_instance.PrintInstruction()
      answer = _GetAnswerToQuestion(question)
      if answer == survey.Survey.ControlOperation.EXIT_SURVEY:
        log.err.Print('Exited the survey')
        return
      elif answer == survey.Survey.ControlOperation.SKIP_QUESTION:
        log.err.Print('Skipped question {}'.format(index))
        continue
      question.AnswerQuestion(answer)
    LogResponse(survey_instance)
