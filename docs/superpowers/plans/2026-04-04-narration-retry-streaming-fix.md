# Narration Retry Streaming Fix Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix the bug where streamed narration text persists in the chat bubble after a narration retry replaces it with different content, by adding a "clear streaming" signal through the stream buffer protocol.

**Architecture:** Add a `"r:"` (reset) chunk type to the existing stream buffer protocol. When the server begins a narration retry, it emits a reset signal after clearing the buffer. The client's polling handler receives this signal and resets its streaming bubble (clears accumulated text, thinking, and the typing indicator), so the retry's text streams into a clean slate. The `finalizeStreamingBubble()` call on RPC completion then receives text matching what the user saw.

**Tech Stack:** Java (GWT client + App Engine server), Memcache stream buffer protocol

---

## File Map

| File | Action | Responsibility |
|------|--------|---------------|
| `server/aiagent/StreamBuffer.java` | Modify | Add `resetStreaming()` method emitting `"r:"` chunk |
| `shared/rpc/aiagent/AIStreamStatus.java` | Modify | Add `resetStreaming` boolean field |
| `server/aiagent/AIAgentEngine.java` | Modify | Call `streamBuffer.resetStreaming()` in `retryIfNarration()` |
| `client/.../aiagent/AIResponseOrchestrator.java` | Modify | Handle `resetStreaming` in polling callback |
| `client/.../aiagent/AIChatRenderer.java` | Modify | Add `resetStreamingBubble()` method |
| `tests/.../aiagent/StreamBufferTest.java` | Modify | Add test for reset chunk |

All paths are relative to `appinventor/appengine/src/com/google/appinventor/`.

---

### Task 1: Add reset signal to StreamBuffer and AIStreamStatus

**Files:**
- Modify: `server/aiagent/StreamBuffer.java:28-47`
- Modify: `shared/rpc/aiagent/AIStreamStatus.java`
- Modify: `server/aiagent/StreamBuffer.java:64-94` (consume method)
- Modify: `tests/.../aiagent/StreamBufferTest.java`

- [ ] **Step 1: Write the failing test**

Add to `StreamBufferTest.java`:

```java
public void testResetStreamingSignal() {
  buffer.init();
  buffer.appendText("narration text");
  buffer.resetStreaming();
  buffer.appendStatus("Preparing response...");
  AIStreamStatus status = buffer.consume();
  assertTrue(status.isResetStreaming());
  assertEquals("Preparing response...", status.getStatusText());
  // Text before reset is cleared; only the reset signal matters
  assertNull(status.getTextDelta());
}

public void testResetStreamingNotSetByDefault() {
  buffer.init();
  buffer.appendText("normal text");
  AIStreamStatus status = buffer.consume();
  assertFalse(status.isResetStreaming());
  assertEquals("normal text", status.getTextDelta());
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd appinventor && ant -f appengine/build.xml AiServerLibTests`
Expected: Compilation failure — `resetStreaming()` and `isResetStreaming()` do not exist yet.

- [ ] **Step 3: Add `resetStreaming` field to `AIStreamStatus`**

In `AIStreamStatus.java`, add:

```java
private boolean resetStreaming;
```

Update the constructor to accept the new field:

```java
public AIStreamStatus(String statusText, String textDelta,
    String thinkingDelta, boolean done, boolean resetStreaming) {
  this.statusText = statusText;
  this.textDelta = textDelta;
  this.thinkingDelta = thinkingDelta;
  this.done = done;
  this.resetStreaming = resetStreaming;
}
```

Add getter:

```java
public boolean isResetStreaming() { return resetStreaming; }
```

- [ ] **Step 4: Add `resetStreaming()` method to `StreamBuffer`**

In `StreamBuffer.java`, add after `appendStatus()`:

```java
/**
 * Emit a reset signal that tells the client to clear its streaming
 * bubble (accumulated text, thinking, and typing indicator).
 * Used before a narration retry so the retry streams into a clean slate.
 */
public void resetStreaming() {
  storageIo.appendAIStreamChunk(projectId, "r:");
}
```

- [ ] **Step 5: Update `consume()` to parse `"r:"` chunks**

In `StreamBuffer.consume()`, add a `boolean reset = false;` local variable alongside the existing locals. Add an `else if` branch in the chunk loop:

```java
} else if (chunk.startsWith("r:")) {
  reset = true;
  // A reset invalidates any text/thinking accumulated before it
  textBuilder = null;
  thinkingBuilder = null;
}
```

Update the return statement to pass `reset`:

```java
return new AIStreamStatus(
    lastStatus,
    textBuilder != null ? textBuilder.toString() : null,
    thinkingBuilder != null ? thinkingBuilder.toString() : null,
    done,
    reset
);
```

- [ ] **Step 6: Fix all other `new AIStreamStatus(...)` call sites**

The existing 4-arg constructor is used by the no-arg GWT constructor and potentially other places. Keep the no-arg constructor as-is (GWT serialization). Update all other `new AIStreamStatus(4 args)` calls to pass `false` as the fifth argument. Search for these with: `grep -rn "new AIStreamStatus(" --include="*.java"`. The only production call site is in `StreamBuffer.consume()` (already updated above).

