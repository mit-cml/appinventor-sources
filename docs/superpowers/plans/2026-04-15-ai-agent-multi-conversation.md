# AI Agent Multi-Conversation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Give each project multiple named, persistent conversations that the user can list, resume, rename, and delete from the AI chat dialog — and fix the latent stateful-provider resume gap so that any conversation can be restored from stored history on any provider.

**Architecture:** Introduce a `ConversationData` Datastore entity holding conversation metadata; switch the main `AIConversationState` Memcache key from `projectId` to `conversationId`; thread a `conversationId` field through `AIAgentRequest` / `AIAgentResponse`; teach the three stateful providers (OpenAI Responses, Gemini, Vertex) to replay full history when `providerRef` is null; swap `AIChatDialog` between a chat view and a list view; render per-message local-tz timestamps and date separators.

**Tech Stack:** Java (GWT client + server), JUnit 3 (`junit.framework.TestCase`), Objectify (Datastore), Google App Engine Memcache, Ant build.

**Spec:** [`docs/superpowers/specs/2026-04-15-ai-agent-multi-conversation-design.md`](../specs/2026-04-15-ai-agent-multi-conversation-design.md)

**Reference:** [`CONTRIBUTING_AI.md`](../../../CONTRIBUTING_AI.md)

---

## Before Starting

**Build commands** (run from `appinventor/`):
- `ant -f appengine/build.xml AiSharedLib` — shared DTOs
- `ant -f appengine/build.xml AiServerLib` — server + resources
- `ant -f appengine/build.xml AiClientLib` — client GWT code
- `ant -f appengine/build.xml AiServerLibTests` — server-side unit tests
- `ant -f appengine/build.xml tests` — all tests

**Conventions:**
- Copyright header on every new Java file.
- JUnit 3 everywhere on server: extend `junit.framework.TestCase`, method names start with `test`, no annotations.
- 2-space indentation in Java. Follow existing file style exactly.
- Commit after each task finishes. Conventional-commit prefixes: `feat:`, `test:`, `chore:`, `docs:`, `fix:`.
- Don't reformat unrelated code in the files you touch.

**File inventory (high-level):**

Server:
- `appengine/src/com/google/appinventor/server/storage/StoredData.java` — add `ConversationData` entity.
- `appengine/src/com/google/appinventor/server/storage/StorageIo.java` — add CRUD + convId-keyed conversation-state methods.
- `appengine/src/com/google/appinventor/server/storage/ObjectifyStorageIo.java` — implementations + register the entity + project-delete cascade job.
- `appengine/src/com/google/appinventor/server/aiagent/ConversationManager.java` — rekey main methods on `conversationId`, add CRUD wrappers, bump `updatedAt` in `storeMessage`.
- `appengine/src/com/google/appinventor/server/aiagent/AIAgentEngine.java` — always load history, route by `conversationId`, relax continuation guard.
- `appengine/src/com/google/appinventor/server/aiagent/AIAgentServiceImpl.java` — new RPC implementations + ownership checks.
- `appengine/src/com/google/appinventor/server/aiagent/llm/OpenAIProvider.java` — history-replay fallback branch.
- `appengine/src/com/google/appinventor/server/aiagent/llm/GeminiProvider.java` — history-replay fallback branch.
- `appengine/src/com/google/appinventor/server/aiagent/llm/VertexProvider.java` — history-replay fallback branch.

Shared:
- `appengine/src/com/google/appinventor/shared/rpc/aiagent/AIAgentRequest.java` — add `conversationId`.
- `appengine/src/com/google/appinventor/shared/rpc/aiagent/AIAgentResponse.java` — add `conversationId`.
- `appengine/src/com/google/appinventor/shared/rpc/aiagent/AIConversationMessage.java` — add `timestamp`.
- `appengine/src/com/google/appinventor/shared/rpc/aiagent/AIConversationSummary.java` — **new**.
- `appengine/src/com/google/appinventor/shared/rpc/aiagent/AIAgentService.java` + `AIAgentServiceAsync.java` — new methods.

Client:
- `appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/AIResponseOrchestrator.java` — track `currentConversationId`, new RPC wrappers.
- `appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/AIChatRenderer.java` — date separators + per-bubble timestamps.
- `appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/dialog/ConversationListPanel.java` — **new**.
- `appengine/src/com/google/appinventor/client/editor/youngandroid/AIChatDialog.java` — two-view layout, header "☰" button.
- `appengine/src/com/google/appinventor/client/OdeMessages.java` — new i18n keys.
- `blocklyeditor/src/msg/ai_blockly/messages.json` (+ 22 locale files) — same keys.

Tests (all JUnit 3):
- `appengine/tests/com/google/appinventor/server/aiagent/ConversationManagerTest.java`
- `appengine/tests/com/google/appinventor/server/aiagent/AIAgentServiceImplTest.java` (new or extended)
- `appengine/tests/com/google/appinventor/server/aiagent/AIAgentEngineTest.java` (new or extended)
- `appengine/tests/com/google/appinventor/server/aiagent/llm/OpenAIProviderHistoryReplayTest.java`
- `appengine/tests/com/google/appinventor/server/aiagent/llm/GeminiProviderHistoryReplayTest.java`
- `appengine/tests/com/google/appinventor/server/aiagent/llm/VertexProviderHistoryReplayTest.java`

Docs:
- `CONTRIBUTING_AI.md` — rewrite the Conversation Management section; add the new RPC methods; mention rehydration.

---

## Phase 1 — Data Layer

Lay the storage foundation. Order matters: entity → StorageIo interface → ObjectifyStorageIo impl → ConversationManager.

### Task 1: Add `ConversationData` entity

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/storage/StoredData.java`

- [ ] **Step 1: Add the entity class**

At the bottom of `StoredData` (just before the `ProjectNotFoundException` inner class), insert:

```java
// AI Agent conversation metadata. One row per user-facing conversation.
// A project can have many conversations; each has a UUID (matched by
// ConversationMessageData.conversationId) and an optional user-set title.
// Retained until the user deletes the conversation or deletes the project.
@Unindexed
public static final class ConversationData implements Serializable {
  @Id Long id;

  // Conversation UUID. Matches ConversationMessageData.conversationId.
  @Indexed public String conversationId;

  // Project that owns this conversation.
  @Indexed public long projectId;

  // User that owns this conversation.
  @Indexed public String userId;

  // User-facing title. Null/empty means the client renders a date fallback.
  public String title;

  // Creation timestamp (System.currentTimeMillis()).
  public long createdAt;

  // Bumped whenever a new message is appended. Sort key for the list UI.
  public long updatedAt;
}
```

Update the `expiresAt` comment on `ConversationMessageData` to:

```java
// Formerly a cleanup sort key when messages auto-expired after 24h.
// Retained for backward compatibility with rows written by the old
// single-conversation model; new rows leave this at 0.
@Indexed public long expiresAt;
```

- [ ] **Step 2: Register the entity in Objectify**

Modify: `appinventor/appengine/src/com/google/appinventor/server/storage/ObjectifyStorageIo.java`

Find the block of `ObjectifyService.register(...)` calls around line 220 (near `register(ConversationMessageData.class)`) and add right after it:

```java
ObjectifyService.register(ConversationData.class);
```

Also add the import near the top:

```java
import com.google.appinventor.server.storage.StoredData.ConversationData;
```

- [ ] **Step 3: Build to confirm compilation**

Run: `cd appinventor && ant -f appengine/build.xml AiServerLib`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/server/storage/StoredData.java \
        appinventor/appengine/src/com/google/appinventor/server/storage/ObjectifyStorageIo.java
git commit -m "feat(ai-agent): add ConversationData Datastore entity"
```

---

### Task 2: Add StorageIo interface methods (conversation CRUD + convId-keyed state)

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/storage/StorageIo.java`

- [ ] **Step 1: Add new method signatures**

After the existing screen-scoped `clearAIStreamCancelled(long projectId, String screenName)` declaration, add a new section:

```java
// ---------- Multi-conversation support ----------

/**
 * Creates a new conversation row for the given user + project. Returns the
 * generated conversation UUID. Callers use this UUID when saving
 * {@link AIConversationState} and when storing messages.
 */
