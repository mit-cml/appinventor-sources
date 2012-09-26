// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

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
