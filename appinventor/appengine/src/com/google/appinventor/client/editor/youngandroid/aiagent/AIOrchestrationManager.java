// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.aiagent;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.ProjectEditor;
import com.google.appinventor.client.editor.blocks.BlocksEditor;
import com.google.appinventor.client.editor.designer.DesignerEditor;
import com.google.appinventor.client.editor.youngandroid.YaBlocksEditor;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;
import com.google.appinventor.client.editor.youngandroid.YaProjectEditor;
import com.google.appinventor.client.editor.youngandroid.aiagent.executor.AIOperationExecutor;
import com.google.appinventor.client.editor.youngandroid.aiagent.executor.ScreenExecutionContext;
import com.google.appinventor.shared.rpc.aiagent.AIAgentResponse;
import com.google.appinventor.shared.rpc.aiagent.AIAgentService;
import com.google.appinventor.shared.rpc.aiagent.AIAgentServiceAsync;
import com.google.appinventor.shared.rpc.aiagent.AIOperation;
import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Timer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Coordinates N parallel child conversations during multi-agent
 * orchestration of an approved plan.
 *
 * <p>The lifecycle is:
 * <ol>
 *   <li>{@link #executePlan(String, AIResponseOrchestrator.ChatCallback)}
 *       parses the plan JSON, groups steps by screen, and spawns one
 *       {@link ChildConversation} per screen.</li>
 *   <li>Each child reports batches via {@link ChildConversation.BatchCallback#onBatchReady},
 *       which are queued in FIFO order.</li>
 *   <li>{@link #presentNextBatch()} shows the front-of-queue batch to the
 *       user via {@link AIResponseOrchestrator.ChatCallback#showOperationPreview}.</li>
 *   <li>{@link #approveBatch()} applies the batch, signals the child to
 *       continue, and presents the next queued batch.</li>
 *   <li>{@link #rejectBatch(String)} cancels all children, clears the queue,
 *       and returns context to the parent chat.</li>
 *   <li>{@link #setAutoAcceptAll()} auto-approves the current and all future
 *       batches.</li>
 * </ol>
 */
public class AIOrchestrationManager implements ChildConversation.BatchCallback {

  private static final Logger LOG = Logger.getLogger(AIOrchestrationManager.class.getName());

  /** Maximum number of concurrent child conversations. */
  private static final int MAX_CHILDREN = 5;

  /** Maximum total RPCs allowed per plan execution. */
  private static final int MAX_RPCS_PER_PLAN = 20;

  /** Polling interval when waiting for editors to become ready (ms). */
  private static final int EDITOR_READY_POLL_MS = 100;

  /** Maximum time to wait for editors to become ready (ms). */
  private static final int EDITOR_READY_TIMEOUT_MS = 10000;

  /**
   * A batch of operations from a child conversation, waiting to be
   * presented to the user and applied.
   */
  private static class PendingBatch {
    final ChildConversation child;
    final AIAgentResponse response;

    PendingBatch(ChildConversation child, AIAgentResponse response) {
      this.child = child;
      this.response = response;
    }
  }

  private final AIContextCollector contextCollector;
  private final AIAgentServiceAsync aiAgentService;
  private AIResponseOrchestrator.ChatCallback callback;

  /** Active child conversations keyed by screen name. */
  private final Map<String, ChildConversation> children = new HashMap<>();

  /** FIFO queue of batches waiting to be shown to the user. */
  private final LinkedList<PendingBatch> batchQueue = new LinkedList<>();

  /** The batch currently being presented/applied. */
  private PendingBatch activeBatch;

  /** Number of children that have reported completion. */
  private int completedChildren;

  /** Total number of children spawned for this plan. */
  private int totalChildren;

  /** Total RPCs consumed across all children for this plan. */
  private int totalRpcs;

  /** Whether auto-accept mode is active. */
  private boolean autoAcceptAll;

  /** Whether execution has been cancelled. */
  private boolean cancelled;

  /** Whether the plan is currently executing. */
  private boolean executing;

  /** Screens that had operations successfully applied. */
  private final List<String> appliedScreens = new ArrayList<>();

  /**
   * Constructs an orchestration manager with the given context collector.
   * The RPC service proxy is created once and shared across all children.
   *
   * @param contextCollector provides request context from the live editor
   */
  public AIOrchestrationManager(AIContextCollector contextCollector) {
    this.contextCollector = contextCollector;
    this.aiAgentService = GWT.create(AIAgentService.class);
  }

  // ---- Public API ----

  /**
   * Parses the plan JSON, groups steps by screen, and spawns child
   * conversations for each screen group. Project-level steps
   * ({@code __project__}) are skipped -- they were already applied by
   * the client before plan approval.
   *
   * @param planJson the approved plan JSON
   * @param callback the chat callback for UI updates
   */
  public void executePlan(String planJson,
      AIResponseOrchestrator.ChatCallback callback) {
    this.callback = callback;
    this.cancelled = false;
    this.executing = true;
    this.autoAcceptAll = false;
    this.completedChildren = 0;
    this.totalChildren = 0;
    this.totalRpcs = 0;
    this.activeBatch = null;
    this.children.clear();
    this.batchQueue.clear();
    this.appliedScreens.clear();

    callback.onPlanExecutionStarted();
    callback.setRequestInFlight(true);

    // Parse the plan and group steps by screen
    Map<String, String> screenSteps = parsePlanSteps(planJson);
    if (screenSteps.isEmpty()) {
      LOG.warning("No screen-level steps found in plan");
      callback.addAiMessage("No screen-level steps found in the plan.");
      finishExecution();
      return;
    }

    // Enforce max children limit
    if (screenSteps.size() > MAX_CHILDREN) {
      LOG.warning("Plan has " + screenSteps.size()
          + " screens, exceeding max " + MAX_CHILDREN);
      callback.addAiMessage("Plan targets too many screens ("
          + screenSteps.size() + ", max " + MAX_CHILDREN
          + "). Please simplify the plan.");
      finishExecution();
      return;
    }

    // Get the project editor
    long projectId = contextCollector.getCurrentProjectId();
    ProjectEditor projectEditor = Ode.getInstance().getEditorManager()
        .getOpenProjectEditor(projectId);
    if (!(projectEditor instanceof YaProjectEditor)) {
      LOG.warning("executePlan: no YaProjectEditor for projectId=" + projectId);
      callback.addAiMessage("Cannot execute plan: project editor not available.");
      finishExecution();
      return;
    }
    YaProjectEditor yaProjectEditor = (YaProjectEditor) projectEditor;

    // Spawn children for each screen after verifying editors are ready
    spawnChildrenWhenReady(yaProjectEditor, screenSteps);
  }

  /**
   * Called when the user approves the currently displayed batch. Applies
   * operations via {@link AIOperationExecutor#executeForScreen} and, on
   * success, signals the child to continue. Then presents the next
   * queued batch if any.
   */
  public void approveBatch() {
    if (activeBatch == null || cancelled) {
      return;
    }

    final PendingBatch batch = activeBatch;
    activeBatch = null;
    callback.hideOperationPreview();
    callback.setRequestInFlight(true);

    final List<AIOperation> operations = batch.response.getOperations();
    final ScreenExecutionContext context = batch.child.getContext();
    final String screenName = batch.child.getScreenName();

    AIOperationExecutor.executeForScreen(context, operations,
        new AIOperationExecutor.ExecutionCallback() {
          @Override
          public void onComplete(AIOperationExecutor.ExecutionResult result) {
            if (cancelled) {
              return;
            }

            if (result.isSuccess()) {
              // Show the deferred AI message if present
              String aiMessage = batch.response.getAiMessage();
              if (aiMessage != null && !aiMessage.isEmpty()) {
                callback.addAiMessage(aiMessage);
              }
              callback.addAiMessage(
                  AIOperationFormatter.buildAppliedSummary(operations));

              if (!appliedScreens.contains(screenName)) {
                appliedScreens.add(screenName);
              }

              // Signal the child to continue
              batch.child.resumeAfterApproval();
            } else {
              // Execution failed -- report and continue with next batch
              callback.addAiMessage("[" + screenName + "] Execution failed: "
                  + result.getErrorMessage());
              // Still signal the child to continue to avoid deadlock
              batch.child.resumeAfterApproval();
            }

            // Present the next batch
            presentNextBatch();
          }
        });
  }

  /**
   * Called when the user rejects the currently displayed batch. Cancels
   * all children, clears the queue, and reports a summary of what was
   * applied.
   *
   * @param userFeedback optional feedback message from the user
   */
  public void rejectBatch(String userFeedback) {
    if (cancelled) {
      return;
    }

    cancelled = true;
    activeBatch = null;

    // Cancel all children
    for (ChildConversation child : children.values()) {
      child.cancel();
    }

    // Clear the queue
    batchQueue.clear();
    callback.hideOperationPreview();

    // Build a summary of what was applied
    StringBuilder summary = new StringBuilder();
    summary.append("Plan execution stopped.");
    if (!appliedScreens.isEmpty()) {
      summary.append(" Changes were applied to: ");
      for (int i = 0; i < appliedScreens.size(); i++) {
        if (i > 0) {
          summary.append(", ");
        }
        summary.append(appliedScreens.get(i));
      }
      summary.append(".");
    } else {
      summary.append(" No changes were applied.");
    }
    if (userFeedback != null && !userFeedback.isEmpty()) {
      summary.append("\nUser feedback: ").append(userFeedback);
    }
    callback.addAiMessage(summary.toString());

    finishExecution();
  }

  /**
   * Sets auto-accept mode. Immediately processes all queued batches and
   * automatically approves future ones without user confirmation.
   */
  public void setAutoAcceptAll() {
    autoAcceptAll = true;
    callback.setAutoAcceptVisible(true);

    // If there's an active batch being presented, approve it
    if (activeBatch != null) {
      approveBatch();
      return;
    }

    // Process any queued batches
    presentNextBatch();
  }

  /**
   * Returns whether the orchestration manager is currently executing a plan.
   */
  public boolean isExecuting() {
    return executing;
  }

  /**
   * Returns whether the orchestration manager is active (executing and not
   * cancelled). Used by the parent orchestrator to decide whether to
   * delegate approve/reject/cancel calls.
   */
  public boolean isActive() {
    return executing && !cancelled;
  }

  /**
   * Cancels all child conversations and stops execution. Called when the
   * user cancels or clears the conversation during orchestration.
   */
  public void cancelAll() {
    if (cancelled) {
      return;
    }
    cancelled = true;
    activeBatch = null;
    batchQueue.clear();

    for (ChildConversation child : children.values()) {
      child.cancel();
    }

    callback.hideOperationPreview();
    finishExecution();
  }

  /**
   * Returns whether auto-accept mode is active.
   */
  public boolean isAutoAcceptAll() {
    return autoAcceptAll;
  }

  /**
   * Increments the total RPC count. Called by children before each RPC.
   *
   * @return true if the RPC is allowed, false if the limit has been reached
   */
  public boolean consumeRpc() {
    if (totalRpcs >= MAX_RPCS_PER_PLAN) {
      LOG.warning("RPC limit reached: " + totalRpcs + "/" + MAX_RPCS_PER_PLAN);
      return false;
    }
    totalRpcs++;
    return true;
  }

  // ---- BatchCallback implementation ----

  @Override
  public void onBatchReady(ChildConversation child, AIAgentResponse response) {
    if (cancelled) {
      return;
    }

    // Label the response with the screen name
    String screenName = child.getScreenName();
    String aiMessage = response.getAiMessage();
    if (aiMessage != null && !aiMessage.isEmpty()) {
      response.setAiMessage("[" + screenName + "] " + aiMessage);
    } else {
      response.setAiMessage("[" + screenName + "]");
    }

    // Add to the FIFO queue
    batchQueue.add(new PendingBatch(child, response));

    // If nothing is currently being presented, show this batch
    if (activeBatch == null) {
      presentNextBatch();
    }
  }

  @Override
  public void onComplete(ChildConversation child) {
    completedChildren++;
    LOG.info("Child complete for " + child.getScreenName()
        + " (" + completedChildren + "/" + totalChildren + ")");

    if (completedChildren >= totalChildren && batchQueue.isEmpty()
        && activeBatch == null) {
      finishExecution();
    }
  }

  @Override
  public void onError(ChildConversation child, String error) {
    LOG.warning("Child error for " + child.getScreenName() + ": " + error);

    if (!cancelled) {
      callback.addAiMessage("[" + child.getScreenName() + "] Error: " + error);
    }

    // Count as completed so we don't hang
    completedChildren++;

    if (completedChildren >= totalChildren && batchQueue.isEmpty()
        && activeBatch == null) {
      finishExecution();
    }
  }

  // ---- Batch presentation ----

  /**
   * Takes the next batch from the FIFO queue and presents it to the user.
   * If auto-accept is active, approves it immediately instead of showing
   * the preview.
   */
  private void presentNextBatch() {
    if (cancelled) {
      return;
    }

    if (batchQueue.isEmpty()) {
      // Nothing to present -- check if all children are done
      callback.setRequestInFlight(completedChildren < totalChildren);
      if (completedChildren >= totalChildren) {
        finishExecution();
      }
      return;
    }

    activeBatch = batchQueue.removeFirst();

    if (autoAcceptAll) {
      // Auto-approve without showing preview
      approveBatch();
    } else {
      // Show operation preview to the user
      callback.setRequestInFlight(false);
      callback.showOperationPreview(activeBatch.response);
    }
  }

  // ---- Plan parsing ----

  /**
   * Parses the plan JSON and extracts screen-level steps. Steps targeting
   * {@code __project__} are skipped (already applied before plan approval).
   *
   * @param planJson the plan JSON string
   * @return map from screen name to concatenated step descriptions
   */
  private Map<String, String> parsePlanSteps(String planJson) {
    Map<String, String> screenSteps = new HashMap<>();

    try {
      JSONValue parsed = JSONParser.parseStrict(planJson);
      JSONObject planObj = parsed.isObject();
      if (planObj == null) {
        LOG.warning("Plan JSON is not an object");
        return screenSteps;
      }

      // Try "steps" array first
      JSONValue stepsVal = planObj.get("steps");
      if (stepsVal == null) {
        // Maybe the plan is the array itself
        JSONArray stepsArr = parsed.isArray();
        if (stepsArr != null) {
          extractStepsFromArray(stepsArr, screenSteps);
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

  /**
   * Iterates over a JSON array of step objects and groups their descriptions
   * by screen name. Steps with {@code screen: "__project__"} are skipped.
   */
  private void extractStepsFromArray(JSONArray steps,
      Map<String, String> screenSteps) {
    for (int i = 0; i < steps.size(); i++) {
      JSONValue stepVal = steps.get(i);
      JSONObject step = stepVal.isObject();
      if (step == null) {
        continue;
      }

      // Extract screen name
      String screen = getJsonString(step, "screen");
      if (screen == null || "__project__".equals(screen)) {
        continue;
      }

      // Extract description
      String description = getJsonString(step, "description");
      if (description == null) {
        description = getJsonString(step, "step");
      }
      if (description == null) {
        description = "Execute step " + (i + 1) + " on " + screen;
      }

      // Accumulate descriptions per screen
      String existing = screenSteps.get(screen);
      if (existing != null) {
        screenSteps.put(screen, existing + "\n" + description);
      } else {
        screenSteps.put(screen, description);
      }
    }
  }

  /**
   * Extracts a string value from a JSONObject field. Returns null if
   * the field is absent or not a string.
   */
  private static String getJsonString(JSONObject obj, String key) {
    JSONValue val = obj.get(key);
    if (val == null) {
      return null;
    }
    JSONString str = val.isString();
    if (str != null) {
      return str.stringValue();
    }
    return null;
  }

  // ---- Editor readiness and child spawning ----

  /**
   * Verifies that each target screen's editors are loaded before spawning
   * children. If any editor is not ready, polls with a 100ms delay until
   * all are ready or the timeout is reached.
   */
  private void spawnChildrenWhenReady(final YaProjectEditor yaProjectEditor,
      final Map<String, String> screenSteps) {
    // Check if all editors are ready now
    if (allEditorsReady(yaProjectEditor, screenSteps)) {
      spawnChildren(yaProjectEditor, screenSteps);
      return;
    }

    // Poll until editors are ready
    final long startTime = System.currentTimeMillis();
    final Timer readyTimer = new Timer() {
      @Override
      public void run() {
        if (cancelled) {
          cancel();
          return;
        }

        if (allEditorsReady(yaProjectEditor, screenSteps)) {
          cancel();
          spawnChildren(yaProjectEditor, screenSteps);
          return;
        }

        long elapsed = System.currentTimeMillis() - startTime;
        if (elapsed > EDITOR_READY_TIMEOUT_MS) {
          cancel();
          LOG.warning("Timed out waiting for editors to become ready");
          callback.addAiMessage(
              "Timed out waiting for screen editors to load. "
              + "Please ensure all target screens are available and try again.");
          finishExecution();
        }
      }
    };
    readyTimer.scheduleRepeating(EDITOR_READY_POLL_MS);
  }

  /**
   * Returns true if all target screens have loaded form and blocks editors.
   */
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

  /**
   * Creates a ChildConversation for each screen group and starts them.
   */
  private void spawnChildren(YaProjectEditor yaProjectEditor,
      Map<String, String> screenSteps) {
    totalChildren = screenSteps.size();
    completedChildren = 0;

    for (Map.Entry<String, String> entry : screenSteps.entrySet()) {
      String screenName = entry.getKey();
      String stepDescription = entry.getValue();

      // Get editors for this screen
      DesignerEditor<?, ?, ?, ?, ?> formEditor =
          yaProjectEditor.getFormFileEditor(screenName);
      BlocksEditor<?, ?> blocksEditor =
          yaProjectEditor.getBlocksFileEditor(screenName);

      // Build the execution context
      YaFormEditor yaFormEditor = (YaFormEditor) formEditor;
      YaBlocksEditor yaBlocksEditor = (YaBlocksEditor) blocksEditor;
      ScreenExecutionContext context = new ScreenExecutionContext(
          screenName, yaFormEditor, yaBlocksEditor);

      // Create and start the child conversation
      ChildConversation child = new ChildConversation(
          screenName, stepDescription, context, contextCollector,
          aiAgentService, this);

      children.put(screenName, child);
      child.start();
    }

    LOG.info("Spawned " + totalChildren + " child conversation(s)");
  }

  // ---- Lifecycle ----

  /**
   * Finishes plan execution. Resets state and notifies the callback.
   */
  private void finishExecution() {
    executing = false;
    autoAcceptAll = false;
    callback.setRequestInFlight(false);
    callback.setAutoAcceptVisible(false);
    callback.onPlanExecutionFinished();

    // Build completion summary
    if (!cancelled && !appliedScreens.isEmpty()) {
      StringBuilder summary = new StringBuilder("Plan execution complete. ");
      summary.append("Applied changes to: ");
      for (int i = 0; i < appliedScreens.size(); i++) {
        if (i > 0) {
          summary.append(", ");
        }
        summary.append(appliedScreens.get(i));
      }
      summary.append(".");
      callback.addAiMessage(summary.toString());
    } else if (!cancelled && appliedScreens.isEmpty() && totalChildren > 0) {
      callback.addAiMessage("Plan execution complete. No changes were applied.");
    }
  }
}
