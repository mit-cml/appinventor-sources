# AI Agent Streaming Responses — Design Spec

## Problem

The AI agent's current request flow blocks the GWT-RPC call for the full duration of the LLM response (5-30+ seconds). During this time, the client polls `getRequestStatus()` every 1 second and shows a static "Calling AI..." message. Users get no feedback about what the model is generating until the entire response is complete.

## Goal

Stream LLM text tokens to the client incrementally while the request is in progress, using the existing polling mechanism — no new servlets, no new transport protocols. Tool call handling remains unchanged.

## Constraints

- No new servlets or transport (SSE, WebSocket). Reuse existing GWT-RPC infrastructure.
- All five providers: Anthropic, OpenAI, Gemini, Ollama, MiniMax.
- Stream text only. Tool invocations (read-only and action) are not streamed.
- Incremental markdown rendering on the client.
- The final RPC response (`AIAgentResponse`) remains the source of truth.

## Architecture Overview

```
Client (AIResponseOrchestrator)
  |
  |--- processRequest (GWT-RPC) -----------> AIAgentServiceImpl
  |                                              |
  |                                              v
  |                                          AIAgentEngine
  |                                              |
  |                                              v
  |                                          LLMProvider.chat()
  |                                              |
  |    polls every 250ms                         | streams tokens
  |--- getRequestStatus (GWT-RPC) --->  StreamBuffer (Memcache)  <--- provider writes
  |<-- AIStreamStatus {                          |
  |      statusText,                             |
  |      textDelta,                              | blocks until complete
  |      done                                    |
  |    }                                         v
  |<-- AIAgentResponse (RPC return) ---- full LLMResponse returned
```

## Design

### 1. Provider Layer — Streaming with Buffer

Each provider enables streaming mode in its LLM API request (`"stream": true` or equivalent). Instead of buffering the entire response in memory, the provider reads SSE/chunked response lines from the LLM and:

1. **Text deltas**: Appends tokens to a `StreamBuffer` keyed by project ID (stored in Memcache).
2. **Read-only tool calls**: Still resolved internally in the provider's tool loop (not streamed to client). Text emitted by the LLM *before* a read-only tool call in the same response chunk is streamed to the buffer immediately — the quiet period only occurs during the tool resolution + next LLM call. This matches the natural conversation flow (model explains what it's doing, then does it).
3. **Action tool calls / end of response**: Stops streaming, returns the complete `LLMResponse` as today.

The `LLMProvider.chat()` signature gains one parameter:

```java
LLMResponse chat(String systemPrompt, List<String> contextMessages,
    String userMessage, List<LLMTool> tools, String providerRef,
    List<ChatMessage> history, ReadOnlyToolResolver resolver,
    StreamBuffer streamBuffer);  // NEW — nullable for non-streaming callers
```

The method still blocks and returns `LLMResponse`. The difference is that while it blocks, text tokens are written to the buffer as a side effect.

Each provider parses its own streaming format:

| Provider | Streaming format | Text delta location |
|----------|-----------------|-------------------|
| Anthropic | SSE (`text/event-stream`) | `content_block_delta` events, `delta.text` field |
| OpenAI | SSE (Responses API) | `response.output_text.delta` for text; also handle `response.function_call_arguments.delta` and `response.output_item.added` to detect tool call start and stop buffering text |
| Gemini | SSE | `candidates[].content.parts[].text` in chunks |
| Ollama | Line-delimited JSON | `message.content` per line |
| MiniMax | SSE (OpenAI-compatible) | `choices[].delta.content` |

`continueWithToolResults()` also accepts a `StreamBuffer` parameter for the same behavior during continuation calls.

### 2. StreamBuffer — Shared Token Storage

A class wrapping Memcache operations for accumulating and consuming streamed tokens.

