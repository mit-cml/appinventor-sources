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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appinventor.server.aiagent.AIDebug;
import com.google.appinventor.server.aiagent.StreamBuffer;

/**
 * LLM provider implementation for AWS Bedrock using the Converse Streaming API
 * with SigV4 authentication.
 *
 * <p>This is a stateless provider: the full conversation history must be passed
 * on each call. It uses the Bedrock {@code converse-stream} endpoint and
 * implements an internal tool-use loop for read-only tools (up to
 * {@value #MAX_TOOL_ITERATIONS} iterations).
 */
class BedrockProvider implements LLMProvider {

  private static final Logger LOG = Logger.getLogger(BedrockProvider.class.getName());

  private static final int MAX_TOOL_ITERATIONS = 5;
  private static final int MAX_RETRIES = 3;
  private static final long INITIAL_BACKOFF_MS = 1000;
  private static final int MAX_TOKENS = 128000;
  private static final int CONNECT_TIMEOUT_MS = 30000;
  private static final int READ_TIMEOUT_MS = 600000;

  private final AwsSigV4Signer signer;
  private final String region;
  private final String model;
  private final String reasoningEffort;

  /**
   * Creates a new Bedrock provider.
   *
   * @param accessKey       AWS access key ID
   * @param secretKey       AWS secret access key
   * @param sessionToken    AWS session token (may be null or empty)
   * @param region          AWS region (e.g. "us-east-1")
   * @param model           Bedrock model ID (e.g. "anthropic.claude-sonnet-4-20250514-v1:0")
   * @param reasoningEffort reasoning effort level (e.g. "low", "medium", "high"),
   *                        or empty/null to use the model's default
   */
  BedrockProvider(String accessKey, String secretKey, String sessionToken,
      String region, String model, String reasoningEffort) {
    this.signer = new AwsSigV4Signer(accessKey, secretKey, sessionToken, region, "bedrock");
    this.region = region;
    this.model = model;
    this.reasoningEffort = reasoningEffort;
  }

  @Override
  public boolean isStateless() {
    return true;
  }

