// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.properties.json;

import com.google.appinventor.shared.properties.json.JSONArray;
import com.google.appinventor.shared.properties.json.JSONValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link JSONArray} that uses the GWT JSON library.
 *
 */
final class ClientJsonArray extends ClientJsonValue implements JSONArray {

  private final List<JSONValue> elements;

  public ClientJsonArray(com.google.gwt.json.client.JSONArray array) {
    elements = new ArrayList<JSONValue>();
    for (int i = 0, n = array.size(); i < n; i++) {
      elements.add(ClientJsonValue.convert(array.get(i)));
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
