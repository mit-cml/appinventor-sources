// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime.errors;

import com.google.appinventor.components.annotations.SimpleObject;

/**
 * Runtime error indicating that no file for the given name could be found.
 *
 */
@SimpleObject
public class NoSuchFileError extends RuntimeError {

  /**
   * Creates a new error.
   *
   * @param message  detailed message
   */
  public NoSuchFileError(String message) {
    super(message);
  }
}
