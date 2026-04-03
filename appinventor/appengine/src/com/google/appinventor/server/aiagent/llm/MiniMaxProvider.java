// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent.llm;

/**
 * LLM provider for the MiniMax Chat Completions API.
 *
 * <p>Thin subclass of {@link OpenAIChatCompletionsProvider} that sets
 * the MiniMax endpoint.
 */
public class MiniMaxProvider extends OpenAIChatCompletionsProvider {

  private static final String MINIMAX_BASE_URL = "https://api.minimax.io";

  MiniMaxProvider(String apiKey, String model) {
    super(apiKey, model, MINIMAX_BASE_URL);
  }

  @Override
  protected String getProviderName() {
    return "MiniMax";
  }
}
