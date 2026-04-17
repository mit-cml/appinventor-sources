# Multi-Agent Orchestration for AI Agent

**Date:** 2026-04-07
**Status:** Draft

## Problem

The AI Agent currently operates as a single agent per conversation. For multi-screen projects, the user must wait for the agent to sequentially navigate between screens, toggling views and applying operations one batch at a time. A request like "Build a login flow with Screen1 as the login form and Screen2 as the dashboard" produces 8+ sequential batches (switch screen, add components, toggle to blocks, write blocks, switch screen again, repeat).

This is slow, produces long approval chains, and doesn't leverage the natural isolation between screens -- work on Screen1 has no dependency on work on Screen2.

## Solution

Introduce a two-phase **Plan & Execute** workflow gated behind a feature flag. In the planning phase, the LLM researches the project and proposes a structured plan. After user approval, the client applies project-level operations (screen creation, project properties) first, then the client manages parallel child conversations -- one per screen -- each backed by that screen's background editors (hidden but functional). Operations are applied to background editors during execution for live continuation state. The user sees compact status cards per agent and approves/rejects results per screen.

## Design

### Feature Flags

**Server-side** (`appengine-web.xml`):
```xml
<property name="ai.agent.orchestration" value="false" />
```

When `false`, the entire system behaves exactly as today. No new tools, no new UI elements, no plan phase. The flag is checked in `AIContextBuilder` (tool filtering), `AIAgentEngine` (request routing), and the client (UI toggle visibility).

**Client-side** (per-conversation toggle):
When the server flag is on **and** the project's AI mode is **Project Editor**, the AI chat dialog shows a mode toggle: **Direct** (current behavior) vs **Plan & Execute**. The toggle is hidden in Advisor and Screen Editor modes -- those modes don't support multi-screen operations, so orchestration doesn't apply. The selected mode is sent in `AIAgentRequest` as a new field and persists in `AIEditorState` for the conversation lifetime. Defaults to Direct. If the user switches AI mode from Project Editor to Screen Editor mid-conversation, the toggle disappears and the mode reverts to Direct.

**Interaction with tutorial mode:** When the project has a non-empty `TutorialURL` and the user selects Plan & Execute, the client shows a confirmation dialog warning that tutorial projects are designed for step-by-step learning and Plan & Execute may skip pedagogical steps. The user can proceed or switch back to Direct mode. The server does not block `executePlan` for tutorial projects -- the decision is the user's.

### Phase 1: Planning

#### Tool Set

When the user selects "Plan & Execute" mode, the LLM receives a restricted tool set:

| Tool | Type | Purpose |
|------|------|---------|
| `lookup_component` | Read-only | Existing -- research component specs |
| `lookup_screen` | Read-only | Existing -- research screen state |
| `propose_plan` | Plan | **New** -- submit a structured execution plan |

No write tools (add_component, write_block, etc.) are available during planning. The LLM can research the project thoroughly via read-only tools before committing to a plan.

#### The `propose_plan` Tool

```json
{
  "name": "propose_plan",
  "description": "Propose an execution plan for the user to review before making changes. Each step targets a specific screen and describes what will be done. Steps targeting different screens can execute in parallel. Use lookup_component and lookup_screen first to research the project, then propose a complete plan.",
  "parameters": {
    "type": "object",
    "properties": {
      "summary": {
        "type": "string",
        "description": "Brief overall description of what the plan accomplishes"
      },
      "steps": {
        "type": "array",
        "items": {
          "type": "object",
          "properties": {
            "id": {
              "type": "string",
              "description": "Unique step identifier (e.g. 's1', 's2')"
            },
            "screen": {
              "type": "string",
              "description": "Target screen name. Use '__project__' for project-level operations (create screen, project properties)"
            },
            "description": {
              "type": "string",
              "description": "What this step will do"
            },
            "depends_on": {
              "type": "array",
              "items": { "type": "string" },
              "description": "Step IDs that must complete before this step starts. Typically project-level steps (screen creation) must precede screen-level steps."
            }
          },
          "required": ["id", "screen", "description"]
        }
      }
    },
    "required": ["summary", "steps"]
  }
}
```

**Example plan:**
```json
{
  "summary": "Build a login flow with a login form on Screen1 and a dashboard on Screen2",
  "steps": [
    {
      "id": "s1",
      "screen": "__project__",
      "description": "Create Screen2 for the dashboard"
    },
    {
      "id": "s2",
      "screen": "Screen1",
      "description": "Add username/password TextBoxes, a Login button, and a click handler that validates input and navigates to Screen2"
    },
    {
      "id": "s3",
      "screen": "Screen2",
      "description": "Add a welcome Label, a data ListView, and an initialization handler that loads data",
      "depends_on": ["s1"]
    }
  ]
}
```

#### Server-Side Handling

When `LLMResponseParser` encounters a `propose_plan` tool call, it is handled as a **special case in the parser**, not through `ModeEnforcer`'s solo-op logic. The semantics are different from solo ops like `TOGGLE_EDITOR` (which are solo because subsequent ops would target the wrong view). `propose_plan` is terminal because it ends the planning turn:

1. Parse and validate the plan structure (required fields, valid step IDs, no circular dependencies).
2. Create a new `AIOperation` with type `PROPOSE_PLAN` and the plan JSON as payload.
3. If `propose_plan` is present alongside other tool calls, the parser **discards the other tool calls** (they should not exist in planning mode, but this is a safety net). This logic lives in `LLMResponseParser.parseToolCalls()`, not in `ModeEnforcer`.
4. The plan tool call result is set to `"Plan delivered to user for review."` in the continuation state.

