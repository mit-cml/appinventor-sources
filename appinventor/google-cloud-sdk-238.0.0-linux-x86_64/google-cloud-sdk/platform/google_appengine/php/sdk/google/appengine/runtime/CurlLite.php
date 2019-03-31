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
 * CurlLite - A simple cURL implementation over URLFetch.
 *
 * Many applications that use cURL do not need any of the more advanced features
 * that would require them to run the 'proper' cURL extension over sockets, so
 * we are providing a 'lite' extension of cURL for those use cases.
 *
 * For options where there is no direct translation to the URLFetch API then we
 * opt for failing fast rather than trying to fudge something that is not quite
 * correct.
 *
 */

namespace google\appengine\runtime;

use google\appengine\runtime\ApiProxy;
use google\appengine\runtime\ApplicationError;
use google\appengine\URLFetchRequest\RequestMethod;
use google\appengine\URLFetchServiceError\ErrorCode;
use google\appengine\util\ArrayUtil;
use google\appengine\util\HttpUtil;

final class CurlLite {
  // The list of requests protocols supported by this implementation.
  static private $supported_url_schemes = ['http', 'https'];

  // Map HTTP request types to URLFetch method enum.
  private static $custom_request_map = [
      "GET" => RequestMethod::GET,
      "POST" => RequestMethod::POST,
      "HEAD" => RequestMethod::HEAD,
      "PUT" => RequestMethod::PUT,
      "DELETE" => RequestMethod::DELETE,
      "PATCH" => RequestMethod::PATCH
  ];

  // Excluding error codes associated with FTP, Telnet et al.
  private static $curle_error_code_str_map = [
      CURLE_OK => "No error",
      CURLE_UNSUPPORTED_PROTOCOL => "Unsupported protocol",
      CURLE_FAILED_INIT => "Failed initialization",
      CURLE_URL_MALFORMAT => "URL using bad/illegal format or missing URL",
      CURLE_COULDNT_RESOLVE_PROXY => "Couldn't resolve proxy name",
      CURLE_COULDNT_RESOLVE_HOST => "Couldn't resolve host name",
      CURLE_COULDNT_CONNECT => "Couldn't connect to server",
      CURLE_PARTIAL_FILE => "Transferred a partial file",
      CURLE_HTTP_RETURNED_ERROR => "HTTP response code said error",
      CURLE_HTTP_NOT_FOUND => "HTTP response code said error",
      CURLE_WRITE_ERROR => "Failed writing received data to disk/application",
      CURLE_READ_ERROR => "Failed to open/read local data",
      CURLE_OUT_OF_MEMORY => "Out of memory",
      CURLE_OPERATION_TIMEDOUT => "Timeout was reached",
      CURLE_OPERATION_TIMEOUTED => "Timeout was reached",
      CURLE_HTTP_RANGE_ERROR => "Requested range was not delivered",
      CURLE_HTTP_POST_ERROR => "Internal problem setting up the POST",
      CURLE_SSL_CONNECT_ERROR => "SSL connect error",
      CURLE_BAD_DOWNLOAD_RESUME => "Couldn't resume download",
      CURLE_FILE_COULDNT_READ_FILE => "Couldn't read a file:// file",
      CURLE_LIBRARY_NOT_FOUND => "Unknown error",
      CURLE_ABORTED_BY_CALLBACK => "Operation aborted by application callback",
      CURLE_BAD_FUNCTION_ARGUMENT => "A function was given a bad argument",
      CURLE_TOO_MANY_REDIRECTS => "Number of redirects hit maximum amount",
      CURLE_SSL_PEER_CERTIFICATE => "SSL peer certificate was not OK",
      CURLE_GOT_NOTHING => "Server returned nothing (no headers, no data)",
      CURLE_SSL_ENGINE_NOTFOUND => "SSL crypto engine not found",
      CURLE_SSL_ENGINE_SETFAILED => "Can not set SSL crypto engine as default",
      CURLE_SEND_ERROR => "Failed sending data to the peer",
      CURLE_RECV_ERROR => "Failure when receiving data from the peer",
      CURLE_SSL_CERTPROBLEM => "Problem with the local SSL certificate",
      CURLE_SSL_CIPHER => "Couldn't use specified SSL cipher",
      CURLE_LDAP_INVALID_URL => "Invalid LDAP URL",
      CURLE_FILESIZE_EXCEEDED => "Maximum file size exceeded",
      CURLE_SSH => "Error in the SSH layer",
  ];

