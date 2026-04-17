# Multi-Agent Orchestration Phase B: Parallel Execution — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Enable parallel per-screen child agent execution. After plan approval, the client spawns N parallel conversations (one per screen), each backed by background editors. Operations flow through a FIFO queue for sequential user approval.

**Architecture:** Server gains screen-scoped Memcache keying so concurrent child conversations don't clobber each other. Client introduces `ScreenExecutionContext` to parameterize the execution pipeline by target screen, `ChildConversation` to manage one child's RPC loop, and `AIOrchestrationManager` to coordinate N children with a FIFO batch queue.

**Tech Stack:** Java (GWT client + App Engine server), GWT-RPC async, Memcache.

**Spec:** `docs/superpowers/specs/2026-04-07-multi-agent-orchestration-design.md` (Phase 2: Execution, Phase 3: Approval)

**Depends on:** Phase A (planning mode) must be complete.

---

## Part 1: Server-Side Screen-Scoped State (Tasks 1-5)

### Task 1: Add screen-scoped StorageIo interface methods

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/storage/StorageIo.java:776`

- [ ] **Step 1: Add screen-scoped overloads after line 776**

After the existing `clearAIStreamCancelled(long projectId)`, add screen-scoped variants for all AI methods:

```java
  // ---- Screen-scoped AI methods (for multi-agent orchestration) ----

  void saveAIConversationState(long projectId, String screenName, AIConversationState state);
  AIConversationState getAIConversationState(long projectId, String screenName);
  void clearAIConversationState(long projectId, String screenName);

  void initAIStreamBuffer(long projectId, String screenName);
  void appendAIStreamChunk(long projectId, String screenName, String chunk);
  List<String> consumeAIStreamChunks(long projectId, String screenName);
  void markAIStreamDone(long projectId, String screenName);
  boolean isAIStreamDone(long projectId, String screenName);
  void clearAIStreamBuffer(long projectId, String screenName);

  void setAIStreamCancelled(long projectId, String screenName);
  boolean isAIStreamCancelled(long projectId, String screenName);
  void clearAIStreamCancelled(long projectId, String screenName);
```

---

### Task 2: Implement screen-scoped methods in ObjectifyStorageIo

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/storage/ObjectifyStorageIo.java:3140`

- [ ] **Step 1: Add a screen-scoped key helper**

After the existing `streamKey` method (line 3039), add:

```java
  private String streamKey(long projectId, String screenName, String suffix) {
    return AI_STREAM_PREFIX + projectId + ":" + screenName + ":" + suffix;
  }

  private String convKey(long projectId, String screenName) {
    return AI_CONV_CACHE_KEY_PREFIX + projectId + ":" + screenName;
  }
```

- [ ] **Step 2: Implement all 12 screen-scoped methods**

Each screen-scoped method mirrors the existing projectId-only version but uses the composite key. For example:

```java
  @Override
  public void saveAIConversationState(long projectId, String screenName,
      AIConversationState state) {
    memcache.put(convKey(projectId, screenName), state,
        Expiration.byDeltaSeconds(CONVERSATION_TTL_SECONDS));
  }

  @Override
  public AIConversationState getAIConversationState(long projectId, String screenName) {
    return (AIConversationState) memcache.get(convKey(projectId, screenName));
  }

  @Override
  public void clearAIConversationState(long projectId, String screenName) {
    memcache.delete(convKey(projectId, screenName));
  }
```

For stream buffer methods, follow the exact same pattern as the existing implementations (lines 3044-3140) but use `streamKey(projectId, screenName, suffix)` instead of `streamKey(projectId, suffix)`. The logic is identical — only the key changes.

---

### Task 3: Add screen-scoped StreamBuffer constructor

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/StreamBuffer.java:18`

- [ ] **Step 1: Add screenName field and second constructor**

```java
  private final StorageIo storageIo;
  private final long projectId;
  private final String screenName; // null for parent, non-null for child

  public StreamBuffer(StorageIo storageIo, long projectId) {
    this(storageIo, projectId, null);
  }

  public StreamBuffer(StorageIo storageIo, long projectId, String screenName) {
    this.storageIo = storageIo;
    this.projectId = projectId;
    this.screenName = screenName;
  }
