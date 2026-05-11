// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent.llm;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appinventor.server.aiagent.AIDebug;
import com.google.appinventor.server.aiagent.StreamBuffer;

/**
 * LLM provider implementation for the Google Vertex AI Gemini API.
 *
 * <p>This is a stateful provider: the {@code providerRef} is used to store
 * a serialized interaction context so the API can continue conversations.
 * It uses the {@code generateContent} endpoint with function calling and
 * implements an internal tool-use loop for read-only tools (up to
 * {@value #MAX_TOOL_ITERATIONS} iterations).
 *
 * <p>Authentication is performed via a GCP service account key file using
 * OAuth2 Bearer tokens managed by {@link GcpAuthHelper}.
 */
class VertexProvider implements LLMProvider {

  private static final Logger LOG = Logger.getLogger(VertexProvider.class.getName());

  private static final int MAX_TOOL_ITERATIONS = 5;
  private static final int MAX_RETRIES = 3;
  private static final long INITIAL_BACKOFF_MS = 1000;
  private static final int MAX_OUTPUT_TOKENS = 65536;
  private static final int CONNECT_TIMEOUT_MS = 30000;
  private static final int READ_TIMEOUT_MS = 300000;

  private final String project;
  private final String region;
  private final String model;
  private final String reasoningEffort;
  private final GcpAuthHelper authHelper;

  /**
   * Creates a new Vertex AI provider.
   *
   * @param project            the GCP project ID
   * @param region             the GCP region (e.g. "us-central1")
   * @param serviceAccountPath path to the service account JSON key file
   * @param model              the model name (e.g. "gemini-2.0-flash")
   * @param reasoningEffort    reasoning effort level (e.g. "low", "medium", "high"),
   *                           or empty/null to use the model's default
   * @throws LLMProviderException if the service account file cannot be loaded
   */
  VertexProvider(String project, String region, String serviceAccountPath, String model,
      String reasoningEffort) throws LLMProviderException {
    this.project = project;
    this.region = region;
    this.model = model;
    this.reasoningEffort = reasoningEffort;
    this.authHelper = new GcpAuthHelper(serviceAccountPath);
  }

  @Override
  public boolean isStateless() {
    return false;
  }

