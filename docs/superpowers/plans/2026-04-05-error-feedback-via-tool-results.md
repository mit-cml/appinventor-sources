# Error Feedback via Tool Results — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Route execution/validation error feedback through native tool results instead of user messages, eliminating conversational filler ("Thanks — fixing now") from LLM responses.

**Architecture:** The server patches per-tool-call results in the continuation state with client execution outcomes and calls `continueWithToolResults()` instead of `chat()`. All 7 provider implementations add context message injection. Client-side executor adds idempotency guards for retry safety.

**Tech Stack:** Java (server + GWT client), JSON manipulation, LLM provider APIs (Anthropic, OpenAI, Gemini, Bedrock, Ollama)

**Spec:** `docs/superpowers/specs/2026-04-05-error-feedback-via-tool-results-design.md`

---

### Task 1: Update `LLMProvider` Interface

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/LLMProvider.java:66-67`

- [ ] **Step 1: Update the `continueWithToolResults` signature**

Change lines 66-67 from:
```java
LLMResponse continueWithToolResults(String continuationState, List<LLMTool> tools,
    ReadOnlyToolResolver resolver, StreamBuffer streamBuffer) throws LLMProviderException;
```

To:
```java
LLMResponse continueWithToolResults(String continuationState, List<LLMTool> tools,
    List<String> contextMessages, ReadOnlyToolResolver resolver,
    StreamBuffer streamBuffer) throws LLMProviderException;
```

Add `import java.util.List;` if not already present (it should be — `List<LLMTool>` already uses it).

- [ ] **Step 2: Verify the build fails**

Run: `ant -f appengine/build.xml AiServerLib 2>&1 | tail -20`
Expected: FAIL — all 7 provider implementations now have the wrong signature.

- [ ] **Step 3: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/LLMProvider.java
git commit -m "feat(ai-agent): add contextMessages to continueWithToolResults interface"
```

---

### Task 2: Update Provider Implementations — Context Message Injection

