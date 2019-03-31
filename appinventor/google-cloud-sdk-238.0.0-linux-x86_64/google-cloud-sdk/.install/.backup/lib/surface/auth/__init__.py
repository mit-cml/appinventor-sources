# -*- coding: utf-8 -*- #
# Copyright 2013 Google Inc. All Rights Reserved.
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

"""Auth for the Google Cloud SDK."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope import base


@base.ReleaseTracks(base.ReleaseTrack.GA,
                    base.ReleaseTrack.BETA,
                    base.ReleaseTrack.ALPHA)
class Auth(base.Group):
  """Manage oauth2 credentials for the Google Cloud SDK.

  The gcloud auth command group lets you grant and revoke authorization to Cloud
  SDK (gcloud) to access Google Cloud Platform. Typically, when scripting Cloud
  SDK tools for use on multiple machines, using `gcloud auth
  activate-service-account` is recommended.

  For more information on authorization and credential types, see:
  [](https://cloud.google.com/sdk/docs/authorizing).

  While running `gcloud auth` commands, the `--account` flag can be specified
  to any command to use that account without activation.

  ## EXAMPLES

  To authenticate a user account with gcloud and minimal user output, run:

    $ gcloud auth login --brief

  To list all credentialed accounts and identify the current active account,
  run:

    $ gcloud auth list

  To revoke credentials for a user account (like logging out), run:

    $ gcloud auth revoke test@gmail.com
  """

  category = base.IDENTITY_AND_SECURITY_CATEGORY

  def Filter(self, context, args):
    del context, args
    base.DisableUserProjectQuota()