  @Override
  public LLMResponse chat(String systemPrompt, List<String> contextMessages,
      String userMessage, List<LLMTool> tools, String providerRef,
      List<ChatMessage> history, ReadOnlyToolResolver resolver,
      StreamBuffer streamBuffer) throws LLMProviderException {

    // Build the contents array. See GeminiProvider.chat for the rationale
    // of the history-replay fallback when providerRef is null/empty.
    boolean replayHistory = (providerRef == null || providerRef.isEmpty())
        && history != null && !history.isEmpty();
    JSONArray contents;
    if (replayHistory) {
      contents = new JSONArray();
      for (ChatMessage m : history) {
        appendHistoryTurnToContents(contents, m);
      }
      JSONArray currentOnly = buildContents(null, contextMessages, userMessage);
      for (int i = 0; i < currentOnly.length(); i++) {
        contents.put(currentOnly.get(i));
      }
    } else {
      contents = buildContents(history, contextMessages, userMessage);
    }
    JSONArray toolDeclarations = buildToolDeclarations(tools);

    // Internal tool-use loop
    for (int iteration = 0; iteration < MAX_TOOL_ITERATIONS; iteration++) {
      JSONObject requestBody = new JSONObject();
      requestBody.put("contents", contents);

      // System instruction
      if (systemPrompt != null && !systemPrompt.isEmpty()) {
        JSONObject systemInstruction = new JSONObject();
        JSONArray systemParts = new JSONArray();
        systemParts.put(new JSONObject().put("text", systemPrompt));
        systemInstruction.put("parts", systemParts);
        requestBody.put("systemInstruction", systemInstruction);
      }

      // Tool declarations
      if (toolDeclarations.length() > 0) {
        JSONArray toolsArray = new JSONArray();
        JSONObject toolsWrapper = new JSONObject();
        toolsWrapper.put("functionDeclarations", toolDeclarations);
        toolsArray.put(toolsWrapper);
        requestBody.put("tools", toolsArray);
      }

      // Generation config
      JSONObject generationConfig = new JSONObject();
      generationConfig.put("maxOutputTokens", MAX_OUTPUT_TOKENS);
      if (reasoningEffort != null && !reasoningEffort.isEmpty()) {
        generationConfig.put("thinkingConfig", new JSONObject()
            .put("thinkingLevel", reasoningEffort.toUpperCase())
            .put("includeThoughts", true));
      }
      requestBody.put("generationConfig", generationConfig);

      // Include provider ref for stateful continuation
      if (iteration == 0 && providerRef != null && !providerRef.isEmpty()) {

        try {
          // The providerRef stores previous conversation turns that the
          // caller has cached, allowing us to resume.
          JSONArray previousContents = new JSONArray(providerRef);
          // Prepend previous contents before current contents
          JSONArray mergedContents = new JSONArray();
          for (int i = 0; i < previousContents.length(); i++) {
            mergedContents.put(previousContents.get(i));
          }
          for (int i = 0; i < contents.length(); i++) {
            mergedContents.put(contents.get(i));
          }
          requestBody.put("contents", mergedContents);
        } catch (JSONException e) {
          LOG.warning("Failed to parse Vertex providerRef, ignoring: " + e.getMessage());
        }
      }

      if (AIDebug.enabled()) {
        AIDebug.log(LOG, "Vertex request (iteration " + iteration + "):\n"
            + requestBody.toString(2));
      }
      JSONObject responseJson = callApi(requestBody, streamBuffer);
      if (AIDebug.enabled()) {
        AIDebug.log(LOG, "Vertex response (iteration " + iteration + "):\n"
            + responseJson.toString(2));
      }

      // Parse candidates
      JSONArray candidates = responseJson.optJSONArray("candidates");
      if (candidates == null || candidates.length() == 0) {
        return new LLMResponse("", new ArrayList<RawToolCall>(), null);
      }

      JSONObject candidate = candidates.getJSONObject(0);

      // Check for truncated or blocked response
      checkFinishReason(candidate);

      JSONObject content = candidate.optJSONObject("content");
      if (content == null) {
        return new LLMResponse("", new ArrayList<RawToolCall>(), null);
      }

      JSONArray parts = content.optJSONArray("parts");
      if (parts == null) {
        return new LLMResponse("", new ArrayList<RawToolCall>(), null);
      }

      // Extract text parts and function call parts
      StringBuilder textBuilder = new StringBuilder();
      List<FunctionCallInfo> functionCalls = new ArrayList<>();

      for (int i = 0; i < parts.length(); i++) {
        JSONObject part = parts.getJSONObject(i);
        if (part.has("text")) {
          textBuilder.append(part.getString("text"));
        } else if (part.has("functionCall")) {
          JSONObject fc = part.getJSONObject("functionCall");
          String name = fc.getString("name");
          JSONObject args = fc.optJSONObject("args");
          functionCalls.add(new FunctionCallInfo(
              name,
              args != null ? args.toString() : "{}"));
        }
      }

      // Build the new providerRef by capturing the current contents
      // plus the model response for future continuation
      String newProviderRef = buildProviderRef(contents, content);

      // If no function calls, return the final response
      if (functionCalls.isEmpty()) {
        return new LLMResponse(textBuilder.toString(), new ArrayList<RawToolCall>(),
            newProviderRef);
      }

      // Classify function calls into read-only and operation tools
      List<FunctionCallInfo> readOnlyCalls = new ArrayList<>();
      List<FunctionCallInfo> operationCalls = new ArrayList<>();

      for (FunctionCallInfo fc : functionCalls) {
        if (resolver != null && resolver.isReadOnly(fc.name)) {
          readOnlyCalls.add(fc);
        } else {
          operationCalls.add(fc);
        }
      }

      // If no read-only tools, return all as operation tool calls
      if (readOnlyCalls.isEmpty()) {
        List<RawToolCall> rawCalls = new ArrayList<>();
        for (FunctionCallInfo fc : operationCalls) {
          rawCalls.add(new RawToolCall(fc.name, fc.argsJson));
        }
        // Add the model response to contents for continuation state
        contents.put(content);
        String contState = buildContinuationState(contents, systemPrompt, operationCalls);
        return new LLMResponse(textBuilder.toString(), rawCalls, contState, true);
      }

      // Add the model response to contents
      contents.put(content);

      // Resolve read-only tools and build function response parts
      JSONArray responseParts = new JSONArray();
      for (FunctionCallInfo fc : readOnlyCalls) {
        String result;
        try {
          result = resolver.resolve(fc.name, fc.argsJson);
        } catch (ReadOnlyToolException e) {
          result = "Error: " + e.getMessage();
        }
        JSONObject responsePart = new JSONObject();
        JSONObject functionResponse = new JSONObject();
        functionResponse.put("name", fc.name);
        functionResponse.put("response", new JSONObject()
            .put("result", result));
        responsePart.put("functionResponse", functionResponse);
        responseParts.put(responsePart);
      }

      // For operation tools in a mixed response, send a placeholder
      for (FunctionCallInfo fc : operationCalls) {
        JSONObject responsePart = new JSONObject();
        JSONObject functionResponse = new JSONObject();
        functionResponse.put("name", fc.name);
        functionResponse.put("response", new JSONObject()
            .put("result", "This operation tool call has been queued for execution. "
                + "Please continue with any remaining read-only lookups you need."));
        responsePart.put("functionResponse", functionResponse);
        responseParts.put(responsePart);
      }

      contents.put(new JSONObject()
          .put("role", "user")
          .put("parts", responseParts));

      if (AIDebug.enabled()) {
        AIDebug.log(LOG, "Vertex tool-use loop iteration " + (iteration + 1)
            + ": readOnly=" + readOnlyCalls.size()
            + ", operations=" + operationCalls.size());
        for (FunctionCallInfo fc : readOnlyCalls) {
          AIDebug.log(LOG, "  read-only: " + fc.name);
        }
        for (FunctionCallInfo fc : operationCalls) {
          AIDebug.log(LOG, "  operation: " + fc.name);
        }
      } else {
        LOG.info("Vertex tool-use loop iteration " + (iteration + 1)
            + ": resolved " + readOnlyCalls.size() + " read-only tools, "
            + operationCalls.size() + " operation tools pending");
      }
    }

    // Safety limit reached
    throw new LLMProviderException(
        "Vertex tool-use loop exceeded " + MAX_TOOL_ITERATIONS + " iterations",
        "The AI took too many steps to process your request. Please try again.");
  }

