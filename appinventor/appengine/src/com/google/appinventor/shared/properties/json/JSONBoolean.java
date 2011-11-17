// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.shared.properties.json;

/**
 * Representation of a JSON boolean value.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public interface JSONBoolean extends JSONValue {
  /**
   * Returns the boolean value.
   *
   * @return  boolean value
   */
  boolean getBoolean();
}
