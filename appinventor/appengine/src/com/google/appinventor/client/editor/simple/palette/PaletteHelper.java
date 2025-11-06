// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2011-2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.palette;

import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * An interface that a palette can implement in order to support showing components in a defined
 * way other than alphabetical.
 */
public interface PaletteHelper {
  /**
   * Adds a palette item to the panel.
   *
   * @param panel the panel to add the component to
   * @param component the component to add
   */
  void addPaletteItem(VerticalPanel panel, SimplePaletteItem component);

  /**
   * Clears the palette.
   */
  void clear();
}