  @Override
  public LLMResponse continueWithToolResults(String continuationState, List<LLMTool> tools,
      List<String> contextMessages, ReadOnlyToolResolver resolver,
      StreamBuffer streamBuffer) throws LLMProviderException {

    JSONObject state;
    try {
      state = new JSONObject(continuationState);
    } catch (JSONException e) {
      throw new LLMProviderException(
          "Invalid continuation state: " + e.getMessage(),
          "Failed to continue the AI response. Please try again.");
    }

    JSONArray contents = state.getJSONArray("contents");
    String systemPrompt = state.optString("systemPrompt", "");
    JSONArray pendingFunctionCalls = state.getJSONArray("pendingFunctionCalls");

    // Append a user content with functionResponse parts for each pending call.
    // Use per-call results when available (annotated by AIAgentEngine) instead
    // of blanket "Done." so the LLM knows which tool calls were rejected.
    JSONArray toolCallResults = state.optJSONArray("toolCallResults");
    JSONArray responseParts = new JSONArray();
    for (int i = 0; i < pendingFunctionCalls.length(); i++) {
      JSONObject pending = pendingFunctionCalls.getJSONObject(i);
      String resultContent = "Done.";
      if (toolCallResults != null && i < toolCallResults.length()) {
        resultContent = toolCallResults.getJSONObject(i).optString("result", "Done.");
      }
      JSONObject responsePart = new JSONObject();
      JSONObject functionResponse = new JSONObject();
      functionResponse.put("name", pending.getString("name"));
      functionResponse.put("response", new JSONObject().put("result", resultContent));
      responsePart.put("functionResponse", functionResponse);
      responseParts.put(responsePart);
    }
    contents.put(new JSONObject()
        .put("role", "user")
        .put("parts", responseParts));

    // Inject per-request context messages after tool results
    if (contextMessages != null) {
      for (String ctx : contextMessages) {
        if (ctx != null && !ctx.isEmpty()) {
          JSONArray ctxParts = new JSONArray();
          ctxParts.put(new JSONObject().put("text", ctx));
          contents.put(new JSONObject()
              .put("role", "user")
              .put("parts", ctxParts));
          JSONArray ackParts = new JSONArray();
          ackParts.put(new JSONObject().put("text", "Understood."));
          contents.put(new JSONObject()
              .put("role", "model")
              .put("parts", ackParts));
        }
      }
      // Drop the trailing model ack so the final context message
      // (continuation scope) is the last turn — Vertex generateContent
      // returns empty when the last role is "model".
      if (contents.length() > 0
          && "model".equals(contents.getJSONObject(
              contents.length() - 1).optString("role"))) {
        contents.remove(contents.length() - 1);
      }
    }

    JSONArray toolDeclarations = buildToolDeclarations(tools);

    // Run the same tool-use loop as chat()
    for (int iteration = 0; iteration < MAX_TOOL_ITERATIONS; iteration++) {
      JSONObject requestBody = new JSONObject();
      requestBody.put("contents", contents);

      if (systemPrompt != null && !systemPrompt.isEmpty()) {
        JSONObject systemInstruction = new JSONObject();
        JSONArray systemParts = new JSONArray();
        systemParts.put(new JSONObject().put("text", systemPrompt));
        systemInstruction.put("parts", systemParts);
        requestBody.put("systemInstruction", systemInstruction);
      }

      if (toolDeclarations.length() > 0) {
        JSONArray toolsArray = new JSONArray();
        JSONObject toolsWrapper = new JSONObject();
        toolsWrapper.put("functionDeclarations", toolDeclarations);
        toolsArray.put(toolsWrapper);
        requestBody.put("tools", toolsArray);
      }

      JSONObject generationConfig = new JSONObject();
      generationConfig.put("maxOutputTokens", MAX_OUTPUT_TOKENS);
      if (reasoningEffort != null && !reasoningEffort.isEmpty()) {
        generationConfig.put("thinkingConfig", new JSONObject()
            .put("thinkingLevel", reasoningEffort.toUpperCase())
            .put("includeThoughts", true));
      }
      requestBody.put("generationConfig", generationConfig);

      if (AIDebug.enabled()) {
        AIDebug.log(LOG, "Vertex continue request (iteration " + iteration + "):\n"
            + requestBody.toString(2));
      }
      JSONObject responseJson = callApi(requestBody, streamBuffer);
      if (AIDebug.enabled()) {
        AIDebug.log(LOG, "Vertex continue response (iteration " + iteration + "):\n"
            + responseJson.toString(2));
      }

      JSONArray candidates = responseJson.optJSONArray("candidates");
      if (candidates == null || candidates.length() == 0) {
        return new LLMResponse("", new ArrayList<RawToolCall>(), null, false);
      }

      JSONObject candidate = candidates.getJSONObject(0);

      // Check for truncated or blocked response
      checkFinishReason(candidate);

      JSONObject content = candidate.optJSONObject("content");
      if (content == null) {
        return new LLMResponse("", new ArrayList<RawToolCall>(), null, false);
      }

      JSONArray parts = content.optJSONArray("parts");
      if (parts == null) {
        return new LLMResponse("", new ArrayList<RawToolCall>(), null, false);
      }

      StringBuilder textBuilder = new StringBuilder();
      List<FunctionCallInfo> functionCalls = new ArrayList<>();

      for (int i = 0; i < parts.length(); i++) {
        JSONObject part = parts.getJSONObject(i);
        if (part.has("text")) {
          textBuilder.append(part.getString("text"));
        } else if (part.has("functionCall")) {
          JSONObject fc = part.getJSONObject("functionCall");
          String name = fc.getString("name");
          JSONObject args = fc.optJSONObject("args");
          functionCalls.add(new FunctionCallInfo(
              name, args != null ? args.toString() : "{}"));
        }
      }

      // No function calls → final response
      if (functionCalls.isEmpty()) {
        return new LLMResponse(textBuilder.toString(), new ArrayList<RawToolCall>(), null, false);
      }

      // Classify function calls
      List<FunctionCallInfo> readOnlyCalls = new ArrayList<>();
      List<FunctionCallInfo> operationCalls = new ArrayList<>();

      for (FunctionCallInfo fc : functionCalls) {
        if (resolver != null && resolver.isReadOnly(fc.name)) {
          readOnlyCalls.add(fc);
        } else {
          operationCalls.add(fc);
        }
      }

      // No read-only tools → return operations with continuation
      if (readOnlyCalls.isEmpty()) {
        List<RawToolCall> rawCalls = new ArrayList<>();
        for (FunctionCallInfo fc : operationCalls) {
          rawCalls.add(new RawToolCall(fc.name, fc.argsJson));
        }
        contents.put(content);
        String contState = buildContinuationState(contents, systemPrompt, operationCalls);
        return new LLMResponse(textBuilder.toString(), rawCalls, contState, true);
      }

      // Resolve read-only tools and continue the loop
      contents.put(content);
      JSONArray roResponseParts = new JSONArray();
      for (FunctionCallInfo fc : readOnlyCalls) {
        String result;
        try {
          result = resolver.resolve(fc.name, fc.argsJson);
        } catch (ReadOnlyToolException e) {
          result = "Error: " + e.getMessage();
        }
        JSONObject roPart = new JSONObject();
        JSONObject fr = new JSONObject();
        fr.put("name", fc.name);
        fr.put("response", new JSONObject().put("result", result));
        roPart.put("functionResponse", fr);
        roResponseParts.put(roPart);
      }
      for (FunctionCallInfo fc : operationCalls) {
        JSONObject roPart = new JSONObject();
        JSONObject fr = new JSONObject();
        fr.put("name", fc.name);
        fr.put("response", new JSONObject().put("result",
            "This operation tool call has been queued for execution. "
            + "Please continue with any remaining read-only lookups you need."));
        roPart.put("functionResponse", fr);
        roResponseParts.put(roPart);
      }
      contents.put(new JSONObject()
          .put("role", "user")
          .put("parts", roResponseParts));
    }

    throw new LLMProviderException(
        "Vertex continuation tool-use loop exceeded " + MAX_TOOL_ITERATIONS + " iterations",
        "The AI took too many steps to process your request. Please try again.");
  }