  @Override
  public LLMResponse chat(String systemPrompt, List<String> contextMessages,
      String userMessage, List<LLMTool> tools, String providerRef,
      List<ChatMessage> history, ReadOnlyToolResolver resolver,
      StreamBuffer streamBuffer) throws LLMProviderException {

    JSONArray systemArray = buildSystemArray(systemPrompt);
    JSONArray messages = buildMessages(history, contextMessages, userMessage);
    JSONArray toolDefs = buildToolDefinitions(tools);

    for (int iteration = 0; iteration < MAX_TOOL_ITERATIONS; iteration++) {
      JSONObject requestBody = buildRequestBody(systemArray, messages, toolDefs);

      if (AIDebug.enabled()) {
        AIDebug.log(LOG, "Bedrock request (iteration " + iteration + "):\n"
            + requestBody.toString(2));
      }

      JSONObject responseJson = callApi(requestBody, streamBuffer);

      if (AIDebug.enabled()) {
        AIDebug.log(LOG, "Bedrock response (iteration " + iteration + "):\n"
            + responseJson.toString(2));
        AIDebug.recordUsage("Bedrock", model,
            TokenUsage.fromBedrock(responseJson.optJSONObject("usage")));
      }

      String stopReason = responseJson.optString("stopReason", "end_turn");
      checkStopReason(stopReason);

      // Extract content blocks from output.message.content
      JSONArray content = extractContent(responseJson);

      StringBuilder textBuilder = new StringBuilder();
      List<ToolUseInfo> toolUseBlocks = new ArrayList<>();

      for (int i = 0; i < content.length(); i++) {
        JSONObject block = content.getJSONObject(i);
        if (block.has("text")) {
          textBuilder.append(block.getString("text"));
        } else if (block.has("toolUse")) {
          JSONObject toolUse = block.getJSONObject("toolUse");
          String inputJson = toolUse.optJSONObject("input") != null
              ? toolUse.getJSONObject("input").toString()
              : "{}";
          toolUseBlocks.add(new ToolUseInfo(
              toolUse.getString("toolUseId"),
              toolUse.getString("name"),
              inputJson));
        }
      }

      // If no tool calls, return the final response
      if (toolUseBlocks.isEmpty() || !"tool_use".equals(stopReason)) {
        return new LLMResponse(textBuilder.toString(), new ArrayList<RawToolCall>(), null);
      }

      // Classify tool calls
      List<ToolUseInfo> readOnlyBlocks = new ArrayList<>();
      List<ToolUseInfo> operationBlocks = new ArrayList<>();
      for (ToolUseInfo block : toolUseBlocks) {
        if (resolver != null && resolver.isReadOnly(block.name)) {
          readOnlyBlocks.add(block);
        } else {
          operationBlocks.add(block);
        }
      }

      // No read-only tools → return all as operations with continuation state
      if (readOnlyBlocks.isEmpty()) {
        List<RawToolCall> rawCalls = new ArrayList<>();
        for (ToolUseInfo block : operationBlocks) {
          rawCalls.add(new RawToolCall(block.name, block.inputJson));
        }
        messages.put(new JSONObject()
            .put("role", "assistant")
            .put("content", content));
        String contState = buildContinuationState(messages, systemArray, operationBlocks);
        return new LLMResponse(textBuilder.toString(), rawCalls, contState, true);
      }

      // Resolve read-only tools and continue the loop
      messages.put(new JSONObject()
          .put("role", "assistant")
          .put("content", content));

      JSONArray toolResultBlocks = new JSONArray();
      for (ToolUseInfo block : readOnlyBlocks) {
        String result;
        try {
          result = resolver.resolve(block.name, block.inputJson);
        } catch (ReadOnlyToolException e) {
          result = "Error: " + e.getMessage();
        }
        toolResultBlocks.put(new JSONObject()
            .put("toolResult", new JSONObject()
                .put("toolUseId", block.id)
                .put("content", new JSONArray().put(new JSONObject().put("text", result)))));
      }
      for (ToolUseInfo block : operationBlocks) {
        toolResultBlocks.put(new JSONObject()
            .put("toolResult", new JSONObject()
                .put("toolUseId", block.id)
                .put("content", new JSONArray().put(new JSONObject().put("text",
                    "This operation tool call has been queued for execution. "
                        + "Please continue with any remaining read-only lookups you need.")))));
      }
      messages.put(new JSONObject()
          .put("role", "user")
          .put("content", toolResultBlocks));

      if (AIDebug.enabled()) {
        AIDebug.log(LOG, "Bedrock tool-use loop iteration " + (iteration + 1)
            + ": readOnly=" + readOnlyBlocks.size()
            + ", operations=" + operationBlocks.size());
      } else {
        LOG.info("Bedrock tool-use loop iteration " + (iteration + 1)
            + ": resolved " + readOnlyBlocks.size() + " read-only tools, "
            + operationBlocks.size() + " operation tools pending");
      }
    }

    throw new LLMProviderException(
        "Bedrock tool-use loop exceeded " + MAX_TOOL_ITERATIONS + " iterations",
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

    JSONArray messages = state.getJSONArray("messages");
    JSONArray systemArray = state.optJSONArray("system");
    if (systemArray == null) {
      systemArray = new JSONArray();
    }
    JSONArray pendingToolCalls = state.getJSONArray("pendingToolCalls");

    // Build tool result blocks for each pending tool call
    JSONArray toolCallResults = state.optJSONArray("toolCallResults");
    JSONArray toolResultBlocks = new JSONArray();
    for (int i = 0; i < pendingToolCalls.length(); i++) {
      JSONObject pending = pendingToolCalls.getJSONObject(i);
      String resultContent = "Done.";
      if (toolCallResults != null && i < toolCallResults.length()) {
        resultContent = toolCallResults.getJSONObject(i).optString("result", "Done.");
      }
      toolResultBlocks.put(new JSONObject()
          .put("toolResult", new JSONObject()
              .put("toolUseId", pending.getString("id"))
              .put("content", new JSONArray().put(
                  new JSONObject().put("text", resultContent)))));
    }
    messages.put(new JSONObject()
        .put("role", "user")
        .put("content", toolResultBlocks));

    // Inject per-request context messages after tool results
    if (contextMessages != null) {
      for (String ctx : contextMessages) {
        if (ctx != null && !ctx.isEmpty()) {
          messages.put(new JSONObject()
              .put("role", "user")
              .put("content", new JSONArray().put(
                  new JSONObject().put("text", ctx))));
          messages.put(new JSONObject()
              .put("role", "assistant")
              .put("content", new JSONArray().put(
                  new JSONObject().put("text", "Understood."))));
        }
      }
      // Bedrock Converse routes to Anthropic Messages which prefills
      // from a trailing assistant turn. Drop the trailing ack so the
      // continuation-scope instruction is the last turn.
      if (messages.length() > 0
          && "assistant".equals(messages.getJSONObject(
              messages.length() - 1).optString("role"))) {
        messages.remove(messages.length() - 1);
      }
    }

    JSONArray toolDefs = buildToolDefinitions(tools);

    for (int iteration = 0; iteration < MAX_TOOL_ITERATIONS; iteration++) {
      JSONObject requestBody = buildRequestBody(systemArray, messages, toolDefs);

      if (AIDebug.enabled()) {
        AIDebug.log(LOG, "Bedrock continue request (iteration " + iteration + "):\n"
            + requestBody.toString(2));
      }

      JSONObject responseJson = callApi(requestBody, streamBuffer);

      if (AIDebug.enabled()) {
        AIDebug.log(LOG, "Bedrock continue response (iteration " + iteration + "):\n"
            + responseJson.toString(2));
        AIDebug.recordUsage("Bedrock", model,
            TokenUsage.fromBedrock(responseJson.optJSONObject("usage")));
      }

      String stopReason = responseJson.optString("stopReason", "end_turn");
      checkStopReason(stopReason);

      JSONArray content = extractContent(responseJson);

      StringBuilder textBuilder = new StringBuilder();
      List<ToolUseInfo> toolUseBlocks = new ArrayList<>();

      for (int i = 0; i < content.length(); i++) {
        JSONObject block = content.getJSONObject(i);
        if (block.has("text")) {
          textBuilder.append(block.getString("text"));
        } else if (block.has("toolUse")) {
          JSONObject toolUse = block.getJSONObject("toolUse");
          String inputJson = toolUse.optJSONObject("input") != null
              ? toolUse.getJSONObject("input").toString()
              : "{}";
          toolUseBlocks.add(new ToolUseInfo(
              toolUse.getString("toolUseId"),
              toolUse.getString("name"),
              inputJson));
        }
      }

      // No tool calls → final response
      if (toolUseBlocks.isEmpty() || !"tool_use".equals(stopReason)) {
        return new LLMResponse(textBuilder.toString(), new ArrayList<RawToolCall>(), null, false);
      }

      // Classify tool calls
      List<ToolUseInfo> readOnlyBlocks = new ArrayList<>();
      List<ToolUseInfo> operationBlocks = new ArrayList<>();
      for (ToolUseInfo block : toolUseBlocks) {
        if (resolver != null && resolver.isReadOnly(block.name)) {
          readOnlyBlocks.add(block);
        } else {
          operationBlocks.add(block);
        }
      }

      // No read-only tools → return operations with continuation
      if (readOnlyBlocks.isEmpty()) {
        List<RawToolCall> rawCalls = new ArrayList<>();
        for (ToolUseInfo block : operationBlocks) {
          rawCalls.add(new RawToolCall(block.name, block.inputJson));
        }
        messages.put(new JSONObject()
            .put("role", "assistant")
            .put("content", content));
        String contState = buildContinuationState(messages, systemArray, operationBlocks);
        return new LLMResponse(textBuilder.toString(), rawCalls, contState, true);
      }

      // Resolve read-only tools and continue the loop
      messages.put(new JSONObject()
          .put("role", "assistant")
          .put("content", content));

      JSONArray readOnlyResults = new JSONArray();
      for (ToolUseInfo block : readOnlyBlocks) {
        String result;
        try {
          result = resolver.resolve(block.name, block.inputJson);
        } catch (ReadOnlyToolException e) {
          result = "Error: " + e.getMessage();
        }
        readOnlyResults.put(new JSONObject()
            .put("toolResult", new JSONObject()
                .put("toolUseId", block.id)
                .put("content", new JSONArray().put(new JSONObject().put("text", result)))));
      }
      for (ToolUseInfo block : operationBlocks) {
        readOnlyResults.put(new JSONObject()
            .put("toolResult", new JSONObject()
                .put("toolUseId", block.id)
                .put("content", new JSONArray().put(new JSONObject().put("text",
                    "This operation tool call has been queued for execution. "
                        + "Please continue with any remaining read-only lookups you need.")))));
      }
      messages.put(new JSONObject()
          .put("role", "user")
          .put("content", readOnlyResults));
    }

    throw new LLMProviderException(
        "Bedrock continuation tool-use loop exceeded " + MAX_TOOL_ITERATIONS + " iterations",
        "The AI took too many steps to process your request. Please try again.");
  }

  // ---------------------------------------------------------------------------
  // Private helpers
  // ---------------------------------------------------------------------------

  /**
   * Builds a Bedrock system array from a system prompt string.
   */
  private static JSONArray buildSystemArray(String systemPrompt) {
    JSONArray system = new JSONArray();
    if (systemPrompt != null && !systemPrompt.isEmpty()) {
      system.put(new JSONObject().put("text", systemPrompt));
    }
    return system;
  }

  /**
   * Builds the messages array from conversation history and the current user message.
   * Translates Anthropic-format structured content into Bedrock format.
   */
  private static JSONArray buildMessages(List<ChatMessage> history,
      List<String> contextMessages, String userMessage) {
    JSONArray messages = new JSONArray();

    if (history != null) {
      for (ChatMessage msg : history) {
        String role = msg.getRole();
        if ("system".equals(role)) {
          continue; // handled separately via system array
        }

        if (msg.hasStructuredContent()) {
          if ("assistant".equals(role)) {
            messages.put(buildBedrockAssistantMessage(msg));
          } else if ("tool_result".equals(role)) {
            messages.put(buildBedrockToolResultMessage(msg));
          } else {
            messages.put(new JSONObject()
                .put("role", role)
                .put("content", new JSONArray().put(
                    new JSONObject().put("text", msg.getText()))));
          }
        } else {
          messages.put(new JSONObject()
              .put("role", role)
              .put("content", new JSONArray().put(
                  new JSONObject().put("text", msg.getText()))));
        }
      }
    }

    // Per-request context as separate user/assistant turns
    if (contextMessages != null) {
      for (String ctx : contextMessages) {
        if (ctx != null && !ctx.isEmpty()) {
          messages.put(new JSONObject()
              .put("role", "user")
              .put("content", new JSONArray().put(new JSONObject().put("text", ctx))));
          messages.put(new JSONObject()
              .put("role", "assistant")
              .put("content", new JSONArray().put(new JSONObject().put("text", "Understood."))));
        }
      }
    }

    messages.put(new JSONObject()
        .put("role", "user")
        .put("content", new JSONArray().put(new JSONObject().put("text", userMessage))));

    return messages;
  }

  /**
   * Translates a stored assistant message with structured content (Anthropic format)
   * into Bedrock content blocks format.
   *
   * <p>Anthropic format uses {@code tool_use} type blocks; Bedrock uses
   * {@code toolUse} nested objects.
   */
  private static JSONObject buildBedrockAssistantMessage(ChatMessage msg) {
    JSONArray content = new JSONArray();
    JSONArray parts = new JSONArray(msg.getStructuredContent());
    for (int i = 0; i < parts.length(); i++) {
      JSONObject part = parts.getJSONObject(i);
      String type = part.getString("type");
      if ("text".equals(type)) {
        content.put(new JSONObject().put("text", part.getString("text")));
      } else if ("tool_use".equals(type)) {
        content.put(new JSONObject()
            .put("toolUse", new JSONObject()
                .put("toolUseId", part.getString("id"))
                .put("name", part.getString("name"))
                .put("input", part.getJSONObject("input"))));
      }
    }
    return new JSONObject()
        .put("role", "assistant")
        .put("content", content);
  }

  /**
   * Translates a stored tool_result message (Anthropic format) into Bedrock
   * content blocks format.
   *
   * <p>Anthropic format uses {@code tool_result} type blocks with
   * {@code tool_use_id}; Bedrock wraps them under a {@code toolResult} key.
   */
  private static JSONObject buildBedrockToolResultMessage(ChatMessage msg) {
    JSONArray content = new JSONArray();
    JSONArray parts = new JSONArray(msg.getStructuredContent());
    for (int i = 0; i < parts.length(); i++) {
      JSONObject part = parts.getJSONObject(i);
      content.put(new JSONObject()
          .put("toolResult", new JSONObject()
              .put("toolUseId", part.getString("tool_use_id"))
              .put("content", new JSONArray().put(
                  new JSONObject().put("text", part.getString("content"))))));
    }
    return new JSONObject()
        .put("role", "user")
        .put("content", content);
  }

  /**
   * Builds the Bedrock-format tool definitions array.
   */
  private static JSONArray buildToolDefinitions(List<LLMTool> tools) {
    JSONArray toolDefs = new JSONArray();
    if (tools == null) {
      return toolDefs;
    }
    for (LLMTool tool : tools) {
      JSONObject schema;
      try {
        schema = new JSONObject(tool.getParameterSchema());
      } catch (JSONException e) {
        LOG.warning("Invalid parameter schema for tool " + tool.getName()
            + ": " + e.getMessage());
        schema = new JSONObject()
            .put("type", "object")
            .put("properties", new JSONObject());
      }
      toolDefs.put(new JSONObject()
          .put("toolSpec", new JSONObject()
              .put("name", tool.getName())
              .put("description", tool.getDescription())
              .put("inputSchema", new JSONObject().put("json", schema))));
    }
    return toolDefs;
  }

  /**
   * Builds the Bedrock Converse API request body.
   */
  private JSONObject buildRequestBody(JSONArray systemArray, JSONArray messages,
      JSONArray toolDefs) {
    JSONObject body = new JSONObject();
    if (systemArray.length() > 0) {
      body.put("system", systemArray);
    }
    body.put("messages", messages);
    body.put("inferenceConfig", new JSONObject().put("maxTokens", MAX_TOKENS));
    if (toolDefs.length() > 0) {
      body.put("toolConfig", new JSONObject().put("tools", toolDefs));
    }
    if (reasoningEffort != null && !reasoningEffort.isEmpty()) {
      body.put("additionalModelRequestFields", new JSONObject()
          .put("thinking", new JSONObject()
              .put("type", "adaptive")
              .put("effort", reasoningEffort)));
    }
    return body;
  }

  /**
   * Extracts the content array from the reconstructed Bedrock response JSON.
   * Returns an empty array if not present.
   */
  private static JSONArray extractContent(JSONObject responseJson) {
    JSONObject output = responseJson.optJSONObject("output");
    if (output == null) {
      return new JSONArray();
    }
    JSONObject message = output.optJSONObject("message");
    if (message == null) {
      return new JSONArray();
    }
    JSONArray content = message.optJSONArray("content");
    return content != null ? content : new JSONArray();
  }

  /**
   * Serializes the messages array, system array, and pending tool call IDs
   * into a JSON continuation state string.
   */
  private static String buildContinuationState(JSONArray messages, JSONArray systemArray,
      List<ToolUseInfo> pendingBlocks) {
    JSONObject state = new JSONObject();
    state.put("continuation", true);
    state.put("messages", messages);
    state.put("system", systemArray);
    JSONArray pending = new JSONArray();
    for (ToolUseInfo block : pendingBlocks) {
      pending.put(new JSONObject()
          .put("id", block.id)
          .put("name", block.name));
    }
    state.put("pendingToolCalls", pending);
    return state.toString();
  }

  /**
   * Checks the stop reason and throws if the response was truncated.
   */
  private static void checkStopReason(String stopReason) throws LLMProviderException {
    if ("max_tokens".equals(stopReason)) {
      LOG.warning("Bedrock response truncated: stopReason=max_tokens");
      throw new LLMProviderException(
          "Bedrock response truncated: stopReason=max_tokens",
          "The AI response was too long and got cut off. "
              + "Please try a simpler request or break it into smaller steps.");
    }
  }

  /**
   * Makes an HTTP POST to the Bedrock Converse Stream API with SigV4 auth,
   * automatic retry and exponential backoff for transient errors (429, 5xx).
   */
  private JSONObject callApi(JSONObject requestBody, StreamBuffer streamBuffer)
      throws LLMProviderException {
    String urlString;
    try {
      urlString = "https://bedrock-runtime." + region
          + ".amazonaws.com/model/"
          + URLEncoder.encode(model, "UTF-8")
          + "/converse-stream";
    } catch (IOException e) {
      throw new LLMProviderException(
          "Failed to encode model ID: " + e.getMessage(),
          "Internal error building the request. Please try again.",
          e);
    }

    byte[] bodyBytes = requestBody.toString().getBytes(StandardCharsets.UTF_8);

    long backoffMs = INITIAL_BACKOFF_MS;
    LLMProviderException lastException = null;

    for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
      if (attempt > 0) {
        LOG.info("Bedrock API retry attempt " + attempt + " after " + backoffMs + "ms backoff");
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
        // Build base headers, then sign
        Map<String, String> baseHeaders = new LinkedHashMap<>();
        baseHeaders.put("Content-Type", "application/json");

        Map<String, String> signedHeaders = signer.sign("POST", urlString, baseHeaders, bodyBytes);

        URL url = new URL(urlString);
        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
        conn.setReadTimeout(READ_TIMEOUT_MS);

        for (Map.Entry<String, String> entry : signedHeaders.entrySet()) {
          conn.setRequestProperty(entry.getKey(), entry.getValue());
        }
        conn.setRequestProperty("Content-Length", String.valueOf(bodyBytes.length));

        try (OutputStream os = conn.getOutputStream()) {
          os.write(bodyBytes);
          os.flush();
        }

        int statusCode = conn.getResponseCode();

        if (statusCode >= 200 && statusCode < 300) {
          if (streamBuffer != null) {
            return readStreamingResponse(conn, streamBuffer);
          } else {
            String responseText = readResponse(conn, statusCode);
            return new JSONObject(responseText);
          }
        }

        String responseText = readResponse(conn, statusCode);
        LOG.warning("Bedrock API error (HTTP " + statusCode + "): " + responseText);

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
              "Bedrock API returned HTTP " + statusCode + ": " + responseText,
              mapHttpErrorToUserMessage(statusCode));
          continue;
        }

        throw new LLMProviderException(
            "Bedrock API returned HTTP " + statusCode + ": " + responseText,
            mapHttpErrorToUserMessage(statusCode));

      } catch (IOException e) {
        if (attempt < MAX_RETRIES) {
          LOG.log(Level.WARNING,
              "Bedrock API connection error (attempt " + (attempt + 1) + ")", e);
          lastException = new LLMProviderException(
              "Failed to connect to Bedrock API: " + e.getMessage(),
              "Could not reach the AI service. Please try again later.",
              e);
          continue;
        }
        LOG.log(Level.WARNING, "Bedrock API connection error (final attempt)", e);
        throw new LLMProviderException(
            "Failed to connect to Bedrock API: " + e.getMessage(),
            "Could not reach the AI service. Please try again later.",
            e);
      } catch (JSONException e) {
        LOG.log(Level.WARNING, "Failed to parse Bedrock API response", e);
        throw new LLMProviderException(
            "Invalid JSON response from Bedrock API: " + e.getMessage(),
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
        "Bedrock API failed after " + MAX_RETRIES + " retries",
        "The AI service is currently unavailable. Please try again later.");
  }

