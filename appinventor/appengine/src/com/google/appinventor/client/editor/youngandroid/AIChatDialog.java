// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.editor.ProjectEditor;
import com.google.appinventor.shared.rpc.aiagent.AIAgentRequest;
import com.google.appinventor.shared.rpc.aiagent.AIAgentResponse;
import com.google.appinventor.shared.rpc.aiagent.AIAgentService;
import com.google.appinventor.shared.rpc.aiagent.AIAgentServiceAsync;
import com.google.appinventor.shared.rpc.aiagent.AIConversationMessage;
import com.google.appinventor.shared.rpc.aiagent.AIOperation;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.RpcRequestBuilder;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.List;
import java.util.logging.Logger;

/**
 * A floating dialog that provides an AI assistant chat interface.
 *
 * <p>The dialog communicates with the server-side AI agent via GWT-RPC.
 * Users type natural-language requests, the AI responds with explanations
 * and structured operations, and the user can review/apply/reject those
 * operations before they take effect on the project.</p>
 *
 * <p>Conversation state is managed server-side (keyed by project ID).
 * On open, existing conversation history is loaded. A polling timer
 * displays intermediate status while a request is in flight.</p>
 */
public class AIChatDialog extends DialogBox {

  private static final Logger LOG = Logger.getLogger(AIChatDialog.class.getName());

  /** RPC timeout for processRequest (2 minutes). */
  private static final int RPC_TIMEOUT_MS = 120000;

  /** Polling interval for request status (1 second). */
  private static final int POLL_INTERVAL_MS = 1000;

  /** Default dialog width. */
  private static final int DIALOG_WIDTH = 420;

  /** Default dialog height. */
  private static final int DIALOG_HEIGHT = 560;

  // RPC service
  private final AIAgentServiceAsync aiAgentService;

  // UI components
  private final ScrollPanel chatScrollPanel;
  private final FlowPanel chatHistory;
  private final FlowPanel operationPreview;
  private final TextArea inputArea;
  private final Button sendButton;
  private final Button applyButton;
  private final Button rejectButton;
  private final Button newConversationButton;
  private final Label statusLabel;

  // State
  private AIAgentResponse pendingResponse;
  private Timer pollingTimer;
  private boolean requestInFlight;

  // Remembered position so the dialog reopens where the user left it
  private int lastPopupLeft = -1;
  private int lastPopupTop = -1;

