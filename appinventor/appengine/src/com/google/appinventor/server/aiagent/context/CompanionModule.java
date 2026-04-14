// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent.context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.logging.Logger;

/**
 * Renders Companion runtime state into the LLM context when the client has
 * attached a {@code companionSnapshot} to the request.
 *
 * <p>The snapshot is a JSON object with the following optional fields:
 * <pre>
 * {
 *   "connectionKind": "webrtc" | "http",
 *   "activeScreen": "Screen1",
 *   "logs":   [{"level": "info",  "text": "...", "timestamp": 123}],
 *   "errors": [{"message": "...", "blockId": "...",
 *               "componentName": "Button1", "timestamp": 456}]
 * }
 * </pre>
 *
 * <p>Logs are capped at 10 entries; errors at 3.
 */
public class CompanionModule extends ContextModule {

  private static final Logger LOG = Logger.getLogger(CompanionModule.class.getName());

  private static final int MAX_LOGS = 10;
  private static final int MAX_ERRORS = 3;

  /**
   * Cached pedagogical instructions loaded from {@code companion_instructions.md}
   * on first use. Mirrors {@link TutorialModule}'s caching pattern.
   */
  private static volatile String cachedInstructions;

  @Override
  public String build(ContextParams params) {
    String snapshotJson = params.getCompanionSnapshot();
    if (snapshotJson == null || snapshotJson.isEmpty()) {
      return null;
    }

    JSONObject snapshot;
    try {
      snapshot = new JSONObject(snapshotJson);
    } catch (Exception e) {
      LOG.warning("Failed to parse companionSnapshot: " + e.getMessage());
      return null;
    }

    if (cachedInstructions == null) {
      cachedInstructions = ContextUtils.loadResource("companion_instructions.md");
    }

    StringBuilder sb = new StringBuilder();
    sb.append("[Companion Runtime State]\n\n");
    if (cachedInstructions != null && !cachedInstructions.isEmpty()) {
      sb.append(cachedInstructions).append("\n\n");
    }
    sb.append("## Companion runtime state\n\n");

    // Connection / active screen line
    String connectionKind = snapshot.optString("connectionKind", "");
    String activeScreen = snapshot.optString("activeScreen", "");
    StringBuilder connLine = new StringBuilder("**Connection:**");
    if (!connectionKind.isEmpty()) {
      connLine.append(" ").append(connectionKind);
    }
    if (!activeScreen.isEmpty()) {
      connLine.append(" \u2014 active screen: **").append(activeScreen).append("**");
    }
    sb.append(connLine).append("\n");

    // Errors section
    JSONArray errors = snapshot.optJSONArray("errors");
    if (errors != null && errors.length() > 0) {
      sb.append("\n### Recent errors\n");
      int limit = Math.min(errors.length(), MAX_ERRORS);
      for (int i = 0; i < limit; i++) {
        JSONObject err = errors.getJSONObject(i);
        String componentName = err.optString("componentName", "");
        String message = err.optString("message", "");
        if (!componentName.isEmpty()) {
          sb.append("- [").append(componentName).append("] ").append(message).append("\n");
        } else {
          sb.append("- ").append(message).append("\n");
        }
      }
    }

    // Logs section
    JSONArray logs = snapshot.optJSONArray("logs");
    if (logs != null && logs.length() > 0) {
      sb.append("\n### Recent logs\n");
      int limit = Math.min(logs.length(), MAX_LOGS);
      for (int i = 0; i < limit; i++) {
        JSONObject logEntry = logs.getJSONObject(i);
        String level = logEntry.optString("level", "");
        String text = logEntry.optString("text", "");
        sb.append("- [").append(level).append("] ").append(text).append("\n");
      }
    }

    sb.append("\n*Use `read_component_property`, `read_variable`, or `read_recent_logs`"
        + " to query live values.*");

    return sb.toString();
  }
}
