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

use google\appengine\util\StringUtil;

/**
 * Represents the details of a single request and may optionally contain
 * application logs written during the request using the <code>syslog</code>
 * function.
 */
final class RequestLog {

  // The underlying protocol buffer.
  private $pb;

  private $app_logs;

  /**
   * @internal
   *
   * @param RequestLog $pb Underlying protocol buffer.
   */
  public function __construct(\google\appengine\RequestLog $pb) {
    $this->pb = $pb;
  }

  /**
   * @return string The application ID that handled this request.
   */
  public function getAppId() {
    return $this->pb->getAppId();
  }

  /**
   * @return AppLogLine[] The {@link AppLogLine}s added during this request.
   */
  public function getAppLogs() {
    if (!isset($app_logs)) {
      $app_logs = [];
      foreach ($this->pb->getLineList() as $line) {
        $app_logs[] = new AppLogLine($line);
      }
    }
    return $app_logs;
  }

  /**
   * @return string The Apache-format combined log entry for this request. While
   * the information in this field can be constructed from the rest of this
   * message, we include this method for convenience.
   */
  public function getCombined() {
    return $this->pb->getCombined();
  }

  /**
   * @return double The time at which this request began processing, in
   * microseconds since the Unix epoch.
   */
  public function getStartTimeUsec() {
    return (double) $this->pb->getStartTime();
  }

  /**
   * @return DateTime The same value as {@link getStartTimeUsec()} as a DateTime
   * instance accurate to the second. For greater accuracy use
   * {@link getStartTimeUsec()}.
   */
  public function getStartDateTime() {
    $result = new \DateTime();
    $result->setTimestamp($this->getStartTimeUsec() / 1e6);
    return $result;
  }

  /**
   * @return double The time at which the request finished processing,
   * in microseconds since the Unix epoch.
   */
  public function getEndTimeUsec() {
    return (double) $this->pb->getEndTime();
  }

  /**
   * @return DateTime The same value as {@link getEndTimeUsec()} as a DateTime
   * instance accurate to the second. For greater accuracy use
   * {@link getEndTimeUsec()}.
   */
  public function getEndDateTime() {
    $result = new \DateTime();
    $result->setTimestamp($this->getEndTimeUsec() / 1e6);
    return $result;
  }

  /**
   * @return string The Internet host and port number of the resource being
   * requested.
   */
  public function getHost() {
    return $this->pb->getHost();
  }

  /**
   * @return string The HTTP version of this request.
   */
  public function getHttpVersion() {
    return $this->pb->getHttpVersion();
  }

  /**
   * @return string Mostly-unique identifier for the instance that handled the
   * request, or the empty string.
   */
  public function getInstanceKey() {
    return $this->pb->getInstanceKey();
  }

  /**
   * @return string The origin IP address of this request.
   * App Engine uses an origin IP address from the 0.0.0.0/8 range when the
   * request is to a web hook.
   * Some examples of web hooks are task queues, cron jobs and warming requests.
   */
  public function getIp() {
    return $this->pb->getIp();
  }

  /**
   * @return double The time required to process this request in microseconds.
   */
  public function getLatencyUsec() {
    return (double) $this->pb->getLatency();
  }

  /**
   * @return string The request's HTTP method (e.g., GET, PUT, POST).
   */
  public function getMethod() {
    return $this->pb->getMethod();
  }

  /**
   * @return string The nickname of the user that made the request. An empty
   * string is returned if the user is not logged in.
   */
  public function getNickname() {
    return $this->pb->getNickname();
  }

  /**
   * @return string A url safe value that may be used as an option to
   * <code>LogService::fetch($options)</code> to continue reading after this
   * log.
   */
  public function getOffset() {
    if ($this->pb->hasOffset()) {
      $offset = $this->pb->getOffset()->serializeToString();
      return StringUtil::base64UrlEncode($offset);
    }
    return false;
  }

  /**
   * @return double The time, in microseconds, that this request spent in the
   * pending request queue, if it was pending at all.
   */
  public function getPendingTimeUsec() {
    return (double) $this->pb->getPendingTime();
  }

  /**
   * @return string The referrer URL of this request.
   */
  public function getReferrer() {
    return $this->pb->getReferrer();
  }

  /**
   * @return integer The module instance that handled the request if
   * manual_scaling or basic_scaling is configured or -1 for automatic_scaling.
   */
  public function getInstanceIndex() {
    return $this->pb->getReplicaIndex();
  }

  /**
   * @return string A globally unique identifier for a request, based on the
   * request's starting time.
   */
  public function getRequestId() {
    return $this->pb->getRequestId();
  }

  /**
   * @return string The resource path on the server requested by the client.
   * Contains only the path component of the request URL.
   */
  public function getResource() {
    return $this->pb->getResource();
  }

  /**
   * @return integer The size (in bytes) of the response sent back to the
   * client.
   */
  public function getResponseSize() {
    return $this->pb->getResponseSize();
  }

  /**
   * @return integer The HTTP response status of this request.
   */
  public function getStatus() {
    return $this->pb->getStatus();
  }

  /**
   * @return string The request's task name, if this request was generated via
   * the Task Queue API.
   */
  public function getTaskName() {
    return $this->pb->getTaskName();
  }

  /**
   * @return string The request's queue name, if this request was generated via
   * the Task Queue API.
   */
  public function getTaskQueueName() {
    return $this->pb->getTaskQueueName();
  }

  /**
   * @return string The file or class within the URL mapping used for this
   * request.
   * Useful for tracking down the source code which was responsible for
   * managing the request, especially for multiply mapped handlers.
   */
  public function getUrlMapEntry() {
    return $this->pb->getUrlMapEntry();
  }

  /**
   * @return string The user agent used to make this request.
   */
  public function getUserAgent() {
    return $this->pb->getUserAgent();
  }

  /**
   * @return string The version of the application that handled this request.
   */
  public function getVersionId() {
    return $this->pb->getVersionId();
  }

  /**
   * @return string The version of the application that handled this request.
   */
  public function getModuleId() {
    return $this->pb->getModuleId();
  }

  /**
   * @return boolean Whether or not this request has finished processing. If
   * not, this request is still active.
   */
  public function isFinished() {
    return $this->pb->getFinished();
  }

  /**
   * @return boolean Whether or not this request was a loading request.
   */
  public function isLoadingRequest() {
    return $this->pb->getWasLoadingRequest();
  }

  /**
   * @return string App Engine Release, e.g. "1.8.4"
   */
  public function getAppEngineRelease() {
    return $this->pb->getAppEngineRelease();
  }
}
