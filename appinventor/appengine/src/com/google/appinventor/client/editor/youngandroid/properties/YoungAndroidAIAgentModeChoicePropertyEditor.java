// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * Property editor for the AI agent mode setting on Screen1.
 */
public class YoungAndroidAIAgentModeChoicePropertyEditor extends ChoicePropertyEditor {

  private static final Choice[] modes = new Choice[] {
    new Choice(MESSAGES.aiAgentModeOff(), "Off"),
    new Choice(MESSAGES.aiAgentModeAdvisor(), "Advisor"),
    new Choice(MESSAGES.aiAgentModeScreenEditor(), "ScreenEditor"),
    new Choice(MESSAGES.aiAgentModeProjectEditor(), "ProjectEditor")
  };

  public YoungAndroidAIAgentModeChoicePropertyEditor() {
    super(modes);
  }
}
