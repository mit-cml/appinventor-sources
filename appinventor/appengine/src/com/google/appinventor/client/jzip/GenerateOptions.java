// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.jzip;

import jsinterop.annotations.JsType;

/**
 * Options for generating a zip file. This class is the Java equivalent of the JSZip options
 * for the {@link JSZip#generateAsync(GenerateOptions)} function.
 */
@JsType
public class GenerateOptions {
  public String type;
  public String compression;

  public GenerateOptions(Type<?> type) {
    this.type = type.getType();
  }

  public static native GenerateOptions create(Type<?> type) /*-{
    return {'type': type.@com.google.appinventor.client.jzip.Type::getType()()};
  }-*/;
}
