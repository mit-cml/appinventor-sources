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

/**
 * LLM provider implementation for the Anthropic Messages API (Claude).
 *
 * <p>This is a stateless provider: the full conversation history must be
 * passed on each call. It uses the {@code tool_use} feature of the Messages
 * API and implements an internal tool-use loop for read-only tools (up to
 * {@value #MAX_TOOL_ITERATIONS} iterations).
 */
public class AnthropicProvider implements LLMProvider {

  private static final Logger LOG = Logger.getLogger(AnthropicProvider.class.getName());

  private static final String API_ENDPOINT = "https://api.anthropic.com/v1/messages";
  private static final String ANTHROPIC_VERSION = "2023-06-01";
  private static final int MAX_TOOL_ITERATIONS = 5;
  private static final int MAX_RETRIES = 3;
  private static final long INITIAL_BACKOFF_MS = 1000;
  private static final int MAX_TOKENS = 4096;
  private static final int CONNECT_TIMEOUT_MS = 30000;
  private static final int READ_TIMEOUT_MS = 120000;

  private final String apiKey;
  private final String model;

  /**
   * Creates a new Anthropic provider.
   *
   * @param apiKey the Anthropic API key
   * @param model  the model name (e.g. "claude-sonnet-4-20250514")
   */
  AnthropicProvider(String apiKey, String model) {
    this.apiKey = apiKey;
    this.model = model;
  }

  @Override
  public boolean isStateless() {
    return true;
  }

