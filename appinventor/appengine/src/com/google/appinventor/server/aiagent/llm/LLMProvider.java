// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent.llm;

import java.util.List;

/**
 * Interface for LLM provider implementations.
 *
 * <p>Each provider wraps a specific LLM API (Anthropic, OpenAI, Gemini,
 * Ollama) and translates between the generic tool/message format used by
 * the AI agent service and the provider-specific wire format.
 *
 * <p>Providers are responsible for their own internal tool-use loop: when
 * the LLM returns read-only tool calls, the provider resolves them via
 * the supplied {@link ReadOnlyToolResolver} and re-calls the LLM, up to
 * a maximum number of iterations.
 */
public interface LLMProvider {

  /**
   * Sends a chat request to the LLM and returns the response.
   *
   * <p>The provider will handle the internal tool-use loop for read-only
   * tools. Any non-read-only tool calls will be included in the returned
   * {@link LLMResponse#getRawToolCalls()}.
   *
   * @param systemPrompt the system prompt to set context
   * @param userMessage  the user's message
   * @param tools        the list of tool definitions available to the LLM
   * @param providerRef  opaque reference from a previous response (for
   *                     stateful providers), or null for the first call
   * @param history      conversation history (role + text pairs)
   * @param resolver     callback for resolving read-only tool calls
   * @return the LLM response
   * @throws LLMProviderException if the API call fails
   */
  LLMResponse chat(String systemPrompt, String userMessage, List<LLMTool> tools,
      String providerRef, List<ChatMessage> history, ReadOnlyToolResolver resolver)
      throws LLMProviderException;

  /**
   * Returns true if this provider is stateless (does not use providerRef
   * for conversation continuity). Stateless providers require the full
   * conversation history to be passed on each call.
   *
   * @return true if stateless
   */
  boolean isStateless();
}
