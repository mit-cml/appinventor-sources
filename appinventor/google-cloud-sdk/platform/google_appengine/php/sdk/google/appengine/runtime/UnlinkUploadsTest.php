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

class UnlinkUploadsTest extends \PHPUnit_Framework_TestCase {
  protected $files;

  protected function setUp() {
    vfsStream::setup('root');

    vfsStream::create(['uploads' => [
      'upload 0 contents',
      'upload 1 contents',
      'upload 2 contents',
      'upload 3 contents',
      'upload 4 contents',
    ]]);

    $this->files = [
      ['tmp_name' => vfsStream::url('root/uploads/0')],
      ['tmp_name' => vfsStream::url('root/uploads/1')],
      // Will not be listed in uploaded files and thus should be ignored.
      ['tmp_name' => vfsStream::url('root/uploads/2')],
      [], //Ignored if 'tmp_name' is empty.
      // Mulitplie files uploaded as an array
      ['tmp_name' => [
          vfsStream::url('root/uploads/3'),
          vfsStream::url('root/uploads/4'),
        ],
      ],
    ];

    is_uploaded_file('', [
      $this->files[0]['tmp_name'],
      $this->files[1]['tmp_name'],
      // Not listing file[2].
      $this->files[4]['tmp_name'][0],
      $this->files[4]['tmp_name'][1],
    ]);

    $this->assertFilesExist();
  }

  protected function assertFilesExist() {
    $this->assertTrue(file_exists($this->files[0]['tmp_name']));
    $this->assertTrue(file_exists($this->files[1]['tmp_name']));
    $this->assertTrue(file_exists($this->files[2]['tmp_name']));
    $this->assertTrue(file_exists($this->files[4]['tmp_name'][0]));
    $this->assertTrue(file_exists($this->files[4]['tmp_name'][1]));
  }

  /**
   * Ensure uploaded files are properly removed.
   */
  public function testShutdownHook() {
    UnlinkUploads::shutdownHook($this->files);

    $this->assertFalse(file_exists($this->files[0]['tmp_name']));
    $this->assertFalse(file_exists($this->files[1]['tmp_name']));
    $this->assertTrue(file_exists($this->files[2]['tmp_name'])); // Ignored.
    $this->assertFalse(file_exists($this->files[4]['tmp_name'][0]));
    $this->assertFalse(file_exists($this->files[4]['tmp_name'][1]));
  }

  /**
   * Ensure shutdown hook terminates if connection status is TIMEOUT.
   */
  public function testShutdownHookTimeout() {
    connection_status(CONNECTION_TIMEOUT);

    UnlinkUploads::shutdownHook($this->files);

    // Shutdown hook should exist on first loop so all files should exist.
    $this->assertFilesExist();
  }

  public function testRemoveEmptyFiles() {
    $files = [
      [
        'name' => '__UNLINK__',
        'type' => 'text/plain',
        'tmp_name' => 'gs://bucket/foo1',
        'error' => UPLOAD_ERR_OK,
        'size' => 17,
      ],
      [
        'name' => 'somefile.txt',
        'type' => 'text/plain',
        'tmp_name' => 'gs://bucket/foo2',
        'error' => UPLOAD_ERR_OK,
        'size' => 17,
      ],
    ];
    $expected = [
      [
        'name' => '',
        'type' => '',
        'tmp_name' => '',
        'error' => UPLOAD_ERR_NO_FILE,
        'size' => 0,
      ],
      [
        'name' => 'somefile.txt',
        'type' => 'text/plain',
        'tmp_name' => 'gs://bucket/foo2',
        'error' => UPLOAD_ERR_OK,
        'size' => 17,
      ],
    ];

    UnlinkUploads::removeEmptyFiles($files);
    $this->assertEquals($expected, $files);
  }
}

// Override SPL connection_status() since UnlinkUploads is in same namespace.
function connection_status($status = null) {
  static $_status = CONNECTION_NORMAL;

  if (isset($status)) {
    $_status = $status;
  }

  return $_status;
}

// Override SPL is_uploaded_file() since UnlinkUploads is in same namespace.
function is_uploaded_file($filename, array $uploaded_files = null) {
  static $_uploaded_files = [];

  if (isset($uploaded_files)) {
    $_uploaded_files = $uploaded_files;
  }

  return in_array($filename, $_uploaded_files);
}
