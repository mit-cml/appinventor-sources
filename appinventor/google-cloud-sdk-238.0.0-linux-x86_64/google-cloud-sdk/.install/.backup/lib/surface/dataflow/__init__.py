# -*- coding: utf-8 -*- #
# Copyright 2014 Google Inc. All Rights Reserved.
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

"""The main command group for myservice.

Everything under here will be the commands in your group.  Each file results in
a command with that name.

This module contains a single class that extends base.Group.  Calliope will
dynamically search for the implementing class and use that as the command group
for this command tree.  You can implement methods in this class to override some
of the default behavior.
"""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope import base
from googlecloudsdk.core import log


SERVICE_NAME = 'dataflow'

DATAFLOW_MESSAGES_MODULE_KEY = 'dataflow_messages'
DATAFLOW_APITOOLS_CLIENT_KEY = 'dataflow_client'
DATAFLOW_REGISTRY_KEY = 'dataflow_registry'


@base.ReleaseTracks(base.ReleaseTrack.BETA, base.ReleaseTrack.GA)
class Dataflow(base.Group):
  """Manage Google Cloud Dataflow jobs.

  The gcloud dataflow command group lets you manage Google Cloud Dataflow jobs.

  Cloud Dataflow is a unified programming model and a managed service for
  developing and executing a wide range of data processing patterns
  including ETL, batch computation, and continuous computation.

  More information on Cloud Dataflow can be found here:
  https://cloud.google.com/dataflow and detailed documentation can be found
  here: https://cloud.google.com/dataflow/docs/
  """

  category = base.DATA_ANALYTICS_CATEGORY

  def Filter(self, context, args):
    del context, args
    base.DisableUserProjectQuota()


@base.ReleaseTracks(base.ReleaseTrack.ALPHA)
class DataflowDeprecated(base.Group):
  """Read and manipulate Google Dataflow resources."""

  def Filter(self, context, args):
    del context, args
    base.DisableUserProjectQuota()
    log.warning('The Dataflow Alpha CLI is now deprecated and will soon be '
                'removed. Please use the new `gcloud beta dataflow` commands.')
