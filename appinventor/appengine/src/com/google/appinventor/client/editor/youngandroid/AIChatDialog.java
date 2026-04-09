// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid;

import static com.google.appinventor.client.Ode.MESSAGES;
import static com.google.appinventor.shared.settings.SettingsConstants.AI_AGENT_MODE_OFF;
import static com.google.appinventor.shared.settings.SettingsConstants.AI_AGENT_MODE_PROJECT_EDITOR;
import static com.google.appinventor.shared.settings.SettingsConstants.AI_AGENT_MODE_SCREEN_EDITOR;

import com.google.appinventor.client.ErrorReporter;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.ProjectEditor;
import com.google.appinventor.client.editor.youngandroid.aiagent.AIChatRenderer;
import com.google.appinventor.client.editor.youngandroid.aiagent.AIContextCollector;
import com.google.appinventor.client.editor.youngandroid.aiagent.AIDialogResizeHandler;
import com.google.appinventor.client.editor.youngandroid.aiagent.AIEditorState;
import com.google.appinventor.client.editor.youngandroid.aiagent.AIModeSelectionDialog;
import com.google.appinventor.client.editor.youngandroid.aiagent.AIOperationFormatter;
import com.google.appinventor.client.editor.youngandroid.aiagent.AIResponseOrchestrator;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.appinventor.shared.rpc.aiagent.AIAgentResponse;
import com.google.appinventor.shared.rpc.aiagent.AIOperation;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.List;

/**
 * A floating dialog that provides an AI assistant chat interface.
 *
 * <p>This is a thin UI shell that delegates to focused classes in the
 * {@code aiagent} subpackage for rendering, context collection, RPC
 * orchestration, operation formatting, resize handling, and mode selection.</p>
 */
