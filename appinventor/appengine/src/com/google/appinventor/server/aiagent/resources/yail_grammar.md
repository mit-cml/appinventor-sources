# YAIL Grammar Reference

YAIL (Young Android Intermediate Language) is an S-expression-based language derived from Scheme.
Use the `write_block` tool to create or replace blocks, providing complete YAIL S-expressions.
Use the `delete_block` tool to remove blocks, providing the YAIL head tokens as identifier.

## Disabled Blocks

Blocks that the user has manually disabled in the workspace appear in the YAIL context with a `;;; DISABLED` comment prefix:
```scheme
;;; DISABLED
(define-event Button1 Click ()
  (set-this-form)
  (call-component-method 'Notifier1 'ShowAlert
    (*list-for-runtime* "Hello!") '(text)))
```
Disabled blocks exist on the workspace but are excluded from code generation and do not run at runtime. When you see a `;;; DISABLED` block:
- **Disabled blocks are read-only.** `write_block` and `delete_block` cannot target them — they only operate on enabled blocks.
- **Do not try to recreate a disabled block.** If you use `write_block` with the same identity as a disabled block, it creates a second (enabled) copy alongside it rather than replacing it.
- **Reference them in your responses** when relevant (e.g. "I see you have a disabled Click handler for Button1").
- You cannot enable or disable blocks — only the user can do that via the block's right-click menu.

## Duplicate Blocks

If you see two enabled blocks with the same identity (e.g. two `(define-event Button1 Click ...)` without `;;; DISABLED`), the workspace is in a corrupted state. Be aware that `write_block` and `delete_block` only target **one copy at a time** — you may need to call `delete_block` twice to remove both. Mention the duplication to the user so they can clean it up.

## Error Recovery

When you receive error feedback about failed `write_block` operations, follow these rules:

- **Re-emit only the failed block.** Operations that passed validation have already been accepted. Do NOT re-emit them. Only re-emit the specific block(s) that failed, with the syntax issue fixed.
- **Parenthesis balancing.** YAIL is S-expression-based — every `(` must have a matching `)`. For complex blocks with deep nesting (`if`/`begin`/`let`/`or-delayed`/`and-delayed`), carefully verify that each opening form is properly closed. Pay special attention to `if` expressions, which require exactly one condition, one then-branch, and optionally one else-branch — each wrapped in its own `(begin ...)`.
- **Reduce complexity.** When a procedure body is very large (many nested conditions or repeated sub-expressions), break it into smaller helper procedures. This reduces nesting depth and makes parenthesis errors less likely. For instance, if a procedure has 8+ nested `and-delayed`/`or-delayed` branches, extract the check into a separate `def-return` procedure.

## Code Style Rules

**Statement primitives cannot be used as values.** Primitives ending with `!` that mutate data in place (`yail-list-set-item!`, `yail-list-remove-item!`, `yail-list-insert-item!`, `yail-list-append!`, `yail-list-add-to-list!`, `yail-dictionary-set-pair`, `yail-dictionary-delete-pair`, `yail-dictionary-recursive-set`, `yail-dictionary-combine-dicts`, `yail-matrix-set-cell!`) are **statement blocks** — they have no output connector and **CANNOT** be nested inside expressions. They must appear as standalone statements in a `begin`, event handler body, or procedure body. When you need to mutate a collection and then use it as a value, use `let` to bind the collection to a local variable, mutate it with the statement primitive, then reference the variable.

```scheme
;; BAD — yail-list-append! is a statement, cannot be used as a value
(set-and-coerce-property! 'ListView1 'Elements
  (call-yail-primitive yail-list-append!
    (*list-for-runtime* (get-var g$list1) (get-var g$list2))
    '(list list) "append to list")
  'list)

;; GOOD — mutate in a statement, then use the variable as the value
;; NOTE: let body requires (begin ...) when it contains multiple statements
(let (($combined (call-yail-primitive yail-list-copy
          (*list-for-runtime* (get-var g$list1))
          '(list) "copy list")))
  (begin
    (call-yail-primitive yail-list-append!
      (*list-for-runtime* (lexical-value $combined) (get-var g$list2))
      '(list list) "append to list")
    (set-and-coerce-property! 'ListView1 'Elements (lexical-value $combined) 'list)))
```

**Global variable initializers cannot reference other globals.** The initializer expression in `(def g$name <value>)` CANNOT contain `(get-var g$...)` references to other global variables, procedure calls `((get-var p$...))`, or component method/property blocks. App Inventor flags these as "This block cannot be in a definition" errors. Only use literal values, `make-yail-list`, `make-yail-dictionary`, and other pure constructors with literal arguments in global variable initializers. If a variable's value is composed from other values, inline those values directly instead of referencing separate global variables. If the globals are genuinely needed independently, initialize the dependent variable with a placeholder and assign the real value in `Screen1 Initialize`.

```scheme
;; BAD — get-var inside a global variable initializer (causes error)
(def g$LABEL_A "Alpha")
(def g$LABEL_B "Beta")
(def g$ALL_LABELS
  (call-yail-primitive make-yail-list
    (*list-for-runtime* (get-var g$LABEL_A) (get-var g$LABEL_B))
    '(text text) "make a list"))

;; GOOD (preferred) — inline the values directly
(def g$ALL_LABELS
  (call-yail-primitive make-yail-list
    (*list-for-runtime* "Alpha" "Beta")
    '(text text) "make a list"))

;; OK — if the separate globals are needed elsewhere, use a placeholder
;; and assign the real value in Screen1 Initialize
(def g$LABEL_A "Alpha")
(def g$LABEL_B "Beta")
(def g$ALL_LABELS (call-yail-primitive make-yail-list (*list-for-runtime*) '() "make a list"))

(define-event Screen1 Initialize ()
  (set-this-form)
  (set-var! g$ALL_LABELS
    (call-yail-primitive make-yail-list
      (*list-for-runtime* (get-var g$LABEL_A) (get-var g$LABEL_B))
      '(text text) "make a list")))
```

**`let` bindings cannot reference sibling bindings.** YAIL's `let` follows standard Scheme semantics: all initializer expressions are evaluated in the **enclosing** environment before any of the new bindings take effect. A binding in the same `let` block is NOT available to other initializers — using `(lexical-value $x)` where `$x` is defined in the same `let` will fail. When one binding depends on another, use **nested** `let` blocks so the outer binding is in scope when the inner initializer runs.

```scheme
;; BAD — $win references $p, but both are in the same let (error)
(let (($p (get-var g$CURRENT_PLAYER))
      ($win (or-delayed
              (and-delayed
                (call-yail-primitive string=?
                  (*list-for-runtime* (get-property 'Cell1 'Text) (lexical-value $p))
                  '(text text) "text=")
                (call-yail-primitive string=?
                  (*list-for-runtime* (get-property 'Cell2 'Text) (lexical-value $p))
                  '(text text) "text=")))))
  (begin ...))

;; GOOD — nested let so $p is in scope when $win is initialized
(let (($p (get-var g$CURRENT_PLAYER)))
  (let (($win (or-delayed
                (and-delayed
                  (call-yail-primitive string=?
                    (*list-for-runtime* (get-property 'Cell1 'Text) (lexical-value $p))
                    '(text text) "text=")
                  (call-yail-primitive string=?
                    (*list-for-runtime* (get-property 'Cell2 'Text) (lexical-value $p))
                    '(text text) "text=")))))
    (begin ...)))
```

**Constants as global variables.** Do not hardcode string or number literals directly in event handlers when they represent a meaningful constant, a configuration value, or appear more than once. Define them as global variables so they are easy to find and change in one place.

```scheme
;; BAD — magic string repeated in two handlers
(define-event Screen1 Initialize ()
  (set-this-form)
  (set-and-coerce-property! 'CategorySpinner 'Selection "Select a category..." 'text))

(define-event ResetButton Click ()
  (set-this-form)
  (set-and-coerce-property! 'CategorySpinner 'Selection "Select a category..." 'text))

;; GOOD — constant extracted to a global variable
(def g$PLACEHOLDER "Select a category...")

(define-event Screen1 Initialize ()
  (set-this-form)
  (set-and-coerce-property! 'CategorySpinner 'Selection (get-var g$PLACEHOLDER) 'text))

(define-event ResetButton Click ()
  (set-this-form)
  (set-and-coerce-property! 'CategorySpinner 'Selection (get-var g$PLACEHOLDER) 'text))
```

