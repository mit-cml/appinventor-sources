// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent.llm;

import java.util.Map;

import org.json.JSONObject;

/**
 * LLM provider for OpenRouter, a unified gateway that routes requests
 * to the best available backend for any model.
 *
 * <p>Thin subclass of {@link OpenAIChatCompletionsProvider} that sets
 * the OpenRouter endpoint, adds required routing headers, and forwards
 * {@code reasoning.effort} so OpenRouter can translate it to the right
 * underlying parameter per model family (OpenAI {@code reasoning.effort},
 * Anthropic {@code thinking}, Gemini {@code thinkingConfig}, etc.).
 */
public class OpenRouterProvider extends OpenAIChatCompletionsProvider {

  private static final String OPENROUTER_BASE_URL = "https://openrouter.ai/api";

  private final String reasoningEffort;

  OpenRouterProvider(String apiKey, String model, String reasoningEffort) {
    super(apiKey, model, OPENROUTER_BASE_URL);
    this.reasoningEffort = reasoningEffort == null ? "" : reasoningEffort;
  }

  @Override
  protected String getProviderName() {
    return "OpenRouter";
  }

  @Override
  protected Map<String, String> getHeaders() {
    Map<String, String> headers = super.getHeaders();
    headers.put("HTTP-Referer", "https://appinventor.mit.edu");
    headers.put("X-Title", "MIT App Inventor");
    return headers;
  }

  @Override
  protected void decorateRequestBody(JSONObject requestBody) {
    if (!reasoningEffort.isEmpty()) {
      requestBody.put("reasoning",
          new JSONObject().put("effort", reasoningEffort));
    }
  }
}
