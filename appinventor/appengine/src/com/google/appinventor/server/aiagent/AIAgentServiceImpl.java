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
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
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

    try {
      updateStatus(projectId, "Building context...");

      // Get or create conversation
      ConversationState conv = getConversation(userId, projectId);
      boolean isNew = (conv == null);
      if (isNew) {
        conv = new ConversationState(getConfiguredProvider(), null);
      }

      // Check provider change
      String currentProvider = getConfiguredProvider();
      if (!currentProvider.equals(conv.providerName)) {
        clearConversationInternal(userId, projectId);
        conv = new ConversationState(currentProvider, null);
        isNew = true;
      }

      // Build system prompt and tools
      String systemPrompt = contextBuilder.build(userId, projectId, screenName, mode);
      List<LLMTool> tools = contextBuilder.buildTools(mode);

      // Build history for stateless providers
      LLMProvider provider = LLMProviderRegistry.get(conv.providerName);
      List<ChatMessage> history = Collections.emptyList();
      if (provider.isStateless()) {
        history = loadHistory(userId, projectId);
      }

      // Build read-only tool resolver
      ReadOnlyToolResolver resolver = createResolver(userId, projectId);

      updateStatus(projectId, "Calling AI...");

      // Call LLM
      LLMResponse llmResponse = provider.chat(
          systemPrompt, userMessage, tools, conv.providerRef, history, resolver);

      // Save user message to history
      saveMessage(userId, projectId, "user", userMessage);

      // Parse tool calls
      List<LLMResponseParser.RawToolCall> rawCalls = new ArrayList<>();
      for (RawToolCall tc : llmResponse.getRawToolCalls()) {
        rawCalls.add(new LLMResponseParser.RawToolCall(tc.getName(), tc.getArgumentsJson()));
      }

      LLMResponseParser.ParseResult parseResult = responseParser.parseToolCalls(rawCalls);
      List<String> allErrors = new ArrayList<>(parseResult.getErrors());
      List<AIOperation> operations = parseResult.getOperations();

      // Stage 2: Mode + semantic validation
      if (!operations.isEmpty()) {
        updateStatus(projectId, "Validating operations...");

        AIOperationValidator.ValidationResult modeResult =
            validator.validateForMode(operations, mode);
        if (modeResult.hasErrors()) {
          allErrors.addAll(modeResult.getErrors());
          operations = modeResult.getAccepted();
        }

        if (!operations.isEmpty()) {
          AIOperationValidator.ValidationResult semanticResult =
              validator.validateOperations(operations, getComponentDb());
          if (semanticResult.hasErrors()) {
            allErrors.addAll(semanticResult.getErrors());
            operations = semanticResult.getAccepted();
          }
        }
      }

      // Retry once if validation failed and we got errors
      if (!allErrors.isEmpty() && !rawCalls.isEmpty()) {
        String feedback = LLMResponseParser.buildValidationErrorFeedback(allErrors);
        LOG.info("Retrying LLM with validation feedback: " + feedback);
        updateStatus(projectId, "Retrying with corrections...");

        try {
          // Save the assistant message + error as context for retry
          LLMResponse retryResponse = provider.chat(
              systemPrompt, feedback, tools, llmResponse.getProviderRef(), history, resolver);

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
              }
            }
          }
        } catch (LLMProviderException retryEx) {
          LOG.log(Level.WARNING, "Retry LLM call failed", retryEx);
        }
      }

      // Belt-and-suspenders: strip write ops in Advisor mode
      operations = validator.stripWriteOpsIfAdvisor(operations, mode);

      // Update conversation state
      conv = new ConversationState(conv.providerName, llmResponse.getProviderRef());
      saveConversation(userId, projectId, conv);

      // Save assistant response to history
      String assistantText = llmResponse.getText() != null ? llmResponse.getText() : "";
      saveMessage(userId, projectId, "assistant", assistantText);

      clearStatus(projectId);

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
    clearConversationInternal(userId, projectId);
  }

  @Override
  public List<AIConversationMessage> getConversationHistory(long projectId) {
    String userId = userInfoProvider.getUserId();
    try {
      storageIo.assertUserHasProject(userId, projectId);
    } catch (SecurityException e) {
      return Collections.emptyList();
    }

    List<ChatMessage> history = loadHistory(userId, projectId);
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
    final String providerRef;

    ConversationState(String providerName, String providerRef) {
      this.providerName = providerName;
      this.providerRef = providerRef;
    }
  }

  private ConversationState getConversation(String userId, long projectId) {
    String key = conversationKey(userId, projectId);
    return (ConversationState) memcache.get(key);
  }

  private void saveConversation(String userId, long projectId, ConversationState state) {
    String key = conversationKey(userId, projectId);
    memcache.put(key, state, Expiration.byDeltaSeconds(CONVERSATION_TTL_SECONDS));
  }

  private void clearConversationInternal(String userId, long projectId) {
    String key = conversationKey(userId, projectId);
    memcache.delete(key);
    clearStatus(projectId);

    // Delete stored messages from Datastore
    Objectify ofy = ObjectifyService.begin();
    List<ConversationMessageData> messages =
        ofy.query(ConversationMessageData.class)
            .filter("userId", userId)
            .filter("projectId", projectId)
            .list();
    for (ConversationMessageData msg : messages) {
      ofy.delete(msg);
    }
  }

  private void saveMessage(String userId, long projectId, String role, String text) {
    Objectify ofy = ObjectifyService.begin();
    ConversationMessageData msg =
        new ConversationMessageData();
    msg.userId = userId;
    msg.projectId = projectId;
    msg.role = role;
    msg.text = text;
    msg.timestamp = System.currentTimeMillis();
    ofy.put(msg);
  }

  private List<ChatMessage> loadHistory(String userId, long projectId) {
    Objectify ofy = ObjectifyService.begin();
    List<ConversationMessageData> messages =
        ofy.query(ConversationMessageData.class)
            .filter("userId", userId)
            .filter("projectId", projectId)
            .order("timestamp")
            .list();

    List<ChatMessage> history = new ArrayList<>();
    for (ConversationMessageData msg : messages) {
      history.add(new ChatMessage(msg.role, msg.text));
    }
    return history;
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

  private static AIAgentResponse errorResponse(String message) {
    return new AIAgentResponse(message, Collections.<AIOperation>emptyList(), false,
        Collections.singletonList(message));
  }

  private static String conversationKey(String userId, long projectId) {
    return "aiconv|" + userId + "|" + projectId;
  }

  private static String statusKey(long projectId) {
    return "aistatus|" + projectId;
  }
}
