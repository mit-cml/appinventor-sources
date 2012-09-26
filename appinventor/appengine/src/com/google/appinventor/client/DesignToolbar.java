// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;
import com.google.appinventor.client.explorer.commands.AddFormCommand;
import com.google.appinventor.client.explorer.commands.BuildCommand;
import com.google.appinventor.client.explorer.commands.ChainableCommand;
import com.google.appinventor.client.explorer.commands.CopyYoungAndroidProjectCommand;
import com.google.appinventor.client.explorer.commands.DeleteFileCommand;
import com.google.appinventor.client.explorer.commands.DownloadProjectOutputCommand;
import com.google.appinventor.client.explorer.commands.DownloadToPhoneCommand;
import com.google.appinventor.client.explorer.commands.EnsurePhoneConnectedCommand;
import com.google.appinventor.client.explorer.commands.SaveAllEditorsCommand;
import com.google.appinventor.client.explorer.commands.SaveBlocksCommand;
import com.google.appinventor.client.explorer.commands.ShowBarcodeCommand;
import com.google.appinventor.client.explorer.commands.WaitForBuildResultCommand;
import com.google.appinventor.client.tracking.Tracking;
import com.google.appinventor.client.widgets.Toolbar;
import com.google.appinventor.client.youngandroid.CodeblocksManager;
import com.google.appinventor.common.version.AppInventorFeatures;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

import java.util.List;

/**
 * The design toolbar houses command buttons in the Young Android Design tab.
 *
 */
public class DesignToolbar extends Toolbar {
  private static final String WIDGET_NAME_SAVE = "Save";
  private static final String WIDGET_NAME_SAVE_AS = "SaveAs";
  private static final String WIDGET_NAME_CHECKPOINT = "Checkpoint";
  private static final String WIDGET_NAME_ADDFORM = "AddForm";
  private static final String WIDGET_NAME_REMOVEFORM = "RemoveForm";
  private static final String WIDGET_NAME_BUILD = "Build";
  private static final String WIDGET_NAME_BARCODE = "Barcode";
  private static final String WIDGET_NAME_DOWNLOAD = "Download";
  private static final String WIDGET_NAME_DOWNLOAD_TO_PHONE = "DownloadToPhone";
  private static final String WIDGET_NAME_OPEN_BLOCKS_EDITOR = "OpenBlocksEditor";

  private boolean codeblocksButtonCancel = false;

  private Label projectName;

  /**
   * Initializes and assembles all commands into buttons in the toolbar.
   */
  public DesignToolbar() {
    super();

    projectName = new Label();
    projectName.setStyleName("ya-ProjectName");
    HorizontalPanel toolbar = (HorizontalPanel) getWidget();
    toolbar.insert(projectName, 0);
    toolbar.setCellWidth(projectName, "222px"); // width of palette minus
                                                // cellspacing/border of buttons

    addButton(new ToolbarItem(WIDGET_NAME_SAVE, MESSAGES.saveButton(),
        new SaveAction()));
    addButton(new ToolbarItem(WIDGET_NAME_SAVE_AS, MESSAGES.saveAsButton(),
        new SaveAsAction()));
    addButton(new ToolbarItem(WIDGET_NAME_CHECKPOINT, MESSAGES.checkpointButton(),
        new CheckpointAction()));
    if (AppInventorFeatures.allowMultiScreenApplications()) {
      addButton(new ToolbarItem(WIDGET_NAME_ADDFORM, MESSAGES.addFormButton(),
          new AddFormAction()));
      addButton(new ToolbarItem(WIDGET_NAME_REMOVEFORM, MESSAGES.removeFormButton(),
          new RemoveFormAction()));
    }

    addButton(new ToolbarItem(WIDGET_NAME_OPEN_BLOCKS_EDITOR,
        MESSAGES.openBlocksEditorButton(), new OpenBlocksEditorAction()), true);

    List<ToolbarItem> buildItems = Lists.newArrayList();
    buildItems.add(new ToolbarItem(WIDGET_NAME_BARCODE,
        MESSAGES.showBarcodeButton(), new BarcodeAction()));
    buildItems.add(new ToolbarItem(WIDGET_NAME_DOWNLOAD,
        MESSAGES.downloadToComputerButton(), new DownloadAction()));
    buildItems.add(new ToolbarItem(WIDGET_NAME_DOWNLOAD_TO_PHONE,
        MESSAGES.downloadToPhoneButton(), new DownloadToPhoneAction()));
    addDropDownButton(WIDGET_NAME_BUILD, MESSAGES.buildButton(), buildItems, true);

  }

