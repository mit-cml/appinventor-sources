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

"""List job command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from apitools.base.py import encoding
from apitools.base.py import list_pager

from googlecloudsdk.api_lib.dataproc import constants
from googlecloudsdk.api_lib.dataproc import dataproc as dp
from googlecloudsdk.api_lib.dataproc import util
from googlecloudsdk.calliope import base
from googlecloudsdk.core import properties


STATE_MATCHER_ENUM_MAP = {'active': 'ACTIVE', 'inactive': 'NON_ACTIVE'}


class TypedJob(util.Bunch):
  """Job with additional type field that corresponds to the job_type one_of."""

  def __init__(self, job):
    super(TypedJob, self).__init__(encoding.MessageToDict(job))
    self._job = job
    self._type = None

  @property
  def type(self):
    for field in [field.name for field in self._job.all_fields()]:
      if field.endswith('Job'):
        job_type, _, _ = field.rpartition('Job')
        if self._job.get_assigned_value(field):
          return job_type
    raise AttributeError('Job has no job type')


class List(base.ListCommand):
  """List jobs in a project.

  List jobs in a project. An optional filter can be used to constrain the jobs
  returned. Filters are case-sensitive and have the following syntax:

    [field = value] AND [field [= value]] ...

  where `field` is `status.state` or `labels.[KEY]`, and `[KEY]` is a label
  key. `value` can be ```*``` to match all values. `status.state` can be either
  `ACTIVE` or `INACTIVE`. Only the logical `AND` operator is supported;
  space-separated items are treated as having an implicit `AND` operator.

  ## EXAMPLES

  To see the list of all jobs, run:

    $ {command}

  To see a list of all active jobs in cluster `my_cluster` with a label of
  `env=staging`, run:

    $ {command} --filter='status.state = ACTIVE AND placement.clusterName = my_cluster AND labels.env = staging'
  """

  @staticmethod
  def Args(parser):
    base.URI_FLAG.RemoveFromParser(parser)
    base.PAGE_SIZE_FLAG.SetDefault(parser, constants.DEFAULT_PAGE_SIZE)

    parser.add_argument(
        '--cluster',
        help='Restrict to the jobs of this Dataproc cluster.')

    parser.add_argument(
        '--state-filter',
        choices=sorted(STATE_MATCHER_ENUM_MAP.keys()),
        help='Filter by job state.')
    parser.display_info.AddFormat("""
          table(
            reference.jobId,
            type.yesno(no="-"),
            status.state:label=STATUS
          )
    """)

  def Run(self, args):
    dataproc = dp.Dataproc(self.ReleaseTrack())

    project = properties.VALUES.core.project.GetOrFail()
    region = properties.VALUES.dataproc.region.GetOrFail()

    request = self.GetRequest(dataproc.messages, project, region, args)

    if args.cluster:
      request.clusterName = args.cluster

    if args.state_filter:
      state = STATE_MATCHER_ENUM_MAP.get(args.state_filter)
      request.jobStateMatcher = (
          dataproc.messages.DataprocProjectsRegionsJobsListRequest
          .JobStateMatcherValueValuesEnum.lookup_by_name(state))

    jobs = list_pager.YieldFromList(
        dataproc.client.projects_regions_jobs,
        request,
        limit=args.limit, field='jobs',
        batch_size=args.page_size,
        batch_size_attribute='pageSize')
    return (TypedJob(job) for job in jobs)

  @staticmethod
  def GetRequest(messages, project, region, args):
    # Explicitly null out args.filter if present because by default args.filter
    # also acts as a postfilter to the things coming back from the backend
    backend_filter = None
    if args.filter:
      backend_filter = args.filter
      args.filter = None

    return messages.DataprocProjectsRegionsJobsListRequest(
        projectId=project, region=region, filter=backend_filter)
