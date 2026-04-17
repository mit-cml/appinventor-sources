# AIChatRenderer chat/ Subpackage Decomposition — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Decompose `AIChatRenderer.java` (750 lines) into three focused classes under an `aiagent/chat/` subpackage while preserving the existing public API.

**Architecture:** Extract streaming state machine and plan card UI into `ChatStreamingHandler` and `ChatPlanCardRenderer` under a new `aiagent/chat/` package. A `ChatRendererHost` interface bridges the streaming handler back to the renderer without circular dependencies. `AIChatRenderer` stays in `aiagent/` as a facade that composes both new classes.

**Tech Stack:** GWT (Java 8 source level, no lambdas in practice), Ant build

---

## File Map

| File | Action | Responsibility |
|------|--------|----------------|
| `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/chat/ChatRendererHost.java` | Create | Interface for streaming handler to call back into the renderer |
| `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/chat/ChatStreamingHandler.java` | Create | Streaming bubble state machine |
| `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/chat/ChatPlanCardRenderer.java` | Create | Plan card UI with approve/edit/reject flow |
| `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/AIChatRenderer.java` | Modify | Implement ChatRendererHost, delegate to new classes, remove extracted code |
| `CONTRIBUTING_AI.md` | Modify | Add chat/ subpackage table, update AIChatRenderer description |

---

## Task 1: Create ChatRendererHost interface

**Files:**
- Create: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/chat/ChatRendererHost.java`

- [ ] **Step 1: Create the chat/ package directory**

```bash
mkdir -p appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/chat
```

- [ ] **Step 2: Write ChatRendererHost.java**

```java
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
```

- [ ] **Step 3: Verify compilation**

```bash
cd appinventor && ant -f appengine/build.xml AiClientLib
```

Expected: BUILD SUCCESSFUL (interface has no dependencies on project code beyond GWT).

- [ ] **Step 4: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/chat/ChatRendererHost.java
git commit -m "refactor(ai-agent): add ChatRendererHost interface for chat/ subpackage"
```

---

## Task 2: Create ChatStreamingHandler

**Files:**
- Create: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/chat/ChatStreamingHandler.java`
- Reference: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/AIChatRenderer.java:475-607` (code being extracted)

- [ ] **Step 1: Write ChatStreamingHandler.java**

This is a direct extraction of methods and fields from `AIChatRenderer.java`. All method bodies are identical except `chatHistory.add(...)` becomes `host.addToHistory(...)`, `scrollToBottom()` becomes `host.scrollToBottom()`, `markdownToSafeHtml(...)` becomes `host.markdownToSafeHtml(...)`, `MESSAGES.aiChatAiLabel()` becomes `host.getAiLabel()`, and `appendFeedbackLink(...)` becomes `host.appendFeedbackLink(...)`.

```java
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
```

- [ ] **Step 2: Verify compilation**

```bash
cd appinventor && ant -f appengine/build.xml AiClientLib
```

Expected: BUILD SUCCESSFUL. `ChatStreamingHandler` depends only on `ChatRendererHost` (same package) and GWT classes.

- [ ] **Step 3: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/chat/ChatStreamingHandler.java
git commit -m "refactor(ai-agent): extract ChatStreamingHandler from AIChatRenderer"
```

---

## Task 3: Create ChatPlanCardRenderer

**Files:**
- Create: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/chat/ChatPlanCardRenderer.java`
- Reference: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/AIChatRenderer.java:130-454` (code being extracted)

- [ ] **Step 1: Write ChatPlanCardRenderer.java**

Direct extraction from `AIChatRenderer.java`. All method bodies are identical except `scrollToBottom()` becomes `scrollCallback.run()` and `chatHistory` comes from the constructor field.

```java
// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.aiagent.chat;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.editor.youngandroid.aiagent.AIJsonUtils;
import com.google.appinventor.client.editor.youngandroid.aiagent.AIResponseOrchestrator;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;

/**
 * Renders plan proposal cards inline in the chat history.
 *
 * <p>Each card shows a summary, steps, and approve/edit/reject buttons.
 * The edit flow replaces the button bar with a JSON textarea for manual
 * plan editing.</p>
 */
public class ChatPlanCardRenderer {

  private final FlowPanel chatHistory;
  private final Runnable scrollCallback;

