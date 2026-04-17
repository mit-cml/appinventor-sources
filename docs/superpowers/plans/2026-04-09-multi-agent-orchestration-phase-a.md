# Multi-Agent Orchestration Phase A: Planning Only — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a Plan & Execute mode where the LLM proposes a structured plan before making changes, gated behind a feature flag. No multi-agent parallelism yet — after plan approval, the parent agent executes sequentially.

**Architecture:** New `propose_plan` tool with restricted tool set during planning. `PROPOSE_PLAN` operation type handled as a parser special case. `EnforcementContext` enum makes `ModeEnforcer` orchestration-aware. Client renders plan cards with approve/reject and executes approved plans sequentially through the existing single-agent flow.

**Tech Stack:** Java (GWT client + App Engine server), GWT-RPC, Memcache.

**Spec:** `docs/superpowers/specs/2026-04-07-multi-agent-orchestration-design.md`

**Scope:** Phase A only. Phases B (parallel execution) and C (retry/feedback) will be planned separately after Phase A ships.

---

### Task 1: Add `ai.agent.orchestration` feature flag

**Files:**
- Modify: `appinventor/appengine/war/WEB-INF/appengine-web.xml:210`
- Modify: `appinventor/appengine/src/com/google/appinventor/server/flags/Flag.java`

- [ ] **Step 1: Add flag to appengine-web.xml**

After line 210 (`ai.agent.debug`), add:

```xml
    <property name="ai.agent.orchestration" value="false" />
```

- [ ] **Step 2: Add Flag accessor in AIAgentEngine**

The `ai.agent.debug` flag lives in `AIDebug.java`, but the orchestration flag is consumed in `AIAgentEngine`. Add the flag declaration in `AIAgentEngine.java` near the other flag declarations (search for `Flag.createFlag`):

```java
private static final Flag<Boolean> ORCHESTRATION_FLAG =
    Flag.createFlag("ai.agent.orchestration", false);
```

- [ ] **Step 3: Commit**

```
feat(ai-agent): add ai.agent.orchestration feature flag
```

---

### Task 2: Add `PROPOSE_PLAN` operation type

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/shared/rpc/aiagent/AIOperation.java:39`

- [ ] **Step 1: Add PROPOSE_PLAN to the Type enum**

In `AIOperation.java`, after `TOGGLE_EDITOR` (line 39), add a comma and the new type:

```java
    TOGGLE_EDITOR,

    // Orchestration
    PROPOSE_PLAN
```

- [ ] **Step 2: Commit**

```
feat(ai-agent): add PROPOSE_PLAN operation type
```

---

### Task 3: Add `EnforcementContext` enum and update `ModeEnforcer`

**Files:**
- Create: `appinventor/appengine/src/com/google/appinventor/server/aiagent/EnforcementContext.java`
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/ModeEnforcer.java:81`

- [ ] **Step 1: Create EnforcementContext enum**

```java
package com.google.appinventor.server.aiagent;

/**
 * Controls which operations are allowed during different orchestration phases.
 * Orthogonal to the user-facing AI mode (Advisor/ScreenEditor/ProjectEditor).
 */
public enum EnforcementContext {
  /** Current behavior — no orchestration active. */
  STANDARD,
  /** Planning phase — only PROPOSE_PLAN and read-only tools allowed. */
  PLANNING,
  /** Child agent execution — screen-level ops only, no project-level or PROPOSE_PLAN. */
  CHILD_EXECUTION
}
```

- [ ] **Step 2: Update ModeEnforcer.enforce() signature**

At line 81 of `ModeEnforcer.java`, change the signature from:

```java
public static List<AIOperation> enforce(List<AIOperation> operations,
    String mode, String currentView, List<String> errors) {
```

to:

```java
public static List<AIOperation> enforce(List<AIOperation> operations,
    String mode, String currentView, EnforcementContext context,
    List<String> errors) {
```

- [ ] **Step 3: Add enforcement logic at the top of the method body**

Add before the existing mode checks (after the method opens):

