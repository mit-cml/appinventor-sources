// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.jzip;

import com.google.appinventor.client.utils.Promise;
import java.util.Date;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * A class representing a file or directory in a zip file.
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public final class ZipObject {
  /**
   * The name of the file or directory.
   */
  public String name;

  /**
   * Flag indicating whether the object is a directory.
   */
  public boolean dir;

  /**
   * The last modified date of the file or directory.
   */
  public Date date;

  /**
   * The comment, if any, for the file or directory.
   */
  public String comment;

  public ZipObject() {}

  /**
   * Returns the contents of the file or directory as a promise.
   *
   * @return a Promise for the contents of the file or directory
   */
  @JsOverlay
  public <T> Promise<T> get(Type<T> type) {
    return async(type.getType());
  }

  /**
   * Asynchronously reads the contents of the file or directory.
   *
   * @param type the type of the contents to read
   * @return a Promise for the contents of the file or directory
   * @param <T> the type of the contents to read
   */
  public native <T> Promise<T> async(String type);
}
