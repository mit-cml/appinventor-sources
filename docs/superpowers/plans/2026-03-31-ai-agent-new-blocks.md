# AI Agent: Support New Blocks from Master Merge

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Enable the AI agent to generate and round-trip YAIL for lambda/anonymous procedures, matrix blocks, and new control blocks introduced by the master merge.

**Architecture:** Four files need updates: (1) the YAIL-to-blocks converter (`yail_to_blocks.js`) needs PRIMITIVE_MAP_ entries and custom converters so `write_block` can parse the new YAIL back into Blockly blocks; (2) the grammar reference (`yail_grammar.md`) needs documentation of the new YAIL constructs; (3) the platform reference (`appinventor_reference.md`) needs the matrices category added; (4) the few-shot examples need matrix and lambda examples.

**Tech Stack:** JavaScript (Blockly/YAIL converter), Markdown (grammar/reference docs), JSON (examples)

---

### Task 1: Add matrix primitives to PRIMITIVE_MAP_

**Files:**
- Modify: `appinventor/blocklyeditor/src/ai/yail_to_blocks.js:1494` (insert before closing brace of PRIMITIVE_MAP_)

These primitives follow standard patterns already handled by `convertPrimitive_`. The variadic blocks use `domToMutation` with `items` attribute and `repeatingInputName` — the existing variadic logic handles this correctly.

- [ ] **Step 1: Add simple matrix primitives to PRIMITIVE_MAP_**

Insert before the closing `};` of `PRIMITIVE_MAP_` (after the `get-plain-start-text` entry at line 1494):

```javascript
  // Matrices
  'make-yail-matrix-multidim': {block: 'matrices_create_multidim', arity: 2},
  'yail-matrix-get-row': {block: 'matrices_get_row', arity: 2},
  'yail-matrix-get-column': {block: 'matrices_get_column', arity: 2},
  'yail-matrix-get-dims': {block: 'matrices_get_dims', arity: 1},
  'yail-matrix-get-cell': {block: 'matrices_get_cell', arity: 'variadic', fixedInputs: 1},
  'yail-matrix-set-cell!': {block: 'matrices_set_cell', arity: 'variadic', fixedInputs: 2},
  'yail-matrix-add': {block: 'matrices_add', arity: 'variadic'},
  'yail-matrix-subtract': {block: 'matrices_subtract', arity: 2},
  'yail-matrix-multiply': {block: 'matrices_multiply', arity: 'variadic'},
  'yail-matrix-power': {block: 'matrices_power', arity: 2},
  'yail-matrix-inverse': {block: 'matrices_operations', mode: 'INVERSE', arity: 1},
  'yail-matrix-transpose': {block: 'matrices_operations', mode: 'TRANSPOSE', arity: 1},
  'yail-matrix-rotate-left': {block: 'matrices_operations', mode: 'ROTATE_LEFT', arity: 1},
  'yail-matrix-rotate-right': {block: 'matrices_operations', mode: 'ROTATE_RIGHT', arity: 1},
  'yail-matrix?': {block: 'matrices_is_matrix', arity: 1},
```

**Why these work with existing converter logic:**
- `fixedInputs: 1` for `get-cell`: YAIL args are `(matrix, idx1, idx2, ...)`. After `domToMutation` with `items = args.length - 1`, block has inputs `[MATRIX, DIM0, DIM1, ...]`. `getInputNames_` discovers them from `block.inputList` via the `fixedInputs` path (lines 1596-1604).
- `fixedInputs: 2` for `set-cell!`: YAIL args are `(matrix, value, idx1, idx2, ...)`. After mutation with `items = args.length - 2`, block has `[MATRIX, VALUE, DIM0, DIM1, ...]`.
- `mode` for operations: `convertPrimitive_` sets `block.setFieldValue(info.mode, 'OP')` at line 1557-1559.

- [ ] **Step 2: Verify no syntax errors**

