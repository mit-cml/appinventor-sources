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
"""The command group for adding cloud dataproc jobs to workflow template."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

import argparse

from googlecloudsdk.calliope import actions
from googlecloudsdk.calliope import arg_parsers
from googlecloudsdk.calliope import base
from googlecloudsdk.core import properties


class AddJob(base.Group):
  r"""Add Google Cloud Dataproc jobs to workflow template.

  ## EXAMPLES

  To add a Hadoop MapReduce job, run:

    $ {command} hadoop --workflow-template my_template --jar my_jar.jar \
        -- arg1 arg2

  To add a Spark Scala or Java job, run:

    $ {command} spark --workflow-template my_template --jar my_jar.jar \
        -- arg1 arg2

  To add a PySpark job, run:

    $ {command} pyspark --workflow-template my_template my_script.py \
        -- arg1 arg2

  To add a Spark SQL job, run:

    $ {command} spark-sql --workflow-template my_template --file my_queries.q

  To add a Pig job, run:

    $ {command} pig --workflow-template my_template --file my_script.pig

  To add a Hive job, run:

    $ {command} hive --workflow-template my_template --file my_queries.q
  """

  @staticmethod
  def Args(parser):
    region_prop = properties.VALUES.dataproc.region
    parser.add_argument(
        '--region',
        help=region_prop.help_text,
        # Don't set default, because it would override users' property setting.
        action=actions.StoreProperty(region_prop))
