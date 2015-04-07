// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.youngandroid;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.GalleryClient;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.boxes.ProjectListBox;
import com.google.appinventor.client.boxes.ViewerBox;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.tracking.Tracking;
import com.google.appinventor.client.widgets.Toolbar;
import com.google.appinventor.client.wizards.youngandroid.NewYoungAndroidProjectWizard;
import com.google.appinventor.shared.rpc.project.GallerySettings;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;

import java.util.List;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * The project toolbar houses command buttons in the Young Android Project tab.
 *
 */
public class ProjectToolbar extends Toolbar {
  private static final String WIDGET_NAME_NEW = "New";
  private static final String WIDGET_NAME_DELETE = "Delete";
  /**
   * Initializes and assembles all commands into buttons in the toolbar.
   */
  public ProjectToolbar() {
    super();

    addButton(new ToolbarItem(WIDGET_NAME_NEW, MESSAGES.newProjectMenuItem(),
        new NewAction()));

    addButton(new ToolbarItem(WIDGET_NAME_DELETE, MESSAGES.deleteProjectButton(),
        new DeleteAction()));

    updateButtons();
  }

  private static class NewAction implements Command {
    @Override
    public void execute() {
      if (Ode.getInstance().screensLocked()) {
        return;                 // Refuse to switch if locked (save file happening)
      }
      new NewYoungAndroidProjectWizard().center();
      // The wizard will switch to the design view when the new
      // project is created.
    }
  }

  private static class DeleteAction implements Command {
    @Override
    public void execute() {
      List<Project> selectedProjects =
          ProjectListBox.getProjectListBox().getProjectList().getSelectedProjects();
      if (selectedProjects.size() > 0) {
        // Show one confirmation window for selected projects.
        if (deleteConfirmation(selectedProjects)) {
          for (Project project : selectedProjects) {
            deleteProject(project);
          }
        }
      } else {
        // The user can select a project to resolve the
        // error.
        ErrorReporter.reportInfo(MESSAGES.noProjectSelectedForDelete());
      }
    }

    private boolean deleteConfirmation(List<Project> projects) {
      String message;
      GallerySettings gallerySettings = GalleryClient.getInstance().getGallerySettings();
      if (projects.size() == 1) {
        if (projects.get(0).isPublished()) {
          message = MESSAGES.confirmDeleteSinglePublishedProject(projects.get(0).getProjectName());
        } else {
          message = MESSAGES.confirmDeleteSingleProject(projects.get(0).getProjectName());
        }
      } else {
        StringBuilder sb = new StringBuilder();
        String separator = "";
        for (Project project : projects) {
          sb.append(separator).append(project.getProjectName());
          separator = ", ";
        }
        String projectNames = sb.toString();
        if(!gallerySettings.galleryEnabled()){
          message = MESSAGES.confirmDeleteManyProjects(projectNames);
        } else {
          message = MESSAGES.confirmDeleteManyProjectsWithGalleryOn(projectNames);
        }
      }
      return Window.confirm(message);
    }

    private void deleteProject(Project project) {
      Tracking.trackEvent(Tracking.PROJECT_EVENT,
          Tracking.PROJECT_ACTION_DELETE_PROJECT_YA, project.getProjectName());

      final long projectId = project.getProjectId();

      Ode ode = Ode.getInstance();
      boolean isCurrentProject = (projectId == ode.getCurrentYoungAndroidProjectId());
      ode.getEditorManager().closeProjectEditor(projectId);
      if (isCurrentProject) {
        // If we're deleting the project that is currently open in the Designer we
        // need to clear the ViewerBox first.
        ViewerBox.getViewerBox().clear();
      }
      if (project.isPublished()) {
        doDeleteGalleryApp(project.getGalleryId());
      }
      // Make sure that we delete projects even if they are not open.
      doDeleteProject(projectId);
    }

    private void doDeleteProject(final long projectId) {
      Ode.getInstance().getProjectService().deleteProject(projectId,
          new OdeAsyncCallback<Void>(
              // failure message
              MESSAGES.deleteProjectError()) {
            @Override
            public void onSuccess(Void result) {
              Ode.getInstance().getProjectManager().removeProject(projectId);
              // Show a welcome dialog in case there are no
              // projects saved.
              if (Ode.getInstance().getProjectManager().getProjects().size() == 0) {
                Ode.getInstance().createNoProjectsDialog(true);
              }
            }
          });
    }
    private void doDeleteGalleryApp(final long galleryId) {
      Ode.getInstance().getGalleryService().deleteApp(galleryId,
          new OdeAsyncCallback<Void>(
              // failure message
              MESSAGES.galleryDeleteError()) {
            @Override
            public void onSuccess(Void result) {
              // need to update gallery list
              GalleryClient gallery = GalleryClient.getInstance();
              gallery.appWasChanged();
            }
          });
    }
  }

  /**
   * Enables and/or disables buttons based on how many projects exist
   * (in the case of "Download All Projects") or are selected (in the case
   * of "Delete" and "Download Source").
   */
  public void updateButtons() {
    ProjectList projectList = ProjectListBox.getProjectListBox().getProjectList();
    int numProjects = projectList.getNumProjects();
    int numSelectedProjects = projectList.getNumSelectedProjects();
    setButtonEnabled(WIDGET_NAME_DELETE, numSelectedProjects > 0);
    Ode.getInstance().getTopToolbar().fileDropDown.setItemEnabled(MESSAGES.deleteProjectMenuItem(),
        numSelectedProjects > 0);
    Ode.getInstance().getTopToolbar().fileDropDown.setItemEnabled(MESSAGES.exportProjectMenuItem(),
        numSelectedProjects > 0);
    Ode.getInstance().getTopToolbar().fileDropDown.setItemEnabled(MESSAGES.exportAllProjectsMenuItem(),
        numSelectedProjects > 0);
  }
}
