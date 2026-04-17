# AIChatDialog Refactoring into `aiagent/dialog/` Subpackage

**Date:** 2026-04-10
**Status:** Draft

## Problem

`AIChatDialog.java` is 835 lines and mixes several concerns: UI widget construction, input handling, operation preview rendering, status animation, plan/execute toggle management, lifecycle, and 15 `ChatCallback` forwarding methods. The existing `aiagent/` package has already established a pattern of focused classes (`AIChatRenderer`, `AIResponseOrchestrator`, `AIDialogResizeHandler`, etc.), and the dialog should follow suit.

## Approach

**Option A (chosen):** Keep `AIChatDialog` as the sole `ChatCallback` implementor. Extract self-contained UI regions into classes in a new `aiagent/dialog/` subpackage. AIChatDialog becomes a thin shell (~220-250 lines) that assembles submodules and delegates callback methods.

**Rejected alternative (B):** Break `ChatCallback` into smaller interfaces. Would require editing ~65 call sites in `AIResponseOrchestrator` (1200+ lines, complex async state machines) for marginal benefit. The callback interface is not the bottleneck — the monolithic UI code is.

## New Package

```
com.google.appinventor.client.editor.youngandroid.aiagent.dialog
```

## Extracted Classes

### 1. `StatusAnimator`

**Responsibility:** Status label ("Thinking...") and ellipsis animation timer.

**Extracted from AIChatDialog:**
- `statusLabel` field and styling
- `ELLIPSIS` constant, `statusBaseText`, `statusEllipsisCounter`, `statusEllipsisTimer`
- `setStatusText()`, `setStatusVisible()`, `startStatusAnimation()`, `stopStatusAnimation()`

**Public API:**

```java
public class StatusAnimator {
  StatusAnimator()
  Label getWidget()
  void setText(String text)
  void setVisible(boolean visible)
}
```

`setVisible(true)` starts the timer; `setVisible(false)` stops it. Timer and counter are fully internal.

### 2. `OperationPreviewPanel`

**Responsibility:** Blue-bordered preview area showing proposed AI operations, action buttons (Apply, Apply & Accept All, Reject), and auto-accept notice panel.

**Extracted from AIChatDialog:**
- `operationPreview` FlowPanel and styling
- `applyButton`, `applyAndAcceptAllButton`, `rejectButton` and click handlers
- `autoAcceptPanel` with inline label and disable link
- `showOperationPreview(AIAgentResponse)` — full rendering with color-coded labels
- `hideOperationPreview()`
- `setAutoAcceptVisible(boolean)`

**Public API:**

```java
public class OperationPreviewPanel {
  OperationPreviewPanel(AIResponseOrchestrator orchestrator)
  FlowPanel getPreviewWidget()
  HorizontalPanel getButtonsWidget()
  FlowPanel getAutoAcceptWidget()
  void showPreview(AIAgentResponse response)
  void hidePreview()
  void setAutoAcceptVisible(boolean visible)
}
```

Takes a direct reference to `AIResponseOrchestrator` for the button click handlers (`applyOperations()`, `applyAndAcceptAll()`, `rejectOperations()`, `resetAutoAcceptAll()`). Uses `AIOperationFormatter.formatOperation()` for rendering operation labels. This matches existing patterns in the codebase.

### 3. `PlanExecuteToggle`

**Responsibility:** Plan & Execute mode button — toggling between direct/plan/executing states, styling, enable/disable based on conversation state, and tutorial-URL confirmation guard.

**Extracted from AIChatDialog:**
- `planExecuteButton` and click handler
- `togglePlanExecuteMode()`, `setPlanExecuteActive(boolean)`, `setPlanExecuteExecuting()`
- `stylePlanExecuteButton(String)`
- `updatePlanExecuteToggle()`

**Public API:**

```java
public class PlanExecuteToggle {
  interface TutorialUrlCheck {
    boolean hasTutorialUrl();
  }

  PlanExecuteToggle(AIContextCollector contextCollector,
                    TutorialUrlCheck tutorialCheck,
                    boolean orchestrationEnabled)
  Button getWidget()
  void update(boolean hasConversationMessages)
  void setExecuting()
  void setOrchestrationEnabled(boolean enabled)
  void reset()  // resets to direct mode (called from hideDialog)
}
```

