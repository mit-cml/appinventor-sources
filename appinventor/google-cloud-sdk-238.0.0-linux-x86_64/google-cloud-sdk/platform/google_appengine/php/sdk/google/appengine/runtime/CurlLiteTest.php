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

use google\appengine\testing\ApiProxyTestBase;
use google\appengine\testing\TestUtils;
use google\appengine\URLFetchRequest\RequestMethod;
use google\appengine\URLFetchServiceError\ErrorCode;
use google\appengine\runtime\ApplicationError;

class CurlLiteTest extends ApiProxyTestBase {

  private $expected_log_messages = [];

  public function setUp() {
    parent::setUp();
    TestUtils::setStaticProperty('google\appengine\runtime\CurlLite',
                                 'logging_callback',
                                 [$this, 'mockLog']);
  }

  private function setupRequest($url, $body = null) {
    $this->request = new \google\appengine\URLFetchRequest();
    $this->response = new \google\appengine\URLFetchResponse();

    $this->request->setMethod(RequestMethod::GET);
    $this->request->setUrl($url);
    $this->request->setFollowRedirects(true);
    $this->request->setMustValidateServerCertificate(true);

    $this->response->setStatusCode(200);
    if (isset($body)) {
      $this->response->setContent($body);
    }
  }

  private function expectLogMessage($log_level, $log_message) {
    $this->expected_log_messages[] = [$log_level, $log_message];
  }

  /**
   * @dataProvider setOptionProvider
   */
  public function testSetOption($key, $option, $expected_exception) {
    $curl_lite = new CurlLite();

    try {
      $result = $curl_lite->setOptionsArray([$key => $option]);
      $exception = false;
    } catch (CurlLiteOptionNotSupportedException $ex) {
      $exception = true;
    }
    if (!$expected_exception) {
      $this->assertEquals(true, $result);
    }
    $this->assertEquals($expected_exception, $exception);
  }

  public function setOptionProvider() {
    $supported_options = ['FOLLOWLOCATION', 'HTTPGET', 'NOBODY', 'POST', 'PUT',
        'SSL_VERIFYPEER', 'TIMEOUT', 'TIMEOUT_MS', 'POSTFIELDS', 'RANGE',
        'REFERER', 'URL', 'USERAGENT',
    ];

    foreach($supported_options as $option) {
      $option = 'CURLOPT_' . $option;
      assert(defined($option), 'Check ' . $option . ' is correct');
      yield [constant($option), true, false];
    }

    $noop_options = ['AUTOREFERER', 'BINARYTRANSFER', 'CRLF',
        'DNS_USE_GLOBAL_CACHE', 'FAILONERROR', 'FILETIME', 'FORBID_REUSE',
        'FRESH_CONNECT', 'TCP_NODELAY', 'HEADER', 'NOPROGRESS',
        'RETURNTRANSFER', 'UNRESTRICTED_AUTH', 'UPLOAD', 'VERBOSE',
        'BUFFERSIZE', 'CLOSEPOLICY', 'CONNECTTIMEOUT', 'CONNECTTIMEOUT_MS',
        'DNS_CACHE_TIMEOUT', 'HTTP_VERSION', 'HTTPAUTH', 'INFILESIZE',
        'LOW_SPEED_LIMIT', 'LOW_SPEED_TIME', 'MAXCONNECTS', 'MAXREDIRS', 'PORT',
        'PROTOCOLS', 'PROXYAUTH', 'PROXYPORT', 'PROXYTYPE', 'REDIR_PROTOCOLS',
        'RESUME_FROM', 'SSL_VERIFYHOST', 'SSLVERSION', 'TIMECONDITION',
        'MAX_RECV_SPEED_LARGE', 'MAX_SEND_SPEED_LARGE', 'SSH_AUTH_TYPES',
        'IPRESOLVE', 'CAPATH', 'COOKIE', 'COOKIEFILE', 'EGDSOCKET', 'ENCODING',
        'INTERFACE', 'PROXY', 'PROXYUSERPWD', 'RANDOM_FILE', 'SSL_CIPHER_LIST',
        'USERPWD', 'HTTP200ALIASES', 'FILE', 'INFILE', 'STDERR', 'WRITEHEADER',
        'HEADERFUNCTION', 'READFUNCTION', 'WRITEFUNCTION'
    ];

    foreach($noop_options as $option) {
      $option = 'CURLOPT_' . $option;
      assert(defined($option), 'Check ' . $option . ' is correct');
      yield [constant($option), true, false];
    }

    $unsupported_options = ['COOKIESESSION', 'CERTINFO', 'CONNECT_ONLY',
        'FTP_USE_EPRT', 'FTP_USE_EPSV', 'FTP_CREATE_MISSING_DIRS', 'FTPAPPEND',
        'FTPLISTONLY', 'HTTPPROXYTUNNEL', 'NETRC', 'NOSIGNAL',
        'SAFE_UPLOAD', 'TRANSFERTEXT', 'FTPSSLAUTH', 'TIMEVALUE', 'CAINFO',
        'COOKIEJAR', 'FTPPORT', 'KEYPASSWD', 'KRB4LEVEL',
        'SSH_HOST_PUBLIC_KEY_MD5', 'SSH_PUBLIC_KEYFILE', 'SSH_PRIVATE_KEYFILE',
        'SSLCERT', 'SSLCERTPASSWD', 'SSLCERTTYPE', 'SSLENGINE',
        'SSLENGINE_DEFAULT', 'SSLKEY', 'SSLKEYPASSWD', 'SSLKEYTYPE',
        'POSTQUOTE', 'QUOTE', 'PROGRESSFUNCTION', 'SHARE',
    ];
    foreach($unsupported_options as $option) {
      $option = 'CURLOPT_' . $option;
      assert(defined($option), 'Check ' . $option . ' is correct');
      yield [constant($option), true, true];
    }
    // CUSTOMREQUEST tests need special values passed.
    yield [CURLOPT_CUSTOMREQUEST, "DELETE", false];
    yield [CURLOPT_CUSTOMREQUEST, "PATCH", false];
    yield [CURLOPT_CUSTOMREQUEST, "FOOBAR", true];
  }

