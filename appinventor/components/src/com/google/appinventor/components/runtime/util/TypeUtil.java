// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2017 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import com.google.appinventor.components.runtime.errors.DispatchableError;

public final class TypeUtil {

  private TypeUtil() {}

  public static <T> T cast(Object o, Class<T> tClass, String expected) {
    if (o == null) {
      return null;
    } else if (tClass.isInstance(o)) {
      return tClass.cast(o);
    } else {
      throw new DispatchableError(ErrorMessages.ERROR_INVALID_TYPE, o.getClass().getSimpleName(), expected);
    }
  }

  public static <T> T castNotNull(Object o, Class<T> tClass, String expected) {
    if (o == null) {
      throw new DispatchableError(ErrorMessages.ERROR_INVALID_TYPE, "null", expected);
    } else {
      return cast(o, tClass, expected);
    }
  }
}