The plan is NOT executed server-side. It goes to the client for review.

#### Client-Side Plan Review

When the orchestrator receives a `PROPOSE_PLAN` operation:

1. Render the plan in the chat as a structured card: summary at top, each step as a row showing screen target, description, and dependency arrows.
2. Show three buttons: **Approve**, **Edit & Approve** (opens plan as editable text), **Reject**.
3. On Reject: send rejection feedback to parent (same as current reject flow).
4. On Approve with project-level steps: extract project operations (`CREATE_SCREEN`, `SET_PROJECT_PROP`, `DELETE_SCREEN`) and present them as a standard operation preview. The user applies them through the existing `AIOperationExecutor` Phase 1 (async) flow. This means new screens are created, saved to storage, and available via `StorageIo` before any child agent starts. After project ops succeed, the client calls `executePlan` with the remaining screen-level steps.
5. On Approve with no project-level steps: call `executePlan` directly.

**Example flow for a plan that creates Screen2:**
```
Plan: [s1: create Screen2, s2: work on Screen1, s3: work on Screen2 (depends s1)]
  -> User approves plan
  -> Client shows project ops preview: "Create Screen2"
  -> User approves -> AIOperationExecutor creates Screen2 (saved to storage)
  -> Client calls executePlan(steps=[s2, s3])
  -> Server spawns child agents:
       Child A (Screen1): reads Screen1 from StorageIo, generates operations
       Child B (Screen2): reads Screen2 from StorageIo (empty, just created), generates operations
  -> Both run in parallel, results returned to client
```

#### New Operation Type

Add `PROPOSE_PLAN` to `AIOperation.Type`. Unlike solo ops (`TOGGLE_EDITOR`, `SWITCH_SCREEN`), this type is not added to `ModeEnforcer`'s solo-op set. Its exclusivity is enforced in `LLMResponseParser` as described above.

### Phase 2: Execution

#### Architecture: Client-Managed Parallel Conversations

Execution is orchestrated **entirely by the client**. No new server-side execution RPCs are needed. The client manages N parallel conversations using the existing `processRequest`/`continueRequest` RPCs, each targeting a specific screen's background editors.

This works because:
1. **Every screen's editors already exist in memory.** `YaProjectEditor` maintains an `editorMap` with a `YaFormEditor` + `BlocksEditor` pair per screen, loaded during `loadProject()`. Note: non-Screen1 editors are loaded lazily (deferred until Screen1 finishes loading). Before starting any child conversation, the orchestration manager must verify editors are fully loaded -- see "Editor Readiness Check" below.
2. **Form editors work without visibility.** The component model (`MockForm root`) is in-memory. `addMockComponent()`, property mutations, etc. work regardless of whether the editor is the visible one.
3. **Blocks editors work without visibility.** Each screen has its own `WorkspaceSvg` in `Blockly.allWorkspaces` (keyed by composite `projectId + "_" + screenName`, not just screen name). Hidden workspaces support block manipulation -- rendering is deferred via `pendingRender` and kicks in when the workspace becomes visible.
4. **GWT is single-threaded.** JavaScript has a single-threaded event loop. GWT async RPC callbacks execute one at a time. This means two child conversations can never have their synchronous operation-application phases interleave. Blockly global state (`Blockly.Events.disabled_` counter, `Blockly.common.getMainWorkspace()`) is safe because each synchronous callback completes atomically before the next one starts.

#### Orchestration Flow

```
1. User approves plan

2. Client applies project-level steps (CREATE_SCREEN, SET_PROJECT_PROP, etc.)
   -> Standard AIOperationExecutor Phase 1 (async)
   -> New screens are created, their editors load (form + blocks)
   -> waitForScreenReady() ensures editors are initialized
   -> After all project ops succeed, proceed

3. Client ensures all target screens' editors are fully loaded (editor readiness check)
   -> Existing screens: already in editorMap (loaded at project open), but verify
      both formEditor.isLoadComplete() and blocksEditor.loadComplete are true
   -> New screens: loaded in step 2 above, verified by waitForScreenReady()
   -> If any editor is still loading (deferred lazy load), wait before proceeding

4. Client spawns N parallel child conversations
   -> For each screen step group:
       * Build AIAgentRequest from that screen's background editor
         (AIContextCollector.buildRequestForScreen(screenName, stepDescription))
       * Call processRequest() -- standard existing RPC
       * Server handles LLM call, returns AIAgentResponse with operations
       * Client applies operations to the BACKGROUND editor (not visible screen)
       * If hasMore: build fresh context from background editor, call continueRequest()
       * Repeat continuation loop until hasMore=false
       * Collect final operations list for this screen

5. All child conversations finish -> show combined preview for approval
```

#### Background Editor Operations

**Designer operations** (ADD_COMPONENT, SET_PROPERTY, DELETE_COMPONENT, RENAME_COMPONENT):

Modify `AIDesignerOperations` to accept a target `YaFormEditor` parameter instead of calling `AIEditorState.getCurrentFormEditor()`. The form editor is retrieved from `YaProjectEditor.getFormFileEditor(screenName)`. Operations mutate the in-memory component model directly -- no visibility required.

**Block operations** (WRITE_BLOCK, DELETE_BLOCK):

`AI.YailToBlocks.convert(yail, workspace)` already takes an explicit workspace parameter and does not reference `Blockly.common.getMainWorkspace()` internally. However, some Blockly internals called during block creation may consult the main workspace. During implementation, audit whether `convert()` works correctly when the target workspace is not the main workspace. If it does, no save/restore is needed. If not, use the existing save/restore pattern from `BlocklyPanel.doLoadBlocksContent()`:

```javascript
// In BlocklyPanel -- new method: doWriteBlockForScreen(formName, yail)
var compositeKey = projectId + '_' + screenName; // Blockly.allWorkspaces key format
var targetWorkspace = Blockly.allWorkspaces[compositeKey];
var previousMainWorkspace = Blockly.common.getMainWorkspace();
try {
    Blockly.common.setMainWorkspace(targetWorkspace);
    AI.YailToBlocks.convert(yail, targetWorkspace);
    // Rendering is automatically deferred (workspace is hidden)
} finally {
    Blockly.common.setMainWorkspace(previousMainWorkspace);
}
```

**Important**: `Blockly.allWorkspaces` is keyed by `projectId + "_" + screenName` (composite key), not just screen name. All JSNI code accessing workspaces must use this composite key. Java-side access goes through `BlocksEditor` wrappers which already hold the correct workspace reference.

The hidden workspace's `requestRender` already detects `display === 'none'` and sets `pendingRender = true` instead of scheduling a setTimeout. When the user eventually views that screen, `makeActive` triggers the deferred render.

**`sendComponentData` skip**: The existing `sendComponentData()` has a guard that no-ops when the target form is not the current form. This is correct for background editors -- the Companion only talks to the active screen. When the user later navigates to the modified screen, the existing `makeActive()` flow (which includes `pollYail`) pushes the updated YAIL to the Companion.

Modify `AIBlockOperations` to accept a target `BlocksEditor` parameter instead of calling `AIEditorState.getCurrentBlocksEditor()`. The blocks editor is retrieved from `YaProjectEditor.getBlocksFileEditor(screenName)`.

#### Targeted Context Collection

`AIContextCollector` gains a new method:

```java
public AIAgentRequest buildRequestForScreen(String screenName, String userMessage) {
    // Note: getFormFileEditor returns DesignerEditor -- downcast required
    YaFormEditor formEditor = (YaFormEditor) projectEditor.getFormFileEditor(screenName);
    YaBlocksEditor blocksEditor = (YaBlocksEditor) projectEditor.getBlocksFileEditor(screenName);

    // Build request using the target screen's editors instead of the visible one
    request.setScreenName(screenName);
    request.setScreenComponentsJson(formEditor.getPropertiesJson()); // public method
    request.setBlocksYail(blocksEditor.getBlocksYail());
    request.setCurrentView(/* child tracks view internally */);
    // ... project snapshot, assets, etc. from current project (shared)
}
```

**Important**: `buildRequestForScreen` must also be used for continuation and error-retry paths within child conversations. The existing `AIContextCollector` methods called during `fetchContinuation()` and `reportValidationErrors()` (which use `buildRequest(null)`) hardcode lookups via `DesignToolbar.currentScreen`. Child conversations must always use the screen-targeted variant, never the visible-screen path.

This provides **live editor state** for continuation contexts, solving the stale-state problem entirely.

**Implementation notes:**
- `getFormFileEditor()` returns `DesignerEditor<?,?,?,?,?>`, not `YaFormEditor`. Downcast is required (safe at runtime -- these are always `YaFormEditor` instances). Consider adding `getYaFormEditor(String)` convenience method.
- `getBlocksFileEditor()` returns `BlocksEditor<?,?>`, not `YaBlocksEditor`. Same downcast pattern.
- `getBlocksYail()` delegates to a JSNI call on the specific workspace instance (not the main workspace). Based on code analysis it should work on hidden workspaces, but verify during implementation.
- `validateYail()` is purely structural (S-expression parsing) -- it does NOT validate against workspace state (e.g., "does this component exist?"). Actual workspace-level validation only happens during `convert()` at execution time. This is the same limitation as the current single-agent flow.

#### Targeted Operation Execution

Introduce a `ScreenExecutionContext` that carries target editors through the entire execution pipeline:

```java
public class ScreenExecutionContext {
    private final String screenName;
    private final YaFormEditor formEditor;
    private final YaBlocksEditor blocksEditor;

    // Replaces all AIEditorState.getCurrentFormEditor() / getCurrentBlocksEditor() calls
    public YaFormEditor getFormEditor() { return formEditor; }
    public YaBlocksEditor getBlocksEditor() { return blocksEditor; }

    // Replaces AIEditorState.componentExists(), blockExists(), etc.
    public boolean componentExists(String name) { ... }
    public boolean blockExists(String identifier) { ... }
}
```

`AIOperationExecutor` gains a new entry point:

```java
public static void executeForScreen(
    ScreenExecutionContext context,
    List<AIOperation> operations,
    ExecutionCallback callback) {
    // Execute phases 2-5 using context.getFormEditor() / context.getBlocksEditor()
    // Phase 1 (SWITCH_SCREEN, CREATE_SCREEN) is NOT used here
}
```

**Scope of parameterization**: `ScreenExecutionContext` replaces `AIEditorState` lookups at **11 call sites across 4 files**:
- `AIDesignerOperations` (4 sites): `executeAddComponent`, `executeSetProperty`, `executeRenameComponent`, `executeDeleteComponent` -- each calls `AIEditorState.getCurrentFormEditor()`
- `AIBlockOperations` (2 sites): `executeWriteBlock`, `executeDeleteBlock` -- each calls `AIEditorState.getCurrentBlocksEditor()`
- `AIOperationExecutor` (5 sites): `isIdempotentSkip()` (calls `componentExists`, `blockExists`, `screenExists`), `setPendingBlockDeletions()`, `addPendingBlockUpserts()`, `clearPendingBlockDeletions()`, and the `runSyncPhases()` finally block -- all call `AIEditorState.getCurrentBlocksEditor()` or `AIEditorState.*Exists()`
- `AIOperationValidator` and its sub-validators (`DesignerOperationValidator`, `BlockOperationValidator`) may also call `AIEditorState` -- verify during implementation.

