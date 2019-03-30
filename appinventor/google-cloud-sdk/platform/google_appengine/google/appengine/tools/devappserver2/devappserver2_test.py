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
"""Tests for google.apphosting.tools.devappserver2.devappserver2."""



import argparse
import os
import platform
import unittest

import google
import mock
from google.appengine.tools.devappserver2 import devappserver2


class WinError(Exception):
  pass


class FakeApplicationConfiguration(object):

  def __init__(self, modules):
    self.modules = modules


class FakeModuleConfiguration(object):

  def __init__(self, module_name):
    self.module_name = module_name


class CreateModuleToSettingTest(unittest.TestCase):

  def setUp(self):
    self.application_configuration = FakeApplicationConfiguration([
        FakeModuleConfiguration('m1'), FakeModuleConfiguration('m2'),
        FakeModuleConfiguration('m3')])

  def test_none(self):
    self.assertEquals(
        {},
        devappserver2.DevelopmentServer._create_module_to_setting(
            None, self.application_configuration, '--option'))

  def test_dict(self):
    self.assertEquals(
        {'m1': 3, 'm3': 1},
        devappserver2.DevelopmentServer._create_module_to_setting(
            {'m1': 3, 'm3': 1}, self.application_configuration, '--option'))

  def test_single_value(self):
    self.assertEquals(
        {'m1': True, 'm2': True, 'm3': True},
        devappserver2.DevelopmentServer._create_module_to_setting(
            True, self.application_configuration, '--option'))

  def test_dict_with_unknown_modules(self):
    self.assertEquals(
        {'m1': 3.5},
        devappserver2.DevelopmentServer._create_module_to_setting(
            {'m1': 3.5, 'm4': 2.7}, self.application_configuration, '--option'))


class CheckDatastoreEmulatorBinaryExistenceTest(unittest.TestCase):
  """Tests for _fail_for_using_datastore_emulator_from_legacy_sdk."""

  def setUp(self):
    self.options = argparse.Namespace()
    self.dev_server = devappserver2.DevelopmentServer()

  @mock.patch.object(os.path, 'exists', return_value=False)
  def test_fail_missing_emulator(self, unused_mock):
    # Following flags simulate the scenario of invoking dev_appserver.py from
    # google-cloud-sdk/platform/google_appengine
    self.options.support_datastore_emulator = True
    self.options.datastore_emulator_cmd = None
    with self.assertRaises(devappserver2.MissingDatastoreEmulatorError) as ctx:
      self.dev_server._options = self.options
      self.dev_server._fail_for_using_datastore_emulator_from_legacy_sdk()
    self.assertIn('Cannot find Cloud Datastore Emulator', ctx.exception.message)

  def test_succeed_not_using_emulator(self):
    self.options.support_datastore_emulator = False
    self.options.datastore_emulator_cmd = None
    self.dev_server._options = self.options
    self.dev_server._fail_for_using_datastore_emulator_from_legacy_sdk()

  @mock.patch.object(os.path, 'exists', return_value=True)
  def test_succeed_emulator_binary_exists(self, unused_mock):
    self.options.support_datastore_emulator = True
    self.options.datastore_emulator_cmd = ''
    self.dev_server._options = self.options
    self.dev_server._fail_for_using_datastore_emulator_from_legacy_sdk()


@mock.patch(
    'google.appengine.tools.devappserver2.devappserver2'
    '._DatastoreEmulatorDepManager.error_hint', new_callable=mock.PropertyMock)
