# AI Agent Streaming Responses — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Stream LLM text tokens to the client incrementally via enhanced GWT-RPC polling, replacing the static "Calling AI..." status with live text as the model generates it.

**Architecture:** LLM providers stream tokens into a Memcache-backed buffer (via StorageIo). The existing `getRequestStatus` polling (now at 250ms adaptive) returns accumulated tokens alongside status. The final RPC response remains the source of truth — streaming is a UX enhancement with zero correctness impact.

**Tech Stack:** Java (App Engine), GWT, Memcache, marked.js/DOMPurify (client-side markdown)

**Spec:** `docs/superpowers/specs/2026-04-01-ai-agent-streaming-design.md`

---

## File Map

### New Files
| File | Responsibility |
|------|---------------|
| `appinventor/appengine/src/com/google/appinventor/shared/rpc/aiagent/AIStreamStatus.java` | GWT-serializable DTO returned by `getRequestStatus` |
| `appinventor/appengine/src/com/google/appinventor/server/aiagent/StreamBuffer.java` | Per-request orchestration over StorageIo stream methods |
| `appinventor/appengine/tests/com/google/appinventor/server/aiagent/StreamBufferTest.java` | Unit tests for StreamBuffer + ObjectifyStorageIo stream methods |

### Modified Files
| File | Change |
|------|--------|
| `appinventor/appengine/src/com/google/appinventor/server/storage/StorageIo.java` | Add 6 stream buffer methods, remove 3 old status methods |
| `appinventor/appengine/src/com/google/appinventor/server/storage/ObjectifyStorageIo.java` | Implement stream buffer methods using memcache key-per-chunk |
| `appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/LLMProvider.java` | Add `StreamBuffer` param to `chat()` and `continueWithToolResults()` |
| `appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/AnthropicProvider.java` | Stream SSE, write tokens to buffer |
| `appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/OpenAIProvider.java` | Same (Responses API format) |
| `appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/GeminiProvider.java` | Same |
| `appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/OllamaProvider.java` | Same (flip `"stream": false` to `true`) |
| `appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/MiniMaxProvider.java` | Same |
| `appinventor/appengine/src/com/google/appinventor/server/aiagent/AIAgentEngine.java` | Create StreamBuffer, pass to providers, use for status updates |
| `appinventor/appengine/src/com/google/appinventor/server/aiagent/ConversationManager.java` | Replace updateStatus/clearStatus with StreamBuffer delegation |
| `appinventor/appengine/src/com/google/appinventor/shared/rpc/aiagent/AIAgentService.java` | Change `getRequestStatus` return type |
| `appinventor/appengine/src/com/google/appinventor/shared/rpc/aiagent/AIAgentServiceAsync.java` | Match async return type |
| `appinventor/appengine/src/com/google/appinventor/server/aiagent/AIAgentServiceImpl.java` | Implement new `getRequestStatus` using StreamBuffer |
| `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/AIResponseOrchestrator.java` | Adaptive polling, streaming bubble management |
| `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/AIChatRenderer.java` | Incremental markdown rendering |

---

## Task 1: Create AIStreamStatus DTO

**Files:**
- Create: `appinventor/appengine/src/com/google/appinventor/shared/rpc/aiagent/AIStreamStatus.java`

- [ ] **Step 1: Create AIStreamStatus.java**

```java
package com.google.appinventor.shared.rpc.aiagent;

import com.google.gwt.user.client.rpc.IsSerializable;
import java.io.Serializable;

/**
 * Status update returned by {@link AIAgentService#getRequestStatus}.
 * Contains incremental text deltas and status updates from the streaming LLM response.
 */
public class AIStreamStatus implements IsSerializable, Serializable {
  private static final long serialVersionUID = 1L;

  private String statusText;
  private String textDelta;
  private boolean done;

  // Required no-arg constructor for GWT serialization
  public AIStreamStatus() {}

  public AIStreamStatus(String statusText, String textDelta, boolean done) {
    this.statusText = statusText;
    this.textDelta = textDelta;
    this.done = done;
  }

  public String getStatusText() { return statusText; }
  public String getTextDelta() { return textDelta; }
  public boolean isDone() { return done; }
}
```

- [ ] **Step 2: Verify it compiles**

