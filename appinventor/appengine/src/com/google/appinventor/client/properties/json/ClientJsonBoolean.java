// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.client.properties.json;

import com.google.appinventor.shared.properties.json.JSONBoolean;

/**
 * Implementation of {@link JSONBoolean} that uses the GWT JSON library.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
final class ClientJsonBoolean extends ClientJsonValue implements JSONBoolean {

  private final boolean value;

  public ClientJsonBoolean(com.google.gwt.json.client.JSONBoolean value) {
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