  private static $urlfetch_curl_error_map = [
      ErrorCode::INVALID_URL => CURLE_URL_MALFORMAT,
      ErrorCode::FETCH_ERROR => CURLE_RECV_ERROR,
      ErrorCode::UNSPECIFIED_ERROR => CURLE_RECV_ERROR,
      ErrorCode::RESPONSE_TOO_LARGE => CURLE_FILESIZE_EXCEEDED,
      ErrorCode::DEADLINE_EXCEEDED => CURLE_OPERATION_TIMEDOUT,
      ErrorCode::SSL_CERTIFICATE_ERROR => CURLE_SSL_CERTPROBLEM,
      ErrorCode::DNS_ERROR => CURLE_COULDNT_RESOLVE_HOST,
      ErrorCode::CLOSED => CURLE_RECV_ERROR,
      ErrorCode::INTERNAL_TRANSIENT_ERROR => CURLE_RECV_ERROR,
      ErrorCode::TOO_MANY_REDIRECTS => CURLE_TOO_MANY_REDIRECTS,
      ErrorCode::MALFORMED_REPLY => CURLE_RECV_ERROR,
      ErrorCode::CONNECTION_ERROR => CURLE_COULDNT_CONNECT,
  ];

  const UNKNOWN_INFO_VALUE = "-128";

  private static $default_getinfo_values = [
      "url" => "",
      "content_type" => "",
      "http_code" => 0,
      "header_size" => self::UNKNOWN_INFO_VALUE,
      "request_size" => self::UNKNOWN_INFO_VALUE,
      "filetime" => -1,
      "ssl_verify_result" => 0,
      "redirect_count" => 0,
      "total_time" => 0,
      "namelookup_time" => 0,
      "connect_time" => 0,
      "pretransfer_time" => 0,
      "size_upload" => 0,
      "size_download" => self::UNKNOWN_INFO_VALUE,
      "speed_download" => 0,
      "speed_upload" => 0,
      "download_content_length" => self::UNKNOWN_INFO_VALUE,
      "upload_content_length" => self::UNKNOWN_INFO_VALUE,
      "starttransfer_time" => 0,
      "redirect_time" => 0,
      "certinfo" => [],
      "primary_ip" => "",
      "primary_port" => 0,
      "local_ip" => 0,
      "local_port" => 0,
      "redirect_url" => "",
  ];

  private static $curlinfo_to_key_map = [
      CURLINFO_EFFECTIVE_URL => "url",
      CURLINFO_HTTP_CODE => "http_code",
      CURLINFO_FILETIME => "filetime",
      CURLINFO_TOTAL_TIME => "total_time",
      CURLINFO_NAMELOOKUP_TIME => "namelookup_time",
      CURLINFO_CONNECT_TIME => "connect_time",
      CURLINFO_PRETRANSFER_TIME => "pretransfer_time",
      CURLINFO_STARTTRANSFER_TIME => "starttransfer_time",
      CURLINFO_REDIRECT_COUNT => "redirect_count",
      CURLINFO_REDIRECT_TIME => "redirect_time",
      CURLINFO_REDIRECT_URL => "redirect_url",
      CURLINFO_PRIMARY_IP => "primary_ip",
      CURLINFO_PRIMARY_PORT => "primary_port",
      CURLINFO_LOCAL_IP => "local_ip",
      CURLINFO_LOCAL_PORT => "local_port",
      CURLINFO_SIZE_UPLOAD => "size_upload",
      CURLINFO_SIZE_DOWNLOAD => "size_download",
      CURLINFO_SPEED_DOWNLOAD => "speed_download",
      CURLINFO_SPEED_UPLOAD => "speed_upload",
      CURLINFO_HEADER_SIZE => "header_size",
      CURLINFO_REQUEST_SIZE => "request_size",
      CURLINFO_SSL_VERIFYRESULT => "ssl_verify_result",
      CURLINFO_CONTENT_LENGTH_DOWNLOAD => "download_content_length",
      CURLINFO_CONTENT_LENGTH_UPLOAD => "upload_content_length",
      CURLINFO_CONTENT_TYPE => "content_type",
      CURLINFO_HEADER_OUT => "request_header"
  ];

