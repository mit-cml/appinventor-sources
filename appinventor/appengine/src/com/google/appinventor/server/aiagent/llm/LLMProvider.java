// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent.llm;

import com.google.appinventor.server.aiagent.StreamBuffer;

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
   * @param systemPrompt  the system prompt to set context
   * @param contextMessages per-request context messages (e.g. mode instructions,
   *                        current screen state) each sent as a separate user
   *                        message before the user's message; may be null or empty
   * @param userMessage   the user's message
   * @param tools         the list of tool definitions available to the LLM
   * @param providerRef   opaque reference from a previous response (for
   *                      stateful providers), or null for the first call
   * @param history       conversation history (role + text pairs)
   * @param resolver      callback for resolving read-only tool calls
   * @return the LLM response
   * @throws LLMProviderException if the API call fails
   */
  LLMResponse chat(String systemPrompt, List<String> contextMessages, String userMessage,
      List<LLMTool> tools, String providerRef, List<ChatMessage> history,
      ReadOnlyToolResolver resolver, StreamBuffer streamBuffer) throws LLMProviderException;

  /**
   * Continues a conversation after operation tool calls have been applied.
   *
   * <p>When a previous {@link #chat} or {@code continueWithToolResults} call
   * returns an {@link LLMResponse} with {@link LLMResponse#hasMore()} set to
   * {@code true}, the caller should apply the operations and then call this
   * method to get the next batch. The provider sends synthetic "Done." results
   * for each pending tool call and re-calls the LLM.
   *
   * @param continuationState the serialized conversation state from
   *                          {@link LLMResponse#getProviderRef()}
   * @param tools             the tool definitions (same as the original call)
   * @param resolver          callback for resolving read-only tool calls
   * @return the next LLM response (may have {@code hasMore = true} again)
   * @throws LLMProviderException if the API call fails
   */
  LLMResponse continueWithToolResults(String continuationState, List<LLMTool> tools,
      ReadOnlyToolResolver resolver, StreamBuffer streamBuffer) throws LLMProviderException;

  /**
   * Returns true if this provider is stateless (does not use providerRef
   * for conversation continuity). Stateless providers require the full
   * conversation history to be passed on each call.
   *
   * @return true if stateless
   */
  boolean isStateless();
}
