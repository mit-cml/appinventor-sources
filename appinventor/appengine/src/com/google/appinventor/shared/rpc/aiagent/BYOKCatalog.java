// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.aiagent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

/**
 * Closed catalog of providers and models exposed to BYOK users. Compiled
 * into both client (GWT) and server. Single source of truth for the
 * dropdown contents in {@code AISettingsWizard} and the validation logic
 * in {@code BYOKResolver}.
 *
 * <p>Reasoning effort is per-model and uses provider-native values (e.g.
 * OpenAI {@code low/medium/high/xhigh}, Gemini {@code LOW/MEDIUM/HIGH}).
 * Models without reasoning support expose an empty option list.
 */
public final class BYOKCatalog {

  private BYOKCatalog() {}

  /** Provider identifiers. The wire name matches the value the server-side
   *  {@code LLMProviderRegistry} uses today. */
  public enum Provider {
    ANTHROPIC("anthropic"),
    OPENAI("openai"),
    GEMINI("gemini"),
    MINIMAX("minimax"),
    OPENROUTER("openrouter"),
    ANTHROPIC_COMPATIBLE("anthropic-compatible"),
    OPENAI_COMPATIBLE("openai-compatible");

    private final String wireName;

    Provider(String wireName) {
      this.wireName = wireName;
    }

    public String wireName() {
      return wireName;
    }

    /** Returns the matching {@link Provider} or {@code null} if none. */
    public static Provider fromWireName(String s) {
      if (s == null) {
        return null;
      }
      for (Provider p : values()) {
        if (p.wireName.equals(s)) {
          return p;
        }
      }
      return null;
    }
  }

  /** A single model entry: id plus the provider-native reasoning options it
   *  accepts (empty when the model does not support reasoning). */
  public static final class ModelInfo {
    private final String id;
    private final List<String> reasoningOptions;

    public ModelInfo(String id, List<String> reasoningOptions) {
      this.id = id;
      this.reasoningOptions = reasoningOptions == null
          ? Collections.<String>emptyList()
          : Collections.unmodifiableList(new ArrayList<String>(reasoningOptions));
    }

    public String getId() {
      return id;
    }

    public List<String> getReasoningOptions() {
      return reasoningOptions;
    }

    public boolean supportsReasoning() {
      return !reasoningOptions.isEmpty();
    }
  }

  // ---- Capabilities ----

  private static final EnumSet<Provider> REQUIRES_BASE_URL =
      EnumSet.of(Provider.ANTHROPIC_COMPATIBLE, Provider.OPENAI_COMPATIBLE);

  private static final EnumSet<Provider> FREE_TEXT_MODEL =
      EnumSet.of(Provider.ANTHROPIC_COMPATIBLE, Provider.OPENAI_COMPATIBLE);

  public static boolean requiresBaseUrl(Provider p) {
    return REQUIRES_BASE_URL.contains(p);
  }

  public static boolean hasFreeTextModel(Provider p) {
    return FREE_TEXT_MODEL.contains(p);
  }

  // ---- Native reasoning option presets ----

  private static final List<String> ANTHROPIC_REASONING =
      Collections.unmodifiableList(Arrays.asList("low", "medium", "high"));
  // OpenAI: o-series uses low/medium/high; GPT-5 family adds xhigh.
  private static final List<String> OPENAI_REASONING_LMH =
      Collections.unmodifiableList(Arrays.asList("low", "medium", "high"));
  private static final List<String> OPENAI_REASONING_LMHX =
      Collections.unmodifiableList(Arrays.asList("low", "medium", "high", "xhigh"));
  private static final List<String> OPENAI_REASONING_PRO =
      Collections.unmodifiableList(Arrays.asList("medium", "high", "xhigh"));
  // Gemini server uppercases the stored value before sending. Use uppercase
  // here to keep the wire form consistent with what's documented.
  private static final List<String> GEMINI_REASONING_LMH =
      Collections.unmodifiableList(Arrays.asList("LOW", "MEDIUM", "HIGH"));
  private static final List<String> GEMINI_REASONING_MLMH =
      Collections.unmodifiableList(Arrays.asList("MINIMAL", "LOW", "MEDIUM", "HIGH"));

  // ---- Models ----

  private static final Map<Provider, List<ModelInfo>> MODELS =
      new EnumMap<Provider, List<ModelInfo>>(Provider.class);

