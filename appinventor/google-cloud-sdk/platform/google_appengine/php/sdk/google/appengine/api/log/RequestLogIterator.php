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
 */

namespace google\appengine\api\log;

use google\appengine\LogReadRequest;
use google\appengine\LogReadResponse;
use google\appengine\runtime\ApiProxy;

/**
 * Allows request logs to be iterated using a standard foreach statement but
 * fetches the results in batches as needed.
 */
final class RequestLogIterator implements \Iterator {
  use ApiProxyAccess;

  // Protocol buffer request containing the parsed fetch options.
  private $request;

  // Original offset value of the request to allow rewind() to return to.
  private $origin;

  // The current response including request logs.
  private $response = null;

  // Index into the request logs included in the $response.
  private $position = 0;

  // Cached current request log in case current() is called multiple times.
  private $current = null;

  /**
   * @internal
   *
   * @param LogReadRequest $request The underlying protocol buffer.
   */
  public function __construct(\google\appengine\LogReadRequest $request) {
    $this->request = $request;

    // Remember start position to allow rewind() to return.
    if ($request->hasOffset()) {
      $this->origin = $request->getOffset();
    }
  }

  public function current() {
    if (!$this->valid()) {
      throw new \LogicException('Invalid iterator state');
    }

    // Maintain current item in case current called multiple times.
    if ($this->current === null) {
      $this->current = new RequestLog($this->response->getLog($this->position));
    }
    return $this->current;
  }

  public function next() {
    if (!$this->valid()) {
      throw new \LogicException('Invalid iterator state');
    }

    // Free up memory.
    $this->response->getLogList()[$this->position] = null;
    $this->current = null;
    $this->position++;

    // Fetch more logs if needed and we have not read all (has offset).
    if ($this->position >= $this->response->getLogSize() &&
        $this->response->hasOffset()) {
      $this->fetch();
    }
  }

  private function fetch() {
    $this->response = self::readLogs($this->request);

    // Update the request with the new offset for next fetch.
    if ($this->response->hasOffset()) {
      $new_offset = $this->response->getOffset();
      $this->request->mutableOffset()->copyFrom($new_offset);
    }
    $this->position = 0;
  }

  public function valid() {
    // Response can only be null after rewind.
    if ($this->response === null) {
      $this->fetch();
    }
    return $this->position < $this->response->getLogSize();
  }

  /**
   * Reset to initial state. Called at start of foreach statement.
   */
  public function rewind() {
    // Reset request to start from original offset.
    if ($this->response !== null) {
      if (isset($this->origin)) {
        $this->request->getOffset()->copyFrom($this->origin);
      } else {
        $this->request->clearOffset();
      }
    }

    $this->current = null;
    $this->response = null;
    $this->position = 0;
  }

  /**
   * @return null Non-associative iterator.
   */
  public function key() {
    return null;
  }
}