```java
    if (context == EnforcementContext.PLANNING) {
      // In planning mode, reject all write operations. Only PROPOSE_PLAN passes through.
      List<AIOperation> planOnly = new ArrayList<>();
      for (AIOperation op : operations) {
        if (op.getType() == AIOperation.Type.PROPOSE_PLAN) {
          planOnly.add(op);
        } else if (WRITE_OPS.contains(op.getType())) {
          errors.add("Operation " + op.getType() + " is not allowed during planning.");
        } else {
          planOnly.add(op);
        }
      }
      return planOnly;
    }

    if (context == EnforcementContext.CHILD_EXECUTION) {
      // In child execution, reject project-level ops and PROPOSE_PLAN.
      List<AIOperation> screenOnly = new ArrayList<>();
      for (AIOperation op : operations) {
        if (op.getType() == AIOperation.Type.PROPOSE_PLAN
            || PROJECT_LEVEL_OPS.contains(op.getType())) {
          errors.add("Operation " + op.getType() + " is not allowed for child agents.");
        } else {
          screenOnly.add(op);
        }
      }
      operations = screenOnly;
      // Fall through to existing mode/view enforcement below
    }
```

- [ ] **Step 4: Add PROPOSE_PLAN to WRITE_OPS set (NOT SOLO_OPS)**

Find the `WRITE_OPS` set definition (static `EnumSet` near line 35). Rebuild it to include `AIOperation.Type.PROPOSE_PLAN`. This ensures PROPOSE_PLAN is blocked in Advisor mode (read-only). Do **NOT** add it to `SOLO_OPS` — its exclusivity is handled in `LLMResponseParser`, not here.

- [ ] **Step 5: Update `parseAndEnforce()` signature and all callers**

`ModeEnforcer.enforce()` is called from `AIAgentEngine.parseAndEnforce()`. Update `parseAndEnforce()` to accept `EnforcementContext` and pass it through:

```java
private ParsedResult parseAndEnforce(LLMResponse llmResponse, String mode,
    String currentView, EnforcementContext context) {
  // ... existing code ...
  List<AIOperation> accepted = ModeEnforcer.enforce(operations, mode, currentView, context, errors);
  // ...
}
```

Then update all 4 callers of `parseAndEnforce()` within `AIAgentEngine` to pass the appropriate context.

- [ ] **Step 6: Commit**

```
feat(ai-agent): add EnforcementContext enum and update ModeEnforcer
```

---

### Task 4: Add orchestration fields to `AIAgentRequest`

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/shared/rpc/aiagent/AIAgentRequest.java:30`

- [ ] **Step 1: Add fields after line 30**

```java
  private boolean orchestrationMode;
  private String targetScreen;
  private boolean planExecuteMode;
```

- [ ] **Step 2: Add getters and setters**

Add after the existing getter/setter block:

```java
  public boolean isOrchestrationMode() {
    return orchestrationMode;
  }

  public void setOrchestrationMode(boolean orchestrationMode) {
    this.orchestrationMode = orchestrationMode;
  }

  public String getTargetScreen() {
    return targetScreen;
  }

  public void setTargetScreen(String targetScreen) {
    this.targetScreen = targetScreen;
  }

  public boolean isPlanExecuteMode() {
    return planExecuteMode;
  }

  public void setPlanExecuteMode(boolean planExecuteMode) {
    this.planExecuteMode = planExecuteMode;
  }
```

- [ ] **Step 3: Commit**

```
feat(ai-agent): add orchestration fields to AIAgentRequest
```

---

### Task 5: Add `propose_plan` tool to `AIContextBuilder`

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/AIContextBuilder.java:200`

- [ ] **Step 1: Update buildTools() signature**

At line 200, change from:

```java
public List<LLMTool> buildTools(String mode, String currentView) {
```

to:

```java
public List<LLMTool> buildTools(String mode, String currentView,
    EnforcementContext context) {
```

- [ ] **Step 2: Add `PROPOSE_PLAN` constant to AIToolNames**

In `AIToolNames.java`, add:

```java
  public static final String PROPOSE_PLAN = "propose_plan";
```

- [ ] **Step 3: Add planning tool set at the top of the method**

After `List<LLMTool> tools = new ArrayList<>();` (line 201), add a planning-mode early return. The existing lookup tools are constructed inline (not via separate methods), so copy their construction pattern:

```java
    if (context == EnforcementContext.PLANNING) {
      // Planning mode: only read-only tools + propose_plan
      // Copy the existing lookup_component and lookup_screen tool construction from below
      tools.add(new LLMTool(AIToolNames.LOOKUP_COMPONENT, /* existing description */,
          /* existing parameters JSON string */));
      tools.add(new LLMTool(AIToolNames.LOOKUP_SCREEN, /* existing description */,
          /* existing parameters JSON string */));
      tools.add(buildProposePlanTool());
      return tools;
    }
```

