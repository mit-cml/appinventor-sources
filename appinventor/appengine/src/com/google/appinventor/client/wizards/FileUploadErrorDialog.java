// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.wizards;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.wizards.FileUploadWizard.FileUploadedCallback;
import com.google.appinventor.shared.rpc.project.FolderNode;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import java.util.Collection;

public class FileUploadErrorDialog {
  interface FileUploadErrorUiBinder extends UiBinder<Dialog, FileUploadErrorDialog> {}

  private static final FileUploadErrorUiBinder uibinder =
      GWT.create(FileUploadErrorDialog.FileUploadErrorUiBinder.class);

  @UiField Dialog uploadError;
  @UiField Button okButton;
  @UiField Button infoButton;
  @UiField HTML errorMessage;
  private final FolderNode folderNode;
  private final Collection<String> acceptableTypes;
  private final FileUploadedCallback fileUploadedCallback;

  FileUploadErrorDialog(String title, String body, FileUploadErrorCode e,
      final FolderNode folderNode, final Collection<String> acceptableTypes,
      final FileUploadedCallback fileUploadedCallback) {
    this.folderNode = folderNode;
    this.acceptableTypes = acceptableTypes;
    this.fileUploadedCallback = fileUploadedCallback;

    uibinder.createAndBindUi(this);
    uploadError.setText(title);
    errorMessage.setHTML(body);

    switch (e) {
      case AIA_MEDIA_ASSET:
        infoButton.setVisible(true);
        break;
      case NO_FILE_SELECTED:
      case MALFORMED_FILENAME:
      case FILENAME_BAD_SIZE:
      default:
        break;
    }
    uploadError.center();
  }

  @UiHandler("okButton")
  void okDialog(@SuppressWarnings("unused") ClickEvent e) {
    uploadError.hide();
    new FileUploadWizard(folderNode, acceptableTypes, fileUploadedCallback).show();
  }

  @UiHandler("infoButton")
  void infoDialog(@SuppressWarnings("unused") ClickEvent e) {
    Window.open(MESSAGES.aiaMediaAssetHelp(), "AIA Help", "");
  }
}
