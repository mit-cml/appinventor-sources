// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.aiagent.chat;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;

/**
 * Manages the streaming bubble state machine for incremental AI responses.
 *
 * <p>Handles the lifecycle of a single streaming message: creation,
 * incremental text/thinking appends, and finalization. All rendering
 * callbacks go through the {@link ChatRendererHost} interface.</p>
 */
public class ChatStreamingHandler {

  private final ChatRendererHost host;

  private String streamingTextAccumulator = "";
  private String streamingThinkingAccumulator = "";
  private FlowPanel streamingWrapper = null;
  private HTML streamingMessageHtml = null;
  private HTML streamingThinkingHtml = null;
  private FlowPanel streamingThinkingPanel = null;
  private HTML typingIndicator = null;

  /**
   * Constructs a streaming handler that delegates rendering to the given host.
   *
   * @param host the renderer host for creating bubbles and converting Markdown
   */
  public ChatStreamingHandler(ChatRendererHost host) {
    this.host = host;
  }

  /**
   * Starts a new streaming AI message bubble. The bubble is added to
   * the chat history immediately with empty content; subsequent calls
   * to {@link #appendStreamingText} fill it incrementally.
   */
  public void startStreamingBubble() {
    streamingTextAccumulator = "";
    streamingThinkingAccumulator = "";
    streamingWrapper = host.createMessageBubble(
        host.getAiLabel(), "", false);
    FlowPanel bubble = (FlowPanel) streamingWrapper.getWidget(0);
    streamingMessageHtml = (HTML) bubble.getWidget(1);
    // Add typing indicator (three bouncing dots) below the message text
    typingIndicator = new HTML(
        "<div class='ai-typing-indicator'><span></span><span></span><span></span></div>");
    bubble.add(typingIndicator);
    host.addToHistory(streamingWrapper);
    host.scrollToBottom();
  }

  /**
   * Appends a text delta to the in-progress streaming bubble.
   * Handles incomplete Markdown code fences by temporarily closing them
   * so the rendered HTML stays valid.
   *
   * @param delta the new text chunk to append
   */
  public void appendStreamingText(String delta) {
    if (streamingMessageHtml == null) return;
    streamingTextAccumulator += delta;
    String textToRender = streamingTextAccumulator;
    int fenceCount = countOccurrences(textToRender, "```");
    if (fenceCount % 2 != 0) {
      textToRender += "\n```";
    }
    streamingMessageHtml.setHTML(host.markdownToSafeHtml(textToRender));
    host.scrollToBottom();
  }

  /**
   * Appends a thinking/reasoning delta to the in-progress streaming bubble.
   * Thinking content is displayed in a collapsible details panel above the
   * main response text, allowing users to inspect the model's reasoning.
   *
   * @param delta the new thinking text chunk to append
   */
  public void appendStreamingThinking(String delta) {
    if (streamingWrapper == null) return;
    FlowPanel bubble = (FlowPanel) streamingWrapper.getWidget(0);

    // Create the thinking panel on first thinking delta
    if (streamingThinkingPanel == null) {
      streamingThinkingPanel = new FlowPanel();
      streamingThinkingPanel.addStyleName("ai-thinking-panel");
      streamingThinkingHtml = new HTML();
      streamingThinkingHtml.addStyleName("ai-thinking-content");
      streamingThinkingPanel.add(streamingThinkingHtml);
      // Insert before the message text (index 1 is the message HTML,
      // index 0 is the role label)
      bubble.insert(streamingThinkingPanel, 1);
    }

    streamingThinkingAccumulator += delta;
    streamingThinkingHtml.setHTML(
        "<details open class='ai-thinking-details'>"
        + "<summary>Thinking\u2026</summary>"
        + "<div class='ai-thinking-text'>"
        + host.markdownToSafeHtml(streamingThinkingAccumulator)
        + "</div></details>");
    host.scrollToBottom();
  }

  /**
   * Finalizes the streaming bubble with the complete AI response text.
   * Resets all streaming state.
   *
   * @param finalText the complete AI response text
   */
  public void finalizeStreamingBubble(String finalText) {
    if (streamingMessageHtml != null) {
      // Remove typing indicator
      if (typingIndicator != null) {
        typingIndicator.removeFromParent();
        typingIndicator = null;
      }
      if (finalText != null && !finalText.isEmpty()) {
        // Canonical text from the server — use it as the final content
        streamingMessageHtml.setHTML(host.markdownToSafeHtml(finalText));
      } else if (streamingTextAccumulator.isEmpty()) {
        // No text at all (streaming started but no deltas arrived) —
        // remove the empty bubble rather than leaving a blank message
        if (streamingWrapper != null) {
          streamingWrapper.removeFromParent();
        }
      }
      // When finalText is empty but streamingTextAccumulator has content,
      // the text was already rendered by appendStreamingText(); just
      // remove the typing indicator (done above) and keep it as-is.

      // Add feedback link to the finalized streaming bubble
      if (streamingWrapper != null) {
        host.appendFeedbackLink(streamingWrapper);
      }

      // Collapse the thinking panel (switch from open to closed)
      if (streamingThinkingHtml != null && !streamingThinkingAccumulator.isEmpty()) {
        streamingThinkingHtml.setHTML(
            "<details class='ai-thinking-details'>"
            + "<summary>Thinking</summary>"
            + "<div class='ai-thinking-text'>"
            + host.markdownToSafeHtml(streamingThinkingAccumulator)
            + "</div></details>");
      } else if (streamingThinkingPanel != null
          && streamingThinkingAccumulator.isEmpty()) {
        streamingThinkingPanel.removeFromParent();
      }
      host.scrollToBottom();
    }
    streamingWrapper = null;
    streamingMessageHtml = null;
    streamingThinkingHtml = null;
    streamingThinkingPanel = null;
    streamingTextAccumulator = "";
    streamingThinkingAccumulator = "";
  }

  /**
   * Counts non-overlapping occurrences of a substring.
   */
  private static int countOccurrences(String text, String sub) {
    int count = 0;
    int idx = 0;
    while ((idx = text.indexOf(sub, idx)) != -1) {
      count++;
      idx += sub.length();
    }
    return count;
  }
}
