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
// @codingStandardsIgnoreFile
// File defines two classes and the funtion names are not CamelCase as they are
// following the streamWrapper interface.

namespace google\appengine\runtime;

use google\appengine\ext\remote_api\Request;
use google\appengine\ext\remote_api\Response;
use google\appengine\ext\remote_api\RpcError\ErrorCode;
use google\appengine\SignForAppRequest;
use google\appengine\SignForAppResponse;
use google\appengine\runtime\ApiProxy;
use google\appengine\runtime\ApplicationError;


class ValueUnexpectedException extends \Exception {
  public function __construct($expected, $actual) {
    $message = sprintf("Expected %s, Got: %s", $expected, $actual);
    parent::__construct($message);
  }
}

// Used to mock calls to file_get_contents('http://.....')
class MockHttpStream {

  public $context;

  // This is the associative array of data used for each stream call.
  private $stream_call_data;

  public function stream_close() {
    if ($this->stream_call_data) {
      $this->stream_call_data = null;
      return true;
    }
    return false;
  }

  public function stream_eof() {
    if ($this->stream_call_data) {
      $data = $this->stream_call_data['stream_read']['bytes'];
      if (strlen($data) > 0) {
        return false;
      }
    }
    return true;
  }

  public function stream_flush() {
    return false;
  }

  public function stream_open($path, $mode, $options, &$opened_path) {
    $this->stream_call_data = array_pop($GLOBALS['mock_http']);
    if ($this->stream_call_data === null) {
      throw new \Exception("Unexpected call to stream_open: args " .
                           print_r(func_get_args(), true));
    }

    $options = $this->stream_call_data['stream_open'];
    if ($options['address'] !== $path) {
      throw new ValueUnexpectedException($options['address'], $path);
    }
    if ($options['mode'] !== $mode) {
      throw new ValueUnexpectedException($options['mode'], $mode);
    }

    $context_array = stream_context_get_options($this->context);
    if ($options['context'] != $context_array) {
      throw new ValueUnexpectedException(print_r($options['context']),
                                         print_r($context_array));
    }
    // Check if we are simulating a failure to open the stream.
    if (isset($options['http_open_failure'])) {
      $this->stream_call_data = null;
      return false;
    }
    return true;
  }

  public function stream_read($count) {
    $data = $this->stream_call_data['stream_read'];
    if ($data === null) {
      throw new \Exception('Unexpected call to stream_read.');
    }
    $str = substr($data['bytes'], 0, $count);
    $this->stream_call_data['stream_read']['bytes'] =
        substr($data['bytes'], $count);
    return $str;
  }

  public function stream_stat() {
    $data = $this->stream_call_data['stream_stat'];
    if ($data === null) {
      throw new \Exception("Unexpected call to stream_stat");
    }
    return $data;
  }
}

class VmAPiProxyTest extends \PHPUnit_Framework_TestCase {
  const PACKAGE_NAME = "TestPackage";
  const CALL_NAME = "TestCall";

  // The default options used when configuring the expected RPC.
  private static $rpc_default_options = [
    'host' => VmApiProxy::SERVICE_BRIDGE_HOST,
    'port' => VmApiProxy::API_PORT,
    'proxy_path' => VmApiProxy::PROXY_PATH,
    'package_name' => self::PACKAGE_NAME,
    'call_name' => self::CALL_NAME,
    'ticket' => 'SomeTicketValue',
    'timeout' => VmApiProxy::DEFAULT_TIMEOUT_SEC,
    'http_headers' => [
      VmApiProxy::SERVICE_ENDPOINT_HEADER => VmApiProxy::SERVICE_ENDPOINT_NAME,
      VmApiProxy::SERVICE_METHOD_HEADER => VmApiProxy::APIHOST_METHOD,
      'Content-Type' => VmApiProxy::RPC_CONTENT_TYPE,
    ],
    'context' => [
      'http' => [
        'method' => 'POST',
      ],
    ],
  ];

  protected function setUp() {
    stream_wrapper_unregister("http");
    stream_wrapper_register("http", __NAMESPACE__ . '\\MockHttpStream');

    ApiProxy::setApiProxy(new VmApiProxy());

    // Clear out any MockHttp calls.
    unset($GLOBALS['mock_http']);

    // Standard environment variables
    putenv(VmApiProxy::TICKET_HEADER . '=' .
           self::$rpc_default_options['ticket']);
  }

