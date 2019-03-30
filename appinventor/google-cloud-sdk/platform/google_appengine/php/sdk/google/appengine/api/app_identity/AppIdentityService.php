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


namespace google\appengine\api\app_identity;

use google\appengine\AppIdentityServiceError\ErrorCode;
use google\appengine\GetAccessTokenRequest;
use google\appengine\GetAccessTokenResponse;
use google\appengine\GetPublicCertificateForAppRequest;
use google\appengine\GetPublicCertificateForAppResponse;
use google\appengine\GetServiceAccountNameRequest;
use google\appengine\GetServiceAccountNameResponse;
use google\appengine\SignForAppRequest;
use google\appengine\SignForAppResponse;
use google\appengine\runtime\ApiProxy;
use google\appengine\runtime\ApplicationError;

/**
 * The AppIdentityService allows you to sign arbitrary byte
 * array using per app private key maintained by App Engine. You can also
 * retrieve a list of public certificates which can be used to
 * verify the signature.
 *
 * App Engine is responsible for maintaining per-application
 * private key. App Engine will keep rotating private keys
 * periodically. App Engine never releases these private keys externally.
 *
 * Since private keys are rotated periodically,
 * getPublicCertificates() could return a list of public
 * certificates. It's the caller's responsibility to try these
 * certificates one by one when doing signature verification.
 */
final class AppIdentityService {

  const PACKAGE_NAME = 'app_identity_service';
  const PARTITION_SEPARATOR = "~";
  const DOMAIN_SEPARATOR = ":";
  const MEMCACHE_KEY_PREFIX = '_ah_app_identity_';
  const EXPIRY_SAFETY_MARGIN_SECS = 300;  // To avoid any clock skew issues
  const EXPIRY_SHORT_MARGIN_SECS = 60;

  /**
   * Signs arbitrary byte array using per app private key.
   *
   * @param string $bytes_to_sign The bytes to generate the signature for.
   *
   * @throws \InvalidArgumentException If $bytes_to_sign is not a string.
   * @throws AppIdentityException If there is an error using the AppIdentity
   * service.
   *
   * @return array An array containing the elements
   * 'key_name' - the name of the key used to sign the bytes
   * 'signature' - the signature of the bytes.
   *
   */
  public static function signForApp($bytes_to_sign) {
    $req = new SignForAppRequest();
    $resp = new SignForAppResponse();

    if (!is_string($bytes_to_sign)) {
      throw new \InvalidArgumentException('$bytes_to_sign must be a string.');
    }

    $req->setBytesToSign($bytes_to_sign);

    try {
      ApiProxy::makeSyncCall(self::PACKAGE_NAME, 'SignForApp', $req, $resp);
    } catch (ApplicationError $e) {
      throw self::applicationErrorToException($e);
    }

    return [
      'key_name' => $resp->getKeyName(),
      'signature' => $resp->getSignatureBytes(),
    ];
  }

  /**
   * Get the service account name for the application.
   *
   * @throws AppIdentityException If there is an error using the AppIdentity
   * service.
   *
   * @return string The service account name.
   */
  public static function getServiceAccountName() {
    $req = new GetServiceAccountNameRequest();
    $resp = new GetServiceAccountNameResponse();

    try {
      ApiProxy::makeSyncCall(self::PACKAGE_NAME, 'GetServiceAccountName', $req,
          $resp);
    } catch (ApplicationError $e) {
      throw self::applicationErrorToException($e);
    }

    return $resp->getServiceAccountName();
  }

  /**
   * Get the list of public certifates for the application.
   *
   * @throws AppIdentityException If there is an error using the AppIdentity
   * service.
   *
   * @return PublicCertificate[] An array of the applications public
   * certificates.
   */
  public static function getPublicCertificates() {
    $req = new GetPublicCertificateForAppRequest();
    $resp = new GetPublicCertificateForAppResponse();

    try {
      ApiProxy::makeSyncCall(self::PACKAGE_NAME, 'GetPublicCertificatesForApp',
          $req, $resp);
    } catch (ApplicationError $e) {
      throw self::applicationErrorToException($e);
    }

    $result = [];

    foreach ($resp->getPublicCertificateListList() as $cert) {
      $result[] = new PublicCertificate($cert->getKeyName(),
                                        $cert->getX509CertificatePem());
    }

    return $result;
  }

