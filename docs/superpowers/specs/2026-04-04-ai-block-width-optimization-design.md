# AI Block Width Optimization via Inside-Out Input Flipping

**Date:** 2026-04-04
**Status:** Approved
**Component:** `blocklyeditor/src/ai/yail_to_blocks.js`

---

## Problem

When the AI generates blocks via `yail_to_blocks.js`, deeply nested or multi-input inline blocks can extend far beyond the viewport width, forcing horizontal scrolling. Blockly has no automatic mechanism to address this — the only existing option is the user manually right-clicking and toggling "External Inputs."

Blockly's built-in `getInputsInline()` heuristic is purely structural (2+ value inputs = inline) and does not consider rendered dimensions. No existing Blockly project or plugin implements automatic width-based input mode switching.

## Solution

A post-render optimization pass in `yail_to_blocks.js` that measures newly created top-level blocks, identifies blocks that are too wide, and selectively flips inline inputs to external — working from the deepest nesting level outward.

## Scope

**Only AI-generated blocks** created during `AI.YailToBlocks.convert()`. Pre-existing user blocks are never touched.

## Algorithm

### Trigger

After `Blockly.renderManagement.triggerQueuedRenders()` completes and before the positioning/overlap-avoidance logic runs.

### Threshold

Workspace viewport width in workspace coordinates: `workspace.getMetrics().viewWidth / workspace.scale`. This division is necessary because `getHeightWidth()` returns dimensions in workspace coordinates (unscaled), while `viewWidth` is in pixels. The existing positioning code in `yail_to_blocks.js` already applies this same scaling factor.

### Main Loop

For each new top-level block:

```
flips = 0
while block.getHeightWidth().width > viewportWidth AND flips < 3:
    candidate = findDeepestEligibleBlock(block)
    if no candidate: break
    candidate.setInputsInline(false)
    candidate.queueRender()
    Blockly.renderManagement.triggerQueuedRenders()
    flips++
```

### Block Eligibility

A block is a flip candidate if **all three conditions** hold:

1. **Currently inline** — `getInputsInline()` returns `true` (whether explicitly set or via Blockly's default heuristic).
2. **At least 2 value inputs have blocks connected to them** — a block with 3 value inputs where only 1 is connected gains nothing from flipping. Both connected inputs must contribute to width for the flip to matter.
3. **At least one connected child is non-leaf** — the connected block has its own children. This prevents flipping simple expressions like `a + b` where both inputs are just number/text literals.

### Selection Strategy

Among all eligible blocks in the tree, select the **deepest** one. Depth is measured by counting **value input connections** only — statement inputs and next connections contribute to vertical nesting, not horizontal width. A block 5 levels deep via statement stacking but only 1 level deep via value inputs has value-depth 1.

If multiple candidates share the same value-depth, select the **widest** (largest `getHeightWidth().width`).

### Cap

Maximum **3 flips** per top-level block. Each flip requires a re-render. If 3 flips cannot bring the block within viewport width, it is genuinely complex and the user can adjust manually.

## Design Rationale

### Why inside-out (not outside-in)

Flipping an inner block to external makes it taller but narrower, reducing the width contribution to all ancestor blocks. The parent may become narrow enough to stay inline without intervention. This:

- Preserves the outermost block structure (better high-level readability)
- Produces minimal visual disruption (surgical fix at the point of complexity)
- Avoids over-correction (a 900px block on an 800px viewport only needs a small reduction)

### Why viewport width as threshold

- Adapts to the user's screen and window size
- Matches the natural expectation: "I shouldn't need to scroll horizontally to see a single block"
- No magic pixel constants to tune

### Why the non-leaf child requirement

A block like `a + b` where both inputs are number literals is narrow and reads naturally inline. Flipping it to external would waste vertical space for no benefit. The non-leaf check ensures we only flip blocks whose children contribute meaningful width.

### Why not a depth-only heuristic

A pure "flip if nesting depth >= N" approach would false-positive on deep but narrow chains like `not(not(not(x)))` — single-input blocks nested deeply have no width problem. The width measurement ensures intervention only when there is an actual visual problem.

### Why not a single-pass approach

A single pass that flips all deep blocks at once risks over-correction. The iterative approach stops as soon as the block fits, producing the minimal number of flips needed.

## Integration Point

In `yail_to_blocks.js`, the current rendering/positioning flow:

```
create blocks -> initSvg/queueRender -> triggerQueuedRenders -> position/overlap avoidance
```

Becomes:

```
create blocks -> initSvg/queueRender -> triggerQueuedRenders -> optimizeWidths() -> position/overlap avoidance
```

The positioning and overlap-avoidance logic already calls `getHeightWidth()` on rendered blocks, so it naturally picks up post-flip dimensions without any changes.

## Block-Specific Behavior

Some App Inventor blocks override `setInputsInline()` with custom logic. Notably, `logic_operation` (and/or) rearranges field rows and moves dropdown fields between inputs when the inline mode changes. This is expected to work correctly — the override delegates to `Blockly.BlockSvg.prototype.setInputsInline` at the end — but the implementation should not assume `setInputsInline` is a trivial property set.

After calling `setInputsInline(false)`, the implementation calls `queueRender()` + `triggerQueuedRenders()`. This should also trigger ancestor re-renders through Blockly's event system (since events are enabled at the integration point). If testing reveals stale parent dimensions, the fallback is `candidate.getRootBlock().queueRender()`.

## User Override

Users can right-click any block and toggle between "Inline Inputs" and "External Inputs." This is standard Blockly behavior and requires no changes. The optimization is a sensible default, not a locked-in decision.

## Performance

- **Worst case per top-level block:** 3 additional SVG render passes
- **Tree walk:** O(n) where n = number of blocks in the tree
- **Practical impact:** AI batches rarely produce blocks with more than ~20 nodes. Blockly SVG renders are fast (path recomputation, no DOM layout). The cost is negligible compared to the LLM round-trip that generated the blocks.

## Files Modified

| File | Change |
|------|--------|
| `blocklyeditor/src/ai/yail_to_blocks.js` | Add `optimizeBlockWidths()` function and call it after initial render, before positioning |

## Testing

- Verify blocks exceeding viewport width get their deepest eligible inline block flipped to external
- Verify blocks within viewport width are not modified
- Verify simple inline blocks (e.g., `a + b` with literal inputs) are never flipped
- Verify max 3 flips cap is respected
- Verify user can right-click and toggle back to inline after optimization
- Verify positioning/overlap logic works correctly with post-flip dimensions