  /**
   * Appends a single history turn to a Vertex {@code contents} array,
   * preserving structured content (function calls and results) when the
   * message carries {@code structuredContent} from
   * {@code ConversationManager.buildStructuredContentPair}.
   *
   * <p>Uses the same Gemini wire format as {@link GeminiProvider}. Kept as a
   * separate copy to avoid a premature utility extraction; the mapping is
   * small enough that duplication is clearer than shared indirection.
   *
   * <p>Package-visible for test access.
   */
  static void appendHistoryTurnToContents(JSONArray contents, ChatMessage m) {
    String role = m.getRole() != null ? m.getRole() : "user";
    String structured = m.getStructuredContent();
    if (structured != null && !structured.isEmpty()) {
      try {
        JSONArray parts = new JSONArray(structured);
        JSONArray modelParts = new JSONArray();
        JSONArray functionParts = new JSONArray();
        for (int i = 0; i < parts.length(); i++) {
          JSONObject part = parts.getJSONObject(i);
          String type = part.optString("type", "");
          if ("text".equals(type)) {
            modelParts.put(new JSONObject().put("text", part.optString("text", "")));
          } else if ("tool_use".equals(type)) {
            String name = part.optString("name", "");
            JSONObject fc = new JSONObject().put("name", name);
            Object input = part.opt("input");
            if (input instanceof JSONObject) {
              fc.put("args", input);
            } else if (input != null) {
              try {
                fc.put("args", new JSONObject(input.toString()));
              } catch (JSONException e) {
                fc.put("args", new JSONObject());
              }
            } else {
              fc.put("args", new JSONObject());
            }
            modelParts.put(new JSONObject().put("functionCall", fc));
          } else if ("tool_result".equals(type)) {
            String toolName = part.optString("tool_name", "tool");
            String content = part.optString("content", "");
            JSONObject fr = new JSONObject()
                .put("name", toolName)
                .put("response", new JSONObject().put("content", content));
            functionParts.put(new JSONObject().put("functionResponse", fr));
          }
        }
        if (modelParts.length() > 0) {
          String vertexRole = "assistant".equals(role) || "model".equals(role)
              ? "model" : "user";
          contents.put(new JSONObject()
              .put("role", vertexRole)
              .put("parts", modelParts));
        }
        if (functionParts.length() > 0) {
          contents.put(new JSONObject()
              .put("role", "function")
              .put("parts", functionParts));
        }
        return;
      } catch (JSONException e) {
        LOG.warning("Failed to parse structuredContent during history replay: "
            + e.getMessage());
      }
    }
    if ("system".equals(role)) {
      return;
    }
    String text = m.getText() != null ? m.getText() : "";
    String vertexRole = "assistant".equals(role) ? "model" : "user";
    contents.put(new JSONObject()
        .put("role", vertexRole)
        .put("parts", new JSONArray().put(new JSONObject().put("text", text))));
  }

