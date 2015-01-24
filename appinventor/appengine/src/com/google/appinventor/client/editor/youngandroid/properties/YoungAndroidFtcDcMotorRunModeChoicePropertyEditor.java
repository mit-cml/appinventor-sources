// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;

/**
 * Property editor for FTC DC motor run mode.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class YoungAndroidFtcDcMotorRunModeChoicePropertyEditor extends ChoicePropertyEditor {

  // FTC DC motor run mode choices
  private static final Choice[] ftcDcMotorRunModeChoices = new Choice[] {
    new Choice(MESSAGES.runFtcDcMotorRunMode(), "RUN"),
    new Choice(MESSAGES.runNoEncoderFtcDcMotorRunMode(), "RUN_NO_ENCODER"),
    new Choice(MESSAGES.positionFtcDcMotorRunMode(), "POSITION"),
    new Choice(MESSAGES.resetFtcDcMotorRunMode(), "RESET"),
  };

  public YoungAndroidFtcDcMotorRunModeChoicePropertyEditor() {
    super(ftcDcMotorRunModeChoices);
  }
}
