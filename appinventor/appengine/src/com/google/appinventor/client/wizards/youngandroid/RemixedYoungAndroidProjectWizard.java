// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.wizards.youngandroid;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.GalleryClient;
import com.google.appinventor.client.widgets.LabeledTextBox;
import com.google.appinventor.client.wizards.NewProjectWizard;
import com.google.appinventor.shared.rpc.project.GalleryApp;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;


/**
 * Wizard for creating new Young Android projects.
 *
 * @author blu2@dons.usfca.edu (Bin Lu)
 */
public final class RemixedYoungAndroidProjectWizard extends NewProjectWizard { //implements GalleryRequestListener
  GalleryClient gallery = null;
  Button actionButton;
  // UI element for project name
  private LabeledTextBox projectNameTextBox;

  /**
   * Creates a new YoungAndroid project wizard.
   */
  public RemixedYoungAndroidProjectWizard(final GalleryApp app, final Button actionButton) {
    super(MESSAGES.remixedYoungAndroidProjectWizardCaption());

    this.actionButton = actionButton;
    gallery = GalleryClient.getInstance();
    // Initialize the UI
    setStylePrimaryName("ode-DialogBox");

    projectNameTextBox = new LabeledTextBox(MESSAGES.projectNameLabel());
    projectNameTextBox.setText(replaceNonTextChar(app.getTitle()));
    projectNameTextBox.getTextBox().addKeyDownHandler(new KeyDownHandler() {
      @Override
      public void onKeyDown(KeyDownEvent event) {
        int keyCode = event.getNativeKeyCode();
        if (keyCode == KeyCodes.KEY_ENTER) {
          handleOkClick();
        } else if (keyCode == KeyCodes.KEY_ESCAPE) {
          handleCancelClick();
        }
      }
    });

    VerticalPanel page = new VerticalPanel();
    page.add(projectNameTextBox);
    addPage(page);
    // Create finish command (create a new Young Android project)
    initFinishCommand(new Command() {
      @Override
      public void execute() {
        String projectName = projectNameTextBox.getText();
        final PopupPanel popup = new PopupPanel(true);
        final FlowPanel content = new FlowPanel();
        popup.setWidget(content);
        Label loading = new Label();
        loading.setText(MESSAGES.loadingAppIndicatorText());
        // loading indicator will be hided or forced to be hided in gallery.loadSourceFile
        content.add(loading);
        popup.center();
        boolean success = gallery.loadSourceFile(app, projectNameTextBox.getText(), popup);
        if(success){
          gallery.appWasDownloaded(app.getGalleryAppId(), app.getDeveloperId());
        }
        else {
          show();
          center();
          return;
        }
      }
    });
  }

  @Override
  public void show() {
    super.show();
    // Wizard size (having it resize between page changes is quite annoying)
    int width = 320;
    int height = 40;
    this.center();

    setPixelSize(width, height);
    super.setPagePanelHeight(40);

    DeferredCommand.addCommand(new Command() {
      public void execute() {
        projectNameTextBox.setFocus(true);
        projectNameTextBox.selectAll();
      }
    });
  }
  /**
   * Invoked immediately before closing the wizard.
   */
  @Override
  protected void onHide() {
    actionButton.setEnabled(true);
  }

  private String replaceNonTextChar(String s){
    return s.replaceAll("[^A-Za-z0-9]", "");
  }
}
