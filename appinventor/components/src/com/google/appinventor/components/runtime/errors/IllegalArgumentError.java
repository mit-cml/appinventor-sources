// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

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
