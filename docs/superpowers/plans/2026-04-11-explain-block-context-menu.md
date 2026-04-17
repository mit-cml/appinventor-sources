# Explain Block Context Menu Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add an "Explain Block" right-click menu item to all Blockly blocks that sends a prompt to the AI chat with block-specific context.

**Architecture:** A new Blockly context menu item constructs a display prompt and a hidden `contextHint` (YAIL, block state, warnings). It bridges to the GWT layer via `BlocklyPanel`, routes through `Ode` to `AIChatDialog`, which renders the display text as a user bubble and sends the full message + contextHint to the server. The server prepends the contextHint to the user message before the LLM call.

**Tech Stack:** JavaScript (Blockly editor), Java (GWT client + App Engine server), GWT-RPC serialization

**Spec:** `docs/superpowers/specs/2026-04-11-explain-block-context-menu-design.md`

---

## File Map

| File | Action | Responsibility |
|---|---|---|
| `appinventor/appengine/src/com/google/appinventor/shared/rpc/aiagent/AIAgentRequest.java` | Modify | Add `contextHint` field + getter/setter |
| `appinventor/appengine/src/com/google/appinventor/server/aiagent/AIAgentServiceImpl.java` | Modify | Prepend contextHint to userMessage |
| `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/AIResponseOrchestrator.java` | Modify | New `sendMessageWithContext()` method |
| `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/AIChatDialog.java` | Modify | New `sendExplainMessage()`, pending fields, modified `show()` |
| `appinventor/appengine/src/com/google/appinventor/client/Ode.java` | Modify | New `sendExplainToAIChat()` routing method |
| `appinventor/appengine/src/com/google/appinventor/client/editor/blocks/BlocklyPanel.java` | Modify | Three new bridge methods + JSNI exports |
| `appinventor/blocklyeditor/src/blocklyeditor.js` | Modify | New `registerExplainBlockOption()` + `buildExplainPrompt()` |
| `appinventor/blocklyeditor/src/msg/ai_blockly/messages.json` | Modify | Two new i18n keys |
| `appinventor/blocklyeditor/src/msg/ai_blockly/messages_*.json` (21 files) | Modify | Same two keys as placeholders |

---

### Task 1: Add contextHint to AIAgentRequest DTO

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/shared/rpc/aiagent/AIAgentRequest.java`

This is the shared DTO used by both client (GWT) and server. Adding a field here first means all downstream consumers can reference it immediately.

- [ ] **Step 1: Add the contextHint field**

After the existing `platformMessage` field (line 30), add:

```java
  private String contextHint;
```

- [ ] **Step 2: Add getter and setter**

After the `wrapPlatformMessage` method (after line 258), add:

```java
  /**
   * Returns an optional context hint for the AI agent. Used to pass
   * additional block-specific context (e.g., YAIL, warnings) that
   * should be prepended to the user message before the LLM call.
   */
  public String getContextHint() {
    return contextHint;
  }

  public void setContextHint(String contextHint) {
    this.contextHint = contextHint;
  }
```

- [ ] **Step 3: Verify the file compiles**

No constructor changes needed — GWT-RPC serialization uses the no-arg constructor + setters. The field defaults to `null`.

---

### Task 2: Prepend contextHint on the server

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/AIAgentServiceImpl.java:65-69`

- [ ] **Step 1: Add contextHint prepend after length check**

In `processRequest()`, after the length check (line 66-69) and before `validateRequest()` (line 71), insert:

```java
    // Prepend optional context hint (system-generated, bypasses length check)
    String contextHint = request.getContextHint();
    if (contextHint != null && !contextHint.isEmpty()) {
      userMessage = contextHint + "\n\n" + userMessage;
    }
```

The insertion goes between the closing `}` of the length check block (line 69) and the blank line before `RequestContext ctx = validateRequest(projectId);` (line 71).

---

