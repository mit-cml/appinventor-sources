// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent;

import com.google.appinventor.server.aiagent.context.ContextUtils;
import com.google.appinventor.server.aiagent.llm.ChatMessage;
import com.google.appinventor.server.aiagent.llm.LLMProvider;
import com.google.appinventor.server.aiagent.llm.LLMProviderException;
import com.google.appinventor.server.aiagent.llm.LLMProviderRegistry;
import com.google.appinventor.server.aiagent.llm.LLMResponse;
import com.google.appinventor.server.aiagent.llm.LLMTool;
import com.google.appinventor.server.aiagent.llm.RawToolCall;
import com.google.appinventor.server.aiagent.llm.ReadOnlyToolResolver;
import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.server.storage.AIConversationState;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StoredData.MessageRole;
import com.google.appinventor.shared.rpc.aiagent.AIAgentRequest;
import com.google.appinventor.shared.rpc.aiagent.AIAgentResponse;
import com.google.appinventor.shared.rpc.aiagent.AIConversationMessage;
import com.google.appinventor.shared.rpc.aiagent.AIOperation;
import com.google.appinventor.shared.rpc.aiagent.AIOperationResult;
import com.google.appinventor.shared.rpc.aiagent.AIStreamStatus;
import com.google.appinventor.shared.settings.SettingsConstants;

import static com.google.appinventor.shared.settings.SettingsConstants.AI_AGENT_MODE_ADVISOR;
import static com.google.appinventor.shared.settings.SettingsConstants.AI_AGENT_MODE_OFF;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Core AI agent orchestration engine.
 *
 * <p>Contains all business logic for conversation management, context building,
 * LLM calls, response parsing, auto-retry, and finalization.  This class is
 * independent of any servlet or HTTP concerns; it receives a pre-validated
 * {@code userId} and {@code mode} from its caller.
 */
public class AIAgentEngine {

  private static final Logger LOG = Logger.getLogger(AIAgentEngine.class.getName());

  /**
   * When {@code true}, the engine retries with a nudge message if the LLM
   * responds with text only (no tool calls) in an editing mode.  Set to
   * {@code false} to disable the narration retry entirely.
   */
  private static final Flag<Boolean> RETRY_NARRATION =
      Flag.createFlag("ai.agent.features.retry-narration", false);

  /**
   * Instruction appended to the context messages on every continuation call.
   * Steers the model away from refactoring or undoing prior user-requested
   * changes when it receives the fresh project state in a continuation.
   * Loaded from {@code continuation_instructions.md}.
   */
  private static volatile String continuationScopeInstruction;

  private static String getContinuationScopeInstruction() {
    if (continuationScopeInstruction == null) {
      continuationScopeInstruction =
          ContextUtils.loadResource("continuation_instructions.md");
    }
    return continuationScopeInstruction;
  }

  private static final Flag<Boolean> ORCHESTRATION_FLAG = Flag.createFlag("ai.agent.features.orchestration", false);
  private static final Flag<Boolean> PLAN_EDIT_FLAG = Flag.createFlag("ai.agent.features.plan-edit", false);

  private final StorageIo storageIo;
  private final AIContextBuilder contextBuilder;
  private final ConversationManager conversationManager;
  private final AIToolResolver toolResolver;
  private final LLMResponseParser responseParser;

  public AIAgentEngine(StorageIo storageIo) {
    this.storageIo = storageIo;
    this.contextBuilder = new AIContextBuilder(storageIo);
    this.conversationManager = new ConversationManager(storageIo);
    this.toolResolver = new AIToolResolver(contextBuilder);
    this.responseParser = new LLMResponseParser();
  }

  // ---------- Inner classes ----------

  static class ConversationInit {
    final AIConversationState conv;
    final boolean isNew;

    ConversationInit(AIConversationState conv, boolean isNew) {
      this.conv = conv;
      this.isNew = isNew;
    }
  }

  static class ParsedResult {
    final List<AIOperation> operations;
    final List<String> errors;
    final List<ToolCallStatus> toolCallStatuses;

    ParsedResult(List<AIOperation> operations, List<String> errors,
        List<ToolCallStatus> toolCallStatuses) {
      this.operations = operations;
      this.errors = errors;
      this.toolCallStatuses = toolCallStatuses;
    }
  }

  /** Mutable holder for state that {@link #retryIfNarration} may update. */
  static class NarrationRetryState {
    LLMResponse llmResponse;
    ParsedResult parsed;
    String assistantText;

    NarrationRetryState(LLMResponse llmResponse, ParsedResult parsed, String assistantText) {
      this.llmResponse = llmResponse;
      this.parsed = parsed;
      this.assistantText = assistantText;
    }
  }

  // ---------- Public API: complex RPC methods ----------