String createConversation(String userId, long projectId);

/**
 * Loads conversation metadata by UUID. Returns null if not found.
 */
StoredData.ConversationData getConversationMetadata(String conversationId);

/**
 * Lists all conversations for a project owned by the given user,
 * sorted by {@code updatedAt} descending (most recent first).
 */
List<StoredData.ConversationData> listConversations(String userId, long projectId);

/**
 * Sets the conversation title. Null or empty restores the fallback state.
 * No-op if the conversation does not exist.
 */
void renameConversation(String conversationId, String title);

/**
 * Bumps {@code updatedAt} on the conversation row. Called from message
 * storage paths so the list view can sort by recent activity.
 */
void touchConversation(String conversationId, long updatedAt);

/**
 * Deletes a conversation: metadata row, all its messages, and its
 * Memcache state entry.
 */
void deleteConversation(String conversationId);

// ---------- ConversationId-keyed AI conversation state ----------

/** Saves AI conversation state keyed by conversationId (replaces the project-keyed overload). */
void saveAIConversationStateByConvId(String conversationId, AIConversationState state);

/** Loads AI conversation state keyed by conversationId. Returns null if absent. */
AIConversationState getAIConversationStateByConvId(String conversationId);

/** Evicts the Memcache state entry for the conversation. */
void clearAIConversationStateByConvId(String conversationId);
```

Add the `StoredData` import if not already present:

```java
import com.google.appinventor.server.storage.StoredData;
```

- [ ] **Step 2: Build (abstract methods only; impls come next)**

Run: `cd appinventor && ant -f appengine/build.xml AiServerLib`
Expected: FAIL — `ObjectifyStorageIo` does not implement the new abstract methods. That's fine; next task adds them.

- [ ] **Step 3: Don't commit yet.** Continue to Task 3 and commit both together.

---

### Task 3: Implement ObjectifyStorageIo methods

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/storage/ObjectifyStorageIo.java`

- [ ] **Step 1: Write the test file**

Create: `appinventor/tests/com/google/appinventor/server/storage/ObjectifyStorageIoConversationTest.java`

```java
// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.storage;

import com.google.appinventor.server.LocalDatastoreTestCase;
import com.google.appinventor.server.storage.StoredData.ConversationData;

import java.util.List;

/**
 * Tests for multi-conversation StorageIo methods: CRUD on ConversationData
 * + convId-keyed AIConversationState.
 */
public class ObjectifyStorageIoConversationTest extends LocalDatastoreTestCase {

  private ObjectifyStorageIo storage;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    storage = new ObjectifyStorageIo();
  }

  public void testCreateAndGetConversation() {
    String id = storage.createConversation("user-a", 42L);
    assertNotNull(id);
    ConversationData cd = storage.getConversationMetadata(id);
    assertNotNull(cd);
    assertEquals("user-a", cd.userId);
    assertEquals(42L, cd.projectId);
    assertNull(cd.title);
    assertTrue(cd.createdAt > 0);
    assertEquals(cd.createdAt, cd.updatedAt);
  }

  public void testListConversationsSortedByUpdatedAtDesc() throws InterruptedException {
    String a = storage.createConversation("user-a", 42L);
    Thread.sleep(2);
    String b = storage.createConversation("user-a", 42L);
    Thread.sleep(2);
    storage.touchConversation(a, System.currentTimeMillis());
    List<ConversationData> list = storage.listConversations("user-a", 42L);
    assertEquals(2, list.size());
    assertEquals(a, list.get(0).conversationId); // most recently touched
    assertEquals(b, list.get(1).conversationId);
  }

  public void testListConversationsFiltersByUserAndProject() {
    storage.createConversation("user-a", 42L);
    storage.createConversation("user-b", 42L);
    storage.createConversation("user-a", 43L);
    assertEquals(1, storage.listConversations("user-a", 42L).size());
    assertEquals(1, storage.listConversations("user-b", 42L).size());
    assertEquals(1, storage.listConversations("user-a", 43L).size());
  }

  public void testRenameAndFallback() {
    String id = storage.createConversation("user-a", 42L);
    storage.renameConversation(id, "My chat");
    assertEquals("My chat", storage.getConversationMetadata(id).title);
    storage.renameConversation(id, "");
    assertNull(storage.getConversationMetadata(id).title);
    storage.renameConversation(id, null);
    assertNull(storage.getConversationMetadata(id).title);
  }

  public void testDeleteConversationRemovesEverything() {
    String id = storage.createConversation("user-a", 42L);
    storage.storeAIConversationMessage(id, System.currentTimeMillis(), 0,
        StoredData.MessageRole.USER, "hello", true);
    AIConversationState state = new AIConversationState("anthropic", id, null);
    storage.saveAIConversationStateByConvId(id, state);

    storage.deleteConversation(id);

    assertNull(storage.getConversationMetadata(id));
    assertTrue(storage.loadAIConversationMessages(id).isEmpty());
    assertNull(storage.getAIConversationStateByConvId(id));
  }

  public void testConvIdKeyedStateRoundtrip() {
    String id = "conv-uuid-1";
    AIConversationState s = new AIConversationState("openai", id, "resp-abc");
    storage.saveAIConversationStateByConvId(id, s);
    AIConversationState got = storage.getAIConversationStateByConvId(id);
    assertNotNull(got);
    assertEquals("openai", got.getProviderName());
    assertEquals(id, got.getConversationId());
    assertEquals("resp-abc", got.getProviderRef());

    storage.clearAIConversationStateByConvId(id);
    assertNull(storage.getAIConversationStateByConvId(id));
  }
}
```

- [ ] **Step 2: Add constants and implementations to `ObjectifyStorageIo`**

Near the existing `AI_CONV_CACHE_KEY_PREFIX` constant (line ~2926), add:

```java
private static final String AI_CONV_META_CACHE_KEY_PREFIX = "ai_conv_meta:";
// Reused for conversationId-keyed state; project-keyed methods delegate
// to this path after resolving the "active" conversation, if needed.
```

After the existing project-keyed `clearAIConversationState` (around line 2944), insert the conversationId-keyed methods:

```java
@Override
public void saveAIConversationStateByConvId(String conversationId, AIConversationState state) {
  memcache.put(AI_CONV_CACHE_KEY_PREFIX + conversationId, state,
      Expiration.byDeltaSeconds(CONVERSATION_TTL_SECONDS));
}

@Override
public AIConversationState getAIConversationStateByConvId(String conversationId) {
  return (AIConversationState) memcache.get(AI_CONV_CACHE_KEY_PREFIX + conversationId);
}

@Override
public void clearAIConversationStateByConvId(String conversationId) {
  memcache.delete(AI_CONV_CACHE_KEY_PREFIX + conversationId);
}
```

After the existing `cleanupConversationMessages` method (around line 3033), add the new CRUD block:

```java
// ---------- Multi-conversation CRUD (ConversationData) ----------

@Override
public String createConversation(String userId, long projectId) {
  String convId = UUID.randomUUID().toString();
  long now = System.currentTimeMillis();
  Objectify ofy = ObjectifyService.begin();
  ConversationData cd = new ConversationData();
  cd.conversationId = convId;
  cd.projectId = projectId;
  cd.userId = userId;
  cd.title = null;
  cd.createdAt = now;
  cd.updatedAt = now;
  ofy.put(cd);
  return convId;
}

@Override
public ConversationData getConversationMetadata(String conversationId) {
  Objectify ofy = ObjectifyService.begin();
  return ofy.query(ConversationData.class)
      .filter("conversationId", conversationId)
      .first().now();
}

@Override
public List<ConversationData> listConversations(String userId, long projectId) {
  Objectify ofy = ObjectifyService.begin();
  List<ConversationData> results = ofy.query(ConversationData.class)
      .filter("projectId", projectId)
      .list();
  List<ConversationData> filtered = new ArrayList<>(results.size());
  for (ConversationData cd : results) {
    if (userId.equals(cd.userId)) {
      filtered.add(cd);
    }
  }
  Collections.sort(filtered, new Comparator<ConversationData>() {
    @Override public int compare(ConversationData a, ConversationData b) {
      return Long.compare(b.updatedAt, a.updatedAt); // desc
    }
  });
  return filtered;
}

@Override
public void renameConversation(String conversationId, String title) {
  Objectify ofy = ObjectifyService.begin();
  ConversationData cd = ofy.query(ConversationData.class)
      .filter("conversationId", conversationId).first().now();
  if (cd == null) return;
  if (title != null) {
    title = title.trim();
    if (title.isEmpty()) title = null;
    else if (title.length() > 120) title = title.substring(0, 120);
  }
  cd.title = title;
  ofy.put(cd);
}

@Override
public void touchConversation(String conversationId, long updatedAt) {
  Objectify ofy = ObjectifyService.begin();
  ConversationData cd = ofy.query(ConversationData.class)
      .filter("conversationId", conversationId).first().now();
  if (cd == null) return;
  cd.updatedAt = updatedAt;
  ofy.put(cd);
}

@Override
public void deleteConversation(String conversationId) {
  Objectify ofy = ObjectifyService.begin();
  ConversationData cd = ofy.query(ConversationData.class)
      .filter("conversationId", conversationId).first().now();
  if (cd != null) {
    ofy.delete(cd);
  }
  ofy.delete(ofy.query(ConversationMessageData.class)
      .filter("conversationId", conversationId).fetchKeys());
  memcache.delete(AI_CONV_CACHE_KEY_PREFIX + conversationId);
}
```

Add imports as needed: `java.util.UUID`, `java.util.ArrayList`, `java.util.Collections`, `java.util.Comparator`.

- [ ] **Step 3: Stop writing `expiresAt` on new messages**

In the existing `storeAIConversationMessage` overload (around line 2954), change:

```java
msg.expiresAt = timestamp + CONVERSATION_TTL_SECONDS * 1000L;
```

to:

```java
msg.expiresAt = 0; // No TTL under multi-conversation model; retained until user deletes.
```

In `loadAIConversationMessages` (around line 2980), change the loop that filters on `expiresAt`:

```java
// OLD
for (ConversationMessageData m : messages) {
  if (m.expiresAt > now) {
    result.add(new ChatMessage(...));
  }
}
// NEW
for (ConversationMessageData m : messages) {
  result.add(new ChatMessage(...));
}
```

Leave `cleanupConversationMessages` as-is for now — we remove it from the call path in Task 6, and delete the method body in Task 21.

- [ ] **Step 4: Run the storage tests**

Run: `cd appinventor && ant -f appengine/build.xml AiServerLibTests -Dtest.class=com.google.appinventor.server.storage.ObjectifyStorageIoConversationTest`

If that target signature isn't supported, run the full `AiServerLibTests` target and grep the output for the class name.

Expected: all tests pass.

- [ ] **Step 5: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/server/storage/StorageIo.java \
        appinventor/appengine/src/com/google/appinventor/server/storage/ObjectifyStorageIo.java \
        appinventor/tests/com/google/appinventor/server/storage/ObjectifyStorageIoConversationTest.java
git commit -m "feat(ai-agent): storage layer for multi-conversation metadata"
```

---

### Task 4: Surface `timestamp` in `AIConversationMessage` + ChatMessage loading

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/shared/rpc/aiagent/AIConversationMessage.java`
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/ChatMessage.java` (verify the field already exists; add only if missing)
- Modify: `appinventor/appengine/src/com/google/appinventor/server/storage/ObjectifyStorageIo.java` (pass timestamp to ChatMessage)
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/AIAgentEngine.java` (propagate timestamp to AIConversationMessage when building history response)

- [ ] **Step 1: Add `timestamp` field + accessors to `AIConversationMessage`**

```java
private long timestamp;

public long getTimestamp() {
  return timestamp;
}

public void setTimestamp(long timestamp) {
  this.timestamp = timestamp;
}
```

Update the existing constructor:

```java
public AIConversationMessage(String role, String text, long timestamp) {
  this.role = role;
  this.text = text;
  this.timestamp = timestamp;
}
```

Keep the no-arg constructor; keep the old 2-arg constructor as a convenience that sets timestamp to 0 so existing callers compile, but grep for its call sites and update them in the same commit.

- [ ] **Step 2: Verify or add `timestamp` on `ChatMessage`**

Read `ChatMessage.java`. If `timestamp` is absent, add:

```java
private final long timestamp;

public ChatMessage(String role, String text, String structuredContent, boolean display,
    long timestamp) {
  this.role = role;
  this.text = text;
  this.structuredContent = structuredContent;
  this.display = display;
  this.timestamp = timestamp;
}

public long getTimestamp() {
  return timestamp;
}
```

Keep the existing constructor for backward compat with a default of 0.

- [ ] **Step 3: Pass `m.timestamp` into `ChatMessage` inside `loadAIConversationMessages`**

In `ObjectifyStorageIo.loadAIConversationMessages`:

```java
result.add(new ChatMessage(
    m.role != null ? m.role : "user",
    m.text, m.structuredContent, m.display, m.timestamp));
```

- [ ] **Step 4: Propagate timestamp when building the RPC response**

In `AIAgentEngine.getConversationHistory`, where it currently builds `AIConversationMessage` DTOs, update the constructor call to pass `cm.getTimestamp()`.

- [ ] **Step 5: Build + run existing tests**

Run:
```
cd appinventor && ant -f appengine/build.xml AiServerLib AiSharedLib
cd appinventor && ant -f appengine/build.xml AiServerLibTests
```
Expected: BUILD SUCCESSFUL and all tests pass.

- [ ] **Step 6: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/shared/rpc/aiagent/AIConversationMessage.java \
        appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/ChatMessage.java \
        appinventor/appengine/src/com/google/appinventor/server/storage/ObjectifyStorageIo.java \
        appinventor/appengine/src/com/google/appinventor/server/aiagent/AIAgentEngine.java
git commit -m "feat(ai-agent): surface per-message timestamps in history DTO"
```

---

## Phase 2 — Server Engine + Provider Rehydration

Rewire ConversationManager to be convId-based, teach AIAgentEngine to always load history, and add history-replay fallback to the three stateful providers.

### Task 5: Rewrite `ConversationManager` around `conversationId`

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/ConversationManager.java`
- Create: `appinventor/tests/com/google/appinventor/server/aiagent/ConversationManagerTest.java`

- [ ] **Step 1: Write the tests first**

```java
// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0

package com.google.appinventor.server.aiagent;

import com.google.appinventor.server.LocalDatastoreTestCase;
import com.google.appinventor.server.storage.AIConversationState;
import com.google.appinventor.server.storage.ObjectifyStorageIo;
import com.google.appinventor.server.storage.StoredData.ConversationData;
import com.google.appinventor.server.storage.StoredData.MessageRole;

import java.util.List;

public class ConversationManagerTest extends LocalDatastoreTestCase {

  private ObjectifyStorageIo storage;
  private ConversationManager cm;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    storage = new ObjectifyStorageIo();
    cm = new ConversationManager(storage);
  }

  public void testCreateAndGetByConvId() {
    String convId = cm.createConversation("user-a", 1L);
    AIConversationState s = new AIConversationState("anthropic", convId, null);
    cm.saveConversation(convId, s);
    AIConversationState got = cm.getConversation(convId);
    assertNotNull(got);
    assertEquals(convId, got.getConversationId());
  }

  public void testStoreMessageBumpsUpdatedAt() throws InterruptedException {
    String convId = cm.createConversation("user-a", 1L);
    long initial = cm.getConversationMetadata(convId).updatedAt;
    Thread.sleep(3);
    cm.storeMessage(convId, MessageRole.USER, "hi", true);
    long after = cm.getConversationMetadata(convId).updatedAt;
    assertTrue(after > initial);
  }

  public void testDeleteConversationRemovesMetadataAndMessages() {
    String convId = cm.createConversation("user-a", 1L);
    cm.storeMessage(convId, MessageRole.USER, "hi", true);
    cm.deleteConversation(convId);
    assertNull(cm.getConversationMetadata(convId));
    assertTrue(cm.loadConversation(convId).isEmpty());
  }

  public void testRenameUpdatesTitle() {
    String convId = cm.createConversation("user-a", 1L);
    cm.renameConversation(convId, "  My thread  ");
    ConversationData cd = cm.getConversationMetadata(convId);
    assertEquals("My thread", cd.title);
  }

  public void testScreenScopedMethodsUnchanged() {
    // Sanity check: screen-scoped still compiles + roundtrips via memcache.
    AIConversationState s = new AIConversationState("anthropic", "conv-x", null);
    cm.saveConversation(1L, "Screen1", s);
    assertNotNull(cm.getConversation(1L, "Screen1"));
    cm.clearConversation(1L, "Screen1");
    assertNull(cm.getConversation(1L, "Screen1"));
  }
}
```

