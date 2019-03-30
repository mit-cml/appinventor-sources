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
 * Helper functions for working with Memcache and Memcached extensions.
 *
 */

namespace google\appengine\runtime;

use google\appengine\MemcacheSetRequest;
use google\appengine\MemcacheSetResponse;

class MemcacheUtils {

  const FLAG_TYPE_MASK = 7;
  // These constants are taken from google/appengine/api/memcache/__init__.py:
  const TYPE_STR = 0;
  const TYPE_UNICODE = 1;  // Value can be read but is never written.
  // TYPE_PICKLED = 2
  const TYPE_INT = 3;
  const TYPE_LONG = 4;
  const TYPE_BOOL = 5;
  // These flags are unique to PHP:
  const TYPE_FLOAT = 6;
  const TYPE_PHP_SERIALIZED = 7;

  public static function serializeValue($value, &$flag) {
    switch (gettype($value)) {
      case "boolean":
        $flag |= self::TYPE_BOOL;
        return ($value ? "1" : "0");
      case "double":
        // Floats must be serialized to strings for compatibility with the
        // memcache PHP extension. This sequence is possible:
        // $m->set("float", 2.0)
        // $m->increment("float", 5)  // Would fail if "float" is non-integral.
        // $m->get("float") => 7.0
        $flag |= self::TYPE_FLOAT;
        return (string) $value;
      case "integer":
        $flag |= self::TYPE_INT;
        return (string) $value;
      case "string":
        $flag |= self::TYPE_STR;
        return $value;
      default:
        $flag |= self::TYPE_PHP_SERIALIZED;
        return serialize($value);
    }
  }

  public static function deserializeValue($value, $flag) {
    $type_flag = $flag & self::FLAG_TYPE_MASK;
    switch ($type_flag) {
      case self::TYPE_BOOL:
        return (boolean) $value;
      case self::TYPE_FLOAT:
        if ($value === "INF") {
          return INF;
        } else if ($value === "-INF") {
          return -INF;
        } else {
          return (double) $value;
        }
      case self::TYPE_INT:
      case self::TYPE_LONG:
        return (integer) $value;
      case self::TYPE_STR:
      case self::TYPE_UNICODE:
        return $value;
      case self::TYPE_PHP_SERIALIZED:
        return unserialize($value);
      default:
        throw new \UnexpectedValueException("unexpected type flag: " .
                                            $type_flag);
    }
  }

  public static function setMultiWithPolicy($keyValues, $expire, $policy) {
    $request = new MemcacheSetRequest();
    $response = new MemcacheSetResponse();

    foreach ($keyValues as $key => $value) {
      $memcache_flag = 0;
      $serialized_value = self::serializeValue($value, $memcache_flag);

      $item = $request->addItem();
      $item->setKey($key);
      $item->setValue($serialized_value);
      $item->setFlags($memcache_flag);
      $item->setSetPolicy($policy);
      $item->setExpirationTime($expire);
    }

    ApiProxy::makeSyncCall('memcache', 'Set', $request, $response);
    return $response->getSetStatusList();
  }
}
