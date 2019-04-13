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
namespace google\appengine\datastore;

require_once 'google/appengine/datastore/datastore_v4_pb.php';

use \google\appengine\datastore\v4\Error\ErrorCode;

class DatastoreV4ProtoTest extends\PHPUnit_Framework_TestCase {
  public function testTrivial() {
    $this->assertEquals(1, ErrorCode::BAD_REQUEST);
  }
}