Run: `cd /home/diego/workplace-Kodular/appinventor-sources && ant -f appinventor/build.xml noplay` (or the project's build command)

Expected: Clean compile, no errors.

- [ ] **Step 3: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/shared/rpc/aiagent/AIStreamStatus.java
git commit -m "feat(ai-agent): add AIStreamStatus DTO for streaming responses"
```

---

## Task 2: Add Stream Buffer Methods to StorageIo Layer

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/storage/StorageIo.java:773-788` (replace old status methods)
- Modify: `appinventor/appengine/src/com/google/appinventor/server/storage/ObjectifyStorageIo.java:2898-3023` (replace old status implementation)

- [ ] **Step 1: Update StorageIo interface**

In `StorageIo.java`, replace the three old status methods (lines ~773-788) with six new stream buffer methods:

```java
  // --- AI Stream Buffer ---

  /**
   * Initialize (or reset) the streaming buffer for a project.
   */
  void initAIStreamBuffer(long projectId);

  /**
   * Append a chunk to the streaming buffer. Chunks are prefixed with
   * "t:" for text deltas or "s:" for status updates.
   */
  void appendAIStreamChunk(long projectId, String chunk);

  /**
   * Read and consume all chunks since the last consume call.
   * Returns a list of prefixed chunk strings, or empty list if none.
   */
  List<String> consumeAIStreamChunks(long projectId);

  /**
   * Mark the stream as done (LLM response fully received).
   */
  void markAIStreamDone(long projectId);

  /**
   * Check whether the stream is done.
   */
  boolean isAIStreamDone(long projectId);

  /**
   * Delete all stream buffer keys for a project.
   */
  void clearAIStreamBuffer(long projectId);
```

- [ ] **Step 2: Implement in ObjectifyStorageIo**

Replace the old constants and methods (lines ~2898-3023) with:

```java
  // --- AI Stream Buffer ---
  private static final String AI_STREAM_PREFIX = "ai_stream:";
  private static final int STREAM_TTL_SECONDS = 60; // 1-minute sliding TTL

  private String streamKey(long projectId, String suffix) {
    return AI_STREAM_PREFIX + projectId + ":" + suffix;
  }

  @Override
  public void initAIStreamBuffer(long projectId) {
    Expiration exp = Expiration.byDeltaSeconds(STREAM_TTL_SECONDS);
    // Read old write counter to know how many chunks to clean up
    Object oldWc = memcache.get(streamKey(projectId, "wc"));
    if (oldWc != null) {
      long oldCount = Long.parseLong(oldWc.toString());
      List<String> keysToDelete = new ArrayList<>();
      for (long i = 0; i <= oldCount; i++) {
        keysToDelete.add(streamKey(projectId, "chunk:" + i));
      }
      keysToDelete.add(streamKey(projectId, "done"));
      memcache.deleteAll(keysToDelete);
    }
    // Initialize counters as Long values (increment() operates on numeric types)
    memcache.put(streamKey(projectId, "wc"), 0L, exp);
    memcache.put(streamKey(projectId, "rc"), 0L, exp);
  }

  @Override
  public void appendAIStreamChunk(long projectId, String chunk) {
    Expiration exp = Expiration.byDeltaSeconds(STREAM_TTL_SECONDS);
    // Atomically increment write counter (initializes to 0 if absent).
    // increment() works on numeric values and returns the post-increment value.
    // The 0L initial value handles the case where the key was evicted after init().
    Long index = memcache.increment(streamKey(projectId, "wc"), 1, 0L);
    // Write chunk at this index
    memcache.put(streamKey(projectId, "chunk:" + index), chunk, exp);
    // Do NOT re-put the write counter — increment() already updated it, and
    // re-putting as a String would break subsequent increment() calls.
    // The 1-minute TTL from init() is sufficient; if it expires, the 0L
    // default in increment() ensures correctness (some chunks may be
    // orphaned but the final RPC response is the source of truth).
  }

  @Override
  public List<String> consumeAIStreamChunks(long projectId) {
    Object wcObj = memcache.get(streamKey(projectId, "wc"));
    Object rcObj = memcache.get(streamKey(projectId, "rc"));
    if (wcObj == null || rcObj == null) {
      return Collections.emptyList();
    }
    long wc = Long.parseLong(wcObj.toString());
    long rc = Long.parseLong(rcObj.toString());
    if (rc >= wc) {
      return Collections.emptyList();
    }
    // Batch read all pending chunks
    List<String> keys = new ArrayList<>();
    for (long i = rc + 1; i <= wc; i++) {
      keys.add(streamKey(projectId, "chunk:" + i));
    }
    Map<String, Object> results = memcache.getAll(keys);
    List<String> chunks = new ArrayList<>();
    for (String key : keys) {
      Object val = results.get(key);
      if (val != null) {
        chunks.add(val.toString());
      }
      // Skip nulls (evicted chunks) — acceptable degradation
    }
    // Advance read counter
    memcache.put(streamKey(projectId, "rc"), String.valueOf(wc),
        Expiration.byDeltaSeconds(STREAM_TTL_SECONDS));
    return chunks;
  }

  @Override
  public void markAIStreamDone(long projectId) {
    memcache.put(streamKey(projectId, "done"), "true",
        Expiration.byDeltaSeconds(STREAM_TTL_SECONDS));
  }

  @Override
  public boolean isAIStreamDone(long projectId) {
    return memcache.get(streamKey(projectId, "done")) != null;
  }

  @Override
  public void clearAIStreamBuffer(long projectId) {
    Object wcObj = memcache.get(streamKey(projectId, "wc"));
    long wc = 0;
    if (wcObj != null) {
      wc = Long.parseLong(wcObj.toString());
    }
    List<String> keys = new ArrayList<>();
    keys.add(streamKey(projectId, "wc"));
    keys.add(streamKey(projectId, "rc"));
    keys.add(streamKey(projectId, "done"));
    for (long i = 0; i <= wc; i++) {
      keys.add(streamKey(projectId, "chunk:" + i));
    }
    memcache.deleteAll(keys);
  }
```

Note: `increment()` operates on numeric types stored in Memcache. `init()` stores `0L` (Long), and `increment()` returns the post-increment Long value. Never `put()` the counter as a String after `increment()` — this would corrupt the type and break subsequent `increment()` calls.

- [ ] **Step 3: Fix any compilation errors from removed methods**

Search for all references to `updateAIRequestStatus`, `getAIRequestStatus`, `clearAIRequestStatus` outside of ObjectifyStorageIo. These will be in `ConversationManager.java` and `AIAgentEngine.java` — they will be updated in later tasks. For now, add temporary stub implementations to keep compilation working:

```java
  // TEMPORARY — remove after Task 5
  public void updateAIRequestStatus(long projectId, String status) {
    appendAIStreamChunk(projectId, "s:" + status);
  }
  public void clearAIRequestStatus(long projectId) {
    clearAIStreamBuffer(projectId);
  }
  public String getAIRequestStatus(long projectId) {
    List<String> chunks = consumeAIStreamChunks(projectId);
    String lastStatus = "";
    for (String c : chunks) {
      if (c.startsWith("s:")) lastStatus = c.substring(2);
    }
    return lastStatus;
  }
```

And keep the old method signatures in `StorageIo.java` temporarily alongside the new ones.

- [ ] **Step 4: Verify it compiles**

Run the project's build command.

Expected: Clean compile.

- [ ] **Step 5: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/server/storage/StorageIo.java \
       appinventor/appengine/src/com/google/appinventor/server/storage/ObjectifyStorageIo.java
git commit -m "feat(ai-agent): add stream buffer methods to StorageIo layer"
```

---

## Task 3: Create StreamBuffer Class

**Files:**
- Create: `appinventor/appengine/src/com/google/appinventor/server/aiagent/StreamBuffer.java`
- Create: `appinventor/appengine/tests/com/google/appinventor/server/aiagent/StreamBufferTest.java`

- [ ] **Step 1: Create StreamBuffer.java**

```java
package com.google.appinventor.server.aiagent;

import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.shared.rpc.aiagent.AIStreamStatus;

import java.util.List;

/**
 * Per-request buffer for streaming LLM text tokens to the client via Memcache.
 * Delegates all storage to {@link StorageIo} stream buffer methods.
 *
 * <p>Chunks are prefixed: "t:" for text deltas, "s:" for status updates.
 */
public class StreamBuffer {
  private final StorageIo storageIo;
  private final long projectId;

  public StreamBuffer(StorageIo storageIo, long projectId) {
    this.storageIo = storageIo;
    this.projectId = projectId;
  }

  /** Initialize or reset the buffer. Call once at the start of an LLM request. */
  public void init() {
    storageIo.initAIStreamBuffer(projectId);
  }

  /** Append a text delta from the LLM. */
  public void appendText(String text) {
    if (text != null && !text.isEmpty()) {
      storageIo.appendAIStreamChunk(projectId, "t:" + text);
    }
  }

  /** Append a status update (e.g., "Building context..."). */
  public void appendStatus(String status) {
    if (status != null && !status.isEmpty()) {
      storageIo.appendAIStreamChunk(projectId, "s:" + status);
    }
  }

  /** Mark the stream as done (LLM response fully received). */
  public void markDone() {
    storageIo.markAIStreamDone(projectId);
  }

  /** Clean up all buffer keys. Call after the RPC response is sent. */
  public void clear() {
    storageIo.clearAIStreamBuffer(projectId);
  }

  /**
   * Consume all pending chunks and return them as an {@link AIStreamStatus}.
   * Text deltas are concatenated into {@code textDelta}. The last status
   * update becomes {@code statusText}. Checks the done flag.
   */
  public AIStreamStatus consume() {
    List<String> chunks = storageIo.consumeAIStreamChunks(projectId);
    boolean done = storageIo.isAIStreamDone(projectId);

    StringBuilder textBuilder = null;
    String lastStatus = null;

    for (String chunk : chunks) {
      if (chunk.startsWith("t:")) {
        if (textBuilder == null) {
          textBuilder = new StringBuilder();
        }
        textBuilder.append(chunk.substring(2));
      } else if (chunk.startsWith("s:")) {
        lastStatus = chunk.substring(2);
      }
    }

    return new AIStreamStatus(
        lastStatus,
        textBuilder != null ? textBuilder.toString() : null,
        done
    );
  }
}
```

- [ ] **Step 2: Create StreamBufferTest.java**

```java
package com.google.appinventor.server.aiagent;

import com.google.appinventor.common.testutils.TestUtils;
import com.google.appinventor.server.storage.ObjectifyStorageIo;
import com.google.appinventor.shared.rpc.aiagent.AIStreamStatus;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import junit.framework.TestCase;

/**
 * Tests for {@link StreamBuffer} using local App Engine Memcache.
 *
 * Note: Cannot extend LocalDatastoreTestCase because it only configures
 * Datastore, not Memcache. We need both since StreamBuffer uses Memcache
 * and ObjectifyStorageIo needs Datastore for initialization.
 */
public class StreamBufferTest extends TestCase {
  private static final String APPENGINE_GENERATED_DIR = TestUtils.APP_INVENTOR_ROOT_DIR +
      "/appengine/build/tests/appengine-generated";
  private static final long PROJECT_ID = 12345L;

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          new LocalDatastoreServiceTestConfig()
              .setBackingStoreLocation(APPENGINE_GENERATED_DIR),
          new LocalMemcacheServiceTestConfig());

  private ObjectifyStorageIo storageIo;
  private StreamBuffer buffer;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    System.setProperty("appengine.generated.dir", APPENGINE_GENERATED_DIR);
    helper.setUp();
    storageIo = new ObjectifyStorageIo();
    buffer = new StreamBuffer(storageIo, PROJECT_ID);
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    helper.tearDown();
  }

  public void testInitAndConsumeEmpty() {
    buffer.init();
    AIStreamStatus status = buffer.consume();
    assertNull(status.getStatusText());
    assertNull(status.getTextDelta());
    assertFalse(status.isDone());
  }

  public void testAppendTextAndConsume() {
    buffer.init();
    buffer.appendText("Hello ");
    buffer.appendText("world");
    AIStreamStatus status = buffer.consume();
    assertEquals("Hello world", status.getTextDelta());
    assertNull(status.getStatusText());
    assertFalse(status.isDone());
  }

  public void testConsumeIsIncremental() {
    buffer.init();
    buffer.appendText("first");
    AIStreamStatus s1 = buffer.consume();
    assertEquals("first", s1.getTextDelta());

    buffer.appendText("second");
    AIStreamStatus s2 = buffer.consume();
    assertEquals("second", s2.getTextDelta());
  }

  public void testStatusUpdates() {
    buffer.init();
    buffer.appendStatus("Building context...");
    buffer.appendText("token1");
    buffer.appendStatus("Calling AI...");
    AIStreamStatus status = buffer.consume();
    assertEquals("Calling AI...", status.getStatusText());
    assertEquals("token1", status.getTextDelta());
  }

  public void testMarkDone() {
    buffer.init();
    buffer.appendText("final");
    buffer.markDone();
    AIStreamStatus status = buffer.consume();
    assertEquals("final", status.getTextDelta());
    assertTrue(status.isDone());
  }

  public void testClear() {
    buffer.init();
    buffer.appendText("data");
    buffer.clear();
    AIStreamStatus status = buffer.consume();
    assertNull(status.getTextDelta());
    assertFalse(status.isDone());
  }

  public void testInitResetsStaleBuffer() {
    buffer.init();
    buffer.appendText("old data");
    buffer.markDone();
    // Re-init should wipe everything
    buffer.init();
    AIStreamStatus status = buffer.consume();
    assertNull(status.getTextDelta());
    assertFalse(status.isDone());
  }

  public void testEmptyTextIgnored() {
    buffer.init();
    buffer.appendText("");
    buffer.appendText(null);
    AIStreamStatus status = buffer.consume();
    assertNull(status.getTextDelta());
  }
}
```

- [ ] **Step 3: Run the tests**

Run: The project's test command targeting `StreamBufferTest`.

Expected: All tests pass. Note: `LocalDatastoreTestCase` sets up App Engine local services including Memcache, so the tests exercise real Memcache operations in-process.

- [ ] **Step 4: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/server/aiagent/StreamBuffer.java \
       appinventor/appengine/tests/com/google/appinventor/server/aiagent/StreamBufferTest.java
git commit -m "feat(ai-agent): add StreamBuffer with tests"
```

