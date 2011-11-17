// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.components.runtime.errors;

import com.google.appinventor.components.annotations.SimpleObject;

/**
 * Runtime error indicating an assert failure.
 *
 */
@SimpleObject
public class AssertionFailure extends RuntimeError {
  /**
   * Creates a new error
   */
  public AssertionFailure() {
    super();
  }

  /**
   * Creates a new error
   *
   * @param msg descriptive message
   */
  public AssertionFailure(String msg) {
    super(msg);
  }
}
