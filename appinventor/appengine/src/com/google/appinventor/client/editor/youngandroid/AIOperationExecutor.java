// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.editor.FileEditor;
import com.google.appinventor.client.editor.ProjectEditor;
import com.google.appinventor.client.editor.simple.SimpleComponentDatabase;
import com.google.appinventor.client.editor.simple.components.MockComponent;
import com.google.appinventor.client.editor.simple.components.MockContainer;
import com.google.appinventor.client.editor.simple.components.MockForm;
import com.google.appinventor.client.editor.youngandroid.DesignToolbar.DesignProject;
import com.google.appinventor.client.editor.youngandroid.DesignToolbar.Screen;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.properties.json.ClientJsonParser;
import com.google.appinventor.client.youngandroid.TextValidators;
import com.google.appinventor.shared.rpc.aiagent.AIOperation;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidBlocksNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidFormNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidPackageNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Applies AI-generated operations to the current project editor.
 *
 * <p>Operations are grouped into five execution phases to ensure that
 * dependencies between operation types are respected:
 * <ol>
 *   <li>Phase 1 (async): Project-level operations &mdash; SWITCH_SCREEN,
 *       CREATE_SCREEN, DELETE_SCREEN, SET_PROJECT_PROP</li>
 *   <li>Phase 2 (sync): Designer additions &mdash; ADD_COMPONENT,
 *       SET_PROPERTY, RENAME_COMPONENT</li>
 *   <li>Phase 3 (sync): Block additions &mdash; SET_EVENT_HANDLER,
 *       SET_VARIABLE, SET_PROCEDURE</li>
 *   <li>Phase 4 (sync): Block deletions &mdash; DELETE_EVENT_HANDLER,
 *       DELETE_VARIABLE, DELETE_PROCEDURE</li>
 *   <li>Phase 5 (sync): Designer deletions &mdash; DELETE_COMPONENT</li>
 * </ol>
 *
 * <p>Each operation is validated immediately before execution (not all at
 * once up front) because earlier operations change editor state.  On the
 * first failure the executor halts; remaining operations are reported as
 * skipped.  There is no rollback &mdash; partially applied changes remain
 * in the editor.
 */
public class AIOperationExecutor {
  private static final Logger LOG = Logger.getLogger(AIOperationExecutor.class.getName());

  // -----------------------------------------------------------------------
  // ExecutionResult
  // -----------------------------------------------------------------------

  /**
   * Result returned after executing a batch of AI operations.
   */
  public static class ExecutionResult {
    private final List<AIOperation> succeeded;
    private final List<AIOperation> failed;
    private final List<AIOperation> skipped;
    private final String errorMessage;

    public ExecutionResult(List<AIOperation> succeeded, List<AIOperation> failed,
        List<AIOperation> skipped, String errorMessage) {
      this.succeeded = succeeded;
      this.failed = failed;
      this.skipped = skipped;
      this.errorMessage = errorMessage;
    }

    public List<AIOperation> getSucceeded() {
      return succeeded;
    }

    public List<AIOperation> getFailed() {
      return failed;
    }

    public List<AIOperation> getSkipped() {
      return skipped;
    }

    public String getErrorMessage() {
      return errorMessage;
    }

    public boolean isSuccess() {
      return failed.isEmpty();
    }
  }

  // -----------------------------------------------------------------------
  // ExecutionCallback
  // -----------------------------------------------------------------------

  /**
   * Callback invoked when all phases have completed (or an error halts
   * execution).
   */
  public interface ExecutionCallback {
    void onComplete(ExecutionResult result);
  }

  // -----------------------------------------------------------------------
  // Public API
  // -----------------------------------------------------------------------

