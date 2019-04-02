# TODO(b/120788962) migrate the implementation to declarative.
# -*- coding: utf-8 -*- #
# Copyright 2017 Google Inc. All Rights Reserved.
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
"""Command to add IAM policy binding for a model."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.ml_engine import models
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.iam import iam_util
from googlecloudsdk.command_lib.ml_engine import flags
from googlecloudsdk.command_lib.ml_engine import models_util


@base.ReleaseTracks(base.ReleaseTrack.BETA, base.ReleaseTrack.GA)
class AddIamPolicyBinding(base.Command):
  r"""Add IAM policy binding to a model.

  Adds IAM policy binding to the given model.

  See https://cloud.google.com/iam/docs/managing-policies for details of
  policy role and member types.

  ## EXAMPLES

  The following command will add an IAM policy binding for the role of
  'roles/editor' for the user 'test-user@gmail.com' on the model
  `my_model`:

    $ {command} my_model \
        --member='user:test-user@gmail.com' \
        --role='roles/editor'
  """

  @staticmethod
  def Args(parser):
    flags.GetModelName().AddToParser(parser)
    iam_util.AddArgsForAddIamPolicyBinding(
        parser,
        flags.MlEngineIamRolesCompleter)

  def Run(self, args):
    return models_util.AddIamPolicyBinding(models.ModelsClient(), args.model,
                                           args.member, args.role)


@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class AddIamPolicyBindingAlpha(base.Command):
  """Adds IAM policy binding to a model.

  Adds a policy binding to the IAM policy of a ML engine model, given a model ID
  and the binding. One binding consists of a member, a role, and an optional
  condition.
  """
  detailed_help = iam_util.GetDetailedHelpForAddIamPolicyBinding(
      'model', 'my_model', role='roles/ml.admin', condition=True)

  @staticmethod
  def Args(parser):
    flags.GetModelName().AddToParser(parser)
    iam_util.AddArgsForAddIamPolicyBinding(
        parser,
        flags.MlEngineIamRolesCompleter,
        add_condition=True)

  def Run(self, args):
    condition = iam_util.ValidateAndExtractCondition(args)
    iam_util.ValidateMutexConditionAndPrimitiveRoles(condition, args.role)
    return models_util.AddIamPolicyBindingWithCondition(
        models.ModelsClient(),
        args.model,
        args.member,
        args.role,
        condition)
