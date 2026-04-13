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

    REQUIRED_FIELDS.put(AIToolNames.ADD_COMPONENT, Arrays.asList("component_type", "component_name"));
    REQUIRED_FIELDS.put(AIToolNames.DELETE_COMPONENT, Collections.singletonList("component_name"));
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
    // Orchestration tools
    KNOWN_TOOLS.add(AIToolNames.PROPOSE_PLAN);
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

    // Special case: if propose_plan is present, it's the only operation returned.
    boolean hasProposePlan = false;
    for (RawToolCall call : rawToolCalls) {
      if (AIToolNames.PROPOSE_PLAN.equals(call.getName())) {
        hasProposePlan = true;
        break;
      }
    }
    if (hasProposePlan) {
      for (RawToolCall call : rawToolCalls) {
        if (AIToolNames.PROPOSE_PLAN.equals(call.getName())) {
          String validationError = validatePlanStructure(call.getArgumentsJson());
          if (validationError != null) {
            errors.add("propose_plan: " + validationError);
          } else {
            operations.add(new AIOperation(AIOperation.Type.PROPOSE_PLAN,
                call.getArgumentsJson()));
          }
          break;
        }
      }
      return new ParseResult(operations, errors);
    }

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
        List<String> missing = new ArrayList<>();
        for (String field : requiredFields) {
          if (!args.has(field) || args.isNull(field)) {
            missing.add(field);
          }
        }
        if (!missing.isEmpty()) {
          // Include the keys the LLM actually provided so a retry can self-correct
          // when the model hallucinates similar-but-wrong names (e.g. "name" vs
          // "component_name", "parent" vs "parent_name").
          List<String> provided = new ArrayList<>(args.keySet());
          Collections.sort(provided);
          errors.add("Missing required field"
              + (missing.size() == 1 ? " " : "s ")
              + quoteJoin(missing) + " for " + toolName
              + ". Required: " + quoteJoin(requiredFields)
              + ". Provided: " + (provided.isEmpty() ? "(none)" : quoteJoin(provided))
              + ".");
          continue;
        }
      }

      // Per-tool semantic validation
      if (AIToolNames.CREATE_SCREEN.equals(toolName)) {
        String screenName = args.optString("screen_name");
        if ("__project__".equals(screenName)) {
          errors.add("Screen name '__project__' is reserved.");
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
   * Joins keys with single quotes and comma separators, e.g.
   * {@code 'component_type', 'component_name'}.
   */
  private static String quoteJoin(List<String> keys) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < keys.size(); i++) {
      if (i > 0) sb.append(", ");
      sb.append('\'').append(keys.get(i)).append('\'');
    }
    return sb.toString();
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

  /**
   * Validates the structure of a propose_plan JSON payload.
   *
   * <p>Checks for required top-level fields, per-step required fields, duplicate
   * step IDs, references to unknown steps in depends_on, and circular dependencies.
   *
   * @param json the raw JSON arguments string from the LLM tool call
   * @return an error message if validation fails, or {@code null} if the plan is valid
   */
  private String validatePlanStructure(String json) {
    try {
      JSONObject plan = new JSONObject(json);
      if (!plan.has("summary")) return "Missing 'summary' field.";
      if (!plan.has("steps")) return "Missing 'steps' array.";
      JSONArray steps = plan.getJSONArray("steps");
      if (steps.length() == 0) return "Plan must have at least one step.";

      Set<String> stepIds = new HashSet<>();
      Map<String, List<String>> deps = new HashMap<>();
      for (int i = 0; i < steps.length(); i++) {
        JSONObject step = steps.getJSONObject(i);
        if (!step.has("id") || !step.has("screen") || !step.has("description")) {
          return "Step " + i + " missing required field (id, screen, description).";
        }
        String id = step.getString("id");
        if (!stepIds.add(id)) return "Duplicate step ID: " + id;
        deps.put(id, new ArrayList<String>());
        if (step.has("depends_on")) {
          JSONArray depArray = step.getJSONArray("depends_on");
          for (int j = 0; j < depArray.length(); j++) {
            deps.get(id).add(depArray.getString(j));
          }
        }
      }
      for (Map.Entry<String, List<String>> entry : deps.entrySet()) {
        for (String dep : entry.getValue()) {
          if (!stepIds.contains(dep)) {
            return "Step " + entry.getKey() + " depends on unknown step: " + dep;
          }
        }
      }
      Set<String> visited = new HashSet<>();
      Set<String> inStack = new HashSet<>();
      for (String id : stepIds) {
        if (hasCycle(id, deps, visited, inStack)) {
          return "Circular dependency detected involving step: " + id;
        }
      }
      return null;
    } catch (Exception e) {
      return "Invalid JSON: " + e.getMessage();
    }
  }

  /**
   * Depth-first cycle detection for the step dependency graph.
   *
   * @param id      the current step ID being visited
   * @param deps    adjacency map from step ID to its dependency IDs
   * @param visited steps that have been fully explored (no cycle through them)
   * @param inStack steps on the current DFS recursion stack
   * @return {@code true} if a cycle is detected, {@code false} otherwise
   */
  private boolean hasCycle(String id, Map<String, List<String>> deps,
      Set<String> visited, Set<String> inStack) {
    if (inStack.contains(id)) return true;
    if (visited.contains(id)) return false;
    visited.add(id);
    inStack.add(id);
    for (String dep : deps.getOrDefault(id, Collections.<String>emptyList())) {
      if (hasCycle(dep, deps, visited, inStack)) return true;
    }
    inStack.remove(id);
    return false;
  }

}
