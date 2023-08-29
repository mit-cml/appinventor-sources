// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.actions;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.boxes.ProjectListBox;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.shared.rpc.RpcResult;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;

import java.util.List;

import static com.google.appinventor.client.Ode.MESSAGES;

// Send to the New Gallery
public class SendToGalleryAction implements Command {
  private static volatile boolean lockPublishButton = false; // To prevent double clicking

  @Override
  public void execute() {
    if (Ode.getInstance().getCurrentView() == Ode.PROJECTS) {
      List<Project> selectedProjects =
          ProjectListBox.getProjectListBox().getProjectList().getSelectedProjects();
      if (selectedProjects.size() != 1) {
        ErrorReporter.reportInfo(MESSAGES.selectOnlyOneProject());
      } else {
        if (!lockPublishButton) {
          lockPublishButton = true;
          Project project = selectedProjects.get(0);
          Ode.getInstance().getProjectService().sendToGallery(project.getProjectId(),
              new OdeAsyncCallback<RpcResult>(
                  MESSAGES.GallerySendingError()) {
                @Override
                public void onSuccess(RpcResult result) {
                  lockPublishButton = false;
                  if (result.getResult() == RpcResult.SUCCESS) {
                    Window.open(result.getOutput(), "_blank", "");
                  } else {
                    ErrorReporter.reportError(result.getError());
                  }
                }

                @Override
                public void onFailure(Throwable t) {
                  lockPublishButton = false;
                  super.onFailure(t);
                }
              });
        }
      }
    }
  }
}
