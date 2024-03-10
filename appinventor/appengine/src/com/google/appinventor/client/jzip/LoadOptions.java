// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.jzip;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * Options for loading a zip file. This class is the Java equivalent of the JSZip options
 * for the {@link JSZip#loadAsync(Object, LoadOptions)} function.
 */
@JsType(namespace = JsPackage.GLOBAL)
public class LoadOptions {
  /**
   * If true, the zip file is loaded as a base64 string. If false, the zip file is loaded as an
   * ArrayBuffer.
   */
  public boolean base64;

  /**
   * Creates a new LoadOptions object.
   *
   * @param base64 If true, the zip file is loaded as a base64 string. If false, the zip file is
   *               loaded as an ArrayBuffer.
   */
  public LoadOptions(boolean base64) {
    this.base64 = base64;
  }

  /**
   * Creates a new LoadOptions object.
   *
   * @param base64 If true, the zip file is loaded as a base64 string. If false, the zip file is
   *               loaded as an ArrayBuffer.
   * @return The new LoadOptions object.
   */
  public static native LoadOptions create(boolean base64)/*-{
    return {'base64': base64};
  }-*/;
}
