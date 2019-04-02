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
 * Google Cloud Storage Stream Wrapper Tests.
 *
 * CodeSniffer does not handle files with multiple namespaces well.
 * @codingStandardsIgnoreFile
 *
 */

namespace {

use google\appengine\api\cloud_storage\CloudStorageTools;

// Mock Memcache class
class Memcache {
  // Mock object to validate calls to memcache
  static $mock_memcache = null;

  public static function setMockMemcache($mock) {
    self::$mock_memcache = $mock;
  }
  public function get($keys, $flags = null) {
    return self::$mock_memcache->get($keys, $flags);
  }
  public function set($key, $value, $flag = null, $expire = 0) {
    return self::$mock_memcache->set($key, $value, $flag, $expire);
  }
}

// Mock memcached class, used when invalidating cache entries on write.
class Memcached {
  // Mock object to validate calls to memcached
  static $mock_memcached = null;

  public static function setMockMemcached($mock) {
    self::$mock_memcached = $mock;
  }

  public function deleteMulti($keys, $time = 0) {
    self::$mock_memcached->deleteMulti($keys, $time);
  }
}

// Mock APC functions for App Identity service.
function apc_fetch($name, &$success) {
  if ($name == CloudStorageTools::GS_DEFAULT_BUCKET_APC_KEY) {
    $success = true;
    return 'bucket';
  }
  $success = false;
  return false;
}

function apc_store($name, $value, $ttl) {
  return false;
}

}  // namespace

namespace google\appengine\ext\cloud_storage_streams {

require_once 'google/appengine/testing/ApiProxyTestBase.php';

use google\appengine\testing\ApiProxyTestBase;
use google\appengine\ext\cloud_storage_streams\CloudStorageClient;
use google\appengine\ext\cloud_storage_streams\CloudStorageReadClient;
use google\appengine\ext\cloud_storage_streams\CloudStorageWriteClient;
use google\appengine\ext\cloud_storage_streams\HttpResponse;
use google\appengine\URLFetchRequest\RequestMethod;
use google\appengine\URLFetchServiceError\ErrorCode;
use google\appengine\runtime\ApplicationError;
use google\appengine\testing\TestUtils;

class CloudStorageStreamWrapperTest extends ApiProxyTestBase {

  protected function setUp() {
    parent::setUp();
    $this->_SERVER = $_SERVER;

    if (!defined("GAE_INCLUDE_GS_BUCKETS")) {
      define("GAE_INCLUDE_GS_BUCKETS", "#default#/object_name.png, to_bucket");
    }

    stream_wrapper_register("gs",
        "\\google\\appengine\\ext\\cloud_storage_streams\\CloudStorageStreamWrapper",
        STREAM_IS_URL);

    // By default disable caching so we don't have to mock out memcache in
    // every test.
    stream_context_set_default(['gs' => ['enable_cache' => false]]);

    date_default_timezone_set("UTC");

    $this->mock_memcache = $this->getMock('\Memcache');
    $this->mock_memcache_call_index = 0;
    \Memcache::setMockMemcache($this->mock_memcache);

    $this->mock_memcached = $this->getMock('\Memcached');
    \Memcached::setMockMemcached($this->mock_memcached);

    $this->triggered_errors = [];
    $this->error_handler_installed = false;

    $this->deadline = CloudStorageClient::DEFAULT_CONNECTION_TIMEOUT_SECONDS;

    // Clear the stat cache for each new test
    CloudStorageClient::clearStatCache();
  }

  public function errorHandler(
      $errno , $errstr, $errfile=null, $errline=null, $errcontext=null) {
    $this->triggered_errors[] = ["errno" => $errno, "errstr" => $errstr];
  }

  private function setErrorHandler() {
    if ($this->error_handler_installed) {
      throw new ErrorException('Only call setErrorHandler once per test.');
    }
    set_error_handler(array($this, "errorHandler"));
    $this->error_handler_installed = true;
  }

  protected function tearDown() {
    stream_wrapper_unregister("gs");

    if ($this->error_handler_installed) {
      restore_error_handler();
    }

    $_SERVER = $this->_SERVER;
    parent::tearDown();
  }

  /**
   * @dataProvider invalidGCSPaths
   */
  public function testInvalidPathName($path) {
    $this->setExpectedException(
      'PHPUnit_Framework_Error_Warning',
      'fopen(' . $path . '): failed to open stream: "\google\appengine\ext' .
      '\cloud_storage_streams\CloudStorageStreamWrapper::stream_open" call ' .
      'failed');
    $this->assertFalse(fopen($path, "r"));
  }

  public function invalidGCSPaths() {
    return [["gs:///object.png"],
            ["gs://"],
            ];
  }

  /**
   * @dataProvider invalidGCSModes
   */
  public function testInvalidMode($mode) {
    $this->setErrorHandler();
    $valid_path = "gs://bucket/object_name.png";
    $this->assertFalse(fopen($valid_path, $mode));
    $this->assertEquals(E_WARNING, $this->triggered_errors[0]["errno"]);
    $this->assertStringStartsWith(
        "fopen($valid_path): failed to open stream",
        $this->triggered_errors[0]["errstr"]);
  }

  public function invalidGCSModes() {
    return [["r+"], ["w+"], ["a"], ["a+"], ["x+"], ["c"], ["c+"]];
  }

  public function testReadObjectSuccess() {
    $body = "Hello from PHP";

    $this->expectFileReadRequest([
        'body' => $body,
        'start_byte' => 0,
        'length' => CloudStorageReadClient::DEFAULT_READ_SIZE,
    ]);

    $valid_path = "gs://bucket/object_name.png";
    $data = file_get_contents($valid_path);

    $this->assertEquals($body, $data);
    $this->apiProxyMock->verify();
  }

  public function testReadObjectFailure() {
    $body = "Hello from PHP";

    $this->expectGetAccessTokenRequest(CloudStorageClient::READ_SCOPE);
    $exected_url = self::makeCloudStorageObjectUrl("bucket",
                                                   "/object_name.png");
    $request_headers = [
        "Authorization" => "OAuth foo token",
        "Range" => sprintf("bytes=0-%d",
                           CloudStorageReadClient::DEFAULT_READ_SIZE-1),
        "x-goog-api-version" => 2,
    ];
    $failure_response = [
        "status_code" => 400,
        "headers" => [],
        "body" => "",
    ];
    $this->expectHttpRequest($exected_url,
                             RequestMethod::GET,
                             $request_headers,
                             null,
                             $failure_response);

    $this->setErrorHandler();
    $this->assertFalse(file_get_contents("gs://bucket/object_name.png"));
    $this->apiProxyMock->verify();

    $this->assertEquals(E_USER_WARNING, $this->triggered_errors[0]["errno"]);
    $this->assertEquals("Cloud Storage Error: BAD REQUEST",
                        $this->triggered_errors[0]["errstr"]);
    $this->assertEquals(E_WARNING, $this->triggered_errors[1]["errno"]);
    $this->assertStringStartsWith(
        "file_get_contents(gs://bucket/object_name.png): failed to open stream",
        $this->triggered_errors[1]["errstr"]);
  }

  public function testReadObjectTransientFailureThenSuccess() {
    $body = "Hello from PHP";

    $this->expectGetAccessTokenRequest(CloudStorageClient::READ_SCOPE);
    $exected_url = self::makeCloudStorageObjectUrl("bucket",
                                                   "object_name.png");

    $this->mock_memcache->expects($this->at($this->mock_memcache_call_index++))
                        ->method('get')
                        ->with($this->stringStartsWith('_ah_gs_read_cache'))
                        ->will($this->returnValue(false));

    $request_headers = [
        "Authorization" => "OAuth foo token",
        "Range" => sprintf("bytes=0-%d",
                           CloudStorageReadClient::DEFAULT_READ_SIZE-1),
        "x-goog-api-version" => 2,
    ];
    // Use a non default connection timeout.
    $this->deadline = 13;

    // The first request will fail urlfetch deadline exceeded exception
    $failure_response = new ApplicationError(ErrorCode::DEADLINE_EXCEEDED,
                                             "Deadline Exceeded");

    $this->expectHttpRequest($exected_url,
                             RequestMethod::GET,
                             $request_headers,
                             null,
                             $failure_response);

    // The second request will succeed.
    $response_headers = [
        "ETag" => "deadbeef",
        "Content-Type" => "text/plain",
        "Last-Modified" => "Mon, 02 Jul 2012 01:41:01 GMT",
    ];
    $response = $this->createSuccessfulGetHttpResponse(
         $response_headers,
         $body,
         0,
         CloudStorageReadClient::DEFAULT_READ_SIZE,
         null);
    $this->expectHttpRequest($exected_url,
                             RequestMethod::GET,
                             $request_headers,
                             null,
                             $response);
    $options = [ 'gs' => [
            'connection_timeout_seconds' => $this->deadline,
        ]
    ];
    $ctx = stream_context_create($options);
    $data = file_get_contents("gs://bucket/object_name.png", false, $ctx);
    $this->assertEquals($body, $data);
    $this->apiProxyMock->verify();
  }

  public function testReadObjectUrlFetchExceptionThenSuccess() {
    $body = "Hello from PHP";

    $this->expectGetAccessTokenRequest(CloudStorageClient::READ_SCOPE);
    $exected_url = self::makeCloudStorageObjectUrl("bucket",
                                                   "object_name.png");
    $request_headers = [
        "Authorization" => "OAuth foo token",
        "Range" => sprintf("bytes=0-%d",
                           CloudStorageReadClient::DEFAULT_READ_SIZE-1),
        "x-goog-api-version" => 2,
    ];

    // The first request will fail with a 500 error, which can be retried.
    $failure_response = [
        "status_code" => 500,
        "headers" => [],
        "body" => "",
    ];
    $this->expectHttpRequest($exected_url,
                             RequestMethod::GET,
                             $request_headers,
                             null,
                             $failure_response);

    // The second request will succeed.
    $response_headers = [
        "ETag" => "deadbeef",
        "Content-Type" => "text/plain",
        "Last-Modified" => "Mon, 02 Jul 2012 01:41:01 GMT",
    ];
    $response = $this->createSuccessfulGetHttpResponse(
        $response_headers,
         $body,
         0,
         CloudStorageReadClient::DEFAULT_READ_SIZE,
         null);
    $this->expectHttpRequest($exected_url,
                             RequestMethod::GET,
                             $request_headers,
                             null,
                             $response);

    $data = file_get_contents("gs://bucket/object_name.png");
    $this->assertEquals($body, $data);
    $this->apiProxyMock->verify();
  }