  /**
   * Process a new user message.
   *
   * @param userId               the authenticated user ID
   * @param projectId            the project being edited
   * @param screenName           the currently active screen
   * @param userMessage          the user's sanitized, validated message
   * @param blocksYail           client-generated YAIL for the current screen's blocks
   * @param currentView          the active editor view ("Designer" or "Blocks")
   * @param mode                 the AI agent mode ("Advisor", "ScreenEditor", or "ProjectEditor")
   * @param screenComponentsJson live component tree JSON from the client
   * @param projectSnapshot      project metadata JSON from the client
   * @param blockWarnings        JSON with block warnings/errors from the client
   * @param contextHint          optional context prepended to the LLM message only (not stored in history)
   * @return the AI agent response
   */
  public AIAgentResponse processRequest(String userId, long projectId, String screenName,
      String userMessage, String blocksYail, String currentView, String mode,
      String screenComponentsJson, String projectSnapshot, String blockWarnings,
      String locale, String languageDisplayName, boolean isPlatformMessage,
      String contextHint, String companionSnapshot,
      boolean planExecuteMode, boolean orchestrationMode, String targetScreen,
      boolean executionPhase) {
    String routingScreen = orchestrationMode ? targetScreen : null;
    StreamBuffer streamBuffer = new StreamBuffer(storageIo, projectId, routingScreen);
    try {
      streamBuffer.init();
      streamBuffer.appendStatus("Building context...");

      // Get or create conversation (resets on provider change)
      ConversationInit init = initConversation(projectId, routingScreen);
      AIConversationState conv = init.conv;
      boolean isNew = init.isNew;

      AIDebug.beginRequest(conv.getConversationId());
      AIDebug.log(LOG, "processRequest: userId=" + userId + ", projectId=" + projectId
          + ", screen=" + screenName + ", mode=" + mode
          + ", msgLen=" + userMessage.length());

      EnforcementContext enforcementContext = EnforcementContext.STANDARD;
      if (ORCHESTRATION_FLAG.get() && executionPhase) {
        enforcementContext = EnforcementContext.EXECUTION;
      } else if (ORCHESTRATION_FLAG.get() && planExecuteMode) {
        enforcementContext = EnforcementContext.PLANNING;
      } else if (orchestrationMode) {
        enforcementContext = EnforcementContext.CHILD_EXECUTION;
      }

      // Build system prompt, context messages, and tools
      String systemPrompt = contextBuilder.build();
      List<String> contextMessages = contextBuilder.buildContextMessages(
          userId, projectId, screenName, mode, blocksYail, currentView,
          screenComponentsJson, projectSnapshot, blockWarnings,
          locale, languageDisplayName, companionSnapshot, enforcementContext);
      List<LLMTool> tools = contextBuilder.buildTools(mode, currentView, enforcementContext,
          companionSnapshot != null);
      AIDebug.log(LOG, "System prompt built: length=" + systemPrompt.length() + " chars");

      // Build history for stateless providers
      LLMProvider provider = LLMProviderRegistry.get(conv.getProviderName());
      List<ChatMessage> history = Collections.emptyList();
      if (provider.isStateless()) {
        history = conversationManager.loadConversation(conv.getConversationId());
      }
      if (AIDebug.enabled()) {
        StringBuilder histInfo = new StringBuilder("History loaded: " + history.size() + " messages");
        for (ChatMessage msg : history) {
          histInfo.append("\n  ").append(msg.getRole())
              .append(" (").append(msg.getText().length()).append(" chars)");
        }
        AIDebug.log(LOG, histInfo.toString());
      }

      // Build read-only tool resolver
      ReadOnlyToolResolver resolver = toolResolver.createResolver(userId, projectId);

      AIDebug.log(LOG, "Pre-LLM: toolCount=" + tools.size()
          + ", provider=" + conv.getProviderName() + ", model=" + getConfiguredModel());

      // Save user message to history BEFORE calling the LLM,
      // so it is persisted even if the LLM call fails.
      conversationManager.storeMessage(conv.getConversationId(),
          MessageRole.USER, userMessage, true);

      streamBuffer.appendStatus("Calling AI...");

      // Call LLM (context messages are sent as separate turns by the provider).
      // Platform messages are wrapped so the LLM knows they are system-generated.
      // Context hint (e.g., block YAIL for "Explain Block") is prepended to the
      // LLM message but NOT stored in history, so it doesn't appear in the chat.
      String llmMessage = userMessage;
      if (contextHint != null && !contextHint.isEmpty()) {
        llmMessage = llmMessage + "\n\n" + contextHint;
      }
      if (isPlatformMessage) {
        llmMessage = AIAgentRequest.wrapPlatformMessage(llmMessage);
      }
      LLMResponse llmResponse = provider.chat(
          systemPrompt, contextMessages, llmMessage, tools, conv.getProviderRef(),
          history, resolver, streamBuffer);

      // Debug: raw LLM response
      if (AIDebug.enabled()) {
        AIDebug.log(LOG, "Raw LLM response text: "
            + (llmResponse.getText() != null ? llmResponse.getText() : "(null)"));
        StringBuilder tcInfo = new StringBuilder("Raw tool calls: "
            + llmResponse.getRawToolCalls().size());
        for (RawToolCall tc : llmResponse.getRawToolCalls()) {
          tcInfo.append("\n  ").append(tc.getName()).append(": ").append(tc.getArgumentsJson());
        }
        AIDebug.log(LOG, tcInfo.toString());
      }

      // Parse tool calls
      ParsedResult parsed = parseAndEnforce(llmResponse, mode, currentView,
          enforcementContext);

      // Debug: parse result
      if (AIDebug.enabled()) {
        AIDebug.log(LOG, "Parse result: operations=" + parsed.operations.size()
            + ", errors=" + parsed.errors.size());
        for (AIOperation op : parsed.operations) {
          AIDebug.log(LOG, "  Parsed op: " + op.getType() + " payload=" + op.getPayload());
        }
        for (String err : parsed.errors) {
          AIDebug.log(LOG, "  Parse error: " + err);
        }
      }

      String assistantText = llmResponse.getText() != null ? llmResponse.getText() : "";

      // Narration detection: if the LLM returned text without any tool calls
      // in an editing mode, retry once with a nudge.
      NarrationRetryState nrs = new NarrationRetryState(llmResponse, parsed, assistantText);
      retryIfNarration(nrs, mode, currentView, provider, systemPrompt,
          contextMessages, tools, history, conv, resolver, streamBuffer, enforcementContext);
      llmResponse = nrs.llmResponse;
      parsed = nrs.parsed;
      assistantText = nrs.assistantText;

      AIDebug.log(LOG, "Final response: operations=" + parsed.operations.size()
          + ", errors=" + parsed.errors.size()
          + ", hasMore=" + (llmResponse.hasMore() && !parsed.operations.isEmpty())
          + ", textLen=" + assistantText.length());

      return finalizeResponse(llmResponse, assistantText, parsed,
          conv, projectId, isNew, routingScreen);

    } catch (StreamBuffer.CancelledException e) {
      LOG.info("Request cancelled by user for project " + projectId);
      // Store synthetic assistant message to keep history role-alternating
      // (user message was already stored before the LLM call).
      AIConversationState conv = routingScreen != null
          ? conversationManager.getConversation(projectId, routingScreen)
          : conversationManager.getConversation(projectId);
      if (conv != null) {
        conversationManager.storeMessage(conv.getConversationId(),
            MessageRole.ASSISTANT, "[Request cancelled]", false);
      }
      new StreamBuffer(storageIo, projectId, routingScreen).clear();
      return new AIAgentResponse("", Collections.<AIOperation>emptyList(), false,
          Collections.<String>emptyList());
    } catch (LLMProviderException e) {
      LOG.log(Level.WARNING, "LLM provider error", e);
      streamBuffer.clear();
      return errorResponse(e.getUserFacingMessage());
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Unexpected error in AI agent", e);
      streamBuffer.clear();
      return errorResponse("An unexpected error occurred. Please try again.");
    } finally {
      AIDebug.endRequest();
    }
  }

