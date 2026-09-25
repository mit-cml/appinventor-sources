// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.actions;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.boxes.ProjectListBox;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.shared.rpc.RpcResult;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.client.Command;

import java.util.List;

/**
 * Exports the current project to the signed-in user's Google Drive for the
 * Google Classroom integration, then shows a banner with a link to the new file
 * and a reminder to attach it to the Classroom assignment. This is Route C, where
 * the student attaches the file by hand; the server endpoint confirms the user
 * owns the project before exporting.
 */
public class SubmitToGoogleClassroomAction implements Command {

  // Guards against a double-click starting a second upload (which would create a
  // duplicate Drive file) while the first request is still in flight.
  private static volatile boolean submitting = false;

  @Override
  public void execute() {
    long projectId;
    if (Ode.getInstance().getCurrentView() == Ode.PROJECTS) {
      List<Project> selectedProjects =
          ProjectListBox.getProjectListBox().getProjectList().getSelectedProjects();
      if (selectedProjects.size() != 1) {
        ErrorReporter.reportInfo(MESSAGES.selectOnlyOneProject());
        return;
      }
      projectId = selectedProjects.get(0).getProjectId();
    } else {
      projectId = Ode.getCurrentProject().getProjectId();
    }
    exportToDrive(projectId);
  }

  private void exportToDrive(long projectId) {
    if (submitting) {
      return;
    }
    submitting = true;
    final String fileName = driveFileName(projectId);
    Ode.getInstance().getProjectService().exportProjectToDrive(projectId,
        new OdeAsyncCallback<RpcResult>(MESSAGES.classroomSubmitError()) {
          @Override
          public void onSuccess(RpcResult result) {
            submitting = false;
            if (result.succeeded()) {
              // The banner renders HTML, so it carries a clickable link to the new
              // file (replacing an auto-opened tab, which browsers commonly block
              // from an asynchronous callback). The URL is sanitized with UriUtils,
              // the file name is HTML-escaped, and the link is opened without access
              // to this window.
              String href = UriUtils.fromString(result.getOutput()).asString();
              String openLink = "<a href=\"" + href
                  + "\" target=\"_blank\" rel=\"noopener noreferrer\">"
                  + MESSAGES.classroomOpenInDrive() + "</a>";
              ErrorReporter.reportInfo(MESSAGES.classroomSubmitSuccess(
                  SafeHtmlUtils.htmlEscape(fileName), openLink));
            } else if ("NOT_CONNECTED".equals(result.getError())) {
              ErrorReporter.reportError(MESSAGES.classroomNotConnected());
            } else {
              ErrorReporter.reportError(MESSAGES.classroomSubmitError());
            }
          }

          @Override
          public void onFailure(Throwable t) {
            submitting = false;
            super.onFailure(t);
          }
        });
  }

  /**
   * Returns the name the exported file will have in Drive. The server forms it as
   * the project name plus {@code .aia}. Falls back to a generic label if the
   * project is not loaded, so the banner never shows a broken name.
   */
  private static String driveFileName(long projectId) {
    Project project = Ode.getInstance().getProjectManager().getProject(projectId);
    if (project == null || project.getProjectName() == null
        || project.getProjectName().isEmpty()) {
      return "your project";
    }
    return project.getProjectName() + ".aia";
  }
}
