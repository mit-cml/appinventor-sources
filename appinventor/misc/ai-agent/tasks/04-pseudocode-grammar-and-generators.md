# Task 04: Pseudocode Grammar & Generators

## Status: Not Started

## Plan Reference
Steps 3, 4 from plan.md (pseudocode parts)

## Files to Create
- `server/aiagent/BlocksPseudocodeGenerator.java` — BKY XML → pseudocode (read path). Converts Blockly XML into human-readable pseudocode for the LLM.
- `server/aiagent/PseudocodeParser.java` — Pseudocode → AST → Blockly XML (write path). Parses LLM-generated pseudocode and converts to valid Blockly XML via BlocksXmlGenerator.

All paths relative to `appinventor/appengine/src/com/google/appinventor/`.

## Files to Modify
None

## Dependencies
- Task 05: BlocksXmlGenerator (PseudocodeParser uses it to produce final XML)
- Task 13: pseudocode_grammar.md resource (defines the grammar both classes implement)

## Acceptance Criteria

### BlocksPseudocodeGenerator (read path)
- [ ] Parses Blockly XML and outputs pseudocode matching the grammar in pseudocode_grammar.md
- [ ] Handles event handlers (instance and generic): `when Button1.Click:` / `when any Button.Click(component, notAlreadyHandled):`
- [ ] Handles method calls (instance and generic): `call Button1.SetText(...)` / `call Button.SetText of <expr>(...)`
- [ ] Handles property set/get (instance and generic)
- [ ] Handles global and local variables
- [ ] Handles control flow: if/else if/else, for each, for range, while, break
- [ ] Handles math/logic/text/list/dict operations with correct pseudocode syntax
- [ ] Handles procedures (defnoreturn and defreturn)
- [ ] Handles color constants, helper blocks (dropdown, screen_names, assets)
- [ ] Handles sequential blocks via `<next>` chain (joined by newlines)
- [ ] Handles legacy block types transparently
- [ ] Produces structured output: `== Global Variables ==`, `== Event Handlers ==`, `== Procedures ==`
- [ ] ~70% token reduction vs raw BKY XML

### PseudocodeParser (write path)
- [ ] Parses pseudocode grammar into AST
- [ ] Converts AST to valid Blockly XML using BlocksXmlGenerator
- [ ] Validates component/method/property references against component database
- [ ] Reports clear error messages for malformed pseudocode
- [ ] Symmetric with BlocksPseudocodeGenerator (round-trip: XML → pseudocode → XML produces equivalent blocks)
- [ ] Handles all ~150 user-facing block types defined in the grammar
- [ ] Handles convenience aliases transparently (e.g., math_cos → math_trig with COS)

## Progress Log
