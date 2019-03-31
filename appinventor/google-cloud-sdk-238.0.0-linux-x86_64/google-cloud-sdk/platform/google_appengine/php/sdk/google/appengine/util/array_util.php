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
 * Various utilities for working with PHP arrays.
 *
 */
namespace google\appengine\util;

/**
 * Various PHP array related utility functions.
 */
final class ArrayUtil {
  /**
   * Find an item in an associative array by a key value, or return null if not
   * found.
   *
   * @param array $array - The array to search
   * @param mixed $key - The key to search for.
   *
   * @return mixed The value of the item in the array with the given key,
   * or null if not found.
   */
  public static function findByKeyOrNull($array, $key) {
    return static::findByKeyOrDefault($array, $key, null);
  }

  /**
   * Find an item in an associative array by a key value, or return default if
   * not found.
   *
   * @param array $array - The array to search
   * @param mixed $key - The key to search for.
   * @param mixed $default - The value to return if key is not found.
   *
   * @return mixed The value of the item in the array with the given key,
   * or the given default if not found.
   */
  public static function findByKeyOrDefault($array, $key, $default) {
    if (array_key_exists($key, $array)) {
      return $array[$key];
    }
    return $default;
  }

  /**
   * Merge a number of arrays using a case insensitive comparison for the array
   * keys.
   *
   * @param mixed array Two or more arrays to merge.
   *
   * @returns array The merged array.
   *
   * @throws InvalidArgumentException If less than two arrays are passed to
   *     the function, or one of the arguments is not an array.
   */
  public static function arrayMergeIgnoreCase() {
    if (func_num_args() < 2) {
      throw new \InvalidArgumentException(
          "At least two arrays must be supplied.");
    }
    $result = [];
    $key_mapping = [];
    $input_args = func_get_args();

    foreach($input_args as $args) {
      if (!is_array($args)) {
        throw new \InvalidArgumentException(
            "Arguments are expected to be arrays, found " . gettype($arg));
      }
      foreach($args as $key => $val) {
        $lower_case_key = strtolower($key);
        if (array_key_exists($lower_case_key, $key_mapping)) {
          $result[$key_mapping[$lower_case_key]] = $val;
        } else {
          $key_mapping[$lower_case_key] = $key;
          $result[$key] = $val;
        }
      }
    }
    return $result;
  }


  /**
   * Checks whether an array's keys are associative. An array's keys are
   * associate if they are not values 0 to count(array) - 1.
   *
   * @param $arr array The array whos keys will be checked.
   *
   * @return bool True if the array's keys are associative. Also true in the
   * case of an empty array.
   */
  public static function isAssociative(array $arr) {
    $size = count($arr);
    $keys = array_keys($arr);
    return $keys !== range(0, $size - 1);
  }

  /**
   * Checks whether every value in an array passes the provided predicate.
   *
   * @param $array array The array to test.
   *
   * @param $predicate callable A predicate which should take one argument and
   *                            return a boolean.
   *
   * @return bool Whether every value in the array passes the predicate.
   */
  public static function all(array $array, callable $predicate) {
    foreach($array as $val) {
      if(!$predicate($val)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Checks whether every value in an array is an instance of a class.
   *
   * @param $array array The array to test.
   *
   * @param $class The fully qualified class name to check every array value
   *               with.
   *
   * @return bool Whether every value in the array is an instance of $class.
   *
   * @throw \InvalidArgumentException if no class with name $class is found.
   */
  public static function allInstanceOf(array $array, $class) {
    if(!is_string($class)) {
      throw new \InvalidArgumentException('$class must be a string.');
    }
    if(!class_exists($class)) {
      throw new \InvalidArgumentException("Class with name $class not found.");
    }
    foreach($array as $val) {
      if(!self::instanceOfClass($val, $class)) {
        return false;
      }
    }
    return true;
  }


  /**
   * Checks whether $obj is an instance of $class.
   */
  private static function instanceOfClass($obj, $class) {
    return is_object($obj) && is_a($obj, $class);
  }

}
