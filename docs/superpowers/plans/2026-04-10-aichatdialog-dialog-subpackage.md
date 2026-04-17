# AIChatDialog Dialog Subpackage Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Extract 4 self-contained UI submodules from the 835-line `AIChatDialog.java` into a new `aiagent/dialog/` subpackage, reducing the dialog to a ~220-line thin shell.

**Architecture:** Each submodule owns its widget construction, styling, and behavior. `AIChatDialog` remains the sole `ChatCallback` implementor, delegating to submodules. No changes to `AIResponseOrchestrator`, `AIChatRenderer`, or any other existing class.

**Tech Stack:** GWT (Google Web Toolkit), Java 8

**Spec:** `docs/superpowers/specs/2026-04-10-aichatdialog-dialog-subpackage-design.md`

---

## File Structure

| Action | File |
|--------|------|
| Create | `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/dialog/StatusAnimator.java` |
| Create | `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/dialog/OperationPreviewPanel.java` |
| Create | `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/dialog/PlanExecuteToggle.java` |
| Create | `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/dialog/ChatInputHandler.java` |
| Modify | `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/AIChatDialog.java` |
| Modify | `appinventor/appengine/src/com/google/appinventor/YaClient.gwt.xml:77-79` |

---

### Task 1: Register the new subpackage in GWT

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/YaClient.gwt.xml:77-79`

- [ ] **Step 1: Add the `dialog` source path to the GWT module**

In `YaClient.gwt.xml`, after the existing `aiagent/validator` line (line 79), add:

```xml
  <source path="client/editor/youngandroid/aiagent/dialog"/>
```

- [ ] **Step 2: Verify the edit**

Run: `grep -n 'aiagent' appinventor/appengine/src/com/google/appinventor/YaClient.gwt.xml`

Expected: 4 lines — `aiagent`, `aiagent/executor`, `aiagent/validator`, `aiagent/dialog`

- [ ] **Step 3: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/YaClient.gwt.xml
git commit -m "Register aiagent/dialog subpackage in GWT module"
```

---

### Task 2: Create `StatusAnimator`

**Files:**
- Create: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/dialog/StatusAnimator.java`

- [ ] **Step 1: Create the file**

```java
// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.aiagent.dialog;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Label;

/**
 * Manages the status label ("Thinking...") with an animated ellipsis.
 *
 * <p>When visible, cycles through ".", "..", "..." on a 600ms timer.
 * When hidden, the timer is stopped.</p>
 */
public class StatusAnimator {

  private static final String[] ELLIPSIS = {".", "..", "..."};

  private final Label statusLabel;
  private String baseText = "";
  private int ellipsisCounter;
  private Timer ellipsisTimer;

  /**
   * Constructs the status label with default styling.
   */
  public StatusAnimator() {
    statusLabel = new Label();
    statusLabel.getElement().getStyle().setProperty("fontStyle", "italic");
    statusLabel.getElement().getStyle().setColor("#666");
    statusLabel.getElement().getStyle().setMarginBottom(4, Unit.PX);
    statusLabel.setVisible(false);
  }

  /**
   * Returns the label widget for adding to a parent panel.
   */
  public Label getWidget() {
    return statusLabel;
  }

  /**
   * Sets the base text displayed before the animated ellipsis.
   */
  public void setText(String text) {
    baseText = text != null ? text : "";
    ellipsisCounter = 0;
    statusLabel.setText(baseText);
  }

  /**
   * Shows or hides the status label. Starts the ellipsis animation
   * when visible; stops it when hidden.
   */
  public void setVisible(boolean visible) {
    statusLabel.setVisible(visible);
    if (visible) {
      startAnimation();
    } else {
      stopAnimation();
    }
  }

  private void startAnimation() {
    stopAnimation();
    ellipsisCounter = 0;
    ellipsisTimer = new Timer() {
      @Override
      public void run() {
        ellipsisCounter++;
        String dots = ELLIPSIS[ellipsisCounter % ELLIPSIS.length];
        statusLabel.setText(baseText + dots);
      }
    };
    ellipsisTimer.scheduleRepeating(600);
  }

  private void stopAnimation() {
    if (ellipsisTimer != null) {
      ellipsisTimer.cancel();
      ellipsisTimer = null;
    }
  }
}
```

- [ ] **Step 2: Verify the file compiles**

Run: `cd appinventor && ant AiClientLib`

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/dialog/StatusAnimator.java
git commit -m "Extract StatusAnimator from AIChatDialog"
```

---