Run: `cd /home/diego/workplace-Kodular/appinventor-sources && node -c appinventor/blocklyeditor/src/ai/yail_to_blocks.js`
Expected: No output (syntax OK)

- [ ] **Step 3: Commit**

```bash
git add appinventor/blocklyeditor/src/ai/yail_to_blocks.js
git commit -m "feat(ai): add matrix primitives to YAIL-to-blocks converter"
```

---

### Task 2: Add custom converter for matrices_create

**Files:**
- Modify: `appinventor/blocklyeditor/src/ai/yail_to_blocks.js:1498` (inside `convertPrimitive_`, add special-case before the PRIMITIVE_MAP_ lookup)

`matrices_create` uses `FieldNumber` fields (`MATRIX_0_0`, `MATRIX_0_1`, etc.) instead of input connections. The standard `convertPrimitive_` can't handle this because it tries to connect args to value inputs. We need to intercept `make-yail-matrix` before the PRIMITIVE_MAP_ lookup and handle it with field-setting logic.

YAIL form: `(call-yail-primitive make-yail-matrix (*list-for-runtime* rows cols v00 v01 ... vNN) '(number number number...) "create a matrix")`

- [ ] **Step 1: Add make-yail-matrix special case in convertPrimitive_**

In `convertPrimitive_` (line 1498), after the `make-exact-yail-integer` / `make-exact-yail-real-number` special case (line 1507-1519) and before the `PRIMITIVE_MAP_` lookup (line 1521), add:

```javascript
  // make-yail-matrix uses FieldNumber fields, not input connections.
  // YAIL: (call-yail-primitive make-yail-matrix
  //          (*list-for-runtime* rows cols v00 v01 ... vNN)
  //          '(number ...) "create a matrix")
  if (primName === 'make-yail-matrix') {
    var args = [];
    if (els.length > 2 && AI.SExprParser.isForm(els[2], '*list-for-runtime*')) {
      var argList = els[2].elements;
      for (var a = 1; a < argList.length; a++) args.push(argList[a]);
    }
    var rows = (args.length > 0 && args[0].type === 'number') ? args[0].value : 2;
    var cols = (args.length > 1 && args[1].type === 'number') ? args[1].value : 2;
    var block = workspace.newBlock('matrices_create');
    var matrixValues = [];
    for (var r = 0; r < rows; r++) {
      matrixValues[r] = [];
      for (var c = 0; c < cols; c++) {
        var idx = 2 + r * cols + c;
        matrixValues[r][c] = (idx < args.length && args[idx].type === 'number')
            ? args[idx].value : 0;
      }
    }
    var mutation = document.createElement('mutation');
    mutation.setAttribute('rows', String(rows));
    mutation.setAttribute('cols', String(cols));
    mutation.setAttribute('matrix', JSON.stringify(matrixValues));
    block.domToMutation(mutation);
    block.initSvg();
    return block;
  }
```

- [ ] **Step 2: Verify no syntax errors**

Run: `cd /home/diego/workplace-Kodular/appinventor-sources && node -c appinventor/blocklyeditor/src/ai/yail_to_blocks.js`
Expected: No output (syntax OK)

- [ ] **Step 3: Commit**

```bash
git add appinventor/blocklyeditor/src/ai/yail_to_blocks.js
git commit -m "feat(ai): add custom converter for matrices_create block"
```

---

### Task 3: Add anonymous procedure and control primitives to converter

**Files:**
- Modify: `appinventor/blocklyeditor/src/ai/yail_to_blocks.js` (PRIMITIVE_MAP_ and convertPrimitive_)

This task handles:
1. Simple PRIMITIVE_MAP_ entries for `run-in-background`, `run-after-period`, `num-args-yail-procedure`, `create-yail-procedure-with-name`, `call-yail-procedure-input-list`
2. Custom handling for `create-yail-procedure` (lambda vs get-var disambiguation)
3. Custom handling for `call-yail-procedure` (variadic with PROCEDURE + ARG0..N, statement vs expression)

