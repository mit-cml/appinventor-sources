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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appinventor.server.aiagent.AIDebug;
import com.google.appinventor.server.aiagent.StreamBuffer;

/**
 * Base class for LLM providers that use the OpenAI Chat Completions wire format.
 *
 * <p>This is a stateless provider: the full conversation history must be
 * passed on each call. It uses the OpenAI-compatible Chat Completions
 * endpoint with function calling and implements an internal tool-use loop
 * for read-only tools (up to {@value #MAX_TOOL_ITERATIONS} iterations).
 *
 * <p>Subclasses must supply the base URL via the constructor and may
 * override {@link #getEndpoint()}, {@link #getHeaders()},
 * {@link #getMaxTokens()}, and {@link #getProviderName()} to customise
 * behaviour.
 */
public class OpenAIChatCompletionsProvider implements LLMProvider {

  private static final Logger LOG = Logger.getLogger(OpenAIChatCompletionsProvider.class.getName());

  private static final int MAX_TOOL_ITERATIONS = 5;
  private static final int MAX_RETRIES = 3;
  private static final long INITIAL_BACKOFF_MS = 1000;
  private static final int MAX_TOKENS = 131072;
  private static final int CONNECT_TIMEOUT_MS = 30000;
  private static final int READ_TIMEOUT_MS = 600000;

  protected final String apiKey;
  protected final String model;
  protected final String baseUrl;

  /**
   * Creates a new OpenAI-compatible Chat Completions provider.
   *
   * @param apiKey  the API key
   * @param model   the model name
   * @param baseUrl the base URL of the API (trailing slash is stripped)
   */
  OpenAIChatCompletionsProvider(String apiKey, String model, String baseUrl) {
    this.apiKey = apiKey;
    this.model = model;
    this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
  }

  /**
   * Returns the full Chat Completions endpoint URL.
   * Subclasses may override to point to a different path.
   */
  protected String getEndpoint() {
    return baseUrl + "/v1/chat/completions";
  }

  /**
   * Returns the HTTP request headers to use for each API call.
   * Subclasses may override to add or change headers.
   */
  protected Map<String, String> getHeaders() {
    Map<String, String> headers = new LinkedHashMap<>();
    headers.put("Content-Type", "application/json");
    headers.put("Authorization", "Bearer " + apiKey);
    return headers;
  }

  /**
   * Returns the max_tokens value to include in each request.
   * Subclasses may override to use a different limit.
   */
  protected int getMaxTokens() {
    return MAX_TOKENS;
  }

  /**
   * Returns a human-readable provider name used in log messages.
   * Subclasses should override to return their own name.
   */
  protected String getProviderName() {
    return "OpenAI-Compatible";
  }

