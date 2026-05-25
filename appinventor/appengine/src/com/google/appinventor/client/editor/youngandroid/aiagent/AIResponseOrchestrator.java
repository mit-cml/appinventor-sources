// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.aiagent;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.common.version.AppInventorFeatures;
import com.google.appinventor.client.editor.blocks.BlocksEditor;
import com.google.appinventor.shared.rpc.aiagent.AIAgentRequest;
import com.google.appinventor.shared.rpc.aiagent.AIAgentResponse;
import com.google.appinventor.shared.rpc.aiagent.AIAgentService;
import com.google.appinventor.shared.rpc.aiagent.AIAgentServiceAsync;
import com.google.appinventor.shared.rpc.aiagent.AIConversationMessage;
import com.google.appinventor.shared.rpc.aiagent.AIConversationSummary;
import com.google.appinventor.shared.rpc.aiagent.AIOperation;
import com.google.appinventor.shared.rpc.aiagent.AIOperationResult;
import com.google.appinventor.shared.rpc.aiagent.AIStreamStatus;
import com.google.appinventor.client.editor.youngandroid.aiagent.companion.CompanionBridge;
import com.google.appinventor.client.editor.youngandroid.aiagent.executor.AIOperationExecutor;
import com.google.appinventor.client.editor.youngandroid.aiagent.validator.CompanionReadValidator;
import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.RpcRequestBuilder;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Orchestrates RPC calls, validation retry loops, status polling,
 * and operation execution for the AI chat dialog.
 *
 * <p>Owns all server communication state (in-flight flag, polling timer,
 * pending response, retry count). Delegates UI updates to a
 * {@link ChatCallback} and context building to an
 * {@link AIContextCollector}.</p>
 */
public class AIResponseOrchestrator {

  private static final Logger LOG = Logger.getLogger(AIResponseOrchestrator.class.getName());

  /** RPC timeout for processRequest (12 minutes, must exceed server-side read timeout). */
  private static final int RPC_TIMEOUT_MS = 720000;

  /** Fast polling interval for streaming text deltas (250ms). */
  private static final int POLL_INTERVAL_FAST_MS = 250;

  /** Slow polling interval for initial status checks (1 second). */
  private static final int POLL_INTERVAL_SLOW_MS = 1000;

  /** Maximum number of client-side validation retries before showing the error. */
  private static final int MAX_VALIDATION_RETRIES = 5;

  /** Maximum number of execution error retries before giving up. */
  private static final int MAX_EXECUTION_RETRIES = 3;

  /**
   * Callback for plan approval decisions (approve or reject).
   */
  public interface PlanApprovalCallback {
    void onApprove(String approvedPlanJson);
    void onReject();
  }

  /**
   * Callback interface for UI updates from the orchestrator.
   */
  public interface ChatCallback {
    void addUserMessage(String text, long timestamp);
    void addAiMessage(String text, long timestamp);
    void startStreamingBubble();
    void appendStreamingText(String delta);
    void appendStreamingThinking(String delta);
    void finalizeStreamingBubble(String finalText);
    void showOperationPreview(AIAgentResponse response);
    void hideOperationPreview();
    void setRequestInFlight(boolean inFlight);
    void setStatusText(String text);
    void setStatusVisible(boolean visible);
    void setAutoAcceptVisible(boolean visible);
    void clearChatHistory();
    /** Shows the debug-mode warning banner in the chat area. */
    void showDebugBanner();
    /** Sets the feedback context for AI messages (links only shown in debug mode). */
    void setFeedbackContext(boolean debugEnabled, String conversationId);
    /** Renders a plan card with approve/reject buttons in the chat area. */
    void renderPlanCard(String planJson, PlanApprovalCallback approvalCallback);
    /** Called when server config has been fetched (orchestration flag, etc.). */
    void onConfigLoaded();
    /** Called when plan execution begins (plan approved, agent executing). */
    void onPlanExecutionStarted();
    /** Called when plan execution completes (all operations done). */
    void onPlanExecutionFinished();
  }

  private final AIContextCollector contextCollector;
  private final ChatCallback callback;
  private final AIAgentServiceAsync aiAgentService;

  // State
  private AIAgentResponse pendingResponse;
  private Timer pollingTimer;
  private boolean requestInFlight;
  private boolean streamingActive;
  private int validationRetryCount;
  private int executionRetryCount;
  private boolean autoAcceptAll;
  private boolean pendingPlanProposal;
  private boolean debugBannerShown;
  // AI agent feature toggles are compile-time and read directly from
  // {@link AppInventorFeatures}. Per-request runtime bits (conversationId,
  // debug banner trigger) arrive on {@link AIStreamStatus}.

  /** Manages parallel child conversations during multi-screen plan execution. */
  private AIOrchestrationManager orchestrationManager;

  /**
   * The active conversation UUID. Null when a new conversation is pending —
   * the server mints one on the first {@link #sendMessage} and echoes it
   * back on the response, at which point this field is populated.
   */
  private String currentConversationId;

  /** Original total number of tools in the batch, preserved across retries. */
  private int originalToolCount;

  /** Operations that passed validation and are held across retries. */
  private List<AIOperation> preservedValidOps;
  /** Original AI message preserved from the first response in a retry sequence. */
  private String preservedAiMessage;

  private void setAutoAcceptAll(boolean value) {
    autoAcceptAll = value;
    callback.setAutoAcceptVisible(value);
  }

  /**
   * Constructs an orchestrator with the given context collector and callback.
   *
   * @param contextCollector provides request context from the live editor
   * @param callback         receives UI update notifications
   */
  public AIResponseOrchestrator(AIContextCollector contextCollector, ChatCallback callback) {
    this.contextCollector = contextCollector;
    this.callback = callback;
    this.aiAgentService = GWT.create(AIAgentService.class);
    configureServiceTimeout(aiAgentService);
  }

  // ---- Public API ----

  /**
   * Sends a message to the AI agent via RPC.
   * Sets request-in-flight state and starts status polling.
   *
   * @param text the user's message text
   */
  public void sendMessage(String text) {
    AIAgentRequest request = contextCollector.buildRequest(text);
    request.setConversationId(currentConversationId);
    requestInFlight = true;
    callback.setRequestInFlight(true);
    validationRetryCount = 0;
    executionRetryCount = 0;
    originalToolCount = 0;
    preservedValidOps = null;
    preservedAiMessage = null;
    CompanionBridge.getInstance().resetTurnBudget();
    startPollingStatus();

    aiAgentService.processRequest(request, new OdeAsyncCallback<AIAgentResponse>(
        MESSAGES.aiChatSendError()) {
      @Override
      public void onSuccess(AIAgentResponse response) {
        handleResponseWithValidation(response);
      }

      @Override
      public void onFailure(Throwable caught) {
        super.onFailure(caught);
        requestInFlight = false;
        callback.setRequestInFlight(false);
        stopPollingStatus();
        callback.addAiMessage(MESSAGES.aiChatSendError() + ": " + caught.getMessage(), System.currentTimeMillis());
      }
    });
  }

