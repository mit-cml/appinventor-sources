# Streaming UI Indicator & Tool Call Progress — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Visually distinguish in-progress streaming bubbles from completed messages, and show real-time progress as the model generates tool calls.

**Architecture:** Three layered changes: (1) CSS-style tweaks on the streaming bubble in AIChatRenderer, (2) a new `c:` chunk type in StreamBuffer for tool call progress with a server-side detail extractor, (3) provider-level hooks to write status/tool-call chunks during streaming and read-only tool resolution.

**Tech Stack:** Java (App Engine), GWT, Memcache (existing StreamBuffer infrastructure)

**Spec:** `docs/superpowers/specs/2026-04-01-streaming-ui-and-tool-progress-design.md`

---

## File Map

### New Files
| File | Responsibility |
|------|---------------|
| `appinventor/appengine/src/com/google/appinventor/server/aiagent/ToolCallDetailExtractor.java` | Static utility: extract human-readable detail from tool call name + arguments JSON |

### Modified Files
| File | Change |
|------|--------|
| `appinventor/appengine/src/com/google/appinventor/shared/rpc/aiagent/AIStreamStatus.java` | Add `toolCallLabel` field |
| `appinventor/appengine/src/com/google/appinventor/server/aiagent/StreamBuffer.java` | Add `appendToolCall()`, update `consume()` to parse `c:` chunks and format labels |
| `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/AIChatRenderer.java` | Apply/remove streaming visual styles |
| `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/AIResponseOrchestrator.java` | Show `toolCallLabel` in status, keep fast poll for tool labels |
| `appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/AnthropicProvider.java` | Write `c:` chunks on tool call completion, `s:` on read-only resolution |
| `appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/OpenAIProvider.java` | Same |
| `appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/GeminiProvider.java` | Same |
| `appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/OllamaProvider.java` | Same |
| `appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/MiniMaxProvider.java` | Same |

---

## Task 1: Streaming Bubble Visual Indicator

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/AIChatRenderer.java`

- [ ] **Step 1: Add streaming style in `startStreamingBubble()`**

After the bubble FlowPanel is obtained (line ~98), apply the streaming visual styles:

```java
  public void startStreamingBubble() {
    streamingTextAccumulator = "";
    streamingWrapper = createMessageBubble(
        MESSAGES.aiChatAiLabel(), "", false);
    FlowPanel bubble = (FlowPanel) streamingWrapper.getWidget(0);
    streamingMessageHtml = (HTML) bubble.getWidget(1);
    // Apply streaming visual indicator
    bubble.getElement().getStyle().setProperty("background", "#f0f0f0");
    bubble.getElement().getStyle().setProperty("borderLeft", "3px solid #90b4d6");
    chatHistory.add(streamingWrapper);
    scrollToBottom();
  }
```

- [ ] **Step 2: Remove streaming style in `finalizeStreamingBubble()`**

Before setting the final HTML, revert the bubble styles to the normal completed appearance:

```java
  public void finalizeStreamingBubble(String finalText) {
    if (streamingMessageHtml != null) {
      // Revert to completed bubble style
      FlowPanel bubble = (FlowPanel) streamingWrapper.getWidget(0);
      bubble.getElement().getStyle().setProperty("background", "#e8e8e8");
      bubble.getElement().getStyle().clearProperty("borderLeft");
      streamingMessageHtml.setHTML(markdownToSafeHtml(finalText));
      scrollToBottom();
    }
    streamingWrapper = null;
    streamingMessageHtml = null;
    streamingTextAccumulator = "";
  }
```

- [ ] **Step 3: Verify it compiles**

- [ ] **Step 4: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/AIChatRenderer.java
git commit -m "feat(ai-agent): add visual indicator for streaming bubbles"
```

---

## Task 2: Add `toolCallLabel` to AIStreamStatus

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/shared/rpc/aiagent/AIStreamStatus.java`

- [ ] **Step 1: Add field, update constructor and getter**

```java
public class AIStreamStatus implements IsSerializable, Serializable {
  private static final long serialVersionUID = 2L;

  private String statusText;
  private String textDelta;
  private boolean done;
  private String toolCallLabel;

  public AIStreamStatus() {}

  public AIStreamStatus(String statusText, String textDelta, boolean done,
      String toolCallLabel) {
    this.statusText = statusText;
    this.textDelta = textDelta;
    this.done = done;
    this.toolCallLabel = toolCallLabel;
  }

