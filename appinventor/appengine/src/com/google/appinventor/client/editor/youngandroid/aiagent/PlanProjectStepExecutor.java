// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.aiagent;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.editor.ProjectEditor;
import com.google.appinventor.client.editor.blocks.BlocksEditor;
import com.google.appinventor.client.editor.designer.DesignerEditor;
import com.google.appinventor.client.editor.youngandroid.DesignToolbar;
import com.google.appinventor.client.editor.youngandroid.YaProjectEditor;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidBlocksNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidFormNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidPackageNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Timer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Handles project-level plan steps: creating screens from {@code __project__}
 * steps, waiting for editor readiness, and parsing plan JSON into per-screen
 * step groups.
 *
 * <p>Extracted from {@link AIOrchestrationManager} to keep that class focused
 * on child conversation coordination and the FIFO batch queue.</p>
 */
final class PlanProjectStepExecutor {

  private static final Logger LOG =
      Logger.getLogger(PlanProjectStepExecutor.class.getName());

  /** Polling interval when waiting for editors to become ready (ms). */
  private static final int EDITOR_READY_POLL_MS = 100;

  /** Maximum time to wait for editors to become ready (ms). */
  private static final int EDITOR_READY_TIMEOUT_MS = 10000;

  /** Callback for when project steps and editor readiness are complete. */
  interface ReadyCallback {
    void onReady(YaProjectEditor projectEditor, Map<String, String> screenSteps);
    void onError(String message);
  }

  private final AIContextCollector contextCollector;
  private volatile boolean cancelled;

  PlanProjectStepExecutor(AIContextCollector contextCollector) {
    this.contextCollector = contextCollector;
  }

  void cancel() {
    cancelled = true;
  }

  // ---- Main entry point ----

  /**
   * Parses the plan, creates any required screens, waits for all target
   * editors to load, then invokes the callback with the project editor
   * and per-screen step map.
   */
  void prepareAndExecute(String planJson,
      AIResponseOrchestrator.ChatCallback uiCallback, ReadyCallback readyCallback) {
    Map<String, String> screenSteps = parsePlanSteps(planJson);
    if (screenSteps.isEmpty()) {
      readyCallback.onError("No screen-level steps found in the plan.");
      return;
    }

    long projectId = contextCollector.getCurrentProjectId();
    ProjectEditor projectEditor = Ode.getInstance().getEditorManager()
        .getOpenProjectEditor(projectId);
    if (!(projectEditor instanceof YaProjectEditor)) {
      readyCallback.onError("Cannot execute plan: project editor not available.");
      return;
    }
    YaProjectEditor yaProjectEditor = (YaProjectEditor) projectEditor;

    List<String> screensToCreate =
        extractScreensToCreate(screenSteps, yaProjectEditor);

    if (screensToCreate.isEmpty()) {
      waitForEditorsReady(yaProjectEditor, screenSteps, readyCallback);
    } else {
      uiCallback.setStatusText("Creating screens...");
      createScreensThenWait(
          screensToCreate, 0, yaProjectEditor, screenSteps, uiCallback, readyCallback);
    }
  }

  // ---- Plan parsing ----

  /**
   * Parses the plan JSON and extracts screen-level steps. Steps targeting
   * {@code __project__} are skipped.
   */
  Map<String, String> parsePlanSteps(String planJson) {
    Map<String, String> screenSteps = new HashMap<>();
    try {
      JSONValue parsed = JSONParser.parseStrict(planJson);
      JSONObject planObj = parsed.isObject();
      if (planObj == null) {
        return screenSteps;
      }
      JSONValue stepsVal = planObj.get("steps");
      if (stepsVal == null) {
        JSONArray arr = parsed.isArray();
        if (arr != null) {
          extractStepsFromArray(arr, screenSteps);
        }
        return screenSteps;
      }
      JSONArray stepsArr = stepsVal.isArray();
      if (stepsArr != null) {
        extractStepsFromArray(stepsArr, screenSteps);
      }
    } catch (Exception e) {
      LOG.warning("Failed to parse plan JSON: " + e.getMessage());
    }
    return screenSteps;
  }

  private void extractStepsFromArray(JSONArray steps,
      Map<String, String> screenSteps) {
    for (int i = 0; i < steps.size(); i++) {
      JSONObject step = steps.get(i).isObject();
      if (step == null) {
        continue;
      }
      String screen = getJsonString(step, "screen");
      if (screen == null || "__project__".equals(screen)) {
        continue;
      }
      String description = getJsonString(step, "description");
      if (description == null) {
        description = "Execute step " + (i + 1) + " on " + screen;
      }
      String existing = screenSteps.get(screen);
      screenSteps.put(screen,
          existing != null ? existing + "\n" + description : description);
    }
  }

  static String getJsonString(JSONObject obj, String key) {
    JSONValue val = obj.get(key);
    if (val == null) {
      return null;
    }
    JSONString str = val.isString();
    return str != null ? str.stringValue() : null;
  }

  // ---- Screen creation ----

  /**
   * Determines which screens need to be created by finding screen names
   * referenced in the plan's screen-level steps that don't yet have
   * editors in the project. This uses the structured {@code screen} field
   * from each step — no description parsing needed.
   */
  private List<String> extractScreensToCreate(
      Map<String, String> screenSteps, YaProjectEditor yaProjectEditor) {
    List<String> screens = new ArrayList<>();
    for (String screenName : screenSteps.keySet()) {
      if (yaProjectEditor.getFormFileEditor(screenName) == null
          && !screens.contains(screenName)) {
        screens.add(screenName);
      }
    }
    return screens;
  }