### Task 3: Add sendMessageWithContext to AIResponseOrchestrator

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/AIResponseOrchestrator.java:159`

- [ ] **Step 1: Add the new method**

After the closing `}` of `sendMessage()` (line 159), add:

```java
  /**
   * Sends a message to the AI agent with an additional context hint.
   * The context hint is attached to the request and prepended to the
   * user message server-side before the LLM call.
   *
   * @param text the user's message text (shown in chat)
   * @param contextHint additional context for the LLM (hidden from chat)
   */
  public void sendMessageWithContext(String text, String contextHint) {
    AIAgentRequest request = contextCollector.buildRequest(text);
    if (contextHint != null && !contextHint.isEmpty()) {
      request.setContextHint(contextHint);
    }
    requestInFlight = true;
    callback.setRequestInFlight(true);
    validationRetryCount = 0;
    executionRetryCount = 0;
    originalToolCount = 0;
    preservedValidOps = null;
    preservedAiMessage = null;
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
        requestInFlight = false;
        callback.setRequestInFlight(false);
        stopPollingStatus();
        callback.addAiMessage(MESSAGES.aiChatSendError() + ": " + caught.getMessage());
      }
    });
  }
```

---

### Task 4: Add sendExplainMessage to AIChatDialog

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/AIChatDialog.java`

- [ ] **Step 1: Add pending fields**

After the existing `private long currentProjectId;` field (line 87), add:

```java
  // Deferred explain message (queued when dialog needs mode selection first)
  private String pendingExplainDisplay;
  private String pendingExplainHint;
```

- [ ] **Step 2: Add sendExplainMessage method**

After the `hideDialog()` method (after line 375), add:

```java
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
    renderer.addUserMessage(displayText);
    editModeWarning.setVisible(false);
    hideOperationPreview();
    orchestrator.sendMessageWithContext(displayText, contextHint);
  }
```

- [ ] **Step 3: Modify show() to handle pending explain message**

In the `show()` method, replace the `orchestrator.loadExistingConversation();` call (line 339) with a conditional block that skips history loading when an explain message is pending (to avoid a race condition where async history load reorders messages):

```java
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
      orchestrator.loadExistingConversation();
    }
```

---

### Task 5: Add routing method to Ode

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/client/Ode.java`

- [ ] **Step 1: Add sendExplainToAIChat method**

After the `toggleAIChatDialog()` method (after its closing `}`), add:

```java
  /**
   * Opens the AI chat dialog and sends an explain-block message.
   * If the dialog hasn't been created yet, it is lazy-initialized.
   *
   * @param displayText the text shown in the user's chat bubble
   * @param contextHint hidden context (YAIL, warnings) sent to the LLM
   */
  public void sendExplainToAIChat(String displayText, String contextHint) {
    if (aiChatDialog == null) {
      aiChatDialog = new AIChatDialog();
    }
    aiChatDialog.sendExplainMessage(displayText, contextHint);
  }
```

---

### Task 6: Add bridge methods to BlocklyPanel

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/client/editor/blocks/BlocklyPanel.java`

- [ ] **Step 1: Add the three static methods**

After `checkIsAdmin()` (after line 314), add:

```java
  /**
   * Returns true if the AI agent feature is available on this server.
   * Called from Blockly JS via the exported bridge function.
   */
  public static boolean isAIAgentAvailable() {
    return Ode.getSystemConfig().getAiAgentAvailable();
  }

  /**
   * Returns the current AI agent mode for the active project.
   * Returns "Off" if no project is open or no mode is configured.
   * Called from Blockly JS via the exported bridge function.
   */
  public static String getAIAgentMode() {
    long projectId = Ode.getInstance().getCurrentYoungAndroidProjectId();
    if (projectId == 0) {
      return SettingsConstants.AI_AGENT_MODE_OFF;
    }
    com.google.appinventor.client.editor.ProjectEditor projectEditor =
        Ode.getInstance().getEditorManager().getOpenProjectEditor(projectId);
    if (projectEditor == null) {
      return SettingsConstants.AI_AGENT_MODE_OFF;
    }
    String mode = projectEditor.getProjectSettingsProperty(
        SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
        SettingsConstants.YOUNG_ANDROID_SETTINGS_AI_AGENT_MODE);
    return (mode == null || mode.isEmpty()) ? SettingsConstants.AI_AGENT_MODE_OFF : mode;
  }

  /**
   * Opens the AI chat and sends an explain-block message.
   * Called from Blockly JS via the exported bridge function.
   */
  public static void explainBlock(String displayText, String contextHint) {
    Ode.getInstance().sendExplainToAIChat(displayText, contextHint);
  }
```

- [ ] **Step 2: Export the methods in JSNI**

In `exportMethodsToJavascript()`, before the closing `}-*/;` (line 723), add:

```javascript
    $wnd.BlocklyPanel_isAIAgentAvailable =
      $entry(@com.google.appinventor.client.editor.blocks.BlocklyPanel::isAIAgentAvailable());
    $wnd.BlocklyPanel_getAIAgentMode =
      $entry(@com.google.appinventor.client.editor.blocks.BlocklyPanel::getAIAgentMode());
    $wnd.BlocklyPanel_explainBlock =
      $entry(@com.google.appinventor.client.editor.blocks.BlocklyPanel::explainBlock(Ljava/lang/String;Ljava/lang/String;));
```

---

### Task 7: Add i18n message keys

**Files:**
- Modify: `appinventor/blocklyeditor/src/msg/ai_blockly/messages.json`
- Modify: `appinventor/blocklyeditor/src/msg/ai_blockly/messages_*.json` (21 locale files)

- [ ] **Step 1: Add keys to English messages.json**

After the `"Blockly.Msg.CLEAR_DO_IT_ERROR"` entry (line 982), add:

```json
	"Blockly.Msg.AI_EXPLAIN_BLOCK": "Explain Block",
	"Blockly.Msg.AI_EXPLAIN_BLOCK_DISABLED": "Explain Block (enable AI Assistant first)",
```

- [ ] **Step 2: Add placeholder keys to all 21 locale files**

Add the same two entries to each of these files (English text as translation placeholder):
`messages_ca.json`, `messages_de.json`, `messages_es.json`, `messages_fr.json`, `messages_he.json`, `messages_hu.json`, `messages_hy.json`, `messages_it.json`, `messages_ja.json`, `messages_ko.json`, `messages_lt.json`, `messages_nl.json`, `messages_pl.json`, `messages_pt.json`, `messages_pt_BR.json`, `messages_ru.json`, `messages_sv.json`, `messages_tr.json`, `messages_uk.json`, `messages_zh_CN.json`, `messages_zh_TW.json`.

Each file: find the `CLEAR_DO_IT_ERROR` entry and add the two new keys after it (same pattern as step 1).

---

### Task 8: Add context menu item in blocklyeditor.js

**Files:**
- Modify: `appinventor/blocklyeditor/src/blocklyeditor.js`

This is the main task — the Blockly JS context menu item with prompt construction logic.

- [ ] **Step 1: Add the buildExplainPrompt helper**

After the `RegisterClearDoItOption` function (after line 246), add:

```javascript
/**
 * Builds a display prompt and context hint for the "Explain Block" action.
 * @param {Blockly.Block} block The block to explain.
 * @return {{displayText: string, contextHint: string}}
 */
AI.Blockly.ContextMenuItems.buildExplainPrompt = function(block) {
  var rootBlock = block.getRootBlock();
  var isTopLevel = (block === rootBlock);

  // -- Build display text --
  var displayText;
  if (isTopLevel) {
    displayText = AI.Blockly.ContextMenuItems.describeBlockForExplain_(block, false);
  } else {
    var rootDesc = AI.Blockly.ContextMenuItems.describeBlockForExplain_(rootBlock, true);
    displayText = rootDesc || 'Explain the selected block';
  }

  // -- Build context hint --
  var parts = [];

  // YAIL
  var yail = null;
  try {
    if (isTopLevel) {
      yail = AI.Yail.blockToCode1(block);
    } else {
      yail = AI.Yail.blockToCode(block);
    }
    if (yail instanceof Array) yail = yail[0];
  } catch (e) {
    // YAIL generation failed
  }

  if (block.isBadBlock()) {
    parts.push('This block has errors and cannot generate valid YAIL.');
  } else if (yail) {
    parts.push('YAIL of the selected block:\n' + yail);
  } else {
    parts.push('YAIL generation failed for this block.');
  }

  // Block state
  if (!block.isEnabled()) {
    parts.push('This block is disabled.');
  }

  // Warnings
  var warningIcon = block.getIcon(Blockly.icons.WarningIcon.TYPE);
  if (warningIcon) {
    var warningText = warningIcon.getText();
    if (warningText) {
      parts.push('Block warnings: ' + warningText);
    }
  }
  if (block.replError) {
    parts.push('Runtime error: ' + block.replError);
  }

  return {
    displayText: displayText,
    contextHint: parts.join('\n')
  };
};

/**
 * Returns a human-readable description of a block for explain prompts.
 * @param {Blockly.Block} block The block to describe.
 * @param {boolean} isPartOf True if this is for a "part of" prompt (non-root).
 * @return {string} The display text.
 * @private
 */
AI.Blockly.ContextMenuItems.describeBlockForExplain_ = function(block, isPartOf) {
  var prefix = isPartOf ? 'Explain the selected part of the ' : 'Explain the selected ';

  if (block.type === 'component_event') {
    var instance = block.instanceName || block.typeName;
    return prefix + instance + '.' + block.eventName + ' event handler';
  } else if (block.type === 'procedures_defnoreturn' ||
             block.type === 'procedures_defreturn') {
    var procName = block.getFieldValue('NAME');
    return prefix + '"' + procName + '" procedure';
  } else if (block.type === 'global_declaration') {
    var varName = block.getFieldValue('NAME');
    return prefix + '"' + varName + '" global variable';
  }

  if (isPartOf) {
    return null; // Caller falls back to generic text
  }
  return 'Explain the selected block';
};
```