  /**
   * Gets an OAuth2 access token for the application's service account from
   * the cache or generates and caches one by calling
   * getAccessTokenUncached($scopes)
   *
   * Each application has an associated Google account. This function returns
   * OAuth2 access token corresponding to the running app. Access tokens are
   * safe to cache and reuse until they expire.
   *
   * @param array $scopes The scopes to acquire the access token for.
   * Can be either a single string or an array of strings.
   *
   * @throws \InvalidArgumentException If $scopes is not a string or an array of
   * strings.
   * @throws AppIdentityException If there is an error using the AppIdentity
   * service.
   *
   * @return array An array with the following key/value pairs.
   * 'access_token' - The access token for the application.
   * 'expiration_time' - The expiration time for the access token.
   */
  public static function getAccessToken($scopes) {
    $cache_key = self::MEMCACHE_KEY_PREFIX . self::DOMAIN_SEPARATOR;
    if (is_string($scopes)) {
       $cache_key .= $scopes;
    } else if (is_array($scopes)) {
      $cache_key .= implode(self::DOMAIN_SEPARATOR, $scopes);
    } else {
      throw new \InvalidArgumentException(
          'Invalid scope ' . htmlspecialchars($scopes));
    }

    $result = self::getTokenFromCache($cache_key);

    if ($result === false) {
      $result = self::getAccessTokenUncached($scopes);

      // Cache the token.
      self::putTokenInCache($cache_key,
                            $result,
                            $result['expiration_time']);
    }
    return $result;
  }

  /**
   * Get an OAuth2 access token for the applications service account without
   * caching the result. Usually getAccessToken($scopes) should be used instead
   * which calls this method and caches the result.
   *
   * @param array $scopes The scopes to acquire the access token for.
   * Can be either a single string or an array of strings.
   *
   * @throws InvalidArgumentException If $scopes is not a string or an array of
   * strings.
   * @throws AppIdentityException If there is an error using the AppIdentity
   * service.
   *
   * @return array An array with the following key/value pairs.
   * 'access_token' - The access token for the application.
   * 'expiration_time' - The expiration time for the access token.
   */
  private static function getAccessTokenUncached($scopes) {
    $req = new GetAccessTokenRequest();
    $resp = new GetAccessTokenResponse();

    if (is_string($scopes)) {
      $req->addScope($scopes);
    } else if (is_array($scopes)) {
      foreach($scopes as $scope) {
        if (is_string($scope)) {
          $req->addScope($scope);
        } else {
          throw new \InvalidArgumentException(
            'Invalid scope ' . htmlspecialchars($scope));
        }
      }
    } else {
      throw new \InvalidArgumentException(
          'Invalid scope ' . htmlspecialchars($scopes));
    }

    try {
      ApiProxy::makeSyncCall(self::PACKAGE_NAME, 'GetAccessToken', $req, $resp);
    } catch (ApplicationError $e) {
      throw self::applicationErrorToException($e);
    }

    return [
        'access_token' => $resp->getAccessToken(),
        'expiration_time' => $resp->getExpirationTime(),
    ];
  }

  /**
   * Get the application id of an app.
   *
   * @return string The application id of the app.
   */
  public static function getApplicationId() {
    $app_id = getenv("APPLICATION_ID");
    $psep = strpos($app_id, self::PARTITION_SEPARATOR);
    if ($psep > 0) {
      $app_id = substr($app_id, $psep + 1);
    }
    return $app_id;
  }

  /**
   * Get the standard hostname of the default version of the app.
   *
   * @return string The standard hostname of the default version of the
   * application, or FALSE if the call failed.
   */
  public static function getDefaultVersionHostname() {
    return getenv("DEFAULT_VERSION_HOSTNAME");
  }

