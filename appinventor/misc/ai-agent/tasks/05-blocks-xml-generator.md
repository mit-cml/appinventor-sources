# Task 05: Blocks XML Generator

## Status: Not Started

## Plan Reference
Step 4 from plan.md

## Files to Create
- `server/aiagent/BlocksXmlGenerator.java` — AST → valid Blockly XML. Template-based XML generation validated against simple_components.json.

Path relative to `appinventor/appengine/src/com/google/appinventor/`.

## Files to Modify
None

## Dependencies
None — standalone (uses component DB data but no task dependencies)

## Acceptance Criteria
- [ ] Generates valid Blockly XML for all supported block types:
  - [ ] component_event blocks (instance and generic)
  - [ ] component_method blocks (instance and generic, with COMPONENT input socket for generic)
  - [ ] component_set_get blocks (instance and generic)
  - [ ] component_component_block and component_all_component_block
  - [ ] global_declaration blocks
  - [ ] procedures_defnoreturn / procedures_defreturn (with mutator for variable params)
  - [ ] controls_if with `<mutation elseif="N" else="0|1">`
  - [ ] controls_forRange, controls_forEach, controls_for_each_dict, controls_while
  - [ ] math_number, text, logic_boolean literals
  - [ ] math_add, text_join, lists_create_with (with mutator for variable input counts)
  - [ ] lists_add_items (with mutator for variable item counts)
  - [ ] logic_operation (with mutator for variable operand counts)
  - [ ] dictionaries_create_with (with mutator for variable pair counts)
  - [ ] math_bitwise (with mutator for variable operands + op attribute)
  - [ ] text_contains, text_split (mode-based mutations)
  - [ ] helpers_dropdown, helpers_screen_names, helpers_assets
  - [ ] local_declaration_statement / local_declaration_expression (with localname mutator)
  - [ ] All color constant blocks
  - [ ] Screen navigation blocks
  - [ ] List/dict mutation statement blocks
- [ ] Correct `<mutation>` XML for all 27 mutator block types (see plan for groupings)
- [ ] Validates parameter counts/names against simple_components.json
- [ ] Each block type maps to a generation method returning an XML string fragment
- [ ] Generated XML passes Blockly.Xml.domToWorkspace without errors

## Progress Log
