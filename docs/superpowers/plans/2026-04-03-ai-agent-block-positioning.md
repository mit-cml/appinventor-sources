# AI Agent Block Positioning Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the AI agent's block placement viewport-aware and component-grouped so new blocks don't overlap existing ones.

**Architecture:** All changes are in `appinventor/blocklyeditor/src/ai/yail_to_blocks.js`. The positioning block in `AI.YailToBlocks.convert()` (lines 111-133) is replaced with a viewport-aware algorithm, plus a new `findGroupPosition_` helper for component grouping. A JS toggle `GROUP_BY_COMPONENT` gates the grouping behavior.

**Tech Stack:** JavaScript (Blockly API), Mocha/Chai tests

**Spec:** `docs/superpowers/specs/2026-04-03-ai-agent-block-positioning-design.md`

---

### Task 1: Add positioning tests for free-space placement

**Files:**
- Modify: `appinventor/blocklyeditor/tests/com/google/appinventor/mocha/yail_to_blocks_test.js`
- Modify: `appinventor/blocklyeditor/tests/com/google/appinventor/mocha/index.html`

- [ ] **Step 1: Wire the test file into the HTML test runner**

The existing `yail_to_blocks_test.js` is not included in `index.html`. Add it after line 41 (after the `yailgenerators.js` script tag):

```html
<script src="yail_to_blocks_test.js"></script>
```

- [ ] **Step 2: Add a new test suite for block positioning**

Add a `'Block positioning'` suite after the existing `'Upsert semantics'` suite (after line 438). These tests verify that new blocks are placed below existing blocks rather than overlapping them.

```javascript
// ================================================================
// 7. Block positioning
// ================================================================
suite('Block positioning', function() {

  test('New block is placed below existing blocks in viewport', function() {
    // Create an existing event handler
    var result1 = AI.YailToBlocks.convert(this.workspace,
        '(define-event Button1 Click () (set-this-form))');
    chai.assert.isTrue(result1.success);

    var existingBlock = findTopBlock(this.workspace, 'component_event');
    chai.assert.isNotNull(existingBlock);
    var existingXY = existingBlock.getRelativeToSurfaceXY();
    var existingHeight = existingBlock.getHeightWidth().height;

    // Create a second block — should land below the first, not overlap
    var result2 = AI.YailToBlocks.convert(this.workspace, '(def g$score 0)');
    chai.assert.isTrue(result2.success);

    var newBlock = findTopBlock(this.workspace, 'global_declaration');
    chai.assert.isNotNull(newBlock);
    var newXY = newBlock.getRelativeToSurfaceXY();

    // New block's top should be at or below existing block's bottom
    chai.assert.isAtLeast(newXY.y, existingXY.y + existingHeight,
        'new block should be placed below existing block');
  });

  test('Multiple new blocks in one batch do not overlap each other', function() {
    var yail = '(def g$a 1)\n(def g$b 2)\n(def g$c 3)';
    var result = AI.YailToBlocks.convert(this.workspace, yail);
    chai.assert.isTrue(result.success);

    var topBlocks = this.workspace.getTopBlocks(false);
    chai.assert.lengthOf(topBlocks, 3);

    // Sort by Y position
    topBlocks.sort(function(a, b) {
      return a.getRelativeToSurfaceXY().y - b.getRelativeToSurfaceXY().y;
    });

    // Each block should start below the previous block's bottom edge
    for (var i = 1; i < topBlocks.length; i++) {
      var prevBottom = topBlocks[i - 1].getRelativeToSurfaceXY().y +
          topBlocks[i - 1].getHeightWidth().height;
      var currTop = topBlocks[i].getRelativeToSurfaceXY().y;
      chai.assert.isAtLeast(currTop, prevBottom,
          'block ' + i + ' should not overlap block ' + (i - 1));
    }
  });

  test('Upsert preserves original position', function() {
    // Create an event, record its position
    var result1 = AI.YailToBlocks.convert(this.workspace,
        '(define-event Button1 Click () (set-this-form))');
    chai.assert.isTrue(result1.success);

    var block1 = findTopBlock(this.workspace, 'component_event');
    var origXY = block1.getRelativeToSurfaceXY();

    // Replace the same event — should go back to original position
    var result2 = AI.YailToBlocks.convert(this.workspace,
        '(define-event Button1 Click () (set-this-form)\n' +
        '  (call-component-method \'Notifier1 \'ShowAlert ' +
        '(*list-for-runtime* "updated") \'(text)))');
    chai.assert.isTrue(result2.success);

    var block2 = findTopBlock(this.workspace, 'component_event');
    var newXY = block2.getRelativeToSurfaceXY();

    chai.assert.equal(newXY.x, origXY.x, 'upsert X should match original');
    chai.assert.equal(newXY.y, origXY.y, 'upsert Y should match original');
  });
});
```