  /**
   * Constructs the AI chat dialog, building the full UI hierarchy.
   */
  public AIChatDialog() {
    super(false /* autohide */, false /* modal */);
    setText(MESSAGES.aiChatDialogTitle());
    setAnimationEnabled(true);
    setGlassEnabled(false);

    // Create the RPC service proxy
    aiAgentService = GWT.create(AIAgentService.class);
    configureServiceTimeout(aiAgentService);

    // ---- Build the UI ----

    VerticalPanel mainPanel = new VerticalPanel();
    mainPanel.getElement().getStyle().setPadding(8, Unit.PX);
    mainPanel.setWidth(DIALOG_WIDTH + "px");

    // Chat history area
    chatHistory = new FlowPanel();
    chatHistory.getElement().getStyle().setProperty("minHeight", "100px");

    chatScrollPanel = new ScrollPanel(chatHistory);
    chatScrollPanel.setSize(DIALOG_WIDTH + "px", (DIALOG_HEIGHT - 200) + "px");
    chatScrollPanel.getElement().getStyle().setProperty("border", "1px solid #ccc");
    chatScrollPanel.getElement().getStyle().setProperty("borderRadius", "4px");
    chatScrollPanel.getElement().getStyle().setProperty("background", "#fafafa");
    chatScrollPanel.getElement().getStyle().setMarginBottom(6, Unit.PX);

    mainPanel.add(chatScrollPanel);

    // Status label (shown while AI is thinking)
    statusLabel = new Label();
    statusLabel.getElement().getStyle().setProperty("fontStyle", "italic");
    statusLabel.getElement().getStyle().setColor("#666");
    statusLabel.getElement().getStyle().setMarginBottom(4, Unit.PX);
    statusLabel.setVisible(false);
    mainPanel.add(statusLabel);

    // Operation preview area (hidden until operations arrive)
    operationPreview = new FlowPanel();
    operationPreview.getElement().getStyle().setProperty("border", "1px solid #4a90d9");
    operationPreview.getElement().getStyle().setProperty("borderRadius", "4px");
    operationPreview.getElement().getStyle().setProperty("background", "#eef4fb");
    operationPreview.getElement().getStyle().setPadding(6, Unit.PX);
    operationPreview.getElement().getStyle().setMarginBottom(6, Unit.PX);
    operationPreview.setVisible(false);
    mainPanel.add(operationPreview);

    // Apply / Reject buttons (hidden until operations arrive)
    HorizontalPanel actionButtons = new HorizontalPanel();
    actionButtons.setSpacing(4);

    applyButton = new Button(MESSAGES.aiChatApplyButton());
    applyButton.getElement().getStyle().setProperty("background", "#4CAF50");
    applyButton.getElement().getStyle().setColor("white");
    applyButton.getElement().getStyle().setProperty("borderRadius", "3px");
    applyButton.getElement().getStyle().setProperty("cursor", "pointer");
    applyButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        applyOperations();
      }
    });
    applyButton.setVisible(false);
    actionButtons.add(applyButton);

    rejectButton = new Button(MESSAGES.aiChatRejectButton());
    rejectButton.getElement().getStyle().setProperty("background", "#f44336");
    rejectButton.getElement().getStyle().setColor("white");
    rejectButton.getElement().getStyle().setProperty("borderRadius", "3px");
    rejectButton.getElement().getStyle().setProperty("cursor", "pointer");
    rejectButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        rejectOperations();
      }
    });
    rejectButton.setVisible(false);
    actionButtons.add(rejectButton);

    mainPanel.add(actionButtons);

    // Input area + send button
    HorizontalPanel inputPanel = new HorizontalPanel();
    inputPanel.setWidth("100%");
    inputPanel.setSpacing(4);

    inputArea = new TextArea();
    inputArea.setCharacterWidth(40);
    inputArea.setVisibleLines(3);
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
          sendMessage();
        }
      }
    });
    inputPanel.add(inputArea);

    sendButton = new Button(MESSAGES.aiChatSendButton());
    sendButton.getElement().getStyle().setProperty("background", "#4a90d9");
    sendButton.getElement().getStyle().setColor("white");
    sendButton.getElement().getStyle().setProperty("borderRadius", "3px");
    sendButton.getElement().getStyle().setProperty("cursor", "pointer");
    sendButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        sendMessage();
      }
    });
    inputPanel.add(sendButton);

    mainPanel.add(inputPanel);

    // Bottom toolbar: new conversation + close
    HorizontalPanel bottomBar = new HorizontalPanel();
    bottomBar.setSpacing(4);
    bottomBar.getElement().getStyle().setMarginTop(6, Unit.PX);

    newConversationButton = new Button(MESSAGES.aiChatNewConversationButton());
    newConversationButton.getElement().getStyle().setProperty("cursor", "pointer");
    newConversationButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        clearConversation();
      }
    });
    bottomBar.add(newConversationButton);

    Button closeButton = new Button(MESSAGES.aiChatCloseButton());
    closeButton.getElement().getStyle().setProperty("cursor", "pointer");
    closeButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        hideDialog();
      }
    });
    bottomBar.add(closeButton);

    mainPanel.add(bottomBar);

    setWidget(mainPanel);
  }

  // ---- Lifecycle ----

  /**
   * Overrides show to check AI mode and load existing conversation history.
   * If the mode is Off, shows a mode selection dialog instead.
   */
  @Override
  public void show() {
    String mode = getCurrentAIAgentMode();
    if ("Off".equals(mode)) {
      showModeSelectionDialog();
      return;
    }
    super.show();
    // Restore previous position, or center on first open
    if (lastPopupLeft >= 0 && lastPopupTop >= 0) {
      setPopupPosition(lastPopupLeft, lastPopupTop);
    } else {
      center();
    }
    loadExistingConversation();
  }

  /**
   * Hides the dialog and remembers its position.
   */
  public void hideDialog() {
    lastPopupLeft = getPopupLeft();
    lastPopupTop = getPopupTop();
    stopPollingStatus();
    hide();
  }

  // ---- Sending messages ----

  /**
   * Sends the current input text to the AI agent.
   * Creates an {@link AIAgentRequest} and invokes the processRequest RPC.
   */
  private void sendMessage() {
    String text = inputArea.getText().trim();
    if (text.isEmpty() || requestInFlight) {
      return;
    }

    long projectId = Ode.getInstance().getCurrentYoungAndroidProjectId();
    if (projectId == 0) {
      ErrorReporter.reportError(MESSAGES.aiChatNoProject());
      return;
    }

    String screenName = getCurrentScreenName();

    addUserMessage(text);
    inputArea.setText("");

    // Hide any previous operation preview
    hideOperationPreview();

    AIAgentRequest request = new AIAgentRequest(text, projectId, screenName);
    setRequestInFlight(true);
    startPollingStatus();

    aiAgentService.processRequest(request, new OdeAsyncCallback<AIAgentResponse>(
        MESSAGES.aiChatSendError()) {
      @Override
      public void onSuccess(AIAgentResponse response) {
        setRequestInFlight(false);
        stopPollingStatus();
        handleResponse(response);
      }

      @Override
      public void onFailure(Throwable caught) {
        super.onFailure(caught);
        setRequestInFlight(false);
        stopPollingStatus();
        addAiMessage(MESSAGES.aiChatSendError() + ": " + caught.getMessage());
      }
    });
  }

  /**
   * Processes a successful AI agent response.
   */
  private void handleResponse(AIAgentResponse response) {
    // Display the AI message
    String aiMessage = response.getAiMessage();
    if (aiMessage != null && !aiMessage.isEmpty()) {
      addAiMessage(aiMessage);
    }

    // Display any errors
    List<String> errors = response.getErrors();
    if (errors != null && !errors.isEmpty()) {
      for (String error : errors) {
        addAiMessage("Error: " + error);
      }
    }

    // Show operation preview if there are operations
    List<AIOperation> operations = response.getOperations();
    if (operations != null && !operations.isEmpty()) {
      showOperationPreview(response);
    }
  }

  // ---- Chat message display ----

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

    // Use HTML to support line breaks in messages
    HTML messageHtml = new HTML(escapeAndFormat(text));
    messageHtml.getElement().getStyle().setFontSize(13, Unit.PX);
    messageHtml.getElement().getStyle().setProperty("wordWrap", "break-word");
    bubble.add(messageHtml);

    wrapper.add(bubble);
    return wrapper;
  }

  // ---- Operation preview ----

  /**
   * Displays a human-readable preview of the proposed operations.
   *
   * @param response the AI response containing operations to preview
   */
  public void showOperationPreview(AIAgentResponse response) {
    pendingResponse = response;
    operationPreview.clear();

    Label header = new Label(MESSAGES.aiChatProposedChanges());
    header.getElement().getStyle().setProperty("fontWeight", "bold");
    header.getElement().getStyle().setMarginBottom(4, Unit.PX);
    operationPreview.add(header);

    List<AIOperation> operations = response.getOperations();
    for (AIOperation op : operations) {
      Label opLabel = new Label(formatOperation(op));
      opLabel.getElement().getStyle().setFontSize(12, Unit.PX);
      opLabel.getElement().getStyle().setMarginBottom(2, Unit.PX);
      opLabel.getElement().getStyle().setPaddingLeft(8, Unit.PX);
      operationPreview.add(opLabel);
    }

    operationPreview.setVisible(true);
    applyButton.setVisible(true);
    rejectButton.setVisible(true);
  }

  /**
   * Hides the operation preview and action buttons.
   */
  private void hideOperationPreview() {
    operationPreview.setVisible(false);
    applyButton.setVisible(false);
    rejectButton.setVisible(false);
    pendingResponse = null;
  }

  /**
   * Formats a single AI operation into a human-readable string.
   *
   * @param op the operation to format
   * @return a human-readable description
   */
  private String formatOperation(AIOperation op) {
    AIOperation.Type type = op.getType();
    String payload = op.getPayload();
    // Provide a readable summary based on operation type.
    // Field names must match the JSON payload keys used by AIOperationExecutor.
    switch (type) {
      case ADD_COMPONENT:
        return "+ Add component: " + extractField(payload, "type")
            + " (" + extractField(payload, "name") + ")";
      case DELETE_COMPONENT:
        return "- Delete component: " + extractField(payload, "name");
      case SET_PROPERTY:
        return "~ Set property: " + extractField(payload, "component")
            + "." + extractField(payload, "property");
      case RENAME_COMPONENT:
        return "~ Rename: " + extractField(payload, "oldName")
            + " -> " + extractField(payload, "newName");
      case SET_EVENT_HANDLER:
        return "+ Set event handler: " + extractField(payload, "component")
            + "." + extractField(payload, "event");
      case DELETE_EVENT_HANDLER:
        return "- Delete event handler: " + extractField(payload, "component")
            + "." + extractField(payload, "event");
      case SET_VARIABLE:
        return "+ Set variable: " + extractField(payload, "name");
      case DELETE_VARIABLE:
        return "- Delete variable: " + extractField(payload, "name");
      case SET_PROCEDURE:
        return "+ Set procedure: " + extractField(payload, "name");
      case DELETE_PROCEDURE:
        return "- Delete procedure: " + extractField(payload, "name");
      case SWITCH_SCREEN:
        return "~ Switch to screen: " + extractField(payload, "screen");
      case CREATE_SCREEN:
        return "+ Create screen: " + extractField(payload, "screen");
      case DELETE_SCREEN:
        return "- Delete screen: " + extractField(payload, "screen");
      case SET_PROJECT_PROP:
        return "~ Set project property: " + extractField(payload, "name");
      default:
        return type.name() + ": " + payload;
    }
  }

  // ---- Operation execution ----

  /**
   * Applies the pending AI operations via the {@link AIOperationExecutor}.
   * Operations are executed asynchronously in phases; the callback reports
   * the final result. If the response has more batches ({@code hasMore}),
   * a continuation request is sent after successful application.
   */
  private void applyOperations() {
    if (pendingResponse == null) {
      return;
    }

    final List<AIOperation> operations = pendingResponse.getOperations();
    final boolean hasMore = pendingResponse.hasMore();
    hideOperationPreview();
    setRequestInFlight(true);

    AIOperationExecutor executor = new AIOperationExecutor();
    executor.execute(operations,
        new AIOperationExecutor.ExecutionCallback() {
          @Override
          public void onComplete(AIOperationExecutor.ExecutionResult result) {
            if (result.isSuccess()) {
              addAiMessage(MESSAGES.aiChatOperationsApplied());
              if (hasMore) {
                // More batches expected — request the next one
                startPollingStatus();
                fetchContinuation();
              } else {
                setRequestInFlight(false);
              }
            } else {
              setRequestInFlight(false);
              addAiMessage(MESSAGES.aiChatApplyError() + ": " + result.getErrorMessage());
            }
          }
        });
  }

  /**
   * Calls the continueRequest RPC to fetch the next batch of operations
   * from a multi-step AI response. On success, displays the response
   * via {@link #handleResponse}, which will show the operation preview
   * for the user to apply or reject.
   */
  private void fetchContinuation() {
    long projectId = Ode.getInstance().getCurrentYoungAndroidProjectId();
    if (projectId == 0) {
      setRequestInFlight(false);
      stopPollingStatus();
      return;
    }

    String screenName = getCurrentScreenName();

    aiAgentService.continueRequest(projectId, screenName,
        new OdeAsyncCallback<AIAgentResponse>(MESSAGES.aiChatSendError()) {
          @Override
          public void onSuccess(AIAgentResponse response) {
            setRequestInFlight(false);
            stopPollingStatus();
            handleResponse(response);
          }

          @Override
          public void onFailure(Throwable caught) {
            super.onFailure(caught);
            setRequestInFlight(false);
            stopPollingStatus();
            addAiMessage(MESSAGES.aiChatSendError() + ": " + caught.getMessage());
          }
        });
  }

  /**
   * Rejects the pending operations and sends feedback to the server
   * so the AI knows the operations were discarded.
   */
  private void rejectOperations() {
    if (pendingResponse == null) {
      return;
    }

    addAiMessage(MESSAGES.aiChatOperationsRejected());
    hideOperationPreview();

    // Inform the server about the rejection by sending a feedback message
    long projectId = Ode.getInstance().getCurrentYoungAndroidProjectId();
    if (projectId == 0) {
      return;
    }
    String screenName = getCurrentScreenName();
    AIAgentRequest feedback = new AIAgentRequest(
        "The user rejected the proposed operations. Please suggest alternatives.",
        projectId, screenName);

    aiAgentService.processRequest(feedback, new OdeAsyncCallback<AIAgentResponse>(
        MESSAGES.aiChatSendError()) {
      @Override
      public void onSuccess(AIAgentResponse response) {
        handleResponse(response);
      }
    });
  }

  // ---- Conversation management ----

  /**
   * Loads existing conversation history from the server.
   * Called when the dialog is opened to restore previous messages.
   */
  private void loadExistingConversation() {
    long projectId = Ode.getInstance().getCurrentYoungAndroidProjectId();
    if (projectId == 0) {
      return;
    }

    chatHistory.clear();
    hideOperationPreview();

    aiAgentService.getConversationHistory(projectId,
        new OdeAsyncCallback<List<AIConversationMessage>>(MESSAGES.aiChatLoadHistoryError()) {
          @Override
          public void onSuccess(List<AIConversationMessage> messages) {
            if (messages != null) {
              for (AIConversationMessage msg : messages) {
                if ("user".equals(msg.getRole())) {
                  addUserMessage(msg.getText());
                } else {
                  addAiMessage(msg.getText());
                }
              }
            }
          }
        });
  }

  /**
   * Clears the current conversation on the server and in the UI.
   */
  private void clearConversation() {
    long projectId = Ode.getInstance().getCurrentYoungAndroidProjectId();
    if (projectId == 0) {
      return;
    }

    aiAgentService.clearConversation(projectId, new OdeAsyncCallback<Void>(
        MESSAGES.aiChatClearError()) {
      @Override
      public void onSuccess(Void result) {
        chatHistory.clear();
        hideOperationPreview();
      }
    });
  }

  // ---- Status polling ----

  /**
   * Starts a timer that polls getRequestStatus every second
   * to display intermediate progress while a request is in flight.
   */
  private void startPollingStatus() {
    statusLabel.setText(MESSAGES.aiChatThinking());
    statusLabel.setVisible(true);

    if (pollingTimer != null) {
      pollingTimer.cancel();
    }

    pollingTimer = new Timer() {
      @Override
      public void run() {
        long projectId = Ode.getInstance().getCurrentYoungAndroidProjectId();
        if (projectId == 0 || !requestInFlight) {
          stopPollingStatus();
          return;
        }

        aiAgentService.getRequestStatus(projectId, new OdeAsyncCallback<String>() {
          @Override
          public void onSuccess(String status) {
            if (status != null && !status.isEmpty()) {
              statusLabel.setText(status);
            }
          }

          @Override
          public void onFailure(Throwable caught) {
            // Silently ignore polling failures; the main request callback
            // handles real errors.
          }
        });
      }
    };
    pollingTimer.scheduleRepeating(POLL_INTERVAL_MS);
  }

  /**
   * Stops the status polling timer.
   */
  private void stopPollingStatus() {
    if (pollingTimer != null) {
      pollingTimer.cancel();
      pollingTimer = null;
    }
    statusLabel.setVisible(false);
  }

  // ---- Helpers ----

  /**
   * Sets the request-in-flight state and updates UI accordingly.
   */
  private void setRequestInFlight(boolean inFlight) {
    this.requestInFlight = inFlight;
    sendButton.setEnabled(!inFlight);
    inputArea.setEnabled(!inFlight);
  }

  /**
   * Returns the name of the currently active screen.
   */
  private String getCurrentScreenName() {
    DesignToolbar toolbar = Ode.getInstance().getDesignToolbar();
    if (toolbar != null) {
      DesignToolbar.DesignProject project = toolbar.getCurrentProject();
      if (project != null) {
        return project.currentScreen;
      }
    }
    return "Screen1";
  }

  /**
   * Scrolls the chat panel to the bottom.
   */
  private void scrollToBottom() {
    chatScrollPanel.scrollToBottom();
  }

  /**
   * Configures the RPC service proxy timeout to 2 minutes.
   * Uses GWT's {@link RpcRequestBuilder} to set the timeout on each
   * outgoing request via {@link ServiceDefTarget#setRpcRequestBuilder}.
   */
  private void configureServiceTimeout(AIAgentServiceAsync service) {
    if (service instanceof ServiceDefTarget) {
      ServiceDefTarget target = (ServiceDefTarget) service;
      target.setRpcRequestBuilder(new RpcRequestBuilder() {
        @Override
        protected void doFinish(com.google.gwt.http.client.RequestBuilder rb) {
          super.doFinish(rb);
          rb.setTimeoutMillis(RPC_TIMEOUT_MS);
        }
      });
    }
  }

  /**
   * Extracts a simple JSON field value from a JSON payload string.
   * This is a lightweight extraction -- not a full JSON parser.
   *
   * @param json      the JSON payload
   * @param fieldName the field name to extract
   * @return the extracted value, or the field name if not found
   */
  private static String extractField(String json, String fieldName) {
    if (json == null || json.isEmpty()) {
      return fieldName;
    }
    String key = "\"" + fieldName + "\"";
    int idx = json.indexOf(key);
    if (idx < 0) {
      return fieldName;
    }
    int colonIdx = json.indexOf(':', idx + key.length());
    if (colonIdx < 0) {
      return fieldName;
    }
    int start = colonIdx + 1;
    // Skip whitespace
    while (start < json.length() && json.charAt(start) == ' ') {
      start++;
    }
    if (start >= json.length()) {
      return fieldName;
    }
    if (json.charAt(start) == '"') {
      // String value
      int end = json.indexOf('"', start + 1);
      if (end < 0) {
        return json.substring(start + 1);
      }
      return json.substring(start + 1, end);
    } else {
      // Non-string value (number, boolean, etc.)
      int end = start;
      while (end < json.length()
          && json.charAt(end) != ',' && json.charAt(end) != '}') {
        end++;
      }
      return json.substring(start, end).trim();
    }
  }

  /**
   * Escapes HTML special characters and converts newlines to {@code <br>} tags.
   */
  private static String escapeAndFormat(String text) {
    if (text == null) {
      return "";
    }
    return text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("\n", "<br>");
  }

  /**
   * Returns the current AIAgentMode from the project settings.
   * Defaults to "Off" if no project is open or the setting is missing.
   */
  private String getCurrentAIAgentMode() {
    ProjectEditor projectEditor = Ode.getInstance().getEditorManager()
        .getOpenProjectEditor(getCurrentProjectId());
    if (projectEditor == null) {
      return "Off";
    }
    String mode = projectEditor.getProjectSettingsProperty(
        SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
        SettingsConstants.YOUNG_ANDROID_SETTINGS_AI_AGENT_MODE);
    return (mode == null || mode.isEmpty()) ? "Off" : mode;
  }

  /**
   * Returns the project ID of the currently open project, or 0 if none.
   */
  private long getCurrentProjectId() {
    DesignToolbar toolbar = Ode.getInstance().getDesignToolbar();
    if (toolbar != null) {
      DesignToolbar.DesignProject project = toolbar.getCurrentProject();
      if (project != null) {
        return project.getProjectId();
      }
    }
    return 0;
  }

  /**
   * Shows a mode selection dialog when the AI agent mode is Off.
   * The user picks a permission level; selecting one sets the AIAgentMode
   * project setting on Screen1 and then opens the chat dialog.
   */
  private void showModeSelectionDialog() {
    final DialogBox modeDialog = new DialogBox(false, true);
    modeDialog.setText(MESSAGES.aiChatDialogTitle());
    modeDialog.setAnimationEnabled(true);

    VerticalPanel panel = new VerticalPanel();
    panel.setSpacing(8);
    panel.getElement().getStyle().setPadding(12, Unit.PX);

    panel.add(new Label(MESSAGES.aiModeSelectionHeader()));

    final RadioButton advisorRadio = new RadioButton("aiMode",
        MESSAGES.aiAgentModeAdvisor() + " \u2014 "
        + MESSAGES.aiAgentModeAdvisorDescription());
    final RadioButton screenEditorRadio = new RadioButton("aiMode",
        MESSAGES.aiAgentModeScreenEditor() + " \u2014 "
        + MESSAGES.aiAgentModeScreenEditorDescription());
    final RadioButton projectEditorRadio = new RadioButton("aiMode",
        MESSAGES.aiAgentModeProjectEditor() + " \u2014 "
        + MESSAGES.aiAgentModeProjectEditorDescription());
    advisorRadio.setValue(true);

    panel.add(advisorRadio);
    panel.add(screenEditorRadio);
    panel.add(projectEditorRadio);

    Label warning = new Label(MESSAGES.aiModeWarning());
    warning.getElement().getStyle().setColor("#c0392b");
    warning.getElement().getStyle().setFontSize(12, Unit.PX);
    panel.add(warning);

    HorizontalPanel buttons = new HorizontalPanel();
    buttons.setSpacing(8);

    Button selectButton = new Button(MESSAGES.aiModeSelectAndOpen());
    selectButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        String selectedMode;
        if (projectEditorRadio.getValue()) {
          selectedMode = "ProjectEditor";
        } else if (screenEditorRadio.getValue()) {
          selectedMode = "ScreenEditor";
        } else {
          selectedMode = "Advisor";
        }
        // Set the AIAgentMode project setting
        ProjectEditor projectEditor = Ode.getInstance().getEditorManager()
            .getOpenProjectEditor(getCurrentProjectId());
        if (projectEditor != null) {
          projectEditor.changeProjectSettingsProperty(
              SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
              SettingsConstants.YOUNG_ANDROID_SETTINGS_AI_AGENT_MODE,
              selectedMode);
        }
        modeDialog.hide();
        // Now open the chat dialog (mode is no longer Off)
        AIChatDialog.this.show();
      }
    });

    Button cancelButton = new Button(MESSAGES.aiChatCloseButton());
    cancelButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        modeDialog.hide();
      }
    });

    buttons.add(selectButton);
    buttons.add(cancelButton);
    panel.add(buttons);

    modeDialog.setWidget(panel);
    modeDialog.center();
    modeDialog.show();
  }

}