  private static $logging_callback = 'syslog';

  const CONTENT_TYPE_HEADER = 'Content-Type';
  const CRLF = '\r\n';
  const STATUS_LINE_FORMAT = 'HTTP/1.1 %d %s\r\n';

  private $request = null;  // The URLFetch request object.
  private $response = null;  // The URLFetch response object
  private $options = [];  // Options configured via setOption
  private $headers = [];  // Headers to send with the request
  private $info = null;  // Request/Response info for getinfo() calls.
  private $error_number = CURLE_OK;  // No error
  private $error_string = "OK";

  /**
   * Class constructor.
   *
   * @param string $url The destination URL for the connection.
   */
  public function __construct($url = null) {
    // Set the request defaults.
    $this->request = new \google\appengine\URLFetchRequest();
    $this->request->setMethod(RequestMethod::GET);
    $this->request->setMustValidateServerCertificate(true);
    $this->request->setFollowRedirects(true);

    if ($url) {
      $this->options[CURLOPT_URL] = $url;
    }
  }

  /**
   * Deep copy operation.
   */
  public function __clone() {
    $this->request = clone $this->request;
    $this->response = clone $this->response;
  }

  /**
   * Set cURL options using an array.
   *
   * @param mixed $options An associative array of cURL options and their
   * respective values.
   * @returns boolean True if all values could be set, false otherwise.
   */
  public function setOptionsArray($options) {
    foreach($options as $key => $value) {
      if (!$this->setOption($key, $value)) {
        return false;
      }
    }
    return true;
  }

  public function getInfo($option = 0) {
    if (is_null($this->info)) {
      return false;
    }
    if ($option === 0) {
      return $this->info;
    }
    if (array_key_exists($option, self::$curlinfo_to_key_map)) {
      $val = $this->info[self::$curlinfo_to_key_map[$option]];
      if ($val !== self::UNKNOWN_INFO_VALUE) {
        return $val;
      }
    }
    return false;
  }

  /**
   * Execute a curl request.
   */
  public function exec() {
    if (!$this->prepareRequest()) {
      return false;
    }
    $this->response = new \google\appengine\URLFetchResponse();

    try {
      ApiProxy::makeSyncCall('urlfetch',
                             'Fetch',
                             $this->request,
                             $this->response);
    } catch (ApplicationError $e) {
      $error_number = $e->getApplicationError();
      $curl_error_number = static::$urlfetch_curl_error_map[$error_number];
      $error_message = static::$curle_error_code_str_map[$curl_error_number];

      static::log(LOG_ERR,
                  sprintf('Call to URLFetch failed with application error %d ' .
                          '(%s) for url %s.',
                          $error_number,
                          $error_message,
                          $this->request->getUrl()));
      $this->setCurlErrorFromUrlFetchError($e->getApplicationError(),
                                           $e->getMessage());
      return false;
    }

    $response = $this->prepareResponse();

    // Must be after prepareResponse() so data is available for info.
    $this->info = self::$default_getinfo_values;
    $this->prepareCurlInfo();

    if ($this->tryGetOption(CURLOPT_RETURNTRANSFER, $value) && $value) {
      return $response;
    } else if ($this->tryGetOption(CURLOPT_FILE, $value) && $value) {
      $length = fwrite($value, $response);
      return ($length === strlen($response));
    } else if ($this->tryGetOption(CURLOPT_WRITEFUNCTION, $cb) && $cb) {
      $response_len = strlen($response);
      do {
        // TODO - what if cb returns 0 or -ve?
        $response_len -= $cb($this, $response);
      } while ($response_len > 0);
    } else {
      echo $response;
    }
    return true;
  }

  /**
   * Return the error number for the most recent error on this cURL object.
   */
  public function errorNumber() {
    return $this->error_number;
  }

  /**
   * Return the error string for the most recent error on this cURL object.
   */
  public function errorString() {
    return $this->error_string;
  }

