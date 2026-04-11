// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.aiagent.dialog;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.youngandroid.aiagent.AIChatRenderer;
import com.google.appinventor.client.editor.youngandroid.aiagent.AIResponseOrchestrator;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextArea;

/**
 * Manages the chat input area and send/stop button.
 *
 * <p>Handles Enter-key submission, project-ID validation,
 * plan-rejection wrapping, and send/stop button toggling.</p>
 */
public class ChatInputHandler {

  /**
   * Callback for cross-cutting side effects when a message is sent.
   */
  public interface SendCallback {
    void onMessageSent();
  }

  private final AIResponseOrchestrator orchestrator;
  private final AIChatRenderer renderer;
  private final SendCallback callback;
  private final HorizontalPanel panel;
  private final TextArea inputArea;
  private final Button sendButton;

  /**
   * Constructs the input area and send button.
   *
   * @param orchestrator the orchestrator for sending messages
   * @param renderer     the renderer for displaying user messages
   * @param callback     notified after a message is sent
   */
  public ChatInputHandler(final AIResponseOrchestrator orchestrator,
      AIChatRenderer renderer, SendCallback callback) {
    this.orchestrator = orchestrator;
    this.renderer = renderer;
    this.callback = callback;

    panel = new HorizontalPanel();
    panel.setWidth("100%");
    panel.setSpacing(4);

    inputArea = new TextArea();
    inputArea.setVisibleLines(3);
    inputArea.setWidth("100%");
    inputArea.getElement().getStyle().setProperty("boxSizing", "border-box");
    inputArea.getElement().setAttribute("placeholder", MESSAGES.aiChatInputPlaceholder());
    inputArea.getElement().getStyle().setProperty("resize", "vertical");
    inputArea.getElement().getStyle().setProperty("borderRadius", "3px");
    inputArea.getElement().getStyle().setProperty("border", "1px solid #ccc");
    inputArea.getElement().getStyle().setPadding(4, Unit.PX);
    inputArea.addKeyDownHandler(new KeyDownHandler() {
      @Override
      public void onKeyDown(KeyDownEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER && !event.isShiftKeyDown()) {
          event.preventDefault();
          doSendMessage();
        }
      }
    });
    panel.add(inputArea);
    panel.setCellWidth(inputArea, "100%");

    sendButton = new Button(MESSAGES.aiChatSendButton());
    sendButton.getElement().getStyle().setProperty("background", "#4a90d9");
    sendButton.getElement().getStyle().setColor("white");
    sendButton.getElement().getStyle().setProperty("borderRadius", "3px");
    sendButton.getElement().getStyle().setProperty("cursor", "pointer");
    sendButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        doSendMessage();
      }
    });
    panel.add(sendButton);
  }

  /** Returns the input panel widget. */
  public HorizontalPanel getWidget() {
    return panel;
  }

  /**
   * Toggles the send/stop button appearance and input area enabled state.
   */
  public void setRequestInFlight(boolean inFlight) {
    inputArea.setEnabled(!inFlight);
    if (inFlight) {
      sendButton.setText(MESSAGES.aiChatStopButton());
      sendButton.getElement().getStyle().setProperty("background", "#d94a4a");
      sendButton.setEnabled(true);
    } else {
      sendButton.setText(MESSAGES.aiChatSendButton());
      sendButton.getElement().getStyle().setProperty("background", "#4a90d9");
      sendButton.setEnabled(true);
    }
  }

  private void doSendMessage() {
    if (orchestrator.isRequestInFlight()) {
      orchestrator.cancelRequest();
      return;
    }

    String text = inputArea.getText().trim();
    if (text.isEmpty()) {
      return;
    }

    long projectId = Ode.getInstance().getCurrentYoungAndroidProjectId();
    if (projectId == 0) {
      ErrorReporter.reportError(MESSAGES.aiChatNoProject());
      return;
    }

    renderer.addUserMessage(text);
    inputArea.setText("");

    if (orchestrator.hasPendingPlanProposal()) {
      renderer.dismissActivePlanCard();
      orchestrator.dismissPendingPlan();
      text = "The user rejected the proposed plan. Their feedback: " + text;
    }
    callback.onMessageSent();
    orchestrator.sendMessage(text);
  }
}
