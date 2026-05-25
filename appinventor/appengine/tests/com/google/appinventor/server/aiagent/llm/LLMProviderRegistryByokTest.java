// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent.llm;

import junit.framework.TestCase;

/**
 * Tests the BYOK-aware overload {@link LLMProviderRegistry#get(String, BYOKConfig)}.
 * The legacy {@code get(String)} path is unchanged and not retested here.
 */
public class LLMProviderRegistryByokTest extends TestCase {

  public void testByokAnthropicReturnsAnthropicProvider()
      throws LLMProviderException {
    BYOKConfig byok = new BYOKConfig(
        "anthropic", "claude-sonnet-4-20250514", "sk-ant-test",
        "", "");
    LLMProvider provider = LLMProviderRegistry.get("anthropic", byok);
    assertNotNull(provider);
    assertTrue(provider instanceof AnthropicCompatibleProvider);
  }

  public void testByokOpenAiReturnsOpenAIProvider() throws LLMProviderException {
    BYOKConfig byok = new BYOKConfig("openai", "gpt-4o", "sk-test", "", "low");
    LLMProvider provider = LLMProviderRegistry.get("openai", byok);
    assertTrue(provider instanceof OpenAIProvider);
  }

  public void testByokAnthropicCompatibleRequiresBaseUrl() {
    BYOKConfig byok = new BYOKConfig(
        "anthropic-compatible", "some-model", "key", "", "");
    try {
      LLMProviderRegistry.get("anthropic-compatible", byok);
      fail("should have thrown for missing base URL");
    } catch (LLMProviderException expected) {
      // ok
    }
  }

  public void testByokOpenAiCompatibleAcceptsBaseUrl()
      throws LLMProviderException {
    BYOKConfig byok = new BYOKConfig(
        "openai-compatible", "some-model", "key",
        "https://example.com/v1", "");
    LLMProvider provider = LLMProviderRegistry.get("openai-compatible", byok);
    assertNotNull(provider);
    assertTrue(provider instanceof OpenAIChatCompletionsProvider);
  }

  public void testByokEmptyKeyThrows() {
    BYOKConfig byok = new BYOKConfig(
        "anthropic", "claude-sonnet-4-20250514", "", "", "");
    try {
      LLMProviderRegistry.get("anthropic", byok);
      fail("should have thrown for empty API key");
    } catch (LLMProviderException expected) {
      // ok
    }
  }

  public void testByokConfigRedactsKeyInToString() {
    BYOKConfig byok = new BYOKConfig(
        "openai", "gpt-4o", "sk-very-secret", "", "high");
    String s = byok.toString();
    assertFalse("toString must not contain the API key",
        s.contains("sk-very-secret"));
    assertTrue("toString must mark redaction", s.contains("***"));
  }
}
