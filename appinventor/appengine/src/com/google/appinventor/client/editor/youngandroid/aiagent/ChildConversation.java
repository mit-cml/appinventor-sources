// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.aiagent;

import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.editor.youngandroid.aiagent.executor.ScreenExecutionContext;
import com.google.appinventor.shared.rpc.aiagent.AIAgentRequest;
import com.google.appinventor.shared.rpc.aiagent.AIAgentResponse;
import com.google.appinventor.shared.rpc.aiagent.AIAgentServiceAsync;
import com.google.appinventor.shared.rpc.aiagent.AIOperation;
import com.google.appinventor.shared.rpc.aiagent.AIStreamStatus;
import com.google.gwt.user.client.Timer;

import java.util.List;
import java.util.logging.Logger;

/**
 * Manages one child agent's RPC loop for a single screen during
 * multi-agent orchestration.
 *
 * <p>The lifecycle is:
 * <ol>
 *   <li>{@link #start()} fires {@code processRequest()} with context
 *       from {@link AIContextCollector#buildRequestForScreen}.</li>
 *   <li>A polling timer polls {@code getRequestStatus(projectId, screenName)}
 *       for streaming progress.</li>
 *   <li>When the RPC returns with operations, the batch is reported to
 *       the {@link BatchCallback} and the child <em>pauses</em>.</li>
 *   <li>After approval, the orchestration manager calls
 *       {@link #resumeAfterApproval()} which rebuilds context from the
 *       now-updated background editor and calls {@code continueRequest()}
 *       if {@code hasMore} was true.</li>
 *   <li>When {@code hasMore=false} and no more operations remain, the
 *       child calls {@link BatchCallback#onComplete}.</li>
 *   <li>{@link #cancel()} fires {@code cancelRequest(projectId, screenName)}
 *       and stops all activity.</li>
 * </ol>
 *
 * <p>Validation errors are reported directly to the callback as errors.
 * The orchestration manager decides how to handle them.</p>
 */
public class ChildConversation {

  private static final Logger LOG = Logger.getLogger(ChildConversation.class.getName());

  /** RPC timeout for child requests (12 minutes, matches parent orchestrator). */
  private static final int RPC_TIMEOUT_MS = 720000;

  /** Slow polling interval for initial status checks (1 second). */
  private static final int POLL_INTERVAL_SLOW_MS = 1000;

  /** Fast polling interval for streaming text deltas (250ms). */
  private static final int POLL_INTERVAL_FAST_MS = 250;

  /**
   * Callback interface for reporting batches and completion to the
   * orchestration manager.
   */
  public interface BatchCallback {
    /** Called when the child produces a batch of operations ready for approval. */
    void onBatchReady(ChildConversation child, AIAgentResponse response);

    /** Called when the child finishes (hasMore=false, no more batches). */
    void onComplete(ChildConversation child);

    /** Called when the child encounters a fatal error. */
    void onError(ChildConversation child, String error);
  }

  private final String screenName;
  private final String stepDescription;
  private final ScreenExecutionContext context;
  private final AIContextCollector contextCollector;
  private final AIAgentServiceAsync aiAgentService;
  private final BatchCallback callback;
  private final long projectId;

  private String currentView = "Designer";
  private Timer pollingTimer;
  private boolean waitingForApproval;
  private boolean lastResponseHasMore;
  private boolean cancelled;
  private boolean streamingActive;

  /**
   * Constructs a child conversation for a single screen.
   *
   * @param screenName       the target screen name
   * @param stepDescription  the plan step description for this screen
   * @param context          the screen's execution context (editors)
   * @param contextCollector the context collector for building requests
   * @param aiAgentService   the RPC service proxy
   * @param callback         the orchestration manager callback
   */
  public ChildConversation(String screenName, String stepDescription,
      ScreenExecutionContext context, AIContextCollector contextCollector,
      AIAgentServiceAsync aiAgentService, BatchCallback callback) {
    this.screenName = screenName;
    this.stepDescription = stepDescription;
    this.context = context;
    this.contextCollector = contextCollector;
    this.aiAgentService = aiAgentService;
    this.callback = callback;
    this.projectId = contextCollector.getCurrentProjectId();
  }

  // ---- Public API ----

  /**
   * Starts the child conversation by firing {@code processRequest()} with
   * context built from the target screen's background editors.
   */
  public void start() {
    if (cancelled) {
      return;
    }

    AIAgentRequest request = contextCollector.buildRequestForScreen(
        screenName, stepDescription, currentView);
    startPollingStatus();

    aiAgentService.processRequest(request, new OdeAsyncCallback<AIAgentResponse>() {
      @Override
      public void onSuccess(AIAgentResponse response) {
        handleResponse(response);
      }

      @Override
      public void onFailure(Throwable caught) {
        super.onFailure(caught);
        stopPollingStatus();
        if (!cancelled) {
          callback.onError(ChildConversation.this,
              "processRequest failed for " + screenName + ": " + caught.getMessage());
        }
      }
    });
  }

  /**
   * Called by the orchestration manager after a batch has been approved and
   * applied. If the last response indicated {@code hasMore}, this rebuilds
   * context from the now-updated background editor and fires
   * {@code continueRequest()}. Otherwise, signals completion.
   */
  public void resumeAfterApproval() {
    waitingForApproval = false;

    if (cancelled) {
      return;
    }

    if (lastResponseHasMore) {
      fetchContinuation();
    } else {
      callback.onComplete(this);
    }
  }