  public function testReadObjectRepeatedTransientFailure() {
    $body = "Hello from PHP";

    $this->expectGetAccessTokenRequest(CloudStorageClient::READ_SCOPE);
    $request_headers = [
        "Authorization" => "OAuth foo token",
        "Range" => sprintf("bytes=0-%d",
                           CloudStorageReadClient::DEFAULT_READ_SIZE-1),
        "x-goog-api-version" => 2,
    ];
    $exected_url = self::makeCloudStorageObjectUrl("bucket",
                                                   "object_name.png");

    // The first request will fail with a 500 error, which can be retried.
    $failure_response = [
        "status_code" => 500,
        "headers" => [],
        "body" => "",
    ];
    $this->expectHttpRequest($exected_url,
                             RequestMethod::GET,
                             $request_headers,
                             null,
                             $failure_response);
    $this->expectHttpRequest($exected_url,
                             RequestMethod::GET,
                             $request_headers,
                             null,
                             $failure_response);
    $this->expectHttpRequest($exected_url,
                             RequestMethod::GET,
                             $request_headers,
                             null,
                             $failure_response);

    $this->setErrorHandler();
    $this->assertFalse(file_get_contents("gs://bucket/object_name.png"));
    $this->apiProxyMock->verify();
    $this->assertEquals(E_USER_WARNING, $this->triggered_errors[0]["errno"]);
    $this->assertEquals("Cloud Storage Error: INTERNAL SERVER ERROR",
                        $this->triggered_errors[0]["errstr"]);
    $this->assertEquals(E_WARNING, $this->triggered_errors[1]["errno"]);
    $this->assertStringStartsWith(
        "file_get_contents(gs://bucket/object_name.png): failed to open stream",
        $this->triggered_errors[1]["errstr"]);
  }

  /**
   * @dataProvider shortNameAndLongNameDataProviderFortCacheTestingHammer
   */
  public function testReadObjectCacheHitSuccess($name_length, $key_prefix) {
    $body = "Hello from PHP";

    // First call is to create the OAuth token.
    $this->expectGetAccessTokenRequest(CloudStorageClient::READ_SCOPE);

    // Second call is to retrieve the cached read.
    $response = [
        'status_code' => 200,
        'headers' => [
            'Content-Length' => strlen($body),
            'ETag' => 'deadbeef',
            'Content-Type' => 'text/plain',
            'Last-Modified' => 'Mon, 02 Jul 2012 01:41:01 GMT',
        ],
        'body' => $body,
    ];
    $this->mock_memcache->expects($this->at($this->mock_memcache_call_index++))
                        ->method('get')
                        ->with($this->stringStartsWith($key_prefix))
                        ->will($this->returnValue($response));

    // We now expect a read request with If-None-Modified set to our etag.
    $request_headers = [
        'Authorization' => 'OAuth foo token',
        'Range' => sprintf('bytes=%d-%d',
                           0,
                           CloudStorageReadClient::DEFAULT_READ_SIZE - 1),
        'If-None-Match' => 'deadbeef',
        'x-goog-api-version' => 2,
    ];
    $response = [
        'status_code' => HttpResponse::NOT_MODIFIED,
        'headers' => [
        ],
    ];

    $bucket = "a_long_bucket_name";
    $object = '/' . str_repeat('a', $name_length);
    $expected_url = $this->makeCloudStorageObjectUrl($bucket, $object);
    $this->expectHttpRequest($expected_url,
                             RequestMethod::GET,
                             $request_headers,
                             null,
                             $response);

    $options = [ 'gs' => [
            'enable_cache' => true,
            'enable_optimistic_cache' => false,
        ]
    ];
    $ctx = stream_context_create($options);
    $valid_path = 'gs://' . $bucket . $object;
    $data = file_get_contents($valid_path, false, $ctx);

    $this->assertEquals($body, $data);
    $this->apiProxyMock->verify();
  }

  /**
   * @dataProvider shortNameAndLongNameDataProviderFortCacheTestingHammer
   */
  public function testReadObjectCacheWriteSuccess($name_length, $key_prefix) {
    $body = "Hello from PHP";
    $bucket = "a_long_bucket_name";
    $object = '/' . str_repeat('a', $name_length);

    $this->expectFileReadRequest([
        'body' => $body,
        'start_byte' => 0,
        'length' => CloudStorageReadClient::DEFAULT_READ_SIZE,
        'bucket' => $bucket,
        'object' => $object
    ]);

    // Don't read the page from the cache
    $this->mock_memcache->expects($this->at($this->mock_memcache_call_index++))
                        ->method('get')
                        ->with($this->stringStartsWith($key_prefix))
                        ->will($this->returnValue(false));

    // Expect a write back to the cache
    $cache_expiry_seconds = 60;
    $this->mock_memcache->expects($this->at($this->mock_memcache_call_index++))
                        ->method('set')
                        ->with($this->stringStartsWith($key_prefix),
                               $this->anything(),
                               null,
                               $cache_expiry_seconds)
                        ->will($this->returnValue(false));


    $options = [ 'gs' => [
            'enable_cache' => true,
            'enable_optimistic_cache' => false,
            'read_cache_expiry_seconds' => $cache_expiry_seconds,
        ]
    ];
    $ctx = stream_context_create($options);
    $valid_path = 'gs://' . $bucket . $object;
    $data = file_get_contents($valid_path, false, $ctx);

    $this->assertEquals($body, $data);
    $this->apiProxyMock->verify();
  }

  /**
   * @dataProvider shortNameAndLongNameDataProviderFortCacheTestingHammer
   */
  public function testReadObjectOptimisiticCacheHitSuccess($name_length,
                                                           $key_prefix) {
    $body = "Hello from PHP";

    // First call is to create the OAuth token.
    $this->expectGetAccessTokenRequest(CloudStorageClient::READ_SCOPE);

    // Second call is to retrieve the cached read.
    $response = [
        'status_code' => 200,
        'headers' => [
            'Content-Length' => strlen($body),
            'ETag' => 'deadbeef',
            'Content-Type' => 'text/plain',
            'Last-Modified' => 'Mon, 02 Jul 2012 01:41:01 GMT',
        ],
        'body' => $body,
    ];
    $this->mock_memcache->expects($this->at($this->mock_memcache_call_index++))
                        ->method('get')
                        ->with($this->stringStartsWith($key_prefix))
                        ->will($this->returnValue($response));

    $options = [ 'gs' => [
            'enable_cache' => true,
            'enable_optimistic_cache' => true,
        ]
    ];
    $ctx = stream_context_create($options);
    $bucket = "a_long_bucket_name";
    $object = '/' . str_repeat('a', $name_length);
    $valid_path = 'gs://' . $bucket . $object;
    $data = file_get_contents($valid_path, false, $ctx);

    $this->assertEquals($body, $data);
    $this->apiProxyMock->verify();
  }

  /**
   * Currently used by:
   * - testReadObjectCacheHitSuccess
   * - testReadObjectCacheWriteSuccess
   * - testReadObjectOptimisiticCacheHitSuccess
   * and maybe more in the future.
   */
  public function shortNameAndLongNameDataProviderFortCacheTestingHammer() {
    return [
      [10, '_ah_gs_read_cache'],
      [255, '_ah_gs_read_hash'],
    ];
  }

  public function testReadObjectPartialContentResponseSuccess() {
    // GCS returns a 206 even if you can obtain all of the file in the first
    // read - this test simulates that behavior.
    $body = "Hello from PHP.";

    $this->expectFileReadRequest([
        'body' => $body,
        'start_byte' => 0,
        'length' => CloudStorageReadClient::DEFAULT_READ_SIZE,
        'partial_content' => true
    ]);

    $valid_path = "gs://bucket/object_name.png";
    $data = file_get_contents($valid_path);

    $this->assertEquals($body, $data);
    $this->apiProxyMock->verify();
  }

  public function testReadLargeObjectSuccess() {
    $body = str_repeat("1234567890", 100000);
    $data_len = strlen($body);

    $read_chunks = ceil($data_len / CloudStorageReadClient::DEFAULT_READ_SIZE);
    $start_chunk = 0;
    $etag = null;

    for ($i = 0; $i < $read_chunks; $i++) {
      $this->expectFileReadRequest([
          'body' => $body,
          'start_byte' => $start_chunk,
          'length' => CloudStorageReadClient::DEFAULT_READ_SIZE,
          'etag' => $etag,
          'partial_content' => true
      ]);
      $start_chunk += CloudStorageReadClient::DEFAULT_READ_SIZE;
      $etag = "deadbeef";
    }

    $valid_path = "gs://bucket/object_name.png";
    $fp = fopen($valid_path, "rt");
    $data = stream_get_contents($fp);
    fclose($fp);

    $this->assertEquals($body, $data);
    $this->apiProxyMock->verify();
  }

  public function testSeekSetReadObjectSuccess() {
    $body = "Hello from PHP";

    $this->expectFileReadRequest([
        'body' => $body,
        'start_byte' => 0,
        'length' => CloudStorageReadClient::DEFAULT_READ_SIZE,
    ]);

    $valid_path = "gs://bucket/object_name.png";
    $fp = fopen($valid_path, "r");
    $this->assertEquals(0, fseek($fp, 4, SEEK_SET));
    $this->assertEquals($body[4], fread($fp, 1));
    $this->assertEquals(-1, fseek($fp, 100, SEEK_SET));
    $this->assertTrue(fclose($fp));

    $this->apiProxyMock->verify();
  }

  public function testSeekCurReadObjectSuccess() {
    $body = "Hello world!";

    $this->expectFileReadRequest([
        'body' => $body,
        'start_byte' => 0,
        'length' => CloudStorageReadClient::DEFAULT_READ_SIZE,
    ]);

    $valid_path = "gs://bucket/object_name.png";
    $fp = fopen($valid_path, "r");
    $this->assertEquals(0, fseek($fp, 2, SEEK_SET));
    $this->assertEquals(0, fseek($fp, 4, SEEK_CUR));
    $this->assertEquals('world!', fread($fp, 6));
    $this->apiProxyMock->verify();
  }

  public function testSeekEndReadObjectSuccess() {
    $body = "Hello world!";

    $this->expectFileReadRequest([
        'body' => $body,
        'start_byte' => 0,
        'length' => CloudStorageReadClient::DEFAULT_READ_SIZE,
    ]);

    $valid_path = "gs://bucket/object_name.png";
    $fp = fopen($valid_path, "r");
    $this->assertEquals(0, fseek($fp, -6, SEEK_END));
    $this->assertEquals('world!', fread($fp, 6));
    $this->apiProxyMock->verify();
  }

  public function testSeekWithinBufferSuccess() {
    $buffer_size = CloudStorageReadClient::DEFAULT_READ_SIZE;
    $body = str_repeat('a', $buffer_size);
    $this->expectFileReadRequest([
        'body' => $body,
        'start_byte' => 0,
        'length' => $buffer_size
    ]);

    $valid_path = "gs://bucket/object_name.png";
    $fp = fopen($valid_path, "r");
    $this->assertEquals(0, fseek($fp, 10));
    $this->assertEquals(10, ftell($fp));
    $this->assertEquals('aa', fread($fp, 2));
    $this->assertEquals(0, fseek($fp, $buffer_size-1));
    $this->assertEquals($buffer_size-1, ftell($fp));
    $this->assertEquals('a', fread($fp, 2));
    $this->assertTrue(fclose($fp));

    $this->apiProxyMock->verify();
  }

