// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.aiagent.executor;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.youngandroid.YaBlocksEditor;
import com.google.appinventor.client.editor.youngandroid.aiagent.AIJsonUtils;
import com.google.appinventor.client.editor.youngandroid.aiagent.validator.AIOperationValidator;
import com.google.appinventor.shared.rpc.aiagent.AIOperation;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Applies AI-generated operations to the current project editor.
 *
 * <p>Operations are grouped into five execution phases to ensure that
 * dependencies between operation types are respected:
 * <ol>
 *   <li>Phase 1 (async): Project-level &mdash; SWITCH_SCREEN,
 *       CREATE_SCREEN, DELETE_SCREEN, SET_PROJECT_PROP,
 *       TOGGLE_EDITOR</li>
 *   <li>Phase 2 (sync): Designer additions &mdash; ADD_COMPONENT,
 *       SET_PROPERTY, RENAME_COMPONENT</li>
 *   <li>Phase 3 (sync): Block additions &mdash; WRITE_BLOCK</li>
 *   <li>Phase 4 (sync): Block deletions &mdash; DELETE_BLOCK</li>
 *   <li>Phase 5 (sync): Designer deletions &mdash; DELETE_COMPONENT</li>
 * </ol>
 *
 * <p>Each operation is validated immediately before execution (not all at
 * once up front) because earlier operations change editor state. On the
 * first failure the executor halts; remaining operations are reported as
 * skipped. There is no rollback.</p>
 */
public class AIOperationExecutor {

  private static final Logger LOG = Logger.getLogger(AIOperationExecutor.class.getName());

  private ScreenExecutionContext context;

  // ---- Public types ----

  /**
   * Result returned after executing a batch of AI operations.
   */
  public static class ExecutionResult {
    private final List<AIOperation> succeeded;
    private final List<AIOperation> failed;
    private final List<AIOperation> skipped;
    private final String errorMessage;

    ExecutionResult(List<AIOperation> succeeded, List<AIOperation> failed,
        List<AIOperation> skipped, String errorMessage) {
      this.succeeded = succeeded;
      this.failed = failed;
      this.skipped = skipped;
      this.errorMessage = errorMessage;
    }

    public List<AIOperation> getSucceeded() { return succeeded; }
    public List<AIOperation> getFailed() { return failed; }
    public List<AIOperation> getSkipped() { return skipped; }
    public String getErrorMessage() { return errorMessage; }
    public boolean isSuccess() { return failed.isEmpty(); }
  }

  /**
   * Callback invoked when all phases have completed (or an error halts
   * execution).
   */
  public interface ExecutionCallback {
    void onComplete(ExecutionResult result);
  }

  // ---- Public API ----

  /**
   * Execute a list of AI operations against the current editor.
   * Operations are grouped into phases and executed in order.
   */
  public void execute(List<AIOperation> operations, ExecutionCallback callback) {
    this.context = ScreenExecutionContext.forCurrentScreen();

    List<AIOperation> phase1 = new ArrayList<>();
    List<AIOperation> phase2 = new ArrayList<>();
    List<AIOperation> phase3 = new ArrayList<>();
    List<AIOperation> phase4 = new ArrayList<>();
    List<AIOperation> phase5 = new ArrayList<>();

    for (AIOperation op : operations) {
      switch (op.getType()) {
        case SWITCH_SCREEN:
        case CREATE_SCREEN:
        case DELETE_SCREEN:
        case SET_PROJECT_PROP:
        case TOGGLE_EDITOR:
          phase1.add(op);
          break;
        case ADD_COMPONENT:
        case SET_PROPERTY:
        case RENAME_COMPONENT:
          phase2.add(op);
          break;
        case WRITE_BLOCK:
          phase3.add(op);
          break;
        case DELETE_BLOCK:
          phase4.add(op);
          break;
        case DELETE_COMPONENT:
          phase5.add(op);
          break;
        default:
          LOG.warning("Unknown operation type: " + op.getType());
          break;
      }
    }

    ExecutionState state = new ExecutionState(callback);
    runPhase1(state, phase1, 0, phase2, phase3, phase4, phase5);
  }

  /**
   * Execute a list of AI operations against a specific screen (phases 2-5 only).
   * Phase 1 (project-level) operations are not supported — they must be handled
   * by the orchestration layer before dispatching to individual screens.
   */
  public static void executeForScreen(ScreenExecutionContext context,
      List<AIOperation> operations, ExecutionCallback callback) {
    AIOperationExecutor executor = new AIOperationExecutor();
    executor.context = context;

    List<AIOperation> phase2 = new ArrayList<>();
    List<AIOperation> phase3 = new ArrayList<>();
    List<AIOperation> phase4 = new ArrayList<>();
    List<AIOperation> phase5 = new ArrayList<>();

    for (AIOperation op : operations) {
      switch (op.getType()) {
        case ADD_COMPONENT:
        case SET_PROPERTY:
        case RENAME_COMPONENT:
          phase2.add(op);
          break;
        case WRITE_BLOCK:
          phase3.add(op);
          break;
        case DELETE_BLOCK:
          phase4.add(op);
          break;
        case DELETE_COMPONENT:
          phase5.add(op);
          break;
        default:
          LOG.warning("Unsupported operation type for screen-targeted execution: "
              + op.getType());
          break;
      }
    }

    ExecutionState state = new ExecutionState(callback);
    executor.runSyncPhases(state, phase2, phase3, phase4, phase5);
  }

