// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.output;

import com.google.appinventor.client.boxes.MotdBox;
import com.google.appinventor.shared.rpc.Motd;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Output panel for displaying MOTD.
 *
 */
public final class MotdUi extends Composite {

  // Singleton build output instance
  private final static MotdUi INSTANCE = new MotdUi();

  // UI elements
  private final VerticalPanel panel;
  private final HTML text;

  // So can manipulate the header and minimize/restore state.
  private MotdBox box;

  private Motd currentMotd = null;  // last one we have seen

  /**
   * Returns singleton MOTD instance.
   *
   * @return  motd output instance
   */
  public static MotdUi getMotd() {
    return INSTANCE;
  }

  /**
   * Creates a new output panel for MOTD.
   */
  private MotdUi() {
    // Initialize UI
    text = new HTML();
    text.setSize("100%", "100%");
    text.setStylePrimaryName("ode-Motd");

    panel = new VerticalPanel();
    panel.add(text);
    panel.setSize("100%", "100%");
    panel.setCellHeight(text, "100%");
    panel.setCellWidth(text, "100%");

    initWidget(panel);
  }

  /**
   * Clears the current MOTD.
   */
  public void clear() {
    text.setHTML("");
  }

  public void setBox(MotdBox box) {
    this.box = box;
  }

  /**
   * Replace the current MOTD.
   *
   * @param motd  new motd
   */
  public void setMotd(Motd motd) {
    if (box == null) return;
    if ((currentMotd == null) || ! currentMotd.equals(motd)) {
      currentMotd = motd;
      box.setCaption(motd.getCaption());
      if (motd.hasContent()) {
        text.setHTML(motd.getContent());
        box.forceRestore();
      }
    }
  }
}