```

- [ ] **Step 2: Update all StorageIo calls to route by screenName**

Every method that calls `storageIo.xxxAIStreamYyy(projectId)` needs to check `screenName != null` and call the screen-scoped overload instead. Create a private helper pattern:

```java
  private void doInit() {
    if (screenName != null) {
      storageIo.initAIStreamBuffer(projectId, screenName);
      storageIo.clearAIStreamCancelled(projectId, screenName);
    } else {
      storageIo.initAIStreamBuffer(projectId);
      storageIo.clearAIStreamCancelled(projectId);
    }
  }
```

Apply this pattern to all 12 StorageIo call sites in StreamBuffer (init, appendChunk, consumeChunks, markDone, isDone, clear, setCancelled, isCancelled, clearCancelled). The `consume()` method (line 105) also needs routing.

---

### Task 4: Add screen-scoped ConversationManager methods

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/ConversationManager.java:55`

- [ ] **Step 1: Add screen-scoped overloads**

```java
  public AIConversationState getConversation(long projectId, String screenName) {
    return storageIo.getAIConversationState(projectId, screenName);
  }

  public void saveConversation(long projectId, String screenName, AIConversationState state) {
    storageIo.saveAIConversationState(projectId, screenName, state);
  }

  public void clearConversation(long projectId, String screenName) {
    storageIo.clearAIConversationState(projectId, screenName);
    storageIo.clearAIStreamBuffer(projectId, screenName);
    // No message deletion — child conversations are ephemeral (no Datastore messages)
  }
```

---

### Task 5: Route AIAgentEngine by orchestrationMode + targetScreen

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/AIAgentEngine.java`

- [ ] **Step 1: Thread targetScreen through processRequest, continueRequest, reportExecutionErrors**

Add `String targetScreen` parameter to each engine method (after `planExecuteMode`). Update `AIAgentServiceImpl` to extract `request.getTargetScreen()` and pass it through.

- [ ] **Step 2: Create screen-scoped StreamBuffer when orchestrationMode is active**

In `processRequest`, where `new StreamBuffer(storageIo, projectId)` is created (line 157), add:

```java
    String targetScreen = orchestrationMode ? request.getTargetScreen() : null;
    StreamBuffer streamBuffer = new StreamBuffer(storageIo, projectId, targetScreen);
```

Apply the same pattern to all 7 StreamBuffer creation sites.

- [ ] **Step 3: Use screen-scoped ConversationManager methods when orchestrationMode**

In `initConversation()` (line 638), add screen-scoped routing:

```java
  ConversationInit initConversation(long projectId, String targetScreen) {
    AIConversationState conv;
    if (targetScreen != null) {
      conv = conversationManager.getConversation(projectId, targetScreen);
    } else {
      conv = conversationManager.getConversation(projectId);
    }
    // ... rest of method uses targetScreen for save/clear calls too
  }
```

Update all callers of `initConversation()` to pass `targetScreen`.

- [ ] **Step 4: Update getRequestStatus and cancelRequest**

In `getRequestStatus()` (line 581), add `String targetScreen` parameter:

```java
  public AIStreamStatus getRequestStatus(long projectId, String targetScreen) {
    StreamBuffer streamBuffer = new StreamBuffer(storageIo, projectId, targetScreen);
    // ... rest unchanged
  }
```

Same for `cancelRequest()` (line 599).

- [ ] **Step 5: Update AIAgentServiceImpl and RPC interface**

`getRequestStatus(long projectId)` and `cancelRequest(long projectId)` in `AIAgentService.java` gain an optional `String targetScreen` parameter. For backward compatibility, add overloaded methods:

```java
  AIStreamStatus getRequestStatus(long projectId);
  AIStreamStatus getRequestStatus(long projectId, String targetScreen);
  void cancelRequest(long projectId);
  void cancelRequest(long projectId, String targetScreen);
```

Update `AIAgentServiceImpl` to extract `targetScreen` and pass through.

- [ ] **Step 6: Exempt orchestration RPCs from rate limiting**

In `AIAgentServiceImpl.checkRateLimit()`, skip the check when `request.isOrchestrationMode()`:

```java
  if (request.isOrchestrationMode()) {
    return; // Child RPCs exempt — client enforces per-plan budget
  }
```

---

## Part 2: Client-Side Parallel Execution (Tasks 6-12)

### Task 6: Create ScreenExecutionContext

**Files:**
- Create: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/executor/ScreenExecutionContext.java`

- [ ] **Step 1: Create the context class**