  /**
   * Execute a list of AI operations against the current editor.
   * Operations are grouped into phases and executed in order.
   *
   * @param operations the operations to apply
   * @param callback   invoked when execution finishes
   */
  public void execute(List<AIOperation> operations, ExecutionCallback callback) {
    // Partition operations into the five phases.
    List<AIOperation> phase1 = new ArrayList<>();
    List<AIOperation> phase2 = new ArrayList<>();
    List<AIOperation> phase3 = new ArrayList<>();
    List<AIOperation> phase4 = new ArrayList<>();
    List<AIOperation> phase5 = new ArrayList<>();

    for (AIOperation op : operations) {
      switch (op.getType()) {
        // Phase 1: project-level
        case SWITCH_SCREEN:
        case CREATE_SCREEN:
        case DELETE_SCREEN:
        case SET_PROJECT_PROP:
          phase1.add(op);
          break;

        // Phase 2: designer adds
        case ADD_COMPONENT:
        case SET_PROPERTY:
        case RENAME_COMPONENT:
          phase2.add(op);
          break;

        // Phase 3: block adds
        case SET_EVENT_HANDLER:
        case SET_VARIABLE:
        case SET_PROCEDURE:
          phase3.add(op);
          break;

        // Phase 4: block deletes
        case DELETE_EVENT_HANDLER:
        case DELETE_VARIABLE:
        case DELETE_PROCEDURE:
          phase4.add(op);
          break;

        // Phase 5: designer deletes
        case DELETE_COMPONENT:
          phase5.add(op);
          break;

        default:
          LOG.warning("Unknown operation type: " + op.getType());
          break;
      }
    }

    // Build a flat ordered list: phase1 .. phase5
    List<AIOperation> ordered = new ArrayList<>();
    ordered.addAll(phase1);
    ordered.addAll(phase2);
    ordered.addAll(phase3);
    ordered.addAll(phase4);
    ordered.addAll(phase5);

    ExecutionState state = new ExecutionState(ordered, callback);
    executePhase1(state, phase1, 0, phase2, phase3, phase4, phase5);
  }

  // -----------------------------------------------------------------------
  // Internal execution state
  // -----------------------------------------------------------------------

  /**
   * Mutable accumulator shared across all phases.
   */
  private static class ExecutionState {
    final List<AIOperation> allOrdered;
    final ExecutionCallback callback;
    final List<AIOperation> succeeded = new ArrayList<>();
    final List<AIOperation> failed = new ArrayList<>();
    final List<AIOperation> skipped = new ArrayList<>();
    String errorMessage = null;
    boolean halted = false;

    ExecutionState(List<AIOperation> allOrdered, ExecutionCallback callback) {
      this.allOrdered = allOrdered;
      this.callback = callback;
    }

    void markFailed(AIOperation op, String message) {
      failed.add(op);
      errorMessage = message;
      halted = true;
    }

    void markSucceeded(AIOperation op) {
      succeeded.add(op);
    }

    /**
     * Skip all operations remaining in the given list starting from
     * {@code startIndex}.
     */
    void skipRemaining(List<AIOperation> ops, int startIndex) {
      for (int i = startIndex; i < ops.size(); i++) {
        skipped.add(ops.get(i));
      }
    }

    void skipAll(List<AIOperation> ops) {
      skipped.addAll(ops);
    }

    ExecutionResult toResult() {
      return new ExecutionResult(succeeded, failed, skipped, errorMessage);
    }

    void finish() {
      callback.onComplete(toResult());
    }
  }

  // -----------------------------------------------------------------------
  // Phase 1 -- Project-level operations (async, chained via callbacks)
  // -----------------------------------------------------------------------