  /**
   * Reads an SSE streaming response from the Bedrock Converse Stream API and
   * reconstructs a non-streaming-style JSON object.
   *
   * <p>Text deltas are pushed to the {@link StreamBuffer} as they arrive.
   *
   * <p>The reconstructed JSON has the shape:
   * <pre>{@code
   * {
   *   "output": {
   *     "message": {
   *       "role": "assistant",
   *       "content": [
   *         {"text": "..."},
   *         {"toolUse": {"toolUseId": "...", "name": "...", "input": {...}}}
   *       ]
   *     }
   *   },
   *   "stopReason": "end_turn",
   *   "usage": {"inputTokens": N, "outputTokens": N}
   * }
   * }</pre>
   */
  private JSONObject readStreamingResponse(HttpURLConnection conn, StreamBuffer streamBuffer)
      throws IOException, LLMProviderException {
    BufferedReader reader = new BufferedReader(
        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));

    String[] stopReason = { "end_turn" };
    JSONObject usage = new JSONObject();

    // Each entry: for text blocks — Object[]{"text", StringBuilder}
    // For tool blocks — Object[]{"toolUse", String id, String name, StringBuilder inputJsonAccum}
    List<Object[]> contentBlocks = new ArrayList<>();

    try {
      String line;
      while ((line = reader.readLine()) != null) {
        if (streamBuffer.isCancelled()) {
          throw new StreamBuffer.CancelledException();
        }
        if (line.isEmpty()) {
          continue;
        }

        if (!line.startsWith("data:")) {
          continue;
        }

        String dataStr = line.substring("data:".length()).trim();
        if (dataStr.isEmpty()) {
          continue;
        }

        try {
          JSONObject event = new JSONObject(dataStr);

          if (event.has("contentBlockStart")) {
            JSONObject cbs = event.getJSONObject("contentBlockStart");
            int index = cbs.optInt("contentBlockIndex", contentBlocks.size());
            JSONObject start = cbs.optJSONObject("start");

            // Ensure list is large enough
            while (contentBlocks.size() <= index) {
              contentBlocks.add(null);
            }

            if (start != null && start.has("toolUse")) {
              JSONObject toolUse = start.getJSONObject("toolUse");
              String id = toolUse.optString("toolUseId", "");
              String name = toolUse.optString("name", "");
              contentBlocks.set(index,
                  new Object[] { "toolUse", id, name, new StringBuilder() });
            } else {
              // Text block start
              contentBlocks.set(index, new Object[] { "text", new StringBuilder() });
            }
          } else if (event.has("contentBlockDelta")) {
            JSONObject cbd = event.getJSONObject("contentBlockDelta");
            int index = cbd.optInt("contentBlockIndex", -1);
            if (index >= 0 && index < contentBlocks.size() && contentBlocks.get(index) != null) {
              JSONObject delta = cbd.optJSONObject("delta");
              if (delta != null) {
                Object[] block = contentBlocks.get(index);
                String blockType = (String) block[0];
                if ("text".equals(blockType) && delta.has("text")) {
                  String text = delta.getString("text");
                  ((StringBuilder) block[1]).append(text);
                  streamBuffer.appendText(text);
                } else if ("toolUse".equals(blockType) && delta.has("toolUse")) {
                  JSONObject toolUseDelta = delta.getJSONObject("toolUse");
                  String partial = toolUseDelta.optString("input", "");
                  ((StringBuilder) block[3]).append(partial);
                }
              }
            }
          } else if (event.has("messageStop")) {
            JSONObject messageStop = event.getJSONObject("messageStop");
            String sr = messageStop.optString("stopReason", null);
            if (sr != null) {
              stopReason[0] = sr;
            }
          } else if (event.has("metadata")) {
            JSONObject metadata = event.getJSONObject("metadata");
            JSONObject eventUsage = metadata.optJSONObject("usage");
            if (eventUsage != null) {
              mergeUsage(usage, eventUsage);
            }
          }
          // contentBlockStop and messageStart need no special handling
        } catch (JSONException e) {
          LOG.log(Level.WARNING, "Skipping unparseable Bedrock SSE data: " + dataStr, e);
        }
      }
    } finally {
      reader.close();
      streamBuffer.markDone();
    }