**Eliminate duplication with variables.** If the same expression or value appears more than once within a block, extract it into a local variable (`let`) or global variable (`def g$...`). Duplicated expressions make code harder to maintain and produce unnecessarily large blocks. This includes repeated string literals, repeated property reads, and repeated computed values.

```scheme
;; BAD — same list literal duplicated in both branches
(if (call-yail-primitive string=? (*list-for-runtime* (lexical-value $selection) "All") '(text text) "=")
  (begin
    (set-and-coerce-property! 'MenuListView 'Elements
      (call-yail-primitive make-yail-list (*list-for-runtime* "No items found.") '(text) "make a list") 'list))
  (begin
    (set-and-coerce-property! 'MenuListView 'Elements
      (call-yail-primitive make-yail-list (*list-for-runtime* "No items found.") '(text) "make a list") 'list)))

;; GOOD — shared value extracted to a local variable
(let (($empty (call-yail-primitive make-yail-list (*list-for-runtime* "No items found.") '(text) "make a list")))
  (if (call-yail-primitive string=? (*list-for-runtime* (lexical-value $selection) "All") '(text text) "=")
    (begin
      (set-and-coerce-property! 'MenuListView 'Elements (lexical-value $empty) 'list))
    (begin
      (set-and-coerce-property! 'MenuListView 'Elements (lexical-value $empty) 'list))))
```

**Inline expressions over local variables.** Only use `let` (local variables) when the same value is needed in more than one place (see above), when mutations via `set-lexical!` are required, or when a statement primitive must be sequenced before a value is used (see statement primitives rule). Do NOT introduce local variables just for readability or semantics — prefer composing expressions directly inline.

```scheme
;; BAD — unnecessary local variable used once
(let (($name (get-property 'TextBox1 'Text)))
  (begin
    (set-and-coerce-property! 'Label1 'Text (lexical-value $name) 'text)))

;; GOOD — inline the expression directly
(set-and-coerce-property! 'Label1 'Text (get-property 'TextBox1 'Text) 'text)

;; OK — local variable used in two places (deduplication)
(let (($name (get-property 'TextBox1 'Text)))
  (begin
    (set-and-coerce-property! 'Label1 'Text (lexical-value $name) 'text)
    (set-and-coerce-property! 'Label2 'Text (lexical-value $name) 'text)))
```

**Prefer mutator blocks over repeated blocks.** Many blocks in App Inventor have mutators that allow adding extra input slots. Always use a single mutator block instead of nesting multiple separate blocks when the operation supports it.

- **Variadic math:** Use `(+ a b c)` or `(call-yail-primitive + (*list-for-runtime* a b c) '(number number number) "+")` — do NOT nest as `(+ (+ a b) c)`. The same applies to `*`, `string-append`, `make-yail-list`, and `make-yail-dictionary`.
- **If/elseif/else:** Use a single nested-if chain for multi-way conditionals — do NOT use separate disconnected `if` blocks. The nested pattern automatically renders as one mutator block with elseif slots.
- **Add items to list:** Use `yail-list-add-to-list!` with multiple items — do NOT call it repeatedly for each item.

```scheme
;; BAD — nested binary additions (renders as 2 separate + blocks)
(call-yail-primitive +
  (*list-for-runtime*
    (call-yail-primitive +
      (*list-for-runtime* (get-var g$a) (get-var g$b))
      '(number number) "+")
    (get-var g$c))
  '(number number) "+")

;; GOOD — single variadic addition (renders as 1 block with 3 slots)
(call-yail-primitive + (*list-for-runtime* (get-var g$a) (get-var g$b) (get-var g$c)) '(number number number) "+")

;; BAD — separate if blocks for a multi-way condition
(if <cond1> (begin <body1>))
(if <cond2> (begin <body2>))
(if <cond3> (begin <body3>))

;; GOOD — single if/elseif/else chain (renders as 1 mutator block)
(if <cond1>
  (begin <body1>)
  (if <cond2>
    (begin <body2>)
    (begin <body3>)))

;; BAD — repeated add-to-list calls
(call-yail-primitive yail-list-add-to-list!
  (*list-for-runtime* (get-var g$myList) "apple") '(list any) "add items to list")
(call-yail-primitive yail-list-add-to-list!
  (*list-for-runtime* (get-var g$myList) "banana") '(list any) "add items to list")
(call-yail-primitive yail-list-add-to-list!
  (*list-for-runtime* (get-var g$myList) "cherry") '(list any) "add items to list")

;; GOOD — single variadic add-to-list (renders as 1 mutator block with 3 item slots)
(call-yail-primitive yail-list-add-to-list!
  (*list-for-runtime* (get-var g$myList) "apple" "banana" "cherry")
  '(list any any any) "add items to list")
```

**Event handlers over procedures.** Put code directly in event handlers. Only create a procedure (`def`/`def-return`) when the same logic is needed in more than one event handler AND it represents a coherent, reusable operation. A single-use helper procedure is never justified.

```scheme
;; BAD — unnecessary procedure for single-use code
(def (p$updateLabel)
  (set-and-coerce-property! 'Label1 'Text "clicked" 'text))

(define-event Button1 Click ()
  (set-this-form)
  ((get-var p$updateLabel)))

;; GOOD — code directly in the event handler
(define-event Button1 Click ()
  (set-this-form)
  (set-and-coerce-property! 'Label1 'Text "clicked" 'text))

;; OK — procedure shared by multiple handlers
(def (p$resetForm)
  (set-and-coerce-property! 'Label1 'Text "" 'text)
  (set-and-coerce-property! 'TextBox1 'Text "" 'text))

(define-event ResetButton Click ()
  (set-this-form)
  ((get-var p$resetForm)))

(define-event Screen1 Initialize ()
  (set-this-form)
  ((get-var p$resetForm)))
```

## Top-Level Forms

### Event Handler
```scheme
(define-event ComponentName EventName ($param1 $param2 ...)
  (set-this-form)
  <body statements>)
```
- `ComponentName`: the instance name (e.g., `Button1`)
- `EventName`: the event (e.g., `Click`, `GotText`)
- Parameters are prefixed with `$` (some configurations use `$param_` or `$local_` prefixes; both forms are accepted)
- `(set-this-form)` must be the first statement in the body

### Global Variable
```scheme
(def g$variableName <initial-value-expression>)
```
- Variable names are prefixed with `g$`
- Initial value is **required** — every global variable must be initialized with a concrete value. YAIL has no concept of `null`, `nil`, or `undefined`. Use the appropriate zero value for the variable's intended type: `0` for numbers, `""` for text, `#f` for booleans, or an empty list/dictionary.

### Procedure (no return value)
```scheme
(def (p$procedureName $param1 $param2 ...)
  <body statements>)
```
- The body must contain **statement** forms: `set-var!`, `set-lexical!`, `set-and-coerce-property!`, `call-component-method`, `if` (with `begin` branches), `while`, `foreach`, `forrange`, `let`, procedure calls, or `(begin <expr> "ignored")`. Pure value expressions (math, comparisons, text operations, literals) are **not** valid statements.

### Procedure (with return value)
```scheme
(def-return (p$procedureName $param1 $param2 ...)
  <body expression>)
```
- Use `def-return` instead of `def` when the procedure computes and returns a value
- If the body computes a value (e.g., arithmetic, string operations, comparisons), use `def-return` — do **not** use `def` with `(begin <expr> "ignored")`
- Procedure names are prefixed with `p$`
- Parameters are prefixed with `$`

## Expressions

### Literals
- Numbers: `0`, `42`, `-3.14`
- Strings: `"Hello world"`
- Booleans: `#t` (true), `#f` (false)
- Empty string: `""`