  private void executePhase1(final ExecutionState state, final List<AIOperation> phase1,
      final int index, final List<AIOperation> phase2, final List<AIOperation> phase3,
      final List<AIOperation> phase4, final List<AIOperation> phase5) {

    if (state.halted || index >= phase1.size()) {
      if (state.halted) {
        // Skip remaining phase 1 plus all later phases.
        state.skipRemaining(phase1, index);
        state.skipAll(phase2);
        state.skipAll(phase3);
        state.skipAll(phase4);
        state.skipAll(phase5);
        state.finish();
      } else {
        // Move to synchronous phases.
        executeSyncPhases(state, phase2, phase3, phase4, phase5);
      }
      return;
    }

    AIOperation op = phase1.get(index);
    try {
      String error = validateOperation(op);
      if (error != null) {
        state.markFailed(op, error);
        state.skipRemaining(phase1, index + 1);
        state.skipAll(phase2);
        state.skipAll(phase3);
        state.skipAll(phase4);
        state.skipAll(phase5);
        state.finish();
        return;
      }

      executeProjectOp(op, new ProjectOpCallback() {
        @Override
        public void onSuccess() {
          state.markSucceeded(op);
          // Continue to next Phase 1 operation after a deferred command so the
          // UI can update.
          Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
              executePhase1(state, phase1, index + 1, phase2, phase3, phase4, phase5);
            }
          });
        }

        @Override
        public void onFailure(String message) {
          state.markFailed(op, message);
          state.skipRemaining(phase1, index + 1);
          state.skipAll(phase2);
          state.skipAll(phase3);
          state.skipAll(phase4);
          state.skipAll(phase5);
          state.finish();
        }
      });
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Phase 1 exception", e);
      state.markFailed(op, "Exception: " + e.getMessage());
      state.skipRemaining(phase1, index + 1);
      state.skipAll(phase2);
      state.skipAll(phase3);
      state.skipAll(phase4);
      state.skipAll(phase5);
      state.finish();
    }
  }

  // -----------------------------------------------------------------------
  // Phases 2-5 -- synchronous
  // -----------------------------------------------------------------------

  private void executeSyncPhases(ExecutionState state, List<AIOperation> phase2,
      List<AIOperation> phase3, List<AIOperation> phase4, List<AIOperation> phase5) {

    if (!executeSyncList(state, phase2, phase3, phase4, phase5)) {
      return; // halted inside phase 2
    }
    if (!executeSyncList(state, phase3, phase4, phase5)) {
      return; // halted inside phase 3
    }
    if (!executeSyncList(state, phase4, phase5)) {
      return; // halted inside phase 4
    }
    executeSyncList(state, phase5);
    state.finish();
  }

  /**
   * Execute the first list synchronously. On failure, skip the remaining
   * items in the first list and all subsequent lists, then finish.
   *
   * @return true if all items in the first list succeeded, false if halted.
   */
  @SuppressWarnings("unchecked")
  private boolean executeSyncList(ExecutionState state, List<AIOperation> current,
      List<AIOperation>... remaining) {
    for (int i = 0; i < current.size(); i++) {
      AIOperation op = current.get(i);
      try {
        String error = validateOperation(op);
        if (error != null) {
          state.markFailed(op, error);
          state.skipRemaining(current, i + 1);
          for (List<AIOperation> rest : remaining) {
            state.skipAll(rest);
          }
          state.finish();
          return false;
        }

        executeSyncOp(op);
        state.markSucceeded(op);
      } catch (Exception e) {
        LOG.log(Level.SEVERE, "Sync execution exception for " + op.getType(), e);
        state.markFailed(op, "Exception executing " + op.getType() + ": " + e.getMessage());
        state.skipRemaining(current, i + 1);
        for (List<AIOperation> rest : remaining) {
          state.skipAll(rest);
        }
        state.finish();
        return false;
      }
    }
    return true;
  }

  // -----------------------------------------------------------------------
  // Validation
  // -----------------------------------------------------------------------

  /**
   * Validate a single operation immediately before execution.
   *
   * @return null if valid, or an error message string.
   */
  private String validateOperation(AIOperation op) {
    if (op.getType() == null) {
      return "Operation has null type";
    }
    if (op.getPayload() == null || op.getPayload().isEmpty()) {
      return "Operation " + op.getType() + " has empty payload";
    }

    JSONObject json;
    try {
      json = JSONParser.parseStrict(op.getPayload()).isObject();
    } catch (Exception e) {
      return "Invalid JSON payload for " + op.getType() + ": " + e.getMessage();
    }
    if (json == null) {
      return "Payload for " + op.getType() + " is not a JSON object";
    }

    switch (op.getType()) {
      case SWITCH_SCREEN:
        return validateSwitchScreen(json);
      case CREATE_SCREEN:
        return validateCreateScreen(json);
      case DELETE_SCREEN:
        return validateDeleteScreen(json);
      case SET_PROJECT_PROP:
        return validateSetProjectProp(json);
      case ADD_COMPONENT:
        return validateAddComponent(json);
      case DELETE_COMPONENT:
        return validateDeleteComponent(json);
      case SET_PROPERTY:
        return validateSetProperty(json);
      case RENAME_COMPONENT:
        return validateRenameComponent(json);
      case SET_EVENT_HANDLER:
        return validateBlockOp(json, "SET_EVENT_HANDLER", true);
      case DELETE_EVENT_HANDLER:
        return validateBlockDeleteOp(json, "DELETE_EVENT_HANDLER");
      case SET_VARIABLE:
        return validateBlockOp(json, "SET_VARIABLE", true);
      case DELETE_VARIABLE:
        return validateBlockDeleteOp(json, "DELETE_VARIABLE");
      case SET_PROCEDURE:
        return validateBlockOp(json, "SET_PROCEDURE", true);
      case DELETE_PROCEDURE:
        return validateBlockDeleteOp(json, "DELETE_PROCEDURE");
      default:
        return "Unknown operation type: " + op.getType();
    }
  }

  private String validateSwitchScreen(JSONObject json) {
    String screen = getStringField(json, "screen");
    if (screen == null || screen.isEmpty()) {
      return "SWITCH_SCREEN: missing 'screen' field";
    }
    DesignProject project = getCurrentProject();
    if (project == null) {
      return "SWITCH_SCREEN: no current project";
    }
    if (!project.screens.containsKey(screen)) {
      return "SWITCH_SCREEN: screen '" + screen + "' does not exist";
    }
    return null;
  }

  private String validateCreateScreen(JSONObject json) {
    String screen = getStringField(json, "screen");
    if (screen == null || screen.isEmpty()) {
      return "CREATE_SCREEN: missing 'screen' field";
    }
    if (!TextValidators.isValidIdentifier(screen)) {
      return "CREATE_SCREEN: '" + screen + "' is not a valid identifier";
    }
    if (TextValidators.isReservedName(screen)) {
      return "CREATE_SCREEN: '" + screen + "' is a reserved name";
    }
    DesignProject project = getCurrentProject();
    if (project == null) {
      return "CREATE_SCREEN: no current project";
    }
    if (project.screens.containsKey(screen)) {
      return "CREATE_SCREEN: screen '" + screen + "' already exists";
    }
    return null;
  }

  private String validateDeleteScreen(JSONObject json) {
    String screen = getStringField(json, "screen");
    if (screen == null || screen.isEmpty()) {
      return "DELETE_SCREEN: missing 'screen' field";
    }
    if ("Screen1".equals(screen)) {
      return "DELETE_SCREEN: cannot delete Screen1";
    }
    DesignProject project = getCurrentProject();
    if (project == null) {
      return "DELETE_SCREEN: no current project";
    }
    if (!project.screens.containsKey(screen)) {
      return "DELETE_SCREEN: screen '" + screen + "' does not exist";
    }
    return null;
  }

  private String validateSetProjectProp(JSONObject json) {
    String category = getStringField(json, "category");
    String name = getStringField(json, "name");
    String value = getStringField(json, "value");
    if (category == null || category.isEmpty()) {
      return "SET_PROJECT_PROP: missing 'category' field";
    }
    if (name == null || name.isEmpty()) {
      return "SET_PROJECT_PROP: missing 'name' field";
    }
    if (value == null) {
      return "SET_PROJECT_PROP: missing 'value' field";
    }
    return null;
  }

  private String validateAddComponent(JSONObject json) {
    String type = getStringField(json, "type");
    String name = getStringField(json, "name");
    if (type == null || type.isEmpty()) {
      return "ADD_COMPONENT: missing 'type' field";
    }
    if (name == null || name.isEmpty()) {
      return "ADD_COMPONENT: missing 'name' field";
    }
    if (!TextValidators.isValidIdentifier(name)) {
      return "ADD_COMPONENT: '" + name + "' is not a valid identifier";
    }

    // Check that the component type is known.
    YaFormEditor formEditor = getCurrentFormEditor();
    if (formEditor == null) {
      return "ADD_COMPONENT: no form editor available for current screen";
    }
    SimpleComponentDatabase db = formEditor.getComponentDatabase();
    try {
      db.getComponentType(type);
    } catch (Exception e) {
      return "ADD_COMPONENT: unknown component type '" + type + "'";
    }

    // Check that name is not already in use.
    Map<String, MockComponent> components = formEditor.getComponents();
    if (components.containsKey(name)) {
      return "ADD_COMPONENT: component '" + name + "' already exists";
    }
    return null;
  }

  private String validateDeleteComponent(JSONObject json) {
    String name = getStringField(json, "name");
    if (name == null || name.isEmpty()) {
      return "DELETE_COMPONENT: missing 'name' field";
    }

    YaFormEditor formEditor = getCurrentFormEditor();
    if (formEditor == null) {
      return "DELETE_COMPONENT: no form editor available for current screen";
    }
    Map<String, MockComponent> components = formEditor.getComponents();
    if (!components.containsKey(name)) {
      return "DELETE_COMPONENT: component '" + name + "' does not exist";
    }
    MockComponent comp = components.get(name);
    if (comp instanceof MockForm) {
      return "DELETE_COMPONENT: cannot delete the Form component";
    }
    return null;
  }

  private String validateSetProperty(JSONObject json) {
    String component = getStringField(json, "component");
    String property = getStringField(json, "property");
    if (component == null || component.isEmpty()) {
      return "SET_PROPERTY: missing 'component' field";
    }
    if (property == null || property.isEmpty()) {
      return "SET_PROPERTY: missing 'property' field";
    }
    if (!json.containsKey("value")) {
      return "SET_PROPERTY: missing 'value' field";
    }

    YaFormEditor formEditor = getCurrentFormEditor();
    if (formEditor == null) {
      return "SET_PROPERTY: no form editor available for current screen";
    }
    Map<String, MockComponent> components = formEditor.getComponents();
    if (!components.containsKey(component)) {
      return "SET_PROPERTY: component '" + component + "' does not exist";
    }
    return null;
  }

  private String validateRenameComponent(JSONObject json) {
    String oldName = getStringField(json, "oldName");
    String newName = getStringField(json, "newName");
    if (oldName == null || oldName.isEmpty()) {
      return "RENAME_COMPONENT: missing 'oldName' field";
    }
    if (newName == null || newName.isEmpty()) {
      return "RENAME_COMPONENT: missing 'newName' field";
    }
    if (!TextValidators.isValidIdentifier(newName)) {
      return "RENAME_COMPONENT: '" + newName + "' is not a valid identifier";
    }

    YaFormEditor formEditor = getCurrentFormEditor();
    if (formEditor == null) {
      return "RENAME_COMPONENT: no form editor available for current screen";
    }
    Map<String, MockComponent> components = formEditor.getComponents();
    if (!components.containsKey(oldName)) {
      return "RENAME_COMPONENT: component '" + oldName + "' does not exist";
    }
    if (components.containsKey(newName)) {
      return "RENAME_COMPONENT: component '" + newName + "' already exists";
    }
    return null;
  }

  /**
   * Validates a block-creation operation (SET_EVENT_HANDLER, SET_VARIABLE,
   * SET_PROCEDURE). These require a "blocksXml" field.
   */
  private String validateBlockOp(JSONObject json, String opName, boolean requiresXml) {
    if (requiresXml) {
      String xml = getStringField(json, "blocksXml");
      if (xml == null || xml.isEmpty()) {
        return opName + ": missing 'blocksXml' field";
      }
    }
    YaBlocksEditor blocksEditor = getCurrentBlocksEditor();
    if (blocksEditor == null) {
      return opName + ": no blocks editor available for current screen";
    }
    return null;
  }

  /**
   * Validates a block-deletion operation (DELETE_EVENT_HANDLER,
   * DELETE_VARIABLE, DELETE_PROCEDURE).
   */
  private String validateBlockDeleteOp(JSONObject json, String opName) {
    YaBlocksEditor blocksEditor = getCurrentBlocksEditor();
    if (blocksEditor == null) {
      return opName + ": no blocks editor available for current screen";
    }
    // The identifier field depends on the type; at minimum we need
    // something to identify the block.
    return null;
  }

  // -----------------------------------------------------------------------
  // Phase 1 execution helpers -- project-level (async)
  // -----------------------------------------------------------------------

  private interface ProjectOpCallback {
    void onSuccess();
    void onFailure(String message);
  }

  private void executeProjectOp(AIOperation op, ProjectOpCallback callback) {
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
      default:
        callback.onFailure("Unexpected project-level op type: " + op.getType());
    }
  }

  private void executeSwitchScreen(JSONObject json, final ProjectOpCallback callback) {
    String screen = getStringField(json, "screen");
    long projectId = Ode.getInstance().getCurrentYoungAndroidProjectId();
    DesignToolbar toolbar = Ode.getInstance().getDesignToolbar();
    try {
      toolbar.switchToScreen(projectId, screen, DesignToolbar.View.DESIGNER);
      // Screen switch is scheduled via deferred command inside DesignToolbar.
      // Wait for the screens-locked flag to clear before continuing.
      waitForScreenReady(callback);
    } catch (Exception e) {
      callback.onFailure("SWITCH_SCREEN: " + e.getMessage());
    }
  }

  private void executeCreateScreen(JSONObject json, final ProjectOpCallback callback) {
    final String screenName = getStringField(json, "screen");
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

            // Add the new form and blocks nodes to the project.
            project.addNode(packageNode, new YoungAndroidFormNode(formFileId));
            project.addNode(packageNode, new YoungAndroidBlocksNode(blocksFileId));

            // Wait for the screen to appear in the design toolbar.
            waitForScreenAvailable(projectId, screenName, new Runnable() {
              @Override
              public void run() {
                // Switch to the newly created screen.
                DesignToolbar toolbar = ode.getDesignToolbar();
                toolbar.switchToScreen(projectId, screenName, DesignToolbar.View.DESIGNER);
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

  private void executeDeleteScreen(JSONObject json, final ProjectOpCallback callback) {
    final String screenName = getStringField(json, "screen");
    final Ode ode = Ode.getInstance();
    final long projectId = ode.getCurrentYoungAndroidProjectId();
    final Project project = ode.getProjectManager().getProject(projectId);

    DesignProject designProject = getCurrentProject();
    if (designProject == null) {
      callback.onFailure("DELETE_SCREEN: no current project");
      return;
    }

    Screen screen = designProject.screens.get(screenName);
    if (screen == null) {
      callback.onFailure("DELETE_SCREEN: screen '" + screenName + "' not found");
      return;
    }

    // Build file IDs for form and blocks.
    String formFileId = screen.designerEditor.getFileId();
    String blocksFileId = screen.blocksEditor.getFileId();

    // Close editors.
    ode.getEditorManager().closeFileEditors(projectId,
        new String[]{formFileId, blocksFileId});

    // Delete on server.
    ode.getProjectService().deleteFile(ode.getSessionId(), projectId, formFileId,
        new OdeAsyncCallback<Long>("Error deleting screen") {
          @Override
          public void onSuccess(Long modDate) {
            ode.updateModificationDate(projectId, modDate);

            // Remove nodes from the project model.
            for (com.google.appinventor.shared.rpc.project.ProjectNode sourceNode
                : project.getRootNode().getAllSourceNodes()) {
              String fid = sourceNode.getFileId();
              if (fid.contains("/" + screenName + ".scm")
                  || fid.contains("/" + screenName + ".bky")
                  || fid.contains("/" + screenName + ".yail")) {
                project.deleteNode(sourceNode);
              }
            }

            // Remove from the DesignToolbar.
            ode.getDesignToolbar().removeScreen(projectId, screenName);
            callback.onSuccess();
          }

          @Override
          public void onFailure(Throwable caught) {
            callback.onFailure("DELETE_SCREEN: server error: " + caught.getMessage());
          }
        });
  }

  private void executeSetProjectProp(JSONObject json, ProjectOpCallback callback) {
    String category = getStringField(json, "category");
    String name = getStringField(json, "name");
    String value = getStringField(json, "value");

    try {
      long projectId = Ode.getInstance().getCurrentYoungAndroidProjectId();
      ProjectEditor projectEditor =
          Ode.getInstance().getEditorManager().getOpenProjectEditor(projectId);
      if (projectEditor == null) {
        callback.onFailure("SET_PROJECT_PROP: no open project editor");
        return;
      }
      projectEditor.changeProjectSettingsProperty(category, name, value);
      callback.onSuccess();
    } catch (Exception e) {
      callback.onFailure("SET_PROJECT_PROP: " + e.getMessage());
    }
  }

  // -----------------------------------------------------------------------
  // Sync operation dispatcher
  // -----------------------------------------------------------------------

  private void executeSyncOp(AIOperation op) {
    JSONObject json = JSONParser.parseStrict(op.getPayload()).isObject();

    switch (op.getType()) {
      case ADD_COMPONENT:
        executeAddComponent(json);
        break;
      case SET_PROPERTY:
        executeSetProperty(json);
        break;
      case RENAME_COMPONENT:
        executeRenameComponent(json);
        break;
      case DELETE_COMPONENT:
        executeDeleteComponent(json);
        break;
      case SET_EVENT_HANDLER:
        executeSetEventHandler(json);
        break;
      case DELETE_EVENT_HANDLER:
        executeDeleteEventHandler(json);
        break;
      case SET_VARIABLE:
        executeSetVariable(json);
        break;
      case DELETE_VARIABLE:
        executeDeleteVariable(json);
        break;
      case SET_PROCEDURE:
        executeSetProcedure(json);
        break;
      case DELETE_PROCEDURE:
        executeDeleteProcedure(json);
        break;
      default:
        throw new IllegalStateException("Unexpected sync op type: " + op.getType());
    }
  }

  // -----------------------------------------------------------------------
  // Phase 2 -- Designer adds
  // -----------------------------------------------------------------------

  private void executeAddComponent(JSONObject json) {
    String type = getStringField(json, "type");
    String name = getStringField(json, "name");
    String parent = getStringField(json, "parent");

    YaFormEditor formEditor = getCurrentFormEditor();
    MockForm form = formEditor.getForm();

    // Determine the container to add to.
    MockContainer container;
    if (parent != null && !parent.isEmpty()) {
      Map<String, MockComponent> components = formEditor.getComponents();
      MockComponent parentComp = components.get(parent);
      if (parentComp instanceof MockContainer) {
        container = (MockContainer) parentComp;
      } else {
        // Fallback to the form itself.
        container = form;
      }
    } else {
      container = form;
    }

    // Build a minimal JSON properties object in the format expected by
    // DesignerEditor.createMockComponent():
    //   { "$Type": "Button", "$Name": "Button1", "$Version": "7", ... }
    // We use the App Inventor shared JSON types (ClientJsonParser / ClientJsonString)
    // because that is what DesignerEditor.createMockComponent expects.
    SimpleComponentDatabase db = formEditor.getComponentDatabase();
    StringBuilder sb = new StringBuilder();
    sb.append("{\"$Type\":\"").append(type).append("\",");
    sb.append("\"$Name\":\"").append(name).append("\",");
    sb.append("\"$Version\":\"").append(db.getComponentVersion(type)).append("\"");

    // Append any initial property values from the payload.
    if (json.containsKey("properties")) {
      JSONValue propsVal = json.get("properties");
      if (propsVal.isObject() != null) {
        JSONObject props = propsVal.isObject();
        for (String propName : props.keySet()) {
          JSONValue propValue = props.get(propName);
          String valueStr = (propValue.isString() != null)
              ? propValue.isString().stringValue()
              : propValue.toString();
          // Escape the value for JSON string embedding.
          sb.append(",\"").append(propName).append("\":\"")
              .append(escapeJsonString(valueStr)).append("\"");
        }
      }
    }
    sb.append("}");

    // Parse via the shared JSON parser (ClientJsonParser) and delegate to
    // YaFormEditor.addMockComponent which wraps DesignerEditor.createMockComponent.
    ClientJsonParser sharedParser = new ClientJsonParser();
    com.google.appinventor.shared.properties.json.JSONObject propertiesObject =
        sharedParser.parse(sb.toString()).asObject();
    formEditor.addMockComponent(propertiesObject, container);
  }

  private void executeSetProperty(JSONObject json) {
    String component = getStringField(json, "component");
    String property = getStringField(json, "property");
    JSONValue valueJson = json.get("value");
    String value = (valueJson.isString() != null)
        ? valueJson.isString().stringValue()
        : valueJson.toString();

    YaFormEditor formEditor = getCurrentFormEditor();
    Map<String, MockComponent> components = formEditor.getComponents();
    MockComponent comp = components.get(component);
    comp.changeProperty(property, value);
  }

  private void executeRenameComponent(JSONObject json) {
    String oldName = getStringField(json, "oldName");
    String newName = getStringField(json, "newName");

    YaFormEditor formEditor = getCurrentFormEditor();
    Map<String, MockComponent> components = formEditor.getComponents();
    MockComponent comp = components.get(oldName);
    comp.rename(newName);
  }

  // -----------------------------------------------------------------------
  // Phase 3 -- Block adds (SET_EVENT_HANDLER, SET_VARIABLE, SET_PROCEDURE)
  // -----------------------------------------------------------------------

  private void executeSetEventHandler(JSONObject json) {
    String blocksXml = getStringField(json, "blocksXml");
    String componentName = getStringField(json, "component");
    String eventName = getStringField(json, "event");

    YaBlocksEditor blocksEditor = getCurrentBlocksEditor();
    // Use replaceBlock to support create-or-replace semantics.
    if (componentName != null && eventName != null) {
      blocksEditor.replaceBlock("component_event", componentName, eventName, blocksXml);
    } else {
      blocksEditor.injectBlocksXml(blocksXml);
    }
  }

  private void executeSetVariable(JSONObject json) {
    String blocksXml = getStringField(json, "blocksXml");
    String varName = getStringField(json, "name");

    YaBlocksEditor blocksEditor = getCurrentBlocksEditor();
    if (varName != null) {
      blocksEditor.replaceBlock("global_declaration", "", varName, blocksXml);
    } else {
      blocksEditor.injectBlocksXml(blocksXml);
    }
  }

  private void executeSetProcedure(JSONObject json) {
    String blocksXml = getStringField(json, "blocksXml");
    String procName = getStringField(json, "name");

    YaBlocksEditor blocksEditor = getCurrentBlocksEditor();
    if (procName != null) {
      // Procedures can be either "procedures_defnoreturn" or "procedures_defreturn".
      // Try the no-return variant first; the replaceBlock method will silently
      // skip if the block doesn't exist.
      boolean hasReturn = json.containsKey("hasReturn")
          && "true".equals(getStringField(json, "hasReturn"));
      String blockType = hasReturn ? "procedures_defreturn" : "procedures_defnoreturn";
      blocksEditor.replaceBlock(blockType, "", procName, blocksXml);
    } else {
      blocksEditor.injectBlocksXml(blocksXml);
    }
  }

  // -----------------------------------------------------------------------
  // Phase 4 -- Block deletes
  // -----------------------------------------------------------------------

  private void executeDeleteEventHandler(JSONObject json) {
    String componentName = getStringField(json, "component");
    String eventName = getStringField(json, "event");

    YaBlocksEditor blocksEditor = getCurrentBlocksEditor();
    if (componentName != null && eventName != null) {
      blocksEditor.deleteBlock("component_event", componentName, eventName);
    }
  }

  private void executeDeleteVariable(JSONObject json) {
    String varName = getStringField(json, "name");

    YaBlocksEditor blocksEditor = getCurrentBlocksEditor();
    if (varName != null) {
      blocksEditor.deleteBlock("global_declaration", "", varName);
    }
  }

  private void executeDeleteProcedure(JSONObject json) {
    String procName = getStringField(json, "name");

    YaBlocksEditor blocksEditor = getCurrentBlocksEditor();
    if (procName != null) {
      // Try both procedure types.
      boolean deleted = blocksEditor.deleteBlock("procedures_defnoreturn", "", procName);
      if (!deleted) {
        blocksEditor.deleteBlock("procedures_defreturn", "", procName);
      }
    }
  }

  // -----------------------------------------------------------------------
  // Phase 5 -- Designer deletes
  // -----------------------------------------------------------------------

  private void executeDeleteComponent(JSONObject json) {
    String name = getStringField(json, "name");

    YaFormEditor formEditor = getCurrentFormEditor();
    Map<String, MockComponent> components = formEditor.getComponents();
    MockComponent comp = components.get(name);
    comp.delete();
  }

  // -----------------------------------------------------------------------
  // Utility: editor access helpers
  // -----------------------------------------------------------------------

  private DesignProject getCurrentProject() {
    DesignToolbar toolbar = Ode.getInstance().getDesignToolbar();
    return toolbar.getCurrentProject();
  }

  /**
   * Returns the YaFormEditor for the currently active screen.
   */
  private YaFormEditor getCurrentFormEditor() {
    DesignProject project = getCurrentProject();
    if (project == null) {
      return null;
    }
    Screen screen = project.screens.get(project.currentScreen);
    if (screen == null) {
      return null;
    }
    FileEditor editor = screen.designerEditor;
    if (editor instanceof YaFormEditor) {
      return (YaFormEditor) editor;
    }
    return null;
  }

  /**
   * Returns the YaBlocksEditor for the currently active screen.
   */
  private YaBlocksEditor getCurrentBlocksEditor() {
    DesignProject project = getCurrentProject();
    if (project == null) {
      return null;
    }
    Screen screen = project.screens.get(project.currentScreen);
    if (screen == null) {
      return null;
    }
    FileEditor editor = screen.blocksEditor;
    if (editor instanceof YaBlocksEditor) {
      return (YaBlocksEditor) editor;
    }
    return null;
  }

  // -----------------------------------------------------------------------
  // Utility: JSON helpers
  // -----------------------------------------------------------------------

  /**
   * Extracts a string value from a JSONObject field. Returns null if the
   * field is absent or not a string.
   */
  private static String getStringField(JSONObject json, String field) {
    if (!json.containsKey(field)) {
      return null;
    }
    JSONValue val = json.get(field);
    if (val.isString() != null) {
      return val.isString().stringValue();
    }
    // Tolerate non-string values by converting to string.
    return val.toString();
  }

  /**
   * Escapes a string for safe embedding inside a JSON string literal.
   */
  private static String escapeJsonString(String raw) {
    if (raw == null) {
      return "";
    }
    StringBuilder sb = new StringBuilder(raw.length());
    for (int i = 0; i < raw.length(); i++) {
      char c = raw.charAt(i);
      switch (c) {
        case '"':
          sb.append("\\\"");
          break;
        case '\\':
          sb.append("\\\\");
          break;
        case '\n':
          sb.append("\\n");
          break;
        case '\r':
          sb.append("\\r");
          break;
        case '\t':
          sb.append("\\t");
          break;
        default:
          sb.append(c);
      }
    }
    return sb.toString();
  }

  // -----------------------------------------------------------------------
  // Utility: async waiting helpers
  // -----------------------------------------------------------------------

  /**
   * Waits until screens are no longer locked, then calls the callback.
   */
  private void waitForScreenReady(final ProjectOpCallback callback) {
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

  /**
   * Waits until a screen becomes available in the DesignToolbar, then runs
   * the continuation.
   */
  private void waitForScreenAvailable(final long projectId, final String screenName,
      final Runnable then) {
    Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
      @Override
      public void execute() {
        DesignToolbar toolbar = Ode.getInstance().getDesignToolbar();
        DesignProject project = toolbar.projectMap.get(projectId);
        if (project != null && project.screens.containsKey(screenName)
            && !Ode.getInstance().screensLocked()) {
          then.run();
        } else {
          Scheduler.get().scheduleDeferred(this);
        }
      }
    });
  }
}
