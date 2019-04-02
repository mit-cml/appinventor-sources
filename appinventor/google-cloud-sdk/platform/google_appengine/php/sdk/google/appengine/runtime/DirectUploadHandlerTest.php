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

class DirectUploadHandlerTest extends \PHPUnit_Framework_TestCase {
  public static function setUpBeforeClass() {
    VirtualFileSystem::getInstance()->initialize();
  }

  public function testHandler() {
    // Add file entries that mimics what the modified php runtime provides.
    $_FILES = [
      // Uploaded properly and should be processed.
      'file1' => [
        'name' => 'test1.txt',
        'type' => 'text/plain',
        'contents' => 'Hello world',
        'tmp_name' => 'vfs://root/uploads/0',
        'error' => UPLOAD_ERR_OK,
        'size' => 11, // strlen('hello world')
      ],
      // Actual error that should be left alone.
      'file2' => [
        'name' => 'test2.txt',
        'type' => 'text/plain',
        'contents' => '',
        'tmp_name' => 'vfs://root/uploads/1',
        'error' => UPLOAD_ERR_INI_SIZE,
        'size' => 0,
      ],
    ];

    DirectUploadHandler::handle();

    // 'contents' key was removed, but everything else untouched.
    $this->assertEquals([
      'file1' => [
        'name' => 'test1.txt',
        'type' => 'text/plain',
        'tmp_name' => 'vfs://root/uploads/0',
        'error' => UPLOAD_ERR_OK,
        'size' => 11,
      ],
      'file2' => [
        'name' => 'test2.txt',
        'type' => 'text/plain',
        'tmp_name' => 'vfs://root/uploads/1',
        'error' => UPLOAD_ERR_INI_SIZE,
        'size' => 0,
      ],
    ], $_FILES);

    // Ensure contents was moved into vfs:// stream where applicable.
    $this->assertEquals('Hello world',
                        file_get_contents('vfs://root/uploads/0'));
    $this->assertFalse(file_exists('vfs://root/uploads/1'));
  }

  public function testMultipleUploadHandler() {
    // Add file entries that mimics what the modified php runtime provides.
    $_FILES = [
      // Uploaded properly and should be processed.
      'file1' => [
        'name' => 'test1.txt',
        'type' => 'text/plain',
        'contents' => 'Hello world',
        'tmp_name' => 'vfs://root/uploads/0',
        'error' => UPLOAD_ERR_OK,
        'size' => 11,  // strlen('hello world')
      ],
      // Actual error that should be left alone.
      'file2' => [
        'name' => 'test2.txt',
        'type' => 'text/plain',
        'contents' => 'Hello world 2',
        'tmp_name' => 'vfs://root/uploads/1',
        'error' => UPLOAD_ERR_OK,
        'size' => 13,  // strlen('hello world 2')
      ],
    ];

    DirectUploadHandler::handle();

    // 'contents' key was removed, but everything else untouched.
    $this->assertEquals([
      'file1' => [
        'name' => 'test1.txt',
        'type' => 'text/plain',
        'tmp_name' => 'vfs://root/uploads/0',
        'error' => UPLOAD_ERR_OK,
        'size' => 11,
      ],
      'file2' => [
        'name' => 'test2.txt',
        'type' => 'text/plain',
        'tmp_name' => 'vfs://root/uploads/1',
        'error' => UPLOAD_ERR_OK,
        'size' => 13,
      ],
    ], $_FILES);

    // Ensure contents was moved into vfs:// stream where applicable.
    $this->assertEquals('Hello world 2',
                        file_get_contents('vfs://root/uploads/1'));
    $this->assertEquals('Hello world',
                        file_get_contents('vfs://root/uploads/0'));
  }

}
