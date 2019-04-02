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

"""The gcloud app deploy command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.app import appengine_api_client
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.app import deploy_util


_DETAILED_HELP = {
    'brief': ('Deploy the local code and/or configuration of your app to App '
              'Engine.'),
    'DESCRIPTION': """\
        This command is used to deploy both code and configuration to the App
        Engine server. As an input it takes one or more ``DEPLOYABLES'' that
        should be uploaded.  A ``DEPLOYABLE'' can be a service's .yaml file or a
        configuration's .yaml file (for more information about configuration
        files specific to your App Engine environment, refer to
        [](https://cloud.google.com/appengine/docs/standard/python/configuration-files)
        or [](https://cloud.google.com/appengine/docs/flexible/python/configuration-files)).
        Note, for Java Standard apps, you must add the path to the
        `appengine-web.xml` file inside the WEB-INF directory. {command}
        skips files specified in the .gcloudignore file (see `gcloud topic
        gcloudignore` for more information).
        """,
    'EXAMPLES': """\
        To deploy a single service, run:

          $ {command} ~/my_app/app.yaml

        To deploy an App Engine Standard Java service, run:

          $ {command} ~/my_app/WEB-INF/appengine-web.xml

        By default, the service is deployed the current project configured via:

          $ gcloud config set core/project PROJECT

        To override this value for a single deployment, use the ``--project''
        flag:

          $ {command} ~/my_app/app.yaml --project=PROJECT

        To deploy multiple services, run:

          $ {command} ~/my_app/app.yaml ~/my_app/another_service.yaml

        To change the default --promote behavior for your current
        environment, run:

          $ gcloud config set app/promote_by_default false
        """,
}


@base.ReleaseTracks(base.ReleaseTrack.GA)
class DeployGA(base.SilentCommand):
  """Deploy the local code and/or configuration of your app to App Engine."""

  @staticmethod
  def Args(parser):
    """Get arguments for this command."""
    deploy_util.ArgsDeploy(parser)

  def Run(self, args):
    runtime_builder_strategy = deploy_util.GetRuntimeBuilderStrategy(
        base.ReleaseTrack.GA)
    api_client = appengine_api_client.GetApiClientForTrack(self.ReleaseTrack())
    return deploy_util.RunDeploy(
        args,
        api_client,
        runtime_builder_strategy=runtime_builder_strategy,
        parallel_build=False)


@base.ReleaseTracks(base.ReleaseTrack.BETA)
class DeployBeta(base.SilentCommand):
  """Deploy the local code and/or configuration of your app to App Engine."""

  @staticmethod
  def Args(parser):
    """Get arguments for this command."""
    deploy_util.ArgsDeploy(parser)
    parser.add_argument(
        '--no-cache',
        action='store_true',
        default=False,
        help='Skip caching mechanisms involved in the deployment process, in '
        'particular do not use cached dependencies during the build step.')

  def Run(self, args):
    runtime_builder_strategy = deploy_util.GetRuntimeBuilderStrategy(
        base.ReleaseTrack.BETA)
    api_client = appengine_api_client.GetApiClientForTrack(self.ReleaseTrack())

    return deploy_util.RunDeploy(
        args,
        api_client,
        use_beta_stager=True,
        runtime_builder_strategy=runtime_builder_strategy,
        parallel_build=True,
        flex_image_build_option=deploy_util.GetFlexImageBuildOption(
            default_strategy=deploy_util.FlexImageBuildOptions.ON_SERVER),
        disable_build_cache=args.no_cache,
        dispatch_admin_api=True)


DeployGA.detailed_help = _DETAILED_HELP
DeployBeta.detailed_help = _DETAILED_HELP
