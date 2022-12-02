// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.commands;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.components.Button;
import com.google.appinventor.client.components.Dialog;
import com.google.appinventor.client.components.FolderTreeItem;
import com.google.appinventor.client.explorer.folder.Folder;
import com.google.appinventor.client.explorer.folder.FolderManager;
import com.google.appinventor.client.youngandroid.TextValidators;

/**
 * A command that adds a new folder.
 */
public final class AddFolderCommand implements Command {
  interface AddFolderCommandUiBinder extends UiBinder<Dialog, AddFolderCommand> {}
  private static final AddFolderCommandUiBinder UI_BINDER = GWT.create(AddFolderCommandUiBinder.class);

  private FolderManager manager;

  @UiField Dialog addDialog;
  @UiField Button addButton;
  @UiField Button cancelButton;
  @UiField TextBox input;
  @UiField Tree tree;

  @UiField(provided=true)
  Resources.Style style = Ode.getUserDarkThemeEnabled() ?
      Resources.INSTANCE.styleDark() : Resources.INSTANCE.styleLight();

  /**
   * Creates a new command for renaming projects
   */
  public AddFolderCommand() {
    style.ensureInjected();
    UI_BINDER.createAndBindUi(this);

    manager = Ode.getInstance().getFolderManager();
    FolderTreeItem root = renderFolder(manager.getGlobalFolder());
    tree.addItem(root);
    tree.setSelectedItem(root);
  }

  private FolderTreeItem renderFolder(Folder folder) {
    FolderTreeItem treeItem = new FolderTreeItem(folder);
    for(Folder child : folder.getChildFolders()) {
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
        input.getValue(), treeItem.getFolder());
    if (status == TextValidators.ProjectNameStatus.SUCCESS) {
      manager.createFolder(input.getValue(), treeItem.getFolder());
    }
    addDialog.hide();
  }

  @Override
  public void execute() {
    addDialog.center();
  }

  public interface Resources extends ClientBundle {

    public static final Resources INSTANCE =  GWT.create(Resources.class);

    @Source({
      "com/google/appinventor/client/resources/base.css",
      "com/google/appinventor/client/resources/light.css",
      "com/google/appinventor/client/explorer/commands/addFolderCommand.css"
    })
    Style styleLight();

    @Source({
      "com/google/appinventor/client/resources/base.css",
      "com/google/appinventor/client/resources/dark.css",
      "com/google/appinventor/client/explorer/commands/addFolderCommand.css"
    })
    Style styleDark();

    public interface Style extends CssResource {
      String text();
      String actions();
      String input();
      String tree();
    }
  }
}
