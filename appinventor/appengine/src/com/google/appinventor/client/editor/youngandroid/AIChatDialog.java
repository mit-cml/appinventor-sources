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
import com.google.appinventor.client.editor.blocks.BlocksEditor;
import com.google.appinventor.client.editor.simple.components.MockForm;
import com.google.appinventor.shared.rpc.aiagent.AIAgentRequest;
import com.google.appinventor.shared.rpc.aiagent.AIAgentResponse;
import com.google.appinventor.shared.rpc.aiagent.AIAgentService;
import com.google.appinventor.shared.rpc.aiagent.AIAgentServiceAsync;
import com.google.appinventor.shared.rpc.aiagent.AIConversationMessage;
import com.google.appinventor.shared.rpc.aiagent.AIOperation;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event;
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

import java.util.ArrayList;
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

  /** Minimum dialog width when resizing. */
  private static final int MIN_DIALOG_WIDTH = 300;

  /** Minimum chat-scroll-panel height when resizing. */
  private static final int MIN_SCROLL_HEIGHT = 100;

  private static final int RESIZE_HANDLE_SIZE = 12;
  private static final int EDGE_TOP = 1;
  private static final int EDGE_RIGHT = 2;
  private static final int EDGE_BOTTOM = 4;
  private static final int EDGE_LEFT = 8;

  // RPC service
  private final AIAgentServiceAsync aiAgentService;

  // UI components
  private final VerticalPanel mainPanel;
  private final ScrollPanel chatScrollPanel;
  private final FlowPanel chatHistory;
  private final FlowPanel operationPreview;
  private final TextArea inputArea;
  private final Button sendButton;
  private final Button applyButton;
  private final Button rejectButton;
  private final Button newConversationButton;
  private final Label statusLabel;

  /** Maximum number of client-side validation retries before showing the error. */
  private static final int MAX_VALIDATION_RETRIES = 5;

  // State
  private AIAgentResponse pendingResponse;
  private Timer pollingTimer;
  private boolean requestInFlight;
  private int validationRetryCount;

  // Remembered position so the dialog reopens where the user left it
  private int lastPopupLeft = -1;
  private int lastPopupTop = -1;

  // Resize state
  private boolean resizing;
  private int resizeStartX;
  private int resizeStartY;
  private int resizeStartPanelWidth;
  private int resizeStartScrollHeight;
  private int resizeStartLeft;
  private int resizeStartTop;
  private int resizeEdge;
  private HandlerRegistration resizePreviewHandler;

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

    mainPanel = new VerticalPanel();
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
    configureMarked();
    setupResizeHandles();
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

    String blocksYail = getCurrentBlocksYail();
    String currentView = getCurrentViewString();
    AIAgentRequest request = new AIAgentRequest(text, projectId, screenName, blocksYail,
        currentView);
    setRequestInFlight(true);
    validationRetryCount = 0;
    startPollingStatus();

    aiAgentService.processRequest(request, new OdeAsyncCallback<AIAgentResponse>(
        MESSAGES.aiChatSendError()) {
      @Override
      public void onSuccess(AIAgentResponse response) {
        handleResponseWithValidation(response);
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
   * Handles an AI response by pre-validating block operations before
   * showing the preview.  If validation fails and retries are available,
   * keeps "Calling AI" visible and sends error feedback to the server
   * for an automatic LLM retry.  The user only sees the operation
   * preview once all block operations pass validation.
   */
  private void handleResponseWithValidation(AIAgentResponse response) {
    List<AIOperation> operations = response.getOperations();

    // Pre-validate WRITE_BLOCK and DELETE_BLOCK operations client-side
    if (operations != null && !operations.isEmpty()) {
      List<String> validationErrors = validateBlockOperations(operations);
      if (!validationErrors.isEmpty()) {
        if (validationRetryCount < MAX_VALIDATION_RETRIES) {
          // Retry: keep "Calling AI" visible, send errors to server
          validationRetryCount++;
          LOG.info("Client validation failed (attempt " + validationRetryCount
              + "/" + MAX_VALIDATION_RETRIES + "), retrying: " + validationErrors);
          reportValidationErrors(validationErrors);
          return;
        }
        // Exhausted retries — strip invalid block ops so they are not
        // presented in the preview or executed.  Non-block operations
        // (component additions, property changes, etc.) are kept.
        LOG.warning("Validation retries exhausted. Stripping "
            + validationErrors.size() + " invalid block operation(s).");
        List<AIOperation> cleaned = new ArrayList<>();
        for (AIOperation op : operations) {
          if (op.getType() != AIOperation.Type.WRITE_BLOCK
              && op.getType() != AIOperation.Type.DELETE_BLOCK) {
            cleaned.add(op);
          }
        }
        response.setOperations(cleaned);
        // Surface the errors so the user sees what went wrong
        for (String err : validationErrors) {
          response.getErrors().add(err);
        }
      }
    }

    // Validation passed (or no block ops, or retries exhausted) — show result
    setRequestInFlight(false);
    stopPollingStatus();
    handleResponse(response);
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

  /**
   * Validates WRITE_BLOCK and DELETE_BLOCK operations using the client-side
   * Blockly runtime (dry-run, no blocks created).
   *
   * @return list of validation error messages; empty if all operations are valid
   */
  private List<String> validateBlockOperations(List<AIOperation> operations) {
    List<String> errors = new ArrayList<>();
    BlocksEditor<?, ?> blocksEditor = getCurrentBlocksEditor();
    if (blocksEditor == null) {
      // Can't validate without a blocks editor — let execution handle it
      return errors;
    }

    for (AIOperation op : operations) {
      if (op.getType() == AIOperation.Type.WRITE_BLOCK) {
        String yail = extractField(op.getPayload(), "yail");
        if (yail != null && !yail.isEmpty()) {
          String resultJson = blocksEditor.validateYail(yail);
          if (resultJson != null) {
            String error = extractValidationError(resultJson);
            if (error != null) {
              // Include the failing YAIL so the LLM can see and fix its mistake
              errors.add("write_block validation failed: " + error
                  + "\nFailing YAIL: " + yail);
            }
          }
        }
      } else if (op.getType() == AIOperation.Type.DELETE_BLOCK) {
        String block = extractField(op.getPayload(), "block");
        if (block != null && !block.isEmpty()) {
          String resultJson = blocksEditor.validateDeleteId(block);
          if (resultJson != null) {
            String error = extractValidationError(resultJson);
            if (error != null) {
              errors.add("delete_block validation failed: " + error);
            }
          }
        }
      }
    }
    return errors;
  }

  /**
   * Extracts the error message from a validation result JSON string.
   * Returns null if valid, the error string otherwise.
   */
  private static String extractValidationError(String resultJson) {
    // Check for "valid":true or "valid":false
    if (resultJson.contains("\"valid\":true")
        || resultJson.contains("\"valid\": true")) {
      return null;
    }
    // Extract the error field value
    String error = extractField(resultJson, "error");
    // extractField returns the field name as fallback — treat that as "unknown error"
    if ("error".equals(error)) {
      return "unknown validation error";
    }
    return error;
  }

  /**
   * Reports client-side validation errors to the server for LLM retry.
   * Keeps "Calling AI" visible and requestInFlight=true during the retry.
   */
  private void reportValidationErrors(List<String> errors) {
    long projectId = Ode.getInstance().getCurrentYoungAndroidProjectId();
    if (projectId == 0) {
      setRequestInFlight(false);
      stopPollingStatus();
      return;
    }
    String screenName = getCurrentScreenName();
    String blocksYail = getCurrentBlocksYail();
    String currentView = getCurrentViewString();

    // Keep polling — "Calling AI" stays visible
    aiAgentService.reportExecutionErrors(projectId, screenName, errors, blocksYail,
        currentView,
        new OdeAsyncCallback<AIAgentResponse>(MESSAGES.aiChatSendError()) {
          @Override
          public void onSuccess(AIAgentResponse response) {
            // Validate the retry response too
            handleResponseWithValidation(response);
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
   * Returns the current screen's blocks editor, or null if unavailable.
   */
  private BlocksEditor<?, ?> getCurrentBlocksEditor() {
    DesignToolbar toolbar = Ode.getInstance().getDesignToolbar();
    if (toolbar != null) {
      DesignToolbar.DesignProject project = toolbar.getCurrentProject();
      if (project != null) {
        DesignToolbar.Screen screen = project.screens.get(project.currentScreen);
        if (screen != null && screen.blocksEditor instanceof BlocksEditor) {
          return (BlocksEditor<?, ?>) screen.blocksEditor;
        }
      }
    }
    return null;
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

    // Use HTML to support line breaks / Markdown in messages
    HTML messageHtml;
    if (isUser) {
      messageHtml = new HTML(escapeAndFormat(text));
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
      case SWITCH_SCREEN:
        return "~ Switch to screen: " + extractField(payload, "screen");
      case CREATE_SCREEN:
        return "+ Create screen: " + extractField(payload, "screen");
      case DELETE_SCREEN:
        return "- Delete screen: " + extractField(payload, "screen");
      case SET_PROJECT_PROP:
        return "~ Set project property: " + extractField(payload, "name");
      case TOGGLE_EDITOR:
        return "~ Switch to " + extractField(payload, "view") + " view";
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
              // Execution failed — report to server for LLM retry
              reportExecutionErrorsToServer(result);
            }
          }
        });
  }

  /**
   * Calls the continueRequest RPC to fetch the next batch of operations
   * from a multi-step AI response. On success, validates block operations
   * before showing the preview.
   */
  private void fetchContinuation() {
    long projectId = Ode.getInstance().getCurrentYoungAndroidProjectId();
    if (projectId == 0) {
      setRequestInFlight(false);
      stopPollingStatus();
      return;
    }

    String screenName = getCurrentScreenName();
    String blocksYail = getCurrentBlocksYail();
    String currentView = getCurrentViewString();
    validationRetryCount = 0;

    aiAgentService.continueRequest(projectId, screenName, blocksYail, currentView,
        new OdeAsyncCallback<AIAgentResponse>(MESSAGES.aiChatSendError()) {
          @Override
          public void onSuccess(AIAgentResponse response) {
            handleResponseWithValidation(response);
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

    setRequestInFlight(true);
    validationRetryCount = 0;
    startPollingStatus();

    aiAgentService.processRequest(feedback, new OdeAsyncCallback<AIAgentResponse>(
        MESSAGES.aiChatSendError()) {
      @Override
      public void onSuccess(AIAgentResponse response) {
        handleResponseWithValidation(response);
      }

      @Override
      public void onFailure(Throwable caught) {
        super.onFailure(caught);
        setRequestInFlight(false);
        stopPollingStatus();
      }
    });
  }

  // ---- Error feedback ----

  /**
   * Reports client-side execution errors to the server for LLM retry.
   * Collects error messages from failed and skipped operations, sends them
   * to the server, and handles the retry response.
   */
  private void reportExecutionErrorsToServer(AIOperationExecutor.ExecutionResult result) {
    long projectId = Ode.getInstance().getCurrentYoungAndroidProjectId();
    if (projectId == 0) {
      setRequestInFlight(false);
      return;
    }
    String screenName = getCurrentScreenName();
    String blocksYail = getCurrentBlocksYail();
    String currentView = getCurrentViewString();

    // Collect error messages
    List<String> errors = new ArrayList<>();
    String mainError = result.getErrorMessage();
    if (mainError != null && !mainError.isEmpty()) {
      errors.add(mainError);
    }
    // Add context about skipped operations
    for (AIOperation skipped : result.getSkipped()) {
      errors.add("Skipped " + skipped.getType() + " due to earlier failure");
    }

    startPollingStatus();
    validationRetryCount = 0;
    aiAgentService.reportExecutionErrors(projectId, screenName, errors, blocksYail,
        currentView,
        new OdeAsyncCallback<AIAgentResponse>(MESSAGES.aiChatSendError()) {
          @Override
          public void onSuccess(AIAgentResponse response) {
            handleResponseWithValidation(response);
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

  // ---- Resize ----

  /**
   * Adds invisible resize handles at each corner of the dialog.
   */
  private void setupResizeHandles() {
    addCornerHandle("nw-resize", EDGE_TOP | EDGE_LEFT,
        "0px", null, null, "0px");
    addCornerHandle("ne-resize", EDGE_TOP | EDGE_RIGHT,
        "0px", "0px", null, null);
    addCornerHandle("sw-resize", EDGE_BOTTOM | EDGE_LEFT,
        null, null, "0px", "0px");
    addCornerHandle("se-resize", EDGE_BOTTOM | EDGE_RIGHT,
        null, "0px", "0px", null);
  }

  /**
   * Creates a single corner resize handle and appends it to the dialog element.
   */
  private void addCornerHandle(String cursor, int edge,
      String top, String right, String bottom, String left) {
    com.google.gwt.dom.client.Element handle = Document.get().createDivElement();
    handle.getStyle().setProperty("position", "absolute");
    handle.getStyle().setProperty("width", RESIZE_HANDLE_SIZE + "px");
    handle.getStyle().setProperty("height", RESIZE_HANDLE_SIZE + "px");
    handle.getStyle().setProperty("cursor", cursor);
    handle.getStyle().setProperty("zIndex", "10");
    if (top != null) {
      handle.getStyle().setProperty("top", top);
    }
    if (right != null) {
      handle.getStyle().setProperty("right", right);
    }
    if (bottom != null) {
      handle.getStyle().setProperty("bottom", bottom);
    }
    if (left != null) {
      handle.getStyle().setProperty("left", left);
    }
    getElement().appendChild(handle);
    attachMouseDown(handle, edge);
  }

  /**
   * Attaches a native mousedown listener to a resize handle element.
   */
  private native void attachMouseDown(
      com.google.gwt.dom.client.Element el, int edge) /*-{
    var self = this;
    el.addEventListener('mousedown', function(e) {
      e.preventDefault();
      e.stopPropagation();
      self.@com.google.appinventor.client.editor.youngandroid.AIChatDialog::startResize(III)(
          e.clientX, e.clientY, edge);
    });
  }-*/;

  /**
   * Begins a resize operation, capturing mouse events globally.
   */
  private void startResize(int clientX, int clientY, int edge) {
    resizing = true;
    resizeStartX = clientX;
    resizeStartY = clientY;
    resizeStartPanelWidth = mainPanel.getOffsetWidth();
    resizeStartScrollHeight = chatScrollPanel.getOffsetHeight();
    resizeStartLeft = getAbsoluteLeft();
    resizeStartTop = getAbsoluteTop();
    resizeEdge = edge;

    if (resizePreviewHandler != null) {
      resizePreviewHandler.removeHandler();
    }
    resizePreviewHandler = Event.addNativePreviewHandler(
        new Event.NativePreviewHandler() {
          @Override
          public void onPreviewNativeEvent(Event.NativePreviewEvent event) {
            int type = event.getTypeInt();
            if (type == Event.ONMOUSEMOVE) {
              event.cancel();
              doResize(event.getNativeEvent().getClientX(),
                  event.getNativeEvent().getClientY());
            } else if (type == Event.ONMOUSEUP) {
              event.cancel();
              stopResize();
            }
          }
        });
  }

  /**
   * Processes a mouse-move during an active resize, updating panel dimensions.
   */
  private void doResize(int clientX, int clientY) {
    int dx = clientX - resizeStartX;
    int dy = clientY - resizeStartY;

    int newPanelWidth = resizeStartPanelWidth;
    int newScrollHeight = resizeStartScrollHeight;
    int newLeft = resizeStartLeft;
    int newTop = resizeStartTop;

    if ((resizeEdge & EDGE_RIGHT) != 0) {
      newPanelWidth = resizeStartPanelWidth + dx;
    }
    if ((resizeEdge & EDGE_LEFT) != 0) {
      newPanelWidth = resizeStartPanelWidth - dx;
      newLeft = resizeStartLeft + dx;
    }
    if ((resizeEdge & EDGE_BOTTOM) != 0) {
      newScrollHeight = resizeStartScrollHeight + dy;
    }
    if ((resizeEdge & EDGE_TOP) != 0) {
      newScrollHeight = resizeStartScrollHeight - dy;
      newTop = resizeStartTop + dy;
    }

    // Enforce minimum dimensions
    if (newPanelWidth < MIN_DIALOG_WIDTH) {
      if ((resizeEdge & EDGE_LEFT) != 0) {
        newLeft = resizeStartLeft + resizeStartPanelWidth - MIN_DIALOG_WIDTH;
      }
      newPanelWidth = MIN_DIALOG_WIDTH;
    }
    if (newScrollHeight < MIN_SCROLL_HEIGHT) {
      if ((resizeEdge & EDGE_TOP) != 0) {
        newTop = resizeStartTop + resizeStartScrollHeight - MIN_SCROLL_HEIGHT;
      }
      newScrollHeight = MIN_SCROLL_HEIGHT;
    }

    setPopupPosition(newLeft, newTop);
    mainPanel.setWidth(newPanelWidth + "px");
    chatScrollPanel.setSize(newPanelWidth + "px", newScrollHeight + "px");
  }

  /**
   * Ends the resize operation and removes the global event handler.
   */
  private void stopResize() {
    resizing = false;
    if (resizePreviewHandler != null) {
      resizePreviewHandler.removeHandler();
      resizePreviewHandler = null;
    }
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
   * Generates YAIL for the current screen's blocks using the client-side
   * Blockly YAIL generators. Returns only block-level YAIL (event handlers,
   * global variables, procedures) without form scaffolding.
   *
   * @return YAIL string, or empty string if the blocks editor is unavailable
   */
  private String getCurrentBlocksYail() {
    DesignToolbar toolbar = Ode.getInstance().getDesignToolbar();
    if (toolbar != null) {
      DesignToolbar.DesignProject project = toolbar.getCurrentProject();
      if (project != null) {
        DesignToolbar.Screen screen = project.screens.get(project.currentScreen);
        if (screen != null && screen.blocksEditor instanceof BlocksEditor) {
          try {
            return ((BlocksEditor<?, ?>) screen.blocksEditor).getBlocksYail();
          } catch (Exception e) {
            LOG.warning("Failed to generate blocks YAIL: " + e.getMessage());
          }
        }
      }
    }
    return "";
  }

  /**
   * Returns the current editor view as a string ("Designer" or "Blocks").
   */
  private String getCurrentViewString() {
    DesignToolbar toolbar = Ode.getInstance().getDesignToolbar();
    if (toolbar != null) {
      return toolbar.getCurrentView() == DesignToolbar.View.BLOCKS ? "Blocks" : "Designer";
    }
    return "Designer";
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
      // String value — find closing quote, skipping escaped characters
      int end = start + 1;
      while (end < json.length()) {
        char c = json.charAt(end);
        if (c == '\\') {
          end += 2; // skip escape sequence (e.g., \", \\, \n)
          continue;
        }
        if (c == '"') {
          break;
        }
        end++;
      }
      if (end >= json.length()) {
        return unescapeJsonString(json.substring(start + 1));
      }
      return unescapeJsonString(json.substring(start + 1, end));
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
   * Unescapes a JSON string value (the content between the outer quotes).
   * Handles standard JSON escape sequences: \", \\, \/, \n, \r, \t.
   */
  private static String unescapeJsonString(String s) {
    if (s.indexOf('\\') < 0) {
      return s; // fast path: no escapes
    }
    StringBuilder sb = new StringBuilder(s.length());
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '\\' && i + 1 < s.length()) {
        char next = s.charAt(i + 1);
        switch (next) {
          case '"':  sb.append('"');  i++; break;
          case '\\': sb.append('\\'); i++; break;
          case '/':  sb.append('/');  i++; break;
          case 'n':  sb.append('\n'); i++; break;
          case 'r':  sb.append('\r'); i++; break;
          case 't':  sb.append('\t'); i++; break;
          default:   sb.append(c);         break;
        }
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
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
        // Set the AIAgentMode on the Screen1 form component property.
        // MockForm.onPropertyChange will propagate this to project settings
        // and the property will be persisted in Screen1.scm for the backend.
        ProjectEditor projectEditor = Ode.getInstance().getEditorManager()
            .getOpenProjectEditor(getCurrentProjectId());
        if (projectEditor instanceof YaProjectEditor) {
          YaProjectEditor yaProjectEditor = (YaProjectEditor) projectEditor;
          MockForm form = (MockForm) yaProjectEditor.getFormFileEditor("Screen1").getRoot();
          if (form != null) {
            form.changeProperty(
                SettingsConstants.YOUNG_ANDROID_SETTINGS_AI_AGENT_MODE,
                selectedMode);
          }
        }
        modeDialog.hide();
        // Force an immediate save so the backend sees the updated mode
        // in Screen1.scm before the first AI request is sent.
        Ode.getInstance().getEditorManager().saveDirtyEditors(new Command() {
          @Override
          public void execute() {
            // Now open the chat dialog (mode is no longer Off)
            AIChatDialog.this.show();
          }
        });
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
