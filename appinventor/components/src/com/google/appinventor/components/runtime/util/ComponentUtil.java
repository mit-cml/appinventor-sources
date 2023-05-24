// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020  MIT, All rights reserve
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
package com.google.appinventor.components.runtime.util;

import com.google.appinventor.components.runtime.Component;

import gnu.mapping.Environment;
import gnu.mapping.LocationEnumeration;
import gnu.mapping.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Util class that handles component filtering from the form environment.
 * Java implementation used over Scheme for ease of working with iterators.
 * See runtime.scm
 */
public final class ComponentUtil {

  private ComponentUtil() {
  }

  /**
   * Filters the form environment and returns all components of a specified
   * type.
   * @param env the current form environment
   * @param type of the component (eg: com.google.appinventor.components.runtime.Label)
   * @returns list of components that match the filter
   */
  public static List<Object> filterComponentsOfType(Environment env, String type) {
    List<Object> components = new ArrayList<>();
    LocationEnumeration iterator = env.enumerateAllLocations();
    while (iterator.hasNext()) {
      Location loc = iterator.next();
      Object maybeComponent = loc.get();
      if (maybeComponent.getClass().getName().equals(type)) {
        components.add(maybeComponent);
      }
    }
    return components;
  }
}
