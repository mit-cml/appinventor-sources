// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime;

import android.view.ViewGroup;

/**
 * The Layout interface provides methods for working with Simple
 * component layouts.
 *
 */
public interface Layout {

  /**
   * Returns the view group (which is a container with a layout manager)
   * associated with the layout.
   *
   * @return  view group
   */
  ViewGroup getLayoutManager();

  /**
   * Adds the specified component to this layout.
   *
   * @param component  component to add
   */
  void add(AndroidViewComponent component);
}
