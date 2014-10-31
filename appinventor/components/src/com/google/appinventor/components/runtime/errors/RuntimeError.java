// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.errors;

import com.google.appinventor.components.annotations.SimpleObject;

/**
 * Superclass of all Simple runtime errors.
 *
 */
@SimpleObject
public abstract class RuntimeError extends RuntimeException {

  /**
   * Creates a runtime error.
   */
  protected RuntimeError() {
  }

  /**
   * Creates a runtime error with a more detailed error message.
   *
   * @param message  detailed error message
   */
  protected RuntimeError(String message) {
    super(message);
  }

  /**
   * Converts a Java {@link Throwable} into a Simple runtime error.
   *
   * @param throwable  Java throwable to be converted (may be a Simple runtime
   *                   error already)
   * @return  Simple runtime error
   */
  public static RuntimeError convertToRuntimeError(Throwable throwable) {
    if (throwable instanceof RuntimeError) {
      return (RuntimeError) throwable;
    }

    // Conversions of Java exceptions
    if (throwable instanceof ArrayIndexOutOfBoundsException) {
      return new ArrayIndexOutOfBoundsError();
    }
    if (throwable instanceof IllegalArgumentException) {
      return new IllegalArgumentError();
    }
    if (throwable instanceof NullPointerException) {
      return new UninitializedInstanceError();
    }

    // TODO(user): needs to be implemented
    throw new UnsupportedOperationException(throwable);
  }
}
