// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Categories for grouping components in the palette within the Designer.
 */
public enum ComponentCategory {
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
  //OBSOLETE("Old stuff"),// removed to remove old stuff from the menu
  INTERNAL("For internal use only"),
  // UNINITIALIZED is used as a default value so Swing libraries can still compile
  UNINITIALIZED("Uninitialized");

  // Mapping of component categories to names consisting only of lower-case letters,
  // suitable for appearing in URLs.
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

  private ComponentCategory(String categoryName) {
    name = categoryName;
  }

  /**
   * Returns the display name of this category, as used on the Designer palette, such
   * as "Not ready for prime time".  To get the enum name (such as "EXPERIMENTAL"),
   * use {@link #toString}.
   *
   * @return the display name of this category
   */
  public String getName() {
    return name;
  }

  /**
   * Returns a version of the name of this category consisting of only lower-case
   * letters, meant for use in a URL.  For example, for the category with the enum
   * name "EXPERIMENTAL" and display name "Not ready for prime time", this returns
   * "experimental".
   *
   * @return a name for this category consisting of only lower-case letters
   */
  public String getDocName() {
    return DOC_MAP.get(name);
  }
}
