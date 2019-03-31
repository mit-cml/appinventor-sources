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
namespace google\appengine\runtime;

use google\appengine\ext\remote_api\Request;
use google\appengine\ext\remote_api\Response;
use google\appengine\ext\remote_api\RpcError\ErrorCode;
use google\appengine\runtime\RPCFailedError;

/**
 * An ApiProxy implementation that communicates with the VMRuntime Service
 * bridge.
 */
class VmApiProxy extends ApiProxyBase{

  const TICKET_HEADER = 'HTTP_X_APPENGINE_API_TICKET';
  const DEV_TICKET_HEADER = 'HTTP_X_APPENGINE_DEV_REQUEST_ID';
  const DAPPER_ENV_KEY = 'HTTP_X_GOOGLE_DAPPERTRACEINFO';
  const SERVICE_BRIDGE_HOST = 'appengine.googleapis.internal';
  const API_PORT = 10001;
  const SERVICE_ENDPOINT_NAME = 'app-engine-apis';
  const APIHOST_METHOD = '/VMRemoteAPI.CallRemoteAPI';
  const PROXY_PATH = '/rpc_http';
  const DAPPER_HEADER = 'X-Google-DapperTraceInfo';
  const SERVICE_DEADLINE_HEADER = 'X-Google-RPC-Service-Deadline';
  const SERVICE_ENDPOINT_HEADER = 'X-Google-RPC-Service-Endpoint';
  const SERVICE_METHOD_HEADER = 'X-Google-RPC-Service-Method';
  const RPC_CONTENT_TYPE = 'application/octet-stream';
  const DEFAULT_TIMEOUT_SEC = 60;
  const DEADLINE_DELTA_SECONDS = 1;

  // Map of Rpc Error Codes to the corresponging runtime exception.
  protected static $exceptionLookupTable = [
    ErrorCode::UNKNOWN => [
      '\google\appengine\runtime\RPCFailedError',
      'The remote RPC to the application server failed for the call %s.%s().',
    ],
    ErrorCode::CALL_NOT_FOUND => [
      '\google\appengine\runtime\CallNotFoundError',
      "The API package '%s' or call '%s()' was not found.",
    ],
    ErrorCode::PARSE_ERROR => [
      '\google\appengine\runtime\ArgumentError',
      'There was an error parsing arguments for API call %s.%s().',
    ],
    ErrorCode::OVER_QUOTA => [
      '\google\appengine\runtime\OverQuotaError',
      'The API call %s.%s() required more quota than is available.',
    ],
    ErrorCode::REQUEST_TOO_LARGE => [
      '\google\appengine\runtime\RequestTooLargeError',
      'The request to API call %s.%s() was too large.',
    ],
    ErrorCode::CAPABILITY_DISABLED => [
      '\google\appengine\runtime\CapabilityDisabledError',
      'The API call %s.%s() is temporarily disabled.',
    ],
    ErrorCode::FEATURE_DISABLED => [
      '\google\appengine\runtime\FeatureNotEnabledError',
      'The API call %s.%s() is currently not enabled.',
    ],
    ErrorCode::RESPONSE_TOO_LARGE => [
      '\google\appengine\runtime\ResponseTooLargeError',
      'The response from API call %s.%s() was too large.',
    ],
    ErrorCode::CANCELLED => [
      '\google\appengine\runtime\CancelledError',
      'The API call %s.%s() was explicitly cancelled.',
    ],
    ErrorCode::DEADLINE_EXCEEDED => [
      '\google\appengine\runtime\DeadlineExceededError',
      'The API call %s.%s() took too long to respond and was cancelled.',
    ],
  ];

  // The default security ticket, if passed in the constructor.
  private $default_ticket = null;

  /**
   * Consruct a VmAPiProxy object.
   *
   * @param string $defult_ticket The default security ticket to use.
   */
  public function __construct($default_ticket = null) {
    $this->default_ticket = $default_ticket;
  }

