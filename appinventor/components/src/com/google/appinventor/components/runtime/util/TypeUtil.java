// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2017 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import com.google.appinventor.components.common.OptionList;
import com.google.appinventor.components.runtime.errors.DispatchableError;
import gnu.mapping.Symbol;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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

  /**
   * Cast a raw value of a option block into its enumeration value.
   *
   * @param value the raw value of the block
   * @param className the target enum for looking up the value
   * @param <T> the underlying type of the OptionList
   * @return the corresponding enum value, or null if the value isn't found
   */
  @SuppressWarnings("unused")  // called from runtime.scm
  public static <T> OptionList<T> castToEnum(T value, Symbol className) {
    String classNameStr = stripEnumSuffix(className.getName());
    try {
      Class<?> clazz = Class.forName(classNameStr);
      if (!OptionList.class.isAssignableFrom(clazz)) {
        // In theory the code generator should never do this, but just in case...
        throw new IllegalArgumentException(classNameStr
            + " does not identify an OptionList type.");
      }
      for (Method m : clazz.getMethods()) {
        if ("fromUnderlyingValue".equals(m.getName())
            && m.getParameterTypes()[0].isAssignableFrom(value.getClass())) {
          return (OptionList<T>) m.invoke(clazz, value);
        }
      }
      return null;
    } catch (ClassNotFoundException e) {
      return null;
    } catch (InvocationTargetException e) {
      return null;
    } catch (IllegalAccessException e) {
      return null;
    }
  }

  private static String stripEnumSuffix(String className) {
    if (className.endsWith("Enum")) {
      return className.substring(0, className.length() - 4);
    }
    return className;
  }
}
