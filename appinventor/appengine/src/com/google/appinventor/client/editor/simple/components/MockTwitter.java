// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.simple.SimpleEditor;

import com.google.appinventor.client.utils.MessageDialog;

import com.google.gwt.core.client.GWT;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Image;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * Mock for the non-visible Twitter component. This needs
 * a separate mock so we can provide a warning message about the component's deprecation.
 *
 * Cribbed from MockFusionTablesControl.java
 */
public class MockTwitter extends MockNonVisibleComponent {

  public static final String TYPE = "Twitter";
  private static boolean warningGiven = false; // Whether or not we have given the
                                               // deprecation warning

  /**
   * Creates a new instance of a non-visible component whose icon is
   * loaded dynamically (not part of the icon image bundle)
   *
   * @param editor
   * @param type
   * @param iconImage
   */
  public MockTwitter(SimpleEditor editor, String type, Image iconImage) {
    super(editor, type, iconImage);
  }

  /**
   * Generate a dialog box indicating that the Twitter component is deprecated.
   */

  @Override
  protected void onAttach() {
    super.onAttach();
    if (!warningGiven) {
      warningGiven = true;
      DeferredCommand.addCommand(new Command() {
          @Override
          public void execute() {
            Ode.getInstance().genericWarning(MESSAGES.TwitterDeprecated());
          }
        });
    }
  }

  /**
   * Called when the component is dropped in the Designer window
   * we give a warning that the Twitter component is deprecated
   */

  @Override
  public void onCreateFromPalette() {
    if (!warningGiven) {
      warningGiven = true;
      MessageDialog.messageDialog(MESSAGES.warningDialogTitle(),
        MESSAGES.TwitterDeprecated(),
        MESSAGES.okButton(), null, null);
    }
  }

  public static void resetWarning() {
    warningGiven = false;
  }

}
