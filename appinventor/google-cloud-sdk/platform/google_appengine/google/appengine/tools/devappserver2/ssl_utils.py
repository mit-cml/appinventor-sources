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
"""Utility functions for working with SSL certificates and sockets."""

import collections
import os
import socket
import ssl

import google

from google.appengine.tools.devappserver2 import errors


# Stores paths to SSL certificate and private key necessary for supporting https
SSLCertificatePaths = collections.namedtuple(
    'SSLCertificatePaths', ['ssl_certificate_path', 'ssl_certificate_key_path'])


class SSLCertificatePathsValidationError(errors.Error):
  """Invalid SSL certificate and key pair."""

  def __init__(self, error_message=None, original_exception=None):
    super(errors.Error, self).__init__()
    self.error_message = error_message
    self.original_exception = original_exception


def validate_ssl_certificate_paths_or_raise(ssl_certificate_paths):
  """Validates the provided SSL certificate and key files.

  Specifically, this function (a) verifies that the paths to both files exist
  and (b) tries to convert a temporary socket to an SSL socket using
  ssl.wrap_socket(), bubbling up any exception that is received. Note that
  on earlier versions of Python (< 2.7.9), ssl.wrap_socket() performs fewer
  checks than in later versions, which may lead to some invalid
  certificate/key pairs passing validation.

  Args:
    ssl_certificate_paths: An instance of ssl_utils.SSLCertificatePaths.

  Raises:
    ssl_utils.SSLCertificatePathsValidationError: Raised if the given
      certificate or key file is invalid. Contains either a specific error
      in error_message or the original exception in original_exception.
  """
  certificate_path = ssl_certificate_paths.ssl_certificate_path
  certificate_key_path = ssl_certificate_paths.ssl_certificate_key_path

  # Check that the paths are valid
  if not os.path.exists(certificate_path):
    raise SSLCertificatePathsValidationError(
        error_message='Could not find certificate at path %s' % certificate_path
    )
  if not os.path.exists(certificate_key_path):
    raise SSLCertificatePathsValidationError(
        error_message='Could not find certificate key at path %s' %
        certificate_key_path)

  for (af, socktype, proto, _, sa) in socket.getaddrinfo('localhost', 0):
    # Find an address family and interface we can bind to
    try:
      dummy_server_socket = socket.socket(af, socktype, proto)
    except socket.error:
      continue
    try:
      dummy_server_socket.bind(sa)
      dummy_server_socket.listen(1)
    except socket.error:
      dummy_server_socket.close()
      continue

    # Try to wrap the socket in an SSL context, and bubble up any exception
    # it causes
    try:
      ssl.wrap_socket(
          dummy_server_socket,
          server_side=True,
          certfile=ssl_certificate_paths.ssl_certificate_path,
          keyfile=ssl_certificate_paths.ssl_certificate_key_path)
      return
    except Exception as e:
      raise SSLCertificatePathsValidationError(original_exception=e)
    finally:
      dummy_server_socket.close()
