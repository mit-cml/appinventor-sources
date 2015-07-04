// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.properties.json;

import com.google.appinventor.shared.properties.json.JSONArray;
import com.google.appinventor.shared.properties.json.JSONBoolean;
import com.google.appinventor.shared.properties.json.JSONNumber;
import com.google.appinventor.shared.properties.json.JSONObject;
import com.google.appinventor.shared.properties.json.JSONString;
import com.google.appinventor.shared.properties.json.JSONValue;

/**
 * Implementation of {@link JSONValue} that uses the GWT JSON library.
 *
 */
abstract class ClientJsonValue implements JSONValue {

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
  static ClientJsonValue convert(Object object) {
    if (object == null || object instanceof com.google.gwt.json.client.JSONNull) {
      return null;
    } else if (object instanceof com.google.gwt.json.client.JSONBoolean) {
      return new ClientJsonBoolean((com.google.gwt.json.client.JSONBoolean) object);
    } else if (object instanceof com.google.gwt.json.client.JSONNumber) {
      return new ClientJsonNumber((com.google.gwt.json.client.JSONNumber) object);
    } else if (object instanceof com.google.gwt.json.client.JSONString) {
      return new ClientJsonString((com.google.gwt.json.client.JSONString) object);
    } else if (object instanceof com.google.gwt.json.client.JSONObject) {
      return new ClientJsonObject((com.google.gwt.json.client.JSONObject) object);
    } else if (object instanceof com.google.gwt.json.client.JSONArray) {
      return new ClientJsonArray((com.google.gwt.json.client.JSONArray) object);
    } else {
      throw new IllegalArgumentException();
    }
  }
}
