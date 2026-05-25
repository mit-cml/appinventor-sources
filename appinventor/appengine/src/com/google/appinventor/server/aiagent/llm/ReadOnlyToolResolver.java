// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent.llm;

/**
 * Callback interface for resolving read-only tool calls during the
 * provider's internal tool-use loop.
 *
 * <p>Read-only tools (e.g. {@code lookup_component}, {@code lookup_screen})
 * are resolved server-side without returning to the client. The provider
 * calls this resolver for each read-only tool call, injects the result
 * into the conversation, and re-calls the LLM.
 */
public interface ReadOnlyToolResolver {

  /**
   * Returns true if the named tool is a read-only tool that should be
   * resolved by this resolver rather than returned to the caller as an
   * operation.
   *
   * @param toolName the tool name to check
   * @return true if the tool is read-only
   */
  boolean isReadOnly(String toolName);

  /**
   * Resolves a read-only tool call and returns the result as a string.
   *
   * @param toolName the name of the tool to resolve
   * @param argsJson the JSON string of tool arguments
   * @return the tool result as a string
   * @throws ReadOnlyToolException if the tool call cannot be resolved
   */
  String resolve(String toolName, String argsJson) throws ReadOnlyToolException;
}