  /**
   * Hook for subclasses to add provider-specific fields to the request
   * body (e.g. {@code reasoning} for OpenRouter). Called after the
   * standard {@code model}/{@code messages}/{@code tools} fields are set.
   * Default no-op.
   */
  protected void decorateRequestBody(JSONObject requestBody) {
    // no-op by default
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

    // Build the messages array from history + current user message
    JSONArray messages = buildMessages(systemPrompt, history, contextMessages, userMessage);
    JSONArray toolDefs = buildToolDefinitions(tools);

    // Internal tool-use loop
    for (int iteration = 0; iteration < MAX_TOOL_ITERATIONS; iteration++) {
      JSONObject requestBody = new JSONObject();
      requestBody.put("model", model);
      requestBody.put("messages", messages);
      requestBody.put("max_tokens", getMaxTokens());
      if (streamBuffer != null) {
        requestBody.put("stream", true);
        if (AIDebug.enabled()) {
          requestBody.put("stream_options",
              new JSONObject().put("include_usage", true));
        }
      }
      if (toolDefs.length() > 0) {
        requestBody.put("tools", toolDefs);
      }
      decorateRequestBody(requestBody);
      if (AIDebug.enabled()) {
        AIDebug.log(LOG, getProviderName() + " request (iteration " + iteration + "):\n"
            + requestBody.toString(2));
      }

      JSONObject responseJson = callApi(requestBody, streamBuffer);
      if (AIDebug.enabled()) {
        AIDebug.log(LOG, getProviderName() + " response (iteration " + iteration + "):\n"
            + responseJson.toString(2));
        AIDebug.recordUsage(getProviderName(), model,
            TokenUsage.fromOpenAIChat(responseJson.optJSONObject("usage")));
      }

      // Parse the response
      JSONArray choices = responseJson.optJSONArray("choices");
      if (choices == null || choices.length() == 0) {
        return new LLMResponse("", new ArrayList<RawToolCall>(), null);
      }

      JSONObject choice = choices.getJSONObject(0);

      // Check for truncated response (output length exceeded)
      checkFinishReason(choice);

      JSONObject message = choice.optJSONObject("message");
      if (message == null) {
        return new LLMResponse("", new ArrayList<RawToolCall>(), null);
      }

      String textContent = message.optString("content", "");
      if (textContent == null) {
        textContent = "";
      }
      JSONArray toolCalls = message.optJSONArray("tool_calls");

      // If no tool calls, return the final response
      if (toolCalls == null || toolCalls.length() == 0) {
        return new LLMResponse(textContent, new ArrayList<RawToolCall>(), null);
      }

      // Classify tool calls into read-only and operation tools
      List<ToolCallInfo> readOnlyCalls = new ArrayList<>();
      List<ToolCallInfo> operationCalls = new ArrayList<>();

      for (int i = 0; i < toolCalls.length(); i++) {
        JSONObject tc = toolCalls.getJSONObject(i);
        JSONObject function = tc.getJSONObject("function");
        String id = tc.getString("id");
        String name = function.getString("name");
        String argsJson = function.optString("arguments", "{}");

        ToolCallInfo info = new ToolCallInfo(id, name, argsJson);
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
        // Add the assistant message to messages for continuation state
        messages.put(message);
        String contState = buildContinuationState(messages, operationCalls);
        return new LLMResponse(textContent, rawCalls, contState, true);
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

      // For operation tools in a mixed response, send a placeholder
      for (ToolCallInfo info : operationCalls) {
        messages.put(new JSONObject()
            .put("role", "tool")
            .put("tool_call_id", info.id)
            .put("content", "This operation tool call has been queued for execution. "
                + "Please continue with any remaining read-only lookups you need."));
      }

      if (AIDebug.enabled()) {
        AIDebug.log(LOG, getProviderName() + " tool-use loop iteration " + (iteration + 1)
            + ": readOnly=" + readOnlyCalls.size()
            + ", operations=" + operationCalls.size());
        for (ToolCallInfo info : readOnlyCalls) {
          AIDebug.log(LOG, "  read-only: " + info.name);
        }
        for (ToolCallInfo info : operationCalls) {
          AIDebug.log(LOG, "  operation: " + info.name);
        }
      } else {
        LOG.info(getProviderName() + " tool-use loop iteration " + (iteration + 1)
            + ": resolved " + readOnlyCalls.size() + " read-only tools, "
            + operationCalls.size() + " operation tools pending");
      }
    }

    // Safety limit reached
    throw new LLMProviderException(
        getProviderName() + " tool-use loop exceeded " + MAX_TOOL_ITERATIONS + " iterations",
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
    JSONArray pendingToolCalls = state.getJSONArray("pendingToolCalls");

    // Append tool results for each pending tool call.
    // Use per-call results when available (annotated by AIAgentEngine) instead
    // of blanket "Done." so the LLM knows which tool calls were rejected.
    JSONArray toolCallResults = state.optJSONArray("toolCallResults");
    for (int i = 0; i < pendingToolCalls.length(); i++) {
      JSONObject pending = pendingToolCalls.getJSONObject(i);
      String resultContent = "Done.";
      if (toolCallResults != null && i < toolCallResults.length()) {
        resultContent = toolCallResults.getJSONObject(i).optString("result", "Done.");
      }
      messages.put(new JSONObject()
          .put("role", "tool")
          .put("tool_call_id", pending.getString("id"))
          .put("content", resultContent));
    }

    // Inject per-request context messages after tool results
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
      // The trailing assistant ack would leave the model with no prompt
      // to respond to (last role = assistant) and it would return an
      // empty completion. Drop it so the final context message (the
      // continuation-scope instruction) is the last turn instead.
      if (messages.length() > 0
          && "assistant".equals(messages.getJSONObject(
              messages.length() - 1).optString("role"))) {
        messages.remove(messages.length() - 1);
      }
    }

    JSONArray toolDefs = buildToolDefinitions(tools);

    // Run the same tool-use loop as chat()
    for (int iteration = 0; iteration < MAX_TOOL_ITERATIONS; iteration++) {
      JSONObject requestBody = new JSONObject();
      requestBody.put("model", model);
      requestBody.put("messages", messages);
      requestBody.put("max_tokens", getMaxTokens());
      if (streamBuffer != null) {
        requestBody.put("stream", true);
        if (AIDebug.enabled()) {
          requestBody.put("stream_options",
              new JSONObject().put("include_usage", true));
        }
      }
      if (toolDefs.length() > 0) {
        requestBody.put("tools", toolDefs);
      }
      decorateRequestBody(requestBody);
      if (AIDebug.enabled()) {
        AIDebug.log(LOG, getProviderName() + " continue request (iteration " + iteration + "):\n"
            + requestBody.toString(2));
      }

      JSONObject responseJson = callApi(requestBody, streamBuffer);
      if (AIDebug.enabled()) {
        AIDebug.log(LOG, getProviderName() + " continue response (iteration " + iteration + "):\n"
            + responseJson.toString(2));
        AIDebug.recordUsage(getProviderName(), model,
            TokenUsage.fromOpenAIChat(responseJson.optJSONObject("usage")));
      }

      JSONArray choices = responseJson.optJSONArray("choices");
      if (choices == null || choices.length() == 0) {
        return new LLMResponse("", new ArrayList<RawToolCall>(), null, false);
      }

      JSONObject choice = choices.getJSONObject(0);

      // Check for truncated response (output length exceeded)
      checkFinishReason(choice);

      JSONObject message = choice.optJSONObject("message");
      if (message == null) {
        return new LLMResponse("", new ArrayList<RawToolCall>(), null, false);
      }

      String textContent = message.optString("content", "");
      if (textContent == null) {
        textContent = "";
      }
      JSONArray toolCallsArr = message.optJSONArray("tool_calls");

      // No tool calls -> final response
      if (toolCallsArr == null || toolCallsArr.length() == 0) {
        return new LLMResponse(textContent, new ArrayList<RawToolCall>(), null, false);
      }

      // Classify tool calls
      List<ToolCallInfo> readOnlyCalls = new ArrayList<>();
      List<ToolCallInfo> operationCalls = new ArrayList<>();

      for (int i = 0; i < toolCallsArr.length(); i++) {
        JSONObject tc = toolCallsArr.getJSONObject(i);
        JSONObject function = tc.getJSONObject("function");
        String id = tc.getString("id");
        String name = function.getString("name");
        String argsJson = function.optString("arguments", "{}");

        ToolCallInfo info = new ToolCallInfo(id, name, argsJson);
        if (resolver != null && resolver.isReadOnly(name)) {
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
        messages.put(message);
        String contState = buildContinuationState(messages, operationCalls);
        return new LLMResponse(textContent, rawCalls, contState, true);
      }

      // Resolve read-only tools and continue the loop
      messages.put(message);
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
      for (ToolCallInfo info : operationCalls) {
        messages.put(new JSONObject()
            .put("role", "tool")
            .put("tool_call_id", info.id)
            .put("content", "This operation tool call has been queued for execution. "
                + "Please continue with any remaining read-only lookups you need."));
      }
    }

    throw new LLMProviderException(
        getProviderName() + " continuation tool-use loop exceeded " + MAX_TOOL_ITERATIONS
            + " iterations",
        "The AI took too many steps to process your request. Please try again.");
  }

  /**
   * Serializes the messages array and pending tool call IDs into a JSON
   * continuation state string.
   */
  private String buildContinuationState(JSONArray messages, List<ToolCallInfo> pendingCalls) {
    JSONObject state = new JSONObject();
    state.put("continuation", true);
    state.put("messages", messages);
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
   * Builds the messages array from system prompt, conversation history,
   * and the current user message.
   */
  private JSONArray buildMessages(String systemPrompt, List<ChatMessage> history,
      List<String> contextMessages, String userMessage) {
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
        if ("system".equals(msg.getRole())) {
          continue; // already handled above
        }
        if (msg.hasStructuredContent()) {
          if ("assistant".equals(msg.getRole())) {
            messages.put(buildStructuredAssistantMessage(msg));
          } else if ("tool_result".equals(msg.getRole())) {
            addStructuredToolResultMessages(messages, msg);
          } else {
            messages.put(new JSONObject()
                .put("role", msg.getRole())
                .put("content", msg.getText()));
          }
        } else {
          messages.put(new JSONObject()
              .put("role", msg.getRole())
              .put("content", msg.getText()));
        }
      }
    }

    // Per-request context as separate user/assistant turns
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

    // Current user message
    messages.put(new JSONObject()
        .put("role", "user")
        .put("content", userMessage));

    return messages;
  }

  /**
   * Translates a stored assistant message with structured content to the
   * OpenAI-compatible Chat Completions format (content + tool_calls array).
   */
  private JSONObject buildStructuredAssistantMessage(ChatMessage msg) {
    JSONArray parts = new JSONArray(msg.getStructuredContent());
    StringBuilder textContent = new StringBuilder();
    JSONArray toolCalls = new JSONArray();

    for (int i = 0; i < parts.length(); i++) {
      JSONObject part = parts.getJSONObject(i);
      String type = part.getString("type");
      if ("text".equals(type)) {
        textContent.append(part.getString("text"));
      } else if ("tool_use".equals(type)) {
        toolCalls.put(new JSONObject()
            .put("id", part.getString("id"))
            .put("type", "function")
            .put("function", new JSONObject()
                .put("name", part.getString("name"))
                .put("arguments", part.getJSONObject("input").toString())));
      }
    }

    JSONObject message = new JSONObject()
        .put("role", "assistant");
    if (textContent.length() > 0) {
      message.put("content", textContent.toString());
    } else {
      message.put("content", JSONObject.NULL);
    }
    if (toolCalls.length() > 0) {
      message.put("tool_calls", toolCalls);
    }
    return message;
  }

  /**
   * Translates a stored tool_result message to the OpenAI-compatible
   * format. Each tool result becomes a separate message with role "tool"
   * and the corresponding tool_call_id.
   */
  private void addStructuredToolResultMessages(JSONArray messages, ChatMessage msg) {
    JSONArray parts = new JSONArray(msg.getStructuredContent());
    for (int i = 0; i < parts.length(); i++) {
      JSONObject part = parts.getJSONObject(i);
      messages.put(new JSONObject()
          .put("role", "tool")
          .put("tool_call_id", part.optString("tool_use_id",
              part.optString("tool_call_id", "")))
          .put("content", part.getString("content")));
    }
  }

  /**
   * Checks the choice's finish_reason and throws if the response was truncated.
   * OpenAI-compatible APIs return {@code finish_reason: "length"} when
   * output is cut off by the token limit.
   */
  private void checkFinishReason(JSONObject choice) throws LLMProviderException {
    String finishReason = choice.optString("finish_reason", "");
    if ("length".equals(finishReason)) {
      LOG.warning(getProviderName() + " response truncated: finish_reason=length");
      throw new LLMProviderException(
          getProviderName() + " response truncated: finish_reason=length",
          "The AI response was too long and got cut off. "
              + "Please try a simpler request or break it into smaller steps.");
    }
  }

  /**
   * Translates generic {@link LLMTool} definitions into the OpenAI-compatible
   * function tool format.
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
          .put("function", new JSONObject()
              .put("name", tool.getName())
              .put("description", tool.getDescription())
              .put("parameters", parameters)));
    }
    return toolDefs;
  }

  /**
   * Makes an HTTP POST to the Chat Completions API with automatic
   * retry and exponential backoff for transient errors (429 rate-limit, 5xx).
   *
   * <p>When {@code streamBuffer} is non-null the request is expected to
   * include {@code "stream": true} and the response will be read as an
   * SSE event stream. Text deltas are pushed to the stream buffer in
   * real time and the full response JSON is reconstructed from the
   * accumulated deltas.
   */
  private JSONObject callApi(JSONObject requestBody, StreamBuffer streamBuffer)
      throws LLMProviderException {
    long backoffMs = INITIAL_BACKOFF_MS;
    LLMProviderException lastException = null;

    for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
      if (attempt > 0) {
        LOG.info(getProviderName() + " API retry attempt " + attempt + " after " + backoffMs
            + "ms backoff");
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
        URL url = new URL(getEndpoint());
        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
        conn.setReadTimeout(READ_TIMEOUT_MS);

        for (Map.Entry<String, String> entry : getHeaders().entrySet()) {
          conn.setRequestProperty(entry.getKey(), entry.getValue());
        }

        byte[] body = requestBody.toString().getBytes(StandardCharsets.UTF_8);
        conn.setRequestProperty("Content-Length", String.valueOf(body.length));

        try (OutputStream os = conn.getOutputStream()) {
          os.write(body);
          os.flush();
        }

        int statusCode = conn.getResponseCode();

        if (statusCode >= 200 && statusCode < 300 && streamBuffer != null) {
          return readStreamingResponse(conn, streamBuffer);
        }

        String responseText = readResponse(conn, statusCode);

        if (statusCode >= 200 && statusCode < 300) {
          return new JSONObject(responseText);
        }

        LOG.warning(getProviderName() + " API error (HTTP " + statusCode + "): " + responseText);

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
              getProviderName() + " API returned HTTP " + statusCode + ": " + responseText,
              mapHttpErrorToUserMessage(statusCode));
          continue;
        }

        String userMsg = mapHttpErrorToUserMessage(statusCode);
        throw new LLMProviderException(
            getProviderName() + " API returned HTTP " + statusCode + ": " + responseText,
            userMsg);

      } catch (IOException e) {
        if (attempt < MAX_RETRIES) {
          LOG.log(Level.WARNING,
              getProviderName() + " API connection error (attempt " + (attempt + 1) + ")", e);
          lastException = new LLMProviderException(
              "Failed to connect to " + getProviderName() + " API: " + e.getMessage(),
              "Could not reach the AI service. Please try again later.",
              e);
          continue;
        }
        LOG.log(Level.WARNING, getProviderName() + " API connection error (final attempt)", e);
        throw new LLMProviderException(
            "Failed to connect to " + getProviderName() + " API: " + e.getMessage(),
            "Could not reach the AI service. Please try again later.",
            e);
      } catch (JSONException e) {
        LOG.log(Level.WARNING, "Failed to parse " + getProviderName() + " API response", e);
        throw new LLMProviderException(
            "Invalid JSON response from " + getProviderName() + " API: " + e.getMessage(),
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
        getProviderName() + " API failed after " + MAX_RETRIES + " retries",
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
   * Reads an SSE streaming response from the Chat Completions API.
   *
   * <p>Uses the OpenAI-compatible SSE format for streaming:
   * <ul>
   *   <li>Each event line starts with {@code data: } followed by a JSON object</li>
   *   <li>Text tokens appear in {@code choices[0].delta.content}</li>
   *   <li>Tool calls appear in {@code choices[0].delta.tool_calls} and are
   *       accumulated across multiple chunks (arguments arrive incrementally)</li>
   *   <li>The final data event has {@code choices[0].finish_reason} set
   *       (e.g. "stop" or "tool_calls")</li>
   *   <li>The stream ends with the sentinel line {@code data: [DONE]}</li>
   * </ul>
   *
   * <p>Text deltas are pushed to the {@link StreamBuffer} in real time.
   * All deltas are accumulated to reconstruct a complete non-streaming-style
   * response JSON that can be processed by the existing parsing logic.
   *
   * @param conn          the open HTTP connection with an SSE response
   * @param streamBuffer  the buffer to push text deltas to
   * @return a reconstructed JSON object matching the non-streaming response format
   * @throws IOException if reading the response fails
   */
  private JSONObject readStreamingResponse(HttpURLConnection conn, StreamBuffer streamBuffer)
      throws IOException {
    // Accumulated state for reconstructing the full response
    String responseId = null;
    String responseModel = null;
    StringBuilder contentBuilder = new StringBuilder();
    String finishReason = null;
    JSONObject lastUsage = null;
    // Tool call accumulation: indexed by tool call index
    JSONArray accumulatedToolCalls = new JSONArray();

    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (streamBuffer.isCancelled()) {
          throw new StreamBuffer.CancelledException();
        }
        // Skip empty lines (SSE event separators)
        if (line.isEmpty()) {
          continue;
        }
        // Only process data lines
        if (!line.startsWith("data: ")) {
          continue;
        }
        String data = line.substring(6).trim();

        // Skip the [DONE] sentinel
        if ("[DONE]".equals(data)) {
          break;
        }

        JSONObject chunk;
        try {
          chunk = new JSONObject(data);
        } catch (JSONException e) {
          LOG.warning(getProviderName() + " streaming: failed to parse SSE data: " + data);
          continue;
        }

        // Capture top-level fields from the first chunk
        if (responseId == null) {
          responseId = chunk.optString("id", null);
        }
        if (responseModel == null) {
          responseModel = chunk.optString("model", null);
        }

        // Usage (when stream_options.include_usage = true) typically arrives
        // in the final chunk, which has an empty choices array.
        JSONObject chunkUsage = chunk.optJSONObject("usage");
        if (chunkUsage != null) {
          lastUsage = chunkUsage;
        }

        JSONArray choices = chunk.optJSONArray("choices");
        if (choices == null || choices.length() == 0) {
          continue;
        }

        JSONObject choice = choices.getJSONObject(0);

        // Capture finish_reason when it appears
        String fr = choice.optString("finish_reason", null);
        if (fr != null && !"null".equals(fr)) {
          finishReason = fr;
        }

        JSONObject delta = choice.optJSONObject("delta");
        if (delta == null) {
          continue;
        }

        // Accumulate text content
        String content = delta.optString("content", null);
        if (content != null && !"null".equals(content)) {
          contentBuilder.append(content);
          streamBuffer.appendText(content);
        }

        // Accumulate tool calls (they arrive incrementally across chunks)
        JSONArray deltaToolCalls = delta.optJSONArray("tool_calls");
        if (deltaToolCalls != null) {
          for (int i = 0; i < deltaToolCalls.length(); i++) {
            JSONObject dtc = deltaToolCalls.getJSONObject(i);
            int index = dtc.optInt("index", i);

            // Ensure we have a slot for this tool call index
            while (accumulatedToolCalls.length() <= index) {
              accumulatedToolCalls.put(JSONObject.NULL);
            }

            Object existing = accumulatedToolCalls.opt(index);
            if (existing == null || existing == JSONObject.NULL) {
              // First chunk for this tool call -- initialize it
              JSONObject tc = new JSONObject();
              tc.put("id", dtc.optString("id", ""));
              tc.put("type", dtc.optString("type", "function"));
              JSONObject fn = dtc.optJSONObject("function");
              if (fn != null) {
                tc.put("function", new JSONObject()
                    .put("name", fn.optString("name", ""))
                    .put("arguments", fn.optString("arguments", "")));
              } else {
                tc.put("function", new JSONObject()
                    .put("name", "")
                    .put("arguments", ""));
              }
              accumulatedToolCalls.put(index, tc);
            } else {
              // Subsequent chunk -- merge incremental data
              JSONObject tc = (JSONObject) existing;
              String id = dtc.optString("id", null);
              if (id != null && !id.isEmpty()) {
                tc.put("id", id);
              }
              JSONObject fn = dtc.optJSONObject("function");
              if (fn != null) {
                JSONObject existingFn = tc.getJSONObject("function");
                String name = fn.optString("name", null);
                if (name != null && !name.isEmpty()) {
                  existingFn.put("name", name);
                }
                String args = fn.optString("arguments", null);
                if (args != null) {
                  existingFn.put("arguments",
                      existingFn.optString("arguments", "") + args);
                }
              }
            }
          }
        }
      }
    } finally {
      streamBuffer.markDone();
    }

    // Reconstruct the full response in the same format as a non-streaming response
    JSONObject message = new JSONObject();
    message.put("role", "assistant");
    String fullContent = contentBuilder.toString();
    message.put("content", fullContent.isEmpty() ? JSONObject.NULL : fullContent);
    if (accumulatedToolCalls.length() > 0) {
      message.put("tool_calls", accumulatedToolCalls);
    }

    JSONObject choiceObj = new JSONObject();
    choiceObj.put("index", 0);
    choiceObj.put("message", message);
    choiceObj.put("finish_reason", finishReason != null ? finishReason : "stop");

    JSONObject response = new JSONObject();
    if (responseId != null) {
      response.put("id", responseId);
    }
    if (responseModel != null) {
      response.put("model", responseModel);
    }
    response.put("choices", new JSONArray().put(choiceObj));
    if (lastUsage != null) {
      response.put("usage", lastUsage);
    }

    return response;
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
   * Internal representation of a tool call. Includes the call ID
   * for correlating tool results with their corresponding calls.
   */
  protected static class ToolCallInfo {
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