  /**
   * Implementation of curl_version().
   */
  public static function version($version) {
    return [
      "version_number" => 0,
      "version" => "cURL Lite",
      "ssl_version_number" => 0,
      "ssl_version" => "",
      "libz_version" => "",
      "host" => "",
      "age" => 0,
      "features" => 0,
      "protocols" => static::$supported_url_schemes,
    ];
  }

  /**
   * Implementation of curl_escape.
   *
   * @param string $str The string to escape.
   * @returns string The escaped string.
   */
  public function escape($str) {
    return rawurlencode($str);
  }

  /**
   * Implementation of curl_unescape.
   *
   * @param string $str The string to unescape.
   * @returns string The unescaped string.
   */
  public function unescape($str) {
    return rawurldecode($str);
  }

  /**
   * Implementation of curl_strerror.
   *
   * @param int $code The error code.
   * @returns string Text representation of the error code.
   */
  public static function strerror($code) {
    if (array_key_exists($code, self::$curle_error_code_str_map)) {
      return self::$curle_error_code_str_map[$code];
    }
    return "Unknown Error";
  }

  /**
   * Configure the request URL.
   *
   * @returns boolean True if the URL could be set, False otherwise. If false
   * then error_number and error_string will be set accordingly.
   */
  private function setRequestUrl() {
    if ($this->tryGetOption(CURLOPT_URL, $value) && $value) {
      if (static::isSupportedUrlScheme($value, $scheme)) {
        $this->request->setUrl($value);
        return true;
      } else {
        $this->setError(CURLE_UNSUPPORTED_PROTOCOL,
                       sprintf("Unsupported protocol '%s'", $scheme));
      }
    } else {
      $this->setError(CURLE_URL_MALFORMAT, "No URL set!");
    }
    return false;
  }

  /**
   * Check if a URL scheme is supported by the CurlLite client.
   *
   * @param string $url The URL for the connection.
   */
  private static function isSupportedUrlScheme($url, &$scheme) {
    $scheme = parse_url($url, PHP_URL_SCHEME);

    return (is_null($scheme) ||
            in_array($scheme, static::$supported_url_schemes));
  }

