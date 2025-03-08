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
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
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

  private static final MoveProjectsWizardUiBinder uibinder =
      GWT.create(MoveProjectsWizardUiBinder.class);

  private final FolderManager manager;

  @UiField Dialog moveDialog;
  @UiField Button moveButton;
  @UiField Button cancelButton;
  @UiField Tree tree;
  @UiField Button topInvisible;
  @UiField Button bottomInvisible;
  private List<Project> selectedProjects;
  private List<ProjectFolder> selectedFolders;

  /**
   * Creates a new wizard for moving projects.
   */
  public MoveProjectsWizard() {
    uibinder.createAndBindUi(this);

    manager = Ode.getInstance().getFolderManager();
    ProjectList projectList = ProjectListBox.getProjectListBox().getProjectList();
    selectedProjects = projectList.getSelectedProjects();
    selectedFolders = projectList.getSelectedFolders();
    FolderTreeItem root = renderFolder(manager.getGlobalFolder());
    // This undescriptive method below sets whether the tree is expanded or not
    root.setState(true);
    tree.addItem(root);
    tree.setSelectedItem(root);
    moveDialog.center();
    tree.setFocus(true);

    tree.addFocusHandler(new FocusHandler() {
      @Override
      public void onFocus(FocusEvent event) {
        tree.getParent().addStyleName("gwt-Tree-focused");
      }
    });
    
    tree.addBlurHandler(new BlurHandler() {
      @Override
      public void onBlur(BlurEvent event) {
        tree.getParent().removeStyleName("gwt-Tree-focused");
      }
    });
  }

  FolderTreeItem renderFolder(ProjectFolder folder) {
    FolderTreeItem treeItem = new FolderTreeItem(folder);
    for (ProjectFolder child : folder.getChildFolders()) {
      if (!"*trash*".equals(child.getName()) && !selectedFolders.contains(child)) {
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
    manager.moveItemsToFolder(selectedProjects, selectedFolders,
        treeItem.getFolder());
    moveDialog.hide();
  }

  @UiHandler("topInvisible")
  protected void FocusLast(FocusEvent event) {
     moveButton.setFocus(true);
  }

  @UiHandler("bottomInvisible")
  protected void FocusFirst(FocusEvent event) {
     tree.setFocus(true);
  }
}
