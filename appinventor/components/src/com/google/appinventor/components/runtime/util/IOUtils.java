// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import java.io.Closeable;
import java.io.IOException;

import android.util.Log;

public final class IOUtils {
  /**
   * Closes the given {@code Closeable}. Suppresses any IO exceptions.
   */
  public static void closeQuietly(String tag, Closeable closeable) {
    try {
      if (closeable != null) {
        closeable.close();
      }
    } catch (IOException e) {
        Log.w(tag, "Failed to close resource", e);
    }
  }
}
