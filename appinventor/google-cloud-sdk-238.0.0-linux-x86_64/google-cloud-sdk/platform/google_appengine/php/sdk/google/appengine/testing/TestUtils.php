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
 * Useful utility functions for SDK testing.
 */

namespace google\appengine\testing;

final class TestUtils {
  /**
   * Call protected/private non-static method of an object.
   *
   * @param object &$object    Instantiated object that we will run method on.
   * @param string $methodName Method name to call
   * @param array  $parameters Array of parameters to pass into method.
   *
   * @return mixed Method return.
   */
  public static function invokeMethod(&$object,
                                      $methodName,
                                      array $parameters = []) {
    $reflection = new \ReflectionClass(get_class($object));
    $method = $reflection->getMethod($methodName);
    $method->setAccessible(true);

    return $method->invokeArgs($object, $parameters);
  }

  /**
   * Call protected/private static method of a class.
   *
   * @param string $className  Name of the class for the static method.
   * @param string $methodName Method name to call
   * @param array  $parameters Array of parameters to pass into method.
   *
   * @return mixed Method return.
   */
  public static function invokeStaticMethod($className,
                                            $methodName,
                                            array $parameters = []) {
    $reflection = new \ReflectionClass($className);
    $method = $reflection->getMethod($methodName);
    $method->setAccessible(true);

    return $method->invokeArgs(null, $parameters);
  }

  /**
   * Set protected/private non-static property of an object.
   *
   * @param object &$object      Instantiated object with the property to set.
   * @param string $propertyName Name of the property to set.
   * @param mixed  $value        New value for the property.
   *
   * @return mixed Previous value for the property before the update.
   */
  public static function setProperty(&$object,
                                     $propertyName,
                                     $value) {
    $reflection = new \ReflectionClass(get_class($object));
    $property = $reflection->getProperty($propertyName);
    $property->setAccessible(true);
    $old_value = $property->getValue($object);
    $property->setValue($object, $value);
    return $old_value;
  }

  /**
   * Get protected/private non-static property of an object.
   *
   * @param object &$object      Instantiated object with the property to get.
   * @param string $propertyName Name of the property to get.
   *
   * @return mixed The value of the property.
   */
  public static function getProperty($object,
                                     $propertyName) {
    $reflection = new \ReflectionClass(get_class($object));
    $property = $reflection->getProperty($propertyName);
    $property->setAccessible(true);
    return $property->getValue($object);
  }

  /**
   * Set protected/private static property of a class.
   *
   * @param object $className    Name of the class for the static property.
   * @param string $propertyName Name of the property to set.
   * @param mixed  $value        New value for the property.
   *
   * @return mixed Previous value for the property before the update.
   */
  public static function setStaticProperty($className,
                                           $propertyName,
                                           $value) {
    $reflection = new \ReflectionClass($className);
    $property = $reflection->getProperty($propertyName);
    $property->setAccessible(true);
    $old_value = $property->getValue();
    $property->setValue(null, $value);
    return $old_value;
  }

  /**
   * Get protected/private static property of a class.
   *
   * @param object $className    Name of the class for the static property.
   * @param string $propertyName Name of the property to get.
   *
   * @return mixed The value for the property.
   */
  public static function getStaticProperty($className,
                                           $propertyName) {
    $reflection = new \ReflectionClass($className);
    $property = $reflection->getProperty($propertyName);
    $property->setAccessible(true);
    return $property->getValue();
  }

}
