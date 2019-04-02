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
"""Constants used by the devappserver2."""



import logging

# These statuses must not include a response body (RFC 2616).
NO_BODY_RESPONSE_STATUSES = frozenset([100, 101, 204, 304])

# The Cache-Control directives that specify non-public caching.
# At least one of these directives must be given if the Set-Cookie header is
# present.
NON_PUBLIC_CACHE_CONTROLS = frozenset(['private', 'no-cache', 'no-store'])

# All of these headers will be stripped from the request.
# See:
# https://developers.google.com/appengine/docs/python/runtime#Request_Headers




IGNORED_REQUEST_HEADERS = frozenset([
    'accept-encoding',
    'connection',
    'keep-alive',
    'proxy-authorization',
    'te',
    'trailer',
    'transfer-encoding',
    'x-appengine-fake-is-admin',
    'x-appengine-fake-logged-in',
    ])

# All of these headers will be stripped from the response.
# See: https://developers.google.com/appengine/docs/python/runtime#Responses
# Note: Content-Length is set by a subsequent rewriter or removed.
# Note: Server and Date are then set by devappserver2.




_COMMON_IGNORED_RESPONSE_HEADERS = frozenset([
    'connection',
    'content-encoding',
    'date',
    'keep-alive',
    'proxy-authenticate',
    'server',
    'trailer',
    'transfer-encoding',
    'upgrade',
    ])
FRONTEND_IGNORED_RESPONSE_HEADERS = frozenset([
    'x-appengine-blobkey',
    ]) | _COMMON_IGNORED_RESPONSE_HEADERS
RUNTIME_IGNORED_RESPONSE_HEADERS = _COMMON_IGNORED_RESPONSE_HEADERS

# Maximum size of the response from the runtime. An error will be returned if
# this limit is exceeded.
MAX_RUNTIME_RESPONSE_SIZE = 32 << 20  # 32 MB

# A header that indicates to the authorization system that the request should
# be considered to have been made by an administrator.
FAKE_IS_ADMIN_HEADER = 'HTTP_X_APPENGINE_FAKE_IS_ADMIN'

# A header that indicates to the authorization system that the request should
# be considered to have been made by a logged-in user.
FAKE_LOGGED_IN_HEADER = 'HTTP_X_APPENGINE_FAKE_LOGGED_IN'

# Valid choices for --log_level and their corresponding constants in
# runtime_config_pb2.Config.stderr_log_level.
LOG_LEVEL_TO_RUNTIME_CONSTANT = {
    'debug': 0,
    'info': 1,
    'warning': 2,
    'error': 3,
    'critical': 4,
}

# Valid choices for --dev_appserver_log_level and their corresponding Python
# logging levels
LOG_LEVEL_TO_PYTHON_CONSTANT = {
    'debug': logging.DEBUG,
    'info': logging.INFO,
    'warning': logging.WARNING,
    'error': logging.ERROR,
    'critical': logging.CRITICAL,
}

# The default encoding used by the production interpreter.
PROD_DEFAULT_ENCODING = 'ascii'

# The environment variable exposed in the devshell.
DEVSHELL_ENV = 'DEVSHELL_CLIENT_PORT'

# GAE instance class defaults.
DEFAULT_AUTO_SCALING_INSTANCE_CLASS = 'F1'
DEFAULT_BASIC_SCALING_INSTANCE_CLASS = 'B2'
DEFAULT_MANUAL_SCALING_INSTANCE_CLASS = 'B2'

# GAE instance class available memory (MB).
# https://cloud.google.com/appengine/docs/standard/#instance_classes
INSTANCE_CLASS_MEMORY_LIMIT = {
    'B1': 128,
    'B2': 256,
    'B4': 512,
    'B4_1G': 1024,
    'B8': 1024,
    'F1': 128,
    'F2': 256,
    'F4': 512,
    'F4_1G': 1024
}

# Prefixes of log messages when starting subprocesses. Complete log messages
# will all be in the format of "My log msg prefix: <host>:<port>"
API_SERVER_STARTING_MSG = 'Starting API server at'
GRPC_API_SERVER_STARTING_MSG = 'Starting gRPC API server at'
DATASTORE_EMULATOR_STARTING_MSG = 'Starting Cloud Datastore emulator at'
