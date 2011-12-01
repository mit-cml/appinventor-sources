// Copyright 2009 Google Inc. All Rights Reserved.

package com.google.appinventor.client.explorer.youngandroid;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.boxes.ProjectListBox;
import com.google.appinventor.client.boxes.ViewerBox;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.tracking.Tracking;
import com.google.appinventor.client.utils.Downloader;
import com.google.appinventor.client.widgets.Toolbar;
import com.google.appinventor.client.widgets.Toolbar.ToolbarItem;
import com.google.appinventor.client.wizards.ProjectUploadWizard;
import com.google.appinventor.client.wizards.youngandroid.NewYoungAndroidProjectWizard;
import com.google.appinventor.client.youngandroid.CodeblocksManager;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

/**
 * The project toolbar houses command buttons in the Young Android Project tab.
 *
 */
public class ProjectToolbar extends Toolbar {
  private static final String WIDGET_NAME_NEW = "New";
  private static final String WIDGET_NAME_DELETE = "Delete";
  private static final String WIDGET_NAME_MORE_ACTIONS = "MoreActions";
  private static final String WIDGET_NAME_DOWNLOAD_ALL = "DownloadAll";
  private static final String WIDGET_NAME_DOWNLOAD_SOURCE = "DownloadSource";
  private static final String WIDGET_NAME_UPLOAD_SOURCE = "UploadSource";

  /**
   * Initializes and assembles all commands into buttons in the toolbar.
   */
  public ProjectToolbar() {
    super();

    addButton(new ToolbarItem(WIDGET_NAME_NEW, MESSAGES.newButton(),
        new NewAction()));

    addButton(new ToolbarItem(WIDGET_NAME_DELETE, MESSAGES.deleteButton(),
        new DeleteAction()));

    addButton(new ToolbarItem(WIDGET_NAME_DOWNLOAD_ALL, MESSAGES.downloadAllButton(),
        new DownloadAllAction()));

    List<ToolbarItem> otherItems = Lists.newArrayList();
    otherItems.add(new ToolbarItem(WIDGET_NAME_DOWNLOAD_SOURCE,
        MESSAGES.downloadSourceButton(), new DownloadSourceAction()));
    otherItems.add(new ToolbarItem(WIDGET_NAME_UPLOAD_SOURCE,
        MESSAGES.uploadSourceButton(), new UploadSourceAction()));
    addDropDownButton(WIDGET_NAME_MORE_ACTIONS, MESSAGES.moreActionsButton(), otherItems);
  }

  private static class NewAction implements Command {
    @Override
    public void execute() {
      new NewYoungAndroidProjectWizard().center();
      // The wizard will switch to the design view when the new
      // project is created.
    }
  }

  private static class DownloadAllAction implements Command {
    @Override
    public void execute() {
      Tracking.trackEvent(Tracking.PROJECT_EVENT,
          Tracking.PROJECT_ACTION_DOWNLOAD_ALL_PROJECTS_SOURCE_YA);

      // Is there a way to disable the Download All button until this completes?
      Window.alert(MESSAGES.downloadAllAlert());

      Downloader.getInstance().download(ServerLayout.DOWNLOAD_SERVLET_BASE +
          ServerLayout.DOWNLOAD_ALL_PROJECTS_SOURCE);
    }
  }

  private static class UploadSourceAction implements Command {
    @Override
    public void execute() {
      new ProjectUploadWizard().center();
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
        ErrorReporter.reportError(MESSAGES.noProjectSelectedForDelete());
      }
    }

    private boolean deleteConfirmation(List<Project> projects) {
      String message;
      if (projects.size() == 1) {
        message = MESSAGES.confirmDeleteSingleProject(projects.get(0).getProjectName());
      } else {
        StringBuilder sb = new StringBuilder();
        String separator = "";
        for (Project project : projects) {
          sb.append(separator).append(project.getProjectName());
          separator = ", ";
        }
        String projectNames = sb.toString();
        message = MESSAGES.confirmDeleteManyProjects(projectNames);
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
        // If we're deleting the project that is currently open in the Designer and Codeblocks we
        // need to clear the ViewerBox and tell Codeblocks to clear its workspace first.  However,
        // even if that fails, we still want to complete the delete operation.
        ViewerBox.getViewerBox().clear();
        CodeblocksManager.getCodeblocksManager().clearCodeblocks(new AsyncCallback<Void>() {
          @Override
          public void onFailure(Throwable caught) {
            doDeleteProject(projectId);
          }

          @Override
          public void onSuccess(Void result) {
            doDeleteProject(projectId);
          }
        });

      } else {
        // Make sure that we delete projects even if they are not open.
        doDeleteProject(projectId);
      }
    }

    private void doDeleteProject(final long projectId) {
      Ode.getInstance().getProjectService().deleteProject(projectId,
          new OdeAsyncCallback<Void>(
            // failure message
          MESSAGES.deleteProjectError()) {
        @Override
        public void onSuccess(Void result) {
          Ode.getInstance().getProjectManager().removeProject(projectId);
        }
      });
    }

  }

  private static class DownloadSourceAction implements Command {
    @Override
    public void execute() {
      List<Project> selectedProjects =
          ProjectListBox.getProjectListBox().getProjectList().getSelectedProjects();
      if (selectedProjects.size() == 1) {
        downloadSource(selectedProjects.get(0));
      } else {
        ErrorReporter.reportError(MESSAGES.wrongNumberProjectsSelected());
      }
    }

    private void downloadSource(Project project) {
      Tracking.trackEvent(Tracking.PROJECT_EVENT,
          Tracking.PROJECT_ACTION_DOWNLOAD_PROJECT_SOURCE_YA, project.getProjectName());

      Downloader.getInstance().download(ServerLayout.DOWNLOAD_SERVLET_BASE +
          ServerLayout.DOWNLOAD_PROJECT_SOURCE + "/" + project.getProjectId());
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

    setButtonEnabled(WIDGET_NAME_DOWNLOAD_ALL, numProjects > 0);

    setButtonEnabled(WIDGET_NAME_DELETE, numSelectedProjects > 0);

    setDropItemEnabled(WIDGET_NAME_DOWNLOAD_SOURCE, numSelectedProjects == 1);
  }

}