  /**
   * Continue a multi-turn response.
   *
   * @param userId               the authenticated user ID
   * @param projectId            the project being edited
   * @param screenName           the currently active screen
   * @param blocksYail           client-generated YAIL for the current screen's blocks
   * @param currentView          the active editor view ("Designer" or "Blocks")
   * @param mode                 the AI agent mode
   * @param screenComponentsJson live component tree JSON from the client
   * @param projectSnapshot      project metadata JSON from the client
   * @param blockWarnings        JSON with block warnings/errors from the client
   * @return the AI agent response
   */
  public AIAgentResponse continueRequest(String userId, long projectId, String screenName,
      String blocksYail, String currentView, String mode,
      String screenComponentsJson, String projectSnapshot, String blockWarnings,
      String locale, String languageDisplayName, String companionSnapshot,
      boolean planExecuteMode, boolean orchestrationMode, String targetScreen,
      boolean executionPhase) {
    String routingScreen = orchestrationMode ? targetScreen : null;
    StreamBuffer streamBuffer = new StreamBuffer(storageIo, projectId, routingScreen);
    try {
      streamBuffer.init();
      streamBuffer.appendStatus("Continuing AI response...");

      // Load conversation state from memcache
      AIConversationState conv = routingScreen != null
          ? conversationManager.getConversation(projectId, routingScreen)
          : conversationManager.getConversation(projectId);
      if (conv == null || conv.getProviderRef() == null || conv.getProviderRef().isEmpty()) {
        streamBuffer.clear();
        return errorResponse("No continuation state available. Please start a new request.");
      }

      AIDebug.beginRequest(conv.getConversationId());
      AIDebug.log(LOG, "continueRequest: userId=" + userId + ", projectId=" + projectId
          + ", screen=" + screenName + ", mode=" + mode);

      EnforcementContext enforcementContext = EnforcementContext.STANDARD;
      if (ORCHESTRATION_FLAG.get() && executionPhase) {
        enforcementContext = EnforcementContext.EXECUTION;
      } else if (ORCHESTRATION_FLAG.get() && planExecuteMode) {
        enforcementContext = EnforcementContext.PLANNING;
      } else if (orchestrationMode) {
        enforcementContext = EnforcementContext.CHILD_EXECUTION;
      }

      // Get provider and rebuild tools
      LLMProvider provider = LLMProviderRegistry.get(conv.getProviderName());
      List<LLMTool> tools = contextBuilder.buildTools(mode, currentView, enforcementContext,
          companionSnapshot != null);
      ReadOnlyToolResolver resolver = toolResolver.createResolver(userId, projectId);

      // Patch the static system prompt into continuation state
      String systemPrompt = contextBuilder.build();
      String providerRef = patchSystemPrompt(conv.getProviderRef(), systemPrompt);

      // Build fresh context messages for the continuation call
      List<String> contextMessages = contextBuilder.buildContextMessages(
          userId, projectId, screenName, mode, blocksYail, currentView,
          screenComponentsJson, projectSnapshot, blockWarnings,
          locale, languageDisplayName, companionSnapshot, enforcementContext);
      // Append a scoping instruction so the model stays focused on the
      // user's request and does not refactor or undo prior changes.
      contextMessages = new ArrayList<String>(contextMessages);
      contextMessages.add(AIAgentRequest.wrapPlatformMessage(getContinuationScopeInstruction()));

      AIDebug.log(LOG, "continueRequest: provider=" + conv.getProviderName()
          + ", providerRef length=" + providerRef.length());

      // Save continuation marker to history BEFORE calling the LLM,
      // so it is persisted even if the LLM call fails.
      conversationManager.storeMessage(conv.getConversationId(),
          MessageRole.USER, "[Continuation requested]", false);

      streamBuffer.appendStatus("Calling AI...");

      // Call the continuation method with updated state
      LLMResponse llmResponse = provider.continueWithToolResults(
          providerRef, tools, contextMessages, resolver, streamBuffer);

      // Debug: raw LLM response
      if (AIDebug.enabled()) {
        AIDebug.log(LOG, "Continue raw LLM response text: "
            + (llmResponse.getText() != null ? llmResponse.getText() : "(null)"));
        StringBuilder tcInfo = new StringBuilder("Continue raw tool calls: "
            + llmResponse.getRawToolCalls().size());
        for (RawToolCall tc : llmResponse.getRawToolCalls()) {
          tcInfo.append("\n  ").append(tc.getName()).append(": ").append(tc.getArgumentsJson());
        }
        AIDebug.log(LOG, tcInfo.toString());
      }

      // Parse, enforce, save, and build response
      ParsedResult parsed = parseAndEnforce(llmResponse, mode, currentView,
          enforcementContext);
      String assistantText = llmResponse.getText() != null ? llmResponse.getText() : "";

      // Narration detection: catches LLMs that describe remaining work
      // instead of calling tools after continuation (e.g. after
      // toggle_editor switches to Blocks, the LLM narrates instead of
      // emitting write_block).
      List<ChatMessage> history = provider.isStateless()
          ? conversationManager.loadConversation(conv.getConversationId())
          : Collections.<ChatMessage>emptyList();
      // Stateful providers already have context; re-sending would duplicate.
      List<String> retryContext = provider.isStateless() ? contextMessages : null;
      NarrationRetryState nrs = new NarrationRetryState(llmResponse, parsed, assistantText);
      retryIfNarration(nrs, mode, currentView, provider, systemPrompt,
          retryContext, tools, history, conv, resolver, streamBuffer, enforcementContext);
      llmResponse = nrs.llmResponse;
      parsed = nrs.parsed;
      assistantText = nrs.assistantText;

      AIDebug.log(LOG, "Continue response: operations=" + parsed.operations.size()
          + ", errors=" + parsed.errors.size()
          + ", hasMore=" + (llmResponse.hasMore() && !parsed.operations.isEmpty())
          + ", textLen=" + assistantText.length());

      return finalizeResponse(llmResponse, assistantText, parsed, conv, projectId, false,
          routingScreen);

    } catch (StreamBuffer.CancelledException e) {
      LOG.info("Continuation cancelled by user for project " + projectId);
      // Store synthetic assistant message to keep history role-alternating.
      // Stateless providers (Anthropic) require strict role alternation.
      AIConversationState cancelConv = routingScreen != null
          ? conversationManager.getConversation(projectId, routingScreen)
          : conversationManager.getConversation(projectId);
      if (cancelConv != null) {
        conversationManager.storeMessage(cancelConv.getConversationId(),
            MessageRole.ASSISTANT, "[Request cancelled]", false);
      }
      streamBuffer.clear();
      return new AIAgentResponse("", Collections.<AIOperation>emptyList(), false,
          Collections.<String>emptyList());
    } catch (LLMProviderException e) {
      LOG.log(Level.WARNING, "LLM provider error in continuation", e);
      streamBuffer.clear();
      return errorResponse(e.getUserFacingMessage());
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Unexpected error in AI agent continuation", e);
      streamBuffer.clear();
      return errorResponse("An unexpected error occurred. Please try again.");
    } finally {
      AIDebug.endRequest();
    }
  }

