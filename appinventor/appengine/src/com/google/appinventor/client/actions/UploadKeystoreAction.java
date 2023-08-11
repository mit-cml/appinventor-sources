// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.actions;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.tracking.Tracking;
import com.google.appinventor.client.wizards.KeystoreUploadWizard;
import com.google.appinventor.shared.storage.StorageUtil;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;

public class UploadKeystoreAction implements Command {
  @Override
  public void execute() {
    Ode.getInstance().getUserInfoService().hasUserFile(StorageUtil.ANDROID_KEYSTORE_FILENAME,
        new OdeAsyncCallback<Boolean>(MESSAGES.uploadKeystoreError()) {
          @Override
          public void onSuccess(Boolean keystoreFileExists) {
            if (!keystoreFileExists || Window.confirm(MESSAGES.confirmOverwriteKeystore())) {
              KeystoreUploadWizard wizard = new KeystoreUploadWizard(new Command() {
                @Override
                public void execute() {
                  Tracking.trackEvent(Tracking.USER_EVENT, Tracking.USER_ACTION_UPLOAD_KEYSTORE);
                  Ode.getInstance().getTopToolbar().updateKeystoreFileMenuButtons();
                }
              });
              wizard.center();
            }
          }
        });
  }
}
