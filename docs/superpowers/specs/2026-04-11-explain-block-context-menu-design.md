# Explain Block Context Menu

**Date:** 2026-04-11
**Status:** Approved

## Problem

Users working with blocks in the Blockly editor have no quick way to ask the AI assistant to explain what a specific block or block group does. They must manually open the AI chat, type out a description of the block, and hope the AI can find it in the full YAIL context. This is especially hard for non-top-level blocks buried inside event handlers or procedures.

## Solution

Add an "Explain Block" context menu item to all blocks in the Blockly editor. When clicked, it opens the AI chat (or appends to the existing conversation) with a natural language prompt describing the selected block. A new `contextHint` field on `AIAgentRequest` carries block-specific context (YAIL, disabled state, warnings/errors) to the LLM without showing it in the user's chat bubble.

## Design

### Layer 1: Context Menu Item (Blockly JS)

New function `AI.Blockly.ContextMenuItems.registerExplainBlockOption()` in `blocklyeditor.js`, called from `registerAll()`.

**Registration:**
```javascript
{
  displayText: function(scope) { ... },
  callback: function(scope) { ... },
  preconditionFn: function(scope) { ... },
  weight: 99,
  id: 'appinventor_explain_block',
  scopeType: Blockly.ContextMenuRegistry.ScopeType.BLOCK,
}
```

