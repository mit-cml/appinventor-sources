// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.aiagent;

import com.google.appinventor.client.editor.blocks.BlocksEditor;
import com.google.appinventor.client.editor.designer.DesignerEditor;
import com.google.appinventor.client.editor.youngandroid.YaBlocksEditor;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;
import com.google.appinventor.client.editor.youngandroid.YaProjectEditor;
import com.google.appinventor.client.editor.youngandroid.aiagent.executor.ScreenExecutionContext;
import com.google.appinventor.shared.rpc.aiagent.AIAgentService;
import com.google.appinventor.shared.rpc.aiagent.AIAgentServiceAsync;
import com.google.gwt.core.client.GWT;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Coordinates parallel child conversations during multi-agent orchestration
 * of an approved plan.
 *
 * <p>This is a thin coordinator that delegates to:
 * <ul>
 *   <li>{@link PlanProjectStepExecutor} — plan parsing, screen creation,
 *       editor readiness</li>
 *   <li>{@link ChildBatchQueue} — FIFO batch queue, per-batch approval,
 *       child lifecycle</li>
 * </ul>
 */
public class AIOrchestrationManager implements ChildBatchQueue.QueueCallback {

  private static final Logger LOG =
      Logger.getLogger(AIOrchestrationManager.class.getName());

  /** Maximum number of concurrent child conversations. */
  private static final int MAX_CHILDREN = 5;

  /** Maximum total RPCs allowed per plan execution. */
  private static final int MAX_RPCS_PER_PLAN = 20;

  /** Callback for orchestration lifecycle events sent to the parent orchestrator. */
  interface CompletionCallback {
    /** Plan executed successfully — send summary to parent LLM. */
    void onOrchestrationComplete(String summary);
    /** Plan rejected mid-execution — send rejection context to parent LLM. */
    void onOrchestrationRejected(String summary);
    /** Plan cancelled — no parent feedback needed. */
    void onOrchestrationCancelled();
  }

  private final AIContextCollector contextCollector;
  private final AIAgentServiceAsync aiAgentService;

  private AIResponseOrchestrator.ChatCallback callback;
  private CompletionCallback completionCallback;
  private PlanProjectStepExecutor projectStepExecutor;
  private ChildBatchQueue batchQueue;

  private final Map<String, ChildConversation> children = new HashMap<>();
  private int totalRpcs;
  private boolean cancelled;
  private boolean executing;

  public AIOrchestrationManager(AIContextCollector contextCollector) {
    this.contextCollector = contextCollector;
    this.aiAgentService = GWT.create(AIAgentService.class);
  }

  // ---- Public API ----

  /**
   * Parses the plan, creates any required screens, waits for editors,
   * then spawns child conversations.
   */
  public void executePlan(String planJson,
      AIResponseOrchestrator.ChatCallback callback,
      CompletionCallback completionCallback) {
    this.callback = callback;
    this.completionCallback = completionCallback;
    this.cancelled = false;
    this.executing = true;
    this.totalRpcs = 0;
    this.children.clear();

    callback.onPlanExecutionStarted();
    callback.setRequestInFlight(true);
    callback.setStatusText("Starting plan execution...");
    callback.setStatusVisible(true);

    projectStepExecutor = new PlanProjectStepExecutor(contextCollector);
    projectStepExecutor.prepareAndExecute(planJson, callback,
        new PlanProjectStepExecutor.ReadyCallback() {
          @Override
          public void onReady(YaProjectEditor projectEditor,
              Map<String, String> screenSteps) {
            if (cancelled) {
              return;
            }
            if (screenSteps.size() > MAX_CHILDREN) {
              callback.addAiMessage("Plan targets too many screens ("
                  + screenSteps.size() + ", max " + MAX_CHILDREN
                  + "). Please simplify the plan.");
              finishExecution();
              return;
            }
            spawnChildren(projectEditor, screenSteps);
          }

          @Override
          public void onError(String message) {
            callback.addAiMessage(message);
            finishExecution();
          }
        });
  }