  public function testSeekOutsideBufferSuccess() {
    $buffer_size = CloudStorageReadClient::DEFAULT_READ_SIZE;
    $body = str_repeat('a', $buffer_size) . '0123456789';
    $this->expectFileReadRequest([
        'body' => $body,
        'start_byte' => 0,
        'length' => $buffer_size,
        'partial_content' => true
    ]);
    $this->expectFileReadRequest([
        'body' => $body,
        'start_byte' => $buffer_size,
        'length' => $buffer_size,
        'etag' => 'deadbeef',
        'partial_content' => true
    ]);

    $valid_path = "gs://bucket/object_name.png";
    $fp = fopen($valid_path, "r");
    $this->assertEquals(0, fseek($fp, $buffer_size));
    $this->assertEquals($buffer_size, ftell($fp));
    $this->assertEquals('012', fread($fp, 3));
    $this->assertEquals(0, fseek($fp, $buffer_size+5));
    $this->assertEquals($buffer_size+5, ftell($fp));
    $this->assertEquals('56789', fread($fp, 5));
    $this->assertTrue(fclose($fp));

    $this->apiProxyMock->verify();
  }
  public function testReadZeroSizedObjectSuccess() {
    $this->expectFileReadRequest([
        'body' => "",
        'start_byte' => 0,
        'length' => CloudStorageReadClient::DEFAULT_READ_SIZE,
    ]);

    $data = file_get_contents("gs://bucket/object_name.png");

    $this->assertEquals("", $data);
    $this->apiProxyMock->verify();
  }

  public function testFileSizeSucess() {
    $body = "Hello from PHP";

    $this->expectFileReadRequest([
        'body' =>$body,
        'start_byte' => 0,
        'length' => CloudStorageReadClient::DEFAULT_READ_SIZE,
    ]);

    $valid_path = "gs://bucket/object_name.png";
    $fp = fopen($valid_path, "r");
    $stat = fstat($fp);
    fclose($fp);
    $this->assertEquals(strlen($body), $stat["size"]);
    $this->apiProxyMock->verify();
  }

  public function testDeleteObjectSuccess() {
    $this->expectGetAccessTokenRequest(CloudStorageClient::WRITE_SCOPE);

    $request_headers = $this->getStandardRequestHeaders();
    $response = [
        'status_code' => 204,
        'headers' => [
        ],
    ];
    $expected_url = $this->makeCloudStorageObjectUrl("my_bucket",
                                                     "some%file.txt");
    $this->expectHttpRequest($expected_url,
                             RequestMethod::DELETE,
                             $request_headers,
                             null,
                             $response);

    $this->assertTrue(unlink("gs://my_bucket/some%file.txt"));
    $this->apiProxyMock->verify();
  }

  public function testDeleteObjectFail() {
    $this->expectGetAccessTokenRequest(CloudStorageClient::WRITE_SCOPE);

    $request_headers = $this->getStandardRequestHeaders();
    $response = [
        'status_code' => 404,
        'headers' => [
        ],
        'body' => "<?xml version='1.0' encoding='utf-8'?>
                   <Error>
                   <Code>NoSuchBucket</Code>
                   <Message>No Such Bucket</Message>
                   </Error>",
    ];
    $expected_url = $this->makeCloudStorageObjectUrl();
    $this->expectHttpRequest($expected_url,
                             RequestMethod::DELETE,
                             $request_headers,
                             null,
                             $response);

    $this->setExpectedException(
        "PHPUnit_Framework_Error",
        "Cloud Storage Error: No Such Bucket (NoSuchBucket)");
    $this->assertFalse(unlink("gs://bucket/object.png"));
    $this->apiProxyMock->verify();
  }

  public function testStatBucketSuccess() {
    $this->expectGetAccessTokenRequest(CloudStorageClient::READ_SCOPE);
    $request_headers = $this->getStandardRequestHeaders();
    $file_results = ['file1.txt', 'file2.txt'];
    $response = [
        'status_code' => 200,
        'headers' => [
        ],
        'body' => $this->makeGetBucketXmlResponse("", $file_results),
    ];
    $expected_url = $this->makeCloudStorageObjectUrl("bucket", null);
    $expected_query = http_build_query([
        "delimiter" => CloudStorageClient::DELIMITER,
        "max-keys" => CloudStorageUrlStatClient::MAX_KEYS,
    ]);

    $this->expectHttpRequest(sprintf("%s?%s", $expected_url, $expected_query),
                             RequestMethod::GET,
                             $request_headers,
                             null,
                             $response);

    // Return a false is writable check from the cache
    $this->expectIsWritableMemcacheLookup(true, false);

    $this->assertTrue(is_dir("gs://bucket"));
    $this->apiProxyMock->verify();
  }

  public function testStatObjectSuccess() {
    $this->expectGetAccessTokenRequest(CloudStorageClient::READ_SCOPE);
    $last_modified = 'Mon, 01 Jul 2013 10:02:46 GMT';
    $request_headers = $this->getStandardRequestHeaders();
    $response = [
        'status_code' => 200,
        'headers' => [
            'x-goog-stored-content-length' => '37337',
            'Last-Modified' => $last_modified,
        ],
        'body' => '',
    ];
    $expected_url = $this->makeCloudStorageObjectUrl("bucket", "object.png");
    $this->expectHttpRequest($expected_url,
                             RequestMethod::HEAD,
                             $request_headers,
                             null,
                             $response);

    // Don't find the key in the cache, to force a write attempt to the bucket.
    $temp_url = $this->makeCloudStorageObjectUrl("bucket",
        CloudStorageClient::WRITABLE_TEMP_FILENAME);
    $this->expectIsWritableMemcacheLookup(false, false);
    $this->expectFileWriteStartRequest(null, null, 'foo', $temp_url, null);
    $this->expectIsWritableMemcacheSet(true);


    $result = stat("gs://bucket/object.png");
    $this->assertEquals(37337, $result['size']);
    $this->assertEquals(0100666, $result['mode']);
    $this->assertEquals(strtotime($last_modified), $result['mtime']);
    $this->apiProxyMock->verify();
  }

  public function testStatObjectCacheSuccess() {
    $last_modified = 'Mon, 01 Jul 2013 10:02:46 GMT';
    $request_headers = $this->getStandardRequestHeaders();
    $response = [
        'status_code' => 200,
        'headers' => [
            'x-goog-stored-content-length' => '37337',
            'Last-Modified' => $last_modified,
        ],
        'body' => '',
    ];
    $expected_url = $this->makeCloudStorageObjectUrl("bucket", "object.png");
    // RPCs for the first stat call.
    $this->expectGetAccessTokenRequest(CloudStorageClient::READ_SCOPE);
    $this->expectHttpRequest($expected_url,
                             RequestMethod::HEAD,
                             $request_headers,
                             null,
                             $response);
    $this->expectIsWritableMemcacheLookup(true, false);

    // RPCs for the second stat call to a different object
    $expected_url = $this->makeCloudStorageObjectUrl("bucket2", "object.png");
    $this->expectGetAccessTokenRequest(CloudStorageClient::READ_SCOPE);
    $this->expectHttpRequest($expected_url,
                             RequestMethod::HEAD,
                             $request_headers,
                             null,
                             $response);
    $this->expectIsWritableMemcacheLookup(true, false);

    $this->assertTrue(is_file("gs://bucket/object.png"));

    // Stat the second object to clear the internal stat cache in PHP
    $this->assertTrue(is_file("gs://bucket2/object.png"));

    // Now stat the first object again, it should come from out cache.
    $this->assertTrue(is_file("gs://bucket/object.png"));

    $this->apiProxyMock->verify();
  }

  public function testStatObjectClearCacheSuccess() {
    $last_modified = 'Mon, 01 Jul 2013 10:02:46 GMT';
    $request_headers = $this->getStandardRequestHeaders();
    $response = [
        'status_code' => 200,
        'headers' => [
            'x-goog-stored-content-length' => '37337',
            'Last-Modified' => $last_modified,
        ],
        'body' => '',
    ];
    // RPCs for the first stat call.
    $expected_url = $this->makeCloudStorageObjectUrl("bucket", "object.png");
    $this->expectGetAccessTokenRequest(CloudStorageClient::READ_SCOPE);
    $this->expectHttpRequest($expected_url,
                             RequestMethod::HEAD,
                             $request_headers,
                             null,
                             $response);
    $this->expectIsWritableMemcacheLookup(true, false);

    // RPCs for the second stat call to a different object
    $expected_url = $this->makeCloudStorageObjectUrl("bucket2", "object.png");
    $this->expectGetAccessTokenRequest(CloudStorageClient::READ_SCOPE);
    $this->expectHttpRequest($expected_url,
                             RequestMethod::HEAD,
                             $request_headers,
                             null,
                             $response);
    $this->expectIsWritableMemcacheLookup(true, false);

    // RPCs for the first object again, which will be called as we cleared the
    // cache
    $expected_url = $this->makeCloudStorageObjectUrl("bucket", "object.png");
    $this->expectGetAccessTokenRequest(CloudStorageClient::READ_SCOPE);
    $this->expectHttpRequest($expected_url,
                             RequestMethod::HEAD,
                             $request_headers,
                             null,
                             $response);
    $this->expectIsWritableMemcacheLookup(true, false);

    $this->assertTrue(is_file("gs://bucket/object.png"));

    // Stat the second object to clear the internal stat cache in PHP
    $this->assertTrue(is_file("gs://bucket2/object.png"));

    // Clear the cache and stat the object again.
    CloudStorageClient::clearStatcache("gs://bucket/object.png");
    $this->assertTrue(is_file("gs://bucket/object.png"));

    clearstatcache();
    $this->assertTrue(is_file("gs://bucket2/object.png"));

    $this->apiProxyMock->verify();
  }

  public function testStatDirectoryWithFolderSuffixSuccess() {
    $this->expectGetAccessTokenRequest(CloudStorageClient::READ_SCOPE);
    $request_headers = $this->getStandardRequestHeaders();
    $this->expectHeadMissingObject("bucket", "a/b", $request_headers);

    // Return the folder we want in the second request so we test fetching from
    // the marker to get all of the results
    $file_results = ['a/b/file1.txt'];
    $response = [
        'status_code' => 200,
        'headers' => [],
        'body' => $this->makeGetBucketXmlResponse('a/b',
                                                  $file_results,
                                                  'foo'),
    ];
    $expected_url = $this->makeCloudStorageObjectUrl('bucket', null);
    $expected_query = http_build_query([
        'delimiter' => CloudStorageClient::DELIMITER,
        'max-keys' => CloudStorageUrlStatClient::MAX_KEYS,
        'prefix' => 'a/b',
    ]);
    $this->expectGetAccessTokenRequest(CloudStorageClient::READ_SCOPE);
    $this->expectHttpRequest(sprintf("%s?%s", $expected_url, $expected_query),
                             RequestMethod::GET,
                             $request_headers,
                             null,
                             $response);

    // Second list bucket request with marker.
    $file_results = ['a/b/file2.txt', 'a/b_$folder$'];
    $response = [
        'status_code' => 200,
        'headers' => [],
        'body' => $this->makeGetBucketXmlResponse('a/b',
                                                  $file_results,
                                                  null),
    ];
    $expected_url = $this->makeCloudStorageObjectUrl('bucket', null);
    $expected_query = http_build_query([
        'delimiter' => CloudStorageClient::DELIMITER,
        'max-keys' => CloudStorageUrlStatClient::MAX_KEYS,
        'prefix' => 'a/b',
        'marker' => 'foo',
    ]);
    $this->expectGetAccessTokenRequest(CloudStorageClient::READ_SCOPE);
    $this->expectHttpRequest(sprintf("%s?%s", $expected_url, $expected_query),
                             RequestMethod::GET,
                             $request_headers,
                             null,
                             $response);

    // Return a false is writable check from the cache
    $this->expectIsWritableMemcacheLookup(true, false);

    $this->assertTrue(is_dir('gs://bucket/a/b/'));
    $this->apiProxyMock->verify();
  }

