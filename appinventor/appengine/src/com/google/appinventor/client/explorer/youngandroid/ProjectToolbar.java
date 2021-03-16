// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.youngandroid;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.TopToolbar;
import com.google.appinventor.client.boxes.ProjectListBox;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.widgets.Toolbar;
import com.google.appinventor.client.wizards.NewFolderWizard;
import com.google.appinventor.client.wizards.youngandroid.NewYoungAndroidProjectWizard;
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
  private static final String WIDGET_NAME_TRASH = "Trash";
  private static final String WIDGET_NAME_PROJECT= "Projects";
  private static final String WIDGET_NAME_RESTORE= "Restore";
  private static final String WIDGET_NAME_DELETE_FROM_TRASH= "Delete From Trash";
  private static final String WIDGET_NAME_CREATE_FOLDER = "New Folder";

  private boolean isReadOnly;

  private boolean galleryEnabled = false; // Is the new gallery enabled

  private static volatile boolean lockPublishButton = false; // To prevent double clicking

  /**
   * Initializes and assembles all commands into buttons in the toolbar.
   */
  public ProjectToolbar() {
    super();
    isReadOnly = Ode.getInstance().isReadOnly();
    galleryEnabled = Ode.getInstance().getSystemConfig().getGalleryEnabled();

    addButton(new ToolbarItem(WIDGET_NAME_NEW, MESSAGES.newProjectMenuItem(),
        new NewAction(this)));

    addButton(new ToolbarItem(WIDGET_NAME_DELETE, MESSAGES.trashProjectButton(),
        new TopToolbar.MoveToTrashAction()));
    addButton(new ToolbarItem(WIDGET_NAME_TRASH,MESSAGES.switchToTrashButton(),
        new SwitchToTrashAction()));
    addButton(new ToolbarItem(WIDGET_NAME_PROJECT,MESSAGES.myProjectsButton(),
        new BackToProjectViewAction()));
    addButton(new ToolbarItem(WIDGET_NAME_RESTORE,MESSAGES.restoreProjectButton(),
        new RestoreProjectAction()));
    addButton(new ToolbarItem(WIDGET_NAME_DELETE_FROM_TRASH,MESSAGES.deleteFromTrashButton(),
        new TopToolbar.DeleteForeverProjectAction()));
    addButton(new ToolbarItem(WIDGET_NAME_CREATE_FOLDER, MESSAGES.createFolderButton(),
        new CreateNewFolderAction()));

    setTrashTabButtonsVisible(false);
    updateButtons();
  }

  public void setTrashTabButtonsVisible(boolean visible) {
    setButtonVisible(WIDGET_NAME_PROJECT, visible);
    setButtonVisible(WIDGET_NAME_RESTORE, visible);
    setButtonVisible(WIDGET_NAME_DELETE_FROM_TRASH, visible);
    updateButtons();
  }

  public void setProjectTabButtonsVisible(boolean visible) {
    setButtonVisible(WIDGET_NAME_NEW, visible);
    setButtonVisible(WIDGET_NAME_TRASH,visible);
    setButtonVisible(WIDGET_NAME_DELETE,visible);
  }

  private static class NewAction implements Command {
    ProjectToolbar parent;

    public NewAction(ProjectToolbar parent) {
      this.parent = parent;
    }

    @Override
    public void execute() {
      if (Ode.getInstance().screensLocked()) {
        return;                 // Refuse to switch if locked (save file happening)
      }
      // Disabled the Start New Project button. We do this because on slow machines people
      // click it multiple times while the wizard (below) is starting. This then causes
      // a second wizard to start and a very confused user experience.
      // We will enable the button again when we re-visit the Project List page
      parent.setButtonEnabled(WIDGET_NAME_NEW, false);
      new NewYoungAndroidProjectWizard(parent).center();
      // The wizard will switch to the design view when the new
      // project is created.
    }
  }

  private static class CreateNewFolderAction implements Command {
    // Need to: spawn widget, in that widget, validate name-- then call projectmanager to create new folder, and
    // afterwards update the folder names by converting the list of folders to a serialized string to store
    // via rpc call-- use ode.getprojectservice
    // Main issue- needs to get current Folder to validate folder name
    // See textvalidators and newyoungandroidprojectwizard

    @Override
    public void execute() {
      if (Ode.getInstance().screensLocked()) {
        return;                 // Refuse to switch if locked (save file happening)
      }
      new NewFolderWizard().center();
    }
  }

  //implementing trash method this method will show the Trash Tab
  private static class SwitchToTrashAction implements Command {
    @Override
    public void execute() {
      Ode.getInstance().getEditorManager().saveDirtyEditors(new Command() {
        @Override
        public void execute() {
          Ode.getInstance().switchToTrash();
        }
      });
    }
  }

  //Moving Back From Trash Tab To Projects Tab
  private static class BackToProjectViewAction implements Command {
    @Override
    public void execute() {
      Ode.getInstance().getEditorManager().saveDirtyEditors(new Command() {
        @Override
        public void execute() {
          Ode.getInstance().getTopToolbar().updateMoveToTrash("Move To Trash");
          Ode.getInstance().switchToProjectsView();
        }
      });
    }
  }

  //Restoring the project back to My Projects from Trash Can
  private static class RestoreProjectAction implements Command {
    @Override
    public void execute() {
      List<Project> selectedProjects = ProjectListBox.getProjectListBox().getProjectList().getSelectedProjects();
      if (selectedProjects.size() > 0) {
        for (Project project : selectedProjects) {
          project.restoreFromTrash();
        }
        Ode.getInstance().switchToTrash();
      } else {
        // The user can select a project to resolve the
        // error.
        ErrorReporter.reportInfo(MESSAGES.noProjectSelectedForRestore());
      }
    }
  }

  /**
   * Enables and/or disables buttons based on how many projects exist
   * (in the case of "Download All Projects") or are selected (in the case
   * of "Delete" and "Download Source").
   */
  public void updateButtons() {
    ProjectList projectList = ProjectListBox.getProjectListBox().getProjectList();
    int numProjects = projectList.getMyProjectsCount();  // Get number of valid projects not in trash
    int numSelectedProjects = projectList.getSelectedProjectsCount();
    int numSelectedFolders = projectList.getNumSelectedFolders();
    if (isReadOnly) {           // If we are read-only, we disable all buttons
      setButtonEnabled(WIDGET_NAME_NEW, false);
      setButtonEnabled(WIDGET_NAME_DELETE, false);
      setButtonEnabled(WIDGET_NAME_RESTORE, false);
      Ode.getInstance().getTopToolbar().updateMenuState(numSelectedProjects, numProjects);
      return;
    }
    setButtonEnabled(WIDGET_NAME_DELETE, numSelectedProjects > 0 || numSelectedFolders > 0);
    Ode.getInstance().getTopToolbar().updateMenuState(numSelectedProjects, numProjects);

    setButtonEnabled(WIDGET_NAME_DELETE_FROM_TRASH, numSelectedProjects > 0);
    setButtonEnabled(WIDGET_NAME_RESTORE, numSelectedProjects > 0);
    Ode.getInstance().getTopToolbar().updateMenuState(numSelectedProjects, numProjects);
  }

  // If we started a project, then the start button was disabled (to avoid
  // a second press while the new project wizard was starting (aka we "debounce"
  // the button). When the person switches to the projects list view again (here)
  // we re-enable it.
  public void enableStartButton() {
    if (!isReadOnly) {
      setButtonEnabled(WIDGET_NAME_NEW, true);
    }
  }

}