- [ ] **Step 7: Run tests to verify they pass**

Run: `cd appinventor && ant -f appengine/build.xml AiServerLibTests`
Expected: All tests pass including the two new ones.

- [ ] **Step 8: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/server/aiagent/StreamBuffer.java \
       appinventor/appengine/src/com/google/appinventor/shared/rpc/aiagent/AIStreamStatus.java \
       appinventor/appengine/tests/com/google/appinventor/server/aiagent/StreamBufferTest.java
git commit -m "feat(ai-agent): add reset streaming signal to stream buffer protocol

Adds a 'r:' chunk type that tells the client to clear its streaming
bubble before a narration retry. This prevents stale narration text
from persisting when the retry produces different content."
```

---

### Task 2: Emit reset signal in narration retry

**Files:**
- Modify: `server/aiagent/AIAgentEngine.java:669-670`

- [ ] **Step 1: Replace `streamBuffer.init()` with `resetStreaming()` + `init()` in `retryIfNarration()`**

In `AIAgentEngine.retryIfNarration()`, change lines 669-670 from:

```java
streamBuffer.init();
streamBuffer.appendStatus("Preparing response...");
```

To:

```java
streamBuffer.resetStreaming();
streamBuffer.init();
streamBuffer.appendStatus("Preparing response...");
```

The `resetStreaming()` must come **before** `init()` — it appends the `"r:"` chunk to the current buffer so the client sees it on its next poll. Then `init()` clears the buffer for the retry's fresh streaming.

- [ ] **Step 2: Verify build compiles**

Run: `cd appinventor && ant -f appengine/build.xml AiServerLib`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/server/aiagent/AIAgentEngine.java
git commit -m "feat(ai-agent): emit reset signal before narration retry

Sends a streaming reset signal before clearing the buffer for retry,
so the client clears its accumulated narration text."
```

---

### Task 3: Handle reset signal on the client

**Files:**
- Modify: `client/.../aiagent/AIChatRenderer.java`
- Modify: `client/.../aiagent/AIResponseOrchestrator.java:64-77` (ChatCallback interface)
- Modify: `client/.../aiagent/AIResponseOrchestrator.java:728-753` (polling handler)

- [ ] **Step 1: Add `resetStreamingBubble()` to `AIChatRenderer`**

Add after `startStreamingBubble()`:

```java
/**
 * Resets the in-progress streaming bubble, clearing all accumulated text
 * and thinking content. The bubble shell and typing indicator are kept;
 * new streaming deltas will fill it fresh.
 *
 * <p>Called when the server signals a narration retry so the retried
 * response streams into a clean bubble instead of appending to the
 * stale narration text.</p>
 */
public void resetStreamingBubble() {
  streamingTextAccumulator = "";
  streamingThinkingAccumulator = "";
  if (streamingMessageHtml != null) {
    streamingMessageHtml.setHTML("");
  }
  if (streamingThinkingPanel != null) {
    streamingThinkingPanel.removeFromParent();
    streamingThinkingPanel = null;
    streamingThinkingHtml = null;
  }
}
```

- [ ] **Step 2: Add `resetStreamingBubble()` to `ChatCallback` interface**

In `AIResponseOrchestrator.ChatCallback`, add:

```java
void resetStreamingBubble();
```

- [ ] **Step 3: Handle `isResetStreaming()` in the polling callback**

In `AIResponseOrchestrator.startPollingStatus()`, in the `onSuccess` handler, add a check **before** the `getThinkingDelta()` / `getTextDelta()` checks (so the reset clears state before any new deltas in the same poll are applied):

```java
if (status.isResetStreaming()) {
  if (streamingActive) {
    callback.resetStreamingBubble();
  }
  // Streaming state stays active — the retry will continue
  // filling the same bubble with fresh content.
}
```

Insert this block between the `setStatusText` call (line 733-734) and the `getThinkingDelta` check (line 736).

- [ ] **Step 4: Implement `resetStreamingBubble` in `AIChatDialog`**

Find where `AIChatDialog` implements `ChatCallback` and add the delegation to the renderer. This follows the same pattern as the other callback methods (e.g., `startStreamingBubble`, `appendStreamingText`):

```java
@Override
public void resetStreamingBubble() {
  chatRenderer.resetStreamingBubble();
}
```

- [ ] **Step 5: Verify build compiles**

Run: `cd appinventor && ant -f appengine/build.xml AiClientLib`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/AIChatRenderer.java \
       appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/AIResponseOrchestrator.java \
       appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/AIChatDialog.java
git commit -m "feat(ai-agent): handle streaming reset on client

Clears accumulated narration text and thinking content when the server
signals a retry, so the retried response streams into a clean bubble."
```

---

### Task 4: Manual verification

- [ ] **Step 1: Full build**

Run: `cd appinventor && ant -f appengine/build.xml AiServerLibTests`
Expected: All tests pass.

- [ ] **Step 2: Verify no regressions in `AiClientLib`**

Run: `cd appinventor && ant -f appengine/build.xml AiClientLib`
Expected: BUILD SUCCESSFUL with no warnings related to `AIStreamStatus` or `ChatCallback`.