---

## Task 4: Update LLMProvider Interface

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/LLMProvider.java:44-65`
- Modify: All 5 provider implementations (signatures only — no streaming logic yet)

- [ ] **Step 1: Add StreamBuffer parameter to LLMProvider interface**

In `LLMProvider.java`, update the two method signatures:

```java
  LLMResponse chat(String systemPrompt, List<String> contextMessages,
      String userMessage, List<LLMTool> tools, String providerRef,
      List<ChatMessage> history, ReadOnlyToolResolver resolver,
      StreamBuffer streamBuffer)
      throws LLMProviderException;

  LLMResponse continueWithToolResults(String continuationState,
      List<LLMTool> tools, ReadOnlyToolResolver resolver,
      StreamBuffer streamBuffer)
      throws LLMProviderException;
```

Add the import:
```java
import com.google.appinventor.server.aiagent.StreamBuffer;
```

- [ ] **Step 2: Update all 5 provider signatures (pass-through only)**

In each provider file (`AnthropicProvider.java`, `OpenAIProvider.java`, `GeminiProvider.java`, `OllamaProvider.java`, `MiniMaxProvider.java`), add the `StreamBuffer streamBuffer` parameter to both `chat()` and `continueWithToolResults()` method signatures. Do NOT change any internal logic yet — just accept and ignore the parameter.

Example for `AnthropicProvider.java` (line ~67):
```java
  @Override
  public LLMResponse chat(String systemPrompt, List<String> contextMessages,
      String userMessage, List<LLMTool> tools, String providerRef,
      List<ChatMessage> history, ReadOnlyToolResolver resolver,
      StreamBuffer streamBuffer)
      throws LLMProviderException {
```

Repeat for all 5 providers, both `chat()` and `continueWithToolResults()`.

Add the import to each provider:
```java
import com.google.appinventor.server.aiagent.StreamBuffer;
```

- [ ] **Step 3: Update all call sites in AIAgentEngine**

In `AIAgentEngine.java`, update the 3 places that call `provider.chat()` and `provider.continueWithToolResults()` to pass `null` for now:

- Line ~161: `provider.chat(..., resolver, null);`
- Line ~263: `provider.continueWithToolResults(..., resolver, null);`
- Line ~395: `provider.chat(..., resolver, null);`

- [ ] **Step 4: Verify it compiles**

Run the project's build command.

Expected: Clean compile.

- [ ] **Step 5: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/LLMProvider.java \
       appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/AnthropicProvider.java \
       appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/OpenAIProvider.java \
       appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/GeminiProvider.java \
       appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/OllamaProvider.java \
       appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/MiniMaxProvider.java \
       appinventor/appengine/src/com/google/appinventor/server/aiagent/AIAgentEngine.java
git commit -m "refactor(ai-agent): add StreamBuffer parameter to LLMProvider interface"
```

---

## Task 5: Wire StreamBuffer Through AIAgentEngine and ConversationManager

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/AIAgentEngine.java:109-411`
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/ConversationManager.java:97-103`
- Modify: `appinventor/appengine/src/com/google/appinventor/server/storage/StorageIo.java` (remove old stubs)
- Modify: `appinventor/appengine/src/com/google/appinventor/server/storage/ObjectifyStorageIo.java` (remove old stubs)

- [ ] **Step 1: Update AIAgentEngine.processRequest()**

At the start of `processRequest()` (after line ~111), create the StreamBuffer and use it for status updates:

```java
StreamBuffer streamBuffer = new StreamBuffer(storageIo, projectId);
streamBuffer.init();
```

Replace all `conversationManager.updateStatus(projectId, "...")` calls with `streamBuffer.appendStatus("...")`.

**Important distinction between `markDone()` and `clear()`:**
- `markDone()` is called by the **provider** inside `readStreamingResponse()` when the LLM HTTP response is fully received (before `chat()` returns). This signals the client to stop polling.
- `clear()` is called by **AIAgentEngine** after the full RPC response is assembled and ready to return. This cleans up the buffer.

So replace `conversationManager.clearStatus(projectId)` calls in the engine with `streamBuffer.clear()` (cleanup). Do NOT call `streamBuffer.markDone()` in the engine — the provider handles that.

Pass `streamBuffer` instead of `null` to `provider.chat()`:
```java
LLMResponse llmResponse = provider.chat(systemPrompt, contextMessages,
    userMessage, tools, conv.getProviderRef(), history, resolver, streamBuffer);
```

- [ ] **Step 2: Update AIAgentEngine.continueRequest()**

Same pattern: create `StreamBuffer`, use for status, pass to `provider.continueWithToolResults()`:

```java
StreamBuffer streamBuffer = new StreamBuffer(storageIo, projectId);
streamBuffer.init();
streamBuffer.appendStatus("Continuing AI response...");
// ...
LLMResponse llmResponse = provider.continueWithToolResults(
    providerRef, tools, resolver, streamBuffer);
```

- [ ] **Step 3: Update AIAgentEngine.reportExecutionErrors()**

Same pattern: create `StreamBuffer`, use for status, pass to `provider.chat()`:

```java
StreamBuffer streamBuffer = new StreamBuffer(storageIo, projectId);
streamBuffer.init();
streamBuffer.appendStatus("Analyzing errors (" + retryInfo + ")...");
// ...
LLMResponse llmResponse = provider.chat(systemPrompt, contextMessages,
    feedback, tools, conv.getProviderRef(), history, resolver, streamBuffer);
```

- [ ] **Step 4: Update AIAgentEngine.getRequestStatus()**

This method currently calls `conversationManager.getStatus()`. Replace it to use `StreamBuffer.consume()`:

```java
public AIStreamStatus getRequestStatus(long projectId) {
  StreamBuffer streamBuffer = new StreamBuffer(storageIo, projectId);
  return streamBuffer.consume();
}
```

- [ ] **Step 5: Remove status methods from ConversationManager**

In `ConversationManager.java`, remove `updateStatus()` and `clearStatus()` methods (lines ~97-103).

Also check `clearConversation()` (line ~66) — it calls `clearStatus(projectId)`. Replace that call with `storageIo.clearAIStreamBuffer(projectId)` to ensure clearing a conversation also clears any active stream buffer.

- [ ] **Step 6: Remove old StorageIo status methods and temporary stubs**

In `StorageIo.java`, remove `updateAIRequestStatus`, `clearAIRequestStatus`, `getAIRequestStatus`.

In `ObjectifyStorageIo.java`, remove the old status constants (`AI_STATUS_CACHE_KEY_PREFIX`, `STATUS_TTL_SECONDS`) and old method implementations, plus the temporary stubs from Task 2.

- [ ] **Step 7: Verify it compiles**

Run the project's build command.

Expected: Clean compile. No references to old status methods remain.

- [ ] **Step 8: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/server/aiagent/AIAgentEngine.java \
       appinventor/appengine/src/com/google/appinventor/server/aiagent/ConversationManager.java \
       appinventor/appengine/src/com/google/appinventor/server/storage/StorageIo.java \
       appinventor/appengine/src/com/google/appinventor/server/storage/ObjectifyStorageIo.java
git commit -m "feat(ai-agent): wire StreamBuffer through engine and remove old status methods"
```

---

## Task 6: Update RPC Interface and Implementation

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/shared/rpc/aiagent/AIAgentService.java:68`
- Modify: `appinventor/appengine/src/com/google/appinventor/shared/rpc/aiagent/AIAgentServiceAsync.java:43`
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/AIAgentServiceImpl.java:137-146`

- [ ] **Step 1: Update AIAgentService.java**

Change the return type of `getRequestStatus` (line ~68):

```java
AIStreamStatus getRequestStatus(long projectId);
```

Add import:
```java
import com.google.appinventor.shared.rpc.aiagent.AIStreamStatus;
```

- [ ] **Step 2: Update AIAgentServiceAsync.java**

Change the callback type (line ~43):

```java
void getRequestStatus(long projectId, AsyncCallback<AIStreamStatus> callback);
```

Add import:
```java
import com.google.appinventor.shared.rpc.aiagent.AIStreamStatus;
```

- [ ] **Step 3: Update AIAgentServiceImpl.java**

Replace the `getRequestStatus` method (lines ~137-146):

```java
@Override
public AIStreamStatus getRequestStatus(long projectId) {
  String userId = userInfoProvider.getUserId();
  try {
    storageIo.assertUserHasProject(userId, projectId);
  } catch (SecurityException e) {
    return new AIStreamStatus("error", null, true);
  }
  return engine.getRequestStatus(projectId);
}
```

Add import:
```java
import com.google.appinventor.shared.rpc.aiagent.AIStreamStatus;
```

- [ ] **Step 4: Verify server-side compiles**

Run the project's build command.

Expected: **The build WILL fail** on client-side code (`AIResponseOrchestrator.java`) because it still expects `String` from `getRequestStatus`. This is intentional — the client is updated in Task 8. Verify there are no OTHER compile errors beyond this expected one. Proceed to Task 7 despite this failure.

- [ ] **Step 5: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/shared/rpc/aiagent/AIAgentService.java \
       appinventor/appengine/src/com/google/appinventor/shared/rpc/aiagent/AIAgentServiceAsync.java \
       appinventor/appengine/src/com/google/appinventor/server/aiagent/AIAgentServiceImpl.java
git commit -m "feat(ai-agent): change getRequestStatus to return AIStreamStatus"
```

---

## Task 7: Update Client — AIChatRenderer Incremental Rendering

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/AIChatRenderer.java`

- [ ] **Step 1: Add streaming methods to AIChatRenderer**

Add three new fields and three new methods for incremental rendering. The existing `createMessageBubble` returns a `FlowPanel` wrapper containing a `FlowPanel` bubble which contains a `Label` (sender) and an `HTML` widget (message content). The streaming code needs to track the `HTML` widget to update its content incrementally.

```java
  private String streamingTextAccumulator = "";
  private FlowPanel streamingWrapper = null;
  private HTML streamingMessageHtml = null;

  /**
   * Create a new AI message bubble in "streaming" state.
   * Reuses createMessageBubble with empty text, then keeps a reference
   * to the HTML widget for incremental updates.
   */
  public void startStreamingBubble() {
    streamingTextAccumulator = "";
    streamingWrapper = createMessageBubble(
        MESSAGES.aiChatAiLabel(), "", false);
    // The HTML widget is the second child of the bubble (first child of wrapper),
    // which is the second widget added in createMessageBubble (after the sender label).
    FlowPanel bubble = (FlowPanel) streamingWrapper.getWidget(0);
    streamingMessageHtml = (HTML) bubble.getWidget(1);
    chatHistory.add(streamingWrapper);
    scrollToBottom();
  }

  /**
   * Append a text delta to the streaming bubble with incremental markdown rendering.
   */
  public void appendStreamingText(String delta) {
    if (streamingMessageHtml == null) return;
    streamingTextAccumulator += delta;
    // Mitigate unclosed code fences: if odd number of ```, append a closing one for rendering
    String textToRender = streamingTextAccumulator;
    int fenceCount = countOccurrences(textToRender, "```");
    if (fenceCount % 2 != 0) {
      textToRender += "\n```";
    }
    streamingMessageHtml.setHTML(markdownToSafeHtml(textToRender));
    scrollToBottom();
  }

  /**
   * Replace streaming bubble content with the final canonical text.
   */
  public void finalizeStreamingBubble(String finalText) {
    if (streamingMessageHtml != null) {
      streamingMessageHtml.setHTML(markdownToSafeHtml(finalText));
      scrollToBottom();
    }
    streamingWrapper = null;
    streamingMessageHtml = null;
    streamingTextAccumulator = "";
  }

  private static int countOccurrences(String text, String sub) {
    int count = 0;
    int idx = 0;
    while ((idx = text.indexOf(sub, idx)) != -1) {
      count++;
      idx += sub.length();
    }
    return count;
  }
```

- [ ] **Step 2: Verify it compiles**

Run the project's build command.

Expected: Clean compile.

- [ ] **Step 3: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/AIChatRenderer.java
git commit -m "feat(ai-agent): add incremental markdown rendering to AIChatRenderer"
```

---

## Task 8: Update Client — AIResponseOrchestrator Streaming Polling

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/AIResponseOrchestrator.java:49,60-70,691-736`

- [ ] **Step 1: Add new ChatCallback methods**

Add the streaming methods to the `ChatCallback` interface (line ~60):

```java
  public interface ChatCallback {
    void addUserMessage(String text);
    void addAiMessage(String text);
    void showOperationPreview(AIAgentResponse response);
    void hideOperationPreview();
    void setRequestInFlight(boolean inFlight);
    void setStatusText(String text);
    void setStatusVisible(boolean visible);
    void setAutoAcceptVisible(boolean visible);
    void clearChatHistory();
    // New streaming methods:
    void startStreamingBubble();
    void appendStreamingText(String delta);
    void finalizeStreamingBubble(String finalText);
  }
```

- [ ] **Step 2: Update poll interval and add adaptive logic**

Replace the constant (line ~49):
```java
private static final int POLL_INTERVAL_FAST_MS = 250;
private static final int POLL_INTERVAL_SLOW_MS = 1000;
```

Add a tracking field:
```java
private boolean streamingActive = false;
```

- [ ] **Step 3: Update startPollingStatus()**

Rewrite the polling method (lines ~691-725) to handle `AIStreamStatus`:

```java
private void startPollingStatus() {
  stopPollingStatus();
  streamingActive = false;
  pollingTimer = new Timer() {
    @Override
    public void run() {
      long projectId = contextCollector.getCurrentProjectId();
      if (projectId == 0 || !requestInFlight) {
        stopPollingStatus();
        return;
      }
      aiAgentService.getRequestStatus(projectId,
          new OdeAsyncCallback<AIStreamStatus>() {
            @Override
            public void onSuccess(AIStreamStatus status) {
              if (status == null) return;
              if (status.getStatusText() != null) {
                callback.setStatusText(status.getStatusText());
              }
              if (status.getTextDelta() != null) {
                if (!streamingActive) {
                  streamingActive = true;
                  callback.startStreamingBubble();
                  // Switch to fast polling
                  pollingTimer.cancel();
                  pollingTimer.scheduleRepeating(POLL_INTERVAL_FAST_MS);
                }
                callback.appendStreamingText(status.getTextDelta());
              }
              if (status.isDone()) {
                stopPollingStatus();
              }
            }
            @Override
            public void onFailure(Throwable caught) {
              // Polling failure is non-fatal — next poll will retry
            }
          });
    }
  };
  pollingTimer.scheduleRepeating(POLL_INTERVAL_SLOW_MS);
}
```

- [ ] **Step 4: Update handleResponse()**

In the `handleResponse` method, replace the existing AI message display logic. The current code only calls `callback.addAiMessage(aiMessage)` when there are no operations (the message is deferred when operations exist). Preserve this behavior while adding streaming finalization:

```java
// Replace the existing aiMessage display block with:
String aiMessage = response.getAiMessage();
boolean hasOps = response.getOperations() != null && !response.getOperations().isEmpty();

if (streamingActive) {
  // Streaming was active — finalize the bubble with the canonical text.
  // If there are operations, we still finalize (the text was already visible
  // during streaming), but the operations preview follows below as before.
  if (aiMessage != null && !aiMessage.isEmpty()) {
    callback.finalizeStreamingBubble(aiMessage);
  }
  streamingActive = false;
} else {
  // No streaming happened (fast response or error) — show message normally,
  // but only if there are no operations (preserves existing deferred-message behavior)
  if (aiMessage != null && !aiMessage.isEmpty() && !hasOps) {
    callback.addAiMessage(aiMessage);
  }
}
// ... rest of handleResponse (error display, operation preview, etc.) stays unchanged
```

Key difference from current behavior: when streaming was active, the message text is always finalized (it was already visible during streaming). When NOT streaming, the original deferred-message logic is preserved unchanged.

- [ ] **Step 5: Update AIChatDialog to implement new ChatCallback methods**

**File:** `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/AIChatDialog.java` (note: NOT in the `aiagent` subdirectory)

The `AIChatDialog` class (which implements `ChatCallback`) needs to wire the new methods to `AIChatRenderer`:

```java
@Override
public void startStreamingBubble() {
  renderer.startStreamingBubble();
}

@Override
public void appendStreamingText(String delta) {
  renderer.appendStreamingText(delta);
}

@Override
public void finalizeStreamingBubble(String finalText) {
  renderer.finalizeStreamingBubble(finalText);
}
```

- [ ] **Step 6: Verify it compiles**

Run the project's build command.

Expected: Clean compile. The full pipeline is now wired: server streams to buffer, client polls and renders incrementally.

- [ ] **Step 7: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/AIResponseOrchestrator.java \
       appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/AIChatRenderer.java \
       appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/AIChatDialog.java
git commit -m "feat(ai-agent): update client polling and rendering for streaming"
```

---

## Task 9: AnthropicProvider Streaming

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/AnthropicProvider.java`

Reference: [Anthropic Streaming API](https://docs.anthropic.com/en/api/messages-streaming) — SSE format with event types: `message_start`, `content_block_start`, `content_block_delta`, `content_block_stop`, `message_delta`, `message_stop`.

- [ ] **Step 1: Enable streaming in the request**

In the method that builds the request body JSON, add:
```java
requestBody.put("stream", true);
```

- [ ] **Step 2: Replace the response reading code**

The current `readResponse()` method (lines ~648-664) reads the entire response into a `StringBuilder`. Replace the response reading in `chat()` (and the internal tool-use loop) with SSE line-by-line parsing:

```java
private String readStreamingResponse(HttpURLConnection conn, StreamBuffer streamBuffer)
    throws IOException {
  BufferedReader reader = new BufferedReader(
      new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));

  StringBuilder fullResponse = new StringBuilder();
  // Accumulate the full response JSON for parsing after streaming is done
  List<JSONObject> contentBlocks = new ArrayList<>();
  String currentEventType = null;
  StringBuilder dataBuffer = new StringBuilder();
  String stopReason = null;

  String line;
  while ((line = reader.readLine()) != null) {
    if (line.startsWith("event: ")) {
      currentEventType = line.substring(7).trim();
      continue;
    }
    if (line.startsWith("data: ")) {
      String data = line.substring(6);
      if ("content_block_delta".equals(currentEventType)) {
        JSONObject delta = new JSONObject(data);
        JSONObject deltaObj = delta.getJSONObject("delta");
        if ("text_delta".equals(deltaObj.getString("type"))) {
          String text = deltaObj.getString("text");
          if (streamBuffer != null) {
            streamBuffer.appendText(text);
          }
        }
        // tool_use input_json_delta — don't stream, just accumulate
      } else if ("content_block_start".equals(currentEventType)) {
        // Track content block type (text vs tool_use)
        JSONObject block = new JSONObject(data);
        contentBlocks.add(block);
      } else if ("message_delta".equals(currentEventType)) {
        JSONObject msgDelta = new JSONObject(data);
        if (msgDelta.has("delta")) {
          JSONObject d = msgDelta.getJSONObject("delta");
          if (d.has("stop_reason")) {
            stopReason = d.getString("stop_reason");
          }
        }
      } else if ("message_stop".equals(currentEventType)) {
        // End of message
      }
      // Accumulate raw data for full response reconstruction
      fullResponse.append(data);
    }
    if (line.isEmpty()) {
      // Event boundary — reset for next event
      currentEventType = null;
    }
  }
  reader.close();

  if (streamBuffer != null) {
    streamBuffer.markDone();
  }

  // Reconstruct the full response JSON for existing parsing logic
  // (The existing parseResponse method expects the complete message JSON)
  return reconstructFullResponse(contentBlocks, stopReason);
}
```

The `reconstructFullResponse` method assembles the content blocks, stop reason, and usage into the same JSON structure that `parseResponse()` already expects. This minimizes changes to the existing parsing logic.

**Important**: The internal read-only tool loop needs adjustment. When streaming, text blocks arrive before tool_use blocks. Stream the text immediately, but when a tool_use block is detected, the provider:
1. Finishes reading the current SSE stream
2. Resolves the tool call
3. Calls the API again with `"stream": true` for the next iteration
4. Continues streaming text from the new response

- [ ] **Step 3: Test manually with a running instance**

Deploy or run locally. Send a message via the AI agent. Verify:
- Tokens appear incrementally in the chat bubble
- The final response replaces the streaming bubble with the canonical text
- Tool calls still work (read-only and action)

- [ ] **Step 4: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/AnthropicProvider.java
git commit -m "feat(ai-agent): add streaming support to AnthropicProvider"
```

