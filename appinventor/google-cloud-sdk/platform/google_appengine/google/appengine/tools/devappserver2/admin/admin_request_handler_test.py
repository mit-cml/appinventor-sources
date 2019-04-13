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
"""Tests for devappserver2.admin.admin_request_handler."""



import os.path
import tempfile
import unittest
import urlparse

import google
import mox
import webapp2

from google.appengine.tools.devappserver2 import metrics
from google.appengine.tools.devappserver2.admin import admin_request_handler


class XSRFHandler(admin_request_handler.AdminRequestHandler):

  def get(self):
    pass

  def post(self):
    pass

APP = webapp2.WSGIApplication([('/.*', XSRFHandler)])


class XSRFTest(unittest.TestCase):
  """Tests for the admin_request_handler XSRF protection."""

  def test_init_xsrf(self):
    xsrf_path = os.path.join(tempfile.mkdtemp(), 'xsrf')
    admin_request_handler.AdminRequestHandler.init_xsrf(xsrf_path)
    xsrf_token = admin_request_handler.AdminRequestHandler.xsrf_token
    self.assertEqual(10, len(xsrf_token))

    admin_request_handler.AdminRequestHandler.init_xsrf(xsrf_path)
    self.assertEqual(xsrf_token,
                     admin_request_handler.AdminRequestHandler.xsrf_token)

  def test_xsrf_required_not_set(self):
    admin_request_handler.AdminRequestHandler.xsrf_token = '123456789'

    response = APP.get_response('/test', method='POST')
    self.assertEqual(403, response.status_int)

  def test_xsrf_required_and_correct(self):
    admin_request_handler.AdminRequestHandler.xsrf_token = '123456789'

    response = APP.get_response('/test',
                                method='POST',
                                POST={'xsrf_token': '123456789'})
    self.assertEqual(200, response.status_int)

  def test_xsrf_required_and_wrong(self):
    admin_request_handler.AdminRequestHandler.xsrf_token = '123456789'

    response = APP.get_response('/test',
                                method='POST',
                                POST={'xsrf_token': 'wrong'})
    self.assertEqual(403, response.status_int)

  def test_xsrf_not_required(self):
    response = APP.get_response('/test')
    self.assertEqual(200, response.status_int)


class ConstructUrlTest(unittest.TestCase):
  """Tests for AdminRequestHandler._construct_url."""

  def test_construct_url_no_args(self):
    request = webapp2.Request.blank('/foo', POST={'arg1': 'value1',
                                                  'arg2': 'value2'})
    response = webapp2.Response()
    handler = admin_request_handler.AdminRequestHandler(request, response)
    url = handler._construct_url()
    parsed_url = urlparse.urlparse(url)
    self.assertEqual('/foo', parsed_url.path)
    self.assertEqual({'arg1': ['value1'], 'arg2': ['value2']},
                     urlparse.parse_qs(parsed_url.query))

  def test_construct_url_remove(self):
    request = webapp2.Request.blank('/foo', POST={'arg1': 'value1',
                                                  'arg2': 'value2'})
    response = webapp2.Response()
    handler = admin_request_handler.AdminRequestHandler(request, response)
    url = handler._construct_url(remove=['arg1'])
    parsed_url = urlparse.urlparse(url)
    self.assertEqual('/foo', parsed_url.path)
    self.assertEqual({'arg2': ['value2']},
                     urlparse.parse_qs(parsed_url.query))

  def test_construct_url_add(self):
    request = webapp2.Request.blank('/foo', POST={'arg1': 'value1',
                                                  'arg2': 'value2'})
    response = webapp2.Response()
    handler = admin_request_handler.AdminRequestHandler(request, response)
    url = handler._construct_url(add={'arg2': 'new2', 'arg3': 'new3'})
    parsed_url = urlparse.urlparse(url)
    self.assertEqual('/foo', parsed_url.path)
    self.assertEqual({'arg1': ['value1'], 'arg2': ['new2'], 'arg3': ['new3']},
                     urlparse.parse_qs(parsed_url.query))


