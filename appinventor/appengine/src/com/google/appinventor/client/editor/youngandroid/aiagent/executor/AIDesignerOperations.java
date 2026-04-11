// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.aiagent.executor;

import com.google.appinventor.client.editor.simple.SimpleComponentDatabase;
import com.google.appinventor.client.editor.simple.components.MockComponent;
import com.google.appinventor.client.editor.simple.components.MockContainer;
import com.google.appinventor.client.editor.simple.components.MockForm;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;
import com.google.appinventor.client.editor.youngandroid.aiagent.AIJsonUtils;
import com.google.appinventor.client.properties.json.ClientJsonParser;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;

import java.util.Map;

/**
 * Executes synchronous designer operations: ADD_COMPONENT, SET_PROPERTY,
 * RENAME_COMPONENT, DELETE_COMPONENT.
 */
final class AIDesignerOperations {

  private AIDesignerOperations() {}

  static void executeAddComponent(JSONObject json, ScreenExecutionContext context) {
    String type = AIJsonUtils.getStringField(json, "component_type");
    String name = AIJsonUtils.getStringField(json, "name");
    String parent = AIJsonUtils.getStringField(json, "parent");

    YaFormEditor formEditor = context.getFormEditor();
    MockForm form = formEditor.getForm();

    // Determine the container to add to.
    MockContainer container;
    if (parent != null && !parent.isEmpty()) {
      Map<String, MockComponent> components = formEditor.getComponents();
      MockComponent parentComp = components.get(parent);
      if (parentComp instanceof MockContainer) {
        container = (MockContainer) parentComp;
      } else {
        container = form;
      }
    } else {
      container = form;
    }

    // Build a minimal JSON properties object in the format expected by
    // DesignerEditor.createMockComponent():
    //   { "$Type": "Button", "$Version": "7", ... }
    // We intentionally omit $Name here. createMockComponent auto-generates a
    // name (e.g., "Button1") and fires fireComponentAdded, which registers
    // that auto-name in Blockly's componentDb_. We then call rename() below
    // to set the desired name, which fires fireComponentRenamed — the same
    // path as a manual user renaming via the properties panel.
    SimpleComponentDatabase db = formEditor.getComponentDatabase();
    StringBuilder sb = new StringBuilder();
    sb.append("{\"$Type\":\"").append(type).append("\",");
    sb.append("\"$Version\":\"").append(db.getComponentVersion(type)).append("\"");

    // Append any initial property values from the payload.
    if (json.containsKey("properties")) {
      JSONValue propsVal = json.get("properties");
      if (propsVal.isObject() != null) {
        JSONObject props = propsVal.isObject();
        for (String propName : props.keySet()) {
          JSONValue propValue = props.get(propName);
          String valueStr = (propValue.isString() != null)
              ? propValue.isString().stringValue()
              : propValue.toString();
          sb.append(",\"").append(propName).append("\":\"")
              .append(AIJsonUtils.escapeJsonString(valueStr)).append("\"");
        }
      }
    }
    sb.append("}");

    ClientJsonParser sharedParser = new ClientJsonParser();
    com.google.appinventor.shared.properties.json.JSONObject propertiesObject =
        sharedParser.parse(sb.toString()).asObject();
    MockComponent created = formEditor.addMockComponent(propertiesObject, container);

    // Rename to the AI-specified name. rename() fires fireComponentRenamed,
    // which notifies BlocksEditor.onComponentRenamed() to update Blockly's
    // componentDb_.
    if (created != null && !name.equals(created.getName())) {
      created.rename(name);
    }
  }

  static void executeSetProperty(JSONObject json, ScreenExecutionContext context) {
    String component = AIJsonUtils.getStringField(json, "component_name");
    String property = AIJsonUtils.getStringField(json, "property_name");
    JSONValue valueJson = json.get("value");
    String value = (valueJson.isString() != null)
        ? valueJson.isString().stringValue()
        : valueJson.toString();

    YaFormEditor formEditor = context.getFormEditor();
    Map<String, MockComponent> components = formEditor.getComponents();
    MockComponent comp = components.get(component);
    comp.changeProperty(property, value);
  }

  static void executeRenameComponent(JSONObject json, ScreenExecutionContext context) {
    String oldName = AIJsonUtils.getStringField(json, "old_name");
    String newName = AIJsonUtils.getStringField(json, "new_name");

    YaFormEditor formEditor = context.getFormEditor();
    Map<String, MockComponent> components = formEditor.getComponents();
    MockComponent comp = components.get(oldName);
    comp.rename(newName);
  }

  static void executeDeleteComponent(JSONObject json, ScreenExecutionContext context) {
    String name = AIJsonUtils.getStringField(json, "name");

    YaFormEditor formEditor = context.getFormEditor();
    Map<String, MockComponent> components = formEditor.getComponents();
    MockComponent comp = components.get(name);
    comp.delete();
  }
}
