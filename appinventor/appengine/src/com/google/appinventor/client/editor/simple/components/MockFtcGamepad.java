// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2011-2016 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.gwt.user.client.ui.Image;

/**
 * Mock FtcGamepad component.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public final class MockFtcGamepad extends MockNonVisibleComponent {

  /**
   * Component type name.
   */
  public static final String TYPE = "FtcGamepad";

  private static final String PROPERTY_NAME_GAMEPADNUMBER = "GamepadNumber";

  public MockFtcGamepad(SimpleEditor editor, String type, Image iconImage) {
    super(editor, type, iconImage);
  }

  @Override
  public void onCreateFromPalette() {
    // Change number property for FtcGamepad2
    if (getName().equals(MESSAGES.ftcGamepadComponentPallette() + "2")) {
      changeProperty(PROPERTY_NAME_GAMEPADNUMBER, "2");
    }
    super.onCreateFromPalette();
  }
}