```java
package com.google.appinventor.client.editor.youngandroid.aiagent.executor;

import com.google.appinventor.client.editor.youngandroid.YaFormEditor;
import com.google.appinventor.client.editor.youngandroid.YaBlocksEditor;
import com.google.appinventor.client.editor.youngandroid.aiagent.AIEditorState;

/**
 * Carries target screen editors through the execution pipeline.
 * Replaces static AIEditorState lookups for screen-targeted operations.
 */
public class ScreenExecutionContext {
  private final String screenName;
  private final YaFormEditor formEditor;
  private final YaBlocksEditor blocksEditor;

  public ScreenExecutionContext(String screenName,
      YaFormEditor formEditor, YaBlocksEditor blocksEditor) {
    this.screenName = screenName;
    this.formEditor = formEditor;
    this.blocksEditor = blocksEditor;
  }

  /** Creates a context for the currently visible screen. */
  public static ScreenExecutionContext forCurrentScreen() {
    return new ScreenExecutionContext(
        null, // screenName not needed for current screen
        AIEditorState.getCurrentFormEditor(),
        AIEditorState.getCurrentBlocksEditor());
  }

  public String getScreenName() { return screenName; }
  public YaFormEditor getFormEditor() { return formEditor; }
  public YaBlocksEditor getBlocksEditor() { return blocksEditor; }

  public boolean componentExists(String name) {
    return formEditor != null && formEditor.getForm().getComponentByName(name) != null;
  }

  public boolean blockExists(String yailId) {
    return blocksEditor != null && blocksEditor.blockExists(yailId);
  }
}
```

---

### Task 7: Refactor AIDesignerOperations + AIBlockOperations to accept ScreenExecutionContext

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/executor/AIDesignerOperations.java`
- Modify: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/executor/AIBlockOperations.java`

- [ ] **Step 1: Update AIDesignerOperations — add context parameter to all 4 methods**

Each method currently calls `AIEditorState.getCurrentFormEditor()`. Change each to accept `ScreenExecutionContext context` and use `context.getFormEditor()`:

```java
  // Line 29: was executeAddComponent(JSONObject json)
  public static void executeAddComponent(JSONObject json, ScreenExecutionContext context) {
    YaFormEditor formEditor = context.getFormEditor();
    // ... rest unchanged, just use formEditor from context
  }
```

Apply to all 4 methods: `executeAddComponent`, `executeSetProperty`, `executeRenameComponent`, `executeDeleteComponent`.

- [ ] **Step 2: Update AIBlockOperations — add context parameter to both methods**

```java
  // Line 22: was executeWriteBlock(JSONObject json)
  public static void executeWriteBlock(JSONObject json, ScreenExecutionContext context) {
    YaBlocksEditor blocksEditor = context.getBlocksEditor();
    // ... rest unchanged
  }
```

Apply to both: `executeWriteBlock`, `executeDeleteBlock`.

---

### Task 8: Refactor AIOperationExecutor to use ScreenExecutionContext

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/executor/AIOperationExecutor.java`

- [ ] **Step 1: Add context field and new entry point**

```java
  private final ScreenExecutionContext context;

  // Existing constructor gains context parameter
  public AIOperationExecutor(ScreenExecutionContext context) {
    this.context = context;
  }

  /** New entry point for screen-targeted execution (phases 2-5 only). */
  public static void executeForScreen(ScreenExecutionContext context,
      List<AIOperation> operations, ExecutionCallback callback) {
    AIOperationExecutor executor = new AIOperationExecutor(context);
    executor.executeScreenOps(operations, callback);
  }
```

- [ ] **Step 2: Update existing execute() to use forCurrentScreen()**

The existing `execute()` method (line 92) should create `ScreenExecutionContext.forCurrentScreen()` and pass it to the constructor. This maintains backward compatibility.

- [ ] **Step 3: Update all 13 AIEditorState call sites**

Replace all `AIEditorState.getCurrentFormEditor()` / `getCurrentBlocksEditor()` / `componentExists()` / `blockExists()` / `screenExists()` calls with `context.getFormEditor()` / `context.getBlocksEditor()` / `context.componentExists()` / `context.blockExists()`.

Key sites:
- `isIdempotentSkip()` (7 calls at lines 335-358): use `context.componentExists()`, `context.blockExists()`
- `dispatchSyncOp()` (6 calls via AIDesignerOperations/AIBlockOperations): pass `context` to each
- `runSyncPhases()` finally block (line 279): use `context.getBlocksEditor()`; skip `sendComponentData` for non-current screens
- `setPendingBlockDeletions/addPendingBlockUpserts/clearPendingBlockDeletions`: use `context.getBlocksEditor()`

- [ ] **Step 4: Skip sendComponentData for background screens**

In `runSyncPhases()` finally block, only call `sendComponentData` if the context targets the visible screen:

```java
  if (context.getScreenName() == null) {
    // Current screen — push to Companion
    YaBlocksEditor blocksEditor = context.getBlocksEditor();
    if (blocksEditor != null) {
      blocksEditor.sendComponentData(true);
    }
  }
  // Background screens: makeActive() handles Companion update when user navigates there
