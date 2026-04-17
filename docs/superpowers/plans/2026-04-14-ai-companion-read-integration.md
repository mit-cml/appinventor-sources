# AI Companion Read Integration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let the AI Agent read runtime state from a paired Companion (component properties, variables, logs) through structured tool calls, and let the user hand runtime errors to the agent via a button — all gated behind a per-session user toggle, with zero new mutation surface.

**Architecture:** New `CompanionModule` context + three read tools that the server defines but the client resolves. Client-side `CompanionBridge` constructs hard-coded Scheme templates, sends through existing `Blockly.ReplMgr.putYail` with synthetic `ai-read-<uuid>` blockids, and routes results back to the LLM via `continueWithToolResults()`. New "Ask AI about this error" surfaces (dialog button + block context menu) reuse the existing `BlocklyPanel.sendExplainMessage` path. A session-scoped toggle in Companion connect dialogs gates all runtime visibility.

**Tech Stack:** Java (GWT client + server), JavaScript (Blockly / replmgr), JUnit 3 via `junit.framework.TestCase`, Ant build.

**Spec:** [`docs/superpowers/specs/2026-04-14-ai-companion-read-integration-design.md`](../specs/2026-04-14-ai-companion-read-integration-design.md)

**Reference:** [`CONTRIBUTING_AI.md`](../../../CONTRIBUTING_AI.md)

---

## Before Starting

**Build commands** (run from `appinventor/`):
- `ant -f appengine/build.xml AiSharedLib` — shared DTOs
- `ant -f appengine/build.xml AiServerLib` — server + resources
- `ant -f appengine/build.xml AiClientLib` — client
- `ant -f appengine/build.xml AiServerLibTests` — server tests
- `ant -f appengine/build.xml tests` — all tests

**Conventions:**
- Copyright header on every new Java file (see existing files).
- JUnit 3: extend `junit.framework.TestCase`, method names start with `test`, no annotations.
- 2-space indentation in Java. Follow existing file style.
- Commit after each task, not mid-task. Use conventional-commit prefixes: `feat:`, `test:`, `chore:`, `docs:`, `fix:`.

**One-time worktree setup.** This plan expects a worktree off `ai-agent`. If not already in one, see `superpowers:using-git-worktrees`.

---

## Phase 1 — Shared RPC Foundations

These tasks add DTO fields everyone else depends on. Do these first; no tests needed — GWT serialization validates shape at build time.

### Task 1: Add `companionSnapshot` field to `AIAgentRequest`

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/shared/rpc/aiagent/AIAgentRequest.java`

- [ ] **Step 1: Add the field and accessors**

In the field declarations block (after `private boolean executionPhase;`), add:

```java
/** JSON snapshot of live Companion state, or null when not shared. */
private String companionSnapshot;
```

At the bottom of the class (after existing accessors), add:

```java
public String getCompanionSnapshot() {
  return companionSnapshot;
}

public void setCompanionSnapshot(String companionSnapshot) {
  this.companionSnapshot = companionSnapshot;
}
```

- [ ] **Step 2: Build shared lib to verify GWT serialization**

Run: `cd appinventor && ant -f appengine/build.xml AiSharedLib`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/shared/rpc/aiagent/AIAgentRequest.java
git commit -m "feat(ai-agent): add companionSnapshot field to AIAgentRequest"
```

---

### Task 2: Add `READ_RUNTIME` operation type and tool-name constants

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/shared/rpc/aiagent/AIOperation.java`
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/AIToolNames.java`

- [ ] **Step 1: Add enum value**

In `AIOperation.java`, find the `Type` enum and add after `PROPOSE_PLAN`:

```java
// Runtime reads resolved client-side via Companion; never reaches the executor.
READ_RUNTIME
```

- [ ] **Step 2: Add tool-name constants**

In `AIToolNames.java`, add a new section at the bottom (before the closing brace):

```java
// ---------- Companion runtime reads (client-resolved) ----------

public static final String READ_COMPONENT_PROPERTY = "read_component_property";
public static final String READ_VARIABLE = "read_variable";
public static final String READ_RECENT_LOGS = "read_recent_logs";
```

- [ ] **Step 3: Build shared + server libs**

Run: `cd appinventor && ant -f appengine/build.xml AiSharedLib AiServerLib`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/shared/rpc/aiagent/AIOperation.java \
        appinventor/appengine/src/com/google/appinventor/server/aiagent/AIToolNames.java
