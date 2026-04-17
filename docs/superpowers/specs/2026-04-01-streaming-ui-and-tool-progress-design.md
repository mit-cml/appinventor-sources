# Streaming UI Indicator & Tool Call Progress — Design Spec

## Problem

1. Streaming and completed AI message bubbles look identical — the user has no visual cue that a message is still being generated.
2. During tool call generation and post-streaming processing, the user sees no feedback about what the model is doing.

## Goal

- Visually distinguish in-progress streaming bubbles from completed messages.
- Show real-time progress as the model generates tool calls (action and read-only).

## Design

### 1. Streaming Bubble Visual Indicator

During streaming, the AI message bubble gets a distinct style:
- **Left border**: 3px solid `#90b4d6` (muted blue accent)
- **Background**: `#f0f0f0` (slightly lighter than the completed `#e8e8e8`)

When `finalizeStreamingBubble()` is called, both revert to the normal completed style (`#e8e8e8` background, no left border).

Implemented purely via inline GWT styles in `AIChatRenderer` — no CSS changes needed. Styles are applied in `startStreamingBubble()` and removed in `finalizeStreamingBubble()`.

**Files changed:** `AIChatRenderer.java` only.

### 2. Read-Only Tool Call Status During Streaming

During the provider's internal read-only tool loop, quiet periods occur while tools are resolved and the next LLM call happens. Currently the user sees nothing.

Before each individual `resolver.resolve()` call in the tool loop, the provider writes a status chunk:
- `streamBuffer.appendStatus("Looking up component...")` for `lookup_component`
- `streamBuffer.appendStatus("Looking up screen...")` for `lookup_screen`

This uses the existing `s:` chunk type. The client already handles status text in the polling callback. When multiple read-only tools are resolved in sequence, each status replaces the previous one.

**Files changed:** Each provider's internal tool-use loop, at the point where read-only tool calls are detected and resolved. ~1 line per provider, before each `resolver.resolve()` call.

### 3. Action Tool Call Progress During Streaming

As the LLM generates action tool calls, providers accumulate the arguments incrementally. Once a tool call block is **complete** (full name + arguments available), the provider writes a `c:` chunk with the tool name and a human-readable detail.

**Chunk format:** `c:tool_name|detail` — uses `|` as delimiter between tool name and detail to avoid collision with colons that may appear in detail strings. Tool names are `[a-z_]+` so `|` is unambiguous. Parsing rule: split on first `|`; everything after is the detail.

Detail is extracted from the completed tool call arguments:

| Tool | Detail source |
|------|--------------|
| `add_component` | `name` field (e.g., "Button1") |
| `delete_component` | `name` field |
| `set_property` | `component_name.property_name` (e.g., "Button1.Text") |
| `rename_component` | `old_name -> new_name` |
| `write_block` | YAIL summary (reuses `AIOperationFormatter.summarizeYail()` logic) |
| `delete_block` | `block` field |
| `switch_screen` | `screen_name` field |
| `create_screen` | `screen_name` field |
| `delete_screen` | `screen_name` field |
| `set_project_property` | `property` field |
| `toggle_editor` | `view` field |

**Detail extraction:** New static utility `ToolCallDetailExtractor.extract(String toolName, String inputJson)` on the server side. For `write_block`, implements its own YAIL head summarization (same algorithm as the client-side `AIOperationFormatter.summarizeYail()`, but server-side — cannot share code across the GWT client/server boundary). Note: `ConversationManager.summarizeYailHead()` already has a server-side YAIL summary implementation that can be referenced. For all others, uses lightweight JSON field extraction. For `write_block`, a length guard of 200 characters on the YAIL input prevents expensive parsing on large blocks.

**StreamBuffer changes:** New method `appendToolCall(String name, String detail)` that writes `c:name|detail`. The `consume()` method parses the last `c:` chunk (split on first `|`), maps the tool name to a verb using the label mapping table, and produces a formatted label like "Adding component Button1...". The verb mapping lives in `StreamBuffer.consume()` as a static method or switch block.

**AIStreamStatus changes:** New nullable `toolCallLabel` field. Set by `consume()` from the last `c:` chunk. The label is pre-formatted: human-readable verb + detail + "..." (e.g., "Setting Button1.Text...").

**Label mapping:**

| Tool name | Verb |
|-----------|------|
| `add_component` | "Adding component" |
| `delete_component` | "Removing component" |
| `set_property` | "Setting" |
| `rename_component` | "Renaming" |
| `write_block` | "Writing" |
| `delete_block` | "Removing block" |
| `switch_screen` | "Switching to" |
| `create_screen` | "Creating screen" |
| `delete_screen` | "Removing screen" |
| `set_project_property` | "Setting project property" |
| `toggle_editor` | "Switching to" |
| (unknown) | "Preparing changes" |

**Client display:** When `toolCallLabel` is present in the poll response, it is shown in the status area (same place as "Calling AI..."). Each new label replaces the previous one — only the most recent tool call is shown. When `toolCallLabel` is null, falls back to `statusText`. Tool call labels take precedence over status text. A `toolCallLabel` arriving also keeps the fast polling interval (250ms) active, same as `textDelta` — this ensures tool call labels update promptly.

Read-only tools use `s:` status chunks per Section 2 and do not produce `c:` chunks.

**Provider changes:** After each complete action tool call is accumulated from the stream, the provider writes a `c:` chunk. The exact hook point differs per provider:
- **Anthropic:** `content_block_stop` event when the block type is `tool_use` (currently has no handling — needs to be added)
- **OpenAI:** `response.output_item.done` event for function_call items
- **Gemini:** After parsing a complete `functionCall` part from a stream event
- **Ollama:** After the final `done: true` line — all tool calls are in the complete response. Write one `c:` chunk per tool call.
- **MiniMax:** After accumulating a complete tool call from `delta.tool_calls` fragments (when `finish_reason` is set)

```java
if (streamBuffer != null) {
    String detail = ToolCallDetailExtractor.extract(toolName, inputJson);
    streamBuffer.appendToolCall(toolName, detail);
}
```

This also applies to `continueWithToolResults()` streaming responses — same `c:` chunks are written during continuation calls.

### 4. Error Handling and Edge Cases

| Scenario | Behavior |
|----------|----------|
| Tool call JSON can't be parsed for detail | Fall back to tool name only (e.g., "Adding component...") |
| YAIL summary fails | Fall back to "Writing block..." |
| Multiple tool calls in same poll interval | Only the last `c:` chunk is shown |
| Tool call label after `done=true` | Won't happen — `markDone()` is called after all tool call blocks |
| Read-only status overlaps with action tool label | Tool call label (`c:`) takes precedence in display |
| Style removal fails on finalize | No-op — cosmetic only |

## Files Changed

### New files
- `ToolCallDetailExtractor.java` — Static utility for extracting human-readable detail from tool call arguments

### Modified files
- `AIStreamStatus.java` — Add `toolCallLabel` field
- `StreamBuffer.java` — Add `appendToolCall()`, update `consume()` to parse `c:` chunks and format labels
- `AIChatRenderer.java` — Apply/remove streaming visual styles (left border, lighter background)
- `AIResponseOrchestrator.java` — Show `toolCallLabel` in status area when present
- `AnthropicProvider.java` — Write `c:` chunk on tool call block completion; write `s:` status on read-only tool resolution
- `OpenAIProvider.java` — Same
- `GeminiProvider.java` — Same
- `OllamaProvider.java` — Same
- `MiniMaxProvider.java` — Same

## Out of Scope

- Streaming tool call arguments incrementally (partial JSON)
- Showing multiple tool calls simultaneously
- Tool call progress bar or count