```

---

### Task 9: Add AIContextCollector.buildRequestForScreen()

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/AIContextCollector.java`

- [ ] **Step 1: Add screen-targeted request builder**

```java
  /**
   * Builds a request using a specific screen's background editors instead of
   * the visible screen. Used by child conversations during orchestration.
   */
  public AIAgentRequest buildRequestForScreen(String screenName, String userMessage) {
    YaProjectEditor projectEditor = getProjectEditor();
    YaFormEditor formEditor = (YaFormEditor) projectEditor.getFormFileEditor(screenName);
    YaBlocksEditor blocksEditor = (YaBlocksEditor) projectEditor.getBlocksFileEditor(screenName);

    AIAgentRequest request = new AIAgentRequest();
    request.setUserMessage(userMessage);
    request.setProjectId(getCurrentProjectId());
    request.setScreenName(screenName);
    request.setScreenComponentsJson(formEditor.getPropertiesJson());
    request.setBlocksYail(blocksEditor.getBlocksYail());
    request.setCurrentView("Designer"); // Child starts in Designer view
    request.setProjectSnapshot(buildProjectSnapshot());
    request.setOrchestrationMode(true);
    request.setTargetScreen(screenName);
    request.setPlanExecuteMode(false); // Executing, not planning
    return request;
  }
```

Add a `getProjectEditor()` helper if one doesn't exist, to get the current `YaProjectEditor` from `Ode`.

---

### Task 10: Create ChildConversation class

**Files:**
- Create: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/ChildConversation.java`

- [ ] **Step 1: Create the class**

`ChildConversation` manages one child agent's RPC loop for a single screen. It fires `processRequest`, handles `hasMore` continuations, validation retries, and reports batches to the orchestration manager.

Key responsibilities:
- Owns a `ScreenExecutionContext` for its target screen
- Uses `AIContextCollector.buildRequestForScreen()` for all requests
- Polls its own StreamBuffer via `getRequestStatus(projectId, screenName)`
- Reports completed batches to a callback (the orchestration manager's FIFO queue)
- Pauses after each batch until the orchestration manager signals approval
- Resumes continuation after approval (builds fresh context from updated background editor)

```java
package com.google.appinventor.client.editor.youngandroid.aiagent;

// Manages one child agent conversation for a single screen.
public class ChildConversation {
  public interface BatchCallback {
    /** Called when the child produces a batch of operations. */
    void onBatchReady(ChildConversation child, AIAgentResponse response);
    /** Called when the child finishes (hasMore=false, no more batches). */
    void onComplete(ChildConversation child);
    /** Called when the child fails. */
    void onError(ChildConversation child, String error);
  }

  private final String screenName;
  private final String stepDescription;
  private final ScreenExecutionContext context;
  private final AIContextCollector contextCollector;
  private final AIAgentServiceAsync aiAgentService;
  private final BatchCallback callback;
  private Timer pollingTimer;
  private boolean waitingForApproval;

  public ChildConversation(String screenName, String stepDescription,
      ScreenExecutionContext context, AIContextCollector contextCollector,
      AIAgentServiceAsync aiAgentService, BatchCallback callback) { ... }

  /** Starts the child by firing processRequest. */
  public void start() { ... }

  /** Called by the orchestration manager after a batch is approved and applied. */
  public void resumeAfterApproval() { ... }

  /** Cancels this child conversation. */
  public void cancel() { ... }

  public String getScreenName() { return screenName; }
  public ScreenExecutionContext getContext() { return context; }
}
```

The internal flow mirrors `AIResponseOrchestrator` but simplified: no plan handling, no operation preview — just fire RPCs, handle responses, report batches.

---

### Task 11: Create AIOrchestrationManager + FIFO queue

**Files:**
- Create: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/AIOrchestrationManager.java`

- [ ] **Step 1: Create the manager class**