  public String getStatusText() { return statusText; }
  public String getTextDelta() { return textDelta; }
  public boolean isDone() { return done; }
  public String getToolCallLabel() { return toolCallLabel; }
}
```

Note: The old 3-arg constructor is removed. All call sites must be updated to use the 4-arg constructor (passing `null` for `toolCallLabel` where not applicable).

- [ ] **Step 2: Fix all call sites**

Search for `new AIStreamStatus(` in the codebase. Update each to pass the 4th argument:
- `StreamBuffer.consume()` — will be updated in Task 3
- `AIAgentServiceImpl.getRequestStatus()` error case — change to `new AIStreamStatus("error", null, true, null)`

- [ ] **Step 3: Verify it compiles**

- [ ] **Step 4: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/shared/rpc/aiagent/AIStreamStatus.java \
       appinventor/appengine/src/com/google/appinventor/server/aiagent/AIAgentServiceImpl.java
git commit -m "feat(ai-agent): add toolCallLabel field to AIStreamStatus"
```

---

## Task 3: Create ToolCallDetailExtractor + Update StreamBuffer

**Files:**
- Create: `appinventor/appengine/src/com/google/appinventor/server/aiagent/ToolCallDetailExtractor.java`
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/StreamBuffer.java`

- [ ] **Step 1: Create ToolCallDetailExtractor.java**

```java
package com.google.appinventor.server.aiagent;

import org.json.JSONObject;

/**
 * Extracts a human-readable detail string from a tool call's name and arguments.
 * Used to provide real-time progress during streaming (e.g., "Button1" for add_component).
 */
public final class ToolCallDetailExtractor {

  private ToolCallDetailExtractor() {}

  /**
   * Extract a short detail string from the tool call arguments.
   *
   * @param toolName  the tool name (e.g., "add_component")
   * @param inputJson the raw JSON arguments string
   * @return a detail string, or empty string if extraction fails
   */
  public static String extract(String toolName, String inputJson) {
    if (toolName == null || inputJson == null) {
      return "";
    }
    try {
      JSONObject args = new JSONObject(inputJson);
      switch (toolName) {
        case "add_component":
          return args.optString("name", "");
        case "delete_component":
          return args.optString("name", "");
        case "set_property":
          String comp = args.optString("component_name", "");
          String prop = args.optString("property_name", "");
          return comp.isEmpty() ? prop : comp + "." + prop;
        case "rename_component":
          String oldName = args.optString("old_name", "");
          String newName = args.optString("new_name", "");
          return oldName.isEmpty() ? newName : oldName + " \u2192 " + newName;
        case "write_block":
          return summarizeYail(args.optString("yail", ""));
        case "delete_block":
          return args.optString("block", "");
        case "switch_screen":
          return args.optString("screen_name", "");
        case "create_screen":
          return args.optString("screen_name", "");
        case "delete_screen":
          return args.optString("screen_name", "");
        case "set_project_property":
          return args.optString("property", "");
        case "toggle_editor":
          return args.optString("view", "");
        default:
          return "";
      }
    } catch (Exception e) {
      return "";
    }
  }

  /**
   * Summarize YAIL code into a short human-readable label.
   * Server-side equivalent of client-side AIOperationFormatter.summarizeYail().
   * References ConversationManager.summarizeYailHead() for approach.
   */
  private static String summarizeYail(String yail) {
    if (yail == null || yail.isEmpty()) {
      return "block";
    }
    // Only inspect first 200 chars for performance
    String head = yail.length() > 200 ? yail.substring(0, 200) : yail;
    String trimmed = head.trim();

    // define-event ComponentName EventName
    if (trimmed.startsWith("(define-event ")) {
      String rest = trimmed.substring(14).trim();
      String[] parts = rest.split("\\s+", 3);
      if (parts.length >= 2) {
        return parts[0] + "." + parts[1];
      }
    }
    // define-generic-event ComponentType EventName
    if (trimmed.startsWith("(define-generic-event ")) {
      String rest = trimmed.substring(22).trim();
      String[] parts = rest.split("\\s+", 3);
      if (parts.length >= 2) {
        return "any " + parts[0] + "." + parts[1];
      }
    }
    // def g$varName (global variable)
    if (trimmed.startsWith("(def ") && trimmed.contains("g$")) {
      int idx = trimmed.indexOf("g$");
      String rest = trimmed.substring(idx + 2);
      String varName = rest.split("[\\s()]+", 2)[0];
      return varName + " (variable)";
    }
    // def (p$procName ...) or def-return (p$procName ...)
    if (trimmed.contains("p$")) {
      int idx = trimmed.indexOf("p$");
      String rest = trimmed.substring(idx + 2);
      String procName = rest.split("[\\s()]+", 2)[0];
      return procName + " (procedure)";
    }

    return "block";
  }
}
```

- [ ] **Step 2: Add `appendToolCall()` to StreamBuffer**

```java
  /** Append a tool call progress chunk. */
  public void appendToolCall(String toolName, String detail) {
    if (toolName != null && !toolName.isEmpty()) {
      String chunk = "c:" + toolName + "|" + (detail != null ? detail : "");
      storageIo.appendAIStreamChunk(projectId, chunk);
    }
  }
```

- [ ] **Step 3: Update `consume()` in StreamBuffer to parse `c:` chunks**

```java
  public AIStreamStatus consume() {
    List<String> chunks = storageIo.consumeAIStreamChunks(projectId);
    boolean done = storageIo.isAIStreamDone(projectId);

    StringBuilder textBuilder = null;
    String lastStatus = null;
    String lastToolCallLabel = null;

    for (String chunk : chunks) {
      if (chunk.startsWith("t:")) {
        if (textBuilder == null) {
          textBuilder = new StringBuilder();
        }
        textBuilder.append(chunk.substring(2));
      } else if (chunk.startsWith("s:")) {
        lastStatus = chunk.substring(2);
      } else if (chunk.startsWith("c:")) {
        lastToolCallLabel = formatToolCallLabel(chunk.substring(2));
      }
    }

    return new AIStreamStatus(
        lastStatus,
        textBuilder != null ? textBuilder.toString() : null,
        done,
        lastToolCallLabel
    );
  }

  /**
   * Format a raw tool call chunk ("tool_name|detail") into a human-readable label.
   */
  private static String formatToolCallLabel(String raw) {
    // Split on first '|'
    int sep = raw.indexOf('|');
    String toolName = sep >= 0 ? raw.substring(0, sep) : raw;
    String detail = sep >= 0 ? raw.substring(sep + 1) : "";

    String verb;
    switch (toolName) {
      case "add_component":     verb = "Adding component"; break;
      case "delete_component":  verb = "Removing component"; break;
      case "set_property":      verb = "Setting"; break;
      case "rename_component":  verb = "Renaming"; break;
      case "write_block":       verb = "Writing"; break;
      case "delete_block":      verb = "Removing block"; break;
      case "switch_screen":     verb = "Switching to"; break;
      case "create_screen":     verb = "Creating screen"; break;
      case "delete_screen":     verb = "Removing screen"; break;
      case "set_project_property": verb = "Setting project property"; break;
      case "toggle_editor":     verb = "Switching to"; break;
      default:                  verb = "Preparing changes"; break;
    }

    if (detail.isEmpty()) {
      return verb + "...";
    }
    return verb + " " + detail + "...";
  }
```

- [ ] **Step 4: Verify it compiles**

- [ ] **Step 5: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/server/aiagent/ToolCallDetailExtractor.java \
       appinventor/appengine/src/com/google/appinventor/server/aiagent/StreamBuffer.java
git commit -m "feat(ai-agent): add tool call detail extraction and StreamBuffer c: chunk support"
```

---

## Task 4: Update Client to Show Tool Call Labels

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/AIResponseOrchestrator.java`

- [ ] **Step 1: Update polling callback to handle `toolCallLabel`**

In `startPollingStatus()`, after the `textDelta` handling block (line ~738), add tool call label handling. Also, `toolCallLabel` should keep fast polling active:

```java
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
                  pollingTimer.cancel();
                  pollingTimer.scheduleRepeating(POLL_INTERVAL_FAST_MS);
                }
                callback.appendStreamingText(status.getTextDelta());
              }
              // Show tool call label — takes precedence over statusText
              if (status.getToolCallLabel() != null) {
                callback.setStatusText(status.getToolCallLabel());
                // Ensure fast polling during tool call generation
                if (!streamingActive) {
                  pollingTimer.cancel();
                  pollingTimer.scheduleRepeating(POLL_INTERVAL_FAST_MS);
                }
              }
              if (status.isDone()) {
                stopPollingStatus();
              }
            }