Alternatively, extract the existing lookup tool construction into private methods first, then call them here. Either approach works — follow whichever keeps the code DRY.

- [ ] **Step 4: Add the propose_plan tool builder method**

`LLMTool` constructor takes `(String name, String description, String parametersJson)` — the third arg is a JSON string, not a `Map`. Build the parameters as a JSON string:

```java
  private LLMTool buildProposePlanTool() {
    String parameters = "{"
        + "\"type\":\"object\","
        + "\"properties\":{"
        + "  \"summary\":{\"type\":\"string\","
        + "    \"description\":\"Brief overall description of what the plan accomplishes\"},"
        + "  \"steps\":{\"type\":\"array\","
        + "    \"items\":{\"type\":\"object\","
        + "      \"properties\":{"
        + "        \"id\":{\"type\":\"string\",\"description\":\"Unique step identifier (e.g. s1, s2)\"},"
        + "        \"screen\":{\"type\":\"string\",\"description\":\"Target screen name. Use __project__ for project-level operations\"},"
        + "        \"description\":{\"type\":\"string\",\"description\":\"What this step will do\"},"
        + "        \"depends_on\":{\"type\":\"array\",\"items\":{\"type\":\"string\"},"
        + "          \"description\":\"Step IDs that must complete before this step starts\"}"
        + "      },\"required\":[\"id\",\"screen\",\"description\"]"
        + "    }"
        + "  }"
        + "},\"required\":[\"summary\",\"steps\"]"
        + "}";
    return new LLMTool(
        AIToolNames.PROPOSE_PLAN,
        "Propose an execution plan for the user to review before making changes. "
            + "Each step targets a specific screen and describes what will be done. "
            + "Steps targeting different screens can execute in parallel. "
            + "Use lookup_component and lookup_screen first to research the project, "
            + "then propose a complete plan.",
        parameters);
  }
```

- [ ] **Step 4: Update all callers of buildTools() to pass context**

Search for `buildTools(` in `AIAgentEngine.java`. Each call needs the `EnforcementContext` parameter. For Phase A, all existing calls pass `EnforcementContext.STANDARD`. The planning context is passed when `request.isPlanExecuteMode()` is true and this is the initial request (not a continuation).

- [ ] **Step 5: Commit**

```
feat(ai-agent): add propose_plan tool to AIContextBuilder
```

---

### Task 6: Handle `propose_plan` in `LLMResponseParser`

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/LLMResponseParser.java:120`

- [ ] **Step 1: Add propose_plan to the known tools map**

Find the `KNOWN_TOOLS` map (or equivalent tool name validation). Add `"propose_plan"` as a known tool that maps to `AIOperation.Type.PROPOSE_PLAN`.

- [ ] **Step 2: Add special-case handling in parseToolCalls()**

At the start of the `parseToolCalls()` method (after line 120), before the main loop, add a pre-scan for `propose_plan`:

```java
    // Special case: if propose_plan is present, it's the only operation returned.
    // This is a parser-level rule, not a ModeEnforcer solo-op rule.
    boolean hasProposePlan = false;
    for (RawToolCall call : rawToolCalls) {
      if (AIToolNames.PROPOSE_PLAN.equals(call.getName())) {
        hasProposePlan = true;
        break;
      }
    }
    if (hasProposePlan) {
      for (RawToolCall call : rawToolCalls) {
        if (AIToolNames.PROPOSE_PLAN.equals(call.getName())) {
          String validationError = validatePlanStructure(call.getArgumentsJson());
          if (validationError != null) {
            errors.add("propose_plan: " + validationError);
          } else {
            operations.add(new AIOperation(AIOperation.Type.PROPOSE_PLAN,
                call.getArgumentsJson()));
          }
          break; // Only one propose_plan, ignore other tool calls
        }
      }
      return new ParseResult(operations, errors);
    }
