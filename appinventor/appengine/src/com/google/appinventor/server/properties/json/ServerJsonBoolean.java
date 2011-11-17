// Copyright 2008 Google Inc. All Rights Reserved.

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
