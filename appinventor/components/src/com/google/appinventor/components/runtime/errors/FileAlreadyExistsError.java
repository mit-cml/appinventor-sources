// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime.errors;

import com.google.appinventor.components.annotations.SimpleObject;

/**
 * Runtime error indicating that the attempt to create a file failed because
 * there is a file already existing with the same name.
 *
 */
@SimpleObject
public class FileAlreadyExistsError extends RuntimeError {

  /**
   * Creates a new error.
   *
   * @param message  detailed message
   */
  public FileAlreadyExistsError(String message) {
    super(message);
  }
}
