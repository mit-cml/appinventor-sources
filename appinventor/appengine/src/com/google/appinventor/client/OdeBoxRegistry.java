// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

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