  /**
   * Sends a message to the AI agent with an additional context hint.
   * The context hint is attached to the request and prepended to the
   * user message server-side before the LLM call.
   *
   * @param text the user's message text (shown in chat)
   * @param contextHint additional context for the LLM (hidden from chat)
   */
  public void sendMessageWithContext(String text, String contextHint) {
    AIAgentRequest request = contextCollector.buildRequest(text);
    request.setConversationId(currentConversationId);
    if (contextHint != null && !contextHint.isEmpty()) {
      request.setContextHint(contextHint);
    }
    requestInFlight = true;
    callback.setRequestInFlight(true);
    validationRetryCount = 0;
    executionRetryCount = 0;
    originalToolCount = 0;
    preservedValidOps = null;
    preservedAiMessage = null;
    CompanionBridge.getInstance().resetTurnBudget();
    startPollingStatus();

    aiAgentService.processRequest(request, new OdeAsyncCallback<AIAgentResponse>(
        MESSAGES.aiChatSendError()) {
      @Override
      public void onSuccess(AIAgentResponse response) {
        handleResponseWithValidation(response);
      }

      @Override
      public void onFailure(Throwable caught) {
        super.onFailure(caught);
        requestInFlight = false;
        callback.setRequestInFlight(false);
        stopPollingStatus();
        callback.addAiMessage(MESSAGES.aiChatSendError() + ": " + caught.getMessage(), System.currentTimeMillis());
      }
    });
  }

  /**
   * Applies the current batch and automatically applies all subsequent
   * batches without requiring user confirmation for each one.
   */
  public void applyAndAcceptAll() {
    if (orchestrationManager != null && orchestrationManager.isActive()) {
      orchestrationManager.setAutoAcceptAll();
      return;
    }
    setAutoAcceptAll(true);
    applyOperations();
  }

  /**
   * Applies the pending AI operations via the {@link AIOperationExecutor}.
   * Operations are executed asynchronously in phases; the callback reports
   * the final result. If the response has more batches ({@code hasMore}),
   * a continuation request is sent after successful application.
   */
  public void applyOperations() {
    if (orchestrationManager != null && orchestrationManager.isActive()) {
      orchestrationManager.approveBatch();
      return;
    }

    if (pendingResponse == null) {
      return;
    }

    final List<AIOperation> operations = pendingResponse.getOperations();
    final boolean hasMore = pendingResponse.hasMore();
    final String deferredMessage = pendingResponse.getAiMessage();
    pendingResponse = null;
    callback.hideOperationPreview();
    requestInFlight = true;
    callback.setRequestInFlight(true);

    AIOperationExecutor executor = new AIOperationExecutor();
    executor.execute(operations,
        new AIOperationExecutor.ExecutionCallback() {
          @Override
          public void onComplete(AIOperationExecutor.ExecutionResult result) {
            if (result.isSuccess()) {
              // Show the deferred AI message (LLM explanatory text), if any
              if (deferredMessage != null && !deferredMessage.isEmpty()) {
                callback.addAiMessage(deferredMessage, System.currentTimeMillis());
              }
              callback.addAiMessage(AIOperationFormatter.buildAppliedSummary(operations),
                  System.currentTimeMillis());
              if (hasMore) {
                // More batches expected — request the next one
                startPollingStatus();
                fetchContinuation();
              } else {
                setAutoAcceptAll(false);
                requestInFlight = false;
                callback.setRequestInFlight(false);
              }
            } else {
              // Execution failed — report to server for LLM retry
              reportExecutionErrorsToServer(result);
            }
          }
        });
  }

  /**
   * Rejects the pending operations and sends feedback to the server
   * so the AI knows the operations were discarded.
   */
  public void rejectOperations() {
    if (orchestrationManager != null && orchestrationManager.isActive()) {
      orchestrationManager.rejectBatch("User rejected the operations.");
      // CompletionCallback.onOrchestrationRejected handles cleanup + parent feedback
      return;
    }

    if (pendingResponse == null) {
      return;
    }

    setAutoAcceptAll(false);
    pendingResponse = null;
    callback.addAiMessage(MESSAGES.aiChatOperationsRejected(), System.currentTimeMillis());
    callback.hideOperationPreview();

    // Inform the server about the rejection by sending a feedback message
    long projectId = contextCollector.getCurrentProjectId();
    if (projectId == 0) {
      return;
    }
    AIAgentRequest feedback = contextCollector.buildRequest(
        "The user rejected the proposed operations. Please suggest alternatives.");
    feedback.setConversationId(currentConversationId);
    feedback.setPlatformMessage(true);

    requestInFlight = true;
    callback.setRequestInFlight(true);
    validationRetryCount = 0;
    executionRetryCount = 0;
    originalToolCount = 0;
    preservedValidOps = null;
    preservedAiMessage = null;
    CompanionBridge.getInstance().resetTurnBudget();
    startPollingStatus();

    aiAgentService.processRequest(feedback, new OdeAsyncCallback<AIAgentResponse>(
        MESSAGES.aiChatSendError()) {
      @Override
      public void onSuccess(AIAgentResponse response) {
        handleResponseWithValidation(response);
      }

      @Override
      public void onFailure(Throwable caught) {
        super.onFailure(caught);
        requestInFlight = false;
        callback.setRequestInFlight(false);
        stopPollingStatus();
      }
    });
  }

  /**
   * Applies per-request bits from an {@link AIStreamStatus} poll result:
   * conversation ID for the feedback link and a one-shot debug banner.
   * Static feature flags are not read from the status — see
   * {@link Ode#getSystemConfig()}.
   */
  private void applyConfig(AIStreamStatus status) {
    if (status == null) {
      return;
    }
    if (isDebugEnabled() && !debugBannerShown) {
      debugBannerShown = true;
      callback.showDebugBanner();
    }
    if (status.getConversationId() != null) {
      callback.setFeedbackContext(isDebugEnabled(), status.getConversationId());
    }
    callback.onConfigLoaded();
  }

  /** Whether AI agent debug logging is compiled in. */
  public boolean isDebugEnabled() {
    return AppInventorFeatures.aiAgentDebugEnabled();
  }

  /** Whether the AI agent orchestration feature is compiled in. */
  public boolean isOrchestrationEnabled() {
    return AppInventorFeatures.aiAgentOrchestrationEnabled();
  }