### Primitives (call-yail-primitive)
Most operations use this form:
```scheme
(call-yail-primitive <primitive-name>
  (*list-for-runtime* <arg1> <arg2> ...)
  '(<type1> <type2> ...)
  "<description>")
```
The type annotations describe the expected types of each argument. The description string is a human-readable label (e.g., `"select list item"`, `"+"`).

> **Important — type annotations are canonical.** When blocks are read back from the workspace, type annotations are regenerated from the block definition, not preserved from your input. For example, writing `'(list)` for `maxl` will appear as `'(list-of-number)` in the screen state. This is correct — do **not** treat it as an error or attempt to "fix" it. Always use the type annotations specified in the tables below; they match what the workspace will produce.

#### Math Primitives
| Primitive | Description | Arity |
|-----------|-------------|-------|
| `+` | Addition | variadic |
| `-` | Subtraction | binary |
| `*` | Multiplication | variadic |
| `yail-divide` | Division | binary |
| `modulo` | Modulo | binary |
| `expt` | Power | binary |
| `sqrt` | Square root | unary |
| `abs` | Absolute value | unary |
| `-` (unary) | Negate | unary |
| `log` | Natural log (ln) | unary |
| `exp` | e^x | unary |
| `yail-round` | Round | unary |
| `yail-ceiling` | Ceiling | unary |
| `yail-floor` | Floor | unary |
| `sin-degrees` | Sine (degrees) | unary |
| `cos-degrees` | Cosine (degrees) | unary |
| `tan-degrees` | Tangent (degrees) | unary |
| `asin-degrees` | Arc sine (degrees) | unary |
| `acos-degrees` | Arc cosine (degrees) | unary |
| `atan-degrees` | Arc tangent (degrees) | unary |
| `atan2-degrees` | Arc tangent 2 (degrees) | binary |
| `random-integer` | Random integer in range | binary |
| `random-fraction` | Random 0..1 | nullary |
| `random-set-seed` | Set random seed | unary |
| `min` | Minimum | binary |
| `max` | Maximum | binary |
| `remainder` | Remainder | binary |
| `quotient` | Quotient | binary |
| `bitwise-and` | Bitwise AND | binary |
| `bitwise-ior` | Bitwise OR | binary |
| `bitwise-xor` | Bitwise XOR | binary |
| `radians->degrees` | Convert radians to degrees | unary |
| `degrees->radians` | Convert degrees to radians | unary |
| `format-as-decimal` | Format number as decimal | binary |
| `is-number?` | Is it a number? | unary |
| `is-base10?` | Is it base 10? | unary |
| `is-hexadecimal?` | Is it hexadecimal? | unary |
| `is-binary?` | Is it binary? | unary |
| `math-convert-dec-hex` | Decimal to hex | unary |
| `math-convert-hex-dec` | Hex to decimal | unary |
| `math-convert-dec-bin` | Decimal to binary | unary |
| `math-convert-bin-dec` | Binary to decimal | unary |

#### Comparison Primitives
| Primitive | Description |
|-----------|-------------|
| `yail-equal?` | Equal (=) |
| `yail-not-equal?` | Not equal |
| `<` | Less than |
| `<=` | Less than or equal |
| `>` | Greater than |
| `>=` | Greater than or equal |

#### Logic Primitives
| Primitive | Description |
|-----------|-------------|
| `yail-not` | Boolean NOT |

Short-circuit logic uses special forms (variadic, no `begin` wrappers):
```scheme
(and-delayed <expr1> <expr2> ...)  ;; AND (2 or more args)
(or-delayed <expr1> <expr2> ...)   ;; OR (2 or more args)
```

#### Text Primitives
| Primitive | Description |
|-----------|-------------|
| `string-append` | Join strings (variadic) |
| `string-length` | Length of string |
| `string-empty?` | Is string empty? |
| `string-contains` | Contains substring |
| `string-contains-any` | Contains any of list |
| `string-contains-all` | Contains all of list |
| `string-starts-at` | Index of substring |
| `string-replace-all` | Replace all occurrences |
| `string-replace-mappings-longest-string` | Replace using mappings (longest match) |
| `string-replace-mappings-dictionary` | Replace using dictionary mappings |
| `string-split` | Split string |
| `string-split-at-any` | Split at any of given chars |
| `string-split-at-first` | Split at first occurrence |
| `string-split-at-first-of-any` | Split at first of any chars |
| `string-split-at-spaces` | Split at spaces |
| `string-trim` | Trim whitespace |
| `string-substring` | Get substring |
| `string-reverse` | Reverse string |
| `string-to-upper-case` | To uppercase |
| `string-to-lower-case` | To lowercase |
| `string?` | Is it a string? |
| `text-deobfuscate` | Deobfuscate text |
| `string<?` | String less than (comparison) |
| `string>?` | String greater than (comparison) |
| `string=?` | String equal (comparison) |

#### List Primitives

Primitives marked **STATEMENT** are void — they mutate in place and have no output connector. They cannot be nested inside expressions; use them only as standalone statements.

| Primitive | Description |
|-----------|-------------|
| `make-yail-list` | Create list (variadic) |
| `yail-list-get-item` | Get item at index |
| `yail-list-set-item!` | **STATEMENT** — Set item at index |
| `yail-list-length` | List length |
| `yail-list-add-to-list!` | **STATEMENT** — Add items to list (variadic) |
| `yail-list-remove-item!` | **STATEMENT** — Remove item at index |
| `yail-list-insert-item!` | **STATEMENT** — Insert item at index |
| `yail-list-append!` | **STATEMENT** — Append list2 to list1 |
| `yail-list-copy` | Copy list |
| `yail-list-member?` | Is item in list? |
| `yail-list-index` | Index of item |
| `yail-list-pick-random` | Random item |
| `yail-list?` | Is it a list? |
| `yail-list-reverse` | Reverse list |
| `yail-list-join-with-separator` | Join with separator |
| `yail-list-from-csv-table` | Parse CSV table |
| `yail-list-from-csv-row` | Parse CSV row |
| `yail-list-to-csv-table` | To CSV table |
| `yail-list-to-csv-row` | To CSV row |
| `yail-list-sort` | Sort list |
| `yail-list-but-first` | All except first |
| `yail-list-but-last` | All except last |
| `yail-list-slice` | Slice list |
| `yail-list-empty?` | Is list empty? |
| `yail-alist-lookup` | Lookup in list of pairs |

##### Higher-Order List Operations
These use special forms (not `call-yail-primitive`):

**Map** (returns new list):
```scheme
(map_nondest $item <body-expression> <list-expression>)
```

**Filter** (returns new list):
```scheme
(filter_nondest $item <test-expression> <list-expression>)
```

**Reduce** (returns accumulated value):
```scheme
(reduceovereach <init-value> $answerVar $itemVar <combine-expression> <list-expression>)
```

**Sort by comparator** (returns new sorted list):
```scheme
(sortcomparator_nondest $item1 $item2 <compare-expression> <list-expression>)
```
The compare expression should return `#t` if `$item1` should come before `$item2`.

**Sort by key** (returns new sorted list):
```scheme
(sortkey_nondest $item <key-expression> <list-expression>)
```

**Min/Max by comparator** (returns single value):
```scheme
(mincomparator-nondest $item1 $item2 <compare-expression> <list-expression>)
(maxcomparator-nondest $item1 $item2 <compare-expression> <list-expression>)
```

#### Dictionary Primitives

Primitives marked **STATEMENT** are void — they mutate in place and have no output connector. They cannot be nested inside expressions; use them only as standalone statements.

| Primitive | Description |
|-----------|-------------|
| `make-yail-dictionary` | Create dictionary (variadic pairs) |
| `make-dictionary-pair` | Create a key-value pair |
| `yail-dictionary-lookup` | Look up key (with default) |
| `yail-dictionary-set-pair` | **STATEMENT** — Set key-value |
| `yail-dictionary-delete-pair` | **STATEMENT** — Delete key |
| `yail-dictionary-recursive-lookup` | Nested lookup |
| `yail-dictionary-recursive-set` | **STATEMENT** — Nested set |
| `yail-dictionary-get-keys` | Get all keys |
| `yail-dictionary-get-values` | Get all values |
| `yail-dictionary-is-key-in` | Check if key exists |
| `yail-dictionary-length` | Dictionary size |
| `yail-dictionary-alist-to-dict` | List of pairs to dict |
| `yail-dictionary-dict-to-alist` | Dict to list of pairs |
| `yail-dictionary-combine-dicts` | **STATEMENT** — Merge dictionaries |
| `yail-dictionary-copy` | Copy dictionary |
| `yail-dictionary-walk` | Walk tree by key path |
| `yail-dictionary?` | Is it a dictionary? |

