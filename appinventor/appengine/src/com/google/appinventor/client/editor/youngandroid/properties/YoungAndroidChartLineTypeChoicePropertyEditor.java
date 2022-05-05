// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;
import com.google.appinventor.components.common.ComponentConstants;

/**
 * Property editor for Chart Line type.
 */
public class YoungAndroidChartLineTypeChoicePropertyEditor extends ChoicePropertyEditor {

  // Chart Point Shape choices
  private static final Choice[] types = new Choice[] {
      new Choice(MESSAGES.lineTypeLinear(), ComponentConstants.CHART_LINE_TYPE_LINEAR + ""),
      new Choice(MESSAGES.lineTypeCurved(), ComponentConstants.CHART_LINE_TYPE_CURVED + ""),
      new Choice(MESSAGES.lineTypeStepped(), ComponentConstants.CHART_LINE_TYPE_STEPPED + "")
  };

  public YoungAndroidChartLineTypeChoicePropertyEditor() {
    super(types);
  }
}
