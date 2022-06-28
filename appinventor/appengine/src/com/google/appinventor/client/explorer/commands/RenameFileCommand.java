// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.commands;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.widgets.LabeledTextBox;
import com.google.appinventor.client.youngandroid.TextValidators;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidAssetsFolder;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * Command for renaming files.
 */
public class RenameFileCommand extends ChainableCommand {

  @Override
  protected boolean willCallExecuteNextCommand() {
    return true;
  }

  @Override
  protected void execute(ProjectNode node) {
    new RenameDialog(node).center();
  }

  /**
   * This class defines the dialog box for renaming an asset.
   */
  private class RenameDialog extends DialogBox {
    // UI elements
    private final LabeledTextBox newNameTextBox;
    private ProjectNode node;

    RenameDialog(ProjectNode node) {
      super(false, true);
      this.node = node;

      setStylePrimaryName("ode-DialogBox");
      setText(MESSAGES.renameTitle());
      VerticalPanel contentPanel = new VerticalPanel();

      LabeledTextBox oldNameTextBox = new LabeledTextBox(MESSAGES.oldNameLabel());
      oldNameTextBox.setText(node.getName());
      oldNameTextBox.setEnabled(false);
      contentPanel.add(oldNameTextBox);

      newNameTextBox = new LabeledTextBox(MESSAGES.newNameLabel());
      newNameTextBox.getTextBox().addKeyUpHandler(new KeyUpHandler() {
        @Override
        public void onKeyUp(KeyUpEvent event) {
          int keyCode = event.getNativeKeyCode();
          if (keyCode == KeyCodes.KEY_ENTER) {
            handleOkClick();
          } else if (keyCode == KeyCodes.KEY_ESCAPE) {
            hide();
          }
        }
      });
      contentPanel.add(newNameTextBox);

      Button cancelButton = new Button(MESSAGES.cancelButton());
      cancelButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          hide();
        }
      });
      Button okButton = new Button(MESSAGES.okButton());
      okButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          handleOkClick();
        }
      });
      HorizontalPanel buttonPanel = new HorizontalPanel();
      buttonPanel.add(cancelButton);
      buttonPanel.add(okButton);
      buttonPanel.setSize("100%", "24px");
      contentPanel.add(buttonPanel);
      contentPanel.setSize("320px", "100%");

      add(contentPanel);
    }

    private void handleOkClick() {
      final String newName = newNameTextBox.getText();
      final String newFileId = node.getParent().getFileId() + "/" + newName;
      // No change in name
      if (newName == this.node.getName()) {
        hide();
        return;
      } else if (!TextValidators.isValidCharFilename(newName)) {
        onRenameErrorDialog(MESSAGES.malformedFilenameTitle(), MESSAGES.malformedFilename(), this.node.getName());
        return;
      } else if (!TextValidators.isValidLengthFilename(newName)) {
        onRenameErrorDialog(MESSAGES.filenameBadSizeTitle(), MESSAGES.filenameBadSize(), this.node.getName());
        return;
      } else if (conflictingNameWithExistingFile(newName, getAssetsFolderForProject(node))) {
        onRenameErrorDialog(MESSAGES.duplicateTitleInAssetRenameError(), MESSAGES.duplicateFileName(), this.node.getName());
        return;
      }

      hide();
      // Passed check
      final Ode ode = Ode.getInstance();
      ode.getProjectService().renameFile(ode.getSessionId(),
          node.getProjectId(), node.getFileId(), newFileId, new OdeAsyncCallback<Long>() {
            @Override
            public void onSuccess(Long date) {
              getProject(node).renameNode(node, newName, newFileId, node.getName());
              ode.updateModificationDate(node.getProjectId(), date);
            }
            @Override
            public void onFailure(Throwable caught) {
              super.onFailure(caught);
              executionFailedOrCanceled();
            }
          });
    }

    @Override
    public void show() {
      super.show();

      DeferredCommand.addCommand(new Command() {
        @Override
        public void execute() {
          newNameTextBox.setFocus(true);
          newNameTextBox.selectAll();
        }
      });
    }

    private YoungAndroidAssetsFolder getAssetsFolderForProject(ProjectNode node) {
      Project project = Ode.getInstance().getProjectManager().getProject(node.getProjectId());
      YoungAndroidAssetsFolder assetsFolder = ((YoungAndroidProjectNode) project.getRootNode()).getAssetsFolder();
      return assetsFolder;
    }

    private boolean conflictingNameWithExistingFile(String newName, YoungAndroidAssetsFolder assetsFolder) {
      for (ProjectNode node : assetsFolder.getChildren()) {
        if (node.getName() == newName) {
          return true;
        }
      }
      return false;
    }

    private void onRenameErrorDialog(String title, String body, final String oldName) {
      hide();
      final DialogBox dialogBox = new DialogBox(false, true);
      HTML message;
      dialogBox.setStylePrimaryName("ode-DialogBox");
      dialogBox.setHeight("150px");
      dialogBox.setWidth("350px");
      dialogBox.setGlassEnabled(true);
      dialogBox.setAnimationEnabled(true);
      dialogBox.center();
      VerticalPanel DialogBoxContents = new VerticalPanel();
      FlowPanel holder = new FlowPanel();
      Button ok = new Button("OK");
      ok.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          dialogBox.hide();
          show();
        }
      });
      holder.add(ok);
      dialogBox.setText(title);
      message = new HTML(body);
      message.setStyleName("DialogBox-message");
      DialogBoxContents.add(message);
      DialogBoxContents.add(holder);
      dialogBox.setWidget(DialogBoxContents);
      dialogBox.show();
    }
  }
}