  /** Whether the AI agent plan-edit feature is compiled in. */
  public boolean isPlanEditEnabled() {
    return AppInventorFeatures.aiAgentPlanEditEnabled();
  }

  /** Whether AI agent editing modes (ScreenEditor / ProjectEditor) are compiled in. */
  public boolean isEditingModesEnabled() {
    return AppInventorFeatures.aiAgentEditingModesEnabled();
  }

  /**
   * Resets per-conversation client-side state without touching the server.
   * Called by {@link AIChatDialog#onProjectChanged} so that stale retry
   * counters, preserved ops, auto-accept, pending preview, etc. from the
   * previous project never leak into the next one.
   *
   * <p>Does NOT clear the renderer — {@link #loadConversation} (or the
   * dialog's clear path on pending-explain) will do that when
   * appropriate.</p>
   */
  public void resetConversationState() {
    if (orchestrationManager != null) {
      orchestrationManager.cancelAll();
      orchestrationManager = null;
    }
    if (requestInFlight) {
      cancelInFlight();
    }
    AIEditorState.setPlanApproved(false);
    setAutoAcceptAll(false);
    validationRetryCount = 0;
    executionRetryCount = 0;
    originalToolCount = 0;
    preservedValidOps = null;
    preservedAiMessage = null;
    pendingResponse = null;
  }

  /**
   * Returns the currently active conversation UUID, or null if a new
   * conversation has not been started yet.
   */
  public String getCurrentConversationId() {
    return currentConversationId;
  }

  /**
   * Manually sets the current conversation ID. Callers should normally
   * rely on {@link #loadConversation} or response echoes; use this only
   * when hydrating state from an external source.
   */
  public void setCurrentConversationId(String id) {
    updateCurrentConversationId(id);
  }

  /**
   * Centralized setter for {@link #currentConversationId}. Also notifies
   * the callback with the feedback context so that AI chat bubbles can
   * render "Share Feedback" links once the server assigns a convId.
   */
  private void updateCurrentConversationId(String convId) {
    this.currentConversationId = convId;
    if (convId != null && !convId.isEmpty()) {
      callback.setFeedbackContext(isDebugEnabled(), convId);
    }
  }

  /**
   * Fetches all conversations for the current project.
   */
  public void listConversations(OdeAsyncCallback<List<AIConversationSummary>> cb) {
    long projectId = contextCollector.getCurrentProjectId();
    if (projectId == 0) {
      return;
    }
    aiAgentService.listConversations(projectId, cb);
  }

  /**
   * Loads an existing conversation: cancels any in-flight request, clears
   * the chat, switches the active conversation id, and replays the stored
   * history into the chat UI.
   *
   * @param convId the conversation UUID to load
   * @param onDone optional callback invoked after history has rendered
   */
  public void loadConversation(String convId, final Runnable onDone) {
    cancelInFlight();
    callback.clearChatHistory();
    updateCurrentConversationId(convId);
    aiAgentService.getConversationHistory(convId,
        new OdeAsyncCallback<List<AIConversationMessage>>(MESSAGES.aiChatLoadHistoryError()) {
          @Override
          public void onSuccess(List<AIConversationMessage> messages) {
            if (messages != null) {
              for (AIConversationMessage m : messages) {
                if ("user".equals(m.getRole())) {
                  callback.addUserMessage(m.getText(), m.getTimestamp());
                } else {
                  callback.addAiMessage(m.getText(), m.getTimestamp());
                }
              }
            }
            if (onDone != null) {
              onDone.run();
            }
          }
        });
  }

  /**
   * Starts a new (empty) conversation. No server call — the server mints
   * the conversation on the first message, and its id is echoed back on
   * the response.
   */
  public void newConversation() {
    cancelInFlight();
    callback.clearChatHistory();
    currentConversationId = null;
    AIEditorState.setPlanApproved(false);
    setAutoAcceptAll(false);
  }

  /**
   * Renames a conversation via RPC.
   */
  public void renameConversation(String convId, String newTitle,
      OdeAsyncCallback<AIConversationSummary> cb) {
    aiAgentService.renameConversation(convId, newTitle, cb);
  }

  /**
   * Deletes a conversation via RPC. Does not touch the active conversation
   * pointer — callers should follow up with {@link #newConversation} if the
   * active conversation was the one deleted.
   */
  public void deleteConversation(String convId, OdeAsyncCallback<Void> cb) {
    aiAgentService.deleteConversation(convId, cb);
  }

  /**
   * One-shot status poll to pick up the conversation ID and feature flags.
   * When {@code then} is non-null, runs it after the response is applied
   * (or immediately on early-out / failure) so callers that need the flags
   * before rendering can chain on completion.
   */
  private void ensureFeedbackContext(final Runnable then) {
    long projectId = contextCollector.getCurrentProjectId();
    if (projectId == 0) {
      if (then != null) then.run();
      return;
    }
    aiAgentService.getRequestStatus(projectId,
        new OdeAsyncCallback<AIStreamStatus>() {
          @Override
          public void onSuccess(AIStreamStatus status) {
            applyConfig(status);
            if (then != null) then.run();
          }

          @Override
          public void onFailure(Throwable caught) {
            // Fall back to defaults so the UI can still proceed.
            if (then != null) then.run();
          }
        });
  }

  /**
   * One-shot status poll. Picks up the conversation ID for the feedback link
   * and triggers {@link ChatCallback#onConfigLoaded()}. AI agent feature
   * toggles come from {@link AppInventorFeatures} (compile-time) and are
   * already available before this call returns.
   */
  public void loadConfig() {
    ensureFeedbackContext(null);
  }

  /**
   * Cancels any in-flight request, stops polling, and resets state.
   */
  public void cancelInFlight() {
    if (orchestrationManager != null && orchestrationManager.isActive()) {
      orchestrationManager.cancelAll();
      orchestrationManager = null;
      callback.onPlanExecutionFinished();
    }
    setAutoAcceptAll(false);
    requestInFlight = false;
    pendingResponse = null;
    preservedValidOps = null;
    preservedAiMessage = null;
    executionRetryCount = 0;
    originalToolCount = 0;
    callback.setRequestInFlight(false);
    stopPollingStatus();
    callback.hideOperationPreview();
  }

