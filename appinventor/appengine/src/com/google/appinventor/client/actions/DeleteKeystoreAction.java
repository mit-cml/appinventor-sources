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
import com.google.appinventor.shared.storage.StorageUtil;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;

public class DeleteKeystoreAction implements Command {
  @Override
  public void execute() {
    final String errorMessage = MESSAGES.deleteKeystoreError();
    Ode.getInstance().getUserInfoService().hasUserFile(StorageUtil.ANDROID_KEYSTORE_FILENAME,
        new OdeAsyncCallback<Boolean>(errorMessage) {
          @Override
          public void onSuccess(Boolean keystoreFileExists) {
            if (keystoreFileExists && Window.confirm(MESSAGES.confirmDeleteKeystore())) {
              Tracking.trackEvent(Tracking.USER_EVENT, Tracking.USER_ACTION_DELETE_KEYSTORE);
              Ode.getInstance().getUserInfoService().deleteUserFile(
                  StorageUtil.ANDROID_KEYSTORE_FILENAME,
                  new OdeAsyncCallback<Void>(errorMessage) {
                    @Override
                    public void onSuccess(Void result) {
                      // The android.keystore shouldn't exist at this point, so reset cached values.
                      Ode.getInstance().getTopToolbar().updateKeystoreStatus(false);
                    }
                  });
            }
          }
        });
  }
}