  private class SaveAction implements Command {
    @Override
    public void execute() {
      ProjectRootNode projectRootNode = Ode.getInstance().getCurrentYoungAndroidProjectRootNode();
      if (projectRootNode != null) {
        ChainableCommand cmd = new SaveAllEditorsCommand(null);
        cmd.startExecuteChain(Tracking.PROJECT_ACTION_SAVE_YA, projectRootNode);
      }
    }
  }

  private class SaveAsAction implements Command {
    @Override
    public void execute() {
      ProjectRootNode projectRootNode = Ode.getInstance().getCurrentYoungAndroidProjectRootNode();
      if (projectRootNode != null) {
        ChainableCommand cmd = new SaveAllEditorsCommand(
            new CopyYoungAndroidProjectCommand(false));
        cmd.startExecuteChain(Tracking.PROJECT_ACTION_SAVE_AS_YA, projectRootNode);
      }
    }
  }

  private class CheckpointAction implements Command {
    @Override
    public void execute() {
      ProjectRootNode projectRootNode = Ode.getInstance().getCurrentYoungAndroidProjectRootNode();
      if (projectRootNode != null) {
        ChainableCommand cmd = new SaveAllEditorsCommand(
            new CopyYoungAndroidProjectCommand(true));
        cmd.startExecuteChain(Tracking.PROJECT_ACTION_CHECKPOINT_YA, projectRootNode);
      }
    }
  }

  private class AddFormAction implements Command {
    @Override
    public void execute() {
      ProjectRootNode projectRootNode = Ode.getInstance().getCurrentYoungAndroidProjectRootNode();
      if (projectRootNode != null) {
        ChainableCommand cmd = new AddFormCommand();
        cmd.startExecuteChain(Tracking.PROJECT_ACTION_ADDFORM_YA, projectRootNode);
      }
    }
  }

  private class RemoveFormAction implements Command {
    @Override
    public void execute() {
      YaFormEditor formEditor = Ode.getInstance().getCurrentYoungAndroidFormEditor();
      if (formEditor != null && !formEditor.isScreen1()) {
        // DeleteFileCommand handles the whole operation, including displaying the confirmation
        // message dialog, closing the form editor, deleting the file in the server's storage,
        // and deleting the corresponding client-side node.
        final String deleteConfirmationMessage = MESSAGES.reallyDeleteForm(
            formEditor.getFormName());
        ChainableCommand cmd = new DeleteFileCommand() {
          protected boolean deleteConfirmation() {
            return Window.confirm(deleteConfirmationMessage);
          }
        };
        cmd.startExecuteChain(Tracking.PROJECT_ACTION_REMOVEFORM_YA, formEditor.getFormNode());
      }
    }
  }

  private class BarcodeAction implements Command {
    @Override
    public void execute() {
      ProjectRootNode projectRootNode = Ode.getInstance().getCurrentYoungAndroidProjectRootNode();
      if (projectRootNode != null) {
        String target = YoungAndroidProjectNode.YOUNG_ANDROID_TARGET_ANDROID;
        ChainableCommand cmd = new SaveAllEditorsCommand(
            new SaveBlocksCommand(
                new BuildCommand(target,
                    new WaitForBuildResultCommand(target,
                        new ShowBarcodeCommand(target)))));
        updateBuildButton(true);
        cmd.startExecuteChain(Tracking.PROJECT_ACTION_BUILD_BARCODE_YA, projectRootNode,
            new Command() {
          @Override
          public void execute() {
            updateBuildButton(false);
          }
        });
      }
    }
  }

  private class DownloadAction implements Command {
    @Override
    public void execute() {
      ProjectRootNode projectRootNode = Ode.getInstance().getCurrentYoungAndroidProjectRootNode();
      if (projectRootNode != null) {
        String target = YoungAndroidProjectNode.YOUNG_ANDROID_TARGET_ANDROID;
        ChainableCommand cmd = new SaveAllEditorsCommand(
            new SaveBlocksCommand(
                new BuildCommand(target,
                    new WaitForBuildResultCommand(target,
                        new DownloadProjectOutputCommand(target)))));
        updateBuildButton(true);
        cmd.startExecuteChain(Tracking.PROJECT_ACTION_BUILD_DOWNLOAD_YA, projectRootNode,
            new Command() {
          @Override
          public void execute() {
            updateBuildButton(false);
          }
        });
      }
    }
  }