class DecideUseDatastoreEmulatorTest(unittest.TestCase):
  """Tests for DevelopmentServer._decide_use_datastore_emulator."""

  def setUp(self):
    self.options = argparse.Namespace()
    self.options.datastore_emulator_cmd = ''
    self.options.google_analytics_client_id = '123'
    self.dev_server = devappserver2.DevelopmentServer()
    devappserver2._EMULATOR_ENROLL_CID_SUFFIX = ['1', '2']

  @mock.patch('logging.info')
  def test_explicit_opt_in_succeed(self, mock_logging, mock_error_hint):
    mock_error_hint.return_value = None
    self.options.support_datastore_emulator = True
    self.dev_server._options = self.options
    self.dev_server._decide_use_datastore_emulator()
    self.assertTrue(self.dev_server._options.support_datastore_emulator)
    mock_logging.assert_called_once()

  def test_explicit_opt_in_fail(self, mock_error_hint):
    mock_error_hint.return_value = 'Some hint'
    self.options.support_datastore_emulator = True
    self.dev_server._options = self.options
    self.assertRaises(
        RuntimeError,
        self.dev_server._decide_use_datastore_emulator)

  @mock.patch('logging.info')
  def test_selected_opt_in_succeed(self, mock_logging, mock_error_hint):
    mock_error_hint.return_value = None
    self.options.support_datastore_emulator = None
    self.options.google_analytics_client_id = '1111'
    self.dev_server._options = self.options
    self.dev_server._decide_use_datastore_emulator()
    self.assertTrue(self.dev_server._options.support_datastore_emulator)
    mock_logging.assert_called_once()
    self.assertIn('Using Cloud Datastore Emulator',
                  mock_logging.call_args_list[0][0][0])

  @mock.patch('logging.debug')
  def test_selected_opt_in_fail(self, mock_logging, mock_error_hint):
    mock_error_hint.return_value = 'Some hint'
    self.options.support_datastore_emulator = None
    self.options.google_analytics_client_id = '1111'
    self.dev_server._options = self.options
    self.dev_server._decide_use_datastore_emulator()
    self.assertFalse(self.dev_server._options.support_datastore_emulator)
    mock_logging.assert_called_once()
    self.assertEqual('Some hint', mock_logging.call_args_list[0][0][0])

  @mock.patch('logging.info')
  def test_explicit_opt_out(self, mock_logging, mock_error_hint):
    mock_error_hint.return_value = None
    self.options.support_datastore_emulator = False
    self.options.google_analytics_client_id = '1111'
    self.dev_server._options = self.options
    self.dev_server._decide_use_datastore_emulator()
    self.assertFalse(self.dev_server._options.support_datastore_emulator)
    mock_logging.assert_not_called()


class _DatastoreEmulatorDepManagerTest(unittest.TestCase):
  """Tests generating grpc import report."""

  @mock.patch('__builtin__.__import__', side_effect=mock.Mock())
  @mock.patch(
      'google.appengine.tools.devappserver2.util.get_java_major_version',
      return_value=8)
  def test_grpc_import_succeed_java_check_succeed(self, unused_1, unused_2):
    dep_manager = devappserver2._DatastoreEmulatorDepManager()
    self.assertNotIn('ImportError', dep_manager.grpc_import_report)
    self.assertTrue(dep_manager.satisfied)
    self.assertIsNone(dep_manager.error_hint)

  @mock.patch('__builtin__.__import__', side_effect=mock.Mock())
  @mock.patch(
      'google.appengine.tools.devappserver2.util.get_java_major_version',
      return_value=7)
  def test_grpc_import_succeed_java_check_fail(self, unused_1, unused_2):
    dep_manager = devappserver2._DatastoreEmulatorDepManager()
    self.assertNotIn('ImportError', dep_manager.grpc_import_report)
    self.assertFalse(dep_manager.satisfied)
    self.assertIn('make sure Java 8+ is installed', dep_manager.error_hint)

  @mock.patch('__builtin__.__import__',
              side_effect=ImportError('Cannot import cygrpc'))
  @mock.patch(
      'google.appengine.tools.devappserver2.util.get_java_major_version',
      return_value=8)
  def test_grpc_import_fail_java_check_succeed(self, unused_1, unused_2):
    dep_manager = devappserver2._DatastoreEmulatorDepManager()
    self.assertEqual(repr(ImportError('Cannot import cygrpc')),
                     dep_manager.grpc_import_report['ImportError'])
    self.assertFalse(dep_manager.satisfied)
    self.assertIn('grpcio is incompatible', dep_manager.error_hint)


class PlatformSupportCheckTest(unittest.TestCase):

  def test_succeed_non_python3_windows(self):
    with mock.patch.object(platform, 'system', return_value='Windows'):
      devappserver2.DevelopmentServer._check_platform_support({'python2'})
      platform.system.assert_not_called()

  def test_succeed_python3_non_windows(self):
    with mock.patch.object(platform, 'system', return_value='Linux'):
      devappserver2.DevelopmentServer._check_platform_support({'python3'})
      platform.system.assert_called_once_with()

  def test_fail_python3_windows(self):
    with mock.patch.object(platform, 'system', return_value='Windows'):
      with self.assertRaises(OSError):
        devappserver2.DevelopmentServer._check_platform_support(
            {'python3', 'python2'})
      platform.system.assert_called_once_with()


if __name__ == '__main__':
  unittest.main()
