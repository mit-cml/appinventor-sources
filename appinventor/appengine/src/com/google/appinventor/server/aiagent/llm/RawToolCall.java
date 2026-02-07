// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent.llm;

/**
 * An unparsed tool call as returned by an LLM provider.
 *
 * <p>Contains the tool name and the raw JSON string of its arguments
 * exactly as the provider returned them. No validation or parsing of the
 * arguments is performed at this level; that responsibility belongs to
 * {@link com.google.appinventor.server.aiagent.LLMResponseParser}.
 */
public class RawToolCall {

  private final String name;
  private final String argumentsJson;

  /**
   * Creates a new raw tool call.
   *
   * @param name          the tool name as returned by the provider
   * @param argumentsJson the JSON string of tool arguments
   */
  public RawToolCall(String name, String argumentsJson) {
    this.name = name;
    this.argumentsJson = argumentsJson;
  }

  /**
   * Returns the tool name.
   *
   * @return the tool name string
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the raw JSON string of the tool arguments.
   *
   * @return the arguments JSON string
   */
  public String getArgumentsJson() {
    return argumentsJson;
  }

  @Override
  public String toString() {
    return "RawToolCall{name='" + name + "', argumentsJson='" + argumentsJson + "'}";
  }
}
