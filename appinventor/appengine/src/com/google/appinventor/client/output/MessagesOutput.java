// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.output;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Output panel for displaying messages (e.g. from build and deploy commands).
 *
 */
public final class MessagesOutput extends Composite {

  // Singleton build output instance
  private final static MessagesOutput INSTANCE = new MessagesOutput();

  // UI elements
  private final VerticalPanel panel;
  private final HTML text;

  /**
   * Returns singleton messages output instance.
   *
   * @return  messages output instance
   */
  public static MessagesOutput getMessagesOutput() {
    return INSTANCE;
  }

  /**
   * Creates a new output panel for messages.
   */
  private MessagesOutput() {
    // Initialize UI
    text = new HTML();
    text.setSize("100%", "100%");
    text.setStylePrimaryName("ode-MessagesOutput");

    panel = new VerticalPanel();
    panel.add(text);
    panel.setSize("100%", "100%");
    panel.setCellHeight(text, "100%");
    panel.setCellWidth(text, "100%");

    initWidget(panel);
  }

  /**
   * Clears the current build messages.
   */
  public void clear() {
    text.setHTML("");
  }

  /**
   * Appends the given messages to any existing messages.
   *
   * @param messages  build messages to add
   */
  public void addMessages(String messages) {
    String s = text.getHTML();
    if (!s.isEmpty()) {
      s += "<br>";
    }
    text.setHTML(s + messages);
  }
}