  /**
   * Cancels this child conversation. Stops polling and fires a
   * server-side cancel RPC (fire-and-forget).
   */
  public void cancel() {
    if (cancelled) {
      return;
    }
    cancelled = true;
    stopPollingStatus();

    if (projectId != 0) {
      aiAgentService.cancelRequest(projectId, screenName,
          new OdeAsyncCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
              // Cancellation is best-effort.
            }
          });
    }
  }

  /**
   * Returns the target screen name for this child conversation.
   */
  public String getScreenName() {
    return screenName;
  }

  /**
   * Returns the screen execution context for this child conversation.
   */
  public ScreenExecutionContext getContext() {
    return context;
  }

  /**
   * Returns whether this child is waiting for batch approval.
   */
  public boolean isWaitingForApproval() {
    return waitingForApproval;
  }

  /**
   * Returns whether this child has been cancelled.
   */
  public boolean isCancelled() {
    return cancelled;
  }

  /**
   * Returns the current editor view tracked by this child conversation.
   */
  public String getCurrentView() {
    return currentView;
  }

  /**
   * Scans the given operations for TOGGLE_EDITOR and updates this child's
   * tracked view accordingly. Called after operations are applied.
   */
  public void updateViewFromOperations(List<AIOperation> operations) {
    for (AIOperation op : operations) {
      if (op.getType() == AIOperation.Type.TOGGLE_EDITOR) {
        String payload = op.getPayload();
        if (payload != null && payload.contains("Blocks")) {
          currentView = "Blocks";
        } else if (payload != null && payload.contains("Designer")) {
          currentView = "Designer";
        }
      }
    }
  }

  // ---- Response handling ----

  /**
   * Handles a successful RPC response. If the response contains operations,
   * pauses and reports the batch to the callback. If there are no operations
   * and no more batches, signals completion. Validation errors are reported
   * as errors to the callback.
   */
  private void handleResponse(AIAgentResponse response) {
    if (cancelled) {
      return;
    }
    stopPollingStatus();

    // Check for errors in the response
    List<String> errors = response.getErrors();
    if (errors != null && !errors.isEmpty()) {
      StringBuilder errorMsg = new StringBuilder("Errors on " + screenName + ":");
      for (String error : errors) {
        errorMsg.append("\n  - ").append(error);
      }
      callback.onError(this, errorMsg.toString());
      return;
    }

    lastResponseHasMore = response.hasMore();

    List<AIOperation> operations = response.getOperations();
    boolean hasOps = operations != null && !operations.isEmpty();

    if (hasOps && isToggleOnly(operations)) {
      // Auto-approve toggle operations -- no user approval needed
      updateViewFromOperations(operations);
      if (lastResponseHasMore) {
        fetchContinuation();
      } else {
        callback.onComplete(this);
      }
      return;
    }

    if (hasOps) {
      // Pause and report the batch to the orchestration manager
      waitingForApproval = true;
      callback.onBatchReady(this, response);
    } else if (lastResponseHasMore) {
      // No operations in this batch but more coming -- continue immediately
      fetchContinuation();
    } else {
      // No operations and no more batches -- done
      callback.onComplete(this);
    }
  }

  /**
   * Returns true if every operation in the list is a TOGGLE_EDITOR operation.
   */
  private boolean isToggleOnly(List<AIOperation> operations) {
    for (AIOperation op : operations) {
      if (op.getType() != AIOperation.Type.TOGGLE_EDITOR) {
        return false;
      }
    }
    return !operations.isEmpty();
  }

  // ---- Continuation ----

  /**
   * Builds fresh context from the (now-updated) background editor and
   * fires {@code continueRequest()} to fetch the next batch.
   */
  private void fetchContinuation() {
    if (cancelled) {
      return;
    }

    AIAgentRequest request = contextCollector.buildRequestForScreen(screenName, null, currentView);
    startPollingStatus();

    aiAgentService.continueRequest(request, new OdeAsyncCallback<AIAgentResponse>() {
      @Override
      public void onSuccess(AIAgentResponse response) {
        handleResponse(response);
      }

      @Override
      public void onFailure(Throwable caught) {
        super.onFailure(caught);
        stopPollingStatus();
        if (!cancelled) {
          callback.onError(ChildConversation.this,
              "continueRequest failed for " + screenName + ": " + caught.getMessage());
        }
      }
    });
  }

  // ---- Status polling ----

  /**
   * Starts a timer that polls {@code getRequestStatus(projectId, screenName)}
   * to track streaming progress for this child's screen-scoped StreamBuffer.
   */
  private void startPollingStatus() {
    stopPollingStatus();
    streamingActive = false;

    pollingTimer = new Timer() {
      @Override
      public void run() {
        if (projectId == 0 || cancelled) {
          stopPollingStatus();
          return;
        }

        aiAgentService.getRequestStatus(projectId, screenName,
            new OdeAsyncCallback<AIStreamStatus>() {
              @Override
              public void onSuccess(AIStreamStatus status) {
                if (status == null || cancelled) {
                  return;
                }
                // Switch to fast polling once streaming text arrives
                if (!streamingActive
                    && (status.getTextDelta() != null
                        || status.getThinkingDelta() != null)) {
                  streamingActive = true;
                  pollingTimer.cancel();
                  pollingTimer.scheduleRepeating(POLL_INTERVAL_FAST_MS);
                }
                if (status.isDone()) {
                  // Stream complete -- the RPC response handles the rest.
                  if (pollingTimer != null) {
                    pollingTimer.cancel();
                    pollingTimer = null;
                  }
                }
              }

              @Override
              public void onFailure(Throwable caught) {
                // Polling failure is non-fatal for child conversations.
              }
            });
      }
    };
    pollingTimer.scheduleRepeating(POLL_INTERVAL_SLOW_MS);
  }

  /**
   * Stops the status polling timer.
   */
  private void stopPollingStatus() {
    if (pollingTimer != null) {
      pollingTimer.cancel();
      pollingTimer = null;
    }
    streamingActive = false;
  }
}