class MyAdminServerHandler(admin_request_handler.AdminRequestHandler):
  """Dummy class used for testing in AdminRequestHandlerMetricsLoggingTest."""

  def get(self):
    super(MyAdminServerHandler, self).get()

  def post(self):
    super(MyAdminServerHandler, self).post()


class AdminRequestHandlerMetricsLoggingTest(unittest.TestCase):
  """Tests for metrics logging in AdminRequestHandler.get and .post."""

  def setUp(self):
    self.mox = mox.Mox()
    self.mox.StubOutWithMock(metrics._MetricsLogger, 'LogOnceOnStop')

  def tearDown(self):
    self.mox.UnsetStubs()

  def test_get(self):
    handler = admin_request_handler.AdminRequestHandler(
        None, webapp2.Response())
    metrics._MetricsLogger().LogOnceOnStop(
        'admin-console', 'AdminRequestHandler.get')
    self.mox.ReplayAll()
    handler.get()
    self.mox.VerifyAll()

  def test_post(self):
    handler = admin_request_handler.AdminRequestHandler(
        None, webapp2.Response())
    metrics._MetricsLogger().LogOnceOnStop(
        'admin-console', 'AdminRequestHandler.post')
    self.mox.ReplayAll()
    handler.post()
    self.mox.VerifyAll()

  def test_get_with_subclassed_handler(self):
    handler = MyAdminServerHandler(None, webapp2.Response())
    metrics._MetricsLogger().LogOnceOnStop(
        'admin-console', 'MyAdminServerHandler.get')
    self.mox.ReplayAll()
    handler.get()
    self.mox.VerifyAll()

  def test_post_with_subclassed_handler(self):
    handler = MyAdminServerHandler(None, webapp2.Response())
    metrics._MetricsLogger().LogOnceOnStop(
        'admin-console', 'MyAdminServerHandler.post')
    self.mox.ReplayAll()
    handler.post()
    self.mox.VerifyAll()


class AdminRequestHandlerSecurityHeadersTest(unittest.TestCase):
  """Tests for security-related headers in AdminRequestHandler."""

  def setUp(self):
    self.mox = mox.Mox()
    self.mox.StubOutWithMock(metrics._MetricsLogger, 'LogOnceOnStop')
    self.test_app = webapp2.WSGIApplication(
        [('/', admin_request_handler.AdminRequestHandler)])

  def tearDown(self):
    self.mox.UnsetStubs()

  def test_get_response_contains_security_headers(self):
    """Test that a response from a GET request has security headers."""
    request = webapp2.Request.blank('/')
    response = request.get_response(self.test_app)
    self.assertEqual('SAMEORIGIN', response.headers.get('X-Frame-Options'))
    self.assertEqual('1; mode=block', response.headers.get('X-XSS-Protection'))
    self.assertEqual(
        {"default-src 'self'", "frame-ancestors 'none'"},
        set(response.headers.getall('Content-Security-Policy')))


class ByteSizeFormatTest(unittest.TestCase):
  """Tests for the _byte_size_format jinja2 filter."""

  def testOneByte(self):
    self.assertEqual('1 Byte',
                     admin_request_handler._byte_size_format('1'))

  def testLessThan1KiB(self):
    self.assertEqual('123 Bytes',
                     admin_request_handler._byte_size_format('123'))

  def testLessThan1MiB(self):
    self.assertEqual('5.5 KiB (5678 Bytes)',
                     admin_request_handler._byte_size_format('5678'))

  def testLessThan1GiB(self):
    self.assertEqual('11.8 MiB (12345678 Bytes)',
                     admin_request_handler._byte_size_format('12345678'))

  def testGreaterThan1GiB(self):
    self.assertEqual('1.1 GiB (1234567890 Bytes)',
                     admin_request_handler._byte_size_format('1234567890'))

if __name__ == '__main__':
  unittest.main()