```

Note: `ParseResult` takes 2 arguments `(operations, errors)`. Tool call status tracking happens in `AIAgentEngine.parseAndEnforce()`, not here.

- [ ] **Step 3: Add plan validation method**

Use `org.json` (the codebase standard), not Gson:

```java
  private String validatePlanStructure(String json) {
    try {
      JSONObject plan = new JSONObject(json);
      if (!plan.has("summary")) {
        return "Missing 'summary' field.";
      }
      if (!plan.has("steps")) {
        return "Missing 'steps' array.";
      }
      JSONArray steps = plan.getJSONArray("steps");
      if (steps.length() == 0) {
        return "Plan must have at least one step.";
      }
      Set<String> stepIds = new HashSet<>();
      Map<String, List<String>> deps = new HashMap<>();
      for (int i = 0; i < steps.length(); i++) {
        JSONObject step = steps.getJSONObject(i);
        if (!step.has("id") || !step.has("screen") || !step.has("description")) {
          return "Step " + i + " missing required field (id, screen, description).";
        }
        String id = step.getString("id");
        if (!stepIds.add(id)) {
          return "Duplicate step ID: " + id;
        }
        deps.put(id, new ArrayList<>());
        if (step.has("depends_on")) {
          JSONArray depArray = step.getJSONArray("depends_on");
          for (int j = 0; j < depArray.length(); j++) {
            deps.get(id).add(depArray.getString(j));
          }
        }
      }
      // Validate depends_on references exist
      for (Map.Entry<String, List<String>> entry : deps.entrySet()) {
        for (String dep : entry.getValue()) {
          if (!stepIds.contains(dep)) {
            return "Step " + entry.getKey() + " depends on unknown step: " + dep;
          }
        }
      }
      // Detect circular dependencies via topological sort
      Set<String> visited = new HashSet<>();
      Set<String> inStack = new HashSet<>();
      for (String id : stepIds) {
        if (hasCycle(id, deps, visited, inStack)) {
          return "Circular dependency detected involving step: " + id;
        }
      }
      return null; // Valid
    } catch (Exception e) {
      return "Invalid JSON: " + e.getMessage();
    }
  }

  private boolean hasCycle(String id, Map<String, List<String>> deps,
      Set<String> visited, Set<String> inStack) {
    if (inStack.contains(id)) return true;
    if (visited.contains(id)) return false;
    visited.add(id);
    inStack.add(id);
    for (String dep : deps.getOrDefault(id, Collections.emptyList())) {
      if (hasCycle(dep, deps, visited, inStack)) return true;
    }
    inStack.remove(id);
    return false;
  }
```

- [ ] **Step 4: Set tool call result for propose_plan in AIAgentEngine**

In `AIAgentEngine.java`, find where `annotateToolCallResults()` is called. For `PROPOSE_PLAN` operations, the tool result should be `"Plan delivered to user for review."`. Find the method that builds tool call results and add a case for `PROPOSE_PLAN`.

- [ ] **Step 5: Commit**

```
feat(ai-agent): handle propose_plan as parser special case
```

---

### Task 7: Thread orchestration fields through RPC layer and route planning mode

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/AIAgentServiceImpl.java:80`
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/AIAgentEngine.java:150`

- [ ] **Step 1: Pass planExecuteMode from AIAgentServiceImpl to AIAgentEngine**

`AIAgentEngine.processRequest()` takes individual primitive parameters, NOT an `AIAgentRequest`. The unpacking happens in `AIAgentServiceImpl.processRequest()` (line 80). Add the new field to the call chain.

In `AIAgentServiceImpl.processRequest()`, extract the new field from the request and pass it to the engine:

```java
    boolean planExecuteMode = request.isPlanExecuteMode();
