// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.properties.json;

/**
 * Representation of a JSON value.
 *
 * <p>Note that this interface only provides the JSON functionality we
 * actually require.
 *
 */
public interface JSONValue extends JSONEncodable {
  /**
   * Returns the value as an array.
   *
   * @return  JSON array value
   * @throws ClassCastException  if the value is not an array
   */
  JSONArray asArray();

  /**
   * Returns the value as an object.
   *
   * @return  JSON object value
   * @throws ClassCastException  if the value is not an object
   */
  JSONObject asObject();

  /**
   * Returns the value as a boolean.
   *
   * @return  JSON boolean value
   * @throws ClassCastException  if the value is not a boolean
   */
  JSONBoolean asBoolean();

  /**
   * Returns the value as a number.
   *
   * @return  JSON number value
   * @throws ClassCastException  if the value is not a number
   */
  JSONNumber asNumber();

  /**
   * Returns the value as a string.
   *
   * @return  JSON string value
   * @throws ClassCastException  if the value is not a string
   */
  JSONString asString();
}
