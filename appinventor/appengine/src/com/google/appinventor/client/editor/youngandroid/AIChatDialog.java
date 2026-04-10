// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid;

import static com.google.appinventor.client.Ode.MESSAGES;
import static com.google.appinventor.shared.settings.SettingsConstants.AI_AGENT_MODE_OFF;
import static com.google.appinventor.shared.settings.SettingsConstants.AI_AGENT_MODE_PROJECT_EDITOR;
import static com.google.appinventor.shared.settings.SettingsConstants.AI_AGENT_MODE_SCREEN_EDITOR;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.ProjectEditor;
import com.google.appinventor.client.editor.youngandroid.aiagent.AIChatRenderer;
import com.google.appinventor.client.editor.youngandroid.aiagent.AIContextCollector;
import com.google.appinventor.client.editor.youngandroid.aiagent.AIDialogResizeHandler;
import com.google.appinventor.client.editor.youngandroid.aiagent.AIModeSelectionDialog;
import com.google.appinventor.client.editor.youngandroid.aiagent.AIResponseOrchestrator;
import com.google.appinventor.client.editor.youngandroid.aiagent.dialog.ChatInputHandler;
import com.google.appinventor.client.editor.youngandroid.aiagent.dialog.OperationPreviewPanel;
import com.google.appinventor.client.editor.youngandroid.aiagent.dialog.PlanExecuteToggle;
import com.google.appinventor.client.editor.youngandroid.aiagent.dialog.StatusAnimator;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.appinventor.shared.rpc.aiagent.AIAgentResponse;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * A floating dialog that provides an AI assistant chat interface.
 *
 * <p>This is a thin UI shell that delegates to focused classes in the
 * {@code aiagent} and {@code aiagent.dialog} subpackages for rendering,
 * context collection, RPC orchestration, operation formatting, resize
 * handling, mode selection, and individual UI panels.</p>
 */
