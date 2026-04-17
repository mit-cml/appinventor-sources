# AI Block Width Optimization Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Automatically flip deeply nested inline blocks to external inputs when AI-generated blocks exceed viewport width.

**Architecture:** A post-render optimization pass in `yail_to_blocks.js` that measures each new top-level block, walks the tree inside-out to find the deepest eligible inline block, flips it to external, re-renders, and repeats until the block fits or 3 flips are exhausted. Inserted between the existing render and positioning steps.

**Tech Stack:** JavaScript (Closure-style), Blockly API (`getHeightWidth`, `setInputsInline`, `inputList`, `getMetrics`)

**Spec:** `docs/superpowers/specs/2026-04-04-ai-block-width-optimization-design.md`

---

## File Structure

| File | Action | Responsibility |
|------|--------|----------------|
| `appinventor/blocklyeditor/src/ai/yail_to_blocks.js` | Modify (lines 118-120) | Add `optimizeBlockWidths_` function and call it after `triggerQueuedRenders()`, before positioning |

Single-file change. The optimization is a private helper within the existing module.

---

### Task 1: Add the `optimizeBlockWidths_` function

**Files:**
- Modify: `appinventor/blocklyeditor/src/ai/yail_to_blocks.js` (insert after line 202, before `deleteBlock`)

- [ ] **Step 1: Add the `findDeepestEligibleBlock_` helper**

This function walks a block tree depth-first, tracking value-input depth only (not statement/next connections), and returns the deepest block that is eligible for flipping.

```javascript
/**
 * Find the deepest block eligible for inline-to-external flipping.
 * Eligibility: (1) currently inline, (2) at least 2 value inputs have
 * blocks connected, (3) at least one connected child is non-leaf.
 * Depth counts only value-input connections (not statement/next).
 *
 * @param {!Blockly.Block} block The block to search from.
 * @param {number} valueDepth Current depth along value-input chain.
 * @return {?{block: !Blockly.Block, depth: number, width: number}}
 * @private
 */
AI.YailToBlocks.findDeepestEligibleBlock_ = function(block, valueDepth) {
  var best = null;

  // Recurse into value-input children (incrementing depth) and
  // statement-input children (same depth — they nest vertically).
  if (block.inputList) {
    for (var i = 0; i < block.inputList.length; i++) {
      var input = block.inputList[i];
      var child = input.connection && input.connection.targetBlock();
      if (!child) continue;
      var childDepth = (input.type === Blockly.INPUT_VALUE)
          ? valueDepth + 1
          : valueDepth;
      // Also recurse through statement stacks (next connections).
      var current = child;
      while (current) {
        var result = AI.YailToBlocks.findDeepestEligibleBlock_(
            current, childDepth);
        if (result && (!best || result.depth > best.depth ||
            (result.depth === best.depth &&
             result.width > best.width))) {
          best = result;
        }
        current = current.getNextBlock();
      }
    }
  }

  // Check if THIS block is eligible.
  if (block.getInputsInline()) {
    var connectedValueCount = 0;
    var hasNonLeafChild = false;
    if (block.inputList) {
      for (var i = 0; i < block.inputList.length; i++) {
        var input = block.inputList[i];
        if (input.type === Blockly.INPUT_VALUE) {
          var child = input.connection && input.connection.targetBlock();
          if (child) {
            connectedValueCount++;
            if (child.getChildren(false).length > 0) {
              hasNonLeafChild = true;
            }
          }
        }
      }
    }
    if (connectedValueCount >= 2 && hasNonLeafChild) {
      var hw = block.getHeightWidth();
      if (!best || valueDepth > best.depth ||
          (valueDepth === best.depth && hw.width > best.width)) {
        best = {block: block, depth: valueDepth, width: hw.width};
      }
    }
  }

  return best;
};
```

- [ ] **Step 2: Add the `optimizeBlockWidths_` function**

This function runs the main optimization loop per top-level block.

```javascript
/**
 * Optimize block widths by flipping deep inline blocks to external inputs.
 * Called after initial render and before positioning. Only affects blocks
 * in the provided array (AI-generated blocks from the current conversion).
 *
 * @param {!Blockly.WorkspaceSvg} workspace The workspace.
 * @param {!Array<!Blockly.Block>} blocks The newly created top-level blocks.
 * @private
 */
AI.YailToBlocks.optimizeBlockWidths_ = function(workspace, blocks) {
  var metrics = workspace.getMetrics();
  var maxWidth = metrics.viewWidth / workspace.scale;
  var MAX_FLIPS = 3;

  for (var i = 0; i < blocks.length; i++) {
    var block = blocks[i];
    var flips = 0;
    while (flips < MAX_FLIPS) {
      var hw = block.getHeightWidth();
      if (hw.width <= maxWidth) break;
      var candidate = AI.YailToBlocks.findDeepestEligibleBlock_(block, 0);
      if (!candidate) break;
      candidate.block.setInputsInline(false);
      candidate.block.queueRender();
      Blockly.renderManagement.triggerQueuedRenders();
      flips++;
    }
  }
};
```

- [ ] **Step 3: Call `optimizeBlockWidths_` in `convert()`**

Insert the call between line 118 (`triggerQueuedRenders()`) and line 120 (the positioning section comment).

In `AI.YailToBlocks.convert`, after:
```javascript
    Blockly.renderManagement.triggerQueuedRenders();
```

And before:
```javascript
    // Position blocks — dimensions are now accurate after rendering.
```

Add:
```javascript

    // Optimize block widths — flip deep inline blocks to external inputs
    // when the rendered block exceeds viewport width.
    AI.YailToBlocks.optimizeBlockWidths_(workspace, createdBlocks);
```

- [ ] **Step 4: Verify the build compiles**

Run:
```bash
cd appinventor && ant -f appengine/build.xml AiClientLib
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: Commit**

```bash
git add appinventor/blocklyeditor/src/ai/yail_to_blocks.js
git commit -m "feat(ai): auto-flip inline blocks to external when exceeding viewport width

Add optimizeBlockWidths_ pass to yail_to_blocks.js that runs after
initial render and before positioning. Walks the block tree inside-out,
finds the deepest eligible inline block (2+ connected value inputs,
at least one non-leaf child), and flips it to external. Repeats up
to 3 times per top-level block until width fits viewport."
```