public class AIChatDialog extends DialogBox
    implements AIResponseOrchestrator.ChatCallback, AIDialogResizeHandler.ResizeTarget {

  /** Default dialog width. */
  private static final int DIALOG_WIDTH = 420;

  /** Default dialog height. */
  private static final int DIALOG_HEIGHT = 560;

  // UI components
  private final VerticalPanel mainPanel;
  private final ScrollPanel chatScrollPanel;
  private final FlowPanel chatHistory;
  private final FlowPanel operationPreview;
  private final TextArea inputArea;
  private final Button sendButton;
  private final Button applyButton;
  private final Button applyAndAcceptAllButton;
  private final Button rejectButton;
  private final Button newConversationButton;
  private final Button planExecuteButton;
  private final Label statusLabel;
  private final Label editModeWarning;
  private final FlowPanel autoAcceptPanel;
  private final FlowPanel debugBanner;

  private String conversationId;

  // Delegates
  private final AIChatRenderer renderer;
  private final AIContextCollector contextCollector;
  private final AIResponseOrchestrator orchestrator;
  private final AIDialogResizeHandler resizeHandler;

  // Remembered position so the dialog reopens where the user left it
  private int lastPopupLeft = -1;
  private int lastPopupTop = -1;

  /** The project ID whose conversation is currently displayed. */
  private long currentProjectId;

  /**
   * Constructs the AI chat dialog, building the full UI hierarchy
   * and creating delegate instances.
   */
  public AIChatDialog() {
    super(false /* autohide */, false /* modal */);
    setText(MESSAGES.aiChatDialogTitle());
    setAnimationEnabled(true);
    setGlassEnabled(false);

    // Create delegates
    contextCollector = new AIContextCollector();
    orchestrator = new AIResponseOrchestrator(contextCollector, this);

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

    // Debug-mode warning banner (inserted into chatHistory by showDebugBanner)
    debugBanner = new FlowPanel();
    debugBanner.getElement().getStyle().setProperty("border", "1px solid #ff9800");
    debugBanner.getElement().getStyle().setProperty("borderRadius", "4px");
    debugBanner.getElement().getStyle().setProperty("background", "#fff3e0");
    debugBanner.getElement().getStyle().setPadding(6, Unit.PX);
    debugBanner.getElement().getStyle().setMarginBottom(6, Unit.PX);
    Label debugLabel = new Label(MESSAGES.aiChatDebugWarning());
    debugLabel.getElement().getStyle().setColor("#e65100");
    debugLabel.getElement().getStyle().setFontSize(11, Unit.PX);
    debugBanner.add(debugLabel);

    mainPanel.add(chatScrollPanel);

    // Create renderer after panels are built
    renderer = new AIChatRenderer(chatHistory, chatScrollPanel);

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
        orchestrator.applyOperations();
      }
    });
    applyButton.setVisible(false);
    actionButtons.add(applyButton);

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
    actionButtons.add(applyAndAcceptAllButton);

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
    actionButtons.add(rejectButton);

    mainPanel.add(actionButtons);

    // Input area + send button
    HorizontalPanel inputPanel = new HorizontalPanel();
    inputPanel.setWidth("100%");
    inputPanel.setSpacing(4);

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
    inputPanel.add(inputArea);
    inputPanel.setCellWidth(inputArea, "100%");

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
    inputPanel.add(sendButton);

    mainPanel.add(inputPanel);

    // Edit-mode warning (shown only before the first message is sent)
    editModeWarning = new Label();
    editModeWarning.getElement().getStyle().setColor("#4a90d9");
    editModeWarning.getElement().getStyle().setFontSize(11, Unit.PX);
    editModeWarning.getElement().getStyle().setMarginTop(2, Unit.PX);
    editModeWarning.getElement().getStyle().setMarginBottom(4, Unit.PX);
    editModeWarning.setVisible(false);
    mainPanel.add(editModeWarning);

    // Auto-accept notice (shown while auto-accept-all is active)
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

    mainPanel.add(autoAcceptPanel);

    // Plan & Execute toggle (only shown in ProjectEditor mode)
    planExecuteCheckBox = new CheckBox(MESSAGES.aiChatPlanExecuteLabel());
    planExecuteCheckBox.getElement().getStyle().setFontSize(12, Unit.PX);
    planExecuteCheckBox.getElement().getStyle().setProperty("cursor", "pointer");
    planExecuteCheckBox.setVisible(false);
    planExecuteCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        boolean checked = event.getValue();
        if (checked && hasTutorialUrl()) {
          boolean confirmed = Window.confirm(MESSAGES.aiChatPlanExecuteTutorialConfirm());
          if (!confirmed) {
            planExecuteCheckBox.setValue(false, false);
            AIEditorState.setPlanExecuteMode(false);
            return;
          }
        }
        AIEditorState.setPlanExecuteMode(checked);
      }
    });
    mainPanel.add(planExecuteCheckBox);

    // Bottom toolbar: new conversation + close
    HorizontalPanel bottomBar = new HorizontalPanel();
    bottomBar.setSpacing(4);
    bottomBar.getElement().getStyle().setMarginTop(6, Unit.PX);

    newConversationButton = new Button(MESSAGES.aiChatNewConversationButton());
    newConversationButton.getElement().getStyle().setProperty("cursor", "pointer");
    newConversationButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        orchestrator.clearConversation();
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
    renderer.initialize();

    // Set up resize handles
    resizeHandler = new AIDialogResizeHandler(this);
    resizeHandler.setupResizeHandles();
  }

  // ---- Lifecycle ----

  /**
   * Overrides show to check AI mode and load existing conversation history.
   * If the mode is Off, shows a mode selection dialog instead.
   */
  @Override
  public void show() {
    String mode = contextCollector.getCurrentAIAgentMode();
    if (AI_AGENT_MODE_OFF.equals(mode)) {
      new AIModeSelectionDialog(contextCollector, new Runnable() {
        @Override
        public void run() {
          AIChatDialog.this.show();
        }
      }).show();
      return;
    }
    super.show();
    // Restore previous position, or center on first open
    if (lastPopupLeft >= 0 && lastPopupTop >= 0) {
      setPopupPosition(lastPopupLeft, lastPopupTop);
    } else {
      center();
    }
    currentProjectId = Ode.getInstance().getCurrentYoungAndroidProjectId();
    updateEditModeWarning();
    updatePlanExecuteToggle();
    orchestrator.loadExistingConversation();
  }

  /**
   * Notifies the dialog that the active project has changed.
   * If the dialog is visible and the project differs from the one currently
   * displayed, the conversation is reloaded for the new project.
   * If there is no open project, the dialog is closed.
   */
  public void onProjectChanged() {
    long newProjectId = Ode.getInstance().getCurrentYoungAndroidProjectId();
    if (newProjectId == 0) {
      if (isShowing()) {
        hideDialog();
      }
      return;
    }
    if (isShowing() && newProjectId != currentProjectId) {
      currentProjectId = newProjectId;
      orchestrator.resetAutoAcceptAll();
      if (orchestrator.isRequestInFlight()) {
        orchestrator.cancelInFlight();
      }
      orchestrator.loadExistingConversation();
    }
  }

  /**
   * Hides the dialog and remembers its position.
   * Resets the Plan & Execute toggle so it starts unchecked on next open.
   */
  public void hideDialog() {
    lastPopupLeft = getPopupLeft();
    lastPopupTop = getPopupTop();
    orchestrator.resetAutoAcceptAll();
    orchestrator.stopPollingStatus();
    planExecuteCheckBox.setValue(false, false);
    AIEditorState.setPlanExecuteMode(false);
    hide();
  }

  // ---- Plan & Execute toggle ----

  /**
   * Shows the Plan & Execute toggle when the current AI mode is ProjectEditor
   * AND the {@code ai.agent.orchestration} server flag is enabled.
   * Hides and resets the toggle otherwise.
   */
  private void updatePlanExecuteToggle() {
    String mode = contextCollector.getCurrentAIAgentMode();
    if (AI_AGENT_MODE_PROJECT_EDITOR.equals(mode) && orchestrator.isOrchestrationEnabled()) {
      planExecuteCheckBox.setVisible(true);
    } else {
      planExecuteCheckBox.setValue(false, false);
      AIEditorState.setPlanExecuteMode(false);
      planExecuteCheckBox.setVisible(false);
    }
  }

  /**
   * Returns true if the current project has a non-empty TutorialURL setting.
   */
  private boolean hasTutorialUrl() {
    long projectId = contextCollector.getCurrentProjectId();
    if (projectId == 0) {
      return false;
    }
    ProjectEditor projectEditor = Ode.getInstance().getEditorManager()
        .getOpenProjectEditor(projectId);
    if (projectEditor == null) {
      return false;
    }
    String tutorialUrl = projectEditor.getProjectSettingsProperty(
        SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
        SettingsConstants.YOUNG_ANDROID_SETTINGS_TUTORIAL_URL);
    return tutorialUrl != null && !tutorialUrl.isEmpty();
  }

  // ---- Edit-mode warning ----

  /**
   * Shows the edit-mode warning when the current AI mode can modify the
   * project. Called when the dialog opens or the conversation is cleared;
   * hidden once the first message is sent or history is loaded.
   */
  private void updateEditModeWarning() {
    String mode = contextCollector.getCurrentAIAgentMode();
    if (AI_AGENT_MODE_SCREEN_EDITOR.equals(mode)) {
      editModeWarning.setText(MESSAGES.aiChatScreenEditorWarning());
      editModeWarning.setVisible(true);
    } else if (AI_AGENT_MODE_PROJECT_EDITOR.equals(mode)) {
      editModeWarning.setText(MESSAGES.aiChatProjectEditorWarning());
      editModeWarning.setVisible(true);
    } else {
      editModeWarning.setVisible(false);
    }
  }

  // ---- Send message ----

  /**
   * Validates input and delegates to the orchestrator.
   */
  private void doSendMessage() {
    // If a request is in flight, clicking the button means "Stop".
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
    editModeWarning.setVisible(false);
    hideOperationPreview();
    orchestrator.sendMessage(text);
  }

  // ---- ChatCallback implementation ----

  @Override
  public void addUserMessage(String text) {
    renderer.addUserMessage(text);
    editModeWarning.setVisible(false);
    debugBanner.setVisible(false);
  }

  @Override
  public void addAiMessage(String text) {
    renderer.addAiMessage(text);
  }

  @Override
  public void startStreamingBubble() {
    renderer.startStreamingBubble();
  }

  @Override
  public void appendStreamingText(String delta) {
    renderer.appendStreamingText(delta);
  }

  @Override
  public void appendStreamingThinking(String delta) {
    renderer.appendStreamingThinking(delta);
  }

  @Override
  public void finalizeStreamingBubble(String finalText) {
    renderer.finalizeStreamingBubble(finalText);
  }

  @Override
  public void showOperationPreview(AIAgentResponse response) {
    operationPreview.clear();

    Label header = new Label(MESSAGES.aiChatProposedChanges());
    header.getElement().getStyle().setProperty("fontWeight", "bold");
    header.getElement().getStyle().setMarginBottom(4, Unit.PX);
    operationPreview.add(header);

    List<AIOperation> operations = response.getOperations();
    for (AIOperation op : operations) {
      Label opLabel = new Label(AIOperationFormatter.formatOperation(op));
      opLabel.getElement().getStyle().setFontSize(12, Unit.PX);
      opLabel.getElement().getStyle().setMarginBottom(3, Unit.PX);
      opLabel.getElement().getStyle().setPaddingLeft(8, Unit.PX);
      opLabel.getElement().getStyle().setProperty("fontFamily",
          "'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace");
      // Color-code by operation category
      AIOperation.Type type = op.getType();
      if (type == AIOperation.Type.ADD_COMPONENT
          || type == AIOperation.Type.CREATE_SCREEN
          || type == AIOperation.Type.WRITE_BLOCK) {
        opLabel.getElement().getStyle().setColor("#2e7d32"); // green for additions
      } else if (type == AIOperation.Type.DELETE_COMPONENT
          || type == AIOperation.Type.DELETE_SCREEN
          || type == AIOperation.Type.DELETE_BLOCK) {
        opLabel.getElement().getStyle().setColor("#c62828"); // red for deletions
      } else {
        opLabel.getElement().getStyle().setColor("#1565c0"); // blue for modifications
      }
      operationPreview.add(opLabel);
    }

    operationPreview.setVisible(true);
    applyButton.setVisible(true);
    applyAndAcceptAllButton.setVisible(true);
    rejectButton.setVisible(true);
  }

  @Override
  public void hideOperationPreview() {
    operationPreview.setVisible(false);
    applyButton.setVisible(false);
    applyAndAcceptAllButton.setVisible(false);
    rejectButton.setVisible(false);
  }

  @Override
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

  @Override
  public void setStatusText(String text) {
    statusLabel.setText(text);
  }

  @Override
  public void setStatusVisible(boolean visible) {
    statusLabel.setVisible(visible);
  }

  @Override
  public void setAutoAcceptVisible(boolean visible) {
    autoAcceptPanel.setVisible(visible);
  }

  @Override
  public void clearChatHistory() {
    renderer.clear();
    updateEditModeWarning();
    updatePlanExecuteToggle();
  }

  @Override
  public void showDebugBanner() {
    debugBanner.setVisible(true);
    if (debugBanner.getParent() != chatHistory) {
      chatHistory.insert(debugBanner, 0);
    }
  }

  @Override
  public void setFeedbackContext(boolean debugEnabled, String conversationId) {
    this.conversationId = conversationId;
    renderer.setFeedbackContext(debugEnabled, conversationId);
  }

  @Override
  public void renderPlanCard(String planJson,
      AIResponseOrchestrator.PlanApprovalCallback approvalCallback) {
    renderer.renderPlanCard(planJson, approvalCallback);
  }

  @Override
  public void onConfigLoaded() {
    updatePlanExecuteToggle();
  }


  // ---- ResizeTarget implementation ----

  @Override
  public Element getDialogElement() {
    return getElement();
  }

  @Override
  public VerticalPanel getMainPanel() {
    return mainPanel;
  }

  @Override
  public ScrollPanel getChatScrollPanel() {
    return chatScrollPanel;
  }
}
