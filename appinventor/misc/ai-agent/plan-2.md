# Plan: YAIL-Based AI Agent Block System

## Goal

Replace the pseudocode intermediary language with YAIL for the AI agent system. The architecture is **client-heavy, server-thin**: the client handles all Blockly-to-YAIL and YAIL-to-Blockly translation using the Blockly runtime it already has, while the server acts as a relay and lightweight validator between the client and the LLM.

## Why YAIL Over Pseudocode

1. **Unambiguous syntax** -- S-expressions have zero parsing ambiguity (no operator precedence, no indentation sensitivity)
2. **LLMs know Scheme** -- trained on significant Lisp/Scheme corpora
3. **Deterministic mapping** -- every Blockly block maps to exactly one YAIL pattern; reverse is a lookup table
4. **Already exists** -- client-side YAIL generators (`blocklyeditor/src/generators/yail/*.js`) are complete and maintained; no need to invent a custom grammar
5. **No server-side reimplementation** -- the client already has the Blockly runtime and YAIL generators loaded; leveraging them avoids writing ~3,150 lines of Java translation code on the server

## Why Client-Side Translation

The Blockly YAIL generators (`yail.js`, `yail/*.js`) are tightly coupled to the Blockly runtime. They depend on block object methods (`getFieldValue()`, `getInput()`), workspace state (`getMainWorkspace()`), and the component database -- none of which exist on the server. The server runs Java 17 on App Engine with no JavaScript engine (Nashorn was removed in Java 15; GraalJS would add ~30MB of dependencies and significant overhead). JSNI is a GWT client-side mechanism that compiles Java to JS for the browser, not a way to run JS on the server.

The existing `BlocksPseudocodeGenerator.java` proves that reimplementing block translation in pure Java is possible (~1,447 lines), but doing this for YAIL is unnecessary when the client already has the authoritative generators. Sending the client-generated YAIL with the request eliminates the entire server-side "read path."

For the "write path" (LLM-generated YAIL back to blocks), the client again has the advantage: it can create blocks programmatically via the Blockly API (`workspace.newBlock()`, `setFieldValue()`, connection APIs), letting Blockly itself enforce validity. This eliminates the need for a server-side YAIL parser and XML generator.

---

## Architecture Overview

```
CLIENT                              SERVER                          LLM
======                              ======                          ===

[User types message]
        |
[Generate blocks YAIL
 via existing Blockly generators]
        |
[Send (message, blocksYail,  -----> [Receive request]
 projectId, screenName)]            [Build LLM context using
                                     blocksYail directly]
                                    [Build tools list]
                                    [Load conversation history]
                                            |
                                    [Call LLM] ---------------> [LLM generates
                                            |                    tool calls with
                                    [Receive response] <-------  YAIL bodies]
                                            |
                                    [Parse tool calls]
                                    [Validate metadata:
                                     - skim YAIL heads
                                     - check component/event
                                       existence
                                     - mode restrictions]
                                            |
                                     FAIL? retry LLM
                                     immediately (tight loop)
                                            |
                                     PASS? send operations
                                     with raw YAIL to client
                                            |
[Receive operations] <------------- [Send response]
[For write_block ops:
 - Parse S-expression (JS)
 - Walk AST
 - Create Blockly blocks
   programmatically]
[For delete_block ops:
 - Parse identifier
 - Find and remove block]
        |
 FAIL? report error ------------>  [Receive error feedback]
 to server for retry               [Retry LLM with error context]
        |
 PASS? blocks appear
 in workspace
        |
[User sees result]
```

---

## Root Block Types

In App Inventor's Blockly, every block has one of three connection shapes:

- **Statement blocks**: have `previousConnection` (notch on top) -- chain inside other blocks
- **Expression blocks**: have `outputConnection` (plug on left) -- fit into inputs
- **Root blocks**: have **neither** -- can only sit at the workspace top level

Only four block types are structurally root blocks:

| Block type | YAIL form | Identity key | Uniqueness |
|---|---|---|---|
| `component_event` | `(define-event Component Event ...)` | `(component, event)` | One handler per component+event pair (enforced by Blockly) |
| `global_declaration` | `(def g$name ...)` | `name` | Variable names are unique |
| `procedures_defnoreturn` | `(def (p$name ...) ...)` | `name` | Procedure names are unique (shared namespace with defreturn) |
| `procedures_defreturn` | `(def (p$name ...) ...)` | `name` | Same namespace as above |

This is confirmed by `Blockly.Component.buildComponentMap()` which partitions the workspace into exactly `globals` (variables + procedures) and `components[name]` (event handlers). Everything else is nested inside these roots or is an orphaned block.

---

## LLM Tool Design

### Block tools (client-validated via YAIL)

**`write_block(yail: string)`** -- Create or replace a top-level block.

The YAIL form head identifies the block type and target. Upsert semantics: if a block with the same identity exists, replace it entirely; if not, create it.

```
write_block(yail="(define-event Button1 Click () (set-this-form)
  (call-component-method 'Notifier1 'ShowAlert
    (*list-for-runtime* \"Hello\") '(text)))")

write_block(yail="(def g$score 0)")

write_block(yail="(def (p$factorial $n)
  (if (call-yail-primitive yail-equal? (*list-for-runtime* (lexical-value $n) 0) '(number number))
    (begin 1)
    (begin (call-yail-primitive * (*list-for-runtime*
      (lexical-value $n)
      ((get-var p$factorial) (call-yail-primitive - (*list-for-runtime* (lexical-value $n) 1) '(number number))))
      '(number number)))))")
```

**`delete_block(block: string)`** -- Delete a top-level block.

The identifier format matches the YAIL head tokens (the first tokens of a `write_block` YAIL form):

```
delete_block(block="define-event Button1 Click")
delete_block(block="def g$score")
delete_block(block="def p$factorial")
```

### Designer tools (server-validated, no YAIL)

These are unchanged from the current system:

- `add_component(component_type, name, parent, properties)`
- `delete_component(name)`
- `set_property(component, property, value)`
- `rename_component(old_name, new_name)`

### Screen tools (server-validated)

- `create_screen(name)`
- `delete_screen(name)`
- `switch_screen(name)`

### Read-only tools

- `lookup_component(name)` -- returns component details
- `lookup_screen(name)` -- returns screen details

---

## Validation Architecture

### Server-side validation (lightweight, no full YAIL parsing)

For **all operations**:
- Well-formed JSON tool call with required fields
- Mode restrictions (Advisor can't write, ScreenEditor can't create screens, etc.)
- Rate limiting, authorization

For **`write_block`**:
- Skim first few tokens of the YAIL string to extract the form head:
  - `define-event` + component name + event name
  - `def` + `g$name` (global variable)
  - `def` + `(p$name` (procedure)
- Validate: component exists, event exists for that component type, variable/procedure name is valid
- Do NOT parse the YAIL body -- pass it through as-is

For **`delete_block`**:
- Parse the identifier string (same format as YAIL head tokens)
- Validate: target block exists

For **designer tools**:
- Component type exists in catalog
- Property is valid for the component type
- Parent component exists

**On validation failure**: retry the LLM immediately with error feedback (tight server-side loop, no client round trip).

### Client-side validation (full Blockly runtime)

For **`write_block`**:
1. Parse the full YAIL string as S-expressions
2. Walk the AST and create Blockly blocks programmatically using the Blockly API
3. Validate: block types exist, connections are compatible, field values are legal
4. If successful: blocks appear in the workspace
5. If failed: report specific error to server (e.g., "Unknown primitive 'string-contians' -- did you mean 'string-contains'?")

For **`delete_block`**:
1. Parse the identifier to determine block type + identity key
2. Find the matching top-level block in the workspace
3. Remove it

**On client validation failure**: send error report to server, server retries the LLM with the error as context. This adds one network round trip per retry, but LLM syntax errors should be infrequent relative to LLM call latency.

