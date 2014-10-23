// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.properties.json;

import com.google.appinventor.shared.properties.json.JSONArray;
import com.google.appinventor.shared.properties.json.JSONBoolean;
import com.google.appinventor.shared.properties.json.JSONNumber;
import com.google.appinventor.shared.properties.json.JSONObject;
import com.google.appinventor.shared.properties.json.JSONString;
import com.google.appinventor.shared.properties.json.JSONValue;

/**
 * Implementation of {@link JSONValue} using the json.org JSON library.
 *
 */
abstract class ServerJsonValue implements JSONValue {

  @Override
  public final JSONArray asArray() {
    return (JSONArray) this;
  }

  @Override
  public final JSONObject asObject() {
    return (JSONObject) this;
  }

  @Override
  public final JSONBoolean asBoolean() {
    return (JSONBoolean) this;
  }

  @Override
  public final JSONNumber asNumber() {
    return (JSONNumber) this;
  }

  @Override
  public final JSONString asString() {
    return (JSONString) this;
  }

  /**
   * Converts the original JSON object from the wrapped JSON parser into
   * a {@link JSONValue} object.
   *
   * @param object  original JSON object from wrapped JSON parser
   * @return  {@code JSONValue} object
   */
  static ServerJsonValue convert(Object object) {
    if (object == null || object == org.json.JSONObject.NULL) {
      return null;
    } else if (object instanceof Boolean) {
      return new ServerJsonBoolean((Boolean) object);
    } else if (object instanceof Number) {
      return new ServerJsonNumber((Number) object);
    } else if (object instanceof String) {
      return new ServerJsonString((String) object);
    } else if (object instanceof org.json.JSONObject) {
      return new ServerJsonObject((org.json.JSONObject) object);
    } else if (object instanceof org.json.JSONArray) {
      return new ServerJsonArray((org.json.JSONArray) object);
    } else {
      throw new IllegalArgumentException();
    }
  }
}
