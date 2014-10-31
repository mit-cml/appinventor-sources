// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
package com.google.appinventor.client.editor.youngandroid.properties;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;
import com.google.appinventor.components.common.ComponentConstants;

/**
 * Property editor for selecting how texts are received.
 *
 * @author jis@mit.edu (Jeff Schiller)
 */
public class YoungAndroidTextReceivingPropertyEditor extends ChoicePropertyEditor {

  // Never
  public static final String OFF = ComponentConstants.TEXT_RECEIVING_OFF + "";
  // Only when the App is in the foreground
  public static final String FOREGROUND = ComponentConstants.TEXT_RECEIVING_FOREGROUND + "";
  // Always, even when the app is not running (display a notification)
  public static final String ALWAYS = ComponentConstants.TEXT_RECEIVING_ALWAYS + "";

  private static final Choice[] tchoices = new Choice[] {
    new Choice(MESSAGES.textReceivingChoiceOff(), OFF),
    new Choice(MESSAGES.textReceivingChoiceForeground(), FOREGROUND),
    new Choice(MESSAGES.textReceivingChoiceAlways(), ALWAYS)
  };

  public YoungAndroidTextReceivingPropertyEditor() {
    super(tchoices);
  }
}
