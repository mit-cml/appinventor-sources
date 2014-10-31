// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

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
