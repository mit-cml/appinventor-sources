# AI Agent Multi-Conversation History — Design

**Date:** 2026-04-15
**Branch:** `ai-agent`
**Status:** Draft — awaiting review

---

## 1. Problem

Today, the AI Agent allows exactly one conversation per project. `AIConversationState` is stored in Memcache keyed by `projectId`, messages in Datastore share a single `conversationId` per project, and the only user-facing action is "clear" — which permanently deletes everything.

This hurts the product in three concrete ways:

1. **No history.** A user working on an app across multiple sessions can't look back at what they asked yesterday unless it all fits inside a single thread. "Clear" is destructive and there is no undo.
2. **No separation of concerns.** A user who is partway through a build and wants to ask a quick one-off question either pollutes the build thread or wipes it to start fresh. Long debugging conversations derail feature work.
3. **Fragile resume on stateful providers.** When the 24-hour Memcache TTL lapses, the `providerRef` for OpenAI Responses / Gemini / Vertex is gone, and `AIAgentEngine.continueRequest()` returns *"No continuation state available. Please start a new request."* even though the user's message history is still in Datastore. Stateful providers have no history-replay fallback path today.

This spec adds **multiple named conversations per project**, with the ability to list, resume, rename, and delete them — and fixes the stateful-provider resume gap as a prerequisite.

## 2. Goals and Non-Goals

### Goals

1. A project can have any number of conversations. They are persistent until the user deletes them or deletes the project.
2. The user can switch between conversations inside the AI chat dialog, rename any of them, and delete any of them.
3. Stateful LLM providers (OpenAI Responses, Gemini, Vertex) transparently resume any conversation, even when their server-side `providerRef` has expired, by replaying the stored message history on the next `chat()` call.
4. Messages render with timestamps in the user's browser-local timezone, and the chat scroll shows a date separator whenever the calendar date changes.
5. The existing single-conversation UX continues to "just work": opening the dialog on a project with prior history lands the user on the most-recently-updated conversation.

### Non-Goals

- No cross-project or cross-user conversation sharing. Conversations remain project-scoped and user-owned.
- No AI-generated titles. Titles are either user-set or absent; the list UI falls back to an italicized last-message-date when a title is absent.
- No automated cleanup / TTL / cron. Retention is purely manual (explicit delete or project-delete cascade).
- No changes to orchestration child conversations. `Plan & Execute` screen-scoped child state (keyed by `projectId + screenName`) stays ephemeral in Memcache.
- No server-side "active conversation per user" persistence. The client tracks the currently-open conversation in-session; on fresh page load the server picks the most-recently-updated one for the project.
- No change to the per-user rate limit model.

## 3. Terminology

- **Conversation** — a named thread of user/assistant messages scoped to a project. Identified by a UUID (`conversationId`). Persistent in Datastore.
- **ConversationData** — new Datastore entity holding conversation metadata (title, timestamps, owner).
- **ConversationMessageData** — existing Datastore entity; now retained indefinitely, still keyed by `conversationId`.
- **AIConversationState** — existing Memcache-only record holding `providerName` + `conversationId` + `providerRef`. Re-keyed from `projectId` to `conversationId` in this spec.
- **providerRef** — stateful-provider-specific resume token (OpenAI `response_id`, Gemini/Vertex serialized `contents`). Opaque. Always nullable.
- **Chat view / List view** — the two display modes of the existing `AIChatDialog` after this change. One is always active; swapping does not tear down the other.

## 4. Architecture Overview

```
                        ┌──────────────────────────────┐
                        │  AIChatDialog                │
                        │  ┌────────────┬───────────┐  │
                        │  │ ChatView   │ ListView  │  │ NEW two-view toggle
                        │  └─────▲──────┴─────▲─────┘  │
                        └────────┼────────────┼────────┘
                                 │            │
                    send / load  │            │ list / rename / delete
                                 │            │
                     ┌───────────┴────────────┴───────────┐
                     │  AIResponseOrchestrator            │
                     │  (tracks currentConversationId)    │
                     └────────────────┬───────────────────┘
                                      │ GWT-RPC
                     ┌────────────────┴───────────────────┐
                     │  AIAgentService                    │
                     │  + listConversations               │
                     │  + renameConversation              │
                     │  + deleteConversation              │
                     │  + getConversationHistory(convId)  │
                     └────────────────┬───────────────────┘
                                      │
                   ┌──────────────────┴───────────────────┐
                   │ AIAgentEngine + ConversationManager  │
                   │ (convId-keyed, history-fallback)     │
                   └─────┬───────────────────┬────────────┘
                         │                   │
                ┌────────▼─────────┐  ┌──────▼────────┐
                │ LLMProvider.chat │  │ StorageIo     │
                │ history used when│  │ ConversationData│ NEW
                │ providerRef null │  │ ConversationMsg │
                └──────────────────┘  └────────────────┘
```

