# -*- coding: utf-8 -*- #
# Copyright 2017 Google Inc. All Rights Reserved.
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
"""Flag Utilities for cloud tpu commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope import base


def GetTPUNameArg():
  return base.Argument(
      'tpu_id',
      help='Name of the TPU.')


def GetDescriptionFlag():
  return base.Argument(
      '--description',
      help='Specifies a text description of the TPU.')


def GetNetworkFlag():
  """Set argument for choosing the TPU Network."""
  return base.Argument(
      '--network',
      help="""\
      Specifies the network that this TPU will be a part of.
       If not specified, this defaults to the `default` network.
      """)


def GetAcceleratorTypeFlag():
  """Set argument for choosing the TPU Accelerator type."""
  return base.Argument(
      '--accelerator-type',
      default='v2-8',
      type=lambda x: x.lower(),
      required=False,
      help="""\
      TPU accelerator type for the TPU.
       If not specified, this defaults to `v2-8`.
      """)


def GetVersionFlag():
  """Set argument for choosing the TPU Tensor Flow version."""
  return base.Argument(
      '--version',
      required=True,
      help="""\
      TensorFlow version for the TPU, such as `1.6`. For a list of available
      TensorFlow versions please see https://www.tensorflow.org/versions/.
      """)


def GetRangeFlag():
  """Set Cidr Range for Cloud TPU."""
  return base.Argument(
      '--range',
      required=True,
      help="""\
      CIDR Range for the TPU.
       The IP range that the TPU will select an IP address from.
       Must be in CIDR notation and a `/29` range, for example `192.168.0.0/29`.
       Errors will occur if the CIDR range has already been used for a
       currently existing TPU, the CIDR range conflicts with any networks
       in the user's provided network, or the provided network is peered with
       another network that is using that CIDR range.
      """)
