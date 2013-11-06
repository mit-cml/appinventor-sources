// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

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
