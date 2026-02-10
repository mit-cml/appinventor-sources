# YAIL Grammar Reference

YAIL (Young Android Intermediate Language) is an S-expression-based language derived from Scheme.
Use the `write_block` tool to create or replace blocks, providing complete YAIL S-expressions.
Use the `delete_block` tool to remove blocks, providing the YAIL head tokens as identifier.

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
- Initial value is any expression (number, string, list, etc.)

### Procedure (no return value)
```scheme
(def (p$procedureName $param1 $param2 ...)
  (set-this-form)
  <body statements>)
```

### Procedure (with return value)
```scheme
(def-return (p$procedureName $param1 $param2 ...)
  (set-this-form)
  <body expression>)
```
- Use `def-return` instead of `def` when the procedure returns a value
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

##### Math on List (statistics)
These operate on a single list argument:
| Primitive | Description |
|-----------|-------------|
| `avg` | Average |
| `minl` | Minimum of list |
| `maxl` | Maximum of list |
| `gm` | Geometric mean |
| `std-dev` | Standard deviation |
| `std-err` | Standard error |
| `mode` | Mode |

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

Short-circuit logic uses special forms:
```scheme
(and-delayed <expr1> (begin <expr2>))  ;; AND
(or-delayed <expr1> (begin <expr2>))   ;; OR
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
| Primitive | Description |
|-----------|-------------|
| `make-yail-list` | Create list (variadic) |
| `yail-list-get-item` | Get item at index |
| `yail-list-set-item!` | Set item at index |
| `yail-list-length` | List length |
| `yail-list-add-to-list!` | Add items to list (variadic) |
| `yail-list-remove-item!` | Remove item at index |
| `yail-list-insert-item!` | Insert item at index |
| `yail-list-append!` | Append two lists |
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
| `yail-list-sort-comparator` | Sort with comparator |
| `yail-list-but-first` | All except first |
| `yail-list-but-last` | All except last |
| `yail-list-first-element` | First item |
| `yail-list-rest` | Rest of list |
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
| Primitive | Description |
|-----------|-------------|
| `make-yail-dictionary` | Create dictionary (variadic pairs) |
| `make-dictionary-pair` | Create a key-value pair |
| `yail-dictionary-lookup` | Look up key (with default) |
| `yail-dictionary-set-pair` | Set key-value |
| `yail-dictionary-delete-pair` | Delete key |
| `yail-dictionary-recursive-lookup` | Nested lookup |
| `yail-dictionary-recursive-set` | Nested set |
| `yail-dictionary-get-keys` | Get all keys |
| `yail-dictionary-get-values` | Get all values |
| `yail-dictionary-is-key-in` | Check if key exists |
| `yail-dictionary-length` | Dictionary size |
| `yail-dictionary-alist-to-dict` | List of pairs to dict |
| `yail-dictionary-dict-to-alist` | Dict to list of pairs |
| `yail-dictionary-combine-dicts` | Merge dictionaries |
| `yail-dictionary-copy` | Copy dictionary |
| `yail-dictionary-walk` | Walk tree by key path |
| `yail-dictionary?` | Is it a dictionary? |

##### Dictionary Walk ALL Constant
When using `yail-dictionary-walk`, you can use the ALL wildcard to match all keys at a level:
```scheme
(static-field com.google.appinventor.components.runtime.util.YailDictionary 'ALL)
```

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

### Enum Dropdown (protect-enum)
```scheme
(protect-enum (static-field com.example.ClassName "ENUM_NAME") concreteValue)
```
Wraps an enum value for use in the REPL. The `static-field` is the authoritative value; the `concreteValue` is a fallback.

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

### Evaluate But Ignore (execute expression, discard result)
```scheme
(begin <expression> "ignored")
```

### Null Value
```scheme
(get-var *the-null-value*)
```

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
  (set-this-form)
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