---

## New Client-Side JavaScript Files

All under `appinventor/blocklyeditor/src/`:

### 1. `generators/yail_blocks_export.js` (~30 lines)

New method on `Blockly.WorkspaceSvg` that generates YAIL for just the blocks (no form scaffolding, no `define-form`, no `add-component`, no `init-runtime`):

```javascript
Blockly.WorkspaceSvg.prototype.getBlocksYail = function() {
  var componentMap = Blockly.Component.buildComponentMap([], [], false, false);
  var code = [];

  // Global variables and procedures
  for (var i = 0, block; block = componentMap.globals[i]; i++) {
    code.push(AI.Yail.blockToCode(block));
  }

  // Event handlers, organized by component
  for (var componentName in componentMap.components) {
    var blocks = componentMap.components[componentName];
    for (var i = 0; i < blocks.length; i++) {
      code.push(AI.Yail.blockToCode(blocks[i]));
    }
  }

  return code.join('\n\n');
};
```

### 2. `ai/sexpr_parser.js` (~100-150 lines)

Tokenizer + recursive descent parser for Scheme S-expressions in JavaScript.

**Token types:** `LPAREN`, `RPAREN`, `QUOTE`, `STRING`, `NUMBER`, `HASH_T`, `HASH_F`, `SYMBOL`, `EOF`

**AST node types:**

```
SExpr
  ├── Symbol   { type: 'symbol', name: string }
  ├── Str      { type: 'string', value: string }
  ├── Num      { type: 'number', value: number, raw: string }
  ├── Bool     { type: 'boolean', value: boolean }
  ├── List     { type: 'list', elements: SExpr[] }
  └── Quoted   { type: 'quoted', inner: SExpr }
```

**Grammar:**
```
sexpr := '(' sexpr* ')'   → List
       | '\'' sexpr        → Quoted
       | STRING            → Str
       | NUMBER            → Num
       | '#t' | '#f'       → Bool
       | SYMBOL            → Symbol
```

**Must handle:** Scheme identifiers with special chars (`yail-divide`, `set-var!`, `string-empty?`, `$varName`, `g$name`, `*list-for-runtime*`), negative numbers, `#x` hex literals, embedded newlines in strings.

**Public API:** `AI.SExprParser.parseAll(input) → SExpr[]`

### 3. `ai/yail_to_blocks.js` (~800-1,200 lines)

Walks parsed S-expression AST and creates Blockly blocks programmatically.

**Public API:**
```javascript
AI.YailToBlocks.convert(workspace, yailString)
// Returns: { success: boolean, error: string|null, blockId: string|null }
```

**Core dispatch** on first symbol of each top-level S-expression list:

| YAIL head | Handler | Blockly block(s) created |
|---|---|---|
| `define-event` | `convertEventHandler` | `component_event` + body |
| `def` (with `g$` symbol) | `convertGlobalVar` | `global_declaration` + init value |
| `def` (with `(p$name ...)` list) | `convertProcedure` | `procedures_defnoreturn` or `procedures_defreturn` + body |

**Expression/statement dispatch** within bodies, on first symbol of nested lists:

| YAIL head | Block type |
|---|---|
| `call-yail-primitive` | Dispatches via primitive map (130+ entries, see below) |
| `call-component-method` | `component_method` |
| `call-component-type-method` | `component_method` (generic) |
| `get-property` | `component_set_get` (get) |
| `set-and-coerce-property!` | `component_set_get` (set) |
| `get-var` | `lexical_variable_get` (global) |
| `set-var!` | `lexical_variable_set` (global) |
| `lexical-value` | `lexical_variable_get` (local) |
| `set-lexical!` | `lexical_variable_set` (local) |
| `if` | `controls_if` or `controls_choose` |
| `while` | `controls_while` |
| `foreach` | `controls_forEach` / `controls_for_each_dict` |
| `forrange` | `controls_forRange` |
| `begin` | Statement chain / `controls_do_then_return` |
| `and-delayed` | `logic_operation` (AND) |
| `or-delayed` | `logic_operation` (OR) |
| `let` | `local_declaration_statement` / `_expression` |
| `get-component` | `component_component_block` |
| `get-all-components` | `component_all_component_block` |
| `break` | `controls_break` |
| `((get-var p$X) args...)` | `procedures_callnoreturn` / `procedures_callreturn` |
| `static-field` | `helpers_dropdown` |

