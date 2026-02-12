// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent.llm;

import com.google.appinventor.server.flags.Flag;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Factory that selects and configures the correct {@link LLMProvider}
 * implementation based on a configuration name.
 *
 * <p>Provider selection and configuration are driven by system properties
 * (set in appengine-web.xml), accessed through the {@link Flag} mechanism:
 * <ul>
 *   <li>{@code ai.agent.provider} -- provider name (anthropic, openai, gemini, ollama, minimax)</li>
 *   <li>{@code ai.agent.api.key} -- API key for the selected provider</li>
 *   <li>{@code ai.agent.model} -- model name override</li>
 *   <li>{@code ai.agent.base.url} -- base URL override (primarily for Ollama)</li>
 * </ul>
 */
public class LLMProviderRegistry {

  private static final Logger LOG = Logger.getLogger(LLMProviderRegistry.class.getName());

  private static final Flag<String> PROVIDER_FLAG =
      Flag.createFlag("ai.agent.provider", "anthropic");
  private static final Flag<String> API_KEY_FLAG =
      Flag.createFlag("ai.agent.api.key", "");
  private static final Flag<String> MODEL_FLAG =
      Flag.createFlag("ai.agent.model", "");
  private static final Flag<String> BASE_URL_FLAG =
      Flag.createFlag("ai.agent.base.url", "");

  /** Default model names per provider. */
  private static final Map<String, String> DEFAULT_MODELS = new HashMap<>();

  static {
    DEFAULT_MODELS.put("anthropic", "claude-sonnet-4-20250514");
    DEFAULT_MODELS.put("openai", "gpt-4o");
    DEFAULT_MODELS.put("gemini", "gemini-2.0-flash");
    DEFAULT_MODELS.put("ollama", "llama3.1");
    DEFAULT_MODELS.put("minimax", "MiniMax-M2");
  }

  /**
   * Returns an {@link LLMProvider} instance for the given provider name.
   *
   * @param providerName the provider name ("anthropic", "openai", "gemini", "ollama", or "minimax")
   * @return the configured provider instance
   * @throws LLMProviderException if the provider name is unknown or configuration is invalid
   */
  public static LLMProvider get(String providerName) throws LLMProviderException {
    if (providerName == null || providerName.isEmpty()) {
      providerName = PROVIDER_FLAG.get();
    }
    providerName = providerName.toLowerCase().trim();

    String apiKey = API_KEY_FLAG.get();
    String model = MODEL_FLAG.get();
    String baseUrl = BASE_URL_FLAG.get();

    // Use default model if none specified
    if (model == null || model.isEmpty()) {
      model = DEFAULT_MODELS.get(providerName);
      if (model == null) {
        model = "";
      }
    }

    LOG.info("Creating LLM provider: " + providerName + " with model: " + model);

    switch (providerName) {
      case "anthropic":
        validateApiKey(apiKey, "Anthropic");
        return new AnthropicProvider(apiKey, model);

      case "openai":
        validateApiKey(apiKey, "OpenAI");
        return new OpenAIProvider(apiKey, model);

      case "gemini":
        validateApiKey(apiKey, "Gemini");
        return new GeminiProvider(apiKey, model);

      case "ollama":
        if (baseUrl == null || baseUrl.isEmpty()) {
          baseUrl = "http://localhost:11434";
        }
        return new OllamaProvider(baseUrl, model, apiKey);

      case "minimax":
        validateApiKey(apiKey, "MiniMax");
        return new MiniMaxProvider(apiKey, model);

      default:
        throw new LLMProviderException(
            "Unknown LLM provider: " + providerName,
            "The configured AI provider is not supported. "
                + "Supported providers: anthropic, openai, gemini, ollama, minimax.");
    }
  }

  /**
   * Returns an {@link LLMProvider} instance using the default provider
   * from the system property {@code ai.agent.provider}.
   *
   * @return the configured provider instance
   * @throws LLMProviderException if configuration is invalid
   */
  public static LLMProvider getDefault() throws LLMProviderException {
    return get(PROVIDER_FLAG.get());
  }

  private static void validateApiKey(String apiKey, String providerName)
      throws LLMProviderException {
    if (apiKey == null || apiKey.isEmpty()) {
      throw new LLMProviderException(
          "No API key configured for " + providerName
              + ". Set the ai.agent.api.key system property.",
          "The AI agent is not configured. Please ask your administrator "
              + "to set up the API key.");
    }
  }
}
