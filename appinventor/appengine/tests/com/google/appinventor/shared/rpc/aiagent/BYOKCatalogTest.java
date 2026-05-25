// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.aiagent;

import java.util.List;
import junit.framework.TestCase;

/** Tests for {@link BYOKCatalog}. */
public class BYOKCatalogTest extends TestCase {

  public void testProviderWireNamesRoundTrip() {
    for (BYOKCatalog.Provider p : BYOKCatalog.Provider.values()) {
      assertEquals(p, BYOKCatalog.Provider.fromWireName(p.wireName()));
    }
  }

  public void testFromWireNameUnknownReturnsNull() {
    assertNull(BYOKCatalog.Provider.fromWireName("bedrock"));
    assertNull(BYOKCatalog.Provider.fromWireName(""));
    assertNull(BYOKCatalog.Provider.fromWireName(null));
  }

  public void testRequiresBaseUrlOnlyForCompatibleProviders() {
    assertTrue(BYOKCatalog.requiresBaseUrl(BYOKCatalog.Provider.ANTHROPIC_COMPATIBLE));
    assertTrue(BYOKCatalog.requiresBaseUrl(BYOKCatalog.Provider.OPENAI_COMPATIBLE));
    assertFalse(BYOKCatalog.requiresBaseUrl(BYOKCatalog.Provider.ANTHROPIC));
    assertFalse(BYOKCatalog.requiresBaseUrl(BYOKCatalog.Provider.OPENAI));
    assertFalse(BYOKCatalog.requiresBaseUrl(BYOKCatalog.Provider.GEMINI));
    assertFalse(BYOKCatalog.requiresBaseUrl(BYOKCatalog.Provider.MINIMAX));
    assertFalse(BYOKCatalog.requiresBaseUrl(BYOKCatalog.Provider.OPENROUTER));
  }

  public void testHasFreeTextModelOnlyForCompatibleProviders() {
    assertTrue(BYOKCatalog.hasFreeTextModel(BYOKCatalog.Provider.ANTHROPIC_COMPATIBLE));
    assertTrue(BYOKCatalog.hasFreeTextModel(BYOKCatalog.Provider.OPENAI_COMPATIBLE));
    assertFalse(BYOKCatalog.hasFreeTextModel(BYOKCatalog.Provider.ANTHROPIC));
  }

  public void testCuratedProvidersHaveNonEmptyModelLists() {
    for (BYOKCatalog.Provider p : BYOKCatalog.Provider.values()) {
      List<BYOKCatalog.ModelInfo> models = BYOKCatalog.models(p);
      assertNotNull("models() must never return null", models);
      if (BYOKCatalog.hasFreeTextModel(p)) {
        assertTrue("free-text providers should expose empty list",
            models.isEmpty());
      } else {
        assertFalse("curated providers must have at least one model: " + p,
            models.isEmpty());
      }
    }
  }

  public void testOpenAiNonReasoningModelsHaveNoOptions() {
    BYOKCatalog.ModelInfo m = BYOKCatalog.modelInfo(
        BYOKCatalog.Provider.OPENAI, "gpt-4o");
    assertNotNull(m);
    assertFalse(m.supportsReasoning());
    assertTrue(m.getReasoningOptions().isEmpty());
  }

  public void testOpenAiOSeriesHasLmhButNotXhigh() {
    BYOKCatalog.ModelInfo m = BYOKCatalog.modelInfo(
        BYOKCatalog.Provider.OPENAI, "o1");
    assertNotNull(m);
    assertTrue(m.supportsReasoning());
    List<String> opts = m.getReasoningOptions();
    assertTrue(opts.contains("low"));
    assertTrue(opts.contains("medium"));
    assertTrue(opts.contains("high"));
    assertFalse("o-series does not support xhigh", opts.contains("xhigh"));
  }

  public void testOpenAiGpt5XhighIsSupported() {
    BYOKCatalog.ModelInfo m = BYOKCatalog.modelInfo(
        BYOKCatalog.Provider.OPENAI, "gpt-5.4");
    assertNotNull(m);
    assertTrue(m.getReasoningOptions().contains("xhigh"));
  }

  public void testGeminiReasoningOptionsAreUppercase() {
    BYOKCatalog.ModelInfo m = BYOKCatalog.modelInfo(
        BYOKCatalog.Provider.GEMINI, "gemini-3.1-pro-preview");
    assertNotNull(m);
    List<String> opts = m.getReasoningOptions();
    assertTrue(opts.contains("LOW"));
    assertTrue(opts.contains("MEDIUM"));
    assertTrue(opts.contains("HIGH"));
  }

  public void testGeminiFlashSupportsMinimal() {
    BYOKCatalog.ModelInfo m = BYOKCatalog.modelInfo(
        BYOKCatalog.Provider.GEMINI, "gemini-3-flash-preview");
    assertNotNull(m);
    assertTrue(m.getReasoningOptions().contains("MINIMAL"));
  }

  public void testAnthropicReasoningOptionsAreLowercase() {
    BYOKCatalog.ModelInfo m = BYOKCatalog.modelInfo(
        BYOKCatalog.Provider.ANTHROPIC, "claude-sonnet-4-20250514");
    assertNotNull(m);
    List<String> opts = m.getReasoningOptions();
    assertEquals(3, opts.size());
    assertEquals("low", opts.get(0));
    assertEquals("high", opts.get(2));
  }

  public void testGemini25HasNoReasoningInCurrentServer() {
    // Gemini 2.5 uses thinkingBudget (integer) which the server does not
    // currently expose, so reasoning options are intentionally empty.
    BYOKCatalog.ModelInfo m = BYOKCatalog.modelInfo(
        BYOKCatalog.Provider.GEMINI, "gemini-2.5-pro");
    assertNotNull(m);
    assertFalse(m.supportsReasoning());
  }

  public void testReasoningOptionsForUnknownModelIsEmpty() {
    assertTrue(BYOKCatalog.reasoningOptions(
        BYOKCatalog.Provider.OPENAI, "no-such-model").isEmpty());
    assertTrue(BYOKCatalog.reasoningOptions(
        BYOKCatalog.Provider.OPENAI_COMPATIBLE, "anything").isEmpty());
  }
}
