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
import com.google.appinventor.client.utils.Downloader;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.appinventor.shared.storage.StorageUtil;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;

public class DownloadKeystoreAction implements Command {
  @Override
  public void execute() {
    Ode.getInstance().getUserInfoService().hasUserFile(StorageUtil.ANDROID_KEYSTORE_FILENAME,
        new OdeAsyncCallback<Boolean>(MESSAGES.downloadKeystoreError()) {
          @Override
          public void onSuccess(Boolean keystoreFileExists) {
            if (keystoreFileExists) {
              Tracking.trackEvent(Tracking.USER_EVENT, Tracking.USER_ACTION_DOWNLOAD_KEYSTORE);
              Downloader.getInstance().download(ServerLayout.DOWNLOAD_SERVLET_BASE +
                  ServerLayout.DOWNLOAD_USERFILE + "/" + StorageUtil.ANDROID_KEYSTORE_FILENAME);
            } else {
              Window.alert(MESSAGES.noKeystoreToDownload());
            }
          }
        });
  }
}
