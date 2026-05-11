// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent.llm;

import com.google.appinventor.server.aiagent.AgentRole;
import junit.framework.TestCase;

public class RoleConfigResolverTest extends TestCase {

  // Global flag keys
  private static final String K_PROVIDER = "ai.agent.provider";
  private static final String K_MODEL = "ai.agent.model";
  private static final String K_API_KEY = "ai.agent.api.key";
  private static final String K_BASE_URL = "ai.agent.base.url";
  private static final String K_REASONING = "ai.agent.reasoning.effort";

  // Per-role flag keys (planner role used as exemplar)
  private static final String K_R_PROVIDER =
      "ai.agent.role.project_editor_planner.provider";
  private static final String K_R_MODEL =
      "ai.agent.role.project_editor_planner.model";
  private static final String K_R_API_KEY =
      "ai.agent.role.project_editor_planner.api.key";
  private static final String K_R_BASE_URL =
      "ai.agent.role.project_editor_planner.base.url";
  private static final String K_R_REASONING =
      "ai.agent.role.project_editor_planner.reasoning.effort";

  @Override
  protected void tearDown() throws Exception {
    System.clearProperty(K_PROVIDER);
    System.clearProperty(K_MODEL);
    System.clearProperty(K_API_KEY);
    System.clearProperty(K_BASE_URL);
    System.clearProperty(K_REASONING);
    System.clearProperty(K_R_PROVIDER);
    System.clearProperty(K_R_MODEL);
    System.clearProperty(K_R_API_KEY);
    System.clearProperty(K_R_BASE_URL);
    System.clearProperty(K_R_REASONING);
    super.tearDown();
  }

  public void testRoleFlagsWinOverGlobal() {
    System.setProperty(K_PROVIDER, "anthropic");
    System.setProperty(K_MODEL, "claude-sonnet-4-20250514");
    System.setProperty(K_API_KEY, "global-key");

    System.setProperty(K_R_PROVIDER, "openrouter");
    System.setProperty(K_R_MODEL, "qwen/qwen3.6-plus");
    System.setProperty(K_R_API_KEY, "planner-key");

    RoleConfig cfg = RoleConfigResolver.resolve(
        AgentRole.PROJECT_EDITOR_PLANNER);

    assertEquals("openrouter", cfg.getProvider());
    assertEquals("qwen/qwen3.6-plus", cfg.getModel());
    assertEquals("planner-key", cfg.getApiKey());
  }

  public void testEmptyRoleFlagsFallBackToGlobal() {
    System.setProperty(K_PROVIDER, "anthropic");
    System.setProperty(K_MODEL, "claude-sonnet-4-20250514");
    System.setProperty(K_API_KEY, "global-key");

    // No K_R_* set.

    RoleConfig cfg = RoleConfigResolver.resolve(
        AgentRole.PROJECT_EDITOR_PLANNER);

    assertEquals("anthropic", cfg.getProvider());
    assertEquals("claude-sonnet-4-20250514", cfg.getModel());
    assertEquals("global-key", cfg.getApiKey());
  }

  public void testPartialRoleOverridesMixWithGlobal() {
    // Role overrides model only; everything else inherits from global.
    System.setProperty(K_PROVIDER, "openrouter");
    System.setProperty(K_MODEL, "anthropic/claude-haiku-4.5");
    System.setProperty(K_API_KEY, "global-key");
    System.setProperty(K_REASONING, "low");

    System.setProperty(K_R_MODEL, "qwen/qwen3.6-plus");

    RoleConfig cfg = RoleConfigResolver.resolve(
        AgentRole.PROJECT_EDITOR_PLANNER);

    assertEquals("openrouter", cfg.getProvider());
    assertEquals("qwen/qwen3.6-plus", cfg.getModel());
    assertEquals("global-key", cfg.getApiKey());
    assertEquals("low", cfg.getReasoningEffort());
  }

  public void testAllEmptyYieldsEmptyConfig() {
    RoleConfig cfg = RoleConfigResolver.resolve(AgentRole.ADVISOR);
    // Provider defaults to "anthropic" via the global flag default;
    // everything else is empty.
    assertEquals("anthropic", cfg.getProvider());
    assertEquals("", cfg.getModel());
    assertEquals("", cfg.getApiKey());
  }
}
