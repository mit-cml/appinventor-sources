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
 * Test helpers shared between various tasks queue tests.
 */

namespace google\appengine\api\taskqueue;

// This mocks out PHP's microtime() function in the taskqueue namespace.
// A test that wants to mock out microtime should call MockMicrotime::reset
// which will cause the autoloader to include this file.
// For each expected call to microtime the test should call
// MockMicrotime::expect with the value that should be returned from the call.
// Any unexpected calls to microtime will return 0.
function microtime($get_as_float = false) {
  if (!$get_as_float) {
    die('microtime called with get_as_float=false');
  }
  if (empty($GLOBALS['microtime'])) {
    return 0;
  }
  $result = array_shift($GLOBALS['microtime']);
  return $result;
}


/**
 * Class to control the values returned from microtime().
 */
class MockMicrotime {
  /**
   * Reset the mock microtime() function - This clears any potential expected
   * calls.
   */
  public static function reset() {
    $GLOBALS['microtime'] = [];
  }

  /**
   * Add an expected call to microtime().
   *
   * @param float $result The result to return from microtime() when called.
   */
  public static function expect($result) {
    array_push($GLOBALS['microtime'], $result);
  }
}
