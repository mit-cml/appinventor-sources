// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.components.runtime.errors;

import com.google.appinventor.components.annotations.SimpleObject;

/**
 * Runtime error indicating a problem accessing a file.
 *
 */
@SimpleObject
public class FileIOError extends RuntimeError {

  /**
   * Creates a new File I/O error.
   *
   * @param message  detailed message
   */
  public FileIOError(String message) {
    super(message);
  }
}
