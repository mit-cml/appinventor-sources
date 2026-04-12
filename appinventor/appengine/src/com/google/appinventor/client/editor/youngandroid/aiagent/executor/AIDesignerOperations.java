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
    //   { "$Type": "Button", "$Version": "7" }
    // We intentionally omit $Name here. createMockComponent auto-generates a
    // name (e.g., "Button1") and fires fireComponentAdded, which registers
    // that auto-name in Blockly's componentDb_. We then call rename() below
    // to set the desired name, which fires fireComponentRenamed — the same
    // path as a manual user renaming via the properties panel.
    //
    // We deliberately do NOT include LLM-supplied properties in this initial
    // JSON. The palette flow (SimpleComponentDescriptor.createMockComponentFromPalette)
    // creates the bare component, then calls onCreateFromPalette() to seed
    // defaults like "Text for Label1". We mirror that sequence below so the
    // mock preview shows the same defaults a user would see, then apply
    // LLM-supplied properties on top so they win over the palette defaults.
    SimpleComponentDatabase db = formEditor.getComponentDatabase();
    String bareJson = "{\"$Type\":\"" + type + "\",\"$Version\":\""
        + db.getComponentVersion(type) + "\"}";

    MockComponent created = formEditor.addMockComponent(
        new ClientJsonParser().parse(bareJson).asObject(), container);

    if (created != null) {
      // Seed palette defaults (e.g. MockLabel sets Text to "Text for Label1")
      // so the mock preview matches what a user sees when dragging from the
      // palette.
      created.onCreateFromPalette();

      // Apply LLM-supplied properties on top of the palette defaults.
      if (json.containsKey("properties")) {
        JSONValue propsVal = json.get("properties");
        if (propsVal.isObject() != null) {
          JSONObject props = propsVal.isObject();
          for (String propName : props.keySet()) {
            JSONValue propValue = props.get(propName);
            String valueStr = (propValue.isString() != null)
                ? propValue.isString().stringValue()
                : propValue.toString();
            valueStr = normalizePropertyValue(valueStr);
            created.changeProperty(propName, valueStr);
          }
        }
      }

      // Rename to the AI-specified name. rename() fires fireComponentRenamed,
      // which notifies BlocksEditor.onComponentRenamed() to update Blockly's
      // componentDb_.
      if (!name.equals(created.getName())) {
        created.rename(name);
      }
    }
  }

  static void executeSetProperty(JSONObject json, ScreenExecutionContext context) {
    String component = AIJsonUtils.getStringField(json, "component_name");
    String property = AIJsonUtils.getStringField(json, "property_name");
    JSONValue valueJson = json.get("value");
    String value = (valueJson.isString() != null)
        ? valueJson.isString().stringValue()
        : valueJson.toString();
    value = normalizePropertyValue(value);

    YaFormEditor formEditor = context.getFormEditor();
    Map<String, MockComponent> components = formEditor.getComponents();
    MockComponent comp = components.get(component);
    comp.changeProperty(property, value);
  }

  /**
   * Normalizes values coming from the LLM to shapes the designer expects.
   *
   * <p>Color literals must use {@code &HAARRGGBB}. The LLM occasionally emits
   * {@code &AARRGGBB} (missing the {@code H}) or {@code #AARRGGBB}, both of
   * which would crash {@code Long.parseLong}. Rewrite those to the canonical
   * form so a single typo doesn't abort a whole operation batch.
   */
  static String normalizePropertyValue(String value) {
    if (value == null || value.length() < 2) {
      return value;
    }
    char first = value.charAt(0);
    if (first == '&' && value.charAt(1) != 'H' && value.charAt(1) != 'h'
        && isHex(value, 1)) {
      return "&H" + value.substring(1);
    }
    if (first == '#' && isHex(value, 1)) {
      return "&H" + value.substring(1);
    }
    return value;
  }

  private static boolean isHex(String s, int from) {
    int len = s.length();
    if (len - from != 6 && len - from != 8) {
      return false;
    }
    for (int i = from; i < len; i++) {
      char c = s.charAt(i);
      if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f')
          || (c >= 'A' && c <= 'F'))) {
        return false;
      }
    }
    return true;
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
