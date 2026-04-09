// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.aiagent;

import com.google.appinventor.client.editor.youngandroid.aiagent.executor.AIOperationExecutor;
import com.google.appinventor.client.editor.youngandroid.aiagent.executor.ScreenExecutionContext;
import com.google.appinventor.shared.rpc.aiagent.AIAgentResponse;
import com.google.appinventor.shared.rpc.aiagent.AIOperation;
import com.google.gwt.user.client.Timer;

import java.util.ArrayList;
import java.util.HashMap;
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
    void onRejected(String rejectionSummary);
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

  /** Accumulated operations per screen, for the grouped summary at the end. */
  private final Map<String, List<AIOperation>> screenOperations = new HashMap<>();

  /** Accumulated errors per screen. */
  private final Map<String, List<String>> screenErrors = new HashMap<>();

  /** Last action description per screen, for live status display. */
  private final Map<String, String> lastAction = new HashMap<>();

  /** Counter for cycling the ellipsis animation in the status line. */
  private int ellipsisCounter;

  /** Timer for animating the status ellipsis while waiting. */
  private Timer ellipsisTimer;

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
              if (!appliedScreens.contains(screenName)) {
                appliedScreens.add(screenName);
              }
              // Accumulate operations for the grouped summary
              if (!screenOperations.containsKey(screenName)) {
                screenOperations.put(screenName, new ArrayList<AIOperation>());
              }
              screenOperations.get(screenName).addAll(operations);
              batch.child.updateViewFromOperations(operations);
              lastAction.put(screenName, summarizeLastAction(operations));

              if (!autoAcceptAll) {
                // Manual approval — show per-batch messages
                String aiMessage = batch.response.getAiMessage();
                if (aiMessage != null && !aiMessage.isEmpty()) {
                  uiCallback.addAiMessage(aiMessage);
                }
                uiCallback.addAiMessage(
                    AIOperationFormatter.buildAppliedSummary(operations));
              }
            } else {
              // Always show errors
              String errorMsg = "[" + screenName + "] Execution failed: "
                  + result.getErrorMessage();
              if (!screenErrors.containsKey(screenName)) {
                screenErrors.put(screenName, new ArrayList<String>());
              }
              screenErrors.get(screenName).add(result.getErrorMessage());
              uiCallback.addAiMessage(errorMsg);
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
    stopEllipsisAnimation();
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
    queueCallback.onRejected(summary.toString());
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
    stopEllipsisAnimation();
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

  /** Returns a grouped summary of all operations applied per screen. */
  Map<String, List<AIOperation>> getScreenOperations() {
    return screenOperations;
  }

  /** Returns any errors that occurred per screen. */
  Map<String, List<String>> getScreenErrors() {
    return screenErrors;
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
      boolean childrenStillRunning = completedChildren < totalChildren;
      uiCallback.setRequestInFlight(childrenStillRunning);
      if (childrenStillRunning) {
        startEllipsisAnimation();
      } else {
        stopEllipsisAnimation();
      }
      checkAllDone();
      return;
    }
    stopEllipsisAnimation();
    activeBatch = queue.removeFirst();
    if (autoAcceptAll) {
      approveBatch();
    } else {
      uiCallback.setRequestInFlight(false);
      uiCallback.setStatusVisible(false);
      uiCallback.showOperationPreview(activeBatch.response);
    }
  }

  private static final String[] ELLIPSIS = {".", "..", "..."};

  private void startEllipsisAnimation() {
    stopEllipsisAnimation();
    ellipsisCounter = 0;
    uiCallback.setStatusText(buildProgressStatus());
    uiCallback.setStatusVisible(true);
    ellipsisTimer = new Timer() {
      @Override
      public void run() {
        if (cancelled || completedChildren >= totalChildren) {
          cancel();
          return;
        }
        ellipsisCounter++;
        uiCallback.setStatusText(buildProgressStatus());
      }
    };
    ellipsisTimer.scheduleRepeating(600);
  }

  private void stopEllipsisAnimation() {
    if (ellipsisTimer != null) {
      ellipsisTimer.cancel();
      ellipsisTimer = null;
    }
    uiCallback.setStatusVisible(false);
  }

  /**
   * Builds a status string showing which children are still working
   * and what each last did, with animated ellipsis.
   */
  private String buildProgressStatus() {
    String dots = ELLIPSIS[ellipsisCounter % ELLIPSIS.length];
    StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (Map.Entry<String, ChildConversation> entry : children.entrySet()) {
      ChildConversation child = entry.getValue();
      if (!child.isCancelled() && !child.isComplete()) {
        if (!first) {
          sb.append(" | ");
        }
        sb.append(entry.getKey());
        String action = lastAction.get(entry.getKey());
        if (action != null) {
          sb.append(": ").append(action).append(dots);
        } else {
          sb.append(": thinking").append(dots);
        }
        first = false;
      }
    }
    sb.append("  (").append(completedChildren).append("/")
        .append(totalChildren).append(" done)");
    return sb.toString();
  }

  /**
   * Returns a short description of the last batch of operations applied.
   */
  private static String summarizeLastAction(List<AIOperation> operations) {
    if (operations.isEmpty()) {
      return "waiting...";
    }
    // Count by type
    int adds = 0, props = 0, blocks = 0, other = 0;
    for (AIOperation op : operations) {
      switch (op.getType()) {
        case ADD_COMPONENT: adds++; break;
        case SET_PROPERTY: props++; break;
        case WRITE_BLOCK: blocks++; break;
        case DELETE_BLOCK: blocks++; break;
        default: other++; break;
      }
    }
    StringBuilder sb = new StringBuilder();
    if (adds > 0) {
      sb.append("added ").append(adds).append(" component").append(adds > 1 ? "s" : "");
    }
    if (props > 0) {
      if (sb.length() > 0) sb.append(", ");
      sb.append("set ").append(props).append(" propert").append(props > 1 ? "ies" : "y");
    }
    if (blocks > 0) {
      if (sb.length() > 0) sb.append(", ");
      sb.append("wrote ").append(blocks).append(" block").append(blocks > 1 ? "s" : "");
    }
    if (other > 0 && sb.length() == 0) {
      sb.append(operations.size()).append(" operation").append(operations.size() > 1 ? "s" : "");
    }
    return sb.toString();
  }

  private void checkAllDone() {
    if (completedChildren >= totalChildren && queue.isEmpty()
        && activeBatch == null) {
      queueCallback.onAllChildrenDone();
    }
  }
}
