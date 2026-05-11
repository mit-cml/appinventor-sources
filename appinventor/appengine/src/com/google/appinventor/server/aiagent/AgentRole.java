// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent;

/**
 * Roles the AI agent can play, used to route to per-role provider/model
 * configuration. Derived from the user-facing {@code AIAgentMode} and the
 * server-side {@link EnforcementContext}.
 *
 * <p>The {@link #flagKey()} method returns the lowercase token used to
 * compose role-keyed flag names of the form
 * {@code ai.agent.role.<flagKey>.<field>}.
 */
public enum AgentRole {
  ADVISOR("advisor"),
  SCREEN_EDITOR("screen_editor"),
  PROJECT_EDITOR("project_editor"),
  PROJECT_EDITOR_PLANNER("project_editor_planner"),
  PROJECT_EDITOR_CHILD("project_editor_child");

  private final String flagKey;

  AgentRole(String flagKey) {
    this.flagKey = flagKey;
  }

  /** Lowercase token used in role-keyed flag names. */
  public String flagKey() {
    return flagKey;
  }
}
