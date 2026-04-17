# AI Companion Read Integration — Design

**Date:** 2026-04-14
**Branch:** `ai-agent`
**Status:** Draft — awaiting review

---

## 1. Problem

The AI Agent today sees everything the *editor* knows: designer tree, YAIL for all blocks, warnings, project settings. It sees nothing the *device* knows: whether the Companion is paired, the active screen on the device, the text a user typed into a `TextBox`, the value of a global variable at runtime, or the last few errors the running app threw.

This gap shows up most painfully in debugging. When a user says "the list is empty but I know I put stuff in it," the agent has to guess from source. When a runtime error fires — typical example: the user puts a `4` into a `TextBox` and triggers an index-out-of-bounds — the user has to copy-paste the stack trace and describe the input state. The agent has no way to look at the running app.

The Companion already exposes a live Scheme REPL through `process-repl-input` (`runtime.scm:3725`) and returns results through `RetValManager.appendReturnValue` (`runtime.scm:3773`). The plumbing to evaluate expressions on the device and route results back to the editor is already in place — it's what "Do It" uses. This spec describes how to expose a tightly-scoped, read-only slice of that plumbing to the AI Agent, plus a user-initiated error handoff button, without changing the Companion itself and without adding any new mutation surface.

## 2. Goals and Non-Goals

### Goals

1. Let the AI Agent read runtime state from a paired Companion — component properties, global variables, and recent logs — with structured tool calls that the LLM can issue during a conversation.
2. Let the user hand a runtime error to the AI Agent with one click, the same way "Explain Block" hands a block to the AI.
3. Gate all runtime visibility behind an explicit, per-session user toggle. No automatic runtime reads without consent.
4. Add zero new mutation surface. The LLM cannot set properties, call methods, push screens, or send arbitrary Scheme through these tools.

### Non-Goals

- No change to the Companion (`AppInvHTTPD`, `RetValManager`, WebRTC, HMAC, `runtime.scm`). All Companion-side code is out of scope.
- No new mutation tools (`set_component_property`, `call_method`, `set_variable`, generic `eval`). The `AIOperation` pipeline remains the only mutation path.
- No server-side runtime access. The server never talks to the Companion; it only receives Companion-derived context as part of the request.
- No automatic injection of runtime state. The toggle is opt-in per Companion session.
- No tool that lets the AI initiate a Companion pairing, reset, or disconnect.
- No changes to the existing "Do It" feature.

## 3. Terminology

- **Companion** — the paired device (phone, emulator, or Chromebook Companion) running the App Inventor runtime. Connected through HTTP polling or WebRTC.
- **REPL pipeline** — the existing path by which the editor sends Scheme to the Companion (`/_newblocks` + HMAC, or WebRTC DataChannel) and receives results via `RetValManager` replies routed through `replmgr.js:processRetvals`.
- **Tool** — a named, structured call the LLM can issue. Existing tools: `lookup_component`, `add_component`, etc. New tools in this spec: `read_component_property`, `read_variable`, `read_recent_logs`.
- **Context module** — a class extending `ContextModule` that contributes one section to the LLM's per-request context (e.g. `ProjectModule`, `ScreenModule`, `TutorialModule`). New in this spec: `CompanionModule`.
- **Block bubble** — the warning-icon overlay on a Blockly block whose `replError` property is set. Expands on click/hover to show the runtime error text. Persistent across screen re-runs until the block is modified.
- **Error dialog** — the transient modal shown by `replmgr.js:runtimeerr()` for device errors not tied to a specific block. Dismissed by clicking "Dismiss."
- **Companion session** — the period from a successful pairing until the Companion disconnects, is reset, or the page reloads. Not the same as a browser session or a project session.

## 4. Architecture Overview

```
                     ┌──────────────────────┐
                     │   AI chat dialog     │
                     │  (existing UI)       │
                     └──────────┬───────────┘
                                │ text + operations
                     ┌──────────▼───────────┐
                     │ AIResponseOrchestrator│◄────┐ client-side
                     └──────────┬───────────┘     │ tool resolve
                                │                  │
            ┌───────────────────┴──────────┐      │
            │                              │      │
   ┌────────▼────────┐           ┌─────────▼──────┴──┐
   │ AIContextCollector│         │ CompanionBridge   │  NEW
   │ .buildRequest()   │         │ (read tools)      │
   └────────┬──────────┘         └─────────┬─────────┘
            │ companionSnapshot            │ (get-property ...)
            │ (new field)                  │
            │                              │
   ┌────────▼──────────────────────────────▼────────┐
   │           ReplMgr (existing)                   │
   │  HMAC + WebRTC + HTTP polling + processRetvals │
   └──────────────────────┬─────────────────────────┘
                          │
                          │ Scheme + HMAC
                          │
                   ┌──────▼──────┐
                   │  Companion  │  (unchanged)
                   │ process-repl-input → RetValManager
                   └─────────────┘
```