  /**
   * Serializes the contents array, system prompt, and pending function call
   * names into a JSON continuation state string.
   */
  private String buildContinuationState(JSONArray contents, String systemPrompt,
      List<FunctionCallInfo> pendingCalls) {
    JSONObject state = new JSONObject();
    state.put("continuation", true);
    state.put("contents", contents);
    state.put("systemPrompt", systemPrompt != null ? systemPrompt : "");
    JSONArray pending = new JSONArray();
    for (FunctionCallInfo fc : pendingCalls) {
      pending.put(new JSONObject().put("name", fc.name));
    }
    state.put("pendingFunctionCalls", pending);
    return state.toString();
  }

  /**
   * Builds the contents array from conversation history and the current
   * user message.
   */
  private JSONArray buildContents(List<ChatMessage> history,
      List<String> contextMessages, String userMessage) {
    JSONArray contents = new JSONArray();
    if (history != null) {
      for (ChatMessage msg : history) {
        String role = msg.getRole();
        // Vertex AI uses "user" and "model" roles
        if ("system".equals(role)) {
          continue; // handled via systemInstruction
        }
        String vertexRole = "assistant".equals(role) ? "model" : role;
        JSONArray parts = new JSONArray();
        parts.put(new JSONObject().put("text", msg.getText()));
        contents.put(new JSONObject()
            .put("role", vertexRole)
            .put("parts", parts));
      }
    }
    // Per-request context as separate user/model turns before the user's message
    if (contextMessages != null) {
      for (String ctx : contextMessages) {
        if (ctx != null && !ctx.isEmpty()) {
          JSONArray ctxParts = new JSONArray();
          ctxParts.put(new JSONObject().put("text", ctx));
          contents.put(new JSONObject()
              .put("role", "user")
              .put("parts", ctxParts));
          JSONArray ackParts = new JSONArray();
          ackParts.put(new JSONObject().put("text", "Understood."));
          contents.put(new JSONObject()
              .put("role", "model")
              .put("parts", ackParts));
        }
      }
    }
    JSONArray userParts = new JSONArray();
    userParts.put(new JSONObject().put("text", userMessage));
    contents.put(new JSONObject()
        .put("role", "user")
        .put("parts", userParts));
    return contents;
  }

