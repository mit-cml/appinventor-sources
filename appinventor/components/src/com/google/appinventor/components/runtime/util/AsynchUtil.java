// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.os.Handler;
import android.os.Looper;

import android.util.Log;

import com.google.appinventor.components.runtime.errors.YailRuntimeError;

/**
 * Utilities for handling asynchronous calls.
 *
 * @author markf@google.com (Mark Friedman)
 */

public class AsynchUtil {

  private static final String LOG_TAG = AsynchUtil.class.getSimpleName();

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

  public static boolean isUiThread() {
    return Looper.getMainLooper().equals(Looper.myLooper());
  }

  /**
   * Pauses the currently running thread to await the result of another thread, which will be
   * reported via the {@code result} synchronizer. The result will be passed to the given
   * {@code continuation} on completion. If the process results in an error, a runtime exception
   * is thrown instead.
   *
   * @param result the synchronizer receiving the result of an operation
   * @param continuation the continuation to call with the result
   * @param <T> the return type of the running operation
   */
  public static <T> void finish(Synchronizer<T> result, Continuation<T> continuation) {
    Log.d(LOG_TAG, "Waiting for synchronizer result");
    result.waitfor();

    // Handle result
    if (result.getThrowable() == null) {
      continuation.call(result.getResult());
    } else {
      Throwable e = result.getThrowable();
      if (e instanceof RuntimeException) {
        throw (RuntimeException) e;
      } else {
        throw new YailRuntimeError(e.toString(), e.getClass().getSimpleName());
      }
    }
  }
}