  /**
   * Cancels the in-flight request: finalizes streaming UI, resets client
   * state, and fires a server-side cancel RPC to abort LLM processing.
   */
  public void cancelRequest() {
    if (!requestInFlight) {
      return;
    }

    // Finalize streaming bubble so partial text is preserved.
    if (streamingActive) {
      callback.finalizeStreamingBubble(null);
      streamingActive = false;
    }

    // Reset client state (stops polling, re-enables UI).
    cancelInFlight();

    // Fire server-side cancel RPC (fire-and-forget).
    long projectId = contextCollector.getCurrentProjectId();
    if (projectId != 0) {
      aiAgentService.cancelRequest(projectId, new OdeAsyncCallback<Void>(
          MESSAGES.aiChatSendError()) {
        @Override
        public void onSuccess(Void result) {
          // Nothing to do — cancellation is best-effort.
        }
      });
    }

    // Show cancellation message.
    callback.addAiMessage(MESSAGES.aiChatRequestCancelled(), System.currentTimeMillis());
  }

  /**
   * Resets the auto-accept-all flag without affecting other state.
   * Called when the dialog is closed or the active project changes.
   */
  public void resetAutoAcceptAll() {
    setAutoAcceptAll(false);
  }

  /**
   * Returns whether a request is currently in flight.
   */
  public boolean isRequestInFlight() {
    return requestInFlight;
  }

  /**
   * Returns whether a plan proposal is awaiting user approval.
   */
  public boolean hasPendingPlanProposal() {
    return pendingPlanProposal;
  }

  /**
   * Dismisses a pending plan proposal as rejected. Called when the user
   * sends a new message instead of clicking the plan card buttons.
   */
  public void dismissPendingPlan() {
    pendingPlanProposal = false;
  }

  /**
   * Returns the pending AI response awaiting user approval, or null.
   */
  public AIAgentResponse getPendingResponse() {
    return pendingResponse;
  }

  // ---- Plan proposal handling ----

  /**
   * Handles a PROPOSE_PLAN response by rendering a plan card in the chat
   * with approve/reject buttons. The AI message (if any) is shown above
   * the card.
   */
  private void handlePlanProposal(AIAgentResponse response) {
    AIOperation planOp = response.getOperations().get(0);
    String planJson = planOp.getPayload();

    // Finalize any in-progress streaming bubble
    if (streamingActive) {
      callback.finalizeStreamingBubble(response.getAiMessage());
      streamingActive = false;
    } else {
      // Show the AI message if present
      String aiMessage = response.getAiMessage();
      if (aiMessage != null && !aiMessage.isEmpty()) {
        callback.addAiMessage(aiMessage, System.currentTimeMillis());
      }
    }

    // Render the plan card with approve/reject
    pendingPlanProposal = true;
    callback.renderPlanCard(planJson, new PlanApprovalCallback() {
      @Override
      public void onApprove(String approvedPlanJson) {
        pendingPlanProposal = false;
        executePlanSequentially(approvedPlanJson);
      }

      @Override
      public void onReject() {
        pendingPlanProposal = false;
        callback.addAiMessage(MESSAGES.aiChatPlanRejected(), System.currentTimeMillis());
        String feedback = "The user rejected the proposed plan. "
            + "Please suggest alternatives or ask what they'd like changed.";
        sendPlatformMessage(feedback);
      }
    });
  }

  /**
   * Dispatches an approved plan for execution. Plans with two or more
   * screen-level steps are executed via the parallel orchestration manager;
   * single-screen plans fall back to the Phase A sequential path where
   * the parent agent executes step by step.
   */
  private void executePlanSequentially(String planJson) {
    // Show loading state immediately so the user sees feedback after
    // pressing Approve (Bug fix: gap between Approve and first batch).
    requestInFlight = true;
    callback.setRequestInFlight(true);

    AIEditorState.setPlanApproved(true);
    int screenStepCount = countScreenSteps(planJson);

    if (screenStepCount <= 1) {
      // Single screen — sequential execution via parent agent (Phase A path)
      callback.onPlanExecutionStarted();
      String planMessage = "The user approved the following execution plan. "
          + "Execute it step by step, one screen at a time. "
          + "Use switch_screen to navigate between screens as needed. "
          + "Use toggle_editor to switch between Designer and Blocks views. "
          + "Work through each step in dependency order.\n\n"
          + planJson;
      sendPlatformMessage(planMessage);
    } else {
      // Multiple screens — parallel orchestration
      callback.onPlanExecutionStarted();
      orchestrationManager = new AIOrchestrationManager(contextCollector);
      orchestrationManager.executePlan(planJson, callback,
          new AIOrchestrationManager.CompletionCallback() {
            @Override
            public void onOrchestrationComplete(String summary) {
              List<String> screens = orchestrationManager.getAppliedScreens();
              orchestrationManager = null;
              String screenStates = collectScreenStates(screens);
              sendPlatformMessage(summary
                  + "\n\nCurrent state of modified screens:\n" + screenStates
                  + "\nProvide a brief summary to the user of what was built.");
            }

            @Override
            public void onOrchestrationRejected(String summary) {
              List<String> screens = orchestrationManager.getAppliedScreens();
              orchestrationManager = null;
              String screenStates = screens.isEmpty() ? "" :
                  "\n\nCurrent state of modified screens:\n" + collectScreenStates(screens);
              sendPlatformMessage(summary + screenStates
                  + "\nThe user stopped the plan execution. Ask what they'd like "
                  + "changed, or propose a revised plan.");
            }

            @Override
            public void onOrchestrationCancelled() {
              orchestrationManager = null;
            }
          });
    }
  }

  /**
   * Counts the number of screen-level steps in the plan JSON (steps where
   * the screen is not {@code __project__}).
   *
   * @param planJson the plan JSON string
   * @return number of distinct screen-level steps
   */
  private int countScreenSteps(String planJson) {
    try {
      JSONValue parsed = JSONParser.parseStrict(planJson);
      JSONArray stepsArr = null;

      JSONObject planObj = parsed.isObject();
      if (planObj != null) {
        JSONValue stepsVal = planObj.get("steps");
        if (stepsVal != null) {
          stepsArr = stepsVal.isArray();
        }
      }
      if (stepsArr == null) {
        stepsArr = parsed.isArray();
      }
      if (stepsArr == null) {
        return 0;
      }

      int count = 0;
      for (int i = 0; i < stepsArr.size(); i++) {
        JSONValue stepVal = stepsArr.get(i);
        JSONObject step = stepVal.isObject();
        if (step == null) {
          continue;
        }
        JSONValue screenVal = step.get("screen");
        if (screenVal == null) {
          continue;
        }
        JSONString screenStr = screenVal.isString();
        if (screenStr != null && !"__project__".equals(screenStr.stringValue())) {
          count++;
        }
      }
      return count;
    } catch (Exception e) {
      LOG.warning("Failed to parse plan JSON for step count: " + e.getMessage());
      return 0;
    }
  }

