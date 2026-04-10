// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.aiagent;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.youngandroid.aiagent.chat.ChatPlanCardRenderer;
import com.google.appinventor.client.editor.youngandroid.aiagent.chat.ChatRendererHost;
import com.google.appinventor.client.editor.youngandroid.aiagent.chat.ChatStreamingHandler;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Facade for chat rendering in the AI agent dialog.
 *
 * <p>Manages message bubbles, feedback links, and Markdown conversion
 * directly. Delegates streaming to {@link ChatStreamingHandler} and
 * plan card rendering to {@link ChatPlanCardRenderer}.</p>
 */
public class AIChatRenderer implements ChatRendererHost {

  private static final String FEEDBACK_LINK_CLASS = "ai-feedback-link";

  private final FlowPanel chatHistory;
  private final ScrollPanel chatScrollPanel;
  private final ChatStreamingHandler streamingHandler;
  private final ChatPlanCardRenderer planCardRenderer;

  // Feedback link context (set via setFeedbackContext; links only shown in debug mode)
  private boolean debugEnabled;
  private String conversationId;

  /**
   * Constructs a renderer for the given chat history and scroll panels.
   *
   * @param chatHistory      the panel that holds message bubbles
   * @param chatScrollPanel  the scroll panel wrapping the chat history
   */
  public AIChatRenderer(FlowPanel chatHistory, ScrollPanel chatScrollPanel) {
    this.chatHistory = chatHistory;
    this.chatScrollPanel = chatScrollPanel;
    this.streamingHandler = new ChatStreamingHandler(this);
    this.planCardRenderer = new ChatPlanCardRenderer(chatHistory, new Runnable() {
      @Override
      public void run() {
        scrollToBottom();
      }
    });
  }

  /**
   * Configures the marked.js library for Markdown rendering.
   * Must be called once during initialization.
   */
  public void initialize() {
    configureMarked();
  }

  // ---- Message bubbles ----

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
   * Adds a left-aligned AI message to the chat history with a
   * "Share Feedback" link (when feedback context is set).
   *
   * @param text the AI's message text
   */
  public void addAiMessage(String text) {
    FlowPanel messageBubble = createMessageBubble(
        MESSAGES.aiChatAiLabel(), text, false);
    appendFeedbackLink(messageBubble);
    chatHistory.add(messageBubble);
    scrollToBottom();
  }

  // ---- Streaming (delegated) ----

  /**
   * Starts a new streaming AI message bubble.
   */
  public void startStreamingBubble() {
    streamingHandler.startStreamingBubble();
  }

  /**
   * Appends a text delta to the in-progress streaming bubble.
   *
   * @param delta the new text chunk to append
   */
  public void appendStreamingText(String delta) {
    streamingHandler.appendStreamingText(delta);
  }

  /**
   * Appends a thinking/reasoning delta to the in-progress streaming bubble.
   *
   * @param delta the new thinking text chunk to append
   */
  public void appendStreamingThinking(String delta) {
    streamingHandler.appendStreamingThinking(delta);
  }

  /**
   * Finalizes the streaming bubble with the complete AI response text.
   *
   * @param finalText the complete AI response text
   */
  public void finalizeStreamingBubble(String finalText) {
    streamingHandler.finalizeStreamingBubble(finalText);
  }

  // ---- Plan cards (delegated) ----

  /**
   * Renders a plan card in the chat with summary, steps, and
   * approve/edit/reject buttons.
   *
   * @param planJson         the raw plan JSON from the PROPOSE_PLAN operation
   * @param approvalCallback receives the user's approve/reject decision
   */
  public void renderPlanCard(String planJson,
      AIResponseOrchestrator.PlanApprovalCallback approvalCallback) {
    planCardRenderer.renderPlanCard(planJson, approvalCallback);
  }

  /**
   * Disables the active plan card buttons.
   */
  public void dismissActivePlanCard() {
    planCardRenderer.dismissActivePlanCard();
  }

  // ---- Feedback links ----

