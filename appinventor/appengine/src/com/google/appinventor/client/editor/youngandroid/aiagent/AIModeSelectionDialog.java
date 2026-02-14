// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.aiagent;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.ProjectEditor;
import com.google.appinventor.client.editor.simple.components.MockForm;
import com.google.appinventor.client.editor.youngandroid.YaProjectEditor;
import com.google.appinventor.shared.settings.SettingsConstants;

import static com.google.appinventor.shared.settings.SettingsConstants.AI_AGENT_MODE_ADVISOR;
import static com.google.appinventor.shared.settings.SettingsConstants.AI_AGENT_MODE_PROJECT_EDITOR;
import static com.google.appinventor.shared.settings.SettingsConstants.AI_AGENT_MODE_SCREEN_EDITOR;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * A self-contained mode picker dialog for selecting the AI agent permission level.
 *
 * <p>When the user selects a mode and confirms, the AIAgentMode project
 * setting is updated on Screen1 and the provided callback is invoked.</p>
 */
public class AIModeSelectionDialog {

  private final AIContextCollector contextCollector;
  private final Runnable onModeSelected;

  /**
   * Constructs a mode selection dialog.
   *
   * @param contextCollector used to read the current project ID
   * @param onModeSelected   called after the mode is set and editors are saved
   */
  public AIModeSelectionDialog(AIContextCollector contextCollector, Runnable onModeSelected) {
    this.contextCollector = contextCollector;
    this.onModeSelected = onModeSelected;
  }

  /**
   * Shows the mode selection dialog. If the user selects a mode and clicks
   * "Select and Open", the AIAgentMode property is set on Screen1, dirty
   * editors are saved, and then {@code onModeSelected} is invoked.
   */
  public void show() {
    final DialogBox modeDialog = new DialogBox(false, true);
    modeDialog.setText(MESSAGES.aiChatDialogTitle());
    modeDialog.setAnimationEnabled(true);

    VerticalPanel panel = new VerticalPanel();
    panel.setSpacing(8);
    panel.getElement().getStyle().setPadding(12, Unit.PX);

    panel.add(new Label(MESSAGES.aiModeSelectionHeader()));

    final RadioButton advisorRadio = new RadioButton("aiMode",
        MESSAGES.aiAgentModeAdvisor() + " \u2014 "
        + MESSAGES.aiAgentModeAdvisorDescription());
    final RadioButton screenEditorRadio = new RadioButton("aiMode",
        MESSAGES.aiAgentModeScreenEditor() + " \u2014 "
        + MESSAGES.aiAgentModeScreenEditorDescription());
    final RadioButton projectEditorRadio = new RadioButton("aiMode",
        MESSAGES.aiAgentModeProjectEditor() + " \u2014 "
        + MESSAGES.aiAgentModeProjectEditorDescription());
    advisorRadio.setValue(true);

    panel.add(advisorRadio);
    panel.add(screenEditorRadio);
    panel.add(projectEditorRadio);

    Label warning = new Label(MESSAGES.aiModeWarning());
    warning.getElement().getStyle().setColor("#c0392b");
    warning.getElement().getStyle().setFontSize(12, Unit.PX);
    panel.add(warning);

    HorizontalPanel buttons = new HorizontalPanel();
    buttons.setSpacing(8);

    Button selectButton = new Button(MESSAGES.aiModeSelectAndOpen());
    selectButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        String selectedMode;
        if (projectEditorRadio.getValue()) {
          selectedMode = AI_AGENT_MODE_PROJECT_EDITOR;
        } else if (screenEditorRadio.getValue()) {
          selectedMode = AI_AGENT_MODE_SCREEN_EDITOR;
        } else {
          selectedMode = AI_AGENT_MODE_ADVISOR;
        }
        // Set the AIAgentMode on the Screen1 form component property.
        // MockForm.onPropertyChange will propagate this to project settings
        // and the property will be persisted in Screen1.scm for the backend.
        ProjectEditor projectEditor = Ode.getInstance().getEditorManager()
            .getOpenProjectEditor(contextCollector.getCurrentProjectId());
        if (projectEditor instanceof YaProjectEditor) {
          YaProjectEditor yaProjectEditor = (YaProjectEditor) projectEditor;
          MockForm form = (MockForm) yaProjectEditor.getFormFileEditor("Screen1").getRoot();
          if (form != null) {
            form.changeProperty(
                SettingsConstants.YOUNG_ANDROID_SETTINGS_AI_AGENT_MODE,
                selectedMode);
          }
        }
        modeDialog.hide();
        // Force an immediate save so the backend sees the updated mode
        // in Screen1.scm before the first AI request is sent.
        Ode.getInstance().getEditorManager().saveDirtyEditors(new Command() {
          @Override
          public void execute() {
            onModeSelected.run();
          }
        });
      }
    });

    Button cancelButton = new Button(MESSAGES.aiChatCloseButton());
    cancelButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        modeDialog.hide();
      }
    });

    buttons.add(selectButton);
    buttons.add(cancelButton);
    panel.add(buttons);

    modeDialog.setWidget(panel);
    modeDialog.center();
    modeDialog.show();
  }
}