  /**
   * Sends a platform message (system-generated, non-user-visible) to the
   * server via the normal processRequest RPC. The message is flagged as
   * a platform message and planExecuteMode is disabled (executing, not
   * planning).
   */
  /**
   * Collects the current component tree and blocks YAIL for each screen,
   * so the parent LLM has accurate, up-to-date state after orchestration.
   */
  private String collectScreenStates(List<String> screenNames) {
    StringBuilder sb = new StringBuilder();
    for (String screenName : screenNames) {
      sb.append("\n--- ").append(screenName).append(" ---\n");
      try {
        String components = contextCollector.getScreenComponentsJson(screenName);
        if (components != null && !components.isEmpty()) {
          sb.append("Components: ").append(components).append("\n");
        }
      } catch (Exception e) {
        sb.append("Components: (unavailable)\n");
      }
      try {
        String blocks = contextCollector.getScreenBlocksYail(screenName);
        if (blocks != null && !blocks.isEmpty()) {
          sb.append("Blocks:\n").append(blocks).append("\n");
        } else {
          sb.append("Blocks: (none)\n");
        }
      } catch (Exception e) {
        sb.append("Blocks: (unavailable)\n");
      }
    }
    return sb.toString();
  }

  private void sendPlatformMessage(String message) {
    requestInFlight = true;
    callback.setRequestInFlight(true);
    validationRetryCount = 0;
    executionRetryCount = 0;
    originalToolCount = 0;
    preservedValidOps = null;
    preservedAiMessage = null;

    AIAgentRequest request = contextCollector.buildRequest(message);
    request.setConversationId(currentConversationId);
    request.setPlatformMessage(true);
    request.setPlanExecuteMode(false);

    startPollingStatus();
    aiAgentService.processRequest(request, new OdeAsyncCallback<AIAgentResponse>(
        MESSAGES.aiChatSendError()) {
      @Override
      public void onSuccess(AIAgentResponse response) {
        handleResponseWithValidation(response);
      }

      @Override
      public void onFailure(Throwable caught) {
        super.onFailure(caught);
        requestInFlight = false;
        callback.setRequestInFlight(false);
        stopPollingStatus();
        callback.addAiMessage(MESSAGES.aiChatSendError() + ": " + caught.getMessage(), System.currentTimeMillis());
      }
    });
  }

  // ---- Validation ----

  /**
   * Handles an AI response by pre-validating block operations before
   * showing the preview. If validation fails and retries are available,
   * keeps "Calling AI" visible and sends error feedback to the server
   * for an automatic LLM retry.
   *
   * <p>Valid operations from a batch with mixed results are preserved
   * across retries in {@link #preservedValidOps} and merged back once
   * all remaining failures are fixed (or retries are exhausted).</p>
   */
  private void handleResponseWithValidation(AIAgentResponse response) {
    // Discard stale RPC responses that arrive after the user cancelled.
    if (!requestInFlight) {
      return;
    }

    // Track the conversation ID the server assigned / resumed. This is set
    // on every response from processRequest / continueRequest /
    // reportExecutionErrors.
    if (response != null && response.getConversationId() != null) {
      updateCurrentConversationId(response.getConversationId());
    }

    List<AIOperation> operations = response.getOperations();

    // Intercept runtime-read operations: resolve client-side and feed results
    // back to the LLM via reportExecutionErrors. Never shown in the preview.
    if (operations != null && !operations.isEmpty()) {
      List<AIOperation> readOps = new ArrayList<>();
      List<AIOperation> nonReadOps = new ArrayList<>();
      for (AIOperation op : operations) {
        if (op.getType() == AIOperation.Type.READ_RUNTIME) {
          readOps.add(op);
        } else {
          nonReadOps.add(op);
        }
      }
      if (!readOps.isEmpty()) {
        // Any non-read ops in this response are dropped; the LLM will re-emit on next turn.
        resolveRuntimeReads(readOps);
        return;
      }
    }

    // Pre-validate WRITE_BLOCK and DELETE_BLOCK operations client-side
    if (operations != null && !operations.isEmpty()) {
      Map<Integer, String> validationErrors = validateBlockOperations(operations);
      if (!validationErrors.isEmpty()) {
        // Separate valid operations from invalid ones
        List<AIOperation> validOps = new ArrayList<>();
        List<AIOperation> invalidOps = new ArrayList<>();
        for (int i = 0; i < operations.size(); i++) {
          if (validationErrors.containsKey(i)) {
            invalidOps.add(operations.get(i));
          } else {
            validOps.add(operations.get(i));
          }
        }

        if (validationRetryCount < MAX_VALIDATION_RETRIES) {
          // Preserve the valid operations and original AI message
          if (preservedValidOps == null) {
            preservedValidOps = new ArrayList<>(validOps);
            preservedAiMessage = response.getAiMessage();
            originalToolCount = operations.size();
          } else {
            preservedValidOps.addAll(validOps);
          }

          // Send only the validation errors for the failed operations.
          // The valid operations are preserved client-side and will be
          // merged back once the LLM fixes the failures.
          validationRetryCount++;
          LOG.info("Client validation failed (attempt " + validationRetryCount
              + "/" + MAX_VALIDATION_RETRIES + "), retrying. Preserved "
              + preservedValidOps.size() + " valid op(s), "
              + invalidOps.size() + " failed.");
          reportValidationErrors(operations, validationErrors);
          return;
        }

        // Exhausted retries — combine preserved valid ops with any valid
        // ops from this final response, strip only the still-invalid ones.
        LOG.warning("Validation retries exhausted. Stripping "
            + validationErrors.size() + " invalid block operation(s).");
        List<AIOperation> merged = new ArrayList<>();
        if (preservedValidOps != null) {
          merged.addAll(preservedValidOps);
        }
        merged.addAll(validOps);
        response.setOperations(merged);
        for (String err : validationErrors.values()) {
          response.getErrors().add(err);
        }
        if (preservedAiMessage != null) {
          response.setAiMessage(preservedAiMessage);
        }
        preservedValidOps = null;
        preservedAiMessage = null;
      }
    }

    // If validation passed and there are preserved ops from earlier
    // retries, merge them into the response.
    if (preservedValidOps != null && !preservedValidOps.isEmpty()) {
      List<AIOperation> merged = new ArrayList<>(preservedValidOps);
      if (operations != null) {
        merged.addAll(operations);
      }
      response.setOperations(merged);
      if (preservedAiMessage != null) {
        response.setAiMessage(preservedAiMessage);
      }
      preservedValidOps = null;
      preservedAiMessage = null;
    }

    // Validation passed (or no block ops, or retries exhausted) — show result
    requestInFlight = false;
    callback.setRequestInFlight(false);
    stopPollingStatus();
    // Ensure feedback context is available — the request may have completed
    // before the first status poll fired, so do a one-shot fetch.  The
    // retroactive scan in setFeedbackContext will patch up any messages
    // that were rendered before the conversation ID arrived.
    ensureFeedbackContext(null);
    handleResponse(response);
  }

