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

use google\appengine\base\VoidProto;
use google\appengine\FlushRequest;
use google\appengine\LogReadRequest;
use google\appengine\LogReadResponse;
use google\appengine\LogServiceError\ErrorCode;
use google\appengine\runtime\ApiProxy;
use google\appengine\runtime\ApplicationError;
use google\appengine\util\StringUtil;
use google\appengine\UserAppLogGroup;

/**
 * The LogService allows an application to query for request and application
 * logs. Application logs are added to a the current request log by calling
 * <code><a href="https://secure.php.net/manual/en/function.syslog.php">
 * syslog(int $priority, string $message)</a></code>. The $priority used when
 * creating the application log is translated into a different scale of severity
 * used by the LogService based on the following mappings,
 * <ul>
 *   <li>LOG_DEBUG => LogService::LEVEL_DEBUG</li>
 *   <li>LOG_INFO => LogService::LEVEL_INFO</li>
 *   <li>LOG_NOTICE => LogService::LEVEL_INFO</li>
 *   <li>LOG_WARNING => LogService::LEVEL_WARNING</li>
 *   <li>LOG_ERR => LogService::LEVEL_ERROR</li>
 *   <li>LOG_CRIT => LogService::LEVEL_CRITICAL</li>
 *   <li>LOG_ALERT => LogService::LEVEL_CRITICAL</li>
 *   <li>LOG_EMERG => LogService::LEVEL_CRITICAL</li>
 * </ul>
 *
 * When fetching application logs or filtering request logs by severity use the
 * LogService's severity levels.
 */
final class LogService {
   use ApiProxyAccess;

  /**
   * Constants for application log levels.
   */
  const LEVEL_DEBUG = 0;
  const LEVEL_INFO = 1;
  const LEVEL_WARNING = 2;
  const LEVEL_ERROR = 3;
  const LEVEL_CRITICAL = 4;

  /**
   * The maximum number of request logs returned in each batch.
   */
  const MAX_BATCH_SIZE = 1000;

  // Map syslog priority levels to appengine severity levels.
  private static $syslog_priority_map = array(
      LOG_EMERG => self::LEVEL_CRITICAL,
      LOG_ALERT => self::LEVEL_CRITICAL,
      LOG_CRIT => self::LEVEL_CRITICAL,
      LOG_ERR => self::LEVEL_ERROR,
      LOG_WARNING => self::LEVEL_WARNING,
      LOG_NOTICE => self::LEVEL_INFO,
      LOG_INFO => self::LEVEL_INFO,
      LOG_DEBUG => self::LEVEL_DEBUG);

  // A UserAppLogGroup object that holds the currently buffered logs.
  private static $app_log_group = null;

  // Maximum number of log entries to buffer before they are automaticallly
  // flushed upon adding the next log entry.
  private static $auto_flush_entries = 100;

  // Maximum size of logs to buffer before they are automaticallly flushed
  // upon adding the next log entry.
  private static $auto_flush_bytes = 524288;

  // Maximum amount of time in seconds before the buffered logs are
  // automatically flushed upon adding the next log entry.
  private static $flush_time_limit = 60;

  private static $last_flush_time;

  // Validation patterns copied from google/appengine/api/logservice/logservice.py
  private static $MAJOR_VERSION_ID_REGEX =
      '/^(?:(?:((?!-)[a-z\d\-]{1,63}):)?)((?!-)[a-z\d\-]{1,100})$/';
  private static $REQUEST_ID_REGEX = '/^[\da-fA-F]+$/';

