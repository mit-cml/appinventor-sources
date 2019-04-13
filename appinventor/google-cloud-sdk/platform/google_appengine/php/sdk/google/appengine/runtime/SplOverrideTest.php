<?php
/**
 * Copyright 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Unit tests for SplOverride.php (overriden Standard PHP Library functions).
 */

namespace google\appengine\runtime;

require_once 'google/appengine/api/modules/modules_service_pb.php';
require_once 'google/appengine/runtime/ApplicationError.php';
require_once 'google/appengine/runtime/SplOverride.php';
require_once 'google/appengine/testing/ApiProxyTestBase.php';

use google\appengine\GetHostnameRequest;
use google\appengine\GetHostnameResponse;
use google\appengine\ModulesServiceError\ErrorCode;
use google\appengine\runtime\ApplicationError;
use google\appengine\testing\ApiProxyTestBase;
use google\appengine\util\StringUtil;

class SplOverrideTest extends ApiProxyTestBase {

  public static function setUpBeforeClass() {
    VirtualFileSystem::getInstance()->initialize();
  }

  // See api\modules\ModulesServiceTest::testGetHostname().
  public function testGetHostName() {
    $req = new GetHostnameRequest();
    $resp = new GetHostnameResponse();

    $resp->setHostname('hostname');

    $this->apiProxyMock->expectCall('modules', 'GetHostname', $req, $resp);

    $this->assertEquals('hostname', SplOverride::gethostname());
    $this->apiProxyMock->verify();
  }

  public function testGetHostNameException() {
    $req = new GetHostnameRequest();
    $resp = new ApplicationError(ErrorCode::TRANSIENT_ERROR, 'unkonwn');

    $this->apiProxyMock->expectCall('modules', 'GetHostname', $req, $resp);

    $this->assertEquals(false, SplOverride::gethostname());
    $this->apiProxyMock->verify();
  }

  // Success case is handled in the cloud_storage e2e test which will upload a
  // file through direct upload resulting in vfs:// wrapper and attempt to move
  // the file to gs://.
  public function testMoveUploadedFileNotUploaded() {
    // Should fail since is_uploaded_file(__FILE__) will result in false.
    $this->assertFalse(SplOverride::move_uploaded_file(__FILE__, '/dev/null'));
  }

  public function testSysGetTempDir() {
    $this->assertEquals('vfs://root/temp', SplOverride::sys_get_temp_dir());
  }

  // Data provider for testTempnam().
  public function tempnamProvider() {
    return [
      ['vfs://root/temp', 'foo', 'vfs://root/temp/foo'],
      ['foo', 'bar', 'vfs://root/temp/foo/bar'],
      ['foo/bar', 'baz', 'vfs://root/temp/foo/bar/baz'],
    ];
  }

  /**
   * @dataProvider tempnamProvider
   */
  public function testTempnam($dir, $prefix, $expected_path_prefix) {
    $filename = SplOverride::tempnam($dir, $prefix);
    $this->assertTrue(StringUtil::startsWith($filename, $expected_path_prefix));
    $this->assertTrue(strlen($filename) > strlen($expected_path_prefix));
    $this->assertTrue(file_exists($filename));
    $this->assertEquals(0600, fileperms($filename) & 0x0FFF);
    $this->assertEquals(0, filesize($filename));
  }

}
