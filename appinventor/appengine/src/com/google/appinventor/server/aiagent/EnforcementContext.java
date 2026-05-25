// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent;

/**
 * Controls which operations are allowed during different orchestration phases.
 * Orthogonal to the user-facing AI mode (Advisor/ScreenEditor/ProjectEditor).
 */
public enum EnforcementContext {
  /** Current behavior - no orchestration active. */
  STANDARD,
  /** Planning phase - only PROPOSE_PLAN and read-only tools allowed. */
  PLANNING,
  /**
   * Post-plan execution phase for the parent agent. Combines STANDARD
   * write tools with PROPOSE_PLAN so the parent can handle small follow-up
   * changes directly or propose a new plan for complex requests.
   */
  EXECUTION,
  /** Child agent execution - screen-level ops only, no project-level or PROPOSE_PLAN. */
  CHILD_EXECUTION
}