git commit -m "feat(ai-agent): add READ_RUNTIME op type and companion read tool names"
```

---

## Phase 2 — Server

### Task 3: Add tool definitions JSON and parse them

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/resources/tool_definitions.json`
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/LLMResponseParser.java`
- Test: `appinventor/appengine/tests/com/google/appinventor/server/aiagent/LLMResponseParserCompanionTest.java` (new)

- [ ] **Step 1: Write failing tests for parsing the three new tools**

Create `LLMResponseParserCompanionTest.java` (mirror the style of existing parser tests in the same directory). Minimum cases:

- `testReadComponentPropertyParsesToReadRuntime` — a raw tool call `{"name":"read_component_property","arguments":"{\"component_name\":\"Button1\",\"property_name\":\"Text\"}"}` parses into one `AIOperation` with `Type.READ_RUNTIME` whose JSON payload contains `tool=read_component_property`, `args.component_name=Button1`, `args.property_name=Text`, and a non-empty `toolCallId`.
- `testReadVariableParsesToReadRuntime` — same shape with `read_variable` and `variable_name=score`.
- `testReadRecentLogsParsesToReadRuntime` — `read_recent_logs` with `n=15`.
- `testReadComponentPropertyMissingArgReportsError` — missing `property_name` produces an error in `response.getErrors()` containing the required-field message, and no operation in the list.

Follow the existing test's setup pattern for constructing raw tool calls and invoking the parser.

- [ ] **Step 2: Run test to verify failures**

Run: `cd appinventor && ant -f appengine/build.xml AiServerLibTests -Dtest.class=com.google.appinventor.server.aiagent.LLMResponseParserCompanionTest`
Expected: all four tests FAIL because parser doesn't recognize the tool names yet.

- [ ] **Step 3: Add tool definitions to JSON**

Append three entries to `tool_definitions.json`, matching the JSON schemas in spec §5.3. Ensure the JSON remains valid (commas between entries).

- [ ] **Step 4: Extend `LLMResponseParser.parseToolCalls`**

Add a case for each of the three tool names. For `read_component_property` / `read_variable`: validate required string fields present, build an `AIOperation(Type.READ_RUNTIME, payload)` where payload is a JSON object `{"tool": "<name>", "args": {...}, "toolCallId": "<id>"}`. For `read_recent_logs`: `n` is optional (default 20) and coerced to int, clamped to 1–50.

Follow the existing handler pattern for `lookup_component` / `lookup_screen` as a reference — same required-field reporting and key-hint error message.

- [ ] **Step 5: Run test to verify pass**

Run same command as Step 2.
Expected: all four tests PASS.

- [ ] **Step 6: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/server/aiagent/resources/tool_definitions.json \
        appinventor/appengine/src/com/google/appinventor/server/aiagent/LLMResponseParser.java \
        appinventor/appengine/tests/com/google/appinventor/server/aiagent/LLMResponseParserCompanionTest.java
git commit -m "feat(ai-agent): parse companion read tools into READ_RUNTIME operations"
```

---

### Task 4: Create `CompanionModule` context class

**Files:**
- Create: `appinventor/appengine/src/com/google/appinventor/server/aiagent/context/CompanionModule.java`
- Test: `appinventor/appengine/tests/com/google/appinventor/server/aiagent/context/CompanionModuleTest.java` (new)

- [ ] **Step 1: Write failing tests**

```java
// CompanionModuleTest.java — excerpt
public class CompanionModuleTest extends TestCase {

  private CompanionModule module;

  @Override
  public void setUp() {
    module = new CompanionModule();
  }

  public void testNullSnapshotReturnsNull() {
    ContextParams p = new ContextParams();
    assertNull(module.build(p));
  }

  public void testEmptySnapshotReturnsNull() {
    ContextParams p = new ContextParams();
    p.setCompanionSnapshot("");
    assertNull(module.build(p));
  }

  public void testPopulatedSnapshotIncludesAllSections() {
    ContextParams p = new ContextParams();
    p.setCompanionSnapshot("{"
        + "\"connectionKind\":\"webrtc\","
        + "\"activeScreen\":\"Screen1\","
        + "\"logs\":[{\"level\":\"info\",\"text\":\"hello\",\"timestamp\":1}],"
        + "\"errors\":[{\"message\":\"boom\",\"blockId\":\"abc\",\"componentName\":\"Button1\",\"timestamp\":2}]"
        + "}");
    String out = module.build(p);
    assertNotNull(out);
    assertTrue(out.contains("Companion runtime state"));
    assertTrue(out.contains("webrtc"));
    assertTrue(out.contains("Screen1"));
    assertTrue(out.contains("hello"));
    assertTrue(out.contains("Button1"));
    assertTrue(out.contains("boom"));
  }

  public void testTruncationAtDeclaredBounds() {
    // 20 logs in snapshot → only 10 in output
    // 5 errors in snapshot → only 3 in output
    // (Build a snapshot programmatically, count occurrences in output.)
  }
}
```

- [ ] **Step 2: Add `setCompanionSnapshot` / `getCompanionSnapshot` to `ContextParams`**

Modify `ContextParams.java` alongside the test. Field + getter + setter. Mirror the style of `setProjectSnapshot`.

- [ ] **Step 3: Run test to verify failures**

