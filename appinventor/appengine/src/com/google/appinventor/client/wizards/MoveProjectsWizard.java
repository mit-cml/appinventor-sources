// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.wizards;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.boxes.ProjectListBox;
import com.google.appinventor.client.explorer.folder.FolderManager;
import com.google.appinventor.client.explorer.folder.FolderTreeItem;
import com.google.appinventor.client.explorer.folder.ProjectFolder;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.youngandroid.ProjectList;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Tree;
import java.util.List;

/**
 * A wizard for moving projects between folders.
 */
public final class MoveProjectsWizard {
  interface MoveProjectsWizardUiBinder extends UiBinder<Dialog, MoveProjectsWizard> {}

  private static final MoveProjectsWizardUiBinder UI_BINDER =
      GWT.create(MoveProjectsWizardUiBinder.class);

  private final FolderManager manager;

  @UiField Dialog moveDialog;
  @UiField Button moveButton;
  @UiField Button cancelButton;
  @UiField Tree tree;


  /**
   * Creates a new wizard for moving projects.
   */
  public MoveProjectsWizard() {
    UI_BINDER.createAndBindUi(this);

    manager = Ode.getInstance().getFolderManager();
    FolderTreeItem root = renderFolder(manager.getGlobalFolder());
    tree.addItem(root);
    tree.setSelectedItem(root);
    moveDialog.center();
  }

  static FolderTreeItem renderFolder(ProjectFolder folder) {
    FolderTreeItem treeItem = new FolderTreeItem(folder);
    for (ProjectFolder child : folder.getChildFolders()) {
      if (!"*trash*".equals(child.getName())) {
        FolderTreeItem childItem = renderFolder(child);
        childItem.setState(true);
        treeItem.addItem(childItem);
      }
    }
    return treeItem;
  }

  @SuppressWarnings("unused")
  @UiHandler("cancelButton")
  void cancelMove(ClickEvent e) {
    moveDialog.hide();
  }

  @SuppressWarnings("unused")
  @UiHandler("moveButton")
  void moveProjects(ClickEvent e) {
    FolderTreeItem treeItem = (FolderTreeItem) tree.getSelectedItem();
    ProjectList projectList = ProjectListBox.getProjectListBox().getProjectList();
    List<Project> selectedProjects = projectList.getSelectedProjects();
    List<ProjectFolder> selectedFolders = projectList.getSelectedFolders();
    manager.moveItemsToFolder(selectedProjects, selectedFolders,
        treeItem.getFolder());
    moveDialog.hide();
  }
}