```

Then update the `engine.processRequest(...)` call to include the new parameter.

- [ ] **Step 2: Update AIAgentEngine.processRequest() signature**

Add `boolean planExecuteMode` parameter to the signature (line 150):

```java
public AIAgentResponse processRequest(String userId, long projectId, String screenName,
    String userMessage, String blocksYail, String currentView, String mode,
    String screenComponentsJson, String projectSnapshot, String blockWarnings,
    String locale, String languageDisplayName, boolean isPlatformMessage,
    boolean planExecuteMode) {
```

- [ ] **Step 3: Determine enforcement context from the new parameter**

Inside `processRequest()`, after existing setup code:

```java
    EnforcementContext enforcementContext = EnforcementContext.STANDARD;
    if (ORCHESTRATION_FLAG.get() && planExecuteMode) {
      enforcementContext = EnforcementContext.PLANNING;
    }
```

- [ ] **Step 4: Pass context through to buildTools() and parseAndEnforce()**

Update the `buildTools()` call to pass `enforcementContext`. Update `parseAndEnforce()` calls to pass `enforcementContext` (all 4 call sites within the engine).

- [ ] **Step 5: Do the same for continueRequest()**

Apply the same pattern: add `planExecuteMode` to `AIAgentServiceImpl.continueRequest()` → `AIAgentEngine.continueRequest()`. For continuations after a plan is approved, the request has `planExecuteMode=false`, so the context will be `STANDARD`.

- [ ] **Step 6: Commit**

```
feat(ai-agent): thread orchestration fields through RPC layer
```

---

### Task 8: Add planning instructions to system prompt context

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/context/ModeModule.java`

- [ ] **Step 1: Find where mode-specific instructions are built**

Read `ModeModule.java` to understand how mode instructions are generated. Find the method that builds the mode text.

- [ ] **Step 2: Add planning-mode instructions**

When `enforcementContext == PLANNING`, the mode instructions should tell the LLM to:
- Research the project using `lookup_component` and `lookup_screen`
- Propose a structured plan using `propose_plan`
- NOT attempt to make changes directly
- Group steps by screen and identify dependencies

Add the planning instructions as a conditional block. The exact insertion point depends on how `ModeModule` is structured — add the planning text alongside existing mode instructions.

Example text:

```
You are in Plan & Execute mode. Your task is to research the project and propose a structured execution plan.

Available tools: lookup_component (research component specs), lookup_screen (research screen state), propose_plan (submit your plan).

DO NOT attempt to add components, write blocks, or make any changes. Instead:
1. Use lookup_component and lookup_screen to understand the current project state.
2. Break the user's request into steps, each targeting a specific screen.
3. Use '__project__' as the screen for project-level operations (creating screens, setting project properties).
4. Set depends_on when a step requires another to complete first (e.g., a screen must be created before components can be added to it).
5. Call propose_plan with your complete plan.
```

- [ ] **Step 3: Update ContextParams to carry EnforcementContext**

`ModeModule.build()` receives a `ContextParams` value class. Add `EnforcementContext` field to `ContextParams.java`:

```java
  private final EnforcementContext enforcementContext;
```

Update the constructor and add a getter. Update all `ContextParams` construction sites in `AIContextBuilder.buildContextMessages()` to pass the enforcement context.

- [ ] **Step 4: Pass enforcementContext to ModeModule**

Update `ModeModule.build()` to read `params.getEnforcementContext()` and conditionally return planning-mode instructions instead of the normal mode text.

- [ ] **Step 4: Commit**

```
feat(ai-agent): add planning-mode instructions to system prompt
```

---

### Task 9: Client — Add Plan & Execute mode toggle

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/AIEditorState.java`
- Modify: the AI chat dialog class (find by searching for the AI chat UI — likely `AIChatDialog.java` or similar)
- Modify: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/AIContextCollector.java`

- [ ] **Step 1: Add planExecuteMode state to AIEditorState**

In `AIEditorState.java`, add a static field and accessors:

```java
  private static boolean planExecuteMode = false;

  public static boolean isPlanExecuteMode() {
    return planExecuteMode;
  }

  public static void setPlanExecuteMode(boolean enabled) {
    planExecuteMode = enabled;
  }
```

- [ ] **Step 2: Add toggle UI to the chat dialog**

Find the AI chat dialog class. Add a toggle (checkbox or segmented button) that shows "Direct | Plan & Execute" when:
- The `ai.agent.orchestration` flag is enabled (check via a server config RPC or a flag passed to the client)
- The current AI mode is ProjectEditor

When the toggle changes, call `AIEditorState.setPlanExecuteMode(enabled)`.

When the AI mode changes away from ProjectEditor, reset to Direct: `AIEditorState.setPlanExecuteMode(false)` and hide the toggle.

- [ ] **Step 3: Add tutorial mode confirmation**

When the user selects Plan & Execute and the project has a non-empty `TutorialURL`, show a confirmation dialog:

```
"This project uses a tutorial. Plan & Execute may skip pedagogical steps. Continue?"
[Continue] [Cancel]
```

On Cancel, revert the toggle to Direct.

- [ ] **Step 4: Set planExecuteMode on AIAgentRequest**

In `AIContextCollector.buildRequest()`, add:

```java
  request.setPlanExecuteMode(AIEditorState.isPlanExecuteMode());
```

- [ ] **Step 5: Commit**

```
feat(ai-agent): add Plan & Execute mode toggle to chat UI
```

---

### Task 10: Client — Render plan card and approve/reject

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/AIResponseOrchestrator.java:558`
- Modify: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/AIChatRenderer.java`
- Modify: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/AIOperationFormatter.java`

- [ ] **Step 1: Detect PROPOSE_PLAN in handleResponse()**

In `AIResponseOrchestrator.handleResponse()` (line 558), before the existing operation preview logic (line 590), add a check:

```java
    if (hasOps && response.getOperations().size() == 1
        && response.getOperations().get(0).getType() == AIOperation.Type.PROPOSE_PLAN) {
      handlePlanProposal(response);
      return;
    }
```

- [ ] **Step 2: Add handlePlanProposal() method**

```java
  private void handlePlanProposal(AIAgentResponse response) {
    requestInFlight = false;
    AIOperation planOp = response.getOperations().get(0);
    String planJson = planOp.getPayload();

    // Render the plan card in chat
    callback.renderPlanCard(planJson, new PlanApprovalCallback() {
      @Override
      public void onApprove(String approvedPlanJson) {
        executePlanSequentially(approvedPlanJson);
      }

      @Override
      public void onReject() {
        rejectOperations(); // Existing rejection flow
      }
    });
  }
```

- [ ] **Step 3: Add plan card rendering to AIChatRenderer**

Add a `renderPlanCard(String planJson, PlanApprovalCallback callback)` method that:
- Parses the plan JSON (summary + steps array)
- Creates a styled card with:
  - Summary text at top
  - Each step as a row: `[step.id] [step.screen] — [step.description]`
  - Dependency indicators (e.g., "depends on: s1")
- Three buttons: **Approve**, **Edit & Approve**, **Reject**
- Approve calls `callback.onApprove(planJson)`
- Reject calls `callback.onReject()`
- Edit & Approve opens a textarea with the plan JSON for manual editing, then calls `callback.onApprove(editedJson)`

- [ ] **Step 4: Add PlanApprovalCallback interface**

```java
public interface PlanApprovalCallback {
  void onApprove(String approvedPlanJson);
  void onReject();
}
```

Place this in the aiagent package alongside existing callback interfaces.

- [ ] **Step 5: Add PROPOSE_PLAN formatting to AIOperationFormatter**

In `AIOperationFormatter`, add a case for `PROPOSE_PLAN` that returns a human-readable summary like "Proposed execution plan (N steps)".

- [ ] **Step 6: Commit**

```
feat(ai-agent): render plan card with approve/reject in chat
```

---

### Task 11: Client — Execute approved plan sequentially (Phase A)

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/AIResponseOrchestrator.java`

- [ ] **Step 1: Add executePlanSequentially() method**

This is the Phase A execution path — no parallelism, the parent agent walks through steps one by one:

```java
  private void executePlanSequentially(String planJson) {
    // Parse the plan
    // Extract project-level steps (__project__) as AIOperations
    // Extract screen-level steps for sequential execution

    // If project-level steps exist:
    //   Convert to standard AIOperations (CREATE_SCREEN, SET_PROJECT_PROP, etc.)
    //   Show operation preview for project ops
    //   After user approves project ops and they're applied,
    //   continue with screen-level steps

    // For screen-level steps (Phase A: sequential):
    //   Send a continuation to the parent agent with the first step as the user message
    //   The parent's enforcement context transitions from PLANNING to STANDARD
    //   The parent executes with full write tools
    //   The normal hasMore/continuation flow handles multi-step execution

    // Implementation: send the approved plan as a new platform message via processRequest.
    // This is a new user message, not a continuation (the plan proposal was the
    // previous turn). The server's parent agent receives it with full write tools
    // (planExecuteMode=false) and begins executing step by step.
    String planMessage = "<system>\nThe user approved the following plan. "
        + "Execute it step by step, one screen at a time. "
        + "Use switch_screen to navigate between screens as needed.\n\n"
        + planJson + "\n</system>";

    requestInFlight = true;
    AIAgentRequest request = contextCollector.buildRequest(planMessage);
    request.setPlatformMessage(true);
    request.setPlanExecuteMode(false); // No longer in planning mode — executing now

    startPollingStatus();
    aiAgentService.processRequest(request, new AsyncCallback<AIAgentResponse>() {
      // ... standard response handling via handleResponseWithValidation()
    });
  }
```

- [ ] **Step 2: Handle project-level step extraction**

Add a helper that parses the plan JSON and identifies `__project__` steps:

```java
  private List<PlanStep> parsePlan(String planJson) {
    // Parse JSON into List<PlanStep> objects
    // PlanStep: { id, screen, description, dependsOn }
  }

  private List<AIOperation> extractProjectOperations(List<PlanStep> steps) {
    List<AIOperation> ops = new ArrayList<>();
    for (PlanStep step : steps) {
      if ("__project__".equals(step.screen)) {
        // Parse step.description to determine operation type
        // For "Create ScreenX" -> AIOperation(CREATE_SCREEN, {"screenName": "ScreenX"})
        // This is a best-effort extraction; the parent agent will also handle these
        // In Phase A, we send the whole plan to the parent and let it handle everything
      }
    }
    return ops;
  }
```

Note: For Phase A simplicity, we can skip client-side project op extraction and just send the entire approved plan to the parent agent as a platform message. The parent agent (with full tools including `create_screen`, `switch_screen`) handles everything sequentially. The client-side extraction is a Phase B optimization.

- [ ] **Step 3: Commit**

```
feat(ai-agent): execute approved plans sequentially via parent agent
```

---

### Task 12: Add `__project__` to reserved screen names

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/LLMResponseParser.java` (or `ProjectOperationValidator.java` — find where screen names are validated for CREATE_SCREEN)

- [ ] **Step 1: Find screen name validation**

Search for where `CREATE_SCREEN` operations validate the screen name. Add `"__project__"` as a reserved name that cannot be used for new screens.

- [ ] **Step 2: Add the check**

```java
  if ("__project__".equals(screenName)) {
    return "Screen name '__project__' is reserved.";
  }
```

- [ ] **Step 3: Commit**

```
feat(ai-agent): reserve __project__ as screen name
```

---

### Task 13: Integration test — end-to-end planning flow

**Files:**
- This is a manual test checklist since the codebase uses manual testing for GWT client + server integration.

- [ ] **Step 1: Verify feature flag off**

Set `ai.agent.orchestration` to `false` in `appengine-web.xml`. Confirm:
- No Plan & Execute toggle visible in the chat dialog
- The `propose_plan` tool is not sent to the LLM
- The system behaves identically to before

- [ ] **Step 2: Verify feature flag on, non-ProjectEditor mode**

Set flag to `true`. Select Advisor or ScreenEditor mode. Confirm:
- No Plan & Execute toggle visible

- [ ] **Step 3: Verify Plan & Execute toggle in ProjectEditor mode**

Select ProjectEditor mode. Confirm:
- Toggle is visible: "Direct | Plan & Execute"
- Default is Direct
- Switching to Plan & Execute updates AIEditorState

- [ ] **Step 4: Verify planning tool set**

In Plan & Execute mode, send a message. Verify (via debug logs):
- LLM receives only `lookup_component`, `lookup_screen`, `propose_plan` tools
- LLM does NOT receive write tools

- [ ] **Step 5: Verify plan proposal rendering**

When the LLM calls `propose_plan`, verify:
- Plan card renders in chat with summary and steps
- Approve/Reject buttons are visible
- Reject sends rejection feedback to the LLM

- [ ] **Step 6: Verify plan approval and sequential execution**

Click Approve. Verify:
- The approved plan is sent to the parent agent as a continuation
- The parent agent begins executing with full tools
- Operations appear for approval as normal (single-agent flow)
- The parent navigates between screens as needed

- [ ] **Step 7: Verify tutorial mode warning**

Set a TutorialURL on the project. Switch to Plan & Execute. Verify:
- Confirmation dialog appears
- Cancel reverts to Direct mode
- Continue allows Plan & Execute

- [ ] **Step 8: Commit**

```
test(ai-agent): verify Phase A planning flow end-to-end
```

---

## Phase B and C Planning

Phases B (client-side parallel execution) and C (retry/feedback loop) will be planned as separate documents after Phase A is shipped and validated. Key dependencies from Phase A:

- `EnforcementContext.CHILD_EXECUTION` is defined but unused until Phase B
- `AIAgentRequest.orchestrationMode` and `targetScreen` fields exist but are unused until Phase B
- The plan card UI and approval flow form the foundation for Phase B's FIFO queue

Phase B will require its own plan covering:
- `ScreenExecutionContext` refactor (11 call sites across 4 files)
- `AIOrchestrationManager` and `ChildConversation` new classes
- `StorageIo` screen-scoped overloads (conversation state, stream buffer, cancellation)
- Background editor operations (Blockly save/restore audit)
- FIFO operation batch queue
- Status cards UI
- Rate limiting changes
