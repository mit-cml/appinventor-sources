// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent;

/**
 * Exception thrown when pseudocode text cannot be parsed into valid block
 * structures. Carries a human-readable message that can be fed back to the
 * LLM so it can correct its output.
 */
public class PseudocodeParseException extends Exception {

  private final int line;

  /**
   * Creates an exception with the given message.
   *
   * @param message description of the parse error
   */
  public PseudocodeParseException(String message) {
    super(message);
    this.line = -1;
  }

  /**
   * Creates an exception with the given message and cause.
   *
   * @param message description of the parse error
   * @param cause the underlying cause
   */
  public PseudocodeParseException(String message, Throwable cause) {
    super(message, cause);
    this.line = -1;
  }

  /**
   * Creates an exception referencing a specific source line.
   *
   * @param message description of the parse error
   * @param line the 1-based line number where the error occurred
   */
  public PseudocodeParseException(String message, int line) {
    super("Line " + line + ": " + message);
    this.line = line;
  }

  /**
   * Returns the 1-based line number where the error occurred, or -1 if not
   * associated with a specific line.
   */
  public int getLine() {
    return line;
  }
}