  /**
   * Get request logs matching the given options in reverse chronological
   * order of request end time.
   *
   * @param array $options Optional associateive arrary of filters and
   * modifiers from following:
   *
   * <ul>
   *   <li>'start_time': <code>DateTime or numeric</code> The earliest
   *   completion time or last-update time for request logs. If the value is
   *   numeric it represents microseconds since Unix epoch.</li>
   *   <li>'end_time': <code>DateTime or numeric</code> The latest completion
   *   time or last-update time for request logs. If the value is numeric it
   *   represents microseconds since Unix epoch.</li>
   *   <li>'offset': <code>string</code> The url-safe offset value from a
   *   <code>RequestLog</code> to continue iterating after.</li>
   *   <li>'minimum_log_level': <code>integer</code> Only return request logs
   *   containing at least one application log of this severity or higher.
   *   Works even if include_app_logs is not <code>true</code></li>
   *   <li>'include_incomplete': <code>boolean</code> Should incomplete request
   *   logs be included. The default is <code>false</code> - only completed
   *   logs are returned</li>
   *   <li>'include_app_logs': <code>boolean</code> Should application logs be
   *   returned. The default is <code>false</code> - application logs are not
   *   returned with their containing request logs.</li>
   *   <li>'versions': <code>array</code> The versions of the default module
   *   for which to fetch request logs. Only one of 'versions' and
   *   'module_versions' can be used.</li>
   *   <li>'module_versions': <code>arrary</code> An associative array of module
   *   names to versions for which to fetch request logs.  Each module name may
   *   be mapped to either a single <code>string</code> version or an <code>
   *   array</code> of versions.</li>
   *   <li>'batch_size': <code>integer</code> The number of request logs to
   *   pre-fetch while iterating.</li>
   * </ul>
   *
   * @return Iterator The matching RequestLog items.
   */
  public static function fetch(array $options = []) {
    $request = new LogReadRequest();
    $request->setAppId(getenv('APPLICATION_ID'));

    // Required options default values - overridden by options below.
    $batch_size = 20;
    $include_app_logs = false;
    $include_incomplete = false;

    foreach ($options as $key => $value) {
      switch ($key) {
        case 'start_time':
          if (is_numeric($value)) {
            $usec = (double) $value;
          } else if ($value instanceof \DateTime) {
            $usec = self::dateTimeToUsecs($value);
          } else {
            self::optionTypeException($key, $value, 'DateTime or numeric');
          }
          $request->setStartTime($usec);
          break;
        case 'end_time':
          if (is_numeric($value)) {
            $usec = (double) $value;
          } else if ($value instanceof \DateTime) {
            $usec = self::dateTimeToUsecs($value);
          } else {
            self::optionTypeException($key, $value, 'DateTime or numeric');
          }
          $request->setEndTime($usec);
          break;
        case 'offset':
          if (!is_string($value)) {
            self::optionTypeException($key, $value, 'string');
          }
          $decoded = StringUtil::base64UrlDecode($value);
          $request->mutableOffset()->parseFromString($decoded);
          break;
        case 'minimum_log_level':
          if (!is_int($value)) {
            self::optionTypeException($key, $value, 'integer');
          }
          if ($value > self::LEVEL_CRITICAL ||
              $value < self::LEVEL_DEBUG) {
            throw new \InvalidArgumentException(
                "Option 'minimum_log_level' must be from " .
                self::LEVEL_DEBUG . " to " .
                self::LEVEL_CRITICAL);
          }
          $request->setMinimumLogLevel($value);
          break;
        case 'include_incomplete':
          if (!is_bool($value)) {
            self::optionTypeException($key, $value, 'boolean');
          }
          $include_incomplete = $value;
          break;
        case 'include_app_logs':
          if (!is_bool($value)) {
            self::optionTypeException($key, $value, 'boolean');
          }
          $include_app_logs = $value;
          break;
        case 'module_versions':
          if (!is_array($value)) {
            self::optionTypeException($key, $value, 'array');
          }
          if (isset($options['versions'])) {
              throw new \InvalidArgumentException(
                  "Only one of 'versions' or " .
                  "'module_versions' may be set");
          }
          foreach ($value as $module => $versions) {
            if (!is_string($module)) {
              throw new \InvalidArgumentException(
                  'Server must be a string but was ' .
                  self::typeOrClass($module));
            }
            // Versions can be a single string or an array of strings.
            if (is_array($versions)) {
              foreach ($versions as $version) {
                if (!is_string($version)) {
                  throw new \InvalidArgumentException(
                      'Server version must be a string but was ' .
                      self::typeOrClass($version));
                }
                $module_version = $request->addModuleVersion();
                if ($module !== 'default') {
                  $module_version->setModuleId($module);
                }
                $module_version->setVersionId($version);
              }
            } else if (is_string($versions)) {
              $module_version = $request->addModuleVersion();
              $module_version->setModuleId($module);
              $module_version->setVersionId($versions);
            } else {
              throw new \InvalidArgumentException(
                  'Server version must be a string or array but was ' .
                  self::typeOrClass($versions));
            }
          }
          break;
        case 'versions':
          if (!is_array($value)) {
            self::optionTypeException($key, $value, 'array');
          }
          if (isset($options['module_versions'])) {
              throw new \InvalidArgumentException(
                  "Only one of 'versions' or " .
                  "'module_versions' may be set");
          }
          foreach ($value as $version) {
            if (!is_string($version)) {
              throw new \InvalidArgumentException(
                  'Version must be a string but was ' .
                  self::typeOrClass($version));
            }
            if (!preg_match(self::$MAJOR_VERSION_ID_REGEX, $version)) {
              throw new \InvalidArgumentException(
                  "Invalid version id " . htmlspecialchars($version));
            }
            $request->addModuleVersion()->setVersionId($version);
          }
          break;
        case 'batch_size':
          if (!is_int($value)) {
            self::optionTypeException($key, $value, 'integer');
          }
          if ($value > self::MAX_BATCH_SIZE || $value < 1) {
            throw new \InvalidArgumentException(
                'Batch size must be > 0 and <= ' . self::MAX_BATCH_SIZE);
          }
          $batch_size = $value;
          break;
        default:
          throw new \InvalidArgumentException(
              "Invalid option " . htmlspecialchars($key));
      }
    }

    // Set required options.
    $request->setIncludeIncomplete($include_incomplete);
    $request->setIncludeAppLogs($include_app_logs);
    $request->setCount($batch_size);

    // Set version to the current version if none set explicitly.
    if ($request->getModuleVersionSize() === 0) {
      self::setDefaultModuleVersion($request);
    }

    return new RequestLogIterator($request);
  }