  // ---- Execution state ----

  private static class ExecutionState {
    final ExecutionCallback callback;
    final List<AIOperation> succeeded = new ArrayList<>();
    final List<AIOperation> failed = new ArrayList<>();
    final List<AIOperation> skipped = new ArrayList<>();
    String errorMessage;
    boolean halted;

    ExecutionState(ExecutionCallback callback) {
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

    void skipRemaining(List<AIOperation> ops, int startIndex) {
      for (int i = startIndex; i < ops.size(); i++) {
        skipped.add(ops.get(i));
      }
    }

    void skipAll(List<AIOperation> ops) {
      skipped.addAll(ops);
    }

    void finish() {
      callback.onComplete(
          new ExecutionResult(succeeded, failed, skipped, errorMessage));
    }
  }

  // ---- Phase 1: async project-level operations ----

  private void runPhase1(final ExecutionState state, final List<AIOperation> phase1,
      final int index, final List<AIOperation> phase2, final List<AIOperation> phase3,
      final List<AIOperation> phase4, final List<AIOperation> phase5) {

    if (state.halted || index >= phase1.size()) {
      if (state.halted) {
        state.skipRemaining(phase1, index);
        state.skipAll(phase2);
        state.skipAll(phase3);
        state.skipAll(phase4);
        state.skipAll(phase5);
        state.finish();
      } else {
        runSyncPhases(state, phase2, phase3, phase4, phase5);
      }
      return;
    }

    final AIOperation op = phase1.get(index);
    if (isIdempotentSkip(op)) {
      LOG.fine("Idempotent skip: " + op.getType() + " already applied");
      state.markSucceeded(op);
      runPhase1(state, phase1, index + 1, phase2, phase3, phase4, phase5);
      return;
    }
    try {
      String error = AIOperationValidator.validate(op, context);
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

      AIProjectOperations.execute(op, new AIProjectOperations.ProjectOpCallback() {
        @Override
        public void onSuccess() {
          state.markSucceeded(op);
          Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
              runPhase1(state, phase1, index + 1, phase2, phase3, phase4, phase5);
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

  // ---- Phases 2-5: synchronous ----

  private void runSyncPhases(ExecutionState state, List<AIOperation> phase2,
      List<AIOperation> phase3, List<AIOperation> phase4, List<AIOperation> phase5) {
    try {
      if (!runSyncList(state, phase2, Arrays.asList(phase3, phase4, phase5))) {
        return;
      }
      // Before writing new blocks (phase 3), tell the positioning algorithm
      // which blocks will be removed (phase 4 deletions) or replaced by
      // upserts (phase 3 writes targeting existing blocks) so new block
      // positioning ignores them.  This prevents gaps where large blocks
      // are about to shrink or disappear.
      setPendingBlockDeletions(phase4);
      addPendingBlockUpserts(phase3);
      try {
        if (!runSyncList(state, phase3, Arrays.asList(phase4, phase5))) {
          return;
        }
        if (!runSyncList(state, phase4, Collections.singletonList(phase5))) {
          return;
        }
      } finally {
        clearPendingBlockDeletions();
      }
      runSyncList(state, phase5, Collections.<List<AIOperation>>emptyList());
      state.finish();
    } finally {
      // AI.YailToBlocks.convert() disables Blockly events during block
      // creation (for performance and to avoid mid-mutation crashes), so
      // workspace change listeners never fire and the editor is never
      // marked dirty.  We must explicitly schedule a save for any editor
      // that was modified.
      //
      // For the current screen we also force a Companion YAIL update via
      // sendComponentData — rename() and WRITE_BLOCK suppress normal
      // update triggers, so an explicit push ensures the Companion
      // receives the latest component definitions and block code.
      // Background screens have no live Companion connection.
      if (context.isCurrentScreen()) {
        YaBlocksEditor blocksEditor = context.getBlocksEditor();
        if (blocksEditor != null) {
          blocksEditor.sendComponentData(true);
        }
      }
      // Mark both editors dirty so auto-save persists the changes —
      // critical for background editors where no Blockly events fired.
      Ode.getInstance().getEditorManager().scheduleAutoSave(context.getBlocksEditor());
      Ode.getInstance().getEditorManager().scheduleAutoSave(context.getFormEditor());
    }
  }

  private boolean runSyncList(ExecutionState state, List<AIOperation> current,
      List<List<AIOperation>> remaining) {
    for (int i = 0; i < current.size(); i++) {
      AIOperation op = current.get(i);
      if (isIdempotentSkip(op)) {
        LOG.fine("Idempotent skip: " + op.getType() + " already applied");
        state.markSucceeded(op);
        continue;
      }
      try {
        String error = AIOperationValidator.validate(op, context);
        if (error != null) {
          state.markFailed(op, error);
          state.skipRemaining(current, i + 1);
          for (List<AIOperation> rest : remaining) {
            state.skipAll(rest);
          }
          state.finish();
          return false;
        }

        dispatchSyncOp(op);
        state.markSucceeded(op);
      } catch (Exception e) {
        LOG.log(Level.SEVERE, "Sync execution exception for " + op.getType(), e);
        state.markFailed(op,
            "Exception executing " + op.getType() + ": " + e.getMessage());
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

  /**
   * Returns {@code true} if the given operation has already been applied and
   * can therefore be silently skipped.  This prevents cascading failures when
   * the LLM retries a tool-call batch that partially succeeded.
   */
  private boolean isIdempotentSkip(AIOperation op) {
    JSONObject json = JSONParser.parseStrict(op.getPayload()).isObject();

    switch (op.getType()) {
      case ADD_COMPONENT: {
        String name = json.get("name").isString().stringValue();
        return context.componentExists(name);
      }
      case DELETE_COMPONENT: {
        String name = json.get("name").isString().stringValue();
        return !context.componentExists(name);
      }
      case RENAME_COMPONENT: {
        String oldName = json.get("old_name").isString().stringValue();
        String newName = json.get("new_name").isString().stringValue();
        boolean oldExists = context.componentExists(oldName);
        boolean newExists = context.componentExists(newName);
        return !oldExists && newExists;
      }
      case DELETE_BLOCK: {
        String block = json.get("block").isString().stringValue();
        return !context.blockExists(block);
      }
      case CREATE_SCREEN: {
        String screenName = json.get("screen_name").isString().stringValue();
        return context.screenExists(screenName);
      }
      case DELETE_SCREEN: {
        String screenName = json.get("screen_name").isString().stringValue();
        return !context.screenExists(screenName);
      }
      default:
        return false;
    }
  }

  private void dispatchSyncOp(AIOperation op) {
    JSONObject json = JSONParser.parseStrict(op.getPayload()).isObject();

    switch (op.getType()) {
      case ADD_COMPONENT:
        AIDesignerOperations.executeAddComponent(json, context);
        break;
      case SET_PROPERTY:
        AIDesignerOperations.executeSetProperty(json, context);
        break;
      case RENAME_COMPONENT:
        AIDesignerOperations.executeRenameComponent(json, context);
        break;
      case DELETE_COMPONENT:
        AIDesignerOperations.executeDeleteComponent(json, context);
        break;
      case WRITE_BLOCK:
        AIBlockOperations.executeWriteBlock(json, context);
        break;
      case DELETE_BLOCK:
        AIBlockOperations.executeDeleteBlock(json, context);
        break;
      default:
        throw new IllegalStateException("Unexpected sync op type: " + op.getType());
    }
  }

  /**
   * Collects DELETE_BLOCK identifiers from the given phase and notifies
   * the block positioning algorithm to ignore those blocks.
   */
  private void setPendingBlockDeletions(List<AIOperation> deleteOps) {
    if (deleteOps.isEmpty()) {
      return;
    }
    YaBlocksEditor blocksEditor = context.getBlocksEditor();
    if (blocksEditor == null) {
      return;
    }
    JSONArray arr = new JSONArray();
    int index = 0;
    for (AIOperation op : deleteOps) {
      JSONObject json = JSONParser.parseStrict(op.getPayload()).isObject();
      String block = AIJsonUtils.getStringField(json, "block");
      if (block != null) {
        arr.set(index++, new JSONString(block));
      }
    }
    if (index > 0) {
      blocksEditor.setPendingDeletions(arr.toString());
    }
  }

  /**
   * Identifies WRITE_BLOCK operations that will upsert (replace) existing
   * blocks and adds them to the pending set so their old dimensions are
   * ignored during positioning.
   */
  private void addPendingBlockUpserts(List<AIOperation> writeOps) {
    if (writeOps.isEmpty()) {
      return;
    }
    YaBlocksEditor blocksEditor = context.getBlocksEditor();
    if (blocksEditor == null) {
      return;
    }
    JSONArray arr = new JSONArray();
    int index = 0;
    for (AIOperation op : writeOps) {
      JSONObject json = JSONParser.parseStrict(op.getPayload()).isObject();
      String yail = AIJsonUtils.getStringField(json, "yail");
      if (yail != null) {
        arr.set(index++, new JSONString(yail));
      }
    }
    if (index > 0) {
      blocksEditor.addPendingUpserts(arr.toString());
    }
  }

  private void clearPendingBlockDeletions() {
    YaBlocksEditor blocksEditor = context.getBlocksEditor();
    if (blocksEditor != null) {
      blocksEditor.clearPendingDeletions();
    }
  }
}