  /**
   * Processes a successful AI agent response.
   *
   * <p>When the response contains operations that require user approval,
   * the AI message is deferred — it is not shown in the chat until the
   * user applies or rejects.</p>
   */
  private void handleResponse(AIAgentResponse response) {
    List<AIOperation> operations = response.getOperations();
    boolean hasOps = operations != null && !operations.isEmpty();

    // Intercept PROPOSE_PLAN responses — render a plan card instead of
    // the normal operation preview.
    if (hasOps && operations.size() == 1
        && operations.get(0).getType() == AIOperation.Type.PROPOSE_PLAN) {
      handlePlanProposal(response);
      return;
    }

    // Finalize streaming bubble or display the AI message.
    String aiMessage = response.getAiMessage();
    if (streamingActive) {
      // Always finalize the streaming bubble to remove the typing indicator.
      // When aiMessage is null/empty, the renderer keeps whatever text was
      // already accumulated from streaming deltas.
      callback.finalizeStreamingBubble(aiMessage);
      if (hasOps && aiMessage != null && !aiMessage.isEmpty()) {
        // Text is already visible in the finalized streaming bubble.
        // Clear it so applyOperations() doesn't add it again as a
        // deferred message.
        response.setAiMessage(null);
      }
      streamingActive = false;
    } else if (aiMessage != null && !aiMessage.isEmpty()) {
      // No streaming — show the AI message as a chat bubble.
      callback.addAiMessage(aiMessage, System.currentTimeMillis());
      if (hasOps) {
        // Clear so applyOperations() doesn't add it again as a deferred
        // message and showPreview() doesn't render it in the preview panel.
        response.setAiMessage(null);
      }
    }

    // Display any errors
    List<String> errors = response.getErrors();
    if (errors != null && !errors.isEmpty()) {
      for (String error : errors) {
        callback.addAiMessage("Error: " + error, System.currentTimeMillis());
      }
    }

    // Show operation preview if there are operations
    if (hasOps) {
      pendingResponse = response;
      if (autoAcceptAll) {
        // Auto-accept mode: skip user confirmation and apply immediately
        applyOperations();
      } else {
        callback.showOperationPreview(response);
      }
    }
  }

  /**
   * Validates WRITE_BLOCK and DELETE_BLOCK operations using the client-side
   * Blockly runtime (dry-run, no blocks created).
   *
   * @return map from operation index to error message for failed operations;
   *         empty if all operations are valid
   */
  private Map<Integer, String> validateBlockOperations(List<AIOperation> operations) {
    Map<Integer, String> errors = new HashMap<>();
    BlocksEditor<?, ?> blocksEditor = contextCollector.getCurrentBlocksEditor();
    if (blocksEditor == null) {
      // Can't validate without a blocks editor — let execution handle it
      return errors;
    }

    for (int i = 0; i < operations.size(); i++) {
      AIOperation op = operations.get(i);
      if (op.getType() == AIOperation.Type.WRITE_BLOCK) {
        String yail = AIJsonUtils.extractField(op.getPayload(), "yail");
        if (yail != null && !yail.isEmpty()) {
          String resultJson = blocksEditor.validateYail(yail);
          if (resultJson != null) {
            String error = extractValidationError(resultJson);
            if (error != null) {
              // Include the failing block code so the LLM can see and fix its mistake
              errors.put(i, "write_block validation failed: " + error
                  + "\nFailing block code: " + yail);
            }
          }
        }
      } else if (op.getType() == AIOperation.Type.DELETE_BLOCK) {
        String block = AIJsonUtils.extractField(op.getPayload(), "block");
        if (block != null && !block.isEmpty()) {
          String resultJson = blocksEditor.validateDeleteId(block);
          if (resultJson != null) {
            String error = extractValidationError(resultJson);
            if (error != null) {
              errors.put(i, "delete_block validation failed: " + error);
            }
          }
        }
      }
    }
    return errors;
  }

  /**
   * Extracts the error message from a validation result JSON string.
   * Returns null if valid, the error string otherwise.
   */
  private static String extractValidationError(String resultJson) {
    if (resultJson.contains("\"valid\":true")
        || resultJson.contains("\"valid\": true")) {
      return null;
    }
    String error = AIJsonUtils.extractField(resultJson, "error");
    // extractField returns the field name as fallback — treat that as "unknown error"
    if ("error".equals(error)) {
      return "unknown validation error";
    }
    return error;
  }

  /**
   * Reports client-side validation errors to the server for LLM retry.
   * Keeps "Calling AI" visible and requestInFlight=true during the retry.
   *
   * <p>Builds structured {@link AIOperationResult} DTOs so the server
   * knows exactly which operations passed validation (and should NOT be
   * re-emitted) and which failed (and need fixing).</p>
   */
  private void reportValidationErrors(List<AIOperation> operations,
      Map<Integer, String> validationErrors) {
    long projectId = contextCollector.getCurrentProjectId();
    if (projectId == 0) {
      requestInFlight = false;
      callback.setRequestInFlight(false);
      stopPollingStatus();
      return;
    }

    // Build structured per-operation results using typed DTOs.
    List<AIOperationResult> results = new ArrayList<>();
    for (int i = 0; i < operations.size(); i++) {
      AIOperation op = operations.get(i);
      if (validationErrors.containsKey(i)) {
        results.add(AIOperationResult.failed(
            AIOperationFormatter.formatOperation(op), validationErrors.get(i)));
      } else {
        results.add(AIOperationResult.succeeded(
            AIOperationFormatter.formatOperation(op)));
      }
    }

    // Keep polling — "Calling AI" stays visible
    sendRetryRequest(results, validationRetryCount, originalToolCount);
  }

  // ---- Runtime reads ----

  /**
   * Callback interface for async runtime-read resolution.
   */
  private interface RuntimeReadCallback {
    void onResult(AIOperationResult result);
  }

  /**
   * Resolves a batch of READ_RUNTIME operations by dispatching each through
   * the appropriate path (CompanionBridge for property/variable reads, the
   * client-side log buffer for read_recent_logs) and feeding all results
   * back to the LLM via reportExecutionErrors.
   */
  private void resolveRuntimeReads(final List<AIOperation> readOps) {
    startPollingStatus();
    validationRetryCount = 0;
    if (executionRetryCount == 0) {
      originalToolCount = readOps.size();
    }
    executionRetryCount++;

    final List<AIOperationResult> results = new ArrayList<>(
        Collections.nCopies(readOps.size(), (AIOperationResult) null));
    final int[] remaining = new int[] { readOps.size() };
    final int retryAttempt = executionRetryCount;
    final int totalTools = originalToolCount;

    for (int i = 0; i < readOps.size(); i++) {
      final int index = i;
      AIOperation op = readOps.get(i);
      resolveOneRead(op, new RuntimeReadCallback() {
        @Override
        public void onResult(AIOperationResult result) {
          results.set(index, result);
          remaining[0]--;
          if (remaining[0] == 0) {
            sendRetryRequest(results, retryAttempt, totalTools);
          }
        }
      });
    }
  }

