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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Stage 2 semantic validation and mode enforcement for AI-generated operations.
 * Validates operations against the component database and enforces the
 * AI agent mode restrictions (Advisor, ScreenEditor, ProjectEditor).
 */
public class AIOperationValidator {

  private static final Logger LOG = Logger.getLogger(AIOperationValidator.class.getName());

  /**
   * Result of validation.
   */
  public static class ValidationResult {
    private final List<AIOperation> accepted;
    private final List<String> errors;

    public ValidationResult(List<AIOperation> accepted, List<String> errors) {
      this.accepted = accepted;
      this.errors = errors;
    }

    public List<AIOperation> getAccepted() {
      return accepted;
    }

    public List<String> getErrors() {
      return errors;
    }

    public boolean hasErrors() {
      return !errors.isEmpty();
    }
  }

  private static final int MAX_OPERATIONS_PER_RESPONSE = 50;

  /** All write operation types (everything except read-only lookups). */
  private static final Set<AIOperation.Type> WRITE_OPS = new HashSet<>(Arrays.asList(
      AIOperation.Type.ADD_COMPONENT,
      AIOperation.Type.DELETE_COMPONENT,
      AIOperation.Type.SET_PROPERTY,
      AIOperation.Type.RENAME_COMPONENT,
      AIOperation.Type.SET_EVENT_HANDLER,
      AIOperation.Type.DELETE_EVENT_HANDLER,
      AIOperation.Type.SET_VARIABLE,
      AIOperation.Type.DELETE_VARIABLE,
      AIOperation.Type.SET_PROCEDURE,
      AIOperation.Type.DELETE_PROCEDURE,
      AIOperation.Type.SWITCH_SCREEN,
      AIOperation.Type.CREATE_SCREEN,
      AIOperation.Type.DELETE_SCREEN,
      AIOperation.Type.SET_PROJECT_PROP
  ));

  /** Project-level operations only allowed in ProjectEditor mode. */
  private static final Set<AIOperation.Type> PROJECT_LEVEL_OPS = new HashSet<>(Arrays.asList(
      AIOperation.Type.SWITCH_SCREEN,
      AIOperation.Type.CREATE_SCREEN,
      AIOperation.Type.DELETE_SCREEN,
      AIOperation.Type.SET_PROJECT_PROP
  ));

  /** Properties that the AI is never allowed to modify. */
  private static final Set<String> PROTECTED_PROPERTIES = new HashSet<>(Arrays.asList(
      "AIAgentMode",
      "Uuid"
  ));

  /**
   * Validate operations against mode restrictions.
   *
   * @param operations the operations to validate
   * @param mode "Off", "Advisor", "ScreenEditor", or "ProjectEditor"
   * @return validation result with accepted operations and errors
   */
  public ValidationResult validateForMode(List<AIOperation> operations, String mode) {
    List<AIOperation> accepted = new ArrayList<>();
    List<String> errors = new ArrayList<>();

    if ("Off".equals(mode)) {
      errors.add("AI agent is disabled for this project.");
      return new ValidationResult(accepted, errors);
    }

    if ("Advisor".equals(mode)) {
      for (AIOperation op : operations) {
        if (WRITE_OPS.contains(op.getType())) {
          String reason = "Advisor mode does not allow write operations. Rejected: " + op.getType();
          errors.add(reason);
          AIDebug.log(LOG, "Mode validation REJECTED: " + reason);
        } else {
          AIDebug.log(LOG, "Mode validation accepted: " + op.getType());
        }
      }
      if (!errors.isEmpty()) {
        return new ValidationResult(accepted, errors);
      }
    }

    if ("ScreenEditor".equals(mode)) {
      for (AIOperation op : operations) {
        if (PROJECT_LEVEL_OPS.contains(op.getType())) {
          String reason = "ScreenEditor mode does not allow project-level operations. "
              + "Rejected: " + op.getType();
          errors.add(reason);
          AIDebug.log(LOG, "Mode validation REJECTED: " + reason);
        } else {
          AIDebug.log(LOG, "Mode validation accepted: " + op.getType());
        }
      }
      if (!errors.isEmpty()) {
        return new ValidationResult(accepted, errors);
      }
    }

    // ProjectEditor allows all
    for (AIOperation op : operations) {
      AIDebug.log(LOG, "Mode validation accepted: " + op.getType());
    }
    accepted.addAll(operations);
    return new ValidationResult(accepted, errors);
  }

  /**
   * Validate operations semantically against the component database.
   *
   * @param operations the operations to validate
   * @param componentDb JSON string of the component database (simple_components.json)
   * @return validation result with accepted operations and errors
   */
  public ValidationResult validateOperations(List<AIOperation> operations, String componentDb) {
    List<AIOperation> accepted = new ArrayList<>();
    List<String> errors = new ArrayList<>();

    if (operations.size() > MAX_OPERATIONS_PER_RESPONSE) {
      errors.add("Too many operations in a single response: " + operations.size()
          + " (max " + MAX_OPERATIONS_PER_RESPONSE + ")");
      return new ValidationResult(accepted, errors);
    }

    Map<String, ComponentInfo> catalog = parseComponentDb(componentDb);

    for (AIOperation op : operations) {
      List<String> opErrors = validateSingleOperation(op, catalog);
      if (opErrors.isEmpty()) {
        accepted.add(op);
        AIDebug.log(LOG, "Semantic validation accepted: " + op.getType());
      } else {
        errors.addAll(opErrors);
        for (String err : opErrors) {
          AIDebug.log(LOG, "Semantic validation REJECTED " + op.getType() + ": " + err);
        }
      }
    }

    return new ValidationResult(accepted, errors);
  }