All 7 providers need the same pattern: accept `contextMessages`, inject them after tool results.

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/AnthropicCompatibleProvider.java:284-440`
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/OpenAIChatCompletionsProvider.java:251-390`
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/OpenAIProvider.java:285-428`
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/GeminiProvider.java:280-454`
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/VertexProvider.java:289-468`
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/BedrockProvider.java:205-347`
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/OllamaProvider.java:209-340`

- [ ] **Step 1: Update AnthropicCompatibleProvider**

In `continueWithToolResults()` (line 284), add `List<String> contextMessages` parameter after `tools`.

After the tool results are appended to the messages array (after line 318), inject context messages using the same pattern from `buildMessages()` (lines 494-506):

```java
// Inject fresh context messages after tool results
if (contextMessages != null) {
  for (String ctx : contextMessages) {
    if (ctx != null && !ctx.isEmpty()) {
      messages.put(new JSONObject()
          .put("role", "user")
          .put("content", ctx));
      messages.put(new JSONObject()
          .put("role", "assistant")
          .put("content", "Understood."));
    }
  }
}
```

- [ ] **Step 2: Update OpenAIChatCompletionsProvider**

Same pattern. In `continueWithToolResults()` (line 251), add `List<String> contextMessages` parameter.

After tool result messages are appended (after line 280), inject context messages using the pattern from `buildMessages()` (lines 449-461):

```java
if (contextMessages != null) {
  for (String ctx : contextMessages) {
    if (ctx != null && !ctx.isEmpty()) {
      messages.put(new JSONObject()
          .put("role", "user")
          .put("content", ctx));
      messages.put(new JSONObject()
          .put("role", "assistant")
          .put("content", "Understood."));
    }
  }
}
```

- [ ] **Step 3: Update OpenAIProvider (Responses API)**

In `continueWithToolResults()` (line 285), add `List<String> contextMessages` parameter.

After `function_call_output` items are added to `toolResultsInput` (after line 316), inject context messages as `role: "user"` items — same pattern from `chat()` (lines 123-131):

```java
if (contextMessages != null) {
  for (String ctx : contextMessages) {
    if (ctx != null && !ctx.isEmpty()) {
      toolResultsInput.put(new JSONObject()
          .put("role", "user")
          .put("content", ctx));
    }
  }
}
```

Note: The OpenAI Responses API is stateful. If it rejects interleaved user content after function_call_output items during testing, wrap this injection in a try/catch and log a warning, falling back to no context messages. See spec Section 6 caveat.

- [ ] **Step 4: Update GeminiProvider**

In `continueWithToolResults()` (line 280), add `List<String> contextMessages` parameter.

After the functionResponse parts are added to the contents array (after line 312), inject context messages as user/model turns — same pattern from `buildContents()` (lines 496-509):

```java
if (contextMessages != null) {
  for (String ctx : contextMessages) {
    if (ctx != null && !ctx.isEmpty()) {
      JSONArray ctxParts = new JSONArray();
      ctxParts.put(new JSONObject().put("text", ctx));
      contents.put(new JSONObject()
          .put("role", "user")
          .put("parts", ctxParts));
      JSONArray ackParts = new JSONArray();
      ackParts.put(new JSONObject().put("text", "Understood."));
      contents.put(new JSONObject()
          .put("role", "model")
          .put("parts", ackParts));
    }
  }
}
```

Same stateful provider caveat as OpenAIProvider — test and fall back if needed.

- [ ] **Step 5: Update VertexProvider**

Same pattern as GeminiProvider. In `continueWithToolResults()` (line 289), add `List<String> contextMessages` parameter. Inject context after tool results (after line 321) using user/model turns — same as GeminiProvider step.

- [ ] **Step 6: Update BedrockProvider**

In `continueWithToolResults()` (line 205), add `List<String> contextMessages` parameter.

After toolResult blocks are added to the messages array (after line 237), inject context using the same pattern from `buildMessages()` (lines 399-413) — user/assistant turns with Bedrock Converse API content format (content must be a JSONArray of text blocks, not a plain string):

```java
if (contextMessages != null) {
  for (String ctx : contextMessages) {
    if (ctx != null && !ctx.isEmpty()) {
      messages.put(new JSONObject()
          .put("role", "user")
          .put("content", new JSONArray().put(
              new JSONObject().put("text", ctx))));
      messages.put(new JSONObject()
          .put("role", "assistant")
          .put("content", new JSONArray().put(
              new JSONObject().put("text", "Understood."))));
    }
  }
}
```

- [ ] **Step 7: Update OllamaProvider**

Same pattern as OpenAIChatCompletionsProvider. In `continueWithToolResults()` (line 209), add parameter. Inject context after tool result messages (after line 237) using user/assistant turns.

- [ ] **Step 8: Verify build succeeds**

Run: `ant -f appengine/build.xml AiServerLib 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

- [ ] **Step 9: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/
git commit -m "feat(ai-agent): inject context messages in continueWithToolResults across all providers"
```

---

### Task 3: Update `continueRequest` to Pass Context Messages

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/AIAgentEngine.java:269-365`

- [ ] **Step 1: Build context messages in `continueRequest`**

`continueRequest` already builds context messages for the narration retry path (lines 332-336). Move that context building to happen unconditionally before the `continueWithToolResults` call.

At line ~291 (after getting the provider and resolver), add:

```java
// Build fresh context messages so the LLM sees post-execution state
List<String> contextMessages = contextBuilder.buildContextMessages(
    userId, projectId, screenName, mode, blocksYail, currentView,
    screenComponentsJson, projectSnapshot, blockWarnings,
    locale, languageDisplayName);
```

- [ ] **Step 2: Pass context messages to `continueWithToolResults`**

Change lines 309-310 from:
```java
LLMResponse llmResponse = provider.continueWithToolResults(
    providerRef, tools, resolver, streamBuffer);
```

To:
```java
LLMResponse llmResponse = provider.continueWithToolResults(
    providerRef, tools, contextMessages, resolver, streamBuffer);
```

- [ ] **Step 3: Update narration retry context**

The narration retry code at lines 332-336 conditionally builds context messages for stateless providers. Since we now build them unconditionally above, remove the duplicate and use the already-built `contextMessages` variable.

- [ ] **Step 4: Verify build succeeds**

Run: `ant -f appengine/build.xml AiServerLib 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/server/aiagent/AIAgentEngine.java
git commit -m "feat(ai-agent): pass fresh context messages through continueRequest path"
```