**`call-yail-primitive` dispatch map** (selected entries -- full map has 130+):

| Primitive | Block type | Arity |
|---|---|---|
| `+` | `math_add` | variadic |
| `-` | `math_subtract` | binary |
| `*` | `math_multiply` | variadic |
| `yail-divide` | `math_division` | binary |
| `expt` | `math_power` | binary |
| `sqrt`, `abs`, `log`, `exp` | `math_single(ROOT/ABS/LN/EXP)` | unary |
| `yail-round`, `yail-ceiling`, `yail-floor` | `math_single(ROUND/CEILING/FLOOR)` | unary |
| `yail-equal?`, `yail-not-equal?` | `math_compare(EQ/NEQ)` | binary |
| `<`, `<=`, `>`, `>=` | `math_compare(LT/LTE/GT/GTE)` | binary |
| `yail-not` | `logic_negate` | unary |
| `sin-degrees` .. `atan-degrees` | `math_trig(SIN..ATAN)` | unary |
| `atan2-degrees` | `math_atan2` | binary |
| `string-append` | `text_join` | variadic |
| `string-length` | `text_length` | unary |
| `string-empty?` | `text_isEmpty` | unary |
| `string-contains` | `text_contains` | binary |
| `string-split` | `text_split(SPLIT)` | binary |
| `string-replace-all` | `text_replace_all` | ternary |
| `make-yail-list` | `lists_create_with` | variadic |
| `yail-list-get-item` | `lists_select_item` | binary |
| `yail-list-set-item!` | `lists_replace_item` | ternary |
| `yail-list-length` | `lists_length` | unary |
| `yail-list-add-to-list!` | `lists_add_items` | variadic |
| `make-yail-dictionary` | `dictionaries_create_with` | variadic |
| `make-dictionary-pair` | `pair` | binary |
| `yail-dictionary-lookup` | `dictionaries_lookup` | ternary |
| `random-integer` | `math_random_int` | binary |
| `random-fraction` | `math_random_float` | nullary |
| `make-color` | `color_make_color` | unary |
| `split-color` | `color_split_color` | unary |
| `open-another-screen` | `controls_openAnotherScreen` | unary |
| `close-screen` | `controls_closeScreen` | nullary |

The complete map is built from the existing JS generators (`yail/*.js`), reading them as the authoritative reference for which primitive maps to which block type.

**Variable prefix handling:**
- `g$X` → global variable named `X`
- `p$X` → procedure named `X`
- `$X` → local/parameter variable named `X`

**If/else/elseif unwinding:**
YAIL nests if/else chains: `(if c1 (begin b1) (if c2 (begin b2) (begin else)))`. The translator iteratively unwraps these into a single `controls_if` block with the appropriate mutation (`elseif` count and `else` flag).

**Block creation approach:**
Uses Blockly's programmatic API rather than XML generation:
```javascript
var block = workspace.newBlock('math_add');
block.initSvg();
block.setFieldValue('2', 'NUM_ITEMS');  // mutation
block.getInput('NUM0').connection.connect(childBlock.outputConnection);
```

This lets Blockly enforce its own validity rules (connection types, field constraints) during creation.

