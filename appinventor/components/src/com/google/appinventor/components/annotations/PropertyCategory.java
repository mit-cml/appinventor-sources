// Copyright 2009 Google Inc. All Rights Reserved.

package com.google.appinventor.components.annotations;

/**
 * Categories for Simple properties.  This is used only for documentation.
 *
 */
public enum PropertyCategory {
  // TODO(user): i18n category names
  BEHAVIOR("Behavior"),
  APPEARANCE("Appearance"),
  DEPRECATED("Deprecated"),
  UNSET("Unspecified");

  private String name;

  PropertyCategory(String categoryName) {
    name = categoryName;
  }

  public String getName() {
    return name;
  }
}
