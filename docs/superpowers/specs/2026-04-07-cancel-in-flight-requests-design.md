# Cancel In-Flight AI Requests

**Date:** 2026-04-07
**Status:** Approved

## Problem

Users cannot cancel an AI request once it's been sent. LLM calls can take 30+ seconds (especially with tool-use loops), and the user is stuck waiting with no way to abort. This wastes time and API credits when the user realizes they sent the wrong message or no longer needs the response.

## Solution

Add a cancel mechanism that spans three layers: a UI control (Send button transforms into Stop), a client-side state reset, and a server-side cancellation flag in Memcache that LLM providers check during streaming to abort early.

## Design

### Layer 1: UI — Send/Stop Button Swap

When `setRequestInFlight(true)` is called, the Send button transforms into a Stop button (same DOM element, swapped text and click handler). When cancelled or request completes, it reverts to Send.

**AIChatDialog.java changes:**
- `setRequestInFlight(boolean)` swaps button text between `aiChatSendButton()` and `aiChatStopButton()`, swaps click handler, and changes styling (stop = red-ish).
- Stop click handler calls `orchestrator.cancelRequest()`.

### Layer 2: Client — Orchestrator Cancel

**AIResponseOrchestrator.java changes:**
- New public method `cancelRequest()` that:
  1. Finalizes any active streaming bubble so partial text is preserved.
  2. Calls the existing `cancelInFlight()` to reset client state (stop polling, clear pending response, re-enable UI).
  3. Fires `cancelRequest(projectId)` RPC to the server (fire-and-forget — `onFailure` logs the error but takes no other action).
  4. Adds a system message: "Request cancelled."
- Guard at the top of `handleResponseWithValidation`: if `!requestInFlight`, silently discard the response. This covers the case where the RPC returns after the user already clicked Stop.

### Layer 3: Server — Memcache Cancellation Flag

`StreamBuffer` is a stateless DAO — each instance is a thin wrapper around `StorageIo` Memcache calls keyed by `projectId`. Creating a new `StreamBuffer` in the cancel path is correct; it accesses the same Memcache keys as the one in the active request thread.

**StorageIo / ObjectifyStorageIo:**
- `setAIStreamCancelled(long projectId)` — sets `ai_stream:<projectId>:cancelled` in Memcache with the standard stream TTL.
- `isAIStreamCancelled(long projectId)` — checks if the flag exists.
- `clearAIStreamCancelled(long projectId)` — deletes the flag.

**StreamBuffer.java:**
- `setCancelled()` — delegates to `storageIo.setAIStreamCancelled(projectId)`.
- `checkCancelled()` — reads the flag and throws `CancelledException` if set.
- `isCancelled()` — non-throwing boolean check for use in streaming loops.
- `CancelledException` — new checked exception nested in `StreamBuffer`.
- `init()` — clears the cancelled flag (so a new request starts clean).

**AIAgentEngine.java:**
- `processRequest()`, `continueRequest()`, `reportExecutionErrors()` — wrap LLM calls in try/catch for `CancelledException`. On catch: clear the cancelled flag explicitly, clear the stream buffer, store a synthetic non-displayed `"[Request cancelled]"` assistant message to keep history role-alternating (the user message was already stored before the LLM call), and return an empty response (empty text, no operations, no errors).
- New `cancelRequest(long projectId)` method that creates a `StreamBuffer` and calls `setCancelled()`.

**AIAgentServiceImpl.java:**
- New `cancelRequest(long projectId)` RPC: validates user owns project, delegates to `engine.cancelRequest(projectId)`.

**AIAgentService.java + AIAgentServiceAsync.java:**
- Add `void cancelRequest(long projectId)` to both interfaces.

### Layer 4: LLM Provider Abort

Each provider's SSE streaming loop checks `streamBuffer.isCancelled()` periodically and aborts by closing the HTTP connection and throwing `CancelledException`.

**AnthropicCompatibleProvider.java** — `readStreamingResponse()`:
- In the `while ((line = reader.readLine()) != null)` loop, check `streamBuffer.isCancelled()` on each `data:` event. If cancelled, close the reader and connection, throw `CancelledException`.

**OpenAIChatCompletionsProvider.java** — same pattern in its SSE loop.

**OpenAIProvider.java, GeminiProvider.java, BedrockProvider.java, VertexProvider.java, OllamaProvider.java** — same pattern in their respective streaming loops. (OllamaProvider implements `LLMProvider` directly with its own SSE loop — it does not inherit from `OpenAIChatCompletionsProvider`.)