##### Dictionary Walk ALL Constant
When using `yail-dictionary-walk`, you can use the ALL wildcard to match all keys at a level:
```scheme
(static-field com.google.appinventor.components.runtime.util.YailDictionary 'ALL)
```

#### Matrix Primitives

Primitives marked **STATEMENT** are void — they mutate in place and have no output connector. They cannot be nested inside expressions; use them only as standalone statements.

| Primitive | Description |
|-----------|-------------|
| `make-yail-matrix` | Create matrix from rows, cols, and cell values |
| `make-yail-matrix-multidim` | Create multidimensional matrix from dimension list |
| `yail-matrix-get-row` | Get a row as a list |
| `yail-matrix-get-column` | Get a column as a list |
| `yail-matrix-get-cell` | Get cell value (variadic indices for n-D) |
| `yail-matrix-set-cell!` | **STATEMENT** — Set cell value (variadic indices) |
| `yail-matrix-get-dims` | Get dimensions as a list |
| `yail-matrix-add` | Matrix addition (variadic) |
| `yail-matrix-subtract` | Matrix subtraction |
| `yail-matrix-multiply` | Matrix multiplication (variadic, supports scalar) |
| `yail-matrix-power` | Matrix exponentiation |
| `yail-matrix-inverse` | Matrix inverse |
| `yail-matrix-transpose` | Transpose matrix |
| `yail-matrix-rotate-left` | Rotate left 90 degrees |
| `yail-matrix-rotate-right` | Rotate right 90 degrees |
| `yail-matrix?` | Is it a matrix? |

#### Color Primitives
| Primitive | Description |
|-----------|-------------|
| `make-color` | Create color from list [R,G,B] or [R,G,B,A] |
| `split-color` | Split color into components |
| `make-exact-yail-integer` | Integer constant (used for color values) |

Color constants are integers:
- Black: `-16777216`, White: `-1`, Red: `-65536`, Green: `-16711936`
- Blue: `-16776961`, Yellow: `-256`, Orange: `-14336`, Pink: `-20561`
- Cyan: `-16711681`, Magenta: `-65281`, Light Gray: `-3355444`, Gray: `-6710887`
- Dark Gray: `-10066330`, None: `0` (alpha=0)

## Component Operations

### Method Call
```scheme
(call-component-method 'ComponentName 'MethodName
  (*list-for-runtime* <arg1> <arg2> ...)
  '(<type1> <type2> ...))
```

### Generic Method Call (any component of a type)
```scheme
(call-component-type-method <component-expression> 'com.google.appinventor.components.runtime.ComponentType 'MethodName
  (*list-for-runtime* <arg1> <arg2> ...)
  '(component <type1> <type2> ...))
```
The component expression precedes the quoted type name. The type list starts with `component`.

### Property Get
```scheme
(get-property 'ComponentName 'PropertyName)
```

### Property Set
```scheme
(set-and-coerce-property! 'ComponentName 'PropertyName <value> '<type>)
```

### Generic Property Get
```scheme
(get-property-and-check  <component-expression> 'com.google.appinventor.components.runtime.ComponentType 'PropertyName)
```

### Generic Property Set
```scheme
(set-and-coerce-property-and-check! <component-expression> 'com.google.appinventor.components.runtime.ComponentType 'PropertyName <value> '<type>)
```

### Component Reference
```scheme
(get-component ComponentName)
```

### All Components of a Type
```scheme
(get-all-components com.google.appinventor.components.runtime.ComponentType)
```
Returns a list of all components of the given type in the current screen.

### Generic Event Handler
```scheme
(define-generic-event ComponentType EventName ($component $param1 $param2 ...)
  (set-this-form)
  <body statements>)
```
Handles events for any component of the given type, rather than a specific instance.
The first parameter (`$component`) is always the component instance that fired the event.

### Blocking Continuation Methods
Some methods use an async variant with the same arguments:
```scheme
(call-component-method-with-blocking-continuation 'ComponentName 'MethodName
  (*list-for-runtime* <args>) '(<types>))
(call-component-type-method-with-blocking-continuation <comp-expr> 'TypeName 'MethodName
  (*list-for-runtime* <args>) '(component <types>))
```
These produce the same block types as their non-blocking counterparts.

### Enum Dropdown (static-field)
```scheme
(static-field com.example.ClassName "ENUM_NAME")
```
Creates an enum dropdown block. The class name is the full Java class of the option list (provided in `lookup_component` results), and the enum name is the selected value. Use this as the value expression in `set-and-coerce-property!` for enum-typed properties. Example:
```scheme
(set-and-coerce-property! 'Trendline1 'Model
  (static-field com.google.appinventor.components.common.BestFitModel "Linear")
  'com.google.appinventor.components.common.BestFitModelEnum)
```
Do **not** use a plain string for enum properties — always use `static-field` so the correct dropdown block is created.

## Control Flow

### If / If-Else
```scheme
;; if only (no else)
(if <condition>
  (begin <then-statements>))

;; if-else
(if <condition>
  (begin <then-statements>)
  (begin <else-statements>))
```
**Empty branches:** When logic is only needed for the false case, negate the condition and use if-only (no else) instead of an if-else with an empty then-branch:
```scheme
;; CORRECT — negate and use if-only
(if (call-yail-primitive yail-not
      (*list-for-runtime* <condition>)
      '(boolean) "not")
  (begin <body>))

;; WRONG — do not use if-else with a dummy then-branch
(if <condition>
  (begin "ignored")
  (begin <body>))
```

Nested if-else for elseif chains:
```scheme
(if <cond1>
  (begin <body1>)
  (begin (if <cond2>
    (begin <body2>)
    (begin <else-body>))))
```

### Choose (if-expression, returns a value)
```scheme
(if <condition> <then-expression> <else-expression>)
```
Unlike the statement `if`, choose does not wrap branches in `(begin ...)`.

### While Loop
```scheme
(while <test-expression>
  (begin <body-statements>))
```

### For Range
```scheme
(forrange $i
  (begin <body>)
  <start> <end> <step>)
```
Note: the body comes before the start/end/step arguments.

### For Each (list)
```scheme
(foreach $item
  (begin <body>)
  <list-expression>)
```
Note: the body comes before the list argument.

### For Each (dictionary)
```scheme
(foreach $item
  (let (($key (call-yail-primitive yail-list-get-item (*list-for-runtime* (lexical-value $item) 1) '(list number) "select list item"))
        ($value (call-yail-primitive yail-list-get-item (*list-for-runtime* (lexical-value $item) 2) '(list number) "select list item")))
    <body>)
  <dict-expression>)
```
The dictionary foreach iterates over a dictionary expression directly. A hidden iteration variable `$item` receives each key-value pair, and a nested `let` extracts `$key` and `$value` from it using list indexing.

### Do-Then-Return (evaluate statements, return expression)
```scheme
(begin
  <statements>
  <return-expression>)
```

### Local Variable Declaration (statement)
```scheme
(let (($localVar <init-value>))
  <body-statements>)
```
Multiple local variables:
```scheme
(let (($var1 <init1>) ($var2 <init2>))
  <body-statements>)
```

### Local Variable Declaration (expression, returns a value)
```scheme
(let (($localVar <init-value>))
  <return-expression>)
```

### Break (exit loop early)
```scheme
(*yail-break* #f)
```
Exits the enclosing `foreach`, `forrange`, or `while` loop.

### Run In Background
```scheme
(call-yail-primitive run-in-background
  (*list-for-runtime* <procedure-expression> <callback-expression>)
  '(any any) "run in background")
```
Runs the first procedure in a background thread. When it completes, the callback procedure is called. Both arguments must be procedure values (use anonymous procedures or `get-var p$name`).

