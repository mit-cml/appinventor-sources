// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

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
