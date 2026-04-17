# Error Feedback via Tool Results

**Date:** 2026-04-05
**Status:** Approved
**Branch:** ai-agent

## Problem

When the client reports execution or validation errors back to the server, the
server sends the feedback to the LLM as a `user`-role message via
`provider.chat()`. The LLM treats this as human input and responds
conversationally ("Thanks — fixing now"), exposing internal machinery in the
chat. Wrapping the message in `<system>` tags reduces but does not reliably
prevent this across all providers (GPT-4o in particular ignores the tags).

## Solution

Route all error feedback through native tool results instead of user messages.
The LLM already emitted tool calls (`write_block`, `add_component`, etc.) for
each operation. When the client reports execution outcomes, the server patches
the per-tool-call results in the continuation state and calls
`continueWithToolResults()` instead of `chat()`. LLMs never acknowledge tool
results conversationally — they process them silently by design.

## Design

### 1. Tool Result Outcome Values

Today `annotateToolCallResults()` writes two outcome types:

| Outcome | When | Value |
|---------|------|-------|
| Accepted | Tool call passed parse + mode enforcement | `"Done."` |
| Rejected | Tool call failed parse or mode enforcement | `"REJECTED: <error>"` |

Add three new values for client-side execution/validation outcomes:

| Outcome | When | Value |
|---------|------|-------|
| Applied | Operation executed successfully | `"Applied successfully."` |
| Failed | Operation failed execution or validation | `"FAILED: <error detail>"` |
| Skipped | Operation never attempted (halted after prior failure) | `"SKIPPED: execution halted after a prior failure."` |

These are plain strings in the `toolCallResults[].result` field. Every
provider's `continueWithToolResults()` already reads this field and passes it
through as tool result content. No new data structures required.

For validation failures, the result string includes the failing code so the LLM
can see and fix its mistake:
```
FAILED: write_block validation failed: Unterminated list — missing 1 closing
parenthesis.
Failing block code: (def (p$BrokenTest) (set-var! g$moves 0)
```

### 2. LLMProvider Interface Change

Current signature:

```java
LLMResponse continueWithToolResults(String continuationState,
    List<LLMTool> tools, ReadOnlyToolResolver resolver,
    StreamBuffer streamBuffer)
```

New signature:

```java
LLMResponse continueWithToolResults(String continuationState,
    List<LLMTool> tools, List<String> contextMessages,
    ReadOnlyToolResolver resolver, StreamBuffer streamBuffer)
```

Context messages are always provided — both in the normal continuation path
(`continueRequest`) and the error retry path (`reportExecutionErrors`). They
carry the fresh editor state (mode instructions, project overview, current
screen component tree and blocks YAIL) so the LLM sees the post-execution
state.

Each provider injects context messages after the tool results and before
triggering the next completion, using the same user/assistant turn pattern
already used in their `chat()` / `buildMessages()` methods.

### 3. Provider Implementations

All providers with their own `continueWithToolResults()` implementation receive
the same change pattern. Thin subclasses (`AnthropicProvider`, `MiniMaxProvider`,
`OpenRouterProvider`) inherit the change from their base classes and need no
individual modifications.

| Provider | Tool result format | Context injection |
|----------|-------------------|-------------------|
| AnthropicCompatibleProvider | `tool_result` blocks in user message | user/assistant pairs after tool results |
| OpenAIChatCompletionsProvider | `role: "tool"` messages | user/assistant pairs |
| OpenAIProvider (Responses API) | `function_call_output` items | `role: "user"` items in input array |
| GeminiProvider | `functionResponse` parts | user/model turns |
| VertexProvider | Same as Gemini | Same as Gemini |
| BedrockProvider | `toolResult` blocks | user/assistant turns |
| OllamaProvider | Same as OpenAIChatCompletions | Same |

No new tool result format parsing needed. The `result` string is free-form text
that providers pass through as-is.

### 4. Server-Side Flow — `reportExecutionErrors` Redesign

**Current flow:**
1. Receive `List<AIOperationResult>` from client
2. Build text feedback via `buildExecutionErrorFeedback()`
3. Store feedback as a user message in conversation history
4. Call `provider.chat()` with feedback as `userMessage`

**New flow:**
1. Receive `List<AIOperationResult>` from client
2. Load continuation state from `conversationManager.getConversation()`
3. Patch `toolCallResults` in continuation state with client outcomes:
   - SUCCEEDED → `"Applied successfully."`
   - FAILED → `"FAILED: <error detail>"`
   - SKIPPED → `"SKIPPED: execution halted after a prior failure."`