  /**
   * Report client-side execution errors and retry with feedback.
   *
   * @param userId               the authenticated user ID
   * @param projectId            the project being edited
   * @param screenName           the currently active screen
   * @param errors               the execution errors from the client
   * @param blocksYail           client-generated YAIL for the current screen's blocks
   * @param currentView          the active editor view ("Designer" or "Blocks")
   * @param mode                 the AI agent mode
   * @param screenComponentsJson live component tree JSON from the client
   * @param projectSnapshot      project metadata JSON from the client
   * @param blockWarnings        JSON with block warnings/errors from the client
   * @return the AI agent response
   */
  public AIAgentResponse reportExecutionErrors(String userId, long projectId, String screenName,
      List<AIOperationResult> results, int retryAttempt, int totalTools, String blocksYail,
      String currentView, String mode, String screenComponentsJson, String projectSnapshot,
      String blockWarnings, String locale, String languageDisplayName, String companionSnapshot,
      boolean planExecuteMode, boolean orchestrationMode, String targetScreen,
      boolean executionPhase) {
    String routingScreen = orchestrationMode ? targetScreen : null;
    StreamBuffer streamBuffer = new StreamBuffer(storageIo, projectId, routingScreen);
    try {
      streamBuffer.init();

      // Count per-status totals for the status message.
      // Use totalTools from the client when available — on subsequent retries
      // the results list shrinks (only failed/skipped ops are re-emitted).
      int failedCount = 0;
      int runtimeReadCount = 0;
      for (AIOperationResult r : results) {
        if (r.getStatus() == AIOperationResult.Status.FAILED) {
          failedCount++;
        } else if (r.getStatus() == AIOperationResult.Status.RUNTIME_READ) {
          runtimeReadCount++;
        }
      }
      int totalCount = totalTools > 0 ? totalTools : results.size();

      // Three flavors of "continuation" share this method:
      //  1. Runtime reads (RUNTIME_READ): tool-result feedback for Companion
      //     reads — not a retry, not an error. Status reflects that.
      //  2. Validation retry (all SUCCEEDED, no SKIPPED): the LLM sees which
      //     block operations passed dry-run so it can fix the failures.
      //  3. Execution retry (mixed or explicit failures): the normal retry
      //     flow — "N out of M tools failed, retry attempt K".
      boolean allRuntimeReads = runtimeReadCount > 0 && runtimeReadCount == results.size();
      String statusTag;
      if (allRuntimeReads) {
        statusTag = runtimeReadCount + (runtimeReadCount == 1 ? " read" : " reads");
        streamBuffer.appendStatus("Analyzing runtime data (" + statusTag + ")...");
      } else {
        statusTag = failedCount + " out of " + totalCount + " tools failed"
            + (retryAttempt > 0 ? ", retry attempt " + retryAttempt : "");
        streamBuffer.appendStatus("Analyzing errors (" + statusTag + ")...");
      }

      // Load conversation state
      AIConversationState conv = routingScreen != null
          ? conversationManager.getConversation(projectId, routingScreen)
          : conversationManager.getConversation(projectId);
      if (conv == null || conv.getProviderRef() == null || conv.getProviderRef().isEmpty()) {
        streamBuffer.clear();
        return errorResponse("No conversation state available. Please start a new request.");
      }

      AIDebug.beginRequest(conv.getConversationId());
      AIDebug.log(LOG, "reportExecutionErrors: userId=" + userId
          + ", projectId=" + projectId + ", results=" + results.size()
          + ", retryAttempt=" + retryAttempt + ", totalTools=" + totalTools);

      // Determine if this is a validation retry (no SKIPPED results means
      // all operations were attempted — this is a validation pass, not
      // a full execution).
      boolean isValidationRetry = true;
      for (AIOperationResult r : results) {
        if (r.getStatus() == AIOperationResult.Status.SKIPPED) {
          isValidationRetry = false;
          break;
        }
      }

      // Patch the continuation state's toolCallResults with client outcomes
      String patchedRef = patchToolCallResults(
          conv.getProviderRef(), results, isValidationRetry);
      if (patchedRef == null) {
        streamBuffer.clear();
        return errorResponse(
            "Missing tool call results in continuation state. Please start a new request.");
      }

      // Patch the system prompt into the continuation state
      String systemPrompt = contextBuilder.build();
      patchedRef = patchSystemPrompt(patchedRef, systemPrompt);

      EnforcementContext enforcementContext = EnforcementContext.STANDARD;
      if (ORCHESTRATION_FLAG.get() && executionPhase) {
        enforcementContext = EnforcementContext.EXECUTION;
      } else if (orchestrationMode) {
        enforcementContext = EnforcementContext.CHILD_EXECUTION;
      }

      // Build fresh context messages with current blocks state
      List<String> contextMessages = contextBuilder.buildContextMessages(
          userId, projectId, screenName, mode, blocksYail, currentView,
          screenComponentsJson, projectSnapshot, blockWarnings,
          locale, languageDisplayName, companionSnapshot, enforcementContext);

      // Get provider, tools, and resolver
      LLMProvider provider = LLMProviderRegistry.get(conv.getProviderName());
      List<LLMTool> tools = contextBuilder.buildTools(mode, currentView, enforcementContext,
          companionSnapshot != null);
      ReadOnlyToolResolver resolver = toolResolver.createResolver(userId, projectId);

      if (AIDebug.enabled()) {
        AIDebug.log(LOG, "Patched tool call results (isValidationRetry="
            + isValidationRetry + "): "
            + patchedRef.substring(0, Math.min(patchedRef.length(), 500)));
      }

      streamBuffer.appendStatus("Calling AI (" + statusTag + ")...");

      // Continue with patched tool results instead of starting a new chat
      LLMResponse llmResponse = provider.continueWithToolResults(
          patchedRef, tools, contextMessages, resolver, streamBuffer);

      // Parse, enforce, save, and build response
      ParsedResult parsed = parseAndEnforce(llmResponse, mode, currentView,
          enforcementContext);
      String assistantText = llmResponse.getText() != null ? llmResponse.getText() : "";

      AIDebug.log(LOG, "Error retry response: operations=" + parsed.operations.size()
          + ", errors=" + parsed.errors.size()
          + ", hasMore=" + (llmResponse.hasMore() && !parsed.operations.isEmpty()));

      return finalizeResponse(llmResponse, assistantText, parsed, conv, projectId, false,
          routingScreen);

    } catch (StreamBuffer.CancelledException e) {
      LOG.info("Error retry cancelled by user for project " + projectId);
      streamBuffer.clear();
      return new AIAgentResponse("", Collections.<AIOperation>emptyList(), false,
          Collections.<String>emptyList());
    } catch (LLMProviderException e) {
      LOG.log(Level.WARNING, "LLM provider error in error retry", e);
      streamBuffer.clear();
      return errorResponse(e.getUserFacingMessage());
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Unexpected error in error retry", e);
      streamBuffer.clear();
      return errorResponse("An unexpected error occurred during retry. Please try again.");
    } finally {
      AIDebug.endRequest();
    }
  }

