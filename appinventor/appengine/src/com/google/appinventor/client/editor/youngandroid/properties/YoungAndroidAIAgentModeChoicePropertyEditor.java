// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.properties;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.widgets.properties.ChoicePropertyEditor;
import com.google.appinventor.shared.settings.SettingsConstants;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * Property editor for the AI agent mode setting on Screen1.
 *
 * <p>The ScreenEditor and ProjectEditor choices are gated by the
 * {@code ai.agent.features.editing-modes} server flag, exposed via
 * {@link Ode#getSystemConfig()}. When disabled, only Off and Advisor are
 * offered; the server additionally coerces any pre-existing editor-mode
 * setting to Advisor at read time (see {@code AIAgentEngine.coerceMode}).
 */
public class YoungAndroidAIAgentModeChoicePropertyEditor extends ChoicePropertyEditor {

  public YoungAndroidAIAgentModeChoicePropertyEditor() {
    super(buildChoices());
  }

  private static Choice[] buildChoices() {
    Choice off = new Choice(MESSAGES.aiAgentModeOff(),
        SettingsConstants.AI_AGENT_MODE_OFF);
    Choice advisor = new Choice(MESSAGES.aiAgentModeAdvisor(),
        SettingsConstants.AI_AGENT_MODE_ADVISOR);
    if (!Ode.getSystemConfig().getAiAgentEditingModesEnabled()) {
      return new Choice[] { off, advisor };
    }
    return new Choice[] {
        off,
        advisor,
        new Choice(MESSAGES.aiAgentModeScreenEditor(),
            SettingsConstants.AI_AGENT_MODE_SCREEN_EDITOR),
        new Choice(MESSAGES.aiAgentModeProjectEditor(),
            SettingsConstants.AI_AGENT_MODE_PROJECT_EDITOR)
    };
  }
}
