// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent.llm;

/**
 * Checked exception thrown when an LLM API call fails.
 *
 * <p>Wraps transport errors, API errors, and response-parsing failures.
 * The {@link #getUserFacingMessage()} method returns a sanitized message
 * safe for display to end-users (no API keys, internal URLs, or stack
 * trace details).
 */
public class LLMProviderException extends Exception {

  private final String userFacingMessage;

  /**
   * Creates a new LLM provider exception.
   *
   * @param internalMessage detailed message for server logs
   * @param userFacingMessage sanitized message safe for display to users
   */
  public LLMProviderException(String internalMessage, String userFacingMessage) {
    super(internalMessage);
    this.userFacingMessage = userFacingMessage;
  }

  /**
   * Creates a new LLM provider exception wrapping a cause.
   *
   * @param internalMessage detailed message for server logs
   * @param userFacingMessage sanitized message safe for display to users
   * @param cause the underlying exception
   */
  public LLMProviderException(String internalMessage, String userFacingMessage, Throwable cause) {
    super(internalMessage, cause);
    this.userFacingMessage = userFacingMessage;
  }

  /**
   * Returns a sanitized message suitable for displaying to end-users.
   * This message will never contain API keys, internal URLs, or stack
   * trace information.
   *
   * @return the user-facing error message
   */
  public String getUserFacingMessage() {
    return userFacingMessage;
  }
}