```

Note: `toolCallLabel` is shown via the existing `setStatusText()` — it replaces whatever was there. The tool call label takes precedence since it's checked after `statusText`.

- [ ] **Step 2: Verify it compiles**

- [ ] **Step 3: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/AIResponseOrchestrator.java
git commit -m "feat(ai-agent): show tool call labels in status during streaming"
```

---

## Task 5: Add Read-Only Tool Status to All Providers

**Files:**
- Modify: All 5 provider files (tool-use loop where `resolver.resolve()` is called)

- [ ] **Step 1: AnthropicProvider — read-only tool status**

In the read-only tool resolution loop (around line ~171), before each `resolver.resolve()` call:

```java
for (ToolUseBlock block : readOnlyBlocks) {
  // Show read-only tool status to user
  if (streamBuffer != null) {
    if ("lookup_component".equals(block.name)) {
      streamBuffer.appendStatus("Looking up component...");
    } else if ("lookup_screen".equals(block.name)) {
      streamBuffer.appendStatus("Looking up screen...");
    }
  }
  String result;
  try {
    result = resolver.resolve(block.name, block.inputJson);
  } catch (ReadOnlyToolException e) {
    result = "Error: " + e.getMessage();
  }
  // ...
}
```

- [ ] **Step 2: OpenAIProvider — same pattern**