    // Reconstruct the response in non-streaming format
    JSONArray content = new JSONArray();
    for (Object[] block : contentBlocks) {
      if (block == null) {
        continue;
      }
      String blockType = (String) block[0];
      if ("text".equals(blockType)) {
        String text = ((StringBuilder) block[1]).toString();
        if (!text.isEmpty()) {
          content.put(new JSONObject().put("text", text));
        }
      } else if ("toolUse".equals(blockType)) {
        String id = (String) block[1];
        String name = (String) block[2];
        String inputJsonStr = ((StringBuilder) block[3]).toString();
        JSONObject inputObj;
        try {
          inputObj = inputJsonStr.isEmpty() ? new JSONObject() : new JSONObject(inputJsonStr);
        } catch (JSONException e) {
          LOG.warning("Failed to parse accumulated toolUse input JSON: " + inputJsonStr);
          inputObj = new JSONObject();
        }
        content.put(new JSONObject()
            .put("toolUse", new JSONObject()
                .put("toolUseId", id)
                .put("name", name)
                .put("input", inputObj)));
      }
    }

    JSONObject result = new JSONObject();
    result.put("output", new JSONObject()
        .put("message", new JSONObject()
            .put("role", "assistant")
            .put("content", content)));
    result.put("stopReason", stopReason[0]);
    if (usage.length() > 0) {
      result.put("usage", usage);
    }
    return result;
  }

  /**
   * Reads the response body from an HTTP connection.
   */
  private static String readResponse(HttpURLConnection conn, int statusCode) throws IOException {
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
   * Returns whether an HTTP status code is eligible for automatic retry.
   */
  private static boolean isRetryable(int statusCode) {
    return statusCode == 429 || statusCode >= 500;
  }

  /**
   * Merges usage fields from a source object into the accumulator, summing numeric values.
   */
  private static void mergeUsage(JSONObject accumulator, JSONObject source) {
    for (Object keyObj : source.keySet()) {
      String key = keyObj.toString();
      if (source.get(key) instanceof Number) {
        int existing = accumulator.optInt(key, 0);
        accumulator.put(key, existing + source.getInt(key));
      } else {
        accumulator.put(key, source.get(key));
      }
    }
  }

  /**
   * Maps HTTP status codes to user-friendly error messages.
   */
  private static String mapHttpErrorToUserMessage(int statusCode) {
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
   * Internal representation of a toolUse content block from the Bedrock API response.
   */
  private static class ToolUseInfo {
    final String id;
    final String name;
    final String inputJson;

    ToolUseInfo(String id, String name, String inputJson) {
      this.id = id;
      this.name = name;
      this.inputJson = inputJson;
    }
  }
}
