// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.actions;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.shared.rpc.RpcResult;
import com.google.gwt.user.client.Command;

import static com.google.appinventor.client.Ode.MESSAGES;

// Submit the current project as an assignment to the classroom portal.
public class SubmitAssignmentAction implements Command {
  private static volatile boolean lockSubmitButton = false; // To prevent double clicking

  @Override
  public void execute() {
    if (lockSubmitButton) {
      return; // De-bounce the submit button
    }
    lockSubmitButton = true;
    final long projectId = Ode.getInstance().getCurrentYoungAndroidProjectId();
    // Force a save pass and wait for every dirty file/setting to be
    // acknowledged before submitting, so we never ship stale content -
    // the autosave timer can otherwise leave up to ~30s of unsaved edits.
    Ode.getInstance().lockScreens(true);
    Ode.getInstance().getEditorManager().saveDirtyEditors(new Command() {
      @Override
      public void execute() {
        Ode.getInstance().getProjectService().submitAssignment(projectId,
          new OdeAsyncCallback<RpcResult>(MESSAGES.submitAssignmentError()) {
            @Override
            public void onSuccess(RpcResult result) {
              lockSubmitButton = false;
              Ode.getInstance().lockScreens(false);
              if (result.getResult() == RpcResult.SUCCESS) {
                ErrorReporter.reportInfo(MESSAGES.submitAssignmentSuccess());
              } else {
                ErrorReporter.reportError(result.getError());
              }
            }

            @Override
            public void onFailure(Throwable caught) {
              lockSubmitButton = false;
              Ode.getInstance().lockScreens(false);
              super.onFailure(caught);
            }
          });
      }
    });
  }

}
