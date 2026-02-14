// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.aiagent;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.editor.blocks.BlocksEditor;
import com.google.appinventor.shared.rpc.aiagent.AIAgentRequest;
import com.google.appinventor.shared.rpc.aiagent.AIAgentResponse;
import com.google.appinventor.shared.rpc.aiagent.AIAgentService;
import com.google.appinventor.shared.rpc.aiagent.AIAgentServiceAsync;
import com.google.appinventor.shared.rpc.aiagent.AIConversationMessage;
import com.google.appinventor.shared.rpc.aiagent.AIOperation;
import com.google.appinventor.client.editor.youngandroid.aiagent.executor.AIOperationExecutor;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.RpcRequestBuilder;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

import java.util.ArrayList;
import java.util.List;
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

  /** Polling interval for request status (1 second). */
  private static final int POLL_INTERVAL_MS = 1000;

  /** Maximum number of client-side validation retries before showing the error. */
  private static final int MAX_VALIDATION_RETRIES = 5;

  /**
   * Callback interface for UI updates from the orchestrator.
   */
  public interface ChatCallback {
    void addUserMessage(String text);
    void addAiMessage(String text);
    void showOperationPreview(AIAgentResponse response);
    void hideOperationPreview();
    void setRequestInFlight(boolean inFlight);
    void setStatusText(String text);
    void setStatusVisible(boolean visible);
    void clearChatHistory();
  }

  private final AIContextCollector contextCollector;
  private final ChatCallback callback;
  private final AIAgentServiceAsync aiAgentService;

  // State
  private AIAgentResponse pendingResponse;
  private Timer pollingTimer;
  private boolean requestInFlight;
  private int validationRetryCount;
  private boolean autoAcceptAll;

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
    requestInFlight = true;
    callback.setRequestInFlight(true);
    validationRetryCount = 0;
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
        callback.addAiMessage(MESSAGES.aiChatSendError() + ": " + caught.getMessage());
      }
    });
  }

  /**
   * Applies the current batch and automatically applies all subsequent
   * batches without requiring user confirmation for each one.
   */
  public void applyAndAcceptAll() {
    autoAcceptAll = true;
    applyOperations();
  }

  /**
   * Applies the pending AI operations via the {@link AIOperationExecutor}.
   * Operations are executed asynchronously in phases; the callback reports
   * the final result. If the response has more batches ({@code hasMore}),
   * a continuation request is sent after successful application.
   */
  public void applyOperations() {
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
                callback.addAiMessage(deferredMessage);
              }
              callback.addAiMessage(AIOperationFormatter.buildAppliedSummary(operations));
              if (hasMore) {
                // More batches expected — request the next one
                startPollingStatus();
                fetchContinuation();
              } else {
                autoAcceptAll = false;
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
    if (pendingResponse == null) {
      return;
    }

    autoAcceptAll = false;
    pendingResponse = null;
    callback.addAiMessage(MESSAGES.aiChatOperationsRejected());
    callback.hideOperationPreview();

    // Inform the server about the rejection by sending a feedback message
    long projectId = contextCollector.getCurrentProjectId();
    if (projectId == 0) {
      return;
    }
    AIAgentRequest feedback = contextCollector.buildRequest(
        "The user rejected the proposed operations. Please suggest alternatives.");

    requestInFlight = true;
    callback.setRequestInFlight(true);
    validationRetryCount = 0;
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
   * Loads existing conversation history from the server.
   * Called when the dialog is opened to restore previous messages.
   */
  public void loadExistingConversation() {
    long projectId = contextCollector.getCurrentProjectId();
    if (projectId == 0) {
      return;
    }

    pendingResponse = null;
    callback.clearChatHistory();
    callback.hideOperationPreview();

    aiAgentService.getConversationHistory(projectId,
        new OdeAsyncCallback<List<AIConversationMessage>>(MESSAGES.aiChatLoadHistoryError()) {
          @Override
          public void onSuccess(List<AIConversationMessage> messages) {
            if (messages != null) {
              for (AIConversationMessage msg : messages) {
                if ("user".equals(msg.getRole())) {
                  callback.addUserMessage(msg.getText());
                } else {
                  callback.addAiMessage(msg.getText());
                }
              }
            }
          }
        });
  }

  /**
   * Clears the current conversation on the server and in the UI.
   */
  public void clearConversation() {
    long projectId = contextCollector.getCurrentProjectId();
    if (projectId == 0) {
      return;
    }

    aiAgentService.clearConversation(projectId, new OdeAsyncCallback<Void>(
        MESSAGES.aiChatClearError()) {
      @Override
      public void onSuccess(Void result) {
        pendingResponse = null;
        callback.clearChatHistory();
        callback.hideOperationPreview();
      }
    });
  }

  /**
   * Cancels any in-flight request, stops polling, and resets state.
   */
  public void cancelInFlight() {
    autoAcceptAll = false;
    requestInFlight = false;
    pendingResponse = null;
    callback.setRequestInFlight(false);
    stopPollingStatus();
    callback.hideOperationPreview();
  }

  /**
   * Resets the auto-accept-all flag without affecting other state.
   * Called when the dialog is closed or the active project changes.
   */
  public void resetAutoAcceptAll() {
    autoAcceptAll = false;
  }

  /**
   * Returns whether a request is currently in flight.
   */
  public boolean isRequestInFlight() {
    return requestInFlight;
  }

  /**
   * Returns the pending AI response awaiting user approval, or null.
   */
  public AIAgentResponse getPendingResponse() {
    return pendingResponse;
  }

  // ---- Validation ----

  /**
   * Handles an AI response by pre-validating block operations before
   * showing the preview. If validation fails and retries are available,
   * keeps "Calling AI" visible and sends error feedback to the server
   * for an automatic LLM retry.
   */
  private void handleResponseWithValidation(AIAgentResponse response) {
    List<AIOperation> operations = response.getOperations();

    // Pre-validate WRITE_BLOCK and DELETE_BLOCK operations client-side
    if (operations != null && !operations.isEmpty()) {
      List<String> validationErrors = validateBlockOperations(operations);
      if (!validationErrors.isEmpty()) {
        if (validationRetryCount < MAX_VALIDATION_RETRIES) {
          // Retry: keep "Calling AI" visible, send errors to server
          validationRetryCount++;
          LOG.info("Client validation failed (attempt " + validationRetryCount
              + "/" + MAX_VALIDATION_RETRIES + "), retrying: " + validationErrors);
          reportValidationErrors(validationErrors);
          return;
        }
        // Exhausted retries — strip invalid block ops so they are not
        // presented in the preview or executed. Non-block operations
        // (component additions, property changes, etc.) are kept.
        LOG.warning("Validation retries exhausted. Stripping "
            + validationErrors.size() + " invalid block operation(s).");
        List<AIOperation> cleaned = new ArrayList<>();
        for (AIOperation op : operations) {
          if (op.getType() != AIOperation.Type.WRITE_BLOCK
              && op.getType() != AIOperation.Type.DELETE_BLOCK) {
            cleaned.add(op);
          }
        }
        response.setOperations(cleaned);
        // Surface the errors so the user sees what went wrong
        for (String err : validationErrors) {
          response.getErrors().add(err);
        }
      }
    }

    // Validation passed (or no block ops, or retries exhausted) — show result
    requestInFlight = false;
    callback.setRequestInFlight(false);
    stopPollingStatus();
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

    // Display the AI message only when there are no pending operations.
    String aiMessage = response.getAiMessage();
    if (aiMessage != null && !aiMessage.isEmpty() && !hasOps) {
      callback.addAiMessage(aiMessage);
    }

    // Display any errors
    List<String> errors = response.getErrors();
    if (errors != null && !errors.isEmpty()) {
      for (String error : errors) {
        callback.addAiMessage("Error: " + error);
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
   * @return list of validation error messages; empty if all operations are valid
   */
  private List<String> validateBlockOperations(List<AIOperation> operations) {
    List<String> errors = new ArrayList<>();
    BlocksEditor<?, ?> blocksEditor = contextCollector.getCurrentBlocksEditor();
    if (blocksEditor == null) {
      // Can't validate without a blocks editor — let execution handle it
      return errors;
    }

    for (AIOperation op : operations) {
      if (op.getType() == AIOperation.Type.WRITE_BLOCK) {
        String yail = AIJsonUtils.extractField(op.getPayload(), "yail");
        if (yail != null && !yail.isEmpty()) {
          String resultJson = blocksEditor.validateYail(yail);
          if (resultJson != null) {
            String error = extractValidationError(resultJson);
            if (error != null) {
              // Include the failing YAIL so the LLM can see and fix its mistake
              errors.add("write_block validation failed: " + error
                  + "\nFailing YAIL: " + yail);
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
              errors.add("delete_block validation failed: " + error);
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
   */
  private void reportValidationErrors(List<String> errors) {
    long projectId = contextCollector.getCurrentProjectId();
    if (projectId == 0) {
      requestInFlight = false;
      callback.setRequestInFlight(false);
      stopPollingStatus();
      return;
    }

    // Keep polling — "Calling AI" stays visible
    aiAgentService.reportExecutionErrors(contextCollector.buildRequest(null), errors,
        new OdeAsyncCallback<AIAgentResponse>(MESSAGES.aiChatSendError()) {
          @Override
          public void onSuccess(AIAgentResponse response) {
            // Validate the retry response too
            handleResponseWithValidation(response);
          }

          @Override
          public void onFailure(Throwable caught) {
            super.onFailure(caught);
            requestInFlight = false;
            callback.setRequestInFlight(false);
            stopPollingStatus();
            callback.addAiMessage(MESSAGES.aiChatSendError() + ": " + caught.getMessage());
          }
        });
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

    aiAgentService.continueRequest(contextCollector.buildRequest(null),
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
            callback.addAiMessage(MESSAGES.aiChatSendError() + ": " + caught.getMessage());
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

    // Collect structured per-operation results so the LLM knows exactly what
    // was applied (do not re-emit), what failed, and what was skipped.
    List<String> errors = new ArrayList<>();
    for (AIOperation op : result.getSucceeded()) {
      errors.add("SUCCEEDED:" + AIOperationFormatter.formatOperation(op));
    }
    for (AIOperation op : result.getFailed()) {
      String mainError = result.getErrorMessage();
      errors.add("FAILED:" + AIOperationFormatter.formatOperation(op)
          + " -- Error: " + (mainError != null ? mainError : "unknown"));
    }
    for (AIOperation op : result.getSkipped()) {
      errors.add("SKIPPED:" + AIOperationFormatter.formatOperation(op));
    }

    startPollingStatus();
    validationRetryCount = 0;
    aiAgentService.reportExecutionErrors(contextCollector.buildRequest(null), errors,
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
            callback.addAiMessage(MESSAGES.aiChatSendError() + ": " + caught.getMessage());
          }
        });
  }

  // ---- Status polling ----

  /**
   * Starts a timer that polls getRequestStatus every second
   * to display intermediate progress while a request is in flight.
   */
  private void startPollingStatus() {
    callback.setStatusText(MESSAGES.aiChatThinking());
    callback.setStatusVisible(true);

    if (pollingTimer != null) {
      pollingTimer.cancel();
    }

    pollingTimer = new Timer() {
      @Override
      public void run() {
        long projectId = contextCollector.getCurrentProjectId();
        if (projectId == 0 || !requestInFlight) {
          stopPollingStatus();
          return;
        }

        aiAgentService.getRequestStatus(projectId, new OdeAsyncCallback<String>() {
          @Override
          public void onSuccess(String status) {
            if (status != null && !status.isEmpty()) {
              callback.setStatusText(status);
            }
          }

          @Override
          public void onFailure(Throwable caught) {
            // Silently ignore polling failures; the main request callback
            // handles real errors.
          }
        });
      }
    };
    pollingTimer.scheduleRepeating(POLL_INTERVAL_MS);
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
