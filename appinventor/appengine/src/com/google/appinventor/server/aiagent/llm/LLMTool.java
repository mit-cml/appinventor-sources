// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent.llm;

/**
 * Definition of a tool that can be presented to an LLM provider.
 *
 * <p>Each tool has a name, a human-readable description, and a JSON Schema
 * string describing the parameters it accepts. The provider implementations
 * translate this generic representation into the format required by their
 * specific API (Anthropic tool_use, OpenAI functions, Gemini function
 * declarations, etc.).
 */
public class LLMTool {

  private final String name;
  private final String description;
  private final String parameterSchema;

  /**
   * Creates a new tool definition.
   *
   * @param name            the tool name (e.g. "add_component")
   * @param description     a human-readable description of what the tool does
   * @param parameterSchema a JSON Schema string describing the tool parameters
   */
  public LLMTool(String name, String description, String parameterSchema) {
    this.name = name;
    this.description = description;
    this.parameterSchema = parameterSchema;
  }

  /**
   * Returns the tool name.
   *
   * @return the tool name
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the human-readable description.
   *
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Returns the JSON Schema string for the tool parameters.
   *
   * @return the parameter schema JSON string
   */
  public String getParameterSchema() {
    return parameterSchema;
  }

  @Override
  public String toString() {
    return "LLMTool{name='" + name + "'}";
  }
}