  /**
   * Makes a synchronous RPC call.
   * @param string $package Package to call
   * @param string $call_name Specific RPC call to make
   * @param string $request Request proto, serialised to string
   * @param string $response Response proto string to populate
   * @param double $deadline Optional deadline for the RPC call in seconds.
   */
  public function makeSyncCall(
      $package,
      $call_name,
      $request,
      $response,
      $deadline = null) {
    if ($deadline === null) {
      $deadline = self::DEFAULT_TIMEOUT_SEC;
    }
    $ticket = getenv(self::TICKET_HEADER);
    if ($ticket === false) {
      $ticket = getenv(self::DEV_TICKET_HEADER);
      if ($ticket === false) {
        $ticket = $this->getDefaultTicket();
      }
    }

    $remote_request = new Request();
    $remote_request->setServiceName($package);
    $remote_request->setMethod($call_name);
    $remote_request->setRequest($request->serializeToString());
    $remote_request->setRequestId($ticket);
    $serialized_remote_request = $remote_request->serializeToString();

    $headers = [
      self::SERVICE_DEADLINE_HEADER => $deadline,
      self::SERVICE_ENDPOINT_HEADER => self::SERVICE_ENDPOINT_NAME,
      self::SERVICE_METHOD_HEADER => self::APIHOST_METHOD,
      'Content-Type' => self::RPC_CONTENT_TYPE,
    ];

    $dapper_header_value = getenv(self::DAPPER_ENV_KEY);
    if ($dapper_header_value !== false) {
      $headers[self::DAPPER_HEADER] = $dapper_header_value;
    }

    // Headers are sorted so we can do a string comparison in the unit test.
    ksort($headers);
    $header_str = "";
    foreach($headers as $k => $v) {
      $header_str .= sprintf("%s: %s\r\n", $k, $v);
    }

    $opts = [
      'http' => [
        'method' => 'POST',
        'header' => $header_str,
        'content' => $serialized_remote_request,
        'timeout' => $deadline + self::DEADLINE_DELTA_SECONDS,
      ],
    ];
    $context = stream_context_create($opts);

    $api_host = static::getEnvOrDefault('API_HOST', self::SERVICE_BRIDGE_HOST);
    $api_port = static::getEnvOrDefault('API_PORT', self::API_PORT);

    $endpoint_url = sprintf("http://%s:%s%s",
                            $api_host,
                            $api_port,
                            self::PROXY_PATH);

    // We silence the error here to prevent spamming the users application.
    // @codingStandardsIgnoreStart
    $serialized_remote_respone = @file_get_contents($endpoint_url,
                                                    false,
                                                    $context);
    // @codingStandardsIgnoreEnd

    if ($serialized_remote_respone === false) {
      throw new RPCFailedError(sprintf('Remote implementation for %s.%s failed',
                                       $package,
                                       $call_name));
    }

    $remote_response = new Response();
    $remote_response->parseFromString($serialized_remote_respone);

    if ($remote_response->hasApplicationError()) {
      throw new ApplicationError(
          $remote_response->getApplicationError()->getCode(),
          $remote_response->getApplicationError()->getDetail());
    }

    if ($remote_response->hasException() ||
        $remote_response->hasJavaException()) {
      // This indicates a bug in the remote implementation.
      throw new RPCFailedError(sprintf('Remote implementation for %s.%s failed',
                                       $package,
                                       $call_name));
    }

    if ($remote_response->hasRpcError()) {
      $rpc_error = $remote_response->getRpcError();
      throw self::getRpcErrorFromException($rpc_error->getCode(),
                                          $package,
                                          $call_name);
    }

    $response->parseFromString($remote_response->getResponse());
  }

  /**
   * Lookup a value from the environment, return the supplied default value
   * if the variable name is not defined.
   *
   * @param string $varname The variable name to lookup.
   * @param mixed $default The default value to use if the variable is not
   * found.
   *
   * @returns mixed The environment variable value, or the default value.
   */
  private static function getEnvOrDefault($varname, $default) {
    $result = getenv($varname);
    if ($result === false) {
      $result = $default;
    }
    return $result;
  }

  /**
   * Return the default security ticket for the RPC call. If the default value
   * was not set in the constructor then it will be retrieved from the
   * environment.
   *
   * @returns string The security ticket.
   */
  private function getDefaultTicket() {
    if ($this->default_ticket) {
      return $this->default_ticket;
    }
    return getenv('DEFAULT_TICKET');
  }

  /**
   * Create a runtime exception from an RPC Error Code.
   *
   * @param int $error_no The RPC error code.
   * @param string $package The package name of the RPC call.
   * @param string $call The call name of the RPC call.
   */
  private static function getRpcErrorFromException($error_no, $package, $call) {
    if (isset(self::$exceptionLookupTable[$error_no])) {
      $res = self::$exceptionLookupTable[$error_no];
      return new $res[0](sprintf($res[1], $package, $call));
    }
    return new RPCFailedError(sprintf('Remote implementation for %s.%s failed',
                                       $package,
                                       $call));
  }
}
