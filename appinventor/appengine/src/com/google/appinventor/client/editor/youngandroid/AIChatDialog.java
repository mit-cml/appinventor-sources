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
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.editor.youngandroid.aiagent.dialog.ChatInputHandler;
import com.google.appinventor.client.editor.youngandroid.aiagent.dialog.ConversationListPanel;
import com.google.appinventor.client.editor.youngandroid.aiagent.dialog.OperationPreviewPanel;
import com.google.appinventor.client.editor.youngandroid.aiagent.dialog.PlanExecuteToggle;
import com.google.appinventor.client.editor.youngandroid.aiagent.dialog.StatusAnimator;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.appinventor.shared.rpc.aiagent.AIAgentResponse;
import com.google.appinventor.shared.rpc.aiagent.AIConversationSummary;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import java.util.List;

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
  /**
   * Default outer-container height in px. Sized to accommodate the debug
   * banner when enabled — the banner's warning message wraps to 3-4 lines
   * at {@link #DIALOG_WIDTH} wide, which costs ~80px on top of the rest
   * of the always-visible rows. Without this headroom the bottom toolbar
   * gets clipped on dialogs that open in debug mode.
   */
  private static final int DIALOG_HEIGHT = 640;

  /**
   * Minimum pixel height reserved for the chat scroll area. The resize
   * handler's minimum dialog height is computed as this plus the height of
   * every always-visible fixed row, so the chat can always show at least
   * this much content.
   */
  static final int CHAT_MIN_HEIGHT = 120;

  // Core UI
  /** Outer container that owns the dialog's pixel dimensions. Both the
   *  chat view ({@link #mainPanel}) and the conversation list view
   *  ({@link #conversationListPanel}) are absolutely positioned to fill
   *  this container, so toggling visibility never changes dialog size. */
  private final FlowPanel outerContainer;
  private final FlowPanel mainPanel;
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

  private ConversationListPanel conversationListPanel;

  /** Top-bar rename button; toggles between "Rename" and "Save" labels. */
  private Button renameButton;
  /** True while the top-bar inline rename TextBox is active. */
  private boolean isRenamingInline;
  /** Captured during inline rename so the Rename/Save button can commit it. */
  private TextBox renameInput;
  private String renameConvId;
  private String renameOriginal;
  private boolean[] renameCommitted;

  /** Top-bar title label for the currently active conversation. Clicking it
   *  swaps the label for an inline rename TextBox. */
  private Label topBarTitleLabel;
  /** Container that holds either {@link #topBarTitleLabel} or an inline
   *  rename TextBox. */
  private FlowPanel topBarTitleContainer;
  /** Active conversation summary, used to render the top-bar title and to
   *  build rename RPC calls. Null while no conversation is selected yet. */
  private AIConversationSummary currentSummary;

  // Deferred explain message (queued when dialog needs mode selection first)
  private String pendingExplainDisplay;
  private String pendingExplainHint;

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

    // Outer container owns the dialog dimensions. Both views fill it via
    // position:absolute inset:0, so swapping views never changes size and
    // the resize handler only needs to size this one widget.
    outerContainer = new FlowPanel();
    outerContainer.setSize(DIALOG_WIDTH + "px", DIALOG_HEIGHT + "px");
    outerContainer.getElement().getStyle().setProperty("position", "relative");
    outerContainer.getElement().getStyle().setProperty("overflow", "hidden");
    outerContainer.getElement().getStyle().setProperty("boxSizing", "border-box");

    // Main chat view: a column flexbox absolutely-positioned to fill the
    // outer container. FlowPanel renders as <div> (unlike VerticalPanel's
    // <table>), so flex:1 on chatScrollPanel works as expected.
    mainPanel = new FlowPanel();
    mainPanel.getElement().getStyle().setPadding(8, Unit.PX);
    mainPanel.getElement().getStyle().setProperty("position", "absolute");
    mainPanel.getElement().getStyle().setProperty("top", "0");
    mainPanel.getElement().getStyle().setProperty("left", "0");
    mainPanel.getElement().getStyle().setProperty("right", "0");
    mainPanel.getElement().getStyle().setProperty("bottom", "0");
    mainPanel.getElement().getStyle().setProperty("display", "flex");
    mainPanel.getElement().getStyle().setProperty("flexDirection", "column");
    mainPanel.getElement().getStyle().setProperty("boxSizing", "border-box");

    // Top bar: conversation title (non-clickable) + "Rename" button.
    // Navigation back to the list lives on the bottom toolbar.
    HorizontalPanel topBar = new HorizontalPanel();
    topBar.setWidth("100%");
    topBar.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
    topBar.getElement().getStyle().setMarginBottom(4, Unit.PX);
    topBar.getElement().getStyle().setProperty("flex", "0 0 auto");

    topBarTitleContainer = new FlowPanel();
    topBarTitleContainer.getElement().getStyle().setProperty("flex", "1");
    topBarTitleContainer.getElement().getStyle().setProperty("overflow", "hidden");
    topBarTitleContainer.getElement().getStyle().setProperty("textOverflow", "ellipsis");
    topBarTitleContainer.getElement().getStyle().setProperty("whiteSpace", "nowrap");
    topBarTitleLabel = new Label(MESSAGES.aiChatUnnamedConversation());
    styleTopBarTitleLabel(topBarTitleLabel);
    topBarTitleContainer.add(topBarTitleLabel);
    topBar.add(topBarTitleContainer);
    topBar.setCellWidth(topBarTitleContainer, "100%");
    topBar.setCellHorizontalAlignment(topBarTitleContainer, HorizontalPanel.ALIGN_LEFT);

    renameButton = new Button(MESSAGES.aiChatRenameConversationButton());
    renameButton.setTitle(MESSAGES.aiChatRenameConversationButton());
    renameButton.getElement().getStyle().setProperty("background", "#f5f5f5");
    renameButton.getElement().getStyle().setColor("#333");
    renameButton.getElement().getStyle().setProperty("border", "1px solid #ccc");
    renameButton.getElement().getStyle().setProperty("borderRadius", "3px");
    renameButton.getElement().getStyle().setProperty("padding", "4px 10px");
    renameButton.getElement().getStyle().setProperty("cursor", "pointer");
    renameButton.getElement().getStyle().setFontSize(12, Unit.PX);
    renameButton.getElement().getStyle().setProperty("marginLeft", "8px");
    renameButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (isRenamingInline) {
          commitActiveInlineRename();
        } else {
          startInlineRename();
        }
      }
    });
    topBar.add(renameButton);
    topBar.setCellHorizontalAlignment(renameButton, HorizontalPanel.ALIGN_RIGHT);

    mainPanel.add(topBar);

    // Chat history area
    chatHistory = new FlowPanel();
    chatHistory.getElement().getStyle().setProperty("minHeight", "100px");

    chatScrollPanel = new ScrollPanel(chatHistory);
    // flex:1 + minHeight:0 makes the scroll area absorb whatever vertical
    // space remains after the fixed-height siblings (top bar, status,
    // input, bottom bar). minHeight:0 is required so flex shrinking can
    // override the scroll panel's intrinsic content height.
    // flex:1 lets the chat scroll absorb whatever space remains after the
    // fixed-height siblings (top bar, status, input, bottom bar).
    // minHeight:CHAT_MIN_HEIGHT is the floor so flex shrinking can't collapse
    // the chat area to nothing; once that floor is reached the resize handler
    // also stops shrinking because MIN_DIALOG_HEIGHT is computed to include it.
    chatScrollPanel.getElement().getStyle().setProperty("flex", "1 1 auto");
    chatScrollPanel.getElement().getStyle().setProperty("minHeight", CHAT_MIN_HEIGHT + "px");
    chatScrollPanel.getElement().getStyle().setProperty("width", "100%");
    chatScrollPanel.getElement().getStyle().setProperty("boxSizing", "border-box");
    chatScrollPanel.getElement().getStyle().setProperty("border", "1px solid #ccc");
    chatScrollPanel.getElement().getStyle().setProperty("borderRadius", "4px");
    chatScrollPanel.getElement().getStyle().setProperty("background", "#fafafa");
    chatScrollPanel.getElement().getStyle().setMarginBottom(6, Unit.PX);

    // Debug banner: sits above the chat scroll panel so it is independent of
    // the message stream. Visibility is driven solely by the server's debug
    // flag; it is not cleared by "New Conversation" or individual messages.
    debugBanner = new FlowPanel();
    debugBanner.getElement().getStyle().setProperty("border", "1px solid #ff9800");
    debugBanner.getElement().getStyle().setProperty("borderRadius", "4px");
    debugBanner.getElement().getStyle().setProperty("background", "#fff3e0");
    debugBanner.getElement().getStyle().setPadding(6, Unit.PX);
    debugBanner.getElement().getStyle().setMarginBottom(6, Unit.PX);
    debugBanner.getElement().getStyle().setProperty("flex", "0 0 auto");
    Label debugLabel = new Label(MESSAGES.aiChatDebugWarning());
    debugLabel.getElement().getStyle().setColor("#e65100");
    debugLabel.getElement().getStyle().setFontSize(11, Unit.PX);
    debugBanner.add(debugLabel);
    debugBanner.setVisible(false);
    mainPanel.add(debugBanner);

    mainPanel.add(chatScrollPanel);

    renderer = new AIChatRenderer(chatHistory, chatScrollPanel);

    // Submodules
    statusAnimator = new StatusAnimator();
    statusAnimator.getWidget().getElement().getStyle().setProperty("flex", "0 0 auto");
    mainPanel.add(statusAnimator.getWidget());

    operationPreview = new OperationPreviewPanel(orchestrator);
    operationPreview.getPreviewWidget().getElement().getStyle().setProperty("flex", "0 0 auto");
    operationPreview.getButtonsWidget().getElement().getStyle().setProperty("flex", "0 0 auto");
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
    inputHandler.getWidget().getElement().getStyle().setProperty("flex", "0 0 auto");
    mainPanel.add(inputHandler.getWidget());

    // Edit-mode warning
    editModeWarning = new Label();
    editModeWarning.getElement().getStyle().setColor("#4a90d9");
    editModeWarning.getElement().getStyle().setFontSize(11, Unit.PX);
    editModeWarning.getElement().getStyle().setMarginTop(2, Unit.PX);
    editModeWarning.getElement().getStyle().setMarginBottom(4, Unit.PX);
    editModeWarning.getElement().getStyle().setProperty("flex", "0 0 auto");
    editModeWarning.setVisible(false);
    mainPanel.add(editModeWarning);

    operationPreview.getAutoAcceptWidget().getElement().getStyle()
        .setProperty("flex", "0 0 auto");
    mainPanel.add(operationPreview.getAutoAcceptWidget());

    // Bottom toolbar
    HorizontalPanel bottomBar = new HorizontalPanel();
    bottomBar.setWidth("100%");
    bottomBar.getElement().getStyle().setMarginTop(6, Unit.PX);
    bottomBar.getElement().getStyle().setProperty("flex", "0 0 auto");

    planToggle = new PlanExecuteToggle(contextCollector, new PlanExecuteToggle.TutorialUrlCheck() {
      @Override
      public boolean hasTutorialUrl() {
        return AIChatDialog.this.hasTutorialUrl();
      }
    }, false);
    bottomBar.add(planToggle.getWidget());
    bottomBar.setCellHorizontalAlignment(planToggle.getWidget(), HorizontalPanel.ALIGN_LEFT);

    Button viewConversationsButton = new Button(MESSAGES.aiChatViewConversationsButton());
    viewConversationsButton.setTitle(MESSAGES.aiChatViewConversationsButton());
    viewConversationsButton.getElement().getStyle().setProperty("cursor", "pointer");
    viewConversationsButton.getElement().getStyle().setFontSize(12, Unit.PX);
    viewConversationsButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        showListView();
      }
    });
    bottomBar.add(viewConversationsButton);
    bottomBar.setCellHorizontalAlignment(viewConversationsButton, HorizontalPanel.ALIGN_RIGHT);

    mainPanel.add(bottomBar);

    // Build the conversation list panel. Both this and mainPanel are
    // absolutely positioned to fill outerContainer, so swapping is just a
    // visibility toggle and dialog dimensions never shift.
    conversationListPanel = new ConversationListPanel(new ConversationListPanel.Listener() {
      @Override
      public void onSelect(final AIConversationSummary summary) {
        currentSummary = summary;
        updateTopBarTitle();
        orchestrator.loadConversation(summary.getConversationId(), new Runnable() {
          @Override
          public void run() {
            hasConversationMessages = true;
            planToggle.update(hasConversationMessages);
            showChatView();
          }
        });
      }

      @Override
      public void onDelete(final String convId) {
        orchestrator.deleteConversation(convId,
            new OdeAsyncCallback<Void>(MESSAGES.aiChatDeleteConversationError()) {
              @Override
              public void onSuccess(Void v) {
                if (convId != null && convId.equals(
                    orchestrator.getCurrentConversationId())) {
                  orchestrator.newConversation();
                  currentSummary = null;
                  updateTopBarTitle();
                }
                showListView();
              }
            });
      }

      @Override
      public void onNew() {
        orchestrator.newConversation();
        currentSummary = null;
        updateTopBarTitle();
        hasConversationMessages = false;
        updateEditModeWarning();
        planToggle.update(hasConversationMessages);
        showChatView();
      }
    });
    conversationListPanel.getElement().getStyle().setProperty("position", "absolute");
    conversationListPanel.getElement().getStyle().setProperty("top", "0");
    conversationListPanel.getElement().getStyle().setProperty("left", "0");
    conversationListPanel.getElement().getStyle().setProperty("right", "0");
    conversationListPanel.getElement().getStyle().setProperty("bottom", "0");

    outerContainer.add(mainPanel);
    outerContainer.add(conversationListPanel);
    showChatView();

    setWidget(outerContainer);
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
    // Fetch server config (orchestration flag, debug flag) eagerly so the
    // Plan & Execute toggle visibility is correct before the first message.
    orchestrator.loadConfig();
    planToggle.update(hasConversationMessages);
    // Expand the chat scroll to fill the outer container on initial render.
    // flex-basis:auto on the scroll panel makes it start at content height
    // (short bubble = short chat area); imperatively sizing it here
    // guarantees it stretches to the dialog's full available height
    // without the user having to drag the resize handle first.
    deferredResizeChat();
    // If an explain message is pending, send it directly without loading
    // history first (avoids async race where history messages would appear
    // after the explain message). The explain exchange will be stored in
    // history and visible on next open.
    if (pendingExplainDisplay != null) {
      String display = pendingExplainDisplay;
      String hint = pendingExplainHint;
      pendingExplainDisplay = null;
      pendingExplainHint = null;
      sendExplainMessage(display, hint);
    } else {
      openMostRecentConversation();
    }
  }

  /**
   * Loads the most recently updated conversation for the current project
   * into the chat view, or starts a new blank conversation if none exist.
   * Always leaves the dialog in the chat view.
   */
  private void openMostRecentConversation() {
    orchestrator.listConversations(new OdeAsyncCallback<List<AIConversationSummary>>(
        MESSAGES.aiChatLoadConversationsError()) {
      @Override
      public void onSuccess(List<AIConversationSummary> rows) {
        if (rows != null && !rows.isEmpty()) {
          AIConversationSummary first = rows.get(0);
          currentSummary = first;
          updateTopBarTitle();
          orchestrator.loadConversation(first.getConversationId(), new Runnable() {
            @Override
            public void run() {
              hasConversationMessages = true;
              planToggle.update(hasConversationMessages);
              showChatView();
            }
          });
        } else {
          orchestrator.newConversation();
          currentSummary = null;
          updateTopBarTitle();
          hasConversationMessages = false;
          planToggle.update(hasConversationMessages);
          showChatView();
        }
      }
    });
  }

  /**
   * Loads the conversation list from the server and swaps the dialog into
   * the list view.
   *
   * <p>Both views are absolutely positioned to fill {@link #outerContainer},
   * so toggling visibility cannot change the dialog's pixel size — even
   * after the user has manually resized.</p>
   */
  private void showListView() {
    orchestrator.listConversations(new OdeAsyncCallback<List<AIConversationSummary>>(
        MESSAGES.aiChatLoadConversationsError()) {
      @Override
      public void onSuccess(List<AIConversationSummary> rows) {
        conversationListPanel.setActive(orchestrator.getCurrentConversationId());
        conversationListPanel.render(rows);
        mainPanel.setVisible(false);
        conversationListPanel.setVisible(true);
      }
    });
  }

  /**
   * Shows the chat view and hides the conversation list. Defers a chat
   * resize so the scroll area expands to fill the available space now
   * that the fixed rows are measurable again (they were all
   * {@code display:none} while the list view was showing).
   */
  private void showChatView() {
    mainPanel.setVisible(true);
    conversationListPanel.setVisible(false);
    deferredResizeChat();
  }

  public void onProjectChanged() {
    long newProjectId = Ode.getInstance().getCurrentYoungAndroidProjectId();
    if (newProjectId == 0) {
      if (isShowing()) {
        hideDialog();
      }
      return;
    }
    if (newProjectId == currentProjectId) {
      return;
    }
    currentProjectId = newProjectId;
    // Reset per-conversation state regardless of visibility so an Ask AI /
    // Explain Block handoff fired later against the new project doesn't
    // replay stale auto-accept, retry counters, preserved ops, etc. from
    // the previous project.
    orchestrator.resetConversationState();
    // A pending explain may have been queued from the old project while
    // the dialog was closed — discard it so we don't open the chat on
    // the new project with a message that was meant for the old one.
    pendingExplainDisplay = null;
    pendingExplainHint = null;
    if (isShowing()) {
      // Refresh history immediately if the user can see the dialog.
      openMostRecentConversation();
    } else {
      // Wipe the renderer so the next show() doesn't briefly flash
      // the previous project's chat before history loads.
      renderer.clear();
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

  /**
   * Sends an explain-block message to the AI. If the dialog is not yet
   * showing (e.g., mode selection is pending), the message is queued and
   * sent automatically once the dialog opens.
   *
   * @param displayText the text shown in the user's chat bubble
   * @param contextHint hidden context (YAIL, warnings) sent to the LLM
   */
  public void sendExplainMessage(String displayText, String contextHint) {
    if (!isShowing()) {
      pendingExplainDisplay = displayText;
      pendingExplainHint = contextHint;
      show();
      return;
    }
    if (orchestrator.isRequestInFlight()) {
      return;
    }
    renderer.addUserMessage(displayText, System.currentTimeMillis());
    editModeWarning.setVisible(false);
    hideOperationPreview();
    orchestrator.sendMessageWithContext(displayText, contextHint);
  }

  // ---- Edit-mode warning ----

  /** Schedules {@link #sizeChatToContainer()} after the current layout settles. */
  private void deferredResizeChat() {
    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
      @Override
      public void execute() {
        sizeChatToContainer();
      }
    });
  }

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
    deferredResizeChat();
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
  public void addUserMessage(String text, long timestamp) {
    renderer.addUserMessage(text, timestamp);
    editModeWarning.setVisible(false);
    hasConversationMessages = true;
    planToggle.update(hasConversationMessages);
  }

  @Override
  public void addAiMessage(String text, long timestamp) {
    renderer.addAiMessage(text, timestamp);
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
    deferredResizeChat();
  }

  @Override
  public void hideOperationPreview() {
    operationPreview.hidePreview();
    deferredResizeChat();
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
    // Overhead row toggling visibility — re-run the chat sizer so the scroll
    // panel shrinks/grows to match the new overhead. Without this, the old
    // explicit height set by sizeChatToContainer() stays put and the bottom
    // toolbar spills past outerContainer's overflow:hidden clip.
    deferredResizeChat();
  }

  @Override
  public void setAutoAcceptVisible(boolean visible) {
    operationPreview.setAutoAcceptVisible(visible);
    // Same overhead-toggle reasoning as setStatusVisible: the auto-accept
    // notice adds a row above the bottom toolbar, so the chat must resize
    // or the toolbar gets clipped.
    deferredResizeChat();
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
    deferredResizeChat();
  }

  @Override
  public void setFeedbackContext(boolean debugEnabled, String conversationId) {
    this.conversationId = conversationId;
    renderer.setFeedbackContext(debugEnabled, conversationId);
    // Best-effort: if the server just minted a conversation id for a fresh
    // chat (no summary in hand), create a placeholder summary so the top
    // bar shows "Unnamed Conversation" and inline rename can target the
    // correct id. The server-truth refresh catches up on next list open.
    if (conversationId != null && !conversationId.isEmpty()
        && (currentSummary == null
            || !conversationId.equals(currentSummary.getConversationId()))) {
      long now = System.currentTimeMillis();
      currentSummary = new AIConversationSummary(conversationId, null, now, now);
      updateTopBarTitle();
    }
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
    renderer.setPlanEditEnabled(orchestrator.isPlanEditEnabled());
  }

  @Override
  public void onPlanExecutionStarted() {
    planToggle.setExecuting();
  }

  @Override
  public void onPlanExecutionFinished() {
    planToggle.update(hasConversationMessages);
  }

  // ---- Top-bar title + inline rename ----

  private void styleTopBarTitleLabel(Label label) {
    label.getElement().getStyle().setFontSize(14, Unit.PX);
    label.getElement().getStyle().setProperty("fontWeight", "500");
    label.getElement().getStyle().setColor("#222");
    label.getElement().getStyle().setProperty("overflow", "hidden");
    label.getElement().getStyle().setProperty("textOverflow", "ellipsis");
    label.getElement().getStyle().setProperty("whiteSpace", "nowrap");
  }

  /** Refreshes {@link #topBarTitleLabel} from {@link #currentSummary}. */
  private void updateTopBarTitle() {
    if (topBarTitleContainer == null) {
      return;
    }
    // If an inline rename is in progress, leave it alone.
    if (topBarTitleContainer.getWidgetCount() > 0
        && topBarTitleContainer.getWidget(0) instanceof TextBox) {
      return;
    }
    // Rename mode ends whenever we render a plain title back into the slot.
    isRenamingInline = false;
    renameInput = null;
    renameConvId = null;
    renameOriginal = null;
    renameCommitted = null;
    setRenameButtonLabel(false);
    String title;
    if (currentSummary != null && currentSummary.getTitle() != null
        && !currentSummary.getTitle().isEmpty()) {
      title = currentSummary.getTitle();
    } else {
      title = MESSAGES.aiChatUnnamedConversation();
    }
    topBarTitleContainer.clear();
    topBarTitleLabel = new Label(title);
    styleTopBarTitleLabel(topBarTitleLabel);
    topBarTitleContainer.add(topBarTitleLabel);
  }

  /**
   * Swaps the top-bar title label for an inline rename TextBox.
   * Enter / blur-with-change / clicking the Save button commits via
   * renameConversation; Esc / blur-without-change restores the label.
   * Also flips the Rename top-bar button to "Save" for the duration.
   */
  private void startInlineRename() {
    if (currentSummary == null || currentSummary.getConversationId() == null) {
      return;
    }
    final String convId = currentSummary.getConversationId();
    final String original = currentSummary.getTitle() != null
        ? currentSummary.getTitle() : "";

    final TextBox input = new TextBox();
    input.setValue(original);
    input.getElement().setAttribute("placeholder",
        MESSAGES.aiChatConversationNamePlaceholder());
    input.getElement().getStyle().setProperty("width", "100%");
    input.getElement().getStyle().setProperty("boxSizing", "border-box");
    input.getElement().getStyle().setFontSize(14, Unit.PX);

    topBarTitleContainer.clear();
    topBarTitleContainer.add(input);

    final boolean[] committed = new boolean[] { false };
    input.addKeyDownHandler(new KeyDownHandler() {
      @Override
      public void onKeyDown(KeyDownEvent event) {
        int code = event.getNativeKeyCode();
        if (code == KeyCodes.KEY_ENTER) {
          event.preventDefault();
          commitInlineRename(input, convId, original, committed);
        } else if (code == KeyCodes.KEY_ESCAPE) {
          event.preventDefault();
          committed[0] = true;
          updateTopBarTitle();
        }
      }
    });
    input.addBlurHandler(new BlurHandler() {
      @Override
      public void onBlur(BlurEvent event) {
        // Defer so a click on the Save/Rename button can fire first and
        // claim the commit explicitly; otherwise blur's own commit wins.
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
          @Override
          public void execute() {
            if (!committed[0]) {
              commitInlineRename(input, convId, original, committed);
            }
          }
        });
      }
    });

    // Remember rename state so the top-bar Rename/Save button can commit.
    isRenamingInline = true;
    renameInput = input;
    renameConvId = convId;
    renameOriginal = original;
    renameCommitted = committed;
    setRenameButtonLabel(true);

    input.setFocus(true);
    input.selectAll();
  }

  /**
   * Commits the currently-active inline rename via the Save-labelled
   * top-bar button. No-op if no rename is in progress.
   */
  private void commitActiveInlineRename() {
    if (!isRenamingInline || renameInput == null) {
      return;
    }
    commitInlineRename(renameInput, renameConvId, renameOriginal, renameCommitted);
  }

  /** Flips the top-bar button between "Rename" (idle) and "Save" (editing). */
  private void setRenameButtonLabel(boolean editing) {
    if (renameButton == null) {
      return;
    }
    String label = editing
        ? MESSAGES.aiChatSaveConversationButton()
        : MESSAGES.aiChatRenameConversationButton();
    renameButton.setText(label);
    renameButton.setTitle(label);
  }

  private void commitInlineRename(TextBox input, final String convId,
      String original, boolean[] committed) {
    committed[0] = true;
    String newTitle = input.getValue() == null ? "" : input.getValue().trim();
    if (newTitle.equals(original)) {
      updateTopBarTitle();
      return;
    }
    orchestrator.renameConversation(convId, newTitle,
        new OdeAsyncCallback<AIConversationSummary>(
            MESSAGES.aiChatRenameConversationError()) {
          @Override
          public void onSuccess(AIConversationSummary updated) {
            if (updated != null) {
              currentSummary = updated;
            }
            updateTopBarTitle();
          }

          @Override
          public void onFailure(Throwable caught) {
            super.onFailure(caught);
            // Restore the label so the input doesn't linger after failure.
            updateTopBarTitle();
          }
        });
  }

  // ---- ResizeTarget implementation ----

  @Override
  public Element getDialogElement() {
    return getElement();
  }

  @Override
  public Panel getResizableContainer() {
    return outerContainer;
  }

  @Override
  public ScrollPanel getChatScrollPanel() {
    return chatScrollPanel;
  }

  /**
   * Imperatively sizes the chat scroll panel to fill whatever vertical
   * space remains inside the outer container after the currently-visible
   * fixed rows. Call after any change that could affect overhead
   * (initial open, debug banner toggle, edit-mode warning toggle,
   * op-preview show/hide) so the chat area expands immediately rather
   * than waiting for a user-driven resize.
   */
  private void sizeChatToContainer() {
    if (outerContainer == null || chatScrollPanel == null) {
      return;
    }
    // Skip while the chat view is hidden. All the overhead rows live inside
    // mainPanel, so when it's display:none every row reports offsetHeight=0
    // and getChatOverheadHeight() would drastically underestimate — the
    // chat would be sized way too tall and push the input + bottom bar
    // off-screen once mainPanel becomes visible again.  showChatView()
    // re-runs this helper after unhiding mainPanel.
    if (mainPanel == null || !mainPanel.isVisible()) {
      return;
    }
    int w = outerContainer.getOffsetWidth();
    int h = outerContainer.getOffsetHeight();
    if (w <= 0 || h <= 0) {
      return;
    }
    int chatHeight = Math.max(h - getChatOverheadHeight(), CHAT_MIN_HEIGHT);
    chatScrollPanel.setSize((w - 16) + "px", chatHeight + "px");
  }

  @Override
  public int getChatOverheadHeight() {
    // Sum the heights of every always-or-currently-visible row above and
    // below the chat scroll, plus mainPanel's padding. Subtracting this
    // from the outer container's height gives the chat scroll's budget.
    int overhead = 0;
    overhead += 16; // mainPanel padding (8 top + 8 bottom)
    overhead += offsetHeightOrZero(topBarTitleContainer == null
        ? null : topBarTitleContainer.getParent());
    overhead += 4;  // topBar marginBottom
    overhead += 8;  // chat border (1px top + 1px bottom) + margin-bottom
    if (debugBanner != null && debugBanner.isVisible()) {
      overhead += debugBanner.getOffsetHeight();
      overhead += 6; // debugBanner marginBottom
    }
    overhead += offsetHeightOrZero(statusAnimator == null
        ? null : statusAnimator.getWidget());
    overhead += offsetHeightOrZero(operationPreview == null
        ? null : operationPreview.getPreviewWidget());
    overhead += offsetHeightOrZero(operationPreview == null
        ? null : operationPreview.getButtonsWidget());
    overhead += offsetHeightOrZero(inputHandler == null
        ? null : inputHandler.getWidget());
    if (editModeWarning != null && editModeWarning.isVisible()) {
      overhead += editModeWarning.getOffsetHeight();
    }
    overhead += offsetHeightOrZero(operationPreview == null
        ? null : operationPreview.getAutoAcceptWidget());
    // bottomBar: parent of planToggle widget — measure its parent if available.
    if (planToggle != null && planToggle.getWidget().getParent() != null) {
      overhead += planToggle.getWidget().getParent().getOffsetHeight();
      overhead += 6; // bottomBar marginTop
    }
    return overhead;
  }

  private static int offsetHeightOrZero(Widget w) {
    if (w == null) {
      return 0;
    }
    if (!w.isVisible()) {
      return 0;
    }
    return w.getOffsetHeight();
  }
}