Before each `resolver.resolve()` call (around line ~228):
```java
  if (streamBuffer != null) {
    if ("lookup_component".equals(info.name)) {
      streamBuffer.appendStatus("Looking up component...");
    } else if ("lookup_screen".equals(info.name)) {
      streamBuffer.appendStatus("Looking up screen...");
    }
  }
```

- [ ] **Step 3: GeminiProvider — same pattern**

Before each `resolver.resolve()` call (around line ~217).

- [ ] **Step 4: OllamaProvider — same pattern**

Before each `resolver.resolve()` call (around line ~166).

- [ ] **Step 5: MiniMaxProvider — same pattern**

Before each `resolver.resolve()` call (around line ~167).

- [ ] **Step 6: Verify it compiles**

- [ ] **Step 7: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/AnthropicProvider.java \
       appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/OpenAIProvider.java \
       appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/GeminiProvider.java \
       appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/OllamaProvider.java \
       appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/MiniMaxProvider.java
git commit -m "feat(ai-agent): show read-only tool resolution status during streaming"
```

---

## Task 6: Add Action Tool Call Progress to All Providers

**Files:**
- Modify: All 5 provider files (streaming response readers)

Each provider writes a `c:` chunk when a complete action tool call block is read from the stream. The hook point differs per provider.

- [ ] **Step 1: AnthropicProvider — write `c:` on content_block_stop for tool_use blocks**

In `readStreamingResponse()`, the code tracks content blocks via `handleContentBlockStart()`. When a `content_block_stop` event is received for a `tool_use` block, write the chunk. Add handling for the `content_block_stop` event:

```java
} else if ("content_block_stop".equals(currentEventType)) {
  JSONObject stopData = new JSONObject(data);
  int index = stopData.getInt("index");
  // Check if this was a tool_use block and write progress
  if (streamBuffer != null && index < contentBlocks.size()) {
    JSONObject block = contentBlocks.get(index);
    if (block.has("content_block")) {
      JSONObject cb = block.getJSONObject("content_block");
      if ("tool_use".equals(cb.optString("type"))) {
        String toolName = cb.optString("name", "");
        // Get accumulated input JSON for this block
        String inputJson = /* accumulated input_json for this index */;
        String detail = ToolCallDetailExtractor.extract(toolName, inputJson);
        streamBuffer.appendToolCall(toolName, detail);
      }
    }
  }
}
```

The exact code depends on how the provider tracks accumulated tool input JSON per block. Read the current `handleContentBlockDelta()` implementation to find where `input_json_delta` fragments are accumulated, and extract the complete JSON from there.

Add import: `import com.google.appinventor.server.aiagent.ToolCallDetailExtractor;`

- [ ] **Step 2: OpenAIProvider — write `c:` on `response.output_item.done` for function_call items**

In `readStreamingResponse()`, add handling for the `response.output_item.done` event (currently skipped). When the item type is `function_call`, extract name and arguments:

```java
} else if ("response.output_item.done".equals(eventType)) {
  JSONObject itemData = new JSONObject(data);
  JSONObject item = itemData.optJSONObject("item");
  if (item != null && "function_call".equals(item.optString("type"))) {
    String toolName = item.optString("name", "");
    String inputJson = item.optString("arguments", "{}");
    if (streamBuffer != null) {
      String detail = ToolCallDetailExtractor.extract(toolName, inputJson);
      streamBuffer.appendToolCall(toolName, detail);
    }
  }
}
```

Add import: `import com.google.appinventor.server.aiagent.ToolCallDetailExtractor;`

- [ ] **Step 3: GeminiProvider — write `c:` when a complete functionCall part is parsed**

In `processStreamEvent()` (or wherever functionCall parts are handled), when a `functionCall` part is detected:

```java
if (part.has("functionCall")) {
  JSONObject fc = part.getJSONObject("functionCall");
  String toolName = fc.optString("name", "");
  String argsJson = fc.optJSONObject("args") != null ? fc.getJSONObject("args").toString() : "{}";
  if (streamBuffer != null) {
    String detail = ToolCallDetailExtractor.extract(toolName, argsJson);
    streamBuffer.appendToolCall(toolName, detail);
  }
  nonTextParts.put(part);
}
```

Add import: `import com.google.appinventor.server.aiagent.ToolCallDetailExtractor;`

- [ ] **Step 4: OllamaProvider — write `c:` chunks from the final response's tool_calls**

Ollama's streaming format delivers tool calls only in the final `done: true` line. After parsing the final response (which contains the complete tool_calls array), iterate and write `c:` for each:

```java
if (finalResponse != null && streamBuffer != null) {
  JSONObject message = finalResponse.optJSONObject("message");
  if (message != null) {
    JSONArray toolCalls = message.optJSONArray("tool_calls");
    if (toolCalls != null) {
      for (int i = 0; i < toolCalls.length(); i++) {
        JSONObject tc = toolCalls.getJSONObject(i);
        JSONObject fn = tc.optJSONObject("function");
        if (fn != null) {
          String toolName = fn.optString("name", "");
          String inputJson = fn.optJSONObject("arguments") != null
              ? fn.getJSONObject("arguments").toString() : "{}";
          String detail = ToolCallDetailExtractor.extract(toolName, inputJson);
          streamBuffer.appendToolCall(toolName, detail);
        }
      }
    }
  }
}
```

Add import: `import com.google.appinventor.server.aiagent.ToolCallDetailExtractor;`

- [ ] **Step 5: MiniMaxProvider — write `c:` when a tool call is fully accumulated**

MiniMax accumulates tool calls from `delta.tool_calls` fragments. When `finish_reason` is set and tool calls are present, write `c:` for each completed tool call:

```java
// After the SSE loop, before markDone():
if (streamBuffer != null && accumulatedToolCalls != null) {
  for (/* each accumulated tool call */) {
    String toolName = /* function name */;
    String inputJson = /* accumulated arguments */;
    String detail = ToolCallDetailExtractor.extract(toolName, inputJson);
    streamBuffer.appendToolCall(toolName, detail);
  }
}
```

Add import: `import com.google.appinventor.server.aiagent.ToolCallDetailExtractor;`

- [ ] **Step 6: Verify it compiles**

- [ ] **Step 7: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/AnthropicProvider.java \
       appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/OpenAIProvider.java \
       appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/GeminiProvider.java \
       appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/OllamaProvider.java \
       appinventor/appengine/src/com/google/appinventor/server/aiagent/llm/MiniMaxProvider.java
git commit -m "feat(ai-agent): show action tool call progress during streaming"
```

---

## Task 7: Verification

- [ ] **Step 1: Verify full build passes**

Run the project's full build command.

- [ ] **Step 2: Manual testing**

Test with the default provider (Anthropic):
1. Send a message that triggers text only (e.g., "explain what App Inventor is") — verify the streaming bubble has a blue left border and lighter background that disappears on completion
2. Send a message that triggers tool calls (e.g., "add a Button to Screen1") — verify:
   - Text streams with the visual indicator
   - Status shows tool call progress like "Adding component Button1..."
   - Streaming bubble finalizes correctly
3. Send a message that triggers read-only tools (e.g., "what components are on Screen1?") — verify status shows "Looking up screen..." during quiet periods

- [ ] **Step 3: Commit any fixes**