  protected function tearDown() {
    $this->assertTrue(empty($GLOBALS['mock_http']));

    // Clear the environment
    putenv(VmApiProxy::TICKET_HEADER);
    stream_wrapper_restore("http");
  }

  protected function expectRpc($request,
                               $response,
                               $call_options = []) {
    $stream_call_data = [];

    $options = array_merge(self::$rpc_default_options, $call_options);

    // Open call will supply the address and the RPC request.
    $address = sprintf('http://%s:%s%s',
                       $options['host'],
                       $options['port'],
                       $options['proxy_path']);

    $remote_request = new Request();
    $remote_request->setServiceName($options['package_name']);
    $remote_request->setMethod($options['call_name']);
    $remote_request->setRequestId($options['ticket']);
    $remote_request->setRequest($request->serializeToString());

    $options['context']['http']['content'] =
        $remote_request->serializeToString();

    $options['context']['http']["timeout"] = $options['timeout'] +
                                             VmApiProxy::DEADLINE_DELTA_SECONDS;

    $options['http_headers'][VmApiProxy::SERVICE_DEADLINE_HEADER] =
        $options['timeout'];

    // Form the header string - sort by key as we do a string compare to check
    // for a match.
    ksort($options['http_headers']);
    $header_str = "";
    foreach($options['http_headers'] as $k => $v) {
      $header_str .= sprintf("%s: %s\r\n", $k, $v);
    }
    $options['context']['http']['header'] = $header_str;

    $stream_call_data['stream_open'] = [
      'address' => $address,
      'mode' => 'rb',
      'context' => $options['context'],
    ];

    if (isset($options['http_open_failure'])) {
      $stream_call_data['stream_open']['http_open_failure'] = true;
    }

    $remote_response = new Response();
    if (isset($options['rpc_exception'])) {
      $error = $remote_response->mutableRpcError();
      $error->setCode($options['rpc_exception']);
    } else if (isset($options['application_error'])) {
      $error = $remote_response->mutableApplicationError();
      $error->setCode($options['application_error']['code']);
      $error->setDetail($options['application_error']['detail']);
    } else if (isset($options['generic_exception'])) {
      $remote_response->setException(true);
    } else {
      $remote_response->setResponse($response->serializeToString());
    }
    $serialized_remote_response = $remote_response->serializeToString();

    $stream_call_data['stream_stat'] = [
      'size' => strlen($serialized_remote_response),
    ];

    $stream_call_data['stream_read'] = [
      'bytes' => $serialized_remote_response,
    ];

    $GLOBALS['mock_http'][] = $stream_call_data;
  }

  public function testBasicRpc() {
    $expected_request = new SignForAppRequest();
    $expected_response = new SignForAppResponse();
    $expected_request->setBytesToSign("SomeBytes");
    $expected_response->setKeyName("TheKeyName");

    $this->expectRpc($expected_request, $expected_response);

    $response = new SignForAppResponse();
    ApiProxy::makeSyncCall(self::PACKAGE_NAME,
                           self::CALL_NAME,
                           $expected_request,
                           $response);

    $this->assertEquals($response->getKeyName(), "TheKeyName");
  }

  /**
   * @dataProvider testRpcExceptionProvider
   */
  public function testRpcException($error_code, $exception) {
    $expected_request = new SignForAppRequest();
    $expected_response = new SignForAppResponse();
    $expected_request->setBytesToSign("SomeBytes");

    $options = [
      'rpc_exception' => $error_code,
    ];

    $this->expectRpc($expected_request, $expected_response, $options);

    $this->setExpectedException('google\appengine\runtime\\' . $exception);
    ApiProxy::makeSyncCall(self::PACKAGE_NAME,
                           self::CALL_NAME,
                           $expected_request,
                           $expected_response);
  }

