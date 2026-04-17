# Cancel In-Flight AI Requests — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let users cancel in-flight AI requests with a Stop button that aborts both client polling and server-side LLM streaming.

**Architecture:** Three-layer cancellation: (1) UI swaps the Send button to a Stop button while a request is in flight, (2) client resets orchestrator state and fires a cancel RPC, (3) server sets a Memcache flag that LLM providers check during SSE streaming to abort early. StreamBuffer is the bridge — the same class already used for streaming — extended with `isCancelled()`/`setCancelled()`/`CancelledException`.

**Tech Stack:** Java (GWT client + App Engine server), Memcache, GWT-RPC, HttpURLConnection SSE streaming.

**Spec:** `docs/superpowers/specs/2026-04-07-cancel-in-flight-requests-design.md`

---

### Task 1: Add Memcache cancellation flag to StorageIo

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/storage/StorageIo.java:772` (add after `clearAIStreamBuffer`)
- Modify: `appinventor/appengine/src/com/google/appinventor/server/storage/ObjectifyStorageIo.java:3093` (add after `clearAIStreamBuffer`)

- [ ] **Step 1: Add interface methods to StorageIo**

Add these three methods after `clearAIStreamBuffer` (line 772) in `StorageIo.java`:

```java
  void setAIStreamCancelled(long projectId);
  boolean isAIStreamCancelled(long projectId);
  void clearAIStreamCancelled(long projectId);
```

- [ ] **Step 2: Implement in ObjectifyStorageIo**

Add after `clearAIStreamBuffer` (line 3093) in `ObjectifyStorageIo.java`:

```java
  @Override
  public void setAIStreamCancelled(long projectId) {
    memcache.put(streamKey(projectId, "cancelled"), "true",
        Expiration.byDeltaSeconds(STREAM_TTL_SECONDS));
  }

  @Override
  public boolean isAIStreamCancelled(long projectId) {
    return memcache.get(streamKey(projectId, "cancelled")) != null;
  }

  @Override
  public void clearAIStreamCancelled(long projectId) {
    memcache.delete(streamKey(projectId, "cancelled"));
  }
```

- [ ] **Step 3: Clear cancelled flag in initAIStreamBuffer**

In `ObjectifyStorageIo.java`, inside `initAIStreamBuffer()` (line 3015), add the cancelled key to the cleanup at line 3024 (the `keysToDelete` list that already deletes `"done"`):

```java
      keysToDelete.add(streamKey(projectId, "cancelled"));
```

And also add a deletion after the `memcache.put` calls at line 3028:

```java
    memcache.delete(streamKey(projectId, "cancelled"));
```

- [ ] **Step 4: Commit**

```
feat(ai-agent): add Memcache cancellation flag to StorageIo
```

---

### Task 2: Add cancellation support to StreamBuffer

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/StreamBuffer.java`
- Modify: `appinventor/appengine/tests/com/google/appinventor/server/aiagent/StreamBufferTest.java`

- [ ] **Step 1: Write failing tests**

Add these test methods to `StreamBufferTest.java`:

```java
  public void testSetCancelledAndIsCancelled() {
    buffer.init();
    assertFalse(buffer.isCancelled());
    buffer.setCancelled();
    assertTrue(buffer.isCancelled());
  }

  public void testCheckCancelledThrowsWhenCancelled() {
    buffer.init();
    buffer.setCancelled();
    try {
      buffer.checkCancelled();
      fail("Expected CancelledException");
    } catch (StreamBuffer.CancelledException e) {
      // expected
    }
  }

  public void testCheckCancelledDoesNotThrowWhenNotCancelled() {
    buffer.init();
    try {
      buffer.checkCancelled();
    } catch (StreamBuffer.CancelledException e) {
      fail("Should not throw when not cancelled");
    }
  }

  public void testInitClearsCancelledFlag() {
    buffer.init();
    buffer.setCancelled();
    assertTrue(buffer.isCancelled());
    buffer.init();
    assertFalse(buffer.isCancelled());
  }
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `cd appinventor && ant -f appengine/build.xml AiServerLibTests`
Expected: compilation error — `CancelledException`, `isCancelled`, `setCancelled`, `checkCancelled` don't exist yet.

- [ ] **Step 3: Implement StreamBuffer changes**

Add the exception class and three methods to `StreamBuffer.java`, after `markDone()` (line 59):

```java
  /**
   * Unchecked exception thrown when a cancellation is detected.
   * Providers throw this from SSE loops; the engine catches it
   * to return an empty response. Extends RuntimeException so it
   * propagates without modifying the LLMProvider interface or
   * any provider method signatures.
   */
  public static class CancelledException extends RuntimeException {
    public CancelledException() {
      super("Request cancelled by user");
    }
  }

  /** Marks this request as cancelled in Memcache. */
  public void setCancelled() {
    storageIo.setAIStreamCancelled(projectId);
  }

  /** Returns true if this request has been cancelled. */
  public boolean isCancelled() {
    return storageIo.isAIStreamCancelled(projectId);
  }

  /**
   * Throws {@link CancelledException} if this request has been cancelled.
   * Call this at check points in long-running operations.
   */
  public void checkCancelled() {
    if (isCancelled()) {
      throw new CancelledException();
    }
  }
