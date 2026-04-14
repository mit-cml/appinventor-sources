// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.aiagent.validator;

import com.google.appinventor.client.editor.simple.SimpleComponentDatabase;
import com.google.appinventor.client.editor.simple.components.MockComponent;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;
import com.google.appinventor.client.editor.youngandroid.aiagent.AIEditorState;
import com.google.appinventor.client.editor.youngandroid.aiagent.AIJsonUtils;
import com.google.appinventor.shared.simple.ComponentDatabaseInterface.BlockPropertyDefinition;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.regexp.shared.RegExp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Validates Companion runtime-read operations (read_component_property,
 * read_variable, read_recent_logs) before they are dispatched through
 * the CompanionBridge. Returns null when valid, or a structured error
 * message (with suggested alternatives where applicable) otherwise.
 */
public final class CompanionReadValidator {

  private CompanionReadValidator() {}

  // Identifier pattern: must start with a letter or underscore, followed
  // by any combination of letters, digits, or underscores.  Matches the
  // pattern the CompanionBridge (Task 11) will use.
  private static final RegExp IDENTIFIER = RegExp.compile("^[A-Za-z_][A-Za-z0-9_]*$");

  // ---------------------------------------------------------------------------
  // Public validate methods
  // ---------------------------------------------------------------------------

  /**
   * Validate a read_component_property tool call.
   *
   * @param json tool call arguments — must include component_name and property_name
   * @return null if valid, or an error message suitable for returning to the LLM
   */
  public static String validateReadComponentProperty(JSONObject json) {
    String componentName = AIJsonUtils.getStringField(json, "component_name");
    String propertyName = AIJsonUtils.getStringField(json, "property_name");

    // 1. Required fields present and non-empty.
    if (componentName == null || componentName.isEmpty()) {
      return "read_component_property: missing 'component_name' field";
    }
    if (propertyName == null || propertyName.isEmpty()) {
      return "read_component_property: missing 'property_name' field";
    }

    // 2. Identifier regex on both.
    if (!isValidIdentifier(componentName)) {
      return "read_component_property: '" + componentName + "' is not a valid identifier";
    }
    if (!isValidIdentifier(propertyName)) {
      return "read_component_property: '" + propertyName + "' is not a valid identifier";
    }

    // 3. Component exists on current screen.
    YaFormEditor formEditor = AIEditorState.getCurrentFormEditor();
    if (formEditor == null) {
      return "read_component_property: no form editor available for current screen";
    }
    Map<String, MockComponent> components = formEditor.getComponents();
    MockComponent component = components.get(componentName);
    if (component == null) {
      return "read_component_property: component '" + componentName
          + "' does not exist. Available components: "
          + joinKeysAlpha(components);
    }

    // 4. Property exists on the component's type and is readable.
    SimpleComponentDatabase db = (SimpleComponentDatabase) formEditor.getComponentDatabase();
    String componentType = component.getType();
    List<BlockPropertyDefinition> props;
    try {
      props = db.getBlockPropertyDefinitions(componentType);
    } catch (Exception e) {
      return "read_component_property: unknown component type '" + componentType + "'";
    }

    BlockPropertyDefinition matching = null;
    for (BlockPropertyDefinition p : props) {
      if (p.getName().equals(propertyName)) {
        matching = p;
        break;
      }
    }
    if (matching == null) {
      return "read_component_property: '" + componentName + "' (type " + componentType
          + ") has no property '" + propertyName + "'. Readable properties: "
          + joinReadableProperties(props);
    }

    // Readable = "read-write" or "read-only". Reject "write-only" and "invisible".
    String rw = matching.getRW();
    if ("write-only".equals(rw) || "invisible".equals(rw)) {
      return "read_component_property: property '" + propertyName + "' on '"
          + componentName + "' is not readable (rw=" + rw + ").";
    }

    return null;
  }

  /**
   * Validate a read_variable tool call.
   *
   * @param json tool call arguments — must include variable_name
   * @param blocksYail YAIL of the current screen, used to verify the variable exists
   * @return null if valid, or an error message
   */
  public static String validateReadVariable(JSONObject json, String blocksYail) {
    String variableName = AIJsonUtils.getStringField(json, "variable_name");
    if (variableName == null || variableName.isEmpty()) {
      return "read_variable: missing 'variable_name' field";
    }
    if (!isValidIdentifier(variableName)) {
      return "read_variable: '" + variableName + "' is not a valid identifier";
    }
    if (blocksYail == null || blocksYail.isEmpty()) {
      return "read_variable: no blocks available on the current screen";
    }
    // Look for "(def g$<name> " in YAIL — simple string search matches the
    // codegen convention in runtime.scm where globals are declared as
    // (def g$<name> <initial-value>).
    String marker = "(def g$" + variableName + " ";
    if (blocksYail.indexOf(marker) < 0) {
      return "read_variable: no global variable named '" + variableName
          + "' on the current screen.";
    }
    return null;
  }

  /**
   * Validate a read_recent_logs tool call.
   *
   * @param json tool call arguments — optional n (default 20, clamped 1..50)
   * @return null if valid, or an error message
   */
  public static String validateReadRecentLogs(JSONObject json) {
    // n is optional; if provided, must be an integer in [1, 50].
    if (json.containsKey("n")) {
      JSONValue nVal = json.get("n");
      if (nVal == null || nVal.isNumber() == null) {
        return "read_recent_logs: 'n' is not a valid integer";
      }
      int ni = (int) nVal.isNumber().doubleValue();
      if (ni < 1 || ni > 50) {
        return "read_recent_logs: 'n' must be between 1 and 50 (got " + ni + ")";
      }
    }
    return null;
  }

  // ---------------------------------------------------------------------------
  // Private helpers
  // ---------------------------------------------------------------------------

  private static boolean isValidIdentifier(String s) {
    if (s == null || s.isEmpty()) {
      return false;
    }
    return IDENTIFIER.test(s);
  }

  private static String joinKeysAlpha(Map<String, ?> map) {
    List<String> keys = new ArrayList<String>(map.keySet());
    Collections.sort(keys);
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < keys.size(); i++) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append(keys.get(i));
    }
    return sb.toString();
  }

  private static String joinReadableProperties(List<BlockPropertyDefinition> props) {
    List<String> names = new ArrayList<String>();
    for (BlockPropertyDefinition p : props) {
      String rw = p.getRW();
      if (!"write-only".equals(rw) && !"invisible".equals(rw)) {
        names.add(p.getName());
      }
    }
    Collections.sort(names);
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < names.size(); i++) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append(names.get(i));
    }
    return sb.toString();
  }
}