public class AIChatDialog extends DialogBox
    implements AIResponseOrchestrator.ChatCallback, AIDialogResizeHandler.ResizeTarget {

  private static final int DIALOG_WIDTH = 420;
  private static final int DIALOG_HEIGHT = 560;

  // Core UI
  private final VerticalPanel mainPanel;
  private final ScrollPanel chatScrollPanel;
  private final FlowPanel chatHistory;
  private final Label editModeWarning;
  private final FlowPanel debugBanner;

  private String conversationId;

  // Delegates
  private final AIChatRenderer renderer;
  private final AIContextCollector contextCollector;
  private final AIResponseOrchestrator orchestrator;
  private final AIDialogResizeHandler resizeHandler;

  // Submodules
  private final StatusAnimator statusAnimator;
  private final OperationPreviewPanel operationPreview;
  private final PlanExecuteToggle planToggle;
  private final ChatInputHandler inputHandler;

  // Position memory
  private int lastPopupLeft = -1;
  private int lastPopupTop = -1;

  private long currentProjectId;
  private boolean hasConversationMessages;

  public AIChatDialog() {
    super(false, false);
    setText(MESSAGES.aiChatDialogTitle());
    setAnimationEnabled(true);
    setGlassEnabled(false);

    // Close button (X) on the title bar
    Element caption = getCaption().asWidget().getElement();
    caption.getStyle().setProperty("position", "relative");
    caption.getStyle().setProperty("paddingRight", "24px");
    com.google.gwt.dom.client.Element closeX = com.google.gwt.dom.client.Document.get()
        .createSpanElement();
    closeX.setInnerHTML("&times;");
    closeX.getStyle().setProperty("position", "absolute");
    closeX.getStyle().setProperty("right", "6px");
    closeX.getStyle().setProperty("top", "50%");
    closeX.getStyle().setProperty("transform", "translateY(-50%)");
    closeX.getStyle().setProperty("cursor", "pointer");
    closeX.getStyle().setProperty("fontSize", "18px");
    closeX.getStyle().setProperty("lineHeight", "1");
    closeX.getStyle().setProperty("color", "#666");
    caption.appendChild(closeX);
    com.google.gwt.user.client.Event.sinkEvents(closeX,
        com.google.gwt.user.client.Event.ONCLICK);
    com.google.gwt.user.client.Event.setEventListener(closeX,
        new com.google.gwt.user.client.EventListener() {
          @Override
          public void onBrowserEvent(com.google.gwt.user.client.Event event) {
            if (com.google.gwt.user.client.Event.ONCLICK == event.getTypeInt()) {
              hideDialog();
            }
          }
        });

    // Create core delegates
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

    // Debug banner
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

    renderer = new AIChatRenderer(chatHistory, chatScrollPanel);

    // Submodules
    statusAnimator = new StatusAnimator();
    mainPanel.add(statusAnimator.getWidget());

    operationPreview = new OperationPreviewPanel(orchestrator);
    mainPanel.add(operationPreview.getPreviewWidget());
    mainPanel.add(operationPreview.getButtonsWidget());

    inputHandler = new ChatInputHandler(orchestrator, renderer, new ChatInputHandler.SendCallback() {
      @Override
      public void onMessageSent() {
        editModeWarning.setVisible(false);
        operationPreview.hidePreview();
        hasConversationMessages = true;
        planToggle.update(hasConversationMessages);
      }
    });
    mainPanel.add(inputHandler.getWidget());

    // Edit-mode warning
    editModeWarning = new Label();
    editModeWarning.getElement().getStyle().setColor("#4a90d9");
    editModeWarning.getElement().getStyle().setFontSize(11, Unit.PX);
    editModeWarning.getElement().getStyle().setMarginTop(2, Unit.PX);
    editModeWarning.getElement().getStyle().setMarginBottom(4, Unit.PX);
    editModeWarning.setVisible(false);
    mainPanel.add(editModeWarning);

    mainPanel.add(operationPreview.getAutoAcceptWidget());

    // Bottom toolbar
    HorizontalPanel bottomBar = new HorizontalPanel();
    bottomBar.setWidth("100%");
    bottomBar.getElement().getStyle().setMarginTop(6, Unit.PX);

    planToggle = new PlanExecuteToggle(contextCollector, new PlanExecuteToggle.TutorialUrlCheck() {
      @Override
      public boolean hasTutorialUrl() {
        return AIChatDialog.this.hasTutorialUrl();
      }
    }, false);
    bottomBar.add(planToggle.getWidget());
    bottomBar.setCellHorizontalAlignment(planToggle.getWidget(), HorizontalPanel.ALIGN_LEFT);

    Button newConversationButton = new Button(MESSAGES.aiChatNewConversationButton());
    newConversationButton.getElement().getStyle().setProperty("cursor", "pointer");
    newConversationButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        confirmAndClearConversation();
      }
    });
    bottomBar.add(newConversationButton);
    bottomBar.setCellHorizontalAlignment(newConversationButton, HorizontalPanel.ALIGN_RIGHT);

    mainPanel.add(bottomBar);

    setWidget(mainPanel);
    renderer.initialize();

    resizeHandler = new AIDialogResizeHandler(this);
    resizeHandler.setupResizeHandles();
  }

  // ---- Lifecycle ----

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
    if (lastPopupLeft >= 0 && lastPopupTop >= 0) {
      setPopupPosition(lastPopupLeft, lastPopupTop);
    } else {
      center();
    }
    currentProjectId = Ode.getInstance().getCurrentYoungAndroidProjectId();
    updateEditModeWarning();
    planToggle.update(hasConversationMessages);
    orchestrator.loadExistingConversation();
  }

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

  public void hideDialog() {
    lastPopupLeft = getPopupLeft();
    lastPopupTop = getPopupTop();
    orchestrator.resetAutoAcceptAll();
    orchestrator.stopPollingStatus();
    planToggle.reset();
    hide();
  }

  // ---- Edit-mode warning ----

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

  private void confirmAndClearConversation() {
    if (hasConversationMessages) {
      boolean confirmed = Window.confirm(MESSAGES.aiChatClearConversationConfirm());
      if (!confirmed) {
        return;
      }
    }
    orchestrator.clearConversation();
  }

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

  // ---- ChatCallback implementation ----

  @Override
  public void addUserMessage(String text) {
    renderer.addUserMessage(text);
    editModeWarning.setVisible(false);
    debugBanner.setVisible(false);
    hasConversationMessages = true;
    planToggle.update(hasConversationMessages);
  }

  @Override
  public void addAiMessage(String text) {
    renderer.addAiMessage(text);
    if (!hasConversationMessages) {
      hasConversationMessages = true;
      planToggle.update(hasConversationMessages);
    }
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
    operationPreview.showPreview(response);
  }

  @Override
  public void hideOperationPreview() {
    operationPreview.hidePreview();
  }

  @Override
  public void setRequestInFlight(boolean inFlight) {
    inputHandler.setRequestInFlight(inFlight);
  }

  @Override
  public void setStatusText(String text) {
    statusAnimator.setText(text);
  }

  @Override
  public void setStatusVisible(boolean visible) {
    statusAnimator.setVisible(visible);
  }

  @Override
  public void setAutoAcceptVisible(boolean visible) {
    operationPreview.setAutoAcceptVisible(visible);
  }

  @Override
  public void clearChatHistory() {
    renderer.clear();
    hasConversationMessages = false;
    updateEditModeWarning();
    planToggle.update(hasConversationMessages);
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
    planToggle.setOrchestrationEnabled(orchestrator.isOrchestrationEnabled());
    planToggle.update(hasConversationMessages);
  }

  @Override
  public void onPlanExecutionStarted() {
    planToggle.setExecuting();
  }

  @Override
  public void onPlanExecutionFinished() {
    planToggle.update(hasConversationMessages);
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
