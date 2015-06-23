// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.properties.json;

import com.google.common.collect.Lists;
import com.google.appinventor.shared.properties.json.JSONArray;
import com.google.appinventor.shared.properties.json.JSONValue;

import org.json.JSONException;

import java.util.List;

/**
 * Implementation of {@link JSONArray} using the json.org JSON library.
 *
 */
final class ServerJsonArray extends ServerJsonValue implements JSONArray {

  private final List<JSONValue> elements;

  public ServerJsonArray(org.json.JSONArray array) {
    int size = array.length();
    elements = Lists.newArrayListWithCapacity(size);
    for (int i = 0; i < size; i++) {
      try {
        elements.add(ServerJsonValue.convert(array.get(i)));
      } catch (JSONException e) {
        // Cannot happen
        throw new AssertionError(e);
      }
    }
  }

  @Override
  public List<JSONValue> getElements() {
    return elements;
  }

  @Override
  public int size() {
    return elements.size();
  }

  @Override
  public JSONValue get(int index) {
    return elements.get(index);
  }

  @Override
  public String toJson() {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    String separator = "";
    for (JSONValue value : elements) {
      sb.append(separator).append(value.toJson());
      separator = ",";
    }
    sb.append("]");
    return sb.toString();
  }
}
