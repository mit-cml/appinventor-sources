// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;
import com.google.appinventor.shared.settings.SettingsConstants;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * Property editor for the AI agent mode setting on Screen1.
 */
public class YoungAndroidAIAgentModeChoicePropertyEditor extends ChoicePropertyEditor {

  private static final Choice[] modes = new Choice[] {
    new Choice(MESSAGES.aiAgentModeOff(), SettingsConstants.AI_AGENT_MODE_OFF),
    new Choice(MESSAGES.aiAgentModeAdvisor(), SettingsConstants.AI_AGENT_MODE_ADVISOR),
    new Choice(MESSAGES.aiAgentModeScreenEditor(), SettingsConstants.AI_AGENT_MODE_SCREEN_EDITOR),
    new Choice(MESSAGES.aiAgentModeProjectEditor(), SettingsConstants.AI_AGENT_MODE_PROJECT_EDITOR)
  };

  public YoungAndroidAIAgentModeChoicePropertyEditor() {
    super(modes);
  }
}
