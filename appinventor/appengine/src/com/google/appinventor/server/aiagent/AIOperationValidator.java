// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent;

import com.google.appinventor.shared.rpc.aiagent.AIOperation;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Stage 2 semantic validation and mode enforcement for AI-generated operations.
 * Validates operations against the component database and enforces the
 * AI agent mode restrictions (Advisor, ScreenEditor, ProjectEditor).
 */
public class AIOperationValidator {

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
          errors.add("Advisor mode does not allow write operations. Rejected: " + op.getType());
        }
      }
      if (!errors.isEmpty()) {
        return new ValidationResult(accepted, errors);
      }
    }

    if ("ScreenEditor".equals(mode)) {
      for (AIOperation op : operations) {
        if (PROJECT_LEVEL_OPS.contains(op.getType())) {
          errors.add("ScreenEditor mode does not allow project-level operations. "
              + "Rejected: " + op.getType());
        }
      }
      if (!errors.isEmpty()) {
        return new ValidationResult(accepted, errors);
      }
    }

    // ProjectEditor allows all
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

    for (AIOperation op : operations) {
      List<String> opErrors = validateSingleOperation(op);
      if (opErrors.isEmpty()) {
        accepted.add(op);
      } else {
        errors.addAll(opErrors);
      }
    }

    return new ValidationResult(accepted, errors);
  }

  private List<String> validateSingleOperation(AIOperation op) {
    List<String> errors = new ArrayList<>();
    JSONObject payload;

    try {
      payload = new JSONObject(op.getPayload());
    } catch (Exception e) {
      errors.add("Invalid payload JSON for " + op.getType() + ": " + e.getMessage());
      return errors;
    }

    switch (op.getType()) {
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
