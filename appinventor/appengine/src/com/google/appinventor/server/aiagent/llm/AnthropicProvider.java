// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent.llm;

/**
 * LLM provider for the Anthropic Messages API (Claude).
 *
 * <p>Thin subclass of {@link AnthropicCompatibleProvider} that sets
 * the Anthropic API endpoint.
 */
public class AnthropicProvider extends AnthropicCompatibleProvider {

  AnthropicProvider(String apiKey, String model) {
    super(apiKey, model, "https://api.anthropic.com");
  }

  @Override
  protected String getProviderName() {
    return "Anthropic";
  }
}