---

### Task 4: Redesign `reportExecutionErrors` — Tool Result Patching

This is the core change. Replace the `chat()` call with `continueWithToolResults()`.

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/AIAgentEngine.java:382-493`

- [ ] **Step 1: Add the `patchToolCallResults` helper method**

Add a new static method to `AIAgentEngine` (after `annotateToolCallResults` at line 826):

```java
/**
 * Patches the toolCallResults in the continuation state with client-side
 * execution outcomes. Iterates the existing toolCallResults, skipping
 * REJECTED entries (already resolved server-side), and maps each ACCEPTED
 * entry to the corresponding client result by position.
 *
 * @param providerRef   the serialized continuation state
 * @param clientResults the client's execution/validation results
 * @return the patched continuation state JSON string, or null if patching
 *         is not possible (missing toolCallResults)
 */
static String patchToolCallResults(String providerRef,
    List<AIOperationResult> clientResults) {
  if (providerRef == null || providerRef.isEmpty()) {
    return null;
  }
  try {
    JSONObject state = new JSONObject(providerRef);
    JSONArray toolCallResults = state.optJSONArray("toolCallResults");
    if (toolCallResults == null) {
      return null;
    }

    int clientIdx = 0;
    for (int i = 0; i < toolCallResults.length(); i++) {
      JSONObject entry = toolCallResults.getJSONObject(i);
      String currentResult = entry.optString("result", "");
      if (currentResult.startsWith("REJECTED:")) {
        // Already set by server-side validation. Leave as-is.
        continue;
      }
      // This is an ACCEPTED entry — map to the next client result.
      if (clientIdx < clientResults.size()) {
        AIOperationResult cr = clientResults.get(clientIdx);
        switch (cr.getStatus()) {
          case SUCCEEDED:
            entry.put("result", "Applied successfully.");
            break;
          case FAILED:
            String detail = cr.getErrorDetail() != null
                ? cr.getErrorDetail() : "Unknown error";
            entry.put("result", "FAILED: " + detail);
            break;
          case SKIPPED:
            entry.put("result",
                "SKIPPED: execution halted after a prior failure.");
            break;
        }
        clientIdx++;
      }
    }
    state.put("toolCallResults", toolCallResults);
    return state.toString();
  } catch (Exception e) {
    LOG.warning("Failed to patch tool call results: " + e.getMessage());
    return null;
  }
}
```

- [ ] **Step 2: Rewrite `reportExecutionErrors` method body**

Replace the method body (lines ~397-487) with the new flow. Key changes:

1. Remove the extraction of succeededSummaries/failedDetails/skippedSummaries
2. Remove the call to `buildExecutionErrorFeedback()`
3. Remove `<system>` wrapping and `storeMessage()` of feedback
4. Replace `provider.chat()` with patching + `continueWithToolResults()`

```java
// (Inside reportExecutionErrors, after the stream buffer setup and conv loading)

// Patch tool results with client execution outcomes
String patchedRef = patchToolCallResults(conv.getProviderRef(), results);
if (patchedRef == null) {
  streamBuffer.clear();
  return errorResponse(
      "No continuation state available for retry. Please start a new request.");
}

// Patch system prompt
String systemPrompt = contextBuilder.build();
patchedRef = patchSystemPrompt(patchedRef, systemPrompt);

// Build fresh context messages with current editor state
List<String> contextMessages = contextBuilder.buildContextMessages(
    userId, projectId, screenName, mode, blocksYail, currentView,
    screenComponentsJson, projectSnapshot, blockWarnings,
    locale, languageDisplayName);

// Get provider and tools
LLMProvider provider = LLMProviderRegistry.get(conv.getProviderName());
List<LLMTool> tools = contextBuilder.buildTools(mode, currentView);
ReadOnlyToolResolver resolver = toolResolver.createResolver(userId, projectId);

AIDebug.log(LOG, "Retrying via continueWithToolResults with patched results");

streamBuffer.appendStatus("Calling AI (" + retryInfo + ")...");

// Retry via tool results — no user message stored
LLMResponse llmResponse = provider.continueWithToolResults(
    patchedRef, tools, contextMessages, resolver, streamBuffer);
