// Copyright 2009 Google Inc. All Rights Reserved.

package com.google.appinventor.client;

import com.google.appinventor.client.boxes.MessagesOutputBox;
import com.google.appinventor.client.boxes.OdeLogBox;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.client.widgets.boxes.BoxRegistry;

/**
 * Registry of boxes for layouts.
 *
 */
public final class OdeBoxRegistry extends BoxRegistry {

  /**
   * Creates a new box registry.
   */
  public OdeBoxRegistry() {
    register(MessagesOutputBox.getMessagesOutputBox());

    if (OdeLog.isLogAvailable()) {
      register(OdeLogBox.getOdeLogBox());
    }
  }
}
