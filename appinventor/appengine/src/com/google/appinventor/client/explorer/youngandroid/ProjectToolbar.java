// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.youngandroid;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.boxes.ProjectListBox;
import com.google.appinventor.client.widgets.Toolbar;
import com.google.appinventor.client.widgets.ToolbarItem;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;


/**
 * The project toolbar houses command buttons in the Young Android Project tab.
 *
 */
public class ProjectToolbar extends Toolbar {
  interface ProjectToolbarUiBinder extends UiBinder<Toolbar, ProjectToolbar> {}

  private static final ProjectToolbar.ProjectToolbarUiBinder UI_BINDER =
      GWT.create(ProjectToolbar.ProjectToolbarUiBinder.class);

  private static final String WIDGET_NAME_NEW = "New";
  private static final String WIDGET_NAME_MOVE = "Move";
  private static final String WIDGET_NAME_DELETE = "Delete";
  private static final String WIDGET_NAME_TRASH = "Trash";
  private static final String WIDGET_NAME_PROJECT = "Projects";
  private static final String WIDGET_NAME_RESTORE = "Restore";
  private static final String WIDGET_NAME_DELETE_FROM_TRASH = "Delete From Trash";
  private static final String WIDGET_NAME_SENDTONG = "Send to Gallery";
  private static final String WIDGET_NAME_LOGINTOGALLERY = "Login to Gallery";

  private final boolean isReadOnly;

  @UiField ToolbarItem newProjectItem;
  @UiField ToolbarItem newFolderItem;

  /**
   * Initializes and assembles all commands into buttons in the toolbar.
   */
  public ProjectToolbar() {
    super();
    isReadOnly = Ode.getInstance().isReadOnly();
    // Is the new gallery enabled
    boolean galleryEnabled = Ode.getSystemConfig().getGalleryEnabled();
    populateToolbar(UI_BINDER.createAndBindUi(this));

    if (galleryEnabled) {
      setButtonVisible(WIDGET_NAME_LOGINTOGALLERY, true);
      if (!Ode.getInstance().getGalleryReadOnly()) {
        setButtonVisible(WIDGET_NAME_SENDTONG, true);
      }
    }

    setTrashTabButtonsVisible(false);
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

  /**
   * Enables and/or disables buttons based on how many projects exist
   * (in the case of "Download All Projects") or are selected (in the case
   * of "Delete" and "Download Source").
   */
  public void updateButtons() {
    ProjectList projectList = ProjectListBox.getProjectListBox().getProjectList();
    int numAllItems = projectList.getMyProjectsCount();  // Get number of valid projects not in trash
    int numSelectedProjects = projectList.getSelectedProjectsCount();
    if (isReadOnly) {           // If we are read-only, we disable all buttons
      setButtonEnabled(WIDGET_NAME_NEW, false);
      setButtonEnabled(WIDGET_NAME_DELETE, false);
      setButtonEnabled(WIDGET_NAME_RESTORE, false);
      Ode.getInstance().getTopToolbar().updateMenuState(numSelectedProjects, numAllItems);
      return;
    }
    setButtonEnabled(WIDGET_NAME_MOVE, numSelectedProjects > 0);
    setButtonEnabled(WIDGET_NAME_DELETE, numSelectedProjects > 0);
    setButtonEnabled(WIDGET_NAME_DELETE_FROM_TRASH, numSelectedProjects > 0);
    setButtonEnabled(WIDGET_NAME_RESTORE, numSelectedProjects > 0);
    Ode.getInstance().getTopToolbar().updateMenuState(numSelectedProjects, numAllItems);
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
