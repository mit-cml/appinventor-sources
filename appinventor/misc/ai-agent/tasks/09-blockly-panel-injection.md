# Task 09: Blockly Panel Injection

## Status: Not Started

## Plan Reference
Step 7 from plan.md

## Files to Create
None

## Files to Modify
- `client/editor/blocks/BlocklyPanel.java` — Add three JSNI methods: injectBlocksXml(), deleteBlockByTypeAndId(), replaceBlock()
- `client/editor/blocks/BlocksEditor.java` — Add public wrapper methods: injectBlocksXml(), deleteBlock(), replaceBlock()

All paths relative to `appinventor/appengine/src/com/google/appinventor/`.

## Dependencies
None — standalone task

## Acceptance Criteria

### BlocklyPanel JSNI methods
- [ ] injectBlocksXml(String xmlString): appends blocks to existing workspace using Blockly.Xml.domToWorkspace
  - [ ] Saves/restores Blockly main workspace (multi-workspace safety, mirrors doLoadBlocksContent pattern)
  - [ ] Calls block.verify() on new blocks after injection
  - [ ] Uses try/finally for workspace restore
- [ ] deleteBlockByTypeAndId(String blockType, String instanceName, String identifier):
  - [ ] Iterates workspace.getTopBlocks(false)
  - [ ] For component_event: matches mutation instance_name + event_name
  - [ ] For global_declaration: matches NAME field value
  - [ ] For procedures_defnoreturn/defreturn: matches NAME field value
  - [ ] Calls block.dispose(true) with animation
  - [ ] Returns boolean (true if found and deleted)
- [ ] replaceBlock(String blockType, String instanceName, String identifier, String newBlockXml):
  - [ ] Calls deleteBlockByTypeAndId (no-op if not found)
  - [ ] Calls injectBlocksXml with new XML

### BlocksEditor wrapper methods
- [ ] injectBlocksXml(String xmlString) delegates to blocklyPanel
- [ ] deleteBlock(String blockType, String instanceName, String identifier) delegates to blocklyPanel
- [ ] replaceBlock(String blockType, String instanceName, String identifier, String newBlockXml) delegates to blocklyPanel

### Integration points
- [ ] Methods are accessible from AIOperationExecutor
- [ ] Injection does not interfere with existing workspace content
- [ ] No orphaned blocks after injection/deletion

## Progress Log
