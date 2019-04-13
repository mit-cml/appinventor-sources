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
 * SPL (Standard PHP Library) function definitions to replace originals.
 *
 * In order to allow unit testing on an unmodified PHP runtime the function
 * implementation definition is contained by this class. The actual function
 * name is mapped in Setup.php which simply calls the implementation.
 */

namespace google\appengine\runtime;

use google\appengine\api\modules\ModulesService;
use google\appengine\util\StringUtil;
use org\bovigo\vfs\vfsStream;

final class SplOverride {
  const DEFAULT_CONTENT_TYPE = 'binary/octet-stream';
  const DEFAULT_TMPFILE_MODE = 0600;

  /**
   * Gets the standard host name for the local machine.
   *
   * @return bool|string
   *   a string with the hostname on success, otherwise FALSE is returned.
   *
   * @see http://php.net/gethostname
   */
  public static function gethostname() {
    // In order to be consistent with PHP core implementation, wrap any
    // exception and return false.
    try {
      return ModulesService::getHostname();
    }
    catch (\Exception $e) {
      return false;
    }
  }

  /**
   * Moves an uploaded file to a new location.
   *
   * @param string $filename
   *   The filename of the uploaded file.
   * @param string $destination
   *   The destination of the moved file.
   * @param array $context_options
   *   An associative array of stream context options. The options will be
   *   merged with defaults and passed to stream_context_create().
   *
   * @see http://php.net/move_uploaded_file
   */
  public static function move_uploaded_file($filename, $destination,
                                            array $context_options = null) {
    // move_uploaded_file() does not support moving a file between two different
    // stream wrappers. Other file handling functions like rename() have the
    // same problem, but copy() works since it explicitly sends the contents to
    // the new source. As such move_uploaded_file() is replaced by
    // is_uploaded_file(), copy(), and unlink() the old file.
    //
    // This also supports upload proxying during which the file may be remotely
    // located and referenced by a stream wrapper. In that case $filename may be
    // something like gs://... and $destination public://... which may end up on
    // the same remote filesystem, but PHP does not make the distinction. Of
    // course, this also means the file can be moved between two different file
    // systems.
    //
    // In the simple case gs:// to gs:// rename() will be called first and
    // invoke the more performant operation.
    if (is_uploaded_file($filename)) {
      // Either use the user provided context options, otherwise use the default
      // context with the Content-Type overridden.
      if ($context_options !== null) {
        $context = stream_context_create($context_options);
      } else {
        // Default to content type provided in $_FILES array.
        $context = stream_context_get_default([
          'gs' => ['Content-Type' => static::lookupContentType($filename)],
        ]);
      }

      // Attempt rename() which is less expensive if the origin and destination
      // use the same stream wrapper, otherwise perform copy() and unlink().
      if (@rename($filename, $destination, $context)) {
        static::removeUploadedFile($filename);
        return true;
      }
      if (copy($filename, $destination, $context) &&
          unlink($filename, $context)) {
        static::removeUploadedFile($filename);
        return true;
      }
    }
    return false;
  }

  /**
   * Lookup the content type associated with an uploaded file.
   *
   * @param string $filename
   *   The filename of the uploaded file.
   * @return
   *   Content type associated with filename, otherwise DEFAULT_CONTENT_TYPE.
   */
  private static function lookupContentType($filename) {
    foreach ($_FILES as $file) {
      if ($file['tmp_name'] == $filename) {
        return $file['type'] ?: static::DEFAULT_CONTENT_TYPE;
      }
    }
    return static::DEFAULT_CONTENT_TYPE;
  }

  /**
   * Remove file from uploaded files list.
   *
   * Provided by the GAE extension, otherwise ignore if not present.
   *
   * @param string $filename
   *   The filename of the uploaded file.
   */
  private static function removeUploadedFile($filename) {
    if (function_exists('__remove_uploaded_file')) {
      __remove_uploaded_file($filename);
    }
  }

  /**
   * Create file with unique file name.
   *
   * @param string $dir
   *   The directory where the temporary filename will be created.
   * @param string $prefix
   *   The prefix of the generated temporary filename.
   * @return bool|string
   *   a string with the new temporary file name on success, otherwise FALSE.
   *
   * @see http://php.net/manual/en/function.tempnam.php
   */
  public static function tempnam($dir, $prefix) {
    // Force $dir into a VFS temp path if it's not already one.
    $temp_root = static::sys_get_temp_dir();
    if (!StringUtil::startsWith($dir, $temp_root)) {
      $dir = $temp_root . '/' . str_replace('\\', '/', $dir);
    }

    // Create all intermediate directories if needed.
    @mkdir($dir, 0777, true);

    // Generate a unique non-existing file name.
    for ($retry = 0; $retry < 10 || file_exists($filename); $retry++) {
      $filename = $dir . '/' . uniqid($prefix, true);
    }
    if (file_exists($filename)) {
      trigger_error('Fail to generate a unique name for the temporary file',
                    E_USER_ERROR);
      return false;
    }

    // tempnam requires the file to be created with permission set to 0600.
    if (touch($filename) === false ||
        chmod($filename, static::DEFAULT_TMPFILE_MODE) === false) {
      trigger_error('Fail to create and change permission of temporary file ' .
                    $filename, E_USER_ERROR);
      return false;
    }

    return $filename;
  }

  /**
   * Returns directory path used for temporary files.
   *
   * @return string
   *   a string with the path of the temporary directory.
   *
   * @see http://php.net/function.sys-get-temp-dir.php
   */
  public static function sys_get_temp_dir() {
    VirtualFileSystem::getInstance()->initialize();
    return vfsStream::url('root/temp');
  }

}
