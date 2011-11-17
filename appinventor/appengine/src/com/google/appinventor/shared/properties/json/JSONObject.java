// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.shared.properties.json;

import java.util.Map;

/**
 * Representation of a JSON object.
 *
 */
public interface JSONObject extends JSONValue {
  /**
   * Returns a map of the properties defined for the object.
   *
   * @return  object properties
   */
  Map<String, JSONValue> getProperties();

  /**
   * Returns the value for the given key.
   *
   * @return  value
   */
  JSONValue get(String key);
}
