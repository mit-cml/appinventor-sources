// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2017-2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.utils;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.boxes.AssetListBox;
import com.google.appinventor.client.boxes.PaletteBox;
import com.google.appinventor.client.boxes.ProjectListBox;
import com.google.appinventor.client.editor.youngandroid.YaBlocksEditor;
import com.google.appinventor.client.explorer.dialogs.NoProjectDialogBox;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.wizards.ComponentImportWizard.ImportComponentCallback;
import com.google.appinventor.client.wizards.RequestNewProjectNameWizard;
import com.google.appinventor.client.wizards.RequestProjectNewNameInterface;
import com.google.appinventor.client.youngandroid.TextValidators;

import com.google.appinventor.shared.rpc.UploadResponse;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidAssetNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.appinventor.shared.storage.StorageUtil;

import com.google.gwt.core.client.GWT;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import com.google.gwt.query.client.builders.JsniBundle;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

import jsinterop.annotations.JsFunction;

/**
 * HTML5DragDrop implements support for dragging projects/extensions/assets from the developer's
 * computer into the browser and dropping them onto the workspace. Depending on the extension of
 * the file, one of uploadProject(), uploadExtension(), or uploadMedia() is called to trigger an
 * import of the dropped entity.
 *
 * Compatibility
 * -------------
 *
 * According to Mozilla, HTML5 Drag and Drop support is available starting in the following
 * browser versions:
 *
 *     Chrome: 4
 *     Edge: (always)
 *     Firefox: 3.5
 *     IE: 10
 *     Opera: 12
 *     Safari: 3.1
 */
@SuppressWarnings("checkstyle:JavadocParagraph")
public final class HTML5DragDrop {
  interface HTML5DragDropSupport extends JsniBundle {
    @LibrarySource("html5dnd.js")
    void init();
  }

  @JsFunction
  public interface ConfirmCallback {
    void run();
  }
  
  @JsFunction
  public interface StringCallback {
    void run(String name);
  }

  public static void init() {
    ((HTML5DragDropSupport) GWT.create(HTML5DragDropSupport.class)).init();
    initJsni();
  }


  private static native void initJsni()/*-{
    top.HTML5DragDrop_isProjectEditorOpen =
      $entry(@com.google.appinventor.client.utils.HTML5DragDrop::isProjectEditorOpen());
    top.HTML5DragDrop_getOpenProjectId =
      $entry(@com.google.appinventor.client.utils.HTML5DragDrop::getOpenProjectId());
    top.HTML5DragDrop_handleUploadResponse =
      $entry(@com.google.appinventor.client.utils.HTML5DragDrop::handleUploadResponse(*));
    top.HTML5DragDrop_reportError =
      $entry(@com.google.appinventor.client.utils.HTML5DragDrop::reportError(*));
    top.HTML5DragDrop_confirmOverwriteKey =
      $entry(@com.google.appinventor.client.utils.HTML5DragDrop::confirmOverwriteKey(*));
    top.HTML5DragDrop_getNewProjectName =
      $entry(@com.google.appinventor.client.utils.HTML5DragDrop::getNewProjectName(*));
    top.HTML5DragDrop_confirmOverwriteAsset =
      $entry(@com.google.appinventor.client.utils.HTML5DragDrop::confirmOverwriteAsset(*));
    top.HTML5DragDrop_isBlocksEditorOpen =
      $entry(@com.google.appinventor.client.utils.HTML5DragDrop::isBlocksEditorOpen());
    top.HTML5DragDrop_checkProjectNameForCollision =
      $entry(@com.google.appinventor.client.utils.HTML5DragDrop::checkProjectNameForCollision(*));
    top.HTML5DragDrop_shouldShowDropTarget =
      $entry(@com.google.appinventor.client.utils.HTML5DragDrop::shouldShowDropTarget(*));
  }-*/;


  public static native void importProjectFromUrl(String url)  /*-{
    $wnd.HTML5DragDrop_importProject(url);
  }-*/;

