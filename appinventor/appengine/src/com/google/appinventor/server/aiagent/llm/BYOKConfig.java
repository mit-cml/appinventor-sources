// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent.llm;

/**
 * Immutable per-request override of provider configuration, resolved from
 * the user's BYOK settings. Passed to
 * {@link LLMProviderRegistry#get(String, BYOKConfig)}.
 *
 * <p>{@link #toString()} redacts {@link #apiKey} so the value is safe to log.
 */
public final class BYOKConfig {
  private final String provider;
  private final String model;
  private final String apiKey;
  private final String baseUrl;
  private final String reasoningEffort;

  public BYOKConfig(String provider, String model, String apiKey,
                    String baseUrl, String reasoningEffort) {
    this.provider = provider;
    this.model = model;
    this.apiKey = apiKey;
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
    return "BYOKConfig{provider=" + provider
        + ", model=" + model
        + ", apiKey=***"
        + ", baseUrl=" + baseUrl
        + ", reasoningEffort=" + reasoningEffort + "}";
  }
}
