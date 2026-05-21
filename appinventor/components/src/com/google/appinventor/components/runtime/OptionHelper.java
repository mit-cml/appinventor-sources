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
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * Includes helper methods for getting the abstract enum value associated with a concrete value for
 * a given SimpleMethod, SimpleProperty, or SimpleEvent. Used in combination with the @Options
 * annotation in the declaration of the component.
 */
public class OptionHelper {

  private static final Map<String, Map<String, Method>> componentMethods =
      new HashMap<String, Map<String, Method>>();
    
  /**
   * Returns the OptionList version of the value if the function's return type has an @Options
   * annotation notating that the value can be coerced to an OptionList.
   * @param c The component that returned the concrete value.
   * @param func The function on the component that returned the concrete value.
   * @param value The concrete value that was returned.
   * @return The OptionList value if one is associated with the concrete value, otherwise the
   *     concrete value.
   */
  public static <T> Object optionListFromValue(Object c, String func, T value) {
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
    try {
      Method fromValue = optionListClass.getMethod("fromUnderlyingValue", value.getClass());
      // Java generally shouldn't return values that aren't defined in the OptionList, but
      // extensions might override a function to return values that aren't included. If the value
      // isn't included, just return the concrete value.
      Object abstractVal = fromValue.invoke(optionListClass, value);
      if (abstractVal != null) {
        return abstractVal;
      }
      return value;
    } catch (NoSuchMethodException e) {
      return value;
    } catch (IllegalAccessException e) {
      return value;
    } catch (InvocationTargetException e) {
      return value;
    }
  }

  /**
   * Returns the args after any coercable args have been coerced to an OptionList. An arg is
   * coercable if the function header has an @Options annotation associated with that arguement.
   * @param c The component containing the function.
   * @param func The function on the component that has the concrete args.
   * @param args The concrete args we want to attempt to cerce to OptionLists.
   * @return The args converted to OptionLists if OptionLists are associated with the args,
   *     otherwise the concrete values of the args.
   */
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
            // Extensions might send values to events which aren't covered by the OptionList
            // definition. In that case send the concrete value. See here for an example:
            // https://github.com/BeksOmega/appinventor-sources/pull/24#discussion_r480355676
            Object abstractVal = fromValue.invoke(optionListClass, args[i]);
            if (abstractVal != null) {
              args[i] = abstractVal;
            }
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

  /**
   * Returns the Method associated with the given component and function name. Returns null if the
   * Method does not exist or shouldn't be operated on in this context (e.g. a void method).
   * @param c The component to get the method of.
   * @param func The function on the component we want to get the Method of.
   * @return The Method representation of the method.
   */
  private static Method getMethod(Object c, String func) {
    Class<?> componentClass = c.getClass();
    String componentKey = componentClass.getSimpleName();
    Map<String, Method> methodMap = componentMethods.get(componentKey);

    if (methodMap == null) {
      methodMap = populateMap(componentClass);
      componentMethods.put(componentKey, methodMap);
    }

    return methodMap.get(func);
  }

  /**
   * Returns a map populated with all relevant Methods of the given Class. This includes all events,
   * property getters, and non-void methods.
   */
  private static Map<String, Method> populateMap(Class<?> clazz) {
    Map<String, Method> methodMap = new HashMap<String, Method>();
    Method[] methods = clazz.getMethods();

    // Add all the relevant methods to the map.
    for (Method m : methods) {
      if ((m.getModifiers() & Modifier.PUBLIC) == 0) {
        continue;
      }
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
