// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023-2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.wizards;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.explorer.commands.ChainableCommand;
import com.google.appinventor.client.tracking.Tracking;
import com.google.appinventor.client.widgets.LabeledTextBox;
import com.google.appinventor.client.widgets.Validator;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;

public class AppStoreSettingsDialog {
  interface AppStoreSettingsDialogUiBinder extends UiBinder<Dialog, AppStoreSettingsDialog> {}

  private static final AppStoreSettingsDialogUiBinder UI_BINDER =
      GWT.create(AppStoreSettingsDialogUiBinder.class);

  private Command next;

  @UiField Dialog settingsDialog;
  @UiField LabeledTextBox appleIdTextbox;
  @UiField(provided = true) LabeledTextBox passwordTextbox =
      new LabeledTextBox("App Specific Password", true);
  @UiField LabeledTextBox shortNameTextbox;
  @UiField Button okButton;
  @UiField Button cancelButton;

  /**
   * Create a new dialog for configuring App Store connections.
   */
  public AppStoreSettingsDialog() {
    UI_BINDER.createAndBindUi(this);
    appleIdTextbox.setValidator(new Validator() {
      @Override
      public boolean validate(String value) {
        return value.contains("@");
      }

      @Override
      public String getErrorMessage() {
        return "Please enter a valid email address corresponding to your Apple ID.";
      }
    });
  }

  /**
   * Create a new dialog for configuring App Store connections. The {@code next}
   * command will be run if the user clicks the OK button.
   *
   * @param next the command to run on completing the dialog
   */
  public AppStoreSettingsDialog(Command next) {
    this();
    this.next = next;
  }

  public void show() {
    settingsDialog.center();
  }

  @UiHandler("cancelButton")
  public void cancel(@SuppressWarnings("unused") ClickEvent e) {
    settingsDialog.hide();
  }

  /**
   * Submit the settings form.
   *
   * @param e the click event
   */
  @UiHandler("okButton")
  public void submit(@SuppressWarnings("unused") ClickEvent e) {
    if (!appleIdTextbox.validate()) {
      Window.alert("You must specify your Apple ID to continue.");
      return;
    }
    if (passwordTextbox.getText().isEmpty()) {
      Window.alert("You must provide your app-specific password for MIT App Inventor.");
      return;
    }
    String content = appleIdTextbox.getText() + "\n" + passwordTextbox.getText() + "\n";
    if (!shortNameTextbox.getText().isEmpty()) {
      content += shortNameTextbox.getText() + "\n";
    }
    okButton.setEnabled(false);
    Ode.getInstance().getUserInfoService()
        .storeAppStoreSettings(content, new OdeAsyncCallback<Void>() {
          @Override
          public void onSuccess(Void result) {
            okButton.setEnabled(true);
            settingsDialog.hide();
            if (next != null) {
              next.execute();
            }
          }

          @Override
          public void onFailure(Throwable caught) {
            okButton.setEnabled(true);
            super.onFailure(caught);
          }
        });
  }
}