  /**
   * Creates screens one at a time (async), then waits for editor readiness.
   * Does NOT navigate to the newly created screens.
   */
  private void createScreensThenWait(final List<String> screensToCreate,
      final int index, final YaProjectEditor yaProjectEditor,
      final Map<String, String> screenSteps,
      final AIResponseOrchestrator.ChatCallback uiCallback,
      final ReadyCallback readyCallback) {
    if (cancelled) {
      return;
    }
    if (index >= screensToCreate.size()) {
      LOG.info("All " + screensToCreate.size() + " screen(s) created");
      uiCallback.setStatusText("Starting plan execution...");
      waitForEditorsReady(yaProjectEditor, screenSteps, readyCallback);
      return;
    }

    final String screenName = screensToCreate.get(index);
    LOG.info("Creating screen " + screenName
        + " (" + (index + 1) + "/" + screensToCreate.size() + ")");

    final Ode ode = Ode.getInstance();
    final long projectId = contextCollector.getCurrentProjectId();
    final Project project = ode.getProjectManager().getProject(projectId);
    final YoungAndroidProjectNode rootNode =
        (YoungAndroidProjectNode) project.getRootNode();
    final YoungAndroidPackageNode packageNode = rootNode.getPackageNode();
    String qualifiedName = packageNode.getPackageName() + '.' + screenName;
    final String formFileId =
        YoungAndroidFormNode.getFormFileId(qualifiedName);
    final String blocksFileId =
        YoungAndroidBlocksNode.getBlocklyFileId(qualifiedName);

    ode.getProjectService().addFile(projectId, formFileId,
        new OdeAsyncCallback<Long>("Error creating screen") {
          @Override
          public void onSuccess(Long modDate) {
            if (cancelled) {
              return;
            }
            ode.updateModificationDate(projectId, modDate);
            project.addNode(packageNode,
                new YoungAndroidFormNode(formFileId));
            project.addNode(packageNode,
                new YoungAndroidBlocksNode(blocksFileId));
            waitForScreenRegistered(projectId, screenName, new Runnable() {
              @Override
              public void run() {
                createScreensThenWait(screensToCreate, index + 1,
                    yaProjectEditor, screenSteps, uiCallback, readyCallback);
              }
            });
          }

          @Override
          public void onFailure(Throwable caught) {
            if (cancelled) {
              return;
            }
            LOG.warning("Failed to create screen " + screenName
                + ": " + caught.getMessage());
            uiCallback.addAiMessage("Failed to create screen " + screenName
                + ": " + caught.getMessage(), System.currentTimeMillis());
            createScreensThenWait(screensToCreate, index + 1,
                yaProjectEditor, screenSteps, uiCallback, readyCallback);
          }
        });
  }

  /**
   * Polls until a newly created screen is registered in the DesignToolbar.
   * Does NOT switch to the screen.
   */
  private void waitForScreenRegistered(final long projectId,
      final String screenName, final Runnable then) {
    Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
      @Override
      public void execute() {
        if (cancelled) {
          return;
        }
        Ode ode = Ode.getInstance();
        DesignToolbar toolbar = ode.getDesignToolbar();
        DesignToolbar.DesignProject designProject =
            toolbar.projectMap.get(projectId);
        if (designProject != null
            && designProject.screens.containsKey(screenName)
            && !ode.screensLocked()) {
          DesignToolbar.Screen screen =
              designProject.screens.get(screenName);
          ProjectEditor pe = ode.getEditorManager()
              .getOpenProjectEditor(projectId);
          if (pe != null
              && pe.getFileEditor(screen.designerEditor.getFileId()) != null) {
            then.run();
            return;
          }
        }
        Scheduler.get().scheduleDeferred(this);
      }
    });
  }

  // ---- Editor readiness ----

  /**
   * Waits until all target screens' form and blocks editors are loaded.
   */
  private void waitForEditorsReady(final YaProjectEditor yaProjectEditor,
      final Map<String, String> screenSteps, final ReadyCallback callback) {
    if (allEditorsReady(yaProjectEditor, screenSteps)) {
      callback.onReady(yaProjectEditor, screenSteps);
      return;
    }
    final long startTime = System.currentTimeMillis();
    final Timer timer = new Timer() {
      @Override
      public void run() {
        if (cancelled) {
          cancel();
          return;
        }
        if (allEditorsReady(yaProjectEditor, screenSteps)) {
          cancel();
          callback.onReady(yaProjectEditor, screenSteps);
          return;
        }
        if (System.currentTimeMillis() - startTime > EDITOR_READY_TIMEOUT_MS) {
          cancel();
          callback.onError(
              "Timed out waiting for screen editors to load. "
              + "Please ensure all target screens are available and try again.");
        }
      }
    };
    timer.scheduleRepeating(EDITOR_READY_POLL_MS);
  }

  private boolean allEditorsReady(YaProjectEditor yaProjectEditor,
      Map<String, String> screenSteps) {
    for (String screenName : screenSteps.keySet()) {
      DesignerEditor<?, ?, ?, ?, ?> formEditor =
          yaProjectEditor.getFormFileEditor(screenName);
      if (formEditor == null || !formEditor.isLoadComplete()) {
        return false;
      }
      BlocksEditor<?, ?> blocksEditor =
          yaProjectEditor.getBlocksFileEditor(screenName);
      if (blocksEditor == null || !blocksEditor.isLoaded()) {
        return false;
      }
    }
    return true;
  }
}
