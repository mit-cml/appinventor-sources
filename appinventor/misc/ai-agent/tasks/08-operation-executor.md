# Task 08: Operation Executor

## Status: Not Started

## Plan Reference
Step 6 from plan.md

## Files to Create
- `client/editor/youngandroid/AIOperationExecutor.java` — Applies AI-generated operations to the editor

Path relative to `appinventor/appengine/src/com/google/appinventor/`.

## Files to Modify
None

## Dependencies
- Task 01: AIOperation types and enum
- Task 09: BlocklyPanel JSNI methods (injectBlocksXml, deleteBlockByTypeAndId, replaceBlock)

## Acceptance Criteria

### Phase-based execution
- [ ] Groups operations into 5 phases: project-level, designer adds, block adds, block deletes, designer deletes
- [ ] Switches to correct view (Designer/Blocks) between phases
- [ ] Phase 1 (project ops) is async — chained via callbacks
- [ ] Phases 2-5 are synchronous within a deferred command
- [ ] Uses deferred scheduling + screensLocked check for view switching (mirrors DesignToolbar.doSwitchScreen pattern)

### Project-level operations (Phase 1)
- [ ] SWITCH_SCREEN: calls designToolbar.switchToScreen()
- [ ] CREATE_SCREEN: follows AddFormAction/AddFormCommand flow (addFile RPC → create nodes → add screen → switch)
- [ ] DELETE_SCREEN: follows RemoveFormAction/DeleteFileCommand flow (close editors → deleteFile RPC → remove nodes)
- [ ] SET_PROJECT_PROP: sets property on Screen1's MockForm via changeProperty()
- [ ] SET_PROJECT_PROP whitelist enforced (AppName, Title, Icon, VersionCode, VersionName, etc.)

### Component operations (Phases 2 & 5)
- [ ] ADD_COMPONENT: uses DesignerEditor.createMockComponent(), handles parent container, insertAfter positioning
- [ ] SET_PROPERTY: uses MockComponent.changeProperty()
- [ ] RENAME_COMPONENT: uses MockComponent.rename()
- [ ] DELETE_COMPONENT: uses MockComponent.delete()

### Block operations (Phases 3 & 4)
- [ ] SET_EVENT_HANDLER: uses blocksEditor.replaceBlock("component_event", instanceName, eventName, xml)
- [ ] DELETE_EVENT_HANDLER: uses blocksEditor.deleteBlock("component_event", instanceName, eventName)
- [ ] SET_VARIABLE: uses blocksEditor.replaceBlock("global_declaration", null, varName, xml)
- [ ] DELETE_VARIABLE: uses blocksEditor.deleteBlock("global_declaration", null, varName)
- [ ] SET_PROCEDURE: uses blocksEditor.replaceBlock("procedures_def*", null, procName, xml)
- [ ] DELETE_PROCEDURE: uses blocksEditor.deleteBlock("procedures_def*", null, procName)

### Pre-validation
- [ ] Validates EACH operation before executing (not all upfront, because earlier ops change state)
- [ ] Component type exists in component database
- [ ] Component name uniqueness (add) / existence (set/delete/rename)
- [ ] Parent container exists, is a container, and accepts component type
- [ ] Property exists on component (via comp.getProperties().getProperty())
- [ ] Screen exists (switch/delete) or doesn't exist (create)
- [ ] Cannot delete Screen1, cannot delete Form root component
- [ ] Editor loadComplete check before touching components

### Error handling
- [ ] ExecutionResult: succeeded, failed, errorMessage, skipped lists
- [ ] Stop-on-error: first failure halts remaining operations
- [ ] No rollback — partially applied changes remain
- [ ] Triggers auto-save via EditorManager.scheduleAutoSave() for non-property operations

### Editor access
- [ ] Obtains YaFormEditor/YaBlocksEditor via DesignToolbar.DesignProject.screens (NOT ProjectEditor)

## Progress Log