### Run After Period
```scheme
(call-yail-primitive run-after-period
  (*list-for-runtime* <millis-expression> <procedure-expression>)
  '(any any) "run after period")
```
Runs the procedure after a delay of the given number of milliseconds. Note: **millis comes first**, procedure second.

### Evaluate But Ignore (execute expression, discard result)
```scheme
(begin <expression> "ignored")
```
This wraps a **return-value expression** so it can be used in statement position (the `controls_eval_but_ignore` block). Use it **only** when calling a return-value procedure or method for its side effects while discarding the result:
```scheme
;; OK — calling a return procedure for its side effects
(begin ((get-var p$computeAndSave) 42) "ignored")

;; OK — calling a method that returns a value you don't need
(begin (call-component-method 'Web1 'UriEncode (*list-for-runtime* "test") '(text)) "ignored")
```
Do **not** use this with pure value expressions (math, comparisons, text operations, variable gets, literals) — those have no side effects, so evaluating and ignoring them is pointless and likely a logic error. Do **not** use `(begin "ignored")` alone as a "do nothing" placeholder.
```scheme
;; BAD — pure expression with no side effects
(begin (call-yail-primitive + (*list-for-runtime* 1 2) '(number number) "+") "ignored")

;; BAD — if you need the result, use def-return instead of def
(def (p$wrong) (begin (call-yail-primitive + (*list-for-runtime* 1 2) '(number number) "+") "ignored"))
;; GOOD
(def-return (p$right) (call-yail-primitive + (*list-for-runtime* 1 2) '(number number) "+"))
```

### No Null Concept
YAIL has **no** `null` literal, keyword, or "nothing" block. Do not use `null`, `(get-var g$null)`, or `(get-var *the-null-value*)` — none of these are valid. Always use a typed zero value instead: `0` for numbers, `""` for text, `#f` for booleans, or an empty list/dictionary.

## Variables

### Global Variable Get
```scheme
(get-var g$variableName)
```

### Global Variable Set
```scheme
(set-var! g$variableName <value>)
```

### Local/Parameter Variable Get
```scheme
(lexical-value $paramName)
```

### Local/Parameter Variable Set
```scheme
(set-lexical! $localVar <value>)
```

## Procedure Calls

### Call Procedure (no return)
```scheme
((get-var p$procedureName) <arg1> <arg2> ...)
```

### Call Procedure (with return)
```scheme
((get-var p$procedureName) <arg1> <arg2> ...)
```
(Same syntax; context determines if return value is used.)

## Anonymous Procedures

Anonymous procedures (lambdas) create first-class procedure values that can be stored in variables, passed as arguments, and called dynamically.

### Create Anonymous Procedure (no return value)
```scheme
(call-yail-primitive create-yail-procedure
  (*list-for-runtime* (lambda ($param1 $param2 ...)
    <body-statements>))
  '(any) "create procedure")
```
- The body contains **statement** forms (same rules as `def` procedures)
- This is an **expression** — it returns a procedure value

### Create Anonymous Procedure (with return value)
```scheme
(call-yail-primitive create-yail-procedure
  (*list-for-runtime* (lambda ($param1 $param2 ...)
    <return-expression>))
  '(any) "create procedure")
```
- The body is a single **expression** that computes the return value
- This is an **expression** — it returns a procedure value

### Call Anonymous Procedure (no return, statement)
```scheme
(call-yail-primitive call-yail-procedure
  (*list-for-runtime* <procedure-expression> <arg1> <arg2> ...)
  '(any any any ...) "call procedure")
```
- The first argument is the procedure value; remaining arguments are passed to it
- This form is a **statement** when the procedure has no return value
- The type list has one `any` per argument (including the procedure itself)

### Call Anonymous Procedure (with return, expression)
```scheme
(call-yail-primitive call-yail-procedure
  (*list-for-runtime* <procedure-expression> <arg1> <arg2> ...)
  '(any any any ...) "call procedure")
```
- Same YAIL as the no-return variant — context determines whether it is a statement or expression
- Use as an **expression** when the procedure returns a value

### Call Anonymous Procedure With Input List (no return)
```scheme
(call-yail-primitive call-yail-procedure-input-list
  (*list-for-runtime* <procedure-expression> <list-expression>)
  '(any any) "call procedure(with input list)")
```
- Calls the procedure with arguments taken from a list
- This form is a **statement** when the procedure has no return value

### Call Anonymous Procedure With Input List (with return)
Same YAIL form, used as an **expression** when the procedure returns a value.

### Get Number of Arguments
```scheme
(call-yail-primitive num-args-yail-procedure
  (*list-for-runtime* <procedure-expression>)
  '(any) "get number of arguments")
```
Returns the number of parameters the procedure expects.

### Get Procedure by Name (text)
```scheme
(call-yail-primitive create-yail-procedure-with-name
  (*list-for-runtime* <text-expression>)
  '(any) "get procedure")
```
Looks up a named procedure by its name as a text string and returns it as a value.

### Get Named Procedure as Value (dropdown)
```scheme
(call-yail-primitive create-yail-procedure
  (*list-for-runtime* (get-var p$procedureName))
  '(any) "get procedure")
```
Gets a reference to a named procedure. Note: this uses the same `create-yail-procedure` primitive but with a `(get-var p$...)` argument instead of a `(lambda ...)` argument.

## Screen Operations

### Open Another Screen
```scheme
(call-yail-primitive open-another-screen (*list-for-runtime* "ScreenName") '(text) "open another screen")
```

### Open Screen With Start Value
```scheme
(call-yail-primitive open-another-screen-with-start-value
  (*list-for-runtime* "ScreenName" <value>) '(text any) "open another screen with start value")
```

### Close Screen
```scheme
(call-yail-primitive close-screen (*list-for-runtime*) '() "close screen")
```

### Close Screen With Value
```scheme
(call-yail-primitive close-screen-with-value (*list-for-runtime* <value>) '(any) "close screen with value")
```

### Close Screen With Plain Text
```scheme
(call-yail-primitive close-screen-with-plain-text (*list-for-runtime* <text>) '(text) "close screen with plain text")
```

### Get Start Value
```scheme
(call-yail-primitive get-start-value (*list-for-runtime*) '() "get start value")
```

### Get Plain Start Text
```scheme
(call-yail-primitive get-plain-start-text (*list-for-runtime*) '() "get plain start text")
```

### Close Application
```scheme
(call-yail-primitive close-application (*list-for-runtime*) '() "close application")
```

## Examples

### Simple event handler
```scheme
(define-event Button1 Click ()
  (set-this-form)
  (call-component-method 'Notifier1 'ShowAlert
    (*list-for-runtime* "Hello!") '(text)))
```

### Global variable with list initial value
```scheme
(def g$myList (call-yail-primitive make-yail-list (*list-for-runtime* 1 2 3) '(number number number) "make a list"))
```

### Procedure with return value
```scheme
(def-return (p$factorial $n)
  (if (call-yail-primitive yail-equal? (*list-for-runtime* (lexical-value $n) 0) '(number number) "=")
    (begin 1)
    (begin
      (call-yail-primitive *
        (*list-for-runtime*
          (lexical-value $n)
          ((get-var p$factorial)
            (call-yail-primitive -
              (*list-for-runtime* (lexical-value $n) 1)
              '(number number) "-")))
        '(number number) "*"))))
```

### Event handler with if-else and variable update
```scheme
(define-event Button1 Click ()
  (set-this-form)
  (if (call-yail-primitive yail-equal?
        (*list-for-runtime* (get-var g$score) 10)
        '(number number) "=")
    (begin
      (call-component-method 'Notifier1 'ShowAlert
        (*list-for-runtime* "You win!") '(text)))
    (begin
      (set-var! g$score
        (call-yail-primitive +
          (*list-for-runtime* (get-var g$score) 1)
          '(number number) "+"))
      (set-and-coerce-property! 'Label1 'Text
        (call-yail-primitive string-append
          (*list-for-runtime* "Score: " (get-var g$score))
          '(text text) "join")
        'text))))
```

