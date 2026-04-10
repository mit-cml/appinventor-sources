// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.aiagent.chat;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Contract for {@link ChatStreamingHandler} to call back into the
 * parent renderer without a circular package dependency.
 */
public interface ChatRendererHost {

  /**
   * Creates a styled message bubble.
   *
   * @param sender the sender label (e.g. "You" or "AI")
   * @param text   the message text
   * @param isUser true for user messages, false for AI messages
   * @return the styled wrapper FlowPanel
   */
  FlowPanel createMessageBubble(String sender, String text, boolean isUser);

  /**
   * Adds a widget to the chat history panel.
   *
   * @param widget the widget to add
   */
  void addToHistory(Widget widget);

  /**
   * Converts Markdown to sanitized HTML using marked.js and DOMPurify.
   *
   * @param markdown the raw Markdown text
   * @return sanitized HTML string
   */
  String markdownToSafeHtml(String markdown);

  /**
   * Scrolls the chat panel to the bottom.
   */
  void scrollToBottom();

  /**
   * Appends a feedback link to an AI message bubble wrapper.
   *
   * @param wrapper the message wrapper FlowPanel
   */
  void appendFeedbackLink(FlowPanel wrapper);

  /**
   * Returns the localized label for AI messages (e.g. "AI").
   *
   * @return the AI sender label
   */
  String getAiLabel();
}