  public function testBasicExecWithReturnTransfer() {
    $url = 'http://google.com';
    $response_body = "Hello World";
    $this->setupRequest($url, $response_body);
    $this->addRequestHeader('Foo', 'Bar');
    $this->addRequestHeader('Baz', 'Boo: Zoop');
    $this->apiProxyMock->expectCall('urlfetch',
                                    'Fetch',
                                    $this->request,
                                    $this->response);

    $curl_lite = new CurlLite();
    $curl_lite->setOptionsArray([CURLOPT_URL => $url]);
    $curl_lite->setOptionsArray([CURLOPT_RETURNTRANSFER => true]);
    $curl_lite->setOptionsArray([CURLOPT_HTTPHEADER => [
        'Foo: Bar',
        'Baz: Boo: Zoop',
        // cURL drops headers that do not have a value.
        'Key-With-Space: ',
        'Key-With-Nothing:',
        'Key-Without-Colon',
    ]]);
    $result = $curl_lite->exec();
    unset($curl_lite);

    $this->assertEquals($response_body, $result);
    $this->apiProxyMock->verify();
  }

  public function testBasicExecWithStdOutput() {
    $url = 'http://google.com';
    $response_body = "Hello World";
    $this->setupRequest($url, $response_body);
    $this->apiProxyMock->expectCall('urlfetch',
                                    'Fetch',
                                    $this->request,
                                    $this->response);

    ob_start();
    $curl_lite = new CurlLite();
    $curl_lite->setOptionsArray([CURLOPT_URL => $url]);
    $result = $curl_lite->exec();
    $curl_content = ob_get_contents();
    unset($curl_lite);
    ob_end_clean();

    $this->assertTrue($result);
    $this->assertEquals($response_body, $curl_content);
    $this->apiProxyMock->verify();
  }

  public function testBasicPost() {
    $url = 'http://google.com';
    $response_body = "Hello World";
    $postfields = "Hello=World&Foo=Bar&Person=John%20Doe";
    $this->setupRequest($url, $response_body);
    $this->apiProxyMock->expectCall('urlfetch',
                                    'Fetch',
                                    $this->request,
                                    $this->response);

    $this->request->setMethod(RequestMethod::POST);
    $this->request->setPayload($postfields);
    $this->addRequestHeader('Content-Type',
                            'application/x-www-form-urlencoded');

    $curl_lite = new CurlLite();
    $curl_lite->setOptionsArray([
      CURLOPT_URL => $url,
      CURLOPT_RETURNTRANSFER => 1,
      CURLOPT_POSTFIELDS => $postfields,
      CURLOPT_POST => 1,
    ]);
    $result = $curl_lite->exec();
    unset($curl_lite);

    $this->assertEquals($response_body, $result);
    $this->apiProxyMock->verify();
  }

  public function testUserSuppliedContentTypePost() {
    $url = 'http://google.com';
    $response_body = "Hello World";
    $postfields = json_encode(["Hello" => "World"]);
    $this->setupRequest($url, $response_body);
    $this->apiProxyMock->expectCall('urlfetch',
                                    'Fetch',
                                    $this->request,
                                    $this->response);

    $this->request->setMethod(RequestMethod::POST);
    $this->request->setPayload($postfields);
    $this->addRequestHeader('Content-Type',
                            'application/json');

    $curl_lite = new CurlLite();
    $curl_lite->setOptionsArray([
      CURLOPT_URL => $url,
      CURLOPT_RETURNTRANSFER => 1,
      CURLOPT_POSTFIELDS => $postfields,
      CURLOPT_POST => 1,
      CURLOPT_HTTPHEADER => [
        'Content-Type: application/json',
      ],
    ]);
    $result = $curl_lite->exec();
    unset($curl_lite);

    $this->assertEquals($response_body, $result);
    $this->apiProxyMock->verify();
  }


