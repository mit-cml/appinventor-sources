# Design: `math_number_property` Block

## Summary

Add a new `math_number_property` block to the blockly editor's Math category. The block takes a single number input and a dropdown to check one of six properties: even, odd, prime, positive, negative, or whole (integer). Returns boolean.

## Motivation

App Inventor currently has no way to check mathematical properties of numbers. Users who need `isOdd`, `isPrime`, etc. must build them from scratch using modulo and comparison blocks. A single dropdown block covers the most common number property checks cleanly.

## Design

### Block Definition — `blocks/math.js`

New block `math_number_property` following the `math_single` pattern:

- **Input:** Single value input `NUM`, check type `number`
- **Output:** Boolean
- **Dropdown field** `OP` with options:
  - `EVEN` — "is even"
  - `ODD` — "is odd"
  - `PRIME` — "is prime"
  - `POSITIVE` — "is positive"
  - `NEGATIVE` — "is negative"
  - `WHOLE` — "is whole"
- **Color:** `Blockly.MATH_CATEGORY_HUE`
- **Category:** Math
- **helpUrl:** Dynamic per dropdown value (like `math_single`)
- **tooltip:** Dynamic per dropdown value
- **typeblock:** One entry per dropdown option

### YAIL Generator — `generators/yail/math.js`

New generator `math_number_property` following the `math_single` generator pattern (single-arg primitive call):

| Dropdown | YAIL primitive | Input type | Description string |
|----------|---------------|------------|-------------------|
| EVEN | `is-even?` | `number` | `"is even?"` |
| ODD | `is-odd?` | `number` | `"is odd?"` |
| PRIME | `is-prime?` | `number` | `"is prime?"` |
| POSITIVE | `is-positive?` | `number` | `"is positive?"` |
| NEGATIVE | `is-negative?` | `number` | `"is negative?"` |
| WHOLE | `is-whole?` | `number` | `"is whole?"` |

### Runtime — `runtime.scm` (Android + iOS)

Six new Scheme procedures. All coerce input and return explicit `#t`/`#f` (matching the `is-number?` pattern).

```scheme
(define (is-even? n)
  (if (even? (inexact->exact (truncate n))) #t #f))

(define (is-odd? n)
  (if (odd? (inexact->exact (truncate n))) #t #f))

(define (is-prime? n)
  (let ((n (inexact->exact (truncate n))))
    (cond ((<= n 1) #f)
          ((<= n 3) #t)
          ((or (zero? (modulo n 2)) (zero? (modulo n 3))) #f)
          (else (let loop ((i 5))
                  (cond ((> (* i i) n) #t)
                        ((or (zero? (modulo n i)) (zero? (modulo n (+ i 2)))) #f)
                        (else (loop (+ i 6)))))))))

(define (is-positive? n)
  (if (> n 0) #t #f))

(define (is-negative? n)
  (if (< n 0) #t #f))

(define (is-whole? n)
  (if (= n (truncate n)) #t #f))
```

These are added to both:
- `appinventor/buildserver/src/com/google/appinventor/buildserver/resources/runtime.scm`
- `appinventor/components-ios/src/runtime.scm`

### i18n — `msg/ai_blockly/messages.json`

18 new keys following existing naming conventions:

- `LANG_MATH_NUMBER_PROPERTY_EVEN` / `_ODD` / `_PRIME` / `_POSITIVE` / `_NEGATIVE` / `_WHOLE` — dropdown labels
- `LANG_MATH_NUMBER_PROPERTY_TOOLTIP_EVEN` / ... — tooltips
- `LANG_MATH_NUMBER_PROPERTY_HELPURL_EVEN` / ... — help URLs (pointing to `/reference/blocks/math.html#numberproperty`)

### Toolbox — `toolkit_beginner.json`

Add `{"type": "math_number_property"}` after the `math_is_a_number` entry in the Math category. Not added to `toolkit_intermediate.json`.

### Versioning

No versioning upgrade entry needed. This is a new block type, not a change to an existing one. Projects without it simply won't contain it.

## Files Changed

| File | Change |
|------|--------|
| `blocklyeditor/src/blocks/math.js` | New block definition (~50 lines) |
| `blocklyeditor/src/generators/yail/math.js` | New YAIL generator (~25 lines) |
| `buildserver/.../runtime.scm` | 6 new Scheme procedures (~25 lines) |
| `components-ios/src/runtime.scm` | Same 6 procedures (~25 lines) |
| `blocklyeditor/src/msg/ai_blockly/messages.json` | 18 new i18n keys |
| `appengine/.../toolkit_beginner.json` | 1 new toolbox entry |

## Out of Scope

- No changes to `math_is_a_number` (type-checking stays separate)
- No new blocks per property (single dropdown block covers all six)
- No intermediate toolkit changes