The orchestration manager:
- Parses the approved plan into screen step groups
- Ensures all target editors are loaded (editor readiness check)
- Spawns `ChildConversation` instances (one per screen)
- Maintains a FIFO queue of pending batches
- Presents batches one at a time to the user (via the existing operation preview UI)
- On approval: applies operations via `AIOperationExecutor.executeForScreen()`, signals the child to resume
- On rejection: cancels all children, returns to parent chat
- On "Apply & Accept All": auto-approves all current and future batches
- Tracks per-plan RPC budget (max 20)

```java
package com.google.appinventor.client.editor.youngandroid.aiagent;

public class AIOrchestrationManager implements ChildConversation.BatchCallback {
  private final List<ChildConversation> activeChildren = new ArrayList<>();
  private final Queue<PendingBatch> batchQueue = new LinkedList<>();
  private boolean autoAcceptAll;
  private int totalRpcCount;
  private static final int MAX_RPCS_PER_PLAN = 20;
  private static final int MAX_CHILDREN = 5;

  /** A batch waiting for user approval. */
  private static class PendingBatch {
    final ChildConversation child;
    final AIAgentResponse response;
    PendingBatch(ChildConversation child, AIAgentResponse response) { ... }
  }

  /** Starts orchestration for an approved plan. */
  public void executePlan(String planJson, AIResponseOrchestrator.ChatCallback callback) {
    // 1. Parse plan, extract screen steps (skip __project__ — already applied)
    // 2. Editor readiness check for each target screen
    // 3. Spawn ChildConversation per screen
    // 4. Each child.start() fires processRequest in parallel
  }

  // ---- ChildConversation.BatchCallback ----

  @Override
  public void onBatchReady(ChildConversation child, AIAgentResponse response) {
    batchQueue.add(new PendingBatch(child, response));
    if (autoAcceptAll) {
      processNextBatch();
    } else {
      presentNextBatch(); // Show to user if nothing else pending
    }
  }

  /** Applies the next batch in the queue. */
  private void processNextBatch() { ... }

  /** Shows the next batch to the user for approval. */
  private void presentNextBatch() { ... }

  /** Called when user approves the current batch. */
  public void approveBatch() { ... }

  /** Called when user rejects — hard stop, cancel all. */
  public void rejectBatch(String feedback) { ... }

  public void setAutoAcceptAll() { this.autoAcceptAll = true; processAllQueued(); }
}
```

---

### Task 12: Wire orchestration into AIResponseOrchestrator

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/AIResponseOrchestrator.java`

- [ ] **Step 1: Replace executePlanSequentially with orchestrated execution**

The current `executePlanSequentially()` sends the whole plan as a platform message to the parent. Replace it: when the plan has multiple screen steps, delegate to `AIOrchestrationManager`. When it has a single screen step, keep the sequential path.

```java
  private void executePlanSequentially(String planJson) {
    List<PlanStep> steps = PlanParser.parse(planJson);
    List<PlanStep> screenSteps = steps.stream()
        .filter(s -> !"__project__".equals(s.screen))
        .collect(Collectors.toList());

    if (screenSteps.size() <= 1) {
      // Single screen — use sequential parent execution (Phase A path)
      String planMessage = "<system>\n...plan...\n</system>";
      sendPlatformMessage(planMessage);
    } else {
      // Multiple screens — use orchestration manager
      callback.onPlanExecutionStarted();
      orchestrationManager.executePlan(planJson, callback);
    }
  }
```

- [ ] **Step 2: Add status cards rendering**

Add a `renderStatusCards()` method to `AIChatRenderer` that shows a compact card per active child with screen name + current status text. Update via polling from each child's StreamBuffer.

- [ ] **Step 3: Label batches with screen name in operation preview**

When presenting a batch from the FIFO queue, prepend the screen name label (e.g., "[Screen1]") to the operation preview header so the user knows which screen's operations they're reviewing.

---

## Testing

### Manual Integration Test Checklist

- [ ] Single-screen plan: falls back to sequential parent execution (Phase A path)
- [ ] Multi-screen plan: spawns child conversations, operations appear in FIFO order
- [ ] "Apply" on a batch: operations applied to correct screen's editor, child continues
- [ ] "Apply & Accept All": all batches auto-apply, children run to completion
- [ ] "Reject": all children cancelled, remaining queue discarded, return to parent chat
- [ ] User sends message during plan card: treated as rejection (Phase A behavior)
- [ ] Screen navigation during execution: user can switch screens freely
- [ ] Cancel (Stop button) during execution: all children cancelled
- [ ] New screen creation in plan: project ops applied first, then children start
- [ ] Rate limit: client stops spawning after 20 RPCs
- [ ] Editor readiness: if an editor isn't loaded, waits before starting child
