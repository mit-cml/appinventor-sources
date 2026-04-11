// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.aiagent;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.shared.rpc.aiagent.AIOperation;

import java.util.List;

/**
 * Formats AI operations into human-readable text for the chat UI.
 *
 * <p>All methods are static. Uses {@link AIJsonUtils#extractField} for
 * payload parsing.</p>
 */
public final class AIOperationFormatter {

  private AIOperationFormatter() {}

  /**
   * Formats a single AI operation into a human-readable string.
   *
   * @param op the operation to format
   * @return a human-readable description
   */
  public static String formatOperation(AIOperation op) {
    AIOperation.Type type = op.getType();
    String payload = op.getPayload();
    // Provide a readable summary based on operation type.
    // Field names must match the JSON payload keys used by AIOperationExecutor.
    switch (type) {
      case ADD_COMPONENT:
        return "+ Add component: " + AIJsonUtils.extractField(payload, "component_type")
            + " (" + AIJsonUtils.extractField(payload, "name") + ")";
      case DELETE_COMPONENT:
        return "- Delete component: " + AIJsonUtils.extractField(payload, "name");
      case SET_PROPERTY:
        return "~ Set " + AIJsonUtils.extractField(payload, "component_name")
            + "." + AIJsonUtils.extractField(payload, "property_name")
            + " to " + AIJsonUtils.extractField(payload, "value");
      case RENAME_COMPONENT:
        return "~ Rename: " + AIJsonUtils.extractField(payload, "old_name")
            + " -> " + AIJsonUtils.extractField(payload, "new_name");
      case SWITCH_SCREEN:
        return "~ Switch to screen: " + AIJsonUtils.extractField(payload, "screen_name");
      case CREATE_SCREEN:
        return "+ Create screen: " + AIJsonUtils.extractField(payload, "screen_name");
      case DELETE_SCREEN:
        return "- Delete screen: " + AIJsonUtils.extractField(payload, "screen_name");
      case SET_PROJECT_PROP:
        return "~ Set project property: " + AIJsonUtils.extractField(payload, "property")
            + " to " + AIJsonUtils.extractField(payload, "value");
      case WRITE_BLOCK:
        return "+ Write block: " + summarizeYail(AIJsonUtils.extractField(payload, "yail"));
      case DELETE_BLOCK:
        return "- Delete block: " + AIJsonUtils.extractField(payload, "block");
      case TOGGLE_EDITOR:
        return "~ Switch to " + AIJsonUtils.extractField(payload, "view") + " view";
      case PROPOSE_PLAN:
        return "Proposed execution plan";
      default:
        return type.name() + ": " + payload;
    }
  }

  /**
   * Builds a human-readable summary of successfully applied operations.
   */
  public static String buildAppliedSummary(List<AIOperation> operations) {
    StringBuilder sb = new StringBuilder(MESSAGES.aiChatOperationsApplied());
    for (AIOperation op : operations) {
      sb.append('\n').append(formatOperation(op));
    }
    return sb.toString();
  }

  /**
   * Extracts a human-readable summary from a YAIL block definition.
   * Recognizes event handlers, global variables, and procedures.
   *
   * @param yail the raw YAIL string
   * @return a short description such as "Screen1.Initialize (event)"
   */
  private static String summarizeYail(String yail) {
    if (yail == null || yail.isEmpty()) {
      return "block";
    }
    yail = yail.trim();
    if (yail.startsWith("(define-event ")) {
      // Pattern: (define-event ComponentName EventName (...) ...)
      String rest = yail.substring("(define-event ".length()).trim();
      int space1 = rest.indexOf(' ');
      if (space1 > 0) {
        String component = rest.substring(0, space1);
        String afterComponent = rest.substring(space1 + 1).trim();
        int end = endOfToken(afterComponent);
        String event = end > 0 ? afterComponent.substring(0, end) : afterComponent;
        return component + "." + event + " (event)";
      }
    }
    if (yail.startsWith("(define-generic-event ")) {
      // Pattern: (define-generic-event ComponentType EventName (...) ...)
      String rest = yail.substring("(define-generic-event ".length()).trim();
      int space1 = rest.indexOf(' ');
      if (space1 > 0) {
        String componentType = rest.substring(0, space1);
        String afterType = rest.substring(space1 + 1).trim();
        int end = endOfToken(afterType);
        String event = end > 0 ? afterType.substring(0, end) : afterType;
        return "any " + componentType + "." + event + " (generic event)";
      }
    }
    if (yail.startsWith("(def g$")) {
      // Pattern: (def g$varName ...)
      String rest = yail.substring("(def g$".length());
      int end = endOfToken(rest);
      if (end > 0) {
        return rest.substring(0, end) + " (variable)";
      }
    }
    if (yail.startsWith("(def (p$")) {
      // Pattern: (def (p$procedureName ...) ...)
      String rest = yail.substring("(def (p$".length());
      int end = endOfToken(rest);
      if (end > 0) {
        return rest.substring(0, end) + " (procedure)";
      }
    }
    if (yail.startsWith("(def-return (p$")) {
      // Pattern: (def-return (p$procedureName ...) ...)
      String rest = yail.substring("(def-return (p$".length());
      int end = endOfToken(rest);
      if (end > 0) {
        return rest.substring(0, end) + " (procedure)";
      }
    }
    return "block";
  }

  /**
   * Returns the index of the first whitespace or delimiter character in {@code s},
   * or -1 if the string contains only token characters.
   */
  private static int endOfToken(String s) {
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == ' ' || c == ')' || c == '(' || c == '\n' || c == '\r' || c == '\t') {
        return i;
      }
    }
    return -1;
  }
}