## 5. Data Model

### 5.1 New entity: `StoredData.ConversationData`

```java
@Unindexed
public static final class ConversationData implements Serializable {
  @Id Long id;
  @Indexed public String conversationId;   // UUID, matches ConversationMessageData.conversationId
  @Indexed public long   projectId;
  @Indexed public String userId;
  public String title;                     // nullable; null/empty → client renders fallback
  public long   createdAt;
  public long   updatedAt;                 // bumped on every storeMessage()
}
```

Registered in `ObjectifyStorageIo.register(…)` alongside existing entities. Queries:

- **List for project:** `ofy.query(ConversationData.class).filter("projectId", projectId).list()` then in-memory sort by `updatedAt` desc. (Single-filter query avoids composite-index requirements.)
- **Lookup by UUID:** same pattern, filter on `conversationId` (single indexed field). Expect 0 or 1 result.

### 5.2 Changes to `ConversationMessageData`

- **Stop writing `expiresAt`.** New rows leave it at 0. The `@Indexed` annotation stays for backward compatibility but the field is no longer used as a cleanup key.
- **No changes to existing fields.** The `timestamp` field (already present, already sorted-on) is what the client uses for in-chat rendering and for the list's last-message fallback label.

### 5.3 Deprecations

- `StorageIo.cleanupConversationMessages()` — removed from the call path (was invoked on every `storeMessage`). Declaration can remain as a no-op for compile-safety during the transition, then be removed.
- Remove the `expiresAt + CONVERSATION_TTL_SECONDS * 1000L` write and the `if (m.expiresAt > now)` filter in the loader — history is always returned in full now.

### 5.4 Shared DTOs

**New — `shared/rpc/aiagent/AIConversationSummary`:**
```java
String conversationId;
String title;          // nullable
long   createdAt;
long   updatedAt;
```

**Extended — `AIConversationMessage`:** adds `long timestamp` so the client can render per-message local-tz time and group by date. (The field already exists on the server-side `ChatMessage` / `ConversationMessageData` entity and is populated with `System.currentTimeMillis()` on every write — this spec only surfaces it through the shared DTO.)

**Extended — `AIAgentRequest`:** adds `String conversationId` (null/"" means "create a new conversation").

**Extended — `AIAgentResponse`:** adds `String conversationId` (server always echoes the id it used, whether pre-existing or freshly-minted).

## 6. Server Behaviour

### 6.1 ConversationManager rewrite

The main-conversation methods switch from project-keyed to conversation-keyed. Screen-scoped (orchestration children) methods are unchanged.

```java
AIConversationState getConversation(String conversationId);          // from Memcache
void saveConversation(String conversationId, AIConversationState s); // to Memcache (24h TTL retained)
void clearConversationState(String conversationId);                  // Memcache only

// New CRUD over ConversationData
String createConversation(String userId, long projectId);            // mints UUID + row, returns it
List<ConversationData> listConversations(long projectId);            // sorted updatedAt desc
ConversationData getConversationMetadata(String conversationId);
void renameConversation(String conversationId, String title);
void touchConversation(String conversationId);                       // bumps updatedAt
void deleteConversation(String conversationId);                      // metadata + messages + state
```

`storeMessage()` calls `touchConversation(conversationId)` after the Datastore write. `cleanupConversationMessages()` is no longer invoked from this path.

### 6.2 AIAgentEngine request routing

```
processRequest(request):
  convId = request.conversationId
  if blank(convId):
    convId = conversationManager.createConversation(userId, projectId)
  else:
    verifyOwnership(userId, convId)        // throws if mismatch or missing
  state = conversationManager.getConversation(convId)        // may be null if Memcache cold
  if state == null:
    state = new AIConversationState(providerName, convId, null)
    conversationManager.saveConversation(convId, state)      // cold-start a Memcache entry
  ...
  response.conversationId = convId                            // always echoed back
```

`continueRequest` and `reportExecutionErrors` follow the same pattern — `convId` is mandatory on these (since continuation implies an existing conversation).

### 6.3 History loading (applies to all providers)