  /**
   * Checks the candidate's finish reason and throws if the response was
   * truncated or blocked.  Vertex AI returns {@code finishReason: "MAX_TOKENS"}
   * when output is cut off and {@code "SAFETY"} when blocked by filters.
   */
  private void checkFinishReason(JSONObject candidate) throws LLMProviderException {
    String finishReason = candidate.optString("finishReason", "");
    if ("MAX_TOKENS".equals(finishReason)) {
      LOG.warning("Vertex response truncated: finishReason=MAX_TOKENS");
      throw new LLMProviderException(
          "Vertex response truncated: finishReason=MAX_TOKENS",
          "The AI response was too long and got cut off. "
              + "Please try a simpler request or break it into smaller steps.");
    }
    if ("SAFETY".equals(finishReason)) {
      LOG.warning("Vertex response blocked: finishReason=SAFETY");
      throw new LLMProviderException(
          "Vertex response blocked by safety filters: finishReason=SAFETY",
          "The AI response was blocked by safety filters. "
              + "Please rephrase your request.");
    }
  }

  /**
   * Translates generic {@link LLMTool} definitions into Vertex AI function
   * declarations.
   */
  private JSONArray buildToolDeclarations(List<LLMTool> tools) {
    JSONArray declarations = new JSONArray();
    if (tools == null) {
      return declarations;
    }
    for (LLMTool tool : tools) {
      JSONObject decl = new JSONObject();
      decl.put("name", tool.getName());
      decl.put("description", tool.getDescription());
      try {
        decl.put("parameters", new JSONObject(tool.getParameterSchema()));
      } catch (JSONException e) {
        LOG.warning("Invalid parameter schema for tool " + tool.getName()
            + ": " + e.getMessage());
        decl.put("parameters", new JSONObject()
            .put("type", "OBJECT")
            .put("properties", new JSONObject()));
      }
      declarations.put(decl);
    }
    return declarations;
  }

  /**
   * Builds a provider reference by serializing the conversation contents
   * plus the model response for future stateful continuation.
   */
  private String buildProviderRef(JSONArray contents, JSONObject modelContent) {
    try {
      JSONArray allContents = new JSONArray();
      for (int i = 0; i < contents.length(); i++) {
        allContents.put(contents.get(i));
      }
      allContents.put(modelContent);
      return allContents.toString();
    } catch (JSONException e) {
      LOG.warning("Failed to build Vertex providerRef: " + e.getMessage());
      return null;
    }
  }

