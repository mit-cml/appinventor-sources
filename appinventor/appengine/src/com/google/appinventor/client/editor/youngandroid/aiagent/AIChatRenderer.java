// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.aiagent;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;

/**
 * Renders chat message bubbles and converts Markdown to safe HTML.
 *
 * <p>Manages the chat history panel and scroll behavior. User messages
 * are right-aligned with blue backgrounds; AI messages are left-aligned
 * with grey backgrounds and support Markdown rendering.</p>
 */
public class AIChatRenderer {

  private final FlowPanel chatHistory;
  private final ScrollPanel chatScrollPanel;

  private String streamingTextAccumulator = "";
  private FlowPanel streamingWrapper = null;
  private HTML streamingMessageHtml = null;

  /**
   * Constructs a renderer for the given chat history and scroll panels.
   *
   * @param chatHistory      the panel that holds message bubbles
   * @param chatScrollPanel  the scroll panel wrapping the chat history
   */
  public AIChatRenderer(FlowPanel chatHistory, ScrollPanel chatScrollPanel) {
    this.chatHistory = chatHistory;
    this.chatScrollPanel = chatScrollPanel;
  }

  /**
   * Configures the marked.js library for Markdown rendering.
   * Must be called once during initialization.
   */
  public void initialize() {
    configureMarked();
  }

  /**
   * Adds a right-aligned user message to the chat history.
   *
   * @param text the user's message text
   */
  public void addUserMessage(String text) {
    FlowPanel messageBubble = createMessageBubble(
        MESSAGES.aiChatUserLabel(), text, true);
    chatHistory.add(messageBubble);
    scrollToBottom();
  }

  /**
   * Adds a left-aligned AI message to the chat history.
   *
   * @param text the AI's message text
   */
  public void addAiMessage(String text) {
    FlowPanel messageBubble = createMessageBubble(
        MESSAGES.aiChatAiLabel(), text, false);
    chatHistory.add(messageBubble);
    scrollToBottom();
  }

  /**
   * Scrolls the chat panel to the bottom.
   */
  public void scrollToBottom() {
    chatScrollPanel.scrollToBottom();
  }

  /**
   * Clears all messages from the chat history.
   */
  public void clear() {
    chatHistory.clear();
  }

  /**
   * Starts a new streaming AI message bubble. The bubble is added to
   * the chat history immediately with empty content; subsequent calls
   * to {@link #appendStreamingText} fill it incrementally.
   */
  public void startStreamingBubble() {
    streamingTextAccumulator = "";
    streamingWrapper = createMessageBubble(
        MESSAGES.aiChatAiLabel(), "", false);
    FlowPanel bubble = (FlowPanel) streamingWrapper.getWidget(0);
    streamingMessageHtml = (HTML) bubble.getWidget(1);
    // Apply streaming visual indicator
    bubble.getElement().getStyle().setProperty("background", "#f0f0f0");
    bubble.getElement().getStyle().setProperty("borderLeft", "3px solid #90b4d6");
    chatHistory.add(streamingWrapper);
    scrollToBottom();
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
    streamingMessageHtml.setHTML(markdownToSafeHtml(textToRender));
    scrollToBottom();
  }