  /**
   * Set a curl option for the request.
   *
   * See: http://php.net/manual/en/function.curl-setopt.php
   *
   * @param int $key The option to set.
   * @param mixed $value The value of the option.
   */
  private function setOption($key, $value) {
    switch ($key) {
      // Cases that we support.
      case CURLOPT_FOLLOWLOCATION:
        $this->request->setFollowRedirects($value);
        break;
      case CURLOPT_HTTPGET:
        $this->request->setMethod(RequestMethod::GET);
        break;
      case CURLOPT_NOBODY:
        $this->request->setMethod(RequestMethod::HEAD);
        break;
      case CURLOPT_POST:
        $this->request->setMethod(RequestMethod::POST);
        break;
      case CURLOPT_PUT:
        $this->request->setMethod(RequestMethod::PUT);
        break;
      case CURLOPT_SSL_VERIFYPEER:
        $this->request->setMustValidateServerCertificate($value);
        break;
      case CURLOPT_TIMEOUT:
        $this->request->setDeadline($value);
        break;
      case CURLOPT_TIMEOUT_MS:
        $this->request->setDeadline($value / 1000.0);
        break;
      case CURLOPT_CUSTOMREQUEST:
        if (!in_array($value, array_keys(static::$custom_request_map))) {
          throw new CurlLiteOptionNotSupportedException(
              'Custom request ' . $value . ' not supported by this curl ' .
              'implementation.');
        }
        $this->request->setMethod(static::$custom_request_map[$value]);
        break;
      case CURLOPT_RANGE:
        $this->headers['Range'] = $value;
        break;
      case CURLOPT_REFERER:
        $this->headers['Referer'] = $value;
        $break;
      case CURLOPT_URL:
        $this->setRequestUrl($value);
        break;
      case CURLOPT_USERAGENT:
        $this->headers['User-Agent'] = $value;
        break;
      case CURLOPT_COOKIE:
        $this->headers['Cookie'] = $value;
        break;
      case CURLOPT_HTTPHEADER:
        $this->headers = ArrayUtil::arrayMergeIgnoreCase(
            $this->headers, $this->parseHttpHeaders($value));
        break;
      // Cases that we don't support, that could cause a semantic change in the
      // application by not supporting.
      case CURLOPT_COOKIESESSION:
      case CURLOPT_CERTINFO:
      case CURLOPT_CONNECT_ONLY:
      case CURLOPT_FTP_USE_EPRT:
      case CURLOPT_FTP_USE_EPSV:
      case CURLOPT_FTP_CREATE_MISSING_DIRS:
      case CURLOPT_FTPAPPEND:
      case CURLOPT_FTPLISTONLY:
      case CURLOPT_HTTPPROXYTUNNEL:
      case CURLOPT_NETRC:
      case CURLOPT_NOSIGNAL:
      case CURLOPT_SAFE_UPLOAD:
      case CURLOPT_TRANSFERTEXT:
      case CURLOPT_FTPSSLAUTH:
      case CURLOPT_TIMEVALUE:
      case CURLOPT_CAINFO:
      case CURLOPT_COOKIEJAR:
      case CURLOPT_FTPPORT:
      case CURLOPT_KEYPASSWD:
      case CURLOPT_KRB4LEVEL:
      case CURLOPT_SSH_HOST_PUBLIC_KEY_MD5:
      case CURLOPT_SSH_PUBLIC_KEYFILE:
      case CURLOPT_SSH_PRIVATE_KEYFILE:
      case CURLOPT_SSLCERT:
      case CURLOPT_SSLCERTPASSWD:
      case CURLOPT_SSLCERTTYPE:
      case CURLOPT_SSLENGINE:
      case CURLOPT_SSLENGINE_DEFAULT:
      case CURLOPT_SSLKEY:
      case CURLOPT_SSLKEYPASSWD:
      case CURLOPT_SSLKEYTYPE:
      case CURLOPT_POSTQUOTE:
      case CURLOPT_QUOTE:
      case CURLOPT_PROGRESSFUNCTION:
      case CURLOPT_SHARE:
        throw new CurlLiteOptionNotSupportedException(
          'Option ' . $key . ' is not supported by this curl implementation.');

      // Everything else is a no-op, or will be configured at request time.
      default:
    }
    $this->options[$key] = $value;
    return true;
  }

  private function prepareRequest() {
    if (!$this->setRequestUrl()) {
      return false;
    }
    foreach($this->headers as $key => $value) {
      $new_header = $this->request->addHeader();
      $new_header->setKey(trim($key));
      $new_header->setValue(trim($value));
    }

    if ($this->tryGetOption(CURLOPT_POSTFIELDS, $value) && $value) {
      if (is_string($value)) {
        $payload = $value;
      } else if (is_array($value)) {
        $payload = http_build_query($value);
        // TODO: Arrays need to be mulitpart encoded.
      }
      if (!$this->tryGetRequestHeaderValue(self::CONTENT_TYPE_HEADER, $val)) {
        $header = $this->request->addHeader();
        $header->setKey(self::CONTENT_TYPE_HEADER);
        $header->setValue('application/x-www-form-urlencoded');
      }
      $this->request->setPayload($payload);
    }
    return true;
  }

  /**
   * Prepare the response from the URLFetch request ready for delivery to the
   * caller.
   *
   * @returns mixed String The response from the request, or false if there
   * was an error.
   */
  private function prepareResponse() {
    if (is_null($this->response)) {
      return false;
    }

    $response = "";

    $this->response_header_block = $this->extractHeadersFromResponse();
    if ($this->tryGetOption(CURLOPT_HEADER, $value) && $value) {
      $response .= $this->response_header_block;
    }
    $response .= $this->response->getContent();

    return $response;
  }

