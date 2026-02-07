// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent.llm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Response from an LLM provider after a chat call.
 *
 * <p>Contains the assistant's text reply, any raw tool calls the provider
 * wishes to invoke, and an optional provider reference (used by stateful
 * providers like OpenAI and Gemini to resume conversations).
 */
public class LLMResponse {

  private final String text;
  private final List<RawToolCall> rawToolCalls;
  private final String providerRef;

  /**
   * Creates a new LLM response.
   *
   * @param text         the assistant's text reply (may be null or empty)
   * @param rawToolCalls the list of raw tool calls (may be null or empty)
   * @param providerRef  an opaque reference for stateful providers (may be null)
   */
  public LLMResponse(String text, List<RawToolCall> rawToolCalls, String providerRef) {
    this.text = text;
    this.rawToolCalls = rawToolCalls != null
        ? Collections.unmodifiableList(new ArrayList<>(rawToolCalls))
        : Collections.<RawToolCall>emptyList();
    this.providerRef = providerRef;
  }

  /**
   * Returns the assistant's text reply.
   *
   * @return the text, or null if the response contains only tool calls
   */
  public String getText() {
    return text;
  }

  /**
   * Returns the list of raw tool calls.
   *
   * @return an unmodifiable list of raw tool calls (never null)
   */
  public List<RawToolCall> getRawToolCalls() {
    return rawToolCalls;
  }

  /**
   * Returns the opaque provider reference for stateful providers.
   *
   * @return the provider reference, or null for stateless providers
   */
  public String getProviderRef() {
    return providerRef;
  }

  /**
   * Returns true if this response contains one or more tool calls.
   *
   * @return true if there are tool calls
   */
  public boolean hasToolCalls() {
    return !rawToolCalls.isEmpty();
  }

  @Override
  public String toString() {
    return "LLMResponse{text='" + (text != null ? text.substring(0, Math.min(text.length(), 50)) : "null")
        + "', toolCalls=" + rawToolCalls.size()
        + ", providerRef='" + providerRef + "'}";
  }
}