  private static function setDefaultModuleVersion($request) {
    $mv = $request->addModuleVersion();
    $current_module = getenv('CURRENT_MODULE_ID');
    if ($current_module !== 'default') {
      $mv->setModuleId($current_module);
    }
    $current_version = getenv('CURRENT_VERSION_ID');
    $whole_version = explode('.', $current_version)[0];
    $mv->setVersionId($whole_version);
  }

  /**
   * Get request logs for the given request log ids and optionally include the
   * application logs addded during each request. Request log ids that are not
   * found are ignored so the returned array may have fewer items than
   * $request_ids.
   *
   * @param mixed $request_ids A string request id or an array of string request
   * ids obtained from <code>RequestLog::getRequestId()</code>.
   * @param boolean $include_app_logs Should applicaiton logs be included in the
   * fetched request logs. Defaults to true - application logs are included.
   *
   * @return RequestLog[] The request logs for ids that were found.
   */
  public static function fetchById($request_ids, $include_app_logs = true) {
    $request = new LogReadRequest();
    $request->setAppId(getenv('APPLICATION_ID'));

    if (!is_bool($include_app_logs)) {
      throw new \InvalidArgumentException(
        'Parameter $include_app_logs must be boolean but was ' .
        typeOrClass($include_app_logs));
    }

    $request->setIncludeAppLogs($include_app_logs);
    self::setDefaultModuleVersion($request);

    if (is_string($request_ids)) {
      if (!preg_match(self::$REQUEST_ID_REGEX, $request_ids)) {
        throw new \InvalidArgumentException(
            "Invalid request id " . htmlspecialchars($request_ids));
      }
      $request->addRequestId($request_ids);
    } else if (is_array($request_ids)) {
      foreach ($request_ids as $id) {
        if (!is_string($id)) {
          throw new \InvalidArgumentException(
              'Request id must be a string but was ' .
              self::typeOrClass($id));
        }
        if (!preg_match(self::$REQUEST_ID_REGEX, $id)) {
          throw new \InvalidArgumentException(
              "Invalid request id " . htmlspecialchars($id));
        }
        $request->addRequestId($id);
      }
    } else {
      throw new \InvalidArgumentException(
          'Expected a string or an array of strings but was '.
          self::typeOrClass($value));
    }

    $response = self::readLogs($request);

    $result = [];
    foreach ($response->getLogList() as $log) {
      $result[] = new RequestLog($log);
    }
    return $result;
  }

  /**
   * Add an app log at a particular Google App Engine severity level.
   *
   * @param integer $severity The Google App Engine severity level for the log.
   * @param string $message The message to log.
   */
  public static function log($severity, $message) {
    if (!is_int($severity)) {
      throw new \InvalidArgumentException(
        'Parameter $severity must be integer but was ' .
        self::typeOrClass($severity));
    }

    if ($severity < self::LEVEL_DEBUG || $severity > self::LEVEL_CRITICAL) {
      throw new \InvalidArgumentException(
        'Invalid severity level ' . $severity);
    }

    if (!is_string($message)) {
      throw new \InvalidArgumentException(
        'Parameter $message must be string but was ' .
        self::typeOrClass($message));
    }

    if (!isset(self::$app_log_group)) {
      self::$app_log_group = new UserAppLogGroup();
    }

    $app_log_line = self::$app_log_group->addLogLine();
    $app_log_line->setTimestampUsec(intval(microtime(true) * 1e6));
    $app_log_line->setLevel($severity);
    $app_log_line->setMessage($message);

    if (function_exists('_gae_stderr_log')) {
      _gae_stderr_log($severity, $message);
    }

    self::flushIfNeeded();
  }

