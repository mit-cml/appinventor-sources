// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.client.properties.json;

import com.google.appinventor.shared.properties.json.JSONString;
import com.google.appinventor.shared.properties.json.JSONUtil;

/**
 * Implementation of {@link JSONString} that uses the GWT JSON library.
 *
 */
public final class ClientJsonString extends ClientJsonValue implements JSONString {

  private final String value;

  public ClientJsonString(com.google.gwt.json.client.JSONString value) {
    this(value.stringValue());
  }

  public ClientJsonString(String value) {
    this.value = value;
  }

  @Override
  public String getString() {
    return value;
  }

  @Override
  public String toJson() {
    return JSONUtil.toJson(value);
  }
}
