// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.server.properties.json;

import com.google.appinventor.shared.properties.json.JSONString;
import com.google.appinventor.shared.properties.json.JSONUtil;

/**
 * Implementation of {@link JSONString} using the json.org JSON library.
 *
 */
final class ServerJsonString extends ServerJsonValue implements JSONString {

  private final String value;

  public ServerJsonString(String value) {
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
