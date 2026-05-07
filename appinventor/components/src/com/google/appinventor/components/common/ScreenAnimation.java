// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines a ScreenAnimation type used by the Form component to specify its open and close
 * animations.
 */
public enum ScreenAnimation implements OptionList<String> {
  Default("default"),
  Fade("fade"),
  Zoom("zoom"),
  SlideHorizontal("slidehorizontal"),
  SlideVertical("slidevertical"),
  None("none");

  private final String value;

  ScreenAnimation(String anim) {
    this.value = anim;
  }

  public String toUnderlyingValue() {
    return value;
  }

  private static final Map<String, ScreenAnimation> lookup = new HashMap<>();

  static {
    for (ScreenAnimation anim : ScreenAnimation.values()) {
      lookup.put(anim.toUnderlyingValue(), anim);
    }
  }

  public static ScreenAnimation fromUnderlyingValue(String anim) {
    return lookup.get(anim);
  }
}
