// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent.llm;

/**
 * Checked exception thrown when a read-only tool call cannot be resolved.
 *
 * <p>The exception message is suitable for injection back into the LLM
 * conversation as a tool result error, so the LLM can adjust its behavior.
 */
public class ReadOnlyToolException extends Exception {

  /**
   * Creates a new read-only tool exception.
   *
   * @param message description of why the tool call failed, suitable for
   *                injection as a tool result error to the LLM
   */
  public ReadOnlyToolException(String message) {
    super(message);
  }

  /**
   * Creates a new read-only tool exception with a cause.
   *
   * @param message description of why the tool call failed
   * @param cause   the underlying exception
   */
  public ReadOnlyToolException(String message, Throwable cause) {
    super(message, cause);
  }
}
