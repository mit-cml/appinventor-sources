// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.util;

import java.util.ArrayList;
import java.util.List;

/**
 * A representation of version number strings of the form [0-9]+{.[0-9]+}*
 * (where "." is interpreted literally) that supports comparison.
 *
 * <p>Here are annotated examples of the relative ordering of version strings,
 * where I refer to each period-delimited number as a component.
 * <ul>
 *   <li> 1.0 == 1.00   (numerical comparisons are done between pairs of components)
 *   <li> 2.15 &gt; 2.9 (numerical comparisons are done between pairs of components)
 *   <li> 1 == 1.0.0    (absent elements are treated zeroes)
 *   <li> 1.1 &gt; 1    (absent elements are treated zeroes)
 * </ul>
 *
 * @author spertus@google.com (Ellen Spertus)
 */
public class VersionString implements Comparable<VersionString> {
  private final List<Integer> components;

  /**
   * Constructor and validator.
   *
   * @throws IllegalArgumentException if any period-delimited region contains
   *         anything but a base-ten digit
   */
  public VersionString(String versionString) throws IllegalArgumentException {
    versionString = versionString.trim();

    // Most illegal characters will be caught by Integer.parseInt(), but not
    // minus signs or empty components, so check here.
    if (versionString.contains("-") || versionString.isEmpty() ||
        versionString.startsWith(".") || versionString.endsWith(".")) {
      throw new IllegalArgumentException("Illegal version string: " + versionString);
    }

    // Convert each period-delimited component into an Integer.
    String[] sComponents = versionString.split("\\.");
    components = new ArrayList<Integer>();
    try {
      for (String sComponent : sComponents) {
        components.add(Integer.parseInt(sComponent));
      }
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Illegal version string: " + versionString);
    }

    // Remove trailing zeroes.
    for (int i = components.size() - 1; i >= 0 && components.get(i) == 0; i--) {
      components.remove(i);
    }
  }

  @Override
  public int compareTo(VersionString other) {
    int minSize = Math.min(components.size(), other.components.size());
    for (int i = 0; i < minSize; i++) {
      if (components.get(i) < other.components.get(i)) {
        return -1;
      } else if (components.get(i) > other.components.get(i)) {
        return +1;
      }
    }
    // If all components are equal, the one with fewer components will be
    // the smaller version number.  If the number of components are equal,
    // so are the version numbers.
    return Integer.signum(components.size() - other.components.size());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (Integer i : components) {
      sb.append(i).append(".");
    }
    return sb.substring(0, sb.length() - 1);
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof VersionString)) {
      return false;
    }
    return compareTo((VersionString) other) == 0;
  }

  @Override
  public int hashCode() {
    // This is not guaranteed to return unique hashcodes for different version
    // numbers.  For example, "101" and "1.0.1" will have the same hashcodes,
    // but that does not violate the contract for hashCode().
    int result = 0;
    for (Integer component : components) {
      result += component;
    }
    return result;
  }
}