```

- [ ] **Step 3: Debug-log the patched results**

After `patchToolCallResults`, add:
```java
if (AIDebug.enabled()) {
  AIDebug.log(LOG, "Patched tool call results: " + patchedRef.substring(
      Math.max(0, patchedRef.indexOf("toolCallResults")),
      Math.min(patchedRef.length(),
          patchedRef.indexOf("toolCallResults") + 500)));
}
```

- [ ] **Step 4: Verify build succeeds**

Run: `ant -f appengine/build.xml AiServerLib 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/server/aiagent/AIAgentEngine.java
git commit -m "feat(ai-agent): route error feedback through tool results instead of chat()"
```

---

### Task 5: Support Validation Retry Semantics

The `patchToolCallResults` helper from Task 4 uses `"Applied successfully."` for SUCCEEDED. For validation retries, preserved-valid operations should use `"Validated successfully. Pending application."` instead.

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/AIAgentEngine.java`
- Modify: `appinventor/appengine/src/com/google/appinventor/shared/rpc/aiagent/AIAgentRequest.java`

- [ ] **Step 1: Add a flag to distinguish validation from execution retries**

The client already sends `retryAttempt` on the request. We need to know whether this is a validation retry or an execution retry to choose the right SUCCEEDED string.

Option: check whether any result has `SKIPPED` status — validation retries never produce SKIPPED (the spec says "validation checks all block operations independently, it does not halt on first failure"). If any result is SKIPPED, it's an execution retry. Otherwise it's a validation retry.

In `patchToolCallResults`, add an `isValidationRetry` parameter:

```java
static String patchToolCallResults(String providerRef,
    List<AIOperationResult> clientResults, boolean isValidationRetry) {
```

And in the SUCCEEDED case:
```java
case SUCCEEDED:
  entry.put("result", isValidationRetry
      ? "Validated successfully. Pending application."
      : "Applied successfully.");
  break;
```

- [ ] **Step 2: Determine isValidationRetry at the call site**

In `reportExecutionErrors`, before calling `patchToolCallResults`:

```java
boolean isValidationRetry = true;
for (AIOperationResult r : results) {
  if (r.getStatus() == AIOperationResult.Status.SKIPPED) {
    isValidationRetry = false;
    break;
  }
}
```

Pass it to `patchToolCallResults(conv.getProviderRef(), results, isValidationRetry)`.

- [ ] **Step 3: Verify build succeeds**

Run: `ant -f appengine/build.xml AiServerLib 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/server/aiagent/AIAgentEngine.java
git commit -m "feat(ai-agent): distinguish validation vs execution retry in tool result strings"
```

---

### Task 6: Cleanup — Remove Dead Code

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/LLMResponseParser.java:203-241`
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/AIAgentEngine.java`

- [ ] **Step 1: Remove `buildExecutionErrorFeedback` from `LLMResponseParser`**

Delete the method at lines 203-241 of `LLMResponseParser.java`.

- [ ] **Step 2: Remove unused imports in `AIAgentEngine`**

After the `reportExecutionErrors` rewrite, the following are likely unused:
- The import for `AIAgentRequest` (if `wrapPlatformMessage` is no longer called from this file — check if narration nudge still uses it)
- Any other imports that only `buildExecutionErrorFeedback` required

Run the build to find unused imports — the compiler won't flag them, but IDE or checkstyle might. If not, leave them.

- [ ] **Step 3: Verify build succeeds**

Run: `ant -f appengine/build.xml AiServerLib 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/server/aiagent/LLMResponseParser.java
git add appinventor/appengine/src/com/google/appinventor/server/aiagent/AIAgentEngine.java
git commit -m "refactor(ai-agent): remove buildExecutionErrorFeedback and dead code"
```

---

### Task 7: Client-Side Idempotency Guards

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/AIEditorState.java`
- Modify: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/executor/AIOperationExecutor.java:82-323`

- [ ] **Step 1: Add existence-check helpers to `AIEditorState`**

`AIEditorState` is a static utility class. Add three static methods for the idempotency checks. Follow existing patterns in the validators (e.g., `DesignerOperationValidator.validateAddComponent` checks component existence, `ProjectOperationValidator` checks screen existence):

