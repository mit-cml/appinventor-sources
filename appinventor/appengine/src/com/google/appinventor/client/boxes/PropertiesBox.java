// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.boxes;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.widgets.boxes.Box;

/**
 * Box implementation for properties panels.
 *
 */
public final class PropertiesBox extends Box {

  // Singleton properties box instance
  private static final PropertiesBox INSTANCE = new PropertiesBox();

  /**
   * Return the properties box.
   *
   * @return  properties box
   */
  public static PropertiesBox getPropertiesBox() {
    return INSTANCE;
  }

  /**
   * Creates new properties box.
   */
  private PropertiesBox() {
    super(MESSAGES.propertiesBoxCaption(),
        200,    // height
        false,  // minimizable
        false,  // removable
        false,  // startMinimized
        false,  // bodyPadding
        false); // highlightCaption
  }
}