**LLM error tolerance:**
The parser should handle common LLM mistakes:
1. Extra whitespace/newlines -- tokenizer ignores all whitespace between tokens
2. Missing `(set-this-form)` -- proceed without it if absent from events
3. Missing type annotations -- if `'(types...)` is omitted from `call-yail-primitive`, infer from arg count
4. Shortened forms -- if LLM writes `(+ a b)` instead of full `(call-yail-primitive + ...)`, recognize as convenience shorthand
5. Missing variable prefixes -- if `g$` or `$` missing, attempt to infer from context
6. Unbalanced parentheses -- report position clearly for LLM retry

---

## Modified Shared/RPC Layer

### `AIAgentRequest.java`

Add field:
```java
private String blocksYail;  // Client-generated YAIL for current screen's blocks
```

The client populates this by calling `getBlocksYail()` on the workspace before sending. The server uses it directly as LLM context without any conversion.

### `AIOperation.java`

Operation type enum simplifies:

**Before:** `SET_EVENT_HANDLER`, `DELETE_EVENT_HANDLER`, `SET_VARIABLE`, `DELETE_VARIABLE`, `SET_PROCEDURE`, `DELETE_PROCEDURE` (6 types)

**After:** `WRITE_BLOCK`, `DELETE_BLOCK` (2 types)

Designer and screen operation types are unchanged.

Payload format:
- `WRITE_BLOCK`: `{ "yail": "(define-event Button1 Click ...)" }`
- `DELETE_BLOCK`: `{ "block": "define-event Button1 Click" }`

---

## Modified Server-Side Files

All under `appinventor/appengine/src/com/google/appinventor/server/aiagent/`:

### `AIAgentServiceImpl.java`

- `processRequest()` reads `blocksYail` from the request and passes it to `AIContextBuilder` instead of reading the `.bky` file and running `BlocksPseudocodeGenerator`
- The pseudocode-to-XML conversion step (`convertPseudocodeToXml`) is removed entirely -- operations carry raw YAIL strings through to the client
- New error feedback handling: receives client YAIL parse errors, includes them as context in LLM retry

### `AIContextBuilder.java`

- Layer 4 (current screen blocks) uses the client-provided `blocksYail` string directly
- Layer 3 references `yail_grammar.md` instead of `pseudocode_grammar.md`
- Layer 5 uses YAIL-based few-shot examples
- System prompt updated to instruct the LLM to use `write_block`/`delete_block` tools with YAIL

### `LLMResponseParser.java`

- Tool definitions updated for `write_block(yail)` and `delete_block(block)`
- Parsing extracts these into `WRITE_BLOCK` and `DELETE_BLOCK` `AIOperation` instances

### `AIOperationValidator.java`

New lightweight YAIL head skimmer (~50 lines). For `write_block` operations:

```java
// Tokenize just the first few whitespace-separated tokens:
// "(define-event Button1 Click" → validate Button1 exists, has Click
// "(def g$score" → validate variable name
// "(def (p$factorial" → validate procedure name
```

This is NOT a full S-expression parser. It reads characters until it has enough tokens to identify the block, then stops. No AST, no recursion.

For `delete_block` operations: parse the identifier string (same token format) and validate the target exists.

---

## Modified Client-Side Java (GWT)

### `AIChatDialog.java`

When the user clicks Send:
1. Call `blocksArea.getBlocksYail()` to get the current blocks as YAIL
2. Include the YAIL string in the `AIAgentRequest`
3. On receiving a response with `WRITE_BLOCK`/`DELETE_BLOCK` operations, delegate to `AIOperationExecutor`
4. If `AIOperationExecutor` reports a YAIL parse error, send the error back to the server for LLM retry

### `BlocklyPanel.java`

New JSNI bridge methods:

```java
public native String doGetBlocksYail() /*-{
    return this.@...::workspace.getBlocksYail();
}-*/;

public native String doWriteBlock(String yail) /*-{
    return JSON.stringify(
        AI.YailToBlocks.convert(this.@...::workspace, yail));
}-*/;

public native void doDeleteBlock(String blockIdentifier) /*-{
    AI.YailToBlocks.deleteBlock(this.@...::workspace, blockIdentifier);
}-*/;
```

### `AIOperationExecutor.java`

