# -*- coding: utf-8 -*- #
# Copyright 2019 Google Inc. All Rights Reserved.
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
"""The command group for cloud dataproc autoscaling policies."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope import base


@base.Hidden  # Hidden until implementation is completed.
@base.ReleaseTracks(base.ReleaseTrack.ALPHA, base.ReleaseTrack.BETA)
class AutoscalingPoliciesBeta(base.Group):
  r"""Create and manage Google Cloud Dataproc autoscaling policies.

  Create and manage Google Cloud Dataproc autoscaling policies.

  ## EXAMPLES

  To create an autoscaling policy, run:

    $ {command} create policy-file.yaml

  To update an autoscaling policy, run:

    $ {command} update policy-file.yaml

  To delete an autoscaling policy, run:

    $ {command} delete my_policy

  To view the details of an autoscaling policy, run:

    $ {command} describe my_policy

  To see the list of all autoscaling policies, run:

    $ {command} list
  """
  pass
