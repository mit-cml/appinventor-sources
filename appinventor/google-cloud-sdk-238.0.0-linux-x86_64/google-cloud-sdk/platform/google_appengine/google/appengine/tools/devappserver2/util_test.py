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
"""Unit tests for the util module."""



import os
import shutil
import socket
import stat
import tempfile
import unittest
import wsgiref

import google
import mock
import mox

from google.appengine.tools import sdk_update_checker
from google.appengine.tools.devappserver2 import util


class UtilTest(unittest.TestCase):

  def test_get_headers_from_environ(self):
    environ = {'SERVER_PORT': '42', 'REQUEST_METHOD': 'GET',
               'SERVER_NAME': 'localhost',
               'CONTENT_TYPE': 'application/json',
               'HTTP_CONTENT_LENGTH': '0', 'HTTP_X_USER_IP': '127.0.0.1'}
    headers = util.get_headers_from_environ(environ)

    self.assertEqual(len(headers), 3)
    self.assertEqual(headers['Content-Type'], 'application/json')
    self.assertEqual(headers['Content-Length'], '0')
    self.assertEqual(headers['X-User-IP'], '127.0.0.1')

  def test_put_headers_in_environ(self):
    environ = {'SERVER_PORT': '42', 'REQUEST_METHOD': 'GET'}
    headers = wsgiref.headers.Headers([])
    headers['Content-Length'] = '2'
    headers['X-User-IP'] = '127.0.0.1'
    headers['Access-Control-Allow-Origin'] = 'google.com'
    util.put_headers_in_environ(headers.items(), environ)

    self.assertEqual(environ,
                     {'SERVER_PORT': '42', 'REQUEST_METHOD': 'GET',
                      'HTTP_CONTENT_LENGTH': '2',
                      'HTTP_X_USER_IP': '127.0.0.1',
                      'HTTP_ACCESS_CONTROL_ALLOW_ORIGIN': 'google.com'})

  def test_construct_url_from_environ(self):
    environ = {
        'HTTP_HOST': 'localhost:8080',
        'PATH_INFO': '/index',
        'QUERY_STRING': 'name=test'
    }

    self.assertEqual(
        util.construct_url_from_environ(
            environ, secure=True, include_query_params=True, port=8081),
        'https://localhost:8081/index?name=test')
    self.assertEqual(
        util.construct_url_from_environ(
            environ, secure=False, include_query_params=False, port=80),
        'http://localhost:80/index')


class HTTPServerIPv6Test(unittest.TestCase):

  def testHasIPv6AddressFamily(self):
    server = util.HTTPServerIPv6(None, None, None)
    self.assertEqual(server.address_family, socket.AF_INET6)


class GetSDKVersionTest(unittest.TestCase):
  """Tests for get_sdk_version."""

  def setUp(self):
    self.mox = mox.Mox()

  def tearDown(self):
    self.mox.UnsetStubs()

  def test_version_file_exists(self):
    """If a VERSION file exists, the default SDK version is not used."""
    self.assertNotEqual(util._DEFAULT_SDK_VERSION,
                        util.get_sdk_version())

  def test_version_file_missing(self):
    """If no VERSION file exists, the default SDK version is used."""
    self.mox.StubOutWithMock(sdk_update_checker, 'GetVersionObject')
    sdk_update_checker.GetVersionObject().AndReturn(None)

    self.mox.ReplayAll()
    self.assertEqual(util._DEFAULT_SDK_VERSION,
                     util.get_sdk_version())
    self.mox.VerifyAll()


class GetJavaMajorVersoinTest(unittest.TestCase):

  def testJavaExecutableNotFound(self):
    with mock.patch('subprocess.check_output', side_effect=OSError()):
      self.assertIsNone(util.get_java_major_version())

  def testJava8Installed(self):
    # Java 8 and its lower versions are pre http://openjdk.java.net/jeps/223
    with mock.patch('subprocess.check_output', return_value='version "1.8.0"'):
      self.assertEqual(8, util.get_java_major_version())

  def testJava9Installed(self):
    # Java 9 and its higher versions are post http://openjdk.java.net/jeps/223
    with mock.patch('subprocess.check_output', return_value='version "9.0.1"'):
      self.assertEqual(9, util.get_java_major_version())

  def testInvaidVersionString(self):
    with mock.patch('subprocess.check_output', return_value='foobar'):
      self.assertIsNone(util.get_java_major_version())


class FindJavaOnSystemPathTest(unittest.TestCase):
  """Tests finding java executable on $PATH."""

  def setUp(self):
    self.orig_path = os.environ.get('PATH', None)
    self.tempdir = tempfile.mkdtemp()

  def tearDown(self):
    if self.orig_path:
      os.environ['PATH'] = self.orig_path
    shutil.rmtree(self.tempdir)

  def _create_fake_java_executable(self, java_path):
    with open(java_path, 'w') as fout:
      fout.write('')
    st = os.stat(java_path)
    os.chmod(java_path, st.st_mode | stat.S_IEXEC)

  def testWindows_HasJava_PathQuoted(self):
    java_path = os.path.join(self.tempdir, 'java.exe')
    self._create_fake_java_executable(java_path)
    os.environ['PATH'] = '"%s":"%s"'% (
        self.tempdir, os.path.join(self.tempdir, 'foo'))
    with mock.patch('platform.system', return_value='windows'):
      self.assertEqual(java_path, util._find_java_on_system_path())

  def testWindows_HasJava_PathNotQuoted(self):
    java_path = os.path.join(self.tempdir, 'java.exe')
    self._create_fake_java_executable(java_path)
    os.environ['PATH'] = '%s:%s'% (
        self.tempdir, os.path.join(self.tempdir, 'foo'))
    with mock.patch('platform.system', return_value='windows'):
      self.assertEqual(java_path, util._find_java_on_system_path())

  def testWindows_NoJava(self):
    java_path = os.path.join(self.tempdir, 'java.sh')
    self._create_fake_java_executable(java_path)
    os.environ['PATH'] = '%s:%s'% (
        self.tempdir, os.path.join(self.tempdir, 'foo'))
    with mock.patch('platform.system', return_value='windows'):
      self.assertEqual(None, util._find_java_on_system_path())

  def testNonWindows_HasJava(self):
    java_path = os.path.join(self.tempdir, 'java.sh')
    self._create_fake_java_executable(java_path)
    os.environ['PATH'] = '%s:%s'% (
        self.tempdir, os.path.join(self.tempdir, 'foo'))
    with mock.patch('platform.system', return_value='Linux'):
      self.assertEqual(java_path, util._find_java_on_system_path())

  def testNonWindows_NoJava(self):
    java_path = os.path.join(self.tempdir, 'notjava')
    self._create_fake_java_executable(java_path)
    os.environ['PATH'] = '%s:%s'% (
        self.tempdir, os.path.join(self.tempdir, 'foo'))
    with mock.patch('platform.system', return_value='Darwin'):
      self.assertEqual(None, util._find_java_on_system_path())


if __name__ == '__main__':
  unittest.main()