**Internal behavior of `update()`:** Reads `contextCollector.getCurrentAIAgentMode()` internally to decide visibility (only shown in ProjectEditor mode). Combines this with the stored `orchestrationEnabled` flag (set via `setOrchestrationEnabled()`) and the `hasConversationMessages` parameter to determine enabled/disabled state. All three inputs are needed — `contextCollector` is held from construction, `orchestrationEnabled` is updated when server config arrives.

`hasTutorialUrl()` stays in AIChatDialog as a lambda passed to the constructor — it depends on `ProjectEditor`/`SettingsConstants` which are dialog-level concerns. `AIEditorState.setPlanExecuteMode()` is called internally by the toggle.

### 4. `ChatInputHandler`

**Responsibility:** Input textarea, send/stop button, Enter-key submission, and the `doSendMessage()` logic (validation, plan-rejection wrapping, state transitions).

**Extracted from AIChatDialog:**
- `inputArea` TextArea + styling + placeholder + key handler
- `sendButton` Button + styling + click handler
- `inputPanel` HorizontalPanel layout
- `doSendMessage()` — full method including plan rejection wrapping
- Send/stop button toggling from `setRequestInFlight(boolean)`

**Internal dependencies:** `doSendMessage()` calls `Ode.getInstance().getCurrentYoungAndroidProjectId()` for project-ID validation and `ErrorReporter.reportError()` on failure. It also handles plan-rejection wrapping (`orchestrator.hasPendingPlanProposal()`, `orchestrator.dismissPendingPlan()`, `renderer.dismissActivePlanCard()`) and prefixes user text with rejection context. All of this logic moves into `ChatInputHandler` — it only notifies the dialog via `SendCallback` for cross-cutting side effects.

**Public API:**

```java
public class ChatInputHandler {
  interface SendCallback {
    void onMessageSent();
  }

  ChatInputHandler(AIResponseOrchestrator orchestrator,
                   AIChatRenderer renderer,
                   SendCallback callback)
  HorizontalPanel getWidget()
  void setRequestInFlight(boolean inFlight)
}
```

`SendCallback.onMessageSent()` is implemented by AIChatDialog to handle cross-cutting side effects:
```java
void onMessageSent() {
  editModeWarning.setVisible(false);
  operationPreview.hidePreview();
  hasConversationMessages = true;
  planToggle.update(hasConversationMessages);
}
```

`setRequestInFlight` handles both the textarea enable/disable and the send↔stop button toggle internally.

## What Stays in `AIChatDialog`

After extraction, AIChatDialog retains (~220-250 lines):

- **Constructor** — creates 4 submodules + renderer/orchestrator, assembles into mainPanel, builds close-X on caption, bottom toolbar (plan toggle + new conversation), debug banner, edit-mode warning label
- **Lifecycle** — `show()`, `hideDialog()`, `onProjectChanged()`, position memory (`lastPopupLeft`/`lastPopupTop`)
- **`ChatCallback` implementation** — each of the 15 methods becomes 1-3 lines delegating to the appropriate submodule
- **`updateEditModeWarning()`** — ~12 lines, depends on `contextCollector` and a dialog-level label
- **`confirmAndClearConversation()`** — ~6 lines
- **`hasTutorialUrl()`** — ~10 lines, passed as lambda to `PlanExecuteToggle`
- **`ResizeTarget` implementation** — 3 getter methods

## Unchanged Files

- `AIResponseOrchestrator.java` — no changes. `ChatCallback` interface stays as-is.
- `AIChatRenderer.java` — no changes.
- `AIDialogResizeHandler.java` — no changes. `ResizeTarget` interface stays as-is.
- All other `aiagent/` classes — no changes.

## Dependency Graph

```
AIChatDialog
  ├── ChatInputHandler (needs orchestrator, renderer, SendCallback)
  ├── OperationPreviewPanel (needs orchestrator; uses AIOperationFormatter)
  ├── StatusAnimator (standalone)
  ├── PlanExecuteToggle (needs contextCollector, TutorialUrlCheck, orchestrationEnabled)
  ├── AIChatRenderer (existing, unchanged)
  ├── AIResponseOrchestrator (existing, unchanged)
  └── AIDialogResizeHandler (existing, unchanged)
```

No submodule depends on another submodule. All dependencies flow from AIChatDialog downward.