---

## Task 10: OpenAIProvider Streaming

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/OpenAIProvider.java`

Reference: [OpenAI Responses API Streaming](https://platform.openai.com/docs/api-reference/responses/create) — SSE format. Key events: `response.output_text.delta` (text), `response.output_item.added` (new output item), `response.function_call_arguments.delta` (tool args), `response.completed`.

- [ ] **Step 1: Enable streaming in the request**

Add `"stream": true` to the request body.

- [ ] **Step 2: Implement SSE parsing**

Similar structure to Anthropic but different event names. Stream `response.output_text.delta` events to buffer. Stop buffering text when `response.function_call_arguments.delta` or tool-related events appear. Reconstruct full response for existing parsing logic.

Key difference from Anthropic: OpenAI's Responses API is stateful (`previous_response_id`), so the `providerRef` handling stays the same — extract the response ID from the stream events.

- [ ] **Step 3: Test manually**

Switch provider to OpenAI, send a message, verify streaming works.

- [ ] **Step 4: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/OpenAIProvider.java
git commit -m "feat(ai-agent): add streaming support to OpenAIProvider"
```

---

## Task 11: GeminiProvider Streaming

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/GeminiProvider.java`

Reference: [Gemini Streaming](https://ai.google.dev/api/generate-content#method:-models.streamgeneratecontent) — uses `streamGenerateContent` endpoint instead of `generateContent`. Returns SSE with partial `GenerateContentResponse` objects.

- [ ] **Step 1: Switch to streaming endpoint**

Change the API URL from `generateContent` to `streamGenerateContent?alt=sse`.

- [ ] **Step 2: Implement SSE parsing**

Parse SSE events. Each `data:` line contains a partial response JSON with `candidates[0].content.parts[0].text`. Stream text parts to buffer. Handle `functionCall` parts by stopping text streaming.

- [ ] **Step 3: Test manually**

Switch provider to Gemini, verify streaming works.

- [ ] **Step 4: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/GeminiProvider.java
git commit -m "feat(ai-agent): add streaming support to GeminiProvider"
```

