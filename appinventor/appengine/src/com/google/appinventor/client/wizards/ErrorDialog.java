// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.wizards;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;

public class ErrorDialog {
  interface ErrorDialogUiBinder extends UiBinder<Dialog, ErrorDialog> {}

  private static final ErrorDialogUiBinder UI_BINDER =
      GWT.create(ErrorDialogUiBinder.class);

  @UiField
  Dialog dialog;
  @UiField
  Button okButton;
  @UiField
  Button infoButton;
  @UiField
  HTML errorMessage;

  /**
   * Create a new dialog for showing an error message.
   *
   * @param title the dialog title
   * @param body the error message to display
   */
  public ErrorDialog(String title, String body) {
    UI_BINDER.createAndBindUi(this);
    dialog.setText(title);
    errorMessage.setHTML(body);
  }

  public void show() {
    dialog.center();
  }

  @UiHandler("okButton")
  void okDialog(@SuppressWarnings("unused") ClickEvent e) {
    dialog.hide();
  }

  @UiHandler("infoButton")
  void infoDialog(@SuppressWarnings("unused") ClickEvent e) {
  }
}
