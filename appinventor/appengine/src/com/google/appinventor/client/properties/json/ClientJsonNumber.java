// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.properties.json;

import com.google.appinventor.shared.properties.json.JSONNumber;

/**
 * Implementation of {@link JSONNumber} that uses the GWT JSON library.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
final class ClientJsonNumber extends ClientJsonValue implements JSONNumber {

  private final Number value;

  public ClientJsonNumber(com.google.gwt.json.client.JSONNumber value) {
    // We use a Double here because GWT's JSONNumber only has one method to get the value and it
    // returns a double. If it had methods to get the value as different primitive number types
    // (int, byte, etc), then it would make sense to keep the GWT JSONNumber around.
    this.value = new Double(value.doubleValue());
  }

  @Override
  public byte getByte() {
    return value.byteValue();
  }

  @Override
  public double getDouble() {
    return value.doubleValue();
  }

  @Override
  public float getFloat() {
    return value.floatValue();
  }

  @Override
  public int getInt() {
    return value.intValue();
  }

  @Override
  public long getLong() {
    return value.longValue();
  }

  @Override
  public short getShort() {
    return value.shortValue();
  }

  @Override
  public String toJson() {
    return "" + value;
  }
}
