// Copyright 2009 Google Inc. All Rights Reserved.

package com.google.appinventor.components.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Categories for grouping components in the palette
 *
 */
public enum ComponentCategory{
  // TODO(user): i18n category names
  BASIC("Basic"),
  MEDIA("Media"),
  ANIMATION("Animation"),
  SOCIAL("Social"),
  SENSORS("Sensors"),
  ARRANGEMENTS("Screen Arrangement"),
  LEGOMINDSTORMS("LEGO\u00AE MINDSTORMS\u00AE"),
  MISC("Other stuff"),
  EXPERIMENTAL("Not ready for prime time"),
  OBSOLETE("Old stuff"),
  INTERNAL("For internal use only"),
  // UNINITIALIZED is used as a default value so Swing libraries can still compile
  UNINITIALIZED("Uninitialized");

  // Mapping of component categories to their goro documentation names
  private static final Map<String, String> DOC_MAP =
      new HashMap<String, String>();
  static {
    DOC_MAP.put("Basic", "basic");
    DOC_MAP.put("Media", "media");
    DOC_MAP.put("Animation", "animation");
    DOC_MAP.put("Social", "social");
    DOC_MAP.put("Sensors", "sensors");
    DOC_MAP.put("Screen Arrangement", "screenarrangement");
    DOC_MAP.put("LEGO\u00AE MINDSTORMS\u00AE", "legomindstorms");
    DOC_MAP.put("Other stuff", "other");
    DOC_MAP.put("Not ready for prime time", "notready");
  }

  private String name;

  ComponentCategory(String categoryName) {
    name = categoryName;
  }

  public String getName() {
    return name;
  }

  public String getDocName() {
    return DOC_MAP.get(name);
  }
}
