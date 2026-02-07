// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.aiagent;

import com.google.gwt.user.client.rpc.IsSerializable;
import java.io.Serializable;

/**
 * Lightweight DTO for loading conversation history into the client.
 * Contains text only (no operations). Used to restore the chat UI
 * after page reload.
 */
public class AIConversationMessage implements IsSerializable, Serializable {

  private String role;
  private String text;

  /**
   * No-arg constructor required for GWT serialization.
   */
  public AIConversationMessage() {
  }

  /**
   * Creates a new conversation message.
   *
   * @param role "user" or "assistant"
   * @param text the message text
   */
  public AIConversationMessage(String role, String text) {
    this.role = role;
    this.text = text;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }
}
