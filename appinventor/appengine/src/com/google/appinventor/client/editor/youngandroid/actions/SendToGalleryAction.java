// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.actions;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.editor.youngandroid.DesignToolbar;
import com.google.appinventor.shared.rpc.RpcResult;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

public class SendToGalleryAction implements Command {
  private static final Logger LOG = Logger.getLogger(SendToGalleryAction.class.getName());
  private final Callable<Boolean> before;
  private final Runnable after;

  public SendToGalleryAction(Callable<Boolean> before, Runnable postCommand) {
    this.before = before;
    this.after = postCommand;
  }

  @Override
  public void execute() {
    final DesignToolbar toolbar = Ode.getInstance().getDesignToolbar();
    if (toolbar.getCurrentProject() == null) {
      LOG.warning("DesignToolbar.currentProject is null. "
          + "Ignoring SendToGalleryAction.execute().");
      return;
    }
    boolean shouldRun;
    try {
      shouldRun = before.call();
    } catch (Exception e) {
      return;
    }
    if (shouldRun) {
      Ode.getInstance().getProjectService().sendToGallery(toolbar.getCurrentProject().getProjectId(),
          new OdeAsyncCallback<RpcResult>(
              MESSAGES.GallerySendingError()) {
            @Override
            public void onSuccess(RpcResult result) {
              after.run();
              if (result.getResult() == RpcResult.SUCCESS) {
                Window.open(result.getOutput(), "_blank", "");
              } else {
                ErrorReporter.reportError(result.getError());
              }
            }

            @Override
            public void onFailure(Throwable t) {
              after.run();
              super.onFailure(t);
            }
          });
    }
  }
}
