// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.boxes;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.output.MessagesOutput;
import com.google.appinventor.client.widgets.boxes.Box;

/**
 * Box implementation for message output.
 *
 */
public class MessagesOutputBox extends Box {
  // Singleton message output box instance.
  private static final MessagesOutputBox INSTANCE = new MessagesOutputBox();

  /**
   * Return the singleton message output box.
   *
   * @return  message output box
   */
  public static MessagesOutputBox getMessagesOutputBox() {
    return INSTANCE;
  }

  /**
   * Creates new message output box.
   */
  private MessagesOutputBox() {
    super(MESSAGES.messagesOutputBoxCaption(), 500, true, false);
    setContent(MessagesOutput.getMessagesOutput());
  }
}