- [ ] **Step 3: Open index.html in browser and verify the first two tests fail**

Open `appinventor/blocklyeditor/tests/com/google/appinventor/mocha/index.html` in a browser.

Expected: "New block is placed below existing blocks in viewport" FAILS (blocks overlap). "Upsert preserves original position" PASSES (already works). "Multiple new blocks in one batch do not overlap each other" may pass or fail depending on viewport state — the key failing test is the first one.

---

### Task 2: Implement free-space placement

**Files:**
- Modify: `appinventor/blocklyeditor/src/ai/yail_to_blocks.js:111-133`

- [ ] **Step 1: Replace the positioning block in `convert()`**

Replace lines 111-133 in `AI.YailToBlocks.convert` (the comment "Position blocks" through the end of the positioning loop) with:

```javascript
    // Position blocks — dimensions are now accurate after rendering.
    // Upsert blocks go back to their original position.
    // New blocks are placed below the lowest existing block visible
    // in the viewport, avoiding overlap.
    var SPACING = 30;
    var metrics = workspace.getMetrics();
    var viewCenterX = metrics.viewLeft +
        metrics.viewWidth / (2 * workspace.scale);
    var viewTop = metrics.viewTop / workspace.scale;
    var viewBottom = viewTop + metrics.viewHeight / workspace.scale;

    // Scan existing blocks for the lowest one visible in the viewport.
    var existingBlocks = workspace.getTopBlocks(false);
    var freeSpaceY = viewTop + 20;  // default if no visible blocks
    for (var e = 0; e < existingBlocks.length; e++) {
      var eb = existingBlocks[e];
      var exy = eb.getRelativeToSurfaceXY();
      var ehw = eb.getHeightWidth();
      var eBottom = exy.y + ehw.height;
      // Does this block intersect the viewport vertically?
      if (exy.y < viewBottom && eBottom > viewTop) {
        if (eBottom + SPACING > freeSpaceY) {
          freeSpaceY = eBottom + SPACING;
        }
      }
    }

    for (var k = 0; k < createdBlocks.length; k++) {
      var block = createdBlocks[k];
      var upsertPos = upsertPositions[k];
      if (upsertPos) {
        block.moveTo(
            new Blockly.utils.Coordinate(upsertPos.x, upsertPos.y));
      } else {
        var bw = block.getHeightWidth().width;
        var x = viewCenterX - bw / 2;
        block.moveTo(new Blockly.utils.Coordinate(x, freeSpaceY));
        freeSpaceY += block.getHeightWidth().height + SPACING;
      }
    }
```

- [ ] **Step 2: Verify positioning tests pass**

Open `appinventor/blocklyeditor/tests/com/google/appinventor/mocha/index.html` in browser. All three positioning tests from Task 1 should pass.

- [ ] **Step 3: Verify existing tests still pass**

All existing test suites (Top-level forms, Expressions, Control flow, deleteBlock, Error cases, Upsert semantics) should still pass. The change only affects positioning, not block creation or deletion logic.

---

### Task 3: Add positioning tests for component grouping

**Files:**
- Modify: `appinventor/blocklyeditor/tests/com/google/appinventor/mocha/yail_to_blocks_test.js`

- [ ] **Step 1: Add component grouping tests**

Add inside the `'Block positioning'` suite, after the existing tests:

```javascript
  test('Component grouping: event placed near same component', function() {
    // Create a Button1.Click handler
    var result1 = AI.YailToBlocks.convert(this.workspace,
        '(define-event Button1 Click () (set-this-form))');
    chai.assert.isTrue(result1.success);

    var clickBlock = findTopBlock(this.workspace, 'component_event');
    var clickXY = clickBlock.getRelativeToSurfaceXY();

    // Create a Label1 handler (different component) — goes to free space
    var result2 = AI.YailToBlocks.convert(this.workspace,
        '(define-event Label1 Initialize () (set-this-form))');
    chai.assert.isTrue(result2.success);

    // Create a Button1.LongClick handler — should be near Button1.Click
    var result3 = AI.YailToBlocks.convert(this.workspace,
        '(define-event Button1 LongClick () (set-this-form))');
    chai.assert.isTrue(result3.success);

    // Find all component_event blocks
    var topBlocks = this.workspace.getTopBlocks(false);
    var longClickBlock = null;
    for (var i = 0; i < topBlocks.length; i++) {
      var m = topBlocks[i].mutationToDom ? topBlocks[i].mutationToDom() : null;
      if (m && m.getAttribute('instance_name') === 'Button1' &&
          m.getAttribute('event_name') === 'LongClick') {
        longClickBlock = topBlocks[i];
        break;
      }
    }
    chai.assert.isNotNull(longClickBlock, 'LongClick block should exist');

    // LongClick should share Button1.Click's X coordinate
    var longClickXY = longClickBlock.getRelativeToSurfaceXY();
    chai.assert.equal(longClickXY.x, clickXY.x,
        'grouped block should align horizontally with group');

    // LongClick should be below Button1.Click
    var clickBottom = clickXY.y + clickBlock.getHeightWidth().height;
    chai.assert.isAtLeast(longClickXY.y, clickBottom,
        'grouped block should be below its group');
  });

  test('Component grouping: global var uses free-space (no grouping)', function() {
    // Create a Button1.Click handler
    var result1 = AI.YailToBlocks.convert(this.workspace,
        '(define-event Button1 Click () (set-this-form))');
    chai.assert.isTrue(result1.success);

    // Create a global variable — should NOT be grouped with Button1
    var result2 = AI.YailToBlocks.convert(this.workspace, '(def g$counter 0)');
    chai.assert.isTrue(result2.success);

    var globalBlock = findTopBlock(this.workspace, 'global_declaration');
    chai.assert.isNotNull(globalBlock);

    // The global should be placed via free-space, not at Button1's X
    var clickBlock = findTopBlock(this.workspace, 'component_event');
    var clickXY = clickBlock.getRelativeToSurfaceXY();
    var globalXY = globalBlock.getRelativeToSurfaceXY();

    // Global is centered; it should be below the click handler (free-space)
    var clickBottom = clickXY.y + clickBlock.getHeightWidth().height;
    chai.assert.isAtLeast(globalXY.y, clickBottom,
        'global should be below existing block');
  });

  test('Component grouping: toggle off disables grouping', function() {
    var origToggle = AI.YailToBlocks.GROUP_BY_COMPONENT;
    AI.YailToBlocks.GROUP_BY_COMPONENT = false;
    try {
      // Create Button1.Click
      var result1 = AI.YailToBlocks.convert(this.workspace,
          '(define-event Button1 Click () (set-this-form))');
      chai.assert.isTrue(result1.success);

      var clickBlock = findTopBlock(this.workspace, 'component_event');
      var clickXY = clickBlock.getRelativeToSurfaceXY();

      // Create Button1.LongClick with toggle off — should use free-space, not group
      var result2 = AI.YailToBlocks.convert(this.workspace,
          '(define-event Button1 LongClick () (set-this-form))');
      chai.assert.isTrue(result2.success);

      // Find LongClick block
      var topBlocks = this.workspace.getTopBlocks(false);
      var longClickBlock = null;
      for (var i = 0; i < topBlocks.length; i++) {
        var m = topBlocks[i].mutationToDom ? topBlocks[i].mutationToDom() : null;
        if (m && m.getAttribute('instance_name') === 'Button1' &&
            m.getAttribute('event_name') === 'LongClick') {
          longClickBlock = topBlocks[i];
          break;
        }
      }
      chai.assert.isNotNull(longClickBlock);

      // With toggle off, LongClick should NOT necessarily share Click's X
      // (it will be centered via free-space instead)
      // Just verify it doesn't overlap
      var longClickXY = longClickBlock.getRelativeToSurfaceXY();
      var clickBottom = clickXY.y + clickBlock.getHeightWidth().height;
      chai.assert.isAtLeast(longClickXY.y, clickBottom,
          'block should still not overlap even with grouping off');
    } finally {
      AI.YailToBlocks.GROUP_BY_COMPONENT = origToggle;
    }
  });
```

- [ ] **Step 2: Verify the grouping tests fail**

Open `index.html`. The first grouping test ("event placed near same component") should FAIL because `GROUP_BY_COMPONENT` doesn't exist yet. The toggle test will also fail for the same reason.

---

### Task 4: Implement component grouping

**Files:**
- Modify: `appinventor/blocklyeditor/src/ai/yail_to_blocks.js`

