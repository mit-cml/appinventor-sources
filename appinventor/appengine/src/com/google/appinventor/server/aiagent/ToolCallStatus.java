// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent;

/**
 * Records the outcome of a single raw tool call from the LLM.
 * Used to build accurate feedback for the LLM in continuation and error-retry paths.
 */
public class ToolCallStatus {

  private final String toolName;
  private final String argumentsSummary;
  private final ToolCallOutcome outcome;
  private final String errorMessage;

  public ToolCallStatus(String toolName, String argumentsSummary,
      ToolCallOutcome outcome, String errorMessage) {
    this.toolName = toolName;
    this.argumentsSummary = argumentsSummary;
    this.outcome = outcome;
    this.errorMessage = errorMessage;
  }

  public String getToolName() {
    return toolName;
  }

  public String getArgumentsSummary() {
    return argumentsSummary;
  }

  public ToolCallOutcome getOutcome() {
    return outcome;
  }

  public String getErrorMessage() {
    return errorMessage;
  }
}