- [ ] **Step 1: Add simple primitive entries to PRIMITIVE_MAP_**

Add after the matrix entries:

```javascript
  // Control — new async blocks
  'run-in-background': {block: 'controls_run_in_background', arity: 2},
  'run-after-period': {block: 'controls_run_after_period', arity: 2},

  // Procedures — anonymous
  'num-args-yail-procedure': {block: 'procedures_numArgs', arity: 1},
  'create-yail-procedure-with-name': {block: 'procedures_getWithName', arity: 1},
  'call-yail-procedure-input-list': {block: 'procedures_callanonnoreturn_inputlist', arity: 2},
```

Note: `call-yail-procedure-input-list` maps to the no-return (statement) variant by default. The expression variant is handled by the `asExpression` check below.

- [ ] **Step 2: Add call-yail-procedure-input-list expression override in convertPrimitive_**

In `convertPrimitive_`, after the `make-yail-matrix` special case and before the `PRIMITIVE_MAP_` lookup, add:

```javascript
  // call-yail-procedure-input-list: statement vs expression variant
  if (primName === 'call-yail-procedure-input-list' && asExpression) {
    var args = [];
    if (els.length > 2 && AI.SExprParser.isForm(els[2], '*list-for-runtime*')) {
      var argList = els[2].elements;
      for (var a = 1; a < argList.length; a++) args.push(argList[a]);
    }
    var block = workspace.newBlock('procedures_callanonreturn_inputlist');
    block.initSvg();
    if (args.length > 0) {
      var procBlock = AI.YailToBlocks.convertExpression_(workspace, args[0]);
      if (procBlock && block.getInput('PROCEDURE')) {
        block.getInput('PROCEDURE').connection.connect(procBlock.outputConnection);
      }
    }
    if (args.length > 1) {
      var listBlock = AI.YailToBlocks.convertExpression_(workspace, args[1]);
      if (listBlock && block.getInput('INPUTLIST')) {
        block.getInput('INPUTLIST').connection.connect(listBlock.outputConnection);
      }
    }
    return block;
  }
```

- [ ] **Step 3: Add call-yail-procedure custom handler**

In `convertPrimitive_`, add after the input-list handler:

```javascript
  // call-yail-procedure: variadic — first arg is procedure, rest are call args.
  // Statement variant: procedures_callanonnoreturn (previousConnection)
  // Expression variant: procedures_callanonreturn (outputConnection)
  if (primName === 'call-yail-procedure') {
    var args = [];
    if (els.length > 2 && AI.SExprParser.isForm(els[2], '*list-for-runtime*')) {
      var argList = els[2].elements;
      for (var a = 1; a < argList.length; a++) args.push(argList[a]);
    }
    var callArgCount = args.length - 1;  // exclude procedure arg
    var blockType = asExpression
        ? 'procedures_callanonreturn'
        : 'procedures_callanonnoreturn';
    var block = workspace.newBlock(blockType);
    if (callArgCount > 0 && block.domToMutation) {
      var mutation = document.createElement('mutation');
      mutation.setAttribute('items', String(callArgCount));
      block.domToMutation(mutation);
    }
    block.initSvg();
    // Connect procedure expression
    if (args.length > 0) {
      var procBlock = AI.YailToBlocks.convertExpression_(workspace, args[0]);
      if (procBlock && block.getInput('PROCEDURE')) {
        block.getInput('PROCEDURE').connection.connect(procBlock.outputConnection);
      }
    }
    // Connect call arguments (ARG0, ARG1, ...)
    for (var i = 1; i < args.length; i++) {
      var argBlock = AI.YailToBlocks.convertExpression_(workspace, args[i]);
      var inputName = 'ARG' + (i - 1);
      if (argBlock && block.getInput(inputName)) {
        block.getInput(inputName).connection.connect(argBlock.outputConnection);
      }
    }
    return block;
  }
```