  private List<String> validateSingleOperation(AIOperation op,
      Map<String, ComponentInfo> catalog) {
    List<String> errors = new ArrayList<>();
    JSONObject payload;

    try {
      payload = new JSONObject(op.getPayload());
    } catch (Exception e) {
      errors.add("Invalid payload JSON for " + op.getType() + ": " + e.getMessage());
      return errors;
    }

    switch (op.getType()) {
      case ADD_COMPONENT:
        validateAddComponent(payload, catalog, errors);
        break;

      case SET_PROPERTY:
        validateProtectedProperty(
            payload.optString("property_name", ""),
            errors);
        break;

      case SET_PROJECT_PROP:
        String projectProp = payload.optString("property", "");
        if (PROTECTED_PROPERTIES.contains(projectProp)) {
          errors.add("Cannot modify protected project property: " + projectProp);
        }
        break;

      default:
        break;
    }

    return errors;
  }

  /**
   * Validate an ADD_COMPONENT operation against the component catalog.
   * Checks that the component type exists and that all specified properties
   * are valid for that type.
   */
  private void validateAddComponent(JSONObject payload,
      Map<String, ComponentInfo> catalog, List<String> errors) {
    String componentType = payload.optString("component_type", "");
    if (componentType.isEmpty()) {
      return; // Missing field is caught by LLMResponseParser
    }

    if (catalog.isEmpty()) {
      return; // No catalog available; skip validation
    }

    ComponentInfo info = catalog.get(componentType);
    if (info == null) {
      errors.add("Unknown component type: " + componentType);
      return;
    }

    JSONObject props = payload.optJSONObject("properties");
    if (props != null) {
      Iterator<String> keys = props.keys();
      while (keys.hasNext()) {
        String propName = keys.next();
        if (propName.startsWith("$")) {
          continue; // Skip internal metadata ($Type, $Version, etc.)
        }
        if (!info.properties.contains(propName)) {
          errors.add("Unknown property '" + propName
              + "' for component type " + componentType);
        }
      }
    }
  }

  // -------------------------------------------------------------------------
  // Component catalog parsing
  // -------------------------------------------------------------------------

  /**
   * Parsed component type information from simple_components.json.
   */
  private static class ComponentInfo {
    final Set<String> properties;
    final Set<String> events;
    final Set<String> methods;

    ComponentInfo(Set<String> properties, Set<String> events, Set<String> methods) {
      this.properties = properties;
      this.events = events;
      this.methods = methods;
    }
  }

  /**
   * Parse the simple_components.json string into a lookup map.
   */
  private Map<String, ComponentInfo> parseComponentDb(String componentDb) {
    Map<String, ComponentInfo> catalog = new HashMap<>();
    if (componentDb == null || componentDb.isEmpty()) {
      return catalog;
    }
    try {
      JSONArray components = new JSONArray(componentDb);
      for (int i = 0; i < components.length(); i++) {
        JSONObject comp = components.getJSONObject(i);
        String name = comp.optString("name", "");
        if (name.isEmpty()) {
          continue;
        }

        Set<String> properties = new HashSet<>();
        JSONArray blockProps = comp.optJSONArray("blockProperties");
        if (blockProps != null) {
          for (int j = 0; j < blockProps.length(); j++) {
            String pName = blockProps.getJSONObject(j).optString("name", "");
            if (!pName.isEmpty()) {
              properties.add(pName);
            }
          }
        }

        Set<String> events = new HashSet<>();
        JSONArray evts = comp.optJSONArray("events");
        if (evts != null) {
          for (int j = 0; j < evts.length(); j++) {
            String eName = evts.getJSONObject(j).optString("name", "");
            if (!eName.isEmpty()) {
              events.add(eName);
            }
          }
        }

        Set<String> methods = new HashSet<>();
        JSONArray meths = comp.optJSONArray("methods");
        if (meths != null) {
          for (int j = 0; j < meths.length(); j++) {
            String mName = meths.getJSONObject(j).optString("name", "");
            if (!mName.isEmpty()) {
              methods.add(mName);
            }
          }
        }

        catalog.put(name, new ComponentInfo(properties, events, methods));
      }
    } catch (JSONException e) {
      LOG.warning("Failed to parse component database: " + e.getMessage());
    }
    return catalog;
  }

  private void validateProtectedProperty(String propertyName, List<String> errors) {
    if (PROTECTED_PROPERTIES.contains(propertyName)) {
      errors.add("Cannot modify protected property: " + propertyName);
    }
    if (propertyName.startsWith("$")) {
      errors.add("Cannot modify internal property: " + propertyName);
    }
  }

  /**
   * Strip any surviving write operations from an Advisor-mode response.
   * Belt-and-suspenders check applied after all validation.
   *
   * @param operations the operations to filter
   * @param mode the current AI agent mode
   * @return filtered operations (all write ops removed if Advisor mode)
   */
  public List<AIOperation> stripWriteOpsIfAdvisor(List<AIOperation> operations, String mode) {
    if (!"Advisor".equals(mode)) {
      return operations;
    }
    List<AIOperation> filtered = new ArrayList<>();
    for (AIOperation op : operations) {
      if (!WRITE_OPS.contains(op.getType())) {
        filtered.add(op);
      }
    }
    return filtered;
  }
}