  static {
    // Anthropic — verified May 2026 against docs.anthropic.com (overview +
    // deprecation pages). Opus 4.7 uses adaptive thinking only (no
    // thinking.effort parameter); leave its reasoning options empty.
    MODELS.put(Provider.ANTHROPIC, Collections.unmodifiableList(Arrays.asList(
        // Current
        new ModelInfo("claude-opus-4-7", Collections.<String>emptyList()),
        new ModelInfo("claude-sonnet-4-6", ANTHROPIC_REASONING),
        new ModelInfo("claude-haiku-4-5-20251001", ANTHROPIC_REASONING),
        // Legacy still callable
        new ModelInfo("claude-opus-4-6", ANTHROPIC_REASONING),
        new ModelInfo("claude-sonnet-4-5-20250929", ANTHROPIC_REASONING),
        new ModelInfo("claude-opus-4-5-20251101", ANTHROPIC_REASONING),
        new ModelInfo("claude-opus-4-1-20250805", ANTHROPIC_REASONING),
        // Deprecated, retiring 2026-06-15
        new ModelInfo("claude-sonnet-4-20250514", ANTHROPIC_REASONING),
        new ModelInfo("claude-opus-4-20250514", ANTHROPIC_REASONING),
        // Older 3.x — extended thinking on 3.7 only
        new ModelInfo("claude-3-7-sonnet-20250219", ANTHROPIC_REASONING),
        new ModelInfo("claude-3-5-sonnet-20241022", Collections.<String>emptyList()),
        new ModelInfo("claude-3-5-haiku-20241022", Collections.<String>emptyList())
    )));
    // OpenAI — verified May 2026 against developers.openai.com.
    // Reasoning levels per OpenAI guide: model-dependent subset of
    // {none, minimal, low, medium, high, xhigh}. We expose
    // {low, medium, high, xhigh} for GPT-5 (xhigh on .2/.4/.5/codex-max),
    // {low, medium, high} for o-series and gpt-5/5.1, and empty for chat.
    MODELS.put(Provider.OPENAI, Collections.unmodifiableList(Arrays.asList(
        // GPT-5.5
        new ModelInfo("gpt-5.5", OPENAI_REASONING_LMHX),
        new ModelInfo("gpt-5.5-pro", OPENAI_REASONING_PRO),
        // GPT-5.4
        new ModelInfo("gpt-5.4", OPENAI_REASONING_LMHX),
        new ModelInfo("gpt-5.4-pro", OPENAI_REASONING_PRO),
        new ModelInfo("gpt-5.4-mini", OPENAI_REASONING_LMHX),
        new ModelInfo("gpt-5.4-nano", OPENAI_REASONING_LMHX),
        // GPT-5.3
        new ModelInfo("gpt-5.3-codex", OPENAI_REASONING_LMHX),
        new ModelInfo("gpt-5.3-chat", Collections.<String>emptyList()),
        // GPT-5.2
        new ModelInfo("gpt-5.2", OPENAI_REASONING_LMHX),
        new ModelInfo("gpt-5.2-pro", OPENAI_REASONING_PRO),
        new ModelInfo("gpt-5.2-codex", OPENAI_REASONING_LMHX),
        new ModelInfo("gpt-5.2-chat", Collections.<String>emptyList()),
        // GPT-5.1
        new ModelInfo("gpt-5.1", OPENAI_REASONING_LMH),
        new ModelInfo("gpt-5.1-codex-max", OPENAI_REASONING_LMHX),
        new ModelInfo("gpt-5.1-codex", OPENAI_REASONING_LMH),
        new ModelInfo("gpt-5.1-codex-mini", OPENAI_REASONING_LMH),
        new ModelInfo("gpt-5.1-chat", Collections.<String>emptyList()),
        // GPT-5
        new ModelInfo("gpt-5", OPENAI_REASONING_LMH),
        new ModelInfo("gpt-5-pro", OPENAI_REASONING_LMH),
        new ModelInfo("gpt-5-mini", OPENAI_REASONING_LMH),
        new ModelInfo("gpt-5-nano", OPENAI_REASONING_LMH),
        new ModelInfo("gpt-5-codex", OPENAI_REASONING_LMH),
        new ModelInfo("gpt-5-chat", Collections.<String>emptyList()),
        // o-series
        new ModelInfo("o4-mini", OPENAI_REASONING_LMH),
        new ModelInfo("o3-pro", OPENAI_REASONING_LMH),
        new ModelInfo("o3", OPENAI_REASONING_LMH),
        new ModelInfo("o3-mini", OPENAI_REASONING_LMH),
        new ModelInfo("o1-pro", OPENAI_REASONING_LMH),
        new ModelInfo("o1", OPENAI_REASONING_LMH),
        new ModelInfo("o1-mini", OPENAI_REASONING_LMH),
        // GPT-4 family — chat, no reasoning
        new ModelInfo("gpt-4.1", Collections.<String>emptyList()),
        new ModelInfo("gpt-4.1-mini", Collections.<String>emptyList()),
        new ModelInfo("gpt-4.1-nano", Collections.<String>emptyList()),
        new ModelInfo("gpt-4o", Collections.<String>emptyList()),
        new ModelInfo("gpt-4o-mini", Collections.<String>emptyList())
    )));
    // Gemini — verified May 2026 against ai.google.dev.
    // Gemini 3.x uses thinkingLevel (server uppercases the stored value).
    // Gemini 2.5 uses thinkingBudget (integer); the server does not currently
    // expose budgets, so reasoning options stay empty for 2.5.
    MODELS.put(Provider.GEMINI, Collections.unmodifiableList(Arrays.asList(
        // Gemini 3.x
        new ModelInfo("gemini-3.1-pro", GEMINI_REASONING_LMH),
        new ModelInfo("gemini-3.1-pro-preview", GEMINI_REASONING_LMH),
        new ModelInfo("gemini-3-flash", GEMINI_REASONING_MLMH),
        new ModelInfo("gemini-3-flash-preview", GEMINI_REASONING_MLMH),
        new ModelInfo("gemini-3.1-flash-lite", GEMINI_REASONING_MLMH),
        new ModelInfo("gemini-3.1-flash-lite-preview", GEMINI_REASONING_MLMH),
        // Gemini 2.5 — thinkingBudget only
        new ModelInfo("gemini-2.5-pro", Collections.<String>emptyList()),
        new ModelInfo("gemini-2.5-flash", Collections.<String>emptyList()),
        new ModelInfo("gemini-2.5-flash-lite", Collections.<String>emptyList()),
        // Gemini 2.0 — no thinking
        new ModelInfo("gemini-2.0-flash", Collections.<String>emptyList()),
        new ModelInfo("gemini-2.0-flash-lite", Collections.<String>emptyList())
    )));
    // MiniMax — verified May 2026 against platform.minimaxi.com.
    MODELS.put(Provider.MINIMAX, Collections.unmodifiableList(Arrays.asList(
        new ModelInfo("MiniMax-M2.7", Collections.<String>emptyList()),
        new ModelInfo("MiniMax-M2.7-highspeed", Collections.<String>emptyList()),
        new ModelInfo("MiniMax-M2.5", Collections.<String>emptyList()),
        new ModelInfo("MiniMax-M2.1", Collections.<String>emptyList()),
        new ModelInfo("MiniMax-M2", Collections.<String>emptyList()),
        new ModelInfo("MiniMax-M1", Collections.<String>emptyList())
    )));
    // OpenRouter — verified May 2026 against openrouter.ai/api/v1/models
    // (live JSON catalog). Server-side OpenRouterProvider does not pipe
    // reasoning effort, so all entries are non-reasoning.
    MODELS.put(Provider.OPENROUTER, Collections.unmodifiableList(Arrays.asList(
        // Anthropic
        new ModelInfo("anthropic/claude-opus-4.7", Collections.<String>emptyList()),
        new ModelInfo("anthropic/claude-opus-4.6", Collections.<String>emptyList()),
        new ModelInfo("anthropic/claude-opus-4.6-fast", Collections.<String>emptyList()),
        new ModelInfo("anthropic/claude-opus-4.5", Collections.<String>emptyList()),
        new ModelInfo("anthropic/claude-opus-4.1", Collections.<String>emptyList()),
        new ModelInfo("anthropic/claude-opus-4", Collections.<String>emptyList()),
        new ModelInfo("anthropic/claude-sonnet-4.6", Collections.<String>emptyList()),
        new ModelInfo("anthropic/claude-sonnet-4.5", Collections.<String>emptyList()),
        new ModelInfo("anthropic/claude-sonnet-4", Collections.<String>emptyList()),
        new ModelInfo("anthropic/claude-haiku-4.5", Collections.<String>emptyList()),
        new ModelInfo("anthropic/claude-3.7-sonnet", Collections.<String>emptyList()),
        new ModelInfo("anthropic/claude-3.7-sonnet:thinking", Collections.<String>emptyList()),
        new ModelInfo("anthropic/claude-3.5-haiku", Collections.<String>emptyList()),
        // OpenAI
        new ModelInfo("openai/gpt-5.5", Collections.<String>emptyList()),
        new ModelInfo("openai/gpt-5.5-pro", Collections.<String>emptyList()),
        new ModelInfo("openai/gpt-5.4", Collections.<String>emptyList()),
        new ModelInfo("openai/gpt-5.4-pro", Collections.<String>emptyList()),
        new ModelInfo("openai/gpt-5.4-mini", Collections.<String>emptyList()),
        new ModelInfo("openai/gpt-5.4-nano", Collections.<String>emptyList()),
        new ModelInfo("openai/gpt-5.3-codex", Collections.<String>emptyList()),
        new ModelInfo("openai/gpt-5.3-chat", Collections.<String>emptyList()),
        new ModelInfo("openai/gpt-5.2", Collections.<String>emptyList()),
        new ModelInfo("openai/gpt-5.2-pro", Collections.<String>emptyList()),
        new ModelInfo("openai/gpt-5.2-codex", Collections.<String>emptyList()),
        new ModelInfo("openai/gpt-5.1", Collections.<String>emptyList()),
        new ModelInfo("openai/gpt-5.1-codex", Collections.<String>emptyList()),
        new ModelInfo("openai/gpt-5.1-codex-max", Collections.<String>emptyList()),
        new ModelInfo("openai/gpt-5.1-codex-mini", Collections.<String>emptyList()),
        new ModelInfo("openai/gpt-5", Collections.<String>emptyList()),
        new ModelInfo("openai/gpt-5-pro", Collections.<String>emptyList()),
        new ModelInfo("openai/gpt-5-mini", Collections.<String>emptyList()),
        new ModelInfo("openai/gpt-5-nano", Collections.<String>emptyList()),
        new ModelInfo("openai/gpt-5-codex", Collections.<String>emptyList()),
        new ModelInfo("openai/o4-mini", Collections.<String>emptyList()),
        new ModelInfo("openai/o4-mini-high", Collections.<String>emptyList()),
        new ModelInfo("openai/o3-pro", Collections.<String>emptyList()),
        new ModelInfo("openai/o3", Collections.<String>emptyList()),
        new ModelInfo("openai/o3-mini", Collections.<String>emptyList()),
        new ModelInfo("openai/o3-mini-high", Collections.<String>emptyList()),
        new ModelInfo("openai/o1-pro", Collections.<String>emptyList()),
        new ModelInfo("openai/o1", Collections.<String>emptyList()),
        new ModelInfo("openai/gpt-4.1", Collections.<String>emptyList()),
        new ModelInfo("openai/gpt-4.1-mini", Collections.<String>emptyList()),
        new ModelInfo("openai/gpt-4.1-nano", Collections.<String>emptyList()),
        new ModelInfo("openai/gpt-4o", Collections.<String>emptyList()),
        new ModelInfo("openai/gpt-4o-mini", Collections.<String>emptyList()),
        // Google
        new ModelInfo("google/gemini-3.1-pro-preview", Collections.<String>emptyList()),
        new ModelInfo("google/gemini-3-flash-preview", Collections.<String>emptyList()),
        new ModelInfo("google/gemini-3.1-flash-lite", Collections.<String>emptyList()),
        new ModelInfo("google/gemini-3.1-flash-lite-preview", Collections.<String>emptyList()),
        new ModelInfo("google/gemini-2.5-pro", Collections.<String>emptyList()),
        new ModelInfo("google/gemini-2.5-flash", Collections.<String>emptyList()),
        new ModelInfo("google/gemini-2.5-flash-lite", Collections.<String>emptyList()),
        new ModelInfo("google/gemini-2.0-flash-001", Collections.<String>emptyList()),
        new ModelInfo("google/gemini-2.0-flash-lite-001", Collections.<String>emptyList()),
        // xAI
        new ModelInfo("x-ai/grok-4.3", Collections.<String>emptyList()),
        new ModelInfo("x-ai/grok-4.20", Collections.<String>emptyList()),
        new ModelInfo("x-ai/grok-4.20-multi-agent", Collections.<String>emptyList()),
        new ModelInfo("x-ai/grok-4.1-fast", Collections.<String>emptyList()),
        new ModelInfo("x-ai/grok-4-fast", Collections.<String>emptyList()),
        new ModelInfo("x-ai/grok-4", Collections.<String>emptyList()),
        new ModelInfo("x-ai/grok-3", Collections.<String>emptyList()),
        new ModelInfo("x-ai/grok-3-mini", Collections.<String>emptyList()),
        new ModelInfo("x-ai/grok-code-fast-1", Collections.<String>emptyList()),
        // DeepSeek
        new ModelInfo("deepseek/deepseek-v4-pro", Collections.<String>emptyList()),
        new ModelInfo("deepseek/deepseek-v4-flash", Collections.<String>emptyList()),
        new ModelInfo("deepseek/deepseek-v3.2", Collections.<String>emptyList()),
        new ModelInfo("deepseek/deepseek-v3.2-speciale", Collections.<String>emptyList()),
        new ModelInfo("deepseek/deepseek-v3.1-terminus", Collections.<String>emptyList()),
        new ModelInfo("deepseek/deepseek-r1", Collections.<String>emptyList()),
        new ModelInfo("deepseek/deepseek-chat-v3.1", Collections.<String>emptyList()),
        new ModelInfo("deepseek/deepseek-chat", Collections.<String>emptyList()),
        // Moonshot
        new ModelInfo("moonshotai/kimi-k2.6", Collections.<String>emptyList()),
        new ModelInfo("moonshotai/kimi-k2.5", Collections.<String>emptyList()),
        new ModelInfo("moonshotai/kimi-k2-thinking", Collections.<String>emptyList()),
        new ModelInfo("moonshotai/kimi-k2", Collections.<String>emptyList()),
        // Xiaomi
        new ModelInfo("xiaomi/mimo-v2.5-pro", Collections.<String>emptyList()),
        new ModelInfo("xiaomi/mimo-v2.5", Collections.<String>emptyList()),
        new ModelInfo("xiaomi/mimo-v2-pro", Collections.<String>emptyList()),
        new ModelInfo("xiaomi/mimo-v2-flash", Collections.<String>emptyList()),
        new ModelInfo("xiaomi/mimo-v2-omni", Collections.<String>emptyList()),
        // MiniMax via OpenRouter
        new ModelInfo("minimax/minimax-m2.7", Collections.<String>emptyList()),
        new ModelInfo("minimax/minimax-m2.5", Collections.<String>emptyList()),
        new ModelInfo("minimax/minimax-m2.1", Collections.<String>emptyList()),
        new ModelInfo("minimax/minimax-m2", Collections.<String>emptyList()),
        new ModelInfo("minimax/minimax-m1", Collections.<String>emptyList()),
        // Qwen
        new ModelInfo("qwen/qwen3.6-max-preview", Collections.<String>emptyList()),
        new ModelInfo("qwen/qwen3.6-plus", Collections.<String>emptyList()),
        new ModelInfo("qwen/qwen3.6-flash", Collections.<String>emptyList()),
        new ModelInfo("qwen/qwen3.5-plus-20260420", Collections.<String>emptyList()),
        new ModelInfo("qwen/qwen3-coder-plus", Collections.<String>emptyList()),
        new ModelInfo("qwen/qwen3-max", Collections.<String>emptyList()),
        new ModelInfo("qwen/qwen3-max-thinking", Collections.<String>emptyList()),
        // Meta
        new ModelInfo("meta-llama/llama-4-maverick", Collections.<String>emptyList()),
        new ModelInfo("meta-llama/llama-4-scout", Collections.<String>emptyList()),
        new ModelInfo("meta-llama/llama-3.3-70b-instruct", Collections.<String>emptyList()),
        // Mistral
        new ModelInfo("mistralai/mistral-large-2512", Collections.<String>emptyList()),
        new ModelInfo("mistralai/mistral-medium-3.1", Collections.<String>emptyList()),
        new ModelInfo("mistralai/devstral-medium", Collections.<String>emptyList()),
        new ModelInfo("mistralai/codestral-2508", Collections.<String>emptyList())
    )));
    MODELS.put(Provider.ANTHROPIC_COMPATIBLE, Collections.<ModelInfo>emptyList());
    MODELS.put(Provider.OPENAI_COMPATIBLE, Collections.<ModelInfo>emptyList());
  }

  /** Returns the curated model list for {@code p}. Empty for free-text
   *  providers. Never {@code null}. */
  public static List<ModelInfo> models(Provider p) {
    List<ModelInfo> list = MODELS.get(p);
    return list == null ? Collections.<ModelInfo>emptyList() : list;
  }

  /** Returns the {@link ModelInfo} for {@code modelId} under {@code p}, or
   *  {@code null} if not found (e.g. compatible-provider free-text input). */
  public static ModelInfo modelInfo(Provider p, String modelId) {
    if (p == null || modelId == null) {
      return null;
    }
    for (ModelInfo m : models(p)) {
      if (m.getId().equals(modelId)) {
        return m;
      }
    }
    return null;
  }

  /** Returns the provider-native reasoning options for the given model, or
   *  empty list when reasoning is unsupported (or the model is unknown). */
  public static List<String> reasoningOptions(Provider p, String modelId) {
    ModelInfo info = modelInfo(p, modelId);
    return info == null ? Collections.<String>emptyList() : info.getReasoningOptions();
  }
}
