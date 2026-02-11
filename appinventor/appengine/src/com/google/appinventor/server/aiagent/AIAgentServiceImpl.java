// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent;

import com.google.appinventor.server.OdeRemoteServiceServlet;
import com.google.appinventor.server.aiagent.llm.ChatMessage;
import com.google.appinventor.server.aiagent.llm.LLMProvider;
import com.google.appinventor.server.aiagent.llm.LLMProviderException;
import com.google.appinventor.server.aiagent.llm.LLMProviderRegistry;
import com.google.appinventor.server.aiagent.llm.LLMResponse;
import com.google.appinventor.server.aiagent.llm.LLMTool;
import com.google.appinventor.server.aiagent.llm.RawToolCall;
import com.google.appinventor.server.aiagent.llm.ReadOnlyToolException;
import com.google.appinventor.server.aiagent.llm.ReadOnlyToolResolver;
import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.server.storage.AIConversationState;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.rpc.aiagent.AIAgentRequest;
import com.google.appinventor.shared.rpc.aiagent.AIAgentResponse;
import com.google.appinventor.shared.rpc.aiagent.AIAgentService;
import com.google.appinventor.shared.rpc.aiagent.AIConversationMessage;
import com.google.appinventor.shared.rpc.aiagent.AIOperation;
import com.google.appinventor.shared.settings.SettingsConstants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Server-side implementation of the AI Agent RPC service.
 *
 * <p>Flow: auth check -> conversation lookup -> context build -> LLM call
 * -> mode enforcement -> response.  Semantic validation (YAIL parsing,
 * block structure) is handled client-side by the Blockly runtime.
 */