  public function testStatDirectoryInCommonPrefixSuccess() {
    $this->expectGetAccessTokenRequest(CloudStorageClient::READ_SCOPE);
    $request_headers = $this->getStandardRequestHeaders();
    $this->expectHeadMissingObject("bucket", "a/b", $request_headers);

    $common_prefix_results = [
        'a/b/',
        'a/bc/',
    ];
    $response = [
        'status_code' => 200,
        'headers' => [],
        'body' => $this->makeGetBucketXmlResponse('a/b',
                                                  [],
                                                  null,
                                                  $common_prefix_results),
    ];
    $expected_url = $this->makeCloudStorageObjectUrl('bucket', null);
    $expected_query = http_build_query([
        'delimiter' => CloudStorageClient::DELIMITER,
        'max-keys' => CloudStorageUrlStatClient::MAX_KEYS,
        'prefix' => 'a/b',
    ]);
    $this->expectGetAccessTokenRequest(CloudStorageClient::READ_SCOPE);
    $this->expectHttpRequest(sprintf("%s?%s", $expected_url, $expected_query),
                             RequestMethod::GET,
                             $request_headers,
                             null,
                             $response);
    // Return a false is writable check from the cache
    $this->expectIsWritableMemcacheLookup(true, false);

    $this->assertTrue(is_dir('gs://bucket/a/b'));
    $this->apiProxyMock->verify();
  }

  public function testStatDirectoryFailed() {
    $this->expectGetAccessTokenRequest(CloudStorageClient::READ_SCOPE);
    $request_headers = $this->getStandardRequestHeaders();
    $this->expectHeadMissingObject("bucket", "a/b", $request_headers);

    $file_results = [
        'a/be_$folder$',
        'a/bf.txt',
    ];
    $common_prefix_results = [
        'a/bc/',
        'a/bd/',
    ];
    $response = [
        'status_code' => 200,
        'headers' => [],
        'body' => $this->makeGetBucketXmlResponse('a/b',
                                                  $file_results,
                                                  null,
                                                  $common_prefix_results),
    ];
    $expected_url = $this->makeCloudStorageObjectUrl('bucket', null);
    $expected_query = http_build_query([
        'delimiter' => CloudStorageClient::DELIMITER,
        'max-keys' => CloudStorageUrlStatClient::MAX_KEYS,
        'prefix' => 'a/b',
    ]);
    $this->expectGetAccessTokenRequest(CloudStorageClient::READ_SCOPE);
    $this->expectHttpRequest(sprintf("%s?%s", $expected_url, $expected_query),
                             RequestMethod::GET,
                             $request_headers,
                             null,
                             $response);

    // is_dir() should return false for non-existing path.
    $this->assertFalse(is_dir('gs://bucket/a/b'));
    $this->apiProxyMock->verify();
  }

  public function testStatObjectFailed() {
    $this->expectGetAccessTokenRequest(CloudStorageClient::READ_SCOPE);
    $request_headers = $this->getStandardRequestHeaders();
    $this->expectHeadMissingObject("bucket", "object.png", $request_headers);

    // Return 404 for non existing bucket.
    $response = [
        'status_code' => 404,
        'headers' => [
        ],
    ];
    $expected_url = $this->makeCloudStorageObjectUrl("bucket", null);
    $expected_query = http_build_query([
        'delimiter' => CloudStorageClient::DELIMITER,
        'max-keys' => CloudStorageUrlStatClient::MAX_KEYS,
        'prefix' => 'object.png',
    ]);
    $this->expectGetAccessTokenRequest(CloudStorageClient::READ_SCOPE);
    $this->expectHttpRequest(sprintf("%s?%s", $expected_url, $expected_query),
                             RequestMethod::GET,
                             $request_headers,
                             null,
                             $response);

    $this->setErrorHandler();
    $result = stat("gs://bucket/object.png");
    $this->apiProxyMock->verify();
    $this->assertEquals(
        [["errno" => E_USER_WARNING,
          "errstr" => "Cloud Storage Error: NOT FOUND"],
         ["errno" => E_WARNING,
          "errstr" => "stat(): stat failed for gs://bucket/object.png"]],
        $this->triggered_errors);
  }

  private function expectHeadMissingObject($bucket, $object, $request_headers) {
    $response = [
        'status_code' => 404,
        'headers' => [],
        'body' => '',
    ];
    $expected_url = $this->makeCloudStorageObjectUrl($bucket, $object);
    $this->expectHttpRequest($expected_url,
                             RequestMethod::HEAD,
                             $request_headers,
                             null,
                             $response);
  }

  public function testRenameInvalidToPath() {
    $this->setErrorHandler();
    $this->assertFalse(rename("gs://bucket/object.png", "gs://to/"));
    $this->assertEquals(
        [["errno" => E_USER_ERROR,
          "errstr" => "Invalid cloud storage bucket name 'to'"],
         ["errno" => E_USER_ERROR,
          "errstr" => "Invalid Google Cloud Storage path: gs://to/"]],
        $this->triggered_errors);
  }

  public function testRenameInvalidFromPath() {
    $this->setExpectedException(
        "PHPUnit_Framework_Error",
        "Invalid Google Cloud Storage path: gs://bucket/");
    $this->assertFalse(rename("gs://bucket/", "gs://to/object.png"));
  }

  public function testRenameObjectWithoutContextSuccess() {
    $this->expectGetAccessTokenRequest(CloudStorageClient::WRITE_SCOPE);

    // First there is a stat
    $request_headers = $this->getStandardRequestHeaders();
    $response = [
        'status_code' => 200,
        'headers' => [
            'x-goog-stored-content-length' => 37337,
            'ETag' => 'abcdef',
            'Content-Type' => 'text/plain',
        ],
    ];

    $expected_url = $this->makeCloudStorageObjectUrl();
    $this->expectHttpRequest($expected_url,
                             RequestMethod::HEAD,
                             $request_headers,
                             null,
                             $response);

    // Then there is a copy
    $request_headers = [
        "Authorization" => "OAuth foo token",
        "x-goog-copy-source" => '/bucket/object.png',
        "x-goog-copy-source-if-match" => 'abcdef',
        "x-goog-metadata-directive" => "COPY",
        "x-goog-api-version" => 2,
    ];
    $response = [
        'status_code' => 200,
        'headers' => [
        ]
    ];
    $expected_url = $this->makeCloudStorageObjectUrl("to_bucket", "/to.png");
    $this->expectHttpRequest($expected_url,
                             RequestMethod::PUT,
                             $request_headers,
                             null,
                             $response);

    // Then we unlink the original.
    $request_headers = $this->getStandardRequestHeaders();
    $response = [
        'status_code' => 204,
        'headers' => [
        ],
    ];
    $expected_url = $this->makeCloudStorageObjectUrl();
    $this->expectHttpRequest($expected_url,
                             RequestMethod::DELETE,
                             $request_headers,
                             null,
                             $response);

    $from = "gs://bucket/object.png";
    $to = "gs://to_bucket/to.png";

    // Simulate the rename is acting on a uploaded file which is then being
    // moved into the allowed include bucket which will trigger a warning.
    $_FILES['foo']['tmp_name'] = $from;

    $this->setErrorHandler();
    $this->assertTrue(rename($from, $to));
    $this->apiProxyMock->verify();

    $this->assertEquals(
      [['errno' => E_USER_WARNING,
        'errstr' => sprintf('Moving uploaded file (%s) to an allowed include ' .
                            'bucket (%s) which may be vulnerable to local ' .
                            'file inclusion (LFI).', $from, 'to_bucket')]],
      $this->triggered_errors);

    $_FILES = [];
  }

  public function testRenameObjectWithContextSuccess() {
    $this->expectGetAccessTokenRequest(CloudStorageClient::WRITE_SCOPE);

    // First there is a stat
    $request_headers = $this->getStandardRequestHeaders();
    $response = [
        'status_code' => 200,
        'headers' => [
            'x-goog-stored-content-length' => 37337,
            'ETag' => 'abcdef',
            // Ensure the pre-existing headers are preserved.
            'Cache-Control' => 'public, max-age=6000',
            'Content-Disposition' => 'attachment; filename=object.png',
            'Content-Encoding' => 'text/plain',
            'Content-Language' => 'en',
            // Ensure context overrides original.
            'Content-Type' => 'text/plain',
        ],
    ];

    $expected_url = $this->makeCloudStorageObjectUrl();
    $this->expectHttpRequest($expected_url,
                             RequestMethod::HEAD,
                             $request_headers,
                             null,
                             $response);

    // Then there is a copy with new context
    $request_headers = [
        "Authorization" => "OAuth foo token",
        "x-goog-copy-source" => "/bucket/object.png",
        "x-goog-copy-source-if-match" => "abcdef",
        "x-goog-metadata-directive" => "REPLACE",
        "Cache-Control" => "public, max-age=6000",
        "Content-Disposition" => "attachment; filename=object.png",
        "Content-Encoding" => "text/plain",
        "Content-Language" => "en",
        "Content-Type" => "image/png",
        "x-goog-meta-foo" => "bar",
        "x-goog-acl" => "public-read-write",
        "x-goog-api-version" => 2,
    ];
    $response = [
        'status_code' => 200,
        'headers' => [
        ]
    ];
    $expected_url = $this->makeCloudStorageObjectUrl("to_bucket", "/to.png");
    $this->expectHttpRequest($expected_url,
                             RequestMethod::PUT,
                             $request_headers,
                             null,
                             $response);

    // Then we unlink the original.
    $request_headers = $this->getStandardRequestHeaders();
    $response = [
        'status_code' => 204,
        'headers' => [
        ],
    ];
    $expected_url = $this->makeCloudStorageObjectUrl();
    $this->expectHttpRequest($expected_url,
                             RequestMethod::DELETE,
                             $request_headers,
                             null,
                             $response);

    $from = "gs://bucket/object.png";
    $to = "gs://to_bucket/to.png";
    $ctx = stream_context_create([
        "gs" => ["Content-Type" => "image/png",
                 "acl" => "public-read-write",
                 "metadata" => ["foo"=> "bar"]]]);

    $this->assertTrue(rename($from, $to, $ctx));
    $this->apiProxyMock->verify();
  }