Today `AIAgentEngine` only loads `history` for stateless providers:

```java
// OLD
List<ChatMessage> history = provider.isStateless()
    ? conversationManager.loadConversation(convId)
    : Collections.emptyList();
```

After this spec, `history` is **always** loaded:

```java
// NEW
List<ChatMessage> history = conversationManager.loadConversation(convId);
```

The `history` parameter is already part of the `LLMProvider.chat()` signature. Stateless providers already use it. Stateful providers start using it **only as a fallback when `providerRef` is null or empty** — see §6.4.

### 6.4 Stateful provider history-replay fallback

For each of the three stateful providers, `chat()` gains a single new branch taken when `providerRef` is null/empty **and** `history` is non-empty.

- **`OpenAIProvider`** — the fallback branch serializes `history` into the Responses API `input` array as a sequence of `{role, content: [{type: "input_text", text: …}]}` / `{role: "assistant", content: [{type: "output_text", …}]}` entries, then appends the current user/context messages. Tool-use blocks in `structuredContent` are replayed as `response.output` entries with their paired `tool_result` entries, matching the existing replay already implemented in `AnthropicProvider`. From the first response onward, the returned `response.id` is captured into the new `providerRef` and future calls continue statefully as today.
- **`GeminiProvider` / `VertexProvider`** — the fallback branch serializes `history` into the `contents` JSON array (role ↔ "user"/"model" mapping; tool calls as `functionCall` parts, tool results as `functionResponse` parts) and sends it alongside the current context. The provider then caches the merged conversation into `providerRef` the same way it does today after a normal turn.
- **`continueWithToolResults()`** — the engine's existing guard `if (conv == null || conv.getProviderRef() == null)` currently returns `"No continuation state available"`. It is relaxed: if the conversation exists but `providerRef` is missing, the engine falls back to `processRequest`-style behavior — it builds a synthetic user message (`"Please continue from the previous tool results."` or the rejection feedback, depending on caller) and invokes `chat()` with full history, so the stateful provider rehydrates. This fixes the latent *"please start a new request"* bug as a side-effect.
- **Stateless providers (Anthropic/Bedrock/MiniMax/OpenRouter/Ollama/compatible)** — no change. They already use `history`; `providerRef` is ignored.

### 6.5 Authorization

- **`processRequest` / `continueRequest` / `reportExecutionErrors`** — on every request with a non-blank `conversationId`, load `ConversationData` and assert `conv.userId.equals(userId) && conv.projectId == request.projectId`. Mismatch → `SecurityException`. Blank `conversationId` falls through to the create path (already covered by `assertUserHasProject`).
- **`listConversations(projectId)`** — `assertUserHasProject(userId, projectId)`; returns metadata for conversations matching both `projectId` and `userId`.
- **`renameConversation` / `deleteConversation` / `getConversationHistory(conversationId)`** — load metadata, assert ownership as above.
- **Legacy `clearConversation(projectId)`** — the new client stops calling it, and since this is a dev branch with no external callers, the implementation can be removed. The RPC method stays declared on the service interface only long enough for any in-flight GWT compiles to succeed; the plan removes it in the last task alongside the CONTRIBUTING_AI update.

### 6.6 Memcache keying

`AI_CONV_CACHE_KEY_PREFIX + conversationId` replaces `AI_CONV_CACHE_KEY_PREFIX + projectId` for the main conversation. The screen-scoped composite key (`AI_CONV_CACHE_KEY_PREFIX + projectId + ":" + screenName`) is untouched. 24-hour TTL is retained — loss of Memcache state is always safe because `history` replays on the next `chat()`.

### 6.7 Project delete cascade

A new job is added inside `ObjectifyStorageIo.deleteProject()` (after the existing `FileData` delete job, before the GCS cleanup). It loops the project's conversations, firing one `ConversationMessageData` delete-by-filter per conversation; this is N+1 Datastore queries in the job, which is acceptable because (a) the project-delete path is rare and off the hot path, (b) per-conversation message counts are typically in the tens or low hundreds, and (c) batching by keys would require us to fan out a two-phase fetch-keys-then-delete that is not meaningfully cheaper on GAE Datastore:

```java
runJobWithRetries(new JobRetryHelper() {
  @Override public void run(Objectify datastore) {
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

Memcache entries for those conversations are evicted naturally via their 24-hour TTL; explicit eviction is not worth the extra RPC cost.

## 7. RPC Surface

### 7.1 AIAgentService additions

```java
List<AIConversationSummary> listConversations(long projectId);
AIConversationSummary renameConversation(String conversationId, String newTitle);
void deleteConversation(String conversationId);
List<AIConversationMessage> getConversationHistory(String conversationId);  // overload
```

The existing `List<AIConversationMessage> getConversationHistory(long projectId)` method is retained and re-defined to "load the most-recently-updated conversation for this project, or an empty list if none." It is used only on fresh page-load before the client knows which conversation to select.

### 7.2 AIAgentServiceAsync mirrors all new methods with `AsyncCallback` variants.

### 7.3 AIAgentRequest / AIAgentResponse

- `AIAgentRequest` gains `String conversationId` (optional on the first send, mandatory on continuations).
- `AIAgentResponse` gains `String conversationId` (always populated). The client persists it locally on each response so subsequent sends target the same conversation.

## 8. Client Behaviour

### 8.1 AIChatDialog two-view layout

A new enum `DialogView { CHAT, LIST }` drives visibility. Both panels stay mounted; swapping is a CSS / `setVisible` toggle so chat state (streaming bubble, operation preview, input text) is preserved across swaps.

- **Chat header** gains a leading `☰` (conversations) button that calls `showListView()`. The current conversation's title — or italic last-message-date fallback — sits next to it and is click-to-edit for inline rename.
- **List view** is a new component `client/.../aiagent/dialog/ConversationListPanel`. It owns an RPC-loaded list of `AIConversationSummary`, renders rows sorted by `updatedAt` desc, and emits callbacks on select / rename / delete.

### 8.2 ConversationListPanel rendering

Each row shows either the title (if set) or the italicized last-message-date fallback, using `DateTimeFormat` in the user's locale and browser-local timezone. Date format rules:

- Within today: *"Today at HH:mm"* (locale-aware time format).
- Yesterday: *"Yesterday at HH:mm"*.
- Within the last 7 days: weekday name + time.
- Older: localized short date (e.g. *"Apr 3"*, *"Apr 3, 2025"* for different-year).

Row affordances (hover/focus-only to avoid clutter):

- Pencil icon → inline rename (in-place `<input>`, Enter commits, Esc cancels, blur commits). Empty-string commit clears the title back to the fallback.
- Trash icon → confirm dialog, then `deleteConversation` RPC. If the deleted conversation was the active one, the client falls back to the next most-recently-updated conversation, or a blank new-conversation state if none remain.

Active conversation is indicated with a leading checkmark and a stronger background colour.

List header has `+ New conversation`. Clicking it: cancels any in-flight request, clears chat panel state, sets `currentConversationId = null` on the orchestrator, swaps to chat view. The new conversation materializes server-side only on the first message send.

### 8.3 Chat message rendering

`AIChatRenderer` gains `lastMessageCalendarDate` state. When appending a message whose local-tz calendar date differs from the previous message's:

1. Insert a centered **date separator row** ("Today", "Yesterday", weekday, or date, using the same rules as the list labels).
2. Update `lastMessageCalendarDate` to the new value.

Each bubble renders a muted subtitle showing local-tz time (HH:mm). Timestamp comes from `AIConversationMessage.timestamp` on initial load and from `System.currentTimeMillis()` on live sends / streaming completion (the server's stored timestamp is authoritative, but the difference is negligible and we don't need a round-trip before render).

### 8.4 AIResponseOrchestrator changes

- Gains `String currentConversationId` field.
- On every outgoing `AIAgentRequest`, sets `request.conversationId = currentConversationId` (may be null for a fresh conversation).
- On every incoming `AIAgentResponse`, sets `currentConversationId = response.conversationId`. (Handles the "server minted a new id" case transparently.)
- New methods: `loadConversation(String convId)` — cancels in-flight, clears chat, calls `getConversationHistory(convId)`, rehydrates bubbles via `AIChatRenderer`; `listConversationsForCurrentProject(callback)` — proxies the RPC; `renameCurrentConversation(title, callback)`; `deleteConversation(convId, callback)`.
- On dialog first open: the orchestrator issues `listConversations(projectId)`, picks the first row (most-recently-updated) as the initial conversation, then issues `getConversationHistory(conversationId)` to rehydrate the chat panel. The legacy `getConversationHistory(projectId)` method is unused in the new flow — it stays callable only to keep the shared RPC interface stable.

### 8.5 Switching / rejection flow compatibility

The existing "rejection feedback" flow (`processRequest` with a `<system>` rejection payload) is unchanged except that `conversationId` travels with the request. If the user switches conversations mid-stream, the orchestrator cancels the in-flight request (existing `cancelRequest` path) before triggering the switch.

### 8.6 i18n

New keys added to `blocklyeditor/src/msg/ai_blockly/messages.json` (English) and stubbed into the 22 locale files with the English value as a passthrough:

```
aiChatConversationsTitle
aiChatBackToConversations
aiChatNewConversation
aiChatRenameConversation
aiChatRenameConversationPlaceholder
aiChatDeleteConversationConfirm
aiChatLoadConversationsError
aiChatRenameConversationError
aiChatDeleteConversationError
aiChatConversationFallbackLabel      (format string for the italic fallback — takes the formatted date as {0}; default English value is just "{0}" so the label is the date alone, but the key exists so translators can prepend a word like "Last edit · {0}" if their language reads better that way)
aiChatConversationDateToday          ("Today at {0}")
aiChatConversationDateYesterday      ("Yesterday at {0}")
aiChatDateSeparatorToday             ("Today")
aiChatDateSeparatorYesterday         ("Yesterday")
```

Entries in `OdeMessages.java` added accordingly.

## 9. Validation and Error Handling

- **Blank title on rename** — accepted; server sets `title = null` and returns a summary with null title. Client re-renders as the italic fallback.
- **Title length** — server caps at 120 chars (silently truncates). Prevents pathological payloads; 120 is well above any reasonable UI width and matches informal conventions elsewhere in the app.
- **Rename / delete race** — if a conversation is deleted by another tab or by project-delete while the user is typing, the RPC returns an error; client logs and refreshes the list.
- **Switch with in-flight request** — orchestrator cancels via `cancelRequest(projectId)` before issuing the switch. Chat panel is cleared after `cancelRequest` acks. Partial operation previews are discarded.
- **Stateful-provider replay failure** — if the replay call fails (e.g. OpenAI rejects a too-long history), the error path already in `AIAgentEngine` returns a user-facing error and leaves the conversation untouched. No silent data loss; user can retry or start a new conversation.
- **Message timestamp field missing** (older rows without a `timestamp`) — `ChatMessage` already surfaces it; legacy code always set it via `System.currentTimeMillis()` at write time, so no compat concerns.

## 10. Testing

- **`ConversationManagerTest`** — CRUD round-trips against a fake `StorageIo`. Ordering of `listConversations` by `updatedAt`.
- **`AIAgentServiceImplTest`** — ownership rejection paths (`renameConversation` on a conversation owned by another user, `deleteConversation` on a mismatched-project conversation).
- **`AIAgentEngineTest`** — (a) fresh `processRequest` with blank `conversationId` mints a new id and echoes it; (b) `processRequest` with a specific `conversationId` routes state correctly; (c) `continueRequest` with no memcache state but a valid conversationId falls back to history replay rather than returning an error.
- **Per-provider replay tests** — `OpenAIProviderTest`, `GeminiProviderTest`, `VertexProviderTest` assert that when `providerRef` is null and history is non-empty, the outbound HTTP body contains the serialized history turns in the correct order and role mapping. Uses the existing HTTP-mock pattern.
- **Client GWT tests** — `ConversationListPanel` renders fallback labels correctly for today / yesterday / older dates; `AIChatRenderer` emits a date separator when the calendar date changes between messages.
- **Existing test coverage preserved.** `StreamBufferTest` and existing validator / executor tests are unaffected.

## 11. Rollout and Migration

This change ships on the `ai-agent` dev branch. There are no committed users on this branch, and all existing `ConversationMessageData` rows auto-expire within 24 hours via the old `expiresAt` field. Approach:

1. **New writes** skip `expiresAt` and include a `ConversationData` row.
2. **Old writes** continue to age out naturally via their stamped `expiresAt`; the old sweep method is removed, but any stragglers will simply sit unreferenced in Datastore (no user-visible effect). A one-off maintenance script can be run out-of-band if the operator cares.
3. **First post-deploy request** on a given project creates a brand-new conversation. The pre-deploy single-conversation data is orphaned (not surfaced anywhere in the new UI). Given the 24h effective lifetime of old data and this being a dev branch, a formal migration is not worth writing.

## 12. Open Questions

None that block implementation. The design assumes the legacy `clearConversation(projectId)` and `getConversationHistory(projectId)` methods remain callable during a phased deploy; both are removable in a follow-up cleanup once no client calls them.