  @Override
  public LLMResponse chat(String systemPrompt, List<String> contextMessages,
      String userMessage, List<LLMTool> tools, String providerRef,
      List<ChatMessage> history, ReadOnlyToolResolver resolver)
      throws LLMProviderException {

    // Build the messages array from history + current user message
    JSONArray messages = buildMessages(history, contextMessages, userMessage);
    JSONArray toolDefs = buildToolDefinitions(tools);

    // Internal tool-use loop
    for (int iteration = 0; iteration < MAX_TOOL_ITERATIONS; iteration++) {
      JSONObject requestBody = new JSONObject();
      requestBody.put("model", model);
      requestBody.put("max_tokens", MAX_TOKENS);
      requestBody.put("system", systemPrompt);
      requestBody.put("messages", messages);
      if (toolDefs.length() > 0) {
        requestBody.put("tools", toolDefs);
      }
      if (AIDebug.enabled()) {
        AIDebug.log(LOG, "Anthropic request (iteration " + iteration + "):\n"
            + requestBody.toString(2));
      }

      JSONObject responseJson = callApi(requestBody);
      if (AIDebug.enabled()) {
        AIDebug.log(LOG, "Anthropic response (iteration " + iteration + "):\n"
            + responseJson.toString(2));
      }

      // Parse the response content blocks
      String stopReason = responseJson.optString("stop_reason", "end_turn");

      // Check for truncated response (max_tokens exceeded)
      checkStopReason(stopReason);

      JSONArray content = responseJson.optJSONArray("content");
      if (content == null) {
        content = new JSONArray();
      }

      // Extract text blocks and tool_use blocks
      StringBuilder textBuilder = new StringBuilder();
      List<ToolUseBlock> toolUseBlocks = new ArrayList<>();

      for (int i = 0; i < content.length(); i++) {
        JSONObject block = content.getJSONObject(i);
        String blockType = block.optString("type", "");
        if ("text".equals(blockType)) {
          textBuilder.append(block.optString("text", ""));
        } else if ("tool_use".equals(blockType)) {
          toolUseBlocks.add(new ToolUseBlock(
              block.getString("id"),
              block.getString("name"),
              block.optJSONObject("input") != null
                  ? block.getJSONObject("input").toString()
                  : "{}"));
        }
      }

      // If no tool calls, return the final response
      if (toolUseBlocks.isEmpty() || !"tool_use".equals(stopReason)) {
        return new LLMResponse(textBuilder.toString(), new ArrayList<RawToolCall>(), null);
      }

      // Classify tool calls into read-only and operation tools
      List<ToolUseBlock> readOnlyBlocks = new ArrayList<>();
      List<ToolUseBlock> operationBlocks = new ArrayList<>();

      for (ToolUseBlock block : toolUseBlocks) {
        if (resolver != null && resolver.isReadOnly(block.name)) {
          readOnlyBlocks.add(block);
        } else {
          operationBlocks.add(block);
        }
      }

      // If there are no read-only tools, return all tool calls as operations
      if (readOnlyBlocks.isEmpty()) {
        List<RawToolCall> rawCalls = new ArrayList<>();
        for (ToolUseBlock block : operationBlocks) {
          rawCalls.add(new RawToolCall(block.name, block.inputJson));
        }
        // Serialize continuation state: the assistant content must be in
        // the messages array so we can submit tool_result for each tool_use.
        messages.put(new JSONObject()
            .put("role", "assistant")
            .put("content", content));
        String contState = buildContinuationState(messages, systemPrompt, operationBlocks);
        return new LLMResponse(textBuilder.toString(), rawCalls, contState, true);
      }

      // Resolve read-only tools and continue the loop
      // Add the assistant message (the full content) to messages
      messages.put(new JSONObject()
          .put("role", "assistant")
          .put("content", content));

      // Build tool results for read-only tools
      JSONArray toolResults = new JSONArray();
      for (ToolUseBlock block : readOnlyBlocks) {
        String result;
        try {
          result = resolver.resolve(block.name, block.inputJson);
        } catch (ReadOnlyToolException e) {
          result = "Error: " + e.getMessage();
        }
        toolResults.put(new JSONObject()
            .put("type", "tool_result")
            .put("tool_use_id", block.id)
            .put("content", result));
      }

      // For any operation tools in a mixed response, send a placeholder result
      // telling the LLM we'll handle those separately
      for (ToolUseBlock block : operationBlocks) {
        toolResults.put(new JSONObject()
            .put("type", "tool_result")
            .put("tool_use_id", block.id)
            .put("content", "This operation tool call has been queued for execution. "
                + "Please continue with any remaining read-only lookups you need."));
      }

      messages.put(new JSONObject()
          .put("role", "user")
          .put("content", toolResults));

      if (AIDebug.enabled()) {
        AIDebug.log(LOG, "Anthropic tool-use loop iteration " + (iteration + 1)
            + ": readOnly=" + readOnlyBlocks.size()
            + ", operations=" + operationBlocks.size());
        for (ToolUseBlock block : readOnlyBlocks) {
          AIDebug.log(LOG, "  read-only: " + block.name);
        }
        for (ToolUseBlock block : operationBlocks) {
          AIDebug.log(LOG, "  operation: " + block.name);
        }
      } else {
        LOG.info("Anthropic tool-use loop iteration " + (iteration + 1)
            + ": resolved " + readOnlyBlocks.size() + " read-only tools, "
            + operationBlocks.size() + " operation tools pending");
      }
    }

    // Safety limit reached
    throw new LLMProviderException(
        "Anthropic tool-use loop exceeded " + MAX_TOOL_ITERATIONS + " iterations",
        "The AI took too many steps to process your request. Please try again.");
  }