  public function testRenameObjectWithContextAllMetaSuccess() {
    $this->expectGetAccessTokenRequest(CloudStorageClient::WRITE_SCOPE);

    // First there is a stat.
    $request_headers = $this->getStandardRequestHeaders();
    $response = [
        'status_code' => 200,
        'headers' => [
            'x-goog-stored-content-length' => 37337,
            'ETag' => 'abcdef',
            // Ensure context overrides original values.
            'Cache-Control' => 'public, max-age=6000',
            'Content-Disposition' => 'attachment; filename=object.png',
            'Content-Encoding' => 'text/plain',
            'Content-Language' => 'en',
            'Content-Type' => 'text/plain',
        ],
    ];

    $expected_url = $this->makeCloudStorageObjectUrl();
    $this->expectHttpRequest($expected_url,
                             RequestMethod::HEAD,
                             $request_headers,
                             null,
                             $response);

    // Then there is a copy with new context.
    $request_headers = [
        "Authorization" => "OAuth foo token",
        "x-goog-copy-source" => "/bucket/object.png",
        "x-goog-copy-source-if-match" => "abcdef",
        "x-goog-metadata-directive" => "REPLACE",
        // All meta heads have had a 2 appended to check that context overrides.
        "Cache-Control" => "public, max-age=6002",
        "Content-Disposition" => "attachment; filename=object.png2",
        "Content-Encoding" => "text/plain2",
        "Content-Language" => "en2",
        "Content-Type" => "image/png2",
        "x-goog-meta-foo" => "bar",
        "x-goog-acl" => "public-read-write",
        "x-goog-api-version" => 2,
    ];
    $response = [
        'status_code' => 200,
        'headers' => [
        ]
    ];
    $expected_url = $this->makeCloudStorageObjectUrl("to_bucket", "/to.png");
    $this->expectHttpRequest($expected_url,
                             RequestMethod::PUT,
                             $request_headers,
                             null,
                             $response);

    // Then we unlink the original.
    $request_headers = $this->getStandardRequestHeaders();
    $response = [
        'status_code' => 204,
        'headers' => [
        ],
    ];
    $expected_url = $this->makeCloudStorageObjectUrl();
    $this->expectHttpRequest($expected_url,
                             RequestMethod::DELETE,
                             $request_headers,
                             null,
                             $response);

    $from = "gs://bucket/object.png";
    $to = "gs://to_bucket/to.png";
    $ctx = stream_context_create([
      "gs" => [
        "acl" => "public-read-write",
        "metadata" => ["foo"=> "bar"],
        // Metadata heads to override.
        "Cache-Control" => "public, max-age=6002",
        "Content-Disposition" => "attachment; filename=object.png2",
        "Content-Encoding" => "text/plain2",
        "Content-Language" => "en2",
        "Content-Type" => "image/png2",
      ],
    ]);

    $this->assertTrue(rename($from, $to, $ctx));
    $this->apiProxyMock->verify();
  }

  public function testRenameObjectFromObjectNotFound() {
    $this->expectGetAccessTokenRequest(CloudStorageClient::WRITE_SCOPE);

    // First there is a stat
    $request_headers = $this->getStandardRequestHeaders();
    $response = [
        'status_code' => 404,
        'headers' => [
        ],
    ];

    $expected_url = $this->makeCloudStorageObjectUrl();
    $this->expectHttpRequest($expected_url,
                             RequestMethod::HEAD,
                             $request_headers,
                             null,
                             $response);

    $from = "gs://bucket/object.png";
    $to = "gs://to_bucket/to_object";
    $this->setErrorHandler();
    $this->assertFalse(rename($from, $to));
    $this->apiProxyMock->verify();
    $this->assertEquals(
        [["errno" => E_USER_WARNING,
          "errstr" => "Unable to rename: gs://to_bucket/to_object. " .
                      "Cloud Storage Error: NOT FOUND"]],
        $this->triggered_errors);
  }

  public function testRenameObjectCopyFailed() {
    $this->expectGetAccessTokenRequest(CloudStorageClient::WRITE_SCOPE);

    // First there is a stat
    $request_headers = $this->getStandardRequestHeaders();
    $response = [
        'status_code' => 200,
        'headers' => [
            'x-goog-stored-content-length' => 37337,
            'ETag' => 'abcdef',
            'Content-Type' => 'text/plain',
        ],
    ];

    $expected_url = $this->makeCloudStorageObjectUrl();
    $this->expectHttpRequest($expected_url,
                             RequestMethod::HEAD,
                             $request_headers,
                             null,
                             $response);

    // Then there is a copy
    $request_headers = [
        "Authorization" => "OAuth foo token",
        "x-goog-copy-source" => '/bucket/object.png',
        "x-goog-copy-source-if-match" => 'abcdef',
        "x-goog-metadata-directive" => "COPY",
        "x-goog-api-version" => 2,
    ];
    $response = [
        'status_code' => 412,
        'headers' => [
        ]
    ];
    $expected_url = $this->makeCloudStorageObjectUrl("to_bucket", "/to_object");
    $this->expectHttpRequest($expected_url,
                             RequestMethod::PUT,
                             $request_headers,
                             null,
                             $response);

    $from = "gs://bucket/object.png";
    $to = "gs://to_bucket/to_object";
    $this->setErrorHandler();
    $this->assertFalse(rename($from, $to));
    $this->apiProxyMock->verify();
    $this->assertEquals(
        [["errno" => E_USER_WARNING,
          "errstr" => "Error copying to gs://to_bucket/to_object. " .
                      "Cloud Storage Error: PRECONDITION FAILED"]],
        $this->triggered_errors);
  }

  public function testRenameObjectUnlinkFailed() {
    $this->expectGetAccessTokenRequest(CloudStorageClient::WRITE_SCOPE);

    // First there is a stat
    $request_headers = $this->getStandardRequestHeaders();
    $response = [
        'status_code' => 200,
        'headers' => [
            'x-googl-stored-content-length' => 37337,
            'ETag' => 'abcdef',
            'Content-Type' => 'text/plain',
        ],
    ];

    $expected_url = $this->makeCloudStorageObjectUrl();
    $this->expectHttpRequest($expected_url,
                             RequestMethod::HEAD,
                             $request_headers,
                             null,
                             $response);

    // Then there is a copy
    $request_headers = [
        "Authorization" => "OAuth foo token",
        "x-goog-copy-source" => '/bucket/object.png',
        "x-goog-copy-source-if-match" => 'abcdef',
        "x-goog-metadata-directive" => "COPY",
        "x-goog-api-version" => 2,
    ];
    $response = [
        'status_code' => 200,
        'headers' => [
        ]
    ];
    $expected_url = $this->makeCloudStorageObjectUrl("to_bucket",
                                                     "/to_object");
    $this->expectHttpRequest($expected_url,
                             RequestMethod::PUT,
                             $request_headers,
                             null,
                             $response);

    // Then we unlink the original.
    $request_headers = $this->getStandardRequestHeaders();
    $response = [
        'status_code' => 404,
        'headers' => [
        ],
    ];
    $expected_url = $this->makeCloudStorageObjectUrl();
    $this->expectHttpRequest($expected_url,
                             RequestMethod::DELETE,
                             $request_headers,
                             null,
                             $response);

    $from = "gs://bucket/object.png";
    $to = "gs://to_bucket/to_object";

    $this->setErrorHandler();
    $this->assertFalse(rename($from, $to));
    $this->apiProxyMock->verify();
    $this->assertEquals(
        [["errno" => E_USER_WARNING,
          "errstr" => "Unable to unlink: gs://bucket/object.png. " .
                      "Cloud Storage Error: NOT FOUND"]],
         $this->triggered_errors);
  }

  public function testWriteObjectSuccess() {
    $this->writeObjectSuccessWithMetadata("Hello To PHP.");
  }

  public function testWriteObjectWithMetadata() {
    $metadata = ["foo" => "far", "bar" => "boo"];
    $this->writeObjectSuccessWithMetadata("Goodbye To PHP.", $metadata);
  }

  public function testWriteObjectWithAllMetadataHeaders() {
    $metadata = ['foo' => 'far', 'bar' => 'boo'];
    $headers = [
      'Cache-Control' => 'public, max-age=6000',
      'Content-Disposition' => 'attachment; filename=object.png',
      'Content-Encoding' => 'text/plain',
      'Content-Language' => 'en',
    ];
    $this->writeObjectSuccessWithMetadata("some text.", $metadata, $headers);
  }

  private function writeObjectSuccessWithMetadata($data,
                                                  array $metadata = null,
                                                  array $headers = []) {
    $data_len = strlen($data);
    $expected_url = $this->makeCloudStorageObjectUrl();
    $this->expectFileWriteStartRequest("text/plain",
                                       "public-read",
                                       "foo_upload_id",
                                       $expected_url,
                                       $metadata,
                                       $headers);

    $this->expectFileWriteContentRequest($expected_url,
                                         "foo_upload_id",
                                         $data,
                                         0,
                                         $data_len - 1,
                                         true);
    $context = [
        "gs" => [
            "acl" => "public-read",
            "Content-Type" => "text/plain",
            'enable_cache' => true,
        ] + $headers,
    ];
    if (isset($metadata)) {
      $context["gs"]["metadata"] = $metadata;
    }

    $range = sprintf("bytes=0-%d", CloudStorageClient::DEFAULT_READ_SIZE - 1);
    $cache_key = CloudStorageClient::getReadMemcacheKey($expected_url,
                                                        $range);
    $this->mock_memcached->expects($this->once())
                         ->method('deleteMulti')
                         ->with($this->identicalTo([$cache_key]));

    stream_context_set_default($context);
    $this->assertEquals($data_len,
        file_put_contents("gs://bucket/object.png", $data));
    $this->apiProxyMock->verify();
  }

  public function testWriteInvalidMetadata() {
    $metadata = ["f o o" => "far"];
    $context = [
        "gs" => [
            "acl" => "public-read",
            "Content-Type" => "text/plain",
            "metadata" => $metadata
        ],
    ];
    stream_context_set_default($context);
    $this->expectGetAccessTokenRequest(CloudStorageClient::WRITE_SCOPE);
    $this->setErrorHandler();
    file_put_contents("gs://bucket/object.png", "Some data");
    $this->apiProxyMock->verify();
    $this->assertEquals(
        ["errno" => E_USER_WARNING,
         "errstr" => "Invalid metadata key: f o o"],
        $this->triggered_errors[0]);
  }

  /**
   * @dataProvider supportedStreamReadModes
   */
  public function testReadMetaDataAndContentTypeInReadMode($mode) {
    $metadata = ["foo" => "far", "bar" => "boo"];
    $this->expectFileReadRequest([
        'body' => "Test data",
        'start_byte'=> 0,
        'length' => CloudStorageReadClient::DEFAULT_READ_SIZE,
        'metadata' => $metadata,
        'content-type' => "image/png"
    ]);

    $stream = new CloudStorageStreamWrapper();
    $this->assertTrue($stream->stream_open("gs://bucket/object_name.png",
                                           $mode,
                                           0,
                                           $unused));

    $this->assertEquals($metadata, $stream->getMetaData());
    $this->assertEquals("image/png", $stream->getContentType());
  }

