// -*- mode: java; c-basic-offset: 2; -*-
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;

/**
 * Property editor for choosing UDOO connection transport
 *
 * @author francesco.monte@gmail.com
 */
public class YoungAndroidUdooTransportsChoicePropertyEditor extends ChoicePropertyEditor {

  private static final Choice[] transports = new Choice[] {
    new Choice("local", "local"),
    new Choice("remote", "remote")
  };

  public YoungAndroidUdooTransportsChoicePropertyEditor() {
    super(transports);
  }
}
