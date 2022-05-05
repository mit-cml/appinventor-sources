// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;
import com.google.appinventor.components.common.ComponentConstants;

/**
 * Property editor for Chart type.
 */
public class YoungAndroidChartTypeChoicePropertyEditor extends ChoicePropertyEditor {

  // Chart type choices
  private static final Choice[] types = new Choice[]{
      new Choice(MESSAGES.lineChartType(), "0"),
      new Choice(MESSAGES.scatterChartType(), "1"),
      new Choice(MESSAGES.areaChartType(), "2"),
      new Choice(MESSAGES.barChartType(), "3"),
      new Choice(MESSAGES.pieChartType(), "4")
  };

  public YoungAndroidChartTypeChoicePropertyEditor() {
    super(types);
  }
}