**preconditionFn logic (three states):**
- `'hidden'` — block is in flyout, OR `window.parent.BlocklyPanel_isAIAgentAvailable()` returns false (server-level feature flag is off)
- `'disabled'` — AI is available but `window.parent.BlocklyPanel_getAIAgentMode()` returns `"Off"` (user hasn't configured AI mode for this project)
- `'enabled'` — AI is available and mode is any active value (Advisor, ScreenEditor, ProjectEditor)

**displayText (function):**
- When enabled: `Blockly.Msg['AI_EXPLAIN_BLOCK']` ("Explain Block")
- When disabled: `Blockly.Msg['AI_EXPLAIN_BLOCK_DISABLED']` ("Explain Block (enable AI Assistant first)")

**callback — prompt construction:**

A helper function `buildExplainPrompt(block)` returns `{ displayText, contextHint }`.

Display text (shown in chat bubble):

| Condition | Display text |
|---|---|
| Top-level `component_event` | "Explain the selected {instanceName}.{eventName} event handler" |
| Top-level `procedures_defnoreturn` / `procedures_defreturn` | "Explain the selected \"{NAME}\" procedure" |
| Top-level `global_declaration` | "Explain the selected \"{NAME}\" global variable" |
| Other top-level | "Explain the selected block" |
| Non-root, root is `component_event` | "Explain the selected part of the {instanceName}.{eventName} event handler" |
| Non-root, root is `procedures_def*` | "Explain the selected part of the \"{NAME}\" procedure" |
| Non-root, root is `global_declaration` | "Explain the selected part of the \"{NAME}\" global variable" |
| Non-root, other/no identifiable root | "Explain the selected block" |

contextHint (hidden from user, sent to LLM):

Always generated. Contains a structured string with:

1. **YAIL of the selected block:**
   - Top-level blocks: `AI.Yail.blockToCode1(block)` — just the block's own code, since the full subtree is already in the standard `blocksYail` context.
   - Non-root blocks: `AI.Yail.blockToCode(block)` — full subtree from this block down, since it's not individually available in the standard context.
   - If YAIL generation throws, include a note: "YAIL generation failed for this block."
2. **Block state:** If `!block.isEnabled()`, include "This block is disabled." If `block.isBadBlock()`, include "This block has errors and cannot generate valid YAIL."
3. **Warnings/errors:** If the block has a WarningIcon (`block.getIcon(Blockly.icons.WarningIcon.TYPE)`), include its text via `.getText()`. If `block.replError` is set, include that too.

The callback calls `window.parent.BlocklyPanel_explainBlock(displayText, contextHint)`.

### Layer 2: Bridge (BlocklyPanel.java)

Three new static methods, exported in `exportMethodsToJavascript()`:

**`isAIAgentAvailable()`** — returns `Ode.getSystemConfig().getAiAgentAvailable()`. Exposed as `$wnd.BlocklyPanel_isAIAgentAvailable`.

**`getAIAgentMode()`** — returns the current AI agent mode string for the active project. Looks up the project setting `YOUNG_ANDROID_SETTINGS_AI_AGENT_MODE` via the open project editor. Returns `"Off"` if no project is open or no mode is set. Exposed as `$wnd.BlocklyPanel_getAIAgentMode`.

**`explainBlock(String displayText, String contextHint)`** — calls `Ode.getInstance().sendExplainToAIChat(displayText, contextHint)`. Exposed as `$wnd.BlocklyPanel_explainBlock`.

### Layer 3: AIChatDialog & Ode Routing

**Ode.java** — new public method:

`sendExplainToAIChat(String displayText, String contextHint)`:
- Lazy-initializes `aiChatDialog` if null.
- Delegates to `aiChatDialog.sendExplainMessage(displayText, contextHint)`.

**AIChatDialog.java** — new fields and method:

Fields:
- `private String pendingExplainDisplay` — queued display text for deferred send.
- `private String pendingExplainHint` — queued context hint for deferred send.

New method `sendExplainMessage(String displayText, String contextHint)`:
- If the dialog is not showing: stores `displayText` and `contextHint` in the pending fields, calls `show()`, and returns. The `show()` method may trigger the mode selection dialog; when mode selection completes it calls `show()` again.
- If the dialog is showing and a request is already in flight: returns (don't interrupt).
- Otherwise: renders `displayText` as a user message bubble via `renderer.addUserMessage()`, hides edit mode warning, hides operation preview, sets `hasConversationMessages = true`, updates plan toggle, then calls `orchestrator.sendMessageWithContext(displayText, contextHint)`.

Modified `show()`: after `super.show()` and existing setup, checks if `pendingExplainDisplay` is non-null. If so, clears the pending fields and calls `sendExplainMessage()` with the saved values. This handles the deferred case where mode selection had to complete first.

### Layer 4: AIResponseOrchestrator

New method `sendMessageWithContext(String text, String contextHint)`:
- Calls `contextCollector.buildRequest(text)` to create the request.
- Calls `request.setContextHint(contextHint)` on the request.
- Remainder is identical to `sendMessage()`: sets in-flight state, starts polling, fires the RPC.

The existing `sendMessage(String text)` is unchanged (contextHint remains null on the request).

### Layer 5: Shared DTO (AIAgentRequest.java)

New optional field:
```java
private String contextHint;
```

With getter/setter:
```java
public String getContextHint() { return contextHint; }
public void setContextHint(String contextHint) { this.contextHint = contextHint; }
```

No constructor change — GWT-RPC serialization uses the no-arg constructor and setters.

### Layer 6: Server — Message Augmentation (AIAgentServiceImpl.java)

In `AIAgentServiceImpl.processRequest()`, after sanitizing `userMessage` (line 65) and after the length check (line 66-68), prepend the contextHint. The prepend happens *after* the length check so that system-generated context does not count toward `MAX_MESSAGE_LENGTH`:

```java
// After: userMessage = sanitize(userMessage);
// After: length check on userMessage
String contextHint = request.getContextHint();
if (contextHint != null && !contextHint.isEmpty()) {
  userMessage = contextHint + "\n\n" + userMessage;
}
```

The augmented message is then passed through to `engine.processRequest()` as the `userMessage` parameter, where it is:
- Stored in conversation history (so follow-up questions have full context).
- Sent to the LLM as the user message.

### Layer 7: i18n

**`messages.json`** (English) + 21 locale files:
- `"Blockly.Msg.AI_EXPLAIN_BLOCK"`: `"Explain Block"`
- `"Blockly.Msg.AI_EXPLAIN_BLOCK_DISABLED"`: `"Explain Block (enable AI Assistant first)"`

Locale files receive the English strings as translation placeholders.

## Files Modified

| File | Change |
|---|---|
| `blocklyeditor/src/blocklyeditor.js` | New `registerExplainBlockOption()`, add to `registerAll()` |
| `blocklyeditor/src/msg/ai_blockly/messages.json` | Two new message keys |
| `blocklyeditor/src/msg/ai_blockly/messages_*.json` (21 files) | Same two keys as placeholders |
| `appengine/.../editor/blocks/BlocklyPanel.java` | Three new bridge methods + exports |
| `appengine/.../client/Ode.java` | New `sendExplainToAIChat()` |
| `appengine/.../editor/youngandroid/AIChatDialog.java` | New `sendExplainMessage()`, pending fields, modified `show()` |
| `appengine/.../aiagent/AIResponseOrchestrator.java` | New `sendMessageWithContext()` |
| `shared/.../rpc/aiagent/AIAgentRequest.java` | New `contextHint` field + getter/setter |
| `appengine/.../server/aiagent/AIAgentServiceImpl.java` | Prepend contextHint to user message |
