# Plan 3: Fixes from YAIL Plan Validation

Validation of `plan-2.md` against the actual YAIL generators (`blocklyeditor/src/generators/yail/*.js`), the Scheme runtime (`components-ios/src/runtime.scm`, `buildserver/src/com/google/appinventor/buildserver/resources/runtime.scm`), and the already-implemented client-side files (`ai/sexpr_parser.js`, `ai/yail_to_blocks.js`, `generators/yail_blocks_export.js`).

This document lists every discrepancy found and the concrete fix for each.

---

## Status of Plan-2 Files

The three "New Client-Side JavaScript Files" described in plan-2 already exist on the `ai-agent-yail` branch:

| Plan description | Actual file | Lines |
|---|---|---|
| `generators/yail_blocks_export.js` (~30 lines) | Implemented | 67 |
| `ai/sexpr_parser.js` (~100-150 lines) | Implemented | 376 |
| `ai/yail_to_blocks.js` (~800-1,200 lines) | Implemented | 1,963 |

Plan-2 should be read as a design document. The implemented code is authoritative where it diverges from the plan.

---

## Fix 1: Document `def-return` Extension

**Problem:** Plan-2 does not explain how the converter distinguishes `procedures_defreturn` from `procedures_defnoreturn`, since both use `(def (p$name ...) body)` in standard YAIL.

**Existing solution:** `yail_blocks_export.js:37-39` rewrites the YAIL head for return procedures:

```javascript
if (block.type === 'procedures_defreturn') {
    code = code.replace(/^\(def /, '(def-return ');
}
```

The converter dispatches on `def-return` at `yail_to_blocks.js:252-253`.

**Fixes required:**

1. **`yail_grammar.md`** (resource file for LLM context): Must document `def-return` as the form for procedures that return a value.

   ```
   ;; Procedure with no return value
   (def (p$greet $name) (call-component-method ...))

   ;; Procedure WITH return value — note "def-return", not "def"
   (def-return (p$factorial $n) (if ...))
   ```

2. **`few_shot_examples.json`**: All procedure-with-return examples must use `def-return`.

3. **`AIOperationValidator.java`** (server-side head skimmer): Must accept both `def` and `def-return` as valid procedure heads:

   ```java
   // Token 0: "def" or "def-return"
   // Token 1: "(p$name" → procedure
   ```

4. **Plan-2 § Root Block Types table** (line 94-96): Add `def-return` row:

   | Block type | YAIL form | Identity key | Uniqueness |
   |---|---|---|---|
   | `procedures_defreturn` | `(def-return (p$name ...) ...)` | `name` | Same namespace as defnoreturn |

5. **Plan-2 § LLM Tool Design** (line 116-122): Update the procedure example to use `def-return`.

6. **Plan-2 § delete_block** (line 133): Accept `def-return` identifiers:

   ```
   delete_block(block="def-return p$factorial")
   ```

---

## Fix 2: Document the 4th Argument to `call-yail-primitive`

**Problem:** Every `call-yail-primitive` example in plan-2 shows 3 arguments. The actual YAIL always has 4.

**Runtime confirmation** (`runtime.scm:820`):

```scheme
(define (call-yail-primitive prim arglist typelist codeblocks-name)
```

The 4th argument `codeblocks-name` is a human-readable description string used for runtime error messages (e.g., `"="`, `"join"`, `"select list item"`).

**Generated YAIL** (confirmed from generators and Screen1.yail):

```scheme
(call-yail-primitive yail-equal? (*list-for-runtime* x y) '(any any) "=")
(call-yail-primitive string-append (*list-for-runtime* a b c) '(text text text) "join")
```

**Fixes required:**

1. **`yail_grammar.md`**: Document `call-yail-primitive` as taking 4 arguments:

   ```
   (call-yail-primitive PRIMITIVE_NAME
     (*list-for-runtime* ARG1 ARG2 ...)
     '(TYPE1 TYPE2 ...)
     "DESCRIPTION")
   ```

   Note that `DESCRIPTION` is a quoted string for error messages. Common values: `"="`, `"+"`, `"join"`, `"not"`, `"select list item"`, `"make a list"`.

2. **`few_shot_examples.json`**: All examples must include the 4th argument.

3. **Plan-2 examples** (lines 110-122): Update all `call-yail-primitive` invocations.

4. **LLM error tolerance** (plan-2 line 372): Add tolerance rule: "If 4th argument (description) is omitted, proceed — the converter ignores it anyway." The converter at `yail_to_blocks.js:1207` already ignores the 4th argument during reverse parsing.

