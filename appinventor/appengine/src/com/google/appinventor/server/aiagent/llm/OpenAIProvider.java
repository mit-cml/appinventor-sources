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

/**
 * LLM provider implementation for the OpenAI Chat Completions API.
 *
 * <p>This is a stateless provider: the full conversation history is sent on
 * each call via the messages array. It uses the function calling feature and
 * implements an internal tool-use loop for read-only tools (up to
 * {@value #MAX_TOOL_ITERATIONS} iterations).
 */
public class OpenAIProvider implements LLMProvider {

  private static final Logger LOG = Logger.getLogger(OpenAIProvider.class.getName());

  private static final String API_ENDPOINT = "https://api.openai.com/v1/chat/completions";
  private static final int MAX_TOOL_ITERATIONS = 5;
  private static final int MAX_TOKENS = 4096;
  private static final int CONNECT_TIMEOUT_MS = 30000;
  private static final int READ_TIMEOUT_MS = 120000;

  private final String apiKey;
  private final String model;

  /**
   * Creates a new OpenAI provider.
   *
   * @param apiKey the OpenAI API key
   * @param model  the model name (e.g. "gpt-4o")
   */
  OpenAIProvider(String apiKey, String model) {
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

    // Build the messages array
    JSONArray messages = buildMessages(systemPrompt, history, userMessage);
    JSONArray toolDefs = buildToolDefinitions(tools);

    // Internal tool-use loop
    for (int iteration = 0; iteration < MAX_TOOL_ITERATIONS; iteration++) {
      JSONObject requestBody = new JSONObject();
      requestBody.put("model", model);
      requestBody.put("max_completion_tokens", MAX_TOKENS);
      requestBody.put("messages", messages);
      if (toolDefs.length() > 0) {
        requestBody.put("tools", toolDefs);
      }
      JSONObject responseJson = callApi(requestBody);

      // Parse choices
      JSONArray choices = responseJson.optJSONArray("choices");
      if (choices == null || choices.length() == 0) {
        return new LLMResponse("", new ArrayList<RawToolCall>(), null);
      }

      JSONObject choice = choices.getJSONObject(0);
      JSONObject message = choice.getJSONObject("message");
      String finishReason = choice.optString("finish_reason", "stop");

      String textContent = message.optString("content", "");
      JSONArray toolCalls = message.optJSONArray("tool_calls");

      // If no tool calls, return the final response
      if (toolCalls == null || toolCalls.length() == 0 || !"tool_calls".equals(finishReason)) {
        return new LLMResponse(
            textContent != null ? textContent : "",
            new ArrayList<RawToolCall>(),
            null);
      }

      // Classify tool calls into read-only and operation tools
      List<ToolCallInfo> readOnlyCalls = new ArrayList<>();
      List<ToolCallInfo> operationCalls = new ArrayList<>();

      for (int i = 0; i < toolCalls.length(); i++) {
        JSONObject tc = toolCalls.getJSONObject(i);
        String tcId = tc.getString("id");
        JSONObject function = tc.getJSONObject("function");
        String name = function.getString("name");
        String args = function.optString("arguments", "{}");

        ToolCallInfo info = new ToolCallInfo(tcId, name, args);
        if (resolver != null && resolver.isReadOnly(name)) {
          readOnlyCalls.add(info);
        } else {
          operationCalls.add(info);
        }
      }

      // If no read-only tools, return all as operation tool calls
      if (readOnlyCalls.isEmpty()) {
        List<RawToolCall> rawCalls = new ArrayList<>();
        for (ToolCallInfo info : operationCalls) {
          rawCalls.add(new RawToolCall(info.name, info.argsJson));
        }
        return new LLMResponse(
            textContent != null ? textContent : "",
            rawCalls,
            null);
      }

      // Add the assistant message with tool_calls to messages
      messages.put(message);

      // Resolve read-only tools and add tool results
      for (ToolCallInfo info : readOnlyCalls) {
        String result;
        try {
          result = resolver.resolve(info.name, info.argsJson);
        } catch (ReadOnlyToolException e) {
          result = "Error: " + e.getMessage();
        }
        messages.put(new JSONObject()
            .put("role", "tool")
            .put("tool_call_id", info.id)
            .put("content", result));
      }

      // For operation tools in a mixed response, send a placeholder result
      for (ToolCallInfo info : operationCalls) {
        messages.put(new JSONObject()
            .put("role", "tool")
            .put("tool_call_id", info.id)
            .put("content", "This operation tool call has been queued for execution. "
                + "Please continue with any remaining read-only lookups you need."));
      }

      LOG.info("OpenAI tool-use loop iteration " + (iteration + 1)
          + ": resolved " + readOnlyCalls.size() + " read-only tools, "
          + operationCalls.size() + " operation tools pending");
    }

    // Safety limit reached
    throw new LLMProviderException(
        "OpenAI tool-use loop exceeded " + MAX_TOOL_ITERATIONS + " iterations",
        "The AI took too many steps to process your request. Please try again.");
  }

