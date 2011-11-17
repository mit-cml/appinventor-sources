// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.shared.properties.json;

/**
 * Representation of a JSON number value.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public interface JSONNumber extends JSONValue {
  /**
   * Returns the value as a byte.
   *
   * @return  byte value
   */
  byte getByte();

  /**
   * Returns the value as a double.
   *
   * @return  double value
   */
  double getDouble();

  /**
   * Returns the value as a float.
   *
   * @return  float value
   */
  float getFloat();

  /**
   * Returns the value as an int.
   *
   * @return  int value
   */
  int getInt();

  /**
   * Returns the value as a long.
   *
   * @return  long value
   */
  long getLong();

  /**
   * Returns the value as a short.
   *
   * @return  short value
   */
  short getShort();
}
