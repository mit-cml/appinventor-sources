// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

/**
 * Interface for callback pair with {@link #onSuccess(Object)} and {@link #onFailure(String)}.
 *
 * @param <T> the type passed to the {@link #onSuccess(Object)} method.
 *
 * @author halabelson@google.com (Hal Abelson)
 */

  public interface AsyncCallbackPair<T> {
  /**
   * Create a pair of callbacks,  for success and failure,
   * used tyically in an asynchronous operation.
   */

    /**
     * Called when an asynchronous call fails to complete normally
     *
     * @param message a message to be consumed by the procedure that
     * set up the callback pair
     */
    void onFailure(String message);

    /**
     * Called when an asynchronous call completes successfully.
     *
     * @param result the return value of asynchronous operation
     */
    void onSuccess(T result);
  }
