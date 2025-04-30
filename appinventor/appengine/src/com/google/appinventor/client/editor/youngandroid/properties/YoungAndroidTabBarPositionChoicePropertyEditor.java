// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020-2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;
import com.google.appinventor.components.common.ComponentConstants;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * Property editor for tab bar position.
 */
public class YoungAndroidTabBarPositionChoicePropertyEditor extends ChoicePropertyEditor {

  // Tab bar position choices
  public static final String DEFAULT = ComponentConstants.TAB_POSITION_DEFAULT + "";
  public static final String TOP = ComponentConstants.TAB_POSITION_TOP + "";
  public static final String BOTTOM = ComponentConstants.TAB_POSITION_BOTTOM + "";

  private static final ChoicePropertyEditor.Choice[] tabAlignments = new ChoicePropertyEditor.Choice[]{
      new ChoicePropertyEditor.Choice(MESSAGES.tabBarPositionChoiceDefault() + " : " + DEFAULT, DEFAULT),
      new ChoicePropertyEditor.Choice(MESSAGES.tabBarPositionChoiceTop() + " : " + TOP, TOP),
      new ChoicePropertyEditor.Choice(MESSAGES.tabBarPositionChoiceBottom() + " : " + BOTTOM, BOTTOM),
  };

  public YoungAndroidTabBarPositionChoicePropertyEditor() {
    super(tabAlignments);
  }
}
