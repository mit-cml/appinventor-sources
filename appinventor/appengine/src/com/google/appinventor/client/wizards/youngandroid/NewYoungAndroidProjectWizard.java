// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.wizards.youngandroid;


import com.google.appinventor.client.Ode;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.components.FolderTreeItem;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidThemeChoicePropertyEditor;
import com.google.appinventor.client.wizards.Dialog;
import com.google.appinventor.client.wizards.NewFolderWizard;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.youngandroid.ProjectToolbar;
import com.google.appinventor.client.tracking.Tracking;
import com.google.appinventor.client.widgets.LabeledTextBox;
import com.google.appinventor.client.widgets.Validator;
import com.google.appinventor.client.widgets.properties.EditableProperties;
import com.google.appinventor.client.widgets.properties.EditableProperty;
import com.google.appinventor.client.widgets.properties.SubsetJSONPropertyEditor;
import com.google.appinventor.client.wizards.NewProjectWizard;
import com.google.appinventor.client.youngandroid.TextValidators;
import com.google.appinventor.common.utils.StringUtils;
import com.google.appinventor.shared.rpc.project.youngandroid.NewYoungAndroidProjectParameters;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.common.collect.Lists;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.ui.Tree;

import java.util.logging.Logger;


/**
 * Wizard for creating new Young Android projects.
 *
 * @author markf@google.com (Mark Friedman)
 */
public final class NewYoungAndroidProjectWizard {
  interface NewYoungAndroidProjectWizardUiBinder extends UiBinder<Dialog, NewYoungAndroidProjectWizard> {}
  private static final NewYoungAndroidProjectWizardUiBinder UI_BINDER = GWT.create(NewYoungAndroidProjectWizardUiBinder.class);

  private static final Logger LOG = Logger.getLogger(NewYoungAndroidProjectWizard.class.getName());


  EditableProperty theme;
  EditableProperty toolkit;
  // UI element for project name
  @UiField Dialog addDialog;
  @UiField Button addButton;
  @UiField Button cancelButton;
  @UiField LabeledTextBox projectNameTextBox;
  @UiField YoungAndroidThemeChoicePropertyEditor themeEditor;
  @UiField SubsetJSONPropertyEditor blockstoolkitEditor;
  @UiField HorizontalPanel horizontalThemePanel;
  @UiField HorizontalPanel horizontalBlocksPanel;
  @UiField Label themeLabel;
  @UiField Label blocksLabel;
  String errorMessage = "";
  
  /**
   * Creates a new YoungAndroid project wizard.
   */
  public NewYoungAndroidProjectWizard() {
    UI_BINDER.createAndBindUi(this);
    addDialog.center();
    projectNameTextBox.setFocus(true);

    EditableProperties themes = new EditableProperties(false);
    theme = new EditableProperty(themes, "theme", MESSAGES.classicTheme(), "Theme", new YoungAndroidThemeChoicePropertyEditor(), 0x01, "", null);
    themeEditor.setProperty(theme);

    EditableProperties toolkits = new EditableProperties(false);
    toolkit = new EditableProperty(toolkits, MESSAGES.blocksToolkitTitle(), "", MESSAGES.blocksToolkitTitle(), new SubsetJSONPropertyEditor(), 0x01, "", null);
    blockstoolkitEditor.setProperty(toolkit);

    horizontalThemePanel.setCellWidth(themeLabel, "40%");
    horizontalThemePanel.setCellWidth(themeEditor, "40%");

    horizontalBlocksPanel.setCellWidth(blocksLabel, "40%");
    horizontalBlocksPanel.setCellWidth(blockstoolkitEditor, "40%");
  }

  @UiHandler("cancelButton")
  void cancelAdd(ClickEvent e) {
    addDialog.hide();
  }

  @UiHandler("addButton")
  void addProject(ClickEvent e) {
    TextValidators.ProjectNameStatus status = TextValidators.checkNewProjectName(projectNameTextBox.getText());
    if (status == TextValidators.ProjectNameStatus.SUCCESS) {
      LOG.info("Project status success");
      createProject();
      addDialog.hide();
    } else {
      LOG.info("Checking for error");
      errorMessage = TextValidators.getErrorMessage(projectNameTextBox.getText());
      if (errorMessage.length() > 0) {
        LOG.info("Found error: " + errorMessage);
        projectNameTextBox.setErrorMessage(errorMessage);
      } else {
        errorMessage = TextValidators.getWarningMessages(projectNameTextBox.getText());
        if (errorMessage.length() > 0) {
          projectNameTextBox.setErrorMessage(errorMessage);
        } else {
          // Internationalize or change handling here.
          projectNameTextBox.setErrorMessage("There has been an unexpected error validating the project name.");
        }
      }
    }
  }

//    projectNameTextBox.getTextBox().addKeyUpHandler(new KeyUpHandler() {
//      @Override
//      public void onKeyUp(KeyUpEvent event) { //Validate the text each time a key is lifted
//        projectNameTextBox.validate();
//      }
//    });

  public void createProject() {
    String projectName = projectNameTextBox.getText().trim();
    projectName = projectName.replaceAll("( )+", " ").replace(" ", "_");
    if (TextValidators.checkNewProjectName(projectName)
            == TextValidators.ProjectNameStatus.SUCCESS) {
      String packageName = StringUtils.getProjectPackage(
          Ode.getInstance().getUser().getUserEmail(), projectName);
      NewYoungAndroidProjectParameters parameters = new NewYoungAndroidProjectParameters(
          packageName, theme.getValue(), toolkit.getValue());
      NewProjectWizard.NewProjectCommand callbackCommand = new NewProjectWizard.NewProjectCommand() {
        @Override
        public void execute(final Project project) {
          Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
              if (Ode.getInstance().screensLocked()) { // Wait until I/O finished
                Scheduler.get().scheduleDeferred(this); // on other project
              } else {
                Ode.getInstance().openYoungAndroidProjectInDesigner(project);
              }
            }
          });
        }
      };

      NewProjectWizard.createNewProject(YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE, projectName,
          parameters, callbackCommand);
      Tracking.trackEvent(Tracking.PROJECT_EVENT, Tracking.PROJECT_ACTION_NEW_YA, projectName);

    }
  }
}
