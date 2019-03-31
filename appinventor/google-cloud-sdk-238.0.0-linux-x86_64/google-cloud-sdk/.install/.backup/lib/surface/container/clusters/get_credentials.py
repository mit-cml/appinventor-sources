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
"""Fetch cluster credentials."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.container import util
from googlecloudsdk.calliope import actions
from googlecloudsdk.calliope import base
from googlecloudsdk.core import log
from googlecloudsdk.core import properties

NOT_RUNNING_MSG = """\
cluster {0} is not running. The kubernetes API may not be available."""


class GetCredentials(base.Command):
  """Fetch credentials for a running cluster.

  {command} updates a `kubeconfig` file with appropriate credentials and
  endpoint information to point `kubectl` at a specific cluster in Google
  Kubernetes Engine.

  It takes a project and a zone as parameters, passed through by set
  defaults or flags. By default, credentials are written to `HOME/.kube/config`.
  You can provide an alternate path by setting the `KUBECONFIG` environment
  variable. If `KUBECONFIG` contains multiple paths, the first one is used.

  This command enables switching to a specific cluster, when working
  with multiple clusters. It can also be used to access a previously created
  cluster from a new workstation.

  By default, {command} will configure kubectl to automatically refresh its
  credentials using the same identity as gcloud. If you are running kubectl as
  part of an application, it is recommended to use [application default
  credentials](https://cloud.google.com/docs/authentication/production).
  To configure a `kubeconfig` file to use application default credentials, set
  the container/use_application_default_credentials
  [Cloud SDK property](https://cloud.google.com/sdk/docs/properties) to true
  before running {command}

  See [](https://cloud.google.com/kubernetes-engine/docs/kubectl) for
  kubectl documentation.
  """
  detailed_help = {
      'EXAMPLES':
          """\
          To switch to working on your cluster 'testcluster1', run:

            $ {command} testcluster1 --zone=us-central1-f
      """,
  }

  @staticmethod
  def Args(parser):
    """Register flags for this command.

    Args:
      parser: An argparse.ArgumentParser-like object. It is mocked out in order
        to capture some information, but behaves like an ArgumentParser.
    """
    parser.add_argument(
        'name',
        help='Name of the cluster to get credentials for.',
        action=actions.StoreProperty(properties.VALUES.container.cluster))
    parser.add_argument(
        '--internal-ip',
        help='Whether to use the internal IP address of the cluster endpoint.',
        action='store_true')

  def Run(self, args):
    """This is what gets called when the user runs this command.

    Args:
      args: an argparse namespace. All the arguments that were provided to this
        command invocation.

    Raises:
      util.Error: if the cluster is unreachable or not running.
    """
    util.CheckKubectlInstalled()
    adapter = self.context['api_adapter']
    location_get = self.context['location_get']
    location = location_get(args)
    cluster_ref = adapter.ParseCluster(args.name, location)
    log.status.Print('Fetching cluster endpoint and auth data.')
    # Call DescribeCluster to get auth info and cache for next time
    cluster = adapter.GetCluster(cluster_ref)
    auth = cluster.masterAuth
    # TODO(b/70856999) Make this consistent with the checks in
    # api_lib/container/kubeconfig.py.
    missing_creds = not (auth and auth.clientCertificate and auth.clientKey)
    if missing_creds and not util.ClusterConfig.UseGCPAuthProvider():
      raise util.Error('get-credentials requires edit permission on {0}'.format(
          cluster_ref.projectId))
    if not adapter.IsRunning(cluster):
      log.warning(NOT_RUNNING_MSG.format(cluster_ref.clusterId))
    util.ClusterConfig.Persist(cluster, cluster_ref.projectId, args.internal_ip)
