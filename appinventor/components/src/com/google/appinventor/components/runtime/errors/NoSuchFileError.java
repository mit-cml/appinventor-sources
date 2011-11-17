//Copyright 2008 Google Inc. All Rights Reserved.

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