  public function testRequestHeaders() {
    $url = 'http://google.com';
    $response_body = "Hello World";
    $this->setupRequest($url, $response_body);
    $this->apiProxyMock->expectCall('urlfetch',
                                    'Fetch',
                                    $this->request,
                                    $this->response);

    $this->addRequestHeader('User-Agent', 'test agent');
    $this->addRequestHeader('Cookie', 'foo=bar;zoo=baz');

    $curl_lite = new CurlLite();
    $curl_lite->setOptionsArray([
      CURLOPT_URL => $url,
      CURLOPT_RETURNTRANSFER => true,
      CURLOPT_USERAGENT => 'test agent',
      CURLOPT_COOKIE => 'foo=bar;zoo=baz',
      CURLINFO_HEADER_OUT => true,
    ]);
    $result = $curl_lite->exec();
    $out_headers = $curl_lite->getInfo(CURLINFO_HEADER_OUT);
    unset($curl_lite);

    $expected_request_heders = 'User-Agent: test agent\r\n' .
        'Cookie: foo=bar;zoo=baz\r\n';

    $this->assertEquals($response_body, $result);
    $this->assertEquals($expected_request_heders, $out_headers);
    $this->apiProxyMock->verify();
  }

  public function testWriteFunction() {
    $url = 'http://google.com';
    $response_body = "Hello World";
    $this->setupRequest($url, $response_body);
    $this->apiProxyMock->expectCall('urlfetch',
                                    'Fetch',
                                    $this->request,
                                    $this->response);

    $cb_response = "";
    $curl_lite = new CurlLite();
    $curl_lite->setOptionsArray([
      CURLOPT_URL => $url,
      CURLOPT_WRITEFUNCTION => function ($ch, $data) use (&$cb_response) {
        $cb_response .= $data;
        return strlen($data);
      },
    ]);
    $result = $curl_lite->exec();
    unset($curl_lite);

    $this->assertTrue($result);
    $this->assertEquals($response_body, $cb_response);
    $this->apiProxyMock->verify();
  }

  public function testNoUrl() {
    // Similar to curl_basic_007.phpt
    $curl_lite = new CurlLite();
    $result = $curl_lite->exec();

    $this->assertFalse($result);
    $this->assertEquals(CURLE_URL_MALFORMAT, $curl_lite->errorNumber());
    $this->assertEquals("No URL set!", $curl_lite->errorString());
  }

  public function testResolveError() {
    // Similar to curl_basic_008.phpt
    $url = 'http://host_does_not_exist.com/';
    $error_msg = 'Unable to resolve ' . $url;
    $this->setupRequest($url);
    $exception = new ApplicationError(ErrorCode::DNS_ERROR, $error_msg);
    $this->apiProxyMock->expectCall('urlfetch',
                                    'Fetch',
                                    $this->request,
                                    $exception);
    $this->expectLogMessage(
      LOG_ERR,
      "Call to URLFetch failed with application error 7 (Couldn't resolve " .
      "host name) for url http://host_does_not_exist.com/.");

    $curl = new CurlLite($url);
    $result = $curl->exec();

    $this->assertFalse($result);
    $this->assertEquals(CURLE_COULDNT_RESOLVE_HOST, $curl->errorNumber());
    $this->assertEquals($error_msg, $curl->errorString());
    $this->apiProxyMock->verify();
  }

  public function testUnsupportedProtocol() {
    // Similar to curl_basic_009.phpt
    $curl_lite = new CurlLite('foo://www.google.com');
    $result = $curl_lite->exec();

    $this->assertFalse($result);
    $this->assertEquals(CURLE_UNSUPPORTED_PROTOCOL, $curl_lite->errorNumber());
    $this->assertEquals("Unsupported protocol 'foo'",
                        $curl_lite->errorString());
  }

  public function testFileWrite() {
    $url = 'http://google.com';
    $response_body = "Hello World";
    $this->setupRequest($url, $response_body);
    $this->apiProxyMock->expectCall('urlfetch',
                                    'Fetch',
                                    $this->request,
                                    $this->response);
    $log_file = tempnam(sys_get_temp_dir(), 'php-curl-test');
    $fp = fopen($log_file, 'w+');

    $curl_lite = new CurlLite();
    $curl_lite->setOptionsArray([
      CURLOPT_URL => $url,
      CURLOPT_FILE => $fp,
    ]);
    $result = $curl_lite->exec();
    unset($curl_lite);
    fclose($fp);

    $data = file_get_contents($log_file);
    $this->assertTrue($result);
    $this->assertEquals($response_body, $data);
    $this->apiProxyMock->verify();
  }