```

Also update `init()` to clear the cancelled flag. Add after line 25 (`storageIo.initAIStreamBuffer(projectId);`):

```java
    storageIo.clearAIStreamCancelled(projectId);
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `cd appinventor && ant -f appengine/build.xml AiServerLibTests`
Expected: all StreamBufferTest tests PASS.

- [ ] **Step 5: Commit**

```
feat(ai-agent): add cancellation support to StreamBuffer
```

---

### Task 3: Add cancellation checks to LLM providers

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/AnthropicCompatibleProvider.java:812`
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/OpenAIChatCompletionsProvider.java:774`
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/OpenAIProvider.java:801`
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/GeminiProvider.java:773`
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/BedrockProvider.java:759`
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/VertexProvider.java:791`
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/OllamaProvider.java:675`

The pattern is the same for all seven providers. Inside each `readStreamingResponse()` method, add a cancellation check as the **first line inside the `while` loop**, before any other processing:

Since `CancelledException` extends `RuntimeException` (see Task 2), no `throws` clause changes are needed on any provider or `LLMProvider` interface methods. The exception propagates automatically through the call chain and is caught explicitly by the engine in Task 4.

For each provider, add the cancellation check as the **first statement** inside the `while ((line = reader.readLine()) != null)` loop in `readStreamingResponse()`, and add the import `import com.google.appinventor.server.aiagent.StreamBuffer;`.

The cancellation check code for all providers:

```java
        if (streamBuffer.isCancelled()) {
          throw new StreamBuffer.CancelledException();
        }
```

- [ ] **Step 1: AnthropicCompatibleProvider** — line 812

- [ ] **Step 2: OpenAIChatCompletionsProvider** — line 774

- [ ] **Step 3: OpenAIProvider** — line 801

- [ ] **Step 4: GeminiProvider** — line 773

- [ ] **Step 5: BedrockProvider** — line 759

- [ ] **Step 6: VertexProvider** — line 791

- [ ] **Step 7: OllamaProvider** — line 675

- [ ] **Step 8: Build to verify compilation**

Run: `cd appinventor && ant -f appengine/build.xml AiServerLib`
Expected: BUILD SUCCESSFUL. The existing `catch (Exception e)` blocks in the engine will catch `CancelledException` at runtime until Task 4 adds explicit handling.

- [ ] **Step 9: Commit**

```
feat(ai-agent): add cancellation checks to all LLM provider SSE loops
```

---

### Task 4: Add server-side cancel handling in engine and servlet

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/AIAgentEngine.java`
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/AIAgentServiceImpl.java`
- Modify: `appinventor/appengine/src/com/google/appinventor/shared/rpc/aiagent/AIAgentService.java`
- Modify: `appinventor/appengine/src/com/google/appinventor/shared/rpc/aiagent/AIAgentServiceAsync.java`

- [ ] **Step 1: Add cancelRequest to AIAgentService interface**

Add after `getRequestStatus` (line 68) in `AIAgentService.java`:

```java
  /**
   * Cancel an in-flight AI request for a project. Sets a cancellation flag
   * in Memcache that the LLM provider checks during streaming to abort early.
   * Best-effort: the request may complete before the flag is checked.
   *
   * @param projectId the project whose request should be cancelled
   */
  void cancelRequest(long projectId);
```

- [ ] **Step 2: Add async mirror to AIAgentServiceAsync**

Add after `getRequestStatus` in `AIAgentServiceAsync.java`:

```java
  /**
   * @see AIAgentService#cancelRequest(long)
   */
  void cancelRequest(long projectId, AsyncCallback<Void> callback);
