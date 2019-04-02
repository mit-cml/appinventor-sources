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

"""Common flags for iam commands."""

from __future__ import absolute_import
from __future__ import division
from __future__ import unicode_literals

from googlecloudsdk.calliope import base


def GetRoleFlag(verb):
  return base.Argument(
      'role',
      metavar='ROLE_ID',
      help='The id of the role to {0}. '
      'Curated roles example: roles/viewer. '
      'Custom roles example: CustomRole. '
      'For custom roles, you must also specify the `--organization` '
      'or `--project` flag.'.format(verb))


def GetCustomRoleFlag(verb):
  return base.Argument(
      'role',
      metavar='ROLE_ID',
      help='The id of the custom role to {0}. '
      'For example: CustomRole. '
      'You must also specify the `--organization` or `--project` '
      'flag.'.format(verb))


def GetOrgFlag(verb):
  return base.Argument(
      '--organization',
      help='The organization of the role you want to {0}.'.format(verb))


_RESOURCE_NAME_HELP = """\
The full resource name or URI to {verb}.

See ["Resource Names"](https://cloud.google.com/apis/design/resource_names) for
details. To get a URI from most `list` commands in `gcloud`, pass the `--uri`
flag. For example:

```
$ gcloud compute instances list --project prj --uri
https://www.googleapis.com/compute/v1/projects/prj/zones/us-east1-c/instances/i1
https://www.googleapis.com/compute/v1/projects/prj/zones/us-east1-d/instances/i2
```

"""


def GetResourceNameFlag(verb):
  return base.Argument('resource', help=_RESOURCE_NAME_HELP.format(verb=verb))
