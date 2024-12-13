// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.actions;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.shared.rpc.RpcResult;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;

import static com.google.appinventor.client.Ode.MESSAGES;

public class LoginToGalleryAction implements Command {
  @Override
  public void execute() {
    Ode.getInstance().getProjectService().loginToGallery(
        new OdeAsyncCallback<RpcResult>(
            MESSAGES.GalleryLoginError()) {
          @Override
          public void onSuccess(RpcResult result) {
            if (result.getResult() == RpcResult.SUCCESS) {
              Window.open(result.getOutput(), "_blank", "");
            } else {
              ErrorReporter.reportError(result.getError());
            }
          }
        });
  }
}

