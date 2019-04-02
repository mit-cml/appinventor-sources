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

/**
 * Contains the details of a single application log created by calling
 * @link http://php.net/manual/en/function.syslog.php syslog within the context
 * of a request.
 */
final class AppLogLine {
  private $pb;

  /**
   * @param LogLine $pb The underlying protocol buffer.
   *
   * @internal
   */
  public function __construct(\google\appengine\LogLine $pb) {
    $this->pb = $pb;
  }

  /**
   * The severity of the log message. This differs from the level passed to
   * <code>syslog</code> as described in @link LogService.
   *
   * @return integer The log severity.
   */
  public function getLevel() {
    return $this->pb->getLevel();
  }

  /**
   * @return string The message logged by the application.
   */
  public function getMessage() {
    return $this->pb->getLogMessage();
  }

  /**
   * @return double The time the log was created in microseconds since the
   * Unix epoch.
   */
  public function getTimeUsec() {
    return (double) $this->pb->getTime();
  }

  /**
   * Returns The same value as {@link getTimeUsec()} as a DateTime.
   * @return DateTime The time the log was created accurate to the second.
   */
  public function getDateTime() {
    $result = new \DateTime();
    $result->setTimestamp((double) $this->pb->getTime() / 1e6);
    return $result;
  }
}
