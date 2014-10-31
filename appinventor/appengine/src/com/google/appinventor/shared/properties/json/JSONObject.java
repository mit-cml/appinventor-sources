// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

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