- [ ] **Step 2: Rewrite ConversationManager main-conversation methods**

Replace the project-keyed main-conversation methods with convId-keyed ones. Preserve the screen-scoped (`projectId + screenName`) overloads — they are used by orchestration children.

```java
// ---------- Conversation state (convId-keyed) ----------

public AIConversationState getConversation(String conversationId) {
  return storageIo.getAIConversationStateByConvId(conversationId);
}

public void saveConversation(String conversationId, AIConversationState state) {
  storageIo.saveAIConversationStateByConvId(conversationId, state);
}

public void clearConversationState(String conversationId) {
  storageIo.clearAIConversationStateByConvId(conversationId);
}

// ---------- Conversation metadata CRUD ----------

public String createConversation(String userId, long projectId) {
  return storageIo.createConversation(userId, projectId);
}

public StoredData.ConversationData getConversationMetadata(String conversationId) {
  return storageIo.getConversationMetadata(conversationId);
}

public List<StoredData.ConversationData> listConversations(String userId, long projectId) {
  return storageIo.listConversations(userId, projectId);
}

public void renameConversation(String conversationId, String title) {
  storageIo.renameConversation(conversationId, title);
}

public void deleteConversation(String conversationId) {
  storageIo.deleteConversation(conversationId);
}
```

In `storeMessage` (both overloads), after the `storageIo.storeAIConversationMessage(...)` call:

```java
storageIo.touchConversation(conversationId, now);
// Removed: storageIo.cleanupConversationMessages();
```

Remove the existing `getConversation(long projectId)` / `saveConversation(long, state)` / `clearConversation(long)` methods. The callers are AIAgentEngine + AIAgentServiceImpl; those get updated in Tasks 6 and 11.

- [ ] **Step 3: Run the test**

Run: `cd appinventor && ant -f appengine/build.xml AiServerLibTests`
Expected: `ConversationManagerTest` passes. Other tests may fail because `AIAgentEngine` still calls removed methods — that's OK; Task 6 fixes it.

- [ ] **Step 4: Do NOT commit yet.** Proceed to Task 6.

---

### Task 6: Route `AIAgentEngine` by `conversationId` + always load history

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/shared/rpc/aiagent/AIAgentRequest.java`
- Modify: `appinventor/appengine/src/com/google/appinventor/shared/rpc/aiagent/AIAgentResponse.java`
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/AIAgentEngine.java`
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/AIAgentServiceImpl.java` (callers of removed methods)

- [ ] **Step 1: Add `conversationId` to the DTOs**

`AIAgentRequest.java` — add field + accessors:

```java
private String conversationId;

public String getConversationId() {
  return conversationId;
}

public void setConversationId(String conversationId) {
  this.conversationId = conversationId;
}
```

`AIAgentResponse.java` — add field + accessors:

```java
private String conversationId;

public String getConversationId() {
  return conversationId;
}

public void setConversationId(String conversationId) {
  this.conversationId = conversationId;
}
```

- [ ] **Step 2: Route engine by conversationId**

In `AIAgentEngine.processRequest(AIAgentRequest, String userId)`:

Early in the method, resolve / create the conversationId:

```java
String convId = request.getConversationId();
if (convId == null || convId.isEmpty()) {
  convId = conversationManager.createConversation(userId, request.getProjectId());
} else {
  StoredData.ConversationData meta = conversationManager.getConversationMetadata(convId);
  if (meta == null
      || !userId.equals(meta.userId)
      || meta.projectId != request.getProjectId()) {
    // Treat as missing; mint a new one rather than leaking existence.
    convId = conversationManager.createConversation(userId, request.getProjectId());
  }
}
```

Replace all existing uses of the project-keyed `conversationManager.getConversation(projectId)` with `conversationManager.getConversation(convId)`.

Remove the stateless-only `history` load:

```java
// OLD
List<ChatMessage> history = provider.isStateless()
    ? conversationManager.loadConversation(conv.getConversationId())
    : Collections.emptyList();

// NEW — always load; stateful providers use it only as a fallback
List<ChatMessage> history = conversationManager.loadConversation(convId);
```

Before returning, set `response.setConversationId(convId)` on every code path that constructs an `AIAgentResponse`.

In `continueRequest`, replace the guard:

```java
// OLD
if (conv == null || conv.getProviderRef() == null || conv.getProviderRef().isEmpty()) {
  streamBuffer.clear();
  return errorResponse("No continuation state available. Please start a new request.");
}
```

with:

```java
if (conv == null) {
  // Memcache expired. Rehydrate minimal state; history replay handles it.
  // Use the current configured provider name — the same value `processRequest`
  // resolves from `LLMProviderRegistry.get(...)` when creating a new state.
  // Grep existing `processRequest` for how it obtains the provider-name string.
  String providerName = /* see existing pattern in processRequest */;
  conv = new AIConversationState(providerName, convId, null);
  conversationManager.saveConversation(convId, conv);
}
// Continue without requiring providerRef — chat() will replay history when null.
```

Where `continueRequest` currently loads history conditionally on `isStateless`, change it to always load:

```java
List<ChatMessage> history = conversationManager.loadConversation(convId);
List<String> retryContext = contextMessages; // send on all providers now
```

Screen-scoped (orchestration child) paths are unchanged.

- [ ] **Step 3: Update AIAgentServiceImpl callers**

`clearConversation(long projectId)` loses its back-compat meaning. Delete the method body; we're removing it in Task 19 along with the interface entry. For now, have it throw `UnsupportedOperationException` and grep for callers — the only remaining caller should be the client's `clearConversation` request wiring, which Task 14 rewrites.

Actually — to keep the build green for the deploy window, replace the body with a no-op + `LOG.warning("clearConversation(projectId) is deprecated; use deleteConversation(conversationId)")`. Task 20 deletes it for good.

- [ ] **Step 4: Build**

Run: `cd appinventor && ant -f appengine/build.xml AiServerLib AiSharedLib`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Run all server tests**

Run: `cd appinventor && ant -f appengine/build.xml AiServerLibTests`
Expected: `ConversationManagerTest` + existing tests pass. Provider tests still pass (we haven't touched the providers yet).

- [ ] **Step 6: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/server/aiagent/ConversationManager.java \
        appinventor/appengine/src/com/google/appinventor/server/aiagent/AIAgentEngine.java \
        appinventor/appengine/src/com/google/appinventor/server/aiagent/AIAgentServiceImpl.java \
        appinventor/appengine/src/com/google/appinventor/shared/rpc/aiagent/AIAgentRequest.java \
        appinventor/appengine/src/com/google/appinventor/shared/rpc/aiagent/AIAgentResponse.java \
        appinventor/tests/com/google/appinventor/server/aiagent/ConversationManagerTest.java
git commit -m "feat(ai-agent): route engine by conversationId; always load history"
```

---