Run: `ant -f appengine/build.xml AiServerLibTests -Dtest.class=com.google.appinventor.server.aiagent.context.CompanionModuleTest`
Expected: tests FAIL (class doesn't exist / returns null).

- [ ] **Step 4: Implement `CompanionModule`**

Structure mirrors `TutorialModule`: parse JSON, return null if absent/empty, otherwise render Markdown starting with:

```
[Companion Runtime State]

## Companion runtime state

**Connection:** <kind> — active screen: **<screen>**

### Recent errors
- ...

### Recent logs
- ...

*Use `read_component_property`, `read_variable`, or `read_recent_logs` to query live values.*
```

Bound log output to 10 entries, error output to 3 entries (defensive — client already bounds, but don't trust input).

- [ ] **Step 5: Run test to verify pass**

Run same command.
Expected: tests PASS.

- [ ] **Step 6: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/server/aiagent/context/CompanionModule.java \
        appinventor/appengine/src/com/google/appinventor/server/aiagent/context/ContextParams.java \
        appinventor/appengine/tests/com/google/appinventor/server/aiagent/context/CompanionModuleTest.java
git commit -m "feat(ai-agent): add CompanionModule renders runtime state for LLM context"
```

---

### Task 5: Wire `CompanionModule` into `AIContextBuilder` + filter tools

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/AIContextBuilder.java`
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/AIAgentEngine.java` (populate `ContextParams.companionSnapshot` from request)

- [ ] **Step 1: Wire `CompanionModule` into `buildContextMessages`**

In `AIContextBuilder.java`, add `CompanionModule` to the per-request context modules list, positioned after `ScreenModule` and before `TutorialModule` (nested runtime info belongs after screen tree, before tutorial pedagogy).

- [ ] **Step 2: Extend `buildTools` to include the three new tools**

Inclusion criteria (see spec §5.5):
- Feature flag `ai.agent.features.companion-context` is enabled (default true — use `Flag.createFlag("ai.agent.features.companion-context", true)` pattern found elsewhere in server).
- `EnforcementContext` is `STANDARD`, `EXECUTION`, or `CHILD_EXECUTION` (not `PLANNING`).
- `params.getCompanionSnapshot() != null` — only ship tools when the client is actively sharing.

Add a unit test in `AIContextBuilderTest` (existing file, if present — otherwise create a minimal one) verifying tool inclusion/exclusion across each criterion.

- [ ] **Step 3: Populate `ContextParams.companionSnapshot` in engine**

In `AIAgentEngine` wherever `ContextParams` is constructed (search for `new ContextParams(` or `ContextParams p = `), copy `request.getCompanionSnapshot()` onto it.

- [ ] **Step 4: Build + run all server tests**

Run: `ant -f appengine/build.xml AiServerLibTests`
Expected: all PASS.

- [ ] **Step 5: Commit**

```bash
# Stage the test file only if it was actually created/touched in Step 2.
git add appinventor/appengine/src/com/google/appinventor/server/aiagent/AIContextBuilder.java \
        appinventor/appengine/src/com/google/appinventor/server/aiagent/AIAgentEngine.java
git status  # confirm any AIContextBuilderTest.java addition, then git add it if present
git commit -m "feat(ai-agent): register CompanionModule and filter companion tools"
```

---

## Phase 3 — Client Foundations

### Task 6: Add `AI.Events.CompanionDisconnect` event

**Files:**
- Modify: `appinventor/blocklyeditor/src/replmgr.js`
- Modify: wherever `AI.Events.CompanionConnect` is defined (search: `grep -rn "CompanionConnect" appinventor/blocklyeditor/src/`)

- [ ] **Step 1: Locate and mirror the CompanionConnect event definition**

Search for the class definition of `AI.Events.CompanionConnect`. Add a parallel `AI.Events.CompanionDisconnect` class in the same file with identical structure (just a different name).

- [ ] **Step 2: Fire the event from all disconnect paths in `replmgr.js`**

At each `resetYail(false)` call site that represents a user-visible disconnect (lines approximately 844, 941, 1212, 1448, 1691 — re-verify with `grep -n "resetYail(false)" replmgr.js`), immediately after the `resetYail` call, add:

```javascript
Blockly.common.getMainWorkspace().fireChangeListener(new AI.Events.CompanionDisconnect());
```

**Exception:** skip the `resetYail(true)` at line 1076 — that's a partial reset for chunked-error recovery, not a user-visible disconnect.

- [ ] **Step 3: Manual sanity check**

Run `ant -f appengine/build.xml AiClientLib` — expect BUILD SUCCESSFUL. No unit tests for this file in the existing codebase; event firing is verified by Task 8's subscriber.

- [ ] **Step 4: Commit**

```bash
git add appinventor/blocklyeditor/src/replmgr.js  # plus event-class file
git commit -m "feat(companion): fire CompanionDisconnect event on reset paths"
```

---

### Task 7: Add log + error + screen ring buffers in `replmgr.js`

**Files:**
- Modify: `appinventor/blocklyeditor/src/replmgr.js`

- [ ] **Step 1: Add buffer state on `Blockly.ReplMgr`**

Near the top of the file alongside other `ReplMgr` fields, add:

```javascript
// Bounded ring buffers populated by processRetvals — read by AIContextCollector.
Blockly.ReplMgr.aiLogBuffer = [];         // entries: {level, text, timestamp}
Blockly.ReplMgr.aiErrorBuffer = [];       // entries: {message, blockId, componentName, timestamp}
Blockly.ReplMgr.aiActiveScreen = null;    // last known device-side screen
Blockly.ReplMgr.AI_LOG_CAP = 10;
Blockly.ReplMgr.AI_ERROR_CAP = 3;
```

On `CompanionDisconnect` firing from Task 6, also reset these to empty/null. (Add a `resetAIBuffers()` helper called from the disconnect sites.)

- [ ] **Step 2: Populate from `processRetvals`**

In `Blockly.ReplMgr.processRetvals` (line ~1032):
- In `case "log":`, push `{level: r.level, text: r.item, timestamp: Date.now()}` and trim to `AI_LOG_CAP`.
- In `case "error":`, push `{message: r.value, blockId: null, componentName: null, timestamp: Date.now()}` and trim to `AI_ERROR_CAP`.
- In `case "return":` when `r.status != "OK"` and `r.blockid` maps to a real block, additionally push an error entry including `blockId=r.blockid` and `componentName` resolved from the block's mutation attributes (`block.getFieldValue("COMPONENT_SELECTOR")` or similar — check how Explain Block resolves it). If the block is not yet present in the workspace (late arrival) or the mutation attr lookup returns nothing, leave `componentName = null` and still push the entry.
- In `case "pushScreen":`, set `Blockly.ReplMgr.aiActiveScreen = r.screen`.

- [ ] **Step 3: Manual verification**

No direct test; behavior verified end-to-end in Task 13 (AIContextCollector test) and manual Companion session.

- [ ] **Step 4: Commit**

```bash
git add appinventor/blocklyeditor/src/replmgr.js
git commit -m "feat(companion): track logs, errors, and active screen in ring buffers"
```

---

### Task 8: Add `companionShareEnabled` field to `AIEditorState`

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/AIEditorState.java`
- Modify: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/AIEditorStateTest.java` if it exists, else skip test for this task

- [ ] **Step 1: Add field + accessors**

```java
// Per-Companion-session flag controlling runtime visibility to the AI agent.
// Set via the connect-dialog toggle; reset on CompanionDisconnect.
private static boolean companionShareEnabled = false;

public static boolean isCompanionShareEnabled() {
  return companionShareEnabled;
}

public static void setCompanionShareEnabled(boolean enabled) {
  companionShareEnabled = enabled;
}
```

- [ ] **Step 2: Subscribe to `CompanionDisconnect` to auto-reset**

In `AIEditorState`'s static init (or the place that wires up other Blockly listeners — if none exists, add a `registerListeners()` called from `Ode` init), register a workspace change listener that, on `AI.Events.CompanionDisconnect`, calls `setCompanionShareEnabled(false)`.

Use JSNI for the Blockly event registration, mirroring how `CompanionConnect` is already consumed elsewhere (search `CompanionConnect` in `appinventor/appengine/src/com/google/appinventor/client/`).

- [ ] **Step 3: Build client lib**

Run: `ant -f appengine/build.xml AiClientLib`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/AIEditorState.java
git commit -m "feat(ai-agent): add companionShareEnabled flag resetting on disconnect"
```

---

### Task 9: Add `ai-read-*` dispatch branch in `processRetvals`

**Files:**
- Modify: `appinventor/blocklyeditor/src/replmgr.js`

- [ ] **Step 1: Add the dispatch branch**

At the top of the `case "return":` block in `processRetvals` (line ~1058), before any other logic that uses `r.blockid`, insert:

```javascript
if (r.blockid && r.blockid.indexOf('ai-read-') === 0) {
  if (top.CompanionBridge_resolvePending) {
    top.CompanionBridge_resolvePending(r.blockid, r.status, r.value);
  }
  break;
}
```

The `if (top.CompanionBridge_resolvePending)` guard lets this commit ship safely before Task 11 lands.

- [ ] **Step 2: Build client lib**

Run: `ant -f appengine/build.xml AiClientLib`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add appinventor/blocklyeditor/src/replmgr.js
git commit -m "feat(companion): route ai-read-* blockids to CompanionBridge"
```

---

## Phase 4 — Client Bridge

### Task 10: `CompanionReadValidator`

**Files:**
- Create: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/validator/CompanionReadValidator.java`
- Test: `appinventor/appengine/tests/com/google/appinventor/client/editor/youngandroid/aiagent/validator/CompanionReadValidatorTest.java` (new)

- [ ] **Step 1: Write failing tests**

Cover:
- `testValidIdentifierPasses` — `Button1` / `Text` passes.
- `testInvalidIdentifierRejected` — names with parens, quotes, spaces, semicolons each rejected.
- `testNonexistentComponentRejected` — component not on current screen → error message listing actual component names.
- `testWriteOnlyPropertyRejected` — (pick a write-only property from `SimpleComponentDatabase` — investigate during impl; if none exist, drop this case and note in PR).
- `testNonexistentVariableRejected` — variable not in YAIL globals → error listing actual globals.
- `testBudgetNotInValidator` — budget enforcement is in the bridge, not the validator; validator is stateless.

Pattern after `DesignerOperationValidator` tests.

- [ ] **Step 2: Run tests to verify failures**

Run: `ant -f appengine/build.xml tests -Dtest.class=...CompanionReadValidatorTest`
Expected: all FAIL.

- [ ] **Step 3: Implement**

```java
public final class CompanionReadValidator {

  private static final Pattern IDENTIFIER = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]*$");

  public static class Result {
    public final boolean ok;
    public final String error; // non-null when !ok
    // ...
  }

  public static Result validateReadComponentProperty(
      String componentName, String propertyName,
      YaFormEditor formEditor) {
    // 1. Identifier regex on both names.
    // 2. Component exists on current screen.
    // 3. Property exists on component's type in SimpleComponentDatabase.
    // 4. Property is readable.
  }

  public static Result validateReadVariable(String variableName, String blocksYail) {
    // 1. Identifier regex.
    // 2. (def g$<name> ...) exists in YAIL.
  }
}
```

- [ ] **Step 4: Run tests to verify pass**

Expected: all PASS.

- [ ] **Step 5: Commit**

```bash
git add ...CompanionReadValidator.java ...CompanionReadValidatorTest.java
git commit -m "feat(ai-agent): add CompanionReadValidator for runtime read tools"
```

---

### Task 11: `CompanionBridge`

**Files:**
- Create: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/companion/CompanionBridge.java`
- Test: `appinventor/appengine/tests/com/google/appinventor/client/editor/youngandroid/aiagent/companion/CompanionBridgeTest.java` (new)

- [ ] **Step 1: Write failing tests**

Design for testability: the bridge takes a `ReplTransport` seam. Production uses a JSNI impl calling `Blockly.ReplMgr.putYail`; tests inject a fake.

```java
interface ReplTransport {
  void send(String schemeCode, String syntheticBlockId);
}

public class CompanionBridgeTest extends TestCase {
  private RecordingTransport transport;
  private CompanionBridge bridge;

  public void setUp() {
    transport = new RecordingTransport();
    bridge = new CompanionBridge(transport);
  }

  public void testReadComponentPropertyConstructsCorrectScheme() {
    bridge.readComponentProperty("Button1", "Text", mockCallback());
    assertEquals(1, transport.sent.size());
    String scheme = transport.sent.get(0).code;
    assertTrue(scheme.contains("(get-property 'Button1 'Text)"));
    assertTrue(scheme.contains("(get-display-representation"));
    assertTrue(scheme.contains("process-repl-input"));
    assertTrue(transport.sent.get(0).blockId.startsWith("ai-read-"));
  }

  public void testResolveWithOkReturnsValue() { ... }
  public void testResolveWithNokReturnsError() { ... }
  public void testTimeoutRejects() { ... }           // requires injectable clock
  public void testBudgetPerTurnEnforced() { ... }    // 11th call rejected
  public void testBudgetSlidingWindowEnforced() { ... }
  public void testUnknownBlockIdIgnored() { ... }    // resolvePending with unknown id is a no-op
  public void testReadVariableConstructsCorrectScheme() { ... }
}
```

- [ ] **Step 2: Run tests to verify failures**

Expected: all FAIL.

- [ ] **Step 3: Implement**

Core structure:

```java
public class CompanionBridge {

  private static final int PER_TURN_CAP = 10;
  private static final int PER_MINUTE_CAP = 30;
  private static final int TIMEOUT_MS = 5000;

  private final ReplTransport transport;
  private final Clock clock;  // injectable, default = real
  private final Map<String, PendingRead> pending = new HashMap<>();
  private int turnCount = 0;
  private final Deque<Long> windowTimestamps = new ArrayDeque<>();

  public void readComponentProperty(String c, String p, Callback<String> cb) {
    if (!checkBudget(cb)) return;
    String blockId = "ai-read-" + generateUuid();
    String scheme = String.format(
        "(process-repl-input \"%s\" (get-display-representation (get-property '%s '%s)))",
        blockId, c, p);
    register(blockId, cb);
    transport.send(scheme, blockId);
  }

  public void readVariable(String name, Callback<String> cb) {
    // Template: (process-repl-input "<id>" (get-display-representation (get-var g$<name>)))
  }

  public void resolvePending(String blockId, String status, String value) {
    PendingRead r = pending.remove(blockId);
    if (r == null) return;  // unknown id (timeout already fired or never registered)
    r.cancelTimeout();
    if ("OK".equals(status)) r.callback.onSuccess(value);
    else r.callback.onFailure(value);
  }

  public void resetTurnBudget() {
    turnCount = 0;
  }
  // ...
}
```

Export `CompanionBridge_resolvePending(blockId, status, value)` as a `$wnd.top.*` JSNI export so `replmgr.js`'s Task-9 branch finds it.

**Singleton discipline:** production code uses exactly one `CompanionBridge` instance, owned by `AIResponseOrchestrator` (or an `AICompanionModule` holder next to it). The `$wnd.top.*` export must resolve to *that* instance, not a freshly constructed one. Tests instantiate their own bridge with a fake transport — they do not touch the global export. Confirm with an assertion that `BlocklyPanel`'s JSNI export routes through the orchestrator-owned bridge.

Use the existing `AIResponseOrchestrator` call-site to trigger `resetTurnBudget()` at the start of each new user turn.

- [ ] **Step 4: Run tests to verify pass**

Expected: all PASS.

- [ ] **Step 5: Commit**

```bash
git add ...CompanionBridge.java ...CompanionBridgeTest.java
git commit -m "feat(ai-agent): CompanionBridge resolves runtime reads via REPL pipeline"
```

---

### Task 12: Intercept `READ_RUNTIME` ops in `AIResponseOrchestrator`

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/AIResponseOrchestrator.java`
- Test: extend existing `AIResponseOrchestratorTest.java` (or create if missing)

- [ ] **Step 1: Write failing test**

`testMixedBatchSplitsReadAndWriteOps` — given a batch of `[READ_RUNTIME(read_component_property, Button1, Text), ADD_COMPONENT(Label1)]`, the read op is routed to `CompanionBridge` (fake) and the write op reaches the executor (fake). Verify:
- Read is NOT shown in the operation preview UI.
- Bridge's `readComponentProperty` is called with `Button1`, `Text`.
- Executor's `execute` is called with the single write op.
- On read resolution, `reportExecutionErrors` is called with a result keyed by the correct `toolCallId`.

- [ ] **Step 2: Run test to verify failure**

Expected: FAIL (routing not implemented).

- [ ] **Step 3: Implement the split**

In the response-handling path (around where the operation preview is currently shown), partition operations:

```java
List<AIOperation> reads = new ArrayList<>();
List<AIOperation> others = new ArrayList<>();
for (AIOperation op : response.getOperations()) {
  if (op.getType() == AIOperation.Type.READ_RUNTIME) reads.add(op);
  else others.add(op);
}
```

For each read:
1. Parse payload to get `tool`, `args`, `toolCallId`.
2. Run the relevant validator (Task 10).
3. On validation failure: synthesize an immediate error result.
4. On validation success: call the matching `CompanionBridge` method; the callback records the result.
5. When all reads resolve (collect a counter), call `reportExecutionErrors(results)` — server will `continueWithToolResults`.

Let the `others` list flow through the existing preview path unchanged.

For `read_recent_logs`: bypass the bridge entirely, read the ring buffer directly from `Blockly.ReplMgr.aiLogBuffer` via JSNI, slice to `n`.

- [ ] **Step 4: Run test to verify pass**

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add ...AIResponseOrchestrator.java ...AIResponseOrchestratorTest.java
git commit -m "feat(ai-agent): intercept READ_RUNTIME ops and route to CompanionBridge"
```

---

## Phase 5 — Client Context + UI

### Task 13: `AIContextCollector.buildCompanionSnapshot`

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/AIContextCollector.java`

- [ ] **Step 1: Add `buildCompanionSnapshot()` private method**

Reads `Blockly.ReplMgr` state via JSNI:
- `ReplState.state == CONNECTED` check.
- Connection kind (WebRTC vs HTTP) — search `replmgr.js` for where transport is selected to identify the right flag.
- `Blockly.ReplMgr.aiActiveScreen`, `aiLogBuffer`, `aiErrorBuffer` from Task 7.

Returns a JSON string matching the schema in spec §5.1, or `null` if not connected.

- [ ] **Step 2: Call from `buildRequest()`**

```java
if (AIEditorState.isCompanionShareEnabled()) {
  String snapshot = buildCompanionSnapshot();
  if (snapshot != null) {
    request.setCompanionSnapshot(snapshot);
  }
}
```

- [ ] **Step 3: Build + manual verification**

`ant -f appengine/build.xml AiClientLib` — BUILD SUCCESSFUL.
Full manual test deferred to Phase 6.

- [ ] **Step 4: Commit**

```bash
git add ...AIContextCollector.java
git commit -m "feat(ai-agent): attach companionSnapshot to request when sharing enabled"
```

---

### Task 14: Share-runtime toggle in Companion connect dialogs

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/actions/WirelessAction.java`
- Modify: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/actions/EmulatorAction.java`
- Modify: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/actions/UsbAction.java`
- Modify: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/actions/ChromebookAction.java`

- [ ] **Step 1: Factor out a shared helper**

Create `AICompanionShareToggle.java` in the same `actions/` package (or in `aiagent/companion/` if the `actions/` package prefers staying lean — match existing structure). It exposes:

```java
public static CheckBox createIfAiEnabled();  // returns null if AI mode == OFF
public static void applyOnAccept(CheckBox toggle);  // sets AIEditorState flag
```

- [ ] **Step 2: Wire into each of the four action dialogs**

At each action's dialog construction (search each file for where the pairing dialog's main content panel is assembled), insert the toggle below the existing content when `AICompanionShareToggle.createIfAiEnabled()` returns non-null. On dialog confirm, call `applyOnAccept`.

- [ ] **Step 3: Build + manual smoke**

`ant -f appengine/build.xml AiClientLib` — BUILD SUCCESSFUL.

Manual: run dev server, toggle AI mode off → connect dialogs show no toggle. Toggle AI mode on → all four dialogs show the toggle.

- [ ] **Step 4: Commit**

```bash
git add ...WirelessAction.java ...EmulatorAction.java ...UsbAction.java ...ChromebookAction.java \
        ...AICompanionShareToggle.java
git commit -m "feat(ai-agent): add share-runtime toggle to companion connect dialogs"
```

---

### Task 15: "Ask AI" button on runtime-error dialog

**Files:**
- Modify: `appinventor/blocklyeditor/src/replmgr.js` — `runtimeerr` function (lines ~1036-1044)
- Modify: `appinventor/appengine/src/com/google/appinventor/client/editor/blocks/BlocklyPanel.java` — add `askAIAboutRuntimeError` JSNI-exported method

- [ ] **Step 1: Add the JSNI-exported handler in `BlocklyPanel`**

```java
public void askAIAboutRuntimeError(String message, String blockId, String componentName) {
  // Build displayText + contextHint (see Task 17 for snapshot logic).
  // For now, stub: contextHint = "Error: " + message + "\nBlock: " + blockId;
  sendExplainMessage(displayText, contextHint);  // reuses Explain Block path
}
```

Export via existing JSNI pattern in `BlocklyPanel`; a `top.BlocklyPanel_askAIAboutRuntimeError` handle becomes available.

- [ ] **Step 2: Extend `Blockly.Util.Dialog` call in `runtimeerr`**

Today (`replmgr.js:1041`), the dialog takes `title, message, buttonLabel, ...`. Investigate whether `Blockly.Util.Dialog` supports multiple buttons — check the `Util.Dialog` class definition. If yes, pass two buttons: Dismiss + Ask AI (latter only when AI mode is not OFF). If no, extend `Util.Dialog` to accept a secondary button list first.

Secondary button click handler: hide dialog, call `top.BlocklyPanel_askAIAboutRuntimeError(message, null, null)` (block-level variant comes in Task 16).

Show secondary button only when `top.AIEditorState_getMode_String() != "OFF"` (add this JSNI export to `AIEditorState` if not already available).

- [ ] **Step 3: Build + manual smoke**

Manual: run dev, trigger a runtime error with no blockid (e.g., invalid screen push). Verify dialog shows both buttons when AI is on and only Dismiss when AI is off. Clicking "Ask AI" opens the chat with a reasonable message.

- [ ] **Step 4: Commit**

```bash
git add appinventor/blocklyeditor/src/replmgr.js \
        appinventor/appengine/src/com/google/appinventor/client/editor/blocks/BlocklyPanel.java
git commit -m "feat(ai-agent): add 'Ask AI' button to runtime error dialog"
```

---

### Task 16: Block context menu "Ask AI about this error"

**Files:**
- Modify: `appinventor/blocklyeditor/src/blocklyeditor.js`
- Modify: `appinventor/blocklyeditor/src/msg/ai_blockly/messages.json` (English string)

- [ ] **Step 1: Register the context menu item**

Find the existing `appinventor_explain_block` registration (around `blocklyeditor.js:380`). Add a parallel registration:

```javascript
var askAiAboutErrorItem = {
  id: 'appinventor_ask_ai_about_error',
  weight: ...,
  preconditionFn: function(scope) {
    var block = scope.block;
    if (!block) return 'hidden';
    if (!block.replError) return 'hidden';
    if (window.parent.AIEditorState_getMode_String() === 'OFF') return 'hidden';
    return 'enabled';
  },
  callback: function(scope) {
    var block = scope.block;
    var componentName = resolveImplicatedComponent(block); // helper
    window.parent.BlocklyPanel_askAIAboutRuntimeError(
        block.replError, block.id, componentName);
  },
  displayText: Blockly.Msg.AI_ASK_ABOUT_ERROR
};
Blockly.ContextMenuRegistry.registry.register(askAiAboutErrorItem);
```

`resolveImplicatedComponent` mirrors logic already present in Explain Block for extracting the component name from the block's mutation attributes.

- [ ] **Step 2: Add i18n string**

`AI_ASK_ABOUT_ERROR: "Ask AI about this error"` in `messages.json`.

- [ ] **Step 3: Manual verification**

Trigger a block-specific error on a Companion. Right-click the warning-marked block — new menu entry appears only when error present + AI on. Clicking opens chat with message and block context.

- [ ] **Step 4: Commit**

```bash
git add appinventor/blocklyeditor/src/blocklyeditor.js \
        appinventor/blocklyeditor/src/msg/ai_blockly/messages.json
git commit -m "feat(ai-agent): add 'Ask AI about this error' block context menu"
```

---

### Task 17: Snapshot-at-error-time YAIL scanner

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/client/editor/blocks/BlocklyPanel.java` — enrich `askAIAboutRuntimeError`
- Create: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/companion/BlockYailSymbolScanner.java`
- Test: scanner test (new)

- [ ] **Step 1: Write failing test for the scanner**

```java
public void testScanFindsGetProperty() {
  List<Symbol> syms = BlockYailSymbolScanner.scan(
      "(define-event Button1 Click () "
      + "(set-var! x (select-list-item (get-var g$items) "
      + "(get-property 'TextBox1 'Text))))");
  assertTrue(syms.contains(new Symbol.Property("TextBox1", "Text")));
  assertTrue(syms.contains(new Symbol.Variable("items")));
}

public void testScanDedupes() { ... }
public void testScanCapsAt10() { ... }
```

- [ ] **Step 2: Run test to verify failure**

- [ ] **Step 3: Implement `BlockYailSymbolScanner.scan(yail)`**

Regex-based or lightweight S-expression pass over the YAIL string. Collect:
- `(get-property 'X 'Y)` → `Symbol.Property(X, Y)`
- `(get-var g$Z)` → `Symbol.Variable(Z)`
- `(lookup-in-current-form-environment 'W)` — treat as a component ref; skip or resolve to property reads as context warrants.

Dedupe, cap at 10.

- [ ] **Step 4: Wire into `askAIAboutRuntimeError`**

After the stub contextHint from Task 15, if `AIEditorState.isCompanionShareEnabled()` and `ReplMgr.isConnected()`:

1. Fetch the block's YAIL (via `BlocksEditor.getBlockYail(blockId)` — investigate exact API).
2. Run `BlockYailSymbolScanner.scan(yail)`.
3. For each symbol (up to the 10-read cap), dispatch a `CompanionBridge.read*` call. Each read carries the bridge's normal 5s per-read timeout.
4. Start a 2000ms wall-clock fan-out gate from the handoff click. All reads are dispatched at once up to the 10-read cap; the gate governs when we stop *waiting* for results, not when we stop sending (no staggered sending here).
5. Readiness condition — send the handoff when **either**: (a) all dispatched reads have resolved, or (b) the 2s wall clock has elapsed. Reads still in flight at the 2s mark continue to their individual 5s cap (so late-arriving results could be logged to the console for diagnostics), but they are **omitted from the snapshot** sent to the LLM. Resolved reads are appended under `Runtime snapshot at error time:`; unresolved symbols are omitted silently (best-effort enrichment — never block the handoff).

This matches spec §5.13 verbatim. Do not re-derive alternative readiness conditions during impl.

- [ ] **Step 5: Run tests + manual**

Expected: scanner tests PASS. Manual: trigger an index-out-of-bounds error on a block that reads a TextBox. Click Ask AI. Verify the chat message's hidden context includes `TextBox1.Text = "..."` in the snapshot section.

- [ ] **Step 6: Commit**

```bash
git add ...BlockYailSymbolScanner.java ...BlockYailSymbolScannerTest.java \
        ...BlocklyPanel.java
git commit -m "feat(ai-agent): snapshot referenced runtime values at error-button click"
```

---

## Phase 6 — Polish

### Task 18: I18n strings (English only)

**Files:**
- Modify: `appinventor/blocklyeditor/src/msg/ai_blockly/messages.json`

**Scope note:** add the new keys to the English `messages.json` only. Do **not** touch the 21 non-English `messages_*.json` files — the i18n pipeline falls back to English automatically when a key is absent, so injecting English strings as "fallbacks" would just pollute translation files with non-translations and confuse future localizers.

- [ ] **Step 1: Add English keys**

Append to `messages.json`:

```json
"REPL_AI_SHARE_RUNTIME": "Share runtime data with AI Assistant (read-only: logs, errors, component values)",
"AI_ASK_ABOUT_ERROR": "Ask AI about this error",
"AI_ASK_ABOUT_RUNTIME_ERROR_BTN": "Ask AI"
```

(The `AI_ASK_ABOUT_ERROR` key added in Task 16 may already be here — check before duplicating.)

- [ ] **Step 2: Build and verify**

`ant -f appengine/build.xml AiClientLib` — BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add appinventor/blocklyeditor/src/msg/ai_blockly/messages.json
git commit -m "i18n(ai-agent): add companion share + ask-ai keys (English)"
```

---

### Task 19: Feature flag + default properties

**Files:**
- Modify: `appinventor/appengine/war/WEB-INF/appengine-web.xml`

- [ ] **Step 1: Add the flag declaration**

In the `<system-properties>` block near the other `ai.agent.features.*` entries:

```xml
<property name="ai.agent.features.companion-context" value="true" />
```

- [ ] **Step 2: Verify flag is wired**

`grep -n "companion-context" appinventor/appengine/src/com/google/appinventor/server/aiagent/AIContextBuilder.java` — expect a match (the `Flag.createFlag` call added in Task 5).

- [ ] **Step 3: Commit**

```bash
git add appinventor/appengine/war/WEB-INF/appengine-web.xml
git commit -m "feat(ai-agent): ship ai.agent.features.companion-context flag (default on)"
```

---

### Task 20: Documentation update

**Files:**
- Modify: `CONTRIBUTING_AI.md`

- [ ] **Step 1: Add a "Companion Integration" section**

Cover (brief, under 300 words):
- The toggle's purpose and per-session lifecycle.
- The three read tools, their validators, and the client-side resolution path (contrast with server-side `lookup_component`).
- The `ai-read-*` blockid invariant.
- The "Ask AI about this error" surfaces (dialog + block context menu).
- The new feature flag.

Add an entry to the "Server" and "Client" directory-structure tables for the new files.

Link to the spec and this plan.

- [ ] **Step 2: Commit**

```bash
git add CONTRIBUTING_AI.md
git commit -m "docs: document companion read integration"
```

---

## Post-Implementation Verification

After all tasks complete:

- [ ] Run full test suite: `ant -f appengine/build.xml tests`
- [ ] Run full client build: `ant -f appengine/build.xml war`
- [ ] Start dev server and run the 5 manual verifications from spec §9.3.
- [ ] Use `superpowers:verification-before-completion` before claiming done.
- [ ] Request code review per `superpowers:requesting-code-review`.

---

## Rollback

Every task is a single commit touching a bounded area. To roll back any single feature (toggle, error button, tools), revert the relevant commit(s). The feature flag `ai.agent.features.companion-context` provides a server-side instant kill for all of Phase 2 and subsequent tool shipping, without requiring a client deploy.

---

## Open Items Carried From Spec

These are deliberately left for the implementer to resolve during the task they naturally fall into, not pre-decided here:

1. **Mixed-batch ordering** (spec §11.1) — resolved in Task 12. Recommendation: reads first, then wait for read results before running writes, so the LLM can inform write decisions on next turn. If that creates latency issues, fall through to parallel with a note.
2. **No-`pushScreen` fallback screen name** (spec §11.3) — resolve in Task 7: default to currently-edited screen; add a small comment documenting the fallback.
3. **Exact `ReplTransport` JSNI surface** — resolved in Task 11 by inspecting `Blockly.ReplMgr.putYail`'s call signature at impl time.