  @Override
  public LLMResponse continueWithToolResults(String continuationState, List<LLMTool> tools,
      ReadOnlyToolResolver resolver) throws LLMProviderException {

    JSONObject state;
    try {
      state = new JSONObject(continuationState);
    } catch (JSONException e) {
      throw new LLMProviderException(
          "Invalid continuation state: " + e.getMessage(),
          "Failed to continue the AI response. Please try again.");
    }

    JSONArray messages = state.getJSONArray("messages");
    String systemPrompt = state.optString("systemPrompt", "");
    JSONArray pendingToolUseIds = state.getJSONArray("pendingToolUseIds");

    // Append a user message with tool_result blocks for each pending tool_use
    JSONArray toolResults = new JSONArray();
    for (int i = 0; i < pendingToolUseIds.length(); i++) {
      JSONObject pending = pendingToolUseIds.getJSONObject(i);
      toolResults.put(new JSONObject()
          .put("type", "tool_result")
          .put("tool_use_id", pending.getString("id"))
          .put("content", "Done."));
    }
    messages.put(new JSONObject()
        .put("role", "user")
        .put("content", toolResults));

    JSONArray toolDefs = buildToolDefinitions(tools);

    // Run the same tool-use loop as chat()
    for (int iteration = 0; iteration < MAX_TOOL_ITERATIONS; iteration++) {
      JSONObject requestBody = new JSONObject();
      requestBody.put("model", model);
      requestBody.put("max_tokens", MAX_TOKENS);
      requestBody.put("system", systemPrompt);
      requestBody.put("messages", messages);
      if (toolDefs.length() > 0) {
        requestBody.put("tools", toolDefs);
      }
      if (AIDebug.enabled()) {
        AIDebug.log(LOG, "Anthropic continue request (iteration " + iteration + "):\n"
            + requestBody.toString(2));
      }

      JSONObject responseJson = callApi(requestBody);
      if (AIDebug.enabled()) {
        AIDebug.log(LOG, "Anthropic continue response (iteration " + iteration + "):\n"
            + responseJson.toString(2));
      }

      String stopReason = responseJson.optString("stop_reason", "end_turn");

      // Check for truncated response (max_tokens exceeded)
      checkStopReason(stopReason);

      JSONArray content = responseJson.optJSONArray("content");
      if (content == null) {
        content = new JSONArray();
      }

      StringBuilder textBuilder = new StringBuilder();
      List<ToolUseBlock> toolUseBlocks = new ArrayList<>();

      for (int i = 0; i < content.length(); i++) {
        JSONObject block = content.getJSONObject(i);
        String blockType = block.optString("type", "");
        if ("text".equals(blockType)) {
          textBuilder.append(block.optString("text", ""));
        } else if ("tool_use".equals(blockType)) {
          toolUseBlocks.add(new ToolUseBlock(
              block.getString("id"),
              block.getString("name"),
              block.optJSONObject("input") != null
                  ? block.getJSONObject("input").toString()
                  : "{}"));
        }
      }

      // No tool calls → final response
      if (toolUseBlocks.isEmpty() || !"tool_use".equals(stopReason)) {
        return new LLMResponse(textBuilder.toString(), new ArrayList<RawToolCall>(), null, false);
      }

      // Classify tool calls
      List<ToolUseBlock> readOnlyBlocks = new ArrayList<>();
      List<ToolUseBlock> operationBlocks = new ArrayList<>();

      for (ToolUseBlock block : toolUseBlocks) {
        if (resolver != null && resolver.isReadOnly(block.name)) {
          readOnlyBlocks.add(block);
        } else {
          operationBlocks.add(block);
        }
      }

      // No read-only tools → return operations with continuation
      if (readOnlyBlocks.isEmpty()) {
        List<RawToolCall> rawCalls = new ArrayList<>();
        for (ToolUseBlock block : operationBlocks) {
          rawCalls.add(new RawToolCall(block.name, block.inputJson));
        }
        messages.put(new JSONObject()
            .put("role", "assistant")
            .put("content", content));
        String contState = buildContinuationState(messages, systemPrompt, operationBlocks);
        return new LLMResponse(textBuilder.toString(), rawCalls, contState, true);
      }

      // Resolve read-only tools and continue the loop
      messages.put(new JSONObject()
          .put("role", "assistant")
          .put("content", content));

      JSONArray readOnlyResults = new JSONArray();
      for (ToolUseBlock block : readOnlyBlocks) {
        String result;
        try {
          result = resolver.resolve(block.name, block.inputJson);
        } catch (ReadOnlyToolException e) {
          result = "Error: " + e.getMessage();
        }
        readOnlyResults.put(new JSONObject()
            .put("type", "tool_result")
            .put("tool_use_id", block.id)
            .put("content", result));
      }
      for (ToolUseBlock block : operationBlocks) {
        readOnlyResults.put(new JSONObject()
            .put("type", "tool_result")
            .put("tool_use_id", block.id)
            .put("content", "This operation tool call has been queued for execution. "
                + "Please continue with any remaining read-only lookups you need."));
      }
      messages.put(new JSONObject()
          .put("role", "user")
          .put("content", readOnlyResults));
    }

    throw new LLMProviderException(
        "Anthropic continuation tool-use loop exceeded " + MAX_TOOL_ITERATIONS + " iterations",
        "The AI took too many steps to process your request. Please try again.");
  }