### Task 7: OpenAIProvider — replay history when `providerRef` is null

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/OpenAIProvider.java`
- Create: `appinventor/tests/com/google/appinventor/server/aiagent/llm/OpenAIProviderHistoryReplayTest.java`

- [ ] **Step 1: Understand current behavior**

Read `OpenAIProvider.chat()` from top to bottom. Note where it currently builds the `input` array for the Responses API and where it passes `previous_response_id` from `providerRef`.

- [ ] **Step 2: Write the failing test**

Create a test that constructs an `OpenAIProvider` with a mocked HTTP transport (there should be one in use already — find it via an existing `OpenAIProvider*Test.java`) and asserts the outgoing request body when `providerRef == null` and `history` is non-empty. The assertion: the `input` array contains the history turns with correct roles, and the current user message appended at the end. If no mock transport exists, add a minimal one (interface + test implementation recording the last-sent JSON).

Example shape:

```java
public void testChatReplaysHistoryWhenProviderRefIsNull() throws Exception {
  RecordingTransport t = new RecordingTransport(
      "{\"id\":\"resp-new\",\"output\":[{\"type\":\"message\",\"role\":\"assistant\",\"content\":[{\"type\":\"output_text\",\"text\":\"hi back\"}]}]}");
  OpenAIProvider p = new OpenAIProvider("key", "gpt-4o", "", t);
  List<ChatMessage> hist = Arrays.asList(
      new ChatMessage("user", "hello", null, true, 1L),
      new ChatMessage("assistant", "hi there", null, true, 2L));
  p.chat("sys", Collections.<String>emptyList(), "what's up",
      Collections.<LLMTool>emptyList(), null, hist, null, new StreamBuffer(...));
  JSONObject body = new JSONObject(t.lastBody);
  JSONArray input = body.getJSONArray("input");
  assertTrue("history replayed", input.length() >= 3);
  assertEquals("user", input.getJSONObject(0).getString("role"));
  assertEquals("assistant", input.getJSONObject(1).getString("role"));
  assertEquals("user", input.getJSONObject(2).getString("role"));
}
```

Run: `ant -f appengine/build.xml AiServerLibTests`
Expected: new test FAILS because current code only sends the current user message when `providerRef == null`.

- [ ] **Step 3: Implement the fallback**

Inside `OpenAIProvider.chat()`, after computing `responseId` from `providerRef`:

```java
if (responseId == null && history != null && !history.isEmpty()) {
  // Fallback path: replay full history as Responses API input items.
  for (ChatMessage m : history) {
    appendHistoryTurnToInput(inputArray, m);
  }
}
```

Add a private helper `appendHistoryTurnToInput(JSONArray, ChatMessage)` that:

1. If `m.getStructuredContent()` is non-null, parse it and emit the corresponding Responses API entries:
   - `tool_use` parts → `{"type":"function_call","call_id":toolUseId,"name":name,"arguments":jsonString}`
   - `tool_result` parts → `{"type":"function_call_output","call_id":toolUseId,"output":contentString}`
2. Otherwise emit a plain message entry: `{"type":"message","role":m.getRole(),"content":[{"type":"input_text"|"output_text","text":m.getText()}]}` (use `output_text` for assistant, `input_text` for user).

Append the current context messages and user message *after* the replayed history, so the API sees the full ordering.

- [ ] **Step 4: Run the test**

Expected: PASS.

- [ ] **Step 5: Run the full OpenAI test suite**

Run: `ant -f appengine/build.xml AiServerLibTests` and grep for `OpenAIProvider`.
Expected: no regressions.

- [ ] **Step 6: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/OpenAIProvider.java \
        appinventor/tests/com/google/appinventor/server/aiagent/llm/OpenAIProviderHistoryReplayTest.java
git commit -m "feat(ai-agent): OpenAIProvider replays history when providerRef is null"
```

---

### Task 8: GeminiProvider — replay history when `providerRef` is null

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/GeminiProvider.java`
- Create: `appinventor/tests/com/google/appinventor/server/aiagent/llm/GeminiProviderHistoryReplayTest.java`

- [ ] **Step 1: Failing test**

Assert that when `providerRef == null` and `history` is non-empty, the `contents` array in the outgoing request body includes the history turns:
- `user` → `{"role":"user","parts":[{"text":...}]}`
- `assistant` → `{"role":"model","parts":[{"text":...}]}`
- structured tool calls → `{"role":"model","parts":[{"functionCall":{"name":..., "args":...}}]}`
- structured tool results → `{"role":"function","parts":[{"functionResponse":{"name":..., "response":{"content":...}}}]}`

- [ ] **Step 2: Implement fallback**

Inside `GeminiProvider.chat()`, in the branch where `providerRef` is null or empty:

```java
if ((providerRef == null || providerRef.isEmpty()) && history != null && !history.isEmpty()) {
  for (ChatMessage m : history) {
    appendHistoryTurnToContents(mergedContents, m);
  }
}
```

Add the private helper mirroring the mapping above. Append current context messages + user message after the replayed history.

- [ ] **Step 3: Run test, commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/GeminiProvider.java \
        appinventor/tests/com/google/appinventor/server/aiagent/llm/GeminiProviderHistoryReplayTest.java
git commit -m "feat(ai-agent): GeminiProvider replays history when providerRef is null"
```

---

### Task 9: VertexProvider — replay history when `providerRef` is null

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/VertexProvider.java`
- Create: `appinventor/tests/com/google/appinventor/server/aiagent/llm/VertexProviderHistoryReplayTest.java`

Same structure and mapping as Gemini; Vertex uses the same `contents` JSON shape. Factor the mapping helper into a small shared utility if the two impls are textually identical (a new `GeminiContentsBuilder` static class). Don't worry about it otherwise — three copies of a 20-line helper is fine; don't over-abstract.

Commit:

```bash
git add ...
git commit -m "feat(ai-agent): VertexProvider replays history when providerRef is null"
```

---

### Task 10: Engine-level test — rehydration on cold Memcache

**Files:**
- Create or extend: `appinventor/tests/com/google/appinventor/server/aiagent/AIAgentEngineTest.java`

- [ ] **Step 1: Write the test**

Two cases:

1. `processRequest` with `conversationId = null` mints a conversation and echoes the id on the response. Verify `ConversationData` row exists.
2. `continueRequest` (or `reportExecutionErrors`) against a conversation whose `AIConversationState` Memcache entry was evicted (`clearConversationState`) does not return an error and instead proceeds with history replay. Use a fake provider that records whether `history` was passed.

- [ ] **Step 2: Run + commit**

```bash
git commit -m "test(ai-agent): engine rehydrates on cold memcache"
```

---

## Phase 3 — RPC Surface + Server CRUD Endpoints

### Task 11: Add `AIConversationSummary` shared DTO

**Files:**
- Create: `appinventor/appengine/src/com/google/appinventor/shared/rpc/aiagent/AIConversationSummary.java`

```java
// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0

package com.google.appinventor.shared.rpc.aiagent;

import com.google.gwt.user.client.rpc.IsSerializable;
import java.io.Serializable;

/**
 * Lightweight metadata for an AI Agent conversation. Used by the list view.
 */
public class AIConversationSummary implements IsSerializable, Serializable {

  private String conversationId;
  private String title;
  private long createdAt;
  private long updatedAt;

  public AIConversationSummary() {}

