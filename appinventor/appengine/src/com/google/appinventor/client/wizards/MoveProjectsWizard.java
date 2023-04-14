// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.wizards;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.components.Button;
import com.google.appinventor.client.components.Dialog;
import com.google.appinventor.client.components.FolderTreeItem;
import com.google.appinventor.client.explorer.folder.Folder;
import com.google.appinventor.client.explorer.folder.FolderManager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Tree;

/**
 * A command that adds a new folder.
 */
public final class MoveProjectsWizard extends Wizard {
  interface MoveProjectsWizardUiBinder extends UiBinder<Dialog, MoveProjectsWizard> {}
  private static final MoveProjectsWizardUiBinder UI_BINDER = GWT.create(MoveProjectsWizardUiBinder.class);

  /**
   * Interface for a command to to move sprojects after a
   * destination is selected
   */
  public static interface MoveProjectsCallback {

    /**
     * Will be invoked after a new project was created.
     *
     * @param project  newly created project
     */
    void onSuccess(Folder destination);
  }

  private FolderManager manager;
  private MoveProjectsCallback moveAction;

  @UiField Dialog moveDialog;
  @UiField Button moveButton;
  @UiField Button cancelButton;
  @UiField Tree tree;

  @UiField(provided=true)
  Resources.Style style = Ode.getUserDarkThemeEnabled() ?
      Resources.INSTANCE.styleDark() : Resources.INSTANCE.styleLight();

  /**
   * Creates a new command for renaming projects
   */
  public MoveProjectsWizard(String title) {
    super(title, true, false);
    style.ensureInjected();
    UI_BINDER.createAndBindUi(this);

    manager = Ode.getInstance().getFolderManager();
    FolderTreeItem root = renderFolder(manager.getGlobalFolder());
    tree.addItem(root);
    tree.setSelectedItem(root);
  }

  static FolderTreeItem renderFolder(Folder folder) {
    FolderTreeItem treeItem = new FolderTreeItem(folder);
    for(Folder child : folder.getChildFolders()) {
      if (!"*trash*".equals(child.getName())) {
        FolderTreeItem childItem = renderFolder(child);
        childItem.setState(true);
        treeItem.addItem(childItem);
      }
    }
    return treeItem;
  }

  @UiHandler("cancelButton")
  void cancelMove(ClickEvent e) {
    moveDialog.hide();
  }

  @UiHandler("moveButton")
  void moveProjects(ClickEvent e) {
    FolderTreeItem treeItem = (FolderTreeItem) tree.getSelectedItem();
    moveAction.onSuccess(treeItem.getFolder());
    moveDialog.hide();
  }

  public void execute(MoveProjectsCallback onSuccessCallback) {
    moveAction = onSuccessCallback;
    moveDialog.center();
  }

  public interface Resources extends ClientBundle {

    public static final Resources INSTANCE =  GWT.create(Resources.class);

    @Source({
      "com/google/appinventor/client/resources/base.css",
      "com/google/appinventor/client/resources/light.css",
        "com/google/appinventor/client/wizards/NewFolderWizard.css"
    })
    Style styleLight();

    @Source({
      "com/google/appinventor/client/resources/base.css",
      "com/google/appinventor/client/resources/dark.css",
        "com/google/appinventor/client/wizards/NewFolderWizard.css"
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