### Two paths, one toggle

**Passive context path.** On every AI request, `AIContextCollector` attaches a `companionSnapshot` JSON field to `AIAgentRequest` *if and only if* the Companion is connected and the user's share toggle is on. Server-side, a new `CompanionModule` renders that snapshot into a context message the LLM sees before the user's prompt. This gives the LLM ambient awareness: "device is connected, on Screen2, here are the last 3 errors and 10 log lines."

**Active tool path.** When the LLM issues one of three new read tools (`read_component_property`, `read_variable`, `read_recent_logs`), the client — not the server — resolves it. `AIResponseOrchestrator` inspects incoming operations; if one is a runtime-read tool call, it hands the call to a new `CompanionBridge`, which constructs a templated Scheme expression with a synthetic blockid, sends it through the existing `ReplMgr`, awaits the `RetValManager` response, and feeds the result back to the LLM via the existing `continueWithToolResults()` path — the same channel validation retries already use.

**Error-handoff path.** When the user clicks "Ask AI about this error" from the error dialog or a block's context menu, the client assembles a `contextHint` (error + stack + block YAIL + implicated component, plus a bounded snapshot from the Companion if the toggle is on) and calls the existing `BlocklyPanel.sendExplainMessage(displayText, contextHint)` — the same entry point the "Explain Block" feature already uses. No new chat plumbing.

### Why client-side tool resolution

Read-only tools that hit *static* project state (`lookup_component`, `lookup_screen`) resolve server-side through `AIToolResolver` because the server has `StorageIo`. The Companion is a *client-owned* resource: its session, HMAC key, WebRTC socket, and `ReplMgr` state all live in the browser. There is no path for the server to invoke the Companion, and giving the server one would mean exposing the HMAC to the server or proxying REPL traffic — both wrong.

So the new tools follow a different pattern: the server *defines* them (they appear in `tool_definitions.json` and the LLM can call them), but the server does *not* resolve them. Instead, the server returns them to the client as `AIOperation` objects; the orchestrator intercepts runtime-read operations before they reach the executor, resolves them through `CompanionBridge`, and feeds results back via `reportExecutionErrors()` → `continueWithToolResults()`. This reuses an existing pipeline and keeps the Companion boundary entirely client-owned.

## 5. Components

### 5.1 Shared RPC

**`AIAgentRequest.companionSnapshot`** (new field, nullable `String`).

JSON-encoded snapshot of Companion state at request time. Schema:

```json
{
  "connectionKind": "webrtc" | "http",
  "activeScreen": "Screen1",
  "logs": [
    {"level": "info", "text": "...", "timestamp": 1712345678901},
    ...
  ],
  "errors": [
    {"message": "Select list item: ...", "blockId": "...", "componentName": "Button1", "timestamp": ...},
    ...
  ]
}
```

- `null` when Companion is disconnected *or* the share toggle is off.
- Bounded: max 10 log entries, max 3 error entries. Older entries are dropped at collection time.

### 5.2 Server — Context module

**`CompanionModule`** (new, `server/aiagent/context/CompanionModule.java`).

