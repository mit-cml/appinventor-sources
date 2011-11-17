// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.server.properties.json;

import com.google.appinventor.shared.properties.json.JSONNumber;

/**
 * Implementation of {@link JSONNumber} using the json.org JSON library.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
final class ServerJsonNumber extends ServerJsonValue implements JSONNumber {

  private final Number value;

  public ServerJsonNumber(Number value) {
    this.value = value;
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