  // ---------- Public API: simple delegations ----------

  public void clearConversation(long projectId) {
    conversationManager.clearConversation(projectId);
  }

  public List<AIConversationMessage> getConversationHistory(long projectId) {
    AIConversationState conv = conversationManager.getConversation(projectId);
    if (conv == null) {
      return Collections.emptyList();
    }

    List<ChatMessage> history = conversationManager.loadConversation(conv.getConversationId());
    List<AIConversationMessage> result = new ArrayList<>();
    for (ChatMessage msg : history) {
      if (!msg.isDisplay()) {
        continue;
      }
      result.add(new AIConversationMessage(msg.getRole(), msg.getText()));
    }
    return result;
  }

  public AIStreamStatus getRequestStatus(long projectId) {
    return getRequestStatus(projectId, null);
  }

  public AIStreamStatus getRequestStatus(long projectId, String targetScreen) {
    StreamBuffer streamBuffer = new StreamBuffer(storageIo, projectId, targetScreen);
    AIStreamStatus status = streamBuffer.consume();
    // Piggyback config fields on every status poll so the client can
    // detect debug mode and build feedback links without a separate RPC.
    status.setDebugEnabled(AIDebug.enabled());
    status.setOrchestrationEnabled(ORCHESTRATION_FLAG.get());
    status.setPlanEditEnabled(PLAN_EDIT_FLAG.get());
    AIConversationState conv = targetScreen != null
        ? conversationManager.getConversation(projectId, targetScreen)
        : conversationManager.getConversation(projectId);
    if (conv != null) {
      status.setConversationId(conv.getConversationId());
    }
    return status;
  }

  /**
   * Sets the cancellation flag for an in-flight request. The flag is stored
   * in Memcache and checked by LLM providers during streaming.
   */
  public void cancelRequest(long projectId) {
    cancelRequest(projectId, null);
  }

  public void cancelRequest(long projectId, String targetScreen) {
    new StreamBuffer(storageIo, projectId, targetScreen).setCancelled();
  }

