// Copyright 2009 Google Inc. All Rights Reserved.

package com.google.appinventor.client.boxes;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.client.widgets.boxes.Box;

/**
 * Box implementation for OdeLog output.
 *
 * <p>Used by developers on the ODE team only - not intended for production.
 *
 * @see OdeLog
 *
 */
public final class OdeLogBox extends Box {

  // Singleton log box instance
  private static final OdeLogBox INSTANCE = new OdeLogBox();

  /**
   * Return the singleton ODE log box.
   *
   * @return  log box
   */
  public static OdeLogBox getOdeLogBox() {
    return INSTANCE;
  }

  /**
   * Creates new ODE log box.
   */
  private OdeLogBox() {
    super(MESSAGES.odeLogBoxCaption(), 500,
        true,   // minimizable
        false,  // removable
        false); // startMinimized
    setContent(OdeLog.getOdeLog());
  }
}