  public AIConversationSummary(String conversationId, String title,
      long createdAt, long updatedAt) {
    this.conversationId = conversationId;
    this.title = title;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public String getConversationId() { return conversationId; }
  public void setConversationId(String v) { this.conversationId = v; }
  public String getTitle() { return title; }
  public void setTitle(String v) { this.title = v; }
  public long getCreatedAt() { return createdAt; }
  public void setCreatedAt(long v) { this.createdAt = v; }
  public long getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(long v) { this.updatedAt = v; }
}
```

Commit:

```bash
git add appinventor/appengine/src/com/google/appinventor/shared/rpc/aiagent/AIConversationSummary.java
git commit -m "feat(ai-agent): AIConversationSummary DTO for conversation list"
```

---

### Task 12: Add new RPC methods to `AIAgentService` + Async

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/shared/rpc/aiagent/AIAgentService.java`
- Modify: `appinventor/appengine/src/com/google/appinventor/shared/rpc/aiagent/AIAgentServiceAsync.java`

- [ ] **Step 1: Add to the sync interface**

```java
List<AIConversationSummary> listConversations(long projectId);
AIConversationSummary renameConversation(String conversationId, String newTitle);
void deleteConversation(String conversationId);
List<AIConversationMessage> getConversationHistory(String conversationId);
```

- [ ] **Step 2: Add mirrored async signatures**

- [ ] **Step 3: Build shared lib**

Run: `cd appinventor && ant -f appengine/build.xml AiSharedLib`
Expected: FAIL on `AIAgentServiceImpl` because it does not implement the new methods. That's OK — next task implements them.

- [ ] **Step 4: Don't commit yet; proceed to Task 13.**

---

### Task 13: Implement new RPC methods in `AIAgentServiceImpl`

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/AIAgentServiceImpl.java`
- Create or extend: `appinventor/tests/com/google/appinventor/server/aiagent/AIAgentServiceImplTest.java`

- [ ] **Step 1: Tests for ownership rejections**

```java
public void testRenameConversationRejectsOtherUser() {
  String convId = storage.createConversation("owner", 1L);
  setCurrentUser("intruder");
  try {
    service.renameConversation(convId, "hacked");
    fail("expected SecurityException");
  } catch (SecurityException expected) {}
}

public void testDeleteConversationRejectsWrongProject() { /* analog */ }

public void testListConversationsFiltersCorrectly() { /* roundtrip */ }

public void testGetHistoryByConvIdReturnsMessagesInOrder() { /* roundtrip */ }
```

- [ ] **Step 2: Implement each method with ownership check**

Add a private helper:

```java
private void assertOwnsConversation(String userId, String conversationId) {
  StoredData.ConversationData cd = engine.getConversationMetadata(conversationId);
  if (cd == null || !userId.equals(cd.userId)) {
    throw new SecurityException("Conversation access denied.");
  }
  storageIo.assertUserHasProject(userId, cd.projectId);
}
```

Expose `getConversationMetadata` on `AIAgentEngine` via a thin delegate so the servlet doesn't poke `ConversationManager` directly.

Method implementations:

```java
@Override
public List<AIConversationSummary> listConversations(long projectId) {
  String userId = userInfoProvider.getUserId();
  storageIo.assertUserHasProject(userId, projectId);
  List<StoredData.ConversationData> rows = engine.listConversations(userId, projectId);
  List<AIConversationSummary> out = new ArrayList<>(rows.size());
  for (StoredData.ConversationData cd : rows) {
    out.add(new AIConversationSummary(cd.conversationId, cd.title, cd.createdAt, cd.updatedAt));
  }
  return out;
}

@Override
public AIConversationSummary renameConversation(String conversationId, String newTitle) {
  String userId = userInfoProvider.getUserId();
  assertOwnsConversation(userId, conversationId);
  engine.renameConversation(conversationId, newTitle);
  StoredData.ConversationData cd = engine.getConversationMetadata(conversationId);
  return new AIConversationSummary(cd.conversationId, cd.title, cd.createdAt, cd.updatedAt);
}

@Override
public void deleteConversation(String conversationId) {
  String userId = userInfoProvider.getUserId();
  assertOwnsConversation(userId, conversationId);
  engine.deleteConversation(conversationId);
}

@Override
public List<AIConversationMessage> getConversationHistory(String conversationId) {
  String userId = userInfoProvider.getUserId();
  assertOwnsConversation(userId, conversationId);
  return engine.getConversationHistoryByConvId(conversationId);
}
```

Add the delegate methods (`engine.listConversations`, `engine.renameConversation`, etc.) to `AIAgentEngine`.

- [ ] **Step 3: Run tests**

```bash
cd appinventor && ant -f appengine/build.xml AiServerLibTests
```
Expected: PASS.

- [ ] **Step 4: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/shared/rpc/aiagent/AIAgentService.java \
        appinventor/appengine/src/com/google/appinventor/shared/rpc/aiagent/AIAgentServiceAsync.java \
        appinventor/appengine/src/com/google/appinventor/server/aiagent/AIAgentServiceImpl.java \
        appinventor/appengine/src/com/google/appinventor/server/aiagent/AIAgentEngine.java \
        appinventor/tests/com/google/appinventor/server/aiagent/AIAgentServiceImplTest.java
git commit -m "feat(ai-agent): RPC methods for listing/renaming/deleting conversations"
```

---

### Task 14: Project-delete cascade

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/storage/ObjectifyStorageIo.java`
- Modify: `appinventor/tests/com/google/appinventor/server/storage/ObjectifyStorageIoConversationTest.java` (extend)

- [ ] **Step 1: Test**

```java
public void testDeleteProjectCascadesToConversations() {
  // Use whatever helper exists in LocalDatastoreTestCase / other storage tests
  // to seed a minimal UserProjectData + ProjectData so deleteProject completes.
  // Look at how existing deleteProject tests (if any) seed state; otherwise use
  // storage.createProject(userId, project, ...) — its signature is in StorageIo.
  long projectId = seedMinimalProject("user-a");
  String convId = storage.createConversation("user-a", projectId);
  storage.storeAIConversationMessage(convId, System.currentTimeMillis(), 0,
      StoredData.MessageRole.USER, "hi", true);

  storage.deleteProject("user-a", projectId);

  assertNull(storage.getConversationMetadata(convId));
  assertTrue(storage.loadAIConversationMessages(convId).isEmpty());
}
```

- [ ] **Step 2: Implement**

Inside `deleteProject`, after the `FileData` delete job and before the GCS cleanup:

```java
runJobWithRetries(new JobRetryHelper() {
  @Override
  public void run(Objectify datastore) {
    List<ConversationData> convs = datastore.query(ConversationData.class)
        .filter("projectId", projectId).list();
    for (ConversationData cd : convs) {
      datastore.delete(datastore.query(ConversationMessageData.class)
          .filter("conversationId", cd.conversationId).fetchKeys());
    }
    datastore.delete(convs);
  }
}, true);
```

- [ ] **Step 3: Run + commit**

```bash
git commit -m "feat(ai-agent): cascade conversation delete on project delete"
```

---

## Phase 4 — Client UI

### Task 15: `AIResponseOrchestrator` — track conversationId + new wrappers

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/AIResponseOrchestrator.java`

- [ ] **Step 1: Add state + accessors**

```java
private String currentConversationId;

public String getCurrentConversationId() { return currentConversationId; }

public void setCurrentConversationId(String id) {
  this.currentConversationId = id;
}
```

- [ ] **Step 2: Thread through requests/responses**

Before sending any request via `aiAgentService.processRequest(...)`, `continueRequest(...)`, or `reportExecutionErrors(...)`, set:

```java
request.setConversationId(currentConversationId);
```

In every response success handler, update local state:

```java
currentConversationId = response.getConversationId();
```

- [ ] **Step 3: Add RPC wrappers**

```java
public void listConversations(OdeAsyncCallback<List<AIConversationSummary>> cb) {
  aiAgentService.listConversations(projectId, cb);
}

public void loadConversation(String convId, final Runnable onDone) {
  cancelInFlight();
  callback.clearChatHistory();
  currentConversationId = convId;
  aiAgentService.getConversationHistory(convId,
      new OdeAsyncCallback<List<AIConversationMessage>>(MESSAGES.aiChatLoadHistoryError()) {
        @Override public void onSuccess(List<AIConversationMessage> messages) {
          if (messages != null) {
            for (AIConversationMessage m : messages) {
              if ("user".equals(m.getRole())) {
                callback.addUserMessage(m.getText(), m.getTimestamp());
              } else {
                callback.addAiMessage(m.getText(), m.getTimestamp());
              }
            }
          }
          if (onDone != null) onDone.run();
        }
      });
}

public void newConversation() {
  cancelInFlight();
  callback.clearChatHistory();
  currentConversationId = null;
}

public void renameConversation(String convId, String newTitle,
    OdeAsyncCallback<AIConversationSummary> cb) {
  aiAgentService.renameConversation(convId, newTitle, cb);
}

public void deleteConversation(String convId, OdeAsyncCallback<Void> cb) {
  aiAgentService.deleteConversation(convId, cb);
}
```

`callback.addUserMessage` / `addAiMessage` signatures gain a `long timestamp` parameter. The interface is declared in `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/chat/RendererHost.java` — update it there, then update the impl in `AIChatRenderer.java`, then fix every call site. Use `System.currentTimeMillis()` at the live-send call sites.

Remove the old `clearConversation()` wrapper — replaced by `deleteConversation(convId, ...)` + `newConversation()`.

- [ ] **Step 4: Build client**

Run: `cd appinventor && ant -f appengine/build.xml AiClientLib`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Commit**

```bash
git commit -m "feat(ai-agent): orchestrator tracks conversationId + new RPC wrappers"
```

---

### Task 16: `AIChatRenderer` — date separators + per-bubble timestamps

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/AIChatRenderer.java`
- Modify: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/chat/RendererHost.java` (if timestamp is added to host-facing signatures)

- [ ] **Step 1: Thread timestamp through the public API**

Change `addUserMessage(String)` → `addUserMessage(String, long timestamp)`, similarly for `addAiMessage` and streaming finalize hooks. Default call sites use `System.currentTimeMillis()` for live messages.

- [ ] **Step 2: Maintain `lastMessageCalendarDate` state**

Add:

```java
private String lastMessageDateKey;

private String calendarDateKey(long ts) {
  // "YYYY-MM-DD" in browser local tz, formed via DateTimeFormat.
  return DateTimeFormat.getFormat("yyyy-MM-dd").format(new Date(ts));
}
```

Before appending each bubble:

```java
String key = calendarDateKey(ts);
if (!key.equals(lastMessageDateKey)) {
  appendDateSeparator(ts);
  lastMessageDateKey = key;
}
```

Implement `appendDateSeparator(long ts)`: create a centered `<div class="ai-chat-date-separator">…</div>` with text from a helper:

```java
private String formatDateSeparator(long ts) {
  Date d = new Date(ts);
  long now = System.currentTimeMillis();
  // compare calendar dates in local tz
  if (calendarDateKey(ts).equals(calendarDateKey(now))) {
    return MESSAGES.aiChatDateSeparatorToday();
  }
  if (isYesterday(d, new Date(now))) {
    return MESSAGES.aiChatDateSeparatorYesterday();
  }
  if (withinLast7Days(d, new Date(now))) {
    return DateTimeFormat.getFormat("EEEE").format(d);
  }
  return DateTimeFormat.getFormat(
      sameYear(d, new Date(now)) ? "MMM d" : "MMM d, yyyy").format(d);
}
```

- [ ] **Step 3: Per-bubble local-tz time**

When creating each bubble element, append a muted `<span class="ai-chat-bubble-time">HH:mm</span>` rendered via `DateTimeFormat.getShortTimeFormat()`.

- [ ] **Step 4: Update `clear()`** to reset `lastMessageDateKey = null`.

- [ ] **Step 5: Add CSS**

Append to the existing AI chat stylesheet (locate the file that styles `.ai-chat-bubble`):

```css
.ai-chat-date-separator {
  text-align: center;
  color: #888;
  font-size: 11px;
  margin: 12px 0 4px 0;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.ai-chat-bubble-time {
  display: block;
  font-size: 10px;
  color: #999;
  margin-top: 2px;
}
```

- [ ] **Step 6: Build + commit**

```bash
git commit -m "feat(ai-agent): date separators and local-tz timestamps in chat"
```

---

### Task 17: `ConversationListPanel` — new component

**Files:**
- Create: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/dialog/ConversationListPanel.java`

- [ ] **Step 1: Skeleton**

```java
// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0

package com.google.appinventor.client.editor.youngandroid.aiagent.dialog;

import com.google.appinventor.client.OdeMessages;
import com.google.appinventor.shared.rpc.aiagent.AIConversationSummary;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickHandler;
// ... GWT widgets

import java.util.List;

/**
 * Shows the user's conversations for the current project. Sits in the
 * AI chat dialog body when the dialog is in LIST view. Emits callbacks
 * for select, rename, delete, and new-conversation actions.
 */
public class ConversationListPanel extends Composite {