### Cancellation Timing Edge Cases

1. **Cancel arrives after LLM completes but before RPC returns**: The `processRequest` RPC returns normally, but the client already set `requestInFlight = false` in `cancelInFlight()`. The guard at the top of `handleResponseWithValidation` (`if (!requestInFlight) return;`) silently discards the stale response.

2. **Cancel arrives during tool-use loop**: The provider checks `streamBuffer.isCancelled()` in its streaming loop. Between iterations, the next `chat()` / `continueWithToolResults()` call enters a new streaming loop that checks the flag immediately.

3. **Cancel arrives during validation/execution retry**: The orchestrator's `cancelInFlight()` already resets all retry state. The pending `reportExecutionErrors` RPC response is discarded by the `!requestInFlight` guard.

4. **Race between cancel RPC and processRequest RPC**: Both run on different server threads. The cancel RPC writes to Memcache; the processRequest thread reads it. Memcache is atomic for single keys, so the ordering is well-defined.

5. **Cancel-then-immediate-resend race**: If the user clicks Stop and immediately sends a new message, the new `processRequest` calls `init()` which clears the cancelled flag. The old request thread may miss the cancellation if it hasn't checked yet. This is accepted as a known limitation — the old RPC response is still discarded client-side by the `!requestInFlight` guard, so user experience is correct. The only cost is the old LLM call continuing to completion on the server.

6. **Orphaned user message in history**: The user message is stored to Datastore before the LLM call (line 183 of `AIAgentEngine.java`). On cancellation, a synthetic non-displayed `"[Request cancelled]"` assistant message is stored to keep history role-alternating. This prevents stateless providers from seeing an unanswered user message.

### i18n

**OdeMessages.java:**
```java
@DefaultMessage("Stop")
@Description("Text on the stop button in the AI chat dialog, shown while a request is in flight")
String aiChatStopButton();

@DefaultMessage("Request cancelled.")
@Description("System message shown when the user cancels an in-flight AI request")
String aiChatRequestCancelled();
```

### Response Handling for Cancelled Requests

No new fields on `AIAgentResponse`. The client already knows it cancelled (via `requestInFlight = false`). The guard at the top of `handleResponseWithValidation` discards any response that arrives after cancellation. The server returns a standard empty response — no special "cancelled" marker needed.

## Files to Modify

| File | Change |
|------|--------|
| `shared/rpc/aiagent/AIAgentService.java` | Add `cancelRequest(long)` |
| `shared/rpc/aiagent/AIAgentServiceAsync.java` | Add async mirror |
| `server/aiagent/AIAgentServiceImpl.java` | Implement `cancelRequest` |
| `server/aiagent/AIAgentEngine.java` | Add `cancelRequest`, catch `CancelledException`, store synthetic history message |
| `server/aiagent/StreamBuffer.java` | Add `setCancelled`, `checkCancelled`, `isCancelled`, `CancelledException`; clear flag in `init()` |
| `server/storage/StorageIo.java` | Add `setAIStreamCancelled`, `isAIStreamCancelled`, `clearAIStreamCancelled` |
| `server/storage/ObjectifyStorageIo.java` | Implement via Memcache |
| `server/aiagent/llm/AnthropicCompatibleProvider.java` | Check cancellation in SSE loop |
| `server/aiagent/llm/OpenAIChatCompletionsProvider.java` | Check cancellation in SSE loop |
| `server/aiagent/llm/OpenAIProvider.java` | Check cancellation in SSE loop |
| `server/aiagent/llm/GeminiProvider.java` | Check cancellation in SSE loop |
| `server/aiagent/llm/BedrockProvider.java` | Check cancellation in SSE loop |
| `server/aiagent/llm/VertexProvider.java` | Check cancellation in SSE loop |
| `server/aiagent/llm/OllamaProvider.java` | Check cancellation in SSE loop |
| `client/editor/youngandroid/AIChatDialog.java` | Send↔Stop button swap |
| `client/editor/youngandroid/aiagent/AIResponseOrchestrator.java` | New `cancelRequest()`, guard in `handleResponseWithValidation` |
| `client/OdeMessages.java` | Add `aiChatStopButton()`, `aiChatRequestCancelled()` |

## Non-Goals

- Cancelling operations that are already being executed (Apply was clicked). Only in-flight LLM requests are cancellable.
- Undo/rollback of already-applied operations.
- Cancel button during validation/execution retries (these are fast and automatic — the existing `cancelInFlight()` handles them via the `requestInFlight` guard).