  /**
   * @dataProvider supportedStreamWriteModes
   */
  public function testReadMetaDataAndContentTypeInWriteMode($mode) {
    $metadata = ["foo" => "far", "bar" => "boo"];
    $headers = [
      "Cache-Control" => "public, max-age=6000",
      "Content-Disposition" => "attachment; filename=object.png",
      "Content-Encoding" => "text/plain",
      "Content-Language" => "en",
      "Content-Type" => "image/png",
    ];

    $expected_url = $this->makeCloudStorageObjectUrl();
    $this->expectFileWriteStartRequest("image/png",
                                       "public-read",
                                       "foo_upload_id",
                                       $expected_url,
                                       $metadata,
                                       $headers);

    $context = [
        "gs" => [
            "acl" => "public-read",
            "Content-Type" => "image/png",
            "metadata" => $metadata
        ],
    ];
    stream_context_set_default($context);

    $stream = new CloudStorageStreamWrapper();
    $this->assertTrue($stream->stream_open("gs://bucket/object.png",
                                           $mode,
                                           0,
                                           $unused));

    $this->assertEquals($metadata, $stream->getMetaData());
    $this->assertEquals("image/png", $stream->getContentType());
  }

  /**
   * DataProvider for
   * - testReadMetaDataAndContentTypeInReadMode
   */
  public function supportedStreamReadModes() {
    return [["r"], ["rt"], ["rb"]];
  }

  /**
   * DataProvider for
   * - testReadMetaDataAndContentTypeInWriteMode
   */
  public function supportedStreamWriteModes() {
    return [["w"], ["wt"], ["wb"]];
  }

  public function testWriteLargeObjectSuccess() {
    $data_to_write = str_repeat("1234567890", 100000);
    $data_len = strlen($data_to_write);

    $expected_url = $this->makeCloudStorageObjectUrl();

    $this->expectFileWriteStartRequest("text/plain",
                                       "public-read",
                                       "foo_upload_id",
                                       $expected_url);

    $chunks = floor($data_len / CloudStorageWriteClient::WRITE_CHUNK_SIZE);
    $start_byte = 0;
    $end_byte = CloudStorageWriteClient::WRITE_CHUNK_SIZE - 1;

    for ($i = 0 ; $i < $chunks ; $i++) {
      $this->expectFileWriteContentRequest($expected_url,
                                           "foo_upload_id",
                                           $data_to_write,
                                           $start_byte,
                                           $end_byte,
                                           false);
      $start_byte += CloudStorageWriteClient::WRITE_CHUNK_SIZE;
      $end_byte += CloudStorageWriteClient::WRITE_CHUNK_SIZE;
    }

    // Write out the remainder
    $this->expectFileWriteContentRequest($expected_url,
                                         "foo_upload_id",
                                         $data_to_write,
                                         $start_byte,
                                         $data_len - 1,
                                         true);

    $file_context = [
        "gs" => [
            "acl" => "public-read",
            "Content-Type" => "text/plain",
            'enable_cache' => true,
        ],
    ];

    $delete_keys = [];
    for ($i = 0; $i < $data_len; $i += CloudStorageClient::DEFAULT_READ_SIZE) {
      $range = sprintf("bytes=%d-%d",
                       $i,
                       $i + CloudStorageClient::DEFAULT_READ_SIZE - 1);
      $delete_keys[] = CloudStorageClient::getReadMemcacheKey($expected_url,
                                                              $range);
    }
    $this->mock_memcached->expects($this->once())
                         ->method('deleteMulti')
                         ->with($this->identicalTo($delete_keys));

    $ctx = stream_context_create($file_context);
    $this->assertEquals($data_len,
                        file_put_contents("gs://bucket/object.png",
                                          $data_to_write,
                                          0,
                                          $ctx));
    $this->apiProxyMock->verify();
  }

  public function testWriteEmptyObjectSuccess() {
    $data_to_write = "";
    $data_len = 0;

    $expected_url = $this->makeCloudStorageObjectUrl("bucket",
                                                     "/empty_file.txt");

    $this->expectFileWriteStartRequest("text/plain",
                                       "public-read",
                                       "foo_upload_id",
                                       $expected_url);

    $this->expectFileWriteContentRequest($expected_url,
                                         "foo_upload_id",
                                         $data_to_write,
                                         null,  // start_byte
                                         0,  // write_length
                                         true);  // Complete write

    $file_context = [
        "gs" => [
            "acl" => "public-read",
            "Content-Type" => "text/plain",
        ],
    ];
    $ctx = stream_context_create($file_context);
    $fp = fopen("gs://bucket/empty_file.txt", "wt", false, $ctx);
    $this->assertEquals($data_len, fwrite($fp, $data_to_write));
    fclose($fp);
    $this->apiProxyMock->verify();
  }

  public function testInvalidBucketForInclude() {
    // Uses GAE_INCLUDE_GS_BUCKETS, which is not defined.
    stream_wrapper_unregister("gs");
    stream_wrapper_register("gs",
        "\\google\\appengine\\ext\\cloud_storage_streams\\CloudStorageStreamWrapper",
        0);

    $this->setErrorHandler();
    include 'gs://unknownbucket/object.php';

    $this->assertEquals(E_WARNING, $this->triggered_errors[0]["errno"]);
    $this->assertStringStartsWith(
        "include(gs://unknownbucket/object.php): failed to open stream:",
        $this->triggered_errors[0]["errstr"]);
    $this->assertEquals(E_WARNING, $this->triggered_errors[1]["errno"]);
    $this->assertStringStartsWith(
        "include(): Failed opening 'gs://unknownbucket/object.php'",
        $this->triggered_errors[1]["errstr"]);
  }

  public function testValidBucketForInclude() {
    stream_wrapper_unregister("gs");
    stream_wrapper_register("gs",
        "\\google\\appengine\\ext\\cloud_storage_streams\\CloudStorageStreamWrapper",
        0);

    $body = '<?php $a = "foo";';
    $this->expectFileReadRequest([
        'body' =>$body,
        'start_byte' => 0,
        'length' => CloudStorageReadClient::DEFAULT_READ_SIZE,
    ]);

    $valid_path = "gs://bucket/object_name.png";
    require $valid_path;

    $this->assertEquals($a, 'foo');
    $this->apiProxyMock->verify();
  }

  public function testInvalidDirectoryForInclude() {
    // Uses GAE_INCLUDE_GS_BUCKETS, which is not defined.
    stream_wrapper_unregister('gs');
    stream_wrapper_register('gs',
        '\\google\\appengine\\ext\\cloud_storage_streams\\' .
        'CloudStorageStreamWrapper',
        0);

    $this->setErrorHandler();
    include 'gs://baz/foo/object.php';

    $this->assertEquals(E_WARNING, $this->triggered_errors[0]["errno"]);
    $this->assertStringStartsWith(
        'include(gs://baz/foo/object.php): failed to open stream:',
        $this->triggered_errors[0]["errstr"]);
    $this->assertEquals(E_WARNING, $this->triggered_errors[1]["errno"]);
    $this->assertStringStartsWith(
        "include(): Failed opening 'gs://baz/foo/object.php'",
        $this->triggered_errors[1]["errstr"]);
  }

  /**
   * DataProvider for
   * - testOpenDirInvalidPath
   */
  public function invalidRootDirPath() {
    return [["gs://"], ["gs:///"]];
  }

  /**
   * DataProvider for
   * - testReadRootDirSuccess
   */
  public function validRootDirPath() {
    return [["gs://bucket"], ["gs://bucket/"]];
  }

  /**
   * @dataProvider invalidRootDirPath
   */
  public function testOpenDirInvalidPath($path) {
    $this->setErrorHandler();
    $this->assertFalse(opendir($path));
    $this->assertEquals(
        ["errno" => E_USER_ERROR,
         "errstr" => "Invalid Google Cloud Storage path: $path"],
        $this->triggered_errors[0]);
  }

  /**
   * @dataProvider validRootDirPath
   */
  public function testReadRootDirSuccess($path) {
    $this->expectGetAccessTokenRequest(CloudStorageClient::READ_SCOPE);

    $request_headers = $this->getStandardRequestHeaders();
    $file_results = ['file1.txt', 'file2.txt', 'file3.txt' ];
    $common_prefixes_results = ['dir/'];
    $response = [
        'status_code' => 200,
        'headers' => [
        ],
        'body' => $this->makeGetBucketXmlResponse(
            "",
            $file_results,
            null,
            $common_prefixes_results),
    ];
    $expected_url = $this->makeCloudStorageObjectUrl("bucket", null);
    $expected_query = http_build_query([
        "delimiter" => CloudStorageDirectoryClient::DELIMITER,
        "max-keys" => CloudStorageDirectoryClient::MAX_KEYS,
    ]);

    $this->expectHttpRequest(sprintf("%s?%s", $expected_url, $expected_query),
                             RequestMethod::GET,
                             $request_headers,
                             null,
                             $response);

    $res = opendir($path);
    $this->assertEquals("file1.txt", readdir($res));
    $this->assertEquals("file2.txt", readdir($res));
    $this->assertEquals("file3.txt", readdir($res));
    $this->assertEquals("dir/", readdir($res));
    $this->assertFalse(readdir($res));
    closedir($res);
    $this->apiProxyMock->verify();
  }

  public function testReadADirSuccess() {
    $this->expectGetAccessTokenRequest(CloudStorageClient::READ_SCOPE);

    $request_headers = $this->getStandardRequestHeaders();
    $file_results = ['f/file1.txt', 'f/file2.txt', 'f/', 'f_$folder$'];
    $common_prefixes_results = ['f/sub/'];
    $response = [
        'status_code' => 200,
        'headers' => [
        ],
        'body' => $this->makeGetBucketXmlResponse(
            "f/",
            $file_results,
            null,
            $common_prefixes_results),
    ];
    $expected_url = $this->makeCloudStorageObjectUrl("bucket", null);
    $expected_query = http_build_query([
        "delimiter" => CloudStorageDirectoryClient::DELIMITER,
        "max-keys" => CloudStorageDirectoryClient::MAX_KEYS,
        "prefix" => "f/",
    ]);

    $this->expectHttpRequest(sprintf("%s?%s", $expected_url, $expected_query),
                             RequestMethod::GET,
                             $request_headers,
                             null,
                             $response);

    $res = opendir("gs://bucket/f");
    $this->assertEquals("file1.txt", readdir($res));
    $this->assertEquals("file2.txt", readdir($res));
    $this->assertEquals("sub/", readdir($res));
    $this->assertFalse(readdir($res));
    closedir($res);
    $this->apiProxyMock->verify();
  }

