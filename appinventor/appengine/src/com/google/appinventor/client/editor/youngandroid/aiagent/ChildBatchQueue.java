// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.aiagent;

import com.google.appinventor.client.editor.youngandroid.aiagent.executor.AIOperationExecutor;
import com.google.appinventor.client.editor.youngandroid.aiagent.executor.ScreenExecutionContext;
import com.google.appinventor.shared.rpc.aiagent.AIAgentResponse;
import com.google.appinventor.shared.rpc.aiagent.AIOperation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * FIFO queue of operation batches from child conversations. Presents
 * batches one at a time to the user for approval, applies approved
 * batches via {@link AIOperationExecutor#executeForScreen}, and
 * manages auto-accept mode.
 *
 * <p>Extracted from {@link AIOrchestrationManager} to keep that class
 * focused on high-level plan coordination.</p>
 */
final class ChildBatchQueue implements ChildConversation.BatchCallback {

  private static final Logger LOG =
      Logger.getLogger(ChildBatchQueue.class.getName());

  /** Callback to notify the orchestration manager of lifecycle events. */
  interface QueueCallback {
    void onAllChildrenDone();
    void onChildError(String screenName, String error);
  }

  /** A batch waiting for user approval. */
  private static class PendingBatch {
    final ChildConversation child;
    final AIAgentResponse response;

    PendingBatch(ChildConversation child, AIAgentResponse response) {
      this.child = child;
      this.response = response;
    }
  }

  private final Map<String, ChildConversation> children;
  private final AIResponseOrchestrator.ChatCallback uiCallback;
  private final QueueCallback queueCallback;

  private final LinkedList<PendingBatch> queue = new LinkedList<>();
  private PendingBatch activeBatch;

  private int completedChildren;
  private int totalChildren;
  private boolean autoAcceptAll;
  private boolean cancelled;

  /** Screens that had operations successfully applied. */
  private final List<String> appliedScreens = new ArrayList<>();

  ChildBatchQueue(Map<String, ChildConversation> children,
      int totalChildren,
      AIResponseOrchestrator.ChatCallback uiCallback,
      QueueCallback queueCallback) {
    this.children = children;
    this.totalChildren = totalChildren;
    this.uiCallback = uiCallback;
    this.queueCallback = queueCallback;
  }

  // ---- Public API ----

  void approveBatch() {
    if (activeBatch == null || cancelled) {
      return;
    }

    final PendingBatch batch = activeBatch;
    activeBatch = null;
    uiCallback.hideOperationPreview();
    uiCallback.setRequestInFlight(true);

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
              String aiMessage = batch.response.getAiMessage();
              if (aiMessage != null && !aiMessage.isEmpty()) {
                uiCallback.addAiMessage(aiMessage);
              }
              uiCallback.addAiMessage(
                  AIOperationFormatter.buildAppliedSummary(operations));
              if (!appliedScreens.contains(screenName)) {
                appliedScreens.add(screenName);
              }
            } else {
              uiCallback.addAiMessage("[" + screenName + "] Execution failed: "
                  + result.getErrorMessage());
            }
            batch.child.resumeAfterApproval();
            presentNext();
          }
        });
  }

  void rejectBatch(String userFeedback) {
    if (cancelled) {
      return;
    }
    cancelled = true;
    activeBatch = null;
    queue.clear();
    uiCallback.hideOperationPreview();

    for (ChildConversation child : children.values()) {
      child.cancel();
    }

    StringBuilder summary = new StringBuilder("Plan execution stopped.");
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
    uiCallback.addAiMessage(summary.toString());
    queueCallback.onAllChildrenDone();
  }

  void setAutoAcceptAll() {
    autoAcceptAll = true;
    uiCallback.setAutoAcceptVisible(true);
    if (activeBatch != null) {
      approveBatch();
    } else {
      presentNext();
    }
  }

  void cancelAll() {
    if (cancelled) {
      return;
    }
    cancelled = true;
    activeBatch = null;
    queue.clear();
    for (ChildConversation child : children.values()) {
      child.cancel();
    }
    uiCallback.hideOperationPreview();
  }

  boolean isCancelled() {
    return cancelled;
  }

  List<String> getAppliedScreens() {
    return appliedScreens;
  }

  // ---- BatchCallback implementation ----

  @Override
  public void onBatchReady(ChildConversation child, AIAgentResponse response) {
    if (cancelled) {
      return;
    }
    String screenName = child.getScreenName();
    String aiMessage = response.getAiMessage();
    if (aiMessage != null && !aiMessage.isEmpty()) {
      response.setAiMessage("[" + screenName + "] " + aiMessage);
    } else {
      response.setAiMessage("[" + screenName + "]");
    }
    queue.add(new PendingBatch(child, response));
    if (activeBatch == null) {
      presentNext();
    }
  }

  @Override
  public void onComplete(ChildConversation child) {
    completedChildren++;
    LOG.info("Child complete: " + child.getScreenName()
        + " (" + completedChildren + "/" + totalChildren + ")");
    checkAllDone();
  }

  @Override
  public void onError(ChildConversation child, String error) {
    LOG.warning("Child error: " + child.getScreenName() + ": " + error);
    if (!cancelled) {
      uiCallback.addAiMessage("[" + child.getScreenName() + "] Error: " + error);
    }
    completedChildren++;
    queueCallback.onChildError(child.getScreenName(), error);
    checkAllDone();
  }

  // ---- Internal ----

  private void presentNext() {
    if (cancelled) {
      return;
    }
    if (queue.isEmpty()) {
      uiCallback.setRequestInFlight(completedChildren < totalChildren);
      checkAllDone();
      return;
    }
    activeBatch = queue.removeFirst();
    if (autoAcceptAll) {
      approveBatch();
    } else {
      uiCallback.setRequestInFlight(false);
      uiCallback.setStatusVisible(false);
      uiCallback.showOperationPreview(activeBatch.response);
    }
  }

  private void checkAllDone() {
    if (completedChildren >= totalChildren && queue.isEmpty()
        && activeBatch == null) {
      queueCallback.onAllChildrenDone();
    }
  }
}
