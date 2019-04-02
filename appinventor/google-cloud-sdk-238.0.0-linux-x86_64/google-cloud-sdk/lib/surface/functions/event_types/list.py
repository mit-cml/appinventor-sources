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
"""List types of events that can be a trigger for a Google Cloud Function."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.functions import triggers
from googlecloudsdk.calliope import base


class List(base.Command):
  """List types of events that can be a trigger for a Google Cloud Function.

  `{command}` displays types of events that can be a trigger for a Google Cloud
  Function.

  * For an event type, `EVENT_TYPE_DEFAULT` marks whether the given event type
    is the default for its provider (in which case the `--event-type` flag may
    be omitted).
  * For a resource, `RESOURCE_OPTIONAL` marks whether the resource has a
    corresponding default value (in which case the `--trigger-resource` flag
    may be omitted).
  """

  @staticmethod
  def Args(parser):
    parser.display_info.AddFormat('''
        table(provider.label:label="EVENT_PROVIDER":sort=1,
              label:label="EVENT_TYPE":sort=2,
              event_is_optional.yesno('Yes'):label="EVENT_TYPE_DEFAULT",
              resource_type.value.name:label="RESOURCE_TYPE",
              resource_is_optional.yesno('Yes'):label="RESOURCE_OPTIONAL"
        )
     ''')

  def Run(self, args):
    for provider in triggers.OUTPUT_TRIGGER_PROVIDER_REGISTRY.providers:
      for event in provider.events:
        yield event
