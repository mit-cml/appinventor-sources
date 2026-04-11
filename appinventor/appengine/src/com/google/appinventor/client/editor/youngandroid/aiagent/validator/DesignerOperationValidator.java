// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.aiagent.validator;

import com.google.appinventor.client.editor.simple.SimpleComponentDatabase;
import com.google.appinventor.client.editor.simple.components.MockComponent;
import com.google.appinventor.client.editor.simple.components.MockForm;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;
import com.google.appinventor.client.editor.youngandroid.aiagent.AIEditorState;
import com.google.appinventor.client.editor.youngandroid.aiagent.AIJsonUtils;
import com.google.appinventor.client.editor.youngandroid.aiagent.executor.ScreenExecutionContext;
import com.google.appinventor.client.youngandroid.TextValidators;
import com.google.gwt.json.client.JSONObject;

import java.util.Map;

/**
 * Validates designer AI operations: ADD_COMPONENT, DELETE_COMPONENT,
 * SET_PROPERTY, RENAME_COMPONENT.
 */
final class DesignerOperationValidator {

  private DesignerOperationValidator() {}

  /**
   * Returns the form editor to use for validation. Prefers the
   * ScreenExecutionContext if provided; falls back to the visible screen.
   */
  private static YaFormEditor resolveFormEditor(ScreenExecutionContext context) {
    if (context != null) {
      return context.getFormEditor();
    }
    return AIEditorState.getCurrentFormEditor();
  }

  static String validateAddComponent(JSONObject json, ScreenExecutionContext context) {
    String type = AIJsonUtils.getStringField(json, "component_type");
    String name = AIJsonUtils.getStringField(json, "name");
    if (type == null || type.isEmpty()) {
      return "ADD_COMPONENT: missing 'component_type' field";
    }
    if (name == null || name.isEmpty()) {
      return "ADD_COMPONENT: missing 'name' field";
    }
    if (!TextValidators.isValidIdentifier(name)) {
      return "ADD_COMPONENT: '" + name + "' is not a valid identifier";
    }

    YaFormEditor formEditor = resolveFormEditor(context);
    if (formEditor == null) {
      return "ADD_COMPONENT: no form editor available for current screen";
    }
    SimpleComponentDatabase db = formEditor.getComponentDatabase();
    try {
      db.getComponentType(type);
    } catch (Exception e) {
      return "ADD_COMPONENT: unknown component type '" + type + "'";
    }

    Map<String, MockComponent> components = formEditor.getComponents();
    if (components.containsKey(name)) {
      return "ADD_COMPONENT: component '" + name + "' already exists";
    }
    return null;
  }

  static String validateDeleteComponent(JSONObject json, ScreenExecutionContext context) {
    String name = AIJsonUtils.getStringField(json, "name");
    if (name == null || name.isEmpty()) {
      return "DELETE_COMPONENT: missing 'name' field";
    }

    YaFormEditor formEditor = resolveFormEditor(context);
    if (formEditor == null) {
      return "DELETE_COMPONENT: no form editor available for current screen";
    }
    Map<String, MockComponent> components = formEditor.getComponents();
    if (!components.containsKey(name)) {
      return "DELETE_COMPONENT: component '" + name + "' does not exist";
    }
    MockComponent comp = components.get(name);
    if (comp instanceof MockForm) {
      return "DELETE_COMPONENT: cannot delete the Form component";
    }
    return null;
  }

  static String validateSetProperty(JSONObject json, ScreenExecutionContext context) {
    String component = AIJsonUtils.getStringField(json, "component_name");
    String property = AIJsonUtils.getStringField(json, "property_name");
    if (component == null || component.isEmpty()) {
      return "SET_PROPERTY: missing 'component_name' field";
    }
    if (property == null || property.isEmpty()) {
      return "SET_PROPERTY: missing 'property_name' field";
    }
    if (!json.containsKey("value")) {
      return "SET_PROPERTY: missing 'value' field";
    }

    YaFormEditor formEditor = resolveFormEditor(context);
    if (formEditor == null) {
      return "SET_PROPERTY: no form editor available for current screen";
    }
    Map<String, MockComponent> components = formEditor.getComponents();
    if (!components.containsKey(component)) {
      return "SET_PROPERTY: component '" + component + "' does not exist";
    }
    return null;
  }

  static String validateRenameComponent(JSONObject json, ScreenExecutionContext context) {
    String oldName = AIJsonUtils.getStringField(json, "old_name");
    String newName = AIJsonUtils.getStringField(json, "new_name");
    if (oldName == null || oldName.isEmpty()) {
      return "RENAME_COMPONENT: missing 'old_name' field";
    }
    if (newName == null || newName.isEmpty()) {
      return "RENAME_COMPONENT: missing 'new_name' field";
    }
    if (!TextValidators.isValidIdentifier(newName)) {
      return "RENAME_COMPONENT: '" + newName + "' is not a valid identifier";
    }

    YaFormEditor formEditor = resolveFormEditor(context);
    if (formEditor == null) {
      return "RENAME_COMPONENT: no form editor available for current screen";
    }
    Map<String, MockComponent> components = formEditor.getComponents();
    if (!components.containsKey(oldName)) {
      return "RENAME_COMPONENT: component '" + oldName + "' does not exist";
    }
    if (components.containsKey(newName)) {
      return "RENAME_COMPONENT: component '" + newName + "' already exists";
    }
    return null;
  }
}
