// Copyright 2008 Google Inc. All Rights Reserved.

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
