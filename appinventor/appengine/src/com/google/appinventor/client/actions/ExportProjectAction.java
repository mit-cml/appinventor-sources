// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.actions;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.boxes.ProjectListBox;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.tracking.Tracking;
import com.google.appinventor.client.utils.Downloader;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.gwt.user.client.Command;
import java.util.List;
import java.util.logging.Logger;

public class ExportProjectAction implements Command {
  private static final Logger LOG = Logger.getLogger(ExportProjectAction.class.getName());
  @Override
  public void execute() {
    if (Ode.getInstance().getCurrentView() == Ode.PROJECTS) {
      //If we are in the projects view
      List<Project> selectedProjects =
          ProjectListBox.getProjectListBox().getProjectList().getSelectedProjects();
      if (selectedProjects.size() == 1) {
        exportProject(selectedProjects.get(0));
      } else if (selectedProjects.size() > 1) {
        exportSelectedProjects(selectedProjects);
      } else {
        // The user needs to select only one project.
        ErrorReporter.reportInfo(MESSAGES.wrongNumberProjectsSelected());
      }
    } else {
      //If we are in the designer view.
      Downloader.getInstance().download(ServerLayout.DOWNLOAD_SERVLET_BASE
          + ServerLayout.DOWNLOAD_PROJECT_SOURCE + "/"
          + Ode.getInstance().getCurrentYoungAndroidProjectId());
    }
  }

  private void exportProject(Project project) {
    Tracking.trackEvent(Tracking.PROJECT_EVENT,
        Tracking.PROJECT_ACTION_DOWNLOAD_PROJECT_SOURCE_YA, project.getProjectName());

    Downloader.getInstance().download(ServerLayout.DOWNLOAD_SERVLET_BASE +
        ServerLayout.DOWNLOAD_PROJECT_SOURCE + "/" + project.getProjectId());
  }

  private void exportSelectedProjects(List<Project> projects) {
    Tracking.trackEvent(Tracking.PROJECT_EVENT,
        Tracking.PROJECT_ACTION_DOWNLOAD_SELECTED_PROJECTS_SOURCE_YA);

    String selectedProjPath = ServerLayout.DOWNLOAD_SERVLET_BASE +
        ServerLayout.DOWNLOAD_SELECTED_PROJECTS_SOURCE + "/";

    for (Project project : projects) {
      selectedProjPath += project.getProjectId() + "-";
    }

    Downloader.getInstance().download(selectedProjPath);
  }
}
