// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.aiagent;

import com.google.gwt.user.client.rpc.IsSerializable;
import java.io.Serializable;

/**
 * Represents a single AI-generated operation to be applied to the project.
 * Each operation has a type (from {@link Type}) and a JSON payload string
 * containing the operation-specific parameters.
 */
public class AIOperation implements IsSerializable, Serializable {

  /**
   * Enumeration of all supported AI operation types.
   */
  public enum Type implements IsSerializable {
    // Component operations (ScreenEditor + ProjectEditor modes)
    ADD_COMPONENT,
    DELETE_COMPONENT,
    SET_PROPERTY,
    RENAME_COMPONENT,

    // YAIL-based block operations (ScreenEditor + ProjectEditor modes)
    WRITE_BLOCK,
    DELETE_BLOCK,

    // Project-level operations (ProjectEditor mode only)
    SWITCH_SCREEN,
    CREATE_SCREEN,
    DELETE_SCREEN,
    SET_PROJECT_PROP,

    // Navigation operations (ScreenEditor + ProjectEditor modes)
    TOGGLE_EDITOR,

    // Orchestration
    PROPOSE_PLAN,

    // Runtime reads (client-resolved, never reach server executor)
    READ_RUNTIME
  }

  private Type type;
  private String payload;

  /**
   * No-arg constructor required for GWT serialization.
   */
  public AIOperation() {
  }

  /**
   * Creates a new AIOperation with the given type and JSON payload.
   *
   * @param type the operation type
   * @param payload JSON string containing operation parameters
   */
  public AIOperation(Type type, String payload) {
    this.type = type;
    this.payload = payload;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public String getPayload() {
    return payload;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }
}
