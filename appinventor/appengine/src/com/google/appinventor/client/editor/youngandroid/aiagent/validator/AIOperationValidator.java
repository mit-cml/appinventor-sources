// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.aiagent.validator;

import com.google.appinventor.shared.rpc.aiagent.AIOperation;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;

/**
 * Validates AI operations immediately before execution.
 *
 * <p>Performs common checks (null type, empty payload, JSON parsing) then
 * delegates to the appropriate domain-specific validator.</p>
 */
public final class AIOperationValidator {

  private AIOperationValidator() {}

  /**
   * Validate a single operation.
   *
   * @return null if valid, or an error message string.
   */
  public static String validate(AIOperation op) {
    if (op.getType() == null) {
      return "Operation has null type";
    }
    if (op.getPayload() == null || op.getPayload().isEmpty()) {
      return "Operation " + op.getType() + " has empty payload";
    }

    JSONObject json;
    try {
      json = JSONParser.parseStrict(op.getPayload()).isObject();
    } catch (Exception e) {
      return "Invalid JSON payload for " + op.getType() + ": " + e.getMessage();
    }
    if (json == null) {
      return "Payload for " + op.getType() + " is not a JSON object";
    }

    switch (op.getType()) {
      case SWITCH_SCREEN:    return ProjectOperationValidator.validateSwitchScreen(json);
      case CREATE_SCREEN:    return ProjectOperationValidator.validateCreateScreen(json);
      case DELETE_SCREEN:    return ProjectOperationValidator.validateDeleteScreen(json);
      case SET_PROJECT_PROP: return ProjectOperationValidator.validateSetProjectProp(json);
      case TOGGLE_EDITOR:    return ProjectOperationValidator.validateToggleEditor(json);
      case ADD_COMPONENT:    return DesignerOperationValidator.validateAddComponent(json);
      case DELETE_COMPONENT: return DesignerOperationValidator.validateDeleteComponent(json);
      case SET_PROPERTY:     return DesignerOperationValidator.validateSetProperty(json);
      case RENAME_COMPONENT: return DesignerOperationValidator.validateRenameComponent(json);
      case WRITE_BLOCK:      return BlockOperationValidator.validateWriteBlock(json);
      case DELETE_BLOCK:     return BlockOperationValidator.validateDeleteBlock(json);
      default:               return "Unknown operation type: " + op.getType();
    }
  }
}