- [ ] **Step 4: Add create-yail-procedure custom handler**

In `convertPrimitive_`, add after the call-yail-procedure handler:

```javascript
  // create-yail-procedure: two cases based on argument form.
  // Case 1: (lambda ($p1 $p2) body) → anonymous procedure definition block
  // Case 2: (get-var p$name) → procedure-by-dropdown block
  if (primName === 'create-yail-procedure') {
    var args = [];
    if (els.length > 2 && AI.SExprParser.isForm(els[2], '*list-for-runtime*')) {
      var argList = els[2].elements;
      for (var a = 1; a < argList.length; a++) args.push(argList[a]);
    }
    if (args.length === 0) return null;
    var firstArg = args[0];

    // Case 2: (get-var p$name) → procedures_getWithDropdown
    if (AI.SExprParser.isForm(firstArg, 'get-var')) {
      var varName = firstArg.elements[1].name || '';
      var procName = varName.replace(/^p\$/, '');
      var block = workspace.newBlock('procedures_getWithDropdown');
      block.initSvg();
      if (block.getField('PROCNAME')) {
        block.setFieldValue(procName, 'PROCNAME');
      }
      return block;
    }

    // Case 1: (lambda ($p1 $p2 ...) body) → anonymous procedure def
    if (AI.SExprParser.isForm(firstArg, 'lambda')) {
      var lambdaEls = firstArg.elements;
      // lambdaEls[0] = 'lambda' symbol
      // lambdaEls[1] = parameter list (may be empty list)
      // lambdaEls[2] = body
      var paramNames = [];
      if (lambdaEls.length > 1 && lambdaEls[1].type === 'list') {
        for (var p = 0; p < lambdaEls[1].elements.length; p++) {
          var pName = lambdaEls[1].elements[p].name || '';
          paramNames.push(pName.replace(/^\$(?:param_)?/, ''));
        }
      }
      var bodyNode = lambdaEls.length > 2 ? lambdaEls[2] : null;

      // Heuristic: if body is a statement form (begin with multiple children,
      // set-var!, if with begin, etc.), use no-return; otherwise use return.
      var isStatement = false;
      if (bodyNode) {
        var bodyHead = AI.SExprParser.formHead(bodyNode);
        if (bodyHead === 'begin' || bodyHead === 'set-var!'
            || bodyHead === 'set-and-coerce-property!'
            || bodyHead === 'call-component-method'
            || bodyHead === 'set-lexical!'
            || bodyHead === 'while' || bodyHead === 'foreach'
            || bodyHead === 'forrange' || bodyHead === 'let') {
          isStatement = true;
        }
        // Also check: if with begin branches
        if (bodyHead === 'if' && bodyNode.elements.length > 2) {
          var branch = bodyNode.elements[2];
          if (AI.SExprParser.isForm(branch, 'begin')) {
            isStatement = true;
          }
        }
      }

      var blockType = isStatement
          ? 'procedures_defanonnoreturn'
          : 'procedures_defanonreturn';
      var block = workspace.newBlock(blockType);

      // Set up parameters via updateParams_
      if (paramNames.length > 0 && block.updateParams_) {
        block.updateParams_(paramNames);
      }
      block.initSvg();

      // Connect body
      if (bodyNode) {
        if (isStatement) {
          // Statement body → connect to STACK input
          var bodyBlock = AI.YailToBlocks.convertBeginToStatements_(
              workspace, bodyNode);
          if (bodyBlock && block.getInput('STACK')) {
            block.getInput('STACK').connection.connect(
                bodyBlock.previousConnection);
          }
        } else {
          // Expression body → connect to RETURN input
          var bodyBlock = AI.YailToBlocks.convertExpression_(
              workspace, bodyNode);
          if (bodyBlock && block.getInput('RETURN')) {
            block.getInput('RETURN').connection.connect(
                bodyBlock.outputConnection);
          }
        }
      }
      return block;
    }

    // Fallback: unknown argument form — treat as expression
    return AI.YailToBlocks.convertExpression_(workspace, firstArg);
  }
```

