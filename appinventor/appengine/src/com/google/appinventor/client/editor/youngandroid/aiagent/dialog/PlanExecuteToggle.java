// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.aiagent.dialog;

import static com.google.appinventor.client.Ode.MESSAGES;
import static com.google.appinventor.shared.settings.SettingsConstants.AI_AGENT_MODE_PROJECT_EDITOR;

import com.google.appinventor.client.editor.youngandroid.aiagent.AIContextCollector;
import com.google.appinventor.client.editor.youngandroid.aiagent.AIEditorState;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;

/**
 * Manages the Plan &amp; Execute mode toggle button.
 *
 * <p>Three visual states: "direct" (grey, default), "plan" (blue, active),
 * and "executing" (blue, disabled). The button is only visible in
 * ProjectEditor mode with orchestration enabled.</p>
 */
public class PlanExecuteToggle {

  /**
   * Callback to check whether the current project has a tutorial URL.
   */
  public interface TutorialUrlCheck {
    boolean hasTutorialUrl();
  }

  private final AIContextCollector contextCollector;
  private final TutorialUrlCheck tutorialCheck;
  private final Button button;
  private boolean orchestrationEnabled;

  /**
   * Constructs the toggle button.
   *
   * @param contextCollector    provides the current AI agent mode
   * @param tutorialCheck       callback to check for tutorial URL
   * @param orchestrationEnabled initial state of the orchestration flag
   */
  public PlanExecuteToggle(AIContextCollector contextCollector,
      TutorialUrlCheck tutorialCheck, boolean orchestrationEnabled) {
    this.contextCollector = contextCollector;
    this.tutorialCheck = tutorialCheck;
    this.orchestrationEnabled = orchestrationEnabled;

    button = new Button(MESSAGES.aiChatPlanExecuteOff());
    styleButton("direct");
    button.setVisible(false);
    button.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        toggleMode();
      }
    });
  }

  /** Returns the button widget. */
  public Button getWidget() {
    return button;
  }

  /**
   * Updates visibility and enabled state based on the current AI mode,
   * orchestration flag, and whether the conversation has messages.
   */
  public void update(boolean hasConversationMessages) {
    String mode = contextCollector.getCurrentAIAgentMode();
    if (AI_AGENT_MODE_PROJECT_EDITOR.equals(mode) && orchestrationEnabled) {
      button.setVisible(true);
      if (hasConversationMessages) {
        button.setEnabled(false);
        button.getElement().getStyle().setProperty("cursor", "default");
        button.getElement().getStyle().setProperty("opacity", "0.7");
      } else {
        button.setEnabled(true);
        button.getElement().getStyle().setProperty("cursor", "pointer");
        button.getElement().getStyle().setProperty("opacity", "1");
      }
    } else {
      setActive(false);
      button.setVisible(false);
    }
  }

  /** Sets the button to "Execution Mode" state (blue, disabled). */
  public void setExecuting() {
    button.setText(MESSAGES.aiChatPlanExecuteExecuting());
    styleButton("executing");
  }

  /** Updates the stored orchestration flag. */
  public void setOrchestrationEnabled(boolean enabled) {
    this.orchestrationEnabled = enabled;
  }

  /** Resets to direct mode. Called when the dialog is hidden. */
  public void reset() {
    setActive(false);
  }

  private void toggleMode() {
    boolean newState = !AIEditorState.isPlanExecuteMode();
    if (newState && tutorialCheck.hasTutorialUrl()) {
      boolean confirmed = Window.confirm(MESSAGES.aiChatPlanExecuteTutorialConfirm());
      if (!confirmed) {
        return;
      }
    }
    setActive(newState);
  }

  private void setActive(boolean active) {
    AIEditorState.setPlanExecuteMode(active);
    if (active) {
      button.setText(MESSAGES.aiChatPlanExecuteOn());
    } else {
      button.setText(MESSAGES.aiChatPlanExecuteOff());
    }
    styleButton(active ? "plan" : "direct");
  }

  private void styleButton(String state) {
    button.getElement().getStyle().setProperty("borderRadius", "3px");
    button.getElement().getStyle().setFontSize(12, Unit.PX);
    if ("direct".equals(state)) {
      button.getElement().getStyle().setProperty("background", "#f5f5f5");
      button.getElement().getStyle().setColor("#333");
      button.getElement().getStyle().setProperty("border", "1px solid #ccc");
      button.getElement().getStyle().setProperty("cursor", "pointer");
      button.getElement().getStyle().setProperty("opacity", "1");
      button.setEnabled(true);
    } else if ("plan".equals(state)) {
      button.getElement().getStyle().setProperty("background", "#4a90d9");
      button.getElement().getStyle().setColor("white");
      button.getElement().getStyle().setProperty("border", "1px solid #3a7bc8");
      button.getElement().getStyle().setProperty("cursor", "pointer");
      button.getElement().getStyle().setProperty("opacity", "1");
      button.setEnabled(true);
    } else {
      button.getElement().getStyle().setProperty("background", "#4a90d9");
      button.getElement().getStyle().setColor("white");
      button.getElement().getStyle().setProperty("border", "1px solid #3a7bc8");
      button.getElement().getStyle().setProperty("cursor", "default");
      button.getElement().getStyle().setProperty("opacity", "0.7");
      button.setEnabled(false);
    }
  }
}