  /**
   * Write all buffered log messages to the log storage. Logs may not be
   * immediately available to read.
   */
  public static function flush() {
    if (isset(self::$app_log_group)) {
      $req = new FlushRequest();
      $req->setLogs(self::$app_log_group->serializeToString());
      $resp = new VoidProto();
      ApiProxy::makeSyncCall('logservice', 'Flush', $req, $resp);
      self::$app_log_group->clear();
      self::$last_flush_time = microtime(true);
    }
  }

  /**
   * Set the maximum number of log entries to buffer before they are
   * automaticallly flushed upon adding the next log entry.
   *
   * @param integer $entries Number of log entries to buffer.
   */
  public static function setAutoFlushEntries($entries) {
    if (!is_int($entries)) {
      throw new \InvalidArgumentException(
          'Entries must be an integer but was ' . self::typeOrClass($entries));
    }
    self::$auto_flush_entries = $entries;
  }

  /**
   * Sets the maximum size of logs to buffer before they are automaticallly
   * flushed upon adding the next log entry.
   *
   * @param integer $bytes Size of logs to buffer in bytes.
   */
  public static function setAutoFlushBytes($bytes) {
    if (!is_int($bytes)) {
      throw new \InvalidArgumentException(
          'Bytes must be an integer but was ' . self::typeOrClass($bytes));
    }
    self::$auto_flush_bytes = $bytes;
  }

  /**
   * Sets the maximum amount of time in seconds before the buffered logs are
   * automatically flushed upon adding the next log entry.
   *
   * @param integer $seconds Time in seconds. Use zero or negative value to
   * disable the time limit.
   */
  public static function setLogFlushTimeLimit($seconds) {
    if (!is_int($seconds)) {
      throw new \InvalidArgumentException(
          'Seconds must be an integer but was ' . self::typeOrClass($seconds));
    }
    self::$flush_time_limit = $seconds;
  }

  private static function shouldFlushBasedOnEntries() {
    return self::$app_log_group->getLogLineSize() > self::$auto_flush_entries;
  }

  private static function shouldFlushBasedOnSize() {
    return self::$app_log_group->byteSizePartial() > self::$auto_flush_bytes;
  }

  private static function shouldFlushBasedOnTime() {
    return self::$flush_time_limit > 0 && isset(self::$last_flush_time) &&
        self::$last_flush_time + self::$flush_time_limit < microtime(true);
  }

  private static function flushIfNeeded() {
    $should_flush = false;
    if (self::shouldFlushBasedOnEntries()) {
      $should_flush = true;
    } else if (self::shouldFlushBasedOnSize()) {
      $should_flush = true;
    } else if (self::shouldFlushBasedOnTime()) {
      $should_flush = true;
    }

    if ($should_flush) {
      self::flush();
    }
  }

  /**
   * Translates a PHP <syslog>syslog<syslog> priority level into a Google App
   * Engine severity level. Useful when filtering logs by minimum severity
   * level given the syslog level.
   *
   * @param integer $syslog_level The priority level passed to
   * <code>syslog</code>.
   * @return integer The app engine severity level.
   */
  public static function getAppEngineLogLevel($syslog_level) {
    return self::$syslog_priority_map[$syslog_level];
  }

  private static function optionTypeException($key, $value, $expected) {
    throw new \InvalidArgumentException(
        htmlspecialchars("Option $key must be type $expected but was ") .
        self::typeOrClass($value));
  }

  /**
   * @return string The type or class name if type is object.
   */
  private static function typeOrClass($value) {
    if (is_object($value)) {
      return get_class($value);
    } else {
      return gettype($value);
    }
  }

  private static function dateTimeToUsecs($datetime) {
    // DateTime is accurate to seconds, StartTime to micro seconds.
    // The time stamp may only represent a date up to 2038 due to 32 bit ints.
    return (double) $datetime->getTimeStamp() * 1e6;
  }

}

/**
 * @internal
 */
trait ApiProxyAccess {
  /**
   * @param LogReadRequest $request The protocol buffer request to fetch.
   *
   * @return LogReadResponse The response including RequestLogs.
   */
  private static function readLogs(LogReadRequest $request) {
    $response = new LogReadResponse();
    try {
      ApiProxy::makeSyncCall('logservice', 'Read', $request, $response);
    } catch (ApplicationError $e) {
      throw self::applicationErrorToException($e);
    }
    return $response;
  }

  private static function applicationErrorToException(ApplicationError $error) {
    switch($error->getApplicationError()) {
      case ErrorCode::INVALID_REQUEST:
        return new LogException('Invalid Request');
      case ErrorCode::STORAGE_ERROR:
        return new LogException('Storage Error');
      default:
        return new LogException(
            'Error Code: ' . $error->getApplicationError());
    }
  }
}
