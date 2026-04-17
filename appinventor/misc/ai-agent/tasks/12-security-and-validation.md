# Task 12: Security & Validation

## Status: Not Started

## Plan Reference
Step 8 from plan.md

## Files to Create
- `server/aiagent/LLMResponseParser.java` — Structural validation of raw LLM tool calls (Stage 1)
- `server/aiagent/AIOperationValidator.java` — Semantic validation + mode enforcement (Stage 2)

All paths relative to `appinventor/appengine/src/com/google/appinventor/`.

## Files to Modify
None

## Dependencies
- Task 01: AIOperation types for validation targets

## Acceptance Criteria

### LLMResponseParser (Stage 1: Structural validation)
- [ ] parseToolCalls(List<RawToolCall>): returns ParseResult(operations, errors)
- [ ] Checks: unknown tool names, malformed JSON arguments, missing required parameters
- [ ] Type coercion for known fields (properties → JSON object, parameters → JSON array)
- [ ] KNOWN_TOOLS set matches all defined tool names
- [ ] Per-tool required field validation (e.g., add_component requires component_type + name)

### AIOperationValidator (Stage 2: Semantic validation)
- [ ] validateForMode(ops, mode): enforces mode restrictions
  - [ ] Advisor: rejects all WRITE_OPS
  - [ ] ScreenEditor: rejects PROJECT_LEVEL_OPS (switch_screen, create_screen, delete_screen, set_project_prop)
  - [ ] ProjectEditor: allows all
- [ ] validateOperations(ops, componentDb): semantic checks
  - [ ] Invalid component types → error
  - [ ] Invalid property names for target component → error
  - [ ] Maximum operation count per response (50) → error
- [ ] Protected properties enforcement:
  - [ ] SET_PROPERTY targeting AIAgentMode → rejected
  - [ ] SET_PROPERTY targeting $-prefixed properties → rejected
  - [ ] SET_PROPERTY targeting Uuid → rejected
  - [ ] SET_PROJECT_PROP with property AIAgentMode → rejected
- [ ] Returns ValidationResult(accepted, errors)

### Error feedback
- [ ] buildValidationErrorFeedback(errors): produces structured error message for LLM retry
- [ ] Error messages are clear enough for LLM self-correction

## Progress Log
