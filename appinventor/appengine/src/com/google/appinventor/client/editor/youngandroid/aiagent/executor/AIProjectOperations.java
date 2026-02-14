// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.aiagent.executor;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.editor.ProjectEditor;
import com.google.appinventor.client.editor.simple.components.MockForm;
import com.google.appinventor.client.editor.youngandroid.DesignToolbar;
import com.google.appinventor.client.editor.youngandroid.DesignToolbar.DesignProject;
import com.google.appinventor.client.editor.youngandroid.DesignToolbar.Screen;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;
import com.google.appinventor.client.editor.youngandroid.YaProjectEditor;
import com.google.appinventor.client.editor.youngandroid.aiagent.AIEditorState;
import com.google.appinventor.client.editor.youngandroid.aiagent.AIJsonUtils;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.shared.rpc.aiagent.AIOperation;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidBlocksNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidFormNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidPackageNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;

/**
 * Executes Phase 1 (async) project-level AI operations: SWITCH_SCREEN,
 * CREATE_SCREEN, DELETE_SCREEN, SET_PROJECT_PROP, TOGGLE_EDITOR.
 */
final class AIProjectOperations {

  interface ProjectOpCallback {
    void onSuccess();
    void onFailure(String message);
  }

  private AIProjectOperations() {}

  static void execute(AIOperation op, ProjectOpCallback callback) {
    JSONObject json = JSONParser.parseStrict(op.getPayload()).isObject();

    switch (op.getType()) {
      case SWITCH_SCREEN:
        executeSwitchScreen(json, callback);
        break;
      case CREATE_SCREEN:
        executeCreateScreen(json, callback);
        break;
      case DELETE_SCREEN:
        executeDeleteScreen(json, callback);
        break;
      case SET_PROJECT_PROP:
        executeSetProjectProp(json, callback);
        break;
      case TOGGLE_EDITOR:
        executeToggleEditor(json, callback);
        break;
      default:
        callback.onFailure("Unexpected project-level op type: " + op.getType());
    }
  }

  private static void executeSwitchScreen(JSONObject json,
      final ProjectOpCallback callback) {
    String screen = AIJsonUtils.getStringField(json, "screen_name");
    long projectId = Ode.getInstance().getCurrentYoungAndroidProjectId();
    DesignToolbar toolbar = Ode.getInstance().getDesignToolbar();
    try {
      toolbar.switchToScreen(projectId, screen, DesignToolbar.View.DESIGNER);
      waitForScreenReady(callback);
    } catch (Exception e) {
      callback.onFailure("SWITCH_SCREEN: " + e.getMessage());
    }
  }

  private static void executeToggleEditor(JSONObject json,
      final ProjectOpCallback callback) {
    String view = AIJsonUtils.getStringField(json, "view");
    long projectId = Ode.getInstance().getCurrentYoungAndroidProjectId();
    DesignToolbar toolbar = Ode.getInstance().getDesignToolbar();
    DesignProject project = AIEditorState.getCurrentProject();
    if (project == null) {
      callback.onFailure("TOGGLE_EDITOR: no current project");
      return;
    }
    String currentScreen = project.currentScreen;
    DesignToolbar.View targetView = "Blocks".equals(view)
        ? DesignToolbar.View.BLOCKS
        : DesignToolbar.View.DESIGNER;
    try {
      toolbar.switchToScreen(projectId, currentScreen, targetView);
      waitForScreenReady(callback);
    } catch (Exception e) {
      callback.onFailure("TOGGLE_EDITOR: " + e.getMessage());
    }
  }

  private static void executeCreateScreen(JSONObject json,
      final ProjectOpCallback callback) {
    final String screenName = AIJsonUtils.getStringField(json, "screen_name");
    final Ode ode = Ode.getInstance();
    final long projectId = ode.getCurrentYoungAndroidProjectId();
    final Project project = ode.getProjectManager().getProject(projectId);
    final YoungAndroidProjectNode projectRootNode =
        (YoungAndroidProjectNode) project.getRootNode();
    final YoungAndroidPackageNode packageNode = projectRootNode.getPackageNode();
    String qualifiedFormName = packageNode.getPackageName() + '.' + screenName;
    final String formFileId = YoungAndroidFormNode.getFormFileId(qualifiedFormName);
    final String blocksFileId = YoungAndroidBlocksNode.getBlocklyFileId(qualifiedFormName);

    ode.getProjectService().addFile(projectId, formFileId,
        new OdeAsyncCallback<Long>("Error creating screen") {
          @Override
          public void onSuccess(Long modDate) {
            ode.updateModificationDate(projectId, modDate);

            project.addNode(packageNode, new YoungAndroidFormNode(formFileId));
            project.addNode(packageNode, new YoungAndroidBlocksNode(blocksFileId));

            waitForScreenAvailable(projectId, screenName, new Runnable() {
              @Override
              public void run() {
                DesignToolbar toolbar = ode.getDesignToolbar();
                toolbar.switchToScreen(projectId, screenName,
                    DesignToolbar.View.DESIGNER);
                waitForScreenReady(callback);
              }
            });
          }

          @Override
          public void onFailure(Throwable caught) {
            callback.onFailure("CREATE_SCREEN: server error: " + caught.getMessage());
          }
        });
  }

