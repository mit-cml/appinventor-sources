#!/usr/bin/env python
#
# Copyright 2007 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#




"""The App Identity API lets an application discover its application ID.

Using the ID, an App Engine application can assert its identity to other App
Engine Apps, Google APIs, and third-party applications and services. The
application ID can also be used to generate a URL or email address, or to make a
run-time decision.

The application ID is the same as the project ID. To learn more about the App
Identity API, read the `App Identity Python Overview`_.

.. _App Identity Python Overview:
   https://cloud.google.com/appengine/docs/python/appidentity/
"""

from app_identity import *
