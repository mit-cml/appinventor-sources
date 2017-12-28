// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2017 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.errors;

import com.google.appinventor.components.runtime.util.ErrorMessages;

public class IterationError extends DispatchableError {

  public IterationError(int error, Object[] arguments) {
    super(error, arguments);
  }

  public int getIndex() {
    return (Integer) getArguments()[0];
  }

  public String getExpected() {
    return (String) getArguments()[1];
  }

  public String getFound() {
    return (String) getArguments()[2];
  }

  public static DispatchableError fromError(int index, DispatchableError e) {
    switch (e.getErrorCode()) {
      case ErrorMessages.ERROR_INVALID_POINT:
        return new IterationError(ErrorMessages.ERROR_INVALID_POINT_AT_INDEX, prepend(index, e.getArguments()));
      case ErrorMessages.ERROR_INVALID_TYPE:
        return new IterationError(ErrorMessages.ERROR_INVALID_TYPE_AT_INDEX, prepend(index, e.getArguments()));
      case ErrorMessages.ERROR_INVALID_NUMBER_OF_VALUES_IN_POINT:
        return new IterationError(ErrorMessages.ERROR_INVALID_NUMBER_OF_VALUES_IN_POINT_AT_INDEX, prepend(index, e.getArguments()));
      default:
        return e;
    }
  }

  private static Object[] prepend(int index, Object[] arguments) {
    Object[] newArguments = new Object[arguments.length + 1];
    newArguments[0] = index;
    System.arraycopy(arguments, 0, newArguments, 1, arguments.length);
    return newArguments;
  }
}
