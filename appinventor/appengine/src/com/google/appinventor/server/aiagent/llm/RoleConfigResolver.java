// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent.llm;

import com.google.appinventor.server.aiagent.AgentRole;

/**
 * Reads {@link RoleConfig} from system properties.
 *
 * <p>Per-role keys take the form
 * {@code ai.agent.role.<role-flag-key>.<field>}. Each field falls back to
 * the corresponding global flag (e.g. {@code ai.agent.model}) when the
 * role-specific value is empty. The global {@code ai.agent.provider}
 * defaults to {@code "anthropic"} when unset, matching legacy behavior.
 *
 * <p>Reads {@link System#getProperty(String)} directly rather than caching
 * via {@code Flag.createFlag(...)} so tests can vary properties between
 * cases.
 */
public final class RoleConfigResolver {

  private RoleConfigResolver() {}

  public static RoleConfig resolve(AgentRole role) {
    String prefix = "ai.agent.role." + role.flagKey() + ".";

    String provider = read(prefix + "provider",
        System.getProperty("ai.agent.provider", "anthropic"));
    String model = read(prefix + "model",
        System.getProperty("ai.agent.model", ""));
    String apiKey = read(prefix + "api.key",
        System.getProperty("ai.agent.api.key", ""));
    String baseUrl = read(prefix + "base.url",
        System.getProperty("ai.agent.base.url", ""));
    String reasoningEffort = read(prefix + "reasoning.effort",
        System.getProperty("ai.agent.reasoning.effort", ""));

    return new RoleConfig(provider, model, apiKey, baseUrl, reasoningEffort);
  }

  private static String read(String key, String fallback) {
    String value = System.getProperty(key, "");
    return value.isEmpty() ? fallback : value;
  }
}
