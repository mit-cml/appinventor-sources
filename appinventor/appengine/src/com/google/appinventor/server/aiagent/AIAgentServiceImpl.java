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
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.server.storage.StoredData.ConversationMessageData;
import com.google.appinventor.shared.rpc.aiagent.AIAgentRequest;
import com.google.appinventor.shared.rpc.aiagent.AIAgentResponse;
import com.google.appinventor.shared.rpc.aiagent.AIAgentService;
import com.google.appinventor.shared.rpc.aiagent.AIConversationMessage;
import com.google.appinventor.shared.rpc.aiagent.AIOperation;
import com.google.appinventor.shared.settings.SettingsConstants;

import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Server-side implementation of the AI Agent RPC service.
 *
 * <p>Flow: auth check -> conversation lookup -> context build -> LLM call
 * -> validation -> retry on failure -> response.
 */
public class AIAgentServiceImpl extends OdeRemoteServiceServlet
    implements AIAgentService {

  private static final Logger LOG = Logger.getLogger(AIAgentServiceImpl.class.getName());

  private static final int MAX_MESSAGE_LENGTH = 2000;
  private static final int CONVERSATION_TTL_SECONDS = 24 * 60 * 60; // 24 hours
  private static final int STATUS_TTL_SECONDS = 5 * 60; // 5 minutes
  private static final int MAX_VALIDATION_RETRIES = 1;
  private static final long RATE_WINDOW_MS = 60_000; // 1 minute
  private static final String COMPONENT_DB_RESOURCE =
      "/com/google/appinventor/simple_components.json";

  private static final Flag<Integer> RATE_LIMIT_FLAG =
      Flag.createFlag("ai.agent.rate.limit", 10);

  private final transient StorageIo storageIo = StorageIoInstanceHolder.getInstance();
  private final transient MemcacheService memcache = MemcacheServiceFactory.getMemcacheService();
  private final transient AIContextBuilder contextBuilder;
  private final transient LLMResponseParser responseParser = new LLMResponseParser();
  private final transient AIOperationValidator validator = new AIOperationValidator();

  // Rate limiting: userId -> list of request timestamps
  private static final ConcurrentHashMap<String, List<Long>> rateLimitMap =
      new ConcurrentHashMap<>();

  // Cached component database JSON
  private static volatile String componentDbJson;

  public AIAgentServiceImpl() {
    memcache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
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
      ConversationState conv = getConversation(projectId);
      boolean isNew = (conv == null);
      if (isNew) {
        conv = new ConversationState(getConfiguredProvider(),
            UUID.randomUUID().toString(), null);
      }

      // Check provider change
      String currentProvider = getConfiguredProvider();
      if (!currentProvider.equals(conv.providerName)) {
        clearConversationInternal(projectId);
        conv = new ConversationState(currentProvider,
            UUID.randomUUID().toString(), null);
        isNew = true;
      }

      // Build system prompt and tools
      String systemPrompt = contextBuilder.build(userId, projectId, screenName, mode);
      List<LLMTool> tools = contextBuilder.buildTools(mode);
      AIDebug.log(LOG, "System prompt built: length=" + systemPrompt.length() + " chars");

      // Build history for stateless providers
      LLMProvider provider = LLMProviderRegistry.get(conv.providerName);
      List<ChatMessage> history = Collections.emptyList();
      if (provider.isStateless()) {
        history = loadConversation(conv.conversationId);
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
          + ", provider=" + conv.providerName + ", model=" + getConfiguredModel());

      updateStatus(projectId, "Calling AI...");

      // Call LLM
      LLMResponse llmResponse = provider.chat(
          systemPrompt, userMessage, tools, conv.providerRef, history, resolver);

      // Save user message to history
      storeMessage(conv.conversationId, "user", userMessage);

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

      // Stage 2: Mode + semantic validation
      if (!operations.isEmpty()) {
        updateStatus(projectId, "Validating operations...");

        AIOperationValidator.ValidationResult modeResult =
            validator.validateForMode(operations, mode);
        if (modeResult.hasErrors()) {
          allErrors.addAll(modeResult.getErrors());
          operations = modeResult.getAccepted();
        }
        AIDebug.log(LOG, "Mode validation: accepted=" + operations.size()
            + ", rejected=" + modeResult.getErrors().size());

        if (!operations.isEmpty()) {
          AIOperationValidator.ValidationResult semanticResult =
              validator.validateOperations(operations, getComponentDb());
          if (semanticResult.hasErrors()) {
            allErrors.addAll(semanticResult.getErrors());
            operations = semanticResult.getAccepted();
          }
          if (AIDebug.enabled()) {
            AIDebug.log(LOG, "Semantic validation: accepted=" + semanticResult.getAccepted().size()
                + ", rejected=" + semanticResult.getErrors().size());
            for (String err : semanticResult.getErrors()) {
              AIDebug.log(LOG, "  Semantic error: " + err);
            }
          }
        }
      }

      // Retry once if validation failed and we got errors
      if (!allErrors.isEmpty() && !rawCalls.isEmpty()) {
        String feedback = LLMResponseParser.buildValidationErrorFeedback(allErrors);
        LOG.info("Retrying LLM with validation feedback: " + feedback);
        AIDebug.log(LOG, "Retry feedback sent: " + feedback);
        updateStatus(projectId, "Retrying with corrections...");

        try {
          // Save the assistant message + error as context for retry
          LLMResponse retryResponse = provider.chat(
              systemPrompt, feedback, tools, llmResponse.getProviderRef(), history, resolver);

          AIDebug.log(LOG, "Retry response text: "
              + (retryResponse.getText() != null ? retryResponse.getText() : "(null)"));

          List<LLMResponseParser.RawToolCall> retryCalls = new ArrayList<>();
          for (RawToolCall tc : retryResponse.getRawToolCalls()) {
            retryCalls.add(new LLMResponseParser.RawToolCall(
                tc.getName(), tc.getArgumentsJson()));
          }

          LLMResponseParser.ParseResult retryParse = responseParser.parseToolCalls(retryCalls);
          if (!retryParse.hasErrors()) {
            AIOperationValidator.ValidationResult retryMode =
                validator.validateForMode(retryParse.getOperations(), mode);
            if (!retryMode.hasErrors()) {
              AIOperationValidator.ValidationResult retrySemantic =
                  validator.validateOperations(retryMode.getAccepted(), getComponentDb());
              if (!retrySemantic.hasErrors()) {
                // Retry succeeded
                operations = retrySemantic.getAccepted();
                allErrors.clear();
                llmResponse = retryResponse;
                AIDebug.log(LOG, "Retry succeeded: " + operations.size() + " operations accepted");
              }
            }
          }
        } catch (LLMProviderException retryEx) {
          LOG.log(Level.WARNING, "Retry LLM call failed", retryEx);
        }
      }

      // Convert pseudocode bodies to Blockly XML for block operations
      if (!operations.isEmpty()) {
        Map<String, String> componentTypes =
            buildComponentTypeMap(userId, projectId, screenName);
        operations = convertPseudocodeToXml(operations, componentTypes);
      }

      // Belt-and-suspenders: strip write ops in Advisor mode
      operations = validator.stripWriteOpsIfAdvisor(operations, mode);

      // Update conversation state
      conv = new ConversationState(conv.providerName, conv.conversationId,
          llmResponse.getProviderRef());
      saveConversation(projectId, conv);

      // Save assistant response to history
      String assistantText = llmResponse.getText() != null ? llmResponse.getText() : "";
      if (assistantText.isEmpty() && !operations.isEmpty()) {
        assistantText = summarizeOperations(operations);
      }
      storeMessage(conv.conversationId, "assistant", assistantText);

      clearStatus(projectId);

      AIDebug.log(LOG, "Final response: operations=" + operations.size()
          + ", errors=" + allErrors.size()
          + ", textLen=" + assistantText.length());

      return new AIAgentResponse(assistantText, operations, isNew, allErrors);

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

    ConversationState conv = getConversation(projectId);
    if (conv == null) {
      return Collections.emptyList();
    }

    List<ChatMessage> history = loadConversation(conv.conversationId);
    List<AIConversationMessage> result = new ArrayList<>();
    for (ChatMessage msg : history) {
      result.add(new AIConversationMessage(msg.getRole(), msg.getText()));
    }
    return result;
  }

  @Override
  public String getRequestStatus(long projectId) {
    String key = statusKey(projectId);
    Object status = memcache.get(key);
    return status != null ? status.toString() : "";
  }

  // ---------- Conversation management ----------

  private static class ConversationState implements java.io.Serializable {
    final String providerName;
    final String conversationId;
    final String providerRef;

    ConversationState(String providerName, String conversationId, String providerRef) {
      this.providerName = providerName;
      this.conversationId = conversationId;
      this.providerRef = providerRef;
    }
  }

  private ConversationState getConversation(long projectId) {
    String key = conversationKey(projectId);
    return (ConversationState) memcache.get(key);
  }

  private void saveConversation(long projectId, ConversationState state) {
    String key = conversationKey(projectId);
    memcache.put(key, state, Expiration.byDeltaSeconds(CONVERSATION_TTL_SECONDS));
  }

  private void clearConversationInternal(long projectId) {
    ConversationState conv = getConversation(projectId);
    String key = conversationKey(projectId);
    memcache.delete(key);
    clearStatus(projectId);

    // Delete stored messages from Datastore
    if (conv != null) {
      deleteConversationMessages(conv.conversationId);
    }
  }

  private void deleteConversationMessages(String conversationId) {
    Objectify ofy = ObjectifyService.begin();
    ofy.delete(ofy.query(ConversationMessageData.class)
        .filter("conversationId", conversationId)
        .fetchKeys());
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
    Objectify ofy = ObjectifyService.begin();
    ConversationMessageData msg = new ConversationMessageData();
    msg.conversationId = conversationId;
    msg.timestamp = now;
    msg.sequence = seq;
    msg.role = role;
    msg.text = text;
    msg.expiresAt = now + (CONVERSATION_TTL_SECONDS * 1000L);
    ofy.put(msg);
  }

  private List<ChatMessage> loadConversation(String conversationId) {
    long now = System.currentTimeMillis();
    Objectify ofy = ObjectifyService.begin();
    List<ConversationMessageData> messages =
        ofy.query(ConversationMessageData.class)
            .filter("conversationId", conversationId)
            .order("timestamp")
            .order("sequence")
            .list();

    return messages.stream()
        .filter(m -> m.expiresAt > now)
        .map(m -> new ChatMessage(m.role, m.text))
        .collect(Collectors.toList());
  }

  // ---------- Status updates (Memcache only) ----------

  private void updateStatus(long projectId, String status) {
    memcache.put(statusKey(projectId), status,
        Expiration.byDeltaSeconds(STATUS_TTL_SECONDS));
  }

  private void clearStatus(long projectId) {
    memcache.delete(statusKey(projectId));
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
   * Convert pseudocode bodies in block operations to Blockly XML.
   * Reconstructs full pseudocode statements from the operation fields,
   * parses them through PseudocodeParser, and replaces the body/initial_value
   * field with a blocksXml field containing the generated XML.
   */
  private List<AIOperation> convertPseudocodeToXml(List<AIOperation> operations,
      Map<String, String> componentTypes) {
    PseudocodeParser parser = new PseudocodeParser();
    parser.setComponentInfo(componentTypes, getComponentDb());
    List<AIOperation> converted = new ArrayList<>();

    for (AIOperation op : operations) {
      if (op.getType() == AIOperation.Type.SET_EVENT_HANDLER
          || op.getType() == AIOperation.Type.SET_VARIABLE
          || op.getType() == AIOperation.Type.SET_PROCEDURE) {
        try {
          JSONObject payload = new JSONObject(op.getPayload());
          String pseudocode = reconstructPseudocode(op.getType(), payload);
          AIDebug.log(LOG, "Pseudocode for " + op.getType() + ":\n" + pseudocode);
          String blocksXml = parser.parse(pseudocode);
          if (!parser.getWarnings().isEmpty()) {
            LOG.info("Pseudocode validation warnings for " + op.getType()
                + ": " + parser.getWarnings());
          }
          blocksXml = fillComponentTypes(blocksXml, componentTypes);
          AIDebug.log(LOG, "Blockly XML for " + op.getType() + ":\n" + blocksXml);
          payload.put("blocksXml", blocksXml);
          converted.add(new AIOperation(op.getType(), payload.toString()));
        } catch (Exception e) {
          LOG.log(Level.WARNING, "Failed to convert pseudocode for " + op.getType(), e);
          // Keep the original operation; client-side will report the error
          converted.add(op);
        }
      } else {
        converted.add(op);
      }
    }
    return converted;
  }

  /**
   * Reconstruct a full pseudocode block from an operation's fields.
   */
  private String reconstructPseudocode(AIOperation.Type type, JSONObject payload) {
    switch (type) {
      case SET_EVENT_HANDLER: {
        String component = payload.optString("component_name", "");
        String event = payload.optString("event_name", "");
        String body = payload.optString("body", "");
        StringBuilder sb = new StringBuilder();
        sb.append("when ").append(component).append(".").append(event).append("() do\n");
        for (String line : body.split("\n")) {
          sb.append("  ").append(line).append("\n");
        }
        return sb.toString();
      }
      case SET_VARIABLE: {
        String name = payload.optString("name", "");
        String initialValue = payload.optString("initial_value", "0");
        return "initialize global " + name + " to " + initialValue;
      }
      case SET_PROCEDURE: {
        String name = payload.optString("name", "");
        String body = payload.optString("body", "");
        boolean hasReturn = payload.has("returns") && !payload.isNull("returns");
        StringBuilder sb = new StringBuilder();
        sb.append("procedure ").append(name).append("(");
        // Add parameters if present
        if (payload.has("params")) {
          JSONArray params = payload.optJSONArray("params");
          if (params != null && params.length() > 0) {
            for (int i = 0; i < params.length(); i++) {
              if (i > 0) sb.append(", ");
              sb.append(params.getString(i));
            }
          }
        }
        sb.append(")");
        if (hasReturn) {
          sb.append(" returns\n");
        }
        sb.append("\n");
        for (String line : body.split("\n")) {
          sb.append("  ").append(line).append("\n");
        }
        if (hasReturn) {
          String returnValue = payload.optString("returns", "");
          if (!returnValue.isEmpty()) {
            sb.append("  return ").append(returnValue).append("\n");
          }
        }
        return sb.toString();
      }
      default:
        return "";
    }
  }

  /**
   * Build a map of component instance name to component type from the SCM file.
   */
  private Map<String, String> buildComponentTypeMap(String userId, long projectId,
      String screenName) {
    Map<String, String> map = new HashMap<>();
    try {
      List<String> files = storageIo.getProjectSourceFiles(userId, projectId);
      String scmSuffix = "/" + screenName + ".scm";
      for (String fileId : files) {
        if (fileId.endsWith(scmSuffix)) {
          String scmContent = storageIo.downloadFile(userId, projectId, fileId, "UTF-8");
          String json = extractScmJson(scmContent);
          if (json != null) {
            JSONObject root = new JSONObject(json);
            JSONObject props = root.optJSONObject("Properties");
            if (props != null) {
              collectComponentTypes(props, map);
            }
          }
          break;
        }
      }
    } catch (Exception e) {
      LOG.log(Level.WARNING, "Failed to build component type map", e);
    }
    return map;
  }

  private void collectComponentTypes(JSONObject component, Map<String, String> map) {
    String type = component.optString("$Type", "");
    String name = component.optString("$Name", "");
    if (!type.isEmpty() && !name.isEmpty()) {
      map.put(name, type);
    }
    JSONArray children = component.optJSONArray("$Components");
    if (children != null) {
      for (int i = 0; i < children.length(); i++) {
        collectComponentTypes(children.getJSONObject(i), map);
      }
    }
  }

  /**
   * Fill in empty component_type attributes in generated Blockly XML
   * by looking up instance names in the component type map.
   */
  private static String fillComponentTypes(String xml, Map<String, String> componentTypes) {
    // Find all instance_name="X" occurrences and fill in the preceding component_type=""
    for (Map.Entry<String, String> entry : componentTypes.entrySet()) {
      String name = escapeXmlAttr(entry.getKey());
      String type = escapeXmlAttr(entry.getValue());
      // The generator always emits component_type="" somewhere before instance_name="X"
      // in the same <mutation> element. Use regex to match and replace.
      xml = xml.replaceAll(
          "component_type=\"\"([^/]*instance_name=\"" + java.util.regex.Pattern.quote(name) + "\")",
          "component_type=\"" + type + "\"$1");
    }
    return xml;
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
          case SET_EVENT_HANDLER:
            sb.append("Set event handler for ")
                .append(payload.optString("component_name"))
                .append(".").append(payload.optString("event_name")).append("\n");
            break;
          case DELETE_EVENT_HANDLER:
            sb.append("Deleted event handler for ")
                .append(payload.optString("component_name"))
                .append(".").append(payload.optString("event_name")).append("\n");
            break;
          case SET_VARIABLE:
            sb.append("Set variable '").append(payload.optString("name")).append("'\n");
            break;
          case SET_PROCEDURE:
            sb.append("Set procedure '").append(payload.optString("name")).append("'\n");
            break;
          case CREATE_SCREEN:
            sb.append("Created screen '").append(payload.optString("screen_name")).append("'\n");
            break;
          case DELETE_SCREEN:
            sb.append("Deleted screen '").append(payload.optString("screen_name")).append("'\n");
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

  private static String escapeXmlAttr(String value) {
    return value.replace("&", "&amp;").replace("\"", "&quot;")
        .replace("<", "&lt;").replace(">", "&gt;");
  }

  private static AIAgentResponse errorResponse(String message) {
    return new AIAgentResponse(message, Collections.<AIOperation>emptyList(), false,
        Collections.singletonList(message));
  }

  private static String conversationKey(long projectId) {
    return "ai_conv:" + projectId;
  }

  private static String statusKey(long projectId) {
    return "ai-status:" + projectId;
  }
}