- [ ] **Step 5: Verify no syntax errors**

Run: `cd /home/diego/workplace-Kodular/appinventor-sources && node -c appinventor/blocklyeditor/src/ai/yail_to_blocks.js`
Expected: No output (syntax OK)

- [ ] **Step 6: Commit**

```bash
git add appinventor/blocklyeditor/src/ai/yail_to_blocks.js
git commit -m "feat(ai): add anonymous procedure and control block converters"
```

---

### Task 4: Update YAIL grammar reference — matrix primitives

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/resources/yail_grammar.md:491` (insert after Dictionary section, before Color Primitives)

- [ ] **Step 1: Add Matrix Primitives section**

Insert a new section after the Dictionary `walk ALL` constant (line 496) and before `#### Color Primitives` (line 499):

```markdown
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
```

- [ ] **Step 2: Add Matrix Argument Order Reference section**

In the `## Argument Order Reference` section (after Screen Primitives table, around line 1008), add:

```markdown
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
```

- [ ] **Step 3: Add Matrix examples**

Add at the end of the `## Real-World Examples` section:

```markdown
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
```

- [ ] **Step 4: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/server/aiagent/resources/yail_grammar.md
git commit -m "feat(ai): document matrix primitives in YAIL grammar"
```

---

### Task 5: Update YAIL grammar reference — anonymous procedures and control blocks

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/resources/yail_grammar.md`

- [ ] **Step 1: Add Anonymous Procedures section**

Insert a new `## Anonymous Procedures` section after `## Procedure Calls` (around line 748):

```markdown
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
```

- [ ] **Step 2: Add new control block primitives**

Insert in the `## Control Flow` section, after the `### Break` subsection (around line 688):

```markdown
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
```

- [ ] **Step 3: Add Anonymous Procedure argument order reference**

In the `## Argument Order Reference` section, add:

```markdown
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
```

- [ ] **Step 4: Add Anonymous Procedure examples**

Add to `## Real-World Examples`:

```markdown
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
```

- [ ] **Step 5: Add statement primitives caveat update**

In the **Statement primitives cannot be used as values** rule (line 17), add `yail-matrix-set-cell!` to the list of statement primitives:

Change the existing list to include: `yail-matrix-set-cell!`

Also add to the **Critical Notes** section (around line 1020), note 6:

```markdown
7. **`yail-matrix-set-cell!` is a STATEMENT**: Like list mutation primitives, `yail-matrix-set-cell!` mutates in place and cannot be nested in expressions. Its argument order is `(matrix, value, idx1, idx2, ...)` — the value comes before the dimension indices.

8. **Anonymous procedure YAIL is the same for statement and expression**: `call-yail-procedure` generates identical YAIL whether it returns a value or not. In statement context (inside `begin`, event handler body), it produces a statement block. In expression context (inside property set, argument to another call), it produces an expression block.
```

- [ ] **Step 6: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/server/aiagent/resources/yail_grammar.md
git commit -m "feat(ai): document anonymous procedures and control blocks in YAIL grammar"
```

---

### Task 6: Update App Inventor reference and few-shot examples

**Files:**
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/resources/appinventor_reference.md:18`
- Modify: `appinventor/appengine/src/com/google/appinventor/server/aiagent/resources/few_shot_examples.json`

- [ ] **Step 1: Add matrices to block categories list**

In `appinventor_reference.md` line 18, change:

```
  snap-together blocks. Blocks fall into several categories: event handlers,
  procedures (functions), variables, control flow, math, text, lists,
  dictionaries, colors, and component-specific getters/setters/method-calls.
```

to:

```
  snap-together blocks. Blocks fall into several categories: event handlers,
  procedures (functions), variables, control flow, math, matrices, text, lists,
  dictionaries, colors, and component-specific getters/setters/method-calls.
```

- [ ] **Step 2: Add matrix few-shot example**

In `few_shot_examples.json`, add before the closing `]`:

```json
  ,
  {
    "description": "Create a matrix and read a cell value",
    "user": "Create a 2x2 identity matrix and show the value at row 1, column 1 when Button1 is clicked",
    "assistant_text": "I'll create a 2x2 identity matrix as a global variable and add a click handler that displays the cell at (1,1).",
    "tool_calls": [
      {
        "tool": "write_block",
        "arguments": {
          "yail": "(def g$identity\n  (call-yail-primitive make-yail-matrix\n    (*list-for-runtime* 2 2 1 0 0 1)\n    '(number number number number number number)\n    \"create a matrix\"))"
        }
      },
      {
        "tool": "write_block",
        "arguments": {
          "yail": "(define-event Button1 Click ()\n  (set-this-form)\n  (set-and-coerce-property! 'Label1 'Text\n    (call-yail-primitive yail-matrix-get-cell\n      (*list-for-runtime* (get-var g$identity) 1 1)\n      '(matrix number number)\n      \"get matrix cell\")\n    'text))"
        }
      }
    ]
  },
  {
    "description": "Anonymous procedure with delayed execution",
    "user": "Make Button1 show 'Wait...' immediately and then show 'Done!' after 3 seconds",
    "assistant_text": "I'll set the label to 'Wait...' on click, then use an anonymous procedure with run-after-period to update it after 3 seconds.",
    "tool_calls": [
      {
        "tool": "write_block",
        "arguments": {
          "yail": "(define-event Button1 Click ()\n  (set-this-form)\n  (set-and-coerce-property! 'Label1 'Text \"Wait...\" 'text)\n  (call-yail-primitive run-after-period\n    (*list-for-runtime* 3000\n      (call-yail-primitive create-yail-procedure\n        (*list-for-runtime* (lambda ()\n          (set-and-coerce-property! 'Label1 'Text \"Done!\" 'text)))\n        '(any) \"create procedure\"))\n    '(any any) \"run after period\"))"
        }
      }
    ]
  }
```

- [ ] **Step 3: Verify JSON is valid**

Run: `cd /home/diego/workplace-Kodular/appinventor-sources && python3 -c "import json; json.load(open('appinventor/appengine/src/com/google/appinventor/server/aiagent/resources/few_shot_examples.json'))"`
Expected: No output (valid JSON)

- [ ] **Step 4: Commit**

```bash
git add appinventor/appengine/src/com/google/appinventor/server/aiagent/resources/appinventor_reference.md
git add appinventor/appengine/src/com/google/appinventor/server/aiagent/resources/few_shot_examples.json
git commit -m "feat(ai): add matrices to reference and new few-shot examples"
```

---

## Verification Checklist

After all tasks are complete, verify the full round-trip:

1. **Syntax check all JS**: `node -c appinventor/blocklyeditor/src/ai/yail_to_blocks.js`
2. **JSON validation**: `python3 -c "import json; json.load(open('appinventor/appengine/src/com/google/appinventor/server/aiagent/resources/few_shot_examples.json'))"`
3. **PRIMITIVE_MAP_ coverage**: Every new primitive mentioned in `yail_grammar.md` has either a PRIMITIVE_MAP_ entry or a custom handler in `convertPrimitive_`
4. **Statement vs expression**: `yail-matrix-set-cell!` returns `code` (no array wrapper) in the YAIL generator → it's a statement. The converter creates it via PRIMITIVE_MAP_ with the default `convertPrimitive_` path which handles this correctly.
5. **Argument order consistency**: Each new entry in the Argument Order Reference matches the exact args produced by the corresponding YAIL generator in `generators/yail/matrices.js` and `generators/yail/procedures.js`