  public String getProjectAIMode(String userId, long projectId) {
    try {
      // Read Screen1's SCM to get the AIAgentMode property
      // The mode is stored as a project-level setting synced from Screen1's Form properties
      List<String> files = storageIo.getProjectSourceFiles(userId, projectId);
      for (String fileId : files) {
        // Find Screen1.scm
        if (fileId.endsWith("/Screen1.scm")) {
          String scmContent = storageIo.downloadFile(userId, projectId, fileId, "UTF-8");
          String json = ContextUtils.extractScmJson(scmContent);
          if (json != null) {
            JSONObject root = new JSONObject(json);
            JSONObject props = root.optJSONObject("Properties");
            if (props != null) {
              String mode = props.optString(
                  SettingsConstants.YOUNG_ANDROID_SETTINGS_AI_AGENT_MODE, AI_AGENT_MODE_OFF);
              if (!mode.isEmpty()) {
                return mode;
              }
            }
          }
        }
      }
    } catch (Exception e) {
      LOG.log(Level.WARNING, "Failed to read AI agent mode for project " + projectId, e);
    }
    return AI_AGENT_MODE_OFF;
  }

  // ---------- Conversation init ----------

  /**
   * Get or create the conversation state for a project, resetting it if the
   * configured provider has changed since the last request.
   *
   * @param projectId  the project ID
   * @param screenName screen name for routing (null for parent conversation)
   */
  ConversationInit initConversation(long projectId, String screenName) {
    AIConversationState conv = screenName != null
        ? conversationManager.getConversation(projectId, screenName)
        : conversationManager.getConversation(projectId);
    boolean isNew = (conv == null);
    if (isNew) {
      conv = new AIConversationState(getConfiguredProvider(),
          UUID.randomUUID().toString(), null);
    }
    String currentProvider = getConfiguredProvider();
    if (!currentProvider.equals(conv.getProviderName())) {
      if (screenName != null) {
        conversationManager.clearConversation(projectId, screenName);
      } else {
        conversationManager.clearConversation(projectId);
      }
      conv = new AIConversationState(currentProvider,
          UUID.randomUUID().toString(), null);
      isNew = true;
    }
    return new ConversationInit(conv, isNew);
  }

  // ---------- Response pipeline ----------

  ParsedResult parseAndEnforce(LLMResponse llmResponse,
      String mode, String currentView, EnforcementContext context) {
    List<RawToolCall> rawToolCalls = llmResponse.getRawToolCalls();
    List<ToolCallStatus> statuses = new ArrayList<>();
    List<AIOperation> allParsedOps = new ArrayList<>();
    List<String> allErrors = new ArrayList<>();
    // Track which raw-tool-call index produced each parsed operation.
    List<Integer> parsedToRawIndex = new ArrayList<>();

    for (int i = 0; i < rawToolCalls.size(); i++) {
      RawToolCall tc = rawToolCalls.get(i);
      LLMResponseParser.RawToolCall wrapped =
          new LLMResponseParser.RawToolCall(tc.getName(), tc.getArgumentsJson());
      LLMResponseParser.ParseResult result =
          responseParser.parseToolCalls(Collections.singletonList(wrapped));

      String argsSummary = tc.getArgumentsJson();
      if (argsSummary.length() > 100) {
        argsSummary = argsSummary.substring(0, 100) + "...";
      }

      if (result.hasErrors()) {
        statuses.add(new ToolCallStatus(tc.getName(), argsSummary,
            ToolCallOutcome.PARSE_REJECTED, result.getErrors().get(0)));
        allErrors.addAll(result.getErrors());
      } else if (!result.getOperations().isEmpty()) {
        allParsedOps.add(result.getOperations().get(0));
        parsedToRawIndex.add(i);
        statuses.add(null); // placeholder, updated after enforcement
      }
      // read-only tool calls silently skipped by the parser produce no op and no error
    }

    // Run mode enforcement on the parsed operations.
    List<String> enforceErrors = new ArrayList<>();
    List<AIOperation> accepted = ModeEnforcer.enforce(
        allParsedOps, mode, currentView, context, enforceErrors);
    allErrors.addAll(enforceErrors);

    // Determine which parsed ops survived enforcement using identity.
    Set<AIOperation> acceptedSet = new HashSet<>(accepted);
    for (int j = 0; j < allParsedOps.size(); j++) {
      int rawIdx = parsedToRawIndex.get(j);
      RawToolCall tc = rawToolCalls.get(rawIdx);
      String argsSummary = tc.getArgumentsJson();
      if (argsSummary.length() > 100) {
        argsSummary = argsSummary.substring(0, 100) + "...";
      }

      if (acceptedSet.contains(allParsedOps.get(j))) {
        statuses.set(rawIdx, new ToolCallStatus(tc.getName(), argsSummary,
            ToolCallOutcome.ACCEPTED, null));
      } else {
        statuses.set(rawIdx, new ToolCallStatus(tc.getName(), argsSummary,
            ToolCallOutcome.MODE_REJECTED, "Rejected by mode/view enforcement"));
      }
    }

    // Remove null placeholders (should not remain, but defensive).
    statuses.removeAll(Collections.singleton(null));

    return new ParsedResult(accepted, allErrors, statuses);
  }

