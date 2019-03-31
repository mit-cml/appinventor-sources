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
 * Utility functions for the UserService API.
 *
 */

namespace google\appengine\api\users;

final class UserServiceUtil {
  const HTTP_X_APPENGINE = 'HTTP_X_APPENGINE_';

  /**
   * Retrieve an environment variable specifically for the UserService.
   *
   * Under managed VMs, the UserService environment variables are sent as
   * HTTP headers with the prefix 'HTTP_X_APPENGINE_'. This function first
   * checks if the environment variable is set, and if not checks if it is
   * set with the prefix string.
   *
   * @param string $var_name The variable name to check.
   *
   * @return The environment value, or false if not found.
   */
  public static function getUserEnvironmentVariable($var_name) {
    $result = getenv($var_name);
    if ($result === false) {
      $result = getenv(self::HTTP_X_APPENGINE . $var_name);
    }
    return $result;
  }
}
