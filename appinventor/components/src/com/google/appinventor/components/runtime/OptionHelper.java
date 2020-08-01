// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.Options;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleProperty;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import android.util.Log;

public class OptionHelper {

  public static final String TAG = "OptionHelper";

  public static Map<String, Map<String, Method>> componentMethods =
      new HashMap<String, Map<String, Method>>();
    
  public static <T> Object optionListFromValue(Component c, String func, T value) {
    Method calledFunc = getMethod(c, func);
    if (calledFunc == null) {
      // Doesn't exist or not relevant.
      return value;
    }
    Options annotation = calledFunc.getAnnotation(Options.class);
    if (annotation == null) {
      return value;
    }
    Class<?> optionListClass = annotation.value();
    Object newValue = null;
    try {
      Method fromValue = optionListClass.getMethod("fromUnderlyingValue", value.getClass());
      newValue = fromValue.invoke(optionListClass, value);
    } finally {
      if (newValue != null) {
        return newValue;
      }
      return value;
    }
  }

  public static Object[] optionListsFromValues(Component c, String func, Object...args) {
    if (args.length == 0) {
      return args;
    }
    Method calledFunc = getMethod(c, func);
    if (calledFunc == null) {
      return args;
    }
    Annotation[][] paramAnnotations = calledFunc.getParameterAnnotations();
    int i = 0;
    for (Annotation[] annotations : paramAnnotations) {
      for (Annotation annotation : annotations) {
        if (annotation.annotationType() == Options.class) {
          Options castAnnotation = (Options) annotation;
          Class<?> optionListClass = castAnnotation.value();
          try {
            Method fromValue = optionListClass.getMethod("fromUnderlyingValue", args[i].getClass());
            args[i] = fromValue.invoke(optionListClass, args[i]);
          } catch (NoSuchMethodException e) {
            // If it doesn't exist just continue.
          } catch (IllegalAccessException e) {
            // If it's not accessible just continue.
          } catch (InvocationTargetException e) {
            // If it doesn't work just continue.
          }
          break;
        }
      }
      i++;
    }
    return args;
  }

  private static Method getMethod(Component c, String func) {
    Class<?> componentClass = c.getClass();
    String componentKey = componentClass.getSimpleName();
    Map<String, Method> methodMap = componentMethods.get(componentKey);

    if (methodMap == null) {
      methodMap = populateMap(componentClass);
      componentMethods.put(componentKey, methodMap);
    }

    return methodMap.get(func);
  }

  private static Map<String, Method> populateMap(Class<?> clazz) {
    Map<String, Method> methodMap = new HashMap<String, Method>();
    Method[] methods = clazz.getMethods();

    // Add all the relevant methods to the map.
    for (Method m : methods) {
      String methodKey = m.getName();

      // Always add events.
      SimpleEvent event = m.getAnnotation(SimpleEvent.class);
      if (event != null) {
        methodMap.put(methodKey, m);
        continue;
      }

      // Ignore void methods and property setters.
      if (m.getReturnType() != Void.TYPE) {
        SimpleFunction func = m.getAnnotation(SimpleFunction.class);
        if (func != null) {
          methodMap.put(methodKey, m);
          continue;
        }
        SimpleProperty prop = m.getAnnotation(SimpleProperty.class);
        if (prop != null) {
          methodMap.put(methodKey, m);
        }
      }
    }
    return methodMap;
  }
}