// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.properties.json;

import com.google.appinventor.shared.properties.json.JSONObject;
import com.google.appinventor.shared.properties.json.JSONValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of {@link JSONObject} that uses the GWT JSON library.
 *
 */
final class ClientJsonObject extends ClientJsonValue implements JSONObject {

  private final Map<String, JSONValue> properties;

  public ClientJsonObject(com.google.gwt.json.client.JSONObject object) {
    properties = new HashMap<String, JSONValue>();
    for (String key : object.keySet()) {
      properties.put(key, ClientJsonValue.convert(object.get(key)));
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