For `WRITE_BLOCK` operations:
1. Extract `yail` from operation payload
2. Find existing block with same identity (if any) and remove it
3. Call `BlocklyPanel.doWriteBlock(yail)` which invokes the JS YAIL-to-Blocks converter
4. Check result for success/error
5. On error: collect error message for feedback to server

For `DELETE_BLOCK` operations:
1. Extract `block` identifier from payload
2. Call `BlocklyPanel.doDeleteBlock(identifier)` which finds and removes the block

---

## Resource Files

All under `appinventor/appengine/src/com/google/appinventor/server/aiagent/resources/`:

### `yail_grammar.md` (replaces `pseudocode_grammar.md`)

Documents the YAIL subset the LLM should produce. Covers:
- S-expression syntax basics
- Top-level forms (`define-event`, `def` for variables, `def` for procedures)
- `call-yail-primitive` with the full primitive name table
- Component operations (`call-component-method`, `get-property`, `set-and-coerce-property!`)
- Variable prefixes (`g$`, `p$`, `$`)
- Control flow (`if`, `while`, `foreach`, `forrange`, `begin`, `let`)
- Type annotations (`'(number number)`, `'(text)`, etc.)

### `few_shot_examples.json` (rewritten)

Examples using `write_block` and `delete_block` tools with YAIL bodies. Covers:
- Simple event handler with component method call
- Global variable declaration
- Procedure with parameters and return value
- Complex logic (if/else, loops, list operations)
- Deleting a block

### `appinventor_reference.md` (updated)

References updated from pseudocode to YAIL where applicable.

---

## Files to Delete (Cleanup)

| File | Reason |
|---|---|
| `BlocksPseudocodeGenerator.java` | Replaced by client-side `getBlocksYail()` |
| `PseudocodeParser.java` | Replaced by client-side YAIL-to-Blocks JS converter |
| `PseudocodeParseException.java` | No longer needed |
| `pseudocode_grammar.md` | Replaced by `yail_grammar.md` |

`BlocksXmlGenerator.java` may also become unused if no other code depends on it, since the client creates blocks programmatically rather than generating XML. To be verified during implementation.

---

## Implementation Order

### Phase 1: Client read path (Blockly blocks → YAIL → server)

1. Implement `getBlocksYail()` on `Blockly.WorkspaceSvg`
2. Add JSNI bridge in `BlocklyPanel.java`
3. Add `blocksYail` field to `AIAgentRequest`
4. Update `AIChatDialog.java` to call `getBlocksYail()` and send it

### Phase 2: Server integration (thin relay)

1. Update `AIContextBuilder.java` to use client-provided YAIL as LLM context
2. Update `LLMResponseParser.java` with `write_block`/`delete_block` tool definitions
3. Update `AIOperation.java` with `WRITE_BLOCK`/`DELETE_BLOCK` types
4. Implement YAIL head skimmer in `AIOperationValidator.java`
5. Update `AIAgentServiceImpl.java` to pass raw YAIL through (no conversion)
6. Write `yail_grammar.md` and update few-shot examples

### Phase 3: Client write path (YAIL → Blockly blocks)

1. Implement `ai/sexpr_parser.js` (S-expression tokenizer + parser)
2. Implement `ai/yail_to_blocks.js` (AST walker + programmatic block creation)
3. Add JSNI bridges in `BlocklyPanel.java` for `doWriteBlock`/`doDeleteBlock`
4. Update `AIOperationExecutor.java` to handle `WRITE_BLOCK`/`DELETE_BLOCK`

### Phase 4: Error feedback loop

1. `AIOperationExecutor` collects YAIL parse errors
2. `AIChatDialog` sends error reports back to server
3. `AIAgentServiceImpl` retries the LLM with error context
4. Define the error report format and RPC method

### Phase 5: Cleanup