---

## Fix 3: Add `define-generic-event` as a Root Block Type

**Problem:** Plan-2 lists only 4 root block types. Generic event handlers (`define-generic-event`) are missing.

**Runtime confirmation** (`runtime.scm:177-184`):

```scheme
(define-macro define-generic-event
  (lambda (form env)
    (let* ((component-type (cadr form))
           (event-name (caddr form))
           (args (cadddr form))
           (body (cddddr form))
           (full-name (symbol-append 'any$ component-type '$ event-name)))
      ...)))
```

**Generator** (`componentblock.js:32-36`):

```javascript
if (this.isGeneric) {
    preamble = AI.Yail.YAIL_DEFINE_GENERIC_EVENT
      + this.typeName + AI.Yail.YAIL_SPACER + this.eventName;
}
```

**Fixes required:**

1. **Plan-2 § Root Block Types table**: Add 5th row:

   | Block type | YAIL form | Identity key | Uniqueness |
   |---|---|---|---|
   | `component_event` (generic) | `(define-generic-event TypeName Event ...)` | `(typeName, event)` | One per type+event pair |

2. **`yail_grammar.md`**: Document the generic event form:

   ```
   (define-generic-event Button Click ($component $param1)
     (set-this-form)
     body...)
   ```

   Note: first parameter is always the component instance.

3. **`yail_blocks_export.js`**: Verify generic events are exported. Currently `buildComponentMap` puts generic events in `globals`, so they are already exported. No code change needed.

4. **`yail_to_blocks.js`**: Add `define-generic-event` to `convertTopLevel_` dispatch. Currently missing — the converter will throw "Unknown top-level form: define-generic-event".

   ```javascript
   case 'define-generic-event':
       return AI.YailToBlocks.convertGenericEventHandler_(workspace, node);
   ```

5. **`AIOperationValidator.java`**: Add head skimming for `define-generic-event`.

6. **`deleteBlock`** in `yail_to_blocks.js`: Add case for `define-generic-event` identifiers.

---

## Fix 4: Document Missing YAIL Forms in Dispatch Table

**Problem:** Plan-2's expression/statement dispatch table (lines 276-308) omits several forms that the generators produce.

**Fixes required:**

Update plan-2 § "Expression/statement dispatch" to add:

| YAIL head | Block type | Notes |
|---|---|---|
| `filter_nondest` | `lists_filter` | Higher-order: takes lambda body |
| `map_nondest` | `lists_map` | Higher-order: takes lambda body |
| `reduceovereach` | `lists_reduce` | Higher-order: takes lambda body |
| `sortcomparator_nondest` | `lists_sort_comparator` | Higher-order: takes lambda body |
| `sortkey_nondest` | `lists_sort_key` | Higher-order: takes lambda body |
| `mincomparator-nondest` | `lists_minimum_value` | Higher-order: takes lambda body |
| `maxcomparator-nondest` | `lists_maximum_value` | Higher-order: takes lambda body |
| `protect-enum` | `helpers_dropdown` | `(protect-enum enumVal numVal)` → returns enumVal |
| `*the-null-value*` | `controls_nothing` | Bare symbol, no parens |
| `*yail-break*` | `controls_break` | `(*yail-break* #f)` — inside loops only |
| `make-exact-yail-integer` | *(unwrap)* | Transparent wrapper — extract inner arg |
| `not` | *(wrap)* | `(not (call-yail-primitive string=? ...))` for text NEQ |

**Implementation status:** Most of these are NOT yet handled in `yail_to_blocks.js`. The higher-order forms (`filter_nondest`, `map_nondest`, `reduceovereach`, `sortcomparator_nondest`, `sortkey_nondest`, `mincomparator-nondest`, `maxcomparator-nondest`) will need dedicated converter functions. `make-exact-yail-integer` IS handled (line 678). `protect-enum` is NOT handled. `*the-null-value*` and `*yail-break*` are NOT handled.

---

## Fix 5: Document `usePrefixInYail` Variable Prefix Variants

**Problem:** Plan-2 says `$X` for local variables and parameters. When `Blockly.usePrefixInYail` is true, the prefixes are longer.

**From the generators:**

| Context | Prefix when `usePrefixInYail=false` | Prefix when `usePrefixInYail=true` |
|---|---|---|
| Procedure params | `$argName` | `$param_argName` |
| Local variables (let) | `$varName` | `$local_varName` |
| For-each-dict keys/values | `$keyName` | `$local_keyName` |
| Global variables | `g$name` | `g$name` (unchanged) |
| Procedure names | `p$name` | `p$name` (unchanged) |

