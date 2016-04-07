// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2016 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets.properties;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * Property editor controlling how a picture is scaled.
 */
public class ScalingChoicePropertyEditor extends ChoicePropertyEditor {

  private static final Choice[] SCALING_MODES = new Choice[] {
    new Choice(MESSAGES.scaleProportionally(), "0"),
    new Choice(MESSAGES.scaleToFit(), "1")
  };

  public ScalingChoicePropertyEditor() {
    super(SCALING_MODES);
  }
}
