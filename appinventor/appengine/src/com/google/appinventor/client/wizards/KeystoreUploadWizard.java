// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.wizards;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.utils.Uploader;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.appinventor.shared.rpc.UploadResponse;
import com.google.appinventor.shared.storage.StorageUtil;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Wizard for uploading an android.keystore file.
 *
 */
public class KeystoreUploadWizard extends Wizard {
  // Keystore extension
  private static final String KEYSTORE_EXTENSION = ".keystore";

  /**
   * Creates a new keystore upload wizard.
   */
  public KeystoreUploadWizard(final Command callbackAfterUpload) {
    super(MESSAGES.keystoreUploadWizardCaption(), true, false);

    // Initialize UI
    final FileUpload upload = new FileUpload();
    upload.setName(ServerLayout.UPLOAD_USERFILE_FORM_ELEMENT);
    setStylePrimaryName("ode-DialogBox");
    VerticalPanel panel = new VerticalPanel();
    panel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
    panel.add(upload);
    addPage(panel);

    // Create finish command (upload a keystore)
    initFinishCommand(new Command() {
      @Override
      public void execute() {
        String filename = upload.getFilename();
        if (filename.endsWith(KEYSTORE_EXTENSION)) {
          String uploadUrl = GWT.getModuleBaseURL() + ServerLayout.UPLOAD_SERVLET + "/" +
              ServerLayout.UPLOAD_USERFILE + "/" + StorageUtil.ANDROID_KEYSTORE_FILENAME;
          Uploader.getInstance().upload(upload, uploadUrl,
              new OdeAsyncCallback<UploadResponse>(
                  // failure message
                  MESSAGES.keystoreUploadError()) {
                @Override
                public void onSuccess(UploadResponse uploadResponse) {
                  switch (uploadResponse.getStatus()) {
                    case SUCCESS:
                      if (callbackAfterUpload != null) {
                        callbackAfterUpload.execute();
                      }
                      break;
                    default:
                      ErrorReporter.reportError(MESSAGES.keystoreUploadError());
                      break;
                  }
                }
              });
        } else {
          Window.alert(MESSAGES.notKeystoreError());
          center();
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
  }
}