**Key**: `ai_stream:{projectId}` — project-scoped, matching the existing `ai_status:{projectId}` pattern. Authorization is enforced in the RPC layer (`storageIo.assertUserHasProject()` in `getRequestStatus`) before any buffer read, so user-scoping in the key is unnecessary. One active request per project, enforced by existing `requestInFlight` flag and server-side rate limiting.

**Lifecycle**: `StreamBuffer` is a per-request object, constructed as `new StreamBuffer(storageIo, projectId)`. Methods take no `projectId` — it's bound at construction. Created by `AIAgentEngine` at request start, passed to the provider via `chat()`.

**Operations**:
- `init()` — Create/reset the buffer. Unconditionally overwrites any stale buffer from a previous request (e.g., if the server crashed mid-stream). Sets write counter to `0` via `put()`, clears read counter, done flag, and any leftover chunk keys.
- `appendText(String text)` — Append a text chunk (provider side). Stored with `t:` prefix.
- `appendStatus(String status)` — Append a status chunk. Stored with `s:` prefix.
- `consume()` — Return all data since last consume as `AIStreamStatus`. Concatenates all `t:` chunks into `textDelta`, uses last `s:` chunk as `statusText`. Advances the read offset. Returns `AIStreamStatus` with all-null fields and `done=false` if no new data (stream not started or quiet period).
- `markDone()` — Signal that the LLM call has finished.
- `clear()` — Clean up after the RPC response is sent.

**Implementation — key-per-chunk storage via StorageIo**:

`StreamBuffer` is a thin orchestration class that delegates all Memcache operations to new methods on `StorageIo` / `ObjectifyStorageIo`, following the existing pattern (same as `updateAIRequestStatus` / `getAIRequestStatus`). `StreamBuffer` never touches `MemcacheService` directly.

New methods added to `StorageIo` interface and `ObjectifyStorageIo` implementation:
- `initAIStreamBuffer(long projectId)` — Reset all stream keys for the project. Sets write counter to `0` via `put()`.
- `appendAIStreamChunk(long projectId, String chunk)` — Write a chunk at the next write counter index. Internally: `memcache.increment(wcKey, 1, 0L)` (atomically increments, initializes to 0 if absent) to get index N, then `put()` the chunk at `ai_stream:{projectId}:chunk:{N}`.
- `consumeAIStreamChunks(long projectId)` — Read write counter and read counter. Use `memcache.getAll()` to batch-read all chunk keys from `rc` to `wc` in a single RPC call. Advance read counter via `put()`. Returns list of chunk strings. Null chunks (evicted) are skipped gracefully.
- `markAIStreamDone(long projectId)` — Set the done flag.
- `isAIStreamDone(long projectId)` — Check the done flag.
- `clearAIStreamBuffer(long projectId)` — Delete all stream keys.

Inside `ObjectifyStorageIo`, these use the existing `memcache` instance with key-per-chunk scheme to avoid contention between writer (provider thread) and reader (polling thread):

- **Write counter key**: `ai_stream:{projectId}:wc` — incremented atomically via `memcache.increment(key, 1, 0L)` on each append. The `0L` initial value handles the case where the key was evicted after `init()`.
- **Chunk keys**: `ai_stream:{projectId}:chunk:{N}` — each append writes a new chunk at index N. Prefixed with `t:` for text or `s:` for status. Each chunk is a small string (a few tokens or a status update).
- **Read offset key**: `ai_stream:{projectId}:rc` — tracks the last chunk consumed. `consumeAIStreamChunks()` uses `memcache.getAll()` to batch-read all pending chunks in one call, then advances `rc`.
- **Done flag key**: `ai_stream:{projectId}:done` — set/checked by the done methods.

No CAS contention — the writer only writes new chunk keys and increments the write counter, the reader only reads chunk keys and increments the read counter.

