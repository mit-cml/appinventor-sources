// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.boxes;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.widgets.boxes.Box;

/**
 * Box implementation for palette panels.
 *
 */
public final class PaletteBox extends Box {

  // Singleton palette box instance
  private static final PaletteBox INSTANCE = new PaletteBox();

  /**
   * Return the palette box.
   *
   * @return  palette box
   */
  public static PaletteBox getPaletteBox() {
    return INSTANCE;
  }

  /**
   * Creates new palette box.
   */
  private PaletteBox() {
    super(MESSAGES.paletteBoxCaption(),
        200,       // height
        false,     // minimizable
        false,     // removable
        false,     // startMinimized
        false,     // usePadding
        false);    // highlightCaption
  }
}
