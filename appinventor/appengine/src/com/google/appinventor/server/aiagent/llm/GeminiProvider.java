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
 * LLM provider implementation for the Google Gemini (Generative Language) API.
 *
 * <p>This is a stateful provider: the {@code providerRef} is used to store
 * a serialized interaction context so the API can continue conversations.
 * It uses the {@code generateContent} endpoint with function calling and
 * implements an internal tool-use loop for read-only tools (up to
 * {@value #MAX_TOOL_ITERATIONS} iterations).
 */
public class GeminiProvider implements LLMProvider {

  private static final Logger LOG = Logger.getLogger(GeminiProvider.class.getName());

  private static final String API_BASE =
      "https://generativelanguage.googleapis.com/v1beta/models/";
  private static final int MAX_TOOL_ITERATIONS = 5;
  private static final int MAX_OUTPUT_TOKENS = 4096;
  private static final int CONNECT_TIMEOUT_MS = 30000;
  private static final int READ_TIMEOUT_MS = 120000;

  private final String apiKey;
  private final String model;

  /**
   * Creates a new Gemini provider.
   *
   * @param apiKey the Gemini API key
   * @param model  the model name (e.g. "gemini-2.0-flash")
   */
  GeminiProvider(String apiKey, String model) {
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

    // Build the contents array from history + current user message
    JSONArray contents = buildContents(history, userMessage);
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
          LOG.warning("Failed to parse Gemini providerRef, ignoring: " + e.getMessage());
        }
      }

      if (AIDebug.enabled()) {
        AIDebug.log(LOG, "Gemini request (iteration " + iteration + "):\n"
            + requestBody.toString(2));
      }
      JSONObject responseJson = callApi(requestBody);
      if (AIDebug.enabled()) {
        AIDebug.log(LOG, "Gemini response (iteration " + iteration + "):\n"
            + responseJson.toString(2));
      }

      // Parse candidates
      JSONArray candidates = responseJson.optJSONArray("candidates");
      if (candidates == null || candidates.length() == 0) {
        return new LLMResponse("", new ArrayList<RawToolCall>(), null);
      }

      JSONObject candidate = candidates.getJSONObject(0);
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
        return new LLMResponse(textBuilder.toString(), rawCalls, newProviderRef);
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
        AIDebug.log(LOG, "Gemini tool-use loop iteration " + (iteration + 1)
            + ": readOnly=" + readOnlyCalls.size()
            + ", operations=" + operationCalls.size());
        for (FunctionCallInfo fc : readOnlyCalls) {
          AIDebug.log(LOG, "  read-only: " + fc.name);
        }
        for (FunctionCallInfo fc : operationCalls) {
          AIDebug.log(LOG, "  operation: " + fc.name);
        }
      } else {
        LOG.info("Gemini tool-use loop iteration " + (iteration + 1)
            + ": resolved " + readOnlyCalls.size() + " read-only tools, "
            + operationCalls.size() + " operation tools pending");
      }
    }

    // Safety limit reached
    throw new LLMProviderException(
        "Gemini tool-use loop exceeded " + MAX_TOOL_ITERATIONS + " iterations",
        "The AI took too many steps to process your request. Please try again.");
  }

  /**
   * Builds the contents array from conversation history and the current
   * user message.
   */
  private JSONArray buildContents(List<ChatMessage> history, String userMessage) {
    JSONArray contents = new JSONArray();
    if (history != null) {
      for (ChatMessage msg : history) {
        String role = msg.getRole();
        // Gemini uses "user" and "model" roles
        if ("system".equals(role)) {
          continue; // handled via systemInstruction
        }
        String geminiRole = "assistant".equals(role) ? "model" : role;
        JSONArray parts = new JSONArray();
        parts.put(new JSONObject().put("text", msg.getText()));
        contents.put(new JSONObject()
            .put("role", geminiRole)
            .put("parts", parts));
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
   * Translates generic {@link LLMTool} definitions into Gemini function
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
      LOG.warning("Failed to build Gemini providerRef: " + e.getMessage());
      return null;
    }
  }

  /**
   * Makes an HTTP POST to the Gemini generateContent API.
   */
  private JSONObject callApi(JSONObject requestBody) throws LLMProviderException {
    HttpURLConnection conn = null;
    try {
      String endpoint = API_BASE + model + ":generateContent?key=" + apiKey;
      URL url = new URL(endpoint);
      conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("POST");
      conn.setDoOutput(true);
      conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
      conn.setReadTimeout(READ_TIMEOUT_MS);

      conn.setRequestProperty("Content-Type", "application/json");

      byte[] body = requestBody.toString().getBytes(StandardCharsets.UTF_8);
      conn.setRequestProperty("Content-Length", String.valueOf(body.length));

      try (OutputStream os = conn.getOutputStream()) {
        os.write(body);
        os.flush();
      }

      int statusCode = conn.getResponseCode();
      String responseText = readResponse(conn, statusCode);

      if (statusCode < 200 || statusCode >= 300) {
        LOG.warning("Gemini API error (HTTP " + statusCode + "): " + responseText);
        String userMsg = mapHttpErrorToUserMessage(statusCode);
        throw new LLMProviderException(
            "Gemini API returned HTTP " + statusCode + ": " + responseText,
            userMsg);
      }

      return new JSONObject(responseText);

    } catch (IOException e) {
      LOG.log(Level.WARNING, "Gemini API connection error", e);
      throw new LLMProviderException(
          "Failed to connect to Gemini API: " + e.getMessage(),
          "Could not reach the AI service. Please try again later.",
          e);
    } catch (JSONException e) {
      LOG.log(Level.WARNING, "Failed to parse Gemini API response", e);
      throw new LLMProviderException(
          "Invalid JSON response from Gemini API: " + e.getMessage(),
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
      case 403:
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
   * Internal representation of a Gemini function call.
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