```

- [ ] **Step 3: Add cancelRequest to AIAgentEngine**

Add after `getRequestStatus` (line 524) in `AIAgentEngine.java`:

```java
  /**
   * Sets the cancellation flag for an in-flight request. The flag is stored
   * in Memcache and checked by LLM providers during streaming.
   */
  public void cancelRequest(long projectId) {
    new StreamBuffer(storageIo, projectId).setCancelled();
  }
```

- [ ] **Step 4: Add CancelledException handling to processRequest**

In `AIAgentEngine.java`, the `processRequest` method (line 132) already has a `try/catch` for `LLMProviderException` (line 242) and `Exception` (line 246). Add a catch for `CancelledException` **before** the `LLMProviderException` catch:

```java
    } catch (StreamBuffer.CancelledException e) {
      LOG.info("Request cancelled by user for project " + projectId);
      // Store synthetic assistant message to keep history role-alternating
      // (user message was already stored before the LLM call).
      AIConversationState conv = conversationManager.getConversation(projectId);
      if (conv != null) {
        conversationManager.storeMessage(conv.getConversationId(),
            MessageRole.ASSISTANT, "[Request cancelled]", false);
      }
      new StreamBuffer(storageIo, projectId).clear();
      return new AIAgentResponse("", Collections.<AIOperation>emptyList(), false,
          Collections.<String>emptyList());
    }
```

Add the import at the top:

```java
import com.google.appinventor.server.aiagent.StreamBuffer.CancelledException;
```

- [ ] **Step 5: Add CancelledException handling to continueRequest**

Same pattern in `continueRequest` (line 269). Add before the `LLMProviderException` catch at line 357:

```java
    } catch (StreamBuffer.CancelledException e) {
      LOG.info("Continuation cancelled by user for project " + projectId);
      // Store synthetic assistant message to keep history role-alternating.
      // Stateless providers (Anthropic) require strict role alternation.
      if (conv != null) {
        conversationManager.storeMessage(conv.getConversationId(),
            MessageRole.ASSISTANT, "[Request cancelled]", false);
      }
      streamBuffer.clear();
      return new AIAgentResponse("", Collections.<AIOperation>emptyList(), false,
          Collections.<String>emptyList());
    }
```

Note: `conv` is already in scope from the `conversationManager.getConversation(projectId)` call at the top of `continueRequest`.

- [ ] **Step 6: Add CancelledException handling to reportExecutionErrors**

Same pattern in `reportExecutionErrors` (line 385). Add before the `LLMProviderException` catch at line 477:

```java
    } catch (StreamBuffer.CancelledException e) {
      LOG.info("Error retry cancelled by user for project " + projectId);
      streamBuffer.clear();
      return new AIAgentResponse("", Collections.<AIOperation>emptyList(), false,
          Collections.<String>emptyList());
    }
```

- [ ] **Step 7: Implement cancelRequest in AIAgentServiceImpl**

Add after the `getRequestStatus` method (line 148) in `AIAgentServiceImpl.java`:

```java
  @Override
  public void cancelRequest(long projectId) {
    String userId = userInfoProvider.getUserId();
    try {
      storageIo.assertUserHasProject(userId, projectId);
    } catch (SecurityException e) {
      throw new SecurityException("You do not have access to this project.");
    }
    engine.cancelRequest(projectId);
  }
```

- [ ] **Step 8: Build to verify compilation**

Run: `cd appinventor && ant -f appengine/build.xml AiServerLib`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 9: Commit**

```
feat(ai-agent): add server-side cancel request handling
```

---

### Task 5: Add i18n strings

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/client/OdeMessages.java`

- [ ] **Step 1: Add message strings**

Add after `aiChatShareFeedback()` (line 6048) in `OdeMessages.java`:

```java
  @DefaultMessage("Stop")
  @Description("Text on the stop button in the AI chat dialog, shown while a request is in flight")
  String aiChatStopButton();

  @DefaultMessage("Request cancelled.")
  @Description("System message shown when the user cancels an in-flight AI request")
  String aiChatRequestCancelled();
```

- [ ] **Step 2: Commit**

```
feat(ai-agent): add i18n strings for stop button and cancel message
```

---