### Task 3: Create `OperationPreviewPanel`

**Files:**
- Create: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/dialog/OperationPreviewPanel.java`

- [ ] **Step 1: Create the file**

```java
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
```

- [ ] **Step 2: Verify the file compiles**

Run: `cd appinventor && ant AiClientLib`

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/dialog/OperationPreviewPanel.java
git commit -m "Extract OperationPreviewPanel from AIChatDialog"
```

---

### Task 4: Create `PlanExecuteToggle`

**Files:**
- Create: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/dialog/PlanExecuteToggle.java`

- [ ] **Step 1: Create the file**

```java
// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.youngandroid.aiagent.dialog;

import static com.google.appinventor.client.Ode.MESSAGES;
import static com.google.appinventor.shared.settings.SettingsConstants.AI_AGENT_MODE_PROJECT_EDITOR;

import com.google.appinventor.client.editor.youngandroid.aiagent.AIContextCollector;
import com.google.appinventor.client.editor.youngandroid.aiagent.AIEditorState;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;

/**
 * Manages the Plan &amp; Execute mode toggle button.
 *
 * <p>Three visual states: "direct" (grey, default), "plan" (blue, active),
 * and "executing" (blue, disabled). The button is only visible in
 * ProjectEditor mode with orchestration enabled.</p>
 */
public class PlanExecuteToggle {

  /**
   * Callback to check whether the current project has a tutorial URL.
   */
  public interface TutorialUrlCheck {
    boolean hasTutorialUrl();
  }

  private final AIContextCollector contextCollector;
  private final TutorialUrlCheck tutorialCheck;
  private final Button button;
  private boolean orchestrationEnabled;

  /**
   * Constructs the toggle button.
   *
   * @param contextCollector    provides the current AI agent mode
   * @param tutorialCheck       callback to check for tutorial URL
   * @param orchestrationEnabled initial state of the orchestration flag
   */
  public PlanExecuteToggle(AIContextCollector contextCollector,
      TutorialUrlCheck tutorialCheck, boolean orchestrationEnabled) {
    this.contextCollector = contextCollector;
    this.tutorialCheck = tutorialCheck;
    this.orchestrationEnabled = orchestrationEnabled;

    button = new Button(MESSAGES.aiChatPlanExecuteOff());
    styleButton("direct");
    button.setVisible(false);
    button.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        toggleMode();
      }
    });
  }

  /** Returns the button widget. */
  public Button getWidget() {
    return button;
  }

  /**
   * Updates visibility and enabled state based on the current AI mode,
   * orchestration flag, and whether the conversation has messages.
   */
  public void update(boolean hasConversationMessages) {
    String mode = contextCollector.getCurrentAIAgentMode();
    if (AI_AGENT_MODE_PROJECT_EDITOR.equals(mode) && orchestrationEnabled) {
      button.setVisible(true);
      if (hasConversationMessages) {
        button.setEnabled(false);
        button.getElement().getStyle().setProperty("cursor", "default");
        button.getElement().getStyle().setProperty("opacity", "0.7");
      } else {
        button.setEnabled(true);
        button.getElement().getStyle().setProperty("cursor", "pointer");
        button.getElement().getStyle().setProperty("opacity", "1");
      }
    } else {
      setActive(false);
      button.setVisible(false);
    }
  }

  /** Sets the button to "Execution Mode" state (blue, disabled). */
  public void setExecuting() {
    button.setText(MESSAGES.aiChatPlanExecuteExecuting());
    styleButton("executing");
  }

  /** Updates the stored orchestration flag. */
  public void setOrchestrationEnabled(boolean enabled) {
    this.orchestrationEnabled = enabled;
  }

  /** Resets to direct mode. Called when the dialog is hidden. */
  public void reset() {
    setActive(false);
  }

  private void toggleMode() {
    boolean newState = !AIEditorState.isPlanExecuteMode();
    if (newState && tutorialCheck.hasTutorialUrl()) {
      boolean confirmed = Window.confirm(MESSAGES.aiChatPlanExecuteTutorialConfirm());
      if (!confirmed) {
        return;
      }
    }
    setActive(newState);
  }

  private void setActive(boolean active) {
    AIEditorState.setPlanExecuteMode(active);
    if (active) {
      button.setText(MESSAGES.aiChatPlanExecuteOn());
    } else {
      button.setText(MESSAGES.aiChatPlanExecuteOff());
    }
    styleButton(active ? "plan" : "direct");
  }

  private void styleButton(String state) {
    button.getElement().getStyle().setProperty("borderRadius", "3px");
    button.getElement().getStyle().setFontSize(12, Unit.PX);
    if ("direct".equals(state)) {
      button.getElement().getStyle().setProperty("background", "#f5f5f5");
      button.getElement().getStyle().setColor("#333");
      button.getElement().getStyle().setProperty("border", "1px solid #ccc");
      button.getElement().getStyle().setProperty("cursor", "pointer");
      button.getElement().getStyle().setProperty("opacity", "1");
      button.setEnabled(true);
    } else if ("plan".equals(state)) {
      button.getElement().getStyle().setProperty("background", "#4a90d9");
      button.getElement().getStyle().setColor("white");
      button.getElement().getStyle().setProperty("border", "1px solid #3a7bc8");
      button.getElement().getStyle().setProperty("cursor", "pointer");
      button.getElement().getStyle().setProperty("opacity", "1");
      button.setEnabled(true);
    } else {
      button.getElement().getStyle().setProperty("background", "#4a90d9");
      button.getElement().getStyle().setColor("white");
      button.getElement().getStyle().setProperty("border", "1px solid #3a7bc8");
      button.getElement().getStyle().setProperty("cursor", "default");
      button.getElement().getStyle().setProperty("opacity", "0.7");
      button.setEnabled(false);
    }
  }
}
```

- [ ] **Step 2: Verify the file compiles**

Run: `cd appinventor && ant AiClientLib`

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/dialog/PlanExecuteToggle.java
git commit -m "Extract PlanExecuteToggle from AIChatDialog"
```