**Fixes required:**

1. **`yail_grammar.md`**: Document both prefix forms.

2. **`yail_to_blocks.js`**: The converter strips `$` when creating parameter/local variable names. Verify it also strips `$param_` and `$local_` prefixes correctly. Current code at line 306:

   ```javascript
   var pName = (p.name || String(p.value)).replace(/^\$/, '');
   ```

   This only strips bare `$`, not `$param_` or `$local_`. If `usePrefixInYail` is true on the client, the export will produce `$param_X` but the converter will create a parameter named `param_X` instead of `X`.

   Fix: Update prefix stripping to handle all variants:

   ```javascript
   var pName = (p.name || String(p.value))
       .replace(/^\$(?:param_|local_)?/, '');
   ```

3. **LLM instructions**: Tell the LLM to use the simple `$X` form (without `param_`/`local_` prefixes) since the converter normalizes them.

---

## Fix 6: Document `controls_choose` vs `controls_if` Distinction

**Problem:** Both generate `(if ...)` but with different structures. Plan-2 mentions the mapping (line 296) but doesn't detail the structural difference.

**Distinction:**

- **`controls_if`** (statement): `(if test (begin stmts...) (begin else-stmts...))` — branches wrapped in `(begin ...)`
- **`controls_choose`** (expression): `(if test thenExpr elseExpr)` — NO `begin` wrappers, returns value

From `control.js:65-75`:
```javascript
AI.Yail['controls_choose'] = function() {
    var code = AI.Yail.YAIL_IF + test
               + AI.Yail.YAIL_SPACER + thenReturn
               + AI.Yail.YAIL_SPACER + elseReturn
               + AI.Yail.YAIL_CLOSE_COMBINATION;
    return [code, AI.Yail.ORDER_ATOMIC];  // expression!
};
```

**Fixes required:**

1. **`yail_grammar.md`**: Document both patterns:

   ```
   ;; Statement if (controls_if) — branches use (begin ...)
   (if condition
     (begin stmt1 stmt2 ...)
     (begin else-stmt1 ...))

   ;; Expression if (controls_choose) — no begin, returns value
   (if condition thenExpr elseExpr)
   ```

2. **`yail_to_blocks.js`**: The converter already handles this — when `if` appears in expression context, it checks whether branches are `(begin ...)` wrapped. Verify this covers all edge cases.

---

## Fix 7: Document Continuation/Blocking Method Variants

**Problem:** Plan-2 mentions `call-component-method` and `call-component-type-method` but not their async variants.

**Runtime signatures:**

| Function | Args | Signature |
|---|---|---|
| `call-component-method` | 4 | `(comp-name method-name arglist typelist)` |
| `call-component-method-with-blocking-continuation` | 4 | `(comp-name method-name arglist typelist)` |
| `call-component-type-method` | **5** | `(possible-comp comp-type method-name arglist typelist)` |
| `call-component-type-method-with-blocking-continuation` | **5** | `(possible-comp comp-type method-name arglist typelist)` |
| `set-and-coerce-property-and-check!` | **5** | `(possible-comp comp-type prop-sym value prop-type)` |
| `get-property-and-check` | **3** | `(possible-comp comp-type prop-name)` |

Note: `call-component-type-method` takes **5** arguments (extra `possible-component` compared to the instance version). Plan-2 doesn't mention this.

**Fixes required:**

1. **`yail_grammar.md`**: Document all method/property call variants with their argument counts.

2. **Plan-2 § dispatch table**: Add:

   | YAIL head | Block type |
   |---|---|
   | `call-component-method-with-blocking-continuation` | `component_method` (async) |
   | `call-component-type-method-with-blocking-continuation` | `component_method` (generic async) |
   | `set-and-coerce-property-and-check!` | `component_set_get` (generic set) |
   | `get-property-and-check` | `component_set_get` (generic get) |

3. **`yail_to_blocks.js`**: Verify all variants are handled. Current dispatch includes `call-component-type-method` (line 670) and `set-and-coerce-property-and-check!` (lines 514, 650), and `get-property-and-check` (line 648). Check if blocking-continuation variants are handled.

---

## Fix 8: Document `begin` Context-Dependent Semantics

**Problem:** Plan-2 lists `begin` → "Statement chain / `controls_do_then_return`" (line 300) but doesn't detail the context-dependent handling.