  public function testReaddirTruncatedSuccess() {
    $this->expectGetAccessTokenRequest(CloudStorageClient::READ_SCOPE);
    $request_headers = $this->getStandardRequestHeaders();
    // First query with a truncated response
    $response_body = "<?xml version='1.0' encoding='UTF-8'?>
        <ListBucketResult xmlns='http://doc.s3.amazonaws.com/2006-03-01'>
        <Name>sjl-test</Name>
        <Prefix>f/</Prefix>
        <Marker></Marker>
        <NextMarker>AA</NextMarker>
        <Delimiter>/</Delimiter>
        <IsTruncated>true</IsTruncated>
        <Contents>
          <Key>f/file1.txt</Key>
        </Contents>
        <Contents>
          <Key>f/file2.txt</Key>
        </Contents>
        </ListBucketResult>";
    $response = [
        'status_code' => 200,
        'headers' => [
        ],
        'body' => $response_body,
    ];
    $expected_url = $this->makeCloudStorageObjectUrl("bucket", null);
    $expected_query = http_build_query([
        "delimiter" => CloudStorageDirectoryClient::DELIMITER,
        "max-keys" => CloudStorageDirectoryClient::MAX_KEYS,
        "prefix" => "f/",
    ]);

    $this->expectHttpRequest(sprintf("%s?%s", $expected_url, $expected_query),
                             RequestMethod::GET,
                             $request_headers,
                             null,
                             $response);

    // Second query with the remaining response
    $response_body = "<?xml version='1.0' encoding='UTF-8'?>
        <ListBucketResult xmlns='http://doc.s3.amazonaws.com/2006-03-01'>
        <Name>sjl-test</Name>
        <Prefix>f/</Prefix>
        <Marker>AA</Marker>
        <Delimiter>/</Delimiter>
        <IsTruncated>false</IsTruncated>
        <Contents>
          <Key>f/file3.txt</Key>
        </Contents>
        <Contents>
          <Key>f/file4.txt</Key>
        </Contents>
        </ListBucketResult>";
    $response = [
        'status_code' => 200,
        'headers' => [
        ],
        'body' => $response_body,
    ];

    $expected_query = http_build_query([
        "delimiter" => CloudStorageDirectoryClient::DELIMITER,
        "max-keys" => CloudStorageDirectoryClient::MAX_KEYS,
        "prefix" => "f/",
        "marker" => "AA",
    ]);

    $this->expectGetAccessTokenRequest(CloudStorageClient::READ_SCOPE);
    $this->expectHttpRequest(sprintf("%s?%s", $expected_url, $expected_query),
                             RequestMethod::GET,
                             $request_headers,
                             null,
                             $response);

    $res = opendir("gs://bucket/f");
    $this->assertEquals("file1.txt", readdir($res));
    $this->assertEquals("file2.txt", readdir($res));
    $this->assertEquals("file3.txt", readdir($res));
    $this->assertEquals("file4.txt", readdir($res));
    $this->assertFalse(readdir($res));
    closedir($res);
    $this->apiProxyMock->verify();
  }

  public function testRewindDirSuccess() {
    $this->expectGetAccessTokenRequest(CloudStorageClient::READ_SCOPE);
    $request_headers = $this->getStandardRequestHeaders();
    $response = [
        'status_code' => 200,
        'headers' => [
        ],
        'body' => $this->makeGetBucketXmlResponse(
            "f/",
            ["f/file1.txt", "f/file2.txt"]),
    ];
    $expected_url = $this->makeCloudStorageObjectUrl("bucket", null);
    $expected_query = http_build_query([
        "delimiter" => CloudStorageDirectoryClient::DELIMITER,
        "max-keys" => CloudStorageDirectoryClient::MAX_KEYS,
        "prefix" => "f/",
    ]);

    $this->expectHttpRequest(sprintf("%s?%s", $expected_url, $expected_query),
                             RequestMethod::GET,
                             $request_headers,
                             null,
                             $response);
    // Expect the requests again when we rewinddir
    $this->expectGetAccessTokenRequest(CloudStorageClient::READ_SCOPE);
    $this->expectHttpRequest(sprintf("%s?%s", $expected_url, $expected_query),
                             RequestMethod::GET,
                             $request_headers,
                             null,
                             $response);

    $res = opendir("gs://bucket/f");
    $this->assertEquals("file1.txt", readdir($res));
    rewinddir($res);
    $this->assertEquals("file1.txt", readdir($res));
    $this->assertEquals("file2.txt", readdir($res));
    $this->assertFalse(readdir($res));
    closedir($res);
    $this->apiProxyMock->verify();
  }

  /**
   * DataProvider for
   * - testMkDirInvalidPath
   * - testRmDirInvalidPath
   */
  public function invalidDirPath() {
    return [["gs://"], ["gs:///"], ["gs://bucket"], ["gs://bucket/"]];
  }

  /**
   * DataProvider for
   * - testMkDirSuccess
   * - testRmDirSuccess
   * - testRmDirNotEmpty
   */
  public function validDirPath() {
    // Each data set contains [gcs_path, bucket_name, object_name, prefix]
    return [["gs://bucket/dira/dirb/", "bucket", "/dira/dirb/", "dira/dirb/"],
            ["gs://bucket/dira/dirb", "bucket", "/dira/dirb/", "dira/dirb/"]];
  }

  /**
   * @dataProvider invalidDirPath
   */
  public function testMkInvalidPath($invalid_path) {
    $this->setErrorHandler();
    $this->assertFalse(mkdir($invalid_path));
    $this->assertEquals(
        [["errno" => E_USER_ERROR,
          "errstr" => "Invalid Google Cloud Storage path: $invalid_path"]],
        $this->triggered_errors);
  }

  /**
   * @dataProvider validDirPath
   */
  public function testMkDirSuccess($path, $bucket, $object, $prefix) {
    $this->expectGetAccessTokenRequest(CloudStorageClient::WRITE_SCOPE);
    $request_headers = [
        "Authorization" => "OAuth foo token",
        "x-goog-if-generation-match" => 0,
        "Content-Range" => "bytes */0",
        "x-goog-api-version" => 2,
    ];

    $response = [
        'status_code' => 200,
        'headers' => [
        ],
    ];

    $expected_url = $this->makeCloudStorageObjectUrl($bucket, $object);
    $this->expectHttpRequest($expected_url,
                             RequestMethod::PUT,
                             $request_headers,
                             null,
                             $response);

    $this->assertTrue(mkdir($path));
    $this->apiProxyMock->verify();
  }

  /**
   * @dataProvider invalidDirPath
   */
  public function testRmDirInvalidPath($path) {
    $this->setErrorHandler();
    $this->assertFalse(rmdir($path));
    $this->assertEquals(
        [["errno" => E_USER_ERROR,
          "errstr" => "Invalid Google Cloud Storage path: $path"]],
        $this->triggered_errors);
  }

  /**
   * @dataProvider validDirPath
   */
  public function testRmDirSuccess($path, $bucket, $object, $prefix) {
    // Expect a request to list the contents of the bucket to ensure that it is
    // empty.
    $this->expectGetAccessTokenRequest(CloudStorageClient::READ_SCOPE);
    $request_headers = $this->getStandardRequestHeaders();
    // First query with a truncated response
    $response = [
        'status_code' => 200,
        'headers' => [
        ],
        'body' => $this->makeGetBucketXmlResponse($prefix, []),
    ];
    $expected_url = $this->makeCloudStorageObjectUrl($bucket, null);
    $expected_query = http_build_query([
        "delimiter" => CloudStorageDirectoryClient::DELIMITER,
        "max-keys" => CloudStorageDirectoryClient::MAX_KEYS,
        "prefix" => $prefix,
    ]);

    $this->expectHttpRequest(sprintf("%s?%s", $expected_url, $expected_query),
                             RequestMethod::GET,
                             $request_headers,
                             null,
                             $response);

    // Expect the unlink request for the folder.
    $this->expectGetAccessTokenRequest(CloudStorageClient::WRITE_SCOPE);
    $request_headers = $this->getStandardRequestHeaders();
    $response = [
        'status_code' => 204,
        'headers' => [
        ],
    ];

    $expected_url = $this->makeCloudStorageObjectUrl($bucket, $object);
    $this->expectHttpRequest($expected_url,
                             RequestMethod::DELETE,
                             $request_headers,
                             null,
                             $response);

    $this->assertTrue(rmdir($path));
    $this->apiProxyMock->verify();
  }

  /**
   * @dataProvider validDirPath
   */
  public function testRmDirNotEmpty($path, $bucket, $object, $prefix) {
    // Expect a request to list the contents of the bucket to ensure that it is
    // empty.
    $this->expectGetAccessTokenRequest(CloudStorageClient::READ_SCOPE);
    $request_headers = $this->getStandardRequestHeaders();
    // First query with a truncated response
    $response = [
        'status_code' => 200,
        'headers' => [
        ],
        'body' => $this->makeGetBucketXmlResponse(
            $prefix,
            [$prefix . "file1.txt"]),
    ];
    $expected_url = $this->makeCloudStorageObjectUrl($bucket, null);
    $expected_query = http_build_query([
        "delimiter" => CloudStorageDirectoryClient::DELIMITER,
        "max-keys" => CloudStorageDirectoryClient::MAX_KEYS,
        "prefix" => $prefix,
    ]);

    $this->expectHttpRequest(sprintf("%s?%s", $expected_url, $expected_query),
                             RequestMethod::GET,
                             $request_headers,
                             null,
                             $response);

    $this->setErrorHandler();
    $this->assertFalse(rmdir($path));
    $this->apiProxyMock->verify();
    $this->assertEquals(
        [["errno" => E_USER_WARNING,
          "errstr" => "The directory is not empty."]],
        $this->triggered_errors);
  }

  public function testStreamCast() {
    $body = "Hello from PHP";

    $this->expectFileReadRequest([
        'body' =>$body,
        'start_byte' => 0,
        'length' => CloudStorageReadClient::DEFAULT_READ_SIZE,
    ]);

    $valid_path = "gs://bucket/object_name.png";
    $this->setErrorHandler();
    $this->assertFalse(gzopen($valid_path, 'rb'));
    $this->apiProxyMock->verify();
    $this->assertEquals(
        [["errno" => E_WARNING,
          "errstr" => "gzopen(): cannot represent a stream of type " .
                      "user-space as a File Descriptor"]],
        $this->triggered_errors);
  }

  public function testFlockReturnsFalse() {
    $this->expectFileReadRequest([
        'body' => "Hello world!",
        'start_byte' => 0,
        'length' => CloudStorageReadClient::DEFAULT_READ_SIZE,
    ]);

    $valid_path = "gs://bucket/object_name.png";
    $fp = fopen($valid_path, "r");
    $this->assertFalse(flock($fp, LOCK_EX));
    $this->assertTrue(fclose($fp));
    $this->apiProxyMock->verify();
  }