```java
/**
 * Returns true if a component with the given name exists on the current screen.
 */
public static boolean componentExists(String name) {
  YaFormEditor formEditor = getCurrentFormEditor();
  if (formEditor == null) return false;
  return formEditor.getComponents().containsKey(name);
}

/**
 * Returns true if a top-level block definition with the given identifier
 * exists on the current screen.
 */
public static boolean blockExists(String blockId) {
  YaBlocksEditor blocksEditor = getCurrentBlocksEditor();
  if (blocksEditor == null) return false;
  return blocksEditor.hasBlock(blockId);
}

/**
 * Returns true if a screen with the given name exists in the project.
 */
public static boolean screenExists(String screenName) {
  DesignProject project = getCurrentProject();
  if (project == null) return false;
  return project.screens.containsKey(screenName);
}
```

Note: `hasBlock()` may not exist on `YaBlocksEditor` — check during implementation. If not, it can be added as a thin wrapper around the Blockly workspace query, or use the YAIL source check. The key is to determine whether a block with that identifier is present.

- [ ] **Step 2: Add `isIdempotentSkip` method to `AIOperationExecutor`**

Add a private method that checks whether an operation can be silently skipped because its effect is already present. Place it before `dispatchSyncOp` (before line 297). All calls are static on `AIEditorState`:

```java
/**
 * Returns true if this operation's intended effect is already reflected
 * in the current editor state (idempotent re-emission during a retry).
 * Skipped operations should be reported as SUCCEEDED.
 */
private boolean isIdempotentSkip(AIOperation op) {
  com.google.gwt.json.client.JSONObject json =
      JSONParser.parseStrict(op.getPayload()).isObject();

  switch (op.getType()) {
    case ADD_COMPONENT: {
      String name = json.get("name").isString().stringValue();
      // Match by name only — type/parent/properties are not compared.
      return AIEditorState.componentExists(name);
    }
    case DELETE_COMPONENT: {
      String name = json.get("name").isString().stringValue();
      return !AIEditorState.componentExists(name);
    }
    case RENAME_COMPONENT: {
      String oldName = json.get("old_name").isString().stringValue();
      String newName = json.get("new_name").isString().stringValue();
      boolean oldExists = AIEditorState.componentExists(oldName);
      boolean newExists = AIEditorState.componentExists(newName);
      // Already renamed: old is gone, new is present
      return !oldExists && newExists;
    }
    case DELETE_BLOCK: {
      String block = json.get("block").isString().stringValue();
      return !AIEditorState.blockExists(block);
    }
    case CREATE_SCREEN: {
      String screenName = json.get("screen_name").isString().stringValue();
      return AIEditorState.screenExists(screenName);
    }
    case DELETE_SCREEN: {
      String screenName = json.get("screen_name").isString().stringValue();
      return !AIEditorState.screenExists(screenName);
    }
    default:
      // SET_PROPERTY, WRITE_BLOCK, SWITCH_SCREEN, TOGGLE_EDITOR,
      // SET_PROJECT_PROP are inherently idempotent — no skip needed.
      return false;
  }
}
```

- [ ] **Step 3: Insert the idempotency check before validation**

In the sync phase execution loop (around line 266-280), right before the `AIOperationValidator.validate(op)` call at line 269, add:

```java
// Idempotent re-emission check — skip if effect already present
if (isIdempotentSkip(op)) {
  LOG.fine("Idempotent skip: " + op.getType() + " already applied");
  state.markSucceeded(op);
  continue;
}
```

Do the same in the phase 1 (async) execution at line 188, before `AIOperationValidator.validate(op)`:

```java
if (isIdempotentSkip(op)) {
  LOG.fine("Idempotent skip: " + op.getType() + " already applied");
  state.markSucceeded(op);
  // Continue to next phase1 op
  runPhase1(state, phase1, index + 1, phase2, phase3, phase4, phase5);
  return;
}
```

- [ ] **Step 4: Verify build succeeds**

Run: `ant -f appengine/build.xml AiClientLib 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/AIEditorState.java
git add appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/executor/AIOperationExecutor.java
git commit -m "feat(ai-agent): add idempotency guards to prevent cascading retry failures"
```

---

