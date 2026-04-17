# AI Agent Block Positioning Design

**Date:** 2026-04-03
**Branch:** ai-agent
**Status:** Approved

## Problem

When the AI agent creates blocks via `write_block`, new blocks are centered horizontally in the viewport and stacked vertically starting at the viewport top. This positioning ignores all existing blocks on the workspace, causing new blocks to overlap with them. Replaced blocks (upserts) correctly preserve their original position, but brand-new blocks have no awareness of what's already there.

## Goals

1. **Avoid overlap** — new blocks should never land on top of existing blocks.
2. **Viewport-aware placement** — blocks should appear where the user is currently looking, not at arbitrary absolute coordinates.
3. **Component grouping** — new event handlers should be placed near the same component's existing handlers when possible.

## Design

All changes are in a single file: `appinventor/blocklyeditor/src/ai/yail_to_blocks.js`. No server-side changes, no RPC changes, no LLM context changes.

Two complementary behaviors, layered:

- **Free-space placement (always active):** Viewport-aware algorithm that avoids existing blocks.
- **Component grouping (toggle-gated):** Derives the component name from the YAIL being created and places the block near that component's existing handlers. Falls back to free-space placement when no group is found.

A JS-level constant `AI.YailToBlocks.GROUP_BY_COMPONENT = true` controls whether component grouping is active.

---

## Free-Space Placement

### Location

`yail_to_blocks.js`, the positioning block after rendering (currently lines 111-133 in `AI.YailToBlocks.convert`).

### Current Behavior

```javascript
var SPACING = 30;
var viewCenterX = metrics.viewLeft + metrics.viewWidth / (2 * workspace.scale);
var viewTop = metrics.viewTop / workspace.scale;
var nextY = viewTop + 20;
// stack new blocks at (viewCenterX, nextY), ignoring existing blocks
```

### New Algorithm

Computed once before the positioning loop, reusing the existing `SPACING = 30` constant:

1. Get viewport bounds from `workspace.getMetrics()`:
   - `viewTop`, `viewBottom` (= `viewTop + viewHeight / scale`)
   - `viewCenterX` (existing calculation)
2. Get all existing top-level blocks via `workspace.getTopBlocks(false)`.
3. Find which existing blocks **intersect the viewport vertically** — a block intersects if its top (`xy.y`) is above `viewBottom` and its bottom (`xy.y + height`) is below `viewTop`.
4. **If there are visible blocks:** set `nextY` to the lowest visible block's bottom edge (`max(xy.y + height)`) plus `SPACING`.
5. **If no blocks are visible** (user scrolled to empty space): use `viewTop + 20` (current behavior).
6. Horizontal position: keep `viewCenterX - blockWidth / 2`. Horizontal overlap is not checked — App Inventor blocks are typically stacked vertically, and centering means all blocks share the same X band. This matches how the existing `arrangeBlocks` vertical layout works.
7. Within a batch, stack vertically as today (`nextY += height + SPACING`).

Upsert positioning is **unchanged** — it always returns to the original position via `lastDeletedPosition_`.

### Rationale

The user implicitly controls placement by scrolling: scroll to empty space and new blocks appear there; stay near existing blocks and the AI stacks below them.

---

## Component Grouping

### Toggle

```javascript
// yail_to_blocks.js, near the top
AI.YailToBlocks.GROUP_BY_COMPONENT = true;
```

When `false`, all new blocks use free-space placement.

### How It Works

The component name is **already extracted during YAIL conversion** — `convertEvent_` reads it at line 601 (`var componentName = els[1].name || String(els[1].value)`) and sets it as `mutation.instance_name` on the created block. The same applies to `convertGenericEvent_` (component type at line 693).

After all blocks are rendered (line 109), the positioning loop already iterates `createdBlocks`. For each new (non-upsert) block:

1. **Extract component name** from the block's mutation: `block.mutationToDom().getAttribute('instance_name')`. This is non-empty for event handlers and empty/absent for globals and procedures.
2. **If component name is non-empty and toggle is on:**
   - Scan `workspace.getTopBlocks(false)` for blocks with the same `instance_name` in their mutation (excluding the current block itself).
   - If group members are found:
     - Find the lowest one (`max(xy.y + height)`).
     - Use the group's X position (`xy.x` of the lowest member) for horizontal alignment.
     - Place at `(groupX, lowestBottom + SPACING)`.
   - If no group members found: fall back to free-space placement.
3. **If component name is empty or toggle is off:** use free-space placement.

### Why This Works Without LLM Involvement

The YAIL `(define-event Button1 Click ...)` inherently encodes the component name. The Blockly converter already parses it. There's no need for the LLM to tell us what component a block belongs to — we can derive it from the YAIL itself. This eliminates:

- `group_with` parameter on the `write_block` tool schema
- `blockLayout` field on `AIAgentRequest`, `ContextParams`
- Server-side threading through `AIAgentEngine`, `AIAgentServiceImpl`, `AIContextBuilder.buildContextMessages()`
- `ScreenModule` formatting changes
- `AIBlockOperations` / `BlocksEditor` / `BlocklyPanel` signature changes
- LLM token cost for block layout context

