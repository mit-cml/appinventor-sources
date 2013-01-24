// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package com.google.appinventor.client.editor.youngandroid.properties;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;
import com.google.appinventor.components.common.ComponentConstants;

/**
 * Property editor for horizontal alignment choice.
 *
 * @author hal@mit.edu (Hal Abelson)
 */
public class YoungAndroidHorizontalAlignmentChoicePropertyEditor extends ChoicePropertyEditor {

  // We need to get these constants as strings because we do a switch on these values
  // in MOckHVLayoutBase.  And you can't switch on strings in Java before Java 7.
  public static final String LEFT = ComponentConstants.GRAVITY_LEFT + "";
  public static final String RIGHT = ComponentConstants.GRAVITY_RIGHT + "";
  public static final String CENTER = ComponentConstants.GRAVITY_CENTER_HORIZONTAL + "";

  private static final Choice[] hAlignments = new Choice[] {
    new Choice(MESSAGES.horizontalAlignmentChoiceLeft(), LEFT),
    new Choice(MESSAGES.horizontalAlignmentChoiceCenter(), CENTER),
    new Choice(MESSAGES.horizontalAlignmentChoiceRight(), RIGHT)
  };

  public YoungAndroidHorizontalAlignmentChoicePropertyEditor() {
    super(hAlignments);
  }
}