### Using local variables
```scheme
(define-event Button1 Click ()
  (set-this-form)
  (let (($total (call-yail-primitive +
                  (*list-for-runtime* (get-var g$price) (get-var g$tax))
                  '(number number) "+")))
    (begin
      (set-and-coerce-property! 'Label1 'Text (lexical-value $total) 'text))))
```

### Generic event handler (any Button)
```scheme
(define-generic-event Button Click ($component)
  (set-this-form)
  (set-and-coerce-property! 'Label1 'Text "A button was clicked" 'text))
```

---

## Argument Order Reference for `call-yail-primitive`

The argument order inside `(*list-for-runtime* ...)` is critical. The arguments must appear in the exact order shown below. Getting the order wrong will cause runtime errors or incorrect behavior.

Recall the general form:
```scheme
(call-yail-primitive PRIMITIVE_NAME
  (*list-for-runtime* ARG1 ARG2 ...)
  '(TYPE1 TYPE2 ...) "description")
```

### Math Primitives

| Primitive | Args in `*list-for-runtime*` | Types | Description |
|---|---|---|---|
| `+` | `a b` | `(number number)` | addition |
| `-` | `a b` | `(number number)` | subtraction |
| `*` | `a b` | `(number number)` | multiplication |
| `yail-divide` | `a b` | `(number number)` | division |
| `modulo` | `a b` | `(number number)` | modulo |
| `expt` | `base exp` | `(number number)` | power |
| `atan2-degrees` | `y x` | `(number number)` | atan2 (y FIRST, then x) |
| `format-as-decimal` | `number places` | `(number number)` | format decimal |
| `random-integer` | `low high` | `(number number)` | random integer in range |
| `yail-floor` | `n` | `(number)` | floor |
| `yail-ceiling` | `n` | `(number)` | ceiling |
| `yail-round` | `n` | `(number)` | round |
| `sin-degrees` | `n` | `(number)` | sine (degrees) |
| `cos-degrees` | `n` | `(number)` | cosine (degrees) |
| `tan-degrees` | `n` | `(number)` | tangent (degrees) |
| `asin-degrees` | `n` | `(number)` | arcsine (degrees) |
| `acos-degrees` | `n` | `(number)` | arccosine (degrees) |
| `atan-degrees` | `n` | `(number)` | arctangent (degrees) |
| `math-convert-dec-hex` | `n` | `(text)` | decimal to hex |
| `math-convert-hex-dec` | `s` | `(text)` | hex to decimal |
| `math-convert-bin-dec` | `s` | `(text)` | binary to decimal |
| `math-convert-dec-bin` | `n` | `(text)` | decimal to binary |
| `random-fraction` | *(none)* | `()` | random float 0–1 |
| `random-set-seed` | `seed` | `(number)` | set random seed |

### Math on List (Statistics) Primitives

These operate on a single list argument. The type annotation is `(list-of-number)`, **not** `(list)`.

| Primitive | Args in `*list-for-runtime*` | Types | Description |
|---|---|---|---|
| `avg` | `list` | `(list-of-number)` | average |
| `minl` | `list` | `(list-of-number)` | minimum of list |
| `maxl` | `list` | `(list-of-number)` | maximum of list |
| `gm` | `list` | `(list-of-number)` | geometric mean |
| `std-dev` | `list` | `(list-of-number)` | standard deviation |
| `std-err` | `list` | `(list-of-number)` | standard error |
| `mode` | `list` | `(list-of-number)` | mode |

### Comparison Primitives

| Primitive | Args in `*list-for-runtime*` | Types | Description |
|---|---|---|---|
| `yail-equal?` | `a b` | `(any any)` | generic equality |
| `yail-not-equal?` | `a b` | `(any any)` | generic not-equal |
| `<` | `a b` | `(number number)` | less than |
| `>` | `a b` | `(number number)` | greater than |
| `<=` | `a b` | `(number number)` | less or equal |
| `>=` | `a b` | `(number number)` | greater or equal |
| `string=?` | `a b` | `(text text)` | text equality |
| `string<?` | `a b` | `(text text)` | text less than |
| `string>?` | `a b` | `(text text)` | text greater than |

> **Note:** Text not-equal uses `(not (call-yail-primitive string=? ...))`.

### Text Primitives

| Primitive | Args in `*list-for-runtime*` | Types | Description |
|---|---|---|---|
| `string-append` | `s1 s2 ...` | `(text text ...)` | join (variadic) |
| `string-length` | `s` | `(text)` | length |
| `string-substring` | `text start length` | `(text number number)` | substring (**3rd arg is LENGTH, not end index**) |
| `string-replace-all` | `text needle replacement` | `(text text text)` | replace all |
| `string-contains` | `text piece` | `(text text)` | contains check |
| `string-starts-at` | `text piece` | `(text text)` | find index of piece in text |
| `string-split-at-first` | `text at` | `(text text)` | split at first occurrence |
| `string-split-at-first-of-any` | `text at` | `(text list)` | split at first of any |
| `string-split` | `text at` | `(text text)` | split at every occurrence |
| `string-split-at-any` | `text at` | `(text list)` | split at any |
| `string-split-at-spaces` | `text` | `(text)` | split at spaces |
| `string-trim` | `text` | `(text)` | trim whitespace |
| `string-to-upper-case` | `text` | `(text)` | uppercase |
| `string-to-lower-case` | `text` | `(text)` | lowercase |
| `string-reverse` | `text` | `(text)` | reverse |
| `text-deobfuscate` | `text confounder` | `(text number)` | deobfuscate |
| `string-empty?` | `text` | `(text)` | is empty check |

### List Primitives

Primitives marked **STATEMENT** are void — they mutate in place, have no output connector, and **cannot be nested inside expressions**. Use them only as standalone statements.

| Primitive | Args in `*list-for-runtime*` | Types | Description |
|---|---|---|---|
| `make-yail-list` | `item1 item2 ...` | `(any any ...)` | create list (variadic) |
| `yail-list-length` | `list` | `(list)` | list length |
| `yail-list-copy` | `list` | `(list)` | copy list |
| `yail-list-reverse` | `list` | `(list)` | reverse list |
| `yail-list-to-csv-row` | `list` | `(list)` | list to CSV row |
| `yail-list-to-csv-table` | `list` | `(list)` | list to CSV table |
| `yail-list-from-csv-row` | `text` | `(text)` | CSV row to list |
| `yail-list-from-csv-table` | `text` | `(text)` | CSV table to list |
| `yail-list-member?` | `item list` | `(any list)` | member check (**item FIRST**) |
| `yail-list-index` | `item list` | `(any list)` | index of (**item FIRST**) |
| `yail-list-get-item` | `list index` | `(list number)` | get item at index |
| `yail-list-remove-item!` | `list index` | `(list number)` | **STATEMENT** — remove item at index |
| `yail-list-set-item!` | `list index value` | `(list number any)` | **STATEMENT** — set item at index |
| `yail-list-insert-item!` | `list index item` | `(list number any)` | **STATEMENT** — insert at index |
| `yail-list-append!` | `list1 list2` | `(list list)` | **STATEMENT** — append list2 to list1 |
| `yail-list-join-with-separator` | `list separator` | `(list text)` | join with separator |
| `yail-list-pick-random` | `list` | `(list)` | pick random item |
| `yail-list-add-to-list!` | `list item1 item2 ...` | `(list any any ...)` | **STATEMENT** — add items to list (variadic) |
| `yail-list-empty?` | `list` | `(list)` | is list empty |
| `yail-list-slice` | `list index1 index2` | `(list number number)` | slice from index1 to index2 |
| `yail-alist-lookup` | `key list default` | `(any list any)` | lookup in list of pairs |

### Dictionary Primitives

Primitives marked **STATEMENT** are void — they mutate in place, have no output connector, and **cannot be nested inside expressions**.

