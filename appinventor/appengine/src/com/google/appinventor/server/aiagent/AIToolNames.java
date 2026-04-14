// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent;

/**
 * Canonical tool name constants used across the AI agent system.
 *
 * <p>Each constant corresponds to a tool exposed to the LLM via
 * {@link AIContextBuilder#buildTools} and parsed by
 * {@link LLMResponseParser#parseToolCalls}.
 */
public final class AIToolNames {

  private AIToolNames() {
    // Utility class — no instantiation.
  }

  // ---------- Read-only tools (all modes) ----------

  public static final String LOOKUP_COMPONENT = "lookup_component";
  public static final String LOOKUP_SCREEN = "lookup_screen";

  // ---------- Planning tools ----------

  public static final String PROPOSE_PLAN = "propose_plan";

  // ---------- Designer-view tools ----------

  public static final String ADD_COMPONENT = "add_component";
  public static final String DELETE_COMPONENT = "delete_component";
  public static final String SET_PROPERTY = "set_property";
  public static final String RENAME_COMPONENT = "rename_component";

  // ---------- Blocks-view tools ----------

  public static final String WRITE_BLOCK = "write_block";
  public static final String DELETE_BLOCK = "delete_block";

  // ---------- Navigation tools ----------

  public static final String TOGGLE_EDITOR = "toggle_editor";

  // ---------- Project-level tools (ProjectEditor mode) ----------

  public static final String SWITCH_SCREEN = "switch_screen";

  public static final String CREATE_SCREEN = "create_screen";
  public static final String DELETE_SCREEN = "delete_screen";
  public static final String SET_PROJECT_PROPERTY = "set_project_property";

  // ---------- Companion runtime reads (client-resolved) ----------

  public static final String READ_COMPONENT_PROPERTY = "read_component_property";
  public static final String READ_VARIABLE = "read_variable";
  public static final String READ_RECENT_LOGS = "read_recent_logs";
}
