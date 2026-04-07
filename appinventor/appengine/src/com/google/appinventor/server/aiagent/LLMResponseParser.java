// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent;

import com.google.appinventor.shared.rpc.aiagent.AIOperation;

import org.json.JSONArray;
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
import java.util.logging.Logger;

/**
 * Stage 1 structural validation of raw LLM tool calls.
 * Converts raw tool name + JSON argument strings into validated AIOperation objects.
 * Rejects unknown tool names, malformed JSON, and missing required parameters.
 */
public class LLMResponseParser {

  private static final Logger LOG = Logger.getLogger(LLMResponseParser.class.getName());

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
    TOOL_NAME_TO_TYPE.put(AIToolNames.ADD_COMPONENT, AIOperation.Type.ADD_COMPONENT);
    TOOL_NAME_TO_TYPE.put(AIToolNames.DELETE_COMPONENT, AIOperation.Type.DELETE_COMPONENT);
    TOOL_NAME_TO_TYPE.put(AIToolNames.SET_PROPERTY, AIOperation.Type.SET_PROPERTY);
    TOOL_NAME_TO_TYPE.put(AIToolNames.RENAME_COMPONENT, AIOperation.Type.RENAME_COMPONENT);
    TOOL_NAME_TO_TYPE.put(AIToolNames.WRITE_BLOCK, AIOperation.Type.WRITE_BLOCK);
    TOOL_NAME_TO_TYPE.put(AIToolNames.DELETE_BLOCK, AIOperation.Type.DELETE_BLOCK);
    TOOL_NAME_TO_TYPE.put(AIToolNames.SWITCH_SCREEN, AIOperation.Type.SWITCH_SCREEN);
    TOOL_NAME_TO_TYPE.put(AIToolNames.CREATE_SCREEN, AIOperation.Type.CREATE_SCREEN);
    TOOL_NAME_TO_TYPE.put(AIToolNames.DELETE_SCREEN, AIOperation.Type.DELETE_SCREEN);
    TOOL_NAME_TO_TYPE.put(AIToolNames.SET_PROJECT_PROPERTY, AIOperation.Type.SET_PROJECT_PROP);
    TOOL_NAME_TO_TYPE.put(AIToolNames.TOGGLE_EDITOR, AIOperation.Type.TOGGLE_EDITOR);

    REQUIRED_FIELDS.put(AIToolNames.ADD_COMPONENT, Arrays.asList("component_type", "name"));
    REQUIRED_FIELDS.put(AIToolNames.DELETE_COMPONENT, Collections.singletonList("name"));
    REQUIRED_FIELDS.put(AIToolNames.SET_PROPERTY, Arrays.asList("component_name", "property_name", "value"));
    REQUIRED_FIELDS.put(AIToolNames.RENAME_COMPONENT, Arrays.asList("old_name", "new_name"));
    REQUIRED_FIELDS.put(AIToolNames.WRITE_BLOCK, Collections.singletonList("yail"));
    REQUIRED_FIELDS.put(AIToolNames.DELETE_BLOCK, Collections.singletonList("block"));
    REQUIRED_FIELDS.put(AIToolNames.SWITCH_SCREEN, Collections.singletonList("screen_name"));
    REQUIRED_FIELDS.put(AIToolNames.CREATE_SCREEN, Collections.singletonList("screen_name"));
    REQUIRED_FIELDS.put(AIToolNames.DELETE_SCREEN, Collections.singletonList("screen_name"));
    REQUIRED_FIELDS.put(AIToolNames.SET_PROJECT_PROPERTY, Arrays.asList("property", "value"));
    REQUIRED_FIELDS.put(AIToolNames.TOGGLE_EDITOR, Collections.singletonList("view"));

    KNOWN_TOOLS.addAll(TOOL_NAME_TO_TYPE.keySet());
    // Read-only tools are handled separately by the provider; not parsed here
    KNOWN_TOOLS.add(AIToolNames.LOOKUP_COMPONENT);
    KNOWN_TOOLS.add(AIToolNames.LOOKUP_SCREEN);
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
      AIDebug.log(LOG, "Parsing tool call: " + toolName + " args=" + call.getArgumentsJson());

      // Skip read-only tools (already resolved by the provider)
      if (AIToolNames.LOOKUP_COMPONENT.equals(toolName) || AIToolNames.LOOKUP_SCREEN.equals(toolName)) {
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

      // Type coercion for known fields
      try {
        coerceTypes(toolName, args);
        AIDebug.log(LOG, "Type coercion OK for " + toolName);
      } catch (JSONException e) {
        errors.add("Type coercion failed for " + toolName + ": " + e.getMessage());
        continue;
      }

      AIDebug.log(LOG, "Parsed operation: " + type + " payload=" + args.toString());
      operations.add(new AIOperation(type, args.toString()));
    }

    return new ParseResult(operations, errors);
  }

  /**
   * Coerce JSON fields to their expected types.
   * Ensures {@code properties} in {@code add_component} is a JSON object,
   * {@code parameters} in {@code set_procedure} is a JSON array, etc.
   */
  private void coerceTypes(String toolName, JSONObject args) throws JSONException {
    if (AIToolNames.ADD_COMPONENT.equals(toolName)) {
      Object properties = args.opt("properties");
      if (properties instanceof String) {
        args.put("properties", new JSONObject((String) properties));
      }
    }
    // write_block and delete_block carry string payloads only — no coercion needed
  }

}