| Primitive | Args in `*list-for-runtime*` | Types | Description |
|---|---|---|---|
| `make-yail-dictionary` | `pair1 pair2 ...` | `(pair pair ...)` | create dict (variadic) |
| `make-dictionary-pair` | `key value` | `(key any)` | create key-value pair |
| `yail-dictionary-set-pair` | `key dict value` | `(any dict any)` | **STATEMENT** — set pair (**key, dict, value** — unusual order!) |
| `yail-dictionary-delete-pair` | `dict key` | `(dict any)` | **STATEMENT** — delete pair (**dict, key** — different order from set!) |
| `yail-dictionary-lookup` | `key dict default` | `(any dict any)` | lookup with default |
| `yail-dictionary-recursive-lookup` | `keys dict default` | `(list dict any)` | recursive lookup |
| `yail-dictionary-recursive-set` | `keys dict value` | `(list dict any)` | **STATEMENT** — recursive set |
| `yail-dictionary-walk` | `path dict` | `(any dict)` | walk path |
| `yail-dictionary-is-key-in` | `key dict` | `(any dict)` | key exists check |
| `yail-dictionary-length` | `dict` | `(dict)` | dictionary size |
| `yail-dictionary-alist-to-dict` | `list` | `(list)` | pairs list to dict |
| `yail-dictionary-dict-to-alist` | `dict` | `(dict)` | dict to pairs list |
| `yail-dictionary-combine-dicts` | `dict1 dict2` | `(dict dict)` | **STATEMENT** — merge dict2 into dict1 |
| `yail-dictionary-is-dict?` | `thing` | `(any)` | is dictionary check |

### Color Primitives

| Primitive | Args in `*list-for-runtime*` | Types | Description |
|---|---|---|---|
| `make-color` | `colorList` | `(list)` | make color from RGB(A) list |
| `split-color` | `color` | `(number)` | split color to components |

### Screen Primitives

| Primitive | Args in `*list-for-runtime*` | Types | Description |
|---|---|---|---|
| `open-another-screen` | `screenName` | `(text)` | open screen |
| `open-another-screen-with-start-value` | `screenName value` | `(text any)` | open screen with value |
| `close-screen-with-value` | `value` | `(any)` | close screen returning value |
| `close-screen-with-plain-text` | `text` | `(text)` | close screen returning plain text |

### Matrix Primitives

| Primitive | Args in `*list-for-runtime*` | Types | Description |
|---|---|---|---|
| `make-yail-matrix` | `rows cols v00 v01 ... vNN` | `(number number number ...)` | create matrix (flat row-major values) |
| `make-yail-matrix-multidim` | `dimList initialValue` | `(list number)` | create n-D matrix |
| `yail-matrix-get-row` | `matrix rowIndex` | `(matrix number)` | get row as list |
| `yail-matrix-get-column` | `matrix colIndex` | `(matrix number)` | get column as list |
| `yail-matrix-get-cell` | `matrix idx1 idx2 ...` | `(matrix number number ...)` | get cell (variadic indices) |
| `yail-matrix-set-cell!` | `matrix value idx1 idx2 ...` | `(matrix number number ...)` | **STATEMENT** — set cell (**value BEFORE indices**) |
| `yail-matrix-get-dims` | `matrix` | `(matrix)` | get dimensions as list |
| `yail-matrix-add` | `mat1 mat2 ...` | `(matrix matrix ...)` | addition (variadic) |
| `yail-matrix-subtract` | `matA matB` | `(matrix matrix)` | subtraction |
| `yail-matrix-multiply` | `mat1 val2 ...` | `(matrix any ...)` | multiplication (**subsequent args can be matrix or scalar**) |
| `yail-matrix-power` | `matrix exponent` | `(matrix number)` | exponentiation |
| `yail-matrix-inverse` | `matrix` | `(matrix)` | inverse |
| `yail-matrix-transpose` | `matrix` | `(matrix)` | transpose |
| `yail-matrix-rotate-left` | `matrix` | `(matrix)` | rotate left 90 degrees |
| `yail-matrix-rotate-right` | `matrix` | `(matrix)` | rotate right 90 degrees |
| `yail-matrix?` | `value` | `(any)` | is matrix check |

> **Note:** `yail-matrix-set-cell!` argument order is `(matrix, value, idx1, idx2, ...)` — the value comes BEFORE the indices. This differs from list's `yail-list-set-item!` which takes `(list, index, value)`.

> **Note:** `yail-matrix-multiply` uses `(matrix any ...)` types because the second and subsequent operands can be either matrices (matrix multiplication) or numbers (scalar multiplication).

> **Note:** `yail-matrix-set-cell!` is a **STATEMENT** — it mutates in place and has no output connector. Do NOT nest it inside expressions. Use the same pattern as list statement primitives: mutate in a `let`/`begin`, then reference the variable.

### Anonymous Procedure Primitives

| Primitive | Args in `*list-for-runtime*` | Types | Description |
|---|---|---|---|
| `create-yail-procedure` | `(lambda ($params) body)` | `(any)` | create anonymous procedure |
| `create-yail-procedure` | `(get-var p$name)` | `(any)` | get named procedure as value |
| `create-yail-procedure-with-name` | `textName` | `(any)` | get procedure by text name |
| `call-yail-procedure` | `proc arg1 arg2 ...` | `(any any ...)` | call procedure (**proc FIRST**) |
| `call-yail-procedure-input-list` | `proc argList` | `(any any)` | call procedure with input list |
| `num-args-yail-procedure` | `proc` | `(any)` | get argument count |
| `run-in-background` | `proc callback` | `(any any)` | run in background |
| `run-after-period` | `millis proc` | `(any any)` | run after delay (**millis FIRST**) |

> **Note:** `run-after-period` takes millis BEFORE the procedure — `(millis, procedure)`. This is the opposite of `run-in-background` which takes `(procedure, callback)`.

### Critical Notes

1. **`string-substring` uses LENGTH**: The third argument is the length of the substring, NOT an end index. Example: `(call-yail-primitive string-substring (*list-for-runtime* "hello" 1 3) '(text number number) "segment")` returns `"hel"`.

2. **`yail-list-member?` and `yail-list-index` take item BEFORE list**: Unlike most list operations, these take the item as the first argument and the list second.

3. **Dictionary `set-pair` vs `delete-pair` have DIFFERENT argument orders**: `set-pair` is `(key, dict, value)` but `delete-pair` is `(dict, key)`. Do not confuse them.

4. **`atan2-degrees` takes y BEFORE x**: This follows the standard mathematical convention `atan2(y, x)`.

5. **Variadic primitives**: `string-append`, `make-yail-list`, `make-yail-dictionary`, `+`, `-`, `*` can take 2 or more arguments. The types list must have a matching number of type entries.

6. **Statement primitives are NOT values**: Mutating primitives (`yail-list-append!`, `yail-list-add-to-list!`, `yail-list-set-item!`, `yail-list-remove-item!`, `yail-list-insert-item!`, `yail-dictionary-set-pair`, `yail-dictionary-delete-pair`, `yail-dictionary-recursive-set`, `yail-dictionary-combine-dicts`) are statement blocks with no output. They **CANNOT** be used as arguments to other expressions — doing so produces disconnected blocks. Use `let` to bind the collection, mutate it with the statement primitive, then reference the variable.

7. **`yail-matrix-set-cell!` is a STATEMENT**: Like list mutation primitives, `yail-matrix-set-cell!` mutates in place and cannot be nested in expressions. Its argument order is `(matrix, value, idx1, idx2, ...)` — the value comes before the dimension indices.

8. **Anonymous procedure YAIL is the same for statement and expression**: `call-yail-procedure` generates identical YAIL whether it returns a value or not. In statement context (inside `begin`, event handler body), it produces a statement block. In expression context (inside property set, argument to another call), it produces an expression block.

9. **No `string->number` or type-conversion primitives.** YAIL has **no** `string->number`, `number->string`, `string-to-number`, or similar conversion primitives. Math primitives (`+`, `-`, `*`, `yail-divide`, etc.) **automatically coerce** text arguments to numbers at runtime. To do arithmetic with TextBox input, pass the text value directly to the math primitive with `'(number number)` types — YAIL handles the conversion:
```scheme
;; CORRECT — pass TextBox text directly, use '(number number) types
(call-yail-primitive +
  (*list-for-runtime* (get-property 'NumberBox1 'Text) (get-property 'NumberBox2 'Text))
  '(number number) "+")

;; WRONG — string->number does not exist
(call-yail-primitive string->number
  (*list-for-runtime* (get-property 'NumberBox1 'Text))
  '(text) "convert")
```
Similarly, when setting a text property to a number result, the coercion to text happens automatically via `'text` in the `set-and-coerce-property!` type argument.

