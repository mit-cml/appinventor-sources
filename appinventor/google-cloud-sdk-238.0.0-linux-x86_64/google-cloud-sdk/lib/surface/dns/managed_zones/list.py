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

"""gcloud dns managed-zones list command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from apitools.base.py import list_pager
from googlecloudsdk.api_lib.dns import util
from googlecloudsdk.api_lib.util import apis
from googlecloudsdk.calliope import base
from googlecloudsdk.core import properties


def _GetUriFunction(api_version):
  def _GetUri(resource):
    return util.GetRegistry(api_version).Create(
        'dns.managedZones',
        project=properties.VALUES.core.project.GetOrFail,
        managedZone=resource.name).SelfLink()
  return _GetUri


@base.ReleaseTracks(base.ReleaseTrack.GA)
class List(base.ListCommand):
  """View the list of all your managed-zones.

  This command displays the list of your managed-zones.

  ## EXAMPLES

  To see the list of all managed-zones, run:

    $ {command}

  To see the list of first 10 managed-zones, run:

    $ {command} --limit=10
  """

  @staticmethod
  def Args(parser):
    parser.display_info.AddFormat('table(name, dnsName, description)')
    parser.display_info.AddUriFunc(_GetUriFunction('v1'))

  def Run(self, args):
    dns_client = apis.GetClientInstance('dns', 'v1')

    project_id = properties.VALUES.core.project.GetOrFail()

    return list_pager.YieldFromList(
        dns_client.managedZones,
        dns_client.MESSAGES_MODULE.DnsManagedZonesListRequest(
            project=project_id),
        limit=args.limit, field='managedZones')


@base.ReleaseTracks(base.ReleaseTrack.BETA)
class ListBeta(base.ListCommand):
  """View the list of all your managed-zones.

  This command displays the list of your managed-zones.

  ## EXAMPLES

  To see the list of all managed-zones, run:

    $ {command}

  To see the list of first 10 managed-zones, run:

    $ {command} --limit=10
  """

  @staticmethod
  def Args(parser):
    parser.display_info.AddFormat('table(name, dnsName, description)')
    parser.display_info.AddUriFunc(_GetUriFunction('v1beta2'))

  def Run(self, args):
    dns_client = apis.GetClientInstance('dns', 'v1beta2')

    project_id = properties.VALUES.core.project.GetOrFail()

    return list_pager.YieldFromList(
        dns_client.managedZones,
        dns_client.MESSAGES_MODULE.DnsManagedZonesListRequest(
            project=project_id),
        limit=args.limit, field='managedZones')
