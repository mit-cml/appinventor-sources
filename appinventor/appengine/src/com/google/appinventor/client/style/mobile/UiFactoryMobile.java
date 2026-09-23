// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
package com.google.appinventor.client.style.mobile;

import com.google.appinventor.client.UiStyleFactory;
import com.google.appinventor.client.editor.ProjectEditor;
import com.google.appinventor.client.editor.youngandroid.YaNonVisibleComponentsPanel;
import com.google.appinventor.client.editor.youngandroid.YaProjectEditor;
import com.google.appinventor.client.editor.youngandroid.YaVisibleComponentsPanel;
import com.google.appinventor.client.explorer.dialogs.NoProjectDialogBox;
import com.google.appinventor.client.explorer.dialogs.ProjectPropertiesDialogBox;
import com.google.appinventor.client.explorer.folder.ProjectFolder;
import com.google.appinventor.client.explorer.youngandroid.ProjectList;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.ui.DialogBox;

import java.util.logging.Logger;


public class UiFactoryMobile extends UiStyleFactory {
  private static final Logger LOG = Logger.getLogger(UiFactoryMobile.class.getName());

    @Override
    public ProjectList createProjectList() {
        return new ProjectListMob();
    }

    @Override
    public ProjectFolder createProjectFolder(String name, long dateCreated, long dateModified, ProjectFolder parent) {
        return new ProjectFolderMob(name, dateCreated, dateModified, parent);
    }

    @Override
    public ProjectFolder createProjectFolder(String name, long dateCreated, ProjectFolder parent) {
        return new ProjectFolderMob(name, dateCreated, parent);
    }

    @Override
    public ProjectFolder createProjectFolder(JSONObject json, ProjectFolder parent) {
        return new ProjectFolderMob(json, parent, this);
    }

  @Override
  public YaVisibleComponentsPanel createSimpleVisibleComponentsPanel
      (ProjectEditor editor, YaNonVisibleComponentsPanel nonVisPanel) {
    return new YaVisibleComponentsPanelMobile(editor, nonVisPanel);
  }

  /**
   * Creates, visually centers, and optionally displays the dialog box
   * that informs the user how to start learning about using App Inventor
   * or create a new project.
   * @param showDialog Convenience variable to show the created DialogBox.
   * @return The created and optionally displayed Dialog box.
   */
  @Override
  public DialogBox createNoProjectsDialog(boolean showDialog) {
    LOG.warning("Creating mobile no projects dialog");
    final NoProjectDialogBoxMob dialogBox = new NoProjectDialogBoxMob();

    if (showDialog) {
      dialogBox.show();
    }
    return dialogBox;
  }

  @Override
  public ProjectPropertiesDialogBox createProjectPropertiesDialog(YaProjectEditor projectEditor) {
    return new ProjectPropertiesDialogBoxMob(projectEditor);
  }

}