  /**
   * Finalizes the streaming bubble with the complete AI response text.
   * Resets all streaming state.
   *
   * @param finalText the complete AI response text
   */
  public void finalizeStreamingBubble(String finalText) {
    if (streamingMessageHtml != null) {
      // Revert to completed bubble style
      FlowPanel bubble = (FlowPanel) streamingWrapper.getWidget(0);
      bubble.getElement().getStyle().setProperty("background", "#e8e8e8");
      bubble.getElement().getStyle().clearProperty("borderLeft");
      streamingMessageHtml.setHTML(markdownToSafeHtml(finalText));
      scrollToBottom();
    }
    streamingWrapper = null;
    streamingMessageHtml = null;
    streamingTextAccumulator = "";
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

  /**
   * Creates a styled message bubble for the chat.
   *
   * @param sender the sender label (e.g. "You" or "AI")
   * @param text   the message text
   * @param isUser true for right-aligned user messages, false for left-aligned AI messages
   * @return the styled FlowPanel
   */
  private FlowPanel createMessageBubble(String sender, String text, boolean isUser) {
    FlowPanel wrapper = new FlowPanel();
    wrapper.getElement().getStyle().setProperty("textAlign", isUser ? "right" : "left");
    wrapper.getElement().getStyle().setMarginBottom(6, Unit.PX);
    wrapper.getElement().getStyle().setPadding(2, Unit.PX);

    FlowPanel bubble = new FlowPanel();
    bubble.getElement().getStyle().setProperty("display", "inline-block");
    bubble.getElement().getStyle().setProperty("maxWidth", "85%");
    bubble.getElement().getStyle().setProperty("textAlign", "left");
    bubble.getElement().getStyle().setPadding(8, Unit.PX);
    bubble.getElement().getStyle().setProperty("borderRadius", "8px");

    if (isUser) {
      bubble.getElement().getStyle().setProperty("background", "#d1e7ff");
      bubble.getElement().getStyle().setColor("#1a3a5c");
    } else {
      bubble.getElement().getStyle().setProperty("background", "#e8e8e8");
      bubble.getElement().getStyle().setColor("#333");
    }

    Label senderLabel = new Label(sender);
    senderLabel.getElement().getStyle().setProperty("fontWeight", "bold");
    senderLabel.getElement().getStyle().setFontSize(11, Unit.PX);
    senderLabel.getElement().getStyle().setMarginBottom(2, Unit.PX);
    bubble.add(senderLabel);

    // Use HTML to support line breaks / Markdown in messages
    HTML messageHtml;
    if (isUser) {
      messageHtml = new HTML(AIJsonUtils.escapeAndFormat(text));
    } else {
      messageHtml = new HTML(markdownToSafeHtml(text));
      messageHtml.addStyleName("ai-chat-markdown");
    }
    messageHtml.getElement().getStyle().setFontSize(13, Unit.PX);
    messageHtml.getElement().getStyle().setProperty("wordWrap", "break-word");
    bubble.add(messageHtml);

    wrapper.add(bubble);
    return wrapper;
  }

  /**
   * Converts a Markdown string to sanitized HTML using marked.js and DOMPurify.
   * Falls back to plain-text escaping if the libraries are not loaded.
   *
   * @param markdown the raw Markdown text
   * @return sanitized HTML string
   */
  private static native String markdownToSafeHtml(String markdown) /*-{
    if (!$wnd.marked || !$wnd.DOMPurify) {
      // Fallback to plain-text escaping
      return markdown.replace(/&/g,'&amp;').replace(/</g,'&lt;')
        .replace(/>/g,'&gt;').replace(/"/g,'&quot;').replace(/\n/g,'<br>');
    }
    var rawHtml = $wnd.marked.parse(markdown);
    return $wnd.DOMPurify.sanitize(rawHtml, {
      ALLOWED_TAGS: ['h1','h2','h3','h4','h5','h6','p','br','hr',
        'b','i','em','strong','del','s','ul','ol','li','a',
        'code','pre','blockquote','table','thead','tbody','tr','th','td',
        'span','div','sup','sub'],
      ALLOWED_ATTR: ['href','target','rel','class'],
      ALLOW_DATA_ATTR: false
    });
  }-*/;

  /**
   * Configures marked.js options: enables GFM and line breaks,
   * and makes links open in a new tab.
   */
  private static native void configureMarked() /*-{
    if ($wnd.marked) {
      var renderer = new $wnd.marked.Renderer();
      var origLink = renderer.link;
      renderer.link = function(token) {
        var html = origLink.call(this, token);
        return html.replace('<a ', '<a target="_blank" rel="noopener noreferrer" ');
      };
      $wnd.marked.setOptions({
        breaks: true, gfm: true, renderer: renderer
      });
    }
  }-*/;
}
