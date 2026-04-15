// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.aiagent.llm;

/**
 * A single message in a conversation history.
 *
 * <p>Each message has a role (e.g. "user", "assistant", "system", or
 * "tool_result") and a text body. Optionally, a message may carry
 * structured content — a provider-agnostic JSON string describing tool
 * calls or tool results — which providers translate into their native
 * wire format when replaying conversation history.
 *
 * <p>For assistant messages with tool calls, the structured content is a
 * JSON array of content parts:
 * <pre>[
 *   {"type":"text","text":"I'll add the component..."},
 *   {"type":"tool_use","id":"tc_abc123","name":"add_component","input":{...}}
 * ]</pre>
 *
 * <p>For tool result messages (role="tool_result"), the structured content is:
 * <pre>[
 *   {"type":"tool_result","tool_use_id":"tc_abc123","tool_name":"add_component","content":"Done."}
 * ]</pre>
 */
public class ChatMessage {

  private final String role;
  private final String text;
  private final String structuredContent;
  private final boolean display;
  private final long timestamp;

  /**
   * Creates a new chat message with text only (no structured content).
   *
   * @param role the role of the message sender ("user", "assistant", or "system")
   * @param text the text content of the message
   */
  public ChatMessage(String role, String text) {
    this(role, text, null);
  }

  /**
   * Creates a new chat message with optional structured content.
   * Defaults to {@code display = true}.
   *
   * @param role              the role of the message sender
   * @param text              the text content (human-readable summary)
   * @param structuredContent provider-agnostic JSON array of content parts,
   *                          or null for plain-text-only messages
   */
  public ChatMessage(String role, String text, String structuredContent) {
    this(role, text, structuredContent, true);
  }

  /**
   * Creates a new chat message with optional structured content and display flag.
   *
   * @param role              the role of the message sender
   * @param text              the text content (human-readable summary)
   * @param structuredContent provider-agnostic JSON array of content parts,
   *                          or null for plain-text-only messages
   * @param display           true if this message should appear in the client chat UI
   */
  public ChatMessage(String role, String text, String structuredContent, boolean display) {
    this(role, text, structuredContent, display, 0L);
  }

  /**
   * Creates a new chat message with a server-side timestamp.
   *
   * @param role              the role of the message sender
   * @param text              the text content (human-readable summary)
   * @param structuredContent provider-agnostic JSON array of content parts,
   *                          or null for plain-text-only messages
   * @param display           true if this message should appear in the client chat UI
   * @param timestamp         server-side timestamp in millis (0 if unknown)
   */
  public ChatMessage(String role, String text, String structuredContent, boolean display,
      long timestamp) {
    this.role = role;
    this.text = text;
    this.structuredContent = structuredContent;
    this.display = display;
    this.timestamp = timestamp;
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

  /**
   * Returns the structured content parts as a JSON string, or null if this
   * message contains only plain text.
   *
   * @return the structured content JSON, or null
   */
  public String getStructuredContent() {
    return structuredContent;
  }

  /**
   * Returns true if this message carries structured content (tool calls or
   * tool results) in addition to its text summary.
   *
   * @return true if structured content is available
   */
  public boolean hasStructuredContent() {
    return structuredContent != null && !structuredContent.isEmpty();
  }

  /**
   * Returns true if this message should be shown in the client chat UI.
   *
   * @return true if displayable
   */
  public boolean isDisplay() {
    return display;
  }

  /**
   * Returns the server-side timestamp (millis) when the message was stored,
   * or 0 if unknown (e.g. messages built from live LLM responses before
   * persistence, or older stored rows).
   *
   * @return the timestamp in millis, or 0 if unknown
   */
  public long getTimestamp() {
    return timestamp;
  }

  @Override
  public String toString() {
    return "ChatMessage{role='" + role + "', text='" + text
        + "', hasStructured=" + hasStructuredContent()
        + ", display=" + display + "}";
  }
}
