// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.boxes;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.output.MotdUi;
import com.google.appinventor.client.widgets.boxes.Box;
import com.google.appinventor.client.widgets.boxes.Box.BoxDescriptor;

/**
 * Box implementation for MOTD-like messages to the user.
 *
 */
public final class MotdBox extends Box {

  private static final int HEIGHT = 55;
  private static final int WIDTH = 250;

  // Singleton log box instance
  private static final MotdBox INSTANCE = new MotdBox();

  /**
   * Return the singleton MOTD box.
   *
   * @return  MOTD box
   */
  public static MotdBox getMotdBox() {
    return INSTANCE;
  }

  /**
   * Creates new MOTD box.
   */
  private MotdBox() {
    super(MESSAGES.motdBoxCaption(), HEIGHT, true, false, false, true);
    MotdUi motdUi = MotdUi.getMotd();
    motdUi.setBox(this);
    setVariableHeightBoxes(true);  // don't allow user to resize MOTD box
    setContent(motdUi);
    onResize(WIDTH, HEIGHT);
    // force it to start minimized
    BoxDescriptor bd = new BoxDescriptor(MotdBox.class, WIDTH, HEIGHT, true);
    restoreLayoutSettings(bd);
  }

  public void forceRestore() {
    BoxDescriptor bd = new BoxDescriptor(MotdBox.class, WIDTH, HEIGHT, false);
    restoreLayoutSettings(bd);
  }

}
