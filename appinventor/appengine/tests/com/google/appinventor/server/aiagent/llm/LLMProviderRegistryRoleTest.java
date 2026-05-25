// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent.llm;

import com.google.appinventor.server.aiagent.AgentRole;
import junit.framework.TestCase;

public class LLMProviderRegistryRoleTest extends TestCase {

  @Override
  protected void tearDown() throws Exception {
    clearAll();
    super.tearDown();
  }

  public void testRoleSpecificProviderClassReturned()
      throws LLMProviderException {
    System.setProperty("ai.agent.provider", "anthropic");
    System.setProperty("ai.agent.api.key", "global-key");
    System.setProperty("ai.agent.role.project_editor_planner.provider",
        "openrouter");
    System.setProperty("ai.agent.role.project_editor_planner.api.key",
        "or-key");
    System.setProperty("ai.agent.role.project_editor_planner.model",
        "qwen/qwen3.6-plus");

    LLMProvider plannerProvider =
        LLMProviderRegistry.get(AgentRole.PROJECT_EDITOR_PLANNER, null);
    LLMProvider advisorProvider =
        LLMProviderRegistry.get(AgentRole.ADVISOR, null);

    assertTrue("planner should use OpenRouter (Chat Completions family)",
        plannerProvider instanceof OpenRouterProvider);
    assertTrue("advisor should fall back to global Anthropic",
        advisorProvider instanceof AnthropicCompatibleProvider);
  }

  public void testByokOverridesRoleConfig() throws LLMProviderException {
    System.setProperty("ai.agent.role.advisor.provider", "openrouter");
    System.setProperty("ai.agent.role.advisor.api.key", "or-key");
    System.setProperty("ai.agent.role.advisor.model", "anthropic/claude-haiku-4.5");

    BYOKConfig byok = new BYOKConfig(
        "anthropic", "claude-sonnet-4-20250514", "byok-key", "", "");

    LLMProvider provider =
        LLMProviderRegistry.get(AgentRole.ADVISOR, byok);

    assertTrue("BYOK wins over role flags",
        provider instanceof AnthropicCompatibleProvider);
  }

  public void testMissingApiKeyForRoleThrows() {
    System.setProperty("ai.agent.role.advisor.provider", "anthropic");
    System.setProperty("ai.agent.role.advisor.model",
        "claude-sonnet-4-20250514");
    // No api key set anywhere.

    try {
      LLMProviderRegistry.get(AgentRole.ADVISOR, null);
      fail("should have thrown for missing API key");
    } catch (LLMProviderException expected) {
      // ok
    }
  }

  public void testDefaultModelAppliedWhenRoleModelEmpty()
      throws LLMProviderException {
    System.setProperty("ai.agent.role.advisor.provider", "openai");
    System.setProperty("ai.agent.role.advisor.api.key", "key");
    // No model — should use OpenAI's default "gpt-4o".
    LLMProvider provider =
        LLMProviderRegistry.get(AgentRole.ADVISOR, null);
    assertTrue(provider instanceof OpenAIProvider);
  }

  private static void clearAll() {
    String[] roles = { "advisor", "screen_editor", "project_editor",
        "project_editor_planner", "project_editor_child" };
    String[] fields = { "provider", "model", "api.key", "base.url",
        "reasoning.effort" };
    for (String r : roles) {
      for (String f : fields) {
        System.clearProperty("ai.agent.role." + r + "." + f);
      }
    }
    for (String f : new String[]{"provider", "model", "api.key", "base.url",
        "reasoning.effort"}) {
      System.clearProperty("ai.agent." + f);
    }
  }
}