  /**
   * Resolves a single READ_RUNTIME operation, invoking the callback with
   * the result. Dispatches to CompanionBridge for property/variable reads,
   * or reads the client-side log buffer for read_recent_logs.
   */
  private void resolveOneRead(AIOperation op, final RuntimeReadCallback cb) {
    JSONObject payload = parseRuntimePayload(op.getPayload());
    String tool = payload != null ? AIJsonUtils.getStringField(payload, "tool") : null;
    JSONObject args = null;
    if (payload != null && payload.containsKey("args")) {
      JSONValue argsVal = payload.get("args");
      if (argsVal != null) {
        args = argsVal.isObject();
      }
    }

    if (tool == null || args == null) {
      cb.onResult(AIOperationResult.failed("read_runtime(malformed)",
          "Payload missing 'tool' or 'args'"));
      return;
    }

    if ("read_component_property".equals(tool)) {
      String err = CompanionReadValidator.validateReadComponentProperty(args);
      if (err != null) {
        cb.onResult(AIOperationResult.failed(formatReadSummary(tool, args), err));
        return;
      }
      final String component = AIJsonUtils.getStringField(args, "component_name");
      final String property = AIJsonUtils.getStringField(args, "property_name");
      CompanionBridge.getInstance().readComponentProperty(
          component, property, new CompanionBridge.Callback() {
            @Override
            public void onSuccess(String value) {
              // value already carries the Scheme display representation
              // (e.g. "fgg" with quotes for strings) — don't re-wrap.
              cb.onResult(AIOperationResult.runtimeRead(
                  "read_component_property(" + component + "." + property + ") = " + value));
            }

            @Override
            public void onFailure(String error) {
              cb.onResult(AIOperationResult.failed(
                  "read_component_property(" + component + "." + property + ")", error));
            }
          });

    } else if ("read_variable".equals(tool)) {
      String blocksYail = contextCollector.getScreenBlocksYail(
          contextCollector.getCurrentScreenName());
      String err = CompanionReadValidator.validateReadVariable(args, blocksYail);
      if (err != null) {
        cb.onResult(AIOperationResult.failed(formatReadSummary(tool, args), err));
        return;
      }
      final String variable = AIJsonUtils.getStringField(args, "variable_name");
      CompanionBridge.getInstance().readVariable(variable, new CompanionBridge.Callback() {
        @Override
        public void onSuccess(String value) {
          cb.onResult(AIOperationResult.runtimeRead(
              "read_variable(" + variable + ") = " + value));
        }

        @Override
        public void onFailure(String error) {
          cb.onResult(AIOperationResult.failed("read_variable(" + variable + ")", error));
        }
      });

    } else if ("read_recent_logs".equals(tool)) {
      String err = CompanionReadValidator.validateReadRecentLogs(args);
      if (err != null) {
        cb.onResult(AIOperationResult.failed("read_recent_logs", err));
        return;
      }
      int n = 20;
      if (args.containsKey("n")) {
        JSONValue nVal = args.get("n");
        if (nVal != null && nVal.isNumber() != null) {
          n = (int) nVal.isNumber().doubleValue();
        }
      }
      if (n < 1) n = 1;
      if (n > 50) n = 50;
      String rendered = renderRecentLogs(n);
      cb.onResult(AIOperationResult.runtimeRead("read_recent_logs(" + n + ") = " + rendered));

    } else {
      cb.onResult(AIOperationResult.failed("read_runtime", "Unknown tool: " + tool));
    }
  }

  /**
   * Reads up to n recent log entries from the client-side ring buffer
   * populated by replmgr.js processRetvals.
   */
  private native String renderRecentLogs(int n) /*-{
    var buf = ($wnd.top.Blockly && $wnd.top.Blockly.ReplMgr
        && $wnd.top.Blockly.ReplMgr.aiLogBuffer)
        ? $wnd.top.Blockly.ReplMgr.aiLogBuffer : [];
    var start = Math.max(0, buf.length - n);
    var out = [];
    for (var i = start; i < buf.length; i++) {
      var e = buf[i];
      out.push('[' + (e.level || 'info') + '] ' + (e.text || ''));
    }
    return out.length === 0 ? '(no recent logs)' : out.join('\n');
  }-*/;

