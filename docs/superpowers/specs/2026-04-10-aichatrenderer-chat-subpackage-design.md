# AIChatRenderer chat/ Subpackage Decomposition

**Date:** 2026-04-10
**Status:** Approved
**Branch:** ai-agent-plan

## Problem

`AIChatRenderer.java` (750 lines) bundles five distinct responsibilities: message bubble rendering, streaming state management, plan card UI, feedback links, and Markdown conversion. The streaming handler alone carries 7 mutable fields forming an independent state machine, and the plan card renderer has its own self-contained UI flow with edit/approve/reject interactions. This makes the file harder to navigate and reason about than it needs to be.

## Solution

Extract two self-contained concerns into a new `aiagent/chat/` subpackage. `AIChatRenderer` stays in `aiagent/` as a facade that composes them. The public API seen by `AIChatDialog` does not change.

### New Package Structure

```
aiagent/
  AIChatRenderer.java          (facade, ~270 lines — down from 750)
  ...
  chat/
    ChatRendererHost.java       (interface, ~15 lines)
    ChatStreamingHandler.java   (~130 lines)
    ChatPlanCardRenderer.java   (~180 lines)
```

## Design

### ChatRendererHost (interface)

Defined in `aiagent/chat/`. Provides the contract that `ChatStreamingHandler` needs from its parent renderer, avoiding a circular package dependency.

```java
public interface ChatRendererHost {
    FlowPanel createMessageBubble(String sender, String text, boolean isUser);
    void addToHistory(Widget widget);
    String markdownToSafeHtml(String markdown);
    void scrollToBottom();
    void appendFeedbackLink(FlowPanel wrapper);
    String getAiLabel();
}
```

`addToHistory` wraps `chatHistory.add(widget)` — needed by `startStreamingBubble()` to insert the new bubble into the panel.

`AIChatRenderer` implements this interface.

### ChatStreamingHandler

**Owns all streaming state:**
- `streamingTextAccumulator`, `streamingThinkingAccumulator`
- `streamingWrapper`, `streamingMessageHtml`, `streamingThinkingHtml`, `streamingThinkingPanel`
- `typingIndicator`

**Constructor:** `ChatStreamingHandler(ChatRendererHost host)`

**Public methods (same signatures as today):**
- `startStreamingBubble()`
- `appendStreamingText(String delta)`
- `appendStreamingThinking(String delta)`
- `finalizeStreamingBubble(String finalText)`

**Private methods that move with it:**
- `countOccurrences(String, String)`

**How it uses the host:**
- `startStreamingBubble()` calls `host.createMessageBubble(host.getAiLabel(), "", false)`, `host.addToHistory(wrapper)`, and `host.scrollToBottom()`
- `appendStreamingText()` calls `host.markdownToSafeHtml()` and `host.scrollToBottom()`
- `appendStreamingThinking()` calls `host.markdownToSafeHtml()` and `host.scrollToBottom()`
- `finalizeStreamingBubble()` calls `host.markdownToSafeHtml()`, `host.appendFeedbackLink()`, and `host.scrollToBottom()`

### ChatPlanCardRenderer

**Owns:** `activePlanButtonBar` field.

**Constructor:** `ChatPlanCardRenderer(FlowPanel chatHistory, Runnable scrollCallback)`

Simpler dependency model than streaming — just needs the panel to add widgets to and a way to scroll.

**Public methods:**
- `renderPlanCard(String planJson, AIResponseOrchestrator.PlanApprovalCallback approvalCallback)`
- `dismissActivePlanCard()`

**Private methods that move with it:**
- `showPlanEditor(FlowPanel, HorizontalPanel, String, PlanApprovalCallback)`
- `disablePlanButtons(HorizontalPanel)`
- `buildStepsHtml(String)` (static)
- `findMatchingBrace(String, int)` (static)
- `extractArrayField(String, String)` (static)
- `escapeHtml(String)` (static)

### AIChatRenderer (what remains)

**Implements:** `ChatRendererHost`

**Composes:**
- `ChatStreamingHandler streamingHandler` — created in constructor, passed `this`
- `ChatPlanCardRenderer planCardRenderer` — created in constructor, passed `chatHistory` and `this::scrollToBottom`

**Public API is unchanged.** Streaming and plan card methods become one-line delegates:

```java
public void startStreamingBubble() {
    streamingHandler.startStreamingBubble();
}
public void renderPlanCard(String planJson, PlanApprovalCallback cb) {
    planCardRenderer.renderPlanCard(planJson, cb);
}
```

**Methods that stay:**
- `initialize()`, `addUserMessage()`, `addAiMessage()`, `scrollToBottom()`, `clear()`
- `setFeedbackContext()`, `appendFeedbackLink()`, `buildFeedbackUrl()` (static)
- `createMessageBubble()` — exposed via `ChatRendererHost`
- `markdownToSafeHtml()` — public instance wrapper delegating to `private static native` JSNI method (interface requires non-static)
- `configureMarked()` (JSNI)

**Visibility changes:** `createMessageBubble` and `appendFeedbackLink` change from `private` to `public` (required by `ChatRendererHost` interface implementation).

## Impact on AIChatDialog

None. The import stays `aiagent.AIChatRenderer`, the public API is identical. `AIChatDialog` does not need to know about the `chat/` subpackage.

## CONTRIBUTING_AI.md Update

Add a new table under the Client section:

```markdown
### Chat Rendering -- `client/.../aiagent/chat/`

| File | Purpose |
|------|---------|
| `ChatRendererHost.java` | Interface for streaming handler to call back into the renderer |
| `ChatStreamingHandler.java` | Streaming bubble state machine: start, append text/thinking, finalize |
| `ChatPlanCardRenderer.java` | Plan card UI with approve/edit/reject flow |
```

Update the existing `AIChatRenderer.java` description to: "Facade for chat rendering; delegates streaming to `ChatStreamingHandler` and plan cards to `ChatPlanCardRenderer`".

## Files Changed

| File | Action |
|------|--------|
| `aiagent/chat/ChatRendererHost.java` | Create |
| `aiagent/chat/ChatStreamingHandler.java` | Create |
| `aiagent/chat/ChatPlanCardRenderer.java` | Create |
| `aiagent/AIChatRenderer.java` | Modify (implement interface, delegate, remove extracted code) |
| `CONTRIBUTING_AI.md` | Update client tables |