  public interface Listener {
    void onSelect(String conversationId);
    void onRename(String conversationId, String newTitle);
    void onDelete(String conversationId);
    void onNew();
  }

  private final FlowPanel root = new FlowPanel();
  private final FlowPanel listBody = new FlowPanel();
  private final Listener listener;
  private String activeConversationId;

  public ConversationListPanel(Listener listener) {
    this.listener = listener;
    root.setStyleName("ai-chat-conversation-list");

    FlowPanel header = new FlowPanel();
    header.setStyleName("ai-chat-conversation-list-header");
    Label title = new Label(OdeMessages.INSTANCE.aiChatConversationsTitle());
    Button addBtn = new Button("+ " + OdeMessages.INSTANCE.aiChatNewConversation());
    addBtn.addClickHandler(e -> listener.onNew());
    header.add(title);
    header.add(addBtn);

    root.add(header);
    root.add(listBody);
    initWidget(root);
  }

  public void setActive(String conversationId) {
    this.activeConversationId = conversationId;
  }

  public void render(List<AIConversationSummary> summaries) {
    listBody.clear();
    for (AIConversationSummary s : summaries) {
      listBody.add(buildRow(s));
    }
  }

  private Widget buildRow(final AIConversationSummary s) {
    // Click row (not icons) → onSelect
    // Pencil icon → enter edit mode (inline <input>)
    // Trash icon → confirm dialog, then onDelete
    // Active conversation: add "ai-chat-conversation-row-active" style
    // Label = title if non-empty else italic fallback from s.getUpdatedAt()
    ...
  }

