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
  public LLMResponse chat(String systemPrompt, String userMessage, List<LLMTool> tools,
      String providerRef, List<ChatMessage> history, ReadOnlyToolResolver resolver)
      throws LLMProviderException {

    // Build the messages array from history + current user message
    JSONArray messages = buildMessages(history, userMessage);
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
        return new LLMResponse(textBuilder.toString(), rawCalls, null);
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

  /**
   * Builds the messages array from conversation history and the current
   * user message.
   */
  private JSONArray buildMessages(List<ChatMessage> history, String userMessage) {
    JSONArray messages = new JSONArray();
    if (history != null) {
      for (ChatMessage msg : history) {
        String role = msg.getRole();
        // Anthropic API uses "user" and "assistant" roles only in messages
        if ("system".equals(role)) {
          continue; // system prompt is handled separately
        }
        messages.put(new JSONObject()
            .put("role", role)
            .put("content", msg.getText()));
      }
    }
    messages.put(new JSONObject()
        .put("role", "user")
        .put("content", userMessage));
    return messages;
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
   * Makes an HTTP POST to the Anthropic Messages API.
   */
  private JSONObject callApi(JSONObject requestBody) throws LLMProviderException {
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

      if (statusCode < 200 || statusCode >= 300) {
        LOG.warning("Anthropic API error (HTTP " + statusCode + "): " + responseText);
        String userMsg = mapHttpErrorToUserMessage(statusCode);
        throw new LLMProviderException(
            "Anthropic API returned HTTP " + statusCode + ": " + responseText,
            userMsg);
      }

      return new JSONObject(responseText);

    } catch (IOException e) {
      LOG.log(Level.WARNING, "Anthropic API connection error", e);
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