  private function prepareCurlInfo() {
    if (!isset($this->response)) {
      return false;
    }

    $this->info['http_code'] = $this->response->getStatusCode();
    $this->info['header_size'] = strlen($this->response_header_block);

    if ($this->response->hasFinalUrl()) {
      $this->info['url'] = $this->response->getFinalUrl();
      $this->info['redirect_count'] = 1;  // We don't know how many.
    } else {
      $this->info['url'] = $this->request->getUrl();
    }

    if ($this->response->hasExternalBytesReceived()) {
      $this->info['size_download'] =
          $this->response->getExternalBytesReceived();
    }

    if ($this->response->hasExternalBytesSent()) {
      $this->info['size_upload'] = $this->response->getExternalBytesSent();
    }

    if ($this->tryGetOption(CURLINFO_HEADER_OUT, $value) && $value) {
      $headers_out = '';
      foreach($this->request->getHeaderList() as $header) {
        $headers_out .= sprintf("%s: %s%s",
                                $header->getKey(),
                                $header->getValue(),
                                self::CRLF);
      }
      $this->info['request_header'] = $headers_out;
    }

    foreach ($this->response->getHeaderList() as $header) {
      if (strcasecmp (self::CONTENT_TYPE_HEADER, $header->getKey()) === 0) {
        $this->info['content_type'] = $header->getValue();
        break;
      }
    }
  }

  /**
   * Try and get a cURL option from the options array.
   *
   * @param int $name The value of the CURLOPT to retreive.
   * @param mixed $value Pass by reference location to store the option value.
   *
   * @returns boolean True of the key was found, False otherwise.
   */
  private function tryGetOption($name, &$value) {
    if (array_key_exists($name, $this->options)) {
      $value = $this->options[$name];
      return true;
    }
    return false;
  }

  /**
   * Set an error on this cURL object.
   *
   * @param int $errno The error number.
   * @param string $errstr The error string.
   */
  private function setError($errno, $errstr = "") {
    $this->error_number = $errno;
    $this->error_string = $errstr;
  }

  /**
   * Convert a URLFetch error code to a cURL error number, with message.
   *
   * @param int $urlfetch_error The URLFetch error number.
   * @param string $urlfetch_message The URLFetch error string.
   */
  private function setCurlErrorFromUrlFetchError($urlfetch_error,
                                                 $urlfetch_message) {
    if (array_key_exists($urlfetch_error, self::$urlfetch_curl_error_map)) {
      $this->setError(self::$urlfetch_curl_error_map[$urlfetch_error],
                      $urlfetch_message);
    } else {
      $this->setError(-1, $urlfetch_message);  // Maps to "Unknown Error".
    }
  }

  /**
   * Create the header body from the URLFetch response.
   *
   * @returns string The header block of the response.
   */
  private function extractHeadersFromResponse() {
    $response = "";
    $code = $this->response->getStatusCode();
    $text = HttpUtil::getResponseTextForCode($code);
    $response .= sprintf(self::STATUS_LINE_FORMAT, $code, $text);

    foreach($this->response->getHeaderList() as $header) {
      $response .= sprintf("%s: %s%s",
                           $header->getKey(),
                           $header->getValue(),
                           self::CRLF);
    }
    $response .= self::CRLF;
    return $response;
  }


  /**
   * Check if the request has the specified header, and if so return it in the
   * pass-by-ref value $value.
   *
   * @param string $key The header key to find.
   * @param string $value The value of the header, of found.
   *
   * @returns boolean True if the header was found, false otherwise.
   */
  private function tryGetRequestHeaderValue($key, &$value) {
    $result = false;
    foreach($this->request->getHeaderList() as $header) {
      if (strcasecmp($key, $header->getKey()) === 0) {
        $value = $header->getValue();
        $result = true;
      }
    }
    return $result;
  }

  /**
   * Add an array of HTTP headers in key:value format and return an assoicative
   * array.
   *
   * @param array $headers An array of header strings in "Key: Value" format.
   *
   * @returns array An associative array of headers.
   */
  private function parseHttpHeaders($headers) {
    $result = [];
    foreach ($headers as $header) {
      $values = explode(':', $header, 2);
      if (count($values) === 2) {
        list($key, $value) = $values;
        $key = trim($key);
        $value = trim($value);
        // Checking with real cURL it only sends a header if the key & the value
        // are set.
        if ($key && $value) {
          $result[$key] = $value;
        }
      }
    }
    return $result;
  }

  /**
   * Log a message to the system log. Provided so we can hook the logging in
   * unit tests without resorting to namespace mocking tricks.
   *
   * @param int $log_level The level of the logging message
   * @param string $message The message to log.
   */
  private static function log($log_level, $message) {
    call_user_func(static::$logging_callback, $log_level, $message);
  }
}