  private HorizontalPanel activePlanButtonBar = null;

  /**
   * Constructs a plan card renderer.
   *
   * @param chatHistory    the panel that holds chat message widgets
   * @param scrollCallback called after adding/modifying cards to scroll to bottom
   */
  public ChatPlanCardRenderer(FlowPanel chatHistory, Runnable scrollCallback) {
    this.chatHistory = chatHistory;
    this.scrollCallback = scrollCallback;
  }

  /**
   * Renders a plan card in the chat with summary, steps, and
   * approve/edit/reject buttons.
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
    activePlanButtonBar = buttonBar;
    scrollCallback.run();
  }

  /**
   * Disables the active plan card buttons (e.g., when the user sends a message
   * instead of clicking approve/reject).
   */
  public void dismissActivePlanCard() {
    if (activePlanButtonBar != null) {
      disablePlanButtons(activePlanButtonBar);
      activePlanButtonBar = null;
    }
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
    scrollCallback.run();
  }

  /**
   * Disables all buttons in the plan card button bar after a decision.
   */
  private void disablePlanButtons(HorizontalPanel buttonBar) {
    if (buttonBar == activePlanButtonBar) {
      activePlanButtonBar = null;
    }
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
}
```

- [ ] **Step 2: Verify compilation**

```bash
cd appinventor && ant -f appengine/build.xml AiClientLib
```

Expected: BUILD SUCCESSFUL. `ChatPlanCardRenderer` depends on `AIJsonUtils` and `AIResponseOrchestrator.PlanApprovalCallback` from the parent `aiagent` package, plus GWT classes.

- [ ] **Step 3: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/chat/ChatPlanCardRenderer.java
git commit -m "refactor(ai-agent): extract ChatPlanCardRenderer from AIChatRenderer"
```

---

## Task 4: Refactor AIChatRenderer to delegate to new classes

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/AIChatRenderer.java`

This is the core refactoring step. `AIChatRenderer` implements `ChatRendererHost`, composes the two new classes, and delegates streaming/plan-card calls to them. All extracted methods and fields are removed.

- [ ] **Step 1: Rewrite AIChatRenderer.java**

The file should contain only the facade, message bubble rendering, feedback links, and Markdown JSNI methods. The complete replacement:

```java
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
```

- [ ] **Step 2: Verify compilation**

```bash
cd appinventor && ant -f appengine/build.xml AiClientLib
```

Expected: BUILD SUCCESSFUL. The public API is unchanged — `AIChatDialog` still compiles with zero changes.

- [ ] **Step 3: Verify full test suite**

```bash
cd appinventor && ant -f appengine/build.xml tests
```

Expected: All tests pass. No client-side tests directly test `AIChatRenderer` internals; they go through `AIChatDialog`.

- [ ] **Step 4: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/AIChatRenderer.java
git commit -m "refactor(ai-agent): AIChatRenderer delegates to ChatStreamingHandler and ChatPlanCardRenderer"
```

---

## Task 5: Update CONTRIBUTING_AI.md

**Files:**
- Modify: `CONTRIBUTING_AI.md:119-131`

- [ ] **Step 1: Update the Client section table**

In `CONTRIBUTING_AI.md`, find the Client table (line ~124) and change the `AIChatRenderer.java` row from:

```
| `AIChatRenderer.java` | Renders chat messages with Markdown and streaming support |
```

to:

```
| `AIChatRenderer.java` | Facade for chat rendering; delegates streaming to `ChatStreamingHandler` and plan cards to `ChatPlanCardRenderer` |
```

- [ ] **Step 2: Add the chat/ subpackage table**

After the existing Client table (after line ~131), add:

```markdown
### Chat Rendering -- `client/.../aiagent/chat/`

| File | Purpose |
|------|---------|
| `ChatRendererHost.java` | Interface for streaming handler to call back into the renderer |
| `ChatStreamingHandler.java` | Streaming bubble state machine: start, append text/thinking, finalize |
| `ChatPlanCardRenderer.java` | Plan card UI with approve/edit/reject flow |
```

- [ ] **Step 3: Commit**

```bash
git add CONTRIBUTING_AI.md
git commit -m "docs: update CONTRIBUTING_AI.md with chat/ subpackage"
```