  public void approveBatch() {
    if (batchQueue != null) {
      batchQueue.approveBatch();
    }
  }

  public void rejectBatch(String userFeedback) {
    if (batchQueue != null) {
      batchQueue.rejectBatch(userFeedback);
    }
  }

  public void setAutoAcceptAll() {
    if (batchQueue != null) {
      batchQueue.setAutoAcceptAll();
    }
  }

  public boolean isActive() {
    return executing && !cancelled;
  }

  public void cancelAll() {
    if (cancelled) {
      return;
    }
    cancelled = true;
    if (projectStepExecutor != null) {
      projectStepExecutor.cancel();
    }
    if (batchQueue != null) {
      batchQueue.cancelAll();
    }
    callback.hideOperationPreview();
    finishExecution();
    if (completionCallback != null) {
      completionCallback.onOrchestrationCancelled();
    }
  }

  /**
   * Checks the per-plan RPC budget. Called by children before each RPC.
   */
  public boolean consumeRpc() {
    if (totalRpcs >= MAX_RPCS_PER_PLAN) {
      LOG.warning("RPC limit reached: " + totalRpcs + "/" + MAX_RPCS_PER_PLAN);
      return false;
    }
    totalRpcs++;
    return true;
  }

  // ---- QueueCallback ----

  @Override
  public void onAllChildrenDone() {
    finishExecution();
  }

  @Override
  public void onChildError(String screenName, String error) {
    // Logged by ChildBatchQueue, no extra action needed here
  }

  @Override
  public void onRejected(String rejectionSummary) {
    finishExecution();
    if (completionCallback != null) {
      completionCallback.onOrchestrationRejected(rejectionSummary);
    }
  }

  // ---- Internal ----

  private void spawnChildren(YaProjectEditor projectEditor,
      Map<String, String> screenSteps) {
    int total = screenSteps.size();

    batchQueue = new ChildBatchQueue(children, total, callback, this);

    for (Map.Entry<String, String> entry : screenSteps.entrySet()) {
      String screenName = entry.getKey();
      String stepDescription = entry.getValue();

      DesignerEditor<?, ?, ?, ?, ?> formEditor =
          projectEditor.getFormFileEditor(screenName);
      BlocksEditor<?, ?> blocksEditor =
          projectEditor.getBlocksFileEditor(screenName);

      ScreenExecutionContext context = new ScreenExecutionContext(
          screenName, (YaFormEditor) formEditor, (YaBlocksEditor) blocksEditor);

      ChildConversation child = new ChildConversation(
          screenName, stepDescription, context, contextCollector,
          aiAgentService, batchQueue);

      children.put(screenName, child);
      child.start();
    }

    LOG.info("Spawned " + total + " child conversation(s)");
  }

  private void finishExecution() {
    executing = false;
    callback.setRequestInFlight(false);
    callback.setStatusVisible(false);
    callback.setAutoAcceptVisible(false);
    callback.onPlanExecutionFinished();

    if (!cancelled && batchQueue != null && completionCallback != null) {
      java.util.List<String> applied = batchQueue.getAppliedScreens();
      String summary = buildCompletionSummary(applied);
      callback.addAiMessage(summary);
      if (!applied.isEmpty()) {
        completionCallback.onOrchestrationComplete(summary);
      }
    }
  }

  private String buildCompletionSummary(java.util.List<String> applied) {
    if (!applied.isEmpty()) {
      StringBuilder sb = new StringBuilder(
          "Plan execution complete. Applied changes to: ");
      for (int i = 0; i < applied.size(); i++) {
        if (i > 0) {
          sb.append(", ");
        }
        sb.append(applied.get(i));
      }
      sb.append(".");
      return sb.toString();
    }
    return "Plan execution complete. No changes were applied.";
  }
}