Extends `ContextModule`. Registered in `AIContextBuilder` as a per-request context message after `ScreenModule` (position matters: the LLM reads runtime state *after* seeing the current screen's design).

`build(ContextParams)` behavior:
- If `params.getCompanionSnapshot()` is null, return an empty string (the builder skips empty sections).
- Otherwise, render a human-readable Markdown section titled `## Companion runtime state` including:
  - Connection kind + active screen.
  - Recent errors (formatted with message, implicated component, and a compact stack trace snippet).
  - Recent logs.
  - A short note: "Use `read_component_property`, `read_variable`, or `read_recent_logs` to query the device for additional live values."

### 5.3 Server — Tool definitions

Three new entries in `server/aiagent/resources/tool_definitions.json`:

```json
{
  "name": "read_component_property",
  "description": "Read the current runtime value of a component property from the connected Companion. Use this when you need to confirm what the live app actually has (e.g. what the user typed in a TextBox). Only available when a Companion is paired and the user has enabled runtime sharing.",
  "input_schema": {
    "type": "object",
    "properties": {
      "component_name": {"type": "string", "description": "The component's identifier in the current screen, e.g. Button1."},
      "property_name": {"type": "string", "description": "The property name, e.g. Text."}
    },
    "required": ["component_name", "property_name"]
  }
}
```

```json
{
  "name": "read_variable",
  "description": "Read the current runtime value of a global variable from the connected Companion. Only available when a Companion is paired and the user has enabled runtime sharing.",
  "input_schema": {
    "type": "object",
    "properties": {
      "variable_name": {"type": "string", "description": "The global variable name without the 'global' prefix."}
    },
    "required": ["variable_name"]
  }
}
```

```json
{
  "name": "read_recent_logs",
  "description": "Read the last N log entries from the connected Companion. Useful for diagnosing behavior over time. Only available when a Companion is paired and the user has enabled runtime sharing.",
  "input_schema": {
    "type": "object",
    "properties": {
      "n": {"type": "integer", "minimum": 1, "maximum": 50, "default": 20}
    }
  }
}
```

Corresponding constants added to `AIToolNames`.

### 5.4 Server — Operation type

**`AIOperation.Type.READ_RUNTIME`** (new).

A single new type covers all three tools. The payload JSON carries:

```json
{"tool": "read_component_property", "args": {...}, "toolCallId": "..."}
```

`LLMResponseParser` recognizes the three tool names, builds this operation, and attaches the tool call ID so the client can reply through `reportExecutionErrors()`.

### 5.5 Server — Tool filtering

`AIContextBuilder.buildTools()` includes the three read tools when **all** of:

- `ai.agent.features.companion-context` is `true` (server-side master switch, default `true`).
- `enforcementContext` is `STANDARD`, `EXECUTION`, or `CHILD_EXECUTION`. Excluded during `PLANNING` (the plan phase is about structure, not inspection).
- `request.companionSnapshot` is non-null. If the client reports no Companion, the tools aren't shipped — avoiding "I'll look it up" narration the LLM can't follow through on.

Available in all user-facing modes (Advisor, Screen Editor, Project Editor) — they're read-only and fit Advisor's contract.

### 5.6 Server — Mode enforcement

`ModeEnforcer` is **not** involved. These operations never reach the executor; they're intercepted client-side. Server-side parsing remains the only server concern, and that's covered by `LLMResponseParser`'s existing shape validation.

### 5.7 Client — Context collection

**`AIContextCollector.buildRequest()`** extended:

- If `AIEditorState.isCompanionShareEnabled()` and `ReplMgr` reports connection state `CONNECTED`, call a new private `buildCompanionSnapshot()` and attach the result to the request.
- Otherwise, leave `companionSnapshot` null.

**`buildCompanionSnapshot()`** collects:
- Connection kind from `ReplMgr`'s active transport.
- Active screen from the most recent `pushScreen` event tracked by a new small client-side `CompanionStateTracker`.
- Last 10 log entries from a new ring buffer populated by a hook added to `replmgr.js:processRetvals` in the `case "log"` branch.
- Last 3 error entries from a new ring buffer populated in the `case "error"` branch and the `r.status != "OK"` branch of `case "return"`.

### 5.8 Client — `CompanionBridge`

**New module** `client/.../aiagent/companion/CompanionBridge.java` + `CompanionBridgeJs.java` (JSNI shim).

Responsibilities:

1. **Scheme templating.** Hard-coded templates per tool, identifiers substituted only after regex validation. Templates:

   ```scheme
   ; read_component_property
   (process-repl-input "<blockid>"
     (get-display-representation (get-property '<component> '<property>)))

   ; read_variable
   (process-repl-input "<blockid>"
     (get-display-representation (get-var g$<variable>)))
   ```

   `read_recent_logs` never hits the Companion — it reads the client-side log ring buffer populated by `processRetvals`. Its tool-resolution path is therefore entirely synchronous (no promise map, no blockid, no transport). It's exposed as a tool (not just as context) so the LLM can pull more history than the 10-line snapshot ceiling when a user asks "what happened earlier?"

2. **Blockid namespace.** Synthetic IDs `ai-read-<uuid>`. No collision with Blockly-assigned IDs possible.

3. **Promise map.** `Map<String, PendingRead>` keyed by blockid. Each entry holds resolve/reject callbacks and a timeout handle. Timeout is **5 seconds**; on expiry, the map entry is removed and the pending call is rejected with `"Companion did not respond within 5 seconds."`

4. **Transport.** Calls `Blockly.ReplMgr.putYail(code, block, success, failure)` (`replmgr.js:325, 348`) — the existing send primitive used for "Do It" and incremental YAIL pushes. HMAC signing, chunking, and WebRTC/HTTP selection are already handled there. The bridge passes a lightweight `block`-shaped object carrying the synthetic `ai-read-<uuid>` ID so existing `putYail` bookkeeping works without modification.

5. **Budget.** Per-conversation-turn counter, enforced before send:
   - Max **10** reads per user turn.
   - Max **30** reads per 60-second sliding window.
   - Overrun → tool result: `"Runtime read budget exceeded for this turn."`

### 5.9 Client — `processRetvals` hook

Add one dispatch branch in `replmgr.js:processRetvals` `case "return":`

```javascript
if (r.blockid && r.blockid.indexOf('ai-read-') === 0) {
    top.CompanionBridge_resolvePending(r.blockid, r.status, r.value);
    break;  // don't fall through to Blockly routing
}
```

No other routes touched. Blockly never sees `ai-read-*` blockids; the bridge never sees Blockly blockids.

### 5.10 Client — `CompanionReadValidator`

**New class** `client/.../aiagent/validator/CompanionReadValidator.java`.

Validates tool calls *before* `CompanionBridge` constructs Scheme. Checks:

- `component_name` matches `^[A-Za-z_][A-Za-z0-9_]*$`.
- `property_name` matches the same regex.
- `variable_name` matches the same regex.
- Component exists on the currently-open screen (reads `YaFormEditor.getPropertiesJson()`, same source `AIContextCollector` uses).
- Component's type has a property named `property_name`, and that property is readable, per `SimpleComponentDatabase`.
- Variable exists in the current screen's YAIL globals.

Failure → structured error returned to the LLM:
- `"Button1 has no readable property 'Foo'. Readable properties: [Text, Enabled, Width, Height, ...]"` — includes the list so the LLM can self-correct.
- `"No variable named 'foo' in the current screen."`

### 5.11 Client — `AIResponseOrchestrator` integration

When the orchestrator receives an `AIAgentResponse` with `READ_RUNTIME` operations:

1. Split the batch: runtime-read ops vs. everything else.
2. For runtime-read ops, skip the executor entirely. Resolve each through `CompanionReadValidator` → `CompanionBridge`.
3. Collect results (success or error) keyed by tool call ID.
4. Call `reportExecutionErrors()` with the patched results. The server's `AIAgentEngine.reportExecutionErrors()` already knows how to hand these to `continueWithToolResults()`.
5. If the batch also had non-read ops, they flow through the normal executor path in parallel (or sequentially, to be determined — see Open Questions).

Runtime-read ops are never shown in the operation-preview UI and never require user approval. They're inspection, not mutation.

### 5.12 Client — Connection toggle UI

Add a checkbox to the Companion connection dialogs constructed by:
- `WirelessAction` (QR-code pairing)
- `EmulatorAction`
- `UsbAction`
- `ChromebookAction`

Label (i18n key `REPL_AI_SHARE_RUNTIME`): *"Share runtime data with AI Assistant (read-only: logs, errors, component values)"*.

Visibility: hidden when `AIEditorState.getMode() == OFF`.

Default: **unchecked** on every connection dialog open.

On dialog confirm (if checked): `AIEditorState.setCompanionShareEnabled(true)` on the currently-paired Companion session.

On Companion disconnect (any cause): `AIEditorState.setCompanionShareEnabled(false)`. Explicitly reset, not carried across sessions.

### 5.13 Client — Error-handoff button

Two integration points, both routing to the same handler.

**Error dialog button.** Modify `replmgr.js:runtimeerr()` to add a second button next to "Dismiss": *"Ask AI"*. Hidden when AI mode is OFF. On click:
- Dismiss the dialog.
- Call a new `top.BlocklyPanel_askAIAboutRuntimeError(message, blockid, componentName)`.

**Block context menu entry.** Register a new context menu item `appinventor_ask_ai_about_error` in `blocklyeditor.js` using the same registry pattern as `appinventor_explain_block` (`blocklyeditor.js:380`). Precondition: `block.replError != null`. Enabled when AI mode is not OFF. On click: call `BlocklyPanel_askAIAboutRuntimeError(block.replError, block.id, resolvedComponentName)`.

**Shared handler** `BlocklyPanel.askAIAboutRuntimeError(message, blockId, componentName)`:
1. Build `displayText`: `"Help me fix this runtime error on <ComponentName>.<EventName>"` when the implicated block is an event handler; otherwise `"Help me fix this runtime error."`
2. Build `contextHint`:
   ```
   Runtime error: <message>
   Block ID: <blockId>
   Block YAIL:
   <block's YAIL>
   Implicated component: <componentName>
   ```
3. If `companionShareEnabled && replState == CONNECTED`: statically scan the block's YAIL for symbols of form `(get-property 'X 'Y)`, `(get-var g$Z)`, `(lookup-in-current-form-environment 'W)`. For each unique symbol (bounded to **10 reads**), fire a `CompanionBridge` read. Each individual read still carries the bridge's normal 5s timeout. A **2s wall-clock gate** governs the *fan-out*: after 2s elapsed since the handoff click, no *new* reads are started, but reads already in flight continue to their individual 5s limit so late-arriving results can still enrich the hint. Reads that haven't resolved by the time the handoff is ready to send are simply omitted from the snapshot (best-effort, as noted below). Results are appended to `contextHint` under a `Runtime snapshot at error time:` section.
4. Call `orchestrator.sendMessageWithContext(displayText, contextHint)` — existing entry point used by "Explain Block."

Snapshot reads here count against the same per-turn budget; a snapshot that hits the budget simply stops at the cap rather than blocking the handoff.

### 5.14 Client — `AIEditorState` additions

New fields:

- `boolean companionShareEnabled` — default false.
- Getter/setter. Setter emits a `CompanionShareChanged` event so UI can react if needed.

Reset to false on:
- Companion disconnect. No existing event fires on disconnect — disconnect paths in `replmgr.js` (lines 844, 941, 1212, 1448, 1691) all call `resetYail(false)` and do not broadcast. The spec adds a new workspace change-listener event `AI.Events.CompanionDisconnect` fired alongside the existing `AI.Events.CompanionConnect` pattern (`replmgr.js:1114, 1662, 1769, 1778`). `AIEditorState` subscribes to it.
- Page unload (automatic — the browser tears down the client).
- Explicit "Reset Connection" user action (already routes through `resetYail`, so covered by the new event).

### 5.15 Feature flags

New:
- `ai.agent.features.companion-context` (server, default `true`) — master switch. Off → `CompanionModule` not registered, tool definitions not shipped, `companionSnapshot` ignored if sent.

Existing, reused:
- `ai.agent.available` — if false, none of this matters.

No second flag for the error button; it's governed by `AIEditorState.getMode() != OFF`.

## 6. Data Flow

### 6.1 Passive context flow

```
User sends message
    ↓
