// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent;

import com.google.appinventor.server.aiagent.llm.BYOKConfig;
import junit.framework.TestCase;

public class BYOKResolverTest extends TestCase {

  // ---- happy path ----

  public void testCompleteAnthropicReturnsConfig() {
    String json = settings("anthropic", "claude-sonnet-4-20250514",
        "sk-ant-x", "", "high");
    BYOKConfig out = BYOKResolver.resolveFromJson(json);
    assertNotNull(out);
    assertEquals("anthropic", out.getProvider());
    assertEquals("claude-sonnet-4-20250514", out.getModel());
    assertEquals("sk-ant-x", out.getApiKey());
    assertEquals("high", out.getReasoningEffort());
  }

  public void testCompleteCompatibleWithBaseUrlReturnsConfig() {
    String json = settings("openai-compatible", "my-model", "key",
        "https://example.com/v1", "");
    BYOKConfig out = BYOKResolver.resolveFromJson(json);
    assertNotNull(out);
    assertEquals("https://example.com/v1", out.getBaseUrl());
  }

  // ---- all-or-nothing ----

  public void testMissingProviderReturnsNull() {
    assertNull(BYOKResolver.resolveFromJson(
        settings("", "claude-sonnet-4-20250514", "sk-x", "", "")));
  }

  public void testMissingModelReturnsNull() {
    assertNull(BYOKResolver.resolveFromJson(
        settings("anthropic", "", "sk-x", "", "")));
  }

  public void testMissingApiKeyReturnsNull() {
    assertNull(BYOKResolver.resolveFromJson(
        settings("anthropic", "claude-sonnet-4-20250514", "", "", "")));
  }

  public void testCompatibleProviderMissingBaseUrlReturnsNull() {
    assertNull(BYOKResolver.resolveFromJson(
        settings("openai-compatible", "my-model", "key", "", "")));
  }

  public void testNonCompatibleProviderIgnoresBaseUrl() {
    BYOKConfig out = BYOKResolver.resolveFromJson(
        settings("openai", "gpt-4o", "sk-x", "ignored", ""));
    assertNotNull(out);
  }

  // ---- catalog membership ----

  public void testProviderNotInCatalogReturnsNull() {
    // 'bedrock' is supported server-side but not in the BYOK catalog.
    assertNull(BYOKResolver.resolveFromJson(
        settings("bedrock", "any-model", "key", "", "")));
  }

  // ---- reasoning sanitization (provider-native values stored verbatim) ----

  public void testReasoningPassedThroughForReasoningModel() {
    BYOKConfig out = BYOKResolver.resolveFromJson(
        settings("anthropic", "claude-sonnet-4-20250514", "sk-x", "", "high"));
    assertNotNull(out);
    assertEquals("high", out.getReasoningEffort());
  }

  public void testReasoningPassedThroughForGeminiNativeUppercase() {
    BYOKConfig out = BYOKResolver.resolveFromJson(
        settings("gemini", "gemini-3.1-pro-preview", "sk-x", "", "MEDIUM"));
    assertNotNull(out);
    assertEquals("MEDIUM", out.getReasoningEffort());
  }

  public void testReasoningPassedThroughForOpenAiXhigh() {
    BYOKConfig out = BYOKResolver.resolveFromJson(
        settings("openai", "gpt-5.4", "sk-x", "", "xhigh"));
    assertNotNull(out);
    assertEquals("xhigh", out.getReasoningEffort());
  }

  public void testReasoningEmptyStoredAsEmpty() {
    BYOKConfig out = BYOKResolver.resolveFromJson(
        settings("openai", "gpt-4o", "sk-x", "", ""));
    assertNotNull(out);
    assertEquals("", out.getReasoningEffort());
  }

  public void testReasoningStrippedWhenModelDoesNotSupport() {
    // gpt-4o does not accept reasoning effort.
    BYOKConfig out = BYOKResolver.resolveFromJson(
        settings("openai", "gpt-4o", "sk-x", "", "high"));
    assertNotNull(out);
    assertEquals("", out.getReasoningEffort());
  }

  public void testReasoningStrippedWhenStaleValue() {
    // 'low' is not a Gemini-native value (Gemini uses LOW/MEDIUM/HIGH).
    BYOKConfig out = BYOKResolver.resolveFromJson(
        settings("gemini", "gemini-3.1-pro-preview", "sk-x", "", "low"));
    assertNotNull(out);
    assertEquals("", out.getReasoningEffort());
  }

  public void testReasoningStrippedForCompatibleProvider() {
    // Free-text model on a compatible provider has no catalog entry, so
    // the resolver cannot validate reasoning options and drops them.
    BYOKConfig out = BYOKResolver.resolveFromJson(
        settings("openai-compatible", "my-model", "k",
            "https://example.com/v1", "high"));
    assertNotNull(out);
    assertEquals("", out.getReasoningEffort());
  }

  // ---- malformed JSON ----

  public void testNullJsonReturnsNull() {
    assertNull(BYOKResolver.resolveFromJson(null));
  }

  public void testEmptyJsonReturnsNull() {
    assertNull(BYOKResolver.resolveFromJson(""));
  }

  public void testGarbageJsonReturnsNull() {
    assertNull(BYOKResolver.resolveFromJson("not-json"));
  }

  // ---- helper ----

  private static String settings(String provider, String model, String apiKey,
                                 String baseUrl, String reasoning) {
    return "{\"GeneralSettings\":{"
        + "\"AIAgentBYOKProvider\":\"" + provider + "\","
        + "\"AIAgentBYOKModel\":\"" + model + "\","
        + "\"AIAgentBYOKApiKey\":\"" + apiKey + "\","
        + "\"AIAgentBYOKBaseUrl\":\"" + baseUrl + "\","
        + "\"AIAgentBYOKReasoning\":\"" + reasoning + "\""
        + "}}";
  }
}