**Actual uses of `(begin ...)` in YAIL:**

| Context | Pattern | Block mapping |
|---|---|---|
| Inside `controls_if` branches | `(begin stmt1 stmt2 ...)` | Part of `controls_if` — not a separate block |
| Inside loop bodies | `(begin body)` | Loop body wrapper — not a separate block |
| As expression (last element is value) | `(begin stmt1 ... returnExpr)` | `controls_do_then_return` |
| `controls_eval_but_ignore` | `(begin valueExpr "ignored")` | `controls_eval_but_ignore` |
| At statement level | `(begin stmt1 stmt2 ...)` | Statement chain — no block, just sequential |

**Fixes required:**

1. **`yail_grammar.md`**: Document `begin` as a contextual form, not a block.

2. **`yail_to_blocks.js`**: The converter has separate handling for `begin` in statement context vs expression context. Verify the `controls_eval_but_ignore` pattern (`(begin value "ignored")`) is handled.

---

## Fix 9: Verify `foreach`/`forrange`/`while` Argument Order

**Problem:** Plan-2 lists these in the dispatch table but doesn't specify the argument positions, which are non-obvious.

**Runtime-confirmed argument order:**

```scheme
(foreach $varName (begin body...) listExpr)        ;; body BEFORE list
(forrange $varName (begin body...) start end step)  ;; body BEFORE range params
(while condition (begin body...))                    ;; standard order
```

Note: In `foreach` and `forrange`, the **body comes before** the data source, which is counterintuitive.

**Fixes required:**

1. **`yail_grammar.md`**: Document the exact argument positions:

   ```
   ;; for-each: (foreach VARIABLE BODY LIST)
   (foreach $item (begin (call-component-method ...)) someList)

   ;; for-range: (forrange VARIABLE BODY START END STEP)
   (forrange $i (begin (call-component-method ...)) 1 10 1)

   ;; while: (while CONDITION BODY)
   (while (call-yail-primitive < ...) (begin ...))
   ```

2. The converter already handles these correctly. No code change needed.

---

## Fix 10: Handle Bare Literals in Reverse Parsing

**Problem:** Plan-2 doesn't explicitly discuss how bare number/string/boolean literals map to blocks.

**Generated YAIL patterns:**

| Literal type | YAIL | Block type |
|---|---|---|
| Integer | `42` | `math_number` |
| Float | `3.14` | `math_number` |
| Hex color | `#xFF0000FF` | `math_number` (or color constant) |
| String | `"hello"` | `text` |
| Boolean true | `#t` | `logic_boolean` (TRUE) |
| Boolean false | `#f` | `logic_boolean` (FALSE) |
| Null | `*the-null-value*` | `controls_nothing` |

**Implementation status:** The converter handles numbers, strings, and booleans (`yail_to_blocks.js:556-563`). Color constants (raw hex numbers like `#xFF000000`) are handled as `math_number` blocks. `*the-null-value*` (bare symbol) is NOT currently handled — it would fall through to the unknown-symbol error path.

**Fix:** Add handling for `*the-null-value*` symbol in the expression converter.

---

## Fix 11: Correct Plan-2 Primitive Name Table

**Validation result:** The plan's primitive names are **mostly correct**. The trig names (`sin-degrees`, `cos-degrees`, etc.) were initially flagged as wrong but are actually correct — the trig generator swaps tuple indices compared to other generators, so `tuple[1]` (not `tuple[0]`) is the primitive name.

However, the plan's table is missing several primitives that appear in the existing converter's `PRIMITIVE_MAP_` (180 entries). Key additions needed:

| Missing primitive | Block type |
|---|---|
| `string-starts-at` | `text_starts_at` |
| `string-contains-any` | `text_contains(CONTAINS_ANY)` |
| `string-contains-all` | `text_contains(CONTAINS_ALL)` |
| `string-substring` | `text_segment` |
| `string?` | `text_is_string` |
| `string-reverse` | `text_reverse` |
| `string-replace-mappings-*` | `text_replace_mappings` |
| `bitwise-and/ior/xor` | `math_bitwise` |
| `format-as-decimal` | `math_format_as_decimal` |
| `is-number?/is-base10?/...` | `math_is_a_number` |
| `math-convert-dec-hex/...` | `math_convert_number` |
| `modulo/remainder/quotient` | `math_divide` variants |
| `radians->degrees/degrees->radians` | `math_convert_angles` |
| `avg/minl/maxl/gm/std-dev/std-err` | `math_on_list2` |
| `mode` | `math_mode_of_list` |
| `yail-list-remove-item!` | `lists_remove_item` |
| `yail-list-insert-item!` | `lists_insert_item` |
| `yail-list-member?` | `lists_is_in` |
| `yail-list-index` | `lists_position_in` |
| `yail-list-pick-random` | `lists_pick_random_item` |
| `yail-list-sort` | `lists_sort` |
| `yail-list-but-first/last` | `lists_but_first/last` |
| `yail-list-slice` | `lists_slice` |
| `yail-list-join-with-separator` | `lists_join_with_separator` |
| All dictionary operations | `dictionaries_*` |
| `open-another-screen-with-start-value` | `controls_openAnotherScreenWithStartValue` |
| `close-screen-with-value` | `controls_closeScreenWithValue` |
| `close-screen-with-plain-text` | `controls_closeScreenWithPlainText` |
| `get-start-value` | `controls_getStartValue` |
| `close-application` | `controls_closeApplication` |

The full authoritative map is in `yail_to_blocks.js:1023-1202`.

---

## Fix 12: Server-Side Head Skimmer Must Handle `def-return`

**Problem:** `AIOperationValidator.java` needs to skim YAIL heads. With the `def-return` extension, it must recognize both forms.

**Fix:** The head skimmer tokenization at plan-2 line 433 should be updated:

```
"(def g$score"           → global variable "score"
"(def (p$factorial"      → procedure "factorial" (no return)
"(def-return (p$factorial" → procedure "factorial" (with return)
"(define-event Button1 Click" → event handler
"(define-generic-event Button Click" → generic event handler
```

---

## Implementation Priority

### Phase A: Resource files (no code changes, enables LLM correctness) — DONE

1. ~~Write `yail_grammar.md` incorporating fixes 1-2, 5-9~~ — done: added `protect-enum`, blocking-continuation, `$param_`/`$local_` prefix note, 4th-arg descriptions to all examples, generic event example
2. ~~Rewrite `few_shot_examples.json` with `def-return` and 4th-arg descriptions~~ — done: added `"+"`, `"="`, `"*"` descriptions to all `call-yail-primitive` examples
3. ~~Update `appinventor_reference.md`~~ — done: added `define-generic-event` to `write_block`/`delete_block` tool descriptions

### Phase B: Converter gaps (client-side JS) — DONE

4. ~~Add `define-generic-event` support to `yail_to_blocks.js` (fix 3)~~ — done: `convertTopLevel_`, `deleteBlock`, `convertGenericEventHandler_`
5. ~~Fix `usePrefixInYail` prefix stripping (fix 5)~~ — done: all 11 `replace(/^\$/, '')` → `replace(/^\$(?:param_|local_)?/, '')`
6. ~~Add `*the-null-value*` symbol handling (fix 10)~~ — done: `convertSymbolExpr_` → `controls_nothing`
7. ~~Add `protect-enum` handling~~ — done: `convertProtectEnum_` delegates to `convertStaticField_`
8. ~~Add higher-order form handlers (fix 4)~~ — done: `HIGHER_ORDER_MAP_` + `convertHigherOrderForm_` for all 7 forms
9. ~~Add `*yail-break*` as alias for `break` in statement dispatch~~ — done
10. ~~Add `not` as alias for `yail-not` in PRIMITIVE_MAP_~~ — done (handles text NEQ wrapping)
11. ~~Add blocking-continuation method variants~~ — done: `call-component-method-with-blocking-continuation`, `call-component-type-method-with-blocking-continuation` in both statement and expression dispatchers

### Phase C: Server-side updates — DONE

9. ~~Update `AIOperationValidator.java` head skimmer for `define-generic-event`~~ — done: added `define-generic-event` case to both `validateWriteBlock` and `validateDeleteBlock`, updated error messages
10. ~~Update `AIContextBuilder.java` tool descriptions~~ — done: added `define-generic-event` to `write_block` and `delete_block` tool description strings
11. ~~`LLMResponseParser.java`~~ — no changes needed (generic tool name/required fields mapping, no YAIL-specific logic)

### Phase D: Testing

12. Round-trip tests: blocks → `getBlocksYail()` → `AI.YailToBlocks.convert()` → compare
13. Test `def-return` round-trip for procedures with return values
14. Test generic event handler round-trip
15. Test all higher-order forms (map, filter, reduce, sort) round-trip
