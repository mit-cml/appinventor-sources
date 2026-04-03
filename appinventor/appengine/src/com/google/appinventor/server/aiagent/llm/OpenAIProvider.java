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
  private static final int MAX_RETRIES = 3;
  private static final long INITIAL_BACKOFF_MS = 1000;
  private static final int MAX_TOKENS = 128000;
  private static final int CONNECT_TIMEOUT_MS = 30000;
  private static final int READ_TIMEOUT_MS = 600000;

  private final String apiKey;
  private final String model;
  private final String reasoningEffort;

  /**
   * Creates a new OpenAI provider.
   *
   * @param apiKey          the OpenAI API key
   * @param model           the model name (e.g. "gpt-4o")
   * @param reasoningEffort reasoning effort level (e.g. "low", "medium", "high"),
   *                        or empty/null to use the model's default
   */
  OpenAIProvider(String apiKey, String model, String reasoningEffort) {
    this.apiKey = apiKey;
    this.model = model;
    this.reasoningEffort = reasoningEffort;
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

    JSONArray toolDefs = buildToolDefinitions(tools);
    String responseId = extractResponseId(providerRef);

    // Internal tool-use loop
    // On the first iteration, input is the user message string — unless the
    // previous response had unresolved tool calls (continuation state), in
    // which case we must submit function_call_output items alongside the new
    // user message to satisfy the Responses API contract.
    // On subsequent iterations, input is a tool results array and we chain
    // via previous_response_id.
    Object currentInput;
    JSONArray pendingCalls = extractPendingToolCalls(providerRef);
    boolean hasContext = contextMessages != null && !contextMessages.isEmpty();
    if (pendingCalls != null && pendingCalls.length() > 0) {
      JSONArray inputArray = new JSONArray();
      // chat() is called for follow-up messages (new question, user rejection,
      // error retry, etc.) — we don't know if the client applied the previous
      // operations.  Use "Not yet applied." for ACCEPTED ops and the real
      // rejection message for server-rejected ops.  Only
      // continueWithToolResults() (the explicit success path) sends "Done.".
      JSONArray prevToolCallResults = null;
      if (providerRef != null) {
        try {
          JSONObject refObj = new JSONObject(providerRef);
          prevToolCallResults = refObj.optJSONArray("toolCallResults");
        } catch (JSONException ignored) {
          // Not JSON or no toolCallResults
        }
      }
      for (int i = 0; i < pendingCalls.length(); i++) {
        JSONObject pc = pendingCalls.getJSONObject(i);
        String resultContent = "Not yet applied.";
        if (prevToolCallResults != null && i < prevToolCallResults.length()) {
          String annotated = prevToolCallResults.getJSONObject(i)
              .optString("result", "Not yet applied.");
          // Preserve rejection info, but don't claim "Done." for accepted ops
          // since we don't know if the client applied them in this code path.
          if (annotated.startsWith("REJECTED:")) {
            resultContent = annotated;
          }
        }
        inputArray.put(new JSONObject()
            .put("type", "function_call_output")
            .put("call_id", pc.getString("id"))
            .put("output", resultContent));
      }
      if (hasContext) {
        for (String ctx : contextMessages) {
          if (ctx != null && !ctx.isEmpty()) {
            inputArray.put(new JSONObject()
                .put("role", "user")
                .put("content", ctx));
          }
        }
      }
      inputArray.put(new JSONObject()
          .put("role", "user")
          .put("content", userMessage));
      currentInput = inputArray;
    } else if (hasContext) {
      JSONArray inputArray = new JSONArray();
      for (String ctx : contextMessages) {
        if (ctx != null && !ctx.isEmpty()) {
          inputArray.put(new JSONObject()
              .put("role", "user")
              .put("content", ctx));
        }
      }
      inputArray.put(new JSONObject()
          .put("role", "user")
          .put("content", userMessage));
      currentInput = inputArray;
    } else {
      currentInput = userMessage;
    }

    for (int iteration = 0; iteration < MAX_TOOL_ITERATIONS; iteration++) {
      JSONObject requestBody = new JSONObject();
      requestBody.put("model", model);
      requestBody.put("max_output_tokens", MAX_TOKENS);
      requestBody.put("truncation", "auto");

      if (reasoningEffort != null && !reasoningEffort.isEmpty()) {
        requestBody.put("reasoning", new JSONObject()
            .put("effort", reasoningEffort)
            .put("summary", "auto"));
      }

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
      // Only stream on the final-looking iterations (first or text-only);
      // pass streamBuffer so callApi can enable SSE when appropriate.
      JSONObject responseJson = callApi(requestBody, streamBuffer);
      if (AIDebug.enabled()) {
        AIDebug.log(LOG, "OpenAI response (iteration " + iteration + "):\n"
            + responseJson.toString(2));
      }

      // Check for incomplete response (e.g. max_output_tokens exceeded)
      checkResponseStatus(responseJson);

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
      ReadOnlyToolResolver resolver, StreamBuffer streamBuffer) throws LLMProviderException {

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

    // Build tool result input items for each pending tool call.
    // Use per-call results when available (annotated by AIAgentEngine) instead
    // of blanket "Done." so the LLM knows which tool calls were rejected.
    JSONArray toolCallResults = state.optJSONArray("toolCallResults");
    JSONArray toolResultsInput = new JSONArray();
    for (int i = 0; i < pendingToolCalls.length(); i++) {
      JSONObject pending = pendingToolCalls.getJSONObject(i);
      String resultContent = "Done.";
      if (toolCallResults != null && i < toolCallResults.length()) {
        resultContent = toolCallResults.getJSONObject(i).optString("result", "Done.");
      }
      toolResultsInput.put(new JSONObject()
          .put("type", "function_call_output")
          .put("call_id", pending.getString("id"))
          .put("output", resultContent));
    }

    JSONArray toolDefs = buildToolDefinitions(tools);

    // Run the same tool-use loop as chat()
    Object currentInput = toolResultsInput;

    for (int iteration = 0; iteration < MAX_TOOL_ITERATIONS; iteration++) {
      JSONObject requestBody = new JSONObject();
      requestBody.put("model", model);
      requestBody.put("max_output_tokens", MAX_TOKENS);
      requestBody.put("truncation", "auto");

      if (reasoningEffort != null && !reasoningEffort.isEmpty()) {
        requestBody.put("reasoning", new JSONObject()
            .put("effort", reasoningEffort)
            .put("summary", "auto"));
      }

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
      JSONObject responseJson = callApi(requestBody, streamBuffer);
      if (AIDebug.enabled()) {
        AIDebug.log(LOG, "OpenAI continue response (iteration " + iteration + "):\n"
            + responseJson.toString(2));
      }

      // Check for incomplete response (e.g. max_output_tokens exceeded)
      checkResponseStatus(responseJson);

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
   * Extracts pending tool calls from a continuation-state providerRef.
   *
   * @return the pending tool calls array, or null if providerRef is not a
   *         continuation state or has no pending calls
   */
  private JSONArray extractPendingToolCalls(String providerRef) {
    if (providerRef == null || providerRef.isEmpty()) {
      return null;
    }
    try {
      JSONObject ref = new JSONObject(providerRef);
      if (ref.optBoolean("continuation", false) && ref.has("pendingToolCalls")) {
        return ref.getJSONArray("pendingToolCalls");
      }
    } catch (JSONException e) {
      // Not a continuation state
    }
    return null;
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
   * Checks the response status and throws if the response is incomplete.
   * The OpenAI Responses API sets {@code status} to {@code "incomplete"}
   * when the output is truncated (e.g. max_output_tokens exceeded).
   */
  private void checkResponseStatus(JSONObject responseJson) throws LLMProviderException {
    String status = responseJson.optString("status", "completed");
    if ("incomplete".equals(status)) {
      String reason = "";
      JSONObject details = responseJson.optJSONObject("incomplete_details");
      if (details != null) {
        reason = details.optString("reason", "unknown");
      }
      LOG.warning("OpenAI response incomplete: " + reason);
      if ("max_output_tokens".equals(reason)) {
        throw new LLMProviderException(
            "OpenAI response truncated: incomplete (reason=" + reason + ")",
            "The AI response was too long and got cut off. "
                + "Please try a simpler request or break it into smaller steps.");
      }
      throw new LLMProviderException(
          "OpenAI response incomplete: " + reason,
          "The AI response was incomplete. Please try again.");
    }
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
   * Makes an HTTP POST to the OpenAI Responses API with automatic retry
   * and exponential backoff for transient errors (429 rate-limit, 5xx).
   *
   * <p>When {@code streamBuffer} is non-null, streaming is enabled: the
   * request includes {@code "stream": true} and the response is read as
   * Server-Sent Events (SSE). Text deltas are pushed to the buffer in
   * real time, and the full response JSON (from the {@code response.completed}
   * event) is returned for normal parsing.
   *
   * @param requestBody  the JSON request body
   * @param streamBuffer optional streaming buffer; may be null
   */
  private JSONObject callApi(JSONObject requestBody, StreamBuffer streamBuffer)
      throws LLMProviderException {
    boolean streaming = streamBuffer != null;
    if (streaming) {
      requestBody.put("stream", true);
    }

    long backoffMs = INITIAL_BACKOFF_MS;
    LLMProviderException lastException = null;

    for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
      if (attempt > 0) {
        LOG.info("OpenAI API retry attempt " + attempt + " after " + backoffMs + "ms backoff");
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
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);

        byte[] body = requestBody.toString().getBytes(StandardCharsets.UTF_8);
        conn.setRequestProperty("Content-Length", String.valueOf(body.length));

        try (OutputStream os = conn.getOutputStream()) {
          os.write(body);
          os.flush();
        }

        int statusCode = conn.getResponseCode();

        if (statusCode >= 200 && statusCode < 300) {
          if (streaming) {
            String fullResponse = readStreamingResponse(conn, streamBuffer);
            return new JSONObject(fullResponse);
          } else {
            String responseText = readResponse(conn, statusCode);
            return new JSONObject(responseText);
          }
        }

        // Error path — always read the full (non-streaming) error body
        String responseText = readResponse(conn, statusCode);
        LOG.warning("OpenAI API error (HTTP " + statusCode + "): " + responseText);

        // Retry on 429 (rate limit) and 5xx (server errors)
        if (isRetryable(statusCode) && attempt < MAX_RETRIES) {
          // Respect Retry-After header if present
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
              "OpenAI API returned HTTP " + statusCode + ": " + responseText,
              mapHttpErrorToUserMessage(statusCode));
          continue;
        }

        String userMsg = mapHttpErrorToUserMessage(statusCode);
        throw new LLMProviderException(
            "OpenAI API returned HTTP " + statusCode + ": " + responseText,
            userMsg);

      } catch (IOException e) {
        // Retry connection errors
        if (attempt < MAX_RETRIES) {
          LOG.log(Level.WARNING, "OpenAI API connection error (attempt " + (attempt + 1) + ")", e);
          lastException = new LLMProviderException(
              "Failed to connect to OpenAI API: " + e.getMessage(),
              "Could not reach the AI service. Please try again later.",
              e);
          continue;
        }
        LOG.log(Level.WARNING, "OpenAI API connection error (final attempt)", e);
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

    // Should not reach here, but just in case
    if (lastException != null) {
      throw lastException;
    }
    throw new LLMProviderException(
        "OpenAI API failed after " + MAX_RETRIES + " retries",
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
   * Reads a streaming (SSE) response from the OpenAI Responses API.
   *
   * <p>The SSE stream consists of lines in the format:
   * <pre>
   *   event: &lt;type&gt;
   *   data: &lt;json&gt;
   * </pre>
   *
   * <p>Key event types handled:
   * <ul>
   *   <li>{@code response.output_text.delta} — text token delta; streamed to
   *       the buffer via {@link StreamBuffer#appendText(String)}</li>
   *   <li>{@code response.completed} — contains the full response object;
   *       extracted and returned so the caller can parse it normally</li>
   *   <li>{@code response.done} — end-of-stream signal</li>
   * </ul>
   *
   * <p>The {@code [DONE]} sentinel that may appear as the last data line is
   * safely skipped.
   *
   * @param conn         the HTTP connection with an active SSE stream
   * @param streamBuffer the buffer to push text deltas into
   * @return the full response JSON string from the {@code response.completed} event
   * @throws IOException if reading the stream fails
   * @throws LLMProviderException if the stream ends without a completed response
   */
  private String readStreamingResponse(HttpURLConnection conn, StreamBuffer streamBuffer)
      throws IOException, LLMProviderException {
    BufferedReader reader = new BufferedReader(
        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));

    String fullResponseJson = null;
    String currentEventType = null;
    String line;

    try {
      while ((line = reader.readLine()) != null) {
        // SSE blank line marks the end of an event block
        if (line.isEmpty()) {
          currentEventType = null;
          continue;
        }

        // Parse event type
        if (line.startsWith("event: ")) {
          currentEventType = line.substring(7).trim();
          continue;
        }

        // Parse data payload
        if (line.startsWith("data: ")) {
          String data = line.substring(6);

          // Handle [DONE] sentinel
          if ("[DONE]".equals(data.trim())) {
            break;
          }

          if (currentEventType == null) {
            continue;
          }

          try {
            if ("response.output_text.delta".equals(currentEventType)) {
              JSONObject deltaObj = new JSONObject(data);
              String delta = deltaObj.optString("delta", "");
              if (!delta.isEmpty()) {
                streamBuffer.appendText(delta);
              }
            } else if ("response.reasoning.delta".equals(currentEventType)) {
              JSONObject deltaObj = new JSONObject(data);
              String delta = deltaObj.optString("delta", "");
              if (!delta.isEmpty()) {
                streamBuffer.appendThinking(delta);
              }
            } else if ("response.completed".equals(currentEventType)) {
              // The data payload for response.completed is the full response object
              JSONObject completedWrapper = new JSONObject(data);
              JSONObject response = completedWrapper.optJSONObject("response");
              if (response != null) {
                fullResponseJson = response.toString();
              } else {
                // Some API versions put the response at the top level
                fullResponseJson = data;
              }
            }
            // Other event types (response.output_item.added,
            // response.function_call_arguments.delta, response.output_item.done,
            // response.done, etc.) are intentionally ignored — we don't need to
            // stream tool call arguments, and the response.completed event
            // provides the full response for parsing.
          } catch (JSONException e) {
            LOG.log(Level.FINE, "Failed to parse SSE data for event '"
                + currentEventType + "': " + data, e);
          }
        }
      }
    } finally {
      reader.close();
      streamBuffer.markDone();
    }

    if (fullResponseJson == null) {
      throw new LLMProviderException(
          "OpenAI streaming response ended without a response.completed event",
          "The AI response stream ended unexpectedly. Please try again.");
    }

    return fullResponseJson;
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
