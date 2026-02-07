// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent.llm;

/**
 * A single message in a conversation history.
 *
 * <p>Each message has a role (e.g. "user", "assistant", or "system") and a
 * text body. This is a simple value object used to pass conversation context
 * to LLM providers.
 */
public class ChatMessage {

  private final String role;
  private final String text;

  /**
   * Creates a new chat message.
   *
   * @param role the role of the message sender ("user", "assistant", or "system")
   * @param text the text content of the message
   */
  public ChatMessage(String role, String text) {
    this.role = role;
    this.text = text;
  }

  /**
   * Returns the role of the message sender.
   *
   * @return the role string
   */
  public String getRole() {
    return role;
  }

  /**
   * Returns the text content of the message.
   *
   * @return the message text
   */
  public String getText() {
    return text;
  }

  @Override
  public String toString() {
    return "ChatMessage{role='" + role + "', text='" + text + "'}";
  }
}
