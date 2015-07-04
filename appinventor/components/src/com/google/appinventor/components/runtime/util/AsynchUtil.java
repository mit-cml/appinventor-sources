// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.os.Handler;

/**
 * Utilities for handling asynchronous calls.
 *
 * @author markf@google.com (Mark Friedman)
 */

public class AsynchUtil {

  /**
   * Make an asynchronous call in a separate thread.
   * @param call a {@link Runnable} to run in the thread.
   */
  public static void runAsynchronously(final Runnable call) {
    Thread thread = new Thread(call);
    thread.start();
  }
  /**
   * Make an asynchronous call in a separate thread, with a callback that's run on the current
   * Android UI thread.
   * @param androidUIHandler  the Handler from the current Android context
   * @param call a {@link Runnable} to run in the thread.
   * @param callback a {@link Runnable} to run in the Android UI thread when the call above returns
   */
  public static void runAsynchronously(final Handler androidUIHandler,
                                       final Runnable call,
                                       final Runnable callback) {
    Runnable runnable = new Runnable() {
      public void run() {
        call.run();
        if (callback != null) {
          androidUIHandler.post(new Runnable() {
            public void run() {
              callback.run();
            }
          });
        }
      }
    };
    Thread thread = new Thread(runnable);
    thread.start();
  }
}