  /**
   * Makes an HTTP POST to the Vertex AI API with automatic retry and exponential
   * backoff for transient errors (429 rate-limit, 5xx).
   *
   * <p>When {@code streamBuffer} is non-null the streaming endpoint
   * ({@code streamGenerateContent?alt=sse}) is used and partial text tokens
   * are pushed to the buffer as they arrive.  The returned JSON object is a
   * merged representation of all SSE events that the existing
   * {@code parseResponse()} / candidate-parsing code can consume unchanged.
   *
   * @param requestBody  the JSON request payload
   * @param streamBuffer if non-null, enables streaming mode
   * @return the (possibly merged) JSON response
   */
  private JSONObject callApi(JSONObject requestBody, StreamBuffer streamBuffer)
      throws LLMProviderException {
    long backoffMs = INITIAL_BACKOFF_MS;
    LLMProviderException lastException = null;
    boolean streaming = streamBuffer != null;

    for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
      if (attempt > 0) {
        LOG.info("Vertex API retry attempt " + attempt + " after " + backoffMs + "ms backoff");
        try {
          Thread.sleep(backoffMs);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new LLMProviderException(
              "Interrupted during retry backoff",
              "The request was interrupted. Please try again.");
        }
        backoffMs *= 2;
      }

      HttpURLConnection conn = null;
      try {
        String endpoint;
        if (streaming) {
          endpoint = "https://" + region + "-aiplatform.googleapis.com/v1/projects/" + project
              + "/locations/" + region + "/publishers/google/models/" + model
              + ":streamGenerateContent?alt=sse";
        } else {
          endpoint = "https://" + region + "-aiplatform.googleapis.com/v1/projects/" + project
              + "/locations/" + region + "/publishers/google/models/" + model
              + ":generateContent";
        }
        URL url = new URL(endpoint);
        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
        conn.setReadTimeout(READ_TIMEOUT_MS);

        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + authHelper.getAccessToken());

        byte[] body = requestBody.toString().getBytes(StandardCharsets.UTF_8);
        conn.setRequestProperty("Content-Length", String.valueOf(body.length));

        try (OutputStream os = conn.getOutputStream()) {
          os.write(body);
          os.flush();
        }

        int statusCode = conn.getResponseCode();

        if (statusCode >= 200 && statusCode < 300) {
          if (streaming) {
            return readStreamingResponse(conn, streamBuffer);
          }
          String responseText = readResponse(conn, statusCode);
          return new JSONObject(responseText);
        }

        String responseText = readResponse(conn, statusCode);
        LOG.warning("Vertex API error (HTTP " + statusCode + "): " + responseText);

        // Retry on 429 (rate limit) and 5xx (server errors)
        if (isRetryable(statusCode) && attempt < MAX_RETRIES) {
          String retryAfter = conn.getHeaderField("Retry-After");
          if (retryAfter != null) {
            try {
              long retryMs = Long.parseLong(retryAfter) * 1000;
              backoffMs = Math.max(backoffMs, retryMs);
            } catch (NumberFormatException ignored) {
              // Use default backoff
            }
          }
          lastException = new LLMProviderException(
              "Vertex API returned HTTP " + statusCode + ": " + responseText,
              mapHttpErrorToUserMessage(statusCode));
          continue;
        }

        String userMsg = mapHttpErrorToUserMessage(statusCode);
        throw new LLMProviderException(
            "Vertex API returned HTTP " + statusCode + ": " + responseText,
            userMsg);

      } catch (IOException e) {
        if (attempt < MAX_RETRIES) {
          LOG.log(Level.WARNING,
              "Vertex API connection error (attempt " + (attempt + 1) + ")", e);
          lastException = new LLMProviderException(
              "Failed to connect to Vertex API: " + e.getMessage(),
              "Could not reach the AI service. Please try again later.",
              e);
          continue;
        }
        LOG.log(Level.WARNING, "Vertex API connection error (final attempt)", e);
        throw new LLMProviderException(
            "Failed to connect to Vertex API: " + e.getMessage(),
            "Could not reach the AI service. Please try again later.",
            e);
      } catch (JSONException e) {
        LOG.log(Level.WARNING, "Failed to parse Vertex API response", e);
        throw new LLMProviderException(
            "Invalid JSON response from Vertex API: " + e.getMessage(),
            "Received an unexpected response from the AI service.",
            e);
      } finally {
        if (conn != null) {
          conn.disconnect();
        }
      }
    }

    if (lastException != null) {
      throw lastException;
    }
    throw new LLMProviderException(
        "Vertex API failed after " + MAX_RETRIES + " retries",
        "The AI service is currently unavailable. Please try again later.");
  }

  /**
   * Reads a streaming SSE response from the Vertex AI
   * {@code streamGenerateContent?alt=sse} endpoint.
   *
   * <p>Each SSE event is a {@code data:} line whose payload is a partial
   * {@code GenerateContentResponse}.  Text parts are streamed to the
   * {@link StreamBuffer} as they arrive; function-call parts are accumulated
   * silently.  After all events have been consumed the buffer is marked done
   * and a single merged {@code GenerateContentResponse} JSON object is
   * returned so that the existing candidate-parsing code works unchanged.
   *
   * <p>Vertex AI may also return the response as a JSON array
   * ({@code [{...},{...}]}) instead of line-by-line SSE.  This method
   * detects and handles both formats.
   */
  private JSONObject readStreamingResponse(HttpURLConnection conn,
      StreamBuffer streamBuffer) throws IOException {

    BufferedReader reader = new BufferedReader(
        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));

    // Accumulate all text parts and all non-text parts across events.
    StringBuilder fullText = new StringBuilder();
    JSONArray nonTextParts = new JSONArray();
    JSONObject lastUsageMetadata = null;
    String lastFinishReason = null;

    // We also buffer the entire raw response so we can fall back to
    // JSON-array parsing if no SSE data lines are found.
    StringBuilder rawBuffer = new StringBuilder();
    boolean sawDataLine = false;

    try {
      String line;
      while ((line = reader.readLine()) != null) {
        if (streamBuffer.isCancelled()) {
          throw new StreamBuffer.CancelledException();
        }
        rawBuffer.append(line).append('\n');

        // SSE format: lines starting with "data: " carry the JSON payload.
        // Blank lines and lines starting with ":" (comments) are ignored.
        if (!line.startsWith("data:")) {
          continue;
        }
        sawDataLine = true;

        String jsonPayload = line.substring("data:".length()).trim();
        if (jsonPayload.isEmpty() || "[DONE]".equals(jsonPayload)) {
          continue;
        }

        try {
          JSONObject event = new JSONObject(jsonPayload);
          processStreamEvent(event, fullText, nonTextParts, streamBuffer);

          // Capture usage metadata and finish reason from the last event
          JSONObject um = event.optJSONObject("usageMetadata");
          if (um != null) {
            lastUsageMetadata = um;
          }
          JSONArray candidates = event.optJSONArray("candidates");
          if (candidates != null && candidates.length() > 0) {
            String fr = candidates.getJSONObject(0).optString("finishReason", null);
            if (fr != null) {
              lastFinishReason = fr;
            }
          }
        } catch (JSONException e) {
          LOG.warning("Skipping malformed SSE data: " + e.getMessage());
        }
      }
    } finally {
      reader.close();
    }

    // Fall back to JSON array format if no SSE data lines were found.
    if (!sawDataLine) {
      String raw = rawBuffer.toString().trim();
      if (raw.startsWith("[")) {
        try {
          JSONArray arr = new JSONArray(raw);
          for (int i = 0; i < arr.length(); i++) {
            JSONObject event = arr.getJSONObject(i);
            processStreamEvent(event, fullText, nonTextParts, streamBuffer);

            JSONObject um = event.optJSONObject("usageMetadata");
            if (um != null) {
              lastUsageMetadata = um;
            }
            JSONArray candidates = event.optJSONArray("candidates");
            if (candidates != null && candidates.length() > 0) {
              String fr = candidates.getJSONObject(0).optString("finishReason", null);
              if (fr != null) {
                lastFinishReason = fr;
              }
            }
          }
        } catch (JSONException e) {
          LOG.warning("Streaming response was neither SSE nor JSON array: "
              + e.getMessage());
          // Try parsing as a single JSON object (non-streaming fallback)
          streamBuffer.markDone();
          return new JSONObject(raw);
        }
      } else {
        // Single JSON object (non-streaming fallback)
        streamBuffer.markDone();
        return new JSONObject(raw);
      }
    }

    streamBuffer.markDone();

    // Build a merged GenerateContentResponse that looks like a normal
    // (non-streaming) response so the caller's candidate-parsing works.
    JSONArray mergedParts = new JSONArray();
    if (fullText.length() > 0) {
      mergedParts.put(new JSONObject().put("text", fullText.toString()));
    }
    for (int i = 0; i < nonTextParts.length(); i++) {
      mergedParts.put(nonTextParts.get(i));
    }

    JSONObject mergedContent = new JSONObject();
    mergedContent.put("role", "model");
    mergedContent.put("parts", mergedParts);

    JSONObject mergedCandidate = new JSONObject();
    mergedCandidate.put("content", mergedContent);
    if (lastFinishReason != null) {
      mergedCandidate.put("finishReason", lastFinishReason);
    }

    JSONObject mergedResponse = new JSONObject();
    mergedResponse.put("candidates", new JSONArray().put(mergedCandidate));
    if (lastUsageMetadata != null) {
      mergedResponse.put("usageMetadata", lastUsageMetadata);
    }

    return mergedResponse;
  }

  /**
   * Processes a single streaming event JSON object.  Text parts are sent to
   * the stream buffer and appended to {@code fullText}.  Non-text parts
   * (e.g. function calls) are accumulated into {@code nonTextParts}.
   */
  private void processStreamEvent(JSONObject event, StringBuilder fullText,
      JSONArray nonTextParts, StreamBuffer streamBuffer) {

    JSONArray candidates = event.optJSONArray("candidates");
    if (candidates == null || candidates.length() == 0) {
      return;
    }

    JSONObject candidate = candidates.getJSONObject(0);
    JSONObject content = candidate.optJSONObject("content");
    if (content == null) {
      return;
    }

    JSONArray parts = content.optJSONArray("parts");
    if (parts == null) {
      return;
    }

    for (int i = 0; i < parts.length(); i++) {
      JSONObject part = parts.getJSONObject(i);
      if (part.has("text")) {
        String text = part.getString("text");
        if (part.optBoolean("thought", false)) {
          streamBuffer.appendThinking(text);
        } else {
          fullText.append(text);
          streamBuffer.appendText(text);
        }
      } else {
        // Accumulate non-text parts (functionCall, etc.) without streaming
        nonTextParts.put(part);
      }
    }
  }

  /**
   * Returns whether an HTTP status code is eligible for automatic retry.
   */
  private static boolean isRetryable(int statusCode) {
    return statusCode == 429 || statusCode >= 500;
  }

  /**
   * Reads the response body from an HTTP connection.
   */
  private String readResponse(HttpURLConnection conn, int statusCode) throws IOException {
    BufferedReader reader;
    if (statusCode >= 200 && statusCode < 300) {
      reader = new BufferedReader(
          new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
    } else {
      reader = new BufferedReader(
          new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
    }
    StringBuilder sb = new StringBuilder();
    String line;
    while ((line = reader.readLine()) != null) {
      sb.append(line);
    }
    reader.close();
    return sb.toString();
  }

  /**
   * Maps HTTP status codes to user-friendly error messages.
   */
  private String mapHttpErrorToUserMessage(int statusCode) {
    switch (statusCode) {
      case 401:
      case 403:
        return "The AI service rejected the credentials. Please contact your administrator.";
      case 429:
        return "The AI service is currently rate-limited. Please wait a moment and try again.";
      case 500:
      case 502:
      case 503:
        return "The AI service is temporarily unavailable. Please try again later.";
      default:
        return "The AI service returned an error. Please try again.";
    }
  }

  /**
   * Internal representation of a Vertex AI function call.
   */
  private static class FunctionCallInfo {
    final String name;
    final String argsJson;

    FunctionCallInfo(String name, String argsJson) {
      this.name = name;
      this.argsJson = argsJson;
    }
  }
}
