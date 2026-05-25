// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.aiagent.dialog;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Label;

/**
 * Manages the status label ("Thinking...") with an animated ellipsis.
 *
 * <p>When visible, cycles through ".", "..", "..." on a 600ms timer.
 * When hidden, the timer is stopped.</p>
 */
public class StatusAnimator {

  private static final String[] ELLIPSIS = {".", "..", "..."};

  private final Label statusLabel;
  private String baseText = "";
  private int ellipsisCounter;
  private Timer ellipsisTimer;

  /**
   * Constructs the status label with default styling.
   */
  public StatusAnimator() {
    statusLabel = new Label();
    statusLabel.getElement().getStyle().setProperty("fontStyle", "italic");
    statusLabel.getElement().getStyle().setColor("#666");
    statusLabel.getElement().getStyle().setMarginBottom(4, Unit.PX);
    statusLabel.setVisible(false);
  }

  /**
   * Returns the label widget for adding to a parent panel.
   */
  public Label getWidget() {
    return statusLabel;
  }

  /**
   * Sets the base text displayed before the animated ellipsis.
   */
  public void setText(String text) {
    baseText = text != null ? text : "";
    ellipsisCounter = 0;
    statusLabel.setText(baseText);
  }

  /**
   * Shows or hides the status label. Starts the ellipsis animation
   * when visible; stops it when hidden.
   */
  public void setVisible(boolean visible) {
    statusLabel.setVisible(visible);
    if (visible) {
      startAnimation();
    } else {
      stopAnimation();
    }
  }

  private void startAnimation() {
    stopAnimation();
    ellipsisCounter = 0;
    ellipsisTimer = new Timer() {
      @Override
      public void run() {
        ellipsisCounter++;
        String dots = ELLIPSIS[ellipsisCounter % ELLIPSIS.length];
        statusLabel.setText(baseText + dots);
      }
    };
    ellipsisTimer.scheduleRepeating(600);
  }

  private void stopAnimation() {
    if (ellipsisTimer != null) {
      ellipsisTimer.cancel();
      ellipsisTimer = null;
    }
  }
}