  /**
   * Builds the messages array from system prompt, conversation history,
   * and the current user message.
   */
  private JSONArray buildMessages(String systemPrompt, List<ChatMessage> history,
      String userMessage) {
    JSONArray messages = new JSONArray();

    // System message first
    if (systemPrompt != null && !systemPrompt.isEmpty()) {
      messages.put(new JSONObject()
          .put("role", "system")
          .put("content", systemPrompt));
    }

    // Conversation history
    if (history != null) {
      for (ChatMessage msg : history) {
        messages.put(new JSONObject()
            .put("role", msg.getRole())
            .put("content", msg.getText()));
      }
    }

    // Current user message
    messages.put(new JSONObject()
        .put("role", "user")
        .put("content", userMessage));

    return messages;
  }

  /**
   * Translates generic {@link LLMTool} definitions into the OpenAI
   * function-calling format.
   */
  private JSONArray buildToolDefinitions(List<LLMTool> tools) {
    JSONArray toolDefs = new JSONArray();
    if (tools == null) {
      return toolDefs;
    }
    for (LLMTool tool : tools) {
      JSONObject parameters;
      try {
        parameters = new JSONObject(tool.getParameterSchema());
      } catch (JSONException e) {
        LOG.warning("Invalid parameter schema for tool " + tool.getName()
            + ": " + e.getMessage());
        parameters = new JSONObject()
            .put("type", "object")
            .put("properties", new JSONObject());
      }

      JSONObject functionDef = new JSONObject()
          .put("name", tool.getName())
          .put("description", tool.getDescription())
          .put("parameters", parameters);

      toolDefs.put(new JSONObject()
          .put("type", "function")
          .put("function", functionDef));
    }
    return toolDefs;
  }

  /**
   * Makes an HTTP POST to the OpenAI Chat Completions API.
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
      conn.setRequestProperty("Authorization", "Bearer " + apiKey);

      byte[] body = requestBody.toString().getBytes(StandardCharsets.UTF_8);
      conn.setRequestProperty("Content-Length", String.valueOf(body.length));

      try (OutputStream os = conn.getOutputStream()) {
        os.write(body);
        os.flush();
      }

      int statusCode = conn.getResponseCode();
      String responseText = readResponse(conn, statusCode);

      if (statusCode < 200 || statusCode >= 300) {
        LOG.warning("OpenAI API error (HTTP " + statusCode + "): " + responseText);
        String userMsg = mapHttpErrorToUserMessage(statusCode);
        throw new LLMProviderException(
            "OpenAI API returned HTTP " + statusCode + ": " + responseText,
            userMsg);
      }

      return new JSONObject(responseText);

    } catch (IOException e) {
      LOG.log(Level.WARNING, "OpenAI API connection error", e);
      throw new LLMProviderException(
          "Failed to connect to OpenAI API: " + e.getMessage(),
          "Could not reach the AI service. Please try again later.",
          e);
    } catch (JSONException e) {
      LOG.log(Level.WARNING, "Failed to parse OpenAI API response", e);
      throw new LLMProviderException(
          "Invalid JSON response from OpenAI API: " + e.getMessage(),
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
   * Internal representation of an OpenAI tool call.
   */
  private static class ToolCallInfo {
    final String id;
    final String name;
    final String argsJson;

    ToolCallInfo(String id, String name, String argsJson) {
      this.id = id;
      this.name = name;
      this.argsJson = argsJson;
    }
  }
}