  /**
   * Sets the conversation ID used for "Share Feedback" links on AI messages.
   *
   * @param debugEnabled   whether debug mode is enabled
   * @param conversationId the conversation UUID
   */
  public void setFeedbackContext(boolean debugEnabled, String conversationId) {
    boolean wasMissing = !this.debugEnabled
        || this.conversationId == null || this.conversationId.isEmpty();
    this.debugEnabled = debugEnabled;
    this.conversationId = conversationId;

    // Retroactively add feedback links to AI messages that were rendered
    // before the context was available (e.g. first messages in a new
    // conversation, before the server assigns a conversation ID).
    if (wasMissing && debugEnabled
        && conversationId != null && !conversationId.isEmpty()) {
      for (int i = 0; i < chatHistory.getWidgetCount(); i++) {
        if (chatHistory.getWidget(i) instanceof FlowPanel) {
          FlowPanel wrapper = (FlowPanel) chatHistory.getWidget(i);
          if ("left".equals(wrapper.getElement().getStyle().getTextAlign())) {
            appendFeedbackLink(wrapper);
          }
        }
      }
    }
  }

  /**
   * Appends a small "Share Feedback" link at the bottom of an AI message bubble.
   */
  @Override
  public void appendFeedbackLink(FlowPanel wrapper) {
    if (!debugEnabled || conversationId == null || conversationId.isEmpty()) {
      return;
    }
    FlowPanel bubble = (FlowPanel) wrapper.getWidget(0);
    // Avoid adding duplicate links (retroactive pass may re-visit bubbles).
    for (int i = 0; i < bubble.getWidgetCount(); i++) {
      if (bubble.getWidget(i).getStyleName().contains(FEEDBACK_LINK_CLASS)) {
        return;
      }
    }
    String url = buildFeedbackUrl(conversationId);
    HTML link = new HTML("<a href='" + url + "' target='_blank' "
        + "rel='noopener noreferrer' "
        + "style='font-size:10px; color:#888; text-decoration:none; "
        + "display:block; margin-top:4px; text-align:right;'>"
        + MESSAGES.aiChatShareFeedback() + "</a>");
    link.addStyleName(FEEDBACK_LINK_CLASS);
    bubble.add(link);
  }

  /**
   * Builds the feedback form URL for the given conversation.
   *
   * @param conversationId the conversation UUID
   * @return the fully-encoded feedback URL
   */
  public static String buildFeedbackUrl(String conversationId) {
    long messageTs = System.currentTimeMillis() / 1000;
    long projectId = 0;
    try {
      projectId = Ode.getInstance().getCurrentYoungAndroidProjectId();
    } catch (Exception e) {
      // best-effort
    }
    return "/ode/feedback"
        + "?notes=" + URL.encodeQueryString("AI Agent Response Feedback")
        + "&foundIn=" + URL.encodeQueryString(conversationId)
        + "&faultData=" + URL.encodeQueryString("message=" + messageTs)
        + "&projectId=" + projectId;
  }

  // ---- ChatRendererHost implementation ----

  @Override
  public FlowPanel createMessageBubble(String sender, String text, boolean isUser) {
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

  @Override
  public void addToHistory(Widget widget) {
    chatHistory.add(widget);
  }

  @Override
  public String markdownToSafeHtml(String markdown) {
    return markdownToSafeHtmlNative(markdown);
  }

  @Override
  public void scrollToBottom() {
    chatScrollPanel.scrollToBottom();
  }

  @Override
  public String getAiLabel() {
    return MESSAGES.aiChatAiLabel();
  }

  /**
   * Clears all messages from the chat history.
   */
  public void clear() {
    chatHistory.clear();
  }

  // ---- JSNI Markdown ----

  /**
   * Converts a Markdown string to sanitized HTML using marked.js and DOMPurify.
   * Falls back to plain-text escaping if the libraries are not loaded.
   *
   * @param markdown the raw Markdown text
   * @return sanitized HTML string
   */
  private static native String markdownToSafeHtmlNative(String markdown) /*-{
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
