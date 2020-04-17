// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2017-2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.utils;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.editor.youngandroid.YaBlocksEditor;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.wizards.ComponentImportWizard.ImportComponentCallback;
import com.google.appinventor.shared.rpc.UploadResponse;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidAssetNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.appinventor.shared.storage.StorageUtil;
import com.google.gwt.core.client.GWT;
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

import static com.google.appinventor.client.Ode.MESSAGES;

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
public final class HTML5DragDrop {
  interface HTML5DragDropSupport extends JsniBundle {
    @LibrarySource("html5dnd.js")
    void init();
  }

  @JsFunction
  public interface ConfirmCallback {
    void run();
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
    top.HTML5DragDrop_isBlocksEditorOpen =
      $entry(@com.google.appinventor.client.utils.HTML5DragDrop::isBlocksEditorOpen());
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

  protected static void handleUploadResponse(String _projectId, String type, String name, String body) {
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
          } else if ("extension".equals(type)) {
            long projectId = Long.parseLong(_projectId);
            YoungAndroidProjectNode projectNode = (YoungAndroidProjectNode) ode.getProjectManager()
                .getProject(projectId).getRootNode();
            ode.getComponentService().importComponentToProject(response.getInfo(), projectId,
                projectNode.getAssetsFolder().getFileId(), new ImportComponentCallback());
          } else if ("asset".equals(type)) {
            long projectId = Long.parseLong(_projectId);
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
}
