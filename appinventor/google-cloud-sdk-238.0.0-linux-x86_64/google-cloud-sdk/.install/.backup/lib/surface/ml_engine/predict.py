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
"""ml-engine predict command."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.api_lib.ml_engine import predict
from googlecloudsdk.calliope import base
from googlecloudsdk.command_lib.ml_engine import flags
from googlecloudsdk.command_lib.ml_engine import predict_utilities


INPUT_INSTANCES_LIMIT = 100


class Predict(base.Command):
  """Run Cloud ML Engine online prediction.

     `{command}` sends a prediction request to Cloud ML Engine for the given
     instances. This command will read up to 100 instances, though the service
     itself will accept instances up to the payload limit size (currently,
     1.5MB). If you are predicting on more instances, you should use batch
     prediction via

         $ {parent_command} jobs submit prediction.
  """

  @staticmethod
  def Args(parser):
    """Register flags for this command."""
    parser.add_argument('--model', required=True, help='Name of the model.')
    parser.add_argument(
        '--version',
        help="""\
Model version to be used.

If unspecified, the default version of the model will be used. To list model
versions run

  $ {parent_command} versions list
""")
    group = parser.add_mutually_exclusive_group(required=True)
    group.add_argument(
        '--json-instances',
        help="""\
        Path to a local file from which instances are read.
        Instances are in JSON format; newline delimited.

        An example of the JSON instances file:

            {"images": [0.0, ..., 0.1], "key": 3}
            {"images": [0.0, ..., 0.1], "key": 2}
            ...

        This flag accepts "-" for stdin.
        """)
    group.add_argument(
        '--text-instances',
        help="""\
        Path to a local file from which instances are read.
        Instances are in UTF-8 encoded text format; newline delimited.

        An example of the text instances file:

            107,4.9,2.5,4.5,1.7
            100,5.7,2.8,4.1,1.3
            ...

        This flag accepts "-" for stdin.
        """)

    flags.SIGNATURE_NAME.AddToParser(parser)

  def Run(self, args):
    """This is what gets called when the user runs this command.

    Args:
      args: an argparse namespace. All the arguments that were provided to this
        command invocation.

    Returns:
      Some value that we want to have printed later.
    """
    instances = predict_utilities.ReadInstancesFromArgs(
        args.json_instances, args.text_instances, limit=INPUT_INSTANCES_LIMIT)

    model_or_version_ref = predict_utilities.ParseModelOrVersionRef(
        args.model, args.version)

    results = predict.Predict(model_or_version_ref, instances,
                              signature_name=args.signature_name)

    if not args.IsSpecified('format'):
      # default format is based on the response.
      args.format = predict_utilities.GetDefaultFormat(
          results.get('predictions'))

    return results