- [ ] **Step 2: Add the registerExplainBlockOption function**

After the `buildExplainPrompt` and `describeBlockForExplain_` functions, add:

```javascript
AI.Blockly.ContextMenuItems.registerExplainBlockOption = function() {
  var explainItem = {
    displayText: function(scope) {
      try {
        var mode = window.parent.BlocklyPanel_getAIAgentMode();
        if (mode === 'Off') {
          return Blockly.Msg['AI_EXPLAIN_BLOCK_DISABLED'];
        }
      } catch (e) {
        // Bridge not available
      }
      return Blockly.Msg['AI_EXPLAIN_BLOCK'];
    },
    callback: function(scope) {
      var prompt = AI.Blockly.ContextMenuItems.buildExplainPrompt(scope.block);
      try {
        window.parent.BlocklyPanel_explainBlock(prompt.displayText, prompt.contextHint);
      } catch (e) {
        // Bridge not available
      }
    },
    preconditionFn: function(scope) {
      if (scope.block.workspace.isFlyout) {
        return 'hidden';
      }
      try {
        if (!window.parent.BlocklyPanel_isAIAgentAvailable()) {
          return 'hidden';
        }
        var mode = window.parent.BlocklyPanel_getAIAgentMode();
        if (mode === 'Off') {
          return 'disabled';
        }
      } catch (e) {
        return 'hidden';
      }
      return 'enabled';
    },
    weight: 99,
    id: 'appinventor_explain_block',
    scopeType: Blockly.ContextMenuRegistry.ScopeType.BLOCK,
  };
  Blockly.ContextMenuRegistry.registry.register(explainItem);
};
```

- [ ] **Step 3: Register in registerAll()**

In `registerAll()`, after `AI.Blockly.ContextMenuItems.RegisterClearDoItOption();` (line 950), add:

```javascript
  AI.Blockly.ContextMenuItems.registerExplainBlockOption();
```

---

### Task 9: Manual testing

No automated tests for this feature — it requires a running App Inventor instance with the AI agent configured.

- [ ] **Step 1: Build and start the dev server**

- [ ] **Step 2: Test visibility states**

1. With AI agent feature flag OFF: right-click a block, verify "Explain Block" does NOT appear
2. With AI agent available but mode OFF: right-click a block, verify "Explain Block (enable AI Assistant first)" appears grayed out
3. With AI agent in Advisor mode: verify "Explain Block" appears and is clickable
4. Right-click in a flyout: verify the item does NOT appear

- [ ] **Step 3: Test top-level block prompts**

1. Right-click a `Button1.Click` event handler → "Explain Block" → verify chat shows "Explain the selected Button1.Click event handler"
2. Right-click a procedure definition → verify chat shows "Explain the selected \"procName\" procedure"
3. Right-click a global variable → verify chat shows "Explain the selected \"varName\" global variable"

- [ ] **Step 4: Test non-root block prompts**

1. Right-click a block inside a `Button1.Click` handler → verify chat shows "Explain the selected part of the Button1.Click event handler"
2. Right-click a block inside a procedure → verify chat shows "Explain the selected part of the \"procName\" procedure"

- [ ] **Step 5: Test edge cases**

1. Right-click a disabled block → verify the explain message is sent and the AI mentions it's disabled
2. Right-click a block with warnings → verify the AI receives warning context
3. Click "Explain Block" while a request is in flight → verify nothing happens (no interruption)
4. Click "Explain Block" when chat is not yet open → verify dialog opens and message is sent
