// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent;

import com.google.appinventor.shared.rpc.aiagent.AIOperation;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Stage 1 structural validation of raw LLM tool calls.
 * Converts raw tool name + JSON argument strings into validated AIOperation objects.
 * Rejects unknown tool names, malformed JSON, and missing required parameters.
 */
public class LLMResponseParser {

  /**
   * Result of parsing raw tool calls.
   */
  public static class ParseResult {
    private final List<AIOperation> operations;
    private final List<String> errors;

    public ParseResult(List<AIOperation> operations, List<String> errors) {
      this.operations = operations;
      this.errors = errors;
    }

    public List<AIOperation> getOperations() {
      return operations;
    }

    public List<String> getErrors() {
      return errors;
    }

    public boolean hasErrors() {
      return !errors.isEmpty();
    }
  }

  /**
   * A raw tool call from the LLM provider (tool name + arguments JSON string).
   */
  public static class RawToolCall {
    private final String name;
    private final String argumentsJson;

    public RawToolCall(String name, String argumentsJson) {
      this.name = name;
      this.argumentsJson = argumentsJson;
    }

    public String getName() {
      return name;
    }

    public String getArgumentsJson() {
      return argumentsJson;
    }
  }

  private static final Map<String, AIOperation.Type> TOOL_NAME_TO_TYPE = new HashMap<>();
  private static final Map<String, List<String>> REQUIRED_FIELDS = new HashMap<>();
  private static final Set<String> KNOWN_TOOLS = new HashSet<>();

  static {
    TOOL_NAME_TO_TYPE.put("add_component", AIOperation.Type.ADD_COMPONENT);
    TOOL_NAME_TO_TYPE.put("delete_component", AIOperation.Type.DELETE_COMPONENT);
    TOOL_NAME_TO_TYPE.put("set_property", AIOperation.Type.SET_PROPERTY);
    TOOL_NAME_TO_TYPE.put("rename_component", AIOperation.Type.RENAME_COMPONENT);
    TOOL_NAME_TO_TYPE.put("set_event_handler", AIOperation.Type.SET_EVENT_HANDLER);
    TOOL_NAME_TO_TYPE.put("delete_event_handler", AIOperation.Type.DELETE_EVENT_HANDLER);
    TOOL_NAME_TO_TYPE.put("set_variable", AIOperation.Type.SET_VARIABLE);
    TOOL_NAME_TO_TYPE.put("delete_variable", AIOperation.Type.DELETE_VARIABLE);
    TOOL_NAME_TO_TYPE.put("set_procedure", AIOperation.Type.SET_PROCEDURE);
    TOOL_NAME_TO_TYPE.put("delete_procedure", AIOperation.Type.DELETE_PROCEDURE);
    TOOL_NAME_TO_TYPE.put("switch_screen", AIOperation.Type.SWITCH_SCREEN);
    TOOL_NAME_TO_TYPE.put("create_screen", AIOperation.Type.CREATE_SCREEN);
    TOOL_NAME_TO_TYPE.put("delete_screen", AIOperation.Type.DELETE_SCREEN);
    TOOL_NAME_TO_TYPE.put("set_project_property", AIOperation.Type.SET_PROJECT_PROP);

    REQUIRED_FIELDS.put("add_component", Arrays.asList("component_type", "name"));
    REQUIRED_FIELDS.put("delete_component", Collections.singletonList("name"));
    REQUIRED_FIELDS.put("set_property", Arrays.asList("component_name", "property_name", "value"));
    REQUIRED_FIELDS.put("rename_component", Arrays.asList("old_name", "new_name"));
    REQUIRED_FIELDS.put("set_event_handler", Arrays.asList("component_name", "event_name", "body"));
    REQUIRED_FIELDS.put("delete_event_handler", Arrays.asList("component_name", "event_name"));
    REQUIRED_FIELDS.put("set_variable", Arrays.asList("name", "initial_value"));
    REQUIRED_FIELDS.put("delete_variable", Collections.singletonList("name"));
    REQUIRED_FIELDS.put("set_procedure", Arrays.asList("name", "body"));
    REQUIRED_FIELDS.put("delete_procedure", Collections.singletonList("name"));
    REQUIRED_FIELDS.put("switch_screen", Collections.singletonList("screen_name"));
    REQUIRED_FIELDS.put("create_screen", Collections.singletonList("screen_name"));
    REQUIRED_FIELDS.put("delete_screen", Collections.singletonList("screen_name"));
    REQUIRED_FIELDS.put("set_project_property", Arrays.asList("property", "value"));

    KNOWN_TOOLS.addAll(TOOL_NAME_TO_TYPE.keySet());
    // Read-only tools are handled separately by the provider; not parsed here
    KNOWN_TOOLS.add("lookup_component");
    KNOWN_TOOLS.add("lookup_screen");
  }

  /**
   * Parse raw tool calls into validated AIOperation objects.
   *
   * @param rawToolCalls the raw tool calls from the LLM provider
   * @return parse result with operations and any errors
   */
  public ParseResult parseToolCalls(List<RawToolCall> rawToolCalls) {
    List<AIOperation> operations = new ArrayList<>();
    List<String> errors = new ArrayList<>();

    for (RawToolCall call : rawToolCalls) {
      String toolName = call.getName();

      // Skip read-only tools (already resolved by the provider)
      if ("lookup_component".equals(toolName) || "lookup_screen".equals(toolName)) {
        continue;
      }

      if (!KNOWN_TOOLS.contains(toolName)) {
        errors.add("Unknown tool: " + toolName);
        continue;
      }

      AIOperation.Type type = TOOL_NAME_TO_TYPE.get(toolName);
      if (type == null) {
        errors.add("Unmapped tool: " + toolName);
        continue;
      }

      JSONObject args;
      try {
        args = new JSONObject(call.getArgumentsJson());
      } catch (JSONException e) {
        errors.add("Malformed JSON arguments for " + toolName + ": " + e.getMessage());
        continue;
      }

      // Check required fields
      List<String> requiredFields = REQUIRED_FIELDS.get(toolName);
      if (requiredFields != null) {
        boolean missingField = false;
        for (String field : requiredFields) {
          if (!args.has(field) || args.isNull(field)) {
            errors.add("Missing required field '" + field + "' for " + toolName);
            missingField = true;
          }
        }
        if (missingField) {
          continue;
        }
      }

      operations.add(new AIOperation(type, args.toString()));
    }

    return new ParseResult(operations, errors);
  }

  /**
   * Build a structured error message suitable for LLM retry feedback.
   */
  public static String buildValidationErrorFeedback(List<String> errors) {
    StringBuilder sb = new StringBuilder();
    sb.append("The following errors were found in your tool calls:\n");
    for (int i = 0; i < errors.size(); i++) {
      sb.append(i + 1).append(". ").append(errors.get(i)).append("\n");
    }
    sb.append("\nPlease fix these issues and try again.");
    return sb.toString();
  }
}
