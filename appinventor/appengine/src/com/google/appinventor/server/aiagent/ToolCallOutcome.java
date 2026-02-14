// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent;

/**
 * Outcome status for an individual tool call after server-side parsing and
 * mode enforcement.
 *
 * <p>Client-side execution outcomes (succeeded, failed, skipped) are reported
 * separately through the {@code reportExecutionErrors} RPC using structured
 * string prefixes ({@code SUCCEEDED:}, {@code FAILED:}, {@code SKIPPED:}).
 */
public enum ToolCallOutcome {
  /** Tool call passed parsing and enforcement and was sent to the client. */
  ACCEPTED,
  /** Tool call was rejected by server-side parsing (bad JSON, unknown tool, etc.). */
  PARSE_REJECTED,
  /** Tool call was rejected by ModeEnforcer (wrong mode/view). */
  MODE_REJECTED
}
