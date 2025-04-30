// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.jzip;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * The TextDecoder class is a GWT interface to the browser's native TextDecoder object.
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class TextDecoder {
  /**
   * Creates a new TextDecoder object for the given encoding.
   *
   * @param encoding the desired encoding for decoded text
   */
  public TextDecoder(String encoding) {}

  /**
   * Decodes the given data using the encoding specified by this decoder.
   *
   * @param data the data to decode
   * @return the decoded text
   */
  public native String decode(Object data);
}