  private function expectFileReadRequest($options) {
    assert(isset($options['start_byte']));
    assert($options['length'] > 0);

    $this->expectGetAccessTokenRequest(CloudStorageClient::READ_SCOPE);

    $last_byte = $options['start_byte'] + $options['length'] - 1;
    $request_headers = [
        "Authorization" => "OAuth foo token",
        "Range" => sprintf("bytes=%d-%d", $options['start_byte'], $last_byte),
    ];

    if (isset($options['etag'])) {
      $request_headers['If-Match'] = $options['etag'];
    }

    $request_headers["x-goog-api-version"] = 2;

    $response_headers = [
        "ETag" => "deadbeef",
        "Last-Modified" => "Mon, 02 Jul 2012 01:41:01 GMT",
    ];

    if (isset($options['content-type'])) {
      $response_headers["Content-Type"] = $options['content-type'];
    } else {
      $response_headers["Content-Type"] = "binary/octet-stream";
    }

    if (isset($options['metadata'])) {
      foreach ($options['metadata'] as $key => $value) {
        $response_headers["x-goog-meta-" . $key] = $value;
      }
    }

    $paritial_content =
        isset($options['paritial_content']) ? $options['paritial_content'] :
                                              null;
    $response = $this->createSuccessfulGetHttpResponse($response_headers,
                                                       $options['body'],
                                                       $options['start_byte'],
                                                       $options['length'],
                                                       $paritial_content);

    $bucket = isset($options['bucket']) ? $options['bucket'] : "bucket";
    $object =
      isset($options['object']) ? $options['object'] : "/object_name.png";
    $exected_url = self::makeCloudStorageObjectUrl($bucket, $object);

    $this->expectHttpRequest($exected_url,
                             RequestMethod::GET,
                             $request_headers,
                             null,
                             $response);
  }

  private function expectGetAccessTokenRequest($scope) {
    $req = new \google\appengine\GetAccessTokenRequest();

    $req->addScope($scope);

    $resp = new \google\appengine\GetAccessTokenResponse();
    $resp->setAccessToken('foo token');
    $resp->setExpirationTime(12345);

    $this->apiProxyMock->expectCall('app_identity_service',
                                    'GetAccessToken',
                                    $req,
                                    $resp);

    $this->mock_memcache->expects($this->at($this->mock_memcache_call_index++))
                        ->method('get')
                        ->with($this->stringStartsWith('_ah_app_identity'))
                        ->will($this->returnValue(false));

    $this->mock_memcache->expects($this->at($this->mock_memcache_call_index++))
                        ->method('set')
                        ->with($this->stringStartsWith('_ah_app_identity'),
                               $this->anything(),
                               $this->anything(),
                               $this->anything())
                        ->will($this->returnValue(false));
  }

  private function createSuccessfulGetHttpResponse($headers,
                                                   $body,
                                                   $start_byte,
                                                   $length,
                                                   $return_partial_content) {
    $total_body_length = strlen($body);
    $partial_content = false;
    $range_cannot_be_satisfied = false;

    if ($total_body_length <= $start_byte) {
      $range_cannot_be_satisfied = true;
      $body = "<Message>The requested range cannot be satisfied.</Message>";
    } else {
      if ($start_byte != 0 || $length < $total_body_length) {
        $final_length = min($length, $total_body_length - $start_byte);
        $body = substr($body, $start_byte, $final_length);
        $partial_content = true;
      } else if ($return_partial_content) {
        $final_length = strlen($body);
        $partial_content = true;
      }
    }

    $success_headers = [];
    if ($range_cannot_be_satisfied) {
      $status_code = HttpResponse::RANGE_NOT_SATISFIABLE;
      $success_headers["Content-Length"] = $total_body_length;
    } else if (!$partial_content) {
      $status_code = HttpResponse::OK;
      $success_headers["Content-Length"] = $total_body_length;
    } else {
      $status_code = HttpResponse::PARTIAL_CONTENT;
      $end_range = $start_byte + $final_length - 1;
      $success_headers["Content-Length"] = $final_length;
      $success_headers["Content-Range"] = sprintf("bytes %d-%d/%d",
                                                  $start_byte,
                                                  $end_range,
                                                  $total_body_length);
    }

    return [
        'status_code' => $status_code,
        'headers' => array_merge($success_headers, $headers),
        'body' => $body,
    ];
  }

  private function expectFileWriteStartRequest($content_type,
                                               $acl,
                                               $id,
                                               $url,
                                               $metadata = NULL,
                                               array $headers = null) {
    $this->expectGetAccessTokenRequest(CloudStorageClient::WRITE_SCOPE);
    $upload_id =  "https://host/bucket/object.png?upload_id=" . $id;
    // The upload will start with a POST to acquire the upload ID.
    $request_headers = [
        "x-goog-resumable" => "start",
        "Authorization" => "OAuth foo token",
    ];
    if ($headers) {
      $request_headers += $headers;
    }
    if ($content_type != null) {
      $request_headers['Content-Type'] = $content_type;
    }
    if ($acl != null) {
      $request_headers['x-goog-acl'] = $acl;
    }
    if (isset($metadata)) {
      foreach ($metadata as $key => $value) {
        $request_headers["x-goog-meta-" . $key] = $value;
      }
    }
    $request_headers["x-goog-api-version"] = 2;
    $response = [
        'status_code' => 201,
        'headers' => [
            'Location' => $upload_id,
        ],
    ];
    $this->expectHttpRequest($url,
                             RequestMethod::POST,
                             $request_headers,
                             null,
                             $response);
  }

  private function expectFileWriteContentRequest($url,
                                                 $upload_id,
                                                 $data,
                                                 $start_byte,
                                                 $end_byte,
                                                 $complete) {
    // The upload will be completed with a PUT with the final length
    $this->expectGetAccessTokenRequest(CloudStorageClient::WRITE_SCOPE);
    // If start byte is null then we assume that this is a PUT with no content,
    // and the end_byte contains the length of the data to write.
    if (is_null($start_byte)) {
      $range = sprintf("bytes */%d", $end_byte);
      $status_code = HttpResponse::OK;
      $body = null;
    } else {
      $length = $end_byte - $start_byte + 1;
      if ($complete) {
        $total_len = $end_byte + 1;
        $range = sprintf("bytes %d-%d/%d", $start_byte, $end_byte, $total_len);
        $status_code = HttpResponse::OK;
      } else {
        $range = sprintf("bytes %d-%d/*", $start_byte, $end_byte);
        $status_code = HttpResponse::RESUME_INCOMPLETE;
      }
      $body = substr($data, $start_byte, $length);
    }
    $request_headers = [
        "Authorization" => "OAuth foo token",
        "Content-Range" => $range,
        "x-goog-api-version" => 2,
    ];
    $response = [
        'status_code' => $status_code,
        'headers' => [
        ],
    ];
    $expected_url = $url . "?upload_id=" . $upload_id;
    $this->expectHttpRequest($expected_url,
                             RequestMethod::PUT,
                             $request_headers,
                             $body,
                             $response);
  }

  private function expectHttpRequest($url, $method, $headers, $body, $result) {
    $req = new \google\appengine\URLFetchRequest();
    $req->setUrl($url);
    $req->setMethod($method);
    $req->setMustValidateServerCertificate(true);
    $req->setDeadline($this->deadline);
    $req->setFollowRedirects(false);

    foreach($headers as $k => $v) {
      $h = $req->addHeader();
      $h->setKey($k);
      $h->setValue($v);
    }

    if (isset($body)) {
      $req->setPayload($body);
    }

    if ($result instanceof \Exception) {
      $resp = $result;
    } else {
      $resp = new \google\appengine\URLFetchResponse();

      $resp->setStatusCode($result['status_code']);
      foreach($result['headers'] as $k => $v) {
        $h = $resp->addHeader();
        $h->setKey($k);
        $h->setValue($v);
      }
      if (isset($result['body'])) {
        $resp->setContent($result['body']);
      }
    }

    $this->apiProxyMock->expectCall('urlfetch',
                                    'Fetch',
                                    $req,
                                    $resp);
  }

  private function expectIsWritableMemcacheLookup($key_found, $result) {
    if ($key_found) {
      $lookup_result = ['is_writable' => $result];
    } else {
      $lookup_result = false;
    }

    $this->mock_memcache->expects($this->at($this->mock_memcache_call_index++))
                        ->method('get')
                        ->with($this->stringStartsWith(
                            '_ah_gs_write_bucket_cache_'))
                        ->will($this->returnValue($lookup_result));
  }

  private function expectIsWritableMemcacheSet($value) {
    $this->mock_memcache->expects($this->at($this->mock_memcache_call_index++))
        ->method('set')
        ->with($this->stringStartsWith('_ah_gs_write_bucket_cache_'),
               ['is_writable' => $value],
               null,
               CloudStorageClient::DEFAULT_WRITABLE_CACHE_EXPIRY_SECONDS)
        ->will($this->returnValue(false));
  }

  private function makeCloudStorageObjectUrl($bucket = "bucket",
                                             $object = "/object.png") {
    return CloudStorageClient::createObjectUrl($bucket, $object);
  }

  private function getStandardRequestHeaders() {
    return [
        "Authorization" => "OAuth foo token",
        "x-goog-api-version" => 2,
    ];
  }

  private function makeGetBucketXmlResponse($prefix,
                                            $contents_array,
                                            $next_marker = null,
                                            $common_prefix_array = null) {
    $result = "<?xml version='1.0' encoding='UTF-8'?>
        <ListBucketResult xmlns='http://doc.s3.amazonaws.com/2006-03-01'>
        <Name>sjl-test</Name>
        <Prefix>" . $prefix . "</Prefix>
        <Marker></Marker>";
    if (isset($next_marker)) {
      $result .= "<NextMarker>" . $next_marker . "</NextMarker>";
    }
    $result .= "<Delimiter>/</Delimiter>
        <IsTruncated>false</IsTruncated>";

    foreach($contents_array as $content) {
      $result .= '<Contents>';
      if (is_string($content)) {
        $result .= '<Key>' . $content . '</Key>';
      } else {
        $result .= '<Key>' . $content['key'] . '</Key>';
        $result .= '<Size>' . $content['size'] . '</Size>';
        $result .= '<LastModified>' . $content['mtime'] . '</LastModified>';
      }
      $result .= '</Contents>';
    }
    if (isset($common_prefix_array)) {
      foreach($common_prefix_array as $common_prefix) {
        $result .= '<CommonPrefixes>';
        $result .= '<Prefix>' . $common_prefix . '</Prefix>';
        $result .= '</CommonPrefixes>';
      }
    }
    $result .= "</ListBucketResult>";
    return $result;
  }
}

// TODO: b/13132830: Remove once feature releases.
/**
 * Gets the value of a configuration option.
 *
 * Override built-in ini_get() to fake INI value that would normally be provided
 * by gae extension, but is not on devappserver. INI will always be true during
 * these tests.
 *
 * - google_app_engine.enable_additional_cloud_storage_headers: true
 * - google_app_engine.enable_gcs_stat_cache: true
 *
 * @param string $varname
 *   The configuration option name.
 * @return mixed
 *   Returns the value of the configuration option as a string on success, or an
 *   empty string for null values. Returns FALSE if the configuration option
 *   doesn't exist.
 *
 * @see http://php.net/ini_get
 */
function ini_get($varname)  {
  if ($varname == 'google_app_engine.enable_additional_cloud_storage_headers') {
    return true;
  }
  if ($varname == 'google_app_engine.enable_gcs_stat_cache') {
    return true;
  }
  if ($varname == 'google_app_engine.gcs_default_keyword') {
    return true;
  }
  return \ini_get($varname);
}

}  // namespace google\appengine\ext\cloud_storage_streams;

