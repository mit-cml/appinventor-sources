// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.wizards;

import com.google.appinventor.client.widgets.LabeledTextBox;
import com.google.appinventor.client.widgets.Validator;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
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
  private static final Logger LOG = Logger.getLogger(NewFolderWizard.class.getName());

  private FolderManager manager;

  @UiField Dialog addDialog;
  @UiField Button addButton;
  @UiField Button cancelButton;
  @UiField LabeledTextBox input;
  @UiField Tree tree;
  @UiField Button topInvisible;
  @UiField Button bottomInvisible;

  /**
   * Creates a new command for adding folders.
   */
  public NewFolderWizard() {
    NewFolderWizardUiBinder uibinder = GWT.create(NewFolderWizardUiBinder.class);
    uibinder.createAndBindUi(this);
    manager = Ode.getInstance().getFolderManager();
    FolderTreeItem root = renderFolder(manager.getGlobalFolder());
    tree.addItem(root);
    tree.setSelectedItem(root);
    addDialog.center();
    input.setFocus(true);
    
    input.setValidator(new Validator() {
      @Override
      public boolean validate(String value) {
        // Sanitize input by replacing invalid characters
        String sanitizedValue = value.replaceAll("[^a-zA-Z0-9_\-]", "_").replaceAll(" ", "_");
        input.setText(sanitizedValue);

        // Validate the sanitized folder name
        String errorMessage = TextValidators.getErrorMessage(sanitizedValue);
        if (!errorMessage.isEmpty()) {
          input.setErrorMessage("Folder name is invalid: " + errorMessage);
          addButton.setEnabled(false);
          return false;
        }

        // Check for warnings (if any)
        String warningMessage = TextValidators.getWarningMessages(sanitizedValue);
        if (!warningMessage.isEmpty()) {
          input.setErrorMessage("Warning: " + warningMessage);
        }

        addButton.setEnabled(true);
        return true;
      }

      @Override
      public String getErrorMessage() {
        return input.getErrorMessage();
      }
    });

    input.getTextBox().addKeyDownHandler(new KeyDownHandler() {
      @Override
      public void onKeyDown(KeyDownEvent event) {
        int keyCode = event.getNativeKeyCode();
        if (keyCode == KeyCodes.KEY_ENTER) {
          addButton.click();
        } else if (keyCode == KeyCodes.KEY_ESCAPE) {
          cancelButton.click();
        }
      }
    });

    input.getTextBox().addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        input.validate(); // Validate on each key release
      }
    });

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

  private FolderTreeItem renderFolder(ProjectFolder folder) {
    FolderTreeItem treeItem = new FolderTreeItem(folder);
    for (ProjectFolder child : folder.getChildFolders()) {
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

    // Sanitize the folder name before creation
    String folderName = input.getText().replaceAll("[^a-zA-Z0-9_\-]", "_").replaceAll(" ", "_");
    TextValidators.ProjectNameStatus status = TextValidators.checkNewFolderName(folderName, treeItem.getFolder());

    if (status == TextValidators.ProjectNameStatus.SUCCESS) {
      manager.createFolder(folderName, treeItem.getFolder());
    } else {
      input.setErrorMessage("Error creating folder: Invalid folder name.");
    }
    addDialog.hide();
  }

  @UiHandler("topInvisible")
  protected void FocusLast(FocusEvent event) {
    addButton.setFocus(true);
  }

  @UiHandler("bottomInvisible")
  protected void FocusFirst(FocusEvent event) {
    input.setFocus(true);
  }
}
