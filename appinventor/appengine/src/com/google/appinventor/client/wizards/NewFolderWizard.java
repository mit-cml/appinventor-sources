// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.wizards;

import com.google.appinventor.client.widgets.LabeledTextBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Tree;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.explorer.folder.FolderTreeItem;
import com.google.appinventor.client.explorer.folder.ProjectFolder;
import com.google.appinventor.client.explorer.folder.FolderManager;
import com.google.appinventor.client.youngandroid.TextValidators;
import java.util.logging.Logger;

/**
 * A command that adds a new folder.
 */
public final class NewFolderWizard {
  interface NewFolderWizardUiBinder extends UiBinder<Dialog, NewFolderWizard> {}
  private static final NewFolderWizardUiBinder UI_BINDER = GWT.create(NewFolderWizardUiBinder.class);
  private static final Logger LOG = Logger.getLogger(NewFolderWizard.class.getName());

  private FolderManager manager;

  @UiField Dialog addDialog;
  @UiField Button addButton;
  @UiField Button cancelButton;
  @UiField LabeledTextBox input;
  @UiField Tree tree;

  /**
   * Creates a new command for renaming projects
   */
  public NewFolderWizard() {
    UI_BINDER.createAndBindUi(this);
    manager = Ode.getInstance().getFolderManager();
    FolderTreeItem root = renderFolder(manager.getGlobalFolder());
    tree.addItem(root);
    tree.setSelectedItem(root);
    addDialog.center();
    input.setFocus(true);
  }

  private FolderTreeItem renderFolder(ProjectFolder folder) {
    FolderTreeItem treeItem = new FolderTreeItem(folder);
    for(ProjectFolder child : folder.getChildFolders()) {
      if (!"*trash*".equals(child.getName())) {
        treeItem.addItem(renderFolder(child));
      }
    }
    return treeItem;
  }

  @UiHandler("cancelButton")
  void cancelAdd(ClickEvent e) {
    addDialog.hide();
  }

  @UiHandler("addButton")
  void addFolder(ClickEvent e) {
    FolderTreeItem treeItem = (FolderTreeItem) tree.getSelectedItem();
    TextValidators.ProjectNameStatus status = TextValidators.checkNewFolderName(
        input.getText(), treeItem.getFolder());
    if (status == TextValidators.ProjectNameStatus.SUCCESS) {
      manager.createFolder(input.getText(), treeItem.getFolder());
    }
    addDialog.hide();
  }
}