---

## Task 12: OllamaProvider Streaming

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/OllamaProvider.java`

Reference: [Ollama API](https://github.com/ollama/ollama/blob/main/docs/api.md#generate-a-chat-completion) — when `"stream": true`, returns line-delimited JSON objects, each with `message.content` field.

- [ ] **Step 1: Enable streaming**

Change `requestBody.put("stream", false)` to `requestBody.put("stream", true)`. **Note: this appears at TWO locations** (lines ~90 and ~246 — in both `chat()` and `continueWithToolResults()`). Update both.

- [ ] **Step 2: Implement line-delimited JSON parsing**

Each line is a complete JSON object. Read line-by-line, parse each, extract `message.content`, stream to buffer. The last object has `"done": true` with the full aggregated response.

- [ ] **Step 3: Test manually**

Run against a local Ollama instance, verify streaming works.

- [ ] **Step 4: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/OllamaProvider.java
git commit -m "feat(ai-agent): add streaming support to OllamaProvider"
```

---

## Task 13: MiniMaxProvider Streaming

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/MiniMaxProvider.java`

Reference: MiniMax uses OpenAI-compatible chat completions format. SSE with `choices[0].delta.content` for text deltas.

- [ ] **Step 1: Enable streaming**

Add `"stream": true` to the request body.

- [ ] **Step 2: Implement SSE parsing**

Standard OpenAI chat completions SSE format: `data: {"choices":[{"delta":{"content":"token"}}]}`. Stream `delta.content` to buffer. Handle `[DONE]` sentinel.

- [ ] **Step 3: Test manually**

Switch provider to MiniMax, verify streaming works.

- [ ] **Step 4: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/MiniMaxProvider.java
git commit -m "feat(ai-agent): add streaming support to MiniMaxProvider"
```

---

## Task 14: End-to-End Verification and Cleanup

- [ ] **Step 1: Run full test suite**

Run the project's full test suite to ensure no regressions.

- [ ] **Step 2: Manual E2E test with each provider**

For each provider (at minimum Anthropic, the default):
1. Open the IDE, start a conversation with the AI agent
2. Verify text appears incrementally in the chat bubble
3. Verify status messages ("Building context...", "Calling AI...") appear before tokens
4. Verify tool calls still work (ask it to add a component)
5. Verify `continueRequest` streaming works (ask for a complex multi-step task)
6. Verify error retry streaming works (trigger a validation error)
7. Verify the final message matches what was streamed (no truncation or duplication)

- [ ] **Step 3: Clean up any remaining temporary code**

Remove any temporary stubs, TODO comments, or debug logging added during development.

- [ ] **Step 4: Final commit**

```bash
git add -A
git commit -m "chore(ai-agent): streaming cleanup and verification"
```