---

### Task 5: Create `ChatInputHandler`

**Files:**
- Create: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/dialog/ChatInputHandler.java`

- [ ] **Step 1: Create the file**

```java
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
```

- [ ] **Step 2: Verify the file compiles**

Run: `cd appinventor && ant AiClientLib`

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/dialog/ChatInputHandler.java
git commit -m "Extract ChatInputHandler from AIChatDialog"
```

---

### Task 6: Rewrite `AIChatDialog` to use submodules

This is the core task. Replace the inline UI code with submodule delegation.

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/AIChatDialog.java`

- [ ] **Step 1: Rewrite AIChatDialog**

The new file should contain approximately the following structure. Key changes:
- Remove all fields that moved to submodules (`inputArea`, `sendButton`, `applyButton`, `applyAndAcceptAllButton`, `rejectButton`, `planExecuteButton`, `statusLabel`, `autoAcceptPanel`, and the status animation fields)
- Add 4 submodule fields: `statusAnimator`, `operationPreview`, `planToggle`, `inputHandler`
- Constructor creates submodules and adds their widgets to `mainPanel` in the same order
- All `ChatCallback` methods delegate to submodules
- Lifecycle methods (`show`, `hideDialog`, `onProjectChanged`) use submodules
- `updateEditModeWarning()`, `confirmAndClearConversation()`, `hasTutorialUrl()`, debug banner, and `ResizeTarget` stay as-is

Replace the full content of `AIChatDialog.java` with:

```java
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
```

- [ ] **Step 2: Verify the full project compiles**

Run: `cd appinventor && ant AiClientLib`

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Verify no references to removed fields/methods remain**

Run: `grep -rn 'stylePlanExecuteButton\|setPlanExecuteActive\|setPlanExecuteExecuting\|togglePlanExecuteMode\|updatePlanExecuteToggle\|startStatusAnimation\|stopStatusAnimation\|statusEllipsis' appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/AIChatDialog.java`

Expected: no output (all moved to submodules)

- [ ] **Step 4: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/AIChatDialog.java
git commit -m "Rewrite AIChatDialog to delegate to dialog submodules

Reduces AIChatDialog from 835 lines to ~250 by delegating to:
- StatusAnimator: status label + ellipsis animation
- OperationPreviewPanel: operation preview + action buttons
- PlanExecuteToggle: plan/execute mode button
- ChatInputHandler: input area + send/stop button

No changes to AIResponseOrchestrator or ChatCallback interface."
```

---

### Task 7: Final verification

- [ ] **Step 1: Full GWT compilation**

Run: `cd appinventor && ant YaClientApp`

Expected: `BUILD SUCCESSFUL`

This compiles the entire GWT client application, ensuring all cross-references resolve.

- [ ] **Step 2: Line count check**

Run: `wc -l appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/AIChatDialog.java appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/dialog/*.java`

Expected: AIChatDialog ~250 lines, 4 submodule files, total roughly equal to original 835 lines.
