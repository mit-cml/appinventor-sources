// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.aiagent;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.Ode;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextArea;

/**
 * Renders chat message bubbles and converts Markdown to safe HTML.
 *
 * <p>Manages the chat history panel and scroll behavior. User messages
 * are right-aligned with blue backgrounds; AI messages are left-aligned
 * with grey backgrounds and support Markdown rendering.</p>
 */
public class AIChatRenderer {

  private static final String FEEDBACK_LINK_CLASS = "ai-feedback-link";

  private final FlowPanel chatHistory;
  private final ScrollPanel chatScrollPanel;

  private String streamingTextAccumulator = "";
  private String streamingThinkingAccumulator = "";
  private FlowPanel streamingWrapper = null;
  private HTML streamingMessageHtml = null;
  private HTML streamingThinkingHtml = null;
  private FlowPanel streamingThinkingPanel = null;
  private HTML typingIndicator = null;

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
   * Sets the conversation ID used for "Share Feedback" links on AI messages.
   *
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

  /**
   * Renders a plan card in the chat with summary, steps, and
   * approve/edit/reject buttons. The card is styled similarly to the
   * operation preview panel but appears inline in the chat history.
   *
   * @param planJson         the raw plan JSON from the PROPOSE_PLAN operation
   * @param approvalCallback receives the user's approve/reject decision
   */
  public void renderPlanCard(String planJson,
      final AIResponseOrchestrator.PlanApprovalCallback approvalCallback) {
    // Outer wrapper (left-aligned like AI messages)
    final FlowPanel wrapper = new FlowPanel();
    wrapper.getElement().getStyle().setProperty("textAlign", "left");
    wrapper.getElement().getStyle().setMarginBottom(6, Unit.PX);
    wrapper.getElement().getStyle().setPadding(2, Unit.PX);

    // Card panel
    final FlowPanel card = new FlowPanel();
    card.getElement().getStyle().setProperty("display", "inline-block");
    card.getElement().getStyle().setProperty("maxWidth", "85%");
    card.getElement().getStyle().setProperty("textAlign", "left");
    card.getElement().getStyle().setPadding(8, Unit.PX);
    card.getElement().getStyle().setProperty("borderRadius", "8px");
    card.getElement().getStyle().setProperty("border", "1px solid #4a90d9");
    card.getElement().getStyle().setProperty("background", "#eef4fb");

    // Header
    Label header = new Label(MESSAGES.aiChatPlanCardHeader());
    header.getElement().getStyle().setProperty("fontWeight", "bold");
    header.getElement().getStyle().setFontSize(13, Unit.PX);
    header.getElement().getStyle().setMarginBottom(6, Unit.PX);
    card.add(header);

    // Parse plan JSON and render summary + steps
    String summary = AIJsonUtils.extractField(planJson, "summary");
    if (summary != null && !summary.isEmpty() && !"summary".equals(summary)) {
      HTML summaryHtml = new HTML("<div style='margin-bottom:6px;'>"
          + AIJsonUtils.escapeAndFormat(summary) + "</div>");
      summaryHtml.getElement().getStyle().setFontSize(12, Unit.PX);
      card.add(summaryHtml);
    }

    // Extract steps from the JSON array
    String stepsHtml = buildStepsHtml(planJson);
    if (stepsHtml != null && !stepsHtml.isEmpty()) {
      HTML steps = new HTML(stepsHtml);
      steps.getElement().getStyle().setFontSize(12, Unit.PX);
      steps.getElement().getStyle().setMarginBottom(6, Unit.PX);
      card.add(steps);
    }

    // Button bar
    final HorizontalPanel buttonBar = new HorizontalPanel();
    buttonBar.setSpacing(4);

    final String finalPlanJson = planJson;

    Button approveBtn = new Button(MESSAGES.aiChatPlanApproveButton());
    approveBtn.getElement().getStyle().setProperty("background", "#4CAF50");
    approveBtn.getElement().getStyle().setColor("white");
    approveBtn.getElement().getStyle().setProperty("borderRadius", "3px");
    approveBtn.getElement().getStyle().setProperty("cursor", "pointer");
    approveBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        disablePlanButtons(buttonBar);
        approvalCallback.onApprove(finalPlanJson);
      }
    });
    buttonBar.add(approveBtn);

    Button editBtn = new Button(MESSAGES.aiChatPlanEditApproveButton());
    editBtn.getElement().getStyle().setProperty("background", "#FF9800");
    editBtn.getElement().getStyle().setColor("white");
    editBtn.getElement().getStyle().setProperty("borderRadius", "3px");
    editBtn.getElement().getStyle().setProperty("cursor", "pointer");
    editBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        showPlanEditor(card, buttonBar, finalPlanJson, approvalCallback);
      }
    });
    buttonBar.add(editBtn);

    Button rejectBtn = new Button(MESSAGES.aiChatPlanRejectButton());
    rejectBtn.getElement().getStyle().setProperty("background", "#f44336");
    rejectBtn.getElement().getStyle().setColor("white");
    rejectBtn.getElement().getStyle().setProperty("borderRadius", "3px");
    rejectBtn.getElement().getStyle().setProperty("cursor", "pointer");
    rejectBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        disablePlanButtons(buttonBar);
        approvalCallback.onReject();
      }
    });
    buttonBar.add(rejectBtn);

    card.add(buttonBar);
    wrapper.add(card);
    chatHistory.add(wrapper);
    scrollToBottom();
  }

  /**
   * Replaces the button bar with a textarea for editing the plan JSON,
   * plus Save and Cancel buttons.
   */
  private void showPlanEditor(final FlowPanel card, final HorizontalPanel originalButtonBar,
      final String planJson, final AIResponseOrchestrator.PlanApprovalCallback approvalCallback) {
    originalButtonBar.setVisible(false);

    final FlowPanel editorPanel = new FlowPanel();
    editorPanel.getElement().getStyle().setMarginTop(4, Unit.PX);

    final TextArea editArea = new TextArea();
    editArea.setText(planJson);
    editArea.setVisibleLines(8);
    editArea.setWidth("100%");
    editArea.getElement().getStyle().setProperty("boxSizing", "border-box");
    editArea.getElement().getStyle().setProperty("fontFamily",
        "'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace");
    editArea.getElement().getStyle().setFontSize(11, Unit.PX);
    editArea.getElement().getStyle().setProperty("borderRadius", "3px");
    editArea.getElement().getStyle().setProperty("border", "1px solid #ccc");
    editArea.getElement().getStyle().setPadding(4, Unit.PX);
    editorPanel.add(editArea);

    HorizontalPanel editButtons = new HorizontalPanel();
    editButtons.setSpacing(4);
    editButtons.getElement().getStyle().setMarginTop(4, Unit.PX);

    Button saveBtn = new Button(MESSAGES.aiChatPlanSaveButton());
    saveBtn.getElement().getStyle().setProperty("background", "#4CAF50");
    saveBtn.getElement().getStyle().setColor("white");
    saveBtn.getElement().getStyle().setProperty("borderRadius", "3px");
    saveBtn.getElement().getStyle().setProperty("cursor", "pointer");
    saveBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        String edited = editArea.getText().trim();
        editorPanel.removeFromParent();
        disablePlanButtons(originalButtonBar);
        approvalCallback.onApprove(edited);
      }
    });
    editButtons.add(saveBtn);

    Button cancelBtn = new Button(MESSAGES.aiChatPlanCancelButton());
    cancelBtn.getElement().getStyle().setProperty("borderRadius", "3px");
    cancelBtn.getElement().getStyle().setProperty("cursor", "pointer");
    cancelBtn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        editorPanel.removeFromParent();
        originalButtonBar.setVisible(true);
      }
    });
    editButtons.add(cancelBtn);

    editorPanel.add(editButtons);
    card.add(editorPanel);
    scrollToBottom();
  }

  /**
   * Disables all buttons in the plan card button bar after a decision.
   */
  private static void disablePlanButtons(HorizontalPanel buttonBar) {
    for (int i = 0; i < buttonBar.getWidgetCount(); i++) {
      if (buttonBar.getWidget(i) instanceof Button) {
        Button btn = (Button) buttonBar.getWidget(i);
        btn.setEnabled(false);
        btn.getElement().getStyle().setOpacity(0.5);
      }
    }
  }

  /**
   * Builds HTML for the plan steps from the plan JSON. Parses the "steps"
   * array using lightweight string extraction.
   */
  private static String buildStepsHtml(String planJson) {
    // Find the "steps" array in the JSON
    int stepsIdx = planJson.indexOf("\"steps\"");
    if (stepsIdx < 0) {
      return null;
    }
    int arrStart = planJson.indexOf('[', stepsIdx);
    if (arrStart < 0) {
      return null;
    }

    // Extract individual step objects by finding matching braces
    StringBuilder html = new StringBuilder();
    int pos = arrStart + 1;
    while (pos < planJson.length()) {
      int objStart = planJson.indexOf('{', pos);
      if (objStart < 0) {
        break;
      }
      int objEnd = findMatchingBrace(planJson, objStart);
      if (objEnd < 0) {
        break;
      }
      String stepJson = planJson.substring(objStart, objEnd + 1);

      String stepId = AIJsonUtils.extractField(stepJson, "id");
      String screen = AIJsonUtils.extractField(stepJson, "screen");
      String description = AIJsonUtils.extractField(stepJson, "description");
      String dependsOn = extractArrayField(stepJson, "depends_on");

      // Format the screen target as a badge; skip __project__ steps (no badge)
      String screenLabel;
      if ("__project__".equals(screen)) {
        screenLabel = null;
      } else if (screen != null && !screen.isEmpty() && !"screen".equals(screen)) {
        screenLabel = screen;
      } else {
        screenLabel = null;
      }

      html.append("<div style='padding:3px 0; padding-left:4px;'>");
      if (screenLabel != null) {
        html.append("<span style='display:inline-block; background:#e0e0e0; "
            + "border-radius:3px; padding:1px 6px; font-size:11px; "
            + "color:#555; margin-right:6px;'>")
            .append(escapeHtml(screenLabel)).append("</span>");
      }
      html.append(escapeHtml(description));
      html.append("</div>");

      pos = objEnd + 1;
    }
    return html.toString();
  }

  /**
   * Finds the index of the closing brace that matches the opening brace
   * at {@code start}. Handles nested braces and quoted strings.
   */
  private static int findMatchingBrace(String json, int start) {
    int depth = 0;
    boolean inString = false;
    for (int i = start; i < json.length(); i++) {
      char c = json.charAt(i);
      if (inString) {
        if (c == '\\') {
          i++; // skip escaped character
        } else if (c == '"') {
          inString = false;
        }
      } else {
        if (c == '"') {
          inString = true;
        } else if (c == '{') {
          depth++;
        } else if (c == '}') {
          depth--;
          if (depth == 0) {
            return i;
          }
        }
      }
    }
    return -1;
  }

  /**
   * Extracts a simple JSON array field as a comma-separated string of values.
   * For example, {@code "depends_on": ["s1", "s2"]} returns "s1, s2".
   */
  private static String extractArrayField(String json, String fieldName) {
    String key = "\"" + fieldName + "\"";
    int idx = json.indexOf(key);
    if (idx < 0) {
      return null;
    }
    int arrStart = json.indexOf('[', idx + key.length());
    if (arrStart < 0) {
      return null;
    }
    int arrEnd = json.indexOf(']', arrStart);
    if (arrEnd < 0) {
      return null;
    }
    String inner = json.substring(arrStart + 1, arrEnd).trim();
    if (inner.isEmpty()) {
      return null;
    }
    // Strip quotes and join
    StringBuilder sb = new StringBuilder();
    String[] parts = inner.split(",");
    for (int i = 0; i < parts.length; i++) {
      String part = parts[i].trim();
      if (part.startsWith("\"") && part.endsWith("\"")) {
        part = part.substring(1, part.length() - 1);
      }
      if (i > 0) {
        sb.append(", ");
      }
      sb.append(part);
    }
    return sb.toString();
  }

  /**
   * Escapes HTML special characters for safe embedding in innerHTML.
   */
  private static String escapeHtml(String text) {
    if (text == null) {
      return "";
    }
    return text.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;");
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
    streamingThinkingAccumulator = "";
    streamingWrapper = createMessageBubble(
        MESSAGES.aiChatAiLabel(), "", false);
    FlowPanel bubble = (FlowPanel) streamingWrapper.getWidget(0);
    streamingMessageHtml = (HTML) bubble.getWidget(1);
    // Add typing indicator (three bouncing dots) below the message text
    typingIndicator = new HTML(
        "<div class='ai-typing-indicator'><span></span><span></span><span></span></div>");
    bubble.add(typingIndicator);
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
        + markdownToSafeHtml(streamingThinkingAccumulator)
        + "</div></details>");
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
      // Remove typing indicator
      if (typingIndicator != null) {
        typingIndicator.removeFromParent();
        typingIndicator = null;
      }
      if (finalText != null && !finalText.isEmpty()) {
        // Canonical text from the server — use it as the final content
        streamingMessageHtml.setHTML(markdownToSafeHtml(finalText));
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
        appendFeedbackLink(streamingWrapper);
      }

      // Collapse the thinking panel (switch from open to closed)
      if (streamingThinkingHtml != null && !streamingThinkingAccumulator.isEmpty()) {
        streamingThinkingHtml.setHTML(
            "<details class='ai-thinking-details'>"
            + "<summary>Thinking</summary>"
            + "<div class='ai-thinking-text'>"
            + markdownToSafeHtml(streamingThinkingAccumulator)
            + "</div></details>");
      } else if (streamingThinkingPanel != null
          && streamingThinkingAccumulator.isEmpty()) {
        streamingThinkingPanel.removeFromParent();
      }
      scrollToBottom();
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

  /**
   * Appends a small "Share Feedback" link at the bottom of an AI message bubble.
   * The link opens the built-in {@code /ode/feedback} form in a new tab,
   * pre-populated with the conversation ID, message timestamp, and project ID
   * so the feedback can be correlated with server-side debug logs.
   */
  private void appendFeedbackLink(FlowPanel wrapper) {
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
   * Builds the feedback form URL for the given conversation, using the
   * built-in {@code /ode/feedback} endpoint with pre-populated context.
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