AIContextCollector.buildRequest()
    ↓
[ReplState.CONNECTED && companionShareEnabled?]
    ├─ yes → buildCompanionSnapshot() → AIAgentRequest.companionSnapshot = JSON
    └─ no  → companionSnapshot = null
    ↓
RPC → AIAgentServiceImpl → AIAgentEngine
    ↓
AIContextBuilder.build() + buildContextMessages()
    ↓
[companionSnapshot != null?]
    └─ yes → CompanionModule.build() renders section
    ↓
LLM sees: system prompt + project + screen + [companion runtime state] + user message
```

### 6.2 Active tool flow

```
LLM calls read_component_property({component_name: "TextBox1", property_name: "Text"})
    ↓
Server LLMResponseParser parses → AIOperation(READ_RUNTIME, {tool: "read_component_property", args: ..., toolCallId: "..."})
    ↓
AIAgentResponse returned to client
    ↓
AIResponseOrchestrator splits batch
    ↓
For runtime-read op:
    CompanionReadValidator.validate(args)
        ├─ invalid → synthesize failure result
        └─ valid   → CompanionBridge.read(args)
                        ↓
                    Identifier regex pass
                        ↓
                    Budget check
                        ↓
                    Build Scheme from template + synthetic ai-read-<uuid> blockid
                        ↓
                    Register promise in pending-map with 5s timeout
                        ↓
                    ReplMgr sends (HMAC signs, routes via WebRTC or HTTP)
                        ↓
                    Companion: process-repl-input → (get-property ...) → RetValManager
                        ↓
                    replmgr.js:processRetvals sees ai-read-* blockid → routes to CompanionBridge_resolvePending
                        ↓
                    Promise resolves with ("OK", "4") or ("NOK", "<error>")
    ↓