1. Remove `BlocksPseudocodeGenerator.java`
2. Remove `PseudocodeParser.java` and `PseudocodeParseException.java`
3. Remove `pseudocode_grammar.md`
4. Verify `BlocksXmlGenerator.java` has no remaining dependents; remove if so
5. Update any remaining references to pseudocode in comments/docs

---

## Testing Strategy

### S-expression parser tests (JS)
- Parse YAIL snippets, verify AST structure
- Edge cases: nested parens, special symbol chars (`yail-divide`, `set-var!`, `*list-for-runtime*`), string escaping, hex number literals, quoted lists

### YAIL-to-Blocks tests (JS)
- For each block type: provide YAIL input, verify the correct Blockly block is created with correct fields, inputs, and connections
- Test the full primitive dispatch map
- Test if/else/elseif unwinding
- Test variable prefix handling

### Round-trip tests
- Use existing `.bky` test files from `blocklyeditor/tests/com/google/appinventor/blocklyeditor/data/` (moleMash, factorial, makeQuiz, paintPot, etc.)
- Load blocks into workspace → `getBlocksYail()` → `AI.YailToBlocks.convert()` → compare resulting blocks with original
- This validates both directions simultaneously

### Server validation tests (Java)
- YAIL head skimmer correctly extracts block identity from various forms
- Validation catches invalid component/event/variable references
- Malformed YAIL heads produce clear error messages

### Error tolerance tests (JS)
- Feed common LLM mistakes (missing type annotations, shortened primitives, missing prefixes)
- Verify the parser recovers or produces actionable error messages

### Integration tests
- Full flow: client generates YAIL → server builds context → LLM call (mocked) → server validates → client parses → blocks appear
- Error feedback loop: client reports error → server retries → success on second attempt

---

## Key Reference Files

**Existing YAIL generators (authoritative reference for block-to-YAIL mapping):**
- `blocklyeditor/src/generators/yail.js` -- main generator, constants, `getFormYail_`
- `blocklyeditor/src/generators/yail/componentblock.js` -- event/method/property patterns
- `blocklyeditor/src/generators/yail/math.js` -- all math primitive mappings
- `blocklyeditor/src/generators/yail/text.js` -- all text primitive mappings
- `blocklyeditor/src/generators/yail/lists.js` -- all list primitive mappings
- `blocklyeditor/src/generators/yail/dictionaries.js` -- all dictionary primitive mappings
- `blocklyeditor/src/generators/yail/control.js` -- control flow patterns
- `blocklyeditor/src/generators/yail/variables.js` -- variable get/set/let patterns
- `blocklyeditor/src/generators/yail/procedures.js` -- procedure def/call patterns
- `blocklyeditor/src/generators/yail/logic.js` -- boolean/and/or/not patterns
- `blocklyeditor/src/generators/yail/colors.js` -- color constant values
- `blocklyeditor/src/generators/yail/helpers.js` -- static-field/screen/asset patterns

**Current AI agent system (to be modified):**
- `server/aiagent/AIAgentServiceImpl.java` -- main RPC handler
- `server/aiagent/AIContextBuilder.java` -- context/prompt building
- `server/aiagent/LLMResponseParser.java` -- tool call parsing
- `server/aiagent/AIOperationValidator.java` -- operation validation
- `client/editor/youngandroid/AIChatDialog.java` -- chat UI + RPC calls
- `client/editor/youngandroid/AIOperationExecutor.java` -- operation application
- `client/editor/blocks/BlocklyPanel.java` -- Java/JS bridge
- `shared/rpc/aiagent/AIAgentRequest.java` -- request DTO
- `shared/rpc/aiagent/AIOperation.java` -- operation DTO

**Current pseudocode system (to be removed):**
- `server/aiagent/BlocksPseudocodeGenerator.java` -- Blockly XML → pseudocode
- `server/aiagent/PseudocodeParser.java` -- pseudocode → Blockly XML
- `server/aiagent/PseudocodeParseException.java` -- exception
- `server/aiagent/BlocksXmlGenerator.java` -- XML generation helper (verify if still needed)