The `runSyncPhases()` finally block calls `blocksEditor.sendComponentData(true)`. For background editors, this will correctly no-op due to the `currentForm` guard in `BlocklyPanel.sendComponentData()`. The `ScreenExecutionContext`-aware executor should skip this call entirely for non-visible screens to avoid the unnecessary call chain.

For the existing single-agent flow, a `ScreenExecutionContext.forCurrentScreen()` factory method creates a context from `AIEditorState`, maintaining backward compatibility.

#### Parallel Client-Side Execution

The client manages parallelism via GWT's async RPC model. Each child conversation is an independent chain of `processRequest` → apply → `continueRequest` → apply → ... calls:

```java
// In AIOrchestrationManager (new client class):
for (ScreenStepGroup group : screenSteps) {
    // Each screen gets its own response orchestrator instance
    ChildConversation child = new ChildConversation(
        group.getScreenName(),
        group.getStepDescription(),
        projectEditor);

    child.start(); // Fires processRequest, handles continuations internally
    activeChildren.add(child);
}

// ChildConversation manages its own:
// - AIContextCollector.buildRequestForScreen() for context
// - processRequest/continueRequest RPC calls
// - AIOperationExecutor.executeForScreen() for applying ops to background editor
// - Streaming status via its own StreamBuffer key
// - hasMore continuation loops
// - Validation retries (up to 5)
```

GWT's async RPC is non-blocking -- N parallel conversations just means N in-flight RPC calls. No client-side threading needed.

#### Child Agent Scope

