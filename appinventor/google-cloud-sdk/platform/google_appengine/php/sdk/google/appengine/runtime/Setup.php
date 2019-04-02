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
 * Performs any required initialization before the user's script is run.
 */

namespace google\appengine\runtime {
  use google\appengine\ext\session\MemcacheSessionHandler;

  // Ensure that the class autoloader is the first include.
  require_once 'google/appengine/runtime/autoloader.php';
  require_once 'google/appengine/runtime/Memcache.php';
  require_once 'google/appengine/runtime/Memcached.php';

  // Setup the Memcache session handler
  MemcacheSessionHandler::configure();

  if (!empty($_FILES)) {
    if (ini_get('google_app_engine.direct_file_upload')) {
      VirtualFileSystem::getInstance()->initialize();
      DirectUploadHandler::handle();
    }
    register_shutdown_function(
      'google\appengine\runtime\UnlinkUploads::shutdownHook', $_FILES);
    UnlinkUploads::removeEmptyFiles($_FILES);
  }

  register_shutdown_function('google\appengine\api\log\LogService::flush');

  // Setup the GS stream wrapper
  $url_flags = STREAM_IS_URL;
  if (GAE_INCLUDE_REQUIRE_GS_STREAMS === 1) {
    // By clearing the STREAM_IS_URL flag we allow this stream handler to be
    // used in include & require calls.
    $url_flags = 0;
  }

  stream_wrapper_register('gs',
      '\google\appengine\ext\cloud_storage_streams\CloudStorageStreamWrapper',
      $url_flags);

  if (ini_get('google_app_engine.enable_curl_lite')) {
    require_once 'google/appengine/runtime/CurlLiteStub.php';
  }
}
