// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.components.runtime.errors;

import com.google.appinventor.components.annotations.SimpleObject;

/**
 * Runtime error indicating an illegal value for a function or procedure
 * argument.
 *
 */
@SimpleObject
public class IllegalArgumentError extends RuntimeError {
  /**
   * Creates a new error
   */
  public IllegalArgumentError() {
    super();
  }

  /**
   * Creates a new error
   *
   * @param msg descriptive message
   */
  public IllegalArgumentError(String msg) {
    super(msg);
  }
}