Orchestrator collects results
    ↓
reportExecutionErrors(results) → server continueWithToolResults(patched results + fresh context) → LLM
    ↓
LLM sees tool result: "4" (or error) → continues reasoning
```

### 6.3 Error handoff flow

```
Companion raises runtime error
    ↓
RetValManager sends {type: "error" | "return" with status "NOK", blockid, value}
    ↓
replmgr.js:processRetvals
    ├─ blockid matched → block.replError = "Error from Companion: ..." (bubble)
    └─ no blockid match → runtimeerr() modal dialog
    ↓
User clicks "Ask AI" (dialog) or "Ask AI about this error" (block context menu)
    ↓
BlocklyPanel.askAIAboutRuntimeError(message, blockId, componentName)
    ↓
Build displayText + contextHint
    ↓
[companionShareEnabled?]
    └─ yes → static-scan block YAIL for symbol refs → bounded snapshot via CompanionBridge (≤10 reads, ≤2s)
    ↓
orchestrator.sendMessageWithContext(displayText, contextHint)
    ↓
AI chat dialog opens (if closed), user-visible message appears, hidden context goes to LLM only
    ↓
LLM responds with diagnosis + possibly AIOperation fixes → normal preview/apply flow
```

## 7. Error Handling

| Situation | Behavior |
|---|---|
| Companion disconnects mid-request | Pending reads time out at 5s → tool result `"Companion disconnected during read."`; `companionShareEnabled` resets to false; next request won't ship `companionSnapshot`. |
| Tool call with malformed identifier (e.g. `"Button1; (set-property ...)"`) | `CompanionReadValidator` rejects before Scheme construction. Tool result: `"Invalid component name."` — LLM sees its mistake via normal tool-result channel. |
| Tool call for nonexistent component/property/variable | Validator returns a structured error listing valid options from the current screen. |
| Scheme evaluation fails on device (e.g. race where component was deleted) | `process-repl-input` catches the exception and returns `("NOK" "<message>")`. Bridge resolves the promise with the error. Tool result carries the message. |
| Device on different screen than editor | `get-property` throws because `*this-form*` is the device's active form. Bridge surfaces the error as-is — LLM learns the device is on Screen2 and can tell the user. |
| Budget exceeded | Bridge rejects immediately without sending. Tool result: `"Runtime read budget exceeded for this turn. Resume in next message."` |
| Error handoff with AI in the middle of a response | `sendMessageWithContext` uses existing "Explain Block" semantics. If the orchestrator is busy, the message queues or displays a "busy" state — whatever "Explain Block" does today. No new behavior required. |
| Error handoff when snapshot reads all fail | Error button still works; `contextHint` omits the snapshot section. No error is surfaced to the user — snapshot is best-effort enrichment. |

## 8. Security

### 8.1 Threat model

An LLM response is untrusted input, even from a trusted provider. Treat every tool call as if it were crafted by an attacker trying to mutate the device or exfiltrate data.

### 8.2 Layered defenses

1. **No string-typed Scheme argument exists in any tool schema.** All tool args are named identifiers with validated types. The LLM cannot pass Scheme code anywhere.
2. **Identifier regex.** `^[A-Za-z_][A-Za-z0-9_]*$` on every name, client-side, before Scheme construction. Blocks quotes, parens, whitespace, escapes.
3. **Existence + readability validation.** Even a well-formed identifier is rejected if the component/property/variable doesn't exist in the current screen or if the property is write-only per `simple_components.json`.
4. **Hard-coded Scheme templates.** Function names (`get-property`, `get-var`) are constants in client code. No dynamic function-name selection from LLM input.
5. **No method-call or setter tools.** The API surface itself excludes mutation. An LLM attempting `(call-component-method ...)` has no tool to invoke.
6. **HMAC is client-owned.** The LLM never sees the HMAC key. The bridge constructs Scheme and `ReplMgr` signs — same trust boundary that already exists for "Do It."
7. **Blockid routing invariant.** `ai-read-*` blockids resolve only through `CompanionBridge_resolvePending`; other blockids never reach the bridge. Prevents an attacker from forging results for real blocks or reading results meant for Blockly.
8. **Budget.** 10/turn, 30/minute cap defeats runaway enumeration.
9. **User toggle.** With `companionShareEnabled == false`, tool definitions aren't shipped and the LLM cannot invoke them. The decision to expose runtime is an explicit, per-session user action.
10. **Feature flag.** Server operators can disable the whole surface via `ai.agent.features.companion-context`.

### 8.3 Data privacy

Runtime state on a user's device can be personal (TextBox contents, TinyDB entries, location, sensor readings). The toggle default is off to match the principle of least surprise: pairing a Companion never silently exposes runtime data to the AI. When the toggle is on, the context that flows to the server is bounded (3 errors, 10 logs, plus on-demand specific reads) and is documented in the toggle's label text.

### 8.4 What's still trusted

- The Companion itself. If an attacker has replaced `runtime.scm` on the device, all bets are off — but that's no different from today.
- The `ReplMgr` transport layer (HMAC signing, WebRTC session). This spec does not harden it; it only uses it.
- The server-side LLM response parser. If the parser mis-parses a tool call, downstream validation still catches it (identifier regex, existence checks), so the failure mode is a rejected tool, not a rogue Scheme send.

## 9. Testing

### 9.1 Server-side unit tests (Java)

`CompanionModuleTest`:
- Empty snapshot → empty context section.
- Populated snapshot → correctly formatted Markdown with all fields.
- Truncation at declared bounds (10 logs, 3 errors).

`AIContextBuilderTest` extensions:
- `buildTools()` includes/excludes read tools based on feature flag, snapshot presence, enforcement context.
- Tools appear in Advisor mode.

`LLMResponseParserTest` extensions:
- `read_component_property` / `read_variable` / `read_recent_logs` parsed into `READ_RUNTIME` operations.
- Missing required fields produce the existing provided-keys-hint error.

### 9.2 Client-side unit tests (GWT)

`CompanionReadValidatorTest`:
- Identifier regex positive and negative cases.
- Component existence lookup using a fixture `YaFormEditor`.
- Property readability lookup using a fixture `SimpleComponentDatabase`.
- Error messages include the list of valid alternatives.

`CompanionBridgeTest`:
- Uses a fake `ReplTransport` seam that records sent Scheme and replays canned responses.
- Happy path: tool call → correct Scheme constructed → canned response → promise resolves with value.
- Timeout: no response → promise rejects with timeout message.
- Budget: 11th call in a turn rejects immediately without touching transport.
- Error response from Companion: `("NOK" ...)` surfaced as tool error.
- Malformed `r.value` handled.

`AIResponseOrchestratorTest` extensions:
- Mixed batch (read + write ops) splits correctly.
- Read-op results feed `reportExecutionErrors` with the right tool call IDs.

### 9.3 Manual verification

Documented in the plan:
1. Connect Companion, toggle share on. Ask AI "what is in TextBox1?" — verify tool call fires, result shows in chat follow-up.
2. Connect Companion, toggle share off. Verify tools are absent from the agent's options (ask "can you read TextBox1 for me?" — agent should say it cannot).
3. Trigger a runtime error on the device. Click "Ask AI" from the dialog — verify chat opens with a reasonable context.
4. Trigger a block-specific runtime error. Right-click the warning-marked block → "Ask AI about this error" — verify chat opens with block YAIL + error in contextHint.
5. Disconnect Companion mid-session. Verify `companionShareEnabled` resets and next request ships no snapshot.

### 9.4 Not tested in CI

Live-device interaction is excluded from CI. The `ReplTransport` seam is the contract: if the real `ReplMgr` honors it, behavior matches tests. A manual test pass is required before release.

## 10. File Structure

### New files

- `appengine/src/com/google/appinventor/server/aiagent/context/CompanionModule.java`
- `appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/companion/CompanionBridge.java`
- `appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/companion/CompanionStateTracker.java`
- `appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/validator/CompanionReadValidator.java`
- `appengine/tests/com/google/appinventor/server/aiagent/context/CompanionModuleTest.java`
- `appengine/tests/com/google/appinventor/client/editor/youngandroid/aiagent/companion/CompanionBridgeTest.java`
- `appengine/tests/com/google/appinventor/client/editor/youngandroid/aiagent/validator/CompanionReadValidatorTest.java`

### Modified files

- `appengine/src/com/google/appinventor/shared/rpc/aiagent/AIAgentRequest.java` — add `companionSnapshot` field.
- `appengine/src/com/google/appinventor/shared/rpc/aiagent/AIOperation.java` — add `READ_RUNTIME` enum value.
- `appengine/src/com/google/appinventor/server/aiagent/AIContextBuilder.java` — register `CompanionModule`, extend `buildTools()`.
- `appengine/src/com/google/appinventor/server/aiagent/AIToolNames.java` — add three constants.
- `appengine/src/com/google/appinventor/server/aiagent/LLMResponseParser.java` — parse the three new tools.
- `appengine/src/com/google/appinventor/server/aiagent/resources/tool_definitions.json` — three new entries.
- `appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/AIContextCollector.java` — build snapshot.
- `appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/AIEditorState.java` — `companionShareEnabled` + reset hook.
- `appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/AIResponseOrchestrator.java` — split batch, route read ops.
- `appengine/src/com/google/appinventor/client/editor/blocks/BlocklyPanel.java` — `askAIAboutRuntimeError` handler + JSNI exports.
- `appengine/src/com/google/appinventor/client/editor/youngandroid/actions/WirelessAction.java` — share toggle checkbox.
- `appengine/src/com/google/appinventor/client/editor/youngandroid/actions/EmulatorAction.java` — same.
- `appengine/src/com/google/appinventor/client/editor/youngandroid/actions/UsbAction.java` — same.
- `appengine/src/com/google/appinventor/client/editor/youngandroid/actions/ChromebookAction.java` — same.
- `blocklyeditor/src/replmgr.js` — `ai-read-*` dispatch branch, "Ask AI" button in `runtimeerr`, log/error ring buffers.
- `blocklyeditor/src/blocklyeditor.js` — `appinventor_ask_ai_about_error` context menu registration.
- `blocklyeditor/src/msg/ai_blockly/messages.json` — new i18n keys (English only; non-English locale files are left untouched and fall back to English automatically via the i18n pipeline).
- `appengine/war/WEB-INF/appengine-web.xml` — `ai.agent.features.companion-context` flag.
- `CONTRIBUTING_AI.md` — document the new module, tools, toggle, and error-button surfaces.

## 11. Open Questions

1. **Mixed batches.** When the LLM returns a batch containing both runtime reads and write ops (e.g. "read TextBox1 then add a Label"): should reads resolve first, or should the entire batch be considered read-only-deferred? Recommendation: resolve reads first, feed results back via `continueWithToolResults()` so the LLM gets the values, then let the LLM decide whether the writes still apply on the next turn. This matches how the LLM would reason if it had access to a real REPL. Confirm during planning.

2. **Snapshot at error time — "other screens" case.** If the failing block references a component on a screen the device isn't currently displaying (unusual but possible if the block ran before a screen change), `get-property` will fail. The snapshot phase should tolerate per-symbol failures silently. Already captured in Section 5.13.

3. **What counts as "active screen"?** The device's `*this-form*`, known to the editor via `pushScreen`/`popScreen` events. If no `pushScreen` has fired since connection, default to the screen the user is editing. Edge case to validate during implementation.

4. **Locale coverage for error button.** New strings land in English `messages.json` only. The i18n pipeline falls back to English when a key is absent from a locale file, so non-English translations are left to a separate localization pass and are not a blocker.

## 12. Success Criteria

- A user can pair a Companion, tick the share toggle, type a runtime value into their app, and have the AI agent correctly report that value when asked.
- A user can trigger a runtime error, click "Ask AI about this error," and get a useful diagnosis from the agent. With the share toggle on, the diagnosis should cite the actual runtime values involved.
- With the share toggle off, the AI cannot read runtime state via tools (tools not shipped) and no `companionSnapshot` leaves the client.
- With AI mode off, no new UI elements appear.
- A malformed tool call (bad identifier, nonexistent component, write-only property) produces a structured tool-result error that the LLM can self-correct against — and never reaches the Companion.
- Tests pass in CI without a live Companion.
