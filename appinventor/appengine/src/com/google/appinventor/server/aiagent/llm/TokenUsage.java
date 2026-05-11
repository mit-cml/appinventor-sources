// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent.llm;

import org.json.JSONObject;

/**
 * Token usage figures returned by an LLM provider for a single API call.
 *
 * <p>All counts are best-effort and may be zero if the provider does not
 * report a given field. Cache fields are populated only by providers that
 * expose prompt-cache hits (Anthropic, OpenAI Responses API, Gemini).
 */
public final class TokenUsage {

  private final long inputTokens;
  private final long outputTokens;
  private final long cachedInputTokens;
  private final long cacheCreationTokens;
  private final long reasoningTokens;
  private final double costUsd;

  public TokenUsage(long inputTokens, long outputTokens,
      long cachedInputTokens, long cacheCreationTokens) {
    this(inputTokens, outputTokens, cachedInputTokens, cacheCreationTokens, 0, 0.0);
  }

  public TokenUsage(long inputTokens, long outputTokens,
      long cachedInputTokens, long cacheCreationTokens,
      long reasoningTokens, double costUsd) {
    this.inputTokens = inputTokens;
    this.outputTokens = outputTokens;
    this.cachedInputTokens = cachedInputTokens;
    this.cacheCreationTokens = cacheCreationTokens;
    this.reasoningTokens = reasoningTokens;
    this.costUsd = costUsd;
  }

  public long getInputTokens() {
    return inputTokens;
  }

  public long getOutputTokens() {
    return outputTokens;
  }

  public long getCachedInputTokens() {
    return cachedInputTokens;
  }

  public long getCacheCreationTokens() {
    return cacheCreationTokens;
  }

  public long getReasoningTokens() {
    return reasoningTokens;
  }

  public double getCostUsd() {
    return costUsd;
  }

  public long getTotalTokens() {
    return inputTokens + outputTokens;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("input=").append(inputTokens)
        .append(" output=").append(outputTokens);
    if (cachedInputTokens > 0) {
      sb.append(" cachedInput=").append(cachedInputTokens);
    }
    if (cacheCreationTokens > 0) {
      sb.append(" cacheCreation=").append(cacheCreationTokens);
    }
    if (reasoningTokens > 0) {
      sb.append(" reasoning=").append(reasoningTokens);
    }
    if (costUsd > 0) {
      sb.append(String.format(" cost=$%.6f", costUsd));
    }
    return sb.toString();
  }

  // ---- Provider-specific parsers ----

  /**
   * Anthropic Messages API. Fields: input_tokens, output_tokens,
   * cache_read_input_tokens, cache_creation_input_tokens.
   */
  public static TokenUsage fromAnthropic(JSONObject usage) {
    if (usage == null) {
      return null;
    }
    return new TokenUsage(
        usage.optLong("input_tokens", 0),
        usage.optLong("output_tokens", 0),
        usage.optLong("cache_read_input_tokens", 0),
        usage.optLong("cache_creation_input_tokens", 0));
  }

  /**
   * Bedrock Converse API. Fields: inputTokens, outputTokens.
   */
  public static TokenUsage fromBedrock(JSONObject usage) {
    if (usage == null) {
      return null;
    }
    return new TokenUsage(
        usage.optLong("inputTokens", 0),
        usage.optLong("outputTokens", 0),
        usage.optLong("cacheReadInputTokens", 0),
        usage.optLong("cacheWriteInputTokens", 0));
  }

  /**
   * Gemini / Vertex API. Fields: promptTokenCount, candidatesTokenCount,
   * cachedContentTokenCount.
   */
  public static TokenUsage fromGemini(JSONObject usage) {
    if (usage == null) {
      return null;
    }
    return new TokenUsage(
        usage.optLong("promptTokenCount", 0),
        usage.optLong("candidatesTokenCount", 0),
        usage.optLong("cachedContentTokenCount", 0),
        0);
  }

  /**
   * OpenAI Responses API. Fields: input_tokens, output_tokens,
   * input_tokens_details.cached_tokens.
   */
  public static TokenUsage fromOpenAIResponses(JSONObject usage) {
    if (usage == null) {
      return null;
    }
    long cached = 0;
    JSONObject details = usage.optJSONObject("input_tokens_details");
    if (details != null) {
      cached = details.optLong("cached_tokens", 0);
    }
    return new TokenUsage(
        usage.optLong("input_tokens", 0),
        usage.optLong("output_tokens", 0),
        cached,
        0);
  }

  /**
   * OpenAI Chat Completions API. Fields: prompt_tokens, completion_tokens,
   * prompt_tokens_details.cached_tokens.
   */
  public static TokenUsage fromOpenAIChat(JSONObject usage) {
    if (usage == null) {
      return null;
    }
    long cached = 0;
    long cacheWrite = 0;
    JSONObject promptDetails = usage.optJSONObject("prompt_tokens_details");
    if (promptDetails != null) {
      cached = promptDetails.optLong("cached_tokens", 0);
      cacheWrite = promptDetails.optLong("cache_write_tokens", 0);
    }
    long reasoning = 0;
    JSONObject completionDetails = usage.optJSONObject("completion_tokens_details");
    if (completionDetails != null) {
      reasoning = completionDetails.optLong("reasoning_tokens", 0);
    }
    // OpenRouter (only) emits a "cost" field in USD.
    double cost = usage.optDouble("cost", 0.0);
    return new TokenUsage(
        usage.optLong("prompt_tokens", 0),
        usage.optLong("completion_tokens", 0),
        cached,
        cacheWrite,
        reasoning,
        cost);
  }
}
