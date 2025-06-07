// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(namespace = JsPackage.GLOBAL, isNative = true)
public class RetValManager {
  private RetValManager() {
    // Private constructor to prevent instantiation
  }

  /**
   * appendReturnValue -- Add a result, already encoded as a String to
   * the array of pending values.
   *
   * @param blockid The block id of the block this is for (-1 for no particular block)
   * @param ok Indication of success or failure
   * @param item The item to append
   */
  public static native void appendReturnValue(String blockid, String ok, String item);

  public static native void sendError(String error);

  public static native void pushScreen(String screenName, Object value);

  public static native void popScreen(String value);

  public static native void assetTransferred(String name);

  public static native void extensionsLoaded();

  public static native String fetch(boolean block);
}
