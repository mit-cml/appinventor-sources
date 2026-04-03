// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent.llm;

import java.util.Map;

/**
 * LLM provider for OpenRouter, a unified gateway that routes requests
 * to the best available backend for any model.
 *
 * <p>Thin subclass of {@link OpenAIChatCompletionsProvider} that sets
 * the OpenRouter endpoint and adds required routing headers.
 */
public class OpenRouterProvider extends OpenAIChatCompletionsProvider {

  private static final String OPENROUTER_BASE_URL = "https://openrouter.ai/api";

  OpenRouterProvider(String apiKey, String model) {
    super(apiKey, model, OPENROUTER_BASE_URL);
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
}