---

## Implementation Details

### Changes to `AI.YailToBlocks.convert`

The positioning block (lines 111-133) is replaced. Pseudocode:

```javascript
var SPACING = 30;  // existing constant
var metrics = workspace.getMetrics();
var viewCenterX = metrics.viewLeft + metrics.viewWidth / (2 * workspace.scale);
var viewTop = metrics.viewTop / workspace.scale;
var viewBottom = viewTop + metrics.viewHeight / workspace.scale;

// Compute free-space Y: below the lowest block visible in the viewport
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

// Position each created block
for (var k = 0; k < createdBlocks.length; k++) {
  var block = createdBlocks[k];
  var upsertPos = upsertPositions[k];
  if (upsertPos) {
    // Upsert: restore original position
    block.moveTo(new Blockly.utils.Coordinate(upsertPos.x, upsertPos.y));
  } else {
    var placed = false;
    // Try component grouping
    if (AI.YailToBlocks.GROUP_BY_COMPONENT) {
      var groupPos = AI.YailToBlocks.findGroupPosition_(
          workspace, block, SPACING);
      if (groupPos) {
        block.moveTo(groupPos);
        placed = true;
        // Advance freeSpaceY past this block so subsequent free-space
        // placements don't overlap with the group-placed block.
        var groupBottom = groupPos.y + block.getHeightWidth().height + SPACING;
        if (groupBottom > freeSpaceY) freeSpaceY = groupBottom;
      }
    }
    // Fall back to free-space placement
    if (!placed) {
      var bw = block.getHeightWidth().width;
      var x = viewCenterX - bw / 2;
      block.moveTo(new Blockly.utils.Coordinate(x, freeSpaceY));
      freeSpaceY += block.getHeightWidth().height + SPACING;
    }
  }
}
```

### New helper: `AI.YailToBlocks.findGroupPosition_`

```javascript
/**
 * Find a position near existing blocks for the same component.
 * @param {!Blockly.Workspace} workspace
 * @param {!Blockly.Block} newBlock The block to position
 * @param {number} spacing Vertical gap between blocks
 * @return {?Blockly.utils.Coordinate} Position, or null if no group found
 * @private
 */
AI.YailToBlocks.findGroupPosition_ = function(
    workspace, newBlock, spacing) {
  // Guard: not all block types define mutationToDom (e.g. global_declaration).
  var mutation = newBlock.mutationToDom ? newBlock.mutationToDom() : null;
  if (!mutation) return null;
  var instanceName = mutation.getAttribute('instance_name');
  if (!instanceName) return null;

  // Query live workspace so blocks placed earlier in the same batch are visible.
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

### Batch-internal visibility

Blocks created earlier in the same batch must be visible to later blocks' group scans. `findGroupPosition_` calls `workspace.getTopBlocks(false)` fresh each time (not a cached snapshot), so blocks added during `convertTopLevel_` are included. Since `moveTo` is synchronous, a block positioned in iteration `k` has its final coordinates visible to iteration `k+1`.

The free-space `freeSpaceY` variable is computed once from a pre-loop viewport scan, then advanced incrementally as blocks are placed — both by free-space and group placement. This avoids re-scanning for the common case while keeping the variable consistent.

---

## File Changed

| File | Change |
|------|--------|
| `blocklyeditor/src/ai/yail_to_blocks.js` | Replace positioning block in `convert()`, add `GROUP_BY_COMPONENT` toggle, add `findGroupPosition_` helper |

## Files NOT Changed

No other files need modification. The entire feature is contained within the Blockly editor's YAIL-to-blocks converter.

## Edge Cases

- **Global variables and procedures** have no `instance_name` — component grouping does not apply; they use free-space placement.
- **Generic events** (`define-generic-event`) have a `component_type` but an empty `instance_name` — they use free-space placement. (Grouping by type could be a future enhancement.)
- **Component with no existing blocks yet:** `findGroupPosition_` returns null, falls back to free-space placement.
- **Toggle is off:** all blocks use free-space placement regardless of component.
- **Multiple new blocks in one batch for the same component:** WRITE_BLOCK operations execute sequentially in Phase 3. The positioning loop iterates `createdBlocks` in order and calls `moveTo` synchronously, so block `k+1`'s group scan sees block `k`'s final position. The second block lands below the first, not on top of it.
- **Group target is off-screen:** the block is placed near the group regardless — component grouping overrides viewport awareness. This is intentional; keeping a component's handlers together is more useful than viewport proximity.
- **All existing blocks are off-screen (user scrolled to empty space):** no blocks intersect the viewport, so `freeSpaceY` defaults to `viewTop + 20`. New blocks appear where the user is looking.
- **Upsert (replacing existing block):** unchanged — always returns to the original position.