  private class DownloadToPhoneAction implements Command {
    @Override
    public void execute() {
      ProjectRootNode projectRootNode = Ode.getInstance().getCurrentYoungAndroidProjectRootNode();
      if (projectRootNode != null) {
        String target = YoungAndroidProjectNode.YOUNG_ANDROID_TARGET_ANDROID;
        ChainableCommand cmd = new EnsurePhoneConnectedCommand(
            new SaveAllEditorsCommand(
                new SaveBlocksCommand(
                    new BuildCommand(target,
                        new WaitForBuildResultCommand(target,
                            new DownloadToPhoneCommand(target))))));
        updateBuildButton(true);
        cmd.startExecuteChain(Tracking.PROJECT_ACTION_BUILD_DOWNLOAD_TO_PHONE_YA, projectRootNode,
            new Command() {
          @Override
          public void execute() {
            updateBuildButton(false);
          }
        });
      }
    }
  }

  private class OpenBlocksEditorAction implements Command {
    @Override
    public void execute() {
      if (codeblocksButtonCancel) {
        CodeblocksManager.getCodeblocksManager().cancelCodeblocks();
      } else {
        CodeblocksManager.getCodeblocksManager().startCodeblocks();
      }
    }
  }

  public void updateProjectName(String name) {
    projectName.setText(name);
  }

  /**
   * Enables and/or disables buttons based (mostly) on whether there is a
   * current form editor.
   */
  public void updateButtons() {
    YaFormEditor formEditor = Ode.getInstance().getCurrentYoungAndroidFormEditor();
    boolean enabled = (formEditor != null);
    setButtonEnabled(WIDGET_NAME_SAVE, enabled);
    setButtonEnabled(WIDGET_NAME_SAVE_AS, enabled);
    setButtonEnabled(WIDGET_NAME_CHECKPOINT, enabled);
    setDropItemEnabled(WIDGET_NAME_BARCODE, enabled);
    setDropItemEnabled(WIDGET_NAME_DOWNLOAD, enabled);
    setDropItemEnabled(WIDGET_NAME_DOWNLOAD_TO_PHONE, enabled);

    if (AppInventorFeatures.allowMultiScreenApplications()) {
      setButtonEnabled(WIDGET_NAME_ADDFORM, enabled);
      enabled = (formEditor != null && !formEditor.isScreen1());
      setButtonEnabled(WIDGET_NAME_REMOVEFORM, enabled);
    }

    updateCodeblocksButton();
  }

  /**
   * Shows feedback on Package for Phone (Build) button while building.
   */
  private void updateBuildButton(boolean isBuilding) {
    setDropDownButtonEnabled(WIDGET_NAME_BUILD, !isBuilding);
    setDropDownButtonCaption(WIDGET_NAME_BUILD,
        isBuilding ? MESSAGES.isBuildingButton() : MESSAGES.buildButton());
  }

  /**
   * Enable or disables the codeblocks button based on whether the
   * {@link CodeblocksManager} can start (or cancel) codeblocks.
   */
  public void updateCodeblocksButton() {
    setButtonEnabled(WIDGET_NAME_OPEN_BLOCKS_EDITOR, codeblocksButtonCancel
        ? CodeblocksManager.getCodeblocksManager().canCancelCodeblocks()
        : CodeblocksManager.getCodeblocksManager().canStartCodeblocks());
  }

  /**
   * Changes the codeblocks button label to reflect its current state
   *
   * @param cancel true if the button should be a cancel button
   */
  public void updateCodeblocksButtonLabel(boolean cancel) {
    codeblocksButtonCancel = cancel;
    String caption;
    if (codeblocksButtonCancel) {
      caption = MESSAGES.cancelBlocksEditorButton();
    } else if (CodeblocksManager.getCodeblocksManager().canStartCodeblocks()) {
      caption = MESSAGES.openBlocksEditorButton();
    } else {
      caption = MESSAGES.blocksEditorIsOpenButton();
    }
    setButtonCaption(WIDGET_NAME_OPEN_BLOCKS_EDITOR, caption);
  }
}