  public function testResponseHeaders() {
    $url = 'http://google.com';
    $response_body = "Hello World";
    $this->setupRequest($url, $response_body);
    $this->addResponseHeader('Content-Type', 'text/html; charset=ISO-8859-1');
    $this->addResponseHeader('Cache-Control', 'private, max-age=0');
    $this->apiProxyMock->expectCall('urlfetch',
                                    'Fetch',
                                    $this->request,
                                    $this->response);
    $curl_lite = new CurlLite();
    $curl_lite->setOptionsArray([
      CURLOPT_URL => $url,
      CURLOPT_HEADER => true,
      CURLOPT_RETURNTRANSFER => true,
    ]);
    $response_header = 'HTTP/1.1 200 OK\r\n' .
        'Content-Type: text/html; charset=ISO-8859-1\r\n' .
        'Cache-Control: private, max-age=0\r\n' .
        '\r\n';
    $response = $curl_lite->exec();
    $expected_response = $response_header . $response_body;
    $this->assertEquals($expected_response, $response);
    $this->assertEquals(strlen($response_header),
                        $curl_lite->getInfo(CURLINFO_HEADER_SIZE));
    $this->apiProxyMock->verify();
  }

  public function testEscapeFunctions() {
    // Similar to curl_escape.phpt
    $str = 'http://www.php.net/ ?!';
    $curl_lite = new CurlLite();
    $escaped = $curl_lite->escape($str);
    $original = $curl_lite->unescape($escaped);
    $this->assertEquals($str, $original);
    $this->assertEquals('http%3A%2F%2Fwww.php.net%2F%20%3F%21', $escaped);
  }

  public function testStrErrorCodes() {
    $curl_lite = new CurlLite();
    $this->assertEquals("No error", $curl_lite->strerror(CURLE_OK));
    $this->assertEquals("Unsupported protocol",
                        $curl_lite->strerror(CURLE_UNSUPPORTED_PROTOCOL));
    $this->assertEquals("Unknown Error", $curl_lite->strerror(-1));
  }

  public function testGetInfo() {
    $url = 'http://google.com';
    $response_body = "Hello World";
    $this->setupRequest($url, $response_body);
    $this->response->setExternalBytesSent(111);
    $this->response->setExternalBytesReceived(222);
    $this->response->setFinalUrl('http://lotr.com');
    $this->addResponseHeader('Content-Type', 'text/html');
    $this->apiProxyMock->expectCall('urlfetch',
                                    'Fetch',
                                    $this->request,
                                    $this->response);

    $curl_lite = new CurlLite();
    $curl_lite->setOptionsArray([
      CURLOPT_URL => $url,
      CURLOPT_RETURNTRANSFER => true,
    ]);
    $result = $curl_lite->exec();

    $this->assertEquals($response_body, $result);
    $this->assertEquals(200, $curl_lite->getInfo(CURLINFO_HTTP_CODE));
    $this->assertEquals('http://lotr.com',
                        $curl_lite->getInfo(CURLINFO_EFFECTIVE_URL));
    $this->assertEquals('text/html',
                        $curl_lite->getInfo(CURLINFO_CONTENT_TYPE));
    $this->assertEquals(111, $curl_lite->getInfo(CURLINFO_SIZE_UPLOAD));
    $this->assertEquals(222, $curl_lite->getInfo(CURLINFO_SIZE_DOWNLOAD));
    $this->assertEquals(1, $curl_lite->getInfo(CURLINFO_REDIRECT_COUNT));
    $this->apiProxyMock->verify();
  }

  public function testSetTimeoutMS() {
    $url = 'http://google.com';
    $this->setupRequest($url);
    $this->request->setDeadline(0.5);
    $this->apiProxyMock->expectCall('urlfetch',
                                    'Fetch',
                                    $this->request,
                                    $this->response);

    $curl_lite = new CurlLite();
    $result = $curl_lite->setOptionsArray([
      CURLOPT_TIMEOUT_MS => 500,
      CURLOPT_URL => $url
    ]);
    $curl_lite->exec();
    $this->assertEquals($result, true);
  }

  public function mockLog($log_level, $log_message) {
    $this->assertFalse(empty($this->expected_log_messages));
    $expected = array_shift($this->expected_log_messages);
    $this->assertEquals($expected[0], $log_level);
    $this->assertEquals($expected[1], $log_message);
  }

  private function addRequestHeader($key, $value) {
    $header = $this->request->addHeader();
    $header->setKey($key);
    $header->setValue($value);
  }

  private function addResponseHeader($key, $value) {
    $header = $this->response->addHeader();
    $header->setKey($key);
    $header->setValue($value);
  }

}