  /**
   * Serializes the messages array, system prompt, and pending tool_use IDs
   * into a JSON continuation state string.
   */
  private String buildContinuationState(JSONArray messages, String systemPrompt,
      List<ToolUseBlock> pendingBlocks) {
    JSONObject state = new JSONObject();
    state.put("continuation", true);
    state.put("messages", messages);
    state.put("systemPrompt", systemPrompt);
    JSONArray pending = new JSONArray();
    for (ToolUseBlock block : pendingBlocks) {
      pending.put(new JSONObject()
          .put("id", block.id)
          .put("name", block.name));
    }
    state.put("pendingToolUseIds", pending);
    return state.toString();
  }

  /**
   * Builds the messages array from conversation history and the current
   * user message.
   */
  private JSONArray buildMessages(List<ChatMessage> history,
      List<String> contextMessages, String userMessage) {
    JSONArray messages = new JSONArray();
    if (history != null) {
      for (ChatMessage msg : history) {
        String role = msg.getRole();
        // Anthropic API uses "user" and "assistant" roles only in messages
        if ("system".equals(role)) {
          continue; // system prompt is handled separately
        }

        if (msg.hasStructuredContent()) {
          if ("assistant".equals(role)) {
            messages.put(buildStructuredAssistantMessage(msg));
          } else if ("tool_result".equals(role)) {
            messages.put(buildStructuredToolResultMessage(msg));
          } else {
            messages.put(new JSONObject()
                .put("role", role)
                .put("content", msg.getText()));
          }
        } else {
          messages.put(new JSONObject()
              .put("role", role)
              .put("content", msg.getText()));
        }
      }
    }
    // Per-request context as separate user/assistant turns before the user's message
    if (contextMessages != null) {
      for (String ctx : contextMessages) {
        if (ctx != null && !ctx.isEmpty()) {
          messages.put(new JSONObject()
              .put("role", "user")
              .put("content", ctx));
          messages.put(new JSONObject()
              .put("role", "assistant")
              .put("content", "Understood."));
        }
      }
    }
    messages.put(new JSONObject()
        .put("role", "user")
        .put("content", userMessage));
    return messages;
  }

  /**
   * Translates a stored assistant message with structured content to
   * Anthropic's native content blocks format.
   */
  private JSONObject buildStructuredAssistantMessage(ChatMessage msg) {
    JSONArray content = new JSONArray();
    JSONArray parts = new JSONArray(msg.getStructuredContent());
    for (int i = 0; i < parts.length(); i++) {
      JSONObject part = parts.getJSONObject(i);
      String type = part.getString("type");
      if ("text".equals(type)) {
        content.put(new JSONObject()
            .put("type", "text")
            .put("text", part.getString("text")));
      } else if ("tool_use".equals(type)) {
        content.put(new JSONObject()
            .put("type", "tool_use")
            .put("id", part.getString("id"))
            .put("name", part.getString("name"))
            .put("input", part.getJSONObject("input")));
      }
    }
    return new JSONObject()
        .put("role", "assistant")
        .put("content", content);
  }

  /**
   * Translates a stored tool_result message to Anthropic's native format.
   * Anthropic expects tool_result blocks inside a user message.
   */
  private JSONObject buildStructuredToolResultMessage(ChatMessage msg) {
    JSONArray content = new JSONArray();
    JSONArray parts = new JSONArray(msg.getStructuredContent());
    for (int i = 0; i < parts.length(); i++) {
      JSONObject part = parts.getJSONObject(i);
      content.put(new JSONObject()
          .put("type", "tool_result")
          .put("tool_use_id", part.getString("tool_use_id"))
          .put("content", part.getString("content")));
    }
    return new JSONObject()
        .put("role", "user")
        .put("content", content);
  }

  /**
   * Checks the stop reason and throws if the response was truncated.
   * Anthropic returns {@code stop_reason: "max_tokens"} when output is cut off.
   */
  private void checkStopReason(String stopReason) throws LLMProviderException {
    if ("max_tokens".equals(stopReason)) {
      LOG.warning("Anthropic response truncated: stop_reason=max_tokens");
      throw new LLMProviderException(
          "Anthropic response truncated: stop_reason=max_tokens",
          "The AI response was too long and got cut off. "
              + "Please try a simpler request or break it into smaller steps.");
    }
  }

