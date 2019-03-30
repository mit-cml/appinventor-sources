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

/**
 * Handles removing (unlink()) uploaded files that are left at tmp_name.
 *
 * The shutdown hook is registered in Setup.php if $_FILES is not empty. Users
 * are expected to use move_uploaded_file() to place wanted files in proper
 * location.
 *
 * If changes are made to the $_FILES array the shutdown hook will have an
 * unmodified copy which both prevents tampering and allows empty files to be
 * removed along with others after removeEmptyFiles() cleans the array.
 *
 * GCS creates empty files when a POST request is sent with empty file fields.
 * Typically PHP would ignore the file fields signified using UPLOAD_ERR_NO_FILE
 * in the $_FILES array entry. Instead rfc1867.c is modified to pass the entries
 * along with __UNLINK__ as the file name to ensure the tmp_name is filled with
 * gs:// address and can be unlinked along with any unprocessed uploads.
 */
final class UnlinkUploads {
  const NAME_UNLINK = '__UNLINK__';

  /**
   * Remove any left over uploads.
   *
   * @param array $files
   *   Associative array of uploaded files ($_FILES).
   */
  public static function shutdownHook(array $files) {
    foreach ($files as $file) {
      // Break and allow a response if the timeout has been reached.
      if ((connection_status() & CONNECTION_TIMEOUT) == CONNECTION_TIMEOUT) {
        break;
      }
      if (isset($file['tmp_name'])) {
        if (is_array($file['tmp_name'])) {
          foreach($file['tmp_name'] as $name) {
            self::checkAndUnlinkFile($name);
          }
        } else {
          self::checkAndUnlinkFile($file['tmp_name']);
        }
      }
    }
  }

  private static function checkAndUnlinkFile($file_name) {
    // When files are moved using move_uploaded_file() they are removed from
    // is_uploaded_file() so this should avoid needless unlink() calls.
    if (isset($file_name) && is_uploaded_file($file_name)) {
      unlink($file_name);
    }
  }

  /**
   * Remove empty file entries signified by __UNLINK__ from array.
   *
   * @param array $files
   *   Associative array of uploaded files ($_FILES).
   */
  public static function removeEmptyFiles(array &$files) {
    foreach ($files as &$file) {
      if ($file['name'] == static::NAME_UNLINK) {
        $file = [
          'name' => '',
          'type' => '',
          'tmp_name' => '',
          'error' => UPLOAD_ERR_NO_FILE,
          'size' => 0,
        ];
      }
    }
  }
}
