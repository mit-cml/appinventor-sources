// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent.llm;

/**
 * Immutable per-role provider configuration resolved from server flags.
 * Shape mirrors {@link BYOKConfig}; the two are kept distinct so the type
 * system makes the source obvious at call sites.
 *
 * <p>{@link #toString()} redacts {@link #apiKey} so the value is safe to log.
 */
public final class RoleConfig {
  private final String provider;
  private final String model;
  private final String apiKey;
  private final String baseUrl;
  private final String reasoningEffort;

  public RoleConfig(String provider, String model, String apiKey,
                    String baseUrl, String reasoningEffort) {
    this.provider = provider == null ? "" : provider;
    this.model = model == null ? "" : model;
    this.apiKey = apiKey == null ? "" : apiKey;
    this.baseUrl = baseUrl == null ? "" : baseUrl;
    this.reasoningEffort = reasoningEffort == null ? "" : reasoningEffort;
  }

  public String getProvider()         { return provider; }
  public String getModel()            { return model; }
  public String getApiKey()           { return apiKey; }
  public String getBaseUrl()          { return baseUrl; }
  public String getReasoningEffort()  { return reasoningEffort; }

  @Override
  public String toString() {
    return "RoleConfig{provider=" + provider
        + ", model=" + model
        + ", apiKey=***"
        + ", baseUrl=" + baseUrl
        + ", reasoningEffort=" + reasoningEffort + "}";
  }
}
