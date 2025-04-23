// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.jzip;

import com.google.gwt.typedarrays.shared.ArrayBuffer;

/**
 * The Type class is a GWT representation of the different object types supported by the JSZip
 * library.
 */
public class Type<T> {
  /**
   * The string type. Using this type will consume and return values as JavaScript strings.
   */
  public static final Type<String> STRING = new Type<>("string");
  /**
   * The ArrayBuffer type. Using this type will consume and return values as ArrayBuffers.
   */
  public static final Type<ArrayBuffer> ARRAY_BUFFER = new Type<>("arraybuffer");
  /**
   * The base64 type. Using this type will consume and return values as base64-encoded strings.
   */
  public static final Type<String> BASE64 = new Type<>("base64");

  private final String type;

  private Type(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }
}
