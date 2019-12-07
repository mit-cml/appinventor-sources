// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
package com.google.appinventor.client.editor.youngandroid.properties;

import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;
import com.google.appinventor.components.common.ComponentConstants;

// cludge for now.  mimic how this is done for button shape


/**
 * Property editor for vertical alignment choice.
 */
public class YoungAndroidNavigationMethodChoicePropertyEditor extends ChoicePropertyEditor {

  private static final Choice[] methods = new Choice[] {
    new Choice("foot-walking", "foot-walking"),
    new Choice("driving-car", "driving-car"),
    new Choice("cycling-regular", "cycling-regular"),
    new Choice ("wheelchair", "wheelchair")
  };

  public YoungAndroidNavigationMethodChoicePropertyEditor() {
    super(methods);
  }
}