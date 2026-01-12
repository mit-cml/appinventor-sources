// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2017-2025 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.blocks;

import com.google.gwt.resources.client.ImageResource;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * BlocksCategory represents a category of blocks in the blocks editor. Instances can be used to
 * construct {@link BlocksLanguage}s by combining different categories of blocks together.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public final class BlocksCategory {
  private final String category;
  private final String translatedName;
  private final ImageResource image;

  public BlocksCategory(String category, ImageResource image) {
    this(category, getBuiltinDrawerNames(category), image);
  }

  public BlocksCategory(String category, String translatedName, ImageResource image) {
    this.category = category;
    this.translatedName = translatedName;
    this.image = image;
  }

  @Override
  public int hashCode() {
    return category.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (o == null) {
      return false;
    } else if (!getClass().equals(o.getClass())) {
      return false;
    } else {
      BlocksCategory other = (BlocksCategory) o;
      return other.category.equals(category);
    }
  }

  public String getCategory() {
    return category;
  }

  public String getName() {
    return translatedName;
  }

  public ImageResource getImage() {
    return image;
  }

  /**
   * Given the drawerName, return the name in current language setting
   */
  private static String getBuiltinDrawerNames(String drawerName) {
    String name;

    if (drawerName.equals("Control")) {
      name = MESSAGES.builtinControlLabel();
    } else if (drawerName.equals("Logic")) {
      name = MESSAGES.builtinLogicLabel();
    } else if (drawerName.equals("Math")) {
      name = MESSAGES.builtinMathLabel();
    } else if (drawerName.equals("Text")) {
      name = MESSAGES.builtinTextLabel();
    } else if (drawerName.equals("Lists")) {
      name = MESSAGES.builtinListsLabel();
    } else if (drawerName.equals("Colors")) {
      name = MESSAGES.builtinColorsLabel();
    } else if (drawerName.equals("Variables")) {
      name = MESSAGES.builtinVariablesLabel();
    } else if (drawerName.equals("Procedures")) {
      name = MESSAGES.builtinProceduresLabel();
    } else if (drawerName.equals("Dictionaries")) {
      name = MESSAGES.builtinDictionariesLabel();
    } else {
      name = drawerName;
    }
    return name;
  }
}
