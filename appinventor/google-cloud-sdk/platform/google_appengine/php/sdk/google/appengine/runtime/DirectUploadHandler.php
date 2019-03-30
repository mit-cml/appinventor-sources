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

use org\bovigo\vfs\vfsStream;

/**
 * Handle direct file uploads by placing contents in virtual file system.
 *
 * The PHP runtime has been modified (in rfc1867.c) to place the file contents
 * in the 'contents' key of each $_FILES entry. The handle() method moves the
 * contents into a virtual file system accessed via stream wrapper (vfsStream)
 * and alters the tmp_name to point to vfs://.
 */
final class DirectUploadHandler {
  const BASE_PATH = 'vfs://root/uploads/';

  public static function handle() {
    // An associative array of filenames and data, so we update vfs in one call.
    $upload_data = [];
    foreach ($_FILES as &$file) {
      // PHP LOL - members of the $_FILES array could be an array if the
      // mutliple file upload syntax was used.
      if (isset($file['tmp_name']) && is_array($file['tmp_name'])) {
        $count = count($file['tmp_name']);
        for ($i = 0; $i < $count; $i++) {
          if ($file['error'][$i] == UPLOAD_ERR_OK && isset($file['contents']) &&
              isset($file['contents'][$i])) {
            $upload_data[$file['tmp_name'][$i]] = $file['contents'][$i];
          }
        }
      } else {
        if ($file['error'] == UPLOAD_ERR_OK && isset($file['contents'])) {
          $upload_data[$file['tmp_name']] = $file['contents'];
        }
      }
      unset($file['contents']);
    }
    self::createVirtualFiles($upload_data);
  }

  protected static function createVirtualFiles($upload_data) {
    $name_corrected_upload_data = [];
    foreach($upload_data as $k => $v) {
      $name_corrected_upload_data[str_replace(self::BASE_PATH, "", $k)] = $v;
    }

    // vfsStream::create will take a reference, not a copy.
    vfsStream::create([
      'uploads' => $name_corrected_upload_data,
    ]);
  }
}
