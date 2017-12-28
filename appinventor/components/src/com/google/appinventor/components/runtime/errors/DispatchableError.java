// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2017 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.errors;

import com.google.appinventor.components.runtime.util.ErrorMessages;

import java.util.Arrays;

public class DispatchableError extends RuntimeError {

  private final int errorCode;
  @SuppressWarnings("squid:S1948")
  private final Object[] arguments;

  public DispatchableError(int errorCode) {
    super(ErrorMessages.formatMessage(errorCode, null));
    this.errorCode = errorCode;
    this.arguments = new Object[0];
  }

  public DispatchableError(int errorCode, Object... arguments) {
    super(ErrorMessages.formatMessage(errorCode, arguments));
    this.errorCode = errorCode;
    this.arguments = arguments;
  }

  public int getErrorCode() {
    return errorCode;
  }

  public Object[] getArguments() {
    return Arrays.copyOf(arguments, arguments.length);
  }
}