public class AIAgentServiceImpl extends OdeRemoteServiceServlet
    implements AIAgentService {

  private static final Logger LOG = Logger.getLogger(AIAgentServiceImpl.class.getName());

  private static final int MAX_MESSAGE_LENGTH = 2000;
  private static final long RATE_WINDOW_MS = 60_000; // 1 minute
  private static final String COMPONENT_DB_RESOURCE =
      "/com/google/appinventor/simple_components.json";

  private static final Flag<Integer> RATE_LIMIT_FLAG =
      Flag.createFlag("ai.agent.rate.limit", 10);

  private final transient StorageIo storageIo = StorageIoInstanceHolder.getInstance();
  private final transient AIContextBuilder contextBuilder;
  private final transient LLMResponseParser responseParser = new LLMResponseParser();

  // Rate limiting: userId -> list of request timestamps
  private static final ConcurrentHashMap<String, List<Long>> rateLimitMap =
      new ConcurrentHashMap<>();

  // Cached component database JSON
  private static volatile String componentDbJson;

  public AIAgentServiceImpl() {
    contextBuilder = new AIContextBuilder(storageIo);
  }

  // ---------- RPC methods ----------

  @Override
  public AIAgentResponse processRequest(AIAgentRequest request) {
    String userId = userInfoProvider.getUserId();
    long projectId = request.getProjectId();
    String screenName = request.getScreenName();
    String userMessage = request.getUserMessage();

    // Reset per-request sequence counter
    messageSequence = 0;

    // Input validation
    if (userMessage == null || userMessage.trim().isEmpty()) {
      return errorResponse("Message cannot be empty.");
    }
    userMessage = sanitize(userMessage);
    if (userMessage.length() > MAX_MESSAGE_LENGTH) {
      return errorResponse("Message too long (max " + MAX_MESSAGE_LENGTH + " characters).");
    }

    // Project ownership check
    try {
      storageIo.assertUserHasProject(userId, projectId);
    } catch (SecurityException e) {
      return errorResponse("You do not have access to this project.");
    }

    // Rate limiting
    if (!checkRateLimit(userId)) {
      return errorResponse("Rate limit exceeded. Please wait before sending another message.");
    }

    // Get mode from project settings
    String mode = getProjectAIMode(userId, projectId);
    if ("Off".equals(mode)) {
      return errorResponse("AI agent is disabled for this project.");
    }

    // Debug: request entry
    AIDebug.log(LOG, "processRequest: userId=" + userId + ", projectId=" + projectId
        + ", screen=" + screenName + ", mode=" + mode
        + ", msgLen=" + userMessage.length());

    try {
      updateStatus(projectId, "Building context...");

      // Get or create conversation
      AIConversationState conv = getConversation(projectId);
      boolean isNew = (conv == null);
      if (isNew) {
        conv = new AIConversationState(getConfiguredProvider(),
            UUID.randomUUID().toString(), null);
      }

      // Check provider change
      String currentProvider = getConfiguredProvider();
      if (!currentProvider.equals(conv.getProviderName())) {
        clearConversationInternal(projectId);
        conv = new AIConversationState(currentProvider,
            UUID.randomUUID().toString(), null);
        isNew = true;
      }

      // Build system prompt and tools (using client-provided blocks YAIL and view)
      String blocksYail = request.getBlocksYail();
      String currentView = request.getCurrentView();
      String systemPrompt = contextBuilder.build(userId, projectId, screenName, mode,
          blocksYail, currentView);
      List<LLMTool> tools = contextBuilder.buildTools(mode, currentView);
      AIDebug.log(LOG, "System prompt built: length=" + systemPrompt.length() + " chars");

      // Build history for stateless providers
      LLMProvider provider = LLMProviderRegistry.get(conv.getProviderName());
      List<ChatMessage> history = Collections.emptyList();
      if (provider.isStateless()) {
        history = loadConversation(conv.getConversationId());
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
      ReadOnlyToolResolver resolver = createResolver(userId, projectId);

      AIDebug.log(LOG, "Pre-LLM: toolCount=" + tools.size()
          + ", provider=" + conv.getProviderName() + ", model=" + getConfiguredModel());

      updateStatus(projectId, "Calling AI...");

      // Call LLM
      LLMResponse llmResponse = provider.chat(
          systemPrompt, userMessage, tools, conv.getProviderRef(), history, resolver);

      // Save user message to history
      storeMessage(conv.getConversationId(), "user", userMessage);

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
      List<LLMResponseParser.RawToolCall> rawCalls = new ArrayList<>();
      for (RawToolCall tc : llmResponse.getRawToolCalls()) {
        rawCalls.add(new LLMResponseParser.RawToolCall(tc.getName(), tc.getArgumentsJson()));
      }

      LLMResponseParser.ParseResult parseResult = responseParser.parseToolCalls(rawCalls);
      List<String> allErrors = new ArrayList<>(parseResult.getErrors());
      List<AIOperation> operations = parseResult.getOperations();

      // Debug: parse result
      if (AIDebug.enabled()) {
        AIDebug.log(LOG, "Parse result: operations=" + operations.size()
            + ", errors=" + allErrors.size());
        for (AIOperation op : operations) {
          AIDebug.log(LOG, "  Parsed op: " + op.getType() + " payload=" + op.getPayload());
        }
        for (String err : allErrors) {
          AIDebug.log(LOG, "  Parse error: " + err);
        }
      }

      // Mode and view enforcement: strip operations not allowed in the current
      // mode or editor view.  Semantic validation is handled client-side.
      operations = enforceMode(operations, mode, currentView, allErrors);

      // Update conversation state
      conv = new AIConversationState(conv.getProviderName(), conv.getConversationId(),
          llmResponse.getProviderRef());
      saveConversation(projectId, conv);

      // Save assistant response to history
      String assistantText = llmResponse.getText() != null ? llmResponse.getText() : "";
      if (assistantText.isEmpty() && !operations.isEmpty()) {
        assistantText = summarizeOperations(operations);
      }
      storeMessage(conv.getConversationId(), "assistant", assistantText);

      clearStatus(projectId);

      boolean hasMore = llmResponse.hasMore() && !operations.isEmpty();
      AIDebug.log(LOG, "Final response: operations=" + operations.size()
          + ", errors=" + allErrors.size()
          + ", hasMore=" + hasMore
          + ", textLen=" + assistantText.length());

      AIAgentResponse agentResponse =
          new AIAgentResponse(assistantText, operations, isNew, allErrors);
      agentResponse.setHasMore(hasMore);
      return agentResponse;

    } catch (LLMProviderException e) {
      LOG.log(Level.WARNING, "LLM provider error", e);
      clearStatus(projectId);
      return errorResponse(e.getUserFacingMessage());
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Unexpected error in AI agent", e);
      clearStatus(projectId);
      return errorResponse("An unexpected error occurred. Please try again.");
    }
  }

  @Override
  public AIAgentResponse continueRequest(long projectId, String screenName,
      String blocksYail, String currentView) {
    String userId = userInfoProvider.getUserId();

    // Project ownership check
    try {
      storageIo.assertUserHasProject(userId, projectId);
    } catch (SecurityException e) {
      return errorResponse("You do not have access to this project.");
    }

    // Get mode
    String mode = getProjectAIMode(userId, projectId);
    if ("Off".equals(mode)) {
      return errorResponse("AI agent is disabled for this project.");
    }

    AIDebug.log(LOG, "continueRequest: userId=" + userId + ", projectId=" + projectId
        + ", screen=" + screenName + ", mode=" + mode);

    try {
      updateStatus(projectId, "Continuing AI response...");

      // Load conversation state from memcache
      AIConversationState conv = getConversation(projectId);
      if (conv == null || conv.getProviderRef() == null || conv.getProviderRef().isEmpty()) {
        clearStatus(projectId);
        return errorResponse("No continuation state available. Please start a new request.");
      }

      // Get provider and rebuild tools
      LLMProvider provider = LLMProviderRegistry.get(conv.getProviderName());
      List<LLMTool> tools = contextBuilder.buildTools(mode, currentView);
      ReadOnlyToolResolver resolver = createResolver(userId, projectId);

      // Rebuild the system prompt with updated blocks state so the LLM
      // sees the result of the previous batch's operations.
      String updatedSystemPrompt = contextBuilder.build(
          userId, projectId, screenName, mode, blocksYail, currentView);
      String providerRef = patchSystemPrompt(conv.getProviderRef(), updatedSystemPrompt);

      AIDebug.log(LOG, "continueRequest: provider=" + conv.getProviderName()
          + ", providerRef length=" + providerRef.length());

      updateStatus(projectId, "Calling AI...");

      // Call the continuation method with updated state
      LLMResponse llmResponse = provider.continueWithToolResults(
          providerRef, tools, resolver);

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

      // Parse tool calls
      List<LLMResponseParser.RawToolCall> rawCalls = new ArrayList<>();
      for (RawToolCall tc : llmResponse.getRawToolCalls()) {
        rawCalls.add(new LLMResponseParser.RawToolCall(tc.getName(), tc.getArgumentsJson()));
      }

      LLMResponseParser.ParseResult parseResult = responseParser.parseToolCalls(rawCalls);
      List<String> allErrors = new ArrayList<>(parseResult.getErrors());
      List<AIOperation> operations = parseResult.getOperations();

      // Mode and view enforcement
      operations = enforceMode(operations, mode, currentView, allErrors);

      // Update conversation state with new providerRef
      conv = new AIConversationState(conv.getProviderName(), conv.getConversationId(),
          llmResponse.getProviderRef());
      saveConversation(projectId, conv);

      // Save assistant response to history
      String assistantText = llmResponse.getText() != null ? llmResponse.getText() : "";
      if (assistantText.isEmpty() && !operations.isEmpty()) {
        assistantText = summarizeOperations(operations);
      }
      storeMessage(conv.getConversationId(), "assistant", assistantText);

      clearStatus(projectId);

      boolean hasMore = llmResponse.hasMore() && !operations.isEmpty();
      AIDebug.log(LOG, "Continue response: operations=" + operations.size()
          + ", errors=" + allErrors.size()
          + ", hasMore=" + hasMore
          + ", textLen=" + assistantText.length());

      AIAgentResponse agentResponse =
          new AIAgentResponse(assistantText, operations, false, allErrors);
      agentResponse.setHasMore(hasMore);
      return agentResponse;

    } catch (LLMProviderException e) {
      LOG.log(Level.WARNING, "LLM provider error in continuation", e);
      clearStatus(projectId);
      return errorResponse(e.getUserFacingMessage());
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Unexpected error in AI agent continuation", e);
      clearStatus(projectId);
      return errorResponse("An unexpected error occurred. Please try again.");
    }
  }

  @Override
  public AIAgentResponse reportExecutionErrors(long projectId, String screenName,
      List<String> errors, String blocksYail, String currentView) {
    String userId = userInfoProvider.getUserId();

    // Security check
    try {
      storageIo.assertUserHasProject(userId, projectId);
    } catch (SecurityException e) {
      return errorResponse("You do not have access to this project.");
    }

    // Mode check
    String mode = getProjectAIMode(userId, projectId);
    if ("Off".equals(mode)) {
      return errorResponse("AI agent is disabled for this project.");
    }

    if (errors == null || errors.isEmpty()) {
      return errorResponse("No errors to report.");
    }

    AIDebug.log(LOG, "reportExecutionErrors: userId=" + userId
        + ", projectId=" + projectId + ", errors=" + errors.size());

    try {
      updateStatus(projectId, "Retrying with error feedback...");

      // Load conversation state
      AIConversationState conv = getConversation(projectId);
      if (conv == null || conv.getProviderRef() == null || conv.getProviderRef().isEmpty()) {
        clearStatus(projectId);
        return errorResponse("No conversation state available. Please start a new request.");
      }

      // Build feedback message from client errors
      String feedback = LLMResponseParser.buildValidationErrorFeedback(errors);
      LOG.info("Retrying LLM with client execution errors: " + feedback);

      // Get provider and tools
      LLMProvider provider = LLMProviderRegistry.get(conv.getProviderName());
      List<LLMTool> tools = contextBuilder.buildTools(mode, currentView);
      List<ChatMessage> history = loadConversation(conv.getConversationId());
      ReadOnlyToolResolver resolver = createResolver(userId, projectId);

      // Build system prompt with the current blocks state from the client
      String systemPrompt = contextBuilder.build(userId, projectId, screenName,
          mode, blocksYail, currentView);

      updateStatus(projectId, "Calling AI...");

      // Retry LLM with the error feedback
      LLMResponse llmResponse = provider.chat(
          systemPrompt, feedback, tools, conv.getProviderRef(), history, resolver);

      // Parse and validate the retry response
      List<LLMResponseParser.RawToolCall> rawCalls = new ArrayList<>();
      for (RawToolCall tc : llmResponse.getRawToolCalls()) {
        rawCalls.add(new LLMResponseParser.RawToolCall(tc.getName(), tc.getArgumentsJson()));
      }

      LLMResponseParser.ParseResult parseResult = responseParser.parseToolCalls(rawCalls);
      List<String> allErrors = new ArrayList<>(parseResult.getErrors());
      List<AIOperation> operations = parseResult.getOperations();

      // Mode and view enforcement
      operations = enforceMode(operations, mode, currentView, allErrors);

      // Update conversation state
      conv = new AIConversationState(conv.getProviderName(), conv.getConversationId(),
          llmResponse.getProviderRef());
      saveConversation(projectId, conv);

      // Save error report and retry response to history
      storeMessage(conv.getConversationId(), "user",
          "[Execution error feedback] " + feedback);
      String assistantText = llmResponse.getText() != null ? llmResponse.getText() : "";
      if (assistantText.isEmpty() && !operations.isEmpty()) {
        assistantText = summarizeOperations(operations);
      }
      storeMessage(conv.getConversationId(), "assistant", assistantText);

      clearStatus(projectId);

      boolean hasMore = llmResponse.hasMore() && !operations.isEmpty();
      AIDebug.log(LOG, "Error retry response: operations=" + operations.size()
          + ", errors=" + allErrors.size() + ", hasMore=" + hasMore);

      AIAgentResponse agentResponse =
          new AIAgentResponse(assistantText, operations, false, allErrors);
      agentResponse.setHasMore(hasMore);
      return agentResponse;

    } catch (LLMProviderException e) {
      LOG.log(Level.WARNING, "LLM provider error in error retry", e);
      clearStatus(projectId);
      return errorResponse(e.getUserFacingMessage());
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Unexpected error in error retry", e);
      clearStatus(projectId);
      return errorResponse("An unexpected error occurred during retry. Please try again.");
    }
  }

  @Override
  public void clearConversation(long projectId) {
    String userId = userInfoProvider.getUserId();
    try {
      storageIo.assertUserHasProject(userId, projectId);
    } catch (SecurityException e) {
      throw new SecurityException("You do not have access to this project.");
    }
    clearConversationInternal(projectId);
  }

  @Override
  public List<AIConversationMessage> getConversationHistory(long projectId) {
    String userId = userInfoProvider.getUserId();
    try {
      storageIo.assertUserHasProject(userId, projectId);
    } catch (SecurityException e) {
      return Collections.emptyList();
    }

    AIConversationState conv = getConversation(projectId);
    if (conv == null) {
      return Collections.emptyList();
    }

    List<ChatMessage> history = loadConversation(conv.getConversationId());
    List<AIConversationMessage> result = new ArrayList<>();
    for (ChatMessage msg : history) {
      result.add(new AIConversationMessage(msg.getRole(), msg.getText()));
    }
    return result;
  }

  @Override
  public String getRequestStatus(long projectId) {
    return storageIo.getAIRequestStatus(projectId);
  }

  // ---------- Conversation management ----------

  private AIConversationState getConversation(long projectId) {
    return storageIo.getAIConversationState(projectId);
  }

  private void saveConversation(long projectId, AIConversationState state) {
    storageIo.saveAIConversationState(projectId, state);
  }

  private void clearConversationInternal(long projectId) {
    AIConversationState conv = getConversation(projectId);
    storageIo.clearAIConversationState(projectId);
    clearStatus(projectId);

    // Delete stored messages from Datastore
    if (conv != null) {
      storageIo.deleteAIConversationMessages(conv.getConversationId());
    }
  }

  /**
   * Per-request sequence counter. Ensures unique ordering when multiple messages
   * are stored within the same millisecond (user + assistant in one request).
   * Reset to 0 at the start of each processRequest() call.
   */
  private int messageSequence;

  private void storeMessage(String conversationId, String role, String text) {
    long now = System.currentTimeMillis();
    int seq = messageSequence++;
    storageIo.storeAIConversationMessage(conversationId, now, seq, role, text);
  }

  private List<ChatMessage> loadConversation(String conversationId) {
    List<String[]> messages = storageIo.loadAIConversationMessages(conversationId);
    List<ChatMessage> result = new ArrayList<>();
    for (String[] pair : messages) {
      result.add(new ChatMessage(pair[0], pair[1]));
    }
    return result;
  }

  // ---------- Status updates ----------

  private void updateStatus(long projectId, String status) {
    storageIo.updateAIRequestStatus(projectId, status);
  }

  private void clearStatus(long projectId) {
    storageIo.clearAIRequestStatus(projectId);
  }

  // ---------- Read-only tool resolver ----------

  private ReadOnlyToolResolver createResolver(String userId, long projectId) {
    return new ReadOnlyToolResolver() {
      @Override
      public boolean isReadOnly(String toolName) {
        return "lookup_component".equals(toolName) || "lookup_screen".equals(toolName);
      }

      @Override
      public String resolve(String toolName, String argsJson) throws ReadOnlyToolException {
        try {
          JSONObject args = new JSONObject(argsJson);
          switch (toolName) {
            case "lookup_component":
              return resolveLookupComponent(args.optString("component_type", ""));
            case "lookup_screen":
              return resolveLookupScreen(userId, projectId,
                  args.optString("screen_name", ""));
            default:
              throw new ReadOnlyToolException("Unknown read-only tool: " + toolName);
          }
        } catch (ReadOnlyToolException e) {
          throw e;
        } catch (Exception e) {
          throw new ReadOnlyToolException("Failed to resolve tool " + toolName
              + ": " + e.getMessage(), e);
        }
      }
    };
  }

  private String resolveLookupComponent(String componentType) throws ReadOnlyToolException {
    if (componentType == null || componentType.isEmpty()) {
      throw new ReadOnlyToolException("component_type is required");
    }
    String db = getComponentDb();
    try {
      JSONArray components = new JSONArray(db);
      for (int i = 0; i < components.length(); i++) {
        JSONObject comp = components.getJSONObject(i);
        if (componentType.equals(comp.optString("name"))
            || componentType.equals(comp.optString("type"))) {
          return comp.toString(2);
        }
      }
    } catch (Exception e) {
      throw new ReadOnlyToolException("Failed to search component database: " + e.getMessage());
    }
    throw new ReadOnlyToolException("Component not found: " + componentType);
  }

  private String resolveLookupScreen(String userId, long projectId, String screenName)
      throws ReadOnlyToolException {
    if (screenName == null || screenName.isEmpty()) {
      throw new ReadOnlyToolException("screen_name is required");
    }
    try {
      return contextBuilder.buildScreenState(userId, projectId, screenName);
    } catch (Exception e) {
      throw new ReadOnlyToolException("Failed to look up screen " + screenName
          + ": " + e.getMessage());
    }
  }

  // ---------- Mode and view enforcement ----------

  /** All write operation types (everything except read-only lookups and navigation). */
  private static final Set<AIOperation.Type> WRITE_OPS =
      Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
          AIOperation.Type.ADD_COMPONENT,
          AIOperation.Type.DELETE_COMPONENT,
          AIOperation.Type.SET_PROPERTY,
          AIOperation.Type.RENAME_COMPONENT,
          AIOperation.Type.WRITE_BLOCK,
          AIOperation.Type.DELETE_BLOCK,
          AIOperation.Type.CREATE_SCREEN,
          AIOperation.Type.DELETE_SCREEN,
          AIOperation.Type.SET_PROJECT_PROP)));

  /** Project-level operations only allowed in ProjectEditor mode. */
  private static final Set<AIOperation.Type> PROJECT_LEVEL_OPS =
      Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
          AIOperation.Type.CREATE_SCREEN,
          AIOperation.Type.DELETE_SCREEN,
          AIOperation.Type.SET_PROJECT_PROP)));

  /** Operations that require Designer view. */
  private static final Set<AIOperation.Type> DESIGNER_OPS =
      Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
          AIOperation.Type.ADD_COMPONENT,
          AIOperation.Type.DELETE_COMPONENT,
          AIOperation.Type.SET_PROPERTY,
          AIOperation.Type.RENAME_COMPONENT)));

  /** Operations that require Blocks view. */
  private static final Set<AIOperation.Type> BLOCKS_OPS =
      Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
          AIOperation.Type.WRITE_BLOCK,
          AIOperation.Type.DELETE_BLOCK)));

  /** Navigation operations that must appear alone (no other ops in same batch). */
  private static final Set<AIOperation.Type> SOLO_OPS =
      Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
          AIOperation.Type.TOGGLE_EDITOR,
          AIOperation.Type.SWITCH_SCREEN)));

  /**
   * Enforce mode, view, and solo-op restrictions on operations.
   * Returns the filtered list; rejected operations are reported in {@code errors}.
   */
  private List<AIOperation> enforceMode(List<AIOperation> operations,
      String mode, String currentView, List<String> errors) {
    if (operations.isEmpty()) {
      return operations;
    }

    // Solo-op detection: if any solo op is present alongside other ops,
    // keep only the solo op(s) and reject the rest.
    boolean hasSoloOp = false;
    boolean hasOtherOp = false;
    for (AIOperation op : operations) {
      if (SOLO_OPS.contains(op.getType())) {
        hasSoloOp = true;
      } else {
        hasOtherOp = true;
      }
    }

    List<AIOperation> accepted = new ArrayList<>();
    for (AIOperation op : operations) {
      boolean rejected = false;

      // Mode enforcement
      if ("Advisor".equals(mode) && WRITE_OPS.contains(op.getType())) {
        errors.add("Advisor mode does not allow write operations. Rejected: "
            + op.getType());
        rejected = true;
      } else if ("ScreenEditor".equals(mode)
          && PROJECT_LEVEL_OPS.contains(op.getType())) {
        errors.add("ScreenEditor mode does not allow project-level operations. "
            + "Rejected: " + op.getType());
        rejected = true;
      }

      // View enforcement
      if (!rejected && "Designer".equals(currentView)
          && BLOCKS_OPS.contains(op.getType())) {
        errors.add("Block operations require Blocks view. Currently in Designer. "
            + "Rejected: " + op.getType());
        rejected = true;
      }
      if (!rejected && "Blocks".equals(currentView)
          && DESIGNER_OPS.contains(op.getType())) {
        errors.add("Designer operations require Designer view. Currently in Blocks. "
            + "Rejected: " + op.getType());
        rejected = true;
      }

      // Solo-op enforcement
      if (!rejected && hasSoloOp && hasOtherOp && !SOLO_OPS.contains(op.getType())) {
        errors.add("toggle_editor/switch_screen must be the only operation. "
            + "Rejected: " + op.getType());
        rejected = true;
      }

      if (!rejected) {
        accepted.add(op);
      }
    }

    AIDebug.log(LOG, "Mode enforcement (" + mode + ", view=" + currentView + "): accepted="
        + accepted.size() + ", rejected=" + (operations.size() - accepted.size()));
    return accepted;
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
   * (safe fallback — the LLM just sees the old prompt).
   */
  private static String patchSystemPrompt(String providerRef, String newSystemPrompt) {
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
    // Unrecognized format — return as-is
    return providerRef;
  }

  // ---------- Helpers ----------

  private String getProjectAIMode(String userId, long projectId) {
    try {
      // Read Screen1's SCM to get the AIAgentMode property
      // The mode is stored as a project-level setting synced from Screen1's Form properties
      List<String> files = storageIo.getProjectSourceFiles(userId, projectId);
      for (String fileId : files) {
        // Find Screen1.scm
        if (fileId.endsWith("/Screen1.scm")) {
          String scmContent = storageIo.downloadFile(userId, projectId, fileId, "UTF-8");
          String json = extractScmJson(scmContent);
          if (json != null) {
            JSONObject root = new JSONObject(json);
            JSONObject props = root.optJSONObject("Properties");
            if (props != null) {
              String mode = props.optString(
                  SettingsConstants.YOUNG_ANDROID_SETTINGS_AI_AGENT_MODE, "Off");
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
    return "Off";
  }

  private String extractScmJson(String scmContent) {
    if (scmContent == null || scmContent.isEmpty()) {
      return null;
    }
    int jsonStart = scmContent.indexOf("{");
    int jsonEnd = scmContent.lastIndexOf("}");
    if (jsonStart >= 0 && jsonEnd > jsonStart) {
      return scmContent.substring(jsonStart, jsonEnd + 1);
    }
    return null;
  }

  private boolean checkRateLimit(String userId) {
    int limit = RATE_LIMIT_FLAG.get();
    long now = System.currentTimeMillis();

    rateLimitMap.putIfAbsent(userId, Collections.synchronizedList(new ArrayList<Long>()));
    List<Long> timestamps = rateLimitMap.get(userId);

    synchronized (timestamps) {
      // Remove old timestamps
      while (!timestamps.isEmpty() && timestamps.get(0) < now - RATE_WINDOW_MS) {
        timestamps.remove(0);
      }
      if (timestamps.size() >= limit) {
        return false;
      }
      timestamps.add(now);
      return true;
    }
  }

  private String sanitize(String input) {
    // Strip control characters except newline and tab
    StringBuilder sb = new StringBuilder(input.length());
    for (int i = 0; i < input.length(); i++) {
      char c = input.charAt(i);
      if (c >= 32 || c == '\n' || c == '\t') {
        sb.append(c);
      }
    }
    return sb.toString().trim();
  }

  private static String getConfiguredProvider() {
    return Flag.createFlag("ai.agent.provider", "anthropic").get();
  }

  private static String getConfiguredModel() {
    return Flag.createFlag("ai.agent.model", "").get();
  }

  private static String getComponentDb() {
    if (componentDbJson == null) {
      try (InputStream is = AIAgentServiceImpl.class.getResourceAsStream(COMPONENT_DB_RESOURCE)) {
        if (is == null) {
          LOG.warning("Component database not found: " + COMPONENT_DB_RESOURCE);
          componentDbJson = "[]";
        } else {
          BufferedReader reader = new BufferedReader(
              new InputStreamReader(is, StandardCharsets.UTF_8));
          StringBuilder sb = new StringBuilder();
          String line;
          while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
          }
          componentDbJson = sb.toString();
        }
      } catch (IOException e) {
        LOG.warning("Failed to load component database: " + e.getMessage());
        componentDbJson = "[]";
      }
    }
    return componentDbJson;
  }

  /**
   * Build a brief summary of operations when the LLM returns no text.
   */
  private static String summarizeOperations(List<AIOperation> operations) {
    StringBuilder sb = new StringBuilder();
    for (AIOperation op : operations) {
      try {
        JSONObject payload = new JSONObject(op.getPayload());
        switch (op.getType()) {
          case ADD_COMPONENT:
            sb.append("Added ").append(payload.optString("component_type"))
                .append(" '").append(payload.optString("name")).append("'\n");
            break;
          case DELETE_COMPONENT:
            sb.append("Deleted component '").append(payload.optString("name")).append("'\n");
            break;
          case SET_PROPERTY:
            sb.append("Set ").append(payload.optString("component_name"))
                .append(".").append(payload.optString("property_name"))
                .append(" to ").append(payload.optString("value")).append("\n");
            break;
          case RENAME_COMPONENT:
            sb.append("Renamed '").append(payload.optString("old_name"))
                .append("' to '").append(payload.optString("new_name")).append("'\n");
            break;
          case WRITE_BLOCK:
            String yail = payload.optString("yail", "");
            sb.append("Wrote block: ").append(summarizeYailHead(yail)).append("\n");
            break;
          case DELETE_BLOCK:
            sb.append("Deleted block: ").append(payload.optString("block")).append("\n");
            break;
          case CREATE_SCREEN:
            sb.append("Created screen '").append(payload.optString("screen_name")).append("'\n");
            break;
          case DELETE_SCREEN:
            sb.append("Deleted screen '").append(payload.optString("screen_name")).append("'\n");
            break;
          case SWITCH_SCREEN:
            sb.append("Switched to screen '").append(payload.optString("screen_name"))
                .append("'\n");
            break;
          case SET_PROJECT_PROP:
            sb.append("Set project property ").append(payload.optString("property"))
                .append(" to ").append(payload.optString("value")).append("\n");
            break;
          case TOGGLE_EDITOR:
            sb.append("Switched to ").append(payload.optString("view")).append(" view\n");
            break;
          default:
            sb.append(op.getType().name()).append("\n");
            break;
        }
      } catch (Exception e) {
        sb.append(op.getType().name()).append("\n");
      }
    }
    return sb.toString().trim();
  }

  /**
   * Extract a human-readable summary from the head of a YAIL S-expression.
   * E.g., "(define-event Button1 Click ...)" -> "define-event Button1 Click"
   */
  private static String summarizeYailHead(String yail) {
    if (yail == null || yail.isEmpty()) {
      return "(unknown)";
    }
    // Strip leading whitespace and opening parens
    String trimmed = yail.trim();
    if (trimmed.startsWith("(")) {
      trimmed = trimmed.substring(1).trim();
    }
    // Take the first few space-separated tokens
    String[] tokens = trimmed.split("\\s+", 5);
    int tokenCount = Math.min(tokens.length, 4);
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < tokenCount; i++) {
      if (i > 0) sb.append(" ");
      String token = tokens[i].replaceAll("[()]", "");
      if (token.isEmpty()) continue;
      sb.append(token);
    }
    return sb.toString();
  }

  private static AIAgentResponse errorResponse(String message) {
    return new AIAgentResponse(message, Collections.<AIOperation>emptyList(), false,
        Collections.singletonList(message));
  }

}
