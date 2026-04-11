// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.aiagent.dialog;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.editor.youngandroid.aiagent.AIOperationFormatter;
import com.google.appinventor.client.editor.youngandroid.aiagent.AIResponseOrchestrator;
import com.google.appinventor.shared.rpc.aiagent.AIAgentResponse;
import com.google.appinventor.shared.rpc.aiagent.AIOperation;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;

import java.util.List;

/**
 * Renders the operation preview area showing proposed AI changes,
 * the apply/reject action buttons, and the auto-accept notice.
 */
public class OperationPreviewPanel {

  private final FlowPanel previewPanel;
  private final Button applyButton;
  private final Button applyAndAcceptAllButton;
  private final Button rejectButton;
  private final HorizontalPanel buttonsPanel;
  private final FlowPanel autoAcceptPanel;

  /**
   * Constructs the preview panel and action buttons, wiring click
   * handlers to the given orchestrator.
   */
  public OperationPreviewPanel(final AIResponseOrchestrator orchestrator) {
    // Preview area
    previewPanel = new FlowPanel();
    previewPanel.getElement().getStyle().setProperty("border", "1px solid #4a90d9");
    previewPanel.getElement().getStyle().setProperty("borderRadius", "4px");
    previewPanel.getElement().getStyle().setProperty("background", "#eef4fb");
    previewPanel.getElement().getStyle().setPadding(6, Unit.PX);
    previewPanel.getElement().getStyle().setMarginBottom(6, Unit.PX);
    previewPanel.setVisible(false);

    // Action buttons
    buttonsPanel = new HorizontalPanel();
    buttonsPanel.setSpacing(4);

    applyButton = new Button(MESSAGES.aiChatApplyButton());
    applyButton.getElement().getStyle().setProperty("background", "#4CAF50");
    applyButton.getElement().getStyle().setColor("white");
    applyButton.getElement().getStyle().setProperty("borderRadius", "3px");
    applyButton.getElement().getStyle().setProperty("cursor", "pointer");
    applyButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        orchestrator.applyOperations();
      }
    });
    applyButton.setVisible(false);
    buttonsPanel.add(applyButton);

    applyAndAcceptAllButton = new Button(MESSAGES.aiChatApplyAndAcceptAllButton());
    applyAndAcceptAllButton.getElement().getStyle().setProperty("background", "#FF9800");
    applyAndAcceptAllButton.getElement().getStyle().setColor("white");
    applyAndAcceptAllButton.getElement().getStyle().setProperty("borderRadius", "3px");
    applyAndAcceptAllButton.getElement().getStyle().setProperty("cursor", "pointer");
    applyAndAcceptAllButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        orchestrator.applyAndAcceptAll();
      }
    });
    applyAndAcceptAllButton.setVisible(false);
    buttonsPanel.add(applyAndAcceptAllButton);

    rejectButton = new Button(MESSAGES.aiChatRejectButton());
    rejectButton.getElement().getStyle().setProperty("background", "#f44336");
    rejectButton.getElement().getStyle().setColor("white");
    rejectButton.getElement().getStyle().setProperty("borderRadius", "3px");
    rejectButton.getElement().getStyle().setProperty("cursor", "pointer");
    rejectButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        orchestrator.rejectOperations();
      }
    });
    rejectButton.setVisible(false);
    buttonsPanel.add(rejectButton);

    // Auto-accept notice
    autoAcceptPanel = new FlowPanel();
    autoAcceptPanel.getElement().getStyle().setFontSize(11, Unit.PX);
    autoAcceptPanel.getElement().getStyle().setColor("#e65100");
    autoAcceptPanel.getElement().getStyle().setMarginTop(2, Unit.PX);
    autoAcceptPanel.getElement().getStyle().setMarginBottom(4, Unit.PX);
    autoAcceptPanel.setVisible(false);

    InlineLabel autoAcceptText = new InlineLabel(MESSAGES.aiChatAutoAcceptEnabled() + " ");
    autoAcceptPanel.add(autoAcceptText);

    InlineLabel autoAcceptDisable = new InlineLabel(MESSAGES.aiChatAutoAcceptDisable());
    autoAcceptDisable.getElement().getStyle().setProperty("textDecoration", "underline");
    autoAcceptDisable.getElement().getStyle().setProperty("cursor", "pointer");
    autoAcceptDisable.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        orchestrator.resetAutoAcceptAll();
      }
    });
    autoAcceptPanel.add(autoAcceptDisable);
  }

  /** Returns the preview area widget. */
  public FlowPanel getPreviewWidget() {
    return previewPanel;
  }

  /** Returns the action buttons row widget. */
  public HorizontalPanel getButtonsWidget() {
    return buttonsPanel;
  }

  /** Returns the auto-accept notice widget. */
  public FlowPanel getAutoAcceptWidget() {
    return autoAcceptPanel;
  }

  /**
   * Renders the proposed operations with color-coded labels.
   */
  public void showPreview(AIAgentResponse response) {
    previewPanel.clear();

    String aiMessage = response.getAiMessage();
    if (aiMessage != null && !aiMessage.isEmpty()) {
      Label aiLabel = new Label(aiMessage);
      aiLabel.getElement().getStyle().setFontSize(12, Unit.PX);
      aiLabel.getElement().getStyle().setColor("#555");
      aiLabel.getElement().getStyle().setMarginBottom(4, Unit.PX);
      previewPanel.add(aiLabel);
    }

    Label header = new Label(MESSAGES.aiChatProposedChanges());
    header.getElement().getStyle().setProperty("fontWeight", "bold");
    header.getElement().getStyle().setMarginBottom(4, Unit.PX);
    previewPanel.add(header);

    List<AIOperation> operations = response.getOperations();
    for (AIOperation op : operations) {
      Label opLabel = new Label(AIOperationFormatter.formatOperation(op));
      opLabel.getElement().getStyle().setFontSize(12, Unit.PX);
      opLabel.getElement().getStyle().setMarginBottom(3, Unit.PX);
      opLabel.getElement().getStyle().setPaddingLeft(8, Unit.PX);
      opLabel.getElement().getStyle().setProperty("fontFamily",
          "'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace");
      AIOperation.Type type = op.getType();
      if (type == AIOperation.Type.ADD_COMPONENT
          || type == AIOperation.Type.CREATE_SCREEN
          || type == AIOperation.Type.WRITE_BLOCK) {
        opLabel.getElement().getStyle().setColor("#2e7d32");
      } else if (type == AIOperation.Type.DELETE_COMPONENT
          || type == AIOperation.Type.DELETE_SCREEN
          || type == AIOperation.Type.DELETE_BLOCK) {
        opLabel.getElement().getStyle().setColor("#c62828");
      } else {
        opLabel.getElement().getStyle().setColor("#1565c0");
      }
      previewPanel.add(opLabel);
    }

    previewPanel.setVisible(true);
    applyButton.setVisible(true);
    applyAndAcceptAllButton.setVisible(true);
    rejectButton.setVisible(true);
  }

  /**
   * Hides the preview area and action buttons.
   */
  public void hidePreview() {
    previewPanel.setVisible(false);
    applyButton.setVisible(false);
    applyAndAcceptAllButton.setVisible(false);
    rejectButton.setVisible(false);
  }

  /**
   * Shows or hides the auto-accept notice.
   */
  public void setAutoAcceptVisible(boolean visible) {
    autoAcceptPanel.setVisible(visible);
  }
}