  private String fallbackLabel(long updatedAt) {
    // see §8.2 rules: Today/Yesterday/weekday/short date
    ...
  }
}
```

Fill in the helper methods. Use `DateTimeFormat` in local tz, same rules as Task 16.

- [ ] **Step 2: Add CSS**

```css
.ai-chat-conversation-list { ... }
.ai-chat-conversation-list-header { display:flex; justify-content:space-between; padding:8px; }
.ai-chat-conversation-row { display:flex; padding:8px; cursor:pointer; border-bottom:1px solid #eee; }
.ai-chat-conversation-row:hover { background:#f5f5f5; }
.ai-chat-conversation-row-active { background:#e8f0fe; }
.ai-chat-conversation-row-title { flex:1; }
.ai-chat-conversation-row-title-fallback { font-style:italic; color:#666; }
.ai-chat-conversation-row-actions { opacity:0; }
.ai-chat-conversation-row:hover .ai-chat-conversation-row-actions { opacity:1; }
```

- [ ] **Step 3: Build + commit**

```bash
git commit -m "feat(ai-agent): ConversationListPanel widget"
```

---

### Task 18: `AIChatDialog` two-view layout + header "☰" button

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/AIChatDialog.java`

- [ ] **Step 1: Introduce the view swap**

Replace the existing content `FlowPanel` with a `DeckPanel` (two-child GWT container that shows one child at a time) or two stacked panels with `setVisible(false/true)`. Index 0 = chat (existing content), index 1 = list.

Add fields:

```java
private DeckPanel viewSwitcher;
private ConversationListPanel conversationListPanel;
```

- [ ] **Step 2: Wire the header button**

Add a `☰` `Button` (style: icon only) to the chat header, left of the title. On click:

```java
showListView();
```

`showListView` behavior:

```java
private void showListView() {
  orchestrator.listConversations(new OdeAsyncCallback<List<AIConversationSummary>>(
      MESSAGES.aiChatLoadConversationsError()) {
    @Override public void onSuccess(List<AIConversationSummary> rows) {
      conversationListPanel.setActive(orchestrator.getCurrentConversationId());
      conversationListPanel.render(rows);
      viewSwitcher.showWidget(1);
    }
  });
}
```

- [ ] **Step 3: Implement `ConversationListPanel.Listener`**

```java
new ConversationListPanel.Listener() {
  @Override public void onSelect(String convId) {
    orchestrator.loadConversation(convId, new Runnable() {
      @Override public void run() { viewSwitcher.showWidget(0); }
    });
  }
  @Override public void onRename(String convId, String newTitle) {
    orchestrator.renameConversation(convId, newTitle,
        new OdeAsyncCallback<AIConversationSummary>(MESSAGES.aiChatRenameConversationError()) {
          @Override public void onSuccess(AIConversationSummary updated) {
            showListView(); // re-render
          }
        });
  }
  @Override public void onDelete(String convId) {
    // confirm then...
    orchestrator.deleteConversation(convId,
        new OdeAsyncCallback<Void>(MESSAGES.aiChatDeleteConversationError()) {
          @Override public void onSuccess(Void v) {
            if (convId.equals(orchestrator.getCurrentConversationId())) {
              orchestrator.newConversation();
            }
            showListView();
          }
        });
  }
  @Override public void onNew() {
    orchestrator.newConversation();
    viewSwitcher.showWidget(0);
  }
}
```

- [ ] **Step 4: Replace old "Clear conversation" button**

Remove the old `confirmAndClearConversation()` button and method. The delete action lives on each list row now.

- [ ] **Step 5: First-open flow**

The existing dialog currently hooks first-open chat hydration from `AIChatDialog.show()` (grep for the call to `orchestrator.loadExistingConversation` or similar — the exact method name may differ). Replace that hook with the list-first flow:

```java
public void show() {
  super.show();
  orchestrator.listConversations(new OdeAsyncCallback<List<AIConversationSummary>>(
      MESSAGES.aiChatLoadConversationsError()) {
    @Override public void onSuccess(List<AIConversationSummary> rows) {
      if (rows != null && !rows.isEmpty()) {
        orchestrator.loadConversation(rows.get(0).getConversationId(), null);
      } else {
        orchestrator.newConversation();
      }
      viewSwitcher.showWidget(0);
    }
  });
}
```

- [ ] **Step 6: Build, compile GWT, commit**

```bash
git commit -m "feat(ai-agent): AIChatDialog two-view layout with conversation list"
```

---

### Task 19: i18n keys

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/client/OdeMessages.java`
- Modify: `appinventor/blocklyeditor/src/msg/ai_blockly/messages.json`
- Modify: `appinventor/blocklyeditor/src/msg/ai_blockly/messages_*.json` (22 locale files)

- [ ] **Step 1: Add keys to `OdeMessages.java`**

```java
@DefaultStringValue("Conversations")
@Description("Header title for the AI chat conversation list view.")
String aiChatConversationsTitle();

@DefaultStringValue("New conversation")
@Description("Button label to start a new AI chat conversation.")
String aiChatNewConversation();

@DefaultStringValue("Rename")
String aiChatRenameConversation();

@DefaultStringValue("Delete conversation?")
String aiChatDeleteConversationConfirm();

@DefaultStringValue("Could not load conversations.")
String aiChatLoadConversationsError();

@DefaultStringValue("Could not rename conversation.")
String aiChatRenameConversationError();

@DefaultStringValue("Could not delete conversation.")
String aiChatDeleteConversationError();

@DefaultStringValue("Today")
String aiChatDateSeparatorToday();

@DefaultStringValue("Yesterday")
String aiChatDateSeparatorYesterday();

@DefaultStringValue("{0}")
@Description("Fallback label for untitled conversations in the list view; {0} is a formatted date.")
String aiChatConversationFallbackLabel(String formattedDate);

@DefaultStringValue("Back to conversations")
String aiChatBackToConversations();

@DefaultStringValue("Rename conversation")
String aiChatRenameConversationPlaceholder();

@DefaultStringValue("Today at {0}")
String aiChatConversationDateToday(String time);

@DefaultStringValue("Yesterday at {0}")
String aiChatConversationDateYesterday(String time);
```

- [ ] **Step 2: English `messages.json`**

Add the same keys with the same English values.

- [ ] **Step 3: 22 locale files**

For each `messages_*.json`, add the keys with the English value. Translators will fill them in later.

- [ ] **Step 4: Build + commit**

```bash
git commit -m "feat(ai-agent): i18n for multi-conversation UI"
```

---

## Phase 5 — Docs + Final Cleanup

### Task 20: Remove deprecated `clearConversation(projectId)`

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/shared/rpc/aiagent/AIAgentService.java`
- Modify: `appinventor/appengine/src/com/google/appinventor/shared/rpc/aiagent/AIAgentServiceAsync.java`
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/AIAgentServiceImpl.java`
- Modify: `appinventor/appengine/src/com/google/appinventor/server/storage/StorageIo.java`
- Modify: `appinventor/appengine/src/com/google/appinventor/server/storage/ObjectifyStorageIo.java`
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/AIAgentEngine.java`

- [ ] **Step 1: Grep for remaining callers**

```bash
cd appinventor && grep -rn "clearConversation" appengine/src
```

There should be none in client code after Task 15. Remove the server-side deprecated no-op body + interface declarations + async mirror.

- [ ] **Step 2: Also delete `cleanupConversationMessages`**

Verified in Task 5 that it's no longer called. Remove the method from `StorageIo` and `ObjectifyStorageIo`.

- [ ] **Step 3: Remove the old `getAIConversationState(long projectId)` / `saveAIConversationState(long projectId, ...)` / `clearAIConversationState(long projectId)` trio**

These are the project-keyed main-conversation methods. The *screen-scoped* overloads (which take a second `String screenName` parameter) are kept — they are used by orchestration children. Grep to confirm nothing else depends on the project-only trio:

```bash
grep -rn "getAIConversationState(.*projectId[^,]*)" appengine/src | grep -v "screenName"
```

- [ ] **Step 4: Build + run full test suite**

```bash
cd appinventor && ant -f appengine/build.xml tests
```
Expected: all pass.

- [ ] **Step 5: Commit**

```bash
git commit -m "chore(ai-agent): drop deprecated project-keyed conversation APIs"
```

---

### Task 21: Update `CONTRIBUTING_AI.md`

**Files:**
- Modify: `CONTRIBUTING_AI.md`

- [ ] **Step 1: Rewrite "Conversation Management" section**

Replace the current section's content. New outline:

1. Data model — `ConversationData` entity (persistent metadata) + `ConversationMessageData` (persistent messages) + `AIConversationState` (24h Memcache cache for `providerRef`, rekeyed by `conversationId`).
2. Lifecycle — create on first send; retained until explicit delete or project delete.
3. Rehydration — `chat()` always receives `history`; stateful providers fall back to replaying it when `providerRef` is null/expired. Mention that this also fixes the previously-error-y continuation path after Memcache expiry.
4. Client UX — two-view dialog; list view with rename/delete; date separators + local-tz timestamps.

- [ ] **Step 2: Update the RPC table**

Under Shared RPC, add `listConversations`, `renameConversation`, `deleteConversation`, `getConversationHistory(conversationId)`. Note that `clearConversation` has been removed and `getConversationHistory(long projectId)` is legacy-only.

- [ ] **Step 3: Update the conventions list**

Remove the entry about "24h TTL" for messages. Add: "conversations are retained until the user deletes them or deletes the project; cascade happens in `ObjectifyStorageIo.deleteProject()`."

- [ ] **Step 4: Add a pointer to this spec**

Under "Further Reading": `docs/superpowers/specs/2026-04-15-ai-agent-multi-conversation-design.md`.

- [ ] **Step 5: Commit**

```bash
git commit -m "docs(ai-agent): document multi-conversation support in CONTRIBUTING_AI"
```

---

## Done Criteria

- [ ] `cd appinventor && ant -f appengine/build.xml tests` passes.
- [ ] Manual smoke: open AI chat, send a message, close dialog, reopen → lands on the same conversation. Click ☰ → list shows the conversation. Click "+ New" → blank chat, send a message → new row appears in list. Rename one → title updates. Delete one → it's gone.
- [ ] Cold-memcache smoke (restart dev server between turns): continuation still works on Anthropic, OpenAI, and Gemini.
- [ ] CONTRIBUTING_AI.md "Conversation Management" section describes the new model.

---

## Notes for the Implementer

- **Do not touch child (screen-scoped) orchestration state.** The `projectId + screenName` methods in `StorageIo` and `ConversationManager` are for Plan & Execute children and stay ephemeral.
- **The provider history-replay branches (Tasks 7-9) are independent of the multi-conversation UX.** They're a fix for a latent bug that this feature exposes. If you get blocked on UI work, these tasks ship value on their own.
- **When in doubt about timestamps**, trust `System.currentTimeMillis()` on the server for storage and `new Date(timestamp)` on the client for rendering. `DateTimeFormat` formats in the browser's local tz by default; that's what we want.
- **Ownership checks are defensive.** `assertUserHasProject` already enforces the main invariant; the extra `userId` check on `ConversationData` guards against the (unlikely) case where a project changes owners.