**TTL strategy**: All keys use a 1-minute TTL. Write operations (`appendAIStreamChunk`, `markAIStreamDone`) refresh the TTL on the counter keys (`wc`, `rc`) and the done flag only — not on individual chunk keys, which would be O(N) per write. Chunk keys keep their original 1-minute TTL from when they were written, which is sufficient since the consumer polls at 250ms-1s and reads them well within that window. If a chunk is evicted before being read (e.g., due to slow client), `consume()` skips it — the final RPC response carries the complete text anyway. `clear()` eagerly deletes all keys on completion. Typical LLM responses are well under 1MB total, so individual chunks (a few bytes to a few KB each) are nowhere near Memcache's 1MB per-value limit.

Status updates ("Building context...", "Calling AI...") are written through the same buffer instead of the current separate `updateStatus` path, so the client has a single source for all incremental updates.

### 3. AIStreamStatus — New RPC DTO

Replaces the current `String` return type of `getRequestStatus()`:

Package: `com.google.appinventor.shared.rpc.aiagent` (alongside `AIAgentResponse`, `AIAgentRequest`, etc.)

```java
public class AIStreamStatus implements IsSerializable {
    private String statusText;   // Phase update (nullable)
    private String textDelta;    // New tokens since last poll (nullable)
    private boolean done;        // Whether the LLM call has finished

    // Required no-arg constructor for GWT serialization
    public AIStreamStatus() {}
}
```

This is a breaking change to the `AIAgentService` RPC interface:

```java
// Before:
String getRequestStatus(long projectId);

// After:
AIStreamStatus getRequestStatus(long projectId);
```

### 4. Client Changes — AIResponseOrchestrator

The existing polling timer is enhanced:

1. **Poll interval**: Adaptive — 250ms when `textDelta` is flowing (tokens arriving), 1000ms otherwise (during "Building context..." and other status-only phases). This balances responsiveness with server load.

2. **`getRequestStatus` callback** handles the richer `AIStreamStatus` response:
   - `statusText` set → update the status label (same as today).
   - `textDelta` set → append to the in-progress chat bubble via `AIChatRenderer`.
   - `done` is true → stop polling. `done` is purely a display signal (stop showing streaming text, show a "finishing..." state). The RPC `processRequest` callback delivers the final `AIAgentResponse` with operations. `done` is set when the LLM HTTP response is fully received, before `chat()` returns — this means it fires before `AIAgentEngine` finishes post-processing (parsing, saving history, etc.), so the RPC response may arrive shortly after.

3. **New `ChatCallback` methods** needed for streaming:
   - `startStreamingBubble()` — Creates the in-progress AI message bubble. Called on first `textDelta`.
   - `appendStreamingText(String delta)` — Appends delta to the streaming bubble with incremental markdown re-render.
   - `finalizeStreamingBubble(String finalText)` — Replaces streaming bubble content with the final canonical `aiMessage` from the RPC response.

4. **Chat bubble lifecycle**:
   - A "streaming" bubble is created when the first `textDelta` arrives.
   - Each subsequent delta is appended and the bubble is re-rendered with incremental markdown.
   - When the full RPC response arrives, the bubble content is replaced with the final `aiMessage` (the complete, canonical text) to correct any incremental rendering artifacts.

5. **`continueRequest`** gets the same treatment — tokens stream via polling while the RPC blocks.

6. **`getRequestStatus` polling** no longer needed once `done` is true — the final RPC callback handles the rest.

### 5. Incremental Markdown Rendering

The existing `AIChatRenderer` uses marked.js + DOMPurify for complete messages. For incremental rendering:

- Maintain a raw text accumulator (`StringBuilder` equivalent in JS) for the in-progress message.
- On each `textDelta`, append to the accumulator, re-render the **full accumulated text** through marked.js + DOMPurify, and replace the bubble's innerHTML.
- This avoids partial-parse edge cases — marked.js handles incomplete markdown gracefully for inline formatting (e.g., unclosed `**` renders as literal text until the closing one arrives).
- **Code fences**: An unclosed ` ``` ` will cause marked.js to render all subsequent text as a code block until the closing fence arrives. To mitigate: before rendering, if the accumulated text has an odd number of ` ``` ` fences, temporarily append a closing fence for rendering only (do not add it to the accumulator).
- On final RPC response, one last render with the complete `aiMessage` ensures consistency.
- Performance: these are short chat messages, not documents. Full re-render per delta is fine.