  public function testRpcExceptionProvider() {
    return [
      [ErrorCode::UNKNOWN, 'RPCFailedError'],
      [ErrorCode::CALL_NOT_FOUND, 'CallNotFoundError'],
      [ErrorCode::PARSE_ERROR, 'ArgumentError'],
      [ErrorCode::SECURITY_VIOLATION, 'RPCFailedError'],
      [ErrorCode::OVER_QUOTA, 'OverQuotaError'],
      [ErrorCode::REQUEST_TOO_LARGE, 'RequestTooLargeError'],
      [ErrorCode::CAPABILITY_DISABLED, 'CapabilityDisabledError'],
      [ErrorCode::FEATURE_DISABLED, 'FeatureNotEnabledError'],
      [ErrorCode::BAD_REQUEST, 'RPCFailedError'],
      [ErrorCode::RESPONSE_TOO_LARGE, 'ResponseTooLargeError'],
      [ErrorCode::CANCELLED, 'CancelledError'],
      [ErrorCode::REPLAY_ERROR, 'RPCFailedError'],
      [ErrorCode::DEADLINE_EXCEEDED, 'DeadlineExceededError'],
    ];
  }

  public function testApplicationError() {
    $expected_request = new SignForAppRequest();
    $expected_response = new SignForAppResponse();
    $expected_request->setBytesToSign("SomeBytes");

    $options = [
      'application_error' => [
        'code' => 666,
        'detail' => 'foo',
      ],
    ];

    $this->expectRpc($expected_request, $expected_response, $options);

    $this->setExpectedException('google\appengine\runtime\ApplicationError');
    ApiProxy::makeSyncCall(self::PACKAGE_NAME,
                           self::CALL_NAME,
                           $expected_request,
                           $expected_response);
  }

  public function testGenericExceptionError() {
    $expected_request = new SignForAppRequest();
    $expected_response = new SignForAppResponse();
    $expected_request->setBytesToSign("SomeBytes");

    $options = [
      'generic_exception' => true,
    ];

    $this->expectRpc($expected_request, $expected_response, $options);

    $this->setExpectedException('google\appengine\runtime\RPCFailedError');
    ApiProxy::makeSyncCall(self::PACKAGE_NAME,
                           self::CALL_NAME,
                           $expected_request,
                           $expected_response);
  }

  public function testRpcDeadline() {
    $expected_request = new SignForAppRequest();
    $expected_response = new SignForAppResponse();
    $expected_request->setBytesToSign("SomeBytes");
    $expected_response->setKeyName("TheKeyName");

    $timeout = 666;
    $options = [
      'timeout' => $timeout,
    ];
    $this->expectRpc($expected_request, $expected_response, $options);

    $response = new SignForAppResponse();
    ApiProxy::makeSyncCall(self::PACKAGE_NAME,
                           self::CALL_NAME,
                           $expected_request,
                           $response,
                           $timeout);

    $this->assertEquals($response->getKeyName(), "TheKeyName");
  }

  public function testFailedHttpConnection() {
    $expected_request = new SignForAppRequest();
    $expected_response = new SignForAppResponse();
    $expected_request->setBytesToSign("SomeBytes");

    $options = [
      'http_open_failure' => true
    ];

    $this->expectRpc($expected_request, $expected_response, $options);

    $this->setExpectedException('google\appengine\runtime\RPCFailedError');
    ApiProxy::makeSyncCall(self::PACKAGE_NAME,
                           self::CALL_NAME,
                           $expected_request,
                           $expected_response);

  }

  public function testRpcDevTicket() {
    $expected_request = new SignForAppRequest();
    $expected_response = new SignForAppResponse();
    $expected_request->setBytesToSign("SomeBytes");
    $expected_response->setKeyName("TheKeyName");

    $ticket = 'TheDevTicket';
    putenv(VmApiProxy::TICKET_HEADER);
    putenv(VmApiProxy::DEV_TICKET_HEADER . "=$ticket");

    $options = [
      'ticket' => $ticket,
    ];

    $this->expectRpc($expected_request, $expected_response, $options);

    $response = new SignForAppResponse();
    ApiProxy::makeSyncCall(self::PACKAGE_NAME,
                           self::CALL_NAME,
                           $expected_request,
                           $response);

    $this->assertEquals($response->getKeyName(), "TheKeyName");
    putenv(VmApiProxy::DEV_TICKET_HEADER);
  }
}