  /**
   * Detects "narration-only" responses (text with no tool calls in an editing
   * mode) and retries once with a nudge.  Mutates {@code state} in place if
   * the retry produces tool calls.
   *
   * @param state           mutable holder for the current response state
   * @param mode            the AI agent mode
   * @param currentView     the active editor view
   * @param provider        the LLM provider
   * @param systemPrompt    the system prompt
   * @param contextMessages context messages for stateless retry (may be null)
   * @param tools           available tools
   * @param history         conversation history for stateless retry
   * @param conv            conversation state
   * @param resolver        read-only tool resolver
   * @param streamBuffer    stream buffer for status updates
   */
  void retryIfNarration(NarrationRetryState state, String mode, String currentView,
      LLMProvider provider, String systemPrompt, List<String> contextMessages,
      List<LLMTool> tools, List<ChatMessage> history, AIConversationState conv,
      ReadOnlyToolResolver resolver, StreamBuffer streamBuffer,
      EnforcementContext enforcementContext)
      throws LLMProviderException {
    if (!RETRY_NARRATION.get()) {
      return;
    }
    boolean isEditingMode = !AI_AGENT_MODE_ADVISOR.equals(mode);
    boolean isNarrationOnly = state.parsed.operations.isEmpty()
        && state.llmResponse.getRawToolCalls().isEmpty()
        && !state.assistantText.isEmpty();
    if (!isEditingMode || !isNarrationOnly) {
      return;
    }
    // In planning mode, text-only responses are expected — the LLM may
    // ask clarifying questions before proposing a plan.  Don't nudge.
    if (enforcementContext == EnforcementContext.PLANNING) {
      return;
    }

    AIDebug.log(LOG, "Narration detected in " + mode
        + " mode — retrying with tool-use nudge");

    // For stateless providers, build a temporary history that includes the
    // narration so the LLM sees its own failed attempt.
    List<ChatMessage> retryHistory = history;
    if (provider.isStateless()) {
      retryHistory = new ArrayList<>(history);
      retryHistory.add(new ChatMessage("assistant", state.assistantText));
    }

    String nudge = "Your response did not include any tool calls. "
        + "If the user's message requires changes to the project, "
        + "use the appropriate tools now. If it was a question or "
        + "conversational message, you may respond with text only.";

    // Stateful providers already have context; re-sending would duplicate.
    List<String> retryContext = provider.isStateless() ? contextMessages : null;

    streamBuffer.resetStreaming();
    streamBuffer.init();
    streamBuffer.appendStatus("Preparing response...");

    LLMResponse retryResponse = provider.chat(
        systemPrompt, retryContext,
        AIAgentRequest.wrapPlatformMessage(nudge),
        tools, state.llmResponse.getProviderRef(), retryHistory, resolver, streamBuffer);

    if (AIDebug.enabled()) {
      AIDebug.log(LOG, "Narration retry: toolCalls="
          + retryResponse.getRawToolCalls().size()
          + ", textLen=" + (retryResponse.getText() != null
              ? retryResponse.getText().length() : 0));
    }

    ParsedResult retryParsed = parseAndEnforce(retryResponse, mode, currentView,
        enforcementContext);
    boolean retryActed = !retryParsed.operations.isEmpty()
        || !retryResponse.getRawToolCalls().isEmpty();

    if (retryActed) {
      // Retry produced tool calls — use its results.  Persist the
      // narration exchange so history keeps alternating roles.
      conversationManager.storeMessage(conv.getConversationId(),
          MessageRole.ASSISTANT, state.assistantText, false);
      conversationManager.storeMessage(conv.getConversationId(),
          MessageRole.USER, nudge, false);
      state.llmResponse = retryResponse;
      state.parsed = retryParsed;
      state.assistantText = retryResponse.getText() != null ? retryResponse.getText() : "";
    } else {
      // Retry also text-only — the model legitimately had nothing to
      // act on.  Keep the original text but take the retry's providerRef
      // so stateful providers stay in sync.
      state.llmResponse = new LLMResponse(state.llmResponse.getText(),
          state.llmResponse.getRawToolCalls(), retryResponse.getProviderRef(),
          state.llmResponse.hasMore());
    }
  }

  AIAgentResponse finalizeResponse(LLMResponse llmResponse, String assistantText,
      ParsedResult parsed, AIConversationState conv, long projectId, boolean isNew,
      String screenName) {
    // Annotate the continuation state with per-tool-call results so that
    // continueWithToolResults() can send accurate feedback instead of blanket "Done.".
    String annotatedRef = annotateToolCallResults(
        llmResponse.getProviderRef(), parsed.toolCallStatuses);

    // Save updated conversation state
    AIConversationState updated = new AIConversationState(conv.getProviderName(),
        conv.getConversationId(), annotatedRef);
    if (screenName != null) {
      conversationManager.saveConversation(projectId, screenName, updated);
    } else {
      conversationManager.saveConversation(projectId, updated);
    }

    // Save assistant response to history (with structured content if tool calls present).
    // When the LLM returns no text but has operations, generate a summary for the
    // conversation history so the LLM retains context.
    String historyText = assistantText;
    if (historyText.isEmpty() && !parsed.operations.isEmpty()) {
      historyText = ConversationManager.summarizeOperations(parsed.operations);
    }
    if (!llmResponse.getRawToolCalls().isEmpty()) {
      String[] pair = ConversationManager.buildStructuredContentPair(
          historyText, llmResponse.getRawToolCalls(), parsed.toolCallStatuses);
      // Display the assistant message only when the LLM produced real text;
      // generated operation summaries are for LLM context only.
      boolean displayAssistant = !assistantText.isEmpty();
      conversationManager.storeMessage(conv.getConversationId(),
          MessageRole.ASSISTANT, historyText, pair[0], displayAssistant);
      conversationManager.storeMessage(conv.getConversationId(),
          MessageRole.TOOL_RESULT, "[Tool results applied]", pair[1], false);
    } else {
      conversationManager.storeMessage(conv.getConversationId(),
          MessageRole.ASSISTANT, historyText, true);
    }

    new StreamBuffer(storageIo, projectId, screenName).clear();

    boolean hasMore = llmResponse.hasMore() && !parsed.operations.isEmpty();
    AIAgentResponse response = new AIAgentResponse(
        assistantText, parsed.operations, isNew, parsed.errors);
    response.setHasMore(hasMore);
    return response;
  }