### 6. Error Handling and Edge Cases

| Scenario | Behavior |
|----------|----------|
| **LLM call fails mid-stream** | Provider writes an error marker to the buffer, then throws. Polling client sees the error. RPC callback receives the error response as usual. |
| **Client disconnects mid-stream** | Buffer tokens go unread, expire with Memcache TTL. Server-side LLM call completes or times out normally. No cleanup needed. |
| **Concurrent requests on same project** | Already prevented by `requestInFlight` (client) and rate limiting (server). One buffer per project at a time. |
| **Read-only tool loops** | Text before a tool call in the same response chunk is streamed immediately. Buffer goes quiet during tool resolution + next LLM call. Status updates cover these quiet periods. |
| **Memcache eviction** | Some tokens lost mid-stream. `consume()` skips null chunks. Final RPC response carries the complete `aiMessage`, so the bubble is corrected on completion. Acceptable degradation. |
| **Provider retry loops** | `init()` is called once before the retry loop, not on each retry. Partial tokens from a failed attempt remain in the buffer — acceptable since the final RPC response is the source of truth and corrects the bubble. |
| **`getRequestStatus` return type change** | Breaking RPC interface change. GWT client and server are always deployed together, so this is safe. |

## Files Changed

### New files
- `StreamBuffer.java` — Orchestration layer over StorageIo for streaming token accumulation/consumption
- `AIStreamStatus.java` — New RPC DTO (shared package)

### Modified files
- `LLMProvider.java` — Add `StreamBuffer` parameter to `chat()` and `continueWithToolResults()`
- `AnthropicProvider.java` — Enable streaming, parse SSE chunks, write to buffer
- `OpenAIProvider.java` — Same (Responses API streaming format)
- `GeminiProvider.java` — Same
- `OllamaProvider.java` — Same (change `"stream": false` to `true`)
- `MiniMaxProvider.java` — Same
- `AIAgentEngine.java` — Create/pass `StreamBuffer`, use it for status updates. Pass `StreamBuffer` to `reportExecutionErrors` LLM calls too (retry responses also stream).
- `AIAgentService.java` — Change `getRequestStatus` return type to `AIStreamStatus`
- `AIAgentServiceAsync.java` — Update async callback type to match (`AsyncCallback<AIStreamStatus>`)
- `StorageIo.java` — Add new stream buffer methods (`initAIStreamBuffer`, `appendAIStreamChunk`, `consumeAIStreamChunks`, `markAIStreamDone`, `isAIStreamDone`, `clearAIStreamBuffer`)
- `ObjectifyStorageIo.java` — Implement stream buffer methods using existing `memcache` instance
- `AIAgentServiceImpl.java` — Update `getRequestStatus` implementation to use `StreamBuffer.consume()`
- `ConversationManager.java` — Replace `updateStatus`/`clearStatus` calls with `StreamBuffer.appendStatus`/`StreamBuffer.clear`. Remove dependency on `StorageIo.updateAIRequestStatus`/`getAIRequestStatus`.
- `AIResponseOrchestrator.java` — Handle `AIStreamStatus`, adaptive poll interval, manage streaming bubble
- `AIChatRenderer.java` — Incremental markdown rendering with text accumulator and code fence mitigation

### Removed
- `StorageIo.updateAIRequestStatus` / `StorageIo.getAIRequestStatus` — replaced by `StreamBuffer`. `ConversationManager` was the only caller; now routes through `StreamBuffer` instead.

## Out of Scope

- Streaming for non-IDE ChatBot component (app runtime)
- WebSocket or SSE transport
- Streaming tool call results
- Audio/voice/realtime APIs
