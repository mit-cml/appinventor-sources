// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime.errors;

/**
 * Runtime error not fitting in any more specific category.
 *
 * @author halabelson@google.com (Hal Abelson)
 */
public class YailRuntimeError extends RuntimeError {

  private String errorType;

  public YailRuntimeError(String message, String errorType) {
    super(message);
    this.errorType = errorType;
  }

  public String getErrorType() {
    return errorType;
  }

}