  // ---------- Continuation state annotation ----------

  /**
   * Annotates the continuation state with per-tool-call results so that
   * {@link LLMProvider#continueWithToolResults} can send accurate feedback
   * instead of blanket "Done." for all pending tool calls.
   *
   * <p>Adds a {@code "toolCallResults"} JSON array to the continuation state,
   * positionally aligned with the pending tool call IDs.
   */
  static String annotateToolCallResults(String providerRef,
      List<ToolCallStatus> statuses) {
    if (providerRef == null || providerRef.isEmpty() || statuses == null) {
      return providerRef;
    }
    try {
      JSONObject state = new JSONObject(providerRef);
      if (!state.optBoolean("continuation", false)) {
        return providerRef;
      }

      JSONArray results = new JSONArray();
      for (ToolCallStatus s : statuses) {
        JSONObject entry = new JSONObject();
        entry.put("name", s.getToolName());
        switch (s.getOutcome()) {
          case ACCEPTED:
            if (AIToolNames.PROPOSE_PLAN.equals(s.getToolName())) {
              entry.put("result", "Plan delivered to user for review.");
            } else {
              entry.put("result", "Done.");
            }
            break;
          case PARSE_REJECTED:
          case MODE_REJECTED:
            entry.put("result", "REJECTED: " + s.getErrorMessage());
            break;
        }
        results.put(entry);
      }
      state.put("toolCallResults", results);
      return state.toString();
    } catch (Exception e) {
      LOG.warning("Failed to annotate tool call results: " + e.getMessage());
      return providerRef;
    }
  }

  /**
   * Patches the toolCallResults in the continuation state with client-side
   * execution outcomes. Iterates the existing toolCallResults, skipping
   * REJECTED entries (already resolved server-side), and maps each ACCEPTED
   * entry to the corresponding client result by position.
   */
  static String patchToolCallResults(String providerRef,
      List<AIOperationResult> clientResults, boolean isValidationRetry) {
    if (providerRef == null || providerRef.isEmpty()) {
      return null;
    }
    try {
      JSONObject state = new JSONObject(providerRef);
      JSONArray toolCallResults = state.optJSONArray("toolCallResults");
      if (toolCallResults == null) {
        return null;
      }

      int clientIdx = 0;
      for (int i = 0; i < toolCallResults.length(); i++) {
        JSONObject entry = toolCallResults.getJSONObject(i);
        String currentResult = entry.optString("result", "");
        if (currentResult.startsWith("REJECTED:")) {
          continue;
        }
        if (clientIdx < clientResults.size()) {
          AIOperationResult cr = clientResults.get(clientIdx);
          switch (cr.getStatus()) {
            case SUCCEEDED:
              entry.put("result", isValidationRetry
                  ? "Validated successfully. Pending application."
                  : "Applied successfully.");
              break;
            case FAILED:
              String detail = cr.getErrorDetail() != null
                  ? cr.getErrorDetail() : "Unknown error";
              entry.put("result", "FAILED: " + detail);
              break;
            case SKIPPED:
              entry.put("result",
                  "SKIPPED: execution halted after a prior failure.");
              break;
            case RUNTIME_READ:
              // Runtime reads carry the full tool-result text in summary —
              // use it verbatim instead of a canned outcome so the LLM sees
              // the actual value (e.g. read_component_property(Foo.Bar) = "x").
              String readText = cr.getSummary();
              entry.put("result", readText != null ? readText : "");
              break;
          }
          clientIdx++;
        }
      }
      state.put("toolCallResults", toolCallResults);
      return state.toString();
    } catch (Exception e) {
      LOG.warning("Failed to patch tool call results: " + e.getMessage());
      return null;
    }
  }

  // ---------- Continuation state patching ----------

  /**
   * Patches the serialized provider continuation state to use an updated
   * system prompt.  This ensures the LLM sees the current project state
   * (blocks, components) rather than the stale snapshot from the original
   * request.
   *
   * <p>For providers that store the system prompt in the continuation JSON
   * (e.g. Anthropic's {@code "systemPrompt"} field), the value is replaced
   * in-place.  For providers that embed it in the messages array (e.g.
   * OpenAI's first {@code "system"} message), the first system message is
   * updated.  If the format is unrecognized, the state is returned as-is
   * (safe fallback -- the LLM just sees the old prompt).
   */
  static String patchSystemPrompt(String providerRef, String newSystemPrompt) {
    try {
      JSONObject state = new JSONObject(providerRef);

      // Anthropic-style: top-level "systemPrompt" field
      if (state.has("systemPrompt")) {
        state.put("systemPrompt", newSystemPrompt);
        return state.toString();
      }

      // OpenAI-style: first message with role "system" in the messages array
      if (state.has("messages")) {
        JSONArray messages = state.getJSONArray("messages");
        for (int i = 0; i < messages.length(); i++) {
          JSONObject msg = messages.getJSONObject(i);
          if ("system".equals(msg.optString("role"))) {
            msg.put("content", newSystemPrompt);
            return state.toString();
          }
        }
      }
    } catch (Exception e) {
      LOG.warning("Failed to patch system prompt in continuation state: " + e.getMessage());
    }
    // Unrecognized format -- return as-is
    return providerRef;
  }

  // ---------- Helpers ----------

  static String getConfiguredProvider() {
    return Flag.createFlag("ai.agent.provider", "anthropic").get();
  }

  static String getConfiguredModel() {
    return Flag.createFlag("ai.agent.model", "").get();
  }

  static AIAgentResponse errorResponse(String message) {
    return new AIAgentResponse(message, Collections.<AIOperation>emptyList(), false,
        Collections.singletonList(message));
  }

  ConversationManager getConversationManager() {
    return conversationManager;
  }
}
