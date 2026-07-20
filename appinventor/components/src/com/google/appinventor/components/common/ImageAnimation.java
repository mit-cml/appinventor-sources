// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines the limited animation choices supported by the Image component.
 */
public enum ImageAnimation implements OptionList<String> {
  @Default
  Stop("Stop"),
  ScrollRightSlow("ScrollRightSlow"),
  ScrollRight("ScrollRight"),
  ScrollRightFast("ScrollRightFast"),
  ScrollLeftSlow("ScrollLeftSlow"),
  ScrollLeft("ScrollLeft"),
  ScrollLeftFast("ScrollLeftFast");

  private final String value;

  ImageAnimation(String value) {
    this.value = value;
  }

  public String toUnderlyingValue() {
    return value;
  }

  private static final Map<String, ImageAnimation> lookup = new HashMap<>();

  static {
    for (ImageAnimation animation : ImageAnimation.values()) {
      lookup.put(animation.toUnderlyingValue(), animation);
    }
  }

  public static ImageAnimation fromUnderlyingValue(String animation) {
    return lookup.get(animation);
  }
}
