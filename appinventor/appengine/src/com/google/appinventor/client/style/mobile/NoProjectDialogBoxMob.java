// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.style.mobile;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.explorer.dialogs.NoProjectDialogBox;
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
public class NoProjectDialogBoxMob extends NoProjectDialogBox {

  interface NoProjectDialogBoxUiBinderMob extends UiBinder<Widget, NoProjectDialogBoxMob> {
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

  public void bindUI(){
    NoProjectDialogBoxUiBinderMob uiBinder = GWT.create(NoProjectDialogBoxUiBinderMob.class);
    add(uiBinder.createAndBindUi(this));
    super.closeDialogBox = this.closeDialogBox;
    super.goToPurr = this.goToPurr;
    super.goToChat = this.goToChat;
    super.goToYR = this.goToYR;
    super.noDialogNewProject = this.noDialogNewProject;
    super.topInvisible = this.topInvisible;
    super.bottomInvisible = this.bottomInvisible;
  }

}