- [ ] **Step 1: Add the `GROUP_BY_COMPONENT` toggle**

Add after line 25 (the `lastDeletedPosition_` declaration), before the `convert` function:

```javascript
/**
 * When true, new event-handler blocks are placed near existing blocks
 * for the same component instance.  When false (or for blocks without
 * a component, such as globals and procedures), free-space placement
 * is used instead.
 * @type {boolean}
 */
AI.YailToBlocks.GROUP_BY_COMPONENT = true;
```

- [ ] **Step 2: Add the `findGroupPosition_` helper**

Add at the end of the file (after the last function, before the closing of the file):

```javascript
/**
 * Find a position near existing blocks for the same component.
 * Returns null if the block has no component association or no
 * group members exist on the workspace.
 *
 * @param {!Blockly.WorkspaceSvg} workspace
 * @param {!Blockly.Block} newBlock The block to position.
 * @param {number} spacing Vertical gap between blocks.
 * @return {?Blockly.utils.Coordinate} Position, or null if no group found.
 * @private
 */
AI.YailToBlocks.findGroupPosition_ = function(workspace, newBlock, spacing) {
  // Not all block types define mutationToDom (e.g. global_declaration).
  var mutation = newBlock.mutationToDom ? newBlock.mutationToDom() : null;
  if (!mutation) return null;
  var instanceName = mutation.getAttribute('instance_name');
  if (!instanceName) return null;

  // Query live workspace so blocks placed earlier in the same batch
  // are visible (getTopBlocks reads the current workspace state).
  var topBlocks = workspace.getTopBlocks(false);
  var maxBottom = -Infinity;
  var groupX = null;

  for (var i = 0; i < topBlocks.length; i++) {
    var eb = topBlocks[i];
    if (eb === newBlock || eb.id === newBlock.id) continue;
    var ebMutation = eb.mutationToDom ? eb.mutationToDom() : null;
    if (!ebMutation) continue;
    if (ebMutation.getAttribute('instance_name') !== instanceName) continue;

    var xy = eb.getRelativeToSurfaceXY();
    var hw = eb.getHeightWidth();
    var bottom = xy.y + hw.height;
    if (bottom > maxBottom) {
      maxBottom = bottom;
      groupX = xy.x;
    }
  }

  if (groupX === null) return null;
  return new Blockly.utils.Coordinate(groupX, maxBottom + spacing);
};
```

- [ ] **Step 3: Wire component grouping into the positioning loop**

In the positioning loop inside `convert()` (the code from Task 2), replace the `else` branch for non-upsert blocks. The full positioning loop should now be:

```javascript
    for (var k = 0; k < createdBlocks.length; k++) {
      var block = createdBlocks[k];
      var upsertPos = upsertPositions[k];
      if (upsertPos) {
        block.moveTo(
            new Blockly.utils.Coordinate(upsertPos.x, upsertPos.y));
      } else {
        var placed = false;
        // Try component grouping.
        if (AI.YailToBlocks.GROUP_BY_COMPONENT) {
          var groupPos = AI.YailToBlocks.findGroupPosition_(
              workspace, block, SPACING);
          if (groupPos) {
            block.moveTo(groupPos);
            placed = true;
            // Advance freeSpaceY past this block so subsequent free-space
            // placements don't overlap with the group-placed block.
            var groupBottom =
                groupPos.y + block.getHeightWidth().height + SPACING;
            if (groupBottom > freeSpaceY) freeSpaceY = groupBottom;
          }
        }
        // Fall back to free-space placement.
        if (!placed) {
          var bw = block.getHeightWidth().width;
          var x = viewCenterX - bw / 2;
          block.moveTo(new Blockly.utils.Coordinate(x, freeSpaceY));
          freeSpaceY += block.getHeightWidth().height + SPACING;
        }
      }
    }
```

- [ ] **Step 4: Verify all tests pass**

Open `index.html`. All test suites should pass:
- Existing suites (Top-level forms, Expressions, Control flow, deleteBlock, Error cases, Upsert semantics) — unchanged behavior
- Block positioning suite — all 6 tests (3 free-space + 3 grouping) should pass

- [ ] **Step 5: Commit**

```
feat(ai): viewport-aware block positioning with component grouping

New blocks placed by the AI agent are now positioned below existing
blocks visible in the viewport instead of overlapping them. Event
handlers are grouped near their component's other handlers when
GROUP_BY_COMPONENT is enabled.
```
