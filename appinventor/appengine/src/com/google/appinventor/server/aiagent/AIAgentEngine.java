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
import com.google.appinventor.shared.rpc.aiagent.AIAgentResponse;
import com.google.appinventor.shared.rpc.aiagent.AIConversationMessage;
import com.google.appinventor.shared.rpc.aiagent.AIOperation;
import com.google.appinventor.shared.rpc.aiagent.AIOperationResult;
import com.google.appinventor.shared.rpc.aiagent.AIStreamStatus;
import com.google.appinventor.shared.settings.SettingsConstants;

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
   * @return the AI agent response
   */
  public AIAgentResponse processRequest(String userId, long projectId, String screenName,
      String userMessage, String blocksYail, String currentView, String mode,
      String screenComponentsJson, String projectSnapshot, String blockWarnings,
      String locale, String languageDisplayName) {
    AIDebug.log(LOG, "processRequest: userId=" + userId + ", projectId=" + projectId
        + ", screen=" + screenName + ", mode=" + mode
        + ", msgLen=" + userMessage.length());

    StreamBuffer streamBuffer = new StreamBuffer(storageIo, projectId);
    try {
      streamBuffer.init();
      streamBuffer.appendStatus("Building context...");

      // Get or create conversation (resets on provider change)
      ConversationInit init = initConversation(projectId);
      AIConversationState conv = init.conv;
      boolean isNew = init.isNew;

      // Build system prompt, context messages, and tools
      String systemPrompt = contextBuilder.build();
      List<String> contextMessages = contextBuilder.buildContextMessages(
          userId, projectId, screenName, mode, blocksYail, currentView,
          screenComponentsJson, projectSnapshot, blockWarnings,
          locale, languageDisplayName);
      List<LLMTool> tools = contextBuilder.buildTools(mode, currentView);
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

      // Call LLM (context messages are sent as separate turns by the provider)
      LLMResponse llmResponse = provider.chat(
          systemPrompt, contextMessages, userMessage, tools, conv.getProviderRef(),
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
      ParsedResult parsed = parseAndEnforce(llmResponse, mode, currentView);

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

      AIDebug.log(LOG, "Final response: operations=" + parsed.operations.size()
          + ", errors=" + parsed.errors.size()
          + ", hasMore=" + (llmResponse.hasMore() && !parsed.operations.isEmpty())
          + ", textLen=" + assistantText.length());

      return finalizeResponse(llmResponse, assistantText, parsed,
          conv, projectId, isNew);

    } catch (LLMProviderException e) {
      LOG.log(Level.WARNING, "LLM provider error", e);
      streamBuffer.clear();
      return errorResponse(e.getUserFacingMessage());
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Unexpected error in AI agent", e);
      streamBuffer.clear();
      return errorResponse("An unexpected error occurred. Please try again.");
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
      String locale, String languageDisplayName) {
    AIDebug.log(LOG, "continueRequest: userId=" + userId + ", projectId=" + projectId
        + ", screen=" + screenName + ", mode=" + mode);

    StreamBuffer streamBuffer = new StreamBuffer(storageIo, projectId);
    try {
      streamBuffer.init();
      streamBuffer.appendStatus("Continuing AI response...");

      // Load conversation state from memcache
      AIConversationState conv = conversationManager.getConversation(projectId);
      if (conv == null || conv.getProviderRef() == null || conv.getProviderRef().isEmpty()) {
        streamBuffer.clear();
        return errorResponse("No continuation state available. Please start a new request.");
      }

      // Get provider and rebuild tools
      LLMProvider provider = LLMProviderRegistry.get(conv.getProviderName());
      List<LLMTool> tools = contextBuilder.buildTools(mode, currentView);
      ReadOnlyToolResolver resolver = toolResolver.createResolver(userId, projectId);

      // Patch the static system prompt into continuation state
      String systemPrompt = contextBuilder.build();
      String providerRef = patchSystemPrompt(conv.getProviderRef(), systemPrompt);

      AIDebug.log(LOG, "continueRequest: provider=" + conv.getProviderName()
          + ", providerRef length=" + providerRef.length());

      // Save continuation marker to history BEFORE calling the LLM,
      // so it is persisted even if the LLM call fails.
      conversationManager.storeMessage(conv.getConversationId(),
          MessageRole.USER, "[Continuation requested]", false);

      streamBuffer.appendStatus("Calling AI...");

      // Call the continuation method with updated state
      LLMResponse llmResponse = provider.continueWithToolResults(
          providerRef, tools, resolver, streamBuffer);

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
      ParsedResult parsed = parseAndEnforce(llmResponse, mode, currentView);
      String assistantText = llmResponse.getText() != null ? llmResponse.getText() : "";

      AIDebug.log(LOG, "Continue response: operations=" + parsed.operations.size()
          + ", errors=" + parsed.errors.size()
          + ", hasMore=" + (llmResponse.hasMore() && !parsed.operations.isEmpty())
          + ", textLen=" + assistantText.length());

      return finalizeResponse(llmResponse, assistantText, parsed, conv, projectId, false);

    } catch (LLMProviderException e) {
      LOG.log(Level.WARNING, "LLM provider error in continuation", e);
      streamBuffer.clear();
      return errorResponse(e.getUserFacingMessage());
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Unexpected error in AI agent continuation", e);
      streamBuffer.clear();
      return errorResponse("An unexpected error occurred. Please try again.");
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
      String blockWarnings, String locale, String languageDisplayName) {
    AIDebug.log(LOG, "reportExecutionErrors: userId=" + userId
        + ", projectId=" + projectId + ", results=" + results.size()
        + ", retryAttempt=" + retryAttempt + ", totalTools=" + totalTools);

    StreamBuffer streamBuffer = new StreamBuffer(storageIo, projectId);
    try {
      streamBuffer.init();

      // Count per-status totals for the status message.
      // Use totalTools from the client when available — on subsequent retries
      // the results list shrinks (only failed/skipped ops are re-emitted).
      int failedCount = 0;
      for (AIOperationResult r : results) {
        if (r.getStatus() == AIOperationResult.Status.FAILED) {
          failedCount++;
        }
      }
      int totalCount = totalTools > 0 ? totalTools : results.size();

      String retryInfo = failedCount + " out of " + totalCount + " tools failed"
          + (retryAttempt > 0 ? ", retry attempt " + retryAttempt : "");
      streamBuffer.appendStatus("Analyzing errors (" + retryInfo + ")...");

      // Load conversation state
      AIConversationState conv = conversationManager.getConversation(projectId);
      if (conv == null || conv.getProviderRef() == null || conv.getProviderRef().isEmpty()) {
        streamBuffer.clear();
        return errorResponse("No conversation state available. Please start a new request.");
      }

      // Extract structured results directly from typed DTOs.
      List<String> succeededSummaries = new ArrayList<>();
      List<String> failedDetails = new ArrayList<>();
      List<String> skippedSummaries = new ArrayList<>();
      for (AIOperationResult r : results) {
        switch (r.getStatus()) {
          case SUCCEEDED:
            succeededSummaries.add(r.getSummary());
            break;
          case FAILED:
            String detail = r.getSummary();
            if (r.getErrorDetail() != null && !r.getErrorDetail().isEmpty()) {
              detail += " -- Error: " + r.getErrorDetail();
            }
            failedDetails.add(detail);
            break;
          case SKIPPED:
            skippedSummaries.add(r.getSummary());
            break;
        }
      }

      String feedback = LLMResponseParser.buildExecutionErrorFeedback(
          succeededSummaries, failedDetails, skippedSummaries);
      LOG.info("Retrying LLM with client execution errors: " + feedback);

      // Get provider and tools
      LLMProvider provider = LLMProviderRegistry.get(conv.getProviderName());
      List<LLMTool> tools = contextBuilder.buildTools(mode, currentView);
      List<ChatMessage> history = Collections.emptyList();
      if (provider.isStateless()) {
        history = conversationManager.loadConversation(conv.getConversationId());
      }
      ReadOnlyToolResolver resolver = toolResolver.createResolver(userId, projectId);

      // Build system prompt and context messages with current blocks state
      String systemPrompt = contextBuilder.build();
      List<String> contextMessages = contextBuilder.buildContextMessages(
          userId, projectId, screenName, mode, blocksYail, currentView,
          screenComponentsJson, projectSnapshot, blockWarnings,
          locale, languageDisplayName);

      // Save error feedback to history BEFORE calling the LLM,
      // so it is persisted even if the LLM call fails.
      conversationManager.storeMessage(conv.getConversationId(),
          MessageRole.USER, "[Execution error feedback] " + feedback, false);

      streamBuffer.appendStatus("Calling AI (" + retryInfo + ")...");

      // Retry LLM with the error feedback
      LLMResponse llmResponse = provider.chat(
          systemPrompt, contextMessages, feedback, tools, conv.getProviderRef(),
          history, resolver, streamBuffer);

      // Parse, enforce, save, and build response
      ParsedResult parsed = parseAndEnforce(llmResponse, mode, currentView);
      String assistantText = llmResponse.getText() != null ? llmResponse.getText() : "";

      AIDebug.log(LOG, "Error retry response: operations=" + parsed.operations.size()
          + ", errors=" + parsed.errors.size()
          + ", hasMore=" + (llmResponse.hasMore() && !parsed.operations.isEmpty()));

      return finalizeResponse(llmResponse, assistantText, parsed, conv, projectId, false);

    } catch (LLMProviderException e) {
      LOG.log(Level.WARNING, "LLM provider error in error retry", e);
      streamBuffer.clear();
      return errorResponse(e.getUserFacingMessage());
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Unexpected error in error retry", e);
      streamBuffer.clear();
      return errorResponse("An unexpected error occurred during retry. Please try again.");
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
    StreamBuffer streamBuffer = new StreamBuffer(storageIo, projectId);
    return streamBuffer.consume();
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
   */
  ConversationInit initConversation(long projectId) {
    AIConversationState conv = conversationManager.getConversation(projectId);
    boolean isNew = (conv == null);
    if (isNew) {
      conv = new AIConversationState(getConfiguredProvider(),
          UUID.randomUUID().toString(), null);
    }
    String currentProvider = getConfiguredProvider();
    if (!currentProvider.equals(conv.getProviderName())) {
      conversationManager.clearConversation(projectId);
      conv = new AIConversationState(currentProvider,
          UUID.randomUUID().toString(), null);
      isNew = true;
    }
    return new ConversationInit(conv, isNew);
  }

  // ---------- Response pipeline ----------

  ParsedResult parseAndEnforce(LLMResponse llmResponse,
      String mode, String currentView) {
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
        allParsedOps, mode, currentView, enforceErrors);
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

  AIAgentResponse finalizeResponse(LLMResponse llmResponse, String assistantText,
      ParsedResult parsed, AIConversationState conv, long projectId, boolean isNew) {
    // Annotate the continuation state with per-tool-call results so that
    // continueWithToolResults() can send accurate feedback instead of blanket "Done.".
    String annotatedRef = annotateToolCallResults(
        llmResponse.getProviderRef(), parsed.toolCallStatuses);

    // Save updated conversation state
    AIConversationState updated = new AIConversationState(conv.getProviderName(),
        conv.getConversationId(), annotatedRef);
    conversationManager.saveConversation(projectId, updated);

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

    new StreamBuffer(storageIo, projectId).clear();

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
            entry.put("result", "Done.");
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
