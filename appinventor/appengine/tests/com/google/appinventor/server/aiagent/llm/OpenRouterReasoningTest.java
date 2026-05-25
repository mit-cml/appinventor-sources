// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent.llm;

import junit.framework.TestCase;
import org.json.JSONObject;

/**
 * Tests that {@link OpenRouterProvider#decorateRequestBody} injects the
 * OpenRouter {@code reasoning.effort} field when configured, and is a
 * no-op otherwise.
 */
public class OpenRouterReasoningTest extends TestCase {

  public void testNoReasoningWhenEffortEmpty() {
    OpenRouterProvider provider = new OpenRouterProvider("k", "m", "");
    JSONObject body = new JSONObject();
    provider.decorateRequestBody(body);
    assertFalse("body must not carry reasoning when effort empty",
        body.has("reasoning"));
  }

  public void testNoReasoningWhenEffortNull() {
    OpenRouterProvider provider = new OpenRouterProvider("k", "m", null);
    JSONObject body = new JSONObject();
    provider.decorateRequestBody(body);
    assertFalse(body.has("reasoning"));
  }

  public void testReasoningEffortInjected() {
    OpenRouterProvider provider =
        new OpenRouterProvider("k", "m", "high");
    JSONObject body = new JSONObject();
    provider.decorateRequestBody(body);
    assertTrue(body.has("reasoning"));
    JSONObject reasoning = body.getJSONObject("reasoning");
    assertEquals("high", reasoning.getString("effort"));
  }

  public void testReasoningEffortLowercasePreserved() {
    OpenRouterProvider provider =
        new OpenRouterProvider("k", "m", "low");
    JSONObject body = new JSONObject();
    provider.decorateRequestBody(body);
    assertEquals("low", body.getJSONObject("reasoning").getString("effort"));
  }
}
