// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.properties.json;

import com.google.appinventor.shared.properties.json.JSONBoolean;

/**
 * Implementation of {@link JSONBoolean} using the json.org JSON library.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
final class ServerJsonBoolean extends ServerJsonValue implements JSONBoolean {

  private final boolean value;

  public ServerJsonBoolean(Boolean value) {
    this.value = value.booleanValue();
  }

  @Override
  public boolean getBoolean() {
    return value;
  }

  @Override
  public String toJson() {
    return Boolean.toString(value);
  }
}