4. Patch system prompt in continuation state (`patchSystemPrompt()`)
5. Build fresh context messages from client's updated editor snapshot
6. Call `provider.continueWithToolResults(patchedState, tools, contextMessages,
   resolver, streamBuffer)`
7. No user message stored in history

**Positional mapping:** The `toolCallResults` array is positionally aligned with
pending tool call IDs. The client's `AIOperationResult` list is positionally
aligned with operations returned in the response. Only `ACCEPTED` entries in
`toolCallResults` map to client operations — `PARSE_REJECTED` and
`MODE_REJECTED` entries are already resolved and don't produce client-side
operations.

Patching algorithm:

```
clientIdx = 0
for each entry in toolCallResults:
    if entry.result starts with "REJECTED:":
        // Already set by server-side validation. Leave as-is.
        continue
    // This is an ACCEPTED entry — map to the next client result.
    patch entry.result with clientResults[clientIdx].toToolResultString()
    clientIdx++
```

**Fallback:** If `toolCallResults` is missing from the continuation state (e.g.,
Memcache eviction between `finalizeResponse` and `reportExecutionErrors`), fall
back to returning an error asking the user to retry. This mirrors the existing
check for missing `providerRef`.

**First request:** `processRequest()` uses `chat()` and creates the
continuation state in `finalizeResponse()`. Only subsequent retries and
continuations use `continueWithToolResults()`, so the first request is
unaffected.

### 5. Validation Retries

Same mechanism as execution retries. The client's `sendRetryRequest()` still
calls the server's `reportExecutionErrors` RPC. The server patches tool results
and calls `continueWithToolResults()`.

Validation-specific semantics:
- Valid operations (preserved client-side): `"Validated successfully. Pending
  application."` — they passed validation and will be merged back by the client
  after the LLM fixes the invalid ones. Distinct from `"Applied successfully."`
  to avoid implying the project state already reflects these changes.
- Invalid operations: `"FAILED: <validation error>\nFailing block code: <YAIL>"`
  — includes the failing code so the LLM can see and fix its mistake.
- No SKIPPED — validation checks all block operations independently, it does
  not halt on first failure.

### 6. `continueRequest` Path

`continueRequest()` also passes fresh context messages through
`continueWithToolResults()` for consistency. Today it relies on the LLM's stale
snapshot from the original request (or patched system prompt for stateful
providers). With this change, the LLM always sees the current editor state after
operations were applied.

**Stateful providers (OpenAI Responses API, Gemini, Vertex):** These providers
manage conversation state server-side. Injecting context messages as user turns
after tool results in the input array needs validation during implementation.
If a stateful provider's API rejects interleaved user content after tool
results, context messages should be omitted for that provider (matching current
behavior) and addressed in a follow-up.

**Narration retry:** `retryIfNarration()` continues to use `provider.chat()`
with a nudge message. It is unaffected by this change — the nudge is a prompt,
not a tool result. `chat()` already accepts `contextMessages` in its current
signature.

### 7. Cleanup

**Removed:**
- `LLMResponseParser.buildExecutionErrorFeedback()` — feedback flows through
  per-tool-call results, no aggregated text message needed
- `<system>` tag wrapping of error feedback in `reportExecutionErrors()`
- The `chat()` call in `reportExecutionErrors()` — replaced by
  `continueWithToolResults()`
- Conversation history storage of error feedback
  (`"[Execution error feedback] ..."`) — tool results are part of the
  continuation state, not stored as user messages. The patched tool results are
  logged via `AIDebug.log()` for debugging (file output in dev, dedicated logger
  in prod).

**Kept:**
- `AIAgentRequest.wrapPlatformMessage()` — still used for narration nudge and
  rejection message
- `appinventor_reference.md` "System Messages" section — still relevant for
  nudge/rejection
- `AIAgentRequest.isPlatformMessage()` flag — still used for rejection via
  `processRequest()`
- `annotateToolCallResults()` — still used in `finalizeResponse()` for initial
  parse/mode rejections; client execution results patch over these annotations

### 8. Idempotent Operation Execution

LLMs may re-emit tool calls that already succeeded during a retry, despite
tool results indicating success. To prevent cascading failures (e.g., adding
a component that already exists → error → another retry), operations must be
idempotent. The client's `AIOperationExecutor` should tolerate re-execution
gracefully.