  /**
   * Translates generic {@link LLMTool} definitions into the Anthropic tool
   * format.
   */
  private JSONArray buildToolDefinitions(List<LLMTool> tools) {
    JSONArray toolDefs = new JSONArray();
    if (tools == null) {
      return toolDefs;
    }
    for (LLMTool tool : tools) {
      JSONObject toolDef = new JSONObject();
      toolDef.put("name", tool.getName());
      toolDef.put("description", tool.getDescription());
      try {
        toolDef.put("input_schema", new JSONObject(tool.getParameterSchema()));
      } catch (JSONException e) {
        LOG.warning("Invalid parameter schema for tool " + tool.getName()
            + ": " + e.getMessage());
        toolDef.put("input_schema", new JSONObject()
            .put("type", "object")
            .put("properties", new JSONObject()));
      }
      toolDefs.put(toolDef);
    }
    return toolDefs;
  }

  /**
   * Makes an HTTP POST to the Anthropic Messages API with automatic retry
   * and exponential backoff for transient errors (429 rate-limit, 5xx).
   */
  private JSONObject callApi(JSONObject requestBody) throws LLMProviderException {
    long backoffMs = INITIAL_BACKOFF_MS;
    LLMProviderException lastException = null;

    for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
      if (attempt > 0) {
        LOG.info("Anthropic API retry attempt " + attempt + " after " + backoffMs + "ms backoff");
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
        URL url = new URL(API_ENDPOINT);
        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
        conn.setReadTimeout(READ_TIMEOUT_MS);

        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("x-api-key", apiKey);
        conn.setRequestProperty("anthropic-version", ANTHROPIC_VERSION);

        byte[] body = requestBody.toString().getBytes(StandardCharsets.UTF_8);
        conn.setRequestProperty("Content-Length", String.valueOf(body.length));

        try (OutputStream os = conn.getOutputStream()) {
          os.write(body);
          os.flush();
        }

        int statusCode = conn.getResponseCode();
        String responseText = readResponse(conn, statusCode);

        if (statusCode >= 200 && statusCode < 300) {
          return new JSONObject(responseText);
        }

        LOG.warning("Anthropic API error (HTTP " + statusCode + "): " + responseText);

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
              "Anthropic API returned HTTP " + statusCode + ": " + responseText,
              mapHttpErrorToUserMessage(statusCode));
          continue;
        }

        String userMsg = mapHttpErrorToUserMessage(statusCode);
        throw new LLMProviderException(
            "Anthropic API returned HTTP " + statusCode + ": " + responseText,
            userMsg);

      } catch (IOException e) {
        if (attempt < MAX_RETRIES) {
          LOG.log(Level.WARNING,
              "Anthropic API connection error (attempt " + (attempt + 1) + ")", e);
          lastException = new LLMProviderException(
              "Failed to connect to Anthropic API: " + e.getMessage(),
              "Could not reach the AI service. Please try again later.",
              e);
          continue;
        }
        LOG.log(Level.WARNING, "Anthropic API connection error (final attempt)", e);
        throw new LLMProviderException(
            "Failed to connect to Anthropic API: " + e.getMessage(),
            "Could not reach the AI service. Please try again later.",
            e);
      } catch (JSONException e) {
        LOG.log(Level.WARNING, "Failed to parse Anthropic API response", e);
        throw new LLMProviderException(
            "Invalid JSON response from Anthropic API: " + e.getMessage(),
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
        "Anthropic API failed after " + MAX_RETRIES + " retries",
        "The AI service is currently unavailable. Please try again later.");
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
        return "The AI service rejected the API key. Please contact your administrator.";
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
   * Internal representation of a tool_use content block from the Anthropic
   * response.
   */
  private static class ToolUseBlock {
    final String id;
    final String name;
    final String inputJson;

    ToolUseBlock(String id, String name, String inputJson) {
      this.id = id;
      this.name = name;
      this.inputJson = inputJson;
    }
  }
}