---

## Real-World Examples

The following examples are drawn from real App Inventor projects and demonstrate common YAIL patterns.

### Procedure returning boolean with or-delayed
Check if a string starts with "http://" or "https://":
```scheme
(def-return (p$isAPK $input)
  (or-delayed
    (call-yail-primitive yail-equal?
      (*list-for-runtime*
        (call-yail-primitive string-starts-at
          (*list-for-runtime* (lexical-value $input) "http://")
          '(text text) "starts at")
        1)
      '(any any) "=")
    (call-yail-primitive yail-equal?
      (*list-for-runtime*
        (call-yail-primitive string-starts-at
          (*list-for-runtime* (lexical-value $input) "https://")
          '(text text) "starts at")
        1)
      '(any any) "=")))
```
Note: `string-starts-at` returns the 1-based index (or 0 if not found). Comparing its result to `1` checks if the string starts with the piece. Arguments to `or-delayed` are bare expressions with no `(begin ...)` wrappers.

### Procedure with string-split and list indexing
Parse a code string containing a semicolon-separated server address and code:
```scheme
(def-return (p$extractServer $code)
  (if (call-yail-primitive string-contains
        (*list-for-runtime* (lexical-value $code) ";")
        '(text text) "string contains")
    (let (($codes (call-yail-primitive string-split
                    (*list-for-runtime* (lexical-value $code) ";")
                    '(text text) "split")))
      (begin
        ((get-var p$setServer)
          (call-yail-primitive yail-list-get-item
            (*list-for-runtime* (lexical-value $codes) 1)
            '(list number) "select list item")
          #f)
        (call-yail-primitive yail-list-get-item
          (*list-for-runtime* (lexical-value $codes) 2)
          '(list number) "select list item")))
    (lexical-value $code)))
```
Note: `string-split` returns a yail list. `yail-list-get-item` takes `(list, index)` — the list comes first. Procedure calls use `((get-var p$name) arg1 arg2)` form.

### Event handler with property negation
Toggle a boolean property using `yail-not`:
```scheme
(define-event CheckBox1 Changed ()
  (set-this-form)
  (set-and-coerce-property! 'PhoneStatus1 'WebRTC
    (call-yail-primitive yail-not
      (*list-for-runtime* (get-property 'CheckBox1 'Checked))
      '(boolean) "not")
    'boolean))
```

### Nested list construction for key-value data
Build a list of key-value pairs for a POST request:
```scheme
(call-yail-primitive make-yail-list
  (*list-for-runtime*
    (call-yail-primitive make-yail-list
      (*list-for-runtime* "key" (lexical-value $code))
      '(any any) "make a list")
    (call-yail-primitive make-yail-list
      (*list-for-runtime* "ipaddr" (call-component-method 'PhoneStatus1 'GetWifiIpAddress (*list-for-runtime*) '()))
      '(any any) "make a list")
    (call-yail-primitive make-yail-list
      (*list-for-runtime* "version" (call-component-method 'PhoneStatus1 'GetVersionName (*list-for-runtime*) '()))
      '(any any) "make a list"))
  '(any any any) "make a list")
```
Note: each inner `make-yail-list` creates a 2-element pair. The outer `make-yail-list` groups them. The types list has one entry per argument.

### Event with string-starts-at comparison (WiFi check)
Check if a string starts with "Error":
```scheme
(define-event Screen1 Initialize ()
  (set-this-form)
  (if (call-yail-primitive yail-equal?
        (*list-for-runtime*
          1
          (call-yail-primitive string-starts-at
            (*list-for-runtime*
              (call-component-method 'PhoneStatus1 'GetWifiIpAddress
                (*list-for-runtime*) '())
              "Error")
            '(text text) "starts at"))
        '(any any) "=")
    (begin
      (call-component-method 'Notifier1 'ShowChooseDialog
        (*list-for-runtime*
          "Your Device does not appear to have a WiFi Connection"
          "No WiFi"
          "Continue without WiFi"
          "Exit"
          #f)
        '(text text text text boolean)))))
```

### String-append with 3+ arguments
Build a URL from parts:
```scheme
(set-and-coerce-property! 'Web1 'Url
  (call-yail-primitive string-append
    (*list-for-runtime* "http://" (get-var g$server) "/rendezvous/")
    '(text text text) "join")
  'text)
```
Note: `string-append` is variadic — the types list must have one `text` entry per argument.

### Procedure call with conditional branching
Process input differently based on a check:
```scheme
(def (p$processcode $x)
  (if ((get-var p$isAPK) (lexical-value $x))
    (begin
      (call-component-method 'PhoneStatus1 'installURL
        (*list-for-runtime* (lexical-value $x)) '(text)))
    (begin
      (set-and-coerce-property! 'PhoneStatus1 'WebRTC
        (call-yail-primitive yail-not
          (*list-for-runtime* (get-property 'CheckBox1 'Checked))
          '(boolean) "not")
        'boolean)
      ((get-var p$callrendezvous)
        (call-component-method 'PhoneStatus1 'setHmacSeedReturnCode
          (*list-for-runtime*
            ((get-var p$extractServer) (lexical-value $x))
            (get-var g$server))
          '(text text))))))
```
Note: procedure calls `((get-var p$name) args...)` can be nested as arguments to other calls. `call-component-method` return values can be used directly as arguments.

### Creating and accessing a matrix
```scheme
;; Create a 2x3 matrix with literal values
(def g$myMatrix
  (call-yail-primitive make-yail-matrix
    (*list-for-runtime* 2 3 1 2 3 4 5 6)
    '(number number number number number number number number)
    "create a matrix"))

;; Get the value at row 1, column 2
(define-event Button1 Click ()
  (set-this-form)
  (set-and-coerce-property! 'Label1 'Text
    (call-yail-primitive yail-matrix-get-cell
      (*list-for-runtime* (get-var g$myMatrix) 1 2)
      '(matrix number number)
      "get matrix cell")
    'text))
```

### Matrix arithmetic and set cell
```scheme
;; Add two matrices and store result
(define-event AddButton Click ()
  (set-this-form)
  (set-var! g$result
    (call-yail-primitive yail-matrix-add
      (*list-for-runtime* (get-var g$matA) (get-var g$matB))
      '(matrix matrix) "yail-matrix-add")))

;; Set a cell value (STATEMENT — standalone, not nested)
(define-event SetButton Click ()
  (set-this-form)
  (call-yail-primitive yail-matrix-set-cell!
    (*list-for-runtime* (get-var g$myMatrix) 99 1 2)
    '(matrix number number number) "set matrix cell"))
```

### Anonymous procedure stored in variable
```scheme
;; Create an anonymous procedure that doubles a number
(def g$doubler
  (call-yail-primitive create-yail-procedure
    (*list-for-runtime* (lambda ($x)
      (call-yail-primitive *
        (*list-for-runtime* (lexical-value $x) 2)
        '(number number) "*")))
    '(any) "create procedure"))

;; Call it
(define-event Button1 Click ()
  (set-this-form)
  (set-and-coerce-property! 'Label1 'Text
    (call-yail-primitive call-yail-procedure
      (*list-for-runtime* (get-var g$doubler) 21)
      '(any any) "call procedure")
    'text))
```

### Run after delay
```scheme
;; Show a notification after 2 seconds
(define-event Button1 Click ()
  (set-this-form)
  (call-yail-primitive run-after-period
    (*list-for-runtime* 2000
      (call-yail-primitive create-yail-procedure
        (*list-for-runtime* (lambda ()
          (call-component-method 'Notifier1 'ShowAlert
            (*list-for-runtime* "Time's up!") '(text))))
        '(any) "create procedure"))
    '(any any) "run after period"))
```