Per-operation idempotency rules:

| Operation | Idempotency behavior |
|-----------|---------------------|
| `add_component` | If a component with the same name already exists, skip silently. Matched by name only — type, parent, and properties are not compared. |
| `delete_component` | If the component does not exist, skip silently |
| `set_property` | Already idempotent — setting the same value is a no-op |
| `rename_component` | See edge cases below |
| `write_block` | Already idempotent — replaces existing block definition |
| `delete_block` | If the block does not exist, skip silently. Note: if the LLM re-emits both `write_block` and `delete_block` for the same identifier, phase ordering (phase 3 writes, phase 4 deletes) makes this safe — net effect is "deleted." |
| `create_screen` | If the screen already exists, skip silently |
| `delete_screen` | If the screen does not exist, skip silently |
| `switch_screen` | Already idempotent |
| `toggle_editor` | Already idempotent |
| `set_project_prop` | Already idempotent |

**`rename_component` edge cases:**

| old_name exists | new_name exists | Meaning | Action |
|-----------------|-----------------|---------|--------|
| Yes | No | Normal rename | Execute |
| No | Yes | Already renamed (re-emission) | Skip silently |
| No | No | Component missing — genuine error | FAILED |
| Yes | Yes | Name conflict — different components | FAILED (fall through to normal validation) |

**Where guards live:** Idempotency checks are added in `AIOperationExecutor`
before dispatching to validators/operation classes. The executor already sits
between parsing and execution, so it is the single point where "skip if already
done" can be decided without touching individual validators. This means only
`AIOperationExecutor.java` needs changes — not the validators or operation
classes.

Idempotent skips are reported as SUCCEEDED to the server (they are effectively
no-ops), not as errors. This prevents the retry loop from treating a harmless
re-emission as a failure. Skips are logged at FINE level with the operation type
and identifying fields for debug observability.

A system prompt reinforcement also helps reduce unnecessary re-emissions. Added
to `appinventor_reference.md` Section 2 ("Rules and Instructions"), near the
"Action, Not Narration" subsection:
> "When a tool result starts with 'Applied successfully' or 'Validated
> successfully', do not re-emit that tool call."

### 9. CONTRIBUTING_AI.md Updates

The following sections require updates:

- **"Error Handling and Retry"** (line 679+): Update flowchart — `reportExecutionErrors`
  uses `continueWithToolResults()` not `chat()`. Document new outcome strings.
- **"Server-Side Retry Handling"** (line 752+): Rewrite to describe patching
  `toolCallResults` and calling `continueWithToolResults()`. Remove references
  to `buildExecutionErrorFeedback()` and `<system>` wrapping.
- **"Request Lifecycle"** (line 474+): Update error retry path in sequence
  diagram to show `continueWithToolResults()`.
- **"LLM Provider System"** (line 789+): Update `continueWithToolResults()`
  signature in "Adding a New Provider" guide. Document `contextMessages`
  parameter.
- **"Validation Pipeline"** (line 402+): Update Stage 1 flowchart — validation
  retries go through `continueWithToolResults()`.
- **"Conventions"** (line 994+): Add convention that execution and validation
  errors are communicated via tool results, not user messages.

## Files Affected

### Server
- `AIAgentEngine.java` — `reportExecutionErrors()` redesign, `continueRequest()` passes context messages
- `LLMResponseParser.java` — remove `buildExecutionErrorFeedback()`
- `LLMProvider.java` — `continueWithToolResults()` signature change
- `AnthropicCompatibleProvider.java` — accept and inject context messages
- `OpenAIChatCompletionsProvider.java` — same
- `OpenAIProvider.java` — same
- `GeminiProvider.java` — same
- `VertexProvider.java` — same
- `BedrockProvider.java` — same
- `OllamaProvider.java` — same

### Shared
- No changes to DTOs — `AIOperationResult` already carries the needed fields

### Client
- `AIOperationExecutor.java` — add idempotency guards per operation type
- No RPC changes — `sendRetryRequest()` still calls `reportExecutionErrors` RPC
  with the same `AIOperationResult` DTOs

### Documentation
- `CONTRIBUTING_AI.md` — update 6 sections as described above

### Resources
- `appinventor_reference.md` — add tool result idempotency instruction (do not
  re-emit successful tool calls). System Messages section still needed for
  other platform messages.