### Task 6: Implement client-side cancel in AIResponseOrchestrator

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/AIResponseOrchestrator.java`

- [ ] **Step 1: Add requestInFlight guard to handleResponseWithValidation**

At the top of `handleResponseWithValidation` (line 424), before `List<AIOperation> operations = response.getOperations();`, add:

```java
    // Discard stale RPC responses that arrive after the user cancelled.
    if (!requestInFlight) {
      return;
    }
```

- [ ] **Step 2: Add cancelRequest method**

Add after `cancelInFlight()` (line 388) in `AIResponseOrchestrator.java`:

```java
  /**
   * Cancels the in-flight request: finalizes streaming UI, resets client
   * state, and fires a server-side cancel RPC to abort LLM processing.
   */
  public void cancelRequest() {
    if (!requestInFlight) {
      return;
    }

    // Finalize streaming bubble so partial text is preserved.
    if (streamingActive) {
      callback.finalizeStreamingBubble(null);
      streamingActive = false;
    }

    // Reset client state (stops polling, re-enables UI).
    cancelInFlight();

    // Fire server-side cancel RPC (fire-and-forget).
    long projectId = contextCollector.getCurrentProjectId();
    if (projectId != 0) {
      aiAgentService.cancelRequest(projectId, new OdeAsyncCallback<Void>(
          MESSAGES.aiChatSendError()) {
        @Override
        public void onSuccess(Void result) {
          // Nothing to do — cancellation is best-effort.
        }
      });
    }

    // Show cancellation message.
    callback.addAiMessage(MESSAGES.aiChatRequestCancelled());
  }
```

- [ ] **Step 3: Build to verify compilation**

Run: `cd appinventor && ant -f appengine/build.xml AiClientLib`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```
feat(ai-agent): add cancelRequest to orchestrator with server RPC
```

---

### Task 7: Implement Send↔Stop button swap in AIChatDialog

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/AIChatDialog.java`

- [ ] **Step 1: Update setRequestInFlight to swap button**

Replace the `setRequestInFlight` method (lines 502-506) in `AIChatDialog.java`:

```java
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
```

Note: the Send button stays enabled in both states — its handler changes behavior.

- [ ] **Step 2: Update doSendMessage to handle Stop clicks**

Replace the `doSendMessage` method (lines 402-419) in `AIChatDialog.java`:

```java
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
```

- [ ] **Step 3: Build to verify compilation**

Run: `cd appinventor && ant -f appengine/build.xml AiClientLib`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```
feat(ai-agent): implement Send/Stop button swap in AI chat dialog
```

---

### Task 8: Update CONTRIBUTING_AI.md

**Files:**
- Modify: `CONTRIBUTING_AI.md`

- [ ] **Step 1: Document the cancel mechanism**

Add a new section after "Rejection Flow" (around line 787) in `CONTRIBUTING_AI.md`:

```markdown
### Cancellation Flow

When the user clicks Stop during an in-flight request:

1. **Client**: `AIResponseOrchestrator.cancelRequest()` finalizes any active streaming bubble, calls `cancelInFlight()` to reset state, fires `cancelRequest(projectId)` RPC to the server, and shows "Request cancelled."
2. **Server**: `AIAgentServiceImpl.cancelRequest()` sets a Memcache flag via `StreamBuffer.setCancelled()`.
3. **LLM providers**: Each provider's SSE streaming loop checks `streamBuffer.isCancelled()` on every event. If cancelled, throws `StreamBuffer.CancelledException`.
4. **Engine**: Catches `CancelledException`, stores a synthetic `"[Request cancelled]"` assistant message to keep history role-alternating, clears the stream buffer, and returns an empty response.
5. **Client guard**: If the RPC response arrives after cancellation, `handleResponseWithValidation` checks `!requestInFlight` and silently discards it.

Cancellation is best-effort: if the LLM call completes before the flag is checked, the response arrives normally but is discarded client-side.
```

- [ ] **Step 2: Commit**

```
docs: document cancel mechanism in CONTRIBUTING_AI.md
```

---

### Task 9: Run full test suite and verify build

- [ ] **Step 1: Run all AI agent tests**

```bash
cd appinventor && ant -f appengine/build.xml AiServerLibTests
```

Expected: all tests PASS, including the new StreamBuffer cancellation tests.

- [ ] **Step 2: Run full client build**

```bash
cd appinventor && ant -f appengine/build.xml AiClientLib
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Verify no regressions**

```bash
cd appinventor && ant -f appengine/build.xml tests
```

Expected: all tests PASS.
