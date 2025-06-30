// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.jzip;

import com.google.gwt.typedarrays.shared.ArrayBuffer;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * The TextEncoder class is a GWT interface to the browser's native TextEncoder object.
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class TextEncoder {
  /**
   * Creates a new TextEncoder object for the given encoding.
   *
   * @param encoding the desired encoding for encoded text
   */
  public TextEncoder(String encoding) {}

  /**
   * Encodes the given text using the encoding specified by this encoder.
   *
   * @param text the text to encode
   * @return the encoded data
   */
  public native ArrayBuffer encode(String text);
}
