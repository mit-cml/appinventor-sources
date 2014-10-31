// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.properties.json;

import com.google.common.collect.Maps;
import com.google.appinventor.shared.properties.json.JSONObject;
import com.google.appinventor.shared.properties.json.JSONValue;

import org.json.JSONException;

import java.util.Iterator;
import java.util.Map;

/**
 * Implementation of {@link JSONObject} using the json.org JSON library.
 *
 */
final class ServerJsonObject extends ServerJsonValue implements JSONObject {

  private final Map<String, JSONValue> properties;

  @SuppressWarnings("unchecked")
  public ServerJsonObject(org.json.JSONObject object) {
    properties = Maps.newHashMap();
    Iterator<String> keys = object.keys();
    while (keys.hasNext()) {
      String key = keys.next();
      try {
        properties.put(key, ServerJsonValue.convert(object.get(key)));
      } catch (JSONException e) {
        // Cannot happen!
        throw new AssertionError(e);
      }
    }
  }

  @Override
  public Map<String, JSONValue> getProperties() {
    return properties;
  }

  @Override
  public JSONValue get(String key) {
    return properties.get(key);
  }

  @Override
  public String toJson() {
    StringBuilder sb = new StringBuilder();
    sb.append("{");
    String separator = "";
    for (Map.Entry<String, JSONValue> entry : properties.entrySet()) {
      sb.append(separator).append("\"").append(entry.getKey()).append("\":");
      sb.append(entry.getValue().toJson());
      separator = ",";
    }
    sb.append("}");
    return sb.toString();
  }
}
