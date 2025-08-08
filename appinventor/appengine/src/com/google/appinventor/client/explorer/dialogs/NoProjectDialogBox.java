// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.dialogs;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.wizards.NewProjectWizard.NewProjectCommand;
import com.google.appinventor.client.wizards.TemplateUploadWizard;
import com.google.appinventor.client.wizards.youngandroid.NewYoungAndroidProjectWizard;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * A dialog containing options to begin 3 different tutorials or being a new
 * project from scratch. Should appear when the user currently has no projects
 * in their projects list.
 */
public class NoProjectDialogBox extends DialogBox {

  private static NoProjectDialogBoxUiBinder uiBinder =
      GWT.create(NoProjectDialogBoxUiBinder.class);
  private static NoProjectDialogBox lastDialog = null;

  interface NoProjectDialogBoxUiBinder extends UiBinder<Widget, NoProjectDialogBox> {
  }

  /**
   * Class to open a new project with the tutorial's contents when the user
   * clicks on the "Go to Tutorial" button.
   */
  private class NewTutorialProject implements NewProjectCommand {
    public void execute(Project project) {
      Ode.getInstance().openYoungAndroidProjectInDesigner(project);
    }
  }

  @UiField
  Button closeDialogBox;
  @UiField
  Button goToPurr;
  @UiField
  Button goToChat;
  @UiField
  Button goToYR;
  @UiField
  Button noDialogNewProject;
  @UiField
  Button topInvisible;
  @UiField
  Button bottomInvisible;

  /**
   * Creates a new dialog box when the user has no current projects in their
   * projects list. This will give them an option to open a tutorial project or
   * create their own project.
   */
  public NoProjectDialogBox() {
    this.setStylePrimaryName("ode-noDialogDiv");
    add(uiBinder.createAndBindUi(this));
    this.center();
    this.setAnimationEnabled(true);
    this.setAutoHideEnabled(true);
    this.setModal(false);
    noDialogNewProject.setFocus(true);
    lastDialog = this;
  }

  @UiHandler("closeDialogBox")
  void handleClose(ClickEvent e) {
    this.hide();
  }

  @UiHandler("goToPurr")
  void handleGoToPurr(ClickEvent e) {
    this.hide();
    new TemplateUploadWizard().createProjectFromExistingZip("HelloPurr", new NewTutorialProject(),
            "HelloPurr");
  }

  @UiHandler("goToChat")
  void handleGoToChat(ClickEvent e) {
    this.hide();
    new TemplateUploadWizard().createProjectFromExistingZip("SimpleChatbot", new NewTutorialProject(),
        "SimpleChatbot");
  }

  @UiHandler("goToYR")
  void handleGoToYR(ClickEvent e) {
    this.hide();
    TemplateUploadWizard.openProjectFromTemplate(Window.Location.getProtocol()
        + "//appinventor.mit.edu/yrtoolkit/yr/aiaFiles/hello_bonjour/translate_tutorial.asc",
        new NewTutorialProject());
  }

  @UiHandler("noDialogNewProject")
  void handleNewProject(ClickEvent e) {
    this.hide();
    new NewYoungAndroidProjectWizard().show();
  }

  public static void closeIfOpen() {
    if (lastDialog != null) {
      lastDialog.removeFromParent();;
      lastDialog = null;
    }
  }

  @UiHandler("topInvisible")
  protected void FocusLast(FocusEvent event) {
     closeDialogBox.setFocus(true);
  }

  @UiHandler("bottomInvisible")
  protected void FocusFirst(FocusEvent event) {
     goToPurr.setFocus(true);
  }
}
