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
 * @file
 * Provide basic mod_rewrite like functionality.
 *
 * Pass through requests for root php files and forward all other requests to
 * index.php with $_GET['q'] equal to path. The following are examples that
 * demonstrate how a request using mod_rewrite.php will appear to a PHP script.
 *
 * - /install.php: install.php
 * - /update.php?op=info: update.php?op=info
 * - /foo/bar: index.php?q=/foo/bar
 * - /: index.php?q=/
 */

$path = parse_url($_SERVER['REQUEST_URI'], PHP_URL_PATH);

// Provide mod_rewrite like functionality. If a php file in the root directory
// is explicitly requested then load the file, otherwise load index.php and
// set get variable 'q' to $_SERVER['REQUEST_URI'].
if (dirname($path) == '/' && pathinfo($path, PATHINFO_EXTENSION) == 'php') {
  $file = pathinfo($path, PATHINFO_BASENAME);
} else {
  $file = 'index.php';

  // Provide mod_rewrite like functionality by using the path which excludes
  // any other part of the request query (ie. ignores ?foo=bar).
  $_GET['q'] = $path;
}

// Override the script name to simulate the behavior without mod_rewrite.php.
// Ensure that $_SERVER['SCRIPT_NAME'] always begins with a / to be consistent
// with HTTP request and the value that is normally provided.
$_SERVER['SCRIPT_NAME'] = '/' . $file;
require $file;