  private static void executeDeleteScreen(JSONObject json,
      final ProjectOpCallback callback) {
    final String screenName = AIJsonUtils.getStringField(json, "screen_name");
    final Ode ode = Ode.getInstance();
    final long projectId = ode.getCurrentYoungAndroidProjectId();
    final Project project = ode.getProjectManager().getProject(projectId);

    DesignProject designProject = AIEditorState.getCurrentProject();
    if (designProject == null) {
      callback.onFailure("DELETE_SCREEN: no current project");
      return;
    }

    Screen screen = designProject.screens.get(screenName);
    if (screen == null) {
      callback.onFailure("DELETE_SCREEN: screen '" + screenName + "' not found");
      return;
    }

    String formFileId = screen.designerEditor.getFileId();
    String blocksFileId = screen.blocksEditor.getFileId();

    ode.getEditorManager().closeFileEditors(projectId,
        new String[]{formFileId, blocksFileId});

    ode.getProjectService().deleteFile(ode.getSessionId(), projectId, formFileId,
        new OdeAsyncCallback<Long>("Error deleting screen") {
          @Override
          public void onSuccess(Long modDate) {
            ode.updateModificationDate(projectId, modDate);

            for (com.google.appinventor.shared.rpc.project.ProjectNode sourceNode
                : project.getRootNode().getAllSourceNodes()) {
              String fid = sourceNode.getFileId();
              if (fid.contains("/" + screenName + ".scm")
                  || fid.contains("/" + screenName + ".bky")
                  || fid.contains("/" + screenName + ".yail")) {
                project.deleteNode(sourceNode);
              }
            }

            ode.getDesignToolbar().removeScreen(projectId, screenName);
            callback.onSuccess();
          }

          @Override
          public void onFailure(Throwable caught) {
            callback.onFailure("DELETE_SCREEN: server error: " + caught.getMessage());
          }
        });
  }

  private static void executeSetProjectProp(JSONObject json,
      ProjectOpCallback callback) {
    String property = AIJsonUtils.getStringField(json, "property");
    String value = AIJsonUtils.getStringField(json, "value");

    try {
      long projectId = Ode.getInstance().getCurrentYoungAndroidProjectId();
      ProjectEditor projectEditor =
          Ode.getInstance().getEditorManager().getOpenProjectEditor(projectId);
      if (projectEditor instanceof YaProjectEditor) {
        YaProjectEditor yaProjectEditor = (YaProjectEditor) projectEditor;
        YaFormEditor formEditor =
            (YaFormEditor) yaProjectEditor.getFormFileEditor("Screen1");
        if (formEditor != null) {
          MockForm form = formEditor.getForm();
          if (form != null) {
            form.changeProperty(property, value);
            callback.onSuccess();
            return;
          }
        }
      }
      callback.onFailure("SET_PROJECT_PROP: Screen1 form editor not available");
    } catch (Exception e) {
      callback.onFailure("SET_PROJECT_PROP: " + e.getMessage());
    }
  }

  // ---- Async waiting helpers ----

  static void waitForScreenReady(final ProjectOpCallback callback) {
    Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
      @Override
      public void execute() {
        if (Ode.getInstance().screensLocked()) {
          Scheduler.get().scheduleDeferred(this);
        } else {
          callback.onSuccess();
        }
      }
    });
  }

  private static void waitForScreenAvailable(final long projectId,
      final String screenName, final Runnable then) {
    Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
      @Override
      public void execute() {
        Ode ode = Ode.getInstance();
        DesignToolbar toolbar = ode.getDesignToolbar();
        DesignProject project = toolbar.projectMap.get(projectId);
        if (project != null && project.screens.containsKey(screenName)
            && !ode.screensLocked()) {
          // The screen is registered in DesignToolbar, but the file editor
          // may not yet be inserted into the ProjectEditor's DeckPanel
          // (insertFileEditor runs in a deferred command).  Switching before
          // that insertion causes an IndexOutOfBoundsException.
          Screen screen = project.screens.get(screenName);
          ProjectEditor projectEditor =
              ode.getEditorManager().getOpenProjectEditor(projectId);
          if (projectEditor != null
              && projectEditor.getFileEditor(
                  screen.designerEditor.getFileId()) != null) {
            then.run();
          } else {
            Scheduler.get().scheduleDeferred(this);
          }
        } else {
          Scheduler.get().scheduleDeferred(this);
        }
      }
    });
  }
}