### Task 8: Prompt Reinforcement

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/resources/appinventor_reference.md:51-61`

- [ ] **Step 1: Add tool result instruction to "Action, Not Narration" subsection**

After the existing content in the "Action, Not Narration" subsection (around line 61), add:

```markdown
### Tool Result Handling

When a tool result starts with "Applied successfully" or "Validated
successfully", do not re-emit that tool call. Only re-emit tool calls whose
results indicate failure (starting with "FAILED:") or that were skipped
(starting with "SKIPPED:").
```

- [ ] **Step 2: Verify build picks up the resource change**

Run: `ant -f appengine/build.xml AiServerLib 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL (resource files are copied during build)

- [ ] **Step 3: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/server/aiagent/resources/appinventor_reference.md
git commit -m "feat(ai-agent): add tool result handling instruction to system prompt"
```

---

### Task 9: Update CONTRIBUTING_AI.md

**Files:**
- Modify: `CONTRIBUTING_AI.md` — sections listed in spec Section 9

- [ ] **Step 1: Update "Error Handling and Retry" section (line 679+)**

Update the flowchart and description: `reportExecutionErrors` now patches `toolCallResults` and calls `continueWithToolResults()` instead of building text feedback and calling `chat()`.

- [ ] **Step 2: Update "Server-Side Retry Handling" subsection (line 752+)**

Rewrite to describe:
- The `patchToolCallResults` helper
- The three new outcome strings (`"Applied successfully."`, `"FAILED: ..."`, `"SKIPPED: ..."`)
- The validation-specific `"Validated successfully. Pending application."` string
- The `continueWithToolResults()` call path

Remove references to `buildExecutionErrorFeedback()` and `<system>` wrapping.

- [ ] **Step 3: Update "Request Lifecycle" diagram (line 474+)**

Update the error retry path in the sequence diagram. Change:
```
Client -> Server: reportExecutionErrors(results)
Server -> LLM: chat(feedback as user message)
```
To:
```
Client -> Server: reportExecutionErrors(results)
Server: patch toolCallResults in continuation state
Server -> LLM: continueWithToolResults(patchedState, contextMessages)
```

- [ ] **Step 4: Update "LLM Provider System" section (line 789+)**

Update the `continueWithToolResults()` signature in the "Adding a New Provider" guide. Document the `contextMessages` parameter and context injection pattern.

- [ ] **Step 5: Update "Validation Pipeline" section (line 402+)**

Update the Stage 1 flowchart: validation retries now go through `continueWithToolResults()` instead of `chat()`.

- [ ] **Step 6: Update "Conventions" section (line 994+)**

Add convention: execution and validation errors are communicated via native tool results, not user messages.

- [ ] **Step 7: Commit**

```bash
git add CONTRIBUTING_AI.md
git commit -m "docs(ai-agent): update CONTRIBUTING_AI.md for tool-result-based error feedback"
```

---

### Task 10: Manual Testing

- [ ] **Step 1: Test full success path**

Send a message that triggers multiple tool calls (e.g., "Add a button with a click handler"). Verify all operations execute, continuation works, and the LLM sees `"Done."` tool results. No behavioral change expected.

- [ ] **Step 2: Test execution failure**

Send a message that triggers a `write_block` with a syntax error (or force a failure). Verify:
- The error feedback goes through `continueWithToolResults()`, NOT `chat()`
- Check debug logs: no `"[Execution error feedback]"` user message stored
- The LLM's retry response does NOT contain conversational filler ("Thanks", "Got it")
- The corrected operations are returned normally

- [ ] **Step 3: Test validation failure**

Force a block YAIL validation failure (e.g., by temporarily making the validator reject a specific pattern). Verify:
- Preserved valid ops get `"Validated successfully. Pending application."`
- Failed ops get `"FAILED: ..."` with the failing YAIL code
- The LLM retries with corrected block only

- [ ] **Step 4: Test idempotency**

If the LLM re-emits an `add_component` for a component that already exists during a retry, verify:
- The executor skips it silently (FINE-level log)
- It's reported as SUCCEEDED
- No cascading failure

- [ ] **Step 5: Test missing continuation state**

Clear memcache or wait for TTL expiry, then trigger an error retry. Verify the server returns a user-facing error ("No continuation state available") instead of crashing.
