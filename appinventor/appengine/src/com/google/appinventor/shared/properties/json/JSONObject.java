// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

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
