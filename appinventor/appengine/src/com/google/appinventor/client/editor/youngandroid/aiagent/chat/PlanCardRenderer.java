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
public class PlanCardRenderer {

  private final FlowPanel chatHistory;
  private final Runnable scrollCallback;

  private HorizontalPanel activePlanButtonBar = null;

  /**
   * Constructs a plan card renderer.
   *
   * @param chatHistory    the panel that holds chat message widgets
   * @param scrollCallback called after adding/modifying cards to scroll to bottom
   */
  public PlanCardRenderer(FlowPanel chatHistory, Runnable scrollCallback) {
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