  /**
   * Add an access token to the cache.
   *
   * @param string $name The name of the token to add to the cache.
   * @param mixed $value An assoicative array containing the token and the
   * expiration time.
   * @param int $expiry_secs The unix time since epoch when the value should
   * expire.
   *
   * @access private
   */
  private static function putTokenInCache($name, $value, $expiry_secs) {
    $expiry_time_from_epoch = $expiry_secs - self::EXPIRY_SAFETY_MARGIN_SECS -
        self::EXPIRY_SHORT_MARGIN_SECS;
    $memcache = new \Memcache();
    $memcache->set($name, $value, null, $expiry_time_from_epoch);
    // Record the expiry time in the object being cached, so we can check it
    // when read from APC.
    self::putTokenInApc($name, $value, $expiry_secs);
  }

  /**
   * Put an access token into the in process cache.
   * @param string $name The name of the token to add to the cache.
   * @param mixed $value An assoicative array containing the token and the
   * expiration time.
   * @param int $expiry_secs The unix time since epoch when the value should
   * expire.
   *
   * @access private
   */
  private static function putTokenInApc($name, $value, $expiry_secs) {
    $expiry_time_from_epoch = $expiry_secs - self::EXPIRY_SAFETY_MARGIN_SECS -
        self::EXPIRY_SHORT_MARGIN_SECS;
    $cache_ttl = self::getTTLForToken($expiry_time_from_epoch);
    $value['eviction_time_epoch'] = $cache_ttl['eviction_time_epoch'];
    apc_store($name, $value, $cache_ttl['apc_ttl_in_seconds']);
  }

  /**
   * Retrieve an access token from the cache.
   *
   * @param $name String name of the token to retrieve.
   *
   * @return mixed The value of the token if it is stored in the cache,
   * otherise false.
   *
   * @access private
   */
  private static function getTokenFromCache($name) {
    $success = false;
    $result = apc_fetch($name, $success);
    if ($success && time() < $result['eviction_time_epoch']) {
      unset($result['eviction_time_epoch']);
      return $result;
    }
    $memcache = new \Memcache();
    $result = $memcache->get($name);
    // If there was a result in memcache but not in apc we can add using a
    // short timeout.
    if ($result !== false) {
      self::putTokenInApc($name, $result, $result['expiration_time']);
    }
    return $result;
  }

  /**
   * Calcualte the TTL for a cache token in APC. Will add some jitter to the
   * cache time so that all tokens do not expire simultaneuosly, and will
   * convert from unix time to number of seconds.
   *
   * @param int $cache_time The unix time that the token will expire.
   * @returns mixed An associate array with the following keys:
   * - 'eviction_time_epoch': Seconds from epoch that this item expires.
   * - 'apc_ttl_in_seconds': Seconds from now when this item expires.
   *
   * @access private
   */
  private static function getTTLForToken($cache_time) {
    // Add some jitter from the $cache_time so that all clones for an app
    // do not expire the key at the same time.
    $cache_time += rand(0, self::EXPIRY_SHORT_MARGIN_SECS);
    // APC expects a TTL in seconds, $cache_time is seconds since epoch().
    return [
      'eviction_time_epoch' => $cache_time,
      'apc_ttl_in_seconds' => $cache_time - time(),
    ];
  }

  /**
   * Converts an application error to the service specific exception.
   *
   * @param ApplicationError $application_error The application error
   *
   * @return mixed An exception that corresponds to the application error.
   *
   * @access private
   */
  private static function applicationErrorToException($application_error) {
    switch ($application_error->getApplicationError()) {
      case ErrorCode::UNKNOWN_SCOPE:
        return new \InvalidArgumentException('An unknown scope was supplied.');
      case ErrorCode::BLOB_TOO_LARGE:
        return new \InvalidArgumentException('The supplied blob was too long.');
      case ErrorCode::DEADLINE_EXCEEDED:
        return new AppIdentityException(
          'The deadline for the call was exceeded.');
      case ErrorCode::NOT_A_VALID_APP:
        return new AppIdentityException('The application is not valid.');
      case ErrorCode::UNKNOWN_ERROR:
        return new AppIdentityException(
          'There was an unknown error using the AppIdentity service.');
      case ErrorCode::GAIAMINT_NOT_INITIAILIZED:
        return new AppIdentityException(
          'There was a GAIA error using the AppIdentity service.');
      case ErrorCode::NOT_ALLOWED:
        return new AppIdentityException('The call is not allowed.');
      default:
        return new AppIdentityException(
          'The AppIdentity service threw an unexpected error.');
    }
  }
}
