// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;

public class YoungAndroidBestFitModelPropertyEditor extends ChoicePropertyEditor {
  private static final Choice[] bestFitModels = new Choice[] {
    new Choice(MESSAGES.fitModelLinear(), "Linear"),
    new Choice(MESSAGES.fitModelQuadratic(), "Quadratic"),
    //new Choice(MESSAGES.fitModelCubic(), "Cubic"),
    new Choice(MESSAGES.fitModelExponential(), "Exponential"),
    new Choice(MESSAGES.fitModelLogarithmic(), "Logarithmic")
  };

  public YoungAndroidBestFitModelPropertyEditor() {
    super(bestFitModels);
  }
}
