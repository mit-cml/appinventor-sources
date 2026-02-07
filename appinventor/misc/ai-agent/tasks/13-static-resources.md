# Task 13: Static Resources

## Status: Not Started

## Plan Reference
Step 3 from plan.md (resource files section)

## Files to Create
- `server/aiagent/resources/pseudocode_grammar.md` — Full grammar spec (~2K tokens) covering ~150 user-facing block types. Defines the pseudocode syntax that BlocksPseudocodeGenerator outputs and PseudocodeParser accepts. Included in the LLM system prompt.
- `server/aiagent/resources/appinventor_reference.md` — Static reference guide (~4K tokens) with 4 sections: platform description, role/instructions, operation output schemas, format conventions. Forms Layer 1 of the system prompt.
- `server/aiagent/resources/component_catalog.json` — Compact listing of ~100 user-facing components (~3K tokens). Excludes 7 INTERNAL-category components. Each entry: name, category, visibility, description, top 4-6 properties/events/methods. Forms Layer 2 of the system prompt.
- `server/aiagent/resources/few_shot_examples.json` — 3-4 complete worked examples (~3K tokens) showing user message → operations list. Forms Layer 5 of the system prompt.

All paths relative to `appinventor/appengine/src/com/google/appinventor/`.

## Files to Modify
None

## Dependencies
None — standalone task

## Acceptance Criteria

### pseudocode_grammar.md
- [ ] Covers all statement forms: component operations (instance + generic), variable operations, procedure calls, control flow, screen navigation, list/dict mutation
- [ ] Covers all expression forms: literals, variables, component expressions, procedure calls, arithmetic, comparisons, logic, math/trig functions, text operations, list operations, dict operations, color operations, component references, helper blocks, ternary/do-return
- [ ] Maps each pseudocode form to its Blockly block type
- [ ] Notes on mutator blocks (variable input counts, mode-based mutations)
- [ ] Excludes 13 mutator-internal blocks, 5 convenience aliases, 2 provider-specific helpers

### appinventor_reference.md
- [ ] Section 1: Platform description (what App Inventor is, how apps are structured)
- [ ] Section 2: Role/instructions (rules for screen scoping, component rules, scope limitations)
- [ ] Section 3: Operation output schemas (all tool definitions with parameters)
- [ ] Section 4: Format conventions (color format, booleans, sizes, alignments, container types)

### component_catalog.json
- [ ] Valid JSON array of component objects
- [ ] ~100 entries (excludes FusiontablesControl, GameClient, MediaStore, PhoneStatus, Twitter, Voting, YandexTranslate)
- [ ] Each entry has: name, cat, vis, desc, props (top 4-6), events (top), methods (top)
- [ ] Stays within ~3K tokens budget

### few_shot_examples.json
- [ ] Valid JSON array of example objects
- [ ] 3-4 examples covering: simple component + event handler, multi-operation (counter app), single property change
- [ ] Each example has: user (string), operations (array of {type, payload})

## Progress Log