| Aspect | Parent Agent | Child Agent |
|--------|-------------|-------------|
| Tool set (server-side) | Full planning tools | Screen-scoped only (no project-level, no propose_plan) |
| Context source | Visible screen's live editor | **Background editor for target screen** (live state, not StorageIo) |
| Conversation | Persisted (Memcache + Datastore) | Ephemeral (server Memcache only, short TTL, not in Datastore) |
| Streaming | Main StreamBuffer | Own StreamBuffer (projectId + ":" + screenName key) |
| Continuation | Via AIResponseOrchestrator | Via ChildConversation (same RPC flow) |
| Validation retries | Up to 5 | Up to 5 (against background editor's Blockly workspace) |
| Mode enforcement | PLANNING context | CHILD_EXECUTION context |

#### Child Agent LLM Context

Each child conversation's LLM receives:

**System prompt** (same 4 cached layers as current single-agent):
- Reference module (App Inventor rules and constraints)
- Catalog module (component specs from simple_components.json)
- Grammar module (YAIL reference)
- Examples module (few-shot examples)

**Context messages** (rebuilt per child from `AIContextBuilder`):
1. **Mode context** (ModeModule): screen-scoped operation instructions. Describes available screen-level tools only -- no project-level operations mentioned.
2. **Project overview** (ProjectModule): project name, full screen list, assets, extensions, other screen summaries (component count + title). The child knows the project structure so it can make cross-screen references (e.g., "open Screen2" in a button handler).
3. **Target screen state** (ScreenModule): full component tree JSON + blocks YAIL from the target screen's background editor. Only this child's screen -- not other screens' full state.
4. **Plan context** (new, orchestration-specific): the overall plan summary, this child's step description, and a brief note of what other children are doing (e.g., "Another agent is building a dashboard on Screen2"). This lets the child make informed cross-screen decisions without seeing other screens' full state.

**User message**: the plan step description (e.g., "Add username/password TextBoxes, a Login button, and a click handler that validates input and navigates to Screen2").

**Tools**: screen-scoped only (`add_component`, `set_property`, `rename_component`, `delete_component`, `write_block`, `delete_block`, `toggle_editor`, `lookup_component`, `lookup_screen`). No `propose_plan`, `switch_screen`, `create_screen`, `delete_screen`, or `set_project_property`.

Child agents get `toggle_editor` -- the child conversation tracks the current view internally and rebuilds context messages accordingly. The background editors support both designer and blocks operations.

**Block pre-validation works**: `validateYail()` is a pure structural check (S-expression parsing) that does not require a workspace at all. It works identically for any screen. Actual workspace-level validation (e.g., "does this component exist?") only happens during `convert()` at execution time -- this is the same as the current single-agent flow.

#### Server-Side Changes

The server requires significant changes to support concurrent child conversations for the same project. Currently, all state (`ConversationManager`, `StreamBuffer`, `AIAgentEngine`) is keyed by `projectId` only. Two concurrent child conversations for the same project would corrupt each other's Memcache state.

**1. `AIAgentRequest` gains fields:**
- `orchestrationMode` (boolean) and `targetScreen` (String).
- When `orchestrationMode=true`, the server uses these to scope all state by screen.

**2. `StorageIo` interface changes** -- new screen-scoped overloads:
```java
// Conversation state (existing: keyed by projectId)
void setAIConversationState(long projectId, String screenName, AIConversationState state);
AIConversationState getAIConversationState(long projectId, String screenName);
void clearAIConversationState(long projectId, String screenName);

// Stream buffer (existing: keyed by projectId)
void initAIStreamBuffer(long projectId, String screenName);
void appendAIStreamChunk(long projectId, String screenName, String chunk);
List<String> consumeAIStreamChunks(long projectId, String screenName);
void clearAIStreamBuffer(long projectId, String screenName);

// Cancellation (existing: keyed by projectId)
void setAIStreamCancelled(long projectId, String screenName);
boolean isAIStreamCancelled(long projectId, String screenName);
void clearAIStreamCancelled(long projectId, String screenName);
```
These use Memcache keys like `AI_CONV_CACHE_KEY_PREFIX + projectId + ":" + screenName`. The existing `projectId`-only methods remain unchanged for the parent conversation and non-orchestration flows.

**3. `ConversationManager` changes:**
- `getConversation`, `saveConversation`, `clearConversation` gain `String screenName` overloads.
- `storeMessage` for child conversations stores to a screen-scoped conversation ID (not persisted to Datastore -- children are ephemeral).

**4. `StreamBuffer` changes:**
- Constructor gains optional `String screenName` parameter: `new StreamBuffer(storageIo, projectId, screenName)`.
- When `screenName != null`, all Memcache operations use the composite key.

**5. `AIAgentEngine` changes:**
- `processRequest` and `continueRequest` check `request.isOrchestrationMode()` and pass `request.getTargetScreen()` through to `ConversationManager` and `StreamBuffer`.
- The `initConversation` method uses the screen-scoped key when in orchestration mode.

**6. `AIContextBuilder.buildTools()`:** when `enforcementContext=CHILD_EXECUTION`, excludes project-level tools and `propose_plan`.

**7. RPC signature changes:**
- `getRequestStatus(long projectId)` gains an optional `String targetScreen` parameter. When null, returns the parent's status (existing behavior). When non-null, returns the specific child's stream status. The client polls once per active child.
- `cancelRequest(long projectId)` gains an optional `String targetScreen` parameter for cancelling specific children.

**8. Rate limiting:**
- When `orchestrationMode=true`, `processRequest`/`continueRequest` calls are exempt from the per-user rate limit. Instead, the client enforces a per-plan budget (max 5 children, max 20 total RPCs). The server trusts the `orchestrationMode` flag -- abuse is mitigated by the flag only being available when the `ai.agent.orchestration` server flag is enabled.
- Alternative: increase the rate limit to 50/min when orchestration is active. This is simpler but less precise.

### Phase 3: Approval (FIFO Queue Model)

#### Operation Batch Queue

All child conversations feed into a single FIFO queue. As each child produces an operation batch (from `processRequest` or `continueRequest`), it is enqueued. The user reviews batches **one at a time** in the chat, same UX as the current single-agent flow. The only difference: batches are labeled with which screen they came from, and they may interleave across screens.

```
Queue state during execution (example):

  [1] Screen1 Agent: ADD_COMPONENT x3, SET_PROPERTY x6    <- user reviewing this
  [2] Screen2 Agent: ADD_COMPONENT x4, SET_PROPERTY x8    <- waiting
  [3] Screen1 Agent: WRITE_BLOCK x2 (continuation)        <- not yet produced (blocked)
```

Batch [3] doesn't exist yet -- Screen1's child is blocked waiting for batch [1] to be approved so it can get fresh editor state for its continuation.

#### Approval Flow

```
Child agents start in parallel. Each fires processRequest().

Screen1 Agent responds first with batch of designer ops:
  [Screen1] I'll add the login form components.
    + TextBox (Username)
    + TextBox (Password)
    + Button (Login)
    [Apply] [Apply & Accept All] [Reject]

User clicks "Apply":
  -> Operations applied to Screen1's editor (background or visible)
  -> Screen1 child's continuation fires (continueRequest with fresh state)
  -> Next batch in queue presented to user

Screen2 Agent's response (was queued while user reviewed Screen1):
  [Screen2] I'll set up the dashboard layout.
    + Label (Welcome)
    + ListView (Data)
    [Apply] [Apply & Accept All] [Reject]

User clicks "Apply":
  -> Operations applied to Screen2's editor
  -> Screen2 child's continuation fires

Screen1 Agent's continuation batch arrives:
  [Screen1] Now I'll add the event handler.
    + WRITE_BLOCK (Button1.Click)
    [Apply] [Apply & Accept All] [Reject]

... and so on until all children complete with hasMore=false.
```

#### Apply & Accept All

When the user clicks **"Apply & Accept All"**, the `autoAcceptAll` flag is set at the orchestration level. All batches currently in the queue and all future batches are auto-approved. This means:
- All queued batches are applied immediately
- Continuations fire as fast as possible
- All children run to completion without user interaction
- Status cards show progress until all children finish

This is the fast path for users who trust the plan and want maximum speed.

#### Rejection = Hard Stop

When the user clicks **"Reject"** on any batch:

1. The rejected batch's operations are **not applied**.
2. All remaining batches in the queue are **discarded**.
3. All running child conversations are **cancelled** (via StreamBuffer cancellation flags).
4. The orchestration manager collects a summary of what was applied so far (which screens, which operations).
5. The user returns to the **parent chat** and explains what was wrong.
6. The parent agent receives context: "Plan execution was partially completed. Applied: [summary of approved batches]. Rejected at: [Screen X] batch with operations [summary]. User feedback: [user's message]."
7. The parent can re-plan (propose a revised plan) or ask clarifying questions.

No rollback is needed -- operations are only applied to editors after the user approves each batch. Rejected batches were never applied.

#### Continuation Depends on Approval

A child conversation's continuation loop is gated on user approval:

1. Child produces batch (operations + hasMore=true)
2. Batch enters the FIFO queue
3. **Child is paused** -- it cannot continue until this batch is approved
4. When the user approves the batch:
   - Operations are applied to the target screen's editor (via `ScreenExecutionContext`)
   - Fresh context is built from the now-updated editor (`buildRequestForScreen`)
   - `continueRequest()` fires for this child
5. Child produces next batch → back to step 2

This means parallelism is highest on the **initial batches** (all children fire `processRequest` simultaneously). Continuations are serialized through the approval queue. With "Apply & Accept All", continuations fire immediately and parallelism is maintained throughout.

#### Block Pre-Validation

Since operations are applied to real (hidden) Blockly workspaces after approval, `validateYail()` can dry-run WRITE_BLOCK operations against the target screen's workspace before showing the batch to the user. The existing validation retry loop (up to 5 retries) works within each child conversation -- validation errors are sent back to the server for the child's LLM to fix, all before the batch reaches the user's queue.

#### Editor Readiness Check

Before starting a child conversation for a screen, the orchestration manager verifies that both editors are fully loaded. Non-Screen1 editors use deferred loading (`YaProjectEditor.addDesigner()` schedules loading after Screen1 finishes). The check:

```java
boolean isEditorReady(String screenName) {
    DesignerEditor<?,?,?,?,?> formEditor = projectEditor.getFormFileEditor(screenName);
    BlocksEditor<?,?> blocksEditor = projectEditor.getBlocksFileEditor(screenName);
    return formEditor != null
        && formEditor.isLoadComplete()   // DesignerEditor method
        && blocksEditor != null
        && blocksEditor.isLoaded();      // BlocksEditor method (note: isLoaded, not isLoadComplete)
}
```

If an editor is not ready, the orchestration manager polls with a short delay (100ms, same as the existing deferred loading interval) until it is. In practice, by the time a user interacts with the AI agent, all editors will be loaded. This check is a safety guard for edge cases (e.g., Plan & Execute triggered immediately after opening a large project).

#### Screens Created by the Plan

When the plan creates new screens (via project-level CREATE_SCREEN steps applied before child conversations start), the `AIOperationExecutor` Phase 1 flow creates the screen and triggers editor loading. `waitForScreenReady()` ensures both `YaFormEditor` and `BlocksEditor` are initialized and added to `editorMap` before child conversations begin. The new screen's editors start with empty state, which is correct.

### Streaming and Status UI

#### Status Cards

During plan execution, the chat shows a status area with one card per active child agent:

```
+------------------------------------------+
| Plan: Build login flow with dashboard     |
|                                           |
|  [x] Project setup          completed     |
|  [~] Screen1: Login Form    adding blocks |
|  [~] Screen2: Dashboard     thinking...   |
+------------------------------------------+
```

Each card shows:
- Screen name and step description (from plan)
- Current status text (from child's StreamBuffer status chunks)
- A completion indicator (pending / in-progress / completed / failed)

Cards update in real-time via polling. The existing `getRequestStatus` RPC is extended:

#### Extended Streaming Poll

`AIStreamStatus` gains new fields for orchestration:

```java
// Existing fields (parent agent)
private String statusText;
private String textDelta;
private String thinkingDelta;
private boolean done;

// New fields (orchestration)
private List<ChildAgentStatus> childStatuses;  // null when orchestration off
```

```java
public class ChildAgentStatus implements Serializable {
    private String agentId;
    private String screenName;
    private String stepDescription;
    private String statusText;       // Current status line
    private boolean completed;
    private boolean failed;
}
```

Each child conversation polls its own StreamBuffer independently. The client's orchestration manager runs one polling timer per active child, calling `getRequestStatus(projectId, targetScreen)`. The server routes to the correct StreamBuffer based on the `targetScreen` parameter. The client aggregates statuses locally for the status card UI. No server-side aggregation is needed.

#### StreamBuffer Keying

Currently, `StreamBuffer` is keyed by `projectId`. For orchestration, child buffers use composite keys:

- Parent: `ai_stream:<projectId>` (unchanged)
- Child: `ai_stream:<projectId>:child:<screenName>`

The `getRequestStatus` RPC gains an optional `String targetScreen` parameter (see Server-Side Changes section). When null, it returns the parent's stream status. When non-null, it returns that child's stream status.

### Post-Completion

#### All Batches Approved (Happy Path)

When all children complete (`hasMore=false` for all) and all batches are approved, the orchestration manager:

1. Sends a summary to the parent agent via `continueWithToolResults`: "Plan executed successfully. All screens updated."
2. The parent LLM responds with a summary message shown in the chat.
3. The user can continue the conversation with the parent for follow-up changes.

#### Rejection (Partial Completion)

When the user rejects a batch, the orchestration manager sends context to the parent via `continueWithToolResults`:
- Which batches were applied (screen name + operation summaries)
- Which batch was rejected (screen name + operations + what the user said)
- Which children were cancelled

The parent can then:
- Propose a revised plan for the incomplete work
- Ask clarifying questions about what the user wanted instead
- Continue in Direct mode for targeted fixes

This maintains the parent's conversation continuity so it can learn from the user's feedback.

### RPC Changes

#### No New RPCs for Execution

Child conversations use the existing `processRequest` and `continueRequest` RPCs. The server distinguishes child requests via the `orchestrationMode` and `targetScreen` fields on `AIAgentRequest`.

#### Extended Request Fields

`AIAgentRequest` gains:
- `orchestrationMode` (boolean): when true, server uses `CHILD_EXECUTION` enforcement and screen-scoped StreamBuffer key.
- `targetScreen` (String): screen name for child conversations. Used to key the StreamBuffer as `ai_stream:<projectId>:child:<targetScreen>`.

#### Extended Streaming Poll

`getRequestStatus` returns `ChildAgentStatus` list when orchestration is active. No signature change needed -- `AIStreamStatus` is extended with nullable fields.

The client polls once for all children. `getRequestStatus` aggregates all child StreamBuffers for the given project. The client knows which screens have active children and maps statuses to status cards.

#### Conversation State Isolation

Each child conversation uses a separate Memcache key: `AI_CONV_CACHE_KEY_PREFIX + projectId + ":" + targetScreen`. This prevents children from clobbering the parent's or each other's conversation state.

### Conversation History

#### Parent Conversation

The parent agent's conversation is persisted as today (Memcache state + Datastore messages). Plan proposals and approvals are stored as regular messages. During plan execution, the parent stores:

- User message: the approved plan (structured content)
- Assistant message: per-step tool calls with results
- Post-approval: summary of what was applied/rejected

#### Child Conversations

Child agent conversations are **ephemeral** -- not persisted to Datastore. They exist only in memory during `executePlan`. Rationale:

- Children are short-lived (seconds to minutes)
- The parent's conversation captures the meaningful history (plan, results, feedback)
- Persisting N child conversations per plan execution would bloat Datastore
- If the user reloads the page, they see the parent conversation with plan history -- they don't need to replay child agent chats

The child's final AI message text IS stored in the parent's conversation (as part of the `ChildAgentResult` summary) so the user can see what each child said.

### Cancellation During Plan Execution

When the user cancels during plan execution (clicks Stop):

1. The client's orchestration manager cancels all active child conversations.
2. For each active child: calls `cancelRequest(projectId)` with the child's screen-specific Memcache key, setting the cancellation flag on that child's StreamBuffer (`ai_stream:<projectId>:child:<screenName>`).
3. Each child's LLM provider checks `streamBuffer.isCancelled()` during SSE streaming, same as the current single-agent cancel flow.
4. Any unapproved batches in the FIFO queue are discarded.
5. Batches that were already approved and applied remain applied (no rollback -- the user explicitly approved them).
6. The parent conversation stores a synthetic `[Plan execution cancelled]` message with a summary of what was applied before cancellation.

### Page Reload Behavior

Plan execution state does **not** survive a page reload. This matches the existing behavior where pending operations are lost on reload. After reloading:

- The parent conversation history is restored (plan proposal and approval are visible as text messages).
- Any in-progress child conversations are lost (the client managed them).
- Background editor changes from partially completed children are lost (editors reload from saved state).
- The user can re-ask the parent to re-plan if needed.

### Mode Enforcement Changes

#### Enforcement Context

`ModeEnforcer` gains awareness of orchestration state via a new `EnforcementContext` enum:

```java
public enum EnforcementContext {
    STANDARD,           // Current behavior (no orchestration)
    PLANNING,           // Only PROPOSE_PLAN + read-only tools
    CHILD_EXECUTION     // Screen-level ops only, no project-level, no PROPOSE_PLAN
}
```

- **STANDARD**: current behavior, unchanged. Used when orchestration is off or in Direct mode.
- **PLANNING**: only `PROPOSE_PLAN` + read-only tools allowed. All write operations rejected.
- **CHILD_EXECUTION**: only screen-level operations allowed. No project-level ops, no `PROPOSE_PLAN`.

Note: there is no `PARENT_EXECUTION` context. Project-level operations are extracted from the plan and executed by the client's `AIOperationExecutor` using the existing pipeline -- no separate LLM call needed.

The `enforce()` signature becomes `enforce(operations, mode, currentView, enforcementContext, errors)`. Existing callers pass `STANDARD`. The existing Advisor/ScreenEditor/ProjectEditor modes remain as the user-facing mode selection. Orchestration is an orthogonal execution strategy.

### Error Handling

#### Child Agent Failure

If a child agent fails (LLM error, timeout, parse error):

1. The child's result is marked `failed` with an error message.
2. Other children continue unaffected.
3. The combined response shows the failure. The user can:
   - Approve the successful screens and retry the failed one (rejection triggers parent retry).
   - Reject all and ask the parent to re-plan.

#### Plan Validation Errors

The server validates the plan before execution:

- All `screen` targets must be valid screen names or `__project__`.
- `depends_on` references must be valid step IDs.
- No circular dependencies.
- Project-level steps (`__project__`) cannot depend on screen-level steps.
- If the plan references screens that don't exist, those screens must be created by a prior `__project__` step.

Invalid plans are rejected with specific error messages, and the parent LLM is asked to fix the plan.

Note: `__project__` is added to the reserved screen names list in `ProjectOperationValidator` to prevent collision with the plan sentinel value.

#### Rate Limiting

See "Server-Side Changes" section for the full rate limiting strategy. In summary: child RPCs are exempt from the per-user rate limit when `orchestrationMode=true`. The client enforces a per-plan budget:

- **Max child conversations per plan**: 5. Plans with more screen steps are rejected at plan validation.
- **Max total RPCs per plan**: 20. If exceeded, remaining children are cancelled and partial results shown.

### Debug Logging

Child agents reuse the parent's debug session. When `ai.agent.debug` is enabled, each child writes to `<conversationId>/child-<screenName>-<timestamp>.txt` under the same log directory as the parent. This keeps all logs for a plan execution grouped together.

### What This Does NOT Change

- **AI modes (Advisor/ScreenEditor/ProjectEditor)**: unchanged. Orchestration is only available in ProjectEditor mode (since it inherently involves multi-screen work).
- **Tutorial mode**: Plan & Execute shows a confirmation warning when `TutorialURL` is set, but does not block it.
- **Single-screen plans**: if the plan has only one screen step (plus optional project-level steps), execution is sequential with the parent agent. No child conversations spawned. After the user approves the plan and any project ops are applied, the parent's enforcement context transitions from `PLANNING` to `STANDARD`, the full write tool set becomes available, and the parent continues via `continueWithToolResults` with the plan step as the user message. This is the same as the current flow, just preceded by a planning step.
- **Direct mode**: when the user selects "Direct" (or orchestration flag is off), the entire system behaves exactly as today.
- **Block validation**: child agents' WRITE_BLOCK operations go through the same dry-run validation pipeline. Validation failures trigger retries within the child agent (up to 5), not at the parent level.
- **Existing streaming**: the parent agent's text streaming continues as today. Child status cards are an additive UI element.
- **Screen navigation**: the user can freely switch between screens during plan execution. Child agents access editors via `ScreenExecutionContext` (by name from `editorMap`), not via `AIEditorState` (which resolves to the visible screen). Block operations on background screens use the save/restore `Blockly.common.setMainWorkspace()` pattern, which completes synchronously within GWT's single-threaded event loop. When the user navigates to a screen with approved changes, deferred rendering (`pendingRender`) kicks in and the changes become visible.
- **Manual edits during orchestration**: if the user manually edits a screen that a child agent is also targeting, the child's next continuation will pick up those manual changes in its fresh context. This may produce unexpected results. The UI should discourage (but not prevent) manual edits on screens with active child agents -- e.g., a subtle indicator on the screen tab showing "AI agent active."

### Implementation Risks (Investigate During Development)

These items are based on code analysis but need runtime verification:

1. **`initSvg()` on hidden workspace blocks**: `AI.YailToBlocks.convert()` calls `initSvg()` and `queueRender()` on all descendant blocks. `queueRender()` is correctly deferred for hidden workspaces via `pendingRender`. However, `initSvg()` is called directly and manipulates SVG DOM. For hidden workspaces (`display: none`), SVG methods like `getScreenCTM()` may return null. Verify that `initSvg()` does not fail or produce corrupt state on hidden blocks. The `pendingRender` mechanism will fix rendering when the workspace becomes visible, but `initSvg()` side effects need audit.

2. **`MockComponent.delete()` on detached widgets**: `AIDesignerOperations.executeDeleteComponent()` calls `comp.delete()` which calls `getContainer().removeComponent(this, true)`. For background form editors, the GWT widgets are in memory but not attached to the visible DOM. GWT widget operations on detached trees should work, but verify no layout-dependent calculations fail.

3. **`getBlocksYail()` on non-main workspaces**: calls `workspace.getBlocksYail()` on the specific workspace instance. Based on code analysis, this does not reference `Blockly.common.getMainWorkspace()` internally. Verify at runtime.

4. **`Blockly.renderManagement.triggerQueuedRenders()`**: called inside `convert()`, this is a global that processes ALL queued renders across ALL workspaces. In GWT's single-threaded model, two child conversations cannot interleave within a synchronous phase, but a `triggerQueuedRenders()` call in one child's callback may process renders queued by a previously-executed child's callback. This should not cause errors (rendering on hidden workspaces is deferred), but verify there are no side effects.

### Implementation Phases

This feature can be built incrementally:

**Phase A: Planning only (no multi-agent)**
- Add `ai.agent.orchestration` flag
- Add `propose_plan` tool and `PROPOSE_PLAN` operation type
- Add plan review UI (structured card, approve/reject)
- After approval, execute sequentially with the parent agent (existing flow, parent walks through plan steps one by one)
- Value: users get the plan/review cycle even without parallelism

**Phase B: Client-side parallel execution**
- Add `ScreenExecutionContext` to parameterize the entire execution pipeline by target screen
- Refactor `AIDesignerOperations`, `AIBlockOperations`, `AIOperationValidator`, `isIdempotentSkip()` to use `ScreenExecutionContext` instead of `AIEditorState` static lookups
- Add `AIOrchestrationManager` and `ChildConversation` client classes
- Add `AIContextCollector.buildRequestForScreen()` for background editor context
- Add `AIOperationExecutor.executeForScreen()` entry point
- Add `BlocklyPanel.doWriteBlockForScreen()` (audit whether save/restore mainWorkspace is needed)
- Add editor readiness check before starting child conversations
- Add FIFO operation batch queue with per-batch approval
- Add child StreamBuffer keying (`orchestrationMode` + `targetScreen` on request)
- Add status cards UI and combined per-screen approval preview
- Value: full parallel execution for multi-screen work

**Phase C: Retry and feedback loop**
- Add per-screen rejection handling via parent
- Add parent re-planning on rejection
- Add rate limit awareness for child agents
- Value: robust error recovery and iterative refinement

### Documentation Updates

When this feature is implemented, `CONTRIBUTING_AI.md` must be updated to document:
- New operation type (`PROPOSE_PLAN`)
- New enforcement context (`EnforcementContext` enum)
- `ScreenExecutionContext` and the parameterized execution pipeline
- `AIOrchestrationManager` and `ChildConversation` in the directory structure
- `orchestrationMode` and `targetScreen` fields on `AIAgentRequest`
- `ChildAgentStatus` streaming fields
- Child StreamBuffer keying scheme
- Background editor operation patterns