  public static boolean isProjectEditorOpen() {
    return Ode.getInstance().getCurrentView() == 0;
  }

  public static boolean isBlocksEditorOpen() {
    return isProjectEditorOpen()
        && Ode.getInstance().getCurrentFileEditor() instanceof YaBlocksEditor;
  }

  public static String getOpenProjectId() {
    return Long.toString(Ode.getInstance().getCurrentYoungAndroidProjectId());
  }

  protected static void reportError(int errorCode) {
    switch (errorCode) {
      case 1:
        Window.alert("No project open to receive upload.");
        break;
      case 2:
        Window.alert("Uploading of APK files is not supported.");
        break;
      default:
        Window.alert("Unexpected HTTP error code: " + errorCode);
    }
  }

  protected static void confirmOverwriteKey(final ConfirmCallback callback) {
    Ode.getInstance().getUserInfoService().hasUserFile(StorageUtil.ANDROID_KEYSTORE_FILENAME,
        new OdeAsyncCallback<Boolean>(MESSAGES.uploadKeystoreError()) {
          @Override
          public void onSuccess(Boolean keystoreFileExists) {
            if (keystoreFileExists) {
              final DialogBox dialog = new DialogBox(false, true);
              dialog.setStylePrimaryName("ode-DialogBox");
              dialog.setText("Confirm Overwrite...");
              Button cancelButton = new Button(MESSAGES.cancelButton());
              Button deleteButton = new Button(MESSAGES.overwriteButton());
              DockPanel buttonPanel = new DockPanel();
              buttonPanel.add(cancelButton, DockPanel.WEST);
              buttonPanel.add(deleteButton, DockPanel.EAST);
              VerticalPanel panel = new VerticalPanel();
              Label label = new Label();
              label.setText(MESSAGES.confirmOverwriteKeystore());
              panel.add(label);
              panel.add(buttonPanel);
              dialog.add(panel);
              cancelButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                  dialog.hide();
                }
              });
              deleteButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                  dialog.hide();
                  callback.run();
                }
              });
              dialog.center();
              dialog.show();
            } else {
              callback.run();
            }
          }
        });
  }

  protected static void confirmOverwriteAsset(String _projectId, String name, final ConfirmCallback callback) {
    // Get the target project
    long projectId = Long.parseLong(_projectId);
    Project project = Ode.getInstance().getProjectManager().getProject(projectId);
    if (project == null) {
      // Project not open so we have nothing to do.
      return;
    }

    // Check if an asset already exists with the given name
    YoungAndroidProjectNode projectNode = (YoungAndroidProjectNode) project.getRootNode();
    YoungAndroidAssetNode node = (YoungAndroidAssetNode) projectNode.getAssetsFolder().findNode("assets/" + name);
    if (node == null) {
      // No asset exists by that name so it is safe to upload.
      callback.run();
      return;
    }

    // Ask user to confirm overwriting the asset
    // This currently uses the same mechanism as FileUploadWizard, but should be rewritten to use a
    // dialog at some point.
    if (Window.confirm(MESSAGES.confirmOverwrite(name, name))) {
      callback.run();
    }
  }

  /**
   * Checks the project name using the standard set of project name validators. If the project name
   * isn't valid, the drop will be aborted. It doesn't show an alert on invalid project name.
   *
   * @param projectName the project name based on the dropped file's name
   * @return true if the project name is allowed, otherwise false
   */
  protected static boolean checkProjectNameForCollision(String projectName) {
    return TextValidators.checkNewProjectName(projectName, true) 
            == TextValidators.ProjectNameStatus.SUCCESS;
  }
  
  /**
   * Shows dialog box to enter new project name when user tries
   * to upload a project with invalid Name.
   * 
   * @param filename initial filename of project , used to suggest a new name
   * @param callback callback to upload after user enters a valid name
   */
  protected static void getNewProjectName(String filename, final StringCallback callback) {  
    filename = filename.substring(0, filename.length() - 4);

    new RequestNewProjectNameWizard(new RequestProjectNewNameInterface() {
        @Override
        public void getNewName(String name) {
          callback.run(name);
        }
    }, filename, true);
  }

  protected static void handleUploadResponse(String projectIdStr, String type, String name,
      String body) {
    Ode ode = Ode.getInstance();
    UploadResponse response = UploadResponse.extractUploadResponse(body);
    if (response != null) {
      switch (response.getStatus()) {
        case SUCCESS:
          ErrorReporter.hide();
          if ("project".equals(type)) {
            String info = response.getInfo();
            UserProject userProject = UserProject.valueOf(info);
            Project uploadedProject = ode.getProjectManager().addProject(userProject);
            ode.openYoungAndroidProjectInDesigner(uploadedProject);
            NoProjectDialogBox.closeIfOpen();
          } else if ("extension".equals(type)) {
            long projectId = Long.parseLong(projectIdStr);
            YoungAndroidProjectNode projectNode = (YoungAndroidProjectNode) ode.getProjectManager()
                .getProject(projectId).getRootNode();
            ode.getComponentService().importComponentToProject(response.getInfo(), projectId,
                projectNode.getAssetsFolder().getFileId(), new ImportComponentCallback());
          } else if ("asset".equals(type)) {
            long projectId = Long.parseLong(projectIdStr);
            ode.updateModificationDate(projectId, response.getModificationDate());
            Project project = ode.getProjectManager().getProject(projectId);
            YoungAndroidProjectNode projectNode = (YoungAndroidProjectNode) project.getRootNode();
            YoungAndroidAssetNode node = new YoungAndroidAssetNode(name,
                projectNode.getAssetsFolder().getFileId() + "/" + name);
            project.addNode(projectNode.getAssetsFolder(), node);
          } else if ("keystore".equals(type)) {
            Ode.getInstance().getTopToolbar().updateKeystoreFileMenuButtons();
          }
          break;
        case FILE_TOO_LARGE:
          ErrorReporter.reportInfo(MESSAGES.fileTooLargeError());
          break;
        case NOT_PROJECT_ARCHIVE:
          ErrorReporter.reportInfo(MESSAGES.notProjectArchiveError());
          break;
        default:
          ErrorReporter.reportError(MESSAGES.fileUploadError());
      }
    } else {
      ErrorReporter.reportError(MESSAGES.fileUploadError());
    }
  }

  /**
   * Determines whether the given element or an ancestor constitutes a drop target. If so, it will
   * return the Element that should be used for the bounds of the drop rectangle.
   *
   * NB: For security reasons, the browser does not share any information about the thing to be
   * dropped until it is actually dropped. This means we can't selectively show a drop target based
   * on what is being dragged. Ideally, we would only show the drop target for the project list if
   * the dragged item were an AIA and the drop target for the palette if the dragged item were an
   * AIX.
   *
   * @param target The source element for the drag/drop event
   * @return the element to use if the drop is valid, otherwise null
   */
  protected static Element shouldShowDropTarget(Element target) {
    if (Ode.getInstance().getCurrentView() == Ode.PROJECTS) {
      boolean noProjects = 0 == ProjectListBox.getProjectListBox()
          .getProjectList().getMyProjectsCount();
      while (target != Document.get().getBody()) {
        if (noProjects && target == Ode.getInstance().getOverDeckPanel().getElement()) {
          // If there aren't any projects, then we want to support dropping into the empty space
          return Ode.getInstance().getOverDeckPanel().getElement();
        } else if (target == ProjectListBox.getProjectListBox().getElement()) {
          // Allow dropping into the project list
          return target;
        }
        target = target.getParentElement();
      }
    } else if (Ode.getInstance().getCurrentView() == Ode.DESIGNER) {
      while (target != Document.get().getBody()) {
        if (target == AssetListBox.getAssetListBox().getElement()) {
          return target;  // Media list is a drop target
        } else if (target == PaletteBox.getPaletteBox().getElement()) {
          return target;  // Palette panel is a drop target (for extensions)
        }
        target = target.getParentElement();
      }
    }
    return null;  // No valid drop target
  }
}
