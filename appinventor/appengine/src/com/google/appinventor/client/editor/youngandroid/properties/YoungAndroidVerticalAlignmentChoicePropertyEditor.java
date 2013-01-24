// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package com.google.appinventor.client.editor.youngandroid.properties;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;
import com.google.appinventor.components.common.ComponentConstants;

// cludge for now.  mimic how this is done for button shape


/**
 * Property editor for vertical alignment choice.
 *
 * @author hal@mit.edu (Hal Abelson)
 */
public class YoungAndroidVerticalAlignmentChoicePropertyEditor extends ChoicePropertyEditor {

  // We need to get these constants as strings because we do a switch on these values
  // in MOckHVLayoutBase.  And you can't switch on strings in Java before Java 7.
  public static final String TOP = ComponentConstants.GRAVITY_TOP + "";
  public static final String CENTER = ComponentConstants.GRAVITY_CENTER_VERTICAL + "";
  public static final String BOTTOM = ComponentConstants.GRAVITY_BOTTOM + "";

  private static final Choice[] vAlignments = new Choice[] {
    new Choice(MESSAGES.verticalAlignmentChoiceTop(), TOP),
    new Choice(MESSAGES.verticalAlignmentChoiceCenter(), CENTER),
    new Choice(MESSAGES.verticalAlignmentChoiceBottom(), BOTTOM),
  };

  public YoungAndroidVerticalAlignmentChoicePropertyEditor() {
    super(vAlignments);
  }
}

