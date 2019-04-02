# -*- coding: utf-8 -*- #
# Copyright 2016 Google Inc. All Rights Reserved.
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
"""ml-engine models list command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.ml_engine import models
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.ml_engine import models_util
from googlecloudsdk.core import resources


_COLLECTION = 'ml.models'
_DEFAULT_FORMAT = """
        table(
            name.basename(),
            defaultVersion.name.basename()
        )
    """


def _GetUri(model):
  ref = resources.REGISTRY.ParseRelativeName(
      model.name, models_util.MODELS_COLLECTION)
  return ref.SelfLink()


class List(base.ListCommand):
  """List existing Cloud ML Engine models."""

  @staticmethod
  def Args(parser):
    parser.display_info.AddFormat(_DEFAULT_FORMAT)
    parser.display_info.AddUriFunc(_GetUri)

  def Run(self, args):
    return models_util.List(models.ModelsClient())
