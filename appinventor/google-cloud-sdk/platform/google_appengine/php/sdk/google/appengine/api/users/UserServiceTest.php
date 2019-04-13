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
 */

use google\appengine\api\users\User;
use google\appengine\api\users\UserService;
use google\appengine\testing\ApiProxyTestBase;
use google\appengine\UserServiceError;

/**
 * Unittest for UserService class.
 */
class UserServiceTest extends ApiProxyTestBase {
  public function tearDown() {
    putenv("AUTH_DOMAIN");
    parent::tearDown();
  }

  public function testCreateLoginURLWithDestination() {
    $req = new \google\appengine\CreateLoginURLRequest();
    $req->setDestinationUrl('http://abc');

    $resp = new \google\appengine\CreateLoginURLResponse();
    $resp->setLoginUrl('http://www');

    $this->apiProxyMock->expectCall('user', 'CreateLoginURL', $req, $resp);
    $loginUrl = UserService::createLoginURL('http://abc');
    $this->assertEquals('http://www', $loginUrl);
    $this->apiProxyMock->verify();
  }

  public function testCreateLoginURLNoArgs() {
    $req = new \google\appengine\CreateLoginURLRequest();
    $req->setDestinationUrl('');

    $resp = new \google\appengine\CreateLoginURLResponse();
    $resp->setLoginUrl('http://www');

    $this->apiProxyMock->expectCall(
        'user', 'CreateLoginURL', $req, $resp);
    $loginUrl = UserService::createLoginURL();
    $this->assertEquals('http://www', $loginUrl);
    $this->apiProxyMock->verify();
  }

  public function testFederatedLoginNotAllowed() {
    $this->setExpectedException('\google\appengine\api\users\UsersException',
      'Action not allowed: OpenID 2.0 is disabled');

    UserService::createLoginURL('http://abc', 'xyz');
  }

  public function testCreateLoginURLArgumentError() {
    $req = new \google\appengine\CreateLoginURLRequest();
    $req->setDestinationUrl('http://abc');

    $exception = new \google\appengine\runtime\ArgumentError('x');

    $this->setExpectedException(
        '\google\appengine\runtime\ArgumentError', 'x');
    $this->apiProxyMock->expectCall(
        'user', 'CreateLoginURL', $req, $exception);

    UserService::createLoginURL('http://abc');
  }

  public function testCreateLoginURLRedirectTooLong() {
    $req = new \google\appengine\CreateLoginURLRequest();
    $req->setDestinationUrl('http://abc');

    $exception = new \google\appengine\runtime\ApplicationError(
        UserServiceError\ErrorCode::REDIRECT_URL_TOO_LONG, 'zzz');

    $this->setExpectedException(
        '\google\appengine\api\users\UsersException',
        'URL too long: http://abc');
    $this->apiProxyMock->expectCall(
        'user', 'CreateLoginURL', $req, $exception);

    UserService::createLoginURL('http://abc');
  }

  public function testCreateLoginURLNotAllowed() {
    $req = new \google\appengine\CreateLoginURLRequest();
    $req->setDestinationUrl('http://abc');

    $exception = new \google\appengine\runtime\ApplicationError(
        UserServiceError\ErrorCode::NOT_ALLOWED, 'yyy');

    $this->setExpectedException(
        '\google\appengine\api\users\UsersException', 'Action not allowed.');
    $this->apiProxyMock->expectCall(
        'user', 'CreateLoginURL', $req, $exception);

    UserService::createLoginURL('http://abc');
  }

  public function testCreateLoginOtherApplicationError() {
    $req = new \google\appengine\CreateLoginURLRequest();
    $req->setDestinationUrl('http://abc');

    $exception = new \google\appengine\runtime\ApplicationError(123, 'yyy');

    $this->setExpectedException(
        '\google\appengine\api\users\UsersException', 'Error code: 123');
    $this->apiProxyMock->expectCall(
        'user', 'CreateLoginURL', $req, $exception);

    UserService::createLoginURL('http://abc');
  }

  public function testCreateLogoutURL() {
    $req = new \google\appengine\CreateLogoutURLRequest();
    $req->setDestinationUrl('http://abc');

    $resp = new \google\appengine\CreateLogoutURLResponse();
    $resp->setLogoutUrl('http://www');

    $this->apiProxyMock->expectCall('user', 'CreateLogoutURL', $req, $resp);
    $logoutUrl = UserService::createLogoutURL('http://abc');
    $this->assertEquals('http://www', $logoutUrl);
    $this->apiProxyMock->verify();
  }

  public function testCreateLogoutURLRedirectTooLong() {
    $req = new \google\appengine\CreateLogoutURLRequest();
    $req->setDestinationUrl('http://abc');

    $exception = new \google\appengine\runtime\ApplicationError(
        UserServiceError\ErrorCode::REDIRECT_URL_TOO_LONG, 'zzz');

    $this->setExpectedException(
        '\google\appengine\api\users\UsersException',
        'URL too long: http://abc');
    $this->apiProxyMock->expectCall(
        'user', 'CreateLogoutURL', $req, $exception);

    UserService::createLogoutURL('http://abc');
  }

  public function testCreateLogoutOtherApplicationError() {
    $req = new \google\appengine\CreateLogoutURLRequest();
    $req->setDestinationUrl('http://abc');

    $exception = new \google\appengine\runtime\ApplicationError(123, 'yyy');

    $this->setExpectedException(
        '\google\appengine\api\users\UsersException', 'Error code: 123');
    $this->apiProxyMock->expectCall(
        'user', 'CreateLogoutURL', $req, $exception);

    UserService::createLogoutURL('http://abc');
  }

  public function testGetCurrentUserException() {
    putenv('AUTH_DOMAIN=google.com');
    $this->assertEquals(null, UserService::getCurrentUser());
  }

  /**
   * @dataProvider getCurrentUserDataProvider
   */
  public function testGetCurrentUser($auth_domain_env, $user_email_env) {
    putenv($auth_domain_env . '=example.com');
    putenv($user_email_env . '=bill@example.com');
    $expectedUser = new User("bill@example.com");
    $user = UserService::getCurrentUser();

    $this->assertEquals($expectedUser, $user);
    $this->assertSame("bill", $user->getNickname());
    $this->assertSame("bill@example.com", $user->getEmail());
    $this->assertSame(null, $user->getUserId());
    $this->assertSame("example.com", $user->getAuthDomain());
    $this->assertSame(null, $user->getFederatedIdentity());
    $this->assertSame(null, $user->getFederatedProvider());

    // Clear environment variables.
    putenv($auth_domain_env);
    putenv($user_email_env);
  }

  public function getCurrentUserDataProvider() {
    return [
        ['AUTH_DOMAIN', 'USER_EMAIL'],
        ['HTTP_X_APPENGINE_AUTH_DOMAIN', 'HTTP_X_APPENGINE_USER_EMAIL'],
    ];
  }

  /**
   * @dataProvider isCurrentUserAdminDataProvider
   */
  public function testIsCurrentUserAdmin($env_var_name) {
    putenv($env_var_name . '=0');
    $this->assertFalse(UserService::isCurrentUserAdmin());

    putenv($env_var_name . '=1');
    $this->assertTrue(UserService::isCurrentUserAdmin());

    // Clear the environment variable.
    putenv($env_var_name);
  }

  public function isCurrentUserAdminDataProvider() {
    $var_name = 'USER_IS_ADMIN';
    return [
        ['USER_IS_ADMIN'],
        ['HTTP_X_APPENGINE_USER_IS_ADMIN'],
    ];
  }
}
