// -*- mode: java; c-basic-offset: 2; -*-
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;

/**
 * Property editor for choosing GPIO port direction.
 *
 * @author francesco.monte@gmail.com
 */
public class YoungAndroidGpioDirectionsChoicePropertyEditor extends ChoicePropertyEditor {

  private static final Choice[] directions = new Choice[] {
    new Choice("in", "in"),
    new Choice("out", "out")
  };

  public YoungAndroidGpioDirectionsChoicePropertyEditor() {
    super(directions);
  }
}