  /**
   * Parses the JSON payload of a READ_RUNTIME operation.
   * Returns null if the payload is absent or unparseable.
   */
  private JSONObject parseRuntimePayload(String payloadJson) {
    if (payloadJson == null || payloadJson.isEmpty()) {
      return null;
    }
    try {
      JSONValue v = JSONParser.parseStrict(payloadJson);
      return v.isObject();
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Returns a JSON-quoted representation of a runtime read value.
   */
  private String quoteValue(String v) {
    return v == null ? "\"\"" : "\"" + v.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
  }

  /**
   * Formats a compact summary of a read tool call for use in operation results.
   * Example: read_component_property(component_name=Button1, property_name=Text)
   */
  private String formatReadSummary(String tool, JSONObject args) {
    StringBuilder sb = new StringBuilder(tool).append("(");
    boolean first = true;
    for (String k : args.keySet()) {
      if (!first) {
        sb.append(", ");
      }
      sb.append(k).append('=').append(args.get(k).toString());
      first = false;
    }
    return sb.append(')').toString();
  }

  // ---- Continuation ----

  /**
   * Calls the continueRequest RPC to fetch the next batch of operations
   * from a multi-step AI response.
   */
  private void fetchContinuation() {
    long projectId = contextCollector.getCurrentProjectId();
    if (projectId == 0) {
      requestInFlight = false;
      callback.setRequestInFlight(false);
      stopPollingStatus();
      return;
    }

    validationRetryCount = 0;
    executionRetryCount = 0;
    originalToolCount = 0;
    preservedValidOps = null;
    preservedAiMessage = null;

    AIAgentRequest continueReq = contextCollector.buildRequest(null);
    continueReq.setConversationId(currentConversationId);
    aiAgentService.continueRequest(continueReq,
        new OdeAsyncCallback<AIAgentResponse>(MESSAGES.aiChatSendError()) {
          @Override
          public void onSuccess(AIAgentResponse response) {
            handleResponseWithValidation(response);
          }

          @Override
          public void onFailure(Throwable caught) {
            super.onFailure(caught);
            requestInFlight = false;
            callback.setRequestInFlight(false);
            stopPollingStatus();
            callback.addAiMessage(MESSAGES.aiChatSendError() + ": " + caught.getMessage(), System.currentTimeMillis());
          }
        });
  }

  // ---- Error feedback ----

  /**
   * Reports client-side execution errors to the server for LLM retry.
   */
  private void reportExecutionErrorsToServer(AIOperationExecutor.ExecutionResult result) {
    long projectId = contextCollector.getCurrentProjectId();
    if (projectId == 0) {
      requestInFlight = false;
      callback.setRequestInFlight(false);
      return;
    }

    if (executionRetryCount >= MAX_EXECUTION_RETRIES) {
      LOG.warning("Execution retries exhausted (" + MAX_EXECUTION_RETRIES
          + "). Stopping retry loop.");
      requestInFlight = false;
      callback.setRequestInFlight(false);
      callback.addAiMessage(MESSAGES.aiChatSendError()
          + ": execution failed after " + MAX_EXECUTION_RETRIES + " retries.",
          System.currentTimeMillis());
      return;
    }

    // Build structured per-operation results using typed DTOs.
    List<AIOperationResult> results = new ArrayList<>();
    for (AIOperation op : result.getSucceeded()) {
      results.add(AIOperationResult.succeeded(
          AIOperationFormatter.formatOperation(op)));
    }
    for (AIOperation op : result.getFailed()) {
      String mainError = result.getErrorMessage();
      results.add(AIOperationResult.failed(
          AIOperationFormatter.formatOperation(op),
          mainError != null ? mainError : "unknown"));
    }
    for (AIOperation op : result.getSkipped()) {
      results.add(AIOperationResult.skipped(
          AIOperationFormatter.formatOperation(op)));
    }

    startPollingStatus();
    validationRetryCount = 0;
    if (executionRetryCount == 0) {
      originalToolCount = results.size();
    }
    executionRetryCount++;
    sendRetryRequest(results, executionRetryCount, originalToolCount);
  }

  /**
   * Sends the retry RPC to the server with the given operation results
   * and retry attempt number. Shared by validation and execution retries.
   */
  private void sendRetryRequest(List<AIOperationResult> results, int retryAttempt,
      int totalTools) {
    AIAgentRequest retryRequest = contextCollector.buildRequest(null);
    retryRequest.setConversationId(currentConversationId);
    retryRequest.setRetryAttempt(retryAttempt);
    retryRequest.setTotalTools(totalTools);
    aiAgentService.reportExecutionErrors(retryRequest, results,
        new OdeAsyncCallback<AIAgentResponse>(MESSAGES.aiChatSendError()) {
          @Override
          public void onSuccess(AIAgentResponse response) {
            handleResponseWithValidation(response);
          }

          @Override
          public void onFailure(Throwable caught) {
            super.onFailure(caught);
            requestInFlight = false;
            callback.setRequestInFlight(false);
            stopPollingStatus();
            callback.addAiMessage(MESSAGES.aiChatSendError() + ": " + caught.getMessage(), System.currentTimeMillis());
          }
        });
  }

  // ---- Status polling ----

  /**
   * Starts a timer that polls getRequestStatus every second
   * to display intermediate progress while a request is in flight.
   */
  private void startPollingStatus() {
    stopPollingStatus();
    streamingActive = false;
    callback.setStatusText(MESSAGES.aiChatThinking());
    callback.setStatusVisible(true);

    pollingTimer = new Timer() {
      @Override
      public void run() {
        long projectId = contextCollector.getCurrentProjectId();
        if (projectId == 0 || !requestInFlight) {
          stopPollingStatus();
          return;
        }

        aiAgentService.getRequestStatus(projectId,
            new OdeAsyncCallback<AIStreamStatus>() {
              @Override
              public void onSuccess(AIStreamStatus status) {
                if (status == null) return;
                // Update config on every poll — picks up the conversation
                // ID once the server creates it (first message in a new
                // conversation) and ensures the debug banner is shown.
                applyConfig(status);
                if (status.getStatusText() != null) {
                  callback.setStatusText(status.getStatusText());
                }
                if (status.isResetStreaming() && streamingActive) {
                  // Narration retry: finalize the current bubble (keeps
                  // accumulated text, removes typing dots) so the user
                  // can still read it.  Reset streamingActive so the
                  // retry's text deltas create a fresh bubble below.
                  callback.finalizeStreamingBubble(null);
                  streamingActive = false;
                }
                if (status.getThinkingDelta() != null) {
                  if (!streamingActive) {
                    streamingActive = true;
                    callback.startStreamingBubble();
                    pollingTimer.cancel();
                    pollingTimer.scheduleRepeating(POLL_INTERVAL_FAST_MS);
                  }
                  callback.appendStreamingThinking(status.getThinkingDelta());
                }
                if (status.getTextDelta() != null) {
                  if (!streamingActive) {
                    streamingActive = true;
                    callback.startStreamingBubble();
                    pollingTimer.cancel();
                    pollingTimer.scheduleRepeating(POLL_INTERVAL_FAST_MS);
                  }
                  callback.appendStreamingText(status.getTextDelta());
                }
                if (status.isDone()) {
                  // Stream complete, but the RPC response may still be in
                  // flight (server-side parsing, tool-use loop iterations,
                  // Datastore writes).  Stop polling but keep the status
                  // indicator visible — it will be hidden when the RPC
                  // response arrives via stopPollingStatus().
                  if (pollingTimer != null) {
                    pollingTimer.cancel();
                    pollingTimer = null;
                  }
                }
              }

              @Override
              public void onFailure(Throwable caught) {
                // Polling failure is non-fatal
              }
            });
      }
    };
    pollingTimer.scheduleRepeating(POLL_INTERVAL_SLOW_MS);
  }

  /**
   * Stops the status polling timer.
   */
  public void stopPollingStatus() {
    if (pollingTimer != null) {
      pollingTimer.cancel();
      pollingTimer = null;
    }
    callback.setStatusVisible(false);
  }

  // ---- RPC configuration ----

  /**
   * Configures the RPC service proxy timeout.
   */
  private void configureServiceTimeout(AIAgentServiceAsync service) {
    if (service instanceof ServiceDefTarget) {
      ServiceDefTarget target = (ServiceDefTarget) service;
      target.setRpcRequestBuilder(new RpcRequestBuilder() {
        @Override
        protected void doFinish(com.google.gwt.http.client.RequestBuilder rb) {
          super.doFinish(rb);
          rb.setTimeoutMillis(RPC_TIMEOUT_MS);
        }
      });
    }
  }
}
