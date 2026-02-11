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
 * LLM provider implementation for the OpenAI Responses API.
 *
 * <p>This is a stateful provider: conversation context is maintained
 * server-side via {@code previous_response_id}. Only the new user message
 * (or tool results) is sent on each call. It uses the function calling
 * feature and implements an internal tool-use loop for read-only tools
 * (up to {@value #MAX_TOOL_ITERATIONS} iterations).
 */
public class OpenAIProvider implements LLMProvider {

  private static final Logger LOG = Logger.getLogger(OpenAIProvider.class.getName());

  private static final String API_ENDPOINT = "https://api.openai.com/v1/responses";
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
    return false;
  }

  @Override
  public LLMResponse chat(String systemPrompt, String userMessage, List<LLMTool> tools,
      String providerRef, List<ChatMessage> history, ReadOnlyToolResolver resolver)
      throws LLMProviderException {

    JSONArray toolDefs = buildToolDefinitions(tools);
    String responseId = extractResponseId(providerRef);

    // Internal tool-use loop
    // On the first iteration, input is the user message string.
    // On subsequent iterations, input is a tool results array and we chain
    // via previous_response_id.
    Object currentInput = userMessage;

    for (int iteration = 0; iteration < MAX_TOOL_ITERATIONS; iteration++) {
      JSONObject requestBody = new JSONObject();
      requestBody.put("model", model);
      requestBody.put("max_output_tokens", MAX_TOKENS);
      requestBody.put("truncation", "auto");

      if (systemPrompt != null && !systemPrompt.isEmpty()) {
        requestBody.put("instructions", systemPrompt);
      }

      requestBody.put("input", currentInput);

      if (responseId != null) {
        requestBody.put("previous_response_id", responseId);
      }

      if (toolDefs.length() > 0) {
        requestBody.put("tools", toolDefs);
      }

      if (AIDebug.enabled()) {
        AIDebug.log(LOG, "OpenAI request (iteration " + iteration + "):\n"
            + requestBody.toString(2));
      }
      JSONObject responseJson = callApi(requestBody);
      if (AIDebug.enabled()) {
        AIDebug.log(LOG, "OpenAI response (iteration " + iteration + "):\n"
            + responseJson.toString(2));
      }

      // Update response ID for chaining
      responseId = responseJson.getString("id");

      // Parse output items
      ParsedOutput parsed = parseOutputItems(responseJson);

      // If no tool calls, return the final response
      if (parsed.toolCalls.isEmpty()) {
        String ref = buildSimpleRef(responseId);
        return new LLMResponse(parsed.text, new ArrayList<RawToolCall>(), ref);
      }

      // Classify tool calls into read-only and operation tools
      List<ToolCallInfo> readOnlyCalls = new ArrayList<>();
      List<ToolCallInfo> operationCalls = new ArrayList<>();

      for (ToolCallInfo info : parsed.toolCalls) {
        if (resolver != null && resolver.isReadOnly(info.name)) {
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
        String contState = buildContinuationState(responseId, systemPrompt, operationCalls);
        return new LLMResponse(
            parsed.text,
            rawCalls,
            contState,
            true);
      }

      // Build tool results input for the next iteration
      JSONArray toolResultsInput = new JSONArray();

      // Resolve read-only tools
      for (ToolCallInfo info : readOnlyCalls) {
        String result;
        try {
          result = resolver.resolve(info.name, info.argsJson);
        } catch (ReadOnlyToolException e) {
          result = "Error: " + e.getMessage();
        }
        toolResultsInput.put(new JSONObject()
            .put("type", "function_call_output")
            .put("call_id", info.id)
            .put("output", result));
      }

      // For operation tools in a mixed response, send a placeholder result
      for (ToolCallInfo info : operationCalls) {
        toolResultsInput.put(new JSONObject()
            .put("type", "function_call_output")
            .put("call_id", info.id)
            .put("output", "This operation tool call has been queued for execution. "
                + "Please continue with any remaining read-only lookups you need."));
      }

      // Set up next iteration: chain via previous_response_id and tool results
      currentInput = toolResultsInput;

      if (AIDebug.enabled()) {
        AIDebug.log(LOG, "OpenAI tool-use loop iteration " + (iteration + 1)
            + ": readOnly=" + readOnlyCalls.size()
            + ", operations=" + operationCalls.size());
        for (ToolCallInfo info : readOnlyCalls) {
          AIDebug.log(LOG, "  read-only: " + info.name);
        }
        for (ToolCallInfo info : operationCalls) {
          AIDebug.log(LOG, "  operation: " + info.name);
        }
      } else {
        LOG.info("OpenAI tool-use loop iteration " + (iteration + 1)
            + ": resolved " + readOnlyCalls.size() + " read-only tools, "
            + operationCalls.size() + " operation tools pending");
      }
    }

    // Safety limit reached
    throw new LLMProviderException(
        "OpenAI tool-use loop exceeded " + MAX_TOOL_ITERATIONS + " iterations",
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

    String responseId = state.getString("responseId");
    String systemPrompt = state.optString("systemPrompt", "");
    JSONArray pendingToolCalls = state.getJSONArray("pendingToolCalls");

    // Build tool result input items for each pending tool call
    JSONArray toolResultsInput = new JSONArray();
    for (int i = 0; i < pendingToolCalls.length(); i++) {
      JSONObject pending = pendingToolCalls.getJSONObject(i);
      toolResultsInput.put(new JSONObject()
          .put("type", "function_call_output")
          .put("call_id", pending.getString("id"))
          .put("output", "Done."));
    }

    JSONArray toolDefs = buildToolDefinitions(tools);

    // Run the same tool-use loop as chat()
    Object currentInput = toolResultsInput;

    for (int iteration = 0; iteration < MAX_TOOL_ITERATIONS; iteration++) {
      JSONObject requestBody = new JSONObject();
      requestBody.put("model", model);
      requestBody.put("max_output_tokens", MAX_TOKENS);
      requestBody.put("truncation", "auto");

      if (systemPrompt != null && !systemPrompt.isEmpty()) {
        requestBody.put("instructions", systemPrompt);
      }

      requestBody.put("input", currentInput);
      requestBody.put("previous_response_id", responseId);

      if (toolDefs.length() > 0) {
        requestBody.put("tools", toolDefs);
      }

      if (AIDebug.enabled()) {
        AIDebug.log(LOG, "OpenAI continue request (iteration " + iteration + "):\n"
            + requestBody.toString(2));
      }
      JSONObject responseJson = callApi(requestBody);
      if (AIDebug.enabled()) {
        AIDebug.log(LOG, "OpenAI continue response (iteration " + iteration + "):\n"
            + responseJson.toString(2));
      }

      // Update response ID for chaining
      responseId = responseJson.getString("id");

      // Parse output items
      ParsedOutput parsed = parseOutputItems(responseJson);

      // No tool calls -> final response
      if (parsed.toolCalls.isEmpty()) {
        String ref = buildSimpleRef(responseId);
        return new LLMResponse(parsed.text, new ArrayList<RawToolCall>(), ref, false);
      }

      // Classify tool calls
      List<ToolCallInfo> readOnlyCalls = new ArrayList<>();
      List<ToolCallInfo> operationCalls = new ArrayList<>();

      for (ToolCallInfo info : parsed.toolCalls) {
        if (resolver != null && resolver.isReadOnly(info.name)) {
          readOnlyCalls.add(info);
        } else {
          operationCalls.add(info);
        }
      }

      // No read-only tools -> return operations with continuation
      if (readOnlyCalls.isEmpty()) {
        List<RawToolCall> rawCalls = new ArrayList<>();
        for (ToolCallInfo info : operationCalls) {
          rawCalls.add(new RawToolCall(info.name, info.argsJson));
        }
        String contState = buildContinuationState(responseId, systemPrompt, operationCalls);
        return new LLMResponse(
            parsed.text,
            rawCalls,
            contState,
            true);
      }

      // Build tool results input for the next iteration
      JSONArray nextToolResults = new JSONArray();

      // Resolve read-only tools
      for (ToolCallInfo info : readOnlyCalls) {
        String result;
        try {
          result = resolver.resolve(info.name, info.argsJson);
        } catch (ReadOnlyToolException e) {
          result = "Error: " + e.getMessage();
        }
        nextToolResults.put(new JSONObject()
            .put("type", "function_call_output")
            .put("call_id", info.id)
            .put("output", result));
      }

      for (ToolCallInfo info : operationCalls) {
        nextToolResults.put(new JSONObject()
            .put("type", "function_call_output")
            .put("call_id", info.id)
            .put("output", "This operation tool call has been queued for execution. "
                + "Please continue with any remaining read-only lookups you need."));
      }

      currentInput = nextToolResults;
    }

    throw new LLMProviderException(
        "OpenAI continuation tool-use loop exceeded " + MAX_TOOL_ITERATIONS + " iterations",
        "The AI took too many steps to process your request. Please try again.");
  }

  /**
   * Extracts the response ID from a providerRef string.
   * Handles both JSON format ({@code {"responseId":"resp_xxx"}}) and null.
   *
   * @return the response ID, or null if providerRef is null/empty/invalid
   */
  private String extractResponseId(String providerRef) {
    if (providerRef == null || providerRef.isEmpty()) {
      return null;
    }
    try {
      JSONObject ref = new JSONObject(providerRef);
      return ref.optString("responseId", null);
    } catch (JSONException e) {
      LOG.warning("Failed to parse providerRef: " + e.getMessage());
      return null;
    }
  }

  /**
   * Builds a simple providerRef JSON string containing the response ID.
   */
  private String buildSimpleRef(String responseId) {
    return new JSONObject().put("responseId", responseId).toString();
  }

  /**
   * Serializes the response ID, system prompt, and pending tool call IDs
   * into a JSON continuation state string.
   */
  private String buildContinuationState(String responseId, String systemPrompt,
      List<ToolCallInfo> pendingCalls) {
    JSONObject state = new JSONObject();
    state.put("continuation", true);
    state.put("responseId", responseId);
    state.put("systemPrompt", systemPrompt != null ? systemPrompt : "");
    JSONArray pending = new JSONArray();
    for (ToolCallInfo info : pendingCalls) {
      pending.put(new JSONObject()
          .put("id", info.id)
          .put("name", info.name));
    }
    state.put("pendingToolCalls", pending);
    return state.toString();
  }

  /**
   * Parses the {@code output} array from an OpenAI Responses API response,
   * extracting text content and tool call information.
   */
  private ParsedOutput parseOutputItems(JSONObject responseJson) {
    StringBuilder text = new StringBuilder();
    List<ToolCallInfo> toolCalls = new ArrayList<>();

    JSONArray output = responseJson.optJSONArray("output");
    if (output == null) {
      return new ParsedOutput(text.toString(), toolCalls);
    }

    for (int i = 0; i < output.length(); i++) {
      JSONObject item = output.getJSONObject(i);
      String type = item.getString("type");

      if ("message".equals(type)) {
        JSONArray content = item.optJSONArray("content");
        if (content != null) {
          for (int j = 0; j < content.length(); j++) {
            JSONObject contentItem = content.getJSONObject(j);
            if ("output_text".equals(contentItem.getString("type"))) {
              text.append(contentItem.getString("text"));
            }
          }
        }
      } else if ("function_call".equals(type)) {
        String callId = item.getString("call_id");
        String name = item.getString("name");
        String args = item.optString("arguments", "{}");
        toolCalls.add(new ToolCallInfo(callId, name, args));
      }
    }

    return new ParsedOutput(text.toString(), toolCalls);
  }

  /**
   * Translates generic {@link LLMTool} definitions into the OpenAI
   * Responses API function tool format.
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

      toolDefs.put(new JSONObject()
          .put("type", "function")
          .put("name", tool.getName())
          .put("description", tool.getDescription())
          .put("parameters", parameters));
    }
    return toolDefs;
  }

  /**
   * Makes an HTTP POST to the OpenAI Responses API.
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
   * The {@code id} field holds the {@code call_id} from Responses API
   * {@code function_call} output items.
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

  /**
   * Holds the parsed text content and tool calls from a Responses API output.
   */
  private static class ParsedOutput {
    final String text;
    final List<ToolCallInfo> toolCalls;

    ParsedOutput(String text, List<ToolCallInfo> toolCalls) {
      this.text = text;
      this.toolCalls = toolCalls;
    }
  }
}
