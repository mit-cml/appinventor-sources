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
 *   <li>{@code ai.agent.provider} -- provider name (anthropic, anthropic-compatible,
 *       openai, gemini, ollama, minimax, openrouter, openai-compatible, bedrock, vertex)</li>
 *   <li>{@code ai.agent.api.key} -- API key for the selected provider</li>
 *   <li>{@code ai.agent.model} -- model name override</li>
 *   <li>{@code ai.agent.base.url} -- base URL override (for compatible providers, Ollama)</li>
 *   <li>{@code ai.agent.provider.bedrock.*} -- Bedrock-specific config (region, access key, etc.)</li>
 *   <li>{@code ai.agent.provider.vertex.*} -- Vertex-specific config (project, region, service account)</li>
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
  private static final Flag<String> REASONING_EFFORT_FLAG =
      Flag.createFlag("ai.agent.reasoning.effort", "");

  // Bedrock-specific flags
  private static final Flag<String> BEDROCK_REGION_FLAG =
      Flag.createFlag("ai.agent.provider.bedrock.region", "us-east-1");
  private static final Flag<String> BEDROCK_ACCESS_KEY_FLAG =
      Flag.createFlag("ai.agent.provider.bedrock.access.key", "");
  private static final Flag<String> BEDROCK_SECRET_KEY_FLAG =
      Flag.createFlag("ai.agent.provider.bedrock.secret.key", "");
  private static final Flag<String> BEDROCK_SESSION_TOKEN_FLAG =
      Flag.createFlag("ai.agent.provider.bedrock.session.token", "");

  // Vertex-specific flags
  private static final Flag<String> VERTEX_PROJECT_FLAG =
      Flag.createFlag("ai.agent.provider.vertex.project", "");
  private static final Flag<String> VERTEX_REGION_FLAG =
      Flag.createFlag("ai.agent.provider.vertex.region", "us-central1");
  private static final Flag<String> VERTEX_SERVICE_ACCOUNT_FLAG =
      Flag.createFlag("ai.agent.provider.vertex.service.account", "");

  /** Default model names per provider. */
  private static final Map<String, String> DEFAULT_MODELS = new HashMap<>();

  static {
    DEFAULT_MODELS.put("anthropic", "claude-sonnet-4-20250514");
    DEFAULT_MODELS.put("openai", "gpt-4o");
    DEFAULT_MODELS.put("gemini", "gemini-2.0-flash");
    DEFAULT_MODELS.put("ollama", "llama3.1");
    DEFAULT_MODELS.put("minimax", "MiniMax-M2");
    DEFAULT_MODELS.put("bedrock", "anthropic.claude-sonnet-4-20250514-v1:0");
    DEFAULT_MODELS.put("vertex", "gemini-2.0-flash");
    DEFAULT_MODELS.put("openrouter", "anthropic/claude-sonnet-4");
    DEFAULT_MODELS.put("openai-compatible", "");
    DEFAULT_MODELS.put("anthropic-compatible", "");
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
    String reasoningEffort = REASONING_EFFORT_FLAG.get();

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
        return new AnthropicCompatibleProvider(apiKey, model, baseUrl, reasoningEffort);

      case "anthropic-compatible":
        validateApiKey(apiKey, "Anthropic-Compatible");
        validateBaseUrl(baseUrl, "Anthropic-Compatible");
        return new AnthropicCompatibleProvider(apiKey, model, baseUrl, reasoningEffort);

      case "openai":
        validateApiKey(apiKey, "OpenAI");
        return new OpenAIProvider(apiKey, model, reasoningEffort);

      case "gemini":
        validateApiKey(apiKey, "Gemini");
        return new GeminiProvider(apiKey, model, reasoningEffort);

      case "ollama":
        if (baseUrl == null || baseUrl.isEmpty()) {
          baseUrl = "http://localhost:11434";
        }
        return new OllamaProvider(baseUrl, model, apiKey);

      case "minimax":
        validateApiKey(apiKey, "MiniMax");
        if (baseUrl != null && !baseUrl.isEmpty()) {
          return new OpenAIChatCompletionsProvider(apiKey, model, baseUrl);
        }
        return new MiniMaxProvider(apiKey, model);

      case "openrouter":
        validateApiKey(apiKey, "OpenRouter");
        return new OpenRouterProvider(apiKey, model);

      case "openai-compatible":
        validateApiKey(apiKey, "OpenAI-Compatible");
        validateBaseUrl(baseUrl, "OpenAI-Compatible");
        return new OpenAIChatCompletionsProvider(apiKey, model, baseUrl);

      case "bedrock":
        validateApiKey(BEDROCK_ACCESS_KEY_FLAG.get(), "Bedrock (access key)");
        validateApiKey(BEDROCK_SECRET_KEY_FLAG.get(), "Bedrock (secret key)");
        return new BedrockProvider(
            BEDROCK_ACCESS_KEY_FLAG.get(),
            BEDROCK_SECRET_KEY_FLAG.get(),
            BEDROCK_SESSION_TOKEN_FLAG.get(),
            BEDROCK_REGION_FLAG.get(),
            model,
            reasoningEffort);

      case "vertex":
        validateApiKey(VERTEX_PROJECT_FLAG.get(), "Vertex (project)");
        validateApiKey(VERTEX_SERVICE_ACCOUNT_FLAG.get(), "Vertex (service account)");
        return new VertexProvider(
            VERTEX_PROJECT_FLAG.get(),
            VERTEX_REGION_FLAG.get(),
            VERTEX_SERVICE_ACCOUNT_FLAG.get(),
            model,
            reasoningEffort);

      default:
        throw new LLMProviderException(
            "Unknown LLM provider: " + providerName,
            "The configured AI provider is not supported. "
                + "Supported providers: anthropic, anthropic-compatible, openai, gemini, "
                + "ollama, minimax, openrouter, openai-compatible, bedrock, vertex.");
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

  private static void validateBaseUrl(String baseUrl, String providerName)
      throws LLMProviderException {
    if (baseUrl == null || baseUrl.isEmpty()) {
      throw new LLMProviderException(
          "No base URL configured for " + providerName
              + ". Set the ai.agent.base.url system property.",
          "The AI agent is not configured. Please ask your administrator "
              + "to set up the provider URL.");
    }
  }
}
